package com.github.creme332.controller.canvas;

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import javax.swing.JComponent;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;

import com.github.creme332.controller.canvas.drawing.DrawCircle;
import com.github.creme332.controller.canvas.drawing.AbstractDrawer;
import com.github.creme332.controller.canvas.drawing.DrawEllipse;
import com.github.creme332.controller.canvas.drawing.DrawIrregularPolygon;
import com.github.creme332.controller.canvas.drawing.DrawLine;
import com.github.creme332.controller.canvas.drawing.DrawRegularPolygon;
import com.github.creme332.controller.canvas.transform.Translator;
import com.github.creme332.model.AppState;
import com.github.creme332.model.CanvasModel;
import com.github.creme332.model.Mode;
import com.github.creme332.model.ShapeManager;
import com.github.creme332.model.ShapeWrapper;
import com.github.creme332.view.Canvas;

/**
 * Main controller for canvas view.
 */
public class CanvasController implements PropertyChangeListener {
    private Canvas canvas;

    /**
     * Used to store coordinate where mouse drag started. This is used to calculate
     * translation of canvas.
     */
    private Point mouseDragStart;
    private AppState app;
    private CanvasModel model;

    private List<AbstractDrawer> drawControllers = new ArrayList<>();

    public CanvasController(AppState app, Canvas canvas) {
        this.app = app;
        this.canvas = canvas;
        this.model = app.getCanvasModel();

        // listen to model
        model.addPropertyChangeListener(this);
        model.getShapeManager().addPropertyChangeListener(this);
        app.addPropertyChangeListener(this);

        // initialize drawing controllers
        drawControllers.add(new DrawLine(app, canvas));
        drawControllers.add(new DrawCircle(app, canvas));
        drawControllers.add(new DrawEllipse(app, canvas));
        drawControllers.add(new DrawRegularPolygon(app, canvas));
        drawControllers.add(new DrawIrregularPolygon(app, canvas));

        // initialize other canvas sub-controllers
        new Translator(app, canvas);

        // when canvas is resized, update dimensions and reset zoom
        canvas.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                model.setCanvasDimension(new Dimension(canvas.getWidth(), canvas.getHeight()));
                model.toStandardView();
            }
        });

        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent arg0) {
                ShapeManager manager = model.getShapeManager();

                // check if a shape was being dragged previously
                if (app.getMode() == Mode.MOVE_CANVAS && model.getSelectedShape() > -1) {
                    // edit previous shape with shape preview

                    if (manager.getShapePreview() != null) {
                        manager.editShape(model.getSelectedShape(), manager.getShapePreview());
                        manager.setShapePreview(null);
                        model.setSelectedShape(-1);
                        canvas.repaint();
                    }
                }
            }
        });

        canvas.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                // display coordinates of pixel where cursor is
                Point2D polySpaceMousePosition = model.toPolySpace(e.getPoint());
                model.setUserMousePosition(polySpaceMousePosition);
                canvas.repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                handleMouseDragged(e);
            }
        });

        // control canvas zoom with mouse wheel
        canvas.addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                model.updateCanvasZoom(e.getWheelRotation() != 1);
            }
        });

        initializeKeyBindings();
    }

    private void initializeKeyBindings() {
        // Export canvas
        canvas.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_DOWN_MASK), "exportCanvas");
        canvas.getActionMap().put("exportCanvas", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleCanvasExport();
            }
        });

        // Zoom in
        canvas.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, java.awt.event.InputEvent.CTRL_DOWN_MASK), "zoomIn");
        canvas.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, java.awt.event.InputEvent.CTRL_DOWN_MASK), "zoomIn");
        canvas.getActionMap().put("zoomIn", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.updateCanvasZoom(true);
                canvas.repaint();
            }
        });

        // Zoom out
        canvas.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, java.awt.event.InputEvent.CTRL_DOWN_MASK), "zoomOut");
        canvas.getActionMap().put("zoomOut", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.updateCanvasZoom(false);
                canvas.repaint();
            }
        });

        // Zoom 100%
        canvas.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_0, java.awt.event.InputEvent.CTRL_DOWN_MASK), "zoomReset");
        canvas.getActionMap().put("zoomReset", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.toStandardView();
                canvas.repaint();
            }
        });
    }

    /**
     * Drags canvas based on translation from start of drag and current mouse
     * position
     * 
     * @param destination current mouse position in user space
     */
    private void dragCanvas(Point destination) {
        // calculate translation
        int deltaX = destination.x - mouseDragStart.x;
        int deltaY = destination.y - mouseDragStart.y;

        // save end position of drag
        mouseDragStart = destination;

        // translate canvas origin
        model.setYZero(model.getYZero() + deltaY);
        model.setXZero(model.getXZero() + deltaX);
        canvas.repaint();
    }

    /**
     * Applies a translation on a shape.
     * 
     * @param destination Current mouse position
     * @param shapeIndex  Index of shape in as given in shapes array from
     *                    ShapeManager
     */
    private void dragShape(final Point destination) {
        final int shapeIndex = model.getSelectedShape();

        ShapeWrapper newShape = model.getShapeManager().getShapes().get(shapeIndex);

        Point2D polyspaceMousePosition = model.toPolySpace(destination);
        Point2D start = newShape.getPlottedPoints().get(0);

        int deltaX = (int) (polyspaceMousePosition.getX() - start.getX());
        int deltaY = (int) (polyspaceMousePosition.getY() - start.getY());

        Polygon oldPolygon = newShape.toPolygon();
        Polygon newPolygon = new Polygon();

        for (int i = 0; i < oldPolygon.npoints; i++) {
            newPolygon.addPoint(deltaX + oldPolygon.xpoints[i], deltaY + oldPolygon.ypoints[i]);
        }

        // update plotted points
        for (int i = 0; i < newShape.getPlottedPoints().size(); i++) {
            Point2D currentPoint = newShape.getPlottedPoints().get(i);
            Point2D translatedPoint = new Point2D.Double(currentPoint.getX() + deltaX, currentPoint.getY() + deltaY);

            newShape.getPlottedPoints().set(i, translatedPoint);
        }
        newShape.setShape(newPolygon);

        // update shape preview on screen
        model.getShapeManager().setShapePreview(newShape);
        canvas.repaint();
    }

    private void handleMouseDragged(MouseEvent e) {
        if (mouseDragStart == null) {
            mouseDragStart = e.getPoint();
            return;
        }

        if (app.getMode() == Mode.MOVE_GRAPHICS_VIEW) {
            dragCanvas(e.getPoint());
            return;
        }

        if (app.getMode() == Mode.MOVE_CANVAS) {
            if (model.getSelectedShape() > -1) {
                dragShape(e.getPoint());
            } else {
                dragCanvas(e.getPoint());
            }
        }
    }

    /**
     * 
     * @param polyspaceMousePosition Coordinate of point lying inside shape
     * @return Index of first shape that contains polyspaceMousePosition. -1 if no
     *         such shape found.
     */
    private int getSelectedShapeIndex(Point2D polyspaceMousePosition) {
        List<ShapeWrapper> shapes = model.getShapeManager().getShapes();
        for (int i = 0; i < shapes.size(); i++) {
            ShapeWrapper wrapper = shapes.get(i);
            Shape shape = wrapper.getShape();
            if (shape.contains(polyspaceMousePosition) || isPointOnShapeBorder(shape, polyspaceMousePosition)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Checks if a point is on the border of a given shape, within a specified
     * tolerance.
     * 
     * @param shape     the shape to check
     * @param point     the point to check
     * @param tolerance the tolerance within which to consider the point on the
     *                  border
     * @return true if the point is on the shape's border, false otherwise
     */
    private boolean isPointOnShapeBorder(Shape shape, Point2D point) {
        final double TOLERANCE = 3.0;

        if (shape == null) {
            return false;
        }
        // Create a small rectangle around the clicked point
        Rectangle2D.Double clickArea = new Rectangle2D.Double(
                point.getX() - TOLERANCE, point.getY() - TOLERANCE,
                2 * TOLERANCE, 2 * TOLERANCE);
        // Check if the clickArea intersects with the shape's outline
        return shape.intersects(clickArea);
    }

    private void handleMousePressed(MouseEvent e) {
        if (app.getMode() == Mode.MOVE_GRAPHICS_VIEW || app.getMode() == Mode.MOVE_CANVAS) {
            mouseDragStart = e.getPoint();
        }

        Point2D polyspaceMousePosition = model.toPolySpace(e.getPoint());

        if (app.getMode() == Mode.MOVE_CANVAS) {
            // save selected shape
            model.setSelectedShape(getSelectedShapeIndex(polyspaceMousePosition));
        }

        if (app.getMode() == Mode.DELETE) {
            int deleteShapeIndex = getSelectedShapeIndex(polyspaceMousePosition);
            model.getShapeManager().deleteShape(deleteShapeIndex);
        }

        canvas.repaint();
    }

    /**
     * Exports canvas to image.
     * <br>
     * <ol>
     * <li>https://stackoverflow.com/a/14369955/17627866</li>
     * <li>
     * https://stackoverflow.com/questions/17690275/exporting-a-jpanel-to-an-image
     * </li>
     * </ol>
     */
    private void handleCanvasExport() {
        // temporarily hide cursor position
        Point2D cursorPosition = model.getUserMousePosition();
        model.setUserMousePosition(null);
        canvas.repaint();

        // get canvas as a buffered image
        BufferedImage image = canvas.toImage();

        // display cursor again
        model.setUserMousePosition(cursorPosition);
        canvas.repaint();

        // let user choose file location
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose folder to save image");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false); // disable the "All files" option.

        int returnValue = fileChooser.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            final String folderPath = fileChooser.getSelectedFile().toString();
            final String imagePath = folderPath + "/canvas.png";
            try {
                ImageIO.write(image, "png", new File(imagePath));
                JOptionPane.showMessageDialog(canvas, "canvas.png was successfully saved at " + folderPath);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
        canvas.getTopLevelAncestor().requestFocus();
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        final String propertyName = e.getPropertyName();
        /**
         * List of property names that should result only in a canvas repaint.
         */
        final Set<String> repaintProperties = Set.of(
                ShapeManager.STATE_CHANGE_PROPERTY_NAME,
                "standardView",
                "enableGuidelines",
                "cellSize",
                "axesVisible",
                "labelFontSize");

        if (repaintProperties.contains(propertyName)) {
            canvas.repaint();
            return;
        }

        // if mode from AppState has changed
        if ("mode".equals(propertyName)) {
            for (AbstractDrawer controller : drawControllers) {
                controller.disposePreview();
            }
            // update canvas to erase any possible incomplete shape
            canvas.repaint();
            return;
        }

        // if printingCanvas property has changed to true, handle export
        if ("printingCanvas".equals(propertyName) && (boolean) e.getNewValue()) {
            handleCanvasExport();
        }
    }
}

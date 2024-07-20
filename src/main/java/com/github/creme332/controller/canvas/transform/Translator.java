package com.github.creme332.controller.canvas.transform;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.github.creme332.model.AppState;
import com.github.creme332.model.Mode;
import com.github.creme332.model.ShapeWrapper;
import com.github.creme332.view.Canvas;

public class Translator extends AbstractTransformer {

    public Translator(AppState app, Canvas canvas) {
        super(app, canvas);
    }

    @Override
    public void handleShapeSelection(int shapeIndex) {
        /**
         * A copy of the shape selected
         */
        final ShapeWrapper selectedWrapperCopy = canvasModel.getShapeManager().getShapes().get(shapeIndex);

        // request user for translation vector
        final Point2D translationVector = requestTranslationVector();

        // apply transformation on shape
        final AffineTransform transform = new AffineTransform();
        transform.translate(translationVector.getX(), translationVector.getY());
        final Shape transformedShape = transform.createTransformedShape(selectedWrapperCopy.getShape());

        // save transformed shape
        selectedWrapperCopy.setShape(transformedShape);
        canvasModel.getShapeManager().editShape(shapeIndex, selectedWrapperCopy);

        // translate plotted points
        final List<Point2D> originalPlottedPoints = selectedWrapperCopy.getPlottedPoints();
        for (int i = 0; i < originalPlottedPoints.size(); i++) {
            Point2D oldPoint = originalPlottedPoints.get(i);
            originalPlottedPoints.set(i,
                    new Point2D.Double(oldPoint.getX() + translationVector.getX(),
                            oldPoint.getY() + translationVector.getY()));
        }

        // repaint canvas
        canvas.repaint();
    }

    @Override
    public boolean shouldDraw() {
        return getCanvasMode() == Mode.TRANSLATION;
    }

    /**
     * Asks user to enter the radii for the ellipse. If input values are invalid
     * or if the operation is cancelled, null is returned.
     * 
     * @return array with radii [rx, ry]
     */
    private Point2D requestTranslationVector() {
        final Point2D zeroVector = new Point2D.Double(0, 0);

        JTextField rxField = new JTextField(5);
        JTextField ryField = new JTextField(5);
        JPanel panel = new JPanel();
        panel.add(new JLabel("X:"));
        panel.add(rxField);
        panel.add(new JLabel("Y:"));
        panel.add(ryField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Enter translation vector",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        // Request focus again otherwise keyboard shortcuts will not work
        canvas.getTopLevelAncestor().requestFocus();

        if (result == JOptionPane.OK_OPTION) {
            try {
                return new Point2D.Double(Integer.parseInt(rxField.getText()), Integer.parseInt(ryField.getText()));
            } catch (NumberFormatException e) {
                return zeroVector;
            }
        }
        return zeroVector;
    }

}
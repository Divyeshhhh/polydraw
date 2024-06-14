package com.github.creme332.model;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class CanvasModel {
    /**
     * Spacing (in pixels) between top of canvas and tick label when axis is out of
     * sight.
     */
    public static final int TICK_PADDING_TOP = 20;

    /**
     * Spacing (in pixels) between bottom of canvas and tick label when axis is out
     * of sight.
     */
    public static final int TICK_PADDING_BOTTOM = 10;

    /**
     * Spacing (in pixels) between left border of canvas and tick label when axis is
     * out of sight.
     */
    public static final int TICK_PADDING_LEFT = 12;

    /**
     * Spacing (in pixels) between right border of canvas and tick label when axis
     * is out of sight.
     */
    public static final int TICK_PADDING_RIGHT = 30;

    /**
     * Distance in pixels between each unit on axes is out of sight.
     */
    int cellSize = 100;

    public static final int MAX_CELL_SIZE = 500;
    public static final int DEFAULT_CELL_SIZE = 100;
    public static final int MIN_CELL_SIZE = 30;
    public static final int ZOOM_INCREMENT = 10;

    private float labelFontSizeScaleFactor = 1.4F;

    /**
     * Vertical distance between top border of canvas and the polydraw origin.
     */
    private int yZero;

    /**
     * Horizontal distance between left border of canvas and the polydraw origin.
     */
    private int xZero;

    private List<ShapeWrapper> shapes = new ArrayList<>();

    private boolean enableGuidelines = true; // Variable to track guidelines visibility

    /**
     * 
     * @return Transformation required to convert a coordinate in the polydraw
     *         coordinate system to the user space coordinate system.
     * 
     *         Let X be the x-coordinate of a point in the polydraw space. The
     *         corresponding
     *         coordinate in the user space coordinate system is X * cellSize +
     *         xAxisOrigin.
     * 
     *         Let Y be the y-coordinate of a point in the polydraw space. The
     *         corresponding
     *         coordinate in the user space coordinate system is -Y * cellSize +
     *         yAxisOrigin.
     */
    public AffineTransform getUserSpaceTransform() {
        AffineTransform transform = new AffineTransform();
        transform.translate(xZero, yZero); // applied second

        transform.scale(cellSize, -cellSize); // applied first

        return transform;
    }

    /**
     * 
     * @return Transformation required to convert a coordinate in the user space
     *         coordinate system to the polydraw coordinate system.
     */
    private AffineTransform getPolySpaceTransform() {
        AffineTransform transform = null;
        try {
            transform = getUserSpaceTransform().createInverse();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
        return transform;
    }

    /**
     * 
     * @param shape Shape in polydraw space
     * @return New shape in user space
     */
    public Shape toUserSpace(Shape shape) {
        return getUserSpaceTransform().createTransformedShape(shape);
    }

    /**
     * 
     * @param point Point in polydraw space
     * @return New shape in user space
     */
    public Point2D toUserSpace(Point2D point) {
        return getUserSpaceTransform().transform(point, null);
    }

    /**
     * 
     * @param shape
     * @return
     */
    public Shape toPolySpace(Shape shape) {
        return getPolySpaceTransform().createTransformedShape(shape);
    }

    /**
     * 
     * @param point
     * @return
     */
    public Point2D toPolySpace(Point2D point) {
        return getPolySpaceTransform().transform(point, null);
    }

    /**
     * Either zooms in or out of canvas, assuming zoom level is within accepted
     * range.
     * 
     * @param zoomIn Zoom in if true, zoom out otherwise
     */
    public void updateCanvasZoom(boolean zoomIn) {
        if (zoomIn) {
            setCellSize(Math.min(CanvasModel.MAX_CELL_SIZE, getCellSize() + CanvasModel.ZOOM_INCREMENT));
        } else {
            setCellSize(Math.max(CanvasModel.MIN_CELL_SIZE, getCellSize() - CanvasModel.ZOOM_INCREMENT));
        }
    }

    public List<ShapeWrapper> getShapes() {
        return shapes;
    }

    public void setShapes(List<ShapeWrapper> shapes) {
        this.shapes = shapes;
    }

    public int getCellSize() {
        return cellSize;
    }

    public float getLabelFontSizeSF() {
        return labelFontSizeScaleFactor;
    }

    public void setCellSize(int newCellSize) {
        cellSize = newCellSize;
    }

    public int getXZero() {
        return xZero;
    }

    public int getYZero() {
        return yZero;
    }

    public void setXZero(int newXZero) {
        xZero = newXZero;
    }

    public void setYZero(int newYZero) {
        yZero = newYZero;
    }

    public boolean isGuidelinesEnabled() {
        return enableGuidelines;
    }

    public void setGuidelinesEnabled(boolean enableGuidelines) {
        this.enableGuidelines = enableGuidelines;
    }
}

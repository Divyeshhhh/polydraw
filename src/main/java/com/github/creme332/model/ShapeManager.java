package com.github.creme332.model;

import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

/**
 * Manages the shapes created by the application. It supports add, undo, redo,
 * and delete operations in a thread-safe manner.
 */
public class ShapeManager {
    /**
     * List of shapes currently visible on canvas. No elements of this array are
     * null.
     */
    private List<ShapeWrapper> shapes;

    /**
     * A preview of a shape to be displayed on the canvas. It is not part of shapes
     * array and is not null while shape is getting constructed.
     */
    private ShapeWrapper shapePreview;

    /**
     * Stack containing the actions for undo functionality.
     */
    private Stack<ShapeAction> undoStack;

    /**
     * Stack containing information about actions that can be redone.
     */
    private Stack<ShapeAction> redoStack;

    private PropertyChangeSupport support;

    public static final String STATE_CHANGE_PROPERTY_NAME = "shapeManagerStateChanged";

    public ShapeManager() {
        shapes = new ArrayList<>();
        undoStack = new Stack<>();
        redoStack = new Stack<>();
        support = new PropertyChangeSupport(this);
    }

    /**
     * Replaces all current shapes with new shapes. Undo and redo stacks are reset.
     */
    public void importShapes(ShapeWrapper[] newShapes) {
        shapes = new ArrayList<>(Arrays.asList(newShapes));
        undoStack = new Stack<>();
        redoStack = new Stack<>();
        shapePreview = null;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(STATE_CHANGE_PROPERTY_NAME, listener);
    }

    public void setShapePreview(ShapeWrapper newPreview) {
        shapePreview = newPreview;
    }

    public ShapeWrapper getShapePreview() {
        return shapePreview;
    }

    // Class to store edit actions for undo/redo
    private class ShapeAction {
        /**
         * Shape affected by action.
         */
        ShapeWrapper shape;

        /**
         * Action performed on shape. If ShapeAction is found in undo stack, you must
         * perform the reverse of action. If ShapeAction is found in redo stack, you
         * must perform action.
         */
        Action action;

        ShapeWrapper oldShape;
        ShapeWrapper newShape;

        /**
         * Use this constructor when a shape was created or deleted.
         * 
         * @param shape
         * @param action
         */
        ShapeAction(ShapeWrapper shape, Action action) {
            this.shape = shape;
            this.action = action;
        }

        /**
         * Use this constructor when the attributes of an existing shape was edited.
         * 
         * @param oldShape
         * @param newShape
         * @param action
         */
        ShapeAction(ShapeWrapper oldShape, ShapeWrapper newShape, Action action) {
            this.action = action;
            this.oldShape = oldShape;
            this.newShape = newShape;
        }
    }

    /**
     * Action performed on shapes array.
     */
    private enum Action {
        /**
         * User added a new shape.
         */
        ADD,

        /**
         * User deleted an existing shape.
         */
        DELETE,

        /**
         * User edited an existing shape.
         */
        EDIT,
    }

    /**
     * Reset shape manager to its initial state, deleting entire save history.
     */
    public void reset() {
        shapes.clear();
        undoStack.clear();
        redoStack.clear();
        support.firePropertyChange(STATE_CHANGE_PROPERTY_NAME, false, true);
    }

    public void addShape(ShapeWrapper shape) {
        shapes.add(shape);
        undoStack.push(new ShapeAction(shape, Action.ADD));
        redoStack.clear(); // Clear redo stack after a new action
        support.firePropertyChange(STATE_CHANGE_PROPERTY_NAME, false, true);
    }

    /**
     * 
     * @param shapeIndex index of shape to be deleted in the shapes array.
     */
    public void deleteShape(final int shapeIndex) {
        if (shapeIndex < 0 || shapeIndex >= shapes.size()) {
            System.out.println("Cannot delete shape at index " + shapeIndex);
            return;
        }

        final ShapeWrapper shape = shapes.get(shapeIndex);

        if (shapes.remove(shape)) {
            undoStack.push(new ShapeAction(shape, Action.DELETE));
            redoStack.clear(); // Clear redo stack after a new action
            support.firePropertyChange(STATE_CHANGE_PROPERTY_NAME, false, true);
        }
    }

    public void editShape(final int oldShapeIndex, final ShapeWrapper newShape) {
        if (newShape == null) {
            throw new NullPointerException("Edit shape failed: Cannot replace a shape with null.");
        }
        
        final ShapeWrapper oldShape = shapes.get(oldShapeIndex);
        if (oldShapeIndex != -1) {
            shapes.set(oldShapeIndex, newShape);
            undoStack.push(new ShapeAction(oldShape, newShape, Action.EDIT));
            redoStack.clear();
        }
    }

    public void undo() {
        if (undoStack.isEmpty())
            return;

        ShapeAction shapeAction = undoStack.pop();
        Action actionToUndo = shapeAction.action;
        ShapeWrapper shapeToUndo = shapeAction.shape;

        if (actionToUndo == Action.ADD) {
            shapes.remove(shapeToUndo);
        }

        if (actionToUndo == Action.DELETE) {
            shapes.add(shapeToUndo);
        }

        if (actionToUndo == Action.EDIT) {
            shapes.remove(shapeAction.newShape);
            shapes.add(shapeAction.oldShape);
        }

        redoStack.push(shapeAction);
        support.firePropertyChange(STATE_CHANGE_PROPERTY_NAME, false, true);
    }

    public boolean isRedoPossible() {
        return !redoStack.empty();
    }

    public boolean isUndoPossible() {
        return !undoStack.empty();
    }

    public void redo() {
        if (redoStack.isEmpty())
            return;

        final ShapeAction shapeAction = redoStack.pop();
        final Action actionToRedo = shapeAction.action;
        ShapeWrapper shapeToRedo = shapeAction.shape;

        if (actionToRedo == Action.ADD) {
            shapes.add(shapeToRedo);
        }

        if (actionToRedo == Action.DELETE) {
            shapes.remove(shapeToRedo);
        }

        if (actionToRedo == Action.EDIT) {
            shapes.remove(shapeAction.oldShape);
            shapes.add(shapeAction.newShape);
        }

        undoStack.push(shapeAction);
        support.firePropertyChange(STATE_CHANGE_PROPERTY_NAME, false, true);
    }

    /**
     * 
     * @param polyspacePoint Coordinate of some point in polyspace.
     * @return Index of first shape that contains the given point. -1 if no
     *         such shape found.
     */
    public int getSelectedShapeIndex(Point2D polyspacePoint) {
        for (int i = 0; i < shapes.size(); i++) {
            ShapeWrapper wrapper = shapes.get(i);
            if (wrapper.isPointOnShape(polyspacePoint)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 
     * @param i Index of shape in shape array
     * @return A copy of the shape
     */
    public ShapeWrapper getShapeByIndex(int i) {
        return new ShapeWrapper(shapes.get(i));
    }

    /**
     * 
     * @return A copy of the the shapes array that should be displayed on the
     *         canvas. A shape preview may also be included as the last element.
     */
    public List<ShapeWrapper> getShapes() {
        ArrayList<ShapeWrapper> shapesCopy = new ArrayList<>();
        for (ShapeWrapper shape : shapes) {
            shapesCopy.add(new ShapeWrapper(shape));
        }

        if (shapePreview != null) {
            shapesCopy.add(shapePreview);
        }

        return shapesCopy;
    }
}
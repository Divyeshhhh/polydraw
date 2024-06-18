package com.github.creme332.view;

import java.awt.BorderLayout;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.github.creme332.model.CanvasModel;
import com.github.creme332.utils.exception.InvalidIconSizeException;
import com.github.creme332.utils.exception.InvalidPathException;

/**
 * A panel that contains control buttons for Canvas. It should be placed on top
 * of the canvas and should have the same dimensions as Canvas.
 */
public class CanvasConsole extends JPanel {
    private Toolbar toolbar;
    private Toast toast = new Toast();
    private ZoomPanel zoomPanel = new ZoomPanel();

    public CanvasConsole(CanvasModel model) {
        setOpaque(false); // make panel transparent
        setLayout(new BorderLayout());

        try {
            toolbar = new Toolbar(model.getLineType(), model.getFillColor(), model.getLineThickness());
        } catch (InvalidIconSizeException | InvalidPathException e) {
            e.printStackTrace();
            System.exit(0);
        }

        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.add(toolbar);
        add(topPanel, BorderLayout.NORTH);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBorder(new EmptyBorder(new Insets(0, 20, 10, 0)));
        southPanel.setOpaque(false);

        southPanel.add(toast, BorderLayout.WEST);
        add(southPanel, BorderLayout.SOUTH);

        JPanel eastPanel = new JPanel(new BorderLayout());
        eastPanel.setOpaque(false);

        eastPanel.add(zoomPanel, BorderLayout.SOUTH);
        add(eastPanel, BorderLayout.EAST);
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    public Toast getToast() {
        return toast;
    }

    public ZoomPanel getZoomPanel() {
        return zoomPanel;
    }
}
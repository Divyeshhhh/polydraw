package com.github.creme332.controller;

import com.github.creme332.model.AppState;
import com.github.creme332.utils.exception.InvalidIconSizeException;
import com.github.creme332.utils.exception.InvalidPathException;
import com.github.creme332.view.*;

/**
 * Controls all the logic in the application by linking views and model.
 */
public class Controller {
    AppState app = new AppState();

    private Frame frame; // frame of app
    private MenuBar menuBar;
    private Canvas canvas;

    private FrameController frameController;

    public Controller() {
        try {
            menuBar = new MenuBar(app.getMenuModels());
            new MenuBarController(app, menuBar);

            CanvasConsole canvasConsole = new CanvasConsole(app.getCanvasModel());
            TutorialCenter tutorialCenter = new TutorialCenter(app.getTutorialScreenModel());

            frame = new Frame(canvas, canvasConsole, menuBar, tutorialCenter);

            new ToolBarController(canvasConsole.getToolbar(), app.getCanvasModel(), frame); 
            new ZoomPanelController(app.getCanvasModel(), canvasConsole.getZoomPanel());

            canvas = new Canvas(app.getCanvasModel());
            new CanvasController(app, canvas);

            new TutorialController(app, tutorialCenter);

            frameController = new FrameController(app, frame);

            new SideMenuController(app, frame.getSideMenuPanel());
        } catch (InvalidIconSizeException | InvalidPathException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        frameController.playStartAnimation();
    }
}

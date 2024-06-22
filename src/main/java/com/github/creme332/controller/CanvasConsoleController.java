package com.github.creme332.controller;

import com.github.creme332.model.AppState;
import com.github.creme332.view.CanvasConsole;

public class CanvasConsoleController {
    public CanvasConsoleController(AppState app, CanvasConsole console) {
        new ToolBarController(console.getToolbar(), app.getCanvasModel());
        new SideMenuController(app, console.getSidebar());
        new ToastController(app, console.getToast());
        new ZoomPanelController(app, console.getZoomPanel());
    }
}
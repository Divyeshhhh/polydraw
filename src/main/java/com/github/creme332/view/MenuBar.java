package com.github.creme332.view;

import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.kordamp.ikonli.swing.FontIcon;

import com.github.creme332.model.MenuItemModel;
import com.github.creme332.model.MenuModel;
import com.github.creme332.utils.IconLoader;
import com.github.creme332.utils.exception.InvalidIconSizeException;
import com.github.creme332.utils.exception.InvalidPathException;

import org.kordamp.ikonli.bootstrapicons.BootstrapIcons;

public class MenuBar extends JMenuBar {
    private JButton sidebarButton;
    private JButton guidelinesButton; // Button to toggle guidelines
    private JButton toggleAxesButton; // Button to toggle axes visibility
    private JButton helpButton;
    private JButton exportButton; // Button to export canvas as image

    public MenuBar(MenuModel[] menus) throws InvalidIconSizeException, InvalidPathException {

        // add menus to menubar
        for (MenuModel menuModel : menus) {
            JMenu menu = new JMenu();
            menu.setIcon(menuModel.getActiveItem().getIcon());
            menu.setToolTipText(menuModel.getActiveItem().getName());

            for (MenuItemModel item : menuModel.getItems()) {
                JMenuItem menuItem = new JMenuItem(item.getName(), item.getIcon());
                menu.add(menuItem);
                menuItem.addActionListener(e -> menu.setToolTipText(item.getName()));
            }

            this.add(menu);
        }

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        leftPanel.setOpaque(false);

        // undo button
        JButton btn = new JButton();
        btn.setIcon(FontIcon.of(BootstrapIcons.ARROW_COUNTERCLOCKWISE, 40));
        btn.setBorderPainted(false);
        leftPanel.add(btn);
        btn.setToolTipText("Undo");

        // redo button
        btn = new JButton();
        btn.setIcon(FontIcon.of(BootstrapIcons.ARROW_CLOCKWISE, 40));
        btn.setBorderPainted(false);
        leftPanel.add(btn);
        btn.setToolTipText("Redo");

        // guidelines button
        guidelinesButton = new JButton();
        guidelinesButton.setIcon(FontIcon.of(BootstrapIcons.GRID_3X3, 37));
        guidelinesButton.setBorderPainted(false);
        leftPanel.add(guidelinesButton);
        guidelinesButton.setToolTipText("Guidelines");

        // toggle axes button
        toggleAxesButton = new JButton();
        toggleAxesButton.setIcon(IconLoader.loadIcon("/icons/axes.png", 40));
        toggleAxesButton.setBorderPainted(false);
        toggleAxesButton.setToolTipText("Toggle axes");
        leftPanel.add(toggleAxesButton);

        // help button
        helpButton = new JButton();
        helpButton.setIcon(FontIcon.of(BootstrapIcons.QUESTION_CIRCLE, 37));
        helpButton.setBorderPainted(false);
        helpButton.setToolTipText("Toggle axes");
        leftPanel.add(helpButton);

        // export button
        exportButton = new JButton();
        exportButton.setIcon(FontIcon.of(BootstrapIcons.CAMERA, 37)); // Use an appropriate icon for export
        exportButton.setBorderPainted(false);
        exportButton.setToolTipText("Export canvas");
        leftPanel.add(exportButton);

        // sidebar menu button
        sidebarButton = new JButton();
        sidebarButton.setIcon(FontIcon.of(BootstrapIcons.LIST, 40));
        sidebarButton.setBorderPainted(false);
        leftPanel.add(sidebarButton);
        sidebarButton.setToolTipText("Toggle sidebar");

        this.add(leftPanel);
    }

    public JButton getSideBarButton() {
        return sidebarButton;
    }

    public JButton getGuidelinesButton() {
        return guidelinesButton;
    }

    public JButton getToggleAxesButton() {
        return toggleAxesButton;
    }

    public JButton getHelpButton() {
        return helpButton;
    }

    public JButton getExportButton() {
        return exportButton;
    }
}

package sim.ui.menu;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.LineBorder;

import sim.actions.ActionHandler;
import sim.logic.CommandManager;
import sim.ui.CanvasPanel;
import sim.util.ThemeManager;

public class SimulatorMenuBar extends JMenuBar {

    private final ActionHandler actionHandler;
    private final CommandManager commandManager;
    private final CanvasPanel canvas;

    public SimulatorMenuBar(ActionHandler actionHandler, CommandManager commandManager, 
                            CanvasPanel canvas) {
        this.actionHandler = actionHandler;
        this.commandManager = commandManager;
        this.canvas = canvas;

        buildMenu();
        ThemeManager.addThemeListener(this::refreshAppearance);
        refreshAppearance();
    }

    private void buildMenu() {
        // File Menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem saveItem = new JMenuItem("Save Circuit");
        saveItem.addActionListener(e -> actionHandler.performSave());
        JMenuItem loadItem = new JMenuItem("Load Circuit");
        loadItem.addActionListener(e -> actionHandler.performLoad());
        JMenuItem importItem = new JMenuItem("Import Sub-Circuit");
        importItem.addActionListener(e -> actionHandler.performImport());
        JMenuItem exportItem = new JMenuItem("Export Sub-Circuit");
        exportItem.addActionListener(e -> actionHandler.performExport());

        fileMenu.add(saveItem);
        fileMenu.add(loadItem);
        fileMenu.addSeparator();
        fileMenu.add(importItem);
        fileMenu.add(exportItem);
        this.add(fileMenu);

        // Edit Menu
        JMenu editMenu = new JMenu("Edit");
        JMenuItem undoItem = new JMenuItem("Undo");
        undoItem.addActionListener(e -> {
            commandManager.undo();
            canvas.repaint();
        });
        JMenuItem redoItem = new JMenuItem("Redo");
        redoItem.addActionListener(e -> {
            commandManager.redo();
            canvas.repaint();
        });
        JMenuItem deleteItem = new JMenuItem("Delete Selection");
        deleteItem.addActionListener(e -> actionHandler.performDelete());
        
        editMenu.add(undoItem);
        editMenu.add(redoItem);
        editMenu.addSeparator();
        editMenu.add(deleteItem);
        this.add(editMenu);
        
        // Theme Menu
        JMenu themeMenu = new JMenu("Theme");
        for (String themeName : ThemeManager.getThemeNames()) {
            JMenuItem themeItem = new JMenuItem(themeName);
            themeItem.addActionListener(e -> ThemeManager.setTheme(themeName));
            themeMenu.add(themeItem);
        }
        this.add(themeMenu);
    }

    private void refreshAppearance() {
        Color bg = ThemeManager.getTheme().bg;
        double brightness = (bg.getRed() * 0.299 + bg.getGreen() * 0.587 + bg.getBlue() * 0.114);
        
        Color menuColor;
        if (brightness > 128) {
            // Light theme: make it slightly darker than bg
            menuColor = new Color(Math.max(0, bg.getRed() - 15), Math.max(0, bg.getGreen() - 15), Math.max(0, bg.getBlue() - 15));
        } else {
            // Dark theme: make it slightly lighter than bg
            menuColor = new Color(Math.min(255, bg.getRed() + 20), Math.min(255, bg.getGreen() + 20), Math.min(255, bg.getBlue() + 20));
        }

        this.setBackground(menuColor);
        this.setBorder(new LineBorder(ThemeManager.getTheme().componentBorder, 1));
        styleMenuComponents(this, menuColor);
    }

    private void styleMenuComponents(Component container, Color menuColor) {
        if (container instanceof JMenuBar) {
            for (Component c : ((JMenuBar) container).getComponents()) {
                styleMenuComponents(c, menuColor);
            }
        } else if (container instanceof JMenu) {
            JMenu menu = (JMenu) container;
            menu.setBackground(menuColor);
            menu.setForeground(ThemeManager.getTheme().text);
            menu.setOpaque(true);
            
            JPopupMenu popup = menu.getPopupMenu();
            popup.setBackground(ThemeManager.getTheme().bg);
            popup.setBorder(new LineBorder(ThemeManager.getTheme().componentBorder, 1));
            
            for (Component c : popup.getComponents()) {
                styleMenuComponents(c, menuColor);
            }
        } else if (container instanceof JMenuItem) {
            JMenuItem item = (JMenuItem) container;
            item.setBackground(ThemeManager.getTheme().bg);
            item.setForeground(ThemeManager.getTheme().text);
            item.setOpaque(true);
        }
    }
}

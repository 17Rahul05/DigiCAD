package sim.ui.menu;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import sim.CircuitComponent;
import sim.actions.ActionHandler;
import sim.ui.CanvasPanel;
import sim.util.ThemeManager;

public class ContextMenu {

    private ContextMenu() {
        /* Utility class */
    }

    public static void show(CanvasPanel canvas, int x, int y, List<CircuitComponent> selectedComponents, ActionHandler actionHandler) {
        if (selectedComponents.isEmpty()) return;

        JPopupMenu menu = new JPopupMenu() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.getTheme().bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(ThemeManager.getTheme().componentBorder);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 10, 10);
                g2.dispose();
            }
        };
        menu.setOpaque(false);
        menu.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        menu.setBackground(new Color(0, 0, 0, 0));

        JMenuItem subCircuitItem = new JMenuItem("Create Sub-Circuit");
        styleMenuItem(subCircuitItem);
        subCircuitItem.addActionListener(ev -> {
            if (actionHandler != null) actionHandler.createSubComponent();
        });
        menu.add(subCircuitItem);

        JMenuItem exportItem = new JMenuItem("Export Sub-Circuit");
        styleMenuItem(exportItem);
        exportItem.addActionListener(ev -> {
            if (actionHandler != null) actionHandler.performExport();
        });
        menu.add(exportItem);

        menu.addSeparator();

        JMenuItem deleteItem = new JMenuItem("Delete");
        styleMenuItem(deleteItem);
        deleteItem.addActionListener(ev -> {
            if (actionHandler != null) actionHandler.performDelete();
        });
        menu.add(deleteItem);

        menu.show(canvas, x, y);
    }

    private static void styleMenuItem(JMenuItem item) {
        item.setBackground(ThemeManager.getTheme().bg);
        item.setForeground(ThemeManager.getTheme().text);
        item.setFont(new Font("SansSerif", Font.PLAIN, 14));
        item.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
    }
}

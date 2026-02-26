package sim.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Path2D;
import java.awt.Rectangle;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import sim.CircuitComponent;
import sim.CircuitManager;
import sim.logic.CommandManager;
import sim.model.Wire;
import sim.util.PinState;
import sim.util.ThemeManager;

public class CanvasPanel extends JPanel {
    private CircuitManager manager;
    private CommandManager commandManager;
    private MouseController mc;
    
    // ~~~~~~~~~~ TRANSFORMATIONS ~~~~~~~~~~
    private double zoom = 1.0;
    private double panX = 0;
    private double panY = 0;

    // ~~~~~~~~~~ UI OVERLAYS ~~~~~~~~~~
    private ToolButton resetButton;
    private ToolButton undoButton;
    private ToolButton redoButton;
    private String errorMessage = "";
    private long errorExpireTime = 0;

    // ~~~~~~~~~~ GRID ~~~~~~~~~~
    private boolean gridVisible = false;
    private int gridSize = 20;

    public CanvasPanel(CircuitManager manager) {
        this.manager = manager;        
        this.setLayout(null);
        this.setFocusable(true);
        this.requestFocusInWindow();

        ThemeManager.addThemeListener(() -> {
            setBackground(ThemeManager.getTheme().bg);
            if (resetButton != null) resetButton.refreshAppearance();
            if (undoButton != null) undoButton.refreshAppearance();
            if (redoButton != null) redoButton.refreshAppearance();
            repaint();
        });
        setBackground(ThemeManager.getTheme().bg);

        setupHUDButtons();

        // Add a listener to reposition the buttons when the panel is resized
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                positionHUDButtons();
            }
        });
    }

    private void setupHUDButtons() {
        resetButton = new ToolButton("Reset", ThemeManager.getTheme().buttonBg, true, null, null, null, "Reset pan and zoom to default");
        resetButton.setFocusable(false);
        resetButton.addActionListener(e -> resetView());
        this.add(resetButton);

        undoButton = new ToolButton("⟲", ThemeManager.getTheme().buttonBg, true, null, null, null, "Undo last action (Ctrl+Z)", 40, 40);
        undoButton.setFocusable(false);
        undoButton.setCornerRadius(5);
        undoButton.setFont(new Font("Segoe UI Symbol", Font.BOLD, 16));
        undoButton.addActionListener(e -> {
            if (commandManager != null) {
                commandManager.undo();
                repaint();
            }
        });
        this.add(undoButton);

        redoButton = new ToolButton("⟳", ThemeManager.getTheme().buttonBg, true, null, null, null, "Redo last action (Ctrl+Y)", 40, 40);
        redoButton.setFocusable(false);
        redoButton.setCornerRadius(5);
        redoButton.setFont(new Font("Segoe UI Symbol", Font.BOLD, 16));
        redoButton.addActionListener(e -> {
            if (commandManager != null) {
                commandManager.redo();
                repaint();
            }
        });
        this.add(redoButton);

        positionHUDButtons();
    }

    private void positionHUDButtons() {
        int resetWidth = 100;
        int resetHeight = 25;
        int iconSize = 40;
        int padding = 15;
        
        resetButton.setBounds(getWidth() - resetWidth - padding, padding, resetWidth, resetHeight);
        undoButton.setBounds(padding, padding, iconSize, iconSize);
        redoButton.setBounds(padding + iconSize + 10, padding, iconSize, iconSize);
    }

    // Setter to connect MouseController and CommandManager
    public void setController(MouseController mc) {
        this.mc = mc;
    }

    public void setCommandManager(CommandManager cm) {
        this.commandManager = cm;
    }

    public void showErrorMessage(String message) {
        this.errorMessage = message;
        this.errorExpireTime = System.currentTimeMillis() + 3000; // 3 seconds
        repaint();
    }

    public void setGridVisible(boolean visible, int size) {
        this.gridVisible = visible;
        this.gridSize = size;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        AffineTransform savedTransform = g2.getTransform();

        // Apply Pan and Zoom
        g2.translate(panX, panY);
        g2.scale(zoom, zoom);
        
        // --- DRAW GRID FIRST ---
        if (gridVisible) {
            drawGrid(g2);
        }

        // Set wire state
        for (Wire wire : manager.getWires()) {
            PinState state = manager.getPinState(wire.getSourcePinID());
            wire.setState(state);
        }

        // Order is important. DO NOT CHANGE
        drawWires(g2);
        drawInteractiveWires(g2);
        drawHighlights(g2);
        drawComponents(g2);
        drawSelectionMarquee(g2);

        // --- UI OVERLAYS ARE DRAWN AFTER RESETTING TRANSFORM ---
        g2.setTransform(savedTransform);
        drawOverlay(g2);
    }

    private void drawGrid(Graphics2D g2) {
        Point2D topLeft = screenToWorld(new Point(0, 0));
        Point2D bottomRight = screenToWorld(new Point(getWidth(), getHeight()));

        double startX = Math.floor(topLeft.getX() / gridSize) * gridSize;
        double startY = Math.floor(topLeft.getY() / gridSize) * gridSize;
        double endX = Math.ceil(bottomRight.getX() / gridSize) * gridSize;
        double endY = Math.ceil(bottomRight.getY() / gridSize) * gridSize;

        Color bg = ThemeManager.getTheme().bg;
        // Calculate brightness to decide if we darken or lighten for the grid
        double brightness = (bg.getRed() * 0.299 + bg.getGreen() * 0.587 + bg.getBlue() * 0.114);
        
        Color gridColor;
        if (brightness > 128) {
            // Light theme: use a darker grid
            gridColor = new Color(0, 0, 0, 60);
        } else {
            // Dark theme: use a lighter grid
            gridColor = new Color(255, 255, 255, 50);
        }

        g2.setColor(gridColor);

        for (double x = startX; x <= endX; x += gridSize) {
            for (double y = startY; y <= endY; y += gridSize) {
                // Draw a small 2x2 dot at each intersection
                g2.fillRect((int) x - 1, (int) y - 1, 2, 2);
            }
        }
    }

    private void drawWires(Graphics2D g2) {
        // Get live pin locations for the component being moved for smooth wire dragging
        CircuitComponent movingComponent = (mc != null) ? mc.getSelectedComponent() : null;
        Map<Integer, Point> movingPins = (movingComponent != null) ? movingComponent.getPinCoordinates() : null;

        // Draw Established Wires
        for (Wire wire : manager.getWires()) {
            Point p1 = manager.getPointForPin(wire.getSourcePinID());
            Point p2 = manager.getPointForPin(wire.getDestPinID());

            // If a component is being moved, override the stale pin map with live data
            if (movingPins != null) {
                if (movingPins.containsKey(wire.getSourcePinID())) {
                    p1 = movingPins.get(wire.getSourcePinID());
                }
                if (movingPins.containsKey(wire.getDestPinID())) {
                    p2 = movingPins.get(wire.getDestPinID());
                }
            }

            if (p1 != null && p2 != null) wire.draw(g2, p1.x, p1.y, p2.x, p2.y);
        }
    }

    private void drawInteractiveWires(Graphics2D g2) {
        if (mc != null) {
            Point p1 = mc.getWirePreviewStart();
            Point p2 = mc.getWirePreviewEnd();

            if (p1 != null && p2 != null) {
                g2.setColor(ThemeManager.getTheme().text);
                float[] dash = {9.0f};
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, dash, 0));
                
                if (mc.isSnapToGrid()) {
                    Path2D path = new Path2D.Double();
                    int midX = (p1.x + p2.x) / 2;
                    path.moveTo(p1.x, p1.y);
                    path.lineTo(midX, p1.y);
                    path.lineTo(midX, p2.y);
                    path.lineTo(p2.x, p2.y);
                    g2.draw(path);
                } else {
                    g2.drawLine(p1.x, p1.y, p2.x, p2.y);
                }
            }
        }
        // Reset stroke to default
        g2.setStroke(new BasicStroke(1));
    }

    private void drawHighlights(Graphics2D g2) {
        if (mc != null) {
            List<CircuitComponent> selected = mc.getSelectedComponents();
            g2.setColor(ThemeManager.getTheme().selection);
            g2.setStroke(new BasicStroke(2));
            for (CircuitComponent c : selected) {
                g2.drawRect(c.getX() - 5, c.getY() - 5, c.getWidth() + 10, c.getHeight() + 10);
            }
        }
    }

    private void drawComponents(Graphics2D g2) {
        for (CircuitComponent component : manager.getComponents()) {
            component.draw(g2);
        }
    }

    private void drawSelectionMarquee(Graphics2D g2) {
        if (mc != null) {
            Rectangle rect = mc.getSelectionRect();
            if (rect != null) {
                // Use a slightly more transparent version for the fill
                Color sel = ThemeManager.getTheme().selection;
                g2.setColor(new Color(sel.getRed(), sel.getGreen(), sel.getBlue(), 30));
                g2.fill(rect);
                g2.setColor(sel);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(rect);
            }
        }
    }

    private void drawOverlay(Graphics2D g2) {
        if (System.currentTimeMillis() < errorExpireTime) {
            g2.setColor(new Color(255, 0, 0, 200));
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            g2.drawString(errorMessage, 20, getHeight() - 20);
        }
    }

    // ==================================================================================
    // COORDINATE TRANSFORMATION
    // ==================================================================================

    public void resetView() {
        this.panX = 0;
        this.panY = 0;
        this.zoom = 1.0;
        repaint();
    }

    public AffineTransform getTransform() {
        AffineTransform tx = new AffineTransform();
        tx.translate(panX, panY);
        tx.scale(zoom, zoom);
        return tx;
    }

    public Point2D screenToWorld(Point screenPoint) {
        try {
            return getTransform().inverseTransform(screenPoint, null);
        } catch (NoninvertibleTransformException e) {
            e.printStackTrace();
            return screenPoint; // Fallback
        }
    }

    public void setZoom(double newZoom) { this.zoom = newZoom; }
    public double getZoom() { return zoom; }
    public void setPan(double newPanX, double newPanY) {
        this.panX = newPanX;
        this.panY = newPanY;
    }
    public double getPanX() { return panX; }
    public double getPanY() { return panY; }
}

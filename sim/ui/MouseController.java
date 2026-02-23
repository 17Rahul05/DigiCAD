package sim.ui;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import sim.CircuitComponent;
import sim.CircuitManager;
import sim.logic.AddComponentCommand;
import sim.logic.AddWireCommand;
import sim.logic.CommandManager;
import sim.logic.MoveComponentCommand;
import sim.logic.RemoveComponentCommand;
import sim.model.LED;
import sim.model.Switch;
import sim.model.Tooltype;
import sim.model.Wire;
import sim.util.Clickable;
import sim.util.ComponentFactory;

public class MouseController extends MouseAdapter {

    // ==================================================================================
    // FIELDS
    // ==================================================================================

    private final CircuitManager manager;
    private final CanvasPanel canvas;
    private final Supplier<Tooltype> toolSupplier;
    private final CommandManager commandManager;

    // Interaction State
    private CircuitComponent selectedComponent = null; // For single-component drag
    private List<CircuitComponent> selectedComponents = new ArrayList<>();
    private Point2D.Double oldWorldPoint = null; 
    private Point dragOffset = new Point();
    private int startPin = -1;

    // Selection Rectangle State
    private Point selectionRectStart = null;
    private Rectangle selectionRect = null;

    // Panning State
    private Point panStartPoint = null;

    // Grid Snapping
    private boolean snapToGrid = false;
    private int gridSize = 10;

    // ==================================================================================
    // CONSTRUCTOR
    // ==================================================================================

    public MouseController(
        CircuitManager manager,
        CanvasPanel canvas,
        Supplier<Tooltype> toolSupplier,
        CommandManager commandManager
    ) {
        this.manager = manager;
        this.canvas = canvas;
        this.toolSupplier = toolSupplier;
        this.commandManager = commandManager;
    }

    // ==================================================================================
    // MOUSE EVENTS
    // ==================================================================================

    @Override
    public void mousePressed(MouseEvent e) {
        // Middle mouse button for panning
        if (SwingUtilities.isMiddleMouseButton(e)) {
            panStartPoint = e.getPoint();
            return;
        }

        Point2D worldPos = canvas.screenToWorld(e.getPoint());
        Tooltype currtool = toolSupplier.get();

        // Double-click to rename - only in SELECT mode
        if (currtool == Tooltype.SELECT && e.getClickCount() == 2) {
            handleRenaming(worldPos);
            return;
        }

        // Reset wire state if not wiring
        if (currtool != Tooltype.WIRE) {
            startPin = -1;
            oldWorldPoint = null;
        }

        switch (currtool) {
            case TOGGLE -> handleClickInteraction(worldPos);
            case WIRE   -> getStartPin(worldPos);
            case SELECT -> handleSelectionPress(worldPos, e.getPoint());
            case DELETE -> handleDeletion(worldPos);
            default     -> placeComponent(currtool, worldPos);
        }
        canvas.repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        panStartPoint = null;

        Point2D worldPos = canvas.screenToWorld(e.getPoint());

        if (toolSupplier.get() == Tooltype.WIRE && startPin != -1) {
            int endPin = manager.getPinAt((int) worldPos.getX(), (int) worldPos.getY(), 10);
            if (endPin != -1 && endPin != startPin) {
                // Try to create wire but use command if successful
                // We need to check if it's valid first
                String warning = manager.validateWire(startPin, endPin);
                if (warning == null) {
                    commandManager.executeCommand(new AddWireCommand(manager, new Wire(startPin, endPin)));
                } else {
                    canvas.showErrorMessage(warning);
                }
            }
        } else if (selectionRect != null) {
            // Finalize marquee selection
            selectedComponents.clear();
            for (CircuitComponent c : manager.getComponents()) {
                if (selectionRect.contains(c.getX() + c.getWidth() / 2, c.getY() + c.getHeight() / 2)) {
                    selectedComponents.add(c);
                }
            }
            selectionRect = null;
            selectionRectStart = null;
        } else if (selectedComponent != null) {
            // Finalize single component move
            if (manager.isSpaceOccupied(selectedComponent)) {
                if (oldWorldPoint != null) {
                    canvas.showErrorMessage("Space Occupied!");
                    selectedComponent.setLocation((int) oldWorldPoint.x, (int) oldWorldPoint.y);
                }
            } else {
                // If it moved, record command
                if (oldWorldPoint != null && (selectedComponent.getX() != (int)oldWorldPoint.x || selectedComponent.getY() != (int)oldWorldPoint.y)) {
                    List<MoveComponentCommand.MoveInfo> moves = new ArrayList<>();
                    moves.add(new MoveComponentCommand.MoveInfo(selectedComponent, new Point((int)oldWorldPoint.x, (int)oldWorldPoint.y), new Point(selectedComponent.getX(), selectedComponent.getY())));
                    commandManager.executeCommand(new MoveComponentCommand(manager, moves));
                }
            }
            manager.refreshAllPinLocations();
            // After moving, the single component becomes the whole selection
            selectedComponents.clear();
            selectedComponents.add(selectedComponent);
        }
        
        // Reset single-drag state
        selectedComponent = null;
        startPin = -1;
        oldWorldPoint = null; 
        
        canvas.repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // Handle Panning
        if (panStartPoint != null) {
            double dx = e.getX() - panStartPoint.x;
            double dy = e.getY() - panStartPoint.y;
            canvas.setPan(canvas.getPanX() + dx, canvas.getPanY() + dy);
            panStartPoint = e.getPoint();
            canvas.repaint();
            return;
        }

        Point2D worldPos = canvas.screenToWorld(e.getPoint());
        
        if (toolSupplier.get() == Tooltype.SELECT) {
            if (selectedComponent != null) {
                // Dragging a single component
                int newX = (int)(worldPos.getX() - dragOffset.x / canvas.getZoom());
                int newY = (int)(worldPos.getY() - dragOffset.y / canvas.getZoom());
                
                if (snapToGrid) {
                    newX = snap(newX);
                    newY = snap(newY);
                }

                selectedComponent.setLocation(newX, newY);
            } else if (selectionRectStart != null) {
                // Dragging a selection rectangle
                Point worldPoint = new Point((int)worldPos.getX(), (int)worldPos.getY());
                selectionRect = new Rectangle(selectionRectStart);
                selectionRect.add(worldPoint);
            }
        } else if (toolSupplier.get() == Tooltype.WIRE && startPin != -1) {
            oldWorldPoint = new Point2D.Double(worldPos.getX(), worldPos.getY());
        }
        canvas.repaint();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        Point screenPoint = e.getPoint();
        Point2D worldPointBeforeZoom = canvas.screenToWorld(screenPoint);

        double newZoom = canvas.getZoom() * Math.pow(1.1, -e.getWheelRotation());
        newZoom = Math.clamp(newZoom, 0.2, 5.0);
        
        canvas.setZoom(newZoom);

        double newPanX = screenPoint.getX() - worldPointBeforeZoom.getX() * newZoom;
        double newPanY = screenPoint.getY() - worldPointBeforeZoom.getY() * newZoom;
        canvas.setPan(newPanX, newPanY);
        
        canvas.repaint();
    }

    // ==================================================================================
    // HANDLERS
    // ==================================================================================

    private void handleRenaming(Point2D worldPos) {
        for (CircuitComponent c : manager.getComponents()) {
            if (c.contains((int) worldPos.getX(), (int) worldPos.getY())) {
                String newID = JOptionPane.showInputDialog(canvas, "Rename Component", c.getID());
                if (newID != null && !newID.trim().isEmpty()) {
                    c.setID(newID.trim());
                    canvas.repaint();
                }
                return;
            }
        }
    }

    private void handleSelectionPress(Point2D worldPos, Point screenPos) {
        selectedComponent = null;
        // First, check if we're clicking a component
        for (CircuitComponent c : manager.getComponents()) {
            if (c.contains((int) worldPos.getX(), (int) worldPos.getY())) {
                selectedComponent = c; // It's a potential single-component drag
                oldWorldPoint = new Point2D.Double(c.getX(), c.getY());

                Point2D screenSpaceComp = canvas.getTransform().transform(oldWorldPoint, null);
                dragOffset.x = screenPos.x - (int) screenSpaceComp.getX();
                dragOffset.y = screenPos.y - (int) screenSpaceComp.getY();
                return; // Found a component, stop here
            }
        }
        // If we clicked empty space, clear selection and start marquee
        selectedComponents.clear();
        selectionRectStart = new Point((int)worldPos.getX(), (int)worldPos.getY());
        selectionRect = new Rectangle(selectionRectStart);
    }
    
    private void handleDeletion(Point2D worldPos) {
        // If multiple components are selected, delete them
        if (!selectedComponents.isEmpty()) {
            commandManager.executeCommand(new RemoveComponentCommand(manager, new ArrayList<>(selectedComponents)));
            selectedComponents.clear();
            return;
        }

        // Otherwise, fall back to single-item deletion
        Wire targetWire = manager.getWireAt((int) worldPos.getX(), (int) worldPos.getY());
        if (targetWire != null) {
            commandManager.executeCommand(new sim.logic.RemoveWireCommand(manager, targetWire));
            return;
        }
        for (CircuitComponent c : manager.getComponents()) {
            if (c.contains((int) worldPos.getX(), (int) worldPos.getY())) {
                commandManager.executeCommand(new RemoveComponentCommand(manager, c));
                return;
            }
        }
    }

    private void placeComponent(Tooltype tool, Point2D worldPos) {
        String id;
        if (tool == Tooltype.SWITCH) {
            long count = manager.getComponents().stream().filter(c -> c instanceof Switch).count();
            id = "S" + (count + 1);
        } else if (tool == Tooltype.LED) {
            long count = manager.getComponents().stream().filter(c -> c instanceof LED).count();
            id = "L" + (count + 1);
        } else {
            id = tool.toString();
        }
        
        int x = (int) worldPos.getX();
        int y = (int) worldPos.getY();

        if (snapToGrid) {
            x = snap(x);
            y = snap(y);
        }

        CircuitComponent newComp = ComponentFactory.create(tool, id, x, y);

        if (newComp != null) {
            if (manager.isSpaceOccupied(newComp)) {
                canvas.showErrorMessage("Space Occupied!");
                return;
            }
            commandManager.executeCommand(new AddComponentCommand(manager, newComp));
        }
    }

    private void handleClickInteraction(Point2D worldPos) {
        for (CircuitComponent c : manager.getComponents()) {
            if (c.contains((int) worldPos.getX(), (int) worldPos.getY())) {
                if (c instanceof Clickable) {
                    ((Clickable) c).click();
                    manager.propagate();
                }
                break;
            }
        }
    }

    private void getStartPin(Point2D worldPos) {
        startPin = manager.getPinAt((int) worldPos.getX(), (int) worldPos.getY(), 10);
        oldWorldPoint = new Point2D.Double(worldPos.getX(), worldPos.getY());
    }

    private int snap(int value) {
        return Math.round((float) value / gridSize) * gridSize;
    }

    public void setSnapToGrid(boolean snap, int size) {
        this.snapToGrid = snap;
        this.gridSize = size;
    }

    // ==================================================================================
    // GETTERS
    // ==================================================================================

    public Point getWirePreviewStart() {
        if (startPin == -1) return null;
        Point2D worldPin = manager.getPointForPin(startPin);
        if (worldPin == null) return null;
        return new Point((int)worldPin.getX(), (int)worldPin.getY());
    }
    public Point getWirePreviewEnd() {
        if (oldWorldPoint == null) return null;
        return new Point((int)oldWorldPoint.getX(), (int)oldWorldPoint.getY());
    }

    public CircuitComponent getSelectedComponent() {
        return selectedComponent;
    }

    public List<CircuitComponent> getSelectedComponents() {
        return selectedComponents;
    }

    public void clearSelection() {
        selectedComponents.clear();
        selectedComponent = null;
        selectionRect = null;
        selectionRectStart = null;
    }

    public Rectangle getSelectionRect() {
        return selectionRect;
    }
}

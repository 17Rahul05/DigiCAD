package sim;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sim.io.CircuitPersistence;
import sim.logic.PropagationEngine;
import sim.logic.SubCircuitManager;
import sim.model.SubCircuit;
import sim.model.Wire;
import sim.util.PinState;

public class CircuitManager {

    // ==================================================================================
    // FIELDS
    // ==================================================================================
    
    private List<CircuitComponent> components = new ArrayList<>();
    private List<Wire> wires = new ArrayList<>();
    private Map<Integer, Point> globalPinMap = new HashMap<>();

    private PropagationEngine propagationEngine = new PropagationEngine();
    private SubCircuitManager subCircuitManager = new SubCircuitManager(this);

    // ==================================================================================
    // COMPONENT MANAGEMENT
    // ==================================================================================

    public void addComponent(CircuitComponent component) {
        components.add(component);
        updatePinRegistry(component);
        propagate();
    }

    public void removeComponenet(CircuitComponent target) {
        if (target == null) return;

        // Find all pins belonging to this component
        List<Integer> allPins = new ArrayList<>();
        allPins.addAll(target.getInputPinIDs());
        allPins.addAll(target.getOutputPinIDs());

        // Remove all wires connected to these pins
        wires.removeIf(w -> allPins.contains(w.getSourcePinID()) ||
                            allPins.contains(w.getDestPinID()));
                        
        // Remove component
        components.remove(target);
        refreshAllPinLocations();
        propagate();
    }

    public boolean isSpaceOccupied(CircuitComponent movingComp) {
        Rectangle currBounds = new Rectangle(movingComp.getX(), movingComp.getY(), movingComp.getWidth(), movingComp.getHeight());

        for (CircuitComponent old : components) {
            if (old == movingComp) continue;
            Rectangle oldBounds = new Rectangle(old.getX(), old.getY(), old.getWidth(), old.getHeight());

            if (currBounds.intersects(oldBounds)) return true;
        }
        return false;
    }

    public void clear() {
        components.clear();
        wires.clear();
        globalPinMap.clear();
    }

    // ==================================================================================
    // SUB-CIRCUIT MANAGEMENT
    // ==================================================================================

    public void createSubCircuit(String name, List<CircuitComponent> componentsToEncapsulate) {
        subCircuitManager.createSubCircuit(name, componentsToEncapsulate);
    }

    public void saveSubCircuitToFile(SubCircuit sub, String filepath) throws IOException {
         try (java.io.FileWriter writer = new java.io.FileWriter(filepath)) {
            writer.write(sim.util.SimpleJson.serialize(sub.getDefinition()));
        }
    }

    public void loadSubCircuitFromFile(String filepath) throws IOException {
        String json = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(filepath)));
        Map<String, Object> definition = sim.util.SimpleJson.parse(json);
        
        if (!definition.containsKey("internalCircuit")) {
            throw new IOException("Invalid sub-circuit file format.");
        }

        SubCircuit sub = new SubCircuit(definition, true);
        sub.setLocation(100, 100); 
        addComponent(sub);
    }

    // ==================================================================================
    // WIRE & CONNECTION LOGIC
    // ==================================================================================

    public String addWire(int sourceID, int destID) {
        return addWire(sourceID, destID, false);
    }

    public String addWire(int sourceID, int destID, boolean orthogonal) {
        String validationError = validateWire(sourceID, destID);
        if (validationError != null) return validationError;

        CircuitComponent srcComp = getComponentByPin(sourceID);

        int outPin = -1;
        int inPin = -1;
        
        if (srcComp.getOutputPinIDs().contains(sourceID)) {
            outPin = sourceID;
            inPin = destID;
        } else {
            outPin = destID;
            inPin = sourceID;
        }

        wires.add(new Wire(outPin, inPin, orthogonal));
        propagate();
        return null;
    }

    public String validateWire(int sourceID, int destID) {
        CircuitComponent srcComp = getComponentByPin(sourceID);
        CircuitComponent destComp = getComponentByPin(destID);

        if (srcComp == null || destComp == null) return "Invalid Pins";

        if (srcComp == destComp) return "INVALID: Cannot connect a component to itself";

        int outPin = -1;
        int inPin = -1;
        
        if (srcComp.getOutputPinIDs().contains(sourceID) && destComp.getInputPinIDs().contains(destID)) {
            outPin = sourceID;
            inPin = destID;
        } else if (srcComp.getInputPinIDs().contains(sourceID) && destComp.getOutputPinIDs().contains(destID)) {
            outPin = destID;
            inPin = sourceID;
        }

        if (outPin == -1 || inPin == -1) return "INVALID: Connection must be between an Output and Input!";

        for (Wire w : wires) {
            if (w.getDestPinID() == inPin) return "INVALID: Input is already driven";
        }
        return null;
    }

    public void addWireDirectly(Wire w) {
        wires.add(w);
    }

    public void removeWire(Wire w) {
        wires.remove(w);
        propagate();
    }

    public Wire getWireAt(int x, int y) {
        for (Wire w : wires) {
            Point p1 = globalPinMap.get(w.getSourcePinID());
            Point p2 = globalPinMap.get(w.getDestPinID());

            if (p1 != null && p2 != null) {
                double minDist;
                if (w.isOrthogonal()) {
                    int midX = (p1.x + p2.x) / 2;
                    double dist1 = distanceToSegment(x, y, p1.x, p1.y, midX, p1.y);
                    double dist2 = distanceToSegment(x, y, midX, p1.y, midX, p2.y);
                    double dist3 = distanceToSegment(x, y, midX, p2.y, p2.x, p2.y);
                    minDist = Math.min(dist1, Math.min(dist2, dist3));
                } else {
                    minDist = distanceToSegment(x, y, p1.x, p1.y, p2.x, p2.y);
                }
                
                if (minDist < 5.0) return w; 
            }
        }
        return null;
    }

    // ==================================================================================
    // SIMULATION STATE
    // ==================================================================================

    public PinState getPinState(int pinID) {
        CircuitComponent owner = getComponentByPin(pinID);
        if (owner == null) return PinState.FLOATING;

        if (owner.getOutputPinIDs().contains(pinID)) {
            return owner.getOutputState(owner.getOutputPinIDs().indexOf(pinID));
        }

        Wire incomingWire = getWireConnectedToInput(pinID);
        if (incomingWire != null) {
            CircuitComponent sourceComponent = getComponentByPin(incomingWire.getSourcePinID());
            if (sourceComponent != null) {
                return sourceComponent.getOutputState(sourceComponent.getOutputPinIDs().indexOf(incomingWire.getSourcePinID()));
            }
        }
        return PinState.FLOATING;
    }

    public void propagate() {
        propagationEngine.propagate(this);
    }

    // ==================================================================================
    // SAVE / LOAD SYSTEM
    // ==================================================================================

    public void save(String filepath) throws IOException {
        CircuitPersistence.save(this, filepath);
    }

    public void load(String filepath) throws IOException {
        CircuitPersistence.load(this, filepath);
    }

    // ==================================================================================
    // HELPERS
    // ==================================================================================

    private void updatePinRegistry(CircuitComponent component) {
        globalPinMap.putAll(component.getPinCoordinates());
    }

    public void refreshAllPinLocations() {
        globalPinMap.clear();
        for (CircuitComponent component : components) {
            globalPinMap.putAll(component.getPinCoordinates());
        }
    }

    public CircuitComponent getComponentByPin(int pinID) { 
        for (CircuitComponent c : components) {
            if (c.getOutputPinIDs().contains(pinID) || c.getInputPinIDs().contains(pinID)) {
                return c;
            }
        }
        return null;
    }

    private Wire getWireConnectedToInput(int inputpinID) {
        for (Wire w : wires) {
            if (w.getDestPinID() == inputpinID) return w;
        }
        return null;
    }

    private double distanceToSegment(int px, int py, int x1, int y1, int x2, int y2) {
        double len = Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2);
        if (len == 0) return Math.sqrt(Math.pow(px - x1, 2) + Math.pow(py - y1, 2));
        double t = Math.clamp(((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1)) / len, 0, 1);
        return Math.sqrt(Math.pow(px - (x1 + t * (x2 - x1)), 2) + Math.pow(py - (y1 + t * (y2 - y1)), 2));
    }

    // ==================================================================================
    // GETTERS
    // ==================================================================================

    public List<CircuitComponent> getComponents() { return components; }
    public List<Wire> getWires() { return wires; }
    public Map<Integer, Point> getPinLocations() { return globalPinMap; }
    public Point getPointForPin(int pinID) { return globalPinMap.get(pinID); }
    
    public int getPinAt(int x, int y, int radius) {
        for (Map.Entry<Integer, Point> entry : getPinLocations().entrySet()) {
            if (entry.getValue().distance(x, y) <= radius) {
                return entry.getKey();
            }
        }
        return -1;
    }
}

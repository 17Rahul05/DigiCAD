package sim.model;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sim.CircuitComponent;
import sim.CircuitManager;
import sim.io.CircuitPersistence;
import sim.util.PinID;
import sim.util.PinState;
import sim.util.Theme;
import sim.util.ThemeManager;

/**
 * A SubCircuit is a CircuitComponent that encapsulates a collection of other
 * components and wires, exposing them as a single block with its own inputs and outputs.
 */
public class SubCircuit extends CircuitComponent {

    private CircuitManager internalManager;
    private List<CircuitComponent> internalComponents;

    // Map external input pins to the internal Switch output pins
    private Map<Integer, Integer> inputPinMapping = new HashMap<>();
    // Map external output pins to the internal LED input pins
    private Map<Integer, Integer> outputPinMapping = new HashMap<>();
    
    // Label map for pins (PinID -> Label String)
    private Map<Integer, String> pinLabels = new HashMap<>();

    // Cached pin coordinates
    private Map<Integer, Point> pinCoordinates = new HashMap<>();

    // The sub-circuit's own output pin states
    private List<PinState> outputStates;

    private static final int PIN_WIDTH = 7;

    public SubCircuit(String id, int x, int y, List<CircuitComponent> componentsToEncapsulate) {
        super(id, x, y, 0, 0); 
        
        this.internalManager = new CircuitManager();
        this.internalComponents = new ArrayList<>(componentsToEncapsulate);
        
        Rectangle bounds = new Rectangle();

        // 1. Find bounding box and identify inputs (Switches) and outputs (LEDs)
        for (CircuitComponent c : this.internalComponents) {
            if (bounds.isEmpty()) {
                bounds = new Rectangle(c.getX(), c.getY(), c.getWidth(), c.getHeight());
            } else {
                bounds.add(new Rectangle(c.getX(), c.getY(), c.getWidth(), c.getHeight()));
            }

            if (c instanceof Switch) {
                int externalPin = PinID.getNextPinID();
                int internalPin = c.getOutputPinIDs().get(0);
                this.inputPinIDs.add(externalPin);
                this.inputPinMapping.put(externalPin, internalPin);
                this.pinLabels.put(externalPin, c.getID());

            } else if (c instanceof LED) {
                int externalPin = PinID.getNextPinID();
                int internalPin = c.getInputPinIDs().get(0);
                this.outputPinIDs.add(externalPin);
                this.outputPinMapping.put(externalPin, internalPin);
                this.pinLabels.put(externalPin, c.getID());
            }
        }
        
        // Initialize own output states
        this.outputStates = new ArrayList<>();
        for (int i = 0; i < this.outputPinIDs.size(); i++) {
            this.outputStates.add(PinState.FLOATING);
        }

        // Set the SubCircuit's own dimensions and position
        final int PIN_SPACING = 25;
        final int MIN_HEIGHT = 60;
        final int FIXED_WIDTH = 120;

        int pinCount = Math.max(this.inputPinIDs.size(), this.outputPinIDs.size());
        this.height = Math.max(MIN_HEIGHT, pinCount * PIN_SPACING);
        this.width = FIXED_WIDTH;

        // Position the new abstract block in the center of the original components
        this.x = bounds.x + bounds.width / 2 - this.width / 2;
        this.y = bounds.y + bounds.height / 2 - this.height / 2;
        
        calculatePinCoordinates();
    }

    // Constructor for Loading from Definition
    @SuppressWarnings("unchecked")
    public SubCircuit(Map<String, Object> definition, boolean reindexPins) {
        super((String) definition.get("id"), 
              ((Number) definition.get("x")).intValue(), 
              ((Number) definition.get("y")).intValue(), 
              ((Number) definition.get("width")).intValue(), 
              ((Number) definition.get("height")).intValue());

        this.internalManager = new CircuitManager();
        
        // 1. Restore Pin Lists with safe casting
        List<Integer> oldInPins = CircuitPersistence.castToIntList(definition.get("inPins"));
        List<Integer> oldOutPins = CircuitPersistence.castToIntList(definition.get("outPins"));
        
        if (reindexPins) {
            // 2. Map for Re-indexing (Old External -> New External)
            Map<Integer, Integer> reindexMap = new HashMap<>();
            
            this.inputPinIDs = new ArrayList<>();
            for (int oldPin : oldInPins) {
                int newPin = PinID.getNextPinID();
                reindexMap.put(oldPin, newPin);
                this.inputPinIDs.add(newPin);
            }
            
            this.outputPinIDs = new ArrayList<>();
            for (int oldPin : oldOutPins) {
                int newPin = PinID.getNextPinID();
                reindexMap.put(oldPin, newPin);
                this.outputPinIDs.add(newPin);
            }

            // 3. Restore mappings with re-indexed external pins
            Map<String, Object> inMap = (Map<String, Object>) definition.get("inputMapping");
            if (inMap != null) {
                for (Map.Entry<String, Object> entry : inMap.entrySet()) {
                    int oldExtPin = Integer.parseInt(entry.getKey());
                    int internalPin = ((Number) entry.getValue()).intValue();
                    if (reindexMap.containsKey(oldExtPin)) {
                        this.inputPinMapping.put(reindexMap.get(oldExtPin), internalPin);
                    }
                }
            }
            
            Map<String, Object> outMap = (Map<String, Object>) definition.get("outputMapping");
            if (outMap != null) {
                for (Map.Entry<String, Object> entry : outMap.entrySet()) {
                    int oldExtPin = Integer.parseInt(entry.getKey());
                    int internalPin = ((Number) entry.getValue()).intValue();
                    if (reindexMap.containsKey(oldExtPin)) {
                        this.outputPinMapping.put(reindexMap.get(oldExtPin), internalPin);
                    }
                }
            }
        } else {
            // Restore Pin IDs and Mappings exactly as they are
            this.inputPinIDs = new ArrayList<>(oldInPins);
            this.outputPinIDs = new ArrayList<>(oldOutPins);

            Map<String, Object> inMap = (Map<String, Object>) definition.get("inputMapping");
            if (inMap != null) {
                for (Map.Entry<String, Object> entry : inMap.entrySet()) {
                    this.inputPinMapping.put(Integer.parseInt(entry.getKey()), ((Number) entry.getValue()).intValue());
                }
            }
            
            Map<String, Object> outMap = (Map<String, Object>) definition.get("outputMapping");
            if (outMap != null) {
                for (Map.Entry<String, Object> entry : outMap.entrySet()) {
                    this.outputPinMapping.put(Integer.parseInt(entry.getKey()), ((Number) entry.getValue()).intValue());
                }
            }
        }

        // Initialize output states
        this.outputStates = new ArrayList<>();
        for (int i = 0; i < this.outputPinIDs.size(); i++) {
            this.outputStates.add(PinState.FLOATING);
        }

        // Restore Internal Circuit
        Map<String, Object> internalData = (Map<String, Object>) definition.get("internalCircuit");
        if (internalData != null) {
            CircuitPersistence.deserialize(this.internalManager, internalData);
        }
        this.internalComponents = this.internalManager.getComponents();
        
        rebuildPinLabels();
        calculatePinCoordinates();
    }

    private void rebuildPinLabels() {
        pinLabels.clear();
        for (Map.Entry<Integer, Integer> entry : inputPinMapping.entrySet()) {
            int extPin = entry.getKey();
            int intPin = entry.getValue();
            CircuitComponent c = internalManager.getComponentByPin(intPin);
            if (c != null) pinLabels.put(extPin, c.getID());
        }
        for (Map.Entry<Integer, Integer> entry : outputPinMapping.entrySet()) {
            int extPin = entry.getKey();
            int intPin = entry.getValue();
            CircuitComponent c = internalManager.getComponentByPin(intPin);
            if (c != null) pinLabels.put(extPin, c.getID());
        }
    }

    public Map<String, Object> getDefinition() {
        Map<String, Object> def = new HashMap<>();
        def.put("id", this.id);
        def.put("x", this.x);
        def.put("y", this.y);
        def.put("width", this.width);
        def.put("height", this.height);
        def.put("inPins", this.inputPinIDs);
        def.put("outPins", this.outputPinIDs);
        def.put("inputMapping", this.inputPinMapping);
        def.put("outputMapping", this.outputPinMapping);
        def.put("internalCircuit", CircuitPersistence.serialize(this.internalManager));
        return def;
    }

    public void initializeInternalCircuit(List<Wire> internalWires) {
        for (CircuitComponent c : internalComponents) {
            this.internalManager.getComponents().add(c);
        }
        for (Wire w : internalWires) {
            this.internalManager.getWires().add(w);
        }
        this.internalManager.propagate();
    }

    public int getExternalPinFor(int internalPinId) {
        for (Map.Entry<Integer, Integer> entry : inputPinMapping.entrySet()) {
            if (entry.getValue().equals(internalPinId)) {
                return entry.getKey();
            }
        }
        for (Map.Entry<Integer, Integer> entry : outputPinMapping.entrySet()) {
            if (entry.getValue().equals(internalPinId)) {
                return entry.getKey();
            }
        }
        return -1; // Not found
    }

    @Override
    public void draw(Graphics2D g2) {
        Theme theme = ThemeManager.getTheme();
        
        // Draw the component body
        g2.setColor(theme.componentBody);
        g2.fillRoundRect(x, y, width, height, 15, 15);
        g2.setColor(theme.componentBorder);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(x, y, width, height, 15, 15);

        // Draw the name (ID)
        g2.setColor(theme.text);
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        int stringWidth = g2.getFontMetrics().stringWidth(id);
        g2.drawString(id, x + (width - stringWidth) / 2, y + g2.getFontMetrics().getAscent());
        
        // Draw Pin Lines and Labels
        g2.setStroke(new BasicStroke(2));
        
        // Using the cached coordinates
        for (int pinId : inputPinIDs) {
            Point p = pinCoordinates.get(pinId);
            if (p != null) {
                g2.setColor(theme.pinLine);
                g2.drawLine(p.x, p.y, p.x + PIN_WIDTH, p.y);
                
                // Draw Label
                String label = pinLabels.get(pinId);
                if (label != null) {
                    g2.setColor(theme.text);
                    g2.setFont(new Font("Arial", Font.PLAIN, 10));
                    g2.drawString(label, x + 5, p.y + 4); 
                }
            }
        }
        for (int pinId : outputPinIDs) {
            Point p = pinCoordinates.get(pinId);
            if (p != null) {
                g2.setColor(theme.pinLine);
                g2.drawLine(p.x - PIN_WIDTH, p.y, p.x, p.y);
                
                // Draw Label
                String label = pinLabels.get(pinId);
                if (label != null) {
                    g2.setColor(theme.text);
                    g2.setFont(new Font("Arial", Font.PLAIN, 10));
                    int lblWidth = g2.getFontMetrics().stringWidth(label);
                    g2.drawString(label, x + width - lblWidth - 5, p.y + 4); 
                }
            }
        }
    }

    @Override
    public PinState getOutputState(int pinIndex) {
        if (pinIndex >= 0 && pinIndex < outputStates.size()) {
            return outputStates.get(pinIndex);
        }
        return PinState.FLOATING;
    }

    @Override
    public boolean updateState(CircuitManager manager) {
        boolean hasChanged = false;

        // Get state of external inputs and update internal Switches
        for (int externalPinId : this.inputPinIDs) {
            PinState externalState = manager.getPinState(externalPinId);
            int internalPinId = inputPinMapping.get(externalPinId);
            
            // Find the switch connected to this internal pin
            for (CircuitComponent c : internalComponents) {
                if (c instanceof Switch && c.getOutputPinIDs().contains(internalPinId)) {
                    ((Switch)c).setState(externalState == PinState.HIGH);
                    break;
                }
            }
        }

        // Propagate changes through the internal circuit
        this.internalManager.propagate();

        // Read state of internal LEDs and update this component's output states
        for (int i = 0; i < this.outputPinIDs.size(); i++) {
            int externalPinId = this.outputPinIDs.get(i);
            int internalPinId = this.outputPinMapping.get(externalPinId);

            PinState internalState = this.internalManager.getPinState(internalPinId);
            
            if (this.outputStates.get(i) != internalState) {
                this.outputStates.set(i, internalState);
                hasChanged = true;
            }
        }

        return hasChanged;
    }
    
    private void calculatePinCoordinates() {
        pinCoordinates.clear();
        // Space input pins along the left edge
        for (int i = 0; i < inputPinIDs.size(); i++) {
            int pinID = inputPinIDs.get(i);
            int pinY = y + (int)((i + 1.0) * height / (inputPinIDs.size() + 1));
            pinCoordinates.put(pinID, new Point(x - PIN_WIDTH, pinY));
        }
        // Space output pins along the right edge
        for (int i = 0; i < outputPinIDs.size(); i++) {
            int pinID = outputPinIDs.get(i);
            int pinY = y + (int)((i + 1.0) * height / (outputPinIDs.size() + 1));
            pinCoordinates.put(pinID, new Point(x + width + PIN_WIDTH, pinY));
        }
    }

    @Override
    public Map<Integer, Point> getPinCoordinates() {
        // Return the pre-calculated map.
        return pinCoordinates;
    }

    @Override
    public void setLocation(int x, int y) {
        super.setLocation(x, y);
        // Recalculate pin positions if the component is ever moved
        calculatePinCoordinates();
    }

    @Override
    public Tooltype getToolType() {
        return Tooltype.SUB_CIRCUIT;
    }
}

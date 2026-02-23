package sim.gates;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Path2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sim.CircuitManager;
import sim.util.PinID;
import sim.util.PinState;
import sim.util.Theme;
import sim.util.ThemeManager;

public class OrGate extends LogicGate {
    
    // --- CONSTANTS ---
    private static final int BODY_WIDTH = 70;
    private static final int BODY_HEIGHT = 60;

    // Geometry for the curved shape
    private static final double CURVE_BACK_RATIO = 0.35;
    private static final double CURVE_FRONT_RATIO = 0.6;
    
    // Pin positions
    private static final int PIN_X_OFFSET = 6;
    private static final int PIN_Y_INPUT_1 = 10;
    private static final int PIN_Y_INPUT_2 = 50;
    private static final int PIN_Y_OUTPUT = BODY_HEIGHT / 2;


    public OrGate(String id, int x, int y) {
        super(id, x, y, BODY_WIDTH, BODY_HEIGHT, "OR");

        // OR Gate has 2 input pins and 1 output pin
        this.inputPinIDs.add(PinID.getNextPinID());
        this.inputPinIDs.add(PinID.getNextPinID());
        this.outputPinIDs.add(PinID.getNextPinID());
    }

    // Loading Constructor
    public OrGate(String id, int x, int y, List<Integer> inPins, List<Integer> outPins) {
        super(id, x, y, BODY_WIDTH, BODY_HEIGHT, "OR", inPins, outPins);
    }

    @Override
    public boolean updateState(CircuitManager manager) {
        PinState in1 = manager.getPinState(inputPinIDs.get(0));
        PinState in2 = manager.getPinState(inputPinIDs.get(1));
        
        PinState newState;
        if (in1 == PinState.HIGH || in2 == PinState.HIGH) {
            newState = PinState.HIGH;
        } else if (in1 == PinState.LOW && in2 == PinState.LOW) {
            newState = PinState.LOW;
        } else {
            newState = PinState.FLOATING;
        }

        boolean hasChanged = (outputState[0] != newState);
        outputState[0] = newState;
        return hasChanged;
    }

    @Override
    public void draw(Graphics2D g2) {
        Theme theme = ThemeManager.getTheme();
        
        // Define key points
        final int rightX = x + BODY_WIDTH;
        final int bottomY = y + BODY_HEIGHT;
        final int midY = y + PIN_Y_OUTPUT;

        // Create the curved shape
        Path2D.Double shape = new Path2D.Double();
        shape.moveTo(x, y);
        shape.quadTo(x + BODY_WIDTH * CURVE_BACK_RATIO, midY, x, bottomY);
        shape.quadTo(x + BODY_WIDTH * CURVE_FRONT_RATIO, bottomY, rightX, midY);
        shape.quadTo(x + BODY_WIDTH * CURVE_FRONT_RATIO, y, x, y);
        shape.closePath();
        
        // Body
        g2.setColor(theme.componentBody);
        g2.fill(shape);
        
        g2.setColor(theme.componentBorder);
        g2.setStroke(new BasicStroke(2));
        g2.draw(shape);

        // Lines for PINs
        g2.setColor(theme.pinLine);
        g2.drawLine(x, y + PIN_Y_INPUT_1, x + PIN_X_OFFSET, y + PIN_Y_INPUT_1);
        g2.drawLine(x, y + PIN_Y_INPUT_2, x + PIN_X_OFFSET, y + PIN_Y_INPUT_2);
        g2.drawLine(rightX, midY, rightX + PIN_WIDTH, midY);
    }

    @Override
    public Map<Integer, Point> getPinCoordinates() {
        Map<Integer, Point> coords = new HashMap<>();

        if (inputPinIDs.size() >= 2) {
            coords.put(inputPinIDs.get(0), new Point(x, y + PIN_Y_INPUT_1));
            coords.put(inputPinIDs.get(1), new Point(x, y + PIN_Y_INPUT_2));
        }
        if (!outputPinIDs.isEmpty()) {
            coords.put(outputPinIDs.get(0), new Point(x + BODY_WIDTH + PIN_WIDTH, y + PIN_Y_OUTPUT));
        }
        return coords;
    }

    @Override
    public sim.model.Tooltype getToolType() {
        return sim.model.Tooltype.OR;
    }
}
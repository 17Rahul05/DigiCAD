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

public class NotGate extends LogicGate {

    // --- CONSTANTS ---
    private static final int INVERSION_DIAMETER = 10;
    private static final int INVERSION_RADIUS = INVERSION_DIAMETER / 2;
    private static final int BODY_WIDTH = 40;
    private static final int BODY_HEIGHT = 40;
    private static final int TOTAL_WIDTH = BODY_WIDTH + INVERSION_DIAMETER;
    
    // Pin positions
    private static final int PIN_Y_MID = BODY_HEIGHT / 2;

    public NotGate(String id, int x, int y) {
        super(id, x, y, TOTAL_WIDTH, BODY_HEIGHT, "NOT");

        // NOT Gate has 1 input pin and 1 output pin
        this.inputPinIDs.add(PinID.getNextPinID());
        this.outputPinIDs.add(PinID.getNextPinID());
    }

    // Loading Constructor
    public NotGate(String id, int x, int y, List<Integer> inPins, List<Integer> outPins) {
        super(id, x, y, TOTAL_WIDTH, BODY_HEIGHT, "NOT", inPins, outPins);
    }

    @Override
    public boolean updateState(CircuitManager manager) {
        PinState in1 = manager.getPinState(inputPinIDs.get(0));
        
        PinState newState;
        if (in1 == PinState.HIGH) {
            newState = PinState.LOW;
        } else if (in1 == PinState.LOW) {
            newState = PinState.HIGH;
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
        
        // Create the triangle shape
        Path2D.Double shape = new Path2D.Double();
        shape.moveTo(x, y);
        shape.lineTo(x + BODY_WIDTH, y + PIN_Y_MID);
        shape.lineTo(x, y + BODY_HEIGHT);
        shape.closePath();

        // Body
        g2.setColor(theme.componentBody);
        g2.fill(shape);
        
        g2.setColor(theme.componentBorder);
        g2.setStroke(new BasicStroke(2));
        g2.draw(shape);

        // Inverter circle
        int circleX = x + BODY_WIDTH;
        int circleY = y + PIN_Y_MID - INVERSION_RADIUS;
        g2.setColor(theme.componentBody);
        g2.fillOval(circleX, circleY, INVERSION_DIAMETER, INVERSION_DIAMETER);
        g2.setColor(theme.componentBorder);
        g2.drawOval(circleX, circleY, INVERSION_DIAMETER, INVERSION_DIAMETER);
        
        // Lines for PINs
        g2.setColor(theme.pinLine);
        g2.drawLine(x - PIN_WIDTH, y + PIN_Y_MID, x, y + PIN_Y_MID); // Input
        g2.drawLine(x + TOTAL_WIDTH, y + PIN_Y_MID, x + TOTAL_WIDTH + PIN_WIDTH, y + PIN_Y_MID); // Output
    }

    @Override
    public Map<Integer, Point> getPinCoordinates() {
        Map<Integer, Point> coords = new HashMap<>();
        coords.put(inputPinIDs.get(0), new Point(x - PIN_WIDTH, y + PIN_Y_MID));
        coords.put(outputPinIDs.get(0), new Point(x + TOTAL_WIDTH + PIN_WIDTH, y + PIN_Y_MID));
        return coords;
    }

    @Override
    public sim.model.Tooltype getToolType() {
        return sim.model.Tooltype.NOT;
    }
}
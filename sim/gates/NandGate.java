package sim.gates;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sim.CircuitManager;
import sim.util.PinID;
import sim.util.PinState;
import sim.util.Theme;
import sim.util.ThemeManager;

public class NandGate extends LogicGate {

    // --- CONSTANTS ---
    private static final int INVERSION_DIAMETER = 10;
    private static final int INVERSION_RADIUS = INVERSION_DIAMETER / 2;
    private static final int BODY_WIDTH = 60;
    private static final int BODY_HEIGHT = 60;
    private static final int TOTAL_WIDTH = BODY_WIDTH + INVERSION_DIAMETER;
    
    // Geometry for the 'D' shape
    private static final int ARC_START_ANGLE = 90;
    private static final int ARC_ANGLE_EXTENT = -180;
    private static final int ARC_OFFSET = BODY_WIDTH / 3;
    private static final double ARC_DIAMETER = (4.0 / 3.0) * BODY_WIDTH;

    // Pin positions
    private static final int PIN_Y_INPUT_1 = 10;
    private static final int PIN_Y_INPUT_2 = 50;
    private static final int PIN_Y_OUTPUT = BODY_HEIGHT / 2;


    public NandGate(String id, int x, int y) {
        super(id, x, y, TOTAL_WIDTH, BODY_HEIGHT, "NAND");

        // NAND Gate has 2 input pins and 1 output pin
        this.inputPinIDs.add(PinID.getNextPinID());
        this.inputPinIDs.add(PinID.getNextPinID());
        this.outputPinIDs.add(PinID.getNextPinID());
    }

    // Loading Constructor
    public NandGate(String id, int x, int y, List<Integer> inPins, List<Integer> outPins) {
        super(id, x, y, TOTAL_WIDTH, BODY_HEIGHT, "NAND", inPins, outPins);
    }

    @Override
    public boolean updateState(CircuitManager manager) {
        PinState in1 = manager.getPinState(inputPinIDs.get(0));
        PinState in2 = manager.getPinState(inputPinIDs.get(1));
        
        PinState newState;
        if (in1 == PinState.LOW || in2 == PinState.LOW) {
            newState = PinState.HIGH;
        } else if (in1 == PinState.HIGH && in2 == PinState.HIGH) {
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
        
        // Create the 'D' shape
        Path2D.Double shape = new Path2D.Double();
        shape.moveTo(x, y);
        shape.lineTo(x + ARC_OFFSET, y);
        shape.append(new Arc2D.Double(x - ARC_OFFSET, y, ARC_DIAMETER, BODY_HEIGHT, ARC_START_ANGLE, ARC_ANGLE_EXTENT, Arc2D.OPEN), true);
        shape.lineTo(x, y + BODY_HEIGHT);
        shape.closePath();

        // Body
        g2.setColor(theme.componentBody);
        g2.fill(shape);
        
        g2.setColor(theme.componentBorder);
        g2.setStroke(new BasicStroke(2));
        g2.draw(shape);

        // Inversion circle
        int circleX = x + BODY_WIDTH;
        int circleY = y + PIN_Y_OUTPUT - INVERSION_RADIUS;
        g2.setColor(theme.componentBody);
        g2.fillOval(circleX, circleY, INVERSION_DIAMETER, INVERSION_DIAMETER);
        g2.setColor(theme.componentBorder);
        g2.drawOval(circleX, circleY, INVERSION_DIAMETER, INVERSION_DIAMETER);

        // Lines for PINs
        g2.setColor(theme.pinLine);
        g2.drawLine(x - PIN_WIDTH, y + PIN_Y_INPUT_1, x, y + PIN_Y_INPUT_1);
        g2.drawLine(x - PIN_WIDTH, y + PIN_Y_INPUT_2, x, y + PIN_Y_INPUT_2);
        g2.drawLine(x + TOTAL_WIDTH, y + PIN_Y_OUTPUT, x + TOTAL_WIDTH + PIN_WIDTH, y + PIN_Y_OUTPUT);
    }

    @Override
    public Map<Integer, Point> getPinCoordinates() {
        Map<Integer, Point> coords = new HashMap<>();
        if (inputPinIDs.size() >= 2) {
            coords.put(inputPinIDs.get(0), new Point(x - PIN_WIDTH, y + PIN_Y_INPUT_1));
            coords.put(inputPinIDs.get(1), new Point(x - PIN_WIDTH, y + PIN_Y_INPUT_2));
        }
        if (!outputPinIDs.isEmpty()) {
            coords.put(outputPinIDs.get(0), new Point(x + TOTAL_WIDTH + PIN_WIDTH, y + PIN_Y_OUTPUT));
        }
        return coords;
    }

    @Override
    public sim.model.Tooltype getToolType() {
        return sim.model.Tooltype.NAND;
    }
}

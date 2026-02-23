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

public class XnorGate extends LogicGate {

    // --- CONSTANTS ---
    private static final int INVERSION_DIAMETER = 10;
    private static final int INVERSION_RADIUS = INVERSION_DIAMETER / 2;
    private static final int BODY_WIDTH = 70;
    private static final int BODY_HEIGHT = 60;
    private static final int TOTAL_WIDTH = BODY_WIDTH + INVERSION_DIAMETER;

    // Geometry for the curved shape
    private static final double CURVE_BACK_RATIO = 0.35;
    private static final double CURVE_FRONT_RATIO = 0.6;
    private static final double CURVE_GAP_RATIO = 0.1;
    
    // Pin positions
    private static final int PIN_X_OFFSET = 7;
    private static final int PIN_Y_INPUT_1 = 12;
    private static final int PIN_Y_INPUT_2 = 48;
    private static final int PIN_Y_OUTPUT = BODY_HEIGHT / 2;


    public XnorGate(String id, int x, int y) {
        super(id, x, y, TOTAL_WIDTH, BODY_HEIGHT, "XNOR");

        // XNOR Gate has 2 input pins and 1 output pin
        this.inputPinIDs.add(PinID.getNextPinID());
        this.inputPinIDs.add(PinID.getNextPinID());
        this.outputPinIDs.add(PinID.getNextPinID());
    }

    // Loading Constructor
    public XnorGate(String id, int x, int y, List<Integer> inPins, List<Integer> outPins) {
        super(id, x, y, TOTAL_WIDTH, BODY_HEIGHT, "XNOR", inPins, outPins);
    }

    @Override
    public boolean updateState(CircuitManager manager) {
        PinState in1 = manager.getPinState(inputPinIDs.get(0));
        PinState in2 = manager.getPinState(inputPinIDs.get(1));
        
        PinState newState;
        if (in1 == PinState.FLOATING || in2 == PinState.FLOATING) {
            newState = PinState.FLOATING;
        } else if (in1 == in2) {
            newState = PinState.HIGH;
        } else {
            newState = PinState.LOW;
        }

        boolean hasChanged = (outputState[0] != newState);
        outputState[0] = newState;
        return hasChanged;
    }

    @Override
    public void draw(Graphics2D g2) {
        Theme theme = ThemeManager.getTheme();
        
        final double gap = BODY_WIDTH * CURVE_GAP_RATIO;
        final int rightX = x + BODY_WIDTH;
        final int bottomY = y + BODY_HEIGHT;
        final int midY = y + PIN_Y_OUTPUT;
        
        // Main body curve
        Path2D.Double shape = new Path2D.Double();
        shape.moveTo(x + gap, y);
        shape.quadTo(x + gap + BODY_WIDTH * CURVE_BACK_RATIO, midY, x + gap, bottomY);
        shape.quadTo(x + gap + BODY_WIDTH * CURVE_FRONT_RATIO, bottomY, rightX, midY);
        shape.quadTo(x + gap + BODY_WIDTH * CURVE_FRONT_RATIO, y, x + gap, y);
        shape.closePath();
        
        // Body
        g2.setColor(theme.componentBody);
        g2.fill(shape);
        
        // Second back curve
        Path2D.Double backCurve = new Path2D.Double();
        backCurve.moveTo(x, y);
        backCurve.quadTo(x + BODY_WIDTH * CURVE_BACK_RATIO, midY, x, bottomY);

        g2.setColor(theme.componentBorder);
        g2.setStroke(new BasicStroke(2));
        g2.draw(shape);
        g2.draw(backCurve);

        // Inversion circle
        int circleX = rightX;
        int circleY = midY - INVERSION_RADIUS;
        g2.setColor(theme.componentBody);
        g2.fillOval(circleX, circleY, INVERSION_DIAMETER, INVERSION_DIAMETER);
        g2.setColor(theme.componentBorder);
        g2.drawOval(circleX, circleY, INVERSION_DIAMETER, INVERSION_DIAMETER);

        // Lines for PINs
        g2.setColor(theme.pinLine);
        g2.drawLine(x - PIN_WIDTH + PIN_X_OFFSET, y + PIN_Y_INPUT_1, x + PIN_X_OFFSET, y + PIN_Y_INPUT_1);
        g2.drawLine(x - PIN_WIDTH + PIN_X_OFFSET, y + PIN_Y_INPUT_2, x + PIN_X_OFFSET, y + PIN_Y_INPUT_2);
        g2.drawLine(x + TOTAL_WIDTH, midY, x + TOTAL_WIDTH + PIN_WIDTH, midY);
    }

    @Override
    public Map<Integer, Point> getPinCoordinates() {
        Map<Integer, Point> coords = new HashMap<>();
        if (inputPinIDs.size() >= 2) {
            coords.put(inputPinIDs.get(0), new Point(x - PIN_WIDTH + PIN_X_OFFSET, y + PIN_Y_INPUT_1));
            coords.put(inputPinIDs.get(1), new Point(x - PIN_WIDTH + PIN_X_OFFSET, y + PIN_Y_INPUT_2));
        }
        if (!outputPinIDs.isEmpty()) {
            coords.put(outputPinIDs.get(0), new Point(x + TOTAL_WIDTH + PIN_WIDTH, y + PIN_Y_OUTPUT));
        }
        return coords;
    }

    @Override
    public sim.model.Tooltype getToolType() {
        return sim.model.Tooltype.XNOR;
    }
}
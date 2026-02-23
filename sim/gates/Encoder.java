package sim.gates;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Path2D;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sim.CircuitManager;
import sim.util.PinID;
import sim.util.PinState;
import sim.util.Theme;
import sim.util.ThemeManager;
import sim.model.Tooltype;

public class Encoder extends LogicGate {

    // --- CONSTANTS ---
    private static final int BODY_WIDTH = 60;
    private static final int BODY_HEIGHT = 100;

    // Geometry
    private static final int TRAP_TOP = BODY_HEIGHT * 2 / 10;
    private static final int TRAP_BOT = BODY_HEIGHT * 8 / 10;
    
    // Pin positions
    private static final int INPUT_0 = 20;
    private static final int INPUT_1 = 40;
    private static final int INPUT_2 = 60;
    private static final int INPUT_3 = 80;

    private static final int OUTPUT_0 = 30;
    private static final int OUTPUT_1 = 50;
    private static final int OUTPUT_V = 70; // For Validation that at least one input is on

    public Encoder(String id, int x, int y) {
        super(id, x, y, BODY_WIDTH, BODY_HEIGHT, "ENCODER");

        // This is a 4x2 Priority Encoder with a valid bit
        for (int i = 0; i < 4; i++) this.inputPinIDs.add(PinID.getNextPinID());
        for (int i = 0; i < 3; i++) this.outputPinIDs.add(PinID.getNextPinID());

        this.outputState = new PinState[3];
        Arrays.fill(this.outputState, PinState.FLOATING);
    }

    // Loading Constructor
    public Encoder(String id, int x, int y, List<Integer> inPins, List<Integer> outPins) {
        super(id, x, y, BODY_WIDTH, BODY_HEIGHT, "ENCODER", inPins, outPins);
        this.outputState = new PinState[outPins.size()];
        Arrays.fill(this.outputState, PinState.FLOATING);
    }

    @Override
    public boolean updateState(CircuitManager manager) {
        PinState i0 = manager.getPinState(inputPinIDs.get(0));
        PinState i1 = manager.getPinState(inputPinIDs.get(1));
        PinState i2 = manager.getPinState(inputPinIDs.get(2));
        PinState i3 = manager.getPinState(inputPinIDs.get(3));

        PinState[] oldState = Arrays.copyOf(outputState, 3);
        
        // Priority Encoder Logic
        if (i3 == PinState.HIGH) {
            outputState[0] = PinState.HIGH; // Y0
            outputState[1] = PinState.HIGH; // Y1
            outputState[2] = PinState.HIGH; // V
        } else if (i2 == PinState.HIGH) {
            outputState[0] = PinState.LOW; // Y0
            outputState[1] = PinState.HIGH;  // Y1
            outputState[2] = PinState.HIGH; // V
        } else if (i1 == PinState.HIGH) {
            outputState[0] = PinState.HIGH;  // Y0
            outputState[1] = PinState.LOW; // Y1
            outputState[2] = PinState.HIGH; // V
        } else if (i0 == PinState.HIGH) {
            outputState[0] = PinState.LOW;  // Y0
            outputState[1] = PinState.LOW;  // Y1
            outputState[2] = PinState.HIGH; // V
        } else if (i0 == PinState.FLOATING || i1 == PinState.FLOATING || i2 == PinState.FLOATING || i3 == PinState.FLOATING) {
            Arrays.fill(outputState, PinState.FLOATING);
        }
        else { // All are LOW
            outputState[0] = PinState.FLOATING;
            outputState[1] = PinState.FLOATING;
            outputState[2] = PinState.LOW;
        }

        return !Arrays.equals(outputState, oldState);
    }

    @Override
    public void draw(Graphics2D g2) {
        Theme theme = ThemeManager.getTheme();

        // Inverted Trapezoid Shape (same as Decoder but mirrored)
        Path2D.Double shape = new Path2D.Double();
        shape.moveTo(x + BODY_WIDTH, y + TRAP_TOP);
        shape.lineTo(x, y);
        shape.lineTo(x, y + BODY_HEIGHT);
        shape.lineTo(x + BODY_WIDTH, y + TRAP_BOT);
        shape.closePath();

        // Body
        g2.setColor(theme.componentBody);
        g2.fill(shape);

        g2.setColor(theme.componentBorder);
        g2.setStroke(new BasicStroke(2));
        g2.draw(shape);

        // Lines for PINs
        g2.setColor(theme.pinLine);
        // INPUTs
        g2.drawLine(x - PIN_WIDTH, y + INPUT_0, x, y + INPUT_0);
        g2.drawLine(x - PIN_WIDTH, y + INPUT_1, x, y + INPUT_1);
        g2.drawLine(x - PIN_WIDTH, y + INPUT_2, x, y + INPUT_2);
        g2.drawLine(x - PIN_WIDTH, y + INPUT_3, x, y + INPUT_3);

        // OUTPUTs
        g2.drawLine(x + BODY_WIDTH, y + OUTPUT_0, x + BODY_WIDTH + PIN_WIDTH, y + OUTPUT_0);
        g2.drawLine(x + BODY_WIDTH, y + OUTPUT_1, x + BODY_WIDTH + PIN_WIDTH, y + OUTPUT_1);
        g2.drawLine(x + BODY_WIDTH, y + OUTPUT_V, x + BODY_WIDTH + PIN_WIDTH, y + OUTPUT_V);
    }

    @Override
    public Map<Integer, Point> getPinCoordinates() {
        Map<Integer, Point> coords = new HashMap<>();
        if (inputPinIDs.size() >= 4) {
            coords.put(inputPinIDs.get(0), new Point(x - PIN_WIDTH, y + INPUT_0));
            coords.put(inputPinIDs.get(1), new Point(x - PIN_WIDTH, y + INPUT_1));
            coords.put(inputPinIDs.get(2), new Point(x - PIN_WIDTH, y + INPUT_2));
            coords.put(inputPinIDs.get(3), new Point(x - PIN_WIDTH, y + INPUT_3));
        }
        if (outputPinIDs.size() >= 3) {
            coords.put(outputPinIDs.get(0), new Point(x + BODY_WIDTH + PIN_WIDTH, y + OUTPUT_0));
            coords.put(outputPinIDs.get(1), new Point(x + BODY_WIDTH + PIN_WIDTH, y + OUTPUT_1));
            coords.put(outputPinIDs.get(2), new Point(x + BODY_WIDTH + PIN_WIDTH, y + OUTPUT_V));
        }
        return coords;
    }

    @Override
    public Tooltype getToolType() {
        return Tooltype.ENCODER;
    }
}
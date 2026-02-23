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

public class Decoder extends LogicGate{

    // --- CONSTANTS ---
    private static final int BODY_WIDTH = 60;
    private static final int BODY_HEIGHT = 100;

    // Geometry
    private static final int TRAP_TOP = BODY_HEIGHT * 2 / 10;
    private static final int TRAP_BOT = BODY_HEIGHT * 8 / 10;
    
    // Pin positions
    private static final int INPUT_A = 35;
    private static final int INPUT_B = 65;
    private static final int OUTPUT_0 = 5;
    private static final int OUTPUT_1 = 35;
    private static final int OUTPUT_2 = 65;
    private static final int OUTPUT_3 = 95;

    public Decoder(String id, int x, int y) {
        super(id, x, y, BODY_WIDTH, BODY_HEIGHT, "DECODER");

        // This is a 2x4 Decoder Gate
        this.inputPinIDs.add(PinID.getNextPinID());
        this.inputPinIDs.add(PinID.getNextPinID());
        for (int i = 0; i < 4; i++) this.outputPinIDs.add(PinID.getNextPinID());

        this.outputState = new PinState[4];
    }

    // Loading Constructor
    public Decoder(String id, int x, int y, List<Integer> inPins, List<Integer> outPins) {
        super(id, x, y, BODY_WIDTH, BODY_HEIGHT, "DECODER", inPins, outPins);
        this.outputState = new PinState[outPins.size()];
    }

    @Override
    public boolean updateState(CircuitManager manager) {
        PinState bit0 = manager.getPinState(inputPinIDs.get(0));
        PinState bit1 = manager.getPinState(inputPinIDs.get(1));

        PinState[] oldState = Arrays.copyOf(outputState, 4);

        if (bit0 == PinState.FLOATING || bit1 == PinState.FLOATING) {
            Arrays.fill(outputState, PinState.FLOATING);
        } else {
            boolean b0 = (bit0 == PinState.HIGH);
            boolean b1 = (bit1 == PinState.HIGH);
            outputState[0] = (!b1 && !b0) ? PinState.HIGH : PinState.LOW;
            outputState[1] = (!b1 && b0) ? PinState.HIGH : PinState.LOW;
            outputState[2] = (b1 && !b0) ? PinState.HIGH : PinState.LOW;
            outputState[3] = (b1 && b0) ? PinState.HIGH : PinState.LOW;
        }

        return !Arrays.equals(outputState, oldState);
    }

    @Override
    public void draw(Graphics2D g2) {
        Theme theme = ThemeManager.getTheme();

        // Inverted Trapezoid Shape
        Path2D.Double shape = new Path2D.Double();
        shape.moveTo(x, y + TRAP_TOP);
        shape.lineTo(x + BODY_WIDTH, y);
        shape.lineTo(x + BODY_WIDTH, y + BODY_HEIGHT);
        shape.lineTo(x, y + TRAP_BOT);
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
        g2.drawLine(x - PIN_WIDTH, y + INPUT_A, x, y + INPUT_A);
        g2.drawLine(x - PIN_WIDTH, y + INPUT_B, x, y + INPUT_B);
        // OUTPUTs
        g2.drawLine(x + BODY_WIDTH, y + OUTPUT_0, x + BODY_WIDTH + PIN_WIDTH, y + OUTPUT_0);
        g2.drawLine(x + BODY_WIDTH, y + OUTPUT_1, x + BODY_WIDTH + PIN_WIDTH, y + OUTPUT_1);
        g2.drawLine(x + BODY_WIDTH, y + OUTPUT_2, x + BODY_WIDTH + PIN_WIDTH, y + OUTPUT_2);
        g2.drawLine(x + BODY_WIDTH, y + OUTPUT_3, x + BODY_WIDTH + PIN_WIDTH, y + OUTPUT_3);
    }

    @Override
    public Map<Integer, Point> getPinCoordinates() {
        Map<Integer, Point> coords = new HashMap<>();
        if (inputPinIDs.size() >= 2) {
            coords.put(inputPinIDs.get(0), new Point(x - PIN_WIDTH, y + INPUT_A));
            coords.put(inputPinIDs.get(1), new Point(x - PIN_WIDTH, y + INPUT_B));
        }
        if (outputPinIDs.size() >= 4) {
            coords.put(outputPinIDs.get(0), new Point(x + BODY_WIDTH + PIN_WIDTH, y + OUTPUT_0));
            coords.put(outputPinIDs.get(1), new Point(x + BODY_WIDTH + PIN_WIDTH, y + OUTPUT_1));
            coords.put(outputPinIDs.get(2), new Point(x + BODY_WIDTH + PIN_WIDTH, y + OUTPUT_2));
            coords.put(outputPinIDs.get(3), new Point(x + BODY_WIDTH + PIN_WIDTH, y + OUTPUT_3));
        }
        return coords;
    }

    @Override
    public sim.model.Tooltype getToolType() {
        return sim.model.Tooltype.DECODER;
    }
}

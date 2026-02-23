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

public class MuxGate extends LogicGate {

    // --- CONSTANTS ---
    private static final int BODY_WIDTH = 60;
    private static final int BODY_HEIGHT = 80;

    // Geometry
    private static final int TRAP_TOP = BODY_HEIGHT * 2 / 10;
    private static final int TRAP_BOT = BODY_HEIGHT * 8 / 10;
    
    // Pin positions
    private static final int PIN_Y_INPUT_A = 20;
    private static final int PIN_Y_INPUT_B = 60;
    private static final int PIN_X_SELECT = BODY_WIDTH / 2;
    private static final int PIN_Y_SELECT = BODY_HEIGHT * 9 / 10;
    private static final int PIN_Y_OUTPUT = BODY_HEIGHT / 2;


    public MuxGate(String id, int x, int y) {
        super(id, x, y, BODY_WIDTH, BODY_HEIGHT, "MUX");

        // MUX has 3 input pins and 1 output pin
        // Order: [Input A, Input B, Select]
        this.inputPinIDs.add(PinID.getNextPinID());
        this.inputPinIDs.add(PinID.getNextPinID());
        this.inputPinIDs.add(PinID.getNextPinID());
        this.outputPinIDs.add(PinID.getNextPinID());
    }

    // Loading Constructor
    public MuxGate(String id, int x, int y, List<Integer> inPins, List<Integer> outPins) {
        super(id, x, y, BODY_WIDTH, BODY_HEIGHT, "MUX", inPins, outPins);
    }

    @Override
    public boolean updateState(CircuitManager manager) {
        PinState inputA = manager.getPinState(inputPinIDs.get(0));
        PinState inputB = manager.getPinState(inputPinIDs.get(1));
        PinState select = manager.getPinState(inputPinIDs.get(2));
        
        PinState newState;
        if (select == PinState.FLOATING) {
            newState = PinState.FLOATING;
        } else if (select == PinState.LOW) {
            newState = inputA;
        } else { // select == PinState.HIGH
            newState = inputB;
        }

        boolean hasChanged = (outputState[0] != newState);
        outputState[0] = newState;
        return hasChanged;
    }

    @Override
    public void draw(Graphics2D g2) {
        Theme theme = ThemeManager.getTheme();
        
        // Create the trapezoid shape
        Path2D.Double shape = new Path2D.Double();
        shape.moveTo(x, y);
        shape.lineTo(x + BODY_WIDTH, y + TRAP_TOP);
        shape.lineTo(x + BODY_WIDTH, y + TRAP_BOT);
        shape.lineTo(x, y + BODY_HEIGHT);
        shape.closePath();

        // Body
        g2.setColor(theme.componentBody);
        g2.fill(shape);
        
        g2.setColor(theme.componentBorder);
        g2.setStroke(new BasicStroke(2));
        g2.draw(shape);

        // Lines for PINs
        g2.setColor(theme.pinLine);
        // Inputs
        g2.drawLine(x - PIN_WIDTH, y + PIN_Y_INPUT_A, x, y + PIN_Y_INPUT_A);
        g2.drawLine(x - PIN_WIDTH, y + PIN_Y_INPUT_B, x, y + PIN_Y_INPUT_B);
        // Select
        g2.drawLine(x + PIN_X_SELECT, y + PIN_Y_SELECT, x + PIN_X_SELECT, y + BODY_HEIGHT);
        // Output
        g2.drawLine(x + BODY_WIDTH, y + PIN_Y_OUTPUT, x + BODY_WIDTH + PIN_WIDTH, y + PIN_Y_OUTPUT);
    }

    @Override
    public Map<Integer, Point> getPinCoordinates() {
        Map<Integer, Point> coords = new HashMap<>();
        if (inputPinIDs.size() >= 3) {
            coords.put(inputPinIDs.get(0), new Point(x - PIN_WIDTH, y + PIN_Y_INPUT_A)); // Pin A
            coords.put(inputPinIDs.get(1), new Point(x - PIN_WIDTH, y + PIN_Y_INPUT_B)); // Pin B
            coords.put(inputPinIDs.get(2), new Point(x + PIN_X_SELECT, y + BODY_HEIGHT)); // Select Pin
        }
        if (!outputPinIDs.isEmpty()) {
            coords.put(outputPinIDs.get(0), new Point(x + BODY_WIDTH + PIN_WIDTH, y + PIN_Y_OUTPUT)); // Output Pin
        }
        return coords;
    }

    @Override
    public sim.model.Tooltype getToolType() {
        return sim.model.Tooltype.MUX;
    }
}

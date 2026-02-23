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

public class DemuxGate extends LogicGate {

    // --- CONSTANTS ---
    private static final int BODY_WIDTH = 60;
    private static final int BODY_HEIGHT = 80;
    
    // Geometry
    private static final int TRAP_TOP = BODY_HEIGHT * 2 / 10;
    private static final int TRAP_BOT = BODY_HEIGHT * 8 / 10;
    
    // Pin positions
    private static final int PIN_Y_INPUT = BODY_HEIGHT / 2;
    private static final int PIN_X_SELECT = BODY_WIDTH / 2;
    private static final int PIN_Y_SELECT = BODY_HEIGHT * 9 / 10;
    private static final int PIN_Y_OUTPUT_0 = 15;
    private static final int PIN_Y_OUTPUT_1 = 65;


    public DemuxGate(String id, int x, int y) {
        super(id, x, y, BODY_WIDTH, BODY_HEIGHT, "DEMUX");

        // DEMUX has 2 input pins [IN, SEL] and 2 output pins [Y0, Y1]
        this.inputPinIDs.add(PinID.getNextPinID());
        this.inputPinIDs.add(PinID.getNextPinID());
        this.outputPinIDs.add(PinID.getNextPinID());
        this.outputPinIDs.add(PinID.getNextPinID());
        
        this.outputState = new PinState[2];
    }

    // Loading Constructor
    public DemuxGate(String id, int x, int y, List<Integer> inPins, List<Integer> outPins) {
        super(id, x, y, BODY_WIDTH, BODY_HEIGHT, "DEMUX", inPins, outPins);
        
        this.outputState = new PinState[outPins.size()];
    }

    @Override
    public boolean updateState(CircuitManager manager) {
        PinState input = manager.getPinState(inputPinIDs.get(0));
        PinState select = manager.getPinState(inputPinIDs.get(1));
        
        PinState oldState0 = outputState[0];
        PinState oldState1 = outputState[1];

        if (select == PinState.FLOATING || input == PinState.FLOATING) {
            outputState[0] = PinState.FLOATING;
            outputState[1] = PinState.FLOATING;
        } else if (select == PinState.LOW) {
            // If select is 0, Y0 = IN, Y1 = 0
            outputState[0] = input;
            outputState[1] = PinState.LOW;
        } else {
            // If select is 1, Y0 = 0, Y1 = IN
            outputState[0] = PinState.LOW;
            outputState[1] = input;
        }
        
        // Return true if either output changed
        return (outputState[0] != oldState0) || (outputState[1] != oldState1);
    }

    @Override
    public void draw(Graphics2D g2) {
        Theme theme = ThemeManager.getTheme();
        
        // Create the inverted trapezoid shape
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
        // Input
        g2.drawLine(x - PIN_WIDTH, y + PIN_Y_INPUT, x, y + PIN_Y_INPUT);
        // Select
        g2.drawLine(x + PIN_X_SELECT, y + PIN_Y_SELECT, x + PIN_X_SELECT, y + BODY_HEIGHT);
        // Outputs
        g2.drawLine(x + BODY_WIDTH, y + PIN_Y_OUTPUT_0, x + BODY_WIDTH + PIN_WIDTH, y + PIN_Y_OUTPUT_0);
        g2.drawLine(x + BODY_WIDTH, y + PIN_Y_OUTPUT_1, x + BODY_WIDTH + PIN_WIDTH, y + PIN_Y_OUTPUT_1);
    }

    @Override
    public Map<Integer, Point> getPinCoordinates() {
        Map<Integer, Point> coords = new HashMap<>();
        if (inputPinIDs.size() >= 2) {
            coords.put(inputPinIDs.get(0), new Point(x - PIN_WIDTH, y + PIN_Y_INPUT)); // Pin IN
            coords.put(inputPinIDs.get(1), new Point(x + PIN_X_SELECT, y + BODY_HEIGHT)); // Select Pin
        }
        if (outputPinIDs.size() >= 2) {
            coords.put(outputPinIDs.get(0), new Point(x + BODY_WIDTH + PIN_WIDTH, y + PIN_Y_OUTPUT_0)); // Output Y0
            coords.put(outputPinIDs.get(1), new Point(x + BODY_WIDTH + PIN_WIDTH, y + PIN_Y_OUTPUT_1)); // Output Y1
        }
        return coords;
    }

    @Override
    public sim.model.Tooltype getToolType() {
        return sim.model.Tooltype.DEMUX;
    }
}

package sim.gates;

import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import sim.CircuitComponent;
import sim.util.PinState;

public abstract class LogicGate extends CircuitComponent {
    
    protected static final int PIN_WIDTH = 10;

    protected static final int IN1 = 10; // Aligned to 10px snap grid
    protected static final int IN2 = 50; // Aligned to 10px snap grid

    protected String label;
    protected PinState[] outputState;

    public LogicGate(String id, int x, int y, int width, int height, String label) {
        super(id, x, y, width, height);
        this.label = label;
        this.outputState = new PinState[1]; // Initialize for one output
        Arrays.fill(this.outputState, PinState.FLOATING);
    }

    // Constructor for Loading
    public LogicGate(String id, int x, int y, int width, int height, String label, List<Integer> inPins, List<Integer> outPins) {
        super(id, x, y, width, height, inPins, outPins);
        this.label = label;
        this.outputState = new PinState[outPins.size()]; // Initialize based on loaded data
        Arrays.fill(this.outputState, PinState.FLOATING);
    }

    @Override
    public PinState getOutputState(int pinIndex) {
        if (pinIndex >= 0 && pinIndex < outputState.length) {
            return outputState[pinIndex];
        }
        return PinState.FLOATING;
    }

    public abstract void draw(Graphics2D g2);

    public abstract Map<Integer, Point> getPinCoordinates();
}
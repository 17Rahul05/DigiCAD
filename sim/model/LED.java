package sim.model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sim.CircuitManager;
import sim.util.PinID;
import sim.util.PinState;
import sim.util.Theme;
import sim.util.ThemeManager;

public class LED extends SinkSource {

    // --- CONSTANTS ---
    protected static final int WIDTH = 30;
    protected static final int HEIGHT = 30;

    private static final Color LED_COLOR_ON = Color.YELLOW;
    private static final Color GLOW_COLOR_OUTER = new Color(255, 200, 0, 50);
    private static final Color GLOW_COLOR_INNER = new Color(255, 200, 0, 100);
    private static final int GLOW_OFFSET_OUTER = 5;
    private static final int GLOW_OFFSET_INNER = 2;
    private static final int LABEL_Y_OFFSET = 5;
    private static final int PIN_Y_MID = HEIGHT / 2;
    private static final int PIN_X_OFFSET = 10;


    private PinState state = PinState.FLOATING;

    // LED is a sink
    // It has 1 input pin
    // Output directly connected to ground
    public LED(String id, int x, int y) {
        super(id, x, y, WIDTH, HEIGHT);
        this.inputPinIDs.add(PinID.getNextPinID());
    }

    // Loading Constructor
    public LED(String id, int x, int y, List<Integer> inPins, List<Integer> outPins) {
        super(id, x, y, WIDTH, HEIGHT);
        this.inputPinIDs = inPins;
        this.outputPinIDs = outPins; // Usually empty for LED
    }

    // Set state for simulation purposes
    public void setState(PinState state) {
        this.state = state;
    }

    @Override
    public boolean updateState(CircuitManager manager) {
        if (inputPinIDs.isEmpty()) return false;

        PinState newState = manager.getPinState(inputPinIDs.get(0));
        boolean hasChanged = (this.state != newState);
        this.state = newState;
        return hasChanged;
    }

    @Override
    public void draw(Graphics2D g2) {
        Theme theme = ThemeManager.getTheme();
        
        switch (state) {
            case HIGH:
                // Outer Glow
                g2.setColor(GLOW_COLOR_OUTER);
                g2.fillOval(x - GLOW_OFFSET_OUTER, y - GLOW_OFFSET_OUTER, WIDTH + (2 * GLOW_OFFSET_OUTER), HEIGHT + (2 * GLOW_OFFSET_OUTER));
                
                // Inner Glow
                g2.setColor(GLOW_COLOR_INNER);
                g2.fillOval(x - GLOW_OFFSET_INNER, y - GLOW_OFFSET_INNER, WIDTH + (2 * GLOW_OFFSET_INNER), HEIGHT + (2 * GLOW_OFFSET_INNER));

                // Core LED
                g2.setColor(LED_COLOR_ON);
                g2.fillOval(x, y, WIDTH, HEIGHT);
                break;
            case LOW:
                g2.setColor(theme.ledOff);
                g2.fillOval(x, y, WIDTH, HEIGHT);
                g2.setColor(theme.componentBorder);
                g2.setStroke(new BasicStroke(1));
                g2.drawOval(x, y, WIDTH, HEIGHT);
                break;
            case FLOATING:
                g2.setColor(theme.ledFloating);
                g2.fillOval(x, y, WIDTH, HEIGHT);
                g2.setColor(theme.componentBorder);
                g2.setStroke(new BasicStroke(1));
                g2.drawOval(x, y, WIDTH, HEIGHT);
                break;
        }

        // Input Pin Lines
        g2.setColor(theme.pinLine);
        g2.setStroke(new BasicStroke(2));
        g2.drawLine(x - PIN_WIDTH, y + PIN_Y_MID, x, y + PIN_Y_MID);
        
        // Label
        g2.setColor(theme.text);
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.drawString(id, x, y - LABEL_Y_OFFSET);
    }

    @Override
    public Map<Integer, Point> getPinCoordinates() {
        Map<Integer, Point> coords = new HashMap<>();
        if (!inputPinIDs.isEmpty()) {
            coords.put(inputPinIDs.get(0), new Point(x - PIN_X_OFFSET, y + PIN_Y_MID));
        }
        return coords;
    }

    @Override
    public PinState getOutputState(int pinIndex) {
        // An LED has no output pins, this is just to satisfy the abstract class.
        return state;
    }

    @Override
    public Tooltype getToolType() {
        return Tooltype.LED;
    }
}
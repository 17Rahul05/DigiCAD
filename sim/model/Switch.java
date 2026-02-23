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
import sim.util.Clickable;
import sim.util.PinID;
import sim.util.PinState;
import sim.util.Theme;
import sim.util.ThemeManager;

public class Switch extends SinkSource implements Clickable {

    // --- CONSTANTS ---
    protected static final int WIDTH = 30;
    protected static final int HEIGHT = 30;

    private static final int CORNER_ARC = 10;
    private static final int HANDLE_PADDING = 2;
    private static final int LABEL_Y_OFFSET = 5;

    private boolean state = false;

    // Switch is a source
    // It has 1 output pin
    // Input directly connected to VCC
    public Switch(String id, int x, int y) {
        super(id, x, y, WIDTH, HEIGHT);
        this.outputPinIDs.add(PinID.getNextPinID());
    }

    // Loading Constructor
    public Switch(String id, int x, int y, List<Integer> inPins, List<Integer> outPins) {
        super(id, x, y, WIDTH, HEIGHT);
        this.inputPinIDs = inPins; // Usually empty for Switch
        this.outputPinIDs = outPins;
        
        if (this.outputPinIDs.isEmpty()) {
            this.outputPinIDs.add(PinID.getNextPinID());
        }
    }

    public void setState(boolean state) {
        this.state = state;
    }

    // Toggle Switch
    @Override
    public void click() {
        state = !state;
    }

    @Override
    public boolean updateState(CircuitManager manager) {
        // A Switch is a root input, its state is only changed by click(),
        // so it never changes during propagation.
        return false;
    }

    @Override
    public void draw(Graphics2D g2) {
        Theme theme = ThemeManager.getTheme();
        
        // Draw the switch body (base)
        g2.setColor(theme.componentBody);
        g2.fillRoundRect(x, y, WIDTH, HEIGHT, CORNER_ARC, CORNER_ARC);
        g2.setColor(theme.componentBorder);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(x, y, WIDTH, HEIGHT, CORNER_ARC, CORNER_ARC);

        // Define colors for ON/OFF states
        Color onColor = new Color(0, 180, 0); // Brighter green
        Color offColor = new Color(180, 0, 0); // Brighter red
        
        int handleWidth = WIDTH / 2;
        int handleX = state ? (x + WIDTH - handleWidth - HANDLE_PADDING) : (x + HANDLE_PADDING);

        // Draw the handle
        g2.setColor(state ? onColor : offColor);
        g2.fillRoundRect(handleX, y + HANDLE_PADDING, handleWidth, HEIGHT - (2 * HANDLE_PADDING), CORNER_ARC, CORNER_ARC);
        g2.setColor(theme.componentBorder.darker());
        g2.drawRoundRect(handleX, y + HANDLE_PADDING, handleWidth, HEIGHT - (2 * HANDLE_PADDING), CORNER_ARC, CORNER_ARC);
        
        // Label
        g2.setColor(theme.text);
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.drawString(id, x + WIDTH / 2 - g2.getFontMetrics().stringWidth(id) / 2, y - LABEL_Y_OFFSET);

        // Output pin Line
        g2.setColor(theme.pinLine);
        g2.drawLine(x + WIDTH, y + (HEIGHT / 2), x + WIDTH + PIN_WIDTH, y + (HEIGHT / 2));
    }

    @Override
    public Map<Integer, Point> getPinCoordinates() {
        Map<Integer, Point> coords = new HashMap<>();
        coords.put(outputPinIDs.get(0), new Point(x + WIDTH + PIN_WIDTH, y + (HEIGHT / 2)));
        return coords;
    }

    @Override
    public PinState getOutputState(int pinIndex) {
        return state ? PinState.HIGH : PinState.LOW;
    }

    @Override
    public Tooltype getToolType() {
        return Tooltype.SWITCH;
    }
}
package sim.model;

import java.awt.BasicStroke;
import java.awt.Color;
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

public class SevenSegmentDisplay extends SinkSource {

    // --- CONSTANTS ---
    private static final int PIN_WIDTH = 5;

    private static final int SEGMENT_THICKNESS = 8;
    private static final int SEGMENT_LENGTH = 32;
    private static final int SEGMENT_GAP = 1;
    private static final int OUTER_GAP = 5;

    private static final int WIDTH = SEGMENT_LENGTH + SEGMENT_THICKNESS + 2 * (SEGMENT_GAP + OUTER_GAP);
    private static final int HEIGHT = (2 * SEGMENT_LENGTH) + SEGMENT_THICKNESS + (4 * SEGMENT_GAP) + (2 * OUTER_GAP);

    private static final int INPUT_TLR = (HEIGHT / 2) - SEGMENT_GAP - (SEGMENT_LENGTH / 2);
    private static final int INPUT_BLR = (HEIGHT / 2) + SEGMENT_GAP + (SEGMENT_LENGTH / 2);
    private static final int INPUT_BOT = (WIDTH / 2);
    private static final int INPUT_TOP = (WIDTH / 3);
    private static final int INPUT_MID = (WIDTH * 2 / 3);


    private static final Color SEGMENT_ON_COLOR = new Color(230, 0, 0);
    private static final Color SEGMENT_GLOW_COLOR_OUTER = new Color(230, 0, 0, 50);
    private static final Color SEGMENT_GLOW_COLOR_INNER = new Color(230, 0, 0, 100);



    private PinState[] segmentStates = new PinState[7];
    private Path2D[] segments = new Path2D[7];

    public SevenSegmentDisplay(String id, int x, int y) {
        super(id, x, y, WIDTH, HEIGHT);
        for (int i = 0; i < 7; i++) {
            this.inputPinIDs.add(PinID.getNextPinID());
        }
        Arrays.fill(segmentStates, PinState.LOW);
        createSegmentPolygons();
    }

    public SevenSegmentDisplay(String id, int x, int y, List<Integer> inPins, List<Integer> outPins) {
        super(id, x, y, WIDTH, HEIGHT);
        this.inputPinIDs = inPins;
        this.outputPinIDs = outPins; // Empty for a Seven Segment Display
        Arrays.fill(segmentStates, PinState.LOW);
        createSegmentPolygons();
    }

    private void createSegmentPolygons() {
        int st = SEGMENT_THICKNESS;
        int sl = SEGMENT_LENGTH;
        int sg = SEGMENT_GAP;
        int og = OUTER_GAP;

        // --- Calculate Coordinate Anchors ---
        
        // Horizontal X positions (The 'tip' of the horizontal hexagon)
        // x + gap + half_thickness (so the pointy end fits between verticals)
        int xHorz = x + og + sg + (st / 2);

        // Vertical X positions
        int xVertLeft  = x + og;
        int xVertRight = x + og + sl + (sg * 2);

        // Y positions
        // Top Row (Segment A)
        int yTopHorz = y + og;
        
        // Upper Vertical Row (Segments F, B) - starts below top segment's center
        int yTopVert = y + og + sg + (st / 2);
        
        // Middle Row (Segment G)
        int yMidHorz = y + og + (2 * sg) + sl;
        
        // Lower Vertical Row (Segments E, C)
        int yBotVert = y + og + (3 * sg) + (st / 2) + sl;
        
        // Bottom Row (Segment D)
        int yBotHorz = y + og + (4 * sg) + 2 * sl;


        // 0: a (Top)
        segments[0] = createSegment(xHorz, yTopHorz, true); 
        
        // 1: b (Top Right)
        segments[1] = createSegment(xVertRight, yTopVert, false); 
        
        // 2: c (Bottom Right)
        segments[2] = createSegment(xVertRight, yBotVert, false); 
        
        // 3: d (Bottom)
        segments[3] = createSegment(xHorz, yBotHorz, true); 
        
        // 4: e (Bottom Left)
        segments[4] = createSegment(xVertLeft, yBotVert, false); 
        
        // 5: f (Top Left)
        segments[5] = createSegment(xVertLeft, yTopVert, false); 
        
        // 6: g (Middle)
        segments[6] = createSegment(xHorz, yMidHorz, true); 
    }

    private Path2D createSegment(int startX, int startY, boolean isHorizontal) {
        int st = SEGMENT_THICKNESS;
        int sl = SEGMENT_LENGTH;
        
        Path2D segment = new Path2D.Double();
        
        if (isHorizontal) {
            // Horizontal Hexagon
            // startX, startY is the LEFT-most point (the tip)
            segment.moveTo(startX, startY + st / 2);        // Left Tip
            segment.lineTo(startX + st / 2, startY);        // Top Left
            segment.lineTo(startX + sl - st / 2, startY);   // Top Right
            segment.lineTo(startX + sl, startY + st / 2);   // Right Tip
            segment.lineTo(startX + sl - st / 2, startY + st); // Bot Right
            segment.lineTo(startX + st / 2, startY + st);   // Bot Left
        } else {
            // Vertical Hexagon
            // startX, startY is the TOP-most point (the tip)
            segment.moveTo(startX + st / 2, startY);        // Top Tip
            segment.lineTo(startX + st, startY + st / 2);   // Top Right
            segment.lineTo(startX + st, startY + sl - st / 2); // Bot Right
            segment.lineTo(startX + st / 2, startY + sl);   // Bot Tip
            segment.lineTo(startX, startY + sl - st / 2);   // Bot Left
            segment.lineTo(startX, startY + st / 2);        // Top Left
        }
        segment.closePath();
        return segment;
    }
    
    @Override
    public void draw(Graphics2D g2) {
        Theme theme = ThemeManager.getTheme();

        // Border
        g2.setColor(theme.componentBody);
        g2.fillRect(x, y, WIDTH, HEIGHT);

        g2.setColor(theme.componentBorder);
        g2.drawRect(x, y, WIDTH, HEIGHT);
        

        // Segments
        for (int i = 0; i < 7; i++) {
            if (segmentStates[i] == PinState.HIGH) {

                // Draw glow effect for individual segment
                g2.setColor(SEGMENT_GLOW_COLOR_OUTER);
                g2.setStroke(new BasicStroke(2)); // Thicker stroke for outer glow
                g2.draw(segments[i]);

                g2.setColor(SEGMENT_GLOW_COLOR_INNER);
                g2.setStroke(new BasicStroke(1)); // Thinner stroke for inner glow
                g2.draw(segments[i]);
                
                // Set color for the actual segment
                g2.setColor(SEGMENT_ON_COLOR);
                g2.fill(segments[i]); // Fill the segment
                g2.setStroke(new BasicStroke((float) 0.5)); // Reset stroke for subsequent draws

            } else if (segmentStates[i] == PinState.LOW) {

                g2.setColor(theme.ledOff);
                g2.fill(segments[i]);
                g2.setStroke(new BasicStroke(1)); // Ensure solid stroke for off segments

            } else { // FLOATING

                g2.setColor(theme.ledFloating);
                g2.fill(segments[i]);
                g2.setStroke(new BasicStroke(1)); // Ensure solid stroke for floating segments

            }
        }

        // INPUT Pin Lines
        g2.setColor(theme.pinLine);
        g2.setStroke(new BasicStroke(1));
        g2.drawLine(x + INPUT_TOP, y - PIN_WIDTH, x + INPUT_TOP, y); // a
        g2.drawLine(x + WIDTH, y + INPUT_TLR, x + WIDTH + PIN_WIDTH, y + INPUT_TLR); // b
        g2.drawLine(x + WIDTH, y + INPUT_BLR, x + WIDTH + PIN_WIDTH, y + INPUT_BLR); // c
        g2.drawLine(x + INPUT_BOT, y + HEIGHT, x + INPUT_BOT, y + HEIGHT + PIN_WIDTH); // d
        g2.drawLine(x - PIN_WIDTH, y + INPUT_BLR, x, y + INPUT_BLR); // e
        g2.drawLine(x - PIN_WIDTH, y + INPUT_TLR, x, y + INPUT_TLR); // f
        g2.drawLine(x + INPUT_MID, y - PIN_WIDTH, x + INPUT_MID, y); // g

    }

    @Override
    public boolean updateState(CircuitManager manager) {
        PinState[] oldStates = Arrays.copyOf(segmentStates, 7);
        for (int i = 0; i < 7; i++) {
            segmentStates[i] = manager.getPinState(inputPinIDs.get(i));
        }
        return !Arrays.equals(segmentStates, oldStates);
    }

    @Override
    public Map<Integer, Point> getPinCoordinates() {
        Map<Integer, Point> coords = new HashMap<>();
        
        if (inputPinIDs.size() >= 7) {
            coords.put(inputPinIDs.get(0), new Point(x + INPUT_TOP, y - PIN_WIDTH)); // a
            coords.put(inputPinIDs.get(1), new Point(x + WIDTH + PIN_WIDTH, y + INPUT_TLR)); // b
            coords.put(inputPinIDs.get(2), new Point(x + WIDTH + PIN_WIDTH, y + INPUT_BLR)); // c
            coords.put(inputPinIDs.get(3), new Point(x + INPUT_BOT, y + HEIGHT + PIN_WIDTH)); // d
            coords.put(inputPinIDs.get(4), new Point(x - PIN_WIDTH, y + INPUT_BLR)); // e
            coords.put(inputPinIDs.get(5), new Point(x - PIN_WIDTH, y + INPUT_TLR)); // f
            coords.put(inputPinIDs.get(6), new Point(x + INPUT_MID, y - PIN_WIDTH)); // g
        }

        return coords;
    }
    
    @Override
    public void setLocation(int x, int y) {
        super.setLocation(x, y);
        createSegmentPolygons();
    }

    @Override
    public Tooltype getToolType() {
        return Tooltype.SEVEN_SEGMENT;
    }
    
    @Override
    public PinState getOutputState(int pinIndex) {
        return PinState.FLOATING; // No outputs
    }
}

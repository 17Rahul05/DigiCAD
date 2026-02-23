package sim;

import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import sim.model.Tooltype;
import sim.util.PinState;

public abstract class CircuitComponent {
    
    // Component Position and ID
    protected int x;
    protected int y;
    protected String id;
    protected int width;
    protected int height;

    // I/O Pins
    protected List<Integer> inputPinIDs;
    protected List<Integer> outputPinIDs;

    // Constructor
    public CircuitComponent(String id, int x, int y, int width, int height) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.inputPinIDs = new ArrayList<>();
        this.outputPinIDs = new ArrayList<>();
    }

    // Constructor for Loading (Explicit IDs)
    public CircuitComponent(String id, int x, int y, int width, int height, List<Integer> inPins, List<Integer> outPins) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.inputPinIDs = inPins;
        this.outputPinIDs = outPins;
    }

    // Draw Componenet
    public abstract void draw(Graphics2D g2);

    // Get Output State
    public abstract PinState getOutputState(int pinIndex);

    // Update internal state based on inputs. Return true if output state changed
    public abstract boolean updateState(CircuitManager manager);

    // Pin Locations
    public abstract Map<Integer, Point> getPinCoordinates();

    // Get the Tooltype of the component
    public abstract Tooltype getToolType();

    // Getters and Setters
    public String getID() {return this.id;}
    public int getX() {return this.x;}
    public int getY() {return this.y;}
    public int getWidth() {return width;}
    public int getHeight() {return height;}
    public List<Integer> getInputPinIDs() {return inputPinIDs;}
    public List<Integer> getOutputPinIDs() {return outputPinIDs;}
    

    public void setID(String id) {
        this.id = id;
    }

    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setLocation(Point point) {
        this.x = point.x;
        this.y = point.y;
    }

    public boolean contains(int mx, int my) {
        return mx >= x && mx <= x + width &&
               my >= y && my <= y + height;
    }
}
package sim.model;

import sim.CircuitComponent;

public abstract class SinkSource extends CircuitComponent {

    protected static final int PIN_WIDTH = 10;

    public SinkSource(String id, int x, int y, int width, int height) {
        super(id, x, y, width, height);
    }
}

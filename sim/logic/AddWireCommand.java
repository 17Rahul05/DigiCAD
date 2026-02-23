package sim.logic;

import sim.CircuitManager;
import sim.model.Wire;

public class AddWireCommand implements Command {
    private final CircuitManager manager;
    private final Wire wire;

    public AddWireCommand(CircuitManager manager, Wire wire) {
        this.manager = manager;
        this.wire = wire;
    }

    @Override
    public void execute() {
        manager.addWireDirectly(wire);
        manager.propagate();
    }

    @Override
    public void undo() {
        manager.removeWire(wire);
    }
}

package sim.logic;

import sim.CircuitManager;
import sim.model.Wire;

public class RemoveWireCommand implements Command {
    private final CircuitManager manager;
    private final Wire wire;

    public RemoveWireCommand(CircuitManager manager, Wire wire) {
        this.manager = manager;
        this.wire = wire;
    }

    @Override
    public void execute() {
        manager.removeWire(wire);
    }

    @Override
    public void undo() {
        manager.addWireDirectly(wire);
        manager.propagate();
    }
}

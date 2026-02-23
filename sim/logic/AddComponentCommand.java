package sim.logic;

import sim.CircuitComponent;
import sim.CircuitManager;

public class AddComponentCommand implements Command {
    private final CircuitManager manager;
    private final CircuitComponent component;

    public AddComponentCommand(CircuitManager manager, CircuitComponent component) {
        this.manager = manager;
        this.component = component;
    }

    @Override
    public void execute() {
        manager.addComponent(component);
    }

    @Override
    public void undo() {
        manager.removeComponenet(component);
    }
}

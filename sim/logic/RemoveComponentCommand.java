package sim.logic;

import java.util.ArrayList;
import java.util.List;
import sim.CircuitComponent;
import sim.CircuitManager;
import sim.model.Wire;

public class RemoveComponentCommand implements Command {
    private final CircuitManager manager;
    private final List<CircuitComponent> components;
    private final List<Wire> removedWires = new ArrayList<>();

    public RemoveComponentCommand(CircuitManager manager, CircuitComponent component) {
        this.manager = manager;
        this.components = new ArrayList<>();
        this.components.add(component);
    }

    public RemoveComponentCommand(CircuitManager manager, List<CircuitComponent> components) {
        this.manager = manager;
        this.components = new ArrayList<>(components);
    }

    @Override
    public void execute() {
        removedWires.clear();
        for (CircuitComponent component : components) {
            // Collect wires that will be removed by manager.removeComponenet
            List<Integer> allPins = new ArrayList<>();
            allPins.addAll(component.getInputPinIDs());
            allPins.addAll(component.getOutputPinIDs());
            
            for (Wire w : manager.getWires()) {
                if (allPins.contains(w.getSourcePinID()) || allPins.contains(w.getDestPinID())) {
                    if (!removedWires.contains(w)) {
                        removedWires.add(w);
                    }
                }
            }
            manager.removeComponenet(component);
        }
    }

    @Override
    public void undo() {
        for (CircuitComponent component : components) {
            manager.addComponent(component);
        }
        for (Wire w : removedWires) {
            manager.addWireDirectly(w);
        }
        manager.propagate();
    }
}

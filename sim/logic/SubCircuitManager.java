package sim.logic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sim.CircuitComponent;
import sim.CircuitManager;
import sim.model.SubCircuit;
import sim.model.Wire;

public class SubCircuitManager {

    private final CircuitManager manager;

    public SubCircuitManager(CircuitManager manager) {
        this.manager = manager;
    }

    public void createSubCircuit(String name, List<CircuitComponent> componentsToEncapsulate) {
        if (componentsToEncapsulate == null || componentsToEncapsulate.isEmpty()) return;
        
        // Create the SubCircuit object. This will define its pins and dimensions.
        SubCircuit newSub = new SubCircuit(name, 0, 0, componentsToEncapsulate);

        // Partition wires into internal and external lists.
        Set<String> encapsulatedIds = new HashSet<>();
        for (CircuitComponent c : componentsToEncapsulate) {
            encapsulatedIds.add(c.getID());
        }

        List<Wire> internalWires = new ArrayList<>();
        List<Wire> externalWires = new ArrayList<>();
        
        // Access wires via manager
        List<Wire> allWires = manager.getWires();

        for (Wire w : allWires) {
            CircuitComponent src = manager.getComponentByPin(w.getSourcePinID());
            CircuitComponent dest = manager.getComponentByPin(w.getDestPinID());

            boolean srcIsInternal = src != null && encapsulatedIds.contains(src.getID());
            boolean destIsInternal = dest != null && encapsulatedIds.contains(dest.getID());

            if (srcIsInternal && destIsInternal) {
                internalWires.add(w);
            } else if (srcIsInternal || destIsInternal) {
                externalWires.add(w);
            }
        }

        // Initialize the sub-circuit's internal state.
        newSub.initializeInternalCircuit(internalWires);

        // Remove the original components. This will also remove all wires attached to them.
        // We do this after finding the wires we need to preserve.
        for (CircuitComponent c : List.copyOf(componentsToEncapsulate)) {
            manager.removeComponenet(c);
        }

        // Add the new SubCircuit component to the main circuit.
        manager.addComponent(newSub);

        // Re-wire the external connections to the new sub-circuit block.
        for (Wire w : externalWires) {
            CircuitComponent src = manager.getComponentByPin(w.getSourcePinID());

            boolean srcIsExternal = src != null && !encapsulatedIds.contains(src.getID());

            if (srcIsExternal) {
                // Wire comes from outside into the sub-circuit
                int newDestPin = newSub.getExternalPinFor(w.getDestPinID());
                if (newDestPin != -1) manager.addWire(w.getSourcePinID(), newDestPin);
            } else {
                // Wire goes from inside the sub-circuit to the outside
                int newSourcePin = newSub.getExternalPinFor(w.getSourcePinID());
                if (newSourcePin != -1) manager.addWire(newSourcePin, w.getDestPinID());
            }
        }

        // Finalize
        manager.refreshAllPinLocations();
        manager.propagate();
    }
}

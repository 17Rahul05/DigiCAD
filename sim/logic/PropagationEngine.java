package sim.logic;

import sim.CircuitComponent;
import sim.CircuitManager;

public class PropagationEngine {

    public void propagate(CircuitManager manager) {
        boolean circuitHasChanged = true;
        int iterations = 0;
        final int maxIterations = 100; // Safety break for oscillating circuits

        while (circuitHasChanged && iterations < maxIterations) {
            circuitHasChanged = false;
            for (CircuitComponent component : manager.getComponents()) {
                if (component.updateState(manager)) {
                    circuitHasChanged = true;
                }
            }
            iterations++;
        }

        if (iterations >= maxIterations) {
            System.err.println("Warning: Propagation exceeded max iterations. Possible oscillating circuit.");
        }
    }
}

package sim.logic;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import sim.CircuitComponent;
import sim.CircuitManager;

public class MoveComponentCommand implements Command {
    private final CircuitManager manager;
    private final List<MoveInfo> moves = new ArrayList<>();

    public static class MoveInfo {
        public final CircuitComponent component;
        public final Point oldPos;
        public final Point newPos;

        public MoveInfo(CircuitComponent component, Point oldPos, Point newPos) {
            this.component = component;
            this.oldPos = oldPos;
            this.newPos = newPos;
        }
    }

    public MoveComponentCommand(CircuitManager manager, List<MoveInfo> moves) {
        this.manager = manager;
        this.moves.addAll(moves);
    }

    @Override
    public void execute() {
        for (MoveInfo move : moves) {
            move.component.setLocation(move.newPos);
        }
        manager.refreshAllPinLocations();
    }

    @Override
    public void undo() {
        for (MoveInfo move : moves) {
            move.component.setLocation(move.oldPos);
        }
        manager.refreshAllPinLocations();
    }
}

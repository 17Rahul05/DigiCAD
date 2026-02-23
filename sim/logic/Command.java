package sim.logic;

public interface Command {
    void execute();
    void undo();
}

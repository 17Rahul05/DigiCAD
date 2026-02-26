package sim;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import sim.actions.ActionHandler;
import sim.logic.CommandManager;
import sim.model.Tooltype;
import sim.ui.CanvasPanel;
import sim.ui.MouseController;
import sim.ui.menu.SimulatorMenuBar;
import sim.ui.menu.SimulatorToolbar;
import sim.util.KeyBindingManager;

public class Simulate extends JFrame {

    private CanvasPanel canvas;
    private Tooltype currentTool = Tooltype.SELECT;
    private CircuitManager manager = new CircuitManager();
    private CommandManager commandManager = new CommandManager();
    private MouseController mc;
    private SimulatorToolbar toolbar;
    private ActionHandler actionHandler;
    
    private boolean snapToGrid = false;
    private static final int GRID_SIZE = 20;

    public Simulate() {
        setupWindow();
        setupCanvas();
        setupMenuBar();
        setupToolbar();
        setupKeyBindings();
    }

    private void setupWindow() {
        this.setTitle("DigiCAD");
        this.setSize(1000, 700);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
    }

    private void setupCanvas() {
        canvas = new CanvasPanel(manager);
        canvas.setCommandManager(commandManager);
        mc = new MouseController(manager, canvas, this::getCurrentTool, commandManager);
        actionHandler = new ActionHandler(this, manager, canvas, mc, commandManager);
        mc.setActionHandler(actionHandler);
        canvas.setController(mc);
        canvas.addMouseListener(mc);
        canvas.addMouseMotionListener(mc);
        canvas.addMouseWheelListener(mc);
        this.add(canvas, BorderLayout.CENTER);
    }

    private void setupMenuBar() {
        JMenuBar menuBar;
        menuBar = new SimulatorMenuBar(actionHandler, commandManager, canvas);
        this.setJMenuBar(menuBar);
    }

    private void setupToolbar() {
        toolbar = new SimulatorToolbar(
            this::getCurrentTool, 
            this::setCurrentTool, 
            this, 
            manager, 
            canvas
        );
        this.add(toolbar.createToolbar(), BorderLayout.WEST);
    }

    private void setupKeyBindings() {
        KeyBindingManager.setupKeyBindings(
            canvas, 
            commandManager, 
            actionHandler, 
            mc, 
            this::setCurrentTool, 
            () -> setSnapToGrid(!snapToGrid)
        );
    }

    public Tooltype getCurrentTool() { return currentTool; }
    
    public void setCurrentTool(Tooltype tool) {
        this.currentTool = tool;
        if (toolbar != null) {
            toolbar.refreshToolButtons();
        }
    }

    public void setSnapToGrid(boolean snap) {
        this.snapToGrid = snap;
        mc.setSnapToGrid(snap, 10); // Snap at 10px
        canvas.setGridVisible(snap, GRID_SIZE); // Keep 20px visual grid
        canvas.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Simulate().setVisible(true);
        });
    }
}


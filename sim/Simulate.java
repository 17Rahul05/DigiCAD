package sim;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import sim.actions.ActionHandler;
import sim.logic.CommandManager;
import sim.model.Tooltype;
import sim.ui.CanvasPanel;
import sim.ui.MouseController;
import sim.ui.menu.SimulatorToolbar;

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
        setupToolbar();
        setupKeyBindings();
    }

    private void setupWindow() {
        this.setTitle("Digital Circuit Simulator");
        this.setSize(1000, 700);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
    }

    private void setupCanvas() {
        canvas = new CanvasPanel(manager);
        canvas.setCommandManager(commandManager);
        mc = new MouseController(manager, canvas, this::getCurrentTool, commandManager);
        canvas.setController(mc);
        canvas.addMouseListener(mc);
        canvas.addMouseMotionListener(mc);
        canvas.addMouseWheelListener(mc);
        this.add(canvas, BorderLayout.CENTER);
    }

    private void setupToolbar() {
        actionHandler = new ActionHandler(this, manager, canvas, mc, commandManager);
        toolbar = new SimulatorToolbar(
            this::getCurrentTool, 
            this::setCurrentTool, 
            actionHandler, 
            this, 
            manager, 
            canvas
        );
        this.add(toolbar.createToolbar(), BorderLayout.WEST);
    }

    private void setupKeyBindings() {
        InputMap inputMap = canvas.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = canvas.getActionMap();

        // Undo/Redo
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "undo");
        actionMap.put("undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                commandManager.undo();
                canvas.repaint();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), "redo");
        actionMap.put("redo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                commandManager.redo();
                canvas.repaint();
            }
        });

        // Delete actions
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "delete");
        actionMap.put("delete", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionHandler.performDelete();
            }
        });

        // File actions
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), "save");
        actionMap.put("save", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionHandler.performSave();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK), "load");
        actionMap.put("load", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionHandler.performLoad();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK), "export");
        actionMap.put("export", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionHandler.performExport();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK), "import");
        actionMap.put("import", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionHandler.performImport();
            }
        });

        // Toggle Grid Snapping
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK), "toggleGrid");
        actionMap.put("toggleGrid", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setSnapToGrid(!snapToGrid);
            }
        });

        // Tool Selection
        addToolBinding(inputMap, actionMap, KeyEvent.VK_S, Tooltype.SELECT, "selectTool");
        addToolBinding(inputMap, actionMap, KeyEvent.VK_L, Tooltype.LED, "ledTool");
        addToolBinding(inputMap, actionMap, KeyEvent.VK_W, Tooltype.WIRE, "wireTool");
        addToolBinding(inputMap, actionMap, KeyEvent.VK_A, Tooltype.AND, "andTool");
        addToolBinding(inputMap, actionMap, KeyEvent.VK_O, Tooltype.OR, "orTool");
        addToolBinding(inputMap, actionMap, KeyEvent.VK_N, Tooltype.NOT, "notTool");
        addToolBinding(inputMap, actionMap, KeyEvent.VK_X, Tooltype.XOR, "xorTool");
        addToolBinding(inputMap, actionMap, KeyEvent.VK_T, Tooltype.TOGGLE, "toggleTool");
        addToolBinding(inputMap, actionMap, KeyEvent.VK_D, Tooltype.DELETE, "deleteTool");

        // Escape to clear selection and reset tool
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
        actionMap.put("escape", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mc.clearSelection();
                setCurrentTool(Tooltype.SELECT);
                canvas.repaint();
            }
        });
    }

    private void addToolBinding(InputMap im, ActionMap am, int keyCode, Tooltype tool, String actionName) {
        im.put(KeyStroke.getKeyStroke(keyCode, 0), actionName);
        am.put(actionName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setCurrentTool(tool);
            }
        });
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
        mc.setSnapToGrid(snap, GRID_SIZE);
        canvas.setGridVisible(snap, GRID_SIZE);
        canvas.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Simulate().setVisible(true);
        });
    }
}


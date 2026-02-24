package sim;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;

import sim.actions.ActionHandler;
import sim.logic.CommandManager;
import sim.model.Tooltype;
import sim.ui.CanvasPanel;
import sim.ui.MouseController;
import sim.ui.menu.QuickAddMenu;
import sim.ui.menu.SimulatorToolbar;
import sim.util.ThemeManager;

public class Simulate extends JFrame {

    private CanvasPanel canvas;
    private Tooltype currentTool = Tooltype.SELECT;
    private CircuitManager manager = new CircuitManager();
    private CommandManager commandManager = new CommandManager();
    private MouseController mc;
    private SimulatorToolbar toolbar;
    private ActionHandler actionHandler;
    private JMenuBar menuBar;

    private boolean snapToGrid = false;
    private static final int GRID_SIZE = 20;

    public Simulate() {
        setupWindow();
        actionHandler = new ActionHandler(this, manager, canvas, mc, commandManager);
        setupCanvas();
        mc.setActionHandler(actionHandler);
        setupMenuBar();
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
        // Ensure action handler has the correct mc reference
        actionHandler = new ActionHandler(this, manager, canvas, mc, commandManager);
        mc.setActionHandler(actionHandler);
        canvas.setController(mc);
        canvas.addMouseListener(mc);
        canvas.addMouseMotionListener(mc);
        canvas.addMouseWheelListener(mc);
        this.add(canvas, BorderLayout.CENTER);
    }

    private void setupMenuBar() {
        menuBar = new JMenuBar();

        // File Menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem saveItem = new JMenuItem("Save Circuit");
        saveItem.addActionListener(e -> actionHandler.performSave());
        JMenuItem loadItem = new JMenuItem("Load Circuit");
        loadItem.addActionListener(e -> actionHandler.performLoad());
        JMenuItem importItem = new JMenuItem("Import Sub-Circuit");
        importItem.addActionListener(e -> actionHandler.performImport());
        JMenuItem exportItem = new JMenuItem("Export Sub-Circuit");
        exportItem.addActionListener(e -> actionHandler.performExport());

        fileMenu.add(saveItem);
        fileMenu.add(loadItem);
        fileMenu.addSeparator();
        fileMenu.add(importItem);
        fileMenu.add(exportItem);
        menuBar.add(fileMenu);

        // Edit Menu
        JMenu editMenu = new JMenu("Edit");
        JMenuItem undoItem = new JMenuItem("Undo");
        undoItem.addActionListener(e -> {
            commandManager.undo();
            canvas.repaint();
        });
        JMenuItem redoItem = new JMenuItem("Redo");
        redoItem.addActionListener(e -> {
            commandManager.redo();
            canvas.repaint();
        });
        JMenuItem deleteItem = new JMenuItem("Delete Selection");
        deleteItem.addActionListener(e -> actionHandler.performDelete());
        
        editMenu.add(undoItem);
        editMenu.add(redoItem);
        editMenu.addSeparator();
        editMenu.add(deleteItem);
        menuBar.add(editMenu);
        
        // Theme Menu
        JMenu themeMenu = new JMenu("Theme");
        for (String themeName : ThemeManager.getThemeNames()) {
            JMenuItem themeItem = new JMenuItem(themeName);
            themeItem.addActionListener(e -> ThemeManager.setTheme(themeName));
            themeMenu.add(themeItem);
        }
        menuBar.add(themeMenu);

        ThemeManager.addThemeListener(this::refreshMenuAppearance);
        refreshMenuAppearance();

        this.setJMenuBar(menuBar);
    }

    private void refreshMenuAppearance() {
        Color bg = ThemeManager.getTheme().bg;
        double brightness = (bg.getRed() * 0.299 + bg.getGreen() * 0.587 + bg.getBlue() * 0.114);
        
        Color menuColor;
        if (brightness > 128) {
            // Light theme: make it slightly darker than bg
            menuColor = new Color(Math.max(0, bg.getRed() - 15), Math.max(0, bg.getGreen() - 15), Math.max(0, bg.getBlue() - 15));
        } else {
            // Dark theme: make it slightly lighter than bg
            menuColor = new Color(Math.min(255, bg.getRed() + 20), Math.min(255, bg.getGreen() + 20), Math.min(255, bg.getBlue() + 20));
        }

        menuBar.setBackground(menuColor);
        menuBar.setBorder(new LineBorder(ThemeManager.getTheme().componentBorder, 1));
        styleMenuComponents(menuBar, menuColor);
    }

    private void styleMenuComponents(Component container, Color menuColor) {
        if (container instanceof JMenuBar) {
            for (Component c : ((JMenuBar) container).getComponents()) {
                styleMenuComponents(c, menuColor);
            }
        } else if (container instanceof JMenu) {
            JMenu menu = (JMenu) container;
            menu.setBackground(menuColor);
            menu.setForeground(ThemeManager.getTheme().text);
            menu.setOpaque(true);
            
            JPopupMenu popup = menu.getPopupMenu();
            popup.setBackground(ThemeManager.getTheme().bg);
            popup.setBorder(new LineBorder(ThemeManager.getTheme().componentBorder, 1));
            
            for (Component c : popup.getComponents()) {
                styleMenuComponents(c, menuColor);
            }
        } else if (container instanceof JMenuItem) {
            JMenuItem item = (JMenuItem) container;
            item.setBackground(ThemeManager.getTheme().bg);
            item.setForeground(ThemeManager.getTheme().text);
            item.setOpaque(true);
        }
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

        // Quick Add Menu
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "quickAdd");
        actionMap.put("quickAdd", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                QuickAddMenu.show(canvas, tool -> setCurrentTool(tool));
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


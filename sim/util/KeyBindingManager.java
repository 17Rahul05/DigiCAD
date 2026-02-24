package sim.util;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import sim.actions.ActionHandler;
import sim.logic.CommandManager;
import sim.model.Tooltype;
import sim.ui.CanvasPanel;
import sim.ui.MouseController;
import sim.ui.menu.QuickAddMenu;

public class KeyBindingManager {

    private KeyBindingManager() {
        /* Utility class */
    }

    public static void setupKeyBindings(
        CanvasPanel canvas, 
        CommandManager commandManager, 
        ActionHandler actionHandler, 
        MouseController mc, 
        Consumer<Tooltype> toolSetter,
        Runnable toggleGridAction
    ) {
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
                toggleGridAction.run();
            }
        });

        // Quick Add Menu
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "quickAdd");
        actionMap.put("quickAdd", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                QuickAddMenu.show(canvas, toolSetter);
            }
        });

        // Tool Selection
        addToolBinding(inputMap, actionMap, KeyEvent.VK_S, Tooltype.SELECT, "selectTool", toolSetter);
        addToolBinding(inputMap, actionMap, KeyEvent.VK_L, Tooltype.LED, "ledTool", toolSetter);
        addToolBinding(inputMap, actionMap, KeyEvent.VK_W, Tooltype.WIRE, "wireTool", toolSetter);
        addToolBinding(inputMap, actionMap, KeyEvent.VK_A, Tooltype.AND, "andTool", toolSetter);
        addToolBinding(inputMap, actionMap, KeyEvent.VK_O, Tooltype.OR, "orTool", toolSetter);
        addToolBinding(inputMap, actionMap, KeyEvent.VK_N, Tooltype.NOT, "notTool", toolSetter);
        addToolBinding(inputMap, actionMap, KeyEvent.VK_X, Tooltype.XOR, "xorTool", toolSetter);
        addToolBinding(inputMap, actionMap, KeyEvent.VK_T, Tooltype.TOGGLE, "toggleTool", toolSetter);
        addToolBinding(inputMap, actionMap, KeyEvent.VK_D, Tooltype.DELETE, "deleteTool", toolSetter);

        // Escape to clear selection and reset tool
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
        actionMap.put("escape", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mc.clearSelection();
                toolSetter.accept(Tooltype.SELECT);
                canvas.repaint();
            }
        });
    }

    private static void addToolBinding(InputMap im, ActionMap am, int keyCode, Tooltype tool, String actionName, Consumer<Tooltype> toolSetter) {
        im.put(KeyStroke.getKeyStroke(keyCode, 0), actionName);
        am.put(actionName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toolSetter.accept(tool);
            }
        });
    }
}
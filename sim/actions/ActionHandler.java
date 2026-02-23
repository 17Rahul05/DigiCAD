package sim.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import sim.CircuitComponent;
import sim.CircuitManager;
import sim.logic.CommandManager;
import sim.logic.RemoveComponentCommand;
import sim.model.SubCircuit;
import sim.ui.CanvasPanel;
import sim.ui.MouseController;

public class ActionHandler {
    private final JFrame parentFrame;
    private final CircuitManager manager;
    private final CanvasPanel canvas;
    private final MouseController mouseController;
    private final CommandManager commandManager;
    private File lastDirectory;

    public ActionHandler(JFrame parentFrame, CircuitManager manager, CanvasPanel canvas, MouseController mouseController, CommandManager commandManager) {
        this.parentFrame = parentFrame;
        this.manager = manager;
        this.canvas = canvas;
        this.mouseController = mouseController;
        this.commandManager = commandManager;
    }

    public void performSave() {
        JFileChooser fileChooser = new JFileChooser(lastDirectory);
        fileChooser.setDialogTitle("Save Circuit");
        if (fileChooser.showSaveDialog(parentFrame) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (file != null) {
                lastDirectory = file.getParentFile();
                try {
                    String path = file.getAbsolutePath();
                    if (!path.endsWith(".json")) path += ".json";
                    manager.save(path);
                } catch (Exception ex) {
                    canvas.showErrorMessage("Save Failed: " + ex.getMessage());
                }
            }
        }
    }

    public void performLoad() {
        JFileChooser fileChooser = new JFileChooser(lastDirectory);
        fileChooser.setDialogTitle("Load Circuit");
        if (fileChooser.showOpenDialog(parentFrame) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (file != null) {
                lastDirectory = file.getParentFile();
                try {
                    manager.load(file.getAbsolutePath());
                    commandManager.clear();
                    canvas.repaint();
                } catch (Exception ex) {
                    canvas.showErrorMessage("Load Failed: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
    }

    public void createSubComponent() {
        List<CircuitComponent> selectedComponents = mouseController.getSelectedComponents();
        if (selectedComponents.isEmpty()) {
            JOptionPane.showMessageDialog(parentFrame, "Please select one or more components to include in the sub-circuit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String name = JOptionPane.showInputDialog(parentFrame, "Enter a name for the sub-circuit:", "Create Sub-Circuit", JOptionPane.PLAIN_MESSAGE);

        if (name != null && !name.trim().isEmpty()) {
            manager.createSubCircuit(name, selectedComponents);
            // Note: createSubCircuit modifies multiple things (removes components, adds one).
            // For simplicity, we clear history after such a complex operation or implement a macro command.
            commandManager.clear();
            mouseController.clearSelection();
            canvas.repaint();
        }
    }

    public void performExport() {
        List<CircuitComponent> selected = mouseController.getSelectedComponents();
        if (selected.size() != 1 || !(selected.get(0) instanceof SubCircuit)) {
            JOptionPane.showMessageDialog(parentFrame, "Please select exactly one Sub-Circuit to export.", "Invalid Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser(lastDirectory);
        fileChooser.setDialogTitle("Export Sub-Circuit");
        if (fileChooser.showSaveDialog(parentFrame) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (file != null) {
                lastDirectory = file.getParentFile();
                try {
                    String path = file.getAbsolutePath();
                    if (!path.endsWith(".json")) path += ".json";
                    manager.saveSubCircuitToFile((SubCircuit) selected.get(0), path);
                } catch (Exception ex) {
                    canvas.showErrorMessage("Export Failed: " + ex.getMessage());
                }
            }
        }
    }

    public void performImport() {
        JFileChooser fileChooser = new JFileChooser(lastDirectory);
        fileChooser.setDialogTitle("Import Sub-Circuit");
        if (fileChooser.showOpenDialog(parentFrame) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (file != null) {
                lastDirectory = file.getParentFile();
                try {
                    manager.loadSubCircuitFromFile(file.getAbsolutePath());
                    // Sub-circuit import is an Add action.
                    // For now, we clear history as the newly added component might be complex.
                    commandManager.clear();
                    canvas.repaint();
                } catch (Exception ex) {
                    canvas.showErrorMessage("Import Failed: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
    }

    public void performDelete() {
        List<CircuitComponent> selected = mouseController.getSelectedComponents();
        if (!selected.isEmpty()) {
            commandManager.executeCommand(new RemoveComponentCommand(manager, new ArrayList<>(selected)));
            mouseController.clearSelection();
            canvas.repaint();
        }
    }
}

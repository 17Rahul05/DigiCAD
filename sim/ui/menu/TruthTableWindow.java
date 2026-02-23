package sim.ui.menu;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import sim.CircuitComponent;
import sim.CircuitManager;
import sim.model.LED;
import sim.model.Switch;
import sim.model.Tooltype;
import sim.ui.CanvasPanel;
import sim.util.PinState;
import sim.util.ThemeManager;

public class TruthTableWindow {
    private TruthTableWindow() {
        /* This utility class should not be instantiated */
    }

    
    public static void show(JFrame parent, CircuitManager manager, CanvasPanel canvas) {
        List<Switch> switches = new ArrayList<>();
        List<LED> leds = new ArrayList<>();

        for (CircuitComponent c : manager.getComponents()) {
            if (c.getToolType() == Tooltype.SWITCH) switches.add((Switch) c);
            if (c.getToolType() == Tooltype.LED) leds.add((LED) c);
        }

        if (switches.isEmpty()) {
            canvas.showErrorMessage("No switches found to test!");
            return;
        }

        // Save the original state of the circuit's switches
        List<PinState> savedStates = new ArrayList<>();
        for (Switch s : switches) savedStates.add(s.getOutputState(0));

        Vector<String> columns = new Vector<>();
        for (Switch s : switches) columns.add(s.getID());
        for (LED l : leds) columns.add(l.getID());

        Vector<Vector<Object>> data = new Vector<>();

        int rows = (int) Math.pow(2, switches.size());
        for (int i = 0; i < rows; i++) {
            Vector<Object> row = new Vector<>();
            // Set inputs for the current row
            for (int j = 0; j < switches.size(); j++) {
                boolean val = ((i >> (switches.size() - j - 1)) & 1) == 1;
                switches.get(j).setState(val);
                row.add(val ? "1" : "0");
            }

            // Propagate the changes from the new switch states through the circuit
            manager.propagate();

            // Now read the stable output from the LEDs
            for (LED led : leds) {
                int inputPin = led.getInputPinIDs().get(0);
                PinState result = manager.getPinState(inputPin);
                switch (result) {
                    case HIGH: row.add("1"); break;
                    case LOW: row.add("0"); break;
                    case FLOATING: row.add("Z"); break;
                }
            }
            data.add(row);
        }

        // Restore the original switch states
        for (int i = 0; i < switches.size(); i++) {
            switches.get(i).setState(savedStates.get(i) == PinState.HIGH);
        }
        // Propagate the restored states to update the main canvas view
        manager.propagate();
        canvas.repaint();

        JDialog dialog = new JDialog(parent, "Truth Table", true);
        dialog.setSize(600, 500);
        dialog.getContentPane().setBackground(ThemeManager.getTheme().bg);

        DefaultTableModel model = new DefaultTableModel(data, columns);
        JTable table = new JTable(model);
        
        table.setBackground(ThemeManager.getTheme().componentBody);
        table.setForeground(ThemeManager.getTheme().text);
        table.setGridColor(Color.GRAY);
        table.setRowHeight(25);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.getTableHeader().setBackground(ThemeManager.getTheme().toolbarBg);
        table.getTableHeader().setForeground(ThemeManager.getTheme().text);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(ThemeManager.getTheme().componentBody);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        dialog.add(scrollPane);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }
}

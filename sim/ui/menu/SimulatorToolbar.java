package sim.ui.menu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import sim.CircuitManager;
import sim.model.Tooltype;
import sim.ui.CanvasPanel;
import sim.ui.FlatScrollBarUI;
import sim.ui.ToolButton;
import sim.util.ThemeManager;

public class SimulatorToolbar {

    private JPanel mainPanel;
    private final Supplier<Tooltype> currentToolSupplier;
    private final Consumer<Tooltype> currentToolSetter;
    private final JFrame parentFrame;
    private final CircuitManager manager;
    private final CanvasPanel canvas;

    public SimulatorToolbar(Supplier<Tooltype> toolGetter, Consumer<Tooltype> toolSetter, 
                            JFrame frame, CircuitManager manager, CanvasPanel canvas) {
        this.currentToolSupplier = toolGetter;
        this.currentToolSetter = toolSetter;
        this.parentFrame = frame;
        this.manager = manager;
        this.canvas = canvas;
    }

    public JScrollPane createToolbar() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Listener to update toolbar colors
        ThemeManager.addThemeListener(this::refreshToolbarAppearance);
        mainPanel.setBackground(ThemeManager.getTheme().toolbarBg);
        
        // --- Edit Tools ---
        addHeader("TOOLS");
        JPanel editGrid = new JPanel(new GridLayout(0, 1, 0, 5));
        editGrid.setOpaque(false);
        editGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        addTextToolButton(editGrid, Tooltype.SELECT, "Select and move components", "Select");
        addTextToolButton(editGrid, Tooltype.WIRE, "Draw a Wire between pins", "Wire");
        addTextToolButton(editGrid, Tooltype.TOGGLE, "Toggle state of Switches", "Toggle");
        addTextToolButton(editGrid, Tooltype.DELETE, "Delete components or wires", "Delete");
        mainPanel.add(editGrid);
        mainPanel.add(Box.createVerticalStrut(15));

        // --- Logic Gates ---
        addHeader("GATES");
        JPanel gatesGrid = createGrid();
        addToolButton(gatesGrid, Tooltype.AND, "Place an AND Gate", "");
        addToolButton(gatesGrid, Tooltype.OR, "Place an OR Gate", "");
        addToolButton(gatesGrid, Tooltype.NOT, "Place a NOT Gate", "");
        addToolButton(gatesGrid, Tooltype.NAND, "Place a NAND Gate", "");
        addToolButton(gatesGrid, Tooltype.NOR, "Place a NOR Gate", "");
        addToolButton(gatesGrid, Tooltype.XOR, "Place an XOR Gate", "");
        addToolButton(gatesGrid, Tooltype.XNOR, "Place an XNOR Gate", "");
        gatesGrid.add(Box.createRigidArea(new Dimension(50, 50))); // Empty slot
        mainPanel.add(gatesGrid);
        mainPanel.add(Box.createVerticalStrut(15));

        // --- Complex Components ---
        addHeader("COMPONENTS");
        JPanel compGrid = createGrid();
        addToolButton(compGrid, Tooltype.MUX, "Place a 2-to-1 Multiplexer", "");
        addToolButton(compGrid, Tooltype.DEMUX, "Place a 1-to-2 Demultiplexer", "");
        addToolButton(compGrid, Tooltype.DECODER, "Place a 2-to-4 Decoder", "");
        addToolButton(compGrid, Tooltype.ENCODER, "Place a 4-to-2 Encoder", "");
        mainPanel.add(compGrid);
        mainPanel.add(Box.createVerticalStrut(15));

        // --- I/O Devices ---
        addHeader("I/O");
        JPanel ioGrid = createGrid();
        addToolButton(ioGrid, Tooltype.SWITCH, "Place a Switch", "");
        addToolButton(ioGrid, Tooltype.LED, "Place an LED", "");
        addToolButton(ioGrid, Tooltype.SEVEN_SEGMENT, "Place a 7-Segment Display", "");
        ioGrid.add(Box.createRigidArea(new Dimension(50, 50))); // Empty slot
        mainPanel.add(ioGrid);
        mainPanel.add(Box.createVerticalStrut(15));

        // --- Truth Table ---
        addHeader("SYSTEM");
        JPanel sysGrid = new JPanel(new GridLayout(0, 1, 0, 8));
        sysGrid.setOpaque(false);
        sysGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        ToolButton ttBtn = new ToolButton("Truth Table", ThemeManager.getTheme().buttonBg, true, null, currentToolSupplier, null, "Show Truth Table", 120, 30);
        ttBtn.addActionListener(e -> TruthTableWindow.show(parentFrame, manager, canvas));
        sysGrid.add(ttBtn);
        mainPanel.add(sysGrid);

        // Push everything to the top
        mainPanel.add(Box.createVerticalGlue());

        // Wrap in a container to respect preferred width
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(mainPanel, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(wrapper);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUI(new FlatScrollBarUI());
        
        scrollPane.setPreferredSize(new Dimension(140, 0)); 

        refreshToolButtons(); // Initial refresh
        return scrollPane;
    }

    private JPanel createGrid() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    private void addHeader(String title) {
        JLabel header = new JLabel(title);
        header.setForeground(ThemeManager.getTheme().text);
        header.setFont(new Font("SansSerif", Font.BOLD, 10));
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Add a small opacity to the header text for a subtle look
        header.setOpaque(false);
        Color c = ThemeManager.getTheme().text;
        header.setForeground(new Color(c.getRed(), c.getGreen(), c.getBlue(), 150));

        mainPanel.add(header);
    }

    private void addToolButton(JPanel grid, Tooltype tool, String tooltip, String text) {
        ToolButton btn = createToolButton(text, ThemeManager.getTheme().buttonBg, true, tool, currentToolSetter, tooltip);
        grid.add(btn);
    }

    private void addTextToolButton(JPanel grid, Tooltype tool, String tooltip, String text) {
        ToolButton btn = new ToolButton(text, ThemeManager.getTheme().buttonBg, true, tool, currentToolSupplier, currentToolSetter, tooltip, 120, 30);
        grid.add(btn);
    }

    private void refreshToolbarAppearance() {
        mainPanel.setBackground(ThemeManager.getTheme().toolbarBg);
        
        for (Component comp : mainPanel.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel header = (JLabel) comp;
                Color c = ThemeManager.getTheme().text;
                header.setForeground(new Color(c.getRed(), c.getGreen(), c.getBlue(), 150));
            }
        }
        
        refreshButtonsRecursively(mainPanel);
        mainPanel.getParent().setBackground(ThemeManager.getTheme().toolbarBg); // Refresh wrapper
    }

    public void refreshToolButtons() {
        refreshButtonsRecursively(mainPanel);
    }

    private void refreshButtonsRecursively(Component container) {
        if (container instanceof ToolButton) {
            ((ToolButton) container).refreshAppearance();
        } else if (container instanceof java.awt.Container) {
            for (Component comp : ((java.awt.Container) container).getComponents()) {
                refreshButtonsRecursively(comp);
            }
        }
    }

    private ToolButton createToolButton(String text, Color bg, boolean useThemeColor, Tooltype associatedTool, Consumer<Tooltype> toolSetter, String tooltipText) {
        return new ToolButton(text, bg, useThemeColor, associatedTool, currentToolSupplier, toolSetter, tooltipText);
    }
}


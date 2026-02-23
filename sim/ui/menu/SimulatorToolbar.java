package sim.ui.menu;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicComboPopup;

import sim.CircuitManager;
import sim.actions.ActionHandler;
import sim.model.Tooltype;
import sim.ui.CanvasPanel;
import sim.ui.FlatScrollBarUI;
import sim.ui.ToolButton;
import sim.util.ThemeManager;

public class SimulatorToolbar {

    private JToolBar toolBar;
    private final Supplier<Tooltype> currentToolSupplier;
    private final Consumer<Tooltype> currentToolSetter;
    private final ActionHandler actionHandler;
    private final JFrame parentFrame;
    private final CircuitManager manager;
    private final CanvasPanel canvas;

    public SimulatorToolbar(Supplier<Tooltype> toolGetter, Consumer<Tooltype> toolSetter, 
                            ActionHandler actionHandler, JFrame frame, 
                            CircuitManager manager, CanvasPanel canvas) {
        this.currentToolSupplier = toolGetter;
        this.currentToolSetter = toolSetter;
        this.actionHandler = actionHandler;
        this.parentFrame = frame;
        this.manager = manager;
        this.canvas = canvas;
    }

    public JScrollPane createToolbar() {
        toolBar = new JToolBar(SwingConstants.VERTICAL);
        toolBar.setFloatable(false);
        toolBar.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        
        // Listener to update toolbar colors
        ThemeManager.addThemeListener(this::refreshToolbarAppearance);
        toolBar.setBackground(ThemeManager.getTheme().toolbarBg);
        
        // --- Edit Tools ---
        addHeader("EDIT");
        addToolButton(Tooltype.SELECT, "Select and move components");
        addToolButton(Tooltype.WIRE, "Draw a Wire between pins");
        addToolButton(Tooltype.TOGGLE, "Toggle state of Switches");
        addToolButton(Tooltype.DELETE, "Delete components or wires");

        toolBar.add(Box.createVerticalStrut(15));

        // --- Logic Gates ---
        addHeader("LOGIC GATES");
        addToolButton(Tooltype.AND, "Place an AND Gate");
        addToolButton(Tooltype.OR, "Place an OR Gate");
        addToolButton(Tooltype.NOT, "Place a NOT Gate");
        addToolButton(Tooltype.NAND, "Place a NAND Gate");
        addToolButton(Tooltype.NOR, "Place a NOR Gate");
        addToolButton(Tooltype.XOR, "Place an XOR Gate");
        addToolButton(Tooltype.XNOR, "Place an XNOR Gate");

        toolBar.add(Box.createVerticalStrut(15));

        // --- Complex Components ---
        addHeader("COMPONENTS");
        addToolButton(Tooltype.MUX, "Place a 2-to-1 Multiplexer");
        addToolButton(Tooltype.DEMUX, "Place a 1-to-2 Demultiplexer");
        addToolButton(Tooltype.DECODER, "Place a 2-to-4 Decoder");
        addToolButton(Tooltype.ENCODER, "Place a 4-to-2 Encoder");

        toolBar.add(Box.createVerticalStrut(15));

        // --- I/O Devices ---
        addHeader("I/O DEVICES");
        addToolButton(Tooltype.SWITCH, "Place a Switch");
        addToolButton(Tooltype.LED, "Place an LED");
        addToolButton(Tooltype.SEVEN_SEGMENT, "Place a 7-Segment Display");

        toolBar.add(Box.createVerticalStrut(15));

        // --- Circuit Operations ---
        addHeader("CIRCUIT");
        createSubCircuitButton();
        createExportModuleButton();
        createImportModuleButton();
        createTruthTableButton();

        toolBar.add(Box.createVerticalStrut(15));

        // --- System ---
        addHeader("SYSTEM");
        createSaveLoadButtons();
        toolBar.add(Box.createVerticalStrut(10));
        createThemeComboBox();
        
        toolBar.add(Box.createVerticalGlue());
        
        JScrollPane scrollPane = new JScrollPane(toolBar);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);

        // Customize ScrollBar
        scrollPane.getVerticalScrollBar().setUI(new FlatScrollBarUI());
        
        // Fix width to prevent cutting off buttons (150px buttons + ~10px scrollbar + padding)
        scrollPane.setPreferredSize(new Dimension(170, 0)); 

        refreshToolButtons(); // Initial refresh
        return scrollPane;
    }

    private void addHeader(String title) {
        JLabel header = new JLabel(title);
        header.setForeground(ThemeManager.getTheme().text);
        header.setFont(new Font("SansSerif", Font.BOLD, 10));
        header.setBorder(BorderFactory.createEmptyBorder(5, 5, 2, 0));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Add a small opacity to the header text for a subtle look
        header.setOpaque(false);
        Color c = ThemeManager.getTheme().text;
        header.setForeground(new Color(c.getRed(), c.getGreen(), c.getBlue(), 150));

        toolBar.add(header);
    }

    private void addToolButton(Tooltype tool, String tooltip) {
        ToolButton btn = createToolButton(tool.name(), ThemeManager.getTheme().buttonBg, true, tool, currentToolSetter, tooltip);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        toolBar.add(btn);
    }

    private void createSubCircuitButton() {
        ToolButton subBtn = createToolButton("SUB", new Color(100, 100, 200), false, null, null, "Create a Sub-Circuit from selected components");
        subBtn.addActionListener(e -> actionHandler.createSubComponent());
        subBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        toolBar.add(subBtn);
    }

    private void createExportModuleButton() {
        ToolButton exportBtn = createToolButton("EXP", new Color(100, 150, 200), false, null, null, "Export selected Sub-Circuit to file");
        exportBtn.addActionListener(e -> actionHandler.performExport());
        exportBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        toolBar.add(exportBtn);
    }

    private void createImportModuleButton() {
        ToolButton importBtn = createToolButton("IMP", new Color(150, 100, 200), false, null, null, "Import Sub-Circuit from file");
        importBtn.addActionListener(e -> actionHandler.performImport());
        importBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        toolBar.add(importBtn);
    }

    private void createTruthTableButton() {
        ToolButton ttBtn = createToolButton("📊 TRUTH TABLE", new Color(0, 120, 215), false, null, null, "Show Truth Table");
        ttBtn.addActionListener(e -> TruthTableWindow.show(parentFrame, manager, canvas));
        ttBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        toolBar.add(ttBtn);
    }

    private void createSaveLoadButtons() {
        ToolButton saveBtn = createToolButton("💾 SAVE", new Color(40, 120, 60), false, null, null, "Save Circuit");
        saveBtn.addActionListener(e -> actionHandler.performSave());
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        toolBar.add(saveBtn);

        ToolButton loadBtn = createToolButton("📂 LOAD", new Color(180, 100, 0), false, null, null, "Load Circuit");
        loadBtn.addActionListener(e -> actionHandler.performLoad());
        loadBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        toolBar.add(loadBtn);
    }

    private void createThemeComboBox() {
        JComboBox<String> themeComboBox = new JComboBox<>(ThemeManager.getThemeNames().toArray(new String[0]));
        themeComboBox.setSelectedItem(ThemeManager.getTheme().name); 
        themeComboBox.setPreferredSize(new Dimension(150, 35));
        themeComboBox.setMaximumSize(new Dimension(150, 35));
        themeComboBox.setFocusable(false);
        themeComboBox.setAlignmentX(Component.LEFT_ALIGNMENT); 
        themeComboBox.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));

        styleComboBox(themeComboBox);

        ThemeManager.addThemeListener(() -> {
            themeComboBox.setBackground(ThemeManager.getTheme().buttonBg);
            themeComboBox.setForeground(ThemeManager.getTheme().text);
            for (int i = 0; i < themeComboBox.getComponentCount(); i++) {
                if (themeComboBox.getComponent(i) instanceof JButton) {
                    ((JButton) themeComboBox.getComponent(i)).setBackground(ThemeManager.getTheme().buttonBg);
                }
            }
            // Update headers in refreshToolbarAppearance
        });
        
        themeComboBox.setBackground(ThemeManager.getTheme().buttonBg);
        themeComboBox.setForeground(ThemeManager.getTheme().text);

        themeComboBox.addActionListener(e -> {
            String selectedThemeName = (String) themeComboBox.getSelectedItem();
            ThemeManager.setTheme(selectedThemeName);
        });
        toolBar.add(themeComboBox);
    }

    private void styleComboBox(JComboBox<String> cb) {
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? ThemeManager.getTheme().selection : ThemeManager.getTheme().buttonBg);
                setForeground(ThemeManager.getTheme().text);
                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                setFont(new java.awt.Font("SansSerif", Font.BOLD, 12));
                return this;
            }
        });

        // Style the popup
        Object child = cb.getAccessibleContext().getAccessibleChild(0);
        if (child instanceof BasicComboPopup) {
            BasicComboPopup popup = (BasicComboPopup) child;
            JScrollPane scrollPane = (JScrollPane) popup.getComponent(0);
            scrollPane.getVerticalScrollBar().setUI(new FlatScrollBarUI());
            scrollPane.setBorder(BorderFactory.createLineBorder(ThemeManager.getTheme().componentBorder));
        }
    }

    private void refreshToolbarAppearance() {
        toolBar.setBackground(ThemeManager.getTheme().toolbarBg);

        for (Component comp : toolBar.getComponents()) {
            if (comp instanceof ToolButton) {
                ((ToolButton) comp).refreshAppearance();
            } else if (comp instanceof JLabel) {
                JLabel header = (JLabel) comp;
                Color c = ThemeManager.getTheme().text;
                header.setForeground(new Color(c.getRed(), c.getGreen(), c.getBlue(), 150));
            } else if (comp instanceof JComboBox) {
                JComboBox<?> cb = (JComboBox<?>) comp;
                cb.setBackground(ThemeManager.getTheme().buttonBg);
                cb.setForeground(ThemeManager.getTheme().text);
                
                // Re-style popup border on theme change
                Object child = cb.getAccessibleContext().getAccessibleChild(0);
                if (child instanceof BasicComboPopup) {
                    BasicComboPopup popup = (BasicComboPopup) child;
                    JScrollPane scrollPane = (JScrollPane) popup.getComponent(0);
                    scrollPane.setBorder(BorderFactory.createLineBorder(ThemeManager.getTheme().componentBorder));
                }

                for (int i = 0; i < cb.getComponentCount(); i++) {
                    if (cb.getComponent(i) instanceof JButton) {
                        ((JButton) cb.getComponent(i)).setBackground(ThemeManager.getTheme().buttonBg);
                    }
                }
            }
        }
    }

    public void refreshToolButtons() {
        for (Component comp : toolBar.getComponents()) {
            if (comp instanceof ToolButton) {
                ((ToolButton) comp).refreshAppearance();
            }
        }
    }

    private ToolButton createToolButton(String text, Color bg, boolean useThemeColor, Tooltype associatedTool, Consumer<Tooltype> toolSetter, String tooltipText) {
        return new ToolButton(text, bg, useThemeColor, associatedTool, currentToolSupplier, toolSetter, tooltipText);
    }
}

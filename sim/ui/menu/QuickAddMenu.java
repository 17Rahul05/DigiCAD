package sim.ui.menu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import sim.model.Tooltype;
import sim.ui.CanvasPanel;
import sim.ui.FlatScrollBarUI;
import sim.util.Theme;
import sim.util.ThemeManager;

public class QuickAddMenu {
    private QuickAddMenu() {
        /* This utility class should not be instantiated */
    }

    public static void show(CanvasPanel canvas, Consumer<Tooltype> toolSetter) {
        Theme theme = ThemeManager.getTheme();

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(canvas));
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(theme.bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(theme.componentBorder);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 15, 15);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        dialog.setContentPane(mainPanel);

        JTextField searchField = new JTextField(15);
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        searchField.setBackground(theme.buttonBg);
        searchField.setForeground(theme.text);
        searchField.setCaretColor(theme.text);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(theme.componentBorder, 1, true),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        DefaultListModel<Tooltype> listModel = new DefaultListModel<>();
        for (Tooltype t : Tooltype.values()) {
            if (t != Tooltype.SELECT && t != Tooltype.WIRE && t != Tooltype.DELETE && t != Tooltype.TOGGLE) {
                listModel.addElement(t);
            }
        }

        JList<Tooltype> list = new JList<>(listModel);
        list.setVisibleRowCount(6);
        list.setFont(new Font("SansSerif", Font.BOLD, 14));
        list.setBackground(theme.bg);
        list.setForeground(theme.text);
        
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(2, 0, 2, 0),
                    BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(isSelected ? theme.selection : theme.buttonBg, 1, true),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)
                    )
                ));
                label.setBackground(isSelected ? theme.selection : theme.buttonBg);
                label.setForeground(theme.text);
                label.setOpaque(true);
                return label;
            }
        });

        if (listModel.getSize() > 0) list.setSelectedIndex(0);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filter(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filter(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filter(); }

            private void filter() {
                String query = searchField.getText().toLowerCase();
                listModel.clear();
                for (Tooltype t : Tooltype.values()) {
                    if (t != Tooltype.SELECT && t != Tooltype.WIRE && t != Tooltype.DELETE && t != Tooltype.TOGGLE) {
                        if (t.name().toLowerCase().contains(query)) {
                            listModel.addElement(t);
                        }
                    }
                }
                if (listModel.getSize() > 0) {
                    list.setSelectedIndex(0);
                }
            }
        });

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    int idx = list.getSelectedIndex() + 1;
                    if (idx < listModel.getSize()) {
                        list.setSelectedIndex(idx);
                        list.ensureIndexIsVisible(idx);
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    int idx = list.getSelectedIndex() - 1;
                    if (idx >= 0) {
                        list.setSelectedIndex(idx);
                        list.ensureIndexIsVisible(idx);
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    Tooltype selected = list.getSelectedValue();
                    if (selected != null) {
                        toolSetter.accept(selected);
                        dialog.dispose();
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dialog.dispose();
                }
            }
        });

        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 1) {
                    Tooltype selected = list.getSelectedValue();
                    if (selected != null) {
                        toolSetter.accept(selected);
                        dialog.dispose();
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        scrollPane.setPreferredSize(new Dimension(220, 200));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUI(new FlatScrollBarUI());

        mainPanel.add(searchField, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        dialog.pack();

        // Calculate center of canvas
        Point canvasLocation = canvas.getLocationOnScreen();
        int px = canvasLocation.x + Math.max(0, (canvas.getWidth() - dialog.getWidth()) / 2);
        int py = canvasLocation.y + Math.max(0, (canvas.getHeight() - dialog.getHeight()) / 2);
        dialog.setLocation(px, py);

        // Hide dialog when it loses focus
        dialog.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowLostFocus(WindowEvent e) {
                dialog.dispose();
            }
        });

        dialog.setVisible(true);
        SwingUtilities.invokeLater(searchField::requestFocusInWindow);
    }
}
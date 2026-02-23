package sim.ui;

import sim.model.Tooltype;
import sim.util.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ToolButton extends JButton {
    private Tooltype associatedTool;
    private Supplier<Tooltype> currentToolSupplier;

    @SuppressWarnings("unused")
    private Consumer<Tooltype> toolSetter; // To set the current tool in Simulate

    private Color defaultBg;
    private boolean useThemeColor;
    private boolean isHovered = false; // Track hover state internally
    private int cornerRadius = 15;

    public ToolButton(String text, Color bg, boolean useThemeColor, Tooltype associatedTool, Supplier<Tooltype> currentToolSupplier, Consumer<Tooltype> toolSetter, String tooltipText) {
        this(text, bg, useThemeColor, associatedTool, currentToolSupplier, toolSetter, tooltipText, 150, 40);
    }

    public ToolButton(String text, Color bg, boolean useThemeColor, Tooltype associatedTool, Supplier<Tooltype> currentToolSupplier, Consumer<Tooltype> toolSetter, String tooltipText, int width, int height) {
        super(text);
        this.associatedTool = associatedTool;
        this.currentToolSupplier = currentToolSupplier;
        this.toolSetter = toolSetter; 
        this.defaultBg = bg;
        this.useThemeColor = useThemeColor;
        
        setPreferredSize(new Dimension(width, height));
        setMaximumSize(new Dimension(width, height));
        setSize(width, height);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setToolTipText(tooltipText);

        if (text.length() < 3 && !text.contentEquals("OR")) {
            setFont(new Font("Segoe UI Symbol", Font.BOLD, height > 30 ? 18 : 14));
        } else {
            setFont(new Font("SansSerif", Font.BOLD, 14));
        }

        // Add action listener to set the current tool
        if (associatedTool != null && toolSetter != null) {
            addActionListener(e -> toolSetter.accept(associatedTool));
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                updateAppearance();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                updateAppearance();
            }
        });
        updateAppearance(); // Initial appearance
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        updateAppearance();
    }

    public void refreshAppearance() {
        updateAppearance(); // Re-evaluate selection state
    }

    private void updateAppearance() {
        Color btnBg;
        Color btnBorderColor;
        Color btnTextColor = ThemeManager.getTheme().text;
        
        boolean isSelected = (associatedTool != null && currentToolSupplier != null && associatedTool == currentToolSupplier.get());

        if (useThemeColor) {
            if (isSelected) {
                btnBg = ThemeManager.getTheme().buttonBg.darker();
                btnBorderColor = ThemeManager.getTheme().componentBorder.darker();
            } else {
                btnBg = ThemeManager.getTheme().buttonBg;
                btnBorderColor = ThemeManager.getTheme().componentBorder;
            }
        } else {
            if (isSelected) {
                btnBg = defaultBg.darker();
                btnBorderColor = Color.DARK_GRAY.darker();
            } else {
                btnBg = defaultBg;
                btnBorderColor = Color.DARK_GRAY;
            }
        }

        if (isHovered) {
            btnBg = btnBg.brighter();
            if (!isSelected) btnBorderColor = btnBorderColor.brighter();
        }
        
        setBackground(btnBg); // Set the background color for gradient calculation
        setForeground(btnTextColor);
        setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(cornerRadius, btnBorderColor, 2),
            BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));
        
        repaint(); // Ensure component repaints itself
    }

    // Custom painting for gradient background
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color startColor = getBackground(); // Uses the color set in updateAppearance
        Color endColor = new Color(
            Math.max(0, startColor.getRed() - 40),
            Math.max(0, startColor.getGreen() - 40),
            Math.max(0, startColor.getBlue() - 40)
        );

        if (isHovered) {
            startColor = startColor.brighter();
            endColor = endColor.brighter();
        }

        GradientPaint gp = new GradientPaint(0, 0, startColor, 0, getHeight(), endColor);
        g2.setPaint(gp);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius * 2, cornerRadius * 2);

        g2.dispose();
        super.paintComponent(g); // Paint children (text/icon)
    }
}

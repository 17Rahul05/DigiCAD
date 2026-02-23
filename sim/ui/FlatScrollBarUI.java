package sim.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicScrollBarUI;

import sim.util.ThemeManager;

public class FlatScrollBarUI extends BasicScrollBarUI {
    
    private final int THUMB_SIZE = 8;
    
    @Override
    protected void installComponents() {
        super.installComponents();
        // Listener to repaint when theme changes
        ThemeManager.addThemeListener(() -> {
            if (scrollbar != null) scrollbar.repaint();
        });
    }

    @Override 
    protected void configureScrollBarColors() {
        // No-op, we use dynamic colors
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        return new Dimension(10, super.getPreferredSize(c).height);
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return createZeroButton();
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return createZeroButton();
    }

    private JButton createZeroButton() {
        JButton btn = new JButton();
        btn.setPreferredSize(new Dimension(0, 0));
        btn.setMinimumSize(new Dimension(0, 0));
        btn.setMaximumSize(new Dimension(0, 0));
        return btn;
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        g.setColor(ThemeManager.getTheme().toolbarBg);
        g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int x = thumbBounds.x + (thumbBounds.width - THUMB_SIZE) / 2;
        int y = thumbBounds.y;
        int w = THUMB_SIZE;
        int h = thumbBounds.height;

        Color thumbColor = ThemeManager.getTheme().buttonBg.darker();
        if (isThumbRollover()) {
            thumbColor = thumbColor.brighter();
        }
        
        g2.setColor(thumbColor);
        g2.fillRoundRect(x, y, w, h, w, w);
        
        g2.dispose();
    }
}

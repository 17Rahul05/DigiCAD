package sim.model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

import sim.util.PinState;
import sim.util.Theme;
import sim.util.ThemeManager;

public class Wire {
    private int srcPinID;
    private int desPinID;
    private PinState state = PinState.FLOATING;
    private boolean orthogonal;

    public Wire(int source, int destination) {
        this(source, destination, false);
    }

    public Wire(int source, int destination, boolean orthogonal) {
        this.srcPinID = source;
        this.desPinID = destination;
        this.orthogonal = orthogonal;
    }

    public void setState(PinState state) {
        this.state = state;
    }

    public boolean isOrthogonal() {
        return orthogonal;
    }

    public void draw(Graphics2D g2, int x1, int y1, int x2, int y2) {
        Theme theme = ThemeManager.getTheme();
        
        java.awt.Shape shape;
        if (orthogonal) {
            Path2D path = new Path2D.Double();
            int midX = (x1 + x2) / 2;
            path.moveTo(x1, y1);
            path.lineTo(midX, y1);
            path.lineTo(midX, y2);
            path.lineTo(x2, y2);
            shape = path;
        } else {
            shape = new java.awt.geom.Line2D.Double(x1, y1, x2, y2);
        }

        if (state == PinState.HIGH) {
            // Glow
            g2.setColor(theme.wireGlow); // Faint wide glow
            g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(shape);

            g2.setColor(theme.wireActive); // Active line
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(shape);

            g2.setColor(Color.WHITE); // Core
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(shape);
        } else if (state == PinState.LOW){
            g2.setColor(theme.wireInactive); // Inactive
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(shape);
        } else { // FLOATING
            g2.setColor(theme.wireFloating); // Floating
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(shape);
        }
    }

    public int getSourcePinID() {return srcPinID;}
    public int getDestPinID() {return desPinID;}
}
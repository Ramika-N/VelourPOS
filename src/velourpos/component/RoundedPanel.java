package velourpos.component;


import javax.swing.*;
import java.awt.*;

/** A JPanel that paints itself with rounded corners and a custom background. */
public class RoundedPanel extends JPanel {

    private Color  bg;
    private int    radius;
    private boolean hasBorder;
    private Color  borderColor;

    public RoundedPanel(Color bg, int radius) {
        this(bg, radius, false, null);
    }

    public RoundedPanel(Color bg, int radius, boolean hasBorder, Color borderColor) {
        this.bg          = bg;
        this.radius      = radius;
        this.hasBorder   = hasBorder;
        this.borderColor = borderColor;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        if (hasBorder && borderColor != null) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
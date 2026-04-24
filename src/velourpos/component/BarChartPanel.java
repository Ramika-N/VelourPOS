package velourpos.component;

import javax.swing.*;
import java.awt.*;

public class BarChartPanel extends JPanel {

    private double[] data;
    private String[] labels;
    private Color    barColor;
    private Color    highlightColor;
    private String   title;

    public BarChartPanel(double[] data, String[] labels, String title,
                         Color barColor, Color highlightColor) {
        this.data          = data;
        this.labels        = labels;
        this.title         = title;
        this.barColor      = barColor;
        this.highlightColor = highlightColor;
        setOpaque(false);
    }

    public void setData(double[] data) {
        this.data = data;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (data == null || data.length == 0) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w  = getWidth();
        int h  = getHeight();
        int padL = 10, padR = 10, padT = 30, padB = 30;
        int chartW = w - padL - padR;
        int chartH = h - padT - padB;

        // Title
        g2.setFont(Theme.FONT_SMALL);
        g2.setColor(Theme.TEXT_SECONDARY);
        g2.drawString(title, padL, 18);

        // Find max
        double max = 0;
        for (double v : data) if (v > max) max = v;
        if (max == 0) max = 1;

        int n        = data.length;
        int barW     = Math.max(4, chartW / n - 4);
        int gap      = (chartW - barW * n) / Math.max(1, n);

        // Horizontal guide lines
        g2.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                0, new float[]{3, 4}, 0));
        g2.setColor(Theme.BORDER);
        for (int line = 1; line <= 4; line++) {
            int y = padT + chartH - (int)(chartH * line / 4.0);
            g2.drawLine(padL, y, padL + chartW, y);
        }
        g2.setStroke(new BasicStroke(1f));

        // Bars
        for (int i = 0; i < n; i++) {
            int barH  = (int)(chartH * data[i] / max);
            int x     = padL + i * (barW + gap) + gap / 2;
            int y     = padT + chartH - barH;

            boolean isLast = (i == n - 1);
            Color bar = isLast ? highlightColor : barColor;

            // Gradient bar
            GradientPaint gp = new GradientPaint(x, y, bar, x, padT + chartH,
                    new Color(bar.getRed(), bar.getGreen(), bar.getBlue(), 40));
            g2.setPaint(gp);
            g2.fillRoundRect(x, y, barW, barH, 4, 4);

            // Label
            if (labels != null && i < labels.length && (i % Math.max(1, n / 7) == 0 || isLast)) {
                g2.setFont(Theme.FONT_SMALL);
                g2.setColor(isLast ? Theme.ACCENT_GOLD : Theme.TEXT_MUTED);
                FontMetrics fm = g2.getFontMetrics();
                int lx = x + barW / 2 - fm.stringWidth(labels[i]) / 2;
                g2.drawString(labels[i], lx, h - 8);
            }

            // Value tooltip on last bar
            if (isLast && data[i] > 0) {
                String val = String.format("$%.0f", data[i]);
                g2.setFont(Theme.FONT_SMALL);
                g2.setColor(Theme.ACCENT_LIGHT);
                FontMetrics fm = g2.getFontMetrics();
                int vx = x + barW / 2 - fm.stringWidth(val) / 2;
                g2.drawString(val, vx, y - 5);
            }
        }

        g2.dispose();
    }
}

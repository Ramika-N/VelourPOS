package velourpos.component;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/** Lightweight donut chart for category sales breakdown. */
public class DonutChartPanel extends JPanel {

    private Map<String, Integer> data;
    private Color[] palette = {
        Theme.ACCENT_GOLD,
        new Color(0x5B8BDE),
        new Color(0x4CAF82),
        new Color(0xE8A54A),
        new Color(0xE05C6A),
        new Color(0xA87CDB)
    };

    public DonutChartPanel(Map<String, Integer> data) {
        this.data = data;
        setOpaque(false);
    }

    public void setData(Map<String, Integer> data) {
        this.data = data;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (data == null || data.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int total = data.values().stream().mapToInt(Integer::intValue).sum();
        if (total == 0) { g2.dispose(); return; }

        int w = getWidth(), h = getHeight();
        int chartSize = Math.min(w / 2 - 20, h - 50);
        int cx = chartSize / 2 + 10, cy = h / 2;
        int outer = chartSize, inner = (int)(outer * 0.58);

        // Title
        g2.setFont(Theme.FONT_SMALL);
        g2.setColor(Theme.TEXT_SECONDARY);
        g2.drawString("SALES BY CATEGORY", 10, 16);

        // Draw arcs
        float startAngle = -90f;
        int idx = 0;
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(data.entrySet());
        for (Map.Entry<String, Integer> e : entries) {
            float sweep = 360f * e.getValue() / total;
            Color c = palette[idx % palette.length];
            g2.setColor(c);
            g2.fillArc(cx - outer / 2, cy - outer / 2, outer, outer, (int) startAngle, -(int) sweep);
            startAngle += sweep;
            idx++;
        }

        // Donut hole
        g2.setColor(Theme.BG_CARD);
        g2.fillOval(cx - inner / 2, cy - inner / 2, inner, inner);

        // Center text
        g2.setFont(Theme.FONT_BODY_B);
        g2.setColor(Theme.TEXT_PRIMARY);
        String totalStr = String.valueOf(total);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(totalStr, cx - fm.stringWidth(totalStr) / 2, cy + 5);
        g2.setFont(Theme.FONT_SMALL);
        g2.setColor(Theme.TEXT_MUTED);
        String unitStr = "units";
        g2.drawString(unitStr, cx - g2.getFontMetrics().stringWidth(unitStr) / 2, cy + 18);

        // Legend
        int legendX = cx + outer / 2 + 16;
        int legendY = cy - (entries.size() * 20) / 2;
        idx = 0;
        for (Map.Entry<String, Integer> e : entries) {
            Color c = palette[idx % palette.length];
            g2.setColor(c);
            g2.fillRoundRect(legendX, legendY - 9, 10, 10, 3, 3);
            g2.setFont(Theme.FONT_SMALL);
            g2.setColor(Theme.TEXT_SECONDARY);
            String cat = capitalize(e.getKey());
            g2.drawString(cat, legendX + 14, legendY);
            g2.setColor(Theme.TEXT_MUTED);
            int pct = (int)(100.0 * e.getValue() / total);
            g2.drawString(pct + "%", legendX + 14 + g2.getFontMetrics().stringWidth(cat) + 6, legendY);
            legendY += 20;
            idx++;
        }

        g2.dispose();
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.charAt(0) + s.substring(1).toLowerCase();
    }
}

package velourpos.gui;

import velourpos.data.DataStore;
import velourpos.model.*;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import velourpos.component.BarChartPanel;
import velourpos.component.DonutChartPanel;
import velourpos.component.RoundedPanel;
import velourpos.component.Theme;

/**
 * Dashboard panel — KPI cards, revenue chart, category donut, recent transactions.
 */
public class DashboardPanel extends JPanel {

    private final DataStore ds = DataStore.getInstance();
    private final NumberFormat currency = NumberFormat.getCurrencyInstance();
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("hh:mm a");

    // Live-update components
    private JLabel todayRevLabel, monthRevLabel, txLabel, lowStockLabel;
    private BarChartPanel barChart;
    private DonutChartPanel donut;
    private JPanel txListPanel;

    public DashboardPanel() {
        setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout(0, 0));
        build();
    }

    private void build() {
        // ── Header ────────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(
                Theme.PAD_L, Theme.PAD_XL, Theme.PAD_M, Theme.PAD_XL));

        JLabel title = new JLabel("Dashboard");
        title.setFont(Theme.FONT_HEADING);
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Welcome back — here's what's happening today.");
        sub.setFont(Theme.FONT_SUBHEAD);
        sub.setForeground(Theme.TEXT_SECONDARY);

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(sub);
        header.add(titleBlock, BorderLayout.WEST);

        // Refresh button
        JButton refresh = styledButton("↻ Refresh");
        refresh.addActionListener(e -> refresh());
        header.add(refresh, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── Scrollable body ───────────────────────────────────────────────────
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(0, Theme.PAD_XL, Theme.PAD_XL, Theme.PAD_XL));

        body.add(buildKpiRow());
        body.add(Box.createVerticalStrut(Theme.PAD_M));
        body.add(buildChartsRow());
        body.add(Box.createVerticalStrut(Theme.PAD_M));
        body.add(buildRecentTx());

        JScrollPane scroll = new JScrollPane(body);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        scroll.getVerticalScrollBar().setBackground(Theme.BG_DARK);
        add(scroll, BorderLayout.CENTER);
    }

    // ── KPI Row ───────────────────────────────────────────────────────────────
    private JPanel buildKpiRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, Theme.PAD_M, 0));
        row.setOpaque(false);

        todayRevLabel  = new JLabel();
        monthRevLabel  = new JLabel();
        txLabel        = new JLabel();
        lowStockLabel  = new JLabel();

        row.add(kpiCard("Today's Revenue",  currency.format(ds.getTodayRevenue()),   Theme.ACCENT_GOLD,  "▲ 12% vs yesterday", todayRevLabel));
        row.add(kpiCard("Month Revenue",    currency.format(ds.getMonthRevenue()),   Theme.INFO,         "▲ 8% vs last month",  monthRevLabel));
        row.add(kpiCard("Transactions",     String.valueOf(ds.getTodayTransactions()),Theme.SUCCESS,     "Today",               txLabel));
        row.add(kpiCard("Low Stock Alerts", String.valueOf(ds.getLowStockCount()),   Theme.DANGER,       "Items need reorder",  lowStockLabel));

        return row;
    }

    private JPanel kpiCard(String title, String value, Color accent, String hint, JLabel valueRef) {
        RoundedPanel card = new RoundedPanel(Theme.BG_CARD, Theme.RADIUS_M, true, Theme.BORDER);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(Theme.PAD_L, Theme.PAD_L, Theme.PAD_L, Theme.PAD_L));

        // Accent top bar
        JPanel accentBar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 3, 3);
                g2.dispose();
            }
        };
        accentBar.setPreferredSize(new Dimension(0, 3));
        accentBar.setOpaque(false);
        card.add(accentBar, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(Theme.PAD_M, 0, 0, 0));

        JLabel titleLbl = new JLabel(title.toUpperCase());
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        titleLbl.setForeground(Theme.TEXT_MUTED);

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(Theme.FONT_STAT);
        valueLbl.setForeground(Theme.TEXT_PRIMARY);
        if (valueRef != null) {
            valueRef.setFont(Theme.FONT_STAT);
            valueRef.setForeground(Theme.TEXT_PRIMARY);
            valueRef.setText(value);
        }

        JLabel hintLbl = new JLabel(hint);
        hintLbl.setFont(Theme.FONT_SMALL);
        hintLbl.setForeground(accent);

        content.add(titleLbl);
        content.add(Box.createVerticalStrut(Theme.PAD_S));
        content.add(valueLbl);
        content.add(Box.createVerticalStrut(4));
        content.add(hintLbl);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    // ── Charts Row ────────────────────────────────────────────────────────────
    private JPanel buildChartsRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, Theme.PAD_M, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        // Revenue bar chart
        double[] rev = ds.getDailyRevenue(30);
        String[] labels = new String[30];
        java.time.LocalDate base = java.time.LocalDate.now().minusDays(29);
        for (int i = 0; i < 30; i++) {
            labels[i] = base.plusDays(i).format(java.time.format.DateTimeFormatter.ofPattern("d MMM"));
        }
        barChart = new BarChartPanel(rev, labels, "30-DAY REVENUE TREND",
                Theme.ACCENT_DIM, Theme.ACCENT_GOLD);

        RoundedPanel barCard = new RoundedPanel(Theme.BG_CARD, Theme.RADIUS_M, true, Theme.BORDER);
        barCard.setLayout(new BorderLayout());
        barCard.setBorder(BorderFactory.createEmptyBorder(Theme.PAD_M, Theme.PAD_M, Theme.PAD_M, Theme.PAD_M));
        barCard.add(barChart, BorderLayout.CENTER);

        // Donut chart
        donut = new DonutChartPanel(ds.getSalesByCategory());
        RoundedPanel donutCard = new RoundedPanel(Theme.BG_CARD, Theme.RADIUS_M, true, Theme.BORDER);
        donutCard.setLayout(new BorderLayout());
        donutCard.setBorder(BorderFactory.createEmptyBorder(Theme.PAD_M, Theme.PAD_M, Theme.PAD_M, Theme.PAD_M));
        donutCard.add(donut, BorderLayout.CENTER);

        row.add(barCard);
        row.add(donutCard);
        return row;
    }

    // ── Recent Transactions ───────────────────────────────────────────────────
    private JPanel buildRecentTx() {
        RoundedPanel card = new RoundedPanel(Theme.BG_CARD, Theme.RADIUS_M, true, Theme.BORDER);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(Theme.PAD_L, Theme.PAD_L, Theme.PAD_L, Theme.PAD_L));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 380));

        JLabel hdr = new JLabel("Recent Transactions");
        hdr.setFont(Theme.FONT_BODY_B);
        hdr.setForeground(Theme.TEXT_PRIMARY);
        card.add(hdr, BorderLayout.NORTH);

        txListPanel = new JPanel();
        txListPanel.setOpaque(false);
        txListPanel.setLayout(new BoxLayout(txListPanel, BoxLayout.Y_AXIS));
        txListPanel.setBorder(BorderFactory.createEmptyBorder(Theme.PAD_M, 0, 0, 0));
        card.add(txListPanel, BorderLayout.CENTER);

        populateTxList();
        return card;
    }

    private void populateTxList() {
        txListPanel.removeAll();

        // Column header
        JPanel colHdr = new JPanel(new GridLayout(1, 5));
        colHdr.setOpaque(false);
        colHdr.setBorder(BorderFactory.createEmptyBorder(0, 0, Theme.PAD_S, 0));
        String[] cols = {"SALE ID", "TIME", "ITEMS", "CASHIER", "TOTAL"};
        for (String c : cols) {
            JLabel l = new JLabel(c);
            l.setFont(new Font("SansSerif", Font.BOLD, 10));
            l.setForeground(Theme.TEXT_MUTED);
            colHdr.add(l);
        }
        txListPanel.add(colHdr);

        // Separator
        txListPanel.add(separator());

        // Last 10 sales (newest first)
        List<Sale> all = new ArrayList<>(ds.getSales());
        all.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        int count = Math.min(10, all.size());
        for (int i = 0; i < count; i++) {
            Sale s = all.get(i);
            txListPanel.add(txRow(s, i % 2 == 0));
        }
    }

    private JPanel txRow(Sale s, boolean alt) {
        JPanel row = new JPanel(new GridLayout(1, 5));
        row.setBackground(alt ? Theme.BG_ELEVATED : Theme.BG_CARD);
        row.setOpaque(alt);
        row.setBorder(BorderFactory.createEmptyBorder(Theme.PAD_S, Theme.PAD_S, Theme.PAD_S, Theme.PAD_S));

        Color methodColor = switch (s.getPaymentMethod()) {
            case CARD   -> Theme.INFO;
            case MOBILE -> Theme.SUCCESS;
            default     -> Theme.TEXT_SECONDARY;
        };

        row.add(rowLabel("#" + s.getId(), Theme.ACCENT_GOLD));
        row.add(rowLabel(s.getTimestamp().format(timeFmt), Theme.TEXT_SECONDARY));
        row.add(rowLabel(s.getItems().size() + " item(s)", Theme.TEXT_PRIMARY));
        row.add(rowLabel(s.getCashierName(), Theme.TEXT_SECONDARY));
        row.add(rowLabel(currency.format(s.getTotal()), Theme.TEXT_PRIMARY));

        return row;
    }

    private JLabel rowLabel(String text, Color c) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_BODY);
        l.setForeground(c);
        return l;
    }

    private JSeparator separator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    // ── Refresh ───────────────────────────────────────────────────────────────
    public void refresh() {
        populateTxList();
        barChart.setData(ds.getDailyRevenue(30));
        donut.setData(ds.getSalesByCategory());
        revalidate();
        repaint();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private JButton styledButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? Theme.ACCENT_DIM : Theme.BG_ELEVATED);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.RADIUS_S, Theme.RADIUS_S);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(Theme.FONT_BODY_B);
        btn.setForeground(Theme.ACCENT_GOLD);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return btn;
    }
}
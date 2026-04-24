package velourpos.gui;

import velourpos.data.DataStore;
import velourpos.model.Product;
import velourpos.model.Product.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

import velourpos.component.Theme;

/**
 * Inventory Management Panel — searchable, sortable table with add/edit/delete dialogs.
 */
public class InventoryPanel extends JPanel {

    private final DataStore ds = DataStore.getInstance();
    private final NumberFormat currency = NumberFormat.getCurrencyInstance();

    private JTable table;
    private InventoryTableModel model;
    private JTextField searchField;
    private JComboBox<String> categoryFilter;
    private JLabel countLabel;
    private List<Product> displayed = new ArrayList<>();

    public InventoryPanel() {
        setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout(0, 0));
        build();
    }

    private void build() {
        // ── Header ────────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout(Theme.PAD_M, 0));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(
                Theme.PAD_L, Theme.PAD_XL, Theme.PAD_M, Theme.PAD_XL));

        JLabel title = new JLabel("Inventory");
        title.setFont(Theme.FONT_HEADING);
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Manage your product catalog and stock levels.");
        sub.setFont(Theme.FONT_SUBHEAD);
        sub.setForeground(Theme.TEXT_SECONDARY);

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(sub);
        header.add(titleBlock, BorderLayout.WEST);

        JButton addBtn = accentButton("+ Add Product");
        addBtn.addActionListener(e -> showProductDialog(null));
        header.add(addBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── Toolbar ───────────────────────────────────────────────────────────
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.PAD_S, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, Theme.PAD_XL, Theme.PAD_M, Theme.PAD_XL));

        searchField = styledTextField("Search by name, SKU, brand…", 22);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { applyFilters(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { applyFilters(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
        });

        String[] catOptions = {"All Categories", "TOPS", "BOTTOMS", "DRESSES",
                               "OUTERWEAR", "ACCESSORIES", "FOOTWEAR"};
        categoryFilter = styledCombo(catOptions);
        categoryFilter.addActionListener(e -> applyFilters());

        String[] statusOpts = {"All Status", "IN_STOCK", "LOW_STOCK", "OUT_OF_STOCK"};
        JComboBox<String> statusFilter = styledCombo(statusOpts);
        statusFilter.addActionListener(e -> applyFilters());

        countLabel = new JLabel();
        countLabel.setFont(Theme.FONT_SMALL);
        countLabel.setForeground(Theme.TEXT_MUTED);

        toolbar.add(searchField);
        toolbar.add(Box.createHorizontalStrut(Theme.PAD_S));
        toolbar.add(categoryFilter);
        toolbar.add(Box.createHorizontalStrut(Theme.PAD_S));
        toolbar.add(statusFilter);
        toolbar.add(Box.createHorizontalStrut(Theme.PAD_M));
        toolbar.add(countLabel);
        add(toolbar, BorderLayout.CENTER); // temporarily; replaced below

        // ── Table ─────────────────────────────────────────────────────────────
        model = new InventoryTableModel();
        table = new JTable(model);
        styleTable();

        table.getSelectionModel().addListSelectionListener(e -> {
            // nothing for now — double-click triggers edit
        });

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) showProductDialog(displayed.get(row));
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(Theme.BG_DARK);
        scroll.getViewport().setBackground(Theme.BG_DARK);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, Theme.PAD_XL, Theme.PAD_XL, Theme.PAD_XL));
        scroll.getVerticalScrollBar().setBackground(Theme.BG_DARK);

        // Build content panel
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(toolbar, BorderLayout.NORTH);
        content.add(scroll,  BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);

        applyFilters();
    }

    // ── Table Styling ─────────────────────────────────────────────────────────
    private void styleTable() {
        table.setBackground(Theme.BG_DARK);
        table.setForeground(Theme.TEXT_PRIMARY);
        table.setFont(Theme.FONT_BODY);
        table.setRowHeight(42);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(Theme.BG_HOVER);
        table.setSelectionForeground(Theme.TEXT_PRIMARY);
        table.setFocusable(false);

        // Header
        JTableHeader hdr = table.getTableHeader();
        hdr.setBackground(Theme.BG_DARKEST);
        hdr.setForeground(Theme.TEXT_MUTED);
        hdr.setFont(new Font("SansSerif", Font.BOLD, 10));
        hdr.setPreferredSize(new Dimension(0, 36));
        hdr.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));
        hdr.setReorderingAllowed(false);

        // Column widths
        int[] widths = {60, 110, 190, 100, 90, 60, 80, 80, 80, 90, 100};
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Custom renderers
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                setBackground(sel ? Theme.BG_HOVER : (row % 2 == 0 ? Theme.BG_DARK : Theme.BG_CARD));
                setForeground(Theme.TEXT_PRIMARY);
                setFont(Theme.FONT_BODY);
                setBorder(BorderFactory.createEmptyBorder(0, Theme.PAD_M, 0, Theme.PAD_M));
                setOpaque(true);
                return this;
            }
        });

        // Status renderer (column index 10 = Status)
        table.getColumnModel().getColumn(10).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val,
                    boolean sel, boolean foc, int row, int col) {
                JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                p.setBackground(sel ? Theme.BG_HOVER : (row % 2 == 0 ? Theme.BG_DARK : Theme.BG_CARD));
                p.setBorder(BorderFactory.createEmptyBorder(8, Theme.PAD_M, 8, Theme.PAD_M));

                String status = val == null ? "" : val.toString();
                Color pillBg = switch (status) {
                    case "IN_STOCK"     -> new Color(Theme.SUCCESS.getRed(), Theme.SUCCESS.getGreen(), Theme.SUCCESS.getBlue(), 30);
                    case "LOW_STOCK"    -> new Color(Theme.WARNING.getRed(), Theme.WARNING.getGreen(), Theme.WARNING.getBlue(), 30);
                    case "OUT_OF_STOCK" -> new Color(Theme.DANGER.getRed(), Theme.DANGER.getGreen(), Theme.DANGER.getBlue(), 30);
                    default             -> Theme.BG_ELEVATED;
                };
                Color pillFg = switch (status) {
                    case "IN_STOCK"     -> Theme.SUCCESS;
                    case "LOW_STOCK"    -> Theme.WARNING;
                    case "OUT_OF_STOCK" -> Theme.DANGER;
                    default             -> Theme.TEXT_MUTED;
                };
                String label = status.replace("_", " ");

                JLabel pill = new JLabel(label) {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(pillBg);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
                pill.setFont(new Font("SansSerif", Font.BOLD, 10));
                pill.setForeground(pillFg);
                pill.setOpaque(false);
                pill.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
                p.add(pill);
                return p;
            }
        });

        // Action renderer (column 11)
        table.getColumnModel().getColumn(11).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val,
                    boolean sel, boolean foc, int row, int col) {
                JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
                p.setBackground(sel ? Theme.BG_HOVER : (row % 2 == 0 ? Theme.BG_DARK : Theme.BG_CARD));

                JLabel edit = iconLabel("✎ Edit",  Theme.ACCENT_GOLD);
                JLabel del  = iconLabel("✕ Del",   Theme.DANGER);
                p.add(edit);
                p.add(del);
                return p;
            }
        });

        // Action editor (column 11 — handles clicks)
        table.getColumnModel().getColumn(11).setCellEditor(new DefaultCellEditor(new JCheckBox()) {
            private int editRow;
            @Override
            public Component getTableCellEditorComponent(JTable tbl, Object val,
                    boolean sel, int row, int col) {
                editRow = row;
                return new JPanel(); // unused; click is handled in mouseListener
            }
        });

        // Context menu on right-click
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row < 0) return;
                    table.setRowSelectionInterval(row, row);
                    showContextMenu(e, displayed.get(row));
                }
            }
        });
    }

    private JLabel iconLabel(String text, Color c) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        l.setForeground(c);
        return l;
    }

    // ── Filter Logic ──────────────────────────────────────────────────────────
    private void applyFilters() {
        String query = searchField.getText().toLowerCase().trim();
        String cat   = (String) categoryFilter.getSelectedItem();

        displayed = new ArrayList<>();
        for (Product p : ds.getProducts()) {
            boolean matchSearch = query.isEmpty()
                    || p.getName().toLowerCase().contains(query)
                    || p.getSku().toLowerCase().contains(query)
                    || p.getBrand().toLowerCase().contains(query);
            boolean matchCat = cat == null || cat.equals("All Categories")
                    || p.getCategory().name().equals(cat);
            if (matchSearch && matchCat) displayed.add(p);
        }
        model.setData(displayed);
        countLabel.setText(displayed.size() + " product" + (displayed.size() == 1 ? "" : "s"));
    }

    // ── Context Menu ──────────────────────────────────────────────────────────
    private void showContextMenu(MouseEvent e, Product p) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(Theme.BG_ELEVATED);
        menu.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        JMenuItem editItem = new JMenuItem("✎  Edit Product");
        editItem.setFont(Theme.FONT_BODY);
        editItem.setForeground(Theme.TEXT_PRIMARY);
        editItem.setBackground(Theme.BG_ELEVATED);
        editItem.addActionListener(ev -> showProductDialog(p));

        JMenuItem delItem = new JMenuItem("✕  Delete Product");
        delItem.setFont(Theme.FONT_BODY);
        delItem.setForeground(Theme.DANGER);
        delItem.setBackground(Theme.BG_ELEVATED);
        delItem.addActionListener(ev -> deleteProduct(p));

        menu.add(editItem);
        menu.add(new JSeparator());
        menu.add(delItem);
        menu.show(table, e.getX(), e.getY());
    }

    // ── Add / Edit Dialog ─────────────────────────────────────────────────────
    private void showProductDialog(Product existing) {
        boolean isNew = existing == null;
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                isNew ? "Add Product" : "Edit Product", true);
        dlg.setSize(520, 580);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(Theme.BG_DARK);
        dlg.setLayout(new BorderLayout());

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.BG_DARK);
        form.setBorder(BorderFactory.createEmptyBorder(Theme.PAD_L, Theme.PAD_L, 0, Theme.PAD_L));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Fields
        JTextField fSku    = dlgField(isNew ? "" : existing.getSku());
        JTextField fName   = dlgField(isNew ? "" : existing.getName());
        JTextField fBrand  = dlgField(isNew ? "" : existing.getBrand());
        JComboBox<Category> fCat = new JComboBox<>(Category.values());
        if (!isNew) fCat.setSelectedItem(existing.getCategory());
        styleComboBox(fCat);
        JTextField fSize   = dlgField(isNew ? "" : existing.getSize());
        JTextField fColor  = dlgField(isNew ? "" : existing.getColor());
        JTextField fCost   = dlgField(isNew ? "" : String.format("%.2f", existing.getCostPrice()));
        JTextField fPrice  = dlgField(isNew ? "" : String.format("%.2f", existing.getSellingPrice()));
        JTextField fQty    = dlgField(isNew ? "" : String.valueOf(existing.getQuantity()));
        JTextField fReorder= dlgField(isNew ? "5" : String.valueOf(existing.getReorderLevel()));

        String[][] rows = {
            {"SKU",            null},
            {"Name",           null},
            {"Brand",          null},
            {"Category",       null},
            {"Size",           null},
            {"Color",          null},
            {"Cost Price ($)", null},
            {"Selling Price ($)", null},
            {"Quantity",       null},
            {"Reorder Level",  null},
        };
        Component[] fields = {fSku, fName, fBrand, fCat, fSize, fColor, fCost, fPrice, fQty, fReorder};

        for (int i = 0; i < fields.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.3;
            JLabel lbl = new JLabel(rows[i][0]);
            lbl.setFont(Theme.FONT_BODY);
            lbl.setForeground(Theme.TEXT_SECONDARY);
            form.add(lbl, gbc);
            gbc.gridx = 1; gbc.weightx = 0.7;
            form.add(fields[i], gbc);
        }

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBackground(Theme.BG_DARK);
        formScroll.getViewport().setBackground(Theme.BG_DARK);
        formScroll.setBorder(null);
        dlg.add(formScroll, BorderLayout.CENTER);

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, Theme.PAD_S, Theme.PAD_M));
        btnRow.setBackground(Theme.BG_DARK);
        btnRow.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER));

        JButton cancel = ghostButton("Cancel");
        cancel.addActionListener(e -> dlg.dispose());

        JButton save = accentButton(isNew ? "Add Product" : "Save Changes");
        save.addActionListener(e -> {
            try {
                String sku    = fSku.getText().trim();
                String name   = fName.getText().trim();
                String brand  = fBrand.getText().trim();
                Category cat  = (Category) fCat.getSelectedItem();
                String size   = fSize.getText().trim();
                String color  = fColor.getText().trim();
                double cost   = Double.parseDouble(fCost.getText().trim());
                double price  = Double.parseDouble(fPrice.getText().trim());
                int qty       = Integer.parseInt(fQty.getText().trim());
                int reorder   = Integer.parseInt(fReorder.getText().trim());

                if (isNew) {
                    ds.addProduct(new Product(ds.nextProductId(), sku, name, brand,
                            cat, size, color, cost, price, qty, reorder));
                } else {
                    existing.setName(name); existing.setBrand(brand);
                    existing.setCategory(cat); existing.setSize(size);
                    existing.setColor(color); existing.setCostPrice(cost);
                    existing.setSellingPrice(price); existing.setQuantity(qty);
                    existing.setReorderLevel(reorder);
                    ds.updateProduct(existing);
                }
                applyFilters();
                dlg.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg,
                        "Please enter valid numbers for price, quantity and reorder level.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRow.add(cancel);
        btnRow.add(save);
        dlg.add(btnRow, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void deleteProduct(Product p) {
        int choice = JOptionPane.showConfirmDialog(this,
                "Delete \"" + p.getName() + "\"? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            ds.deleteProduct(p.getId());
            applyFilters();
        }
    }

    // ── Table Model ───────────────────────────────────────────────────────────
    private class InventoryTableModel extends AbstractTableModel {
        private List<Product> data = new ArrayList<>();
        private final String[] COL = {
            "ID", "SKU", "Name", "Brand", "Category", "Size", "Color",
            "Cost", "Price", "Qty", "Status", "Actions"
        };

        void setData(List<Product> d) { this.data = d; fireTableDataChanged(); }

        @Override public int getRowCount()    { return data.size(); }
        @Override public int getColumnCount() { return COL.length; }
        @Override public String getColumnName(int c) { return COL[c]; }
        @Override public boolean isCellEditable(int r, int c) { return c == 11; }

        @Override public Object getValueAt(int r, int c) {
            Product p = data.get(r);
            return switch (c) {
                case 0  -> p.getId();
                case 1  -> p.getSku();
                case 2  -> p.getName();
                case 3  -> p.getBrand();
                case 4  -> p.getCategory().name();
                case 5  -> p.getSize();
                case 6  -> p.getColor();
                case 7  -> currency.format(p.getCostPrice());
                case 8  -> currency.format(p.getSellingPrice());
                case 9  -> p.getQuantity();
                case 10 -> p.getStatus().name();
                case 11 -> "actions";
                default -> "";
            };
        }
    }

    // ── UI Helpers ────────────────────────────────────────────────────────────
    private JTextField styledTextField(String placeholder, int cols) {
        JTextField f = new JTextField(cols) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.RADIUS_S, Theme.RADIUS_S);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        f.setFont(Theme.FONT_BODY);
        f.setForeground(Theme.TEXT_PRIMARY);
        f.setBackground(Theme.BG_CARD);
        f.setCaretColor(Theme.ACCENT_GOLD);
        f.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        f.setOpaque(false);
        return f;
    }

    private JTextField dlgField(String value) {
        JTextField f = styledTextField("", 20);
        f.setText(value);
        f.setBackground(Theme.BG_ELEVATED);
        return f;
    }

    private JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        styleComboBox(cb);
        return cb;
    }

    private void styleComboBox(JComboBox<?> cb) {
        cb.setBackground(Theme.BG_CARD);
        cb.setForeground(Theme.TEXT_PRIMARY);
        cb.setFont(Theme.FONT_BODY);
        cb.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v,
                    int i, boolean s, boolean f) {
                super.getListCellRendererComponent(l, v, i, s, f);
                setBackground(s ? Theme.BG_HOVER : Theme.BG_ELEVATED);
                setForeground(Theme.TEXT_PRIMARY);
                setFont(Theme.FONT_BODY);
                setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
                return this;
            }
        });
    }

    private JButton accentButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, Theme.ACCENT_GOLD,
                        0, getHeight(), Theme.ACCENT_DIM);
                g2.setPaint(getModel().isPressed() ? new GradientPaint(0, 0, Theme.ACCENT_DIM, 0, getHeight(), Theme.ACCENT_GOLD) : gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.RADIUS_S, Theme.RADIUS_S);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(Theme.FONT_BODY_B);
        btn.setForeground(Theme.BG_DARKEST);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return btn;
    }

    private JButton ghostButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? Theme.BG_HOVER : Theme.BG_ELEVATED);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.RADIUS_S, Theme.RADIUS_S);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(Theme.FONT_BODY);
        btn.setForeground(Theme.TEXT_SECONDARY);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return btn;
    }
}
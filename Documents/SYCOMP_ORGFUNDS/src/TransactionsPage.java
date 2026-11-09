import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.geom.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/** Transaction form + searchable JTable with Add / Update / Delete / Search / Clear. */
public class TransactionsPage extends Theme.ScrollPanel {
    private static final String[] CATEGORIES = {"Membership Fees", "Fundraising", "Donations", "Events",
            "Supplies", "Meetings", "Projects", "Other"};

    private final DashboardFrame shell;
    private final FinanceManager manager;

    private final JTextField idField = new JTextField();
    private final JTextField dateField = new JTextField();
    private final JTextField descField = new JTextField();
    private final JComboBox<String> categoryBox = new JComboBox<>(CATEGORIES);
    private final JComboBox<String> typeBox = new JComboBox<>(new String[]{"Income", "Expense"});
    private final JTextField amountField = new JTextField();
    private final JTextField searchField = new Theme.HintField("Search by ID or description");
    private final JLabel countLabel = Theme.label(" ", Font.PLAIN, 12.5f, Theme.MUTED);
    private final JLabel modeLabel = Theme.label("New transaction", Font.BOLD, 17f, Theme.TEXT);

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Date", "Description", "Category", "Type", "Amount"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private List<FinancialTransaction> shown = new ArrayList<>();

    private final Theme.RoundButton addBtn = new Theme.RoundButton("Add", Theme.GREEN, false);
    private final Theme.RoundButton updateBtn = new Theme.RoundButton("Update", Theme.PURPLE, false);
    private final Theme.RoundButton deleteBtn = new Theme.RoundButton("Delete", Theme.RED, false);
    private final Theme.RoundButton clearBtn = new Theme.RoundButton("Clear", Theme.PURPLE, true);

    public TransactionsPage(DashboardFrame shell, FinanceManager manager) {
        this.shell = shell;
        this.manager = manager;
        setLayout(new BorderLayout(0, 16));
        setBorder(new EmptyBorder(20, 26, 24, 26));

        // page header
        JPanel head = new JPanel(new BorderLayout(16, 0));
        head.setOpaque(false);
        head.add(Theme.iconCircle("coins", 56), BorderLayout.WEST);
        JPanel ht = new JPanel();
        ht.setOpaque(false);
        ht.setLayout(new BoxLayout(ht, BoxLayout.Y_AXIS));
        ht.add(Box.createVerticalGlue());
        ht.add(Theme.label("Transactions", Font.BOLD, 28f, Theme.PURPLE_DARK));
        ht.add(Theme.label("Record, search, update and delete the organization's income and expenses.", Font.PLAIN, 13.5f, Theme.MUTED));
        ht.add(Box.createVerticalGlue());
        head.add(ht, BorderLayout.CENTER);
        head.setPreferredSize(new Dimension(100, 62));
        add(head, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(16, 0));
        body.setOpaque(false);
        body.setPreferredSize(new Dimension(100, 600));
        body.add(buildFormCard(), BorderLayout.WEST);
        body.add(buildTableCard(), BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        manager.addChangeListener(this::refreshTable);
        clearForm();
        refreshTable();
    }

    // ------------------------------------------------------------------ layout
    private JPanel buildFormCard() {
        Theme.Card card = new Theme.Card(new BorderLayout(0, 12));
        card.setPreferredSize(new Dimension(300, 100));
        card.add(modeLabel, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        Theme.styleField(idField);
        Theme.styleField(dateField);
        Theme.styleField(descField);
        Theme.styleField(amountField);
        categoryBox.setEditable(true);
        Theme.styleCombo(categoryBox);
        Theme.styleCombo(typeBox);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;
        row = addRow(form, c, row, "Transaction ID", idField);
        row = addRow(form, c, row, "Date (yyyy-MM-dd)", dateField);
        row = addRow(form, c, row, "Description", descField);
        row = addRow(form, c, row, "Category", categoryBox);
        row = addRow(form, c, row, "Type", typeBox);
        row = addRow(form, c, row, "Amount (₱)", amountField);

        JPanel buttons = new JPanel(new GridLayout(2, 2, 10, 10));
        buttons.setOpaque(false);
        buttons.add(addBtn); buttons.add(updateBtn); buttons.add(deleteBtn); buttons.add(clearBtn);
        c.gridy = row; c.insets = new Insets(16, 0, 0, 0);
        form.add(buttons, c);

        addBtn.addActionListener(e -> handleAdd());
        updateBtn.addActionListener(e -> handleUpdate());
        deleteBtn.addActionListener(e -> handleDelete());
        clearBtn.addActionListener(e -> { searchField.setText(""); clearForm(); refreshTable(); });

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(form, BorderLayout.NORTH);
        card.add(wrap, BorderLayout.CENTER);
        return card;
    }

    private int addRow(JPanel p, GridBagConstraints c, int row, String label, JComponent field) {
        c.gridy = row; c.insets = new Insets(row == 0 ? 0 : 9, 0, 4, 0);
        p.add(Theme.fieldLabel(label), c);
        c.gridy = row + 1; c.insets = new Insets(0, 0, 0, 0);
        p.add(field, c);
        return row + 2;
    }

    private JPanel buildTableCard() {
        Theme.Card card = new Theme.Card(new BorderLayout(0, 12));

        Theme.styleField(searchField);
        searchField.setToolTipText("Search by transaction ID or description");
        searchField.addActionListener(e -> handleSearch());
        Theme.RoundButton searchBtn = new Theme.RoundButton("Search", Theme.PURPLE, false).icon("search");
        searchBtn.addActionListener(e -> handleSearch());
        Theme.RoundButton showAll = new Theme.RoundButton("Show all", Theme.PURPLE, true);
        showAll.addActionListener(e -> { searchField.setText(""); refreshTable(); });

        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setOpaque(false);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(searchBtn); buttons.add(showAll);
        bar.add(Theme.label("Records", Font.BOLD, 17f, Theme.TEXT), BorderLayout.WEST);
        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setOpaque(false);
        searchRow.add(searchField, BorderLayout.CENTER);
        searchRow.add(buttons, BorderLayout.EAST);
        searchRow.setPreferredSize(new Dimension(470, 40));
        bar.add(searchRow, BorderLayout.EAST);
        card.add(bar, BorderLayout.NORTH);

        Theme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TableColumnModel cm = table.getColumnModel();
        Theme.PaddedRenderer left = new Theme.PaddedRenderer(SwingConstants.LEFT);
        for (int i : new int[]{0, 1, 2, 3}) cm.getColumn(i).setCellRenderer(left);
        cm.getColumn(4).setCellRenderer(new Theme.PillRenderer());
        cm.getColumn(5).setCellRenderer(new Theme.PaddedRenderer(SwingConstants.RIGHT) {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                l.setForeground("Income".equals(t.getValueAt(r, 4)) ? Theme.GREEN : Theme.RED);
                l.setFont(Theme.font(Font.BOLD, 13f));
                return l;
            }
        });
        int[] widths = {80, 110, 200, 130, 110, 130};
        for (int i = 0; i < widths.length; i++) cm.getColumn(i).setPreferredWidth(widths[i]);
        table.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) loadSelectedIntoForm(); });

        card.add(Theme.scroll(table), BorderLayout.CENTER);
        card.add(countLabel, BorderLayout.SOUTH);
        return card;
    }

    // ---------------------------------------------------------------- behavior
    public void refreshTable() { fillTable(manager.searchTransaction(searchField.getText())); }

    public void applySearch(String text) { searchField.setText(text); handleSearch(); }

    /** Selects the row with this ID (used when a recent activity is clicked on the dashboard). */
    public void focusTransaction(String id) {
        searchField.setText("");
        refreshTable();
        for (int i = 0; i < shown.size(); i++)
            if (shown.get(i).getId().equalsIgnoreCase(id)) {
                table.setRowSelectionInterval(i, i);
                table.scrollRectToVisible(table.getCellRect(i, 0, true));
                return;
            }
    }

    private void fillTable(List<FinancialTransaction> list) {
        String keepId = selectedId();
        shown = manager.newestFirst(list);
        model.setRowCount(0);
        for (FinancialTransaction t : shown)
            model.addRow(new Object[]{t.getId(), t.getDate(), t.getDescription(), t.getCategory(), t.getType(),
                    (t instanceof Income ? "+ " : "− ") + Theme.peso(t.getAmount())});
        countLabel.setText("Showing " + shown.size() + " of " + manager.getCount() + " transactions");
        if (keepId != null)
            for (int i = 0; i < shown.size(); i++)
                if (shown.get(i).getId().equalsIgnoreCase(keepId)) { table.setRowSelectionInterval(i, i); break; }
    }

    private String selectedId() {
        int r = table.getSelectedRow();
        return r >= 0 && r < shown.size() ? shown.get(r).getId() : null;
    }

    private void loadSelectedIntoForm() {
        int r = table.getSelectedRow();
        if (r < 0 || r >= shown.size()) return;
        FinancialTransaction t = shown.get(r);
        idField.setText(t.getId());
        idField.setEditable(false);              // the ID identifies the record being edited
        idField.setBackground(new Color(0xF3F0FD));
        dateField.setText(t.getDate().toString());
        descField.setText(t.getDescription());
        categoryBox.setSelectedItem(t.getCategory());
        typeBox.setSelectedItem(t.getType());
        amountField.setText(t.getAmount().toPlainString());
        modeLabel.setText("Edit transaction");
        addBtn.setEnabled(false);
    }

    private void clearForm() {
        table.clearSelection();
        idField.setEditable(true);
        idField.setBackground(Color.WHITE);
        idField.setText(manager.nextSuggestedId());
        dateField.setText(LocalDate.now().toString());
        descField.setText("");
        categoryBox.setSelectedIndex(0);
        typeBox.setSelectedIndex(0);
        amountField.setText("");
        modeLabel.setText("New transaction");
        addBtn.setEnabled(true);
    }

    /** Validates every field; shows an error dialog and returns null on failure. */
    public FinancialTransaction validateInput() {
        String id = idField.getText().trim();
        String desc = descField.getText().trim();
        Object cat = categoryBox.getEditor().getItem();
        String category = cat == null ? "" : cat.toString().trim();
        String amountText = amountField.getText().trim().replace(",", "").replace("₱", "");

        if (id.isEmpty()) return fail("Please enter a Transaction ID.", idField);
        if (dateField.getText().trim().isEmpty()) return fail("Please enter a date.", dateField);
        if (desc.isEmpty()) return fail("Description cannot be empty.", descField);
        if (category.isEmpty()) return fail("Please choose or type a category.", categoryBox);
        if (amountText.isEmpty()) return fail("Please enter an amount.", amountField);

        LocalDate date;
        try { date = LocalDate.parse(dateField.getText().trim()); }
        catch (DateTimeParseException ex) { return fail("Date must use the format yyyy-MM-dd (e.g. 2026-10-22).", dateField); }

        BigDecimal amount;
        try { amount = new BigDecimal(amountText); }
        catch (NumberFormatException ex) { return fail("Amount must be a number (digits only, e.g. 1500 or 1500.50).", amountField); }
        if (amount.signum() <= 0) return fail("Amount must be greater than zero.", amountField);

        try {
            return "Income".equals(typeBox.getSelectedItem())
                    ? new Income(id, date, desc, category, amount)
                    : new Expense(id, date, desc, category, amount);
        } catch (IllegalArgumentException ex) {
            return fail(ex.getMessage(), idField);
        }
    }

    private FinancialTransaction fail(String msg, JComponent focus) {
        JOptionPane.showMessageDialog(this, msg, "Invalid input", JOptionPane.ERROR_MESSAGE);
        focus.requestFocusInWindow();
        return null;
    }

    public void handleAdd() {
        FinancialTransaction t = validateInput();
        if (t == null) return;
        try {
            manager.addTransaction(t);
        } catch (IllegalArgumentException ex) {
            fail(ex.getMessage(), idField);
            return;
        }
        JOptionPane.showMessageDialog(this, "Transaction " + t.getId() + " was added.", "Success", JOptionPane.INFORMATION_MESSAGE);
        clearForm();
    }

    public void handleUpdate() {
        String id = selectedId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Select a record in the table first.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        FinancialTransaction t = validateInput();
        if (t == null) return;
        manager.updateTransaction(id, t);
        JOptionPane.showMessageDialog(this, "Transaction " + id + " was updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public void handleDelete() {
        String id = selectedId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Select a record in the table first.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "Delete transaction " + id + "? This cannot be undone.",
                "Confirm delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;
        manager.deleteTransaction(id);
        clearForm();
        JOptionPane.showMessageDialog(this, "Transaction " + id + " was deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
    }

    public void handleSearch() {
        List<FinancialTransaction> hits = manager.searchTransaction(searchField.getText());
        fillTable(hits);
        if (hits.isEmpty() && !searchField.getText().isBlank())
            JOptionPane.showMessageDialog(this, "No transaction matches \"" + searchField.getText().trim() + "\".",
                    "Search", JOptionPane.INFORMATION_MESSAGE);
        else if (hits.size() == 1) table.setRowSelectionInterval(0, 0);
    }
}

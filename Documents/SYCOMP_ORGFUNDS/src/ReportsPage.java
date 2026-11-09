import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.*;

/** Report library: filters, statistics, paginated report table, previous-report access. */
public class ReportsPage extends Theme.ScrollPanel {
    private static final String[] SEMESTERS = {"Whole School Year", "1st Semester", "2nd Semester", "Summer"};
    private static final String[] TYPES = {"All Types", "Financial", "Membership", "Activity"};
    private static final int PAGE_SIZE = 7;
    private static final double[] COLS = {2.3, 1.4, 1.7, 1.1, 1.25, 1.9};

    private final DashboardFrame shell;
    private final FinanceManager manager;
    private final ReportCatalog catalog;

    private final JComboBox<String> syBox = new JComboBox<>();
    private final JComboBox<String> semBox = new JComboBox<>(SEMESTERS);
    private final JComboBox<String> typeBox = new JComboBox<>(TYPES);
    private final JTextField searchField = new Theme.HintField("Search reports...", "search");
    private final JComboBox<String> prevSy = new JComboBox<>();
    private final JComboBox<String> prevSem = new JComboBox<>(SEMESTERS);

    private SchoolPeriod period = SchoolPeriod.current();
    private String typeFilter = "All Types";
    private String searchText = "";
    private int page = 0;
    private int activeTab = 0;

    private final JPanel tableBody = new JPanel();
    private final JPanel pager = new JPanel(new BorderLayout());
    private final JPanel recentList = new JPanel();
    private final JLabel[] statValues = new JLabel[4];
    private final JLabel[] statSubs = new JLabel[4];
    private final Chip[] chips = new Chip[3];
    private boolean updating = false;

    public ReportsPage(DashboardFrame shell, FinanceManager manager, ReportCatalog catalog) {
        this.shell = shell;
        this.manager = manager;
        this.catalog = catalog;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(20, 26, 24, 26));

        add(Theme.fixedHeight(buildHeader(), 66));
        add(Box.createVerticalStrut(14));
        add(Theme.fixedHeight(buildFilterCard(), 160));
        add(Box.createVerticalStrut(14));
        add(Theme.fixedHeight(buildStats(), 112));
        add(Box.createVerticalStrut(14));
        add(Theme.fixedHeight(Theme.row(14, new double[]{2.15, 1}, buildTableCard(), buildRightColumn()), 572));
        add(Box.createVerticalStrut(14));
        add(Theme.fixedHeight(buildNote(), 58));

        refreshYears();
        syncControls();
        manager.addChangeListener(() -> { refreshYears(); rebuild(); });
        rebuild();
    }

    // ------------------------------------------------------------------ pieces
    private JPanel buildHeader() {
        JPanel head = new JPanel(new BorderLayout(16, 0));
        head.setOpaque(false);
        head.add(Theme.iconCircle("doc", 56), BorderLayout.WEST);
        JPanel t = new JPanel();
        t.setOpaque(false);
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        t.add(Box.createVerticalGlue());
        t.add(Theme.label("Reports", Font.BOLD, 28f, Theme.PURPLE_LIGHT));
        t.add(Theme.label("View and download financial reports, activity reports, and organization records.", Font.PLAIN, 13.5f, Theme.MUTED));
        t.add(Box.createVerticalGlue());
        head.add(t, BorderLayout.CENTER);
        Theme.RoundButton exp = new Theme.RoundButton("Export / Download", Theme.PURPLE, false).icon("download");
        exp.addActionListener(e -> {
            ReportCatalog.Content c = catalog.ledger(period);
            ReportDialog.export(this, catalog, c, "SYCOMP_Ledger_" + period.startYear + "-" + (period.startYear + 1) + "_" + period.semesterLabel().replace(' ', '-'));
            rebuild();
        });
        JPanel eb = new JPanel(new GridBagLayout());
        eb.setOpaque(false);
        eb.add(exp);
        head.add(eb, BorderLayout.EAST);
        return head;
    }

    private JPanel buildFilterCard() {
        Theme.Card card = new Theme.Card(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(14, 18, 18, 18));
        Theme.styleCombo(syBox); Theme.styleCombo(semBox); Theme.styleCombo(typeBox);
        Theme.styleField(searchField);
        searchField.setBorder(new CompoundBorder(new LineBorder(Theme.BORDER, 1, true), new EmptyBorder(7, 34, 7, 11)));
        searchField.setToolTipText("Search report titles");
        searchField.addActionListener(e -> applyFilters());

        Theme.RoundButton apply = new Theme.RoundButton("Apply Filters", Theme.PURPLE, false).icon("filter");
        apply.addActionListener(e -> applyFilters());

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        String[] names = {"School Year", "Semester", "Report Type", null, null};
        JComponent[] comps = {syBox, semBox, typeBox, searchField, apply};
        double[] wx = {1.25, 1.1, 1.1, 1.5, 0.0};
        for (int i = 0; i < comps.length; i++) {
            c.gridx = i; c.weightx = wx[i];
            c.insets = new Insets(0, i == 0 ? 0 : 12, 0, 0);
            c.gridy = 0;
            fields.add(names[i] == null ? Box.createVerticalStrut(16) : Theme.fieldLabel(names[i]), c);
            c.gridy = 1;
            fields.add(comps[i], c);
        }
        card.add(fields, BorderLayout.NORTH);

        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8)) {
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setColor(new Color(0x211C39));
                g.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));
                g.dispose();
            }
        };
        tabs.setOpaque(false);
        String[] tn = {"Current Semester", "Previous Semesters", "Previous School Years"};
        String[] ti = {"calendar", "history", "calendar"};
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            chips[i] = new Chip(tn[i], ti[i], () -> selectTab(idx));
            tabs.add(chips[i]);
        }
        tabs.setPreferredSize(new Dimension(100, 52));
        card.add(tabs, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildStats() {
        String[] titles = {"Total Reports", "Published Reports", "Pending Reports", "Total Downloads"};
        String[] icons = {"doc", "folder", "clock", "download"};
        Color[][] tones = {{Theme.PURPLE_LIGHT, Theme.PURPLE_DARK}, {new Color(0x8B6CF0), new Color(0x4A2FC9)},
                {new Color(0x6FB0FF), new Color(0x2F63D4)}, {new Color(0x3FCB93), new Color(0x138A5E)}};
        JPanel row = new JPanel(new Theme.RowLayout(14, 1, 1, 1, 1));
        row.setOpaque(false);
        for (int i = 0; i < 4; i++) {
            Theme.Card c = new Theme.Card(new BorderLayout(14, 0));
            c.setBorder(new EmptyBorder(14, 16, 16, 12));
            JPanel iw = new JPanel(new BorderLayout());
            iw.setOpaque(false);
            iw.add(Theme.iconCircle(icons[i], 52, tones[i][0], tones[i][1], Color.WHITE), BorderLayout.NORTH);
            JPanel col = new JPanel();
            col.setOpaque(false);
            col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
            col.add(Theme.label(titles[i], Font.BOLD, 13f, Theme.TEXT));
            statValues[i] = Theme.label("0", Font.BOLD, 26f, Theme.TEXT);
            col.add(statValues[i]);
            statSubs[i] = Theme.label(" ", Font.PLAIN, 12f, Theme.MUTED);
            col.add(statSubs[i]);
            c.add(iw, BorderLayout.WEST);
            c.add(col, BorderLayout.CENTER);
            row.add(c);
        }
        return row;
    }

    private JPanel buildTableCard() {
        Theme.Card card = new Theme.Card(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(Theme.cardTitle("doc", "Available Reports", null), BorderLayout.WEST);
        Theme.RoundButton arch = new Theme.RoundButton("View Report Archive", Theme.PURPLE, true).icon("history");
        arch.setFont(Theme.font(Font.BOLD, 12f));
        arch.setBorder(new EmptyBorder(7, 14, 7, 14));
        arch.addActionListener(e -> { typeBox.setSelectedIndex(0); searchField.setText(""); selectTab(2); });
        top.add(arch, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        tableBody.setOpaque(false);
        tableBody.setLayout(new BoxLayout(tableBody, BoxLayout.Y_AXIS));
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(tableBody, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        pager.setOpaque(false);
        pager.setPreferredSize(new Dimension(100, 40));
        card.add(pager, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildRightColumn() {
        JPanel col = new JPanel(new BorderLayout(0, 14));
        col.setOpaque(false);

        Theme.Card prev = new Theme.Card(new GridBagLayout());
        prev.setBorder(new EmptyBorder(16, 16, 16, 16));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.anchor = GridBagConstraints.NORTHWEST;
        c.gridy = 0;
        prev.add(Theme.cardTitle("history", "Access Previous Reports", "View reports from past semesters and school years."), c);
        Theme.styleCombo(prevSy); Theme.styleCombo(prevSem);
        prev.setPreferredSize(new Dimension(100, 292));
        c.gridy = 1; c.insets = new Insets(14, 0, 4, 0); prev.add(Theme.fieldLabel("School Year"), c);
        c.gridy = 2; c.insets = new Insets(0, 0, 0, 0); prev.add(prevSy, c);
        c.gridy = 3; c.insets = new Insets(10, 0, 4, 0); prev.add(Theme.fieldLabel("Semester"), c);
        c.gridy = 4; c.insets = new Insets(0, 0, 0, 0); prev.add(prevSem, c);
        Theme.RoundButton go = new Theme.RoundButton("View Previous Reports", Theme.PURPLE, false).icon("search");
        go.addActionListener(e -> {
            period = new SchoolPeriod(startYearAt(prevSy.getSelectedIndex()), prevSem.getSelectedIndex());
            page = 0;
            syncControls();
            rebuild();
        });
        c.gridy = 5; c.insets = new Insets(14, 0, 0, 0); c.fill = GridBagConstraints.NONE;
        prev.add(go, c);
        c.gridy = 6; c.weighty = 1; c.insets = new Insets(0, 0, 0, 0);
        prev.add(Box.createVerticalGlue(), c);
        prev.setPreferredSize(new Dimension(100, 292));
        col.add(prev, BorderLayout.NORTH);

        Theme.Card rec = new Theme.Card(new BorderLayout(0, 8));
        rec.setBorder(new EmptyBorder(16, 16, 10, 16));
        rec.add(Theme.cardTitle("clock", "Recently Opened", null), BorderLayout.NORTH);
        recentList.setOpaque(false);
        recentList.setLayout(new BoxLayout(recentList, BoxLayout.Y_AXIS));
        rec.add(recentList, BorderLayout.CENTER);
        col.add(rec, BorderLayout.CENTER);
        return col;
    }

    private JPanel buildNote() {
        Theme.Card c = new Theme.Card(new BorderLayout(12, 0)).tint(new Color(0x211C39), new Color(0x19162D));
        c.setBorder(new EmptyBorder(8, 18, 12, 18));
        c.add(new JLabel(Icons.of("info", 18, Theme.PURPLE)), BorderLayout.WEST);
        c.add(Theme.label("<html><b>Note:</b> Reports are generated from the recorded transactions of the selected school year and semester. For unavailable reports, please contact the administrator.</html>",
                Font.PLAIN, 12f, Theme.MUTED), BorderLayout.CENTER);
        JPanel r = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        r.setOpaque(false);
        r.add(new JLabel(Icons.of("mail", 16, Theme.PURPLE)));
        r.add(Theme.link("Request a Report", () -> shell.openContact("Report Request")));
        c.add(r, BorderLayout.EAST);
        return c;
    }

    // ------------------------------------------------------------- filter state
    private int startYearAt(int idx) {
        List<Integer> ys = manager.schoolYears();
        return ys.get(Math.max(0, Math.min(idx, ys.size() - 1)));
    }

    private void refreshYears() {
        updating = true;
        List<Integer> ys = manager.schoolYears();
        for (JComboBox<String> b : List.of(syBox, prevSy)) {
            int keep = b.getSelectedIndex() >= 0 && b.getItemCount() > b.getSelectedIndex() ? oldYear(b) : -1;
            b.removeAllItems();
            for (int y : ys) b.addItem(y + " – " + (y + 1));
            int idx = keep < 0 ? (b == prevSy ? Math.min(1, ys.size() - 1) : 0) : Math.max(0, ys.indexOf(keep));
            b.setSelectedIndex(idx);
        }
        updating = false;
        syncControls();
    }

    private int oldYear(JComboBox<String> b) {
        String s = String.valueOf(b.getSelectedItem());
        try { return Integer.parseInt(s.substring(0, 4)); } catch (RuntimeException e) { return -1; }
    }

    /** Shows the active period in the dropdowns and highlights the matching tab. */
    private void syncControls() {
        updating = true;
        List<Integer> ys = manager.schoolYears();
        int i = ys.indexOf(period.startYear);
        if (i < 0) { refreshYearsAdd(); i = manager.schoolYears().indexOf(period.startYear); }
        if (i >= 0 && syBox.getItemCount() > i) syBox.setSelectedIndex(i);
        semBox.setSelectedIndex(period.semester);
        typeBox.setSelectedItem(typeFilter);
        updating = false;
        SchoolPeriod cur = SchoolPeriod.current();
        activeTab = period.equals(cur) ? 0 : period.equals(cur.previousRegular()) ? 1
                : (period.semester == 0 && period.startYear < cur.startYear) ? 2 : -1;
        for (int k = 0; k < chips.length; k++) if (chips[k] != null) chips[k].setSelected(k == activeTab);
    }

    private void refreshYearsAdd() { /* period always comes from the dropdowns, so it is always in the list */ }

    private void applyFilters() {
        if (updating) return;
        period = new SchoolPeriod(startYearAt(syBox.getSelectedIndex()), semBox.getSelectedIndex());
        typeFilter = String.valueOf(typeBox.getSelectedItem());
        searchText = searchField.getText().trim();
        page = 0;
        syncControls();
        rebuild();
    }

    private void selectTab(int i) {
        SchoolPeriod cur = SchoolPeriod.current();
        period = i == 0 ? cur : i == 1 ? cur.previousRegular() : new SchoolPeriod(cur.startYear - 1, 0);
        page = 0;
        syncControls();
        rebuild();
    }

    public void applySearch(String text) {
        searchField.setText(text);
        typeFilter = "All Types";
        searchText = text.trim();
        page = 0;
        syncControls();
        rebuild();
    }

    // ----------------------------------------------------------------- rebuild
   
private List<ReportCatalog.Report> filtered() {
    List<ReportCatalog.Report> out = new ArrayList<>();

    String keyword = searchText.trim().toLowerCase(Locale.ROOT);

    for (ReportCatalog.Report r : catalog.all()) {

        if (!"All Types".equals(typeFilter)
                && !r.type.equals(typeFilter)) {
            continue;
        }

        String searchableText =
                r.title + " "
                + r.type + " "
                + r.id + " "
                + r.description;

        if (!searchableText.toLowerCase(Locale.ROOT)
                .contains(keyword)) {
            continue;
        }

        out.add(r);
    }

    return out;
}


    private void rebuild() {
        // stats
        int total = catalog.all().size(), pub = catalog.publishedCount();
        statValues[0].setText(String.valueOf(total)); statSubs[0].setText("available report types");
        statValues[1].setText(String.valueOf(pub)); statSubs[1].setText("ready to view");
        statValues[2].setText(String.valueOf(total - pub)); statSubs[2].setText("awaiting publication");
        statValues[3].setText(String.valueOf(catalog.downloads())); statSubs[3].setText("CSV exports");

        // table
        List<ReportCatalog.Report> list = filtered();
        int pages = Math.max(1, (list.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        page = Math.min(page, pages - 1);
        tableBody.removeAll();
        tableBody.add(headerRow());
        int from = page * PAGE_SIZE, to = Math.min(list.size(), from + PAGE_SIZE);
        for (int i = from; i < to; i++) tableBody.add(reportRow(list.get(i)));
        if (list.isEmpty()) {
            JLabel e = Theme.label("No reports match your filters.", Font.PLAIN, 13.5f, Theme.MUTED);
            e.setBorder(new EmptyBorder(40, 14, 0, 0));
            tableBody.add(e);
        }
        buildPager(pages, from, to, list.size());

        // recently opened
        recentList.removeAll();
        List<ReportCatalog.Opened> rec = catalog.recent();
        if (rec.isEmpty()) {
            JLabel e = Theme.label("<html>Reports you open will appear here.</html>", Font.PLAIN, 12.5f, Theme.MUTED);
            e.setBorder(new EmptyBorder(8, 2, 0, 0));
            recentList.add(e);
        }
        DateTimeFormatter df = DateTimeFormatter.ofPattern("MMM d, yyyy");
        for (ReportCatalog.Opened o : rec) {
            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setOpaque(false);
            row.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER), new EmptyBorder(6, 0, 6, 0)));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
            JPanel t = new JPanel(new GridLayout(2, 1));
            t.setOpaque(false);
            t.add(Theme.label(o.report.title, Font.BOLD, 12f, Theme.TEXT));
            t.add(Theme.label("SY " + o.period.schoolYearLabel() + ", " + o.period.semesterLabel() + " · " + o.on.format(df), Font.PLAIN, 11f, Theme.MUTED));
            row.add(Theme.iconCircle("doc", 30, Theme.LAVENDER, Theme.LAVENDER, Theme.PURPLE), BorderLayout.WEST);
            row.add(t, BorderLayout.CENTER);
            JLabel dl = new JLabel(Icons.of("download", 16, Theme.PURPLE));
            dl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            dl.setToolTipText("Export CSV");
            dl.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { exportReport(o.report, o.period); }
            });
            row.add(dl, BorderLayout.EAST);
            recentList.add(row);
        }
        revalidate();
        repaint();
    }

    private JComponent headerRow() {
        JPanel h = new JPanel(new Theme.RowLayout(8, COLS)) {
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setColor(new Color(0x211C39));
                g.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g.dispose();
            }
        };
        h.setOpaque(false);
        h.setBorder(new EmptyBorder(0, 12, 0, 8));
        for (String s : new String[]{"Report Title", "Type", "Period Covered", "School Year", "Status", "Actions"})
            h.add(Theme.label(s, Font.BOLD, 11.5f, Theme.MUTED));
        h.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        h.setPreferredSize(new Dimension(100, 38));
        return h;
    }

    private static JComponent leftWrap(JComponent c) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);
        p.add(c);
        return p;
    }

    private JComponent reportRow(ReportCatalog.Report r) {
        JPanel row = new JPanel(new Theme.RowLayout(8, COLS));
        row.setOpaque(false);
        row.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER), new EmptyBorder(0, 12, 0, 8)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        row.setPreferredSize(new Dimension(100, 56));

        JPanel title = new JPanel(new BorderLayout(8, 0));
        title.setOpaque(false);
        title.add(new JLabel(Icons.of("doc", 20, Theme.PURPLE)), BorderLayout.WEST);
        JLabel tl = Theme.label(r.title, Font.BOLD, 12.5f, Theme.TEXT);
        tl.setToolTipText(r.title);
        title.add(tl, BorderLayout.CENTER);
        row.add(title);
        row.add(centerV(leftWrap(new Theme.Pill(r.type))));
        row.add(Theme.label(period.shortLabel(), Font.PLAIN, 11.5f, Theme.MUTED));
        row.add(Theme.label(period.startYear + " – " + (period.startYear + 1), Font.PLAIN, 11.5f, Theme.MUTED));
        row.add(centerV(leftWrap(new Theme.Pill(r.status()))));

        JPanel act = new JPanel();
        act.setOpaque(false);
        act.setLayout(new BoxLayout(act, BoxLayout.X_AXIS));
        Theme.RoundButton view = new Theme.RoundButton("View Report", Theme.PURPLE_DARK, false);
        view.setFont(Theme.font(Font.BOLD, 12f));
        view.setBorder(new EmptyBorder(6, 12, 6, 12));
        view.addActionListener(e -> { ReportDialog.show(SwingUtilities.getWindowAncestor(this), catalog, r, period); rebuild(); });
        JLabel dots = new JLabel(Icons.of("dots", 18, Theme.MUTED));
        dots.setPreferredSize(new Dimension(22, 28));
        dots.setMaximumSize(new Dimension(22, 28));
        dots.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        dots.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { actionMenu(r).show(dots, 0, dots.getHeight()); }
        });
        act.add(Box.createHorizontalGlue());
        act.add(view);
        act.add(Box.createHorizontalStrut(8));
        act.add(dots);
        act.add(Box.createHorizontalGlue());
        row.add(act);
        return row;
    }

    private static JComponent centerV(JComponent c) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.add(c);
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.WEST;
        gc.weightx = 1;
        p.removeAll();
        p.add(c, gc);
        return p;
    }

    private JPopupMenu actionMenu(ReportCatalog.Report r) {
        JPopupMenu m = new JPopupMenu();
        JMenuItem open = new JMenuItem("View Report");
        open.addActionListener(e -> { ReportDialog.show(SwingUtilities.getWindowAncestor(this), catalog, r, period); rebuild(); });
        JMenuItem exp = new JMenuItem("Export CSV");
        exp.addActionListener(e -> exportReport(r, period));
        JMenuItem toggle = new JMenuItem(r.isPublished() ? "Mark as Pending" : "Mark as Published");
        toggle.addActionListener(e -> { catalog.setPublished(r, !r.isPublished()); rebuild(); shellRefresh(); });
        m.add(open); m.add(exp); m.addSeparator(); m.add(toggle);
        return m;
    }

    private void shellRefresh() { shell.showPage(DashboardFrame.REPORTS); }

    private void exportReport(ReportCatalog.Report r, SchoolPeriod p) {
        ReportDialog.export(this, catalog, catalog.build(r, p), r.id + "_" + p.startYear + "-" + (p.startYear + 1) + "_" + p.semesterLabel().replace(' ', '-'));
        rebuild();
    }

    private void buildPager(int pages, int from, int to, int total) {
        pager.removeAll();
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        btns.setOpaque(false);
        btns.add(new PageBtn("chevLeft", false, page > 0, () -> { page--; rebuild(); }));
        for (int i = 0; i < pages; i++) {
            final int idx = i;
            btns.add(new PageBtn(String.valueOf(i + 1), i == page, true, () -> { page = idx; rebuild(); }));
        }
        btns.add(new PageBtn("chevRight", false, page < pages - 1, () -> { page++; rebuild(); }));
        pager.add(btns, BorderLayout.WEST);
        String s = total == 0 ? "Showing 0 of 0 reports" : "Showing " + (from + 1) + "–" + to + " of " + total + " reports";
        JPanel r = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 12));
        r.setOpaque(false);
        r.add(Theme.label(s, Font.PLAIN, 12f, Theme.MUTED));
        pager.add(r, BorderLayout.EAST);
    }

    // -------------------------------------------------------------- components
    private static class Chip extends JComponent {
        private final String text, icon;
        private boolean selected;
        private final Theme.Anim hoverA = new Theme.Anim(this), pressA = new Theme.Anim(this);
        Chip(String text, String icon, Runnable action) {
            this.text = text; this.icon = icon;
            setFont(Theme.font(Font.BOLD, 12.5f));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { action.run(); }
                @Override public void mouseEntered(MouseEvent e) { hoverA.to(true); }
                @Override public void mouseExited(MouseEvent e) { hoverA.to(false); pressA.to(false); }
                @Override public void mousePressed(MouseEvent e) { pressA.to(true); }
                @Override public void mouseReleased(MouseEvent e) { pressA.to(false); }
            });
        }
        void setSelected(boolean s) { selected = s; repaint(); }
        @Override public Dimension getPreferredSize() { return new Dimension(getFontMetrics(getFont()).stringWidth(text) + 62, 36); }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Theme.antialias(g);
            float h = hoverA.get(), p = pressA.get();
            RoundRectangle2D shape = new RoundRectangle2D.Float(3, 3, getWidth() - 7, getHeight() - 7, 14, 14);
            if (selected) {
                Theme.glow(g, shape, Theme.PURPLE_LIGHT, 0.7f + 0.3f * Math.max(h, p), 3);
                g.setPaint(new GradientPaint(0, 0, Theme.PURPLE_DARK, getWidth(), 0, Theme.PURPLE_LIGHT));
                g.fill(shape);
            } else if (h > 0.01f) {
                Theme.glow(g, shape, Theme.PURPLE_LIGHT, h * 0.35f + p * 0.5f, 3);
                g.setColor(new Color(180, 160, 255, Math.round(22 + 35 * h)));
                g.fill(shape);
            }
            Color fg = selected ? Color.WHITE : Theme.mix(Theme.MUTED, Theme.TEXT, h);
            Icons.paint(g, icon, 14, (getHeight() - 18) / 2.0, 18, fg);
            g.setFont(getFont());
            g.setColor(fg);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(text, 40, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            g.dispose();
        }
    }

    private static class PageBtn extends JComponent {
        private final String label;
        private final boolean selected, enabled;
        private final Theme.Anim hoverA = new Theme.Anim(this);
        PageBtn(String label, boolean selected, boolean enabled, Runnable action) {
            this.label = label; this.selected = selected; this.enabled = enabled;
            setPreferredSize(new Dimension(32, 32));
            if (enabled) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseClicked(MouseEvent e) { action.run(); }
                    @Override public void mouseEntered(MouseEvent e) { hoverA.to(true); }
                    @Override public void mouseExited(MouseEvent e) { hoverA.to(false); }
                });
            }
        }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Theme.antialias(g);
            float h = hoverA.get();
            Shape s = new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 8, 8);
            if (enabled) Theme.glow(g, (RectangularShape) s, Theme.PURPLE_LIGHT, selected ? 0.6f : h * 0.5f, 2);
            g.setColor(selected ? Theme.PURPLE : Theme.mix(new Color(0x1B172F), Theme.LAVENDER, h));
            g.fill(s);
            g.setColor(selected ? Theme.PURPLE : Theme.mix(Theme.BORDER, Theme.PURPLE_LIGHT, h));
            g.draw(s);
            Color fg = selected ? Color.WHITE : enabled ? Theme.TEXT : Theme.MUTED;
            if (label.startsWith("chev")) Icons.paint(g, label, 9, 9, 14, fg);
            else {
                g.setFont(Theme.font(Font.BOLD, 12f));
                g.setColor(fg);
                FontMetrics fm = g.getFontMetrics();
                g.drawString(label, (getWidth() - fm.stringWidth(label)) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
            g.dispose();
        }
    }
}

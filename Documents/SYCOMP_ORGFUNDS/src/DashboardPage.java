import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.IntConsumer;

/** Welcome banner, financial overview cards, monthly chart, recent activity and latest reports. */
public class DashboardPage extends Theme.ScrollPanel {
    private final DashboardFrame shell;
    private final FinanceManager manager;
    private final ReportCatalog catalog;

    private boolean semesterMode = true;
    private final StatCard collections = new StatCard("Total Collections", "coins");
    private final StatCard expenses = new StatCard("Total Expenses", "wallet");
    private final StatCard balance = new StatCard("Remaining Balance", "folder");
    private final StatCard activities = new StatCard("Total Activities", "people");
    private final BarChart chart = new BarChart();
    private final JLabel overviewSub = Theme.label(" ", Font.PLAIN, 13.5f, Theme.MUTED);
    private final JLabel chartSub = Theme.label(" ", Font.PLAIN, 12.5f, Theme.MUTED);
    private final JPanel recent = new JPanel();
    private final JPanel reportRow = new JPanel(new Theme.RowLayout(14, 1, 1, 1));

    public DashboardPage(DashboardFrame shell, FinanceManager manager, ReportCatalog catalog) {
        this.shell = shell;
        this.manager = manager;
        this.catalog = catalog;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(16, 24, 24, 24));

        add(Theme.fixedHeight(new Art.Banner("dashboard", null, "Welcome, Student!", null,
                "Stay informed and be part of a transparent and accountable organization. Explore our financial reports and see how your organization's funds are used.",
                null, null), 150));
        add(Box.createVerticalStrut(14));

        // overview header
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JPanel ht = new JPanel();
        ht.setOpaque(false);
        ht.setLayout(new BoxLayout(ht, BoxLayout.Y_AXIS));
        ht.add(Theme.label("Financial Overview", Font.BOLD, 22f, Theme.PURPLE_DARK));
        ht.add(Box.createVerticalStrut(2));
        ht.add(overviewSub);
        head.add(ht, BorderLayout.WEST);
        Segmented seg = new Segmented(new String[]{"Current Semester", "All Records"}, i -> { semesterMode = i == 0; refresh(); });
        JPanel segWrap = new JPanel(new GridBagLayout());
        segWrap.setOpaque(false);
        segWrap.add(seg);
        head.add(segWrap, BorderLayout.EAST);
        add(Theme.fixedHeight(head, 58));
        add(Box.createVerticalStrut(8));

        add(Theme.fixedHeight(Theme.row(14, new double[]{1, 1, 1, 1}, collections, expenses, balance, activities), 126));
        add(Box.createVerticalStrut(14));

        // chart + recent
        Theme.Card chartCard = new Theme.Card(new BorderLayout(0, 4));
        JPanel ct = new JPanel();
        ct.setOpaque(false);
        ct.setLayout(new BoxLayout(ct, BoxLayout.Y_AXIS));
        ct.add(Theme.label("Collections vs. Expenses", Font.BOLD, 17f, Theme.TEXT));
        ct.add(Box.createVerticalStrut(2));
        ct.add(chartSub);
        JPanel chartHead = new JPanel(new BorderLayout());
        chartHead.setOpaque(false);
        chartHead.add(ct, BorderLayout.WEST);
        chartHead.add(legend(), BorderLayout.EAST);
        chartCard.add(chartHead, BorderLayout.NORTH);
        chartCard.add(chart, BorderLayout.CENTER);

        Theme.Card recentCard = new Theme.Card(new BorderLayout(0, 6));
        JPanel rh = new JPanel(new BorderLayout());
        rh.setOpaque(false);
        rh.add(Theme.cardTitle("calendar", "Recent Activities", null), BorderLayout.WEST);
        rh.add(Theme.link("View All", () -> shell.showPage(DashboardFrame.TRANSACTIONS)), BorderLayout.EAST);
        recentCard.add(rh, BorderLayout.NORTH);
        recent.setOpaque(false);
        recent.setLayout(new BoxLayout(recent, BoxLayout.Y_AXIS));
        recentCard.add(recent, BorderLayout.CENTER);

        add(Theme.fixedHeight(Theme.row(14, new double[]{1.55, 1}, chartCard, recentCard), 318));
        add(Box.createVerticalStrut(14));

        // latest reports
        Theme.Card reportsCard = new Theme.Card(new BorderLayout(0, 10));
        JPanel rp = new JPanel(new BorderLayout());
        rp.setOpaque(false);
        rp.add(Theme.cardTitle("doc", "Latest Published Reports", "View the most recently published financial reports."), BorderLayout.WEST);
        JPanel all = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 8));
        all.setOpaque(false);
        all.add(Theme.link("View All Reports", () -> shell.showPage(DashboardFrame.REPORTS)));
        all.add(new JLabel(Icons.of("arrowRight", 14, Theme.PURPLE)));
        rp.add(all, BorderLayout.EAST);
        reportsCard.add(rp, BorderLayout.NORTH);
        reportRow.setOpaque(false);
        reportsCard.add(reportRow, BorderLayout.CENTER);
        add(Theme.fixedHeight(reportsCard, 214));

        manager.addChangeListener(this::refresh);
        refresh();
    }

    private JComponent legend() {
        JComponent l = new JComponent() {
            { setPreferredSize(new Dimension(190, 24)); }
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                Theme.antialias(g);
                g.setFont(Theme.font(Font.PLAIN, 12f));
                g.setColor(BarChart.C_INC); g.fill(new Ellipse2D.Double(8, 8, 9, 9));
                g.setColor(Theme.MUTED); g.drawString("Collections", 22, 17);
                g.setColor(BarChart.C_EXP); g.fill(new Ellipse2D.Double(104, 8, 9, 9));
                g.setColor(Theme.MUTED); g.drawString("Expenses", 118, 17);
                g.dispose();
            }
        };
        return l;
    }

    // ----------------------------------------------------------------- refresh
    public void refresh() {
        SchoolPeriod cur = SchoolPeriod.current();
        List<FinancialTransaction> list = semesterMode ? manager.inPeriod(cur) : manager.getTransactions();
        BigDecimal inc = FinanceManager.incomeOf(list), exp = FinanceManager.expensesOf(list), bal = inc.subtract(exp);

        overviewSub.setText(semesterMode
                ? "A quick look at the organization's financial status for the current semester (" + cur.semesterLabel() + ", SY " + cur.schoolYearLabel() + ")."
                : "A quick look at the organization's financial status across all recorded transactions.");
        chartSub.setText(semesterMode ? "Monthly comparison for the current semester." : "Monthly comparison for the last six months.");

        List<FinancialTransaction> prev = semesterMode ? manager.inPeriod(cur.previousRegular()) : null;
        BigDecimal pInc = prev == null ? null : FinanceManager.incomeOf(prev);
        BigDecimal pExp = prev == null ? null : FinanceManager.expensesOf(prev);
        collections.set(Theme.peso(inc), delta(inc, pInc, true), semesterMode ? "vs. last semester" : "all records");
        expenses.set(Theme.peso(exp), delta(exp, pExp, false), semesterMode ? "vs. last semester" : "all records");
        balance.set(Theme.peso(bal), delta(bal, pInc == null ? null : pInc.subtract(pExp), true), semesterMode ? "vs. last semester" : "income − expenses");
        balance.valueColor(bal.signum() < 0 ? Theme.RED : Theme.TEXT);
        activities.set(String.valueOf(list.size()), null, semesterMode ? "for this semester" : "recorded so far");

        // chart
        List<YearMonth> months = new ArrayList<>();
        if (semesterMode) months.addAll(cur.months());
        else for (int i = 5; i >= 0; i--) months.add(YearMonth.now().minusMonths(i));
        List<String> labels = new ArrayList<>();
        List<BigDecimal> ci = new ArrayList<>(), ce = new ArrayList<>();
        for (YearMonth ym : months) {
            BigDecimal a = BigDecimal.ZERO, b = BigDecimal.ZERO;
            for (FinancialTransaction t : manager.getTransactions()) {
                if (!YearMonth.from(t.getDate()).equals(ym)) continue;
                if (t instanceof Income) a = a.add(t.getAmount()); else b = b.add(t.getAmount());
            }
            labels.add(ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
            ci.add(a); ce.add(b);
        }
        chart.set(labels, ci, ce);

        // recent activity
        recent.removeAll();
        List<FinancialTransaction> latest = manager.newestFirst(manager.getTransactions());
        if (latest.isEmpty()) {
            JLabel e = Theme.label("No transactions yet – add your first one under Transactions.", Font.PLAIN, 13f, Theme.MUTED);
            e.setBorder(new EmptyBorder(18, 4, 0, 0));
            recent.add(e);
        }
        for (int i = 0; i < Math.min(4, latest.size()); i++) recent.add(activityRow(latest.get(i)));

        // report cards
        reportRow.removeAll();
        List<ReportCatalog.Report> pub = new ArrayList<>();
        for (ReportCatalog.Report r : catalog.all()) if (r.isPublished() && pub.size() < 3) pub.add(r);
        Color[][] tones = {{new Color(0x2FBF8A), new Color(0x138A5E)}, {Theme.PURPLE_LIGHT, Theme.PURPLE_DARK}, {new Color(0x5BA0FF), new Color(0x2F63D4)}};
        for (int i = 0; i < pub.size(); i++) reportRow.add(reportCard(pub.get(i), cur, tones[i % 3]));
        for (int i = pub.size(); i < 3; i++) reportRow.add(new JPanel() {{ setOpaque(false); }});

        revalidate();
        repaint();
    }

    private static Delta delta(BigDecimal cur, BigDecimal prev, boolean upIsGood) {
        if (prev == null || prev.signum() == 0) return null;
        BigDecimal pct = cur.subtract(prev).multiply(BigDecimal.valueOf(100)).divide(prev.abs(), 0, RoundingMode.HALF_UP);
        boolean up = pct.signum() >= 0;
        return new Delta(Math.abs(pct.intValue()) + "%", up, up == upIsGood);
    }

    private record Delta(String text, boolean up, boolean good) {}

    private JComponent activityRow(FinancialTransaction t) {
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setOpaque(false);
        row.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, new Color(0xF0EDFA)), new EmptyBorder(6, 2, 6, 2)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        row.setPreferredSize(new Dimension(100, 62));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JComponent date = new JComponent() {
            { setPreferredSize(new Dimension(46, 46)); }
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                Theme.antialias(g);
                g.setColor(Theme.LAVENDER);
                g.fill(new RoundRectangle2D.Float(0, 0, 46, 46, 12, 12));
                g.setFont(Theme.font(Font.BOLD, 10.5f));
                g.setColor(Theme.PURPLE_DARK);
                String m = t.getDate().getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase();
                g.drawString(m, 23 - g.getFontMetrics().stringWidth(m) / 2, 17);
                g.setFont(Theme.font(Font.BOLD, 17f));
                g.setColor(Theme.TEXT);
                String d = String.format("%02d", t.getDate().getDayOfMonth());
                g.drawString(d, 23 - g.getFontMetrics().stringWidth(d) / 2, 36);
                g.dispose();
            }
        };
        JPanel text = new JPanel(new GridLayout(2, 1, 0, 1));
        text.setOpaque(false);
        text.add(Theme.label(t.getDescription(), Font.BOLD, 13.5f, Theme.TEXT));
        text.add(Theme.label(t.getCategory() + "  •  " + (t instanceof Income ? "+" : "−") + Theme.peso(t.getAmount()), Font.PLAIN, 12f, Theme.MUTED));
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 11));
        right.setOpaque(false);
        right.add(new Theme.Pill(t.getType()));
        right.add(new JLabel(Icons.of("chevRight", 14, Theme.MUTED)));
        row.add(date, BorderLayout.WEST);
        row.add(text, BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);
        row.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { shell.openTransaction(t.getId()); }
        });
        return row;
    }

    private JComponent reportCard(ReportCatalog.Report r, SchoolPeriod p, Color[] tone) {
        JPanel c = new JPanel(new BorderLayout(14, 0)) {
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                Theme.antialias(g);
                Shape s = new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 14, 14);
                g.setColor(new Color(0xFBFAFF));
                g.fill(s);
                g.setColor(Theme.BORDER);
                g.draw(s);
                g.dispose();
            }
        };
        c.setOpaque(false);
        c.setBorder(new EmptyBorder(14, 16, 12, 16));
        JPanel iconWrap = new JPanel(new BorderLayout());
        iconWrap.setOpaque(false);
        iconWrap.add(Theme.iconCircle("doc", 50, tone[0], tone[1], Color.WHITE), BorderLayout.NORTH);
        c.add(iconWrap, BorderLayout.WEST);

        JPanel mid = new JPanel(new BorderLayout(0, 4));
        mid.setOpaque(false);
        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(Theme.label(r.title.length() > 34 ? r.title.substring(0, 32) + "…" : r.title, Font.BOLD, 14f, Theme.TEXT));
        JTextArea d = new JTextArea(r.description);
        d.setLineWrap(true); d.setWrapStyleWord(true); d.setEditable(false); d.setOpaque(false); d.setFocusable(false);
        d.setFont(Theme.font(Font.PLAIN, 11.5f)); d.setForeground(Theme.MUTED); d.setBorder(new EmptyBorder(3, 0, 0, 0));
        d.setAlignmentX(0f);
        texts.add(d);
        mid.add(texts, BorderLayout.CENTER);

        JPanel foot = new JPanel(new BorderLayout());
        foot.setOpaque(false);
        foot.setBorder(new MatteBorder(1, 0, 0, 0, new Color(0xEDE9FA)));
        JPanel when = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 8));
        when.setOpaque(false);
        when.add(new JLabel(Icons.of("calendar", 14, Theme.MUTED)));
        when.add(Theme.label(p.coveredLabel(), Font.PLAIN, 11.5f, Theme.MUTED));
        foot.add(when, BorderLayout.WEST);
        Theme.RoundButton view = new Theme.RoundButton("View Report", Theme.PURPLE_DARK, false);
        view.setFont(Theme.font(Font.BOLD, 12f));
        view.setBorder(new EmptyBorder(6, 16, 6, 16));
        view.addActionListener(e -> ReportDialog.show(SwingUtilities.getWindowAncestor(this), catalog, r, p));
        JPanel vb = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        vb.setOpaque(false);
        vb.add(view);
        foot.add(vb, BorderLayout.EAST);
        mid.add(foot, BorderLayout.SOUTH);
        c.add(mid, BorderLayout.CENTER);
        return c;
    }

    // -------------------------------------------------------------- components
    private static class StatCard extends Theme.Card {
        private final JLabel value = Theme.label("-", Font.BOLD, 25f, Theme.TEXT);
        private final JLabel deltaLabel = new JLabel(" ");
        private final JLabel sub = Theme.label(" ", Font.PLAIN, 12f, Theme.MUTED);

        StatCard(String title, String icon) {
            super(new BorderLayout(14, 0));
            tint(new Color(0xF3F0FF), new Color(0xFBFAFF));
            if (icon.equals("wallet")) tint(new Color(0xFBF0FB), new Color(0xFFFAFF));
            if (icon.equals("folder")) tint(new Color(0xEAF4FF), new Color(0xF8FBFF));
            setBorder(new EmptyBorder(16, 16, 18, 14));
            JPanel iw = new JPanel(new BorderLayout());
            iw.setOpaque(false);
            iw.add(Theme.iconCircle(icon, 52), BorderLayout.NORTH);
            JPanel col = new JPanel();
            col.setOpaque(false);
            col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
            col.add(Theme.label(title, Font.BOLD, 13.5f, Theme.TEXT));
            col.add(Box.createVerticalStrut(2));
            col.add(value);
            col.add(Box.createVerticalStrut(1));
            deltaLabel.setFont(Theme.font(Font.BOLD, 12f));
            col.add(deltaLabel);
            col.add(sub);
            add(iw, BorderLayout.WEST);
            add(col, BorderLayout.CENTER);
        }

        void set(String v, Delta d, String subText) {
            value.setText(v);
            if (d == null) { deltaLabel.setText(" "); deltaLabel.setIcon(null); }
            else {
                Color c = d.good() ? Theme.GREEN : Theme.RED;
                deltaLabel.setText(" " + d.text());
                deltaLabel.setForeground(c);
                deltaLabel.setIcon(Icons.of(d.up() ? "arrowUp" : "arrowDown", 12, c));
            }
            sub.setText(subText);
        }

        void valueColor(Color c) { value.setForeground(c); }
    }

    /** Two-option pill switch. */
    private static class Segmented extends JComponent {
        private final String[] labels;
        private int sel = 0;
        Segmented(String[] labels, IntConsumer cb) {
            this.labels = labels;
            setPreferredSize(new Dimension(250, 38));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    int i = e.getX() < getWidth() / 2 ? 0 : 1;
                    if (i != sel) { sel = i; repaint(); cb.accept(i); }
                }
            });
        }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Theme.antialias(g);
            int w = getWidth(), h = getHeight();
            g.setColor(Theme.LAVENDER);
            g.fillRoundRect(0, 0, w, h, h, h);
            g.setPaint(new GradientPaint(0, 0, Theme.PURPLE, w, 0, Theme.PURPLE_LIGHT));
            g.fillRoundRect(sel * w / 2 + 3, 3, w / 2 - 6, h - 6, h, h);
            g.setFont(Theme.font(Font.BOLD, 12.5f));
            FontMetrics fm = g.getFontMetrics();
            for (int i = 0; i < 2; i++) {
                g.setColor(i == sel ? Color.WHITE : Theme.PURPLE_DARK);
                g.drawString(labels[i], i * w / 2 + (w / 2 - fm.stringWidth(labels[i])) / 2, (h + fm.getAscent() - fm.getDescent()) / 2);
            }
            g.dispose();
        }
    }

    /** Grouped bar chart (collections vs expenses per month). */
    static class BarChart extends JComponent {
        static final Color C_INC = new Color(0x5B35C9), C_EXP = new Color(0xA58AF0);
        private List<String> labels = new ArrayList<>();
        private List<BigDecimal> inc = new ArrayList<>(), exp = new ArrayList<>();
        private final List<Rectangle2D> hitBoxes = new ArrayList<>();
        private final List<String> hitText = new ArrayList<>();

        BarChart() { setToolTipText(""); }

        void set(List<String> l, List<BigDecimal> i, List<BigDecimal> e) { labels = l; inc = i; exp = e; repaint(); }

        @Override public String getToolTipText(MouseEvent e) {
            for (int i = 0; i < hitBoxes.size(); i++) if (hitBoxes.get(i).contains(e.getPoint())) return hitText.get(i);
            return null;
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Theme.antialias(g);
            hitBoxes.clear();
            hitText.clear();
            int w = getWidth(), h = getHeight();
            int left = 74, bottom = 28, top = 14, right = 8;
            double max = 0;
            for (BigDecimal b : inc) max = Math.max(max, b.doubleValue());
            for (BigDecimal b : exp) max = Math.max(max, b.doubleValue());
            double step = niceStep(max);
            int ticks = Math.max(1, (int) Math.ceil(max / step));
            double top$ = step * ticks;
            double plotH = h - top - bottom, plotW = w - left - right;
            g.setFont(Theme.font(Font.PLAIN, 11.5f));
            FontMetrics fm = g.getFontMetrics();
            for (int i = 0; i <= ticks; i++) {
                double y = top + plotH - plotH * i / ticks;
                g.setColor(new Color(0xECE8FA));
                g.draw(new Line2D.Double(left, y, w - right, y));
                g.setColor(Theme.MUTED);
                String s = Theme.pesoShort(BigDecimal.valueOf(step * i));
                g.drawString(s, left - 10 - fm.stringWidth(s), (float) (y + 4));
            }
            int n = Math.max(1, labels.size());
            double groupW = plotW / n;
            double barW = Math.min(30, groupW * 0.28);
            for (int i = 0; i < labels.size(); i++) {
                double gx = left + groupW * i + groupW / 2;
                drawBar(g, gx - barW - 2, barW, inc.get(i).doubleValue(), top$, top, plotH, C_INC, labels.get(i) + " – Collections: " + Theme.peso(inc.get(i)));
                drawBar(g, gx + 2, barW, exp.get(i).doubleValue(), top$, top, plotH, C_EXP, labels.get(i) + " – Expenses: " + Theme.peso(exp.get(i)));
                g.setColor(Theme.MUTED);
                g.drawString(labels.get(i), (float) (gx - fm.stringWidth(labels.get(i)) / 2.0), h - 8);
            }
            g.dispose();
        }

        private void drawBar(Graphics2D g, double x, double bw, double v, double max, int top, double plotH, Color c, String tip) {
            double bh = max <= 0 ? 0 : plotH * v / max;
            double y = top + plotH - bh;
            if (bh > 0) {
                g.setColor(c);
                g.fill(new RoundRectangle2D.Double(x, y, bw, bh + 4, 5, 5));
                g.setColor(Color.WHITE);
                g.fill(new Rectangle2D.Double(x - 1, top + plotH, bw + 2, 6));
            }
            hitBoxes.add(new Rectangle2D.Double(x, top, bw, plotH));
            hitText.add(tip);
        }

        private static double niceStep(double max) {
            return max <= 0 ? 1000 : niceNumber(max / 4);
        }

        private static double niceNumber(double x) {
            double e = Math.pow(10, Math.floor(Math.log10(x)));
            double f = x / e;
            double nf = f <= 1 ? 1 : f <= 2 ? 2 : f <= 5 ? 5 : 10;
            return nf * e;
        }
    }
}

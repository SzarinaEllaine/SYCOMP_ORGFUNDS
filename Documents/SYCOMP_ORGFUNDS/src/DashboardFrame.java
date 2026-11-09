import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Main window: purple sidebar + top bar + the five pages (Dashboard, Transactions, Reports, About, Contact). */
public class DashboardFrame extends JFrame {
    public static final String DASHBOARD = "dashboard", TRANSACTIONS = "transactions", REPORTS = "reports",
            ABOUT = "about", CONTACT = "contact";

    private final FinanceManager manager;
    private final ReportCatalog catalog;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final Map<String, NavItem> nav = new LinkedHashMap<>();
    private final Map<String, JScrollPane> scrollers = new LinkedHashMap<>();
    private final TopBar topBar;

    private final DashboardPage dashboardPage;
    private final TransactionsPage transactionsPage;
    private final ReportsPage reportsPage;
    private final AboutPage aboutPage;
    private final ContactPage contactPage;
    private String current = DASHBOARD;

    public DashboardFrame(FinanceManager manager, ReportCatalog catalog) {
        super("SYCOMP – Financial Management System");
        this.manager = manager;
        this.catalog = catalog;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Image icon = Art.iconImage();
        if (icon != null) setIconImage(icon);
        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setSize(Math.min(1440, screen.width), Math.min(900, screen.height));
        setMinimumSize(new Dimension(Math.min(1180, screen.width), Math.min(700, screen.height)));
        setLocationRelativeTo(null);

        dashboardPage = new DashboardPage(this, manager, catalog);
        transactionsPage = new TransactionsPage(this, manager);
        reportsPage = new ReportsPage(this, manager, catalog);
        aboutPage = new AboutPage(this);
        contactPage = new ContactPage(this);

        content.setBackground(Theme.BG);
        content.add(wrap(DASHBOARD, dashboardPage), DASHBOARD);
        content.add(wrap(TRANSACTIONS, transactionsPage), TRANSACTIONS);
        content.add(wrap(REPORTS, reportsPage), REPORTS);
        content.add(wrap(ABOUT, aboutPage), ABOUT);
        content.add(wrap(CONTACT, contactPage), CONTACT);

        topBar = new TopBar();
        JPanel right = new JPanel(new BorderLayout());
        right.add(topBar, BorderLayout.NORTH);
        right.add(content, BorderLayout.CENTER);

        JPanel root = new JPanel(new BorderLayout());
        root.add(new Sidebar(), BorderLayout.WEST);
        root.add(right, BorderLayout.CENTER);
        setContentPane(root);

        manager.addChangeListener(topBar::notifyChange);
        showPage(DASHBOARD);
    }

    private JScrollPane wrap(String key, JComponent page) {
        JScrollPane sp = new JScrollPane(page);
        sp.setBorder(null);
        sp.getViewport().setBackground(Theme.BG);
        Theme.slimScroll(sp);
        scrollers.put(key, sp);
        return sp;
    }

    // -------------------------------------------------------------- navigation
    public void showPage(String key) {
        current = key;
        cards.show(content, key);
        for (Map.Entry<String, NavItem> e : nav.entrySet()) e.getValue().repaint();
        topBar.showTitle(DASHBOARD.equals(key));
        SwingUtilities.invokeLater(() -> scrollers.get(key).getViewport().setViewPosition(new Point(0, 0)));
        if (DASHBOARD.equals(key)) dashboardPage.refresh();
    }

    public void openTransaction(String id) { showPage(TRANSACTIONS); transactionsPage.focusTransaction(id); }
    public void openReports(String search) { showPage(REPORTS); reportsPage.applySearch(search); }
    public void openContact(String subject) { showPage(CONTACT); contactPage.setSubject(subject); }

    /** Top-bar search: reports first, then transactions. */
    public void globalSearch(String text) {
        String q = text.trim();
        if (q.isEmpty()) return;
        boolean reportHit = false;
        for (ReportCatalog.Report r : catalog.all())
            if (r.title.toLowerCase().contains(q.toLowerCase()) || r.type.toLowerCase().contains(q.toLowerCase()) || r.id.equalsIgnoreCase(q)) reportHit = true;
        if (reportHit || manager.searchTransaction(q).isEmpty()) openReports(q);
        else { showPage(TRANSACTIONS); transactionsPage.applySearch(q); }
    }

    // ----------------------------------------------------------------- sidebar
    private class Sidebar extends JPanel {
        Sidebar() {
            setPreferredSize(new Dimension(270, 100));
            setLayout(new BorderLayout());
            setOpaque(false);
            JPanel list = new JPanel();
            list.setOpaque(false);
            list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
            list.setBorder(new EmptyBorder(6, 14, 0, 14));
            addNav(list, DASHBOARD, "Dashboard", "home");
            addNav(list, TRANSACTIONS, "Transactions", "coins");
            addNav(list, REPORTS, "Reports", "doc");
            addNav(list, ABOUT, "About the Organization", "people");
            addNav(list, CONTACT, "Contact Us", "mail");
            JComponent brand = new JComponent() {
                { setPreferredSize(new Dimension(270, 118)); }
                @Override protected void paintComponent(Graphics g0) {
                    Graphics2D g = (Graphics2D) g0.create();
                    Theme.antialias(g);
                    Art.logo(g, 58, 62, 80);
                    g.setColor(Color.WHITE);
                    g.setFont(Theme.font(Font.BOLD, 25f));
                    g.drawString("SYCOMP", 110, 56);
                    g.setFont(Theme.font(Font.PLAIN, 11.5f));
                    g.setColor(new Color(0xD8CFFA));
                    g.drawString("Financial Management", 110, 74);
                    g.drawString("System", 110, 89);
                    g.dispose();
                }
            };
            add(brand, BorderLayout.NORTH);
            add(list, BorderLayout.CENTER);
        }

        private void addNav(JPanel list, String key, String text, String icon) {
            NavItem n = new NavItem(key, text, icon);
            nav.put(key, n);
            list.add(n);
            list.add(Box.createVerticalStrut(6));
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Art.sidebarBackground(g, getWidth(), getHeight());
            g.setColor(new Color(0xA68BFF));
            g.setFont(Theme.font(Font.BOLD, 17f));
            int y = getHeight() - 175;
            g.drawString("Empowering", 30, y);
            g.drawString("Future Tech Leaders", 30, y + 23);
            g.setFont(Theme.font(Font.PLAIN, 13f));
            g.setColor(new Color(0xD8CFFA));
            g.drawString("Through Knowledge,", 30, y + 50);
            g.drawString("Community, and Innovation.", 30, y + 69);
            g.dispose();
        }
    }

    private class NavItem extends JComponent {
        private final String key, text, icon;
        private boolean hover;
        NavItem(String key, String text, String icon) {
            this.key = key; this.text = text; this.icon = icon;
            setPreferredSize(new Dimension(100, 52));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
            setAlignmentX(LEFT_ALIGNMENT);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                @Override public void mouseClicked(MouseEvent e) { showPage(key); }
            });
        }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Theme.antialias(g);
            boolean active = key.equals(current);
            RoundRectangle2D shape = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight() - 1, 14, 14);
            if (active) {
                g.setPaint(new GradientPaint(0, 0, new Color(0x4A2FC9), getWidth(), 0, new Color(0x7B57F2)));
                g.fill(shape);
                g.setColor(new Color(0xB59CFF));
                g.fill(new RoundRectangle2D.Float(0, 12, 4, getHeight() - 25, 4, 4));
            } else if (hover) {
                g.setColor(new Color(255, 255, 255, 18));
                g.fill(shape);
            }
            Icons.paint(g, icon, 18, (getHeight() - 24) / 2.0, 24, Color.WHITE);
            g.setFont(Theme.font(active ? Font.BOLD : Font.PLAIN, 15f));
            g.setColor(Color.WHITE);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(text, 58, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            g.dispose();
        }
    }

    // ------------------------------------------------------------------ top bar
    private class TopBar extends JPanel {
        private final JPanel title = new JPanel();
        private final SearchField search = new SearchField();
        private boolean unread = true;

        TopBar() {
            super(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER), new EmptyBorder(0, 28, 0, 24)));
            setPreferredSize(new Dimension(100, 74));

            title.setOpaque(false);
            title.setLayout(new BoxLayout(title, BoxLayout.Y_AXIS));
            title.add(Box.createVerticalGlue());
            title.add(Theme.label("SYCOMP", Font.BOLD, 22f, Theme.PURPLE_DARK));
            title.add(Theme.label("Financial Management System", Font.PLAIN, 13.5f, Theme.TEXT));
            title.add(Box.createVerticalGlue());
            add(title, BorderLayout.WEST);

            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 0));
            right.setOpaque(false);
            search.setPreferredSize(new Dimension(330, 42));
            search.addActionListener(e -> globalSearch(search.getText()));
            right.add(search);
            right.add(bell());
            right.add(userChip());
            JPanel wrapR = new JPanel(new GridBagLayout());
            wrapR.setOpaque(false);
            wrapR.add(right);
            add(wrapR, BorderLayout.EAST);
        }

        void showTitle(boolean on) { title.setVisible(on); }
        void notifyChange() { unread = true; repaint(); }

        private JComponent bell() {
            JComponent b = new JComponent() {
                { setPreferredSize(new Dimension(40, 42)); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
                @Override protected void paintComponent(Graphics g0) {
                    Graphics2D g = (Graphics2D) g0.create();
                    Theme.antialias(g);
                    Icons.paint(g, "bell", 8, 9, 24, Theme.TEXT);
                    if (unread && manager.getCount() > 0) {
                        g.setColor(Color.WHITE);
                        g.fill(new Ellipse2D.Double(24, 5, 11, 11));
                        g.setColor(new Color(0xFF4D6D));
                        g.fill(new Ellipse2D.Double(25.5, 6.5, 8, 8));
                    }
                    g.dispose();
                }
            };
            b.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { unread = false; b.repaint(); showNotifications(b); }
            });
            return b;
        }

        private void showNotifications(Component anchor) {
            JPopupMenu m = new JPopupMenu();
            m.setBorder(new LineBorder(Theme.BORDER));
            JPanel p = new JPanel();
            p.setBackground(Color.WHITE);
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBorder(new EmptyBorder(12, 14, 12, 14));
            p.add(Theme.label("Recent activity", Font.BOLD, 14f, Theme.TEXT));
            p.add(Box.createVerticalStrut(8));
            List<FinancialTransaction> recent = manager.newestFirst(manager.getTransactions());
            if (recent.isEmpty()) p.add(Theme.label("No notifications yet.", Font.PLAIN, 13f, Theme.MUTED));
            DateTimeFormatter f = DateTimeFormatter.ofPattern("MMM d");
            for (int i = 0; i < Math.min(5, recent.size()); i++) {
                FinancialTransaction t = recent.get(i);
                boolean inc = t instanceof Income;
                JLabel l = Theme.label("<html><b>" + (inc ? "+" : "−") + Theme.peso(t.getAmount()) + "</b> – "
                        + t.getDescription() + " <span style='color:#6E6A8C'>(" + t.getDate().format(f) + ")</span></html>", Font.PLAIN, 12.5f, inc ? Theme.GREEN : Theme.RED);
                l.setBorder(new EmptyBorder(4, 0, 4, 0));
                p.add(l);
            }
            m.add(p);
            m.show(anchor, -250, anchor.getHeight() + 6);
        }

        private JComponent userChip() {
            JComponent c = new JComponent() {
                { setPreferredSize(new Dimension(150, 44)); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
                @Override protected void paintComponent(Graphics g0) {
                    Graphics2D g = (Graphics2D) g0.create();
                    Theme.antialias(g);
                    g.setPaint(new GradientPaint(0, 0, Theme.PURPLE_LIGHT, 40, 40, Theme.PURPLE_DARK));
                    g.fill(new Ellipse2D.Double(0, 2, 40, 40));
                    Icons.paint(g, "user", 9, 11, 22, Color.WHITE);
                    g.setFont(Theme.font(Font.BOLD, 13.5f));
                    g.setColor(Theme.TEXT);
                    g.drawString("Guest", 50, 19);
                    g.setFont(Theme.font(Font.PLAIN, 12f));
                    g.setColor(Theme.MUTED);
                    g.drawString("Student", 50, 35);
                    Icons.paint(g, "chevDown", 124, 15, 14, Theme.MUTED);
                    g.dispose();
                }
            };
            c.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    JPopupMenu m = new JPopupMenu();
                    JMenuItem who = new JMenuItem("Viewing as Guest (Student)");
                    who.setEnabled(false);
                    m.add(who);
                    m.addSeparator();
                    JMenuItem about = new JMenuItem("About the Organization");
                    about.addActionListener(a -> showPage(ABOUT));
                    JMenuItem contact = new JMenuItem("Contact Us");
                    contact.addActionListener(a -> showPage(CONTACT));
                    JMenuItem exit = new JMenuItem("Exit");
                    exit.addActionListener(a -> System.exit(0));
                    m.add(about); m.add(contact); m.addSeparator(); m.add(exit);
                    m.show(c, 0, c.getHeight() + 4);
                }
            });
            return c;
        }
    }

    /** Rounded search box with a magnifier icon and placeholder text. */
    static class SearchField extends JTextField {
        SearchField() {
            setOpaque(false);
            setBorder(new EmptyBorder(0, 42, 0, 14));
            setFont(Theme.font(Font.PLAIN, 13f));
            setForeground(Theme.TEXT);
            setCaretColor(Theme.PURPLE);
        }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Theme.antialias(g);
            Shape s = new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
            g.setColor(Color.WHITE);
            g.fill(s);
            g.setColor(hasFocus() ? Theme.PURPLE_LIGHT : Theme.BORDER);
            g.setStroke(new BasicStroke(1.4f));
            g.draw(s);
            Icons.paint(g, "search", 15, (getHeight() - 19) / 2.0, 19, Theme.PURPLE);
            if (getText().isEmpty()) {
                g.setFont(getFont());
                g.setColor(new Color(0x9A96B5));
                g.drawString("Search reports or activities...", 42, (getHeight() + g.getFontMetrics().getAscent() - g.getFontMetrics().getDescent()) / 2);
            }
            g.dispose();
            super.paintComponent(g0);
        }
    }
}

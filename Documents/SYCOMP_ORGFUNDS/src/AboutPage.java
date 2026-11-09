import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

/** Organization profile: mission, vision, board, statistics and past administrators. */
public class AboutPage extends Theme.ScrollPanel {
    private final DashboardFrame shell;
    private final JPanel adminCards = new JPanel(new Theme.RowLayout(12, 1, 1, 1, 1, 1));
    private final JComboBox<String> yearBox = new JComboBox<>();
    private final ArrowBtn leftBtn, rightBtn;
    private int offset = 0;
    private int visible = 5;
    private List<String[]> shownAdmins = new ArrayList<>();

    public AboutPage(DashboardFrame shell) {
        this.shell = shell;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(16, 24, 24, 24));
        leftBtn = new ArrowBtn("chevLeft", () -> { offset = Math.max(0, offset - 1); buildAdmins(); });
        rightBtn = new ArrowBtn("chevRight", () -> { offset++; buildAdmins(); });

        add(Theme.fixedHeight(new Art.Banner("about", "About the Organization", "About " + OrgInfo.SHORT_NAME, OrgInfo.FULL_NAME,
                OrgInfo.INTRO, "“Learn   •   Build   •   Lead”", null), 196));
        add(Box.createVerticalStrut(14));
        add(Theme.fixedHeight(buildInfoRow(), 156));
        add(Box.createVerticalStrut(14));
        add(buildBoardCard());
        add(Box.createVerticalStrut(14));
        add(buildStatsCard());
        add(Box.createVerticalStrut(14));
        add(Theme.fixedHeight(buildAdminCard(), 300));
        add(Box.createVerticalStrut(14));
        add(Theme.fixedHeight(buildCta(), 98));
    }

    // ------------------------------------------------------------- info cards
    private JPanel buildInfoRow() {
        JPanel valuesBody = new JPanel();
        valuesBody.setOpaque(false);
        valuesBody.setLayout(new BoxLayout(valuesBody, BoxLayout.Y_AXIS));
        for (String v : OrgInfo.VALUES) valuesBody.add(Theme.label("•  " + v, Font.PLAIN, 13f, Theme.TEXT));
        return Theme.row(14, new double[]{1, 1, 1, 1.15},
                infoCard("target", "Our Mission", textArea(OrgInfo.MISSION, 13.5f)),
                infoCard("eye", "Our Vision", textArea(OrgInfo.VISION, 13.5f)),
                infoCard("gem", "Core Values", valuesBody),
                infoCard("building", "Organization Overview", textArea(OrgInfo.OVERVIEW, 12f)));
    }

    private static JTextArea textArea(String s, float size) {
        JTextArea d = new JTextArea(s);
        d.setLineWrap(true); d.setWrapStyleWord(true); d.setEditable(false); d.setOpaque(false); d.setFocusable(false);
        d.setFont(Theme.font(Font.PLAIN, size)); d.setForeground(Theme.MUTED);
        d.setBorder(new EmptyBorder(4, 0, 0, 0));
        return d;
    }

    private JPanel infoCard(String icon, String title, JComponent body) {
        Theme.Card c = new Theme.Card(new BorderLayout(14, 0));
        c.setBorder(new EmptyBorder(16, 16, 18, 14));
        JPanel iw = new JPanel(new BorderLayout());
        iw.setOpaque(false);
        iw.add(Theme.iconCircle(icon, 52), BorderLayout.NORTH);
        JPanel col = new JPanel(new BorderLayout(0, 4));
        col.setOpaque(false);
        col.add(Theme.label(title, Font.BOLD, 16f, Theme.TEXT), BorderLayout.NORTH);
        col.add(body, BorderLayout.CENTER);
        c.add(iw, BorderLayout.WEST);
        c.add(col, BorderLayout.CENTER);
        return c;
    }

    // ------------------------------------------------------------------ board
    private JPanel buildBoardCard() {
        Theme.Card card = new Theme.Card(new BorderLayout(0, 14)) {
            @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, getPreferredSize().height); }
        };
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(Theme.cardTitle("people", "Current Executive Board", OrgInfo.BOARD.size() + " officers"), BorderLayout.WEST);
        top.add(chip(OrgInfo.SY), BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);
        card.add(new BoardGrid(), BorderLayout.CENTER);
        return card;
    }

    /** Responsive grid of officer tiles: 2–4 columns depending on the available width. */
    private class BoardGrid extends JPanel {
        private static final int TILE_H = 68, GAP = 10;
        private int lastCols = 3;
        BoardGrid() {
            super(null);
            setOpaque(false);
            for (String[] m : OrgInfo.BOARD) add(memberCard(m));
        }
        private int cols() { return getWidth() <= 0 ? 3 : Math.max(2, Math.min(4, (getWidth() + GAP) / 262)); }
        private int rows(int cols) { return (getComponentCount() + cols - 1) / cols; }
        @Override public Dimension getPreferredSize() {
            int r = rows(cols());
            return new Dimension(100, r * TILE_H + Math.max(0, r - 1) * GAP);
        }
        @Override public Dimension getMinimumSize() { return new Dimension(0, getPreferredSize().height); }
        @Override public void doLayout() {
            int cols = cols();
            int w = (getWidth() - GAP * (cols - 1)) / cols;
            for (int i = 0; i < getComponentCount(); i++)
                getComponent(i).setBounds((i % cols) * (w + GAP), (i / cols) * (TILE_H + GAP), w, TILE_H);
            if (cols != lastCols) { lastCols = cols; SwingUtilities.invokeLater(this::revalidate); }
        }
    }

    private static JComponent chip(String s) {
        JLabel l = Theme.label(s, Font.BOLD, 11.5f, Theme.PURPLE_LIGHT);
        l.setBorder(new EmptyBorder(5, 12, 5, 12));
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                Theme.antialias(g);
                g.setColor(Theme.LAVENDER);
                g.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g.dispose();
            }
        };
        p.setOpaque(false);
        p.add(l);
        JPanel w = new JPanel(new GridBagLayout());
        w.setOpaque(false);
        w.add(p);
        return w;
    }

    /** Officer tile: avatar, complete name and position only. m = {position, name}. */
    private JComponent memberCard(String[] m) {
        JPanel c = new JPanel() {
            private final Theme.Anim hoverA = new Theme.Anim(this);
            {
                setOpaque(false);
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hoverA.to(true); }
                    @Override public void mouseExited(MouseEvent e) { hoverA.to(false); }
                });
            }
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                Theme.antialias(g);
                float hv = hoverA.get();
                RoundRectangle2D s = new RoundRectangle2D.Float(2.5f, 2.5f, getWidth() - 5, getHeight() - 5, 14, 14);
                Theme.glow(g, s, Theme.PURPLE_LIGHT, hv * 0.5f, 2);
                g.setColor(Theme.mix(new Color(0x211C39), new Color(0x30264F), hv));
                g.fill(s);
                g.setColor(Theme.mix(Theme.BORDER, Theme.PURPLE_LIGHT, hv));
                g.draw(s);
                int h = getHeight(), tx = 62, tw = getWidth() - tx - 12;
                Art.avatar(g, 33, h / 2.0, 38);
                g.setFont(Theme.font(Font.PLAIN, 11.5f));
                List<String> pos = wrap(g.getFontMetrics(), m[0], tw, 2);
                int total = 16 * (1 + pos.size()), top = (h - total) / 2;
                g.setFont(Theme.font(Font.BOLD, 12.5f));
                g.setColor(Theme.TEXT);
                left(g, m[1], tx, top + 13, tw);
                g.setFont(Theme.font(Font.PLAIN, 11.5f));
                g.setColor(Theme.PURPLE);
                for (int i = 0; i < pos.size(); i++) g.drawString(pos.get(i), tx, top + 13 + 16 * (i + 1));
                g.dispose();
            }
        };
        return c;
    }

    private static void left(Graphics2D g, String s, int x, int y, int w) {
        FontMetrics fm = g.getFontMetrics();
        String t = s;
        while (fm.stringWidth(t) > w && t.length() > 3) t = t.substring(0, t.length() - 2);
        if (!t.equals(s)) t += "…";
        g.drawString(t, x, y);
    }

    /** Word-wraps text into at most maxLines lines of the given pixel width (last line is ellipsised if needed). */
    private static List<String> wrap(FontMetrics fm, String s, int w, int maxLines) {
        List<String> lines = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for (String word : s.split(" ")) {
            String next = cur.length() == 0 ? word : cur + " " + word;
            if (fm.stringWidth(next) <= w || cur.length() == 0) cur = new StringBuilder(next);
            else { lines.add(cur.toString()); cur = new StringBuilder(word); }
        }
        if (cur.length() > 0) lines.add(cur.toString());
        while (lines.size() > maxLines) {
            int last = lines.size() - 1;
            lines.set(last - 1, lines.get(last - 1) + " " + lines.remove(last));
        }
        for (int i = 0; i < lines.size(); i++) {
            String t = lines.get(i);
            while (fm.stringWidth(t) > w && t.length() > 3) t = t.substring(0, t.length() - 2);
            if (!t.equals(lines.get(i))) t += "…";
            lines.set(i, t);
        }
        return lines;
    }

    private static void center(Graphics2D g, String s, int w, int y) {
        FontMetrics fm = g.getFontMetrics();
        String t = s;
        while (fm.stringWidth(t) > w - 10 && t.length() > 3) t = t.substring(0, t.length() - 2);
        if (!t.equals(s)) t += "…";
        g.drawString(t, (w - fm.stringWidth(t)) / 2, y);
    }

    private JPanel buildStatsCard() {
        Theme.Card card = new Theme.Card(new BorderLayout(0, 10)) {
            @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, getPreferredSize().height); }
        };
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(Theme.cardTitle("chart", "Organization Statistics", null), BorderLayout.NORTH);
        JPanel grid = new JPanel(null) {
            private int lastCols = 4;
            private int cols() { return getWidth() <= 0 ? 4 : getWidth() >= 1040 ? 4 : 2; }
            @Override public Dimension getPreferredSize() {
                int rows = cols() == 4 ? 1 : 2;
                return new Dimension(100, rows * 76 + (rows - 1) * 10);
            }
            @Override public void doLayout() {
                int cols = cols(), n = getComponentCount();
                int w = (getWidth() - 10 * (cols - 1)) / cols;
                for (int i = 0; i < n; i++) getComponent(i).setBounds((i % cols) * (w + 10), (i / cols) * 86, w, 76);
                if (cols != lastCols) { lastCols = cols; SwingUtilities.invokeLater(this::revalidate); }
            }
        };
        grid.setOpaque(false);
        String[] icons = {"people", "calendar", "trophy", "star"};
        for (int i = 0; i < OrgInfo.STATS.size(); i++) {
            String[] s = OrgInfo.STATS.get(i);
            JPanel p = new JPanel(new BorderLayout(10, 0)) {
                @Override protected void paintComponent(Graphics g0) {
                    Graphics2D g = (Graphics2D) g0.create();
                    Theme.antialias(g);
                    Shape sh = new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 12, 12);
                    g.setColor(new Color(0x211C39));
                    g.fill(sh);
                    g.setColor(Theme.BORDER);
                    g.draw(sh);
                    g.dispose();
                }
            };
            p.setOpaque(false);
            p.setBorder(new EmptyBorder(8, 10, 8, 8));
            JPanel iw = new JPanel(new GridBagLayout());
            iw.setOpaque(false);
            iw.add(Theme.iconCircle(icons[i], 40, Theme.LAVENDER, Theme.LAVENDER, Theme.PURPLE));
            JPanel t = new JPanel();
            t.setOpaque(false);
            t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
            t.add(Box.createVerticalGlue());
            t.add(Theme.label(s[0], Font.BOLD, 20f, Theme.TEXT));
            t.add(Theme.label(s[1], Font.BOLD, 11.5f, Theme.TEXT));
            t.add(Theme.label(s[2], Font.PLAIN, 10.5f, Theme.MUTED));
            t.add(Box.createVerticalGlue());
            p.add(iw, BorderLayout.WEST);
            p.add(t, BorderLayout.CENTER);
            grid.add(p);
        }
        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    // ---------------------------------------------------------- past admins
    private JPanel buildAdminCard() {
        Theme.Card card = new Theme.Card(new BorderLayout(0, 10));
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(Theme.cardTitle("people", "Past Administrators", null), BorderLayout.WEST);
        yearBox.addItem("All School Years");
        for (String[] a : OrgInfo.PAST_ADMINS) yearBox.addItem(a[0]);
        Theme.styleCombo(yearBox);
        yearBox.setPreferredSize(new Dimension(190, 34));
        yearBox.addActionListener(e -> { offset = 0; buildAdmins(); });
        JPanel yw = new JPanel(new GridBagLayout());
        yw.setOpaque(false);
        yw.add(yearBox);
        top.add(yw, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        JPanel mid = new JPanel(new BorderLayout(8, 0));
        mid.setOpaque(false);
        mid.add(centerWrap(leftBtn), BorderLayout.WEST);
        adminCards.setOpaque(false);
        mid.add(adminCards, BorderLayout.CENTER);
        mid.add(centerWrap(rightBtn), BorderLayout.EAST);
        card.add(mid, BorderLayout.CENTER);

        adminCards.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                int v = Math.max(2, Math.min(5, adminCards.getWidth() / 215));
                if (v != visible) { visible = v; buildAdmins(); }
            }
        });
        buildAdmins();
        return card;
    }

    private static JComponent centerWrap(JComponent c) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.add(c);
        return p;
    }

    private void buildAdmins() {
        shownAdmins = new ArrayList<>();
        String sel = String.valueOf(yearBox.getSelectedItem());
        for (String[] a : OrgInfo.PAST_ADMINS) if ("All School Years".equals(sel) || a[0].equals(sel)) shownAdmins.add(a);
        offset = Math.max(0, Math.min(offset, Math.max(0, shownAdmins.size() - visible)));
        adminCards.removeAll();
        double[] w = new double[visible];
        java.util.Arrays.fill(w, 1);
        adminCards.setLayout(new Theme.RowLayout(12, w));
        for (int i = 0; i < visible; i++) {
            int idx = offset + i;
            if (idx < shownAdmins.size()) adminCards.add(adminCard(shownAdmins.get(idx)));
            else { JPanel e = new JPanel(); e.setOpaque(false); adminCards.add(e); }
        }
        leftBtn.setEnabledState(offset > 0);
        rightBtn.setEnabledState(offset + visible < shownAdmins.size());
        adminCards.revalidate();
        adminCards.repaint();
    }

    private JComponent adminCard(String[] a) {
        JPanel c = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                Theme.antialias(g);
                Shape sh = new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 12, 12);
                g.setColor(new Color(0x30264F));
                g.fill(sh);
                g.setColor(Theme.BORDER);
                g.draw(sh);
                g.setColor(new Color(0x40336A));
                g.fill(new RoundRectangle2D.Float(1, 1, getWidth() - 2, 38, 11, 11));
                g.fillRect(1, 20, getWidth() - 2, 19);
                g.dispose();
            }
        };
        c.setOpaque(false);
        JPanel head = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 9));
        head.setOpaque(false);
        head.setPreferredSize(new Dimension(100, 40));
        head.add(new JLabel(Icons.of("clock", 16, Theme.PURPLE)));
        head.add(Theme.label(a[0], Font.BOLD, 13f, Theme.TEXT));
        c.add(head, BorderLayout.NORTH);
        JPanel rows = new JPanel(new GridLayout(5, 1));
        rows.setOpaque(false);
        rows.setBorder(new EmptyBorder(4, 12, 6, 10));
        for (int i = 0; i < 5; i++) {
            JPanel r = new JPanel(new BorderLayout(6, 0));
            r.setOpaque(false);
            r.setBorder(new MatteBorder(0, 0, i < 4 ? 1 : 0, 0, Theme.BORDER));
            r.add(Theme.label(OrgInfo.ROLES[i], Font.BOLD, 11.5f, Theme.TEXT), BorderLayout.WEST);
            JLabel n = Theme.label(a[i + 1], Font.PLAIN, 11.5f, Theme.MUTED);
            n.setHorizontalAlignment(SwingConstants.RIGHT);
            r.add(n, BorderLayout.CENTER);
            rows.add(r);
        }
        c.add(rows, BorderLayout.CENTER);
        return c;
    }

    // -------------------------------------------------------------------- CTA
    private JPanel buildCta() {
        JPanel bar = new JPanel(new BorderLayout(18, 0)) {
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                Theme.antialias(g);
                Shape s = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight() - 3, 18, 18);
                g.clip(s);
                g.setPaint(new GradientPaint(0, 0, new Color(0x3B25A8), getWidth(), 0, new Color(0x6A47E8)));
                g.fillRect(0, 0, getWidth(), getHeight());
                Art.circuits(g, getWidth() * 0.4, 0, getWidth() * 0.6, getHeight(), 33, 50, new Color(200, 180, 255));
                g.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(14, 26, 17, 26));
        bar.add(centerWrap(Theme.iconCircle("people", 60, new Color(0x7C5CEB), new Color(0x4A2FC9), Color.WHITE)), BorderLayout.WEST);
        JPanel t = new JPanel();
        t.setOpaque(false);
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        t.add(Box.createVerticalGlue());
        t.add(Theme.label("Together, we build a stronger community", Font.BOLD, 16f, Color.WHITE));
        t.add(Theme.label("of future tech leaders.", Font.BOLD, 16f, Color.WHITE));
        t.add(Box.createVerticalGlue());
        bar.add(t, BorderLayout.CENTER);
        Theme.RoundButton b = new Theme.RoundButton("View Organization History", Color.WHITE, Theme.Style.ON_DARK).icon("send");
        b.addActionListener(e -> showHistory());
        JPanel bw = new JPanel(new GridBagLayout());
        bw.setOpaque(false);
        bw.add(b);
        bar.add(bw, BorderLayout.EAST);
        return bar;
    }

    private void showHistory() {
        StringBuilder sb = new StringBuilder();
        sb.append(OrgInfo.FULL_NAME).append("\n\n").append(OrgInfo.INTRO).append("\n\n");
        sb.append(OrgInfo.SY).append(" (current)\n");
        for (String[] m : OrgInfo.BOARD) sb.append("   ").append(m[0]).append(": ").append(m[1]).append('\n');
        for (String[] a : OrgInfo.PAST_ADMINS) {
            sb.append('\n').append(a[0]).append('\n');
            for (int i = 0; i < 5; i++) sb.append("   ").append(OrgInfo.ROLES[i]).append(": ").append(a[i + 1]).append('\n');
        }
        JTextArea ta = new JTextArea(sb.toString(), 22, 54);
        ta.setEditable(false);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setFont(Theme.font(Font.PLAIN, 13f));
        ta.setBorder(new EmptyBorder(8, 10, 8, 10));
        JOptionPane.showMessageDialog(this, Theme.scroll(ta), "Organization History", JOptionPane.PLAIN_MESSAGE);
    }

    private static class ArrowBtn extends JComponent {
        private final String icon;
        private boolean on = true;
        private final Theme.Anim hoverA = new Theme.Anim(this);
        ArrowBtn(String icon, Runnable action) {
            this.icon = icon;
            setPreferredSize(new Dimension(30, 30));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { if (on) action.run(); }
                @Override public void mouseEntered(MouseEvent e) { hoverA.to(true); }
                @Override public void mouseExited(MouseEvent e) { hoverA.to(false); }
            });
        }
        void setEnabledState(boolean b) { on = b; repaint(); }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Theme.antialias(g);
            float h = on ? hoverA.get() : 0f;
            RoundRectangle2D s = new RoundRectangle2D.Float(1.5f, 1.5f, getWidth() - 3, getHeight() - 3, 8, 8);
            Theme.glow(g, s, Theme.PURPLE_LIGHT, h * 0.55f, 2);
            g.setColor(Theme.mix(new Color(0x211C39), Theme.LAVENDER, h));
            g.fill(s);
            g.setColor(Theme.mix(Theme.BORDER, Theme.PURPLE_LIGHT, h));
            g.draw(s);
            Icons.paint(g, icon, 8, 8, 14, on ? Theme.PURPLE_LIGHT : Theme.MUTED);
            g.dispose();
        }
    }
}

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Shared colours, fonts and custom-painted components (SYCOMP purple theme). */
public final class Theme {
    private Theme() {}

    public static final Color SIDEBAR_TOP = new Color(0x0B0928);
    public static final Color SIDEBAR_BOTTOM = new Color(0x1A0F48);
    public static final Color PURPLE = new Color(0x5B3FD6);
    public static final Color PURPLE_DARK = new Color(0x3B25A8);
    public static final Color PURPLE_LIGHT = new Color(0x9B7BEA);
    public static final Color LAVENDER = new Color(0xEFEBFD);
    public static final Color BG = new Color(0xF8F7FE);
    public static final Color TEXT = new Color(0x14123A);
    public static final Color MUTED = new Color(0x6E6A8C);
    public static final Color BORDER = new Color(0xE3DFF5);
    public static final Color GREEN = new Color(0x16A34A);
    public static final Color RED = new Color(0xE5484D);
    public static final Color BLUE = new Color(0x3B82F6);

    private static final String FAMILY = pickFamily();

    private static String pickFamily() {
        Set<String> have = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String f : new String[]{"Segoe UI", "SF Pro Text", "Helvetica Neue", "Inter", "Noto Sans", "DejaVu Sans"})
            if (have.contains(f)) return f;
        return Font.SANS_SERIF;
    }

    public static Font font(int style, float size) { return new Font(FAMILY, style, 12).deriveFont(style, size); }

    private static final DecimalFormat PESO = new DecimalFormat("₱#,##0.00");
    public static String peso(BigDecimal v) { return PESO.format(v); }
    public static String pesoShort(BigDecimal v) { return new DecimalFormat("₱ #,##0").format(v); }

    public static void antialias(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    public static Color alpha(Color c, int a) { return new Color(c.getRed(), c.getGreen(), c.getBlue(), a); }

    public static JLabel label(String text, int style, float size, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font(style, size));
        l.setForeground(color);
        return l;
    }

    public static <T extends JComponent> T fixedHeight(T c, int h) {
        c.setPreferredSize(new Dimension(100, h));
        c.setMinimumSize(new Dimension(0, h));
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    // ------------------------------------------------------------------ layouts
    /** Lays children out in one row; widths follow the weights, height = tallest child. */
    public static class RowLayout implements LayoutManager {
        private final double[] w;
        private final int gap;
        public RowLayout(int gap, double... weights) { this.gap = gap; this.w = weights; }
        @Override public void addLayoutComponent(String n, Component c) {}
        @Override public void removeLayoutComponent(Component c) {}
        @Override public Dimension preferredLayoutSize(Container p) {
            int h = 0, n = 0;
            for (Component c : p.getComponents()) if (c.isVisible()) { h = Math.max(h, c.getPreferredSize().height); n++; }
            Insets in = p.getInsets();
            return new Dimension(100 * n + gap * Math.max(0, n - 1) + in.left + in.right, h + in.top + in.bottom);
        }
        @Override public Dimension minimumLayoutSize(Container p) { return new Dimension(0, preferredLayoutSize(p).height); }
        @Override public void layoutContainer(Container p) {
            Insets in = p.getInsets();
            java.util.List<Component> vis = new java.util.ArrayList<>();
            for (Component c : p.getComponents()) if (c.isVisible()) vis.add(c);
            int n = vis.size();
            if (n == 0) return;
            double total = 0;
            for (int i = 0; i < n; i++) total += i < w.length ? w[i] : 1;
            int avail = p.getWidth() - in.left - in.right - gap * (n - 1);
            int h = p.getHeight() - in.top - in.bottom;
            double x = in.left;
            for (int i = 0; i < n; i++) {
                double wi = avail * (i < w.length ? w[i] : 1) / total;
                vis.get(i).setBounds((int) Math.round(x), in.top, (int) Math.round(wi), h);
                x += wi + gap;
            }
        }
    }

    public static JPanel row(int gap, double[] weights, Component... cs) {
        JPanel p = new JPanel(new RowLayout(gap, weights));
        p.setOpaque(false);
        for (Component c : cs) p.add(c);
        return p;
    }

    /** Page container that follows the viewport width and scrolls vertically when too tall. */
    public static class ScrollPanel extends JPanel implements Scrollable {
        public ScrollPanel() { setBackground(BG); }
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 28; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return Math.max(60, (int) (r.height * 0.9)); }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() {
            return getParent() instanceof JViewport v && v.getHeight() > getPreferredSize().height;
        }
    }

    // -------------------------------------------------------------------- card
    /** White rounded panel with a hairline border and soft shadow; optional two-colour tint. */
    public static class Card extends JPanel {
        private Color tintA, tintB;
        public Card(LayoutManager lm) {
            super(lm);
            setOpaque(false);
            setBorder(new EmptyBorder(16, 18, 20, 18));
        }
        public Card tint(Color a, Color b) { tintA = a; tintB = b; return this; }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            antialias(g);
            int w = getWidth() - 2, h = getHeight() - 5;
            for (int i = 3; i >= 1; i--) {
                g.setColor(new Color(60, 37, 168, 6));
                g.fill(new RoundRectangle2D.Float(1 - i * 0.5f, 2 + i * 0.5f, w + i, h + i * 0.4f, 18 + i, 18 + i));
            }
            Shape s = new RoundRectangle2D.Float(0.5f, 0.5f, w, h, 16, 16);
            g.setPaint(tintA == null ? Color.WHITE : new GradientPaint(0, 0, tintA, getWidth(), getHeight(), tintB));
            g.fill(s);
            g.setColor(BORDER);
            g.draw(s);
            g.dispose();
            super.paintComponent(g0);
        }
    }

    // ----------------------------------------------------------------- buttons
    public enum Style { FILLED, OUTLINE, ON_DARK }

    public static class RoundButton extends JButton {
        private final Color base;
        private final Style style;
        private boolean hover;

        public RoundButton(String text, Color base, boolean outline) { this(text, base, outline ? Style.OUTLINE : Style.FILLED); }

        public RoundButton(String text, Color base, Style style) {
            super(text);
            this.base = base;
            this.style = style;
            setFont(font(Font.BOLD, 13f));
            setForeground(style == Style.FILLED ? Color.WHITE : style == Style.ON_DARK ? Color.WHITE : base);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setIconTextGap(8);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 20, 10, 20));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        public RoundButton icon(String name) {
            setIcon(Icons.of(name, 16, style == Style.OUTLINE ? base : Color.WHITE));
            return this;
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            antialias(g);
            RoundRectangle2D shape = new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 12, 12);
            switch (style) {
                case FILLED -> {
                    g.setPaint(!isEnabled() ? new Color(0xC9C4E3)
                            : new GradientPaint(0, 0, hover ? PURPLE.darker() : base, 0, getHeight(), hover ? PURPLE_DARK : base.darker()));
                    g.fill(shape);
                }
                case OUTLINE -> {
                    g.setColor(hover ? alpha(base, 24) : Color.WHITE);
                    g.fill(shape);
                    g.setColor(alpha(base, 150));
                    g.setStroke(new BasicStroke(1.3f));
                    g.draw(shape);
                }
                case ON_DARK -> {
                    g.setColor(new Color(255, 255, 255, hover ? 60 : 30));
                    g.fill(shape);
                    g.setColor(new Color(255, 255, 255, 190));
                    g.setStroke(new BasicStroke(1.4f));
                    g.draw(shape);
                }
            }
            g.dispose();
            super.paintComponent(g0);
        }
    }

    // ------------------------------------------------------------------- pills
    public static Color pillColor(String text) {
        return switch (text) {
            case "Income", "Published" -> GREEN;
            case "Expense" -> RED;
            case "Pending" -> BLUE;
            default -> PURPLE;
        };
    }

    public static class Pill extends JComponent {
        private final String text;
        private final Color color;
        public Pill(String text) { this(text, pillColor(text)); }
        public Pill(String text, Color color) {
            this.text = text;
            this.color = color;
            setFont(font(Font.BOLD, 11.5f));
            setOpaque(false);
        }
        @Override public Dimension getPreferredSize() {
            return new Dimension(getFontMetrics(getFont()).stringWidth(text) + 26, 24);
        }
        @Override public Dimension getMinimumSize() { return getPreferredSize(); }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            antialias(g);
            int h = 22, y = (getHeight() - h) / 2;
            g.setColor(alpha(color, 30));
            g.fillRoundRect(0, y, getWidth(), h, h, h);
            g.setFont(getFont());
            g.setColor(color);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, y + (h + fm.getAscent() - fm.getDescent()) / 2);
            g.dispose();
        }
    }

    // ------------------------------------------------------------ icon circles
    public static JComponent iconCircle(String icon, int size, Color bgA, Color bgB, Color fg) {
        return new JComponent() {
            { setPreferredSize(new Dimension(size, size)); setMinimumSize(new Dimension(size, size)); setMaximumSize(new Dimension(size, size)); }
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                antialias(g);
                g.setPaint(new GradientPaint(0, 0, bgA, size, size, bgB));
                g.fill(new Ellipse2D.Double(0, 0, size, size));
                double is = size * 0.5;
                Icons.paint(g, icon, (size - is) / 2, (size - is) / 2, is, fg);
                g.dispose();
            }
        };
    }

    public static JComponent iconCircle(String icon, int size) { return iconCircle(icon, size, PURPLE_LIGHT, PURPLE_DARK, Color.WHITE); }

    // ------------------------------------------------------------ form widgets
    public static void styleField(JComponent c) {
        c.setFont(font(Font.PLAIN, 13.5f));
        c.setForeground(TEXT);
        c.setBackground(Color.WHITE);
        c.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(7, 11, 7, 11)));
        c.setPreferredSize(new Dimension(100, 38));
        if (c instanceof JTextComponent tc) tc.setCaretColor(PURPLE);
    }

    public static void styleCombo(JComboBox<?> c) {
        c.setFont(font(Font.PLAIN, 13.5f));
        c.setBackground(Color.WHITE);
        c.setForeground(TEXT);
        c.setOpaque(true);
        c.setUI(new BasicComboBoxUI() {
            @Override protected JButton createArrowButton() {
                JButton b = new JButton() {
                    @Override public void paint(Graphics g0) {
                        Graphics2D g = (Graphics2D) g0.create();
                        antialias(g);
                        g.setColor(Color.WHITE);
                        g.fillRect(0, 0, getWidth(), getHeight());
                        Icons.paint(g, "chevDown", getWidth() / 2.0 - 6, getHeight() / 2.0 - 6, 12, MUTED);
                        g.dispose();
                    }
                };
                b.setBorder(new EmptyBorder(0, 0, 0, 0));
                b.setFocusPainted(false);
                b.setContentAreaFilled(false);
                b.setPreferredSize(new Dimension(30, 30));
                return b;
            }
            @Override public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                g.setColor(Color.WHITE);
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        });
        c.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(0, 4, 0, 0)));
        c.setPreferredSize(new Dimension(100, 38));
        c.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean sel, boolean foc) {
                JLabel lb = (JLabel) super.getListCellRendererComponent(l, v, i, sel, false);
                lb.setBorder(new EmptyBorder(7, 10, 7, 10));
                lb.setFont(font(Font.PLAIN, 13.5f));
                lb.setForeground(TEXT);
                lb.setBackground(i >= 0 && sel ? LAVENDER : Color.WHITE);
                return lb;
            }
        });
        if (c.isEditable() && c.getEditor().getEditorComponent() instanceof JComponent ed) {
            ed.setBorder(new EmptyBorder(0, 6, 0, 4));
            ed.setFont(font(Font.PLAIN, 13.5f));
        }
    }

    /** Text field with grey hint text and an optional leading icon. */
    public static class HintField extends JTextField {
        private final String hint, icon;
        public HintField(String hint, String icon) { this.hint = hint; this.icon = icon; }
        public HintField(String hint) { this(hint, null); }
        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            antialias(g);
            if (icon != null) Icons.paint(g, icon, 11, (getHeight() - 16) / 2.0, 16, PURPLE);
            if (getText().isEmpty()) {
                g.setFont(getFont());
                g.setColor(new Color(0xA19DBA));
                Insets in = getInsets();
                g.drawString(hint, in.left, (getHeight() + g.getFontMetrics().getAscent() - g.getFontMetrics().getDescent()) / 2);
            }
            g.dispose();
        }
    }

    public static JLabel fieldLabel(String text) { return label(text, Font.BOLD, 12f, MUTED); }

    // ------------------------------------------------------------------ tables
    public static void styleTable(JTable t) {
        t.setFont(font(Font.PLAIN, 13f));
        t.setForeground(TEXT);
        t.setRowHeight(42);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(new Color(0xEAE5FD));
        t.setSelectionForeground(TEXT);
        t.setFillsViewportHeight(true);
        t.setBackground(Color.WHITE);
        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(100, 40));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tb, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tb, v, false, false, r, c);
                l.setFont(font(Font.BOLD, 12f));
                l.setForeground(MUTED);
                l.setBackground(new Color(0xF3F0FD));
                l.setBorder(new EmptyBorder(0, 10, 0, 10));
                TableCellRenderer cr = tb.getColumnModel().getColumn(c).getCellRenderer();
                int align = cr instanceof PaddedRenderer pr ? pr.align : SwingConstants.LEFT;
                l.setHorizontalAlignment(align);
                return l;
            }
        });
    }

    public static JScrollPane scroll(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(new LineBorder(BORDER, 1, true));
        sp.getViewport().setBackground(Color.WHITE);
        slimScroll(sp);
        return sp;
    }

    public static void slimScroll(JScrollPane sp) {
        sp.getVerticalScrollBar().setUI(new SlimScrollUI());
        sp.getHorizontalScrollBar().setUI(new SlimScrollUI());
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(10, 10));
        sp.getHorizontalScrollBar().setPreferredSize(new Dimension(10, 10));
        sp.getVerticalScrollBar().setUnitIncrement(24);
    }

    private static class SlimScrollUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() { thumbColor = new Color(0xC9C0F0); trackColor = new Color(0, 0, 0, 0); }
        @Override protected JButton createDecreaseButton(int o) { return zero(); }
        @Override protected JButton createIncreaseButton(int o) { return zero(); }
        private JButton zero() { JButton b = new JButton(); b.setPreferredSize(new Dimension(0, 0)); return b; }
        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {}
        @Override protected void paintThumb(Graphics g0, JComponent c, Rectangle r) {
            if (r.isEmpty()) return;
            Graphics2D g = (Graphics2D) g0.create();
            antialias(g);
            g.setColor(thumbColor);
            g.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
            g.dispose();
        }
    }

    public static class PaddedRenderer extends DefaultTableCellRenderer {
        final int align;
        public PaddedRenderer(int align) { this.align = align; }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, false, r, c);
            l.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, new Color(0xF0EDFA)), new EmptyBorder(0, 10, 0, 10)));
            l.setHorizontalAlignment(align);
            return l;
        }
    }

    /** Status / type pill inside a table cell. */
    public static class PillRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            JPanel p = new JPanel(new GridBagLayout());
            p.setBackground(sel ? t.getSelectionBackground() : Color.WHITE);
            p.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, new Color(0xF0EDFA)), new EmptyBorder(0, 10, 0, 6)));
            GridBagConstraints gc = new GridBagConstraints();
            gc.anchor = GridBagConstraints.WEST;
            gc.weightx = 1;
            p.add(new Pill(String.valueOf(v)), gc);
            return p;
        }
    }

    // ------------------------------------------------------------ small widgets
    /** Clickable text link. */
    public static JLabel link(String text, Runnable action) {
        JLabel l = label(text, Font.BOLD, 12.5f, PURPLE);
        l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        l.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { action.run(); } });
        return l;
    }

    /** Section title with a purple icon, used at the top of cards. */
    public static JPanel cardTitle(String icon, String title, String subtitle) {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        JComponent ic = new JComponent() {
            { setPreferredSize(new Dimension(24, 24)); }
            @Override protected void paintComponent(Graphics g) { Icons.paint((Graphics2D) g, icon, 0, 1, 22, PURPLE); }
        };
        JPanel t = new JPanel();
        t.setOpaque(false);
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        t.add(label(title, Font.BOLD, 16f, TEXT));
        if (subtitle != null) { t.add(Box.createVerticalStrut(2)); t.add(label(subtitle, Font.PLAIN, 12f, MUTED)); }
        p.add(ic, BorderLayout.WEST);
        p.add(t, BorderLayout.CENTER);
        return p;
    }
}

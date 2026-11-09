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

    // ---- design tokens: SYCOMP violet system ----
    public static final Color SIDEBAR_TOP = new Color(0x100D24);
    public static final Color SIDEBAR_BOTTOM = new Color(0x15112B);
    public static final Color PURPLE = new Color(0x9B7BFF);
    public static final Color PURPLE_DARK = new Color(0x5135B8);
    public static final Color PURPLE_LIGHT = new Color(0xB49CFF);
    public static final Color LAVENDER = new Color(0x30264F);
    public static final Color SOFT = new Color(0x1B172F);
    public static final Color BG = new Color(0x0C0A18);
    public static final Color TEXT = new Color(0xF4F1FF);
    public static final Color MUTED = new Color(0xAAA4C4);
    public static final Color BORDER = new Color(0x332D4D);
    public static final Color GREEN = new Color(0x4BD39A);
    public static final Color WARNING = new Color(0xF4BC62);
    public static final Color RED = new Color(0xFF758A);
    public static final Color BLUE = new Color(0x78A9FF);
    public static final Color CYAN = new Color(0x22D3EE);

    private static final String FAMILY = pickFamily(new String[]{"Inter", "Plus Jakarta Sans", "Segoe UI Variable Text", "Segoe UI", "SF Pro Text", "Helvetica Neue", "Noto Sans", "DejaVu Sans"});
    /** Headings use Plus Jakarta Sans when available, otherwise the body family. */
    private static final String HEADING_FAMILY = pickFamily(new String[]{"Plus Jakarta Sans", "Inter", "Segoe UI", "SF Pro Display", "Helvetica Neue", "Noto Sans", "DejaVu Sans"});

    /**
     * Drop Inter / Plus Jakarta Sans .ttf files in a "fonts" folder next to the app (or in src/fonts) and they
     * are registered automatically; otherwise the first installed family from the preference list is used.
     */
    private static String pickFamily(String[] prefs) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (String dir : new String[]{"fonts", "src/fonts", "../fonts"}) {
            java.io.File[] files = new java.io.File(dir).listFiles((d, n) -> n.toLowerCase().endsWith(".ttf") || n.toLowerCase().endsWith(".otf"));
            if (files == null) continue;
            for (java.io.File f : files) {
                try { ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, f)); } catch (Exception ignored) {}
            }
        }
        Set<String> have = new HashSet<>(Arrays.asList(ge.getAvailableFontFamilyNames()));
        for (String f : prefs) if (have.contains(f)) return f;
        return Font.SANS_SERIF;
    }

    // --------------------------------------------------------------- animation
    /** Smoothly eases a 0..1 value toward a target and repaints its component on every step. */
    public static class Anim {
        private final JComponent comp;
        private final javax.swing.Timer timer;
        private float value, target;
        public Anim(JComponent comp) {
            this.comp = comp;
            this.timer = new javax.swing.Timer(15, e -> {
                float d = target - value;
                if (Math.abs(d) < 0.02f) { value = target; ((javax.swing.Timer) e.getSource()).stop(); }
                else value += d * 0.28f;
                comp.repaint();
            });
        }
        public void to(boolean on) { target = on ? 1f : 0f; if (!timer.isRunning()) timer.start(); }
        /** Eased progress, 0..1. */
        public float get() { return value * value * (3 - 2 * value); }
    }

    /** Soft coloured halo drawn just outside a rounded shape (used for hover / pressed / focus glow). */
    public static void glow(Graphics2D g, RectangularShape s, Color c, float strength, int radius) {
        if (strength <= 0.01f) return;
        for (int i = radius; i >= 1; i--) {
            float t = 1f - (i - 1f) / radius;
            int a = Math.min(255, Math.round(strength * 20 * t * t));
            g.setColor(alpha(c, a));
            g.setStroke(new BasicStroke(i * 2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(s);
        }
    }

    public static Color mix(Color a, Color b, float t) {
        return new Color(Math.round(a.getRed() + (b.getRed() - a.getRed()) * t),
                Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    /** Type scale: bold text of 15pt and above is a heading (heading family); everything else is body text. */
    public static Font font(int style, float size) {
        String fam = (style & Font.BOLD) != 0 && size >= 15f ? HEADING_FAMILY : FAMILY;
        return new Font(fam, style, 12).deriveFont(style, size);
    }

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
    /** Rounded glass-style panel with a hairline border and soft shadow; optional two-colour tint. */
    public static class Card extends JPanel {
        private Color tintA, tintB;
        private final Anim hover = new Anim(this);
        public Card(LayoutManager lm) {
            super(lm);
            setOpaque(false);
            setBorder(new EmptyBorder(16, 18, 20, 18));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover.to(true); }
                @Override public void mouseExited(MouseEvent e) { hover.to(false); }
            });
        }
        public Card tint(Color a, Color b) { tintA = a; tintB = b; return this; }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            antialias(g);
            int w = getWidth() - 2, h = getHeight() - 5;
            float progress = hover.get();
            for (int i = 5; i >= 1; i--) {
                g.setColor(new Color(104, 76, 214, Math.round(4 + progress * 2)));
                g.fill(new RoundRectangle2D.Float(1 - i * 0.6f, 2 + i * 0.6f, w + i * 1.2f, h + i * 0.5f, 18 + i, 18 + i));
            }
            Shape s = new RoundRectangle2D.Float(0.5f, 0.5f, w, h, 16, 16);
            g.setPaint(tintA == null
                    ? new GradientPaint(0, 0, mix(new Color(0x211C39), new Color(0x292244), progress), 0, getHeight(), new Color(0x17142A))
                    : new GradientPaint(0, 0, tintA, getWidth(), getHeight(), tintB));
            g.fill(s);
            g.setColor(mix(BORDER, new Color(0x65549B), progress * 0.55f));
            g.draw(s);
            g.setColor(new Color(255, 255, 255, Math.round(14 + progress * 12)));
            g.draw(new RoundRectangle2D.Float(1.5f, 1.5f, w - 2, Math.max(12, getHeight() * 0.35f), 15, 15));
            g.dispose();
            super.paintComponent(g0);
        }
    }

    // ----------------------------------------------------------------- buttons
    public enum Style { FILLED, OUTLINE, ON_DARK }

    public static class RoundButton extends JButton {
        private final Color base;
        private final Style style;
        private final Anim hoverA = new Anim(this), pressA = new Anim(this);

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
            // the outer 3px of the button are reserved for the glow halo
            setBorder(new EmptyBorder(10, 20, 10, 20));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hoverA.to(true); }
                @Override public void mouseExited(MouseEvent e) { hoverA.to(false); pressA.to(false); }
                @Override public void mousePressed(MouseEvent e) { pressA.to(true); }
                @Override public void mouseReleased(MouseEvent e) { pressA.to(false); }
            });
            addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { repaint(); }
                @Override public void focusLost(FocusEvent e) { repaint(); }
            });
        }

        public RoundButton icon(String name) {
            setIcon(Icons.of(name, 16, style == Style.OUTLINE ? base : Color.WHITE));
            return this;
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            antialias(g);
            float h = hoverA.get(), p = pressA.get();
            RoundRectangle2D shape = new RoundRectangle2D.Float(3f, 3f, getWidth() - 7, getHeight() - 7, 12, 12);
            float glowAmt = Math.min(1f, h * 0.55f + p * 0.9f + (hasFocus() ? 0.5f : 0f));
            if (isEnabled()) glow(g, shape, style == Style.ON_DARK ? PURPLE_LIGHT : mix(base, PURPLE_LIGHT, 0.35f), glowAmt, 3);
            switch (style) {
                case FILLED -> {
                    // flat fill: hover and press only deepen the colour
                    Color c = mix(base, PURPLE_DARK, 0.16f * h + 0.22f * p);
                    g.setColor(!isEnabled() ? new Color(0xCFC9EC) : c);
                    g.fill(shape);
                }
                case OUTLINE -> {
                    g.setColor(mix(SOFT, mix(SOFT, base, 0.28f), Math.min(1f, h + p * 0.5f)));
                    g.fill(shape);
                    g.setColor(alpha(base, Math.round(130 + 100 * h)));
                    g.setStroke(new BasicStroke(1.3f));
                    g.draw(shape);
                }
                case ON_DARK -> {
                    g.setColor(new Color(255, 255, 255, Math.round(26 + 44 * h + 30 * p)));
                    g.fill(shape);
                    g.setColor(new Color(255, 255, 255, Math.round(150 + 80 * h)));
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
        c.setBackground(new Color(0x1A1730));
        Border idle = new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(7, 11, 7, 11));
        Border focus = new CompoundBorder(new CompoundBorder(new LineBorder(alpha(PURPLE_LIGHT, 90), 1, true), new LineBorder(PURPLE, 1, true)),
                new EmptyBorder(6, 10, 6, 10));
        c.setBorder(idle);
        c.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { c.setBorder(focus); }
            @Override public void focusLost(FocusEvent e) { c.setBorder(idle); }
        });
        c.setPreferredSize(new Dimension(100, 38));
        if (c instanceof JTextComponent tc) tc.setCaretColor(PURPLE);
    }

    public static void styleCombo(JComboBox<?> c) {
        c.setFont(font(Font.PLAIN, 13.5f));
        c.setBackground(new Color(0x1A1730));
        c.setForeground(TEXT);
        c.setOpaque(true);
        c.setUI(new BasicComboBoxUI() {
            @Override protected JButton createArrowButton() {
                JButton b = new JButton() {
                    @Override public void paint(Graphics g0) {
                        Graphics2D g = (Graphics2D) g0.create();
                        antialias(g);
                        g.setColor(new Color(0x1A1730));
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
                g.setColor(new Color(0x1A1730));
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
                lb.setBackground(i >= 0 && sel ? LAVENDER : new Color(0x1A1730));
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
                g.setColor(MUTED);
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
        t.setSelectionBackground(new Color(0x40336A));
        t.setSelectionForeground(TEXT);
        t.setFillsViewportHeight(true);
        t.setBackground(new Color(0x17142A));
        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(100, 40));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tb, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tb, v, false, false, r, c);
                l.setFont(font(Font.BOLD, 12f));
                l.setForeground(MUTED);
                l.setBackground(new Color(0x211C39));
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
        sp.getViewport().setBackground(BG);
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
        @Override protected void configureScrollBarColors() { thumbColor = new Color(0x65549B); trackColor = new Color(0, 0, 0, 0); }
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
            l.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, BORDER), new EmptyBorder(0, 10, 0, 10)));
            l.setHorizontalAlignment(align);
            return l;
        }
    }

    /** Status / type pill inside a table cell. */
    public static class PillRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            JPanel p = new JPanel(new GridBagLayout());
            p.setBackground(sel ? t.getSelectionBackground() : new Color(0x17142A));
            p.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, BORDER), new EmptyBorder(0, 10, 0, 6)));
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

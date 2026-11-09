import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.imageio.ImageIO;

/** Java2D artwork: logo, circuit traces, laptop, building, avatars and the page banners. */
public final class Art {
    private Art() {}

    // ---------------------------------------------------------------- logo
    private static BufferedImage logoImage;
    private static boolean logoTried;
    private static final Map<Integer, BufferedImage> SCALED = new HashMap<>();

    /** Loads logo.png (transparent PNG) from the classpath, or from the folder the app is run in. */
    private static synchronized BufferedImage loadLogo() {
        if (!logoTried) {
            logoTried = true;
            try {
                URL u = Art.class.getResource("/logo.png");
                if (u != null) logoImage = ImageIO.read(u);
                else if (new File("logo.png").exists()) logoImage = ImageIO.read(new File("logo.png"));
            } catch (Exception ignored) { /* falls back to the drawn emblem */ }
        }
        return logoImage;
    }

    /** High-quality downscale (repeated halving + bicubic) so the small sidebar logo stays crisp. */
    private static synchronized BufferedImage scaledLogo(int d) {
        BufferedImage cached = SCALED.get(d);
        if (cached != null) return cached;
        BufferedImage cur = loadLogo();
        int w = cur.getWidth();
        while (w / 2 >= d) {
            w /= 2;
            cur = resize(cur, w);
        }
        if (w != d) cur = resize(cur, d);
        SCALED.put(d, cur);
        return cur;
    }

    private static BufferedImage resize(BufferedImage src, int d) {
        BufferedImage out = new BufferedImage(d, d, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(src, 0, 0, d, d, null);
        g.dispose();
        return out;
    }

    /** Square icon for the window title bar / taskbar, or null if logo.png is missing. */
    public static Image iconImage() {
        return loadLogo() == null ? null : scaledLogo(128);
    }

    /** The organization logo (logo.png). (cx, cy) = centre, size = diameter in pixels. */
    public static void logo(Graphics2D g0, double cx, double cy, double size) {
        if (loadLogo() == null) { drawnLogo(g0, cx, cy, size); return; }   // no logo.png found
        Graphics2D g = (Graphics2D) g0.create();
        Theme.antialias(g);
        int d = Math.max(8, (int) Math.round(size));
        int x = (int) Math.round(cx - d / 2.0), y = (int) Math.round(cy - d / 2.0);
        // soft glow so the dark disc separates from dark backgrounds
        g.setColor(new Color(150, 110, 255, 60));
        g.fill(new Ellipse2D.Double(x - 2, y - 2, d + 4, d + 4));
        g.drawImage(scaledLogo(d), x, y, null);
        g.dispose();
    }

    /** Fallback emblem drawn in code: dashed ring, glowing hexagon and an S ribbon. */
    private static void drawnLogo(Graphics2D g0, double cx, double cy, double size) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.antialias(g);
        g.translate(cx, cy);
        double r = size / 2;
        // outer dashed ring (stands in for the circular lettering)
        g.setColor(new Color(190, 170, 255, 120));
        g.setStroke(new BasicStroke((float) (size * 0.035), BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1f,
                new float[]{(float) (size * 0.02), (float) (size * 0.035)}, 0));
        g.draw(new Ellipse2D.Double(-r, -r, size, size));
        g.setStroke(new BasicStroke(1f));
        g.setColor(new Color(190, 170, 255, 70));
        g.draw(new Ellipse2D.Double(-r * 0.86, -r * 0.86, size * 0.86, size * 0.86));
        // hexagon
        double hr = r * 0.66;
        Path2D.Double hex = new Path2D.Double();
        for (int i = 0; i < 6; i++) {
            double a = Math.toRadians(-90 + i * 60);
            double px = hr * Math.cos(a), py = hr * Math.sin(a);
            if (i == 0) hex.moveTo(px, py); else hex.lineTo(px, py);
        }
        hex.closePath();
        g.setPaint(new GradientPaint(0, (float) -hr, new Color(120, 90, 235, 120), 0, (float) hr, new Color(50, 30, 150, 150)));
        g.fill(hex);
        g.setPaint(new GradientPaint(0, (float) -hr, new Color(235, 228, 255), 0, (float) hr, new Color(150, 120, 245)));
        g.setStroke(new BasicStroke((float) (size * 0.03), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(hex);
        // S ribbon
        double u = hr * 0.78;
        Path2D.Double s = new Path2D.Double();
        s.moveTo(0.50 * u, -0.42 * u);
        s.curveTo(0.30 * u, -0.80 * u, -0.62 * u, -0.72 * u, -0.50 * u, -0.22 * u);
        s.curveTo(-0.40 * u, 0.12 * u, 0.55 * u, -0.02 * u, 0.52 * u, 0.36 * u);
        s.curveTo(0.50 * u, 0.80 * u, -0.30 * u, 0.80 * u, -0.50 * u, 0.44 * u);
        g.setStroke(new BasicStroke((float) (size * 0.115), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setPaint(new GradientPaint(0, (float) (-u * 0.8), Color.WHITE, 0, (float) (u * 0.8), new Color(190, 165, 255)));
        g.draw(s);
        g.dispose();
    }

    // -------------------------------------------------------------- circuits
    /** Faint circuit traces inside the rectangle (x, y, w, h). Deterministic for a given seed. */
    public static void circuits(Graphics2D g0, double x, double y, double w, double h, long seed, int alpha, Color col) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.antialias(g);
        g.clip(new Rectangle2D.Double(x, y, w, h));
        Random rnd = new Random(seed);
        int n = (int) Math.max(6, w * h / 14000);
        for (int i = 0; i < n; i++) {
            double px = x + rnd.nextDouble() * w, py = y + rnd.nextDouble() * h;
            Path2D.Double p = new Path2D.Double();
            p.moveTo(px, py);
            int segs = 2 + rnd.nextInt(3);
            for (int k = 0; k < segs; k++) {
                double len = 30 + rnd.nextDouble() * 70;
                switch (rnd.nextInt(3)) {
                    case 0 -> px += len;
                    case 1 -> { px += len * 0.7; py -= len * 0.7; }
                    default -> py -= len;
                }
                p.lineTo(px, py);
            }
            g.setColor(Theme.alpha(col, alpha));
            g.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(p);
            g.setColor(Theme.alpha(col, Math.min(255, alpha + 40)));
            g.draw(new Ellipse2D.Double(px - 3.5, py - 3.5, 7, 7));
        }
        g.dispose();
    }

    // ---------------------------------------------------------------- laptop
    private static Point2D.Double map(double[][] q, double u, double v, double x, double y, double w) {
        double px = q[0][0] * (1 - u) * (1 - v) + q[1][0] * u * (1 - v) + q[2][0] * u * v + q[3][0] * (1 - u) * v;
        double py = q[0][1] * (1 - u) * (1 - v) + q[1][1] * u * (1 - v) + q[2][1] * u * v + q[3][1] * (1 - u) * v;
        return new Point2D.Double(x + px * w, y + py * w);
    }

    private static Path2D.Double quad(double[][] q, double x, double y, double w) {
        Path2D.Double p = new Path2D.Double();
        for (int i = 0; i < 4; i++) {
            double px = x + q[i][0] * w, py = y + q[i][1] * w;
            if (i == 0) p.moveTo(px, py); else p.lineTo(px, py);
        }
        p.closePath();
        return p;
    }

    /** Open laptop with glowing code on the screen. (x,y) = top-left of its bounding box, w = width. */
    public static void laptop(Graphics2D g0, double x, double y, double w) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.antialias(g);
        double[][] screen = {{0.18, 0.06}, {1.0, -0.10}, {1.0, 0.50}, {0.18, 0.58}};
        double[][] base = {{0.18, 0.58}, {1.0, 0.50}, {0.97, 0.66}, {0.0, 0.80}};
        // keyboard base
        g.setPaint(new GradientPaint((float) x, (float) (y + 0.6 * w), new Color(60, 52, 120), (float) (x + w), (float) (y + 0.8 * w), new Color(30, 24, 80)));
        g.fill(quad(base, x, y, w));
        g.setColor(new Color(150, 130, 240, 90));
        g.setStroke(new BasicStroke(1f));
        for (int r = 1; r < 5; r++) g.draw(new Line2D.Double(map(base, 0, r / 5.0, x, y, w), map(base, 1, r / 5.0, x, y, w)));
        // screen
        g.setPaint(new GradientPaint((float) x, (float) y, new Color(18, 12, 60), (float) (x + w), (float) y, new Color(34, 22, 100)));
        g.fill(quad(screen, x, y, w));
        g.setColor(new Color(120, 100, 220, 140));
        g.setStroke(new BasicStroke(1.5f));
        g.draw(quad(screen, x, y, w));
        // code lines
        Random rnd = new Random(11);
        Color[] cols = {new Color(150, 120, 255), new Color(236, 110, 200), new Color(100, 200, 255), new Color(250, 200, 110), new Color(190, 190, 230)};
        g.setStroke(new BasicStroke((float) (w * 0.009), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int r = 0; r < 18; r++) {
            double v = 0.08 + r * 0.05;
            double u = 0.06 + (rnd.nextInt(4)) * 0.05;
            int chunks = 2 + rnd.nextInt(3);
            for (int c = 0; c < chunks; c++) {
                double len = 0.05 + rnd.nextDouble() * 0.16;
                if (u + len > 0.95) break;
                g.setColor(Theme.alpha(cols[rnd.nextInt(cols.length)], 200));
                g.draw(new Line2D.Double(map(screen, u, v, x, y, w), map(screen, u + len, v, x, y, w)));
                u += len + 0.03;
            }
        }
        g.dispose();
    }

    // -------------------------------------------------------------- building
    public static void building(Graphics2D g0, double x, double y, double w, double h) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.antialias(g);
        double[][] towers = {{0.00, 0.35, 0.28}, {0.26, 0.05, 0.34}, {0.58, 0.22, 0.24}, {0.80, 0.45, 0.20}};
        for (double[] t : towers) {
            double tx = x + t[0] * w, ty = y + t[1] * h, tw = t[2] * w, th = h - t[1] * h;
            g.setPaint(new GradientPaint((float) tx, (float) ty, new Color(70, 55, 170, 200), (float) tx, (float) (ty + th), new Color(20, 14, 70, 230)));
            g.fill(new Rectangle2D.Double(tx, ty, tw, th));
            g.setColor(new Color(170, 150, 255, 55));
            g.setStroke(new BasicStroke(1f));
            for (double gx = tx + 12; gx < tx + tw; gx += 12) g.draw(new Line2D.Double(gx, ty, gx, ty + th));
            for (double gy = ty + 14; gy < ty + th; gy += 14) g.draw(new Line2D.Double(tx, gy, tx + tw, gy));
            g.setColor(new Color(210, 195, 255, 70));
            g.draw(new Rectangle2D.Double(tx, ty, tw, th));
        }
        g.dispose();
    }

    // ---------------------------------------------------------------- avatar
    public static void avatar(Graphics2D g0, double cx, double cy, double d) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.antialias(g);
        g.translate(cx - d / 2, cy - d / 2);
        g.setPaint(new GradientPaint(0, 0, new Color(0xC9BCF7), (float) d, (float) d, new Color(0xA48BF0)));
        g.fill(new Ellipse2D.Double(0, 0, d, d));
        g.clip(new Ellipse2D.Double(0, 0, d, d));
        g.setColor(new Color(0x2E1A8C));
        g.fill(new Ellipse2D.Double(d * 0.32, d * 0.18, d * 0.36, d * 0.36));
        g.fill(new Ellipse2D.Double(d * 0.12, d * 0.62, d * 0.76, d * 0.7));
        g.dispose();
    }

    // --------------------------------------------------------------- sidebar
    public static void sidebarBackground(Graphics2D g0, int w, int h) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.antialias(g);
        g.setPaint(new GradientPaint(0, 0, Theme.SIDEBAR_TOP, 0, h, Theme.SIDEBAR_BOTTOM));
        g.fillRect(0, 0, w, h);
        circuits(g, 0, h * 0.42, w, h * 0.58, 5, 70, new Color(140, 100, 255));
        // diagonal glow lines
        g.setColor(new Color(150, 110, 255, 90));
        g.setStroke(new BasicStroke(1.4f));
        g.draw(new Line2D.Double(0, h - 250, 120, h - 380));
        g.draw(new Line2D.Double(120, h - 380, w, h - 380));
        g.setColor(new Color(150, 110, 255, 45));
        g.draw(new Line2D.Double(0, h - 190, 150, h - 320));
        // purple corner triangle
        Path2D.Double tri = new Path2D.Double();
        tri.moveTo(0, h - 85); tri.lineTo(0, h); tri.lineTo(92, h); tri.closePath();
        g.setPaint(new GradientPaint(0, h - 85, new Color(0x7B4DFF), 92, h, new Color(0x3E1FB8)));
        g.fill(tri);
        g.setColor(new Color(150, 110, 255, 90));
        g.draw(new Line2D.Double(92, h, w, h - 100));
        g.dispose();
    }

    // ---------------------------------------------------------------- banner
    public static List<String> wrap(String s, FontMetrics fm, int maxW) {
        List<String> out = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : s.split(" ")) {
            String t = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(t) > maxW && line.length() > 0) { out.add(line.toString()); line = new StringBuilder(word); }
            else line = new StringBuilder(t);
        }
        if (line.length() > 0) out.add(line.toString());
        return out;
    }

    /** Dark purple hero banner used at the top of the Dashboard, About and Contact pages. */
    public static class Banner extends JPanel {
        private final String kind, eyebrow, title, subtitle, body, rightTitle, rightSub;

        public Banner(String kind, String eyebrow, String title, String subtitle, String body, String rightTitle, String rightSub) {
            this.kind = kind; this.eyebrow = eyebrow; this.title = title; this.subtitle = subtitle;
            this.body = body; this.rightTitle = rightTitle; this.rightSub = rightSub;
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Theme.antialias(g);
            int w = getWidth(), h = getHeight();
            Shape clip = new RoundRectangle2D.Float(0, 0, w, h - 3, 18, 18);
            g.clip(clip);
            g.setPaint(new GradientPaint(0, 0, new Color(0x120B3A), w, h, new Color(0x4B2FC4)));
            g.fillRect(0, 0, w, h);
            circuits(g, w * 0.35, 0, w * 0.65, h, 21, 60, new Color(170, 140, 255));

            switch (kind) {
                case "about" -> {
                    building(g, w - 520, 0, 520, h);
                    g.setPaint(new GradientPaint(w - 520, 0, new Color(0x120B3A), w - 200, 0, new Color(18, 11, 58, 0)));
                    g.fillRect(w - 520, 0, 520, h);
                }
                case "contact" -> {
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.38f));
                    laptop(g, w - 470, 8, 440);
                    g.setComposite(AlphaComposite.SrcOver);
                    g.setPaint(new GradientPaint(w - 560, 0, new Color(0x120B3A), w - 240, 0, new Color(18, 11, 58, 0)));
                    g.fillRect(w - 560, 0, 560, h);
                }
                default -> {
                    laptop(g, w - 470, 4, 440);
                    g.setPaint(new GradientPaint(w - 640, 0, new Color(0x1A0F52), w - 330, 0, new Color(26, 15, 82, 0)));
                    g.fillRect(w - 640, 0, 640, h);
                }
            }

            double logoSize = Math.min(120, h - 36);
            logo(g, 28 + logoSize / 2 + 8, h / 2.0 - 2, logoSize);

            int tx = (int) (logoSize + 70);
            int maxTextW = Math.max(260, Math.min(620, (int) (w * 0.58) - tx));
            int y = 0;
            // measure total text height to centre vertically
            Font fe = Theme.font(Font.PLAIN, 12f), ft = Theme.font(Font.BOLD, 28f), fs = Theme.font(Font.PLAIN, 16f), fb = Theme.font(Font.PLAIN, 14f);
            List<String> bodyLines = wrap(body, g.getFontMetrics(fb), maxTextW);
            int total = (eyebrow != null ? 20 : 0) + 38 + (subtitle != null ? 26 : 0) + bodyLines.size() * 21 + 6;
            y = (h - total) / 2 + 6;
            if (eyebrow != null) { g.setFont(fe); g.setColor(new Color(0xBFA8FF)); g.drawString(eyebrow, tx, y + 8); y += 20; }
            g.setFont(ft); g.setColor(Color.WHITE); g.drawString(title, tx, y + 30); y += 38;
            if (subtitle != null) { g.setFont(fs); g.setColor(new Color(0xE6DEFF)); g.drawString(subtitle, tx, y + 18); y += 26; }
            g.setFont(fb); g.setColor(new Color(0xDAD2FA));
            for (String l : bodyLines) { y += 21; g.drawString(l, tx, y + 4); }

            if (rightTitle != null) {
                g.setFont(Theme.font("about".equals(kind) ? Font.ITALIC : Font.PLAIN, "about".equals(kind) ? 19f : 17f));
                g.setColor(new Color(0xEFE9FF));
                FontMetrics fm = g.getFontMetrics();
                int ry = "about".equals(kind) ? h - 34 : 52;
                g.drawString(rightTitle, w - 34 - fm.stringWidth(rightTitle), ry);
                if (rightSub != null) {
                    g.setFont(Theme.font(Font.PLAIN, 12f));
                    g.setColor(new Color(0xCFC4F5));
                    g.drawString(rightSub, w - 34 - g.getFontMetrics().stringWidth(rightSub), ry + 22);
                    g.setColor(new Color(0xA68BFF));
                    g.fillRect(w - 34 - 36, ry + 34, 36, 2);
                }
            }
            g.dispose();
        }
    }
}

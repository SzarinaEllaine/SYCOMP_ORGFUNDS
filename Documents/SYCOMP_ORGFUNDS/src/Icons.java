import javax.swing.Icon;
import java.awt.*;
import java.awt.geom.*;

/** Vector icon set drawn on a 24 x 24 grid. */
public final class Icons {
    private Icons() {}

    public static Icon of(String name, int size, Color c) {
        return new Icon() {
            @Override public void paintIcon(Component cmp, Graphics g, int x, int y) { paint((Graphics2D) g, name, x, y, size, c); }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    private static Path2D.Double poly(boolean close, double... p) {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(p[0], p[1]);
        for (int i = 2; i < p.length; i += 2) path.lineTo(p[i], p[i + 1]);
        if (close) path.closePath();
        return path;
    }

    private static Area cut(Shape base, Shape... lines) {
        Area a = new Area(base);
        BasicStroke st = new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        for (Shape l : lines) a.subtract(new Area(st.createStrokedShape(l)));
        return a;
    }

    public static void paint(Graphics2D g0, String name, double x, double y, double size, Color c) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.antialias(g);
        g.translate(x, y);
        double s = size / 24.0;
        g.scale(s, s);
        g.setColor(c);
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (name) {
            case "home" -> {
                Path2D.Double p = poly(true, 2.5, 11.5, 12, 3, 21.5, 11.5, 19, 11.5, 19, 20.5, 14.5, 20.5, 14.5, 14.5, 9.5, 14.5, 9.5, 20.5, 5, 20.5, 5, 11.5);
                g.fill(p);
                g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(p);
            }
            case "doc" -> {
                Path2D.Double p = poly(true, 5.5, 3.5, 14, 3.5, 19, 8.5, 19, 20.5, 5.5, 20.5);
                Area a = cut(p, new Line2D.Double(8.5, 12.5, 15.5, 12.5), new Line2D.Double(8.5, 16, 15.5, 16),
                        poly(false, 14, 3.5, 14, 8.5, 19, 8.5));
                g.fill(a);
                g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(p);
            }
            case "people" -> {
                g.fill(new Ellipse2D.Double(5, 4, 7, 7));
                g.fill(new RoundRectangle2D.Double(2.5, 13, 12.5, 8, 7, 7));
                g.fill(new Ellipse2D.Double(14, 5, 5.6, 5.6));
                g.fill(new RoundRectangle2D.Double(14.5, 12.5, 7.5, 8.5, 6, 6));
            }
            case "mail" -> {
                RoundRectangle2D.Double r = new RoundRectangle2D.Double(2.5, 5, 19, 14.5, 3.5, 3.5);
                g.fill(cut(r, poly(false, 4.5, 8, 12, 13.5, 19.5, 8)));
            }
            case "user" -> {
                g.fill(new Ellipse2D.Double(8, 3.5, 8, 8));
                g.fill(new RoundRectangle2D.Double(4.5, 13.5, 15, 8.5, 8, 8));
            }
            case "search" -> {
                g.draw(new Ellipse2D.Double(3.5, 3.5, 13, 13));
                g.draw(new Line2D.Double(15, 15, 20.5, 20.5));
            }
            case "bell" -> {
                Path2D.Double p = new Path2D.Double();
                p.moveTo(6, 10.5); p.curveTo(6, 6.5, 8.7, 4.2, 12, 4.2); p.curveTo(15.3, 4.2, 18, 6.5, 18, 10.5);
                p.lineTo(18, 15); p.lineTo(20, 17.5); p.lineTo(4, 17.5); p.lineTo(6, 15); p.closePath();
                g.draw(p);
                g.draw(new Line2D.Double(12, 2.5, 12, 4.2));
                g.draw(new Arc2D.Double(9.6, 17.6, 4.8, 4, 180, 180, Arc2D.OPEN));
            }
            case "calendar" -> {
                g.draw(new RoundRectangle2D.Double(3.5, 5, 17, 15.5, 3.5, 3.5));
                g.draw(new Line2D.Double(3.5, 10, 20.5, 10));
                g.draw(new Line2D.Double(8, 3, 8, 6.5));
                g.draw(new Line2D.Double(16, 3, 16, 6.5));
                g.fill(new Rectangle2D.Double(7.5, 13, 2.4, 2.4));
                g.fill(new Rectangle2D.Double(11, 13, 2.4, 2.4));
                g.fill(new Rectangle2D.Double(14.5, 13, 2.4, 2.4));
            }
            case "coins" -> {
                g.draw(new Ellipse2D.Double(5, 4, 14, 5.5));
                g.draw(new Line2D.Double(5, 6.8, 5, 18));
                g.draw(new Line2D.Double(19, 6.8, 19, 18));
                g.draw(new Arc2D.Double(5, 9.2, 14, 5.5, 180, 180, Arc2D.OPEN));
                g.draw(new Arc2D.Double(5, 15.2, 14, 5.5, 180, 180, Arc2D.OPEN));
            }
            case "wallet" -> {
                g.draw(new RoundRectangle2D.Double(3, 6, 18, 14, 3.5, 3.5));
                g.draw(poly(false, 5, 6, 16, 3.5, 17.5, 6));
                g.draw(new RoundRectangle2D.Double(14, 11.5, 7, 4.5, 2, 2));
                g.fill(new Ellipse2D.Double(16, 13, 1.8, 1.8));
            }
            case "folder" -> g.draw(poly(true, 3, 6, 9.5, 6, 11.5, 8.5, 21, 8.5, 21, 19, 3, 19));
            case "clock" -> {
                g.draw(new Ellipse2D.Double(3, 3, 18, 18));
                g.draw(poly(false, 12, 7, 12, 12, 15.5, 14));
            }
            case "download" -> {
                g.draw(new Line2D.Double(12, 3.5, 12, 14.5));
                g.draw(poly(false, 7.5, 10.5, 12, 15, 16.5, 10.5));
                g.draw(poly(false, 4, 16, 4, 20, 20, 20, 20, 16));
            }
            case "filter" -> g.draw(poly(true, 3.5, 5, 20.5, 5, 14, 12.5, 14, 19.5, 10, 17.5, 10, 12.5));
            case "chevDown" -> { g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); g.draw(poly(false, 6, 9, 12, 15, 18, 9)); }
            case "chevRight" -> { g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); g.draw(poly(false, 9, 6, 15, 12, 9, 18)); }
            case "chevLeft" -> { g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); g.draw(poly(false, 15, 6, 9, 12, 15, 18)); }
            case "arrowRight" -> { g.draw(new Line2D.Double(4, 12, 19, 12)); g.draw(poly(false, 13, 6, 19.5, 12, 13, 18)); }
            case "arrowUp" -> { g.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); g.draw(new Line2D.Double(12, 20, 12, 5)); g.draw(poly(false, 6, 11, 12, 5, 18, 11)); }
            case "arrowDown" -> { g.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); g.draw(new Line2D.Double(12, 4, 12, 19)); g.draw(poly(false, 6, 13, 12, 19, 18, 13)); }
            case "target" -> {
                g.draw(new Ellipse2D.Double(3, 3, 18, 18));
                g.draw(new Ellipse2D.Double(7, 7, 10, 10));
                g.fill(new Ellipse2D.Double(10.3, 10.3, 3.4, 3.4));
                g.draw(new Line2D.Double(12, 12, 20.5, 3.5));
            }
            case "eye" -> {
                Path2D.Double p = new Path2D.Double();
                p.moveTo(2, 12); p.curveTo(5, 5.5, 19, 5.5, 22, 12); p.curveTo(19, 18.5, 5, 18.5, 2, 12);
                g.draw(p);
                g.fill(new Ellipse2D.Double(9, 9, 6, 6));
            }
            case "gem" -> {
                g.draw(poly(true, 6.5, 4, 17.5, 4, 22, 10, 12, 21, 2, 10));
                g.draw(new Line2D.Double(2, 10, 22, 10));
                g.draw(poly(false, 9, 4, 7.5, 10, 12, 21, 16.5, 10, 15, 4));
            }
            case "building" -> {
                g.draw(new Rectangle2D.Double(5, 3, 10, 18));
                g.draw(new Rectangle2D.Double(15, 9, 5, 12));
                for (int i = 0; i < 3; i++) { g.fill(new Rectangle2D.Double(8, 6.5 + i * 4.5, 1.8, 1.8)); g.fill(new Rectangle2D.Double(11.2, 6.5 + i * 4.5, 1.8, 1.8)); }
            }
            case "trophy" -> {
                g.draw(poly(true, 7, 4, 17, 4, 17, 9.5));
                Path2D.Double cup = new Path2D.Double();
                cup.moveTo(7, 4); cup.lineTo(7, 9.5); cup.curveTo(7, 13, 17, 13, 17, 9.5); cup.lineTo(17, 4);
                g.draw(cup);
                g.draw(poly(false, 7, 6, 4, 6, 4, 8, 7, 11));
                g.draw(poly(false, 17, 6, 20, 6, 20, 8, 17, 11));
                g.draw(new Line2D.Double(12, 13, 12, 18));
                g.draw(new Line2D.Double(8, 20, 16, 20));
            }
            case "star" -> {
                Path2D.Double p = new Path2D.Double();
                for (int i = 0; i < 10; i++) {
                    double r = i % 2 == 0 ? 9.5 : 4.2, a = -Math.PI / 2 + i * Math.PI / 5;
                    double px = 12 + r * Math.cos(a), py = 12.5 + r * Math.sin(a);
                    if (i == 0) p.moveTo(px, py); else p.lineTo(px, py);
                }
                p.closePath();
                g.fill(p);
                g.draw(p);
            }
            case "phone" -> {
                Path2D.Double p = new Path2D.Double();
                p.moveTo(6, 3.5); p.lineTo(9.5, 3.5); p.lineTo(11, 8); p.lineTo(9, 9.8);
                p.curveTo(10, 12.2, 11.8, 14, 14.2, 15); p.lineTo(16, 13); p.lineTo(20.5, 14.5); p.lineTo(20.5, 18);
                p.curveTo(20.5, 19.4, 19.4, 20.5, 18, 20.5); p.curveTo(10, 20, 4, 14, 3.5, 6); p.curveTo(3.5, 4.6, 4.6, 3.5, 6, 3.5);
                g.fill(p);
            }
            case "pin" -> {
                Path2D.Double p = new Path2D.Double();
                p.moveTo(12, 21.5); p.curveTo(6, 14.5, 5, 12, 5, 9.5); p.curveTo(5, 5.6, 8, 2.8, 12, 2.8);
                p.curveTo(16, 2.8, 19, 5.6, 19, 9.5); p.curveTo(19, 12, 18, 14.5, 12, 21.5);
                Area a = new Area(p);
                a.subtract(new Area(new Ellipse2D.Double(9.2, 6.7, 5.6, 5.6)));
                g.fill(a);
            }
            case "map" -> {
                g.draw(poly(true, 3, 6.5, 9, 4.5, 15, 6.5, 21, 4.5, 21, 17.5, 15, 19.5, 9, 17.5, 3, 19.5));
                g.draw(new Line2D.Double(9, 4.5, 9, 17.5));
                g.draw(new Line2D.Double(15, 6.5, 15, 19.5));
            }
            case "share" -> {
                g.draw(new Line2D.Double(8, 10.8, 16, 7));
                g.draw(new Line2D.Double(8, 13.2, 16, 17));
                g.fill(new Ellipse2D.Double(3.5, 9, 5.5, 5.5));
                g.fill(new Ellipse2D.Double(15, 3.5, 5.5, 5.5));
                g.fill(new Ellipse2D.Double(15, 15.5, 5.5, 5.5));
            }
            case "headset" -> {
                g.draw(new Arc2D.Double(4, 3, 16, 16, 0, 180, Arc2D.OPEN));
                g.fill(new RoundRectangle2D.Double(3, 11, 4.5, 7.5, 3, 3));
                g.fill(new RoundRectangle2D.Double(16.5, 11, 4.5, 7.5, 3, 3));
                g.draw(poly(false, 19, 18.5, 19, 20.5, 14, 21));
            }
            case "help" -> {
                g.fill(new Ellipse2D.Double(2, 2, 20, 20));
                g.setColor(Theme.PURPLE_DARK);
                g.setFont(Theme.font(Font.BOLD, 17f).deriveFont(17f / (float) 1));
                FontMetrics fm = g.getFontMetrics();
                g.drawString("?", 12 - fm.stringWidth("?") / 2f, 12 + (fm.getAscent() - fm.getDescent()) / 2f);
            }
            case "send" -> {
                g.draw(poly(true, 3, 11, 21, 3, 14, 21, 11, 14));
                g.draw(new Line2D.Double(11, 14, 21, 3));
            }
            case "info" -> {
                g.draw(new Ellipse2D.Double(3, 3, 18, 18));
                g.draw(new Line2D.Double(12, 11, 12, 16.5));
                g.fill(new Ellipse2D.Double(11, 7, 2, 2));
            }
            case "layers" -> {
                g.draw(poly(true, 12, 3.5, 21.5, 8.5, 12, 13.5, 2.5, 8.5));
                g.draw(poly(false, 2.5, 12.5, 12, 17.5, 21.5, 12.5));
                g.draw(poly(false, 2.5, 16.5, 12, 21.5, 21.5, 16.5));
            }
            case "history" -> {
                g.draw(new Arc2D.Double(4, 4, 16, 16, 50, 270, Arc2D.OPEN));
                g.draw(poly(false, 12, 8, 12, 12, 15, 14));
                g.draw(poly(false, 3.2, 3.5, 4.3, 8.5, 9, 7.5));
            }
            case "chart" -> {
                g.fill(new RoundRectangle2D.Double(4, 12, 4.4, 8, 1.5, 1.5));
                g.fill(new RoundRectangle2D.Double(9.8, 7, 4.4, 13, 1.5, 1.5));
                g.fill(new RoundRectangle2D.Double(15.6, 3, 4.4, 17, 1.5, 1.5));
            }
            case "check" -> { g.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); g.draw(poly(false, 5, 12.5, 10, 17.5, 19, 7)); }
            case "plus" -> { g.draw(new Line2D.Double(12, 5, 12, 19)); g.draw(new Line2D.Double(5, 12, 19, 12)); }
            case "dots" -> { for (int i = 0; i < 3; i++) g.fill(new Ellipse2D.Double(10.4, 3.4 + i * 7, 3.4, 3.4)); }
            case "fb" -> {
                g.setFont(Theme.font(Font.BOLD, 22f));
                FontMetrics fm = g.getFontMetrics();
                g.drawString("f", 12 - fm.stringWidth("f") / 2f + 0.5f, 20.5f);
            }
            case "xx" -> {
                g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new Line2D.Double(5.5, 4.5, 18.5, 19.5));
                g.draw(new Line2D.Double(18.5, 4.5, 5.5, 19.5));
            }
            case "ig" -> {
                g.draw(new RoundRectangle2D.Double(3.5, 3.5, 17, 17, 6, 6));
                g.draw(new Ellipse2D.Double(8, 8, 8, 8));
                g.fill(new Ellipse2D.Double(15.6, 5.8, 1.9, 1.9));
            }
            case "yt" -> {
                g.fill(new RoundRectangle2D.Double(2, 5.5, 20, 13, 6, 6));
                g.setColor(Theme.PURPLE_DARK);
                g.fill(poly(true, 10, 9.3, 16, 12, 10, 14.7));
            }
            default -> { }
        }
        g.dispose();
    }
}

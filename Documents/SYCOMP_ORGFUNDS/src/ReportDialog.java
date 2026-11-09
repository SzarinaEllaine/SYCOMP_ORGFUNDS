import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

/** Shows one generated report and lets the user export it to CSV. */
public class ReportDialog extends JDialog {
    private final ReportCatalog catalog;
    private final ReportCatalog.Content content;
    private final String fileBase;

    public static void show(Window owner, ReportCatalog catalog, ReportCatalog.Report r, SchoolPeriod p) {
        catalog.markOpened(r, p);
        new ReportDialog(owner, catalog, r, catalog.build(r, p), r.id + "_" + p.startYear + "-" + (p.startYear + 1) + "_" + p.semesterLabel().replace(' ', '-')).setVisible(true);
    }

    public static void export(Component parent, ReportCatalog catalog, ReportCatalog.Content content, String fileBase) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(fileBase + ".csv"));
        if (fc.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();
        if (!f.getName().toLowerCase().endsWith(".csv")) f = new File(f.getParentFile(), f.getName() + ".csv");
        try {
            writeCsv(f, content);
            catalog.addDownload();
            JOptionPane.showMessageDialog(parent, "Report saved to:\n" + f.getAbsolutePath(), "Export complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(parent, "Could not save the file: " + ex.getMessage(), "Export failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Writes the report (title, period, KPIs, table) as a UTF-8 CSV that Excel opens correctly. */
    public static void writeCsv(File f, ReportCatalog.Content content) throws IOException {
        try (PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8))) {
            w.write('﻿');
            w.println(csv(new String[]{content.title}));
            w.println(csv(new String[]{"Period", content.period.schoolYearLabel() + ", " + content.period.semesterLabel() + " (" + content.period.coveredLabel() + ")"}));
            for (String[] k : content.kpis) w.println(csv(k));
            w.println();
            w.println(csv(content.columns));
            for (String[] row : content.rows) w.println(csv(row));
        }
    }

    private static String csv(String[] cells) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cells.length; i++) {
            if (i > 0) sb.append(',');
            sb.append('"').append(cells[i].replace("\"", "\"\"")).append('"');
        }
        return sb.toString();
    }

    private ReportDialog(Window owner, ReportCatalog catalog, ReportCatalog.Report r, ReportCatalog.Content c, String fileBase) {
        super(owner, r.title, ModalityType.APPLICATION_MODAL);
        this.catalog = catalog;
        this.content = c;
        this.fileBase = fileBase;
        setSize(940, 660);
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);

        // header
        JPanel head = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                Theme.antialias(g);
                g.setPaint(new GradientPaint(0, 0, new Color(0x120B3A), getWidth(), getHeight(), new Color(0x4B2FC4)));
                g.fillRect(0, 0, getWidth(), getHeight());
                Art.circuits(g, getWidth() * 0.5, 0, getWidth() * 0.5, getHeight(), 9, 55, new Color(170, 140, 255));
                Art.logo(g, getWidth() - 70, getHeight() / 2.0, 74);
                g.dispose();
            }
        };
        head.setBorder(new EmptyBorder(18, 26, 18, 26));
        head.setPreferredSize(new Dimension(100, 112));
        JPanel ht = new JPanel();
        ht.setOpaque(false);
        ht.setLayout(new BoxLayout(ht, BoxLayout.Y_AXIS));
        ht.add(Theme.label(c.title, Font.BOLD, 21f, Color.WHITE));
        ht.add(Box.createVerticalStrut(4));
        ht.add(Theme.label("School Year " + c.period.schoolYearLabel() + "  •  " + c.period.semesterLabel() + "  •  " + c.period.coveredLabel(),
                Font.PLAIN, 13f, new Color(0xD8CFFA)));
        JLabel st = Theme.label(r.status().toUpperCase(), Font.BOLD, 10.5f, r.isPublished() ? new Color(0x8FF0B8) : new Color(0xA9D0FF));
        ht.add(Box.createVerticalStrut(4));
        ht.add(st);
        head.add(ht, BorderLayout.WEST);
        root.add(head, BorderLayout.NORTH);

        // body
        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(16, 22, 8, 22));

        JPanel kpis = new JPanel(new GridLayout(1, Math.max(1, c.kpis.size()), 12, 0));
        kpis.setOpaque(false);
        kpis.setPreferredSize(new Dimension(100, 84));
        for (String[] k : c.kpis) {
            Theme.Card card = new Theme.Card(new BorderLayout(0, 4)).tint(new Color(0x211C39), new Color(0x19162D));
            card.setBorder(new EmptyBorder(12, 16, 14, 12));
            card.add(Theme.label(k[0], Font.PLAIN, 12f, Theme.MUTED), BorderLayout.NORTH);
            card.add(Theme.label(k[1], Font.BOLD, 19f, Theme.TEXT), BorderLayout.CENTER);
            kpis.add(card);
        }
        body.add(kpis, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(c.columns, 0) {
            @Override public boolean isCellEditable(int r, int cc) { return false; }
        };
        for (String[] row : c.rows) model.addRow(row);
        JTable table = new JTable(model);
        Theme.styleTable(table);
        table.setRowHeight(36);
        for (int i = 0; i < c.columns.length; i++) {
            String col = c.columns[i];
            boolean num = col.equals("Amount") || col.equals("Collections") || col.equals("Expenses") || col.equals("Net")
                    || col.equals("Spent") || col.startsWith("%") || col.equals("Running Total");
            table.getColumnModel().getColumn(i).setCellRenderer(new Theme.PaddedRenderer(num ? SwingConstants.RIGHT : SwingConstants.LEFT));
        }
        JScrollPane sp = Theme.scroll(table);
        body.add(sp, BorderLayout.CENTER);

        if (!c.notes.isEmpty()) {
            JPanel notes = new JPanel();
            notes.setOpaque(false);
            notes.setLayout(new BoxLayout(notes, BoxLayout.Y_AXIS));
            for (String n : c.notes) notes.add(Theme.label("•  " + n, Font.PLAIN, 12.5f, Theme.MUTED));
            body.add(notes, BorderLayout.SOUTH);
        }
        root.add(body, BorderLayout.CENTER);

        // buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btns.setOpaque(false);
        btns.setBorder(new EmptyBorder(0, 12, 6, 12));
        Theme.RoundButton close = new Theme.RoundButton("Close", Theme.PURPLE, true);
        close.addActionListener(e -> dispose());
        Theme.RoundButton exp = new Theme.RoundButton("Export CSV", Theme.PURPLE, false).icon("download");
        exp.addActionListener(e -> export(this, catalog, content, fileBase));
        btns.add(close);
        btns.add(exp);
        root.add(btns, BorderLayout.SOUTH);

        setContentPane(root);
    }
}

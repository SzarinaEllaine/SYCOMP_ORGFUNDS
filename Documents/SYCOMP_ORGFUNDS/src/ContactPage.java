import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

/** Contact channels, a validated message form, office hours, social links and FAQs. */
public class ContactPage extends Theme.ScrollPanel {
    private static final String[] SUBJECTS = {"General Inquiry", "Report Request", "Membership", "Collaboration", "Feedback", "Other"};
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private final DashboardFrame shell;
    private final Hint nameField = new Hint("Enter your full name");
    private final Hint emailField = new Hint("you@example.com");
    private final JComboBox<String> subjectBox = new JComboBox<>();
    private final JTextArea messageArea = new JTextArea();
    private final JLabel counter = Theme.label("0/500", Font.PLAIN, 11.5f, Theme.MUTED);
    private final JPanel faqList = new JPanel();
    private int openFaq = -1;

    public ContactPage(DashboardFrame shell) {
        this.shell = shell;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(16, 24, 24, 24));

        add(Theme.fixedHeight(new Art.Banner("contact", null, "Contact Us", null,
                "We're here to help! Reach out to us for inquiries, support, or collaboration opportunities.",
                "Have questions?", "We'd love to hear from you."), 144));
        add(Box.createVerticalStrut(14));

        JPanel left = column(Theme.fixedHeight(buildChannels(), 200), Box.createVerticalStrut(14), Theme.fixedHeight(buildForm(), 428));
        JPanel right = column(Theme.fixedHeight(buildHours(), 190), Box.createVerticalStrut(12),
                Theme.fixedHeight(buildFollow(), 118), Box.createVerticalStrut(12), Theme.fixedHeight(buildFaq(), 310));
        JPanel body = Theme.row(14, new double[]{2.05, 1}, left, right);
        add(Theme.fixedHeight(body, 642));
        add(Box.createVerticalStrut(14));
        add(Theme.fixedHeight(buildCta(), 96));
    }

    private static JPanel column(Component... cs) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        for (Component c : cs) p.add(c);
        return p;
    }

    public void setSubject(String subject) {
        if (subject != null) subjectBox.setSelectedItem(subject);
        nameField.requestFocusInWindow();
    }

    // --------------------------------------------------------------- channels
    private JPanel buildChannels() {
        Theme.Card card = new Theme.Card(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(14, 16, 18, 16));
        JPanel title = new JPanel(new BorderLayout(14, 0));
        title.setOpaque(false);
        title.add(Theme.iconCircle("mail", 48), BorderLayout.WEST);
        JPanel t = new JPanel();
        t.setOpaque(false);
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        t.add(Box.createVerticalGlue());
        t.add(Theme.label("Get in Touch", Font.BOLD, 19f, Theme.TEXT));
        t.add(Theme.label("Reach out to us through the following channels.", Font.PLAIN, 12.5f, Theme.MUTED));
        t.add(Box.createVerticalGlue());
        title.add(t, BorderLayout.CENTER);
        card.add(title, BorderLayout.NORTH);

        JPanel row = new JPanel(new Theme.RowLayout(12, 1, 1, 1.05, 0.9));
        row.setOpaque(false);
        row.add(channel("mail", "Email", OrgInfo.EMAIL, "Replies in 1–2 business days.", () -> mailTo(OrgInfo.EMAIL)));
        row.add(channel("phone", "Phone", OrgInfo.PHONE, "Mon–Fri, 8 AM – 5 PM", null));
        row.add(channel("pin", "Office Address", String.join("\n", OrgInfo.ADDRESS), null, null));
        JPanel loc = channel("map", "Office Location", null, null, null);
        JPanel lw = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        lw.setOpaque(false);
        lw.setAlignmentX(LEFT_ALIGNMENT);
        lw.add(Theme.link("View on Maps →", () -> openUri(this, OrgInfo.MAP_URL)));
        ((JPanel) loc.getComponent(1)).add(lw);
        row.add(loc);
        card.add(row, BorderLayout.CENTER);
        return card;
    }

    private static JComponent left(JComponent c) { c.setAlignmentX(LEFT_ALIGNMENT); return c; }

    private JPanel channel(String icon, String title, String line, String small, Runnable click) {
        JPanel c = new JPanel(new BorderLayout(0, 6)) {
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                Theme.antialias(g);
                Shape s = new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 12, 12);
                g.setColor(new Color(0xFBFAFF));
                g.fill(s);
                g.setColor(Theme.BORDER);
                g.draw(s);
                g.dispose();
            }
        };
        c.setOpaque(false);
        c.setBorder(new EmptyBorder(10, 12, 8, 8));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.setOpaque(false);
        top.add(Theme.iconCircle(icon, 38));
        c.add(top, BorderLayout.NORTH);
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(left(Theme.label(title, Font.BOLD, 13f, Theme.TEXT)));
        if (line != null) for (String l : line.split("\n")) body.add(left(Theme.label(l, Font.PLAIN, 11.5f, Theme.TEXT)));
        if (small != null) body.add(left(Theme.label(small, Font.PLAIN, 10.5f, Theme.MUTED)));
        c.add(body, BorderLayout.CENTER);
        if (click != null) {
            c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            c.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { click.run(); } });
        }
        return c;
    }

    // ------------------------------------------------------------------- form
    private JPanel buildForm() {
        Theme.Card card = new Theme.Card(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(14, 18, 18, 18));
        JPanel title = new JPanel(new BorderLayout(12, 0));
        title.setOpaque(false);
        title.add(Theme.iconCircle("send", 42), BorderLayout.WEST);
        JPanel t = new JPanel();
        t.setOpaque(false);
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        t.add(Theme.label("Send Us a Message", Font.BOLD, 17f, Theme.TEXT));
        t.add(Theme.label("Fill out the form below and we'll get back to you as soon as possible.", Font.PLAIN, 12f, Theme.MUTED));
        title.add(t, BorderLayout.CENTER);
        card.add(title, BorderLayout.NORTH);

        Theme.styleField(nameField);
        Theme.styleField(emailField);
        subjectBox.addItem("Select a subject");
        for (String s : SUBJECTS) subjectBox.addItem(s);
        Theme.styleCombo(subjectBox);

        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setFont(Theme.font(Font.PLAIN, 13.5f));
        messageArea.setForeground(Theme.TEXT);
        messageArea.setBorder(new EmptyBorder(8, 10, 8, 10));
        ((AbstractDocument) messageArea.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override public void replace(FilterBypass fb, int off, int len, String text, AttributeSet a) throws BadLocationException {
                int newLen = fb.getDocument().getLength() - len + (text == null ? 0 : text.length());
                if (newLen <= 500) super.replace(fb, off, len, text, a);
            }
            @Override public void insertString(FilterBypass fb, int off, String text, AttributeSet a) throws BadLocationException {
                if (fb.getDocument().getLength() + text.length() <= 500) super.insertString(fb, off, text, a);
            }
        });
        messageArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            void upd() { counter.setText(messageArea.getDocument().getLength() + "/500"); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { upd(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { upd(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { upd(); }
        });
        JScrollPane msgScroll = Theme.scroll(messageArea);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy = 0; c.gridx = 0; c.weightx = 1; c.insets = new Insets(0, 0, 4, 6); form.add(req("Full Name"), c);
        c.gridx = 1; c.insets = new Insets(0, 6, 4, 0); form.add(req("Email Address"), c);
        c.gridy = 1; c.gridx = 0; c.insets = new Insets(0, 0, 0, 6); form.add(nameField, c);
        c.gridx = 1; c.insets = new Insets(0, 6, 0, 0); form.add(emailField, c);
        c.gridy = 2; c.gridx = 0; c.gridwidth = 2; c.insets = new Insets(10, 0, 4, 0); form.add(req("Subject"), c);
        c.gridy = 3; c.insets = new Insets(0, 0, 0, 0); form.add(subjectBox, c);
        c.gridy = 4; c.insets = new Insets(10, 0, 4, 0); form.add(req("Message"), c);
        c.gridy = 5; c.insets = new Insets(0, 0, 0, 0); c.weighty = 1; c.fill = GridBagConstraints.BOTH; form.add(msgScroll, c);
        c.gridy = 6; c.weighty = 0; c.fill = GridBagConstraints.HORIZONTAL; c.insets = new Insets(3, 0, 0, 2);
        JPanel cw = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        cw.setOpaque(false);
        cw.add(counter);
        form.add(cw, c);
        Theme.RoundButton send = new Theme.RoundButton("Send Message", Theme.PURPLE, false).icon("send");
        send.addActionListener(e -> sendMessage());
        JPanel sw = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        sw.setOpaque(false);
        sw.add(send);
        c.gridy = 7; c.insets = new Insets(6, 0, 0, 0);
        form.add(sw, c);
        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private static JComponent req(String text) {
        return Theme.label("<html>" + text + " <font color='#D6336C'>*</font></html>", Font.BOLD, 12f, Theme.TEXT);
    }

    private void sendMessage() {
        String name = nameField.getText().trim(), email = emailField.getText().trim(), msg = messageArea.getText().trim();
        if (name.isEmpty()) { err("Please enter your full name.", nameField); return; }
        if (!EMAIL.matcher(email).matches()) { err("Please enter a valid email address (e.g. name@example.com).", emailField); return; }
        if (subjectBox.getSelectedIndex() <= 0) { err("Please select a subject.", subjectBox); return; }
        if (msg.length() < 10) { err("Please write a message of at least 10 characters.", messageArea); return; }

        File f = new File("contact_messages.csv");
        boolean fresh = !f.exists();
        try (PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f, true), StandardCharsets.UTF_8))) {
            if (fresh) w.println("timestamp,name,email,subject,message");
            w.println(q(LocalDateTime.now().withNano(0).toString()) + "," + q(name) + "," + q(email) + "," + q((String) subjectBox.getSelectedItem()) + "," + q(msg));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Your message could not be saved: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(this, "Thank you, " + name + "! Your message has been recorded in contact_messages.csv\nfor the officers to review.",
                "Message sent", JOptionPane.INFORMATION_MESSAGE);
        nameField.setText(""); emailField.setText(""); subjectBox.setSelectedIndex(0); messageArea.setText("");
    }

    private static String q(String s) { return "\"" + s.replace("\"", "\"\"").replace("\n", " ") + "\""; }

    private void err(String msg, JComponent focus) {
        JOptionPane.showMessageDialog(this, msg, "Please check the form", JOptionPane.WARNING_MESSAGE);
        focus.requestFocusInWindow();
    }

    // ---------------------------------------------------------- right column
    private JPanel buildHours() {
        Theme.Card card = new Theme.Card(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(14, 16, 16, 16));
        JPanel t = new JPanel(new BorderLayout(12, 0));
        t.setOpaque(false);
        t.add(Theme.iconCircle("clock", 40), BorderLayout.WEST);
        JPanel tt = new JPanel();
        tt.setOpaque(false);
        tt.setLayout(new BoxLayout(tt, BoxLayout.Y_AXIS));
        tt.add(Theme.label("Office Hours", Font.BOLD, 16f, Theme.TEXT));
        tt.add(Theme.label("We are available during the following hours:", Font.PLAIN, 12f, Theme.MUTED));
        t.add(tt, BorderLayout.CENTER);
        card.add(t, BorderLayout.NORTH);
        JPanel rows = new JPanel(new GridLayout(OrgInfo.HOURS.size(), 1)) {
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                Theme.antialias(g);
                Shape s = new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 12, 12);
                g.setColor(new Color(0xFBFAFF));
                g.fill(s);
                g.setColor(Theme.BORDER);
                g.draw(s);
                g.dispose();
            }
        };
        rows.setOpaque(false);
        int i = 0;
        for (String[] h : OrgInfo.HOURS) {
            JPanel r = new JPanel(new BorderLayout());
            r.setOpaque(false);
            r.setBorder(new CompoundBorder(new MatteBorder(0, 0, ++i < OrgInfo.HOURS.size() ? 1 : 0, 0, new Color(0xF0EDFA)), new EmptyBorder(0, 14, 0, 14)));
            r.add(Theme.label(h[0], i == 1 ? Font.BOLD : Font.PLAIN, 12.5f, Theme.TEXT), BorderLayout.WEST);
            r.add(Theme.label(h[1], Font.PLAIN, 12.5f, Theme.MUTED), BorderLayout.EAST);
            rows.add(r);
        }
        card.add(rows, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildFollow() {
        Theme.Card card = new Theme.Card(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));
        JPanel t = new JPanel(new BorderLayout(12, 0));
        t.setOpaque(false);
        t.add(Theme.iconCircle("share", 40), BorderLayout.WEST);
        JPanel tt = new JPanel();
        tt.setOpaque(false);
        tt.setLayout(new BoxLayout(tt, BoxLayout.Y_AXIS));
        tt.add(Theme.label("Follow Us", Font.BOLD, 16f, Theme.TEXT));
        tt.add(Theme.label("Stay connected for updates.", Font.PLAIN, 12f, Theme.MUTED));
        t.add(tt, BorderLayout.CENTER);
        card.add(t, BorderLayout.NORTH);
        JPanel icons = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        icons.setOpaque(false);
        icons.add(social("fb", "Facebook", OrgInfo.FACEBOOK));
        icons.add(social("xx", "X", OrgInfo.X));
        icons.add(social("ig", "Instagram", OrgInfo.INSTAGRAM));
        icons.add(social("yt", "YouTube", OrgInfo.YOUTUBE));
        JComponent mail = social("mail", "Email", "");
        mail.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { mailTo(OrgInfo.EMAIL); } });
        icons.add(mail);
        card.add(icons, BorderLayout.CENTER);
        return card;
    }

    private JComponent social(String icon, String name, String url) {
        JComponent c = new JComponent() {
            { setPreferredSize(new Dimension(36, 36)); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); setToolTipText(name); }
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                Theme.antialias(g);
                g.setPaint(new GradientPaint(0, 0, Theme.PURPLE_LIGHT, 36, 36, Theme.PURPLE_DARK));
                g.fill(new Ellipse2D.Double(0, 0, 36, 36));
                Icons.paint(g, icon, 9, 9, 18, Color.WHITE);
                g.dispose();
            }
        };
        if (!icon.equals("mail"))
            c.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (url.isBlank()) JOptionPane.showMessageDialog(ContactPage.this, "No " + name + " link has been set yet.\nAdd it in OrgInfo.java.", name, JOptionPane.INFORMATION_MESSAGE);
                    else openUri(ContactPage.this, url);
                }
            });
        return c;
    }

    private JPanel buildFaq() {
        Theme.Card card = new Theme.Card(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));
        JPanel t = new JPanel(new BorderLayout(12, 0));
        t.setOpaque(false);
        t.add(Theme.iconCircle("help", 40), BorderLayout.WEST);
        JPanel tt = new JPanel();
        tt.setOpaque(false);
        tt.setLayout(new BoxLayout(tt, BoxLayout.Y_AXIS));
        tt.add(Theme.label("Frequently Asked Questions", Font.BOLD, 15f, Theme.TEXT));
        tt.add(Theme.label("Find quick answers to common questions.", Font.PLAIN, 12f, Theme.MUTED));
        t.add(tt, BorderLayout.CENTER);
        card.add(t, BorderLayout.NORTH);
        faqList.setOpaque(false);
        faqList.setLayout(new BoxLayout(faqList, BoxLayout.Y_AXIS));
        card.add(faqList, BorderLayout.CENTER);
        buildFaqList();
        return card;
    }

    private void buildFaqList() {
        faqList.removeAll();
        for (int i = 0; i < OrgInfo.FAQS.size(); i++) {
            final int idx = i;
            String[] qa = OrgInfo.FAQS.get(i);
            JPanel row = new JPanel(new BorderLayout(8, 0)) {
                @Override protected void paintComponent(Graphics g0) {
                    Graphics2D g = (Graphics2D) g0.create();
                    Theme.antialias(g);
                    Shape s = new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 10, 10);
                    g.setColor(new Color(0xFBFAFF));
                    g.fill(s);
                    g.setColor(Theme.BORDER);
                    g.draw(s);
                    g.dispose();
                }
            };
            row.setOpaque(false);
            row.setBorder(new EmptyBorder(0, 12, 0, 10));
            row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            row.add(Theme.label(qa[0], Font.PLAIN, 12f, Theme.TEXT), BorderLayout.CENTER);
            row.add(new JLabel(Icons.of(idx == openFaq ? "chevDown" : "chevRight", 12, Theme.TEXT)), BorderLayout.EAST);
            row.setPreferredSize(new Dimension(100, 34));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            row.setAlignmentX(LEFT_ALIGNMENT);
            row.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { openFaq = openFaq == idx ? -1 : idx; buildFaqList(); }
            });
            faqList.add(row);
            if (idx == openFaq) {
                JTextArea a = new JTextArea(qa[1]);
                a.setLineWrap(true); a.setWrapStyleWord(true); a.setEditable(false); a.setOpaque(false); a.setFocusable(false);
                a.setFont(Theme.font(Font.PLAIN, 11.5f)); a.setForeground(Theme.MUTED);
                a.setBorder(new EmptyBorder(4, 12, 4, 8));
                a.setAlignmentX(LEFT_ALIGNMENT);
                a.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
                faqList.add(a);
            }
            faqList.add(Box.createVerticalStrut(5));
        }
        faqList.revalidate();
        faqList.repaint();
    }

    // -------------------------------------------------------------------- CTA
    private JPanel buildCta() {
        JPanel bar = new JPanel(new BorderLayout(18, 0)) {
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                Theme.antialias(g);
                g.clip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight() - 3, 18, 18));
                g.setPaint(new GradientPaint(0, 0, new Color(0x1A1050), getWidth(), 0, new Color(0x4B2FC4)));
                g.fillRect(0, 0, getWidth(), getHeight());
                Art.circuits(g, getWidth() * 0.3, 0, getWidth() * 0.7, getHeight(), 41, 50, new Color(200, 180, 255));
                g.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(14, 26, 17, 26));
        JPanel iw = new JPanel(new GridBagLayout());
        iw.setOpaque(false);
        iw.add(Theme.iconCircle("headset", 58, new Color(0x7C5CEB), new Color(0x4A2FC9), Color.WHITE));
        bar.add(iw, BorderLayout.WEST);
        JPanel t = new JPanel();
        t.setOpaque(false);
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        t.add(Box.createVerticalGlue());
        t.add(Theme.label("Need more help?", Font.BOLD, 16f, Color.WHITE));
        t.add(Theme.label("Our support team is here to assist you with any concerns or inquiries.", Font.PLAIN, 12.5f, new Color(0xD8CFFA)));
        t.add(Box.createVerticalGlue());
        bar.add(t, BorderLayout.CENTER);
        Theme.RoundButton b = new Theme.RoundButton("Contact Support", Color.WHITE, Theme.Style.ON_DARK).icon("mail");
        b.addActionListener(e -> mailTo(OrgInfo.EMAIL));
        JPanel bw = new JPanel(new GridBagLayout());
        bw.setOpaque(false);
        bw.add(b);
        bar.add(bw, BorderLayout.EAST);
        return bar;
    }

    // ---------------------------------------------------------------- helpers
    private void mailTo(String address) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.MAIL)) {
                Desktop.getDesktop().mail(new URI("mailto:" + address));
                return;
            }
        } catch (Exception ignored) {}
        JOptionPane.showMessageDialog(this, "Email us at " + address, "Contact", JOptionPane.INFORMATION_MESSAGE);
    }

    static void openUri(Component parent, String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                return;
            }
        } catch (Exception ignored) {}
        JOptionPane.showMessageDialog(parent, "Open this link in your browser:\n" + url, "Link", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Text field that shows grey hint text while empty. */
    private static class Hint extends JTextField {
        private final String hint;
        Hint(String hint) { this.hint = hint; }
        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            if (getText().isEmpty()) {
                Graphics2D g = (Graphics2D) g0.create();
                Theme.antialias(g);
                g.setFont(getFont());
                g.setColor(new Color(0xA19DBA));
                Insets in = getInsets();
                g.drawString(hint, in.left, (getHeight() + g.getFontMetrics().getAscent() - g.getFontMetrics().getDescent()) / 2);
                g.dispose();
            }
        }
    }
}

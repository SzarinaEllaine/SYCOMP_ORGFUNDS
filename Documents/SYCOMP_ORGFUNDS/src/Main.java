import javax.swing.*;
import java.awt.Font;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Launcher. Data lives in an ArrayList and is auto-saved to orgfunds_data.csv. */
public class Main {
    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception ignored) {}
            UIManager.put("OptionPane.messageFont", Theme.font(Font.PLAIN, 14f));
            UIManager.put("OptionPane.buttonFont", Theme.font(Font.BOLD, 13f));
            UIManager.put("Panel.background", Theme.BG);
            UIManager.put("Label.foreground", Theme.TEXT);
            UIManager.put("Menu.background", new java.awt.Color(0x18142C));
            UIManager.put("Menu.foreground", Theme.TEXT);
            UIManager.put("MenuItem.background", new java.awt.Color(0x18142C));
            UIManager.put("MenuItem.foreground", Theme.TEXT);
            UIManager.put("PopupMenu.background", new java.awt.Color(0x18142C));
            UIManager.put("OptionPane.background", Theme.BG);
            UIManager.put("OptionPane.messageForeground", Theme.TEXT);
            UIManager.put("ToolTip.background", new java.awt.Color(0x211C39));
            UIManager.put("ToolTip.foreground", Theme.TEXT);
            UIManager.put("ToolTip.font", Theme.font(Font.PLAIN, 12f));

            FinanceManager manager = new FinanceManager(new File("orgfunds_data.csv"));
            if (manager.storageExists()) manager.load();
            else seedSampleData(manager);
            ReportCatalog catalog = new ReportCatalog(manager, new Prefs(new File("orgfunds_settings.properties")));

            new DashboardFrame(manager, catalog).setVisible(true);
        });
    }

    /** First-run sample data, placed inside the current and previous semester so every screen has something to show. */
    private static void seedSampleData(FinanceManager m) {
        SchoolPeriod cur = SchoolPeriod.current();
        LocalDate prev = cur.previousRegular().start();
        LocalDate now = cur.start();
        LocalDate today = LocalDate.now();
        m.addTransaction(new Income("TX-001", prev.plusDays(5), "Membership fees (2nd semester)", "Membership Fees", new BigDecimal("12000")));
        m.addTransaction(new Expense("TX-002", prev.plusDays(20), "Hackathon prizes", "Events", new BigDecimal("6000")));
        m.addTransaction(new Income("TX-003", prev.plusDays(45), "Booth sales", "Fundraising", new BigDecimal("7500")));
        m.addTransaction(new Expense("TX-004", prev.plusDays(60), "Tech workshop supplies", "Supplies", new BigDecimal("2100")));
        Object[][] cs = {
                {3, true, "Membership fees (1st semester)", "Membership Fees", "18750"},
                {8, false, "Orientation snacks and materials", "Events", "3200"},
                {18, true, "Tech Talk registration fees", "Fundraising", "6400"},
                {24, false, "Poster printing", "Supplies", "850"},
                {33, true, "Alumni donation", "Donations", "5000"},
                {41, false, "Coding Bootcamp venue and food", "Events", "7800"},
                {52, true, "Hoodie sales proceeds", "Fundraising", "14500"},
                {60, false, "Hoodie production cost", "Projects", "9800"},
                {70, false, "Foundation week booth", "Events", "4200"},
                {78, true, "Sponsorship from a local IT firm", "Donations", "8000"},
                {86, false, "General assembly refreshments", "Meetings", "2400"},
                {92, false, "Domain and hosting renewal", "Projects", "1800"}};
        int n = 5;
        for (Object[] c : cs) {
            LocalDate d = now.plusDays((int) c[0]);
            if (d.isAfter(today)) d = today;
            String id = String.format("TX-%03d", n++);
            m.addTransaction((boolean) c[1]
                    ? new Income(id, d, (String) c[2], (String) c[3], new BigDecimal((String) c[4]))
                    : new Expense(id, d, (String) c[2], (String) c[3], new BigDecimal((String) c[4])));
        }
    }
}

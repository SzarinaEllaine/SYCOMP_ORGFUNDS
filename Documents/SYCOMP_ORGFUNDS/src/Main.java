
import java.awt.Font;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.swing.*;

/** Main launcher for the SYCOMP Financial Management System. */
public class Main {

    public static void main(String[] args) {

        System.setProperty("awt.useSystemAAFontSettings", "on");

        SwingUtilities.invokeLater(() -> {

            try {
                // Set application appearance
                try {
                    UIManager.setLookAndFeel(
                        UIManager.getCrossPlatformLookAndFeelClassName()
                    );
                } catch (Exception e) {
                    System.err.println(
                        "Could not set application appearance: "
                        + e.getMessage()
                    );
                }

                UIManager.put(
                    "OptionPane.messageFont",
                    Theme.font(Font.PLAIN, 14f)
                );

                UIManager.put(
                    "OptionPane.buttonFont",
                    Theme.font(Font.BOLD, 13f)
                );

                UIManager.put(
                    "ToolTip.font",
                    Theme.font(Font.PLAIN, 12f)
                );

                // Initialize financial manager
                File dataFile = new File("orgfunds_data.csv");

                FinanceManager manager =
                    new FinanceManager(dataFile);

                // Load saved transactions or create sample data
                if (manager.storageExists()) {
                    manager.load();
                } else {
                    seedSampleData(manager);
                }

                // Initialize reports
                ReportCatalog catalog = new ReportCatalog(
                    manager,
                    new Prefs(new File("orgfunds_settings.properties"))
                );

                // Open main application window
                DashboardFrame dashboard =
                    new DashboardFrame(manager, catalog);

                dashboard.setVisible(true);

            } catch (Exception e) {

                e.printStackTrace();

                JOptionPane.showMessageDialog(
                    null,
                    "The application could not start.\n\n"
                        + "Error: " + e.getMessage()
                        + "\n\nPlease restart the application "
                        + "or contact the system administrator.",
                    "Application Startup Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }

    /**
     * Creates sample transactions when the application
     * is launched for the first time.
     */
    private static void seedSampleData(FinanceManager manager) {

        SchoolPeriod current = SchoolPeriod.current();

        LocalDate previous = current.previousRegular().start();
        LocalDate now = current.start();
        LocalDate today = LocalDate.now();

        manager.addTransaction(
            new Income(
                "TX-001",
                previous.plusDays(5),
                "Membership fees (2nd semester)",
                "Membership Fees",
                new BigDecimal("12000")
            )
        );

        manager.addTransaction(
            new Expense(
                "TX-002",
                previous.plusDays(20),
                "Hackathon prizes",
                "Events",
                new BigDecimal("6000")
            )
        );

        manager.addTransaction(
            new Income(
                "TX-003",
                previous.plusDays(45),
                "Booth sales",
                "Fundraising",
                new BigDecimal("7500")
            )
        );

        manager.addTransaction(
            new Expense(
                "TX-004",
                previous.plusDays(60),
                "Tech workshop supplies",
                "Supplies",
                new BigDecimal("2100")
            )
        );

        Object[][] sampleData = {
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
            {92, false, "Domain and hosting renewal", "Projects", "1800"}
        };

        int number = 5;

        for (Object[] data : sampleData) {

            LocalDate date = now.plusDays((int) data[0]);

            if (date.isAfter(today)) {
                date = today;
            }

            String id = String.format("TX-%03d", number++);

            boolean isIncome = (boolean) data[1];

            String description = (String) data[2];
            String category = (String) data[3];

            BigDecimal amount =
                new BigDecimal((String) data[4]);

            FinancialTransaction transaction;

            if (isIncome) {

                transaction = new Income(
                    id, date, description, category, amount
                );

            } else {

                transaction = new Expense(
                    id, date, description, category, amount
                );
            }

            manager.addTransaction(transaction);
        }
    }
}

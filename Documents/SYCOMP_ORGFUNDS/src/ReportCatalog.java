import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

/** The list of reports the app can produce, and the code that builds each one from the transactions. */
public class ReportCatalog {

    public static class Report {
        public final String id, title, type, description;
        private boolean published;
        Report(String id, String title, String type, String description, boolean published) {
            this.id = id; this.title = title; this.type = type; this.description = description; this.published = published;
        }
        public boolean isPublished() { return published; }
        public String status() { return published ? "Published" : "Pending"; }
    }

    /** One generated report: KPI tiles, a table and footnotes. */
    public static class Content {
        public String title;
        public SchoolPeriod period;
        public final List<String[]> kpis = new ArrayList<>();   // label, value
        public String[] columns = {};
        public final List<String[]> rows = new ArrayList<>();
        public final List<String> notes = new ArrayList<>();
    }

    public static class Opened {
        public final Report report; public final SchoolPeriod period; public final LocalDate on;
        Opened(Report r, SchoolPeriod p) { report = r; period = p; on = LocalDate.now(); }
    }

    private final FinanceManager manager;
    private final Prefs prefs;
    private final List<Report> reports = new ArrayList<>();
    private final LinkedList<Opened> recent = new LinkedList<>();

    public ReportCatalog(FinanceManager manager, Prefs prefs) {
        this.manager = manager;
        this.prefs = prefs;
        add("RPT-001", "Statement of Collections and Expenses (SOCE)", "Financial", "Statement of collections and expenses for the selected period.", true);
        add("RPT-002", "Financial Summary", "Financial", "Monthly collections, expenses and balance.", true);
        add("RPT-003", "Expenses Breakdown", "Financial", "Every expense with its category and amount.", true);
        add("RPT-004", "Collections Breakdown", "Financial", "Every collection with its source category.", true);
        add("RPT-005", "Membership Report", "Membership", "Membership fee collections.", true);
        add("RPT-006", "Activity Summary", "Activity", "Collections and spending on events, projects and fundraisers.", false);
        add("RPT-007", "Liquidation Report", "Financial", "Breakdown of expenses with a running total.", true);
        add("RPT-008", "Budget Utilization", "Financial", "Share of collections used, per category.", false);
        add("RPT-009", "Transaction Ledger", "Financial", "Complete record list showing each Income / Expense detail.", true);
    }

    private void add(String id, String title, String type, String desc, boolean defaultPublished) {
        boolean pub = "Published".equals(prefs.get("status." + id, defaultPublished ? "Published" : "Pending"));
        reports.add(new Report(id, title, type, desc, pub));
    }

    public List<Report> all() { return Collections.unmodifiableList(reports); }
    public int publishedCount() { int n = 0; for (Report r : reports) if (r.published) n++; return n; }
    public int downloads() { return prefs.getInt("downloads", 0); }
    public void addDownload() { prefs.set("downloads", String.valueOf(downloads() + 1)); }
    public List<Opened> recent() { return recent; }

    public void setPublished(Report r, boolean published) {
        r.published = published;
        prefs.set("status." + r.id, r.status());
    }

    public void markOpened(Report r, SchoolPeriod p) {
        recent.removeIf(o -> o.report == r && o.period.equals(p));
        recent.addFirst(new Opened(r, p));
        while (recent.size() > 4) recent.removeLast();
    }

    // ------------------------------------------------------------- building
    private static String money(BigDecimal v) { return Theme.peso(v); }

    private static String pct(BigDecimal part, BigDecimal whole) {
        if (whole.signum() == 0) return "0.0%";
        return part.multiply(BigDecimal.valueOf(100)).divide(whole, 1, RoundingMode.HALF_UP) + "%";
    }

    public Content build(Report r, SchoolPeriod p) {
        List<FinancialTransaction> all = new ArrayList<>(manager.inPeriod(p));
        all.sort(Comparator.comparing(FinancialTransaction::getDate).thenComparing(FinancialTransaction::getId));
        List<FinancialTransaction> inc = new ArrayList<>(), exp = new ArrayList<>();
        for (FinancialTransaction t : all) (t instanceof Income ? inc : exp).add(t);
        BigDecimal ti = FinanceManager.incomeOf(all), te = FinanceManager.expensesOf(all), bal = ti.subtract(te);

        Content c = new Content();
        c.title = r.title;
        c.period = p;
        switch (r.id) {
            case "RPT-001" -> {
                kpi(c, "Total Collections", money(ti)); kpi(c, "Total Expenses", money(te)); kpi(c, "Remaining Balance", money(bal));
                c.columns = new String[]{"Category", "Collections", "Expenses", "Net"};
                for (Map.Entry<String, BigDecimal[]> e : FinanceManager.totalsByCategory(all).entrySet())
                    c.rows.add(new String[]{e.getKey(), money(e.getValue()[0]), money(e.getValue()[1]), money(e.getValue()[0].subtract(e.getValue()[1]))});
                c.notes.add("Remaining Balance = Total Collections − Total Expenses = " + money(ti) + " − " + money(te) + " = " + money(bal));
            }
            case "RPT-002" -> {
                kpi(c, "Total Collections", money(ti)); kpi(c, "Total Expenses", money(te)); kpi(c, "Remaining Balance", money(bal));
                kpi(c, "Transactions", String.valueOf(all.size()));
                c.columns = new String[]{"Month", "Collections", "Expenses", "Net"};
                for (YearMonth ym : p.months()) {
                    BigDecimal mi = BigDecimal.ZERO, me = BigDecimal.ZERO;
                    for (FinancialTransaction t : all) if (YearMonth.from(t.getDate()).equals(ym)) {
                        if (t instanceof Income) mi = mi.add(t.getAmount()); else me = me.add(t.getAmount());
                    }
                    c.rows.add(new String[]{ym.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + ym.getYear(), money(mi), money(me), money(mi.subtract(me))});
                }
            }
            case "RPT-003", "RPT-007" -> {
                kpi(c, "Total Expenses", money(te)); kpi(c, "Expense Records", String.valueOf(exp.size()));
                BigDecimal big = BigDecimal.ZERO;
                for (FinancialTransaction t : exp) if (t.getAmount().compareTo(big) > 0) big = t.getAmount();
                kpi(c, "Largest Expense", money(big));
                boolean liq = r.id.equals("RPT-007");
                c.columns = liq ? new String[]{"Date", "Description", "Category", "Amount", "Running Total"}
                                : new String[]{"ID", "Date", "Description", "Category", "Amount"};
                BigDecimal run = BigDecimal.ZERO;
                for (FinancialTransaction t : exp) {
                    run = run.add(t.getAmount());
                    c.rows.add(liq ? new String[]{t.getDate().toString(), t.getDescription(), t.getCategory(), money(t.getAmount()), money(run)}
                                   : new String[]{t.getId(), t.getDate().toString(), t.getDescription(), t.getCategory(), money(t.getAmount())});
                }
            }
            case "RPT-004" -> {
                kpi(c, "Total Collections", money(ti)); kpi(c, "Collection Records", String.valueOf(inc.size()));
                c.columns = new String[]{"ID", "Date", "Description", "Category", "Amount"};
                for (FinancialTransaction t : inc) c.rows.add(new String[]{t.getId(), t.getDate().toString(), t.getDescription(), t.getCategory(), money(t.getAmount())});
            }
            case "RPT-005" -> {
                BigDecimal sum = BigDecimal.ZERO; int n = 0;
                c.columns = new String[]{"ID", "Date", "Description", "Amount"};
                for (FinancialTransaction t : inc) if (t.getCategory().equalsIgnoreCase("Membership Fees")) {
                    sum = sum.add(t.getAmount()); n++;
                    c.rows.add(new String[]{t.getId(), t.getDate().toString(), t.getDescription(), money(t.getAmount())});
                }
                kpi(c, "Membership Fees Collected", money(sum)); kpi(c, "Payments Recorded", String.valueOf(n));
                kpi(c, "Share of Collections", pct(sum, ti));
                c.notes.add("Includes income recorded under the category \"Membership Fees\".");
            }
            case "RPT-006" -> {
                Set<String> acts = Set.of("events", "projects", "fundraising");
                BigDecimal ai = BigDecimal.ZERO, ae = BigDecimal.ZERO; int n = 0;
                c.columns = new String[]{"ID", "Date", "Activity", "Category", "Type", "Amount"};
                for (FinancialTransaction t : all) if (acts.contains(t.getCategory().toLowerCase())) {
                    n++;
                    if (t instanceof Income) ai = ai.add(t.getAmount()); else ae = ae.add(t.getAmount());
                    c.rows.add(new String[]{t.getId(), t.getDate().toString(), t.getDescription(), t.getCategory(), t.getType(), money(t.getAmount())});
                }
                kpi(c, "Activity Records", String.valueOf(n)); kpi(c, "Activity Collections", money(ai)); kpi(c, "Activity Spending", money(ae));
                c.notes.add("Includes the categories Events, Projects and Fundraising.");
            }
            case "RPT-008" -> {
                kpi(c, "Total Collections", money(ti)); kpi(c, "Total Expenses", money(te)); kpi(c, "Budget Used", pct(te, ti));
                c.columns = new String[]{"Category", "Spent", "% of Collections", "% of All Spending"};
                for (Map.Entry<String, BigDecimal[]> e : FinanceManager.totalsByCategory(all).entrySet())
                    if (e.getValue()[1].signum() > 0)
                        c.rows.add(new String[]{e.getKey(), money(e.getValue()[1]), pct(e.getValue()[1], ti), pct(e.getValue()[1], te)});
                c.notes.add("Budget Used = Total Expenses ÷ Total Collections.");
            }
            default -> { // ledger: runtime polymorphism, each object supplies its own getDetails()
                kpi(c, "Records", String.valueOf(all.size())); kpi(c, "Total Collections", money(ti)); kpi(c, "Total Expenses", money(te));
                c.columns = new String[]{"Details"};
                for (FinancialTransaction t : all) c.rows.add(new String[]{t.getDetails()});
            }
        }
        if (c.rows.isEmpty()) c.notes.add("No records were found for " + p.coveredLabel() + ".");
        return c;
    }

    private static void kpi(Content c, String label, String value) { c.kpis.add(new String[]{label, value}); }

    /** Ledger rows for the whole filtered period, used by the page-level Export button. */
    public Content ledger(SchoolPeriod p) { return build(reports.get(reports.size() - 1), p); }
}

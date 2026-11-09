import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

/** Stores transactions in an ArrayList, performs CRUD + calculations, and saves/loads a CSV file. */
public class FinanceManager {
    private final ArrayList<FinancialTransaction> transactions = new ArrayList<>();
    private final List<Runnable> listeners = new ArrayList<>();
    private final File storage;

    public FinanceManager(File storage) { this.storage = storage; }

    public void addChangeListener(Runnable r) { listeners.add(r); }

    private void changed() {
        save();
        for (Runnable r : listeners) r.run();
    }

    // ---------- CRUD ----------
    public void addTransaction(FinancialTransaction t) {
        if (findById(t.getId()) != null)
            throw new IllegalArgumentException("Transaction ID \"" + t.getId() + "\" already exists.");
        transactions.add(t);
        changed();
    }

    public FinancialTransaction findById(String id) {
        for (FinancialTransaction t : transactions)
            if (t.getId().equalsIgnoreCase(id.trim())) return t;
        return null;
    }

    /** Case-insensitive match on transaction ID or description. Blank keyword returns everything. */
    public List<FinancialTransaction> searchTransaction(String keyword) {
        String k = keyword == null ? "" : keyword.trim().toLowerCase();
        List<FinancialTransaction> out = new ArrayList<>();
        for (FinancialTransaction t : transactions)
            if (k.isEmpty() || t.getId().toLowerCase().contains(k) || t.getDescription().toLowerCase().contains(k))
                out.add(t);
        return out;
    }

    /** Replaces the record that has this ID (the type may change from Income to Expense). */
    public void updateTransaction(String id, FinancialTransaction replacement) {
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getId().equalsIgnoreCase(id)) {
                transactions.set(i, replacement);
                changed();
                return;
            }
        }
        throw new IllegalArgumentException("Transaction \"" + id + "\" was not found.");
    }

    public boolean deleteTransaction(String id) {
        boolean removed = transactions.removeIf(t -> t.getId().equalsIgnoreCase(id));
        if (removed) changed();
        return removed;
    }

    /** Read-only view so GUI code cannot modify the list directly. */
    public List<FinancialTransaction> getTransactions() { return Collections.unmodifiableList(transactions); }
    public int getCount() { return transactions.size(); }

    /** Newest first (ties broken by ID, descending). */
    public List<FinancialTransaction> newestFirst(List<FinancialTransaction> src) {
        List<FinancialTransaction> out = new ArrayList<>(src);
        out.sort(Comparator.comparing(FinancialTransaction::getDate).thenComparing(FinancialTransaction::getId).reversed());
        return out;
    }

    public List<FinancialTransaction> inPeriod(SchoolPeriod p) {
        List<FinancialTransaction> out = new ArrayList<>();
        for (FinancialTransaction t : transactions) if (p.contains(t.getDate())) out.add(t);
        return out;
    }

    /** School-year start years that have data, plus the current one, newest first. */
    public List<Integer> schoolYears() {
        TreeSet<Integer> set = new TreeSet<>(Comparator.reverseOrder());
        set.add(SchoolPeriod.current().startYear);
        set.add(SchoolPeriod.current().startYear - 1);
        for (FinancialTransaction t : transactions) set.add(SchoolPeriod.of(t.getDate()).startYear);
        return new ArrayList<>(set);
    }

    // ---------- calculations ----------
    public static BigDecimal incomeOf(Collection<? extends FinancialTransaction> list) {
        BigDecimal sum = BigDecimal.ZERO;
        for (FinancialTransaction t : list) if (t instanceof Income) sum = sum.add(t.getAmount());
        return sum;
    }

    public static BigDecimal expensesOf(Collection<? extends FinancialTransaction> list) {
        BigDecimal sum = BigDecimal.ZERO;
        for (FinancialTransaction t : list) if (t instanceof Expense) sum = sum.add(t.getAmount());
        return sum;
    }

    public BigDecimal calculateIncome() { return incomeOf(transactions); }
    public BigDecimal calculateExpenses() { return expensesOf(transactions); }
    public BigDecimal calculateBalance() { return calculateIncome().subtract(calculateExpenses()); }

    /** category -> {income, expense} */
    public static Map<String, BigDecimal[]> totalsByCategory(Collection<? extends FinancialTransaction> list) {
        Map<String, BigDecimal[]> map = new TreeMap<>();
        for (FinancialTransaction t : list) {
            BigDecimal[] v = map.computeIfAbsent(t.getCategory(), k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            if (t instanceof Income) v[0] = v[0].add(t.getAmount());
            else v[1] = v[1].add(t.getAmount());
        }
        return map;
    }

    public Map<String, BigDecimal[]> totalsByCategory() { return totalsByCategory(transactions); }

    public String nextSuggestedId() {
        int n = transactions.size() + 1;
        String id;
        do { id = String.format("TX-%03d", n++); } while (findById(id) != null);
        return id;
    }

    // ---------- CSV persistence ----------
    public boolean storageExists() { return storage.exists(); }

    public void save() {
        try (PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(storage), StandardCharsets.UTF_8))) {
            w.println("id,date,description,category,type,amount");
            for (FinancialTransaction t : transactions)
                w.println(q(t.getId()) + "," + t.getDate() + "," + q(t.getDescription()) + "," + q(t.getCategory())
                        + "," + t.getType() + "," + t.getAmount().toPlainString());
        } catch (IOException e) {
            System.err.println("Could not save data: " + e.getMessage());
        }
    }

    public void load() {
        if (!storage.exists()) return;
        transactions.clear();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(storage), StandardCharsets.UTF_8))) {
            String line = r.readLine(); // header
            while ((line = r.readLine()) != null) {
                if (line.isBlank()) continue;
                List<String> f = parseCsv(line);
                if (f.size() < 6) continue;
                try {
                    LocalDate d = LocalDate.parse(f.get(1));
                    BigDecimal a = new BigDecimal(f.get(5));
                    transactions.add("Income".equalsIgnoreCase(f.get(4))
                            ? new Income(f.get(0), d, f.get(2), f.get(3), a)
                            : new Expense(f.get(0), d, f.get(2), f.get(3), a));
                } catch (RuntimeException skip) { /* ignore a damaged row */ }
            }
        } catch (IOException e) {
            System.err.println("Could not load data: " + e.getMessage());
        }
    }

    private static String q(String s) { return "\"" + s.replace("\"", "\"\"") + "\""; }

    private static List<String> parseCsv(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQ = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQ) {
                if (c == '"' && i + 1 < line.length() && line.charAt(i + 1) == '"') { sb.append('"'); i++; }
                else if (c == '"') inQ = false;
                else sb.append(c);
            } else if (c == '"') inQ = true;
            else if (c == ',') { out.add(sb.toString()); sb.setLength(0); }
            else sb.append(c);
        }
        out.add(sb.toString());
        return out;
    }
}

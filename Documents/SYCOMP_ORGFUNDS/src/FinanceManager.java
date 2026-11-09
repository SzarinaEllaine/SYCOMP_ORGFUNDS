
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

/**
 * Manages financial transactions.
 * Handles CRUD operations, calculations, searching,
 * and CSV file storage.
 */
public class FinanceManager {

    private final ArrayList<FinancialTransaction> transactions = new ArrayList<>();
    private final List<Runnable> listeners = new ArrayList<>();
    private final File storage;

    public FinanceManager(File storage) {
        if (storage == null) {
            throw new IllegalArgumentException("Storage file is required.");
        }
        this.storage = storage;
    }

    // ---------- LISTENERS ----------

    public void addChangeListener(Runnable r) {
        if (r != null && !listeners.contains(r)) {
            listeners.add(r);
        }
    }

    private void changed() {
        save();

        for (Runnable r : new ArrayList<>(listeners)) {
            r.run();
        }
    }

    // ---------- CRUD OPERATIONS ----------

    public void addTransaction(FinancialTransaction t) {

        if (t == null) {
            throw new IllegalArgumentException("Transaction is required.");
        }

        if (findById(t.getId()) != null) {
            throw new IllegalArgumentException(
                "Transaction ID \"" + t.getId() + "\" already exists."
            );
        }

        transactions.add(t);
        changed();
    }

    public FinancialTransaction findById(String id) {

        if (id == null || id.trim().isEmpty()) {
            return null;
        }

        for (FinancialTransaction t : transactions) {
            if (t.getId().equalsIgnoreCase(id.trim())) {
                return t;
            }
        }

        return null;
    }

    public void updateTransaction(
        String id,
        FinancialTransaction replacement
    ) {

        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Transaction ID is required."
            );
        }

        if (replacement == null) {
            throw new IllegalArgumentException(
                "Updated transaction is required."
            );
        }

        String originalId = id.trim();

        if (findById(originalId) == null) {
            throw new IllegalArgumentException(
                "Transaction \"" + originalId + "\" was not found."
            );
        }

        FinancialTransaction existing = findById(originalId);

        if (!existing.getId().equalsIgnoreCase(replacement.getId())
                && findById(replacement.getId()) != null) {

            throw new IllegalArgumentException(
                "Transaction ID \"" + replacement.getId()
                + "\" already exists."
            );
        }

        for (int i = 0; i < transactions.size(); i++) {

            if (transactions.get(i).getId()
                    .equalsIgnoreCase(originalId)) {

                transactions.set(i, replacement);
                changed();
                return;
            }
        }
    }

    public boolean deleteTransaction(String id) {

        if (id == null || id.trim().isEmpty()) {
            return false;
        }

        boolean removed = transactions.removeIf(
            t -> t.getId().equalsIgnoreCase(id.trim())
        );

        if (removed) {
            changed();
        }

        return removed;
    }

    // ---------- TRANSACTION ACCESS ----------

    public List<FinancialTransaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public int getCount() {
        return transactions.size();
    }

    // ---------- SEARCH ----------

    public List<FinancialTransaction> searchTransaction(
        String keyword
    ) {

        String k = keyword == null
            ? ""
            : keyword.trim().toLowerCase(Locale.ROOT);

        List<FinancialTransaction> results = new ArrayList<>();

        for (FinancialTransaction t : transactions) {

            if (k.isEmpty()
                    || t.getId().toLowerCase(Locale.ROOT).contains(k)
                    || t.getDescription().toLowerCase(Locale.ROOT).contains(k)) {

                results.add(t);
            }
        }

        return results;
    }

    // ---------- SORTING ----------

    public List<FinancialTransaction> newestFirst(
        List<FinancialTransaction> src
    ) {

        List<FinancialTransaction> result =
            new ArrayList<>(src);

        result.sort(
            Comparator.comparing(FinancialTransaction::getDate)
                .thenComparing(FinancialTransaction::getId)
                .reversed()
        );

        return result;
    }

    // ---------- SCHOOL PERIOD ----------

    public List<FinancialTransaction> inPeriod(
        SchoolPeriod period
    ) {

        List<FinancialTransaction> result = new ArrayList<>();

        if (period == null) {
            return result;
        }

        for (FinancialTransaction t : transactions) {

            if (period.contains(t.getDate())) {
                result.add(t);
            }
        }

        return result;
    }

    public List<Integer> schoolYears() {

        TreeSet<Integer> years =
            new TreeSet<>(Comparator.reverseOrder());

        years.add(SchoolPeriod.current().startYear);
        years.add(SchoolPeriod.current().startYear - 1);

        for (FinancialTransaction t : transactions) {
            years.add(
                SchoolPeriod.of(t.getDate()).startYear
            );
        }

        return new ArrayList<>(years);
    }

    // ---------- FINANCIAL CALCULATIONS ----------

    public static BigDecimal incomeOf(
        Collection<? extends FinancialTransaction> list
    ) {

        BigDecimal total = BigDecimal.ZERO;

        for (FinancialTransaction t : list) {

            if (t instanceof Income) {
                total = total.add(t.getAmount());
            }
        }

        return total;
    }

    public static BigDecimal expensesOf(
        Collection<? extends FinancialTransaction> list
    ) {

        BigDecimal total = BigDecimal.ZERO;

        for (FinancialTransaction t : list) {

            if (t instanceof Expense) {
                total = total.add(t.getAmount());
            }
        }

        return total;
    }

    public BigDecimal calculateIncome() {
        return incomeOf(transactions);
    }

    public BigDecimal calculateExpenses() {
        return expensesOf(transactions);
    }

    public BigDecimal calculateBalance() {
        return calculateIncome().subtract(
            calculateExpenses()
        );
    }

    // ---------- CATEGORY TOTALS ----------

    public static Map<String, BigDecimal[]> totalsByCategory(
        Collection<? extends FinancialTransaction> list
    ) {

        Map<String, BigDecimal[]> totals = new TreeMap<>();

        for (FinancialTransaction t : list) {

            BigDecimal[] values = totals.computeIfAbsent(
                t.getCategory(),
                k -> new BigDecimal[] {
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
                }
            );

            if (t instanceof Income) {
                values[0] = values[0].add(t.getAmount());
            } else if (t instanceof Expense) {
                values[1] = values[1].add(t.getAmount());
            }
        }

        return totals;
    }

    public Map<String, BigDecimal[]> totalsByCategory() {
        return totalsByCategory(transactions);
    }

    // ---------- TRANSACTION ID GENERATOR ----------

    public String nextSuggestedId() {

        int number = transactions.size() + 1;
        String id;

        do {
            id = String.format("TX-%03d", number++);
        } while (findById(id) != null);

        return id;
    }

    // ---------- CSV FILE STORAGE ----------

    public boolean storageExists() {
        return storage.exists();
    }

    public void save() {

        try (PrintWriter writer = new PrintWriter(
            new OutputStreamWriter(
                new FileOutputStream(storage),
                StandardCharsets.UTF_8
            )
        )) {

            writer.println(
                "id,date,description,category,type,amount"
            );

            for (FinancialTransaction t : transactions) {

                writer.println(
                    q(t.getId()) + ","
                    + t.getDate() + ","
                    + q(t.getDescription()) + ","
                    + q(t.getCategory()) + ","
                    + t.getType() + ","
                    + t.getAmount().toPlainString()
                );
            }

        } catch (IOException e) {

            System.err.println(
                "Could not save data: " + e.getMessage()
            );
        }
    }

    public void load() {

        if (!storage.exists()) {
            return;
        }

        transactions.clear();

        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(
                new FileInputStream(storage),
                StandardCharsets.UTF_8
            )
        )) {

            reader.readLine(); // Skip CSV header

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.isBlank()) {
                    continue;
                }

                List<String> fields = parseCsv(line);

                if (fields.size() < 6) {
                    continue;
                }

                try {

                    String id = fields.get(0);
                    LocalDate date = LocalDate.parse(fields.get(1));
                    String description = fields.get(2);
                    String category = fields.get(3);
                    String type = fields.get(4);

                    BigDecimal amount =
                        new BigDecimal(fields.get(5));

                    FinancialTransaction transaction;

                    if ("Income".equalsIgnoreCase(type)) {

                        transaction = new Income(
                            id, date, description,
                            category, amount
                        );

                    } else if ("Expense".equalsIgnoreCase(type)) {

                        transaction = new Expense(
                            id, date, description,
                            category, amount
                        );

                    } else {
                        continue;
                    }

                    if (findById(id) == null) {
                        transactions.add(transaction);
                    }

                } catch (RuntimeException e) {

                    System.err.println(
                        "Skipped invalid transaction row: " + line
                    );
                }
            }

        } catch (IOException e) {

            System.err.println(
                "Could not load data: " + e.getMessage()
            );
        }
    }

    // ---------- CSV HELPERS ----------

    private static String q(String value) {

        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private static List<String> parseCsv(String line) {

        List<String> result = new ArrayList<>();

        StringBuilder value = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {

            char c = line.charAt(i);

            if (insideQuotes) {

                if (c == '"' && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {

                    value.append('"');
                    i++;

                } else if (c == '"') {

                    insideQuotes = false;

                } else {

                    value.append(c);
                }

            } else if (c == '"') {

                insideQuotes = true;

            } else if (c == ',') {

                result.add(value.toString());
                value.setLength(0);

            } else {

                value.append(c);
            }
        }

        result.add(value.toString());

        return result;
    }
}

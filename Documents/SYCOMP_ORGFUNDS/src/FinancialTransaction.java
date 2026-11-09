import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/** Superclass holding the data shared by every transaction. Fields are private (encapsulation). */
public class FinancialTransaction {
    private String id;
    private String description;
    private BigDecimal amount;
    private LocalDate date;
    private String category;

    public FinancialTransaction(String id, LocalDate date, String description, String category, BigDecimal amount) {
        setId(id);
        setDate(date);
        setDescription(description);
        setCategory(category);
        setAmount(amount);
    }

    public String getId() { return id; }
    public String getDescription() { return description; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getDate() { return date; }
    public String getCategory() { return category; }

    public void setId(String id) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("Transaction ID is required.");
        this.id = id.trim();
    }

    public void setDescription(String description) {
        if (description == null || description.trim().isEmpty())
            throw new IllegalArgumentException("Description is required.");
        this.description = description.trim();
    }

    public void setCategory(String category) {
        if (category == null || category.trim().isEmpty()) throw new IllegalArgumentException("Category is required.");
        this.category = category.trim();
    }

    public void setDate(LocalDate date) {
        if (date == null) throw new IllegalArgumentException("Date is required.");
        this.date = date;
    }

    /** Rejects zero and negative amounts. */
    public void setAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0)
            throw new IllegalArgumentException("Amount must be greater than zero.");
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    /** Label shown in the "Type" column; overridden by subclasses. */
    public String getType() { return "Transaction"; }

    /** Overridden by Income and Expense (method overriding / runtime polymorphism). */
    public String getDetails() {
        return "[TRANSACTION] " + id + " | " + description;
    }
}

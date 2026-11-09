import java.math.BigDecimal;
import java.time.LocalDate;

/** Money spent by the organization. */
public class Expense extends FinancialTransaction {
    public Expense(String id, LocalDate date, String description, String category, BigDecimal amount) {
        super(id, date, description, category, amount);
    }

    @Override
    public String getType() { return "Expense"; }

    @Override
    public String getDetails() {
        return "[EXPENSE] " + getId() + " | " + getDate() + " | " + getDescription()
                + " (" + getCategory() + ") spent -" + Theme.peso(getAmount());
    }
}

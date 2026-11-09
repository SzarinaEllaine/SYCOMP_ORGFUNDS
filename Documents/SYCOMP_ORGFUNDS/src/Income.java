import java.math.BigDecimal;
import java.time.LocalDate;

/** Money received by the organization. */
public class Income extends FinancialTransaction {
    public Income(String id, LocalDate date, String description, String category, BigDecimal amount) {
        super(id, date, description, category, amount);
    }

    @Override
    public String getType() { return "Income"; }

    @Override
    public String getDetails() {
        return "[INCOME]  " + getId() + " | " + getDate() + " | " + getDescription()
                + " (" + getCategory() + ") received +" + Theme.peso(getAmount());
    }
}

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * A school year (August to July) optionally narrowed to one semester.
 * semester: 0 = whole school year, 1 = 1st (Aug-Dec), 2 = 2nd (Jan-May), 3 = Summer (Jun-Jul).
 */
public final class SchoolPeriod {
    public final int startYear;
    public final int semester;

    public SchoolPeriod(int startYear, int semester) {
        this.startYear = startYear;
        this.semester = semester;
    }

    public static SchoolPeriod of(LocalDate d) {
        int m = d.getMonthValue();
        int sy = m >= 8 ? d.getYear() : d.getYear() - 1;
        int sem = m >= 8 ? 1 : (m <= 5 ? 2 : 3);
        return new SchoolPeriod(sy, sem);
    }

    public static SchoolPeriod current() { return of(LocalDate.now()); }

    public LocalDate start() {
        return switch (semester) {
            case 2 -> LocalDate.of(startYear + 1, 1, 1);
            case 3 -> LocalDate.of(startYear + 1, 6, 1);
            default -> LocalDate.of(startYear, 8, 1);
        };
    }

    public LocalDate end() {
        return switch (semester) {
            case 1 -> LocalDate.of(startYear, 12, 31);
            case 2 -> LocalDate.of(startYear + 1, 5, 31);
            default -> LocalDate.of(startYear + 1, 7, 31);
        };
    }

    public boolean contains(LocalDate d) { return !d.isBefore(start()) && !d.isAfter(end()); }

    public List<YearMonth> months() {
        List<YearMonth> out = new ArrayList<>();
        for (YearMonth ym = YearMonth.from(start()); !ym.isAfter(YearMonth.from(end())); ym = ym.plusMonths(1)) out.add(ym);
        return out;
    }

    /** The regular (non-summer) semester before this one, used for "vs. last semester". */
    public SchoolPeriod previousRegular() {
        return switch (semester) {
            case 1 -> new SchoolPeriod(startYear - 1, 2);
            case 2 -> new SchoolPeriod(startYear, 1);
            case 3 -> new SchoolPeriod(startYear, 2);
            default -> new SchoolPeriod(startYear - 1, 0);
        };
    }

    public String schoolYearLabel() { return startYear + " – " + (startYear + 1); }

    public String semesterLabel() {
        return switch (semester) { case 1 -> "1st Semester"; case 2 -> "2nd Semester"; case 3 -> "Summer"; default -> "Whole School Year"; };
    }

    public String coveredLabel() { return monthYear(start()) + " – " + monthYear(end()); }

    /** "Aug – Dec 2026" when both ends fall in the same year, otherwise the full label. */
    public String shortLabel() {
        LocalDate s = start(), e = end();
        if (s.getYear() != e.getYear()) return coveredLabel();
        return s.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " \u2013 "
                + e.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + e.getYear();
    }

    private static String monthYear(LocalDate d) {
        return d.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + d.getYear();
    }

    @Override public boolean equals(Object o) {
        return o instanceof SchoolPeriod p && p.startYear == startYear && p.semester == semester;
    }
    @Override public int hashCode() { return Objects.hash(startYear, semester); }
    @Override public String toString() { return "SY " + schoolYearLabel() + ", " + semesterLabel(); }
}

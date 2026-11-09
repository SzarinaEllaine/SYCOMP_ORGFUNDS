import java.util.List;

/**
 * Editable organization profile shown on the About and Contact pages.
 * Replace these sample values with your organization's real details.
 */
public final class OrgInfo {
    private OrgInfo() {}

    public static final String SHORT_NAME = "SYCOMP";
    public static final String FULL_NAME = "Society of Young Computer Professionals";
    public static final String INTRO = "SYCOMP is a community-driven organization of young and aspiring computer professionals dedicated to fostering growth, collaboration, and innovation in the field of technology.";
    public static final String MISSION = "To empower future tech leaders through knowledge, community, and innovation.";
    public static final String VISION = "A community of skilled, innovative, and socially responsible computer professionals.";
    public static final List<String> VALUES = List.of("Transparency", "Innovation", "Community", "Professionalism");
    public static final String OVERVIEW = "SYCOMP is a student organization under the College of Computer Studies, dedicated to building competent, compassionate, and socially responsible IT professionals.";

    public static final String SY = "SY 2025 – 2026";

    /** role, name, year/course */
    public static final List<String[]> BOARD = List.of(
            new String[]{"President", "Juan Dela Cruz", "BSCS 3rd Year"},
            new String[]{"Vice President", "Maria Santos", "BSCS 3rd Year"},
            new String[]{"Secretary", "Carlos Reyes", "BSCS 3rd Year"},
            new String[]{"Treasurer", "Ana Lopez", "BSCS 3rd Year"},
            new String[]{"Auditor", "Rafael Garcia", "BSCS 3rd Year"});

    /** value, label, note */
    public static final List<String[]> STATS = List.of(
            new String[]{"125+", "Total Members", "Active and growing"},
            new String[]{"8", "Executive Boards", "Since the organization's founding"},
            new String[]{"15+", "Events & Activities", "Per academic year"},
            new String[]{"4", "Core Committees", "Supporting the organization"});

    /** school year followed by President, Vice President, Secretary, Treasurer, Auditor names */
    public static final List<String[]> PAST_ADMINS = List.of(
            new String[]{"2024 – 2025", "Jessa Marie Santos", "Daniel Cruz", "Kristine Flores", "Miguel Reyes", "Patricia Lim"},
            new String[]{"2023 – 2024", "Lance Villanueva", "Angelica Ramos", "John Paolo Dela Cruz", "Samantha Lee", "Renz Santos"},
            new String[]{"2022 – 2023", "Mark Anthony Reyes", "Francesca Gonzales", "Earl Mendoza", "Bea Garcia", "Kevin Tolentino"},
            new String[]{"2021 – 2022", "Jasmine Cruz", "Pauline Santos", "Ivan Dela Torre", "Monica Flores", "Clarence Ramos"},
            new String[]{"2020 – 2021", "Karlo Santos", "Alyssa Mendoza", "Rafael Cruz", "Liza Fernandez", "Neil Garcia"});
    public static final String[] ROLES = {"President", "Vice President", "Secretary", "Treasurer", "Auditor"};

    public static final String EMAIL = "sycomp@e-svuc.edu.ph";
    public static final String PHONE = "+63 (42) 123 4567";
    public static final String[] ADDRESS = {"Computer Studies Department", "Main Campus, Building A", "SYCOMP Office"};
    public static final String MAP_URL = "https://www.google.com/maps/search/?api=1&query=SYCOMP+Office";

    /** day, hours */
    public static final List<String[]> HOURS = List.of(
            new String[]{"Monday – Friday", "8:00 AM – 5:00 PM"},
            new String[]{"Saturday", "8:00 AM – 12:00 PM"},
            new String[]{"Sunday", "Closed"});

    /** Social links. Leave a URL empty to show a "not set" message instead of opening a browser. */
    public static final String FACEBOOK = "";
    public static final String X = "";
    public static final String INSTAGRAM = "";
    public static final String YOUTUBE = "";

    /** question, answer */
    public static final List<String[]> FAQS = List.of(
            new String[]{"How can I get a copy of a financial report?", "Open the Reports page, click View Report on the report you need, then press Export CSV."},
            new String[]{"When are reports published?", "The treasurer publishes reports at the end of each semester. Reports that are not yet published are marked Pending."},
            new String[]{"How can I join the organization?", "Send us a message using the form on this page or visit the SYCOMP office during office hours."},
            new String[]{"Who can access the reports?", "Any student can view the published reports to keep the organization transparent and accountable."},
            new String[]{"How do I contact the officers?", "Use the contact form or email us at " + EMAIL + ". We usually reply within 1–2 business days."});
}

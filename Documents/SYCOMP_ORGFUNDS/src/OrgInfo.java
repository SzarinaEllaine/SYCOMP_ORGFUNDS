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

    public static final String SY = "SY 2026–2027";

    /** Official SYCOMP Administration 2026–2027: position, complete name (36 officers). */
    public static final List<String[]> BOARD = List.of(
            new String[]{"President", "One Marie R. Fortuna"},
            new String[]{"VP Internal", "Sharee Angela D. Cuevas"},
            new String[]{"VP External", "Rael Raphael F. Deloso"},
            new String[]{"Secretary", "Sarah Mae P. Hinayon"},
            new String[]{"Assistant Secretary", "Samuel A. Tipan"},
            new String[]{"Internal Liaison Officer", "Crizzabelle M. Alcala"},
            new String[]{"External Liaison Officer", "Laurence Jay Galon"},
            new String[]{"Treasurer", "Aleann Venice A. Achacoso"},
            new String[]{"Assistant Treasurer", "Szarina Ellaine T. Palaca"},
            new String[]{"Budget Officer", "El Niño Oliver B. Avelino"},
            new String[]{"Assistant Budget Officer", "Smith Jay S. Castil"},
            new String[]{"Auditor", "James Benedict Gumapac"},
            new String[]{"Assistant Auditor", "Roniel S. Minion"},
            new String[]{"Creative Lead", "Carla Jane Clarabal"},
            new String[]{"Creative Staff", "Nina Marie A. Medrina"},
            new String[]{"Creative Staff", "Cris M. Toring"},
            new String[]{"Creative Staff", "Jacklyn Mae G. Salvo"},
            new String[]{"Creative Staff", "Princess Danica C. Calamba"},
            new String[]{"Creative Staff", "Hannavie Mae A. Polinar"},
            new String[]{"Documentation Officer", "Lawrence D. Caguitla"},
            new String[]{"Documentation Officer", "Alexes James Cesar"},
            new String[]{"Technical Director", "Marchus O. Deligero"},
            new String[]{"Technical Staff", "Osiris Kedigadash Palac"},
            new String[]{"Technical Staff", "Michael James Melicor"},
            new String[]{"Technical Staff", "Ralph Symone R. Manlangit"},
            new String[]{"Public Information Officer", "Karl Niño H. Ranay"},
            new String[]{"Assistant Public Information Officer", "Lorie Anne L. Ave"},
            new String[]{"1st Year Representative", "Vanica Kate C. Cagas"},
            new String[]{"1st Year Representative", "Akuji Gracian S. Afable"},
            new String[]{"2nd Year Representative", "Mariah Claresse P. Ochea"},
            new String[]{"2nd Year Representative", "Eldren Nicko Nastor"},
            new String[]{"3rd Year Representative", "Abby Lauren G. Mante"},
            new String[]{"3rd Year Representative", "Angel B. Enriquez"},
            new String[]{"4th Year Representative", "Renato D. Jumang-it"},
            new String[]{"4th Year Representative", "Alexa Mae Sybille Palgan"});

    /** value, label, note */
    public static final List<String[]> STATS = List.of(
            new String[]{"125+", "Total Members", "Active and growing"},
            new String[]{"8", "Executive Boards", "Since the organization's founding"},
            new String[]{"15+", "Events & Activities", "Per academic year"},
            new String[]{"4", "Core Committees", "Supporting the organization"});

    /** School year followed by names in the order listed by ROLES. */
    public static final List<String[]> PAST_ADMINS = List.of(
            new String[]{"2025 – 2026", "Mia Therese Villanueva", "Gabriel Santos", "Ariana Dela Cruz", "Paolo Mendoza", "Cheska Lim", "Rafael Navarro", "Bianca Reyes", "Kyla Ramos", "Nathan Flores", "Sofia Castillo", "Ethan Garcia", "Lara Bautista", "Marco Aquino", "Janelle Mercado", "Luis Fernandez"},
            new String[]{"2024 – 2025", "Jessa Marie Santos", "Daniel Cruz", "Kristine Flores", "Miguel Reyes", "Patricia Lim", "Rafael Dizon", "Camille Navarro", "Lara Villanueva", "Paolo Mendoza", "Trisha Garcia", "Enzo Ramos", "Mikaela Bautista", "Kurt Salcedo", "Bianca Torres", "Adrian Flores"},
            new String[]{"2023 – 2024", "Lance Villanueva", "Angelica Ramos", "John Paolo Dela Cruz", "Samantha Lee", "Renz Santos", "Marco Villareal", "Janelle Aquino", "Joshua Lim", "Andrea Castillo", "Gabriel Pineda", "Chloe Manalo", "Nico Valdez", "Sofia Herrera", "Carlo Domingo", "Kyla Fernandez"},
            new String[]{"2022 – 2023", "Mark Anthony Reyes", "Francesca Gonzales", "Earl Mendoza", "Bea Garcia", "Kevin Tolentino", "Diana Javier", "Patrick Flores", "Alyssa Rivera", "Sean Cabrera", "Maureen Lopez", "Rico Salazar", "Ivy Santiago", "Andrei Cruz", "Nicole Ramos", "Nathaniel Ong"},
            new String[]{"2021 – 2022", "Jasmine Cruz", "Pauline Santos", "Ivan Dela Torre", "Monica Flores", "Clarence Ramos", "Angela Bautista", "Carlo Reyes", "Mara Villanueva", "Joaquin Garcia", "Leah Mendoza", "Francis Tan", "Trina Castillo", "Ethan Navarro", "Camille Dizon", "Gabrielle Lim"},
            new String[]{"2020 – 2021", "Karlo Santos", "Alyssa Mendoza", "Rafael Cruz", "Liza Fernandez", "Neil Garcia", "Patricia Aquino", "Jerome Valdez", "Katrina Domingo", "Miguel Salcedo", "Aira Pineda", "Vincent Herrera", "Shaina Mercado", "Noel Castillo", "Beatrice Lopez", "Daryl Santiago"});
    public static final String[] ROLES = {
            "President", "Vice President", "Secretary", "Treasurer", "Auditor",
            "Public Information Officer", "Assistant Public Information Officer",
            "1st Year Representative", "1st Year Representative",
            "2nd Year Representative", "2nd Year Representative",
            "3rd Year Representative", "3rd Year Representative",
            "4th Year Representative", "4th Year Representative"};

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
    public static final String FACEBOOK = "https://www.facebook.com/sycomp.hnu.org";
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

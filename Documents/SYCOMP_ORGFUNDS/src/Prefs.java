import java.io.*;
import java.util.Properties;

/** Small settings file (report publish status + download counter). */
public class Prefs {
    private final File file;
    private final Properties p = new Properties();

    public Prefs(File file) {
        this.file = file;
        if (file.exists()) {
            try (InputStream in = new FileInputStream(file)) { p.load(in); } catch (IOException ignored) {}
        }
    }

    public String get(String key, String def) { return p.getProperty(key, def); }

    public int getInt(String key, int def) {
        try { return Integer.parseInt(p.getProperty(key, String.valueOf(def))); } catch (NumberFormatException e) { return def; }
    }

    public void set(String key, String value) {
        p.setProperty(key, value);
        try (OutputStream out = new FileOutputStream(file)) { p.store(out, "OrgFunds settings"); } catch (IOException ignored) {}
    }
}

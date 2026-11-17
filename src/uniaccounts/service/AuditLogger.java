package uniaccounts.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pattern: SINGLETON -> role: Singleton (the one and only audit log).
 *
 * "Initialization-on-demand holder" idiom:
 *  - the instance is created lazily, on first use;
 *  - thread-safe thanks to JVM class loading (no synchronized in getInstance());
 *  - private constructor: nobody can create a second log.
 */
public final class AuditLogger {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final List<String> entries = new ArrayList<>();

    private AuditLogger() { }

    private static class Holder {
        private static final AuditLogger INSTANCE = new AuditLogger();
    }

    public static AuditLogger getInstance() {
        return Holder.INSTANCE;
    }

    public synchronized void log(String action, String details) {
        entries.add("[" + LocalDateTime.now().format(FMT) + "] " + action + " - " + details);
    }

    public synchronized List<String> getEntries() {
        return Collections.unmodifiableList(new ArrayList<>(entries));
    }
}

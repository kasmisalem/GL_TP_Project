package uniaccounts;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

import uniaccounts.model.StudentAccount;
import uniaccounts.model.StudentAccountBuilder;
import uniaccounts.service.AccountRepository;
import uniaccounts.service.AuditLogger;

/**
 * Interactive console client: YOU type the student data.
 *  - Builder   : add a student (option 1)
 *  - Prototype : create a class template (2), then clone it (3)
 *  - Singleton : one repository and one audit log (4, 5, 6)
 *
 * Rule: two students can never have the same id (checked as soon as you type the id).
 */
public class Main {

    private static final Scanner in = new Scanner(System.in);
    private static final AccountRepository repo = AccountRepository.getInstance(); // Singleton
    private static final AuditLogger audit = AuditLogger.getInstance();            // Singleton

    /** Pattern: PROTOTYPE -> the registry of ready-made templates (name -> prototype account). */
    private static final Map<String, StudentAccount> templates = new LinkedHashMap<>();

    public static void main(String[] args) {
        System.out.println("=== UniAccounts - Work 1 (Builder / Singleton / Prototype) ===");
        try {
            while (true) {
                System.out.println();
                System.out.println("1) Add a student                         (Builder)");
                System.out.println("2) Create a class template               (Prototype)");
                System.out.println("3) Create students from a template       (Prototype clone)");
                System.out.println("4) List all accounts");
                System.out.println("5) Show audit log");
                System.out.println("6) Check Singleton");
                System.out.println("0) Quit");
                switch (read("Your choice")) {
                    case "1": addStudent(); break;
                    case "2": createTemplate(); break;
                    case "3": cloneFromTemplate(); break;
                    case "4": listAccounts(); break;
                    case "5": showAudit(); break;
                    case "6": checkSingleton(); break;
                    case "0": System.out.println("Bye."); return;
                    default:  System.out.println("Unknown choice.");
                }
            }
        } catch (NoSuchElementException e) {
            System.out.println("\nInput closed. Bye.");
        }
    }

    // ------------------------------------------------------------------
    // 1) BUILDER
    // ------------------------------------------------------------------
    private static void addStudent() {
        String[] who = readIdentity();
        StudentAccountBuilder b = new StudentAccountBuilder(who[0], who[1], who[2]);

        String phone = read("Phone (optional, Enter to skip)");
        if (!phone.isEmpty()) b.phone(phone);

        String level = read("Level L1/L2/L3/M1/M2 (Enter = L1)").toUpperCase();
        if (!level.isEmpty()) b.level(level);

        String major = read("Major (optional)");
        if (!major.isEmpty()) b.major(major);

        b.scholarship(isYes(read("Scholarship? y/n (Enter = n)")));
        b.courses(splitCourses(read("Courses, separated by commas (optional)")));

        try {
            StudentAccount acc = b.build();
            repo.save(acc);
            System.out.println("Saved: " + acc);
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Not saved -> " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // 2) PROTOTYPE: build the template of a class (promotion)
    // ------------------------------------------------------------------
    private static void createTemplate() {
        String key = read("Template name (e.g. L1-INFO-2026)");
        if (key.isEmpty()) { System.out.println("Name required."); return; }

        // the template is a StudentAccount with a placeholder identity (never saved in the repository)
        StudentAccountBuilder b = new StudentAccountBuilder("TEMPLATE", "Template", "template@univ.dz");
        String level = read("Level L1/L2/L3/M1/M2 (Enter = L1)").toUpperCase();
        if (!level.isEmpty()) b.level(level);
        String major = read("Major");
        if (!major.isEmpty()) b.major(major);
        b.scholarship(isYes(read("Scholarship? y/n (Enter = n)")));
        b.courses(splitCourses(read("Courses, separated by commas")));

        try {
            StudentAccount template = b.build();
            templates.put(key, template);
            audit.log("TEMPLATE_CREATED", key);
            System.out.println("Template '" + key + "' created: " + template);
        } catch (IllegalArgumentException e) {
            System.out.println("Template not created -> " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // 3) PROTOTYPE: clone the template, change only the identity
    // ------------------------------------------------------------------
    private static void cloneFromTemplate() {
        if (templates.isEmpty()) {
            System.out.println("No template yet. Use option 2 first.");
            return;
        }
        System.out.println("Available templates: " + templates.keySet());
        StudentAccount template = templates.get(read("Template to use"));
        if (template == null) { System.out.println("Unknown template."); return; }

        int n = readInt("How many students");
        for (int i = 1; i <= n; i++) {
            System.out.println("--- Student " + i + " of " + n + " ---");
            String[] who = readIdentity();
            try {
                StudentAccount clone = template.copyWithIdentity(who[0], who[1], who[2]);
                repo.save(clone);
                System.out.println("Saved: " + clone);
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Not saved -> " + e.getMessage());
            }
        }
    }

    // ------------------------------------------------------------------
    // 4-6) Display
    // ------------------------------------------------------------------
    private static void listAccounts() {
        System.out.println("Accounts in the repository: " + repo.count());
        for (StudentAccount a : repo.findAll()) {
            System.out.println("  " + a.getId() + " | " + a.getFullName() + " | " + a.getEmail()
                    + " | " + a.getLevel() + " | " + a.getMajor()
                    + " | scholarship=" + a.hasScholarship() + " | courses=" + a.getCourses());
        }
    }

    private static void showAudit() {
        System.out.println("Audit log:");
        audit.getEntries().forEach(e -> System.out.println("  " + e));
    }

    private static void checkSingleton() {
        System.out.println("Same repository? " + (AccountRepository.getInstance() == repo));
        System.out.println("Same audit log?  " + (AuditLogger.getInstance() == audit));
    }

    // ------------------------------------------------------------------
    // Input helpers
    // ------------------------------------------------------------------

    /**
     * Asks for id, full name and email.
     * The id is checked immediately: it must be non-empty and NOT already used by another
     * student. Name and email are validated by the Builder (email needs @).
     */
    private static String[] readIdentity() {
        String id;
        while (true) {
            id = read("ID");
            if (id.isEmpty()) {
                System.out.println("ID is required.");
            } else if (repo.exists(id)) {
                audit.log("DUPLICATE_ID_ATTEMPT", id);
                System.out.println("ID " + id + " is already used by another student. Choose a different ID.");
            } else {
                break;
            }
        }
        while (true) {
            String name = read("Full name");
            String email = read("Email");
            try {
                new StudentAccountBuilder(id, name, email).build(); // validation only
                return new String[] { id, name, email };
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid -> " + e.getMessage() + ". Try again.");
            }
        }
    }

    private static String read(String label) {
        System.out.print(label + ": ");
        return in.nextLine().trim();
    }

    private static int readInt(String label) {
        while (true) {
            try {
                int n = Integer.parseInt(read(label));
                if (n > 0) return n;
            } catch (NumberFormatException ignored) { }
            System.out.println("Enter a positive number.");
        }
    }

    private static boolean isYes(String s) {
        return s.equalsIgnoreCase("y") || s.equalsIgnoreCase("yes");
    }

    private static List<String> splitCourses(String line) {
        List<String> list = new ArrayList<>();
        for (String c : line.split(",")) {
            if (!c.trim().isEmpty()) list.add(c.trim());
        }
        return list;
    }
}

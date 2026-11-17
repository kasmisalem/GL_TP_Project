package uniaccounts.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Pattern: BUILDER -> role: ConcreteBuilder.
 *
 * The three required fields (id, fullName, email) go in the constructor.
 * The five optional fields have one chained method each, with a default value.
 * build() checks the business rules: an invalid account can never exist.
 */
public class StudentAccountBuilder {

    private static final List<String> VALID_LEVELS = Arrays.asList("L1", "L2", "L3", "M1", "M2");

    // required
    private final String id;
    private final String fullName;
    private final String email;

    // optional, with defaults
    private String phone = null;
    private String level = "L1";
    private String major = null;
    private boolean scholarship = false;
    private List<String> courses = new ArrayList<>();

    public StudentAccountBuilder(String id, String fullName, String email) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
    }

    public StudentAccountBuilder phone(String phone)              { this.phone = phone; return this; }
    public StudentAccountBuilder level(String level)              { this.level = level; return this; }
    public StudentAccountBuilder major(String major)              { this.major = major; return this; }
    public StudentAccountBuilder scholarship(boolean scholarship) { this.scholarship = scholarship; return this; }
    public StudentAccountBuilder addCourse(String course)         { this.courses.add(course); return this; }

    public StudentAccountBuilder courses(List<String> courses) {
        this.courses = (courses == null) ? new ArrayList<>() : new ArrayList<>(courses);
        return this;
    }

    /** Validates the rules, then builds the account. */
    public StudentAccount build() {
        if (id == null || id.trim().isEmpty())
            throw new IllegalArgumentException("id is required and must not be empty");
        if (fullName == null || fullName.trim().isEmpty())
            throw new IllegalArgumentException("full name is required and must not be empty");
        if (email == null || !email.contains("@"))
            throw new IllegalArgumentException("invalid email (must contain @): " + email);
        if (level == null || !VALID_LEVELS.contains(level))
            throw new IllegalArgumentException("invalid level: " + level + " (allowed: " + VALID_LEVELS + ")");
        return new StudentAccount(id, fullName, email, phone, level, major, scholarship, courses);
    }
}

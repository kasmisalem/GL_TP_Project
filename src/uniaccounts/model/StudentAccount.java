package uniaccounts.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Student account.
 *
 * Pattern: BUILDER   -> role: Product (built only through StudentAccountBuilder).
 * Pattern: PROTOTYPE -> role: ConcretePrototype (implements Prototype, see bottom of this file).
 *
 * The constructor is package-private, so code outside this package cannot call
 * "new StudentAccount(...)" with eight arguments: it must use the Builder or a copy.
 */
public final class StudentAccount implements Prototype<StudentAccount> {

    private final String id;
    private final String fullName;
    private final String email;
    private final String phone;          // optional, default null
    private final String level;          // optional, default L1
    private final String major;          // optional
    private final boolean scholarship;   // optional, default false
    private final List<String> courses;  // optional, empty by default

    /** Used only by StudentAccountBuilder (same package). */
    StudentAccount(String id, String fullName, String email, String phone,
                   String level, String major, boolean scholarship, List<String> courses) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.level = level;
        this.major = major;
        this.scholarship = scholarship;
        this.courses = new ArrayList<>(courses); // the account owns its own list (defensive copy)
    }

    // ------------------------------------------------------------------
    // PROTOTYPE: deep copies
    // ------------------------------------------------------------------

    /** Exact deep copy (same identity). */
    @Override
    public StudentAccount copy() {
        return new StudentAccountBuilder(id, fullName, email)
                .phone(phone).level(level).major(major)
                .scholarship(scholarship).courses(courses).build();
    }

    /**
     * Deep copy that changes ONLY the identity (id, full name, email).
     * Level, major, scholarship and courses are kept. The phone is personal, so it
     * is not copied. Validation runs again through the Builder.
     */
    @Override
    public StudentAccount copyWithIdentity(String newId, String newFullName, String newEmail) {
        return new StudentAccountBuilder(newId, newFullName, newEmail)
                .level(level).major(major)
                .scholarship(scholarship).courses(courses).build();
    }

    // ------------------------------------------------------------------
    // Getters (the object is immutable)
    // ------------------------------------------------------------------

    public String getId()            { return id; }
    public String getFullName()      { return fullName; }
    public String getEmail()         { return email; }
    public String getPhone()         { return phone; }
    public String getLevel()         { return level; }
    public String getMajor()         { return major; }
    public boolean hasScholarship()  { return scholarship; }
    public List<String> getCourses() { return Collections.unmodifiableList(courses); }

    @Override
    public String toString() {
        return "StudentAccount{id=" + id + ", fullName=" + fullName + ", email=" + email
                + ", phone=" + phone + ", level=" + level + ", major=" + major
                + ", scholarship=" + scholarship + ", courses=" + courses + "}";
    }
}

/**
 * Pattern: PROTOTYPE -> role: Prototype (interface).
 * Package-private and kept in this file to reduce the number of files.
 */
interface Prototype<T> {
    T copy();
    T copyWithIdentity(String newId, String newFullName, String newEmail);
}

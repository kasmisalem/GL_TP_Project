package uniaccounts.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import uniaccounts.model.StudentAccount;

/**
 * Pattern: SINGLETON -> role: Singleton (the one and only central repository).
 *
 * Every part of the program gets the SAME repository through getInstance(), so no
 * account can get lost in another copy. Because there is a single repository, it can
 * guarantee that two students never share the same id.
 * Every operation is written to the AuditLogger (also a Singleton).
 */
public final class AccountRepository {

    private final Map<String, StudentAccount> accounts = new LinkedHashMap<>();

    private AccountRepository() { }

    private static class Holder {
        private static final AccountRepository INSTANCE = new AccountRepository();
    }

    public static AccountRepository getInstance() {
        return Holder.INSTANCE;
    }

    /** True if a student with this id is already stored. */
    public synchronized boolean exists(String id) {
        return accounts.containsKey(id);
    }

    /** Adds an account. Last line of defence: refuses an id that already exists. */
    public synchronized void save(StudentAccount account) {
        if (accounts.containsKey(account.getId())) {
            AuditLogger.getInstance().log("SAVE_REJECTED", "id already exists: " + account.getId());
            throw new IllegalStateException("id already used by another student: " + account.getId());
        }
        accounts.put(account.getId(), account);
        AuditLogger.getInstance().log("SAVE", "account " + account.getId() + " (" + account.getFullName() + ")");
    }

    public synchronized Optional<StudentAccount> findById(String id) {
        return Optional.ofNullable(accounts.get(id));
    }

    public synchronized Collection<StudentAccount> findAll() {
        return new ArrayList<>(accounts.values());
    }

    public synchronized int count() {
        return accounts.size();
    }
}

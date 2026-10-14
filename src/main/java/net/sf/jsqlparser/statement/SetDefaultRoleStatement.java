/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import net.sf.jsqlparser.statement.create.user.RoleSelection;
import net.sf.jsqlparser.statement.create.user.UserAccount;

/**
 * MySQL {@code SET DEFAULT ROLE {NONE | ALL | role, ...} TO user, ...}. MariaDB's
 * {@code SET DEFAULT ROLE {role | NONE} [FOR user]} is kept with {@link #isForKeyword()}; without
 * accounts the statement applies to the current user.
 */
public class SetDefaultRoleStatement implements Statement {

    private RoleSelection roles;
    private final List<UserAccount> accounts = new ArrayList<>();
    private boolean forKeyword;

    public RoleSelection getRoles() {
        return roles;
    }

    public void setRoles(RoleSelection roles) {
        this.roles = roles;
    }

    /** The accounts after {@code TO} or {@code FOR}; empty when omitted. */
    public List<UserAccount> getAccounts() {
        return Collections.unmodifiableList(accounts);
    }

    public void setAccounts(Collection<? extends UserAccount> accounts) {
        this.accounts.clear();
        if (accounts != null) {
            this.accounts.addAll(accounts);
        }
    }

    /** Whether the accounts follow MariaDB's {@code FOR} instead of {@code TO}. */
    public boolean isForKeyword() {
        return forKeyword;
    }

    public void setForKeyword(boolean forKeyword) {
        this.forKeyword = forKeyword;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("SET DEFAULT ROLE ").append(roles);
        if (!accounts.isEmpty()) {
            builder.append(forKeyword ? " FOR " : " TO ").append(
                    accounts.stream().map(Object::toString).collect(Collectors.joining(", ")));
        }
        return builder.toString();
    }

    public SetDefaultRoleStatement withRoles(RoleSelection roles) {
        setRoles(roles);
        return this;
    }

    public SetDefaultRoleStatement withAccounts(Collection<? extends UserAccount> accounts) {
        setAccounts(accounts);
        return this;
    }

    public SetDefaultRoleStatement addAccounts(UserAccount... accounts) {
        Collections.addAll(this.accounts, accounts);
        return this;
    }

    public SetDefaultRoleStatement withForKeyword(boolean forKeyword) {
        setForKeyword(forKeyword);
        return this;
    }
}

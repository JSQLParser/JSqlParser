/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.role;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.user.UserAccount;

/**
 * PostgreSQL {@code REASSIGN OWNED BY old_role [, ...] TO new_role}. Roles use the same name model
 * as the roles of {@code DROP OWNED BY}.
 */
public class ReassignOwned extends ASTNodeAccessImpl implements Statement {
    private final List<UserAccount> roles = new ArrayList<>();
    private UserAccount newRole;

    public List<UserAccount> getRoles() {
        return Collections.unmodifiableList(roles);
    }

    public void setRoles(Collection<? extends UserAccount> roles) {
        this.roles.clear();
        if (roles != null) {
            this.roles.addAll(roles);
        }
    }

    public ReassignOwned withRoles(Collection<? extends UserAccount> roles) {
        setRoles(roles);
        return this;
    }

    public ReassignOwned addRoles(UserAccount... roles) {
        Collections.addAll(this.roles, roles);
        return this;
    }

    public UserAccount getNewRole() {
        return newRole;
    }

    public void setNewRole(UserAccount newRole) {
        this.newRole = newRole;
    }

    public ReassignOwned withNewRole(UserAccount newRole) {
        setNewRole(newRole);
        return this;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    @Override
    public String toString() {
        return "REASSIGN OWNED BY "
                + roles.stream().map(Object::toString).collect(Collectors.joining(", "))
                + " TO " + newRole;
    }
}

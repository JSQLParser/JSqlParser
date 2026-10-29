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

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.create.user.RoleSelection;

/**
 * MySQL/MariaDB {@code SET ROLE {DEFAULT | NONE | ALL [EXCEPT role, ...] | role, ...}}. PostgreSQL
 * {@code SET [SESSION | LOCAL] ROLE name} remains a {@link SetStatement}.
 */
public class SetRoleStatement extends ASTNodeAccessImpl implements Statement {

    private RoleSelection roles;

    public SetRoleStatement() {
        // empty constructor
    }

    public SetRoleStatement(RoleSelection roles) {
        this.roles = roles;
    }

    public RoleSelection getRoles() {
        return roles;
    }

    public void setRoles(RoleSelection roles) {
        this.roles = roles;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    @Override
    public String toString() {
        return "SET ROLE " + roles;
    }

    public SetRoleStatement withRoles(RoleSelection roles) {
        setRoles(roles);
        return this;
    }
}

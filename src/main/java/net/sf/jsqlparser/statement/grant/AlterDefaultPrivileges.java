/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.grant;

import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

public class AlterDefaultPrivileges extends ASTNodeAccessImpl implements Statement {
    public enum RoleKeyword {
        ROLE, USER
    }

    private RoleKeyword roleKeyword;
    private List<String> roles;
    private List<String> schemas;
    private Grant grant;
    private Revoke revoke;

    public RoleKeyword getRoleKeyword() {
        return roleKeyword;
    }

    public void setRoleKeyword(RoleKeyword roleKeyword) {
        this.roleKeyword = roleKeyword;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<String> getSchemas() {
        return schemas;
    }

    public void setSchemas(List<String> schemas) {
        this.schemas = schemas;
    }

    public Grant getGrant() {
        return grant;
    }

    public void setGrant(Grant grant) {
        this.grant = grant;
    }

    public Revoke getRevoke() {
        return revoke;
    }

    public void setRevoke(Revoke revoke) {
        this.revoke = revoke;
    }

    public void appendTo(StringBuilder sql, Consumer<Expression> visitor) {
        sql.append("ALTER DEFAULT PRIVILEGES");
        if (roles != null) {
            sql.append(" FOR ").append(roleKeyword).append(' ').append(String.join(", ", roles));
        }
        if (schemas != null) {
            sql.append(" IN SCHEMA ").append(String.join(", ", schemas));
        }
        sql.append(' ');
        if ((grant == null) == (revoke == null)) {
            throw new IllegalStateException("Exactly one GRANT or REVOKE is required");
        }
        if (grant != null) {
            grant.appendTo(sql, visitor);
        } else {
            revoke.appendTo(sql, visitor);
        }
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();
        appendTo(sql, sql::append);
        return sql.toString();
    }
}

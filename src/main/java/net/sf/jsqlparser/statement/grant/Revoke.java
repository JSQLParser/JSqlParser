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

import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.CascadeBehavior;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

public class Revoke extends ASTNodeAccessImpl implements Statement {
    private PrivilegeClause clause = new PrivilegeClause();
    private boolean ifExists;
    private GrantOption.Kind optionFor;
    private CascadeBehavior behavior;
    private boolean ignoreUnknownUser;

    public PrivilegeClause getClause() {
        return clause;
    }

    public void setClause(PrivilegeClause clause) {
        this.clause = clause;
    }

    /** MySQL {@code REVOKE IF EXISTS}. */
    public boolean isIfExists() {
        return ifExists;
    }

    public void setIfExists(boolean ifExists) {
        this.ifExists = ifExists;
    }

    /** MySQL {@code IGNORE UNKNOWN USER}. */
    public boolean isIgnoreUnknownUser() {
        return ignoreUnknownUser;
    }

    public void setIgnoreUnknownUser(boolean ignoreUnknownUser) {
        this.ignoreUnknownUser = ignoreUnknownUser;
    }

    public GrantOption.Kind getOptionFor() {
        return optionFor;
    }

    public void setOptionFor(GrantOption.Kind optionFor) {
        this.optionFor = optionFor;
    }

    public CascadeBehavior getBehavior() {
        return behavior;
    }

    public void setBehavior(CascadeBehavior behavior) {
        this.behavior = behavior;
    }

    public void appendTo(StringBuilder sql, Consumer<Expression> visitor) {
        sql.append("REVOKE ");
        if (ifExists) {
            sql.append("IF EXISTS ");
        }
        if (optionFor != null) {
            sql.append(optionFor).append(" OPTION FOR ");
        }
        clause.appendTo(sql, true, visitor);
        if (clause.getGrantor() != null) {
            sql.append(" GRANTED BY ").append(clause.getGrantor());
        }
        if (behavior != null) {
            sql.append(' ').append(behavior);
        }
        if (ignoreUnknownUser) {
            sql.append(" IGNORE UNKNOWN USER");
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

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
import net.sf.jsqlparser.statement.CascadeBehavior;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

public class Revoke implements Statement {
    private PrivilegeClause clause = new PrivilegeClause();
    private GrantOption.Kind optionFor;
    private CascadeBehavior behavior;

    public PrivilegeClause getClause() {
        return clause;
    }

    public void setClause(PrivilegeClause clause) {
        this.clause = clause;
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
        if (optionFor != null) {
            sql.append(optionFor).append(" OPTION FOR ");
        }
        clause.appendTo(sql, true, visitor);
        if (clause.getGrantedBy() != null) {
            sql.append(" GRANTED BY ").append(clause.getGrantedBy());
        }
        if (behavior != null) {
            sql.append(' ').append(behavior);
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

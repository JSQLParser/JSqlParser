/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.insert;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.expression.Expression;

public class OracleMultiInsertBranch implements Serializable {

    private Expression whenExpression;
    private boolean elseClause;
    private List<OracleMultiInsertClause> clauses = new ArrayList<>();

    public Expression getWhenExpression() {
        return whenExpression;
    }

    public void setWhenExpression(Expression whenExpression) {
        this.whenExpression = whenExpression;
        if (whenExpression != null) {
            this.elseClause = false;
        }
    }

    public boolean isElseClause() {
        return elseClause;
    }

    public void setElseClause(boolean elseClause) {
        this.elseClause = elseClause;
        if (elseClause) {
            this.whenExpression = null;
        }
    }

    public List<OracleMultiInsertClause> getClauses() {
        return clauses;
    }

    public void setClauses(List<OracleMultiInsertClause> clauses) {
        this.clauses = clauses == null ? new ArrayList<>() : clauses;
    }

    public void addClause(OracleMultiInsertClause clause) {
        if (clause == null) {
            return;
        }
        clauses.add(clause);
    }

    public OracleMultiInsertBranch withWhenExpression(Expression whenExpression) {
        this.setWhenExpression(whenExpression);
        return this;
    }

    public OracleMultiInsertBranch withElseClause(boolean elseClause) {
        this.setElseClause(elseClause);
        return this;
    }

    public OracleMultiInsertBranch withClauses(List<OracleMultiInsertClause> clauses) {
        this.setClauses(clauses);
        return this;
    }
}

/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2022 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.expression.Expression;

import java.io.Serializable;

public class ErrorClause implements Serializable {
    private ErrorDestination errorDestination;
    private Expression expression;
    private RejectClause rejectClause;
    private boolean replace;
    private boolean truncate;

    public ErrorDestination getErrorDestination() {
        return errorDestination;
    }

    public void setErrorDestination(ErrorDestination errorDestination) {
        this.errorDestination = errorDestination;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public RejectClause getRejectClause() {
        return rejectClause;
    }

    public void setRejectClause(RejectClause rejectClause) {
        this.rejectClause = rejectClause;
    }

    public boolean isReplace() {
        return replace;
    }

    public void setReplace(boolean replace) {
        this.replace = replace;
    }

    public boolean isTruncate() {
        return truncate;
    }

    public void setTruncate(boolean truncate) {
        this.truncate = truncate;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();

        if (errorDestination != null) {
            sql.append("ERRORS INTO ");
            sql.append(errorDestination);
            if (expression != null) {
                sql.append(" (");
                sql.append(expression);
                sql.append(")");
            }

            if (replace) {
                sql.append(" REPLACE");
            } else if (truncate) {
                sql.append(" TRUNCATE");
            }
        }

        if (rejectClause != null) {
            sql.append(" ");
            sql.append(rejectClause);
        }

        return sql.toString();
    }
}

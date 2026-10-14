/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.show;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.show.ShowTablesStatement.SelectionMode;

/**
 * A MySQL {@code SHOW TABLE STATUS [{FROM | IN} db] [LIKE 'pattern' | WHERE expr]} statement.
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/show-table-status.html">MySQL SHOW TABLE
 *      STATUS</a>
 */
public class ShowTableStatusStatement implements Statement {

    private SelectionMode selectionMode = SelectionMode.FROM;
    private String dbName;
    private Expression likeExpression;
    private Expression whereCondition;

    /** The keyword before the database name, {@code FROM} unless {@code IN} was written. */
    public SelectionMode getSelectionMode() {
        return selectionMode;
    }

    public void setSelectionMode(SelectionMode selectionMode) {
        this.selectionMode = selectionMode;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public Expression getLikeExpression() {
        return likeExpression;
    }

    public void setLikeExpression(Expression likeExpression) {
        this.likeExpression = likeExpression;
    }

    public Expression getWhereCondition() {
        return whereCondition;
    }

    public void setWhereCondition(Expression whereCondition) {
        this.whereCondition = whereCondition;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("SHOW TABLE STATUS");
        if (dbName != null) {
            builder.append(' ').append(selectionMode == null ? SelectionMode.FROM : selectionMode)
                    .append(' ').append(dbName);
        }
        if (likeExpression != null) {
            builder.append(" LIKE ").append(likeExpression);
        } else if (whereCondition != null) {
            builder.append(" WHERE ").append(whereCondition);
        }
        return builder.toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    public ShowTableStatusStatement withSelectionMode(SelectionMode selectionMode) {
        this.setSelectionMode(selectionMode);
        return this;
    }

    public ShowTableStatusStatement withDbName(String dbName) {
        this.setDbName(dbName);
        return this;
    }

    public ShowTableStatusStatement withLikeExpression(Expression likeExpression) {
        this.setLikeExpression(likeExpression);
        return this;
    }

    public ShowTableStatusStatement withWhereCondition(Expression whereCondition) {
        this.setWhereCondition(whereCondition);
        return this;
    }
}

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

import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.show.ShowTablesStatement.SelectionMode;

/**
 * A MySQL {@code SHOW TABLE STATUS [{FROM | IN} db] [LIKE 'pattern' | WHERE expr]} statement.
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/show-table-status.html">MySQL SHOW TABLE
 *      STATUS</a>
 */
public class ShowTableStatusStatement extends ASTNodeAccessImpl implements Statement {

    private ShowDbSelection dbSelection;
    private ShowFilter filter;

    /** The keyword before the database name, {@code FROM} unless {@code IN} was written. */
    public SelectionMode getSelectionMode() {
        return dbSelection == null ? SelectionMode.FROM : dbSelection.getSelectionMode();
    }

    /** Sets the keyword before the database name, which is kept until the name is set. */
    public void setSelectionMode(SelectionMode selectionMode) {
        dbSelection = ShowDbSelection.updateSelectionMode(dbSelection, selectionMode);
    }

    public String getDbName() {
        return ShowDbSelection.name(dbSelection);
    }

    public void setDbName(String dbName) {
        dbSelection = ShowDbSelection.updateName(dbSelection, dbName);
    }

    /** The {@code {FROM | IN} db} clause, or null. */
    public ShowDbSelection getDbSelection() {
        return dbSelection;
    }

    public void setDbSelection(ShowDbSelection dbSelection) {
        this.dbSelection = dbSelection;
    }

    /** The {@code LIKE} or {@code WHERE} filter, or null. */
    public ShowFilter getFilter() {
        return filter;
    }

    public void setFilter(ShowFilter filter) {
        this.filter = filter;
    }

    public Expression getLikeExpression() {
        return ShowFilter.likeExpression(filter);
    }

    /** Sets a {@code LIKE} filter, which replaces a {@code WHERE} filter; null removes it. */
    public void setLikeExpression(Expression likeExpression) {
        filter = ShowFilter.replace(filter, ShowFilter.Kind.LIKE, likeExpression);
    }

    public Expression getWhereCondition() {
        return ShowFilter.whereCondition(filter);
    }

    /** Sets a {@code WHERE} filter, which replaces a {@code LIKE} filter; null removes it. */
    public void setWhereCondition(Expression whereCondition) {
        filter = ShowFilter.replace(filter, ShowFilter.Kind.WHERE, whereCondition);
    }

    /** Appends the statement, printing the filter expression with the given printer. */
    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> printer) {
        builder.append("SHOW TABLE STATUS");
        if (ShowDbSelection.isPresent(dbSelection)) {
            builder.append(' ').append(dbSelection);
        }
        if (filter != null) {
            filter.appendTo(builder.append(' '), printer);
        }
        return builder;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, builder::append).toString();
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

    public ShowTableStatusStatement withDbSelection(ShowDbSelection dbSelection) {
        this.setDbSelection(dbSelection);
        return this;
    }

    public ShowTableStatusStatement withFilter(ShowFilter filter) {
        this.setFilter(filter);
        return this;
    }
}

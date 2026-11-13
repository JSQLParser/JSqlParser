/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.show;

import java.util.Locale;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

import java.util.EnumSet;

/**
 * A {@code SHOW [EXTENDED] [FULL] TABLES [{FROM | IN} db] [LIKE 'pattern' | WHERE expr]} statement.
 *
 * <p>
 * The database and the filter are held by {@link ShowDbSelection} and {@link ShowFilter}, shared
 * with the other MySQL {@code SHOW} statements; the separate selection mode, database name,
 * {@code LIKE} and {@code WHERE} accessors read and write them.
 * </p>
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/show-tables.html">MySQL show tables</a>
 */
public class ShowTablesStatement extends ASTNodeAccessImpl implements Statement {

    private EnumSet<Modifiers> modifiers;
    private ShowDbSelection dbSelection;
    private ShowFilter filter;

    public EnumSet<Modifiers> getModifiers() {
        return modifiers;
    }

    public void setModifiers(EnumSet<Modifiers> modifiers) {
        this.modifiers = modifiers;
    }

    /** {@code FROM} or {@code IN}, or null without a database. */
    public SelectionMode getSelectionMode() {
        return dbSelection == null ? null : dbSelection.getSelectionMode();
    }

    /** Sets the keyword before the database name, which is kept until the name is set. */
    public void setSelectionMode(SelectionMode selectionMode) {
        if (selectionMode == null && !ShowDbSelection.isPresent(dbSelection)) {
            dbSelection = null;
        } else {
            dbSelection = ShowDbSelection.updateSelectionMode(dbSelection, selectionMode);
        }
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
        builder.append("SHOW");
        if (modifiers != null && modifiers.contains(Modifiers.EXTENDED)) {
            builder.append(" EXTENDED");
        }
        if (modifiers != null && modifiers.contains(Modifiers.FULL)) {
            builder.append(" FULL");
        }
        builder.append(" TABLES");
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

    public ShowTablesStatement withModifiers(EnumSet<Modifiers> modifiers) {
        this.setModifiers(modifiers);
        return this;
    }

    public ShowTablesStatement withDbSelection(ShowDbSelection dbSelection) {
        this.setDbSelection(dbSelection);
        return this;
    }

    public ShowTablesStatement withFilter(ShowFilter filter) {
        this.setFilter(filter);
        return this;
    }

    /**
     * The keyword before a database name in the MySQL {@code SHOW} statements, shared by all of
     * them.
     */
    public enum SelectionMode {
        FROM, IN;

        public static SelectionMode from(String mode) {
            return Enum.valueOf(SelectionMode.class, mode.toUpperCase(Locale.ROOT));
        }
    }

    public enum Modifiers {
        EXTENDED, FULL;

        public static Modifiers from(String modifier) {
            return Enum.valueOf(Modifiers.class, modifier.toUpperCase(Locale.ROOT));
        }
    }
}

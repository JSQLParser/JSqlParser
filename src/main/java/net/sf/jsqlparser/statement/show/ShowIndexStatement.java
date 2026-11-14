/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.show;

import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.show.ShowTablesStatement.SelectionMode;

/**
 * A MySQL {@code SHOW [EXTENDED] {INDEX | INDEXES | KEYS} {FROM | IN} table [{FROM | IN} db]
 * [WHERE expr]} statement.
 *
 * @author Jayant Kumar Yadav
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/show-index.html">MySQL SHOW INDEX</a>
 */
public class ShowIndexStatement extends ASTNodeAccessImpl implements Statement {

    /** The spelling of the statement keyword. */
    public enum Keyword {
        INDEX, INDEXES, KEYS
    }

    private boolean extended;
    private Keyword keyword = Keyword.INDEX;
    private SelectionMode tableSelectionMode = SelectionMode.FROM;
    private Table table;
    private ShowDbSelection dbSelection;
    private Expression whereCondition;

    public ShowIndexStatement() {
        // empty constructor
    }

    public ShowIndexStatement(String tableName) {
        setTableName(tableName);
    }

    public ShowIndexStatement(Table table) {
        this.table = table;
    }

    public boolean isExtended() {
        return extended;
    }

    public void setExtended(boolean extended) {
        this.extended = extended;
    }

    /** {@code INDEX}, {@code INDEXES} or {@code KEYS}, as written; never null. */
    public Keyword getKeyword() {
        return keyword;
    }

    /** Sets the keyword spelling; null means {@code INDEX}. */
    public void setKeyword(Keyword keyword) {
        this.keyword = keyword == null ? Keyword.INDEX : keyword;
    }

    /** The keyword before the table name, {@code FROM} unless {@code IN} was written. */
    public SelectionMode getTableSelectionMode() {
        return tableSelectionMode;
    }

    /** Sets the keyword before the table name; null means {@code FROM}. */
    public void setTableSelectionMode(SelectionMode tableSelectionMode) {
        this.tableSelectionMode =
                tableSelectionMode == null ? SelectionMode.FROM : tableSelectionMode;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    /** The table name as written, including any qualifier; derived from {@link #getTable()}. */
    public String getTableName() {
        return table == null ? null : table.getFullyQualifiedName();
    }

    public void setTableName(String tableName) {
        this.table = tableName == null ? null : new Table(tableName);
    }

    /** The trailing {@code {FROM | IN} db} clause, which names the database of the table. */
    public ShowDbSelection getDbSelection() {
        return dbSelection;
    }

    public void setDbSelection(ShowDbSelection dbSelection) {
        this.dbSelection = dbSelection;
    }

    /** The database of the trailing {@code {FROM | IN} db} clause, or null. */
    public String getDbName() {
        return ShowDbSelection.name(dbSelection);
    }

    public Expression getWhereCondition() {
        return whereCondition;
    }

    public void setWhereCondition(Expression whereCondition) {
        this.whereCondition = whereCondition;
    }

    /**
     * The table the statement reads: {@link #getTable()}, or the table in the database of the
     * trailing {@code FROM db} clause, which MySQL reads as {@code db.table}.
     */
    public Table getQualifiedTable() {
        if (table == null || !ShowDbSelection.isPresent(dbSelection)) {
            return table;
        }
        return new Table(dbSelection.getName(), table.getName());
    }

    /** Appends the statement, printing the {@code WHERE} condition with the given printer. */
    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> printer) {
        builder.append("SHOW");
        if (extended) {
            builder.append(" EXTENDED");
        }
        builder.append(' ').append(keyword).append(' ').append(tableSelectionMode).append(' ')
                .append(table);
        if (ShowDbSelection.isPresent(dbSelection)) {
            builder.append(' ').append(dbSelection);
        }
        if (whereCondition != null) {
            builder.append(" WHERE ");
            printer.accept(whereCondition);
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

    public ShowIndexStatement withTableName(String tableName) {
        this.setTableName(tableName);
        return this;
    }

    public ShowIndexStatement withTable(Table table) {
        this.setTable(table);
        return this;
    }

    public ShowIndexStatement withExtended(boolean extended) {
        this.setExtended(extended);
        return this;
    }

    public ShowIndexStatement withKeyword(Keyword keyword) {
        this.setKeyword(keyword);
        return this;
    }

    public ShowIndexStatement withTableSelectionMode(SelectionMode tableSelectionMode) {
        this.setTableSelectionMode(tableSelectionMode);
        return this;
    }

    public ShowIndexStatement withDbSelection(ShowDbSelection dbSelection) {
        this.setDbSelection(dbSelection);
        return this;
    }

    public ShowIndexStatement withWhereCondition(Expression whereCondition) {
        this.setWhereCondition(whereCondition);
        return this;
    }
}

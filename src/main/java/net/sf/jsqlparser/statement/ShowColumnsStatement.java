/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.show.ShowTablesStatement.SelectionMode;

/**
 * A MySQL {@code SHOW [EXTENDED] [FULL] {COLUMNS | FIELDS} {FROM | IN} table [{FROM | IN} db]
 * [LIKE 'pattern' | WHERE expr]} statement.
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/show-columns.html">MySQL SHOW COLUMNS</a>
 */
public class ShowColumnsStatement extends ASTNodeAccessImpl implements Statement {

    private boolean extended;
    private boolean full;
    private boolean fieldsKeyword;
    private SelectionMode tableSelectionMode = SelectionMode.FROM;
    private Table table;
    private SelectionMode dbSelectionMode = SelectionMode.FROM;
    private String dbName;
    private Expression likeExpression;
    private Expression whereCondition;

    public ShowColumnsStatement() {
        // empty constructor
    }

    public ShowColumnsStatement(String tableName) {
        setTableName(tableName);
    }

    public ShowColumnsStatement(Table table) {
        this.table = table;
    }

    public boolean isExtended() {
        return extended;
    }

    public void setExtended(boolean extended) {
        this.extended = extended;
    }

    public boolean isFull() {
        return full;
    }

    public void setFull(boolean full) {
        this.full = full;
    }

    /** Whether the statement is spelled with {@code FIELDS} instead of {@code COLUMNS}. */
    public boolean isFieldsKeyword() {
        return fieldsKeyword;
    }

    public void setFieldsKeyword(boolean fieldsKeyword) {
        this.fieldsKeyword = fieldsKeyword;
    }

    /** The keyword before the table name, {@code FROM} unless {@code IN} was written. */
    public SelectionMode getTableSelectionMode() {
        return tableSelectionMode;
    }

    public void setTableSelectionMode(SelectionMode tableSelectionMode) {
        this.tableSelectionMode = tableSelectionMode;
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

    /** The keyword before the database name, {@code FROM} unless {@code IN} was written. */
    public SelectionMode getDbSelectionMode() {
        return dbSelectionMode;
    }

    public void setDbSelectionMode(SelectionMode dbSelectionMode) {
        this.dbSelectionMode = dbSelectionMode;
    }

    /** The database of the trailing {@code FROM db} or {@code IN db} clause, or null. */
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
        StringBuilder builder = new StringBuilder("SHOW");
        if (extended) {
            builder.append(" EXTENDED");
        }
        if (full) {
            builder.append(" FULL");
        }
        builder.append(fieldsKeyword ? " FIELDS " : " COLUMNS ")
                .append(tableSelectionMode == null ? SelectionMode.FROM : tableSelectionMode)
                .append(' ').append(table);
        if (dbName != null) {
            builder.append(' ')
                    .append(dbSelectionMode == null ? SelectionMode.FROM : dbSelectionMode)
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

    public ShowColumnsStatement withTableName(String tableName) {
        this.setTableName(tableName);
        return this;
    }

    public ShowColumnsStatement withTable(Table table) {
        this.setTable(table);
        return this;
    }

    public ShowColumnsStatement withExtended(boolean extended) {
        this.setExtended(extended);
        return this;
    }

    public ShowColumnsStatement withFull(boolean full) {
        this.setFull(full);
        return this;
    }

    public ShowColumnsStatement withFieldsKeyword(boolean fieldsKeyword) {
        this.setFieldsKeyword(fieldsKeyword);
        return this;
    }

    public ShowColumnsStatement withTableSelectionMode(SelectionMode tableSelectionMode) {
        this.setTableSelectionMode(tableSelectionMode);
        return this;
    }

    public ShowColumnsStatement withDbSelectionMode(SelectionMode dbSelectionMode) {
        this.setDbSelectionMode(dbSelectionMode);
        return this;
    }

    public ShowColumnsStatement withDbName(String dbName) {
        this.setDbName(dbName);
        return this;
    }

    public ShowColumnsStatement withLikeExpression(Expression likeExpression) {
        this.setLikeExpression(likeExpression);
        return this;
    }

    public ShowColumnsStatement withWhereCondition(Expression whereCondition) {
        this.setWhereCondition(whereCondition);
        return this;
    }
}

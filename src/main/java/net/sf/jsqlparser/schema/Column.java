/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.jsqlparser.expression.ArrayConstructor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.operators.relational.SupportsOldOracleJoinSyntax;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 * A column. It can have the table name it belongs to.
 */
public class Column extends ASTNodeAccessImpl implements Expression, MultiPartName {

    private Table table;
    private String columnName;
    private String commentText;
    private ArrayConstructor arrayConstructor;
    private String tableDelimiter = ".";
    private int oldOracleJoinSyntax = SupportsOldOracleJoinSyntax.NO_ORACLE_JOIN;

    // holds the physical table when resolved against an actual schema information
    private Table resolvedTable = null;

    public Column() {}

    public Column(Table table, String columnName) {
        setTable(table);
        setColumnName(columnName);
    }

    public Column(List<String> nameParts) {
        this(nameParts, nameParts.size() > 1 ? Collections.nCopies(nameParts.size() - 1, ".")
                : new ArrayList<>());
    }

    public Column(List<String> nameParts, List<String> delimiters) {
        this(
                nameParts.size() > 1 ? new Table(nameParts.subList(0, nameParts.size() - 1),
                        delimiters.subList(0, delimiters.size() - 1)) : null,
                nameParts.get(nameParts.size() - 1));
        setTableDelimiter(delimiters.isEmpty() ? "." : delimiters.get(delimiters.size() - 1));
    }

    public Column(String columnName) {
        this();
        setColumnName(columnName);
    }

    public ArrayConstructor getArrayConstructor() {
        return arrayConstructor;
    }

    public Column setArrayConstructor(ArrayConstructor arrayConstructor) {
        this.arrayConstructor = arrayConstructor;
        return this;
    }

    /**
     * Retrieve the information regarding the {@code Table} this {@code Column} does belong to, if
     * any can be inferred.
     * <p>
     * The inference is based only on local information, and not on the whole SQL command. For
     * example, consider the following query: <blockquote>
     *
     * <pre>
     *  SELECT x FROM Foo
     * </pre>
     *
     * </blockquote> Given the {@code Column} called {@code x}, this method would return
     * {@code null}, and not the info about the table {@code Foo}. On the other hand, consider:
     * <blockquote>
     *
     * <pre>
     *  SELECT t.x FROM Foo t
     * </pre>
     *
     * </blockquote> Here, we will get a {@code Table} object for a table called {@code t}. But
     * because the inference is local, such object will not know that {@code t} is just an alias for
     * {@code Foo}.
     *
     * @return an instance of {@link net.sf.jsqlparser.schema.Table} representing the table this
     *         column does belong to, if it can be inferred. Can be {@code null}.
     */
    public Table getTable() {
        return table;
    }

    public String getTableName() {
        return table != null ? table.getName() : null;
    }

    public String getUnquotedTableName() {
        return table != null ? table.getUnquotedName() : null;
    }

    public String getSchemaName() {
        return table != null ? table.getSchemaName() : null;
    }

    public String getUnquotedSchemaName() {
        return table != null ? table.getUnquotedSchemaName() : null;
    }

    public String getCatalogName() {
        return table != null ? table.getCatalogName() : null;
    }

    public String getUnquotedCatalogName() {
        return table != null ? table.getUnquotedCatalogName() : null;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getUnquotedColumnName() {
        return MultiPartName.unquote(columnName);
    }

    public void setColumnName(String name) {
        // BigQuery seems to allow things like: `catalogName.schemaName.tableName` in only one pair
        // of quotes
        // however, some people believe that Dots in Names are a good idea, so provide a switch-off
        boolean splitNamesOnDelimiter = System.getProperty("SPLIT_NAMES_ON_DELIMITER") == null ||
                !List
                        .of("0", "N", "n", "FALSE", "false", "OFF", "off")
                        .contains(System.getProperty("SPLIT_NAMES_ON_DELIMITER"));

        setName(name, splitNamesOnDelimiter);
    }

    public void setName(String name, boolean splitNamesOnDelimiter) {
        if (MultiPartName.isQuoted(name) && name.contains(".") && splitNamesOnDelimiter) {
            String[] parts = MultiPartName.unquote(name).split("\\.");
            switch (parts.length) {
                case 3:
                    this.table = new Table("\"" + parts[0] + "\".\"" + parts[1] + "\"");
                    this.columnName = "\"" + parts[2] + "\"";
                    break;
                case 2:
                    this.table = new Table("\"" + parts[0] + "\"");
                    this.columnName = "\"" + parts[1] + "\"";
                    break;
                case 1:
                    this.columnName = "\"" + parts[0] + "\"";
                    break;
                default:
                    throw new RuntimeException("Invalid column name: " + name);
            }
        } else if (name.contains(".") && splitNamesOnDelimiter) {
            String[] parts = MultiPartName.unquote(name).split("\\.");
            switch (parts.length) {
                case 3:
                    this.table = new Table(parts[0] + "." + parts[1]);
                    this.columnName = parts[2];
                    break;
                case 2:
                    this.table = new Table(parts[0]);
                    this.columnName = parts[1];
                    break;
                case 1:
                    this.columnName = parts[0];
                    break;
                default:
                    throw new RuntimeException("Invalid column name: " + name);
            }
        } else {
            this.columnName = name;
        }
    }

    public String getTableDelimiter() {
        return tableDelimiter;
    }

    public void setTableDelimiter(String tableDelimiter) {
        this.tableDelimiter = tableDelimiter;
    }

    public int getOldOracleJoinSyntax() {
        return oldOracleJoinSyntax;
    }

    public void setOldOracleJoinSyntax(int oldOracleJoinSyntax) {
        this.oldOracleJoinSyntax = oldOracleJoinSyntax;
    }

    @Override
    public String getFullyQualifiedName() {
        return getFullyQualifiedName(false);
    }

    @Override
    public String getUnquotedName() {
        return MultiPartName.unquote(columnName);
    }

    public String getFullyQualifiedName(boolean aliases) {
        StringBuilder fqn = new StringBuilder();

        if (table != null) {
            if (table.getAlias() != null && aliases) {
                fqn.append(table.getAlias().getName());
            } else {
                fqn.append(table.getFullyQualifiedName());
            }
        }
        if (fqn.length() > 0) {
            fqn.append(tableDelimiter);
        }
        if (columnName != null) {
            fqn.append(columnName);
        }

        if (commentText != null) {
            fqn.append(" COMMENT ");
            fqn.append(commentText);
        }

        if (arrayConstructor != null) {
            fqn.append(arrayConstructor);
        }

        return fqn.toString();
    }

    // old and confusing, don't use it!
    @Deprecated
    public String getName(boolean aliases) {
        return columnName;
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }

    @Override
    public String toString() {
        return getFullyQualifiedName(true)
                + (oldOracleJoinSyntax != SupportsOldOracleJoinSyntax.NO_ORACLE_JOIN ? "(+)" : "")
                + (commentText != null ? " /* " + commentText + "*/ " : "");
    }

    public Column withTable(Table table) {
        this.setTable(table);
        return this;
    }

    public Column withColumnName(String columnName) {
        this.setColumnName(columnName);
        return this;
    }

    public Column withCommentText(String commentText) {
        this.setCommentText(commentText);
        return this;
    }

    public Column withTableDelimiter(String delimiter) {
        this.setTableDelimiter(delimiter);
        return this;
    }

    public Column withOldOracleJoinSyntax(int oldOracleJoinSyntax) {
        this.setOldOracleJoinSyntax(oldOracleJoinSyntax);
        return this;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    /**
     * Gets the actual table when resolved against a physical schema information.
     *
     * @return the actual table when resolved against a physical schema information
     */
    public Table getResolvedTable() {
        return resolvedTable;
    }

    /**
     * Sets resolved table.
     *
     * @param resolvedTable the resolved table
     * @return this column
     */
    public Column setResolvedTable(Table resolvedTable) {
        // clone, not reference
        this.resolvedTable =
                resolvedTable != null ? new Table(resolvedTable.getFullyQualifiedName()) : null;
        return this;
    }
}

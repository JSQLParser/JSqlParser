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

import java.util.List;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 * A column. It can have the table name it belongs to.
 */
public final class Column extends ASTNodeAccessImpl implements Expression, MultiPartName {

    private Table table;
    private String columnName;

    public Column() {
    }

    public Column(Table table, String columnName) {
        setTable(table);
        setColumnName(columnName);
    }

    public Column(List<String> nameParts) {
        this(nameParts.size() > 1
                ? new Table(nameParts.subList(0, nameParts.size() - 1)) : null,
                nameParts.get(nameParts.size() - 1));
    }

    public Column(String columnName) {
        this(null, columnName);
    }

    /**
     * Retrieve the information regarding the {@code Table} this {@code Column} does
     * belong to, if any can be inferred.
     * <p>
     * The inference is based only on local information, and not on the whole SQL command.
     * For example, consider the following query:
     * <blockquote><pre>
     *  SELECT x FROM Foo
     * </pre></blockquote>
     * Given the {@code Column} called {@code x}, this method would return {@code null},
     * and not the info about the table {@code Foo}.
     * On the other hand, consider:
     * <blockquote><pre>
     *  SELECT t.x FROM Foo t
     * </pre></blockquote>
     * Here, we will get a {@code Table} object for a table called {@code t}.
     * But because the inference is local, such object will not know that {@code t} is
     * just an alias for {@code Foo}.
     *
     * @return an instance of {@link net.sf.jsqlparser.schema.Table} representing the
     *          table this column does belong to, if it can be inferred. Can be {@code null}.
     */
    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String string) {
        columnName = string;
    }

    @Override
    public String getFullyQualifiedName() {
        return getName(false);
    }

    public String getName(boolean aliases) {
        StringBuilder fqn = new StringBuilder();

        if (table != null) {
            if (table.getAlias() != null && aliases) {
                fqn.append(table.getAlias().getName());
            } else {
                fqn.append(table.getFullyQualifiedName());
            }
        }
        if (fqn.length() > 0) {
            fqn.append('.');
        }
        if (columnName != null) {
            fqn.append(columnName);
        }
        return fqn.toString();
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    @Override
    public String toString() {
        return getName(true);
    }
}

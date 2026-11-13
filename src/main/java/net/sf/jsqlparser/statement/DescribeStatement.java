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

import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Select;

/**
 * {@code DESCRIBE} or {@code DESC} of a table, or, in DuckDB, of a query's result.
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/explain.html">MySQL DESCRIBE</a>
 */
public class DescribeStatement extends ASTNodeAccessImpl implements Statement {

    private Table table;
    private Select select;
    private String describeType;
    private Expression columnPattern;

    public DescribeStatement() {
        // empty constructor
    }

    public DescribeStatement(Table table) {
        this.table = table;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    /** DuckDB also describes a query: {@code DESCRIBE SELECT ...}. */
    public Select getSelect() {
        return select;
    }

    public void setSelect(Select select) {
        this.select = select;
    }

    public DescribeStatement withSelect(Select select) {
        setSelect(select);
        return this;
    }

    /**
     * The MySQL column name ({@link net.sf.jsqlparser.schema.Column}) or wildcard string that
     * limits the described columns to those it matches, or null.
     */
    public Expression getColumnPattern() {
        return columnPattern;
    }

    public void setColumnPattern(Expression columnPattern) {
        this.columnPattern = columnPattern;
    }

    public DescribeStatement withColumnPattern(Expression columnPattern) {
        setColumnPattern(columnPattern);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, builder::append, builder::append).toString();
    }

    /** Shared by SQL rendering and statement deparsers. */
    public StringBuilder appendTo(StringBuilder builder, Consumer<Select> selectPrinter,
            Consumer<Expression> expressionPrinter) {
        builder.append(describeType).append(" ");
        if (select != null) {
            selectPrinter.accept(select);
        } else {
            builder.append(table);
            if (columnPattern != null) {
                builder.append(" ");
                expressionPrinter.accept(columnPattern);
            }
        }
        return builder;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    public DescribeStatement withTable(Table table) {
        this.setTable(table);
        return this;
    }

    public String getDescribeType() {
        return describeType;
    }

    public DescribeStatement setDescribeType(String describeType) {
        this.describeType = describeType;
        return this;
    }
}

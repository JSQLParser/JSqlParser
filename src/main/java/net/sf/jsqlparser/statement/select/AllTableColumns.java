/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import java.util.List;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.ReturningReferenceType;

public class AllTableColumns extends AllColumns {

    private Table table;
    private ReturningReferenceType returningReferenceType = null;
    private String returningQualifier = null;

    public AllTableColumns(Table table, ExpressionList<Column> exceptColumns,
            List<SelectItem<?>> replaceExpressions, String exceptKeyword) {
        super(exceptColumns, replaceExpressions, exceptKeyword);
        this.table = table;
    }

    public AllTableColumns(Table table, ExpressionList<Column> exceptColumns,
            List<SelectItem<?>> replaceExpressions) {
        this(table, exceptColumns, replaceExpressions, "EXCEPT");
    }

    public AllTableColumns(Table table) {
        this(table, null, null);
    }

    public AllTableColumns(Table table, AllColumns allColumns) {
        this(table, allColumns.exceptColumns, allColumns.replaceExpressions,
                allColumns.getExceptKeyword());
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public AllTableColumns withTable(Table table) {
        this.setTable(table);
        return this;
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        if (returningQualifier != null) {
            return super.appendTo(builder.append(returningQualifier).append("."));
        }
        if (table != null) {
            return super.appendTo(table.appendTo(builder).append("."));
        }
        return super.appendTo(builder);
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }

    public ReturningReferenceType getReturningReferenceType() {
        return returningReferenceType;
    }

    public AllTableColumns setReturningReferenceType(
            ReturningReferenceType returningReferenceType) {
        this.returningReferenceType = returningReferenceType;
        return this;
    }

    public String getReturningQualifier() {
        return returningQualifier;
    }

    public AllTableColumns setReturningQualifier(String returningQualifier) {
        this.returningQualifier = returningQualifier;
        return this;
    }

    public AllTableColumns withReturningReference(ReturningReferenceType returningReferenceType,
            String returningQualifier) {
        this.returningReferenceType = returningReferenceType;
        this.returningQualifier = returningQualifier;
        return this;
    }
}

/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;

/**
 * DuckDB's simplified UNPIVOT statement:
 * {@code UNPIVOT tbl ON columns [INTO NAME name VALUE values]}, the counterpart of
 * {@link PivotQuery}.
 *
 * @see <a href="https://duckdb.org/docs/stable/sql/statements/unpivot">UNPIVOT</a>
 */
public class UnPivotQuery extends Select {
    private FromItem fromItem;
    private ExpressionList<Expression> onExpressions;
    private String nameColumn;
    private ExpressionList<Column> valueColumns;

    public FromItem getFromItem() {
        return fromItem;
    }

    public void setFromItem(FromItem fromItem) {
        this.fromItem = fromItem;
    }

    public UnPivotQuery withFromItem(FromItem fromItem) {
        setFromItem(fromItem);
        return this;
    }

    public ExpressionList<Expression> getOnExpressions() {
        return onExpressions;
    }

    public void setOnExpressions(ExpressionList<Expression> onExpressions) {
        this.onExpressions = onExpressions;
    }

    public UnPivotQuery withOnExpressions(ExpressionList<Expression> onExpressions) {
        setOnExpressions(onExpressions);
        return this;
    }

    public String getNameColumn() {
        return nameColumn;
    }

    public void setNameColumn(String nameColumn) {
        this.nameColumn = nameColumn;
    }

    public UnPivotQuery withNameColumn(String nameColumn) {
        setNameColumn(nameColumn);
        return this;
    }

    public ExpressionList<Column> getValueColumns() {
        return valueColumns;
    }

    public void setValueColumns(ExpressionList<Column> valueColumns) {
        this.valueColumns = valueColumns;
    }

    public UnPivotQuery withValueColumns(ExpressionList<Column> valueColumns) {
        setValueColumns(valueColumns);
        return this;
    }

    @Override
    public StringBuilder appendSelectBodyTo(StringBuilder builder) {
        builder.append("UNPIVOT ").append(fromItem);
        if (onExpressions != null) {
            builder.append(" ON ").append(onExpressions);
        }
        if (nameColumn != null) {
            builder.append(" INTO NAME ").append(nameColumn);
            if (valueColumns != null) {
                builder.append(" VALUE ").append(valueColumns);
            }
        }
        return builder;
    }

    @Override
    public <T, S> T accept(SelectVisitor<T> selectVisitor, S context) {
        return selectVisitor.visit(this, context);
    }

    @Override
    public <T, S> T accept(FromItemVisitor<T> fromItemVisitor, S context) {
        return fromItemVisitor.visit(this, context);
    }

    @Override
    public SampleClause getSampleClause() {
        return null;
    }

    @Override
    public FromItem setSampleClause(SampleClause sampleClause) {
        return null;
    }
}

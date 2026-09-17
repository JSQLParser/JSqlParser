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

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;

@SuppressWarnings({"PMD.UncommentedEmptyMethodBody"})
public class PivotVisitorAdapter<T> implements PivotVisitor<T> {
    private final ExpressionVisitor<T> expressionVisitor;

    public PivotVisitorAdapter() {
        this.expressionVisitor = new ExpressionVisitorAdapter<T>();
    }

    public PivotVisitorAdapter(ExpressionVisitor<T> expressionVisitor) {
        this.expressionVisitor = expressionVisitor;
    }

    @Override
    public <S> T visit(Pivot pivot, S context) {
        if (pivot.getFunctionItems() != null) {
            pivot.getFunctionItems()
                    .forEach(item -> item.getExpression().accept(expressionVisitor, context));
        }
        if (pivot.getForColumns() != null) {
            pivot.getForColumns().accept(expressionVisitor, context);
        }
        if (pivot.getSingleInItems() != null) {
            pivot.getSingleInItems()
                    .forEach(item -> item.getExpression().accept(expressionVisitor, context));
        }
        if (pivot.getMultiInItems() != null) {
            pivot.getMultiInItems()
                    .forEach(item -> item.getExpression().accept(expressionVisitor, context));
        }
        return null;
    }

    @Override
    public <S> T visit(PivotXml pivot, S context) {
        visit((Pivot) pivot, context);
        if (pivot.getInSelect() != null) {
            pivot.getInSelect().accept(expressionVisitor, context);
        }
        return null;
    }

    @Override
    public <S> T visit(UnPivot unpivot, S context) {
        if (unpivot.getUnPivotClause() != null) {
            unpivot.getUnPivotClause().forEach(column -> column.accept(expressionVisitor, context));
        }
        if (unpivot.getUnPivotForClause() != null) {
            unpivot.getUnPivotForClause()
                    .forEach(column -> column.accept(expressionVisitor, context));
        }
        if (unpivot.getUnPivotInClause() != null) {
            unpivot.getUnPivotInClause()
                    .forEach(item -> item.getExpression().accept(expressionVisitor, context));
        }
        return null;
    }
}

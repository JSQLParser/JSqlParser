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

        return null;
    }

    @Override
    public <S> T visit(PivotXml pivot, S context) {

        return null;
    }

    @Override
    public <S> T visit(UnPivot unpivot, S context) {

        return null;
    }
}

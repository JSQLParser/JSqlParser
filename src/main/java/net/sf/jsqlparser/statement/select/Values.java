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

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;

import java.util.Arrays;
import java.util.Collection;

public class Values extends Select implements FromItem {

    private ExpressionList<Expression> expressions;
    private Alias alias;

    public Values() {
        this(null, null);
    }

    public Values(ExpressionList<Expression> expressions) {
        this.expressions = expressions;
    }

    public Values(ExpressionList<Expression> expressions, Alias alias) {
        this.expressions = expressions;
        this.alias = alias;
    }

    public ExpressionList<?> getExpressions() {
        return expressions;
    }


    public void setExpressions(ExpressionList<Expression> expressions) {
        this.expressions = expressions;
    }

    @Override
    public StringBuilder appendSelectBodyTo(StringBuilder builder) {
        builder.append("VALUES ");
        builder.append(expressions.toString());
        appendTo(builder, alias);
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

    public Values withExpressions(ExpressionList<Expression> expressions) {
        this.setExpressions(expressions);
        return this;
    }

    public Values addExpressions(Expression... expressions) {
        return this.addExpressions(Arrays.asList(expressions));
    }

    public Values addExpressions(Collection<? extends Expression> expressions) {
        if (this.expressions == null) {
            this.expressions = new ParenthesedExpressionList<>();
        }
        this.expressions.addAll(expressions);
        return this;
    }

    @Override
    public Alias getAlias() {
        return alias;
    }

    @Override
    public void setAlias(Alias alias) {
        this.alias = alias;
    }

    @Override
    public Pivot getPivot() {
        return null;
    }

    @Override
    public void setPivot(Pivot pivot) {

    }

    @Override
    public UnPivot getUnPivot() {
        return null;
    }

    @Override
    public void setUnPivot(UnPivot unpivot) {

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

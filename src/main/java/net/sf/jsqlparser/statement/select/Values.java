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

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;

import java.util.ArrayList;
import java.util.Collection;

public class Values extends SelectBody {

    private ItemsList expressions;

    public Values() {
        // empty constructor
    }

    public Values(ItemsList expressions) {
        this.expressions = expressions;
    }

    public ItemsList getExpressions() {
        return expressions;
    }

    public void setExpressions(ItemsList expressions) {
        this.expressions = expressions;
    }

    @Override
    public StringBuilder appendSelectBodyTo(StringBuilder builder) {
        builder.append("VALUES ");
        builder.append(expressions.toString());
        return builder;
    }

    @Override
    public void accept(SelectVisitor selectVisitor) {
        selectVisitor.visit(this);
    }

    public Values withExpressions(ItemsList expressions) {
        this.setExpressions(expressions);
        return this;
    }

    public Values addExpressions(Expression... addExpressions) {
        if (expressions != null && expressions instanceof ExpressionList) {
            ((ExpressionList) expressions).addExpressions(addExpressions);
            return this;
        } else {
            return this.withExpressions(new ExpressionList(addExpressions));
        }
    }

    public Values addExpressions(Collection<? extends Expression> addExpressions) {
        if (expressions != null && expressions instanceof ExpressionList) {
            ((ExpressionList) expressions).addExpressions(addExpressions);
            return this;
        } else {
            return this.withExpressions(new ExpressionList(new ArrayList<>(addExpressions)));
        }
    }
}

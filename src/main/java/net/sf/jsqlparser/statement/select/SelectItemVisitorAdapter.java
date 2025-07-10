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
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;

@SuppressWarnings({"PMD.UncommentedEmptyMethodBody"})
public class SelectItemVisitorAdapter<T> implements SelectItemVisitor<T> {
    private final ExpressionVisitor<T> expressionVisitor;

    public SelectItemVisitorAdapter() {
        this.expressionVisitor = new ExpressionVisitorAdapter<>();
    }

    public SelectItemVisitorAdapter(ExpressionVisitor<T> expressionVisitor) {
        this.expressionVisitor = expressionVisitor;
    }

    @Override
    public <S> T visit(SelectItem<? extends Expression> item, S context) {
        return item.getExpression().accept(expressionVisitor, context);
    }
}

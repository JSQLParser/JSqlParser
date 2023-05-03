/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression.operators.relational;

import net.sf.jsqlparser.expression.Expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A list of ExpressionList items. e.g. multi values of insert statements. This one allows only
 * equally sized ExpressionList.
 */
public class MultiExpressionList<T extends ExpressionList<? extends Expression>>
        extends ArrayList<ExpressionList<? extends Expression>> implements ItemsList {

    @Override
    public void accept(ItemsListVisitor itemsListVisitor) {
        itemsListVisitor.visit(this);
    }

    @Deprecated
    public MultiExpressionList<?> getExprList() {
        return this;
    }

    @Deprecated
    public MultiExpressionList<?> getExpressionLists() {
        return this;
    }

    @Deprecated
    public void setExpressionLists(List<T> expressionLists) {
        this.clear();
        this.addAll(expressionLists);
    }

    public MultiExpressionList<T> addExpressionList(T expressionList) {
        this.add(expressionList);
        return this;
    }

    public void addExpressionList(List<Expression> expressions) {
        this.add(new ExpressionList(expressions));
    }

    public void addExpressionList(Expression... expressions) {
        this.add(new ExpressionList(expressions));
    }

    public MultiExpressionList<T> addExpressions(Collection<T> expressions) {
        ExpressionList<?> expressionList = new ExpressionList(expressions);
        this.add(expressionList);
        return this;
    }

    public MultiExpressionList<T> addExpressions(T... expressionLists) {
        return this.addExpressions(Arrays.asList(expressionLists));
    }

    public MultiExpressionList<T> withExpressionLists(Collection<T> expressionLists) {
        this.clear();
        return addExpressions(expressionLists);
    }

    public MultiExpressionList<T> withExpressionLists(T... expressionLists) {
        this.clear();
        return addExpressions(expressionLists);
    }

    @Override
    public String toString() {
        return this.stream().map(ExpressionList::toString).collect(Collectors.joining(", "));
    }
}

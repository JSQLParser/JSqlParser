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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.sf.jsqlparser.expression.Expression;

/**
 * A list of ExpressionList items. e.g. multi values of insert statements. This one allows only
 * equally sized ExpressionList.
 */
public class MultiExpressionList implements ItemsList {

    private List<ExpressionList> expressionLists;

    public MultiExpressionList() {
        this.expressionLists = new ArrayList<>();
    }

    @Override
    public void accept(ItemsListVisitor itemsListVisitor) {
        itemsListVisitor.visit(this);
    }

    @Deprecated
    public List<ExpressionList> getExprList() {
        return getExpressionLists();
    }

    public List<ExpressionList> getExpressionLists() {
        return expressionLists;
    }

    public void setExpressionLists(List<ExpressionList> expressionLists) {
        this.expressionLists = expressionLists;
    }

    public MultiExpressionList withExpressionLists(List<ExpressionList> expressionLists) {
        this.setExpressionLists(expressionLists);
        return this;
    }

    public void addExpressionList(ExpressionList el) {
        if (!expressionLists.isEmpty()
                && expressionLists.get(0).getExpressions().size() != el.getExpressions().size()) {
            throw new IllegalArgumentException("different count of parameters");
        }
        expressionLists.add(el);
    }

    public void addExpressionList(List<Expression> list) {
        addExpressionList(new ExpressionList(list));
    }

    public void addExpressionList(Expression... expr) {
        addExpressionList(new ExpressionList(Arrays.asList(expr)));
    }

    public MultiExpressionList addExpressionLists(ExpressionList... expr) {
        Stream.of(expr).forEach(this::addExpressionList);
        return this;
    }

    public MultiExpressionList addExpressionLists(Collection<? extends ExpressionList> expr) {
        expr.stream().forEach(this::addExpressionList);
        return this;
    }

    @Override
    public String toString() {
        return expressionLists.stream().map(ExpressionList::toString).collect(Collectors.joining(", "));
    }
}

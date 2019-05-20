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
import java.util.Iterator;
import java.util.List;
import net.sf.jsqlparser.expression.Expression;

/**
 * A list of ExpressionList items. e.g. multi values of insert statements. This one allows only
 * equally sized ExpressionList.
 */
public class MultiExpressionList implements ItemsList {

    private List<ExpressionList> exprList;

    public MultiExpressionList() {
        this.exprList = new ArrayList<ExpressionList>();
    }

    @Override
    public void accept(ItemsListVisitor itemsListVisitor) {
        itemsListVisitor.visit(this);
    }

    public List<ExpressionList> getExprList() {
        return exprList;
    }

    public void addExpressionList(ExpressionList el) {
        if (!exprList.isEmpty()
                && exprList.get(0).getExpressions().size() != el.getExpressions().size()) {
            throw new IllegalArgumentException("different count of parameters");
        }
        exprList.add(el);
    }

    public void addExpressionList(List<Expression> list) {
        addExpressionList(new ExpressionList(list));
    }

    public void addExpressionList(Expression expr) {
        addExpressionList(new ExpressionList(Arrays.asList(expr)));
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (Iterator<ExpressionList> it = exprList.iterator(); it.hasNext();) {
            b.append(it.next().toString());
            if (it.hasNext()) {
                b.append(", ");
            }
        }
        return b.toString();
    }
}

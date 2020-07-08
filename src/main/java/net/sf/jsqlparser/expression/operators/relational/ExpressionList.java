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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.PlainSelect;

/**
 * A list of expressions, as in SELECT A FROM TAB WHERE B IN (expr1,expr2,expr3)
 */
public class ExpressionList implements ItemsList {

    private List<Expression> expressions;

    public ExpressionList() {
    }

    public ExpressionList(List<Expression> expressions) {
        this.expressions = expressions;
    }

    public ExpressionList(Expression... expressions) {
        this.expressions = Arrays.asList(expressions);
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    public ExpressionList addExpressions(Expression... elements) {
        List<Expression> list = Optional.ofNullable(getExpressions()).orElseGet(ArrayList::new);
        Collections.addAll(list, elements);
        return withExpressions(list);
    }

    public ExpressionList withExpressions(List<Expression> expressions) {
        this.setExpressions(expressions);
        return this;
    }

    public void setExpressions(List<Expression> expressions) {
        this.expressions = expressions;
    }

    @Override
    public void accept(ItemsListVisitor itemsListVisitor) {
        itemsListVisitor.visit(this);
    }

    @Override
    public String toString() {
        return PlainSelect.getStringList(expressions, true, true);
    }

    public ExpressionList addExpressions(Collection<? extends Expression> expressions) {
        List<Expression> collection = Optional.ofNullable(getExpressions()).orElseGet(ArrayList::new);
        collection.addAll(expressions);
        return this.withExpressions(collection);
    }
}

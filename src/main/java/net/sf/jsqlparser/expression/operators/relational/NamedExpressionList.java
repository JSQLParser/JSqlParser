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

/**
 * A list of named expressions, as in
 * as in select substr('xyzzy' from 2 for 3)
 */
public class NamedExpressionList implements ItemsList {

    private List<Expression> expressions;
    private List<String> names;

    public NamedExpressionList() {
    }

    public NamedExpressionList(List<Expression> expressions) {
        this.expressions = expressions;
    }

    public NamedExpressionList(Expression... expressions) {
        this.expressions = Arrays.asList(expressions);
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    public List<String> getNames() {
        return names;
    }

    public void setExpressions(List<Expression> list) {
        expressions = list;
    }

    public void setNames(List<String> list) {
        names = list;
    }

    @Override
    public void accept(ItemsListVisitor itemsListVisitor) {
        itemsListVisitor.visit(this);
    }

    @Override
    public String toString() {

        StringBuilder ret = new StringBuilder();
        ret.append("(");
        for (int i = 0; i < expressions.size(); i++) {
            if (i > 0) {
                ret.append(" ");
            }
            if (!names.get(i).equals("")) {
                ret.append(names.get(i)).append(" ").append(expressions.get(i));
            } else {
                ret.append(expressions.get(i));
            }
        }
        ret.append(")");

        return ret.toString();
    }

    public NamedExpressionList expressions(List<Expression> expressions) {
        this.setExpressions(expressions);
        return this;
    }

    public NamedExpressionList names(List<String> names) {
        this.setNames(names);
        return this;
    }

    public NamedExpressionList addExpressions(Expression... expressions) {
        List<Expression> collection = Optional.ofNullable(getExpressions()).orElseGet(ArrayList::new);
        Collections.addAll(collection, expressions);
        return this.expressions(collection);
    }

    public NamedExpressionList addExpressions(Collection<? extends Expression> expressions) {
        List<Expression> collection = Optional.ofNullable(getExpressions()).orElseGet(ArrayList::new);
        collection.addAll(expressions);
        return this.expressions(collection);
    }

    public NamedExpressionList addNames(String... names) {
        List<String> collection = Optional.ofNullable(getNames()).orElseGet(ArrayList::new);
        Collections.addAll(collection, names);
        return this.names(collection);
    }

    public NamedExpressionList addNames(Collection<String> names) {
        List<String> collection = Optional.ofNullable(getNames()).orElseGet(ArrayList::new);
        collection.addAll(names);
        return this.names(collection);
    }
}

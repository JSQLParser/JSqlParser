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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A list of named expressions, as in as in select substr('xyzzy' from 2 for 3)
 */
public class NamedExpressionList<T extends Expression> extends ExpressionList<T> {
    private List<String> names;

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> list) {
        names = list;
    }

    @Override
    public String toString() {

        StringBuilder ret = new StringBuilder();
        ret.append("(");
        for (int i = 0; i < size(); i++) {
            if (i > 0) {
                ret.append(" ");
            }
            if (!names.get(i).equals("")) {
                ret.append(names.get(i)).append(" ").append(get(i));
            } else {
                ret.append(get(i));
            }
        }
        ret.append(")");

        return ret.toString();
    }

    public NamedExpressionList withNames(List<String> names) {
        this.setNames(names);
        return this;
    }

    public NamedExpressionList addNames(String... names) {
        List<String> collection = Optional.ofNullable(getNames()).orElseGet(ArrayList::new);
        Collections.addAll(collection, names);
        return this.withNames(collection);
    }

    public NamedExpressionList addNames(Collection<String> names) {
        List<String> collection = Optional.ofNullable(getNames()).orElseGet(ArrayList::new);
        collection.addAll(names);
        return this.withNames(collection);
    }
}

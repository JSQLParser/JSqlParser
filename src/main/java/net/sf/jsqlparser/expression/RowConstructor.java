/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;

public class RowConstructor<T extends Expression> extends ParenthesedExpressionList<T>
        implements Expression {
    private String name = null;

    public RowConstructor() {}

    public RowConstructor(String name, ExpressionList<T> expressionList) {
        this.name = name;
        addAll(expressionList);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return (name != null ? name : "") + super.toString();
    }

    public RowConstructor withName(String name) {
        this.setName(name);
        return this;
    }
}

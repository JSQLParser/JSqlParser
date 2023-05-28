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
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import java.io.Serializable;

public class ExpressionListItem implements Serializable {

    private ExpressionList expressionList;

    private Alias alias;

    public ExpressionList getExpressionList() {
        return expressionList;
    }

    public void setExpressionList(ExpressionList expressionList) {
        this.expressionList = expressionList;
    }

    public Alias getAlias() {
        return alias;
    }

    public void setAlias(Alias alias) {
        this.alias = alias;
    }

    @Override
    public String toString() {
        return expressionList + ((alias != null) ? alias.toString() : "");
    }

    public ExpressionListItem withExpressionList(ExpressionList expressionList) {
        this.setExpressionList(expressionList);
        return this;
    }

    public ExpressionListItem withAlias(Alias alias) {
        this.setAlias(alias);
        return this;
    }
}

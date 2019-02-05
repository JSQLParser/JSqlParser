/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2013 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 * An expression as in "SELECT expr1 AS EXPR"
 */
public class SelectExpressionItem extends ASTNodeAccessImpl implements SelectItem {

    private Expression expression;
    private Alias alias;

    public SelectExpressionItem() {
    }

    public SelectExpressionItem(Expression expression) {
        this.expression = expression;
    }

    public Alias getAlias() {
        return alias;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setAlias(Alias alias) {
        this.alias = alias;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public void accept(SelectItemVisitor selectItemVisitor) {
        selectItemVisitor.visit(this);
    }

    @Override
    public String toString() {
        return expression + ((alias != null) ? alias.toString() : "");
    }
}

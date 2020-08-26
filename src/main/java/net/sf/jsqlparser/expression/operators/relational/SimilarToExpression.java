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

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;

public class SimilarToExpression extends BinaryExpression {

    private boolean not = false;
    private String escape = null;

    public boolean isNot() {
        return not;
    }

    public void setNot(boolean b) {
        not = b;
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    @Override
    public String getStringExpression() {
        return "SIMILAR TO";
    }

    @Override
    public String toString() {
        String retval = getLeftExpression() + " " + (not ? "NOT " : "") + getStringExpression() + " " + getRightExpression();
        if (escape != null) {
            retval += " ESCAPE " + "'" + escape + "'";
        }

        return retval;
    }

    public String getEscape() {
        return escape;
    }

    public void setEscape(String escape) {
        this.escape = escape;
    }

    public SimilarToExpression withEscape(String escape) {
        this.setEscape(escape);
        return this;
    }

    public SimilarToExpression withNot(boolean not) {
        this.setNot(not);
        return this;
    }

    @Override
    public SimilarToExpression withLeftExpression(Expression arg0) {
        return (SimilarToExpression) super.withLeftExpression(arg0);
    }

    @Override
    public SimilarToExpression withRightExpression(Expression arg0) {
        return (SimilarToExpression) super.withRightExpression(arg0);
    }
}

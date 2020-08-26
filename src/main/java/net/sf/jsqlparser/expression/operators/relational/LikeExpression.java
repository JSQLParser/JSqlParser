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

public class LikeExpression extends BinaryExpression {

    private boolean not = false;
    private String escape = null;
    private boolean caseInsensitive = false;

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
        return caseInsensitive ? "ILIKE" : "LIKE";
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

    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }

    public void setCaseInsensitive(boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }

    public LikeExpression withEscape(String escape) {
        this.setEscape(escape);
        return this;
    }

    public LikeExpression withCaseInsensitive(boolean caseInsensitive) {
        this.setCaseInsensitive(caseInsensitive);
        return this;
    }

    public LikeExpression withNot(boolean not) {
        this.setNot(not);
        return this;
    }

    @Override
    public LikeExpression withLeftExpression(Expression arg0) {
        return (LikeExpression) super.withLeftExpression(arg0);
    }

    @Override
    public LikeExpression withRightExpression(Expression arg0) {
        return (LikeExpression) super.withRightExpression(arg0);
    }
}

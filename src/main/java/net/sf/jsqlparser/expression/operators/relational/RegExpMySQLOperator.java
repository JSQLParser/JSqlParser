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
import net.sf.jsqlparser.expression.ExpressionVisitor;

public class RegExpMySQLOperator extends BinaryExpression {

    private RegExpMatchOperatorType operatorType;
    private boolean useRLike = false;

    public RegExpMySQLOperator(RegExpMatchOperatorType operatorType) {
        if (operatorType == null) {
            throw new NullPointerException();
        }
        this.operatorType = operatorType;
    }

    public RegExpMatchOperatorType getOperatorType() {
        return operatorType;
    }

    public boolean isUseRLike() {
        return useRLike;
    }

    public RegExpMySQLOperator useRLike() {
        useRLike = true;
        return this;
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    @Override
    public String getStringExpression() {
        return (useRLike ? "RLIKE" : "REGEXP")
                + (operatorType == RegExpMatchOperatorType.MATCH_CASESENSITIVE ? " BINARY" : "");
    }
}

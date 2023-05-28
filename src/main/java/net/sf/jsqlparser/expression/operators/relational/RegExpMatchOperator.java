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

import java.util.Objects;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;

public class RegExpMatchOperator extends BinaryExpression {

    private RegExpMatchOperatorType operatorType;

    public RegExpMatchOperator(RegExpMatchOperatorType operatorType) {
        this.operatorType = Objects.requireNonNull(operatorType, "The provided RegExpMatchOperatorType must not be NULL.");
    }

    public RegExpMatchOperatorType getOperatorType() {
        return operatorType;
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    @Override
    public String getStringExpression() {
        switch(operatorType) {
            case MATCH_CASESENSITIVE:
                return "~";
            case MATCH_CASEINSENSITIVE:
                return "~*";
            case NOT_MATCH_CASESENSITIVE:
                return "!~";
            case NOT_MATCH_CASEINSENSITIVE:
                return "!~*";
            default:
                break;
        }
        return null;
    }

    @Override
    public RegExpMatchOperator withLeftExpression(Expression arg0) {
        return (RegExpMatchOperator) super.withLeftExpression(arg0);
    }

    @Override
    public RegExpMatchOperator withRightExpression(Expression arg0) {
        return (RegExpMatchOperator) super.withRightExpression(arg0);
    }
}

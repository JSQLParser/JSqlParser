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

public abstract class ComparisonOperator extends OldOracleJoinBinaryExpression {

    private final String operator;

    public ComparisonOperator(String operator) {
        this.operator = operator;
    }

    @Override
    public String getStringExpression() {
        return operator;
    }
}

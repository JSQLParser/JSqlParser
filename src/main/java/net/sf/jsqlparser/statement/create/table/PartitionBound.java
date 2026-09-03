/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.table;

import java.io.Serializable;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;

/** A PostgreSQL declarative-partition bound. */
public class PartitionBound implements Serializable {

    public enum Type {
        RANGE, LIST, HASH, DEFAULT
    }

    private Type type;
    private ExpressionList<Expression> fromExpressions;
    private ExpressionList<Expression> toExpressions;
    private ExpressionList<Expression> inExpressions;
    private Expression modulus;
    private Expression remainder;

    public PartitionBound() {}

    public PartitionBound(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public ExpressionList<Expression> getFromExpressions() {
        return fromExpressions;
    }

    public void setFromExpressions(ExpressionList<Expression> fromExpressions) {
        this.fromExpressions = fromExpressions;
    }

    public ExpressionList<Expression> getToExpressions() {
        return toExpressions;
    }

    public void setToExpressions(ExpressionList<Expression> toExpressions) {
        this.toExpressions = toExpressions;
    }

    public ExpressionList<Expression> getInExpressions() {
        return inExpressions;
    }

    public void setInExpressions(ExpressionList<Expression> inExpressions) {
        this.inExpressions = inExpressions;
    }

    public Expression getModulus() {
        return modulus;
    }

    public void setModulus(Expression modulus) {
        this.modulus = modulus;
    }

    public Expression getRemainder() {
        return remainder;
    }

    public void setRemainder(Expression remainder) {
        this.remainder = remainder;
    }

    public PartitionBound withType(Type type) {
        setType(type);
        return this;
    }

    public PartitionBound withFromExpressions(ExpressionList<Expression> fromExpressions) {
        setFromExpressions(fromExpressions);
        return this;
    }

    public PartitionBound withToExpressions(ExpressionList<Expression> toExpressions) {
        setToExpressions(toExpressions);
        return this;
    }

    public PartitionBound withInExpressions(ExpressionList<Expression> inExpressions) {
        setInExpressions(inExpressions);
        return this;
    }

    public PartitionBound withModulus(Expression modulus) {
        setModulus(modulus);
        return this;
    }

    public PartitionBound withRemainder(Expression remainder) {
        setRemainder(remainder);
        return this;
    }

    @Override
    public String toString() {
        switch (type) {
            case RANGE:
                return "FOR VALUES FROM (" + fromExpressions + ") TO (" + toExpressions + ")";
            case LIST:
                return "FOR VALUES IN (" + inExpressions + ")";
            case HASH:
                return "FOR VALUES WITH (MODULUS " + modulus + ", REMAINDER " + remainder + ")";
            case DEFAULT:
                return "DEFAULT";
            default:
                return "";
        }
    }
}

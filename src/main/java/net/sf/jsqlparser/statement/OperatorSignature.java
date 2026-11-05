/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ExclusionOperator;

/**
 * A PostgreSQL operator, named by its possibly schema-qualified symbol and its operand types:
 * {@code [schema.]op ({left_type | NONE}, {right_type | NONE})}, as in
 * {@code DROP OPERATOR ~ (NONE, bigint)}. A null operand type is the NONE marker of a missing
 * operand, and is rendered as {@code NONE}.
 */
public class OperatorSignature extends ASTNodeAccessImpl {

    private ExclusionOperator operator;
    private ColDataType leftType;
    private ColDataType rightType;

    public OperatorSignature() {
        // the operator and its operand types are set by the parser or the setters
    }

    public OperatorSignature(ExclusionOperator operator, ColDataType leftType,
            ColDataType rightType) {
        this.operator = operator;
        this.leftType = leftType;
        this.rightType = rightType;
    }

    /** The operator symbol and its schema, read by the reference COMMENT ON OPERATOR uses. */
    public ExclusionOperator getOperator() {
        return operator;
    }

    public void setOperator(ExclusionOperator operator) {
        this.operator = operator;
    }

    public OperatorSignature withOperator(ExclusionOperator operator) {
        setOperator(operator);
        return this;
    }

    /** The left operand type, or null for NONE, the missing left operand of a prefix operator. */
    public ColDataType getLeftType() {
        return leftType;
    }

    public void setLeftType(ColDataType leftType) {
        this.leftType = leftType;
    }

    public OperatorSignature withLeftType(ColDataType leftType) {
        setLeftType(leftType);
        return this;
    }

    /** The right operand type, or null for NONE. */
    public ColDataType getRightType() {
        return rightType;
    }

    public void setRightType(ColDataType rightType) {
        this.rightType = rightType;
    }

    public OperatorSignature withRightType(ColDataType rightType) {
        setRightType(rightType);
        return this;
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        return builder.append(operator).append(" (").append(operand(leftType)).append(", ")
                .append(operand(rightType)).append(')');
    }

    private static Object operand(ColDataType type) {
        return type == null ? "NONE" : type;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}

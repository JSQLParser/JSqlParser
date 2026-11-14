/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.operator;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import net.sf.jsqlparser.statement.RoutineReference;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ExclusionOperator;

/**
 * One member of a PostgreSQL operator class or family, as listed by CREATE OPERATOR CLASS and by
 * ALTER OPERATOR FAMILY ... ADD or DROP:
 * <ul>
 * <li>{@code OPERATOR n op [(left, right)] [FOR SEARCH | FOR ORDER BY sort_family]}</li>
 * <li>{@code FUNCTION n [(type [, ...])] fn [(argument [, ...])]}</li>
 * <li>{@code STORAGE type}</li>
 * <li>{@code OPERATOR n (type [, ...])} and {@code FUNCTION n (type [, ...])}, which name the
 * member that ALTER OPERATOR FAMILY ... DROP removes.</li>
 * </ul>
 */
public class OperatorClassItem implements Serializable {
    public enum Kind {
        OPERATOR, FUNCTION, STORAGE
    }

    /** What an operator of an operator class is used for. */
    public enum Purpose {
        SEARCH, ORDER_BY
    }

    private Kind kind;
    private Integer number;
    private ExclusionOperator operator;
    private List<ColDataType> operandTypes;
    private Purpose purpose;
    private String sortFamily;
    private RoutineReference function;
    private ColDataType storageType;

    public OperatorClassItem() {
        // the parser or the setters fill the item
    }

    public OperatorClassItem(Kind kind) {
        this.kind = kind;
    }

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }

    /**
     * The strategy number of an operator or the support number of a function.
     *
     * @return the number, or {@code null} for {@code STORAGE}
     */
    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    /**
     * The operator of an {@code OPERATOR} member.
     *
     * @return the operator, or {@code null} for another kind and for a member to drop
     */
    public ExclusionOperator getOperator() {
        return operator;
    }

    public void setOperator(ExclusionOperator operator) {
        this.operator = operator;
    }

    /**
     * The operand types in parentheses. After an operator they are its left and right operand type,
     * where {@code null} stands for {@code NONE}.
     *
     * @return the types, or {@code null} when the member has no such list
     */
    public List<ColDataType> getOperandTypes() {
        return operandTypes;
    }

    public void setOperandTypes(List<ColDataType> operandTypes) {
        this.operandTypes = operandTypes;
    }

    /**
     * The purpose of an operator, {@code FOR SEARCH} or {@code FOR ORDER BY sort_family}.
     *
     * @return the purpose, or {@code null} when none is written
     */
    public Purpose getPurpose() {
        return purpose;
    }

    public void setPurpose(Purpose purpose) {
        this.purpose = purpose;
    }

    /**
     * The operator family of {@code FOR ORDER BY}.
     *
     * @return the family name, or {@code null}
     */
    public String getSortFamily() {
        return sortFamily;
    }

    public void setSortFamily(String sortFamily) {
        this.sortFamily = sortFamily;
    }

    /**
     * The function of a {@code FUNCTION} member. Its arguments are {@code null} when the function
     * is written without an argument list.
     *
     * @return the function, or {@code null} for another kind and for a member to drop
     */
    public RoutineReference getFunction() {
        return function;
    }

    public void setFunction(RoutineReference function) {
        this.function = function;
    }

    public ColDataType getStorageType() {
        return storageType;
    }

    public void setStorageType(ColDataType storageType) {
        this.storageType = storageType;
    }

    /**
     * Renders a member list.
     *
     * @param items the members
     * @return the members, separated by commas
     */
    public static String toString(List<OperatorClassItem> items) {
        return items.stream().map(Object::toString).collect(Collectors.joining(", "));
    }

    private void appendOperandTypes(StringBuilder builder) {
        if (operandTypes != null) {
            builder.append(" (").append(operandTypes.stream()
                    .map(type -> type == null ? "NONE" : type.toString())
                    .collect(Collectors.joining(", "))).append(')');
        }
    }

    @Override
    public String toString() {
        if (kind == Kind.STORAGE) {
            return "STORAGE " + storageType;
        }
        StringBuilder builder = new StringBuilder(kind.name()).append(' ').append(number);
        if (operator != null) {
            builder.append(' ').append(operator.getQualifiedName());
        }
        appendOperandTypes(builder);
        if (function != null) {
            builder.append(' ').append(function);
        }
        if (purpose == Purpose.SEARCH) {
            builder.append(" FOR SEARCH");
        } else if (purpose == Purpose.ORDER_BY) {
            builder.append(" FOR ORDER BY ").append(sortFamily);
        }
        return builder.toString();
    }
}

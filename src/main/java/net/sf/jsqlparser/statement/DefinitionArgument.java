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

import java.io.Serializable;
import java.util.Objects;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.statement.create.table.ColDataType;

/**
 * The value of a {@link DefinitionElement}: PostgreSQL's {@code def_arg}. The grammar reads a name,
 * such as a function or a storage strategy, as a type name, so it is a {@link Kind#TYPE} argument.
 */
public final class DefinitionArgument implements Serializable {
    public enum Kind {
        /** A type or a name, see {@link #getDataType()}. */
        TYPE,
        /** A reserved keyword such as {@code TRUE} or {@code NULL}, see {@link #getKeyword()}. */
        KEYWORD,
        /** An operator, see {@link #getOperator()}. */
        OPERATOR,
        /** A number with an optional sign, see {@link #getNumber()}. */
        NUMBER,
        /** A string constant, see {@link #getString()}. */
        STRING,
        /** The keyword {@code NONE}. */
        NONE
    }

    private final Kind kind;
    private ColDataType dataType;
    private boolean setOf;
    private boolean typeReference;
    private String keyword;
    private String operator;
    private boolean operatorKeyword;
    private Expression number;
    private StringValue string;

    private DefinitionArgument(Kind kind) {
        this.kind = kind;
    }

    public static DefinitionArgument type(ColDataType dataType) {
        return type(dataType, false, false);
    }

    /**
     * A type argument.
     *
     * @param dataType the type or name
     * @param setOf whether {@code SETOF} precedes the type
     * @param typeReference whether {@code %TYPE} follows the name of a column
     * @return the argument
     */
    public static DefinitionArgument type(ColDataType dataType, boolean setOf,
            boolean typeReference) {
        DefinitionArgument argument = new DefinitionArgument(Kind.TYPE);
        argument.dataType = Objects.requireNonNull(dataType);
        argument.setOf = setOf;
        argument.typeReference = typeReference;
        return argument;
    }

    public static DefinitionArgument keyword(String keyword) {
        DefinitionArgument argument = new DefinitionArgument(Kind.KEYWORD);
        argument.keyword = Objects.requireNonNull(keyword);
        return argument;
    }

    /**
     * An operator argument.
     *
     * @param operator the operator, with its schema when written as {@code OPERATOR(schema.op)}
     * @param operatorKeyword whether the operator is written as {@code OPERATOR(...)}
     * @return the argument
     */
    public static DefinitionArgument operator(String operator, boolean operatorKeyword) {
        DefinitionArgument argument = new DefinitionArgument(Kind.OPERATOR);
        argument.operator = Objects.requireNonNull(operator);
        argument.operatorKeyword = operatorKeyword;
        return argument;
    }

    public static DefinitionArgument number(Expression number) {
        DefinitionArgument argument = new DefinitionArgument(Kind.NUMBER);
        argument.number = Objects.requireNonNull(number);
        return argument;
    }

    public static DefinitionArgument string(StringValue string) {
        DefinitionArgument argument = new DefinitionArgument(Kind.STRING);
        argument.string = Objects.requireNonNull(string);
        return argument;
    }

    public static DefinitionArgument none() {
        return new DefinitionArgument(Kind.NONE);
    }

    public Kind getKind() {
        return kind;
    }

    /**
     * The type of a {@link Kind#TYPE} argument. A plain name, such as the function of
     * {@code INPUT = box3_in}, is the type's {@link ColDataType#getDataType() name}.
     *
     * @return the type, or {@code null} for another kind
     */
    public ColDataType getDataType() {
        return dataType;
    }

    public boolean isSetOf() {
        return setOf;
    }

    /**
     * Whether {@code %TYPE} follows the name, which then names a column.
     *
     * @return {@code true} for {@code table.column%TYPE}
     */
    public boolean isTypeReference() {
        return typeReference;
    }

    public String getKeyword() {
        return keyword;
    }

    public String getOperator() {
        return operator;
    }

    public boolean isOperatorKeyword() {
        return operatorKeyword;
    }

    /**
     * The number of a {@link Kind#NUMBER} argument: a long or double value, inside a signed
     * expression when a sign is written.
     *
     * @return the number, or {@code null} for another kind
     */
    public Expression getNumber() {
        return number;
    }

    public StringValue getString() {
        return string;
    }

    @Override
    public String toString() {
        switch (kind) {
            case TYPE:
                return (setOf ? "SETOF " : "") + dataType + (typeReference ? "%TYPE" : "");
            case KEYWORD:
                return keyword;
            case OPERATOR:
                return operatorKeyword ? "OPERATOR(" + operator + ")" : operator;
            case NUMBER:
                return number.toString();
            case STRING:
                return string.toString();
            default:
                return "NONE";
        }
    }
}

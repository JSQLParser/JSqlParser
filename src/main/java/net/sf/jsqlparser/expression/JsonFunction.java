/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import java.util.ArrayList;
import java.util.Objects;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 * Represents a JSON-Function.<br>
 * Currently supported are the types in {@link JsonFunctionType}.<br>
 * <br>
 * For JSON_OBJECT the parameters are available from {@link #getKeyValuePairs()}<br>
 * <br>
 * For JSON_ARRAY the parameters are availble from {@link #getExpressions()}.<br>
 *
 * @author <a href="mailto:andreas@manticore-projects.com">Andreas Reichel</a>
 */
public class JsonFunction extends ASTNodeAccessImpl implements Expression {
    private final ArrayList<JsonKeyValuePair> keyValuePairs = new ArrayList<>();
    private final ArrayList<JsonFunctionExpression> expressions = new ArrayList<>();
    private JsonFunctionType functionType;
    private JsonAggregateOnNullType onNullType;
    private JsonAggregateUniqueKeysType uniqueKeysType;

    private boolean isStrict = false;

    public JsonFunction() {}

    public JsonFunction(JsonFunctionType functionType) {
        this.functionType = functionType;
    }

    /**
     * Returns the Parameters of an JSON_OBJECT<br>
     * The KeyValuePairs may not have both key and value set, in some cases only the Key is set.
     *
     * @see net.sf.jsqlparser.parser.feature.Feature#allowCommaAsKeyValueSeparator
     *
     * @return A List of KeyValuePairs, never NULL
     */
    public ArrayList<JsonKeyValuePair> getKeyValuePairs() {
        return keyValuePairs;
    }

    /**
     * Returns the parameters of JSON_ARRAY<br>
     *
     * @return A List of {@link JsonFunctionExpression}s, never NULL
     */
    public ArrayList<JsonFunctionExpression> getExpressions() {
        return expressions;
    }

    public JsonKeyValuePair getKeyValuePair(int i) {
        return keyValuePairs.get(i);
    }

    public JsonFunctionExpression getExpression(int i) {
        return expressions.get(i);
    }

    public boolean add(JsonKeyValuePair keyValuePair) {
        return keyValuePairs.add(keyValuePair);
    }

    public void add(int i, JsonKeyValuePair keyValuePair) {
        keyValuePairs.add(i, keyValuePair);
    }

    public boolean add(JsonFunctionExpression expression) {
        return expressions.add(expression);
    }

    public void add(int i, JsonFunctionExpression expression) {
        expressions.add(i, expression);
    }

    public boolean isEmpty() {
        return keyValuePairs.isEmpty();
    }

    public JsonAggregateOnNullType getOnNullType() {
        return onNullType;
    }

    public void setOnNullType(JsonAggregateOnNullType onNullType) {
        this.onNullType = onNullType;
    }

    public JsonFunction withOnNullType(JsonAggregateOnNullType onNullType) {
        this.setOnNullType(onNullType);
        return this;
    }

    public JsonAggregateUniqueKeysType getUniqueKeysType() {
        return uniqueKeysType;
    }

    public void setUniqueKeysType(JsonAggregateUniqueKeysType uniqueKeysType) {
        this.uniqueKeysType = uniqueKeysType;
    }

    public JsonFunction withUniqueKeysType(JsonAggregateUniqueKeysType uniqueKeysType) {
        this.setUniqueKeysType(uniqueKeysType);
        return this;
    }

    public JsonFunctionType getType() {
        return functionType;
    }

    public void setType(JsonFunctionType type) {
        this.functionType =
                Objects.requireNonNull(type,
                        "The Type of the JSON Aggregate Function must not be null");
    }

    public void setType(String typeName) {
        this.functionType = JsonFunctionType.valueOf(
                Objects.requireNonNull(typeName,
                        "The Type of the JSON Aggregate Function must not be null")
                        .toUpperCase());
    }

    public JsonFunction withType(JsonFunctionType type) {
        this.setType(type);
        return this;
    }

    public JsonFunction withType(String typeName) {
        this.setType(typeName);
        return this;
    }

    public boolean isStrict() {
        return isStrict;
    }

    public void setStrict(boolean strict) {
        isStrict = strict;
    }

    public JsonFunction withStrict(boolean strict) {
        this.setStrict(strict);
        return this;
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }

    // avoid countless Builder --> String conversion
    public StringBuilder append(StringBuilder builder) {
        switch (functionType) {
            case OBJECT:
            case POSTGRES_OBJECT:
            case MYSQL_OBJECT:
                appendObject(builder);
                break;
            case ARRAY:
                appendArray(builder);
                break;
            default:
                // this should never happen really
        }
        return builder;
    }

    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public StringBuilder appendObject(StringBuilder builder) {
        builder.append("JSON_OBJECT( ");
        int i = 0;
        for (JsonKeyValuePair keyValuePair : keyValuePairs) {
            if (i > 0) {
                builder.append(", ");
            }
            if (keyValuePair.isUsingKeyKeyword()
                    && keyValuePair.getSeparator() == JsonKeyValuePairSeparator.VALUE) {
                builder.append("KEY ");
            }
            builder.append(keyValuePair.getKey());

            if (keyValuePair.getValue() != null) {
                builder.append(keyValuePair.getSeparator().getSeparatorString());
                builder.append(keyValuePair.getValue());
            }

            if (keyValuePair.isUsingFormatJson()) {
                builder.append(" FORMAT JSON");
            }
            i++;
        }

        appendOnNullType(builder);
        if (isStrict) {
            builder.append(" STRICT");
        }
        appendUniqueKeys(builder);

        builder.append(" ) ");

        return builder;
    }

    private void appendOnNullType(StringBuilder builder) {
        if (onNullType != null) {
            switch (onNullType) {
                case NULL:
                    builder.append(" NULL ON NULL");
                    break;
                case ABSENT:
                    builder.append(" ABSENT ON NULL");
                    break;
                default:
                    // this should never happen
            }
        }
    }

    private void appendUniqueKeys(StringBuilder builder) {
        if (uniqueKeysType != null) {
            switch (uniqueKeysType) {
                case WITH:
                    builder.append(" WITH UNIQUE KEYS");
                    break;
                case WITHOUT:
                    builder.append(" WITHOUT UNIQUE KEYS");
                    break;
                default:
                    // this should never happen
            }
        }
    }

    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public StringBuilder appendArray(StringBuilder builder) {
        builder.append("JSON_ARRAY( ");
        int i = 0;

        for (JsonFunctionExpression expr : expressions) {
            if (i > 0) {
                builder.append(", ");
            }
            expr.append(builder);
            i++;
        }

        appendOnNullType(builder);
        builder.append(") ");

        return builder;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return append(builder).toString();
    }
}

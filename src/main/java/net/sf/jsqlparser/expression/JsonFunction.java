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
import net.sf.jsqlparser.statement.create.table.ColDataType;

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
    public enum JsonOnResponseBehaviorType {
        ERROR, NULL, DEFAULT, EMPTY_ARRAY, EMPTY_OBJECT, TRUE, FALSE, UNKNOWN
    }

    public enum JsonWrapperType {
        WITHOUT, WITH
    }

    public enum JsonWrapperMode {
        CONDITIONAL, UNCONDITIONAL
    }

    public enum JsonQuotesType {
        KEEP, OMIT
    }

    public static class JsonOnResponseBehavior {
        private JsonOnResponseBehaviorType type;
        private Expression expression;

        public JsonOnResponseBehavior(JsonOnResponseBehaviorType type) {
            this(type, null);
        }

        public JsonOnResponseBehavior(JsonOnResponseBehaviorType type, Expression expression) {
            this.type = type;
            this.expression = expression;
        }

        public JsonOnResponseBehaviorType getType() {
            return type;
        }

        public void setType(JsonOnResponseBehaviorType type) {
            this.type = type;
        }

        public Expression getExpression() {
            return expression;
        }

        public void setExpression(Expression expression) {
            this.expression = expression;
        }

        public StringBuilder append(StringBuilder builder) {
            switch (type) {
                case ERROR:
                    builder.append("ERROR");
                    break;
                case NULL:
                    builder.append("NULL");
                    break;
                case DEFAULT:
                    builder.append("DEFAULT ").append(expression);
                    break;
                case EMPTY_ARRAY:
                    builder.append("EMPTY ARRAY");
                    break;
                case EMPTY_OBJECT:
                    builder.append("EMPTY OBJECT");
                    break;
                case TRUE:
                    builder.append("TRUE");
                    break;
                case FALSE:
                    builder.append("FALSE");
                    break;
                case UNKNOWN:
                    builder.append("UNKNOWN");
                    break;
                default:
                    // this should never happen
            }
            return builder;
        }

        @Override
        public String toString() {
            return append(new StringBuilder()).toString();
        }
    }

    private final ArrayList<JsonKeyValuePair> keyValuePairs = new ArrayList<>();
    private final ArrayList<JsonFunctionExpression> expressions = new ArrayList<>();
    private final ArrayList<Expression> passingExpressions = new ArrayList<>();
    private final ArrayList<String> additionalQueryPathArguments = new ArrayList<>();
    private JsonFunctionType functionType;
    private JsonAggregateOnNullType onNullType;
    private JsonAggregateUniqueKeysType uniqueKeysType;

    private boolean isStrict = false;
    private JsonFunctionExpression inputExpression;
    private Expression jsonPathExpression;
    private ColDataType returningType;
    private boolean returningFormatJson;
    private String returningEncoding;
    private JsonOnResponseBehavior onEmptyBehavior;
    private JsonOnResponseBehavior onErrorBehavior;
    private JsonWrapperType wrapperType;
    private JsonWrapperMode wrapperMode;
    private boolean wrapperArray;
    private JsonQuotesType quotesType;
    private boolean quotesOnScalarString;

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

    public ArrayList<Expression> getPassingExpressions() {
        return passingExpressions;
    }

    public boolean addPassingExpression(Expression expression) {
        return passingExpressions.add(expression);
    }

    public ArrayList<String> getAdditionalQueryPathArguments() {
        return additionalQueryPathArguments;
    }

    public boolean addAdditionalQueryPathArgument(String argument) {
        return additionalQueryPathArguments.add(argument);
    }

    public JsonFunctionExpression getInputExpression() {
        return inputExpression;
    }

    public void setInputExpression(JsonFunctionExpression inputExpression) {
        this.inputExpression = inputExpression;
    }

    public Expression getJsonPathExpression() {
        return jsonPathExpression;
    }

    public void setJsonPathExpression(Expression jsonPathExpression) {
        this.jsonPathExpression = jsonPathExpression;
    }

    public ColDataType getReturningType() {
        return returningType;
    }

    public void setReturningType(ColDataType returningType) {
        this.returningType = returningType;
    }

    public boolean isReturningFormatJson() {
        return returningFormatJson;
    }

    public void setReturningFormatJson(boolean returningFormatJson) {
        this.returningFormatJson = returningFormatJson;
    }

    public String getReturningEncoding() {
        return returningEncoding;
    }

    public void setReturningEncoding(String returningEncoding) {
        this.returningEncoding = returningEncoding;
    }

    public JsonOnResponseBehavior getOnEmptyBehavior() {
        return onEmptyBehavior;
    }

    public void setOnEmptyBehavior(JsonOnResponseBehavior onEmptyBehavior) {
        this.onEmptyBehavior = onEmptyBehavior;
    }

    public JsonOnResponseBehavior getOnErrorBehavior() {
        return onErrorBehavior;
    }

    public void setOnErrorBehavior(JsonOnResponseBehavior onErrorBehavior) {
        this.onErrorBehavior = onErrorBehavior;
    }

    public JsonWrapperType getWrapperType() {
        return wrapperType;
    }

    public void setWrapperType(JsonWrapperType wrapperType) {
        this.wrapperType = wrapperType;
    }

    public JsonWrapperMode getWrapperMode() {
        return wrapperMode;
    }

    public void setWrapperMode(JsonWrapperMode wrapperMode) {
        this.wrapperMode = wrapperMode;
    }

    public boolean isWrapperArray() {
        return wrapperArray;
    }

    public void setWrapperArray(boolean wrapperArray) {
        this.wrapperArray = wrapperArray;
    }

    public JsonQuotesType getQuotesType() {
        return quotesType;
    }

    public void setQuotesType(JsonQuotesType quotesType) {
        this.quotesType = quotesType;
    }

    public boolean isQuotesOnScalarString() {
        return quotesOnScalarString;
    }

    public void setQuotesOnScalarString(boolean quotesOnScalarString) {
        this.quotesOnScalarString = quotesOnScalarString;
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
            case VALUE:
                appendValue(builder);
                break;
            case QUERY:
                appendQuery(builder);
                break;
            case EXISTS:
                appendExists(builder);
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
            keyValuePair.append(builder);
            i++;
        }

        appendOnNullType(builder);
        if (isStrict) {
            builder.append(" STRICT");
        }
        appendUniqueKeys(builder);
        appendReturningClause(builder, true);

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
        appendReturningClause(builder, true);
        builder.append(") ");

        return builder;
    }

    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public StringBuilder appendValue(StringBuilder builder) {
        builder.append("JSON_VALUE(");
        appendValueOrQueryPrefix(builder);

        if (returningType != null) {
            builder.append(" RETURNING ").append(returningType);
        }

        appendOnResponseClause(builder, onEmptyBehavior, "EMPTY");
        appendOnResponseClause(builder, onErrorBehavior, "ERROR");

        builder.append(")");
        return builder;
    }

    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public StringBuilder appendQuery(StringBuilder builder) {
        builder.append("JSON_QUERY(");
        appendValueOrQueryPrefix(builder);

        appendReturningClause(builder, true);

        appendWrapperClause(builder);
        appendQuotesClause(builder);
        appendOnResponseClause(builder, onEmptyBehavior, "EMPTY");
        appendOnResponseClause(builder, onErrorBehavior, "ERROR");

        for (String additionalQueryPathArgument : additionalQueryPathArguments) {
            builder.append(", ").append(additionalQueryPathArgument);
        }

        builder.append(")");
        return builder;
    }

    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public StringBuilder appendExists(StringBuilder builder) {
        builder.append("JSON_EXISTS(");
        appendValueOrQueryPrefix(builder);
        appendOnResponseClause(builder, onErrorBehavior, "ERROR");
        builder.append(")");
        return builder;
    }

    private void appendValueOrQueryPrefix(StringBuilder builder) {
        if (inputExpression != null) {
            inputExpression.append(builder);
        }

        if (jsonPathExpression != null) {
            if (inputExpression != null) {
                builder.append(", ");
            }
            builder.append(jsonPathExpression);
        }

        if (!passingExpressions.isEmpty()) {
            builder.append(" PASSING ");
            boolean comma = false;
            for (Expression passingExpression : passingExpressions) {
                if (comma) {
                    builder.append(", ");
                } else {
                    comma = true;
                }
                builder.append(passingExpression);
            }
        }
    }

    private void appendOnResponseClause(StringBuilder builder, JsonOnResponseBehavior behavior,
            String clause) {
        if (behavior != null) {
            builder.append(" ");
            behavior.append(builder);
            builder.append(" ON ").append(clause);
        }
    }

    private void appendReturningClause(StringBuilder builder, boolean formatJsonAllowed) {
        if (returningType != null) {
            builder.append(" RETURNING ").append(returningType);
            if (formatJsonAllowed && returningFormatJson) {
                builder.append(" FORMAT JSON");
                if (returningEncoding != null) {
                    builder.append(" ENCODING ").append(returningEncoding);
                }
            }
        }
    }

    private void appendWrapperClause(StringBuilder builder) {
        if (wrapperType != null) {
            builder.append(" ").append(wrapperType);
            if (wrapperMode != null) {
                builder.append(" ").append(wrapperMode);
            }
            if (wrapperArray) {
                builder.append(" ARRAY");
            }
            builder.append(" WRAPPER");
        }
    }

    private void appendQuotesClause(StringBuilder builder) {
        if (quotesType != null) {
            builder.append(" ").append(quotesType).append(" QUOTES");
            if (quotesOnScalarString) {
                builder.append(" ON SCALAR STRING");
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return append(builder).toString();
    }
}

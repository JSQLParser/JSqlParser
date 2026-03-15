/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.create.table.ColDataType;

public class JsonTableFunction extends Function {
    public enum JsonTablePlanOperator {
        COMMA(", "), INNER(" INNER "), OUTER(" OUTER "), CROSS(" CROSS "), UNION(" UNION ");

        private final String display;

        JsonTablePlanOperator(String display) {
            this.display = display;
        }

        public String getDisplay() {
            return display;
        }
    }

    public enum JsonTableOnErrorType {
        ERROR, EMPTY
    }

    public static class JsonTablePassingClause extends ASTNodeAccessImpl implements Serializable {
        private Expression valueExpression;
        private String parameterName;

        public JsonTablePassingClause() {}

        public JsonTablePassingClause(Expression valueExpression, String parameterName) {
            this.valueExpression = valueExpression;
            this.parameterName = parameterName;
        }

        public Expression getValueExpression() {
            return valueExpression;
        }

        public JsonTablePassingClause setValueExpression(Expression valueExpression) {
            this.valueExpression = valueExpression;
            return this;
        }

        public String getParameterName() {
            return parameterName;
        }

        public JsonTablePassingClause setParameterName(String parameterName) {
            this.parameterName = parameterName;
            return this;
        }

        public void collectExpressions(List<Expression> expressions) {
            if (valueExpression != null) {
                expressions.add(valueExpression);
            }
        }

        @Override
        public String toString() {
            return valueExpression + " AS " + parameterName;
        }
    }

    public static class JsonTableWrapperClause extends ASTNodeAccessImpl implements Serializable {
        private JsonFunction.JsonWrapperType wrapperType;
        private JsonFunction.JsonWrapperMode wrapperMode;
        private boolean array;

        public JsonFunction.JsonWrapperType getWrapperType() {
            return wrapperType;
        }

        public JsonTableWrapperClause setWrapperType(JsonFunction.JsonWrapperType wrapperType) {
            this.wrapperType = wrapperType;
            return this;
        }

        public JsonFunction.JsonWrapperMode getWrapperMode() {
            return wrapperMode;
        }

        public JsonTableWrapperClause setWrapperMode(JsonFunction.JsonWrapperMode wrapperMode) {
            this.wrapperMode = wrapperMode;
            return this;
        }

        public boolean isArray() {
            return array;
        }

        public JsonTableWrapperClause setArray(boolean array) {
            this.array = array;
            return this;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(wrapperType);
            if (wrapperMode != null) {
                builder.append(" ").append(wrapperMode);
            }
            if (array) {
                builder.append(" ARRAY");
            }
            builder.append(" WRAPPER");
            return builder.toString();
        }
    }

    public static class JsonTableQuotesClause extends ASTNodeAccessImpl implements Serializable {
        private JsonFunction.JsonQuotesType quotesType;
        private boolean onScalarString;

        public JsonFunction.JsonQuotesType getQuotesType() {
            return quotesType;
        }

        public JsonTableQuotesClause setQuotesType(JsonFunction.JsonQuotesType quotesType) {
            this.quotesType = quotesType;
            return this;
        }

        public boolean isOnScalarString() {
            return onScalarString;
        }

        public JsonTableQuotesClause setOnScalarString(boolean onScalarString) {
            this.onScalarString = onScalarString;
            return this;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(quotesType).append(" QUOTES");
            if (onScalarString) {
                builder.append(" ON SCALAR STRING");
            }
            return builder.toString();
        }
    }

    public static class JsonTableOnErrorClause extends ASTNodeAccessImpl implements Serializable {
        private JsonTableOnErrorType type;

        public JsonTableOnErrorType getType() {
            return type;
        }

        public JsonTableOnErrorClause setType(JsonTableOnErrorType type) {
            this.type = type;
            return this;
        }

        @Override
        public String toString() {
            return type + " ON ERROR";
        }
    }

    public static class JsonTablePlanTerm extends ASTNodeAccessImpl implements Serializable {
        private JsonTablePlanExpression nestedPlanExpression;
        private String name;
        private Expression expression;

        public JsonTablePlanExpression getNestedPlanExpression() {
            return nestedPlanExpression;
        }

        public JsonTablePlanTerm setNestedPlanExpression(
                JsonTablePlanExpression nestedPlanExpression) {
            this.nestedPlanExpression = nestedPlanExpression;
            return this;
        }

        public String getName() {
            return name;
        }

        public JsonTablePlanTerm setName(String name) {
            this.name = name;
            return this;
        }

        public Expression getExpression() {
            return expression;
        }

        public JsonTablePlanTerm setExpression(Expression expression) {
            this.expression = expression;
            return this;
        }

        public void collectExpressions(List<Expression> expressions) {
            if (expression != null) {
                expressions.add(expression);
            }
            if (nestedPlanExpression != null) {
                nestedPlanExpression.collectExpressions(expressions);
            }
        }

        @Override
        public String toString() {
            if (nestedPlanExpression != null) {
                return "(" + nestedPlanExpression + ")";
            }
            if (name != null) {
                return name;
            }
            return expression != null ? expression.toString() : "";
        }
    }

    public static class JsonTablePlanExpression extends ASTNodeAccessImpl implements Serializable {
        private final List<JsonTablePlanTerm> terms = new ArrayList<>();
        private final List<JsonTablePlanOperator> operators = new ArrayList<>();

        public List<JsonTablePlanTerm> getTerms() {
            return terms;
        }

        public JsonTablePlanExpression addTerm(JsonTablePlanTerm term) {
            terms.add(term);
            return this;
        }

        public List<JsonTablePlanOperator> getOperators() {
            return operators;
        }

        public JsonTablePlanExpression addOperator(JsonTablePlanOperator operator) {
            operators.add(operator);
            return this;
        }

        public void collectExpressions(List<Expression> expressions) {
            for (JsonTablePlanTerm term : terms) {
                if (term != null) {
                    term.collectExpressions(expressions);
                }
            }
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            if (!terms.isEmpty()) {
                builder.append(terms.get(0));
            }
            for (int i = 0; i < operators.size() && i + 1 < terms.size(); i++) {
                builder.append(operators.get(i).getDisplay()).append(terms.get(i + 1));
            }
            return builder.toString();
        }
    }

    public static class JsonTablePlanClause extends ASTNodeAccessImpl implements Serializable {
        private boolean defaultPlan;
        private JsonTablePlanExpression planExpression;

        public boolean isDefaultPlan() {
            return defaultPlan;
        }

        public JsonTablePlanClause setDefaultPlan(boolean defaultPlan) {
            this.defaultPlan = defaultPlan;
            return this;
        }

        public JsonTablePlanExpression getPlanExpression() {
            return planExpression;
        }

        public JsonTablePlanClause setPlanExpression(JsonTablePlanExpression planExpression) {
            this.planExpression = planExpression;
            return this;
        }

        public void collectExpressions(List<Expression> expressions) {
            if (planExpression != null) {
                planExpression.collectExpressions(expressions);
            }
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("PLAN");
            if (defaultPlan) {
                builder.append(" DEFAULT");
            }
            builder.append(" (").append(planExpression).append(")");
            return builder.toString();
        }
    }

    public abstract static class JsonTableColumnDefinition extends ASTNodeAccessImpl
            implements Serializable {
        public abstract void collectExpressions(List<Expression> expressions);
    }

    public static class JsonTableNestedColumnDefinition extends JsonTableColumnDefinition {
        private boolean pathKeyword;
        private Expression pathExpression;
        private String pathName;
        private JsonTableColumnsClause columnsClause;

        public boolean isPathKeyword() {
            return pathKeyword;
        }

        public JsonTableNestedColumnDefinition setPathKeyword(boolean pathKeyword) {
            this.pathKeyword = pathKeyword;
            return this;
        }

        public Expression getPathExpression() {
            return pathExpression;
        }

        public JsonTableNestedColumnDefinition setPathExpression(Expression pathExpression) {
            this.pathExpression = pathExpression;
            return this;
        }

        public String getPathName() {
            return pathName;
        }

        public JsonTableNestedColumnDefinition setPathName(String pathName) {
            this.pathName = pathName;
            return this;
        }

        public JsonTableColumnsClause getColumnsClause() {
            return columnsClause;
        }

        public JsonTableNestedColumnDefinition setColumnsClause(
                JsonTableColumnsClause columnsClause) {
            this.columnsClause = columnsClause;
            return this;
        }

        @Override
        public void collectExpressions(List<Expression> expressions) {
            if (pathExpression != null) {
                expressions.add(pathExpression);
            }
            if (columnsClause != null) {
                columnsClause.collectExpressions(expressions);
            }
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("NESTED");
            if (pathKeyword) {
                builder.append(" PATH");
            }
            builder.append(" ").append(pathExpression);
            if (pathName != null) {
                builder.append(" AS ").append(pathName);
            }
            builder.append(" ").append(columnsClause);
            return builder.toString();
        }
    }

    public static class JsonTableValueColumnDefinition extends JsonTableColumnDefinition {
        private String columnName;
        private boolean forOrdinality;
        private ColDataType dataType;
        private boolean formatJson;
        private String encoding;
        private Expression pathExpression;
        private JsonTableWrapperClause wrapperClause;
        private JsonTableQuotesClause quotesClause;
        private JsonFunction.JsonOnResponseBehavior onEmptyBehavior;
        private JsonFunction.JsonOnResponseBehavior onErrorBehavior;

        public String getColumnName() {
            return columnName;
        }

        public JsonTableValueColumnDefinition setColumnName(String columnName) {
            this.columnName = columnName;
            return this;
        }

        public boolean isForOrdinality() {
            return forOrdinality;
        }

        public JsonTableValueColumnDefinition setForOrdinality(boolean forOrdinality) {
            this.forOrdinality = forOrdinality;
            return this;
        }

        public ColDataType getDataType() {
            return dataType;
        }

        public JsonTableValueColumnDefinition setDataType(ColDataType dataType) {
            this.dataType = dataType;
            return this;
        }

        public boolean isFormatJson() {
            return formatJson;
        }

        public JsonTableValueColumnDefinition setFormatJson(boolean formatJson) {
            this.formatJson = formatJson;
            return this;
        }

        public String getEncoding() {
            return encoding;
        }

        public JsonTableValueColumnDefinition setEncoding(String encoding) {
            this.encoding = encoding;
            return this;
        }

        public Expression getPathExpression() {
            return pathExpression;
        }

        public JsonTableValueColumnDefinition setPathExpression(Expression pathExpression) {
            this.pathExpression = pathExpression;
            return this;
        }

        public JsonTableWrapperClause getWrapperClause() {
            return wrapperClause;
        }

        public JsonTableValueColumnDefinition setWrapperClause(
                JsonTableWrapperClause wrapperClause) {
            this.wrapperClause = wrapperClause;
            return this;
        }

        public JsonTableQuotesClause getQuotesClause() {
            return quotesClause;
        }

        public JsonTableValueColumnDefinition setQuotesClause(JsonTableQuotesClause quotesClause) {
            this.quotesClause = quotesClause;
            return this;
        }

        public JsonFunction.JsonOnResponseBehavior getOnEmptyBehavior() {
            return onEmptyBehavior;
        }

        public JsonTableValueColumnDefinition setOnEmptyBehavior(
                JsonFunction.JsonOnResponseBehavior onEmptyBehavior) {
            this.onEmptyBehavior = onEmptyBehavior;
            return this;
        }

        public JsonFunction.JsonOnResponseBehavior getOnErrorBehavior() {
            return onErrorBehavior;
        }

        public JsonTableValueColumnDefinition setOnErrorBehavior(
                JsonFunction.JsonOnResponseBehavior onErrorBehavior) {
            this.onErrorBehavior = onErrorBehavior;
            return this;
        }

        @Override
        public void collectExpressions(List<Expression> expressions) {
            if (pathExpression != null) {
                expressions.add(pathExpression);
            }
            if (onEmptyBehavior != null && onEmptyBehavior.getExpression() != null) {
                expressions.add(onEmptyBehavior.getExpression());
            }
            if (onErrorBehavior != null && onErrorBehavior.getExpression() != null) {
                expressions.add(onErrorBehavior.getExpression());
            }
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(columnName);
            if (forOrdinality) {
                builder.append(" FOR ORDINALITY");
                return builder.toString();
            }

            builder.append(" ").append(dataType);
            if (formatJson) {
                builder.append(" FORMAT JSON");
                if (encoding != null) {
                    builder.append(" ENCODING ").append(encoding);
                }
            }
            if (pathExpression != null) {
                builder.append(" PATH ").append(pathExpression);
            }
            if (wrapperClause != null) {
                builder.append(" ").append(wrapperClause);
            }
            if (quotesClause != null) {
                builder.append(" ").append(quotesClause);
            }
            if (onEmptyBehavior != null) {
                builder.append(" ").append(onEmptyBehavior).append(" ON EMPTY");
            }
            if (onErrorBehavior != null) {
                builder.append(" ").append(onErrorBehavior).append(" ON ERROR");
            }
            return builder.toString();
        }
    }

    public static class JsonTableColumnsClause extends ASTNodeAccessImpl implements Serializable {
        private final List<JsonTableColumnDefinition> columnDefinitions = new ArrayList<>();

        public List<JsonTableColumnDefinition> getColumnDefinitions() {
            return columnDefinitions;
        }

        public JsonTableColumnsClause addColumnDefinition(
                JsonTableColumnDefinition columnDefinition) {
            columnDefinitions.add(columnDefinition);
            return this;
        }

        public void collectExpressions(List<Expression> expressions) {
            for (JsonTableColumnDefinition columnDefinition : columnDefinitions) {
                if (columnDefinition != null) {
                    columnDefinition.collectExpressions(expressions);
                }
            }
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("COLUMNS (");
            boolean first = true;
            for (JsonTableColumnDefinition columnDefinition : columnDefinitions) {
                if (!first) {
                    builder.append(", ");
                }
                builder.append(columnDefinition);
                first = false;
            }
            builder.append(")");
            return builder.toString();
        }
    }

    private Expression jsonInputExpression;
    private Expression jsonPathExpression;
    private String pathName;
    private final List<JsonTablePassingClause> passingClauses = new ArrayList<>();
    private JsonTableColumnsClause columnsClause;
    private JsonTablePlanClause planClause;
    private JsonTableOnErrorClause onErrorClause;

    public JsonTableFunction() {
        setName("JSON_TABLE");
    }

    public Expression getJsonInputExpression() {
        return jsonInputExpression;
    }

    public JsonTableFunction setJsonInputExpression(Expression jsonInputExpression) {
        this.jsonInputExpression = jsonInputExpression;
        return this;
    }

    public Expression getJsonPathExpression() {
        return jsonPathExpression;
    }

    public JsonTableFunction setJsonPathExpression(Expression jsonPathExpression) {
        this.jsonPathExpression = jsonPathExpression;
        return this;
    }

    public String getPathName() {
        return pathName;
    }

    public JsonTableFunction setPathName(String pathName) {
        this.pathName = pathName;
        return this;
    }

    public List<JsonTablePassingClause> getPassingClauses() {
        return passingClauses;
    }

    public JsonTableFunction addPassingClause(JsonTablePassingClause passingClause) {
        passingClauses.add(Objects.requireNonNull(passingClause, "passingClause"));
        return this;
    }

    public JsonTableColumnsClause getColumnsClause() {
        return columnsClause;
    }

    public JsonTableFunction setColumnsClause(JsonTableColumnsClause columnsClause) {
        this.columnsClause = columnsClause;
        return this;
    }

    public JsonTablePlanClause getPlanClause() {
        return planClause;
    }

    public JsonTableFunction setPlanClause(JsonTablePlanClause planClause) {
        this.planClause = planClause;
        return this;
    }

    public JsonTableOnErrorClause getOnErrorClause() {
        return onErrorClause;
    }

    public JsonTableFunction setOnErrorClause(JsonTableOnErrorClause onErrorClause) {
        this.onErrorClause = onErrorClause;
        return this;
    }

    public List<Expression> getAllExpressions() {
        List<Expression> expressions = new ArrayList<>();
        if (jsonInputExpression != null) {
            expressions.add(jsonInputExpression);
        }
        if (jsonPathExpression != null) {
            expressions.add(jsonPathExpression);
        }
        for (JsonTablePassingClause passingClause : passingClauses) {
            passingClause.collectExpressions(expressions);
        }
        if (columnsClause != null) {
            columnsClause.collectExpressions(expressions);
        }
        if (planClause != null) {
            planClause.collectExpressions(expressions);
        }
        return expressions;
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("JSON_TABLE(");
        builder.append(jsonInputExpression).append(", ").append(jsonPathExpression);
        if (pathName != null) {
            builder.append(" AS ").append(pathName);
        }
        if (!passingClauses.isEmpty()) {
            builder.append(" PASSING ");
            boolean first = true;
            for (JsonTablePassingClause passingClause : passingClauses) {
                if (!first) {
                    builder.append(", ");
                }
                builder.append(passingClause);
                first = false;
            }
        }
        builder.append(" ").append(columnsClause);
        if (planClause != null) {
            builder.append(" ").append(planClause);
        }
        if (onErrorClause != null) {
            builder.append(" ").append(onErrorClause);
        }
        builder.append(")");
        return builder.toString();
    }
}

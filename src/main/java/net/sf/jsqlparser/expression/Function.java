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

import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NamedExpressionList;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.OrderByElement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A function as MAX,COUNT...
 */
public class Function extends ASTNodeAccessImpl implements Expression {
    private List<String> nameparts;
    private ExpressionList<?> parameters;
    private ExpressionList<?> chainedParameters;
    private NamedExpressionList<?> namedParameters;
    private boolean allColumns = false;
    private boolean distinct = false;
    private boolean unique = false;
    private boolean isEscaped = false;
    private Expression attributeExpression;
    private HavingClause havingClause;
    private Column attributeColumn = null;
    private List<OrderByElement> orderByElements;
    private NullHandling nullHandling = null;
    private boolean ignoreNullsOutside = false; // IGNORE NULLS outside function parameters
    private Limit limit = null;
    private KeepExpression keep = null;
    private String onOverflowTruncate = null;
    private String extraKeyword = null;

    /**
     * Generic keyword arguments captured inside function parentheses, e.g.
     * {@code GROUP_CONCAT(col ORDER BY col SEPARATOR ',')} where {@code SEPARATOR ','} is a keyword
     * argument. This acts as a catch-all for dialect-specific {@code KEYWORD expr} pairs that don't
     * have dedicated grammar branches.
     */
    private List<KeywordArgument> keywordArguments = null;

    public Function() {}

    public Function(String name, Expression... parameters) {
        this.nameparts = Collections.singletonList(name);
        this.parameters = new ExpressionList<>(parameters);
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }

    public String getName() {
        return nameparts == null ? null
                : String.join(nameparts.get(0).equalsIgnoreCase("APPROXIMATE") ? " " : ".",
                        nameparts);
    }

    public void setName(String string) {
        nameparts = Arrays.asList(string);
    }

    public void setName(List<String> string) {
        nameparts = string;
    }

    public List<String> getMultipartName() {
        return nameparts;
    }

    public Function withName(String name) {
        this.setName(name);
        return this;
    }

    public Function withName(List<String> nameparts) {
        this.nameparts = nameparts;
        return this;
    }

    public boolean isAllColumns() {
        return allColumns;
    }

    public void setAllColumns(boolean b) {
        allColumns = b;
    }

    public NullHandling getNullHandling() {
        return nullHandling;
    }

    public Function setNullHandling(NullHandling nullHandling) {
        this.nullHandling = nullHandling;
        return this;
    }

    public boolean isIgnoreNullsOutside() {
        return ignoreNullsOutside;
    }

    public Function setIgnoreNullsOutside(boolean ignoreNullsOutside) {
        this.ignoreNullsOutside = ignoreNullsOutside;
        return this;
    }

    public Limit getLimit() {
        return limit;
    }

    public Function setLimit(Limit limit) {
        this.limit = limit;
        return this;
    }

    public boolean isIgnoreNulls() {
        return nullHandling != null && nullHandling == NullHandling.IGNORE_NULLS;
    }

    /**
     * This is at the moment only necessary for AnalyticExpression initialization and not for normal
     * functions. Therefore there is no deparsing for it for normal functions.
     */
    public void setIgnoreNulls(boolean ignoreNulls) {
        this.nullHandling = ignoreNulls ? NullHandling.IGNORE_NULLS : null;
    }

    public HavingClause getHavingClause() {
        return havingClause;
    }

    public Function setHavingClause(HavingClause havingClause) {
        this.havingClause = havingClause;
        return this;
    }

    public Function setHavingClause(String havingType, Expression expression) {
        this.havingClause = new HavingClause(
                HavingClause.HavingType.valueOf(havingType.trim().toUpperCase()), expression);
        return this;
    }

    /**
     * true if the function is "distinct"
     *
     * @return true if the function is "distinct"
     */
    public boolean isDistinct() {
        return distinct;
    }

    public void setDistinct(boolean b) {
        distinct = b;
    }

    /**
     * true if the function is "unique"
     *
     * @return true if the function is "unique"
     */
    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean b) {
        unique = b;
    }

    /**
     * The list of parameters of the function (if any, else null) If the parameter is "*",
     * allColumns is set to true
     *
     * @return the list of parameters of the function (if any, else null)
     */
    public ExpressionList<?> getParameters() {
        return parameters;
    }

    public void setParameters(Expression... expressions) {
        if (expressions.length == 1 && expressions[0] instanceof ExpressionList) {
            parameters = (ExpressionList<?>) expressions[0];
        } else {
            parameters = new ExpressionList<>(expressions);
        }
    }

    public void setParameters(ExpressionList<?> list) {
        parameters = list;
    }

    /**
     * Additional function-call parameters for dialects that support chained function calls, e.g.
     * quantile(0.95)(cost) in ClickHouse.
     *
     * @return the chained parameters of the function (if any, else null)
     */
    public ExpressionList<?> getChainedParameters() {
        return chainedParameters;
    }

    public void setChainedParameters(ExpressionList<?> chainedParameters) {
        this.chainedParameters = chainedParameters;
    }

    /**
     * the parameters might be named parameters, e.g. substring('foobar' from 2 for 3)
     *
     * @return the list of named parameters of the function (if any, else null)
     */
    public NamedExpressionList<?> getNamedParameters() {
        return namedParameters;
    }

    public void setNamedParameters(NamedExpressionList<?> list) {
        namedParameters = list;
    }

    /**
     * Return true if it's in the form "{fn function_body() }"
     *
     * @return true if it's java-escaped
     */
    public boolean isEscaped() {
        return isEscaped;
    }

    public void setEscaped(boolean isEscaped) {
        this.isEscaped = isEscaped;
    }

    public Object getAttribute() {
        return attributeExpression != null ? attributeExpression : attributeColumn;
    }

    public void setAttribute(Expression attributeExpression) {
        this.attributeExpression = attributeExpression;
    }

    public void setAttribute(Column attributeColumn) {
        attributeExpression = null;
        this.attributeColumn = attributeColumn;
    }

    @Deprecated
    public String getAttributeName() {
        return attributeColumn.toString();
    }

    public void setAttributeName(String attributeName) {
        this.attributeColumn = new Column().withColumnName(attributeName);
    }

    public Column getAttributeColumn() {
        return attributeColumn;
    }

    public Function withAttribute(Column attributeColumn) {
        setAttribute(attributeColumn);
        return this;
    }

    public KeepExpression getKeep() {
        return keep;
    }

    public void setKeep(KeepExpression keep) {
        this.keep = keep;
    }

    public String getExtraKeyword() {
        return extraKeyword;
    }

    public Function setExtraKeyword(String extraKeyword) {
        this.extraKeyword = extraKeyword;
        return this;
    }

    // ── Generic keyword argument support ───────────────────────────────

    /**
     * Returns the list of generic keyword arguments, e.g. {@code SEPARATOR ','}.
     *
     * @return keyword arguments or {@code null}
     */
    public List<KeywordArgument> getKeywordArguments() {
        return keywordArguments;
    }

    public void setKeywordArguments(List<KeywordArgument> keywordArguments) {
        this.keywordArguments = keywordArguments;
    }

    /**
     * Adds a single keyword argument (appends to the list, creating it if needed).
     */
    public Function addKeywordArgument(String keyword, Expression expression) {
        if (this.keywordArguments == null) {
            this.keywordArguments = new ArrayList<>();
        }
        this.keywordArguments.add(new KeywordArgument(keyword, expression));
        return this;
    }

    public Function withKeywordArguments(List<KeywordArgument> keywordArguments) {
        this.keywordArguments = keywordArguments;
        return this;
    }

    /**
     * Convenience lookup: returns the expression for the first keyword argument matching the given
     * keyword (case-insensitive), or {@code null}.
     */
    public Expression getKeywordArgumentValue(String keyword) {
        if (keywordArguments == null) {
            return null;
        }
        for (KeywordArgument ka : keywordArguments) {
            if (ka.getKeyword().equalsIgnoreCase(keyword)) {
                return ka.getExpression();
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public String toString() {
        String params;

        if (parameters != null || namedParameters != null) {
            if (parameters != null) {
                StringBuilder b = new StringBuilder();
                b.append("(");
                if (isDistinct()) {
                    b.append("DISTINCT ");
                } else if (isUnique()) {
                    b.append("UNIQUE ");
                }
                if (isAllColumns()) {
                    b.append("ALL ");
                }

                if (extraKeyword != null) {
                    b.append(extraKeyword).append(" ");
                }

                b.append(parameters);

                if (havingClause != null) {
                    havingClause.appendTo(b);
                }

                if (nullHandling != null && !isIgnoreNullsOutside()) {
                    switch (nullHandling) {
                        case IGNORE_NULLS:
                            b.append(" IGNORE NULLS");
                            break;
                        case RESPECT_NULLS:
                            b.append(" RESPECT NULLS");
                            break;
                    }
                }
                if (orderByElements != null) {
                    b.append(" ORDER BY ");
                    boolean comma = false;
                    for (OrderByElement orderByElement : orderByElements) {
                        if (comma) {
                            b.append(", ");
                        } else {
                            comma = true;
                        }
                        b.append(orderByElement);
                    }
                }
                if (limit != null) {
                    b.append(limit);
                }

                if (onOverflowTruncate != null) {
                    b.append(" ON OVERFLOW ").append(onOverflowTruncate);
                }

                // Generic keyword arguments (e.g. SEPARATOR ',')
                if (keywordArguments != null) {
                    for (KeywordArgument ka : keywordArguments) {
                        ka.appendTo(b);
                    }
                }

                b.append(")");
                params = b.toString();
            } else {
                params = namedParameters.toString();
            }
        } else {
            params = "()";
        }

        String ans = getName() + params;

        if (chainedParameters != null) {
            ans += "(" + chainedParameters + ")";
        }

        if (nullHandling != null && isIgnoreNullsOutside()) {
            switch (nullHandling) {
                case IGNORE_NULLS:
                    ans += " IGNORE NULLS";
                    break;
                case RESPECT_NULLS:
                    ans += " RESPECT NULLS";
                    break;
            }
        }

        if (attributeExpression != null) {
            ans += "." + attributeExpression;
        } else if (attributeColumn != null) {
            ans += "." + attributeColumn;
        }

        if (keep != null) {
            ans += " " + keep;
        }

        if (isEscaped) {
            ans = "{fn " + ans + "}";
        }

        return ans;
    }

    public Function withAttribute(Expression attribute) {
        this.setAttribute(attribute);
        return this;
    }

    @Deprecated
    public Function withAttributeName(String attributeName) {
        this.setAttributeName(attributeName);
        return this;
    }

    public Function withKeep(KeepExpression keep) {
        this.setKeep(keep);
        return this;
    }

    public Function withIgnoreNulls(boolean ignoreNulls) {
        this.setIgnoreNulls(ignoreNulls);
        return this;
    }

    public Function withParameters(ExpressionList<?> parameters) {
        this.setParameters(parameters);
        return this;
    }

    public Function withParameters(Expression... parameters) {
        return withParameters(new ExpressionList<>(parameters));
    }

    public Function withChainedParameters(ExpressionList<?> chainedParameters) {
        this.setChainedParameters(chainedParameters);
        return this;
    }

    public Function withNamedParameters(NamedExpressionList<?> namedParameters) {
        this.setNamedParameters(namedParameters);
        return this;
    }

    public Function withAllColumns(boolean allColumns) {
        this.setAllColumns(allColumns);
        return this;
    }

    public Function withDistinct(boolean distinct) {
        this.setDistinct(distinct);
        return this;
    }

    public Function withUnique(boolean unique) {
        this.setUnique(unique);
        return this;
    }

    public List<OrderByElement> getOrderByElements() {
        return orderByElements;
    }

    public void setOrderByElements(List<OrderByElement> orderByElements) {
        this.orderByElements = orderByElements;
    }

    public String getOnOverflowTruncate() {
        return onOverflowTruncate;
    }

    public Function setOnOverflowTruncate(String onOverflowTruncate) {
        this.onOverflowTruncate = onOverflowTruncate;
        return this;
    }

    public <E extends Expression> E getAttribute(Class<E> type) {
        return type.cast(getAttribute());
    }

    public enum NullHandling {
        IGNORE_NULLS, RESPECT_NULLS;
    }

    // ── KeywordArgument inner class ────────────────────────────────────

    /**
     * Represents a generic {@code KEYWORD expression} pair inside a function call.
     * <p>
     * Examples:
     * <ul>
     * <li>{@code GROUP_CONCAT(col SEPARATOR ',')} → keyword="SEPARATOR", expression=','</li>
     * </ul>
     */
    public static class KeywordArgument implements Serializable {
        private String keyword;
        private Expression expression;

        public KeywordArgument() {}

        public KeywordArgument(String keyword, Expression expression) {
            this.keyword = keyword;
            this.expression = expression;
        }

        public String getKeyword() {
            return keyword;
        }

        public KeywordArgument setKeyword(String keyword) {
            this.keyword = keyword;
            return this;
        }

        public Expression getExpression() {
            return expression;
        }

        public KeywordArgument setExpression(Expression expression) {
            this.expression = expression;
            return this;
        }

        public StringBuilder appendTo(StringBuilder builder) {
            builder.append(" ").append(keyword).append(" ").append(expression);
            return builder;
        }

        @Override
        public String toString() {
            return keyword + " " + expression;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof KeywordArgument)) {
                return false;
            }
            KeywordArgument that = (KeywordArgument) o;
            return Objects.equals(keyword, that.keyword)
                    && Objects.equals(expression, that.expression);
        }

        @Override
        public int hashCode() {
            return Objects.hash(keyword, expression);
        }
    }

    public static class HavingClause extends ASTNodeAccessImpl implements Expression {
        HavingType havingType;
        Expression expression;

        public HavingClause(HavingType havingType, Expression expression) {
            this.havingType = havingType;
            this.expression = expression;
        }

        public HavingType getHavingType() {
            return havingType;
        }

        public HavingClause setHavingType(HavingType havingType) {
            this.havingType = havingType;
            return this;
        }

        public Expression getExpression() {
            return expression;
        }

        public HavingClause setExpression(Expression expression) {
            this.expression = expression;
            return this;
        }

        @Override
        public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
            return expression.accept(expressionVisitor, context);
        }

        public StringBuilder appendTo(StringBuilder builder) {
            builder.append(" HAVING ").append(havingType.name()).append(" ").append(expression);
            return builder;
        }

        @Override
        public String toString() {
            return appendTo(new StringBuilder()).toString();
        }

        enum HavingType {
            MAX, MIN;
        }
    }
}

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
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.OrderByElement;

import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * Analytic function. The name of the function is variable but the parameters following the special
 * analytic function path. e.g. row_number() over (order by test). Additionally, there can be an
 * expression for an analytical aggregate like sum(col) or the "all columns" wildcard like count(*).
 *
 * @author tw
 */
public class AnalyticExpression extends ASTNodeAccessImpl implements Expression {


    private String name;
    private Expression expression;
    private Expression offset;
    private Expression defaultValue;
    private boolean allColumns = false;
    private KeepExpression keep = null;
    private AnalyticType type = AnalyticType.OVER;
    private boolean distinct = false;
    private boolean unique = false;
    private boolean ignoreNullsOutside = false; // IGNORE NULLS outside function parameters
    private Expression filterExpression = null;
    private List<OrderByElement> funcOrderBy = null;
    private String windowName = null; // refers to an external window definition (paritionBy,
                                      // orderBy, windowElement)
    private WindowDefinition windowDef = new WindowDefinition();

    private Function.HavingClause havingClause;

    private Function.NullHandling nullHandling = null;

    private Limit limit = null;

    public AnalyticExpression() {}

    public AnalyticExpression(Function function) {
        this.name = String.join(" ", function.getMultipartName());
        this.allColumns = function.isAllColumns();
        this.distinct = function.isDistinct();
        this.unique = function.isUnique();


        ExpressionList<? extends Expression> list = function.getParameters();
        if (list != null) {
            if (list.size() > 3) {
                throw new IllegalArgumentException(
                        "function object not valid to initialize analytic expression");
            }

            expression = list.get(0);
            if (list.size() > 1) {
                offset = list.get(1);
            }
            if (list.size() > 2) {
                defaultValue = list.get(2);
            }
        }
        this.havingClause = function.getHavingClause();
        this.ignoreNullsOutside = function.isIgnoreNullsOutside();
        this.nullHandling = function.getNullHandling();
        this.funcOrderBy = function.getOrderByElements();
        this.limit = function.getLimit();
        this.keep = function.getKeep();
    }


    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S arguments) {
        return expressionVisitor.visit(this, arguments);
    }

    public List<OrderByElement> getOrderByElements() {
        return windowDef.orderBy.getOrderByElements();
    }

    public void setOrderByElements(List<OrderByElement> orderByElements) {
        windowDef.orderBy.setOrderByElements(orderByElements);
    }

    public KeepExpression getKeep() {
        return keep;
    }

    public void setKeep(KeepExpression keep) {
        this.keep = keep;
    }

    public ExpressionList<?> getPartitionExpressionList() {
        return windowDef.partitionBy.getPartitionExpressionList();
    }

    public void setPartitionExpressionList(ExpressionList<?> partitionExpressionList) {
        setPartitionExpressionList(partitionExpressionList, false);
    }

    public void setPartitionExpressionList(ExpressionList<?> partitionExpressionList,
            boolean brackets) {
        windowDef.partitionBy.setPartitionExpressionList(partitionExpressionList, brackets);
    }

    public boolean isPartitionByBrackets() {
        return windowDef.partitionBy.isBrackets();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public Expression getOffset() {
        return offset;
    }

    public void setOffset(Expression offset) {
        this.offset = offset;
    }

    public Expression getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Expression defaultValue) {
        this.defaultValue = defaultValue;
    }

    public WindowElement getWindowElement() {
        return windowDef.windowElement;
    }

    public void setWindowElement(WindowElement windowElement) {
        windowDef.windowElement = windowElement;
    }

    public AnalyticType getType() {
        return type;
    }

    public void setType(AnalyticType type) {
        this.type = type;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public boolean isIgnoreNulls() {
        return this.nullHandling == Function.NullHandling.IGNORE_NULLS;
    }

    public void setIgnoreNulls(boolean ignoreNulls) {
        this.nullHandling = ignoreNulls ? Function.NullHandling.IGNORE_NULLS : null;
    }

    public boolean isIgnoreNullsOutside() {
        return ignoreNullsOutside;
    }

    public void setIgnoreNullsOutside(boolean ignoreNullsOutside) {
        this.ignoreNullsOutside = ignoreNullsOutside;
    }

    public String getWindowName() {
        return windowName;
    }

    public void setWindowName(String windowName) {
        this.windowName = windowName;
    }

    public WindowDefinition getWindowDefinition() {
        return windowDef;
    }

    public void setWindowDefinition(WindowDefinition windowDef) {
        this.windowDef = windowDef;
    }


    public Function.HavingClause getHavingClause() {
        return havingClause;
    }

    public AnalyticExpression setHavingClause(Function.HavingClause havingClause) {
        this.havingClause = havingClause;
        return this;
    }

    public AnalyticExpression setHavingClause(String havingType, Expression expression) {
        this.havingClause = new Function.HavingClause(
                Function.HavingClause.HavingType.valueOf(havingType.trim().toUpperCase()),
                expression);
        return this;
    }

    public Function.NullHandling getNullHandling() {
        return nullHandling;
    }

    public AnalyticExpression setNullHandling(Function.NullHandling nullHandling) {
        this.nullHandling = nullHandling;
        return this;
    }

    public Limit getLimit() {
        return limit;
    }

    public AnalyticExpression setLimit(Limit limit) {
        this.limit = limit;
        return this;
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity",
            "PMD.MissingBreakInSwitch"})
    public String toString() {
        StringBuilder b = new StringBuilder();

        b.append(name).append("(");
        if (isDistinct()) {
            b.append("DISTINCT ");
        }
        if (expression != null) {
            b.append(expression);
            if (offset != null) {
                b.append(", ").append(offset);
                if (defaultValue != null) {
                    b.append(", ").append(defaultValue);
                }
            }
        } else if (isAllColumns()) {
            b.append("*");
        }

        if (havingClause != null) {
            havingClause.appendTo(b);
        }

        if (nullHandling != null && !ignoreNullsOutside) {
            switch (nullHandling) {
                case IGNORE_NULLS:
                    b.append(" IGNORE NULLS");
                    break;
                case RESPECT_NULLS:
                    b.append(" RESPECT NULLS");
                    break;
            }
        }

        if (funcOrderBy != null) {
            b.append(" ORDER BY ");
            b.append(funcOrderBy.stream().map(OrderByElement::toString).collect(joining(", ")));
        }

        if (limit != null) {
            b.append(limit);
        }

        b.append(") ");
        if (keep != null) {
            b.append(keep).append(" ");
        }

        if (filterExpression != null) {
            b.append("FILTER (WHERE ");
            b.append(filterExpression);
            b.append(")");
            if (type != AnalyticType.FILTER_ONLY) {
                b.append(" ");
            }
        }

        if (nullHandling != null && ignoreNullsOutside) {
            switch (nullHandling) {
                case IGNORE_NULLS:
                    b.append(" IGNORE NULLS ");
                    break;
                case RESPECT_NULLS:
                    b.append(" RESPECT NULLS ");
                    break;
            }
        }

        switch (type) {
            case FILTER_ONLY:
                return b.toString();
            case WITHIN_GROUP:
                b.append("WITHIN GROUP");
                break;
            case WITHIN_GROUP_OVER:
                b.append("WITHIN GROUP (");
                windowDef.orderBy.toStringOrderByElements(b);
                b.append(") OVER (");
                windowDef.partitionBy.toStringPartitionBy(b);
                b.append(")");
                break;
            default:
                b.append("OVER");
        }

        if (windowName != null) {
            b.append(" ").append(windowName);
        } else if (type != AnalyticType.WITHIN_GROUP_OVER) {
            b.append(" ");
            b.append(windowDef.toString());
        }

        return b.toString();
    }

    public boolean isAllColumns() {
        return allColumns;
    }

    public void setAllColumns(boolean allColumns) {
        this.allColumns = allColumns;
    }

    public Expression getFilterExpression() {
        return filterExpression;
    }

    public void setFilterExpression(Expression filterExpression) {
        this.filterExpression = filterExpression;
    }

    public AnalyticExpression withName(String name) {
        this.setName(name);
        return this;
    }

    public AnalyticExpression withExpression(Expression expression) {
        this.setExpression(expression);
        return this;
    }

    public AnalyticExpression withOffset(Expression offset) {
        this.setOffset(offset);
        return this;
    }

    public AnalyticExpression withDefaultValue(Expression defaultValue) {
        this.setDefaultValue(defaultValue);
        return this;
    }

    public AnalyticExpression withAllColumns(boolean allColumns) {
        this.setAllColumns(allColumns);
        return this;
    }

    public AnalyticExpression withKeep(KeepExpression keep) {
        this.setKeep(keep);
        return this;
    }

    public AnalyticExpression withType(AnalyticType type) {
        this.setType(type);
        return this;
    }

    public AnalyticExpression withDistinct(boolean distinct) {
        this.setDistinct(distinct);
        return this;
    }

    public AnalyticExpression withUnique(boolean unique) {
        this.setUnique(unique);
        return this;
    }

    public AnalyticExpression withIgnoreNulls(boolean ignoreNulls) {
        this.setIgnoreNulls(ignoreNulls);
        return this;
    }

    public AnalyticExpression withFilterExpression(Expression filterExpression) {
        this.setFilterExpression(filterExpression);
        return this;
    }

    public AnalyticExpression withWindowElement(WindowElement windowElement) {
        this.setWindowElement(windowElement);
        return this;
    }

    public <E extends Expression> E getExpression(Class<E> type) {
        return type.cast(getExpression());
    }

    public <E extends Expression> E getOffset(Class<E> type) {
        return type.cast(getOffset());
    }

    public <E extends Expression> E getDefaultValue(Class<E> type) {
        return type.cast(getDefaultValue());
    }

    public <E extends Expression> E getFilterExpression(Class<E> type) {
        return type.cast(getFilterExpression());
    }

    public List<OrderByElement> getFuncOrderBy() {
        return funcOrderBy;
    }

    public void setFuncOrderBy(List<OrderByElement> funcOrderBy) {
        this.funcOrderBy = funcOrderBy;
    }
}

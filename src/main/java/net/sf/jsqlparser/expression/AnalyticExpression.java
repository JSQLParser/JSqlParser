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

import java.util.List;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.select.OrderByElement;

/**
 * Analytic function. The name of the function is variable but the parameters following the special
 * analytic function path. e.g. row_number() over (order by test). Additional there can be an
 * expression for an analytical aggregate like sum(col) or the "all collumns" wildcard like
 * count(*).
 *
 * @author tw
 */
public class AnalyticExpression extends ASTNodeAccessImpl implements Expression {

    private final OrderByClause orderBy = new OrderByClause();
    private final PartitionByClause partitionBy = new PartitionByClause();
    private String name;
    private Expression expression;
    private Expression offset;
    private Expression defaultValue;
    private boolean allColumns = false;
    private KeepExpression keep = null;
    private AnalyticType type = AnalyticType.OVER;
    private boolean distinct = false;
    private boolean ignoreNulls = false;
    private Expression filterExpression = null;
    private WindowElement windowElement = null;

    public AnalyticExpression() {
    }

    public AnalyticExpression(Function function) {
        name = function.getName();
        allColumns = function.isAllColumns();
        distinct = function.isDistinct();

        ExpressionList list = function.getParameters();
        if (list != null) {
            if (list.getExpressions().size() > 3) {
                throw new IllegalArgumentException("function object not valid to initialize analytic expression");
            }

            expression = list.getExpressions().get(0);
            if (list.getExpressions().size() > 1) {
                offset = list.getExpressions().get(1);
            }
            if (list.getExpressions().size() > 2) {
                defaultValue = list.getExpressions().get(2);
            }
        }
        ignoreNulls = function.isIgnoreNulls();
        keep = function.getKeep();
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    public List<OrderByElement> getOrderByElements() {
        return orderBy.getOrderByElements();
    }

    public void setOrderByElements(List<OrderByElement> orderByElements) {
        orderBy.setOrderByElements(orderByElements);
    }

    public KeepExpression getKeep() {
        return keep;
    }

    public void setKeep(KeepExpression keep) {
        this.keep = keep;
    }

    public ExpressionList getPartitionExpressionList() {
        return partitionBy.getPartitionExpressionList();
    }

    public void setPartitionExpressionList(ExpressionList partitionExpressionList) {
        setPartitionExpressionList(partitionExpressionList, false);
    }

    public void setPartitionExpressionList(ExpressionList partitionExpressionList, boolean brackets) {
        partitionBy.setPartitionExpressionList(partitionExpressionList, brackets);
    }

    public boolean isPartitionByBrackets() {
        return partitionBy.isBrackets();
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
        return windowElement;
    }

    public void setWindowElement(WindowElement windowElement) {
        this.windowElement = windowElement;
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

    public boolean isIgnoreNulls() {
        return ignoreNulls;
    }

    public void setIgnoreNulls(boolean ignoreNulls) {
        this.ignoreNulls = ignoreNulls;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();

        b.append(name).append("(");
        if (isDistinct()) {
            b.append("DISTINCT ");
        }
        if (expression != null) {
            b.append(expression.toString());
            if (offset != null) {
                b.append(", ").append(offset.toString());
                if (defaultValue != null) {
                    b.append(", ").append(defaultValue.toString());
                }
            }
        } else if (isAllColumns()) {
            b.append("*");
        }
        if (isIgnoreNulls()) {
            b.append(" IGNORE NULLS");
        }
        b.append(") ");
        if (keep != null) {
            b.append(keep.toString()).append(" ");
        }

        if (filterExpression != null) {
            b.append("FILTER (WHERE ");
            b.append(filterExpression.toString());
            b.append(")");
            if (type != AnalyticType.FILTER_ONLY) {
                b.append(" ");
            }
        }

        switch (type) {
            case FILTER_ONLY:
                return b.toString();
            case WITHIN_GROUP:
                b.append("WITHIN GROUP");
                break;
            default:
                b.append("OVER");
        }
        b.append(" (");

        partitionBy.toStringPartitionBy(b);
        orderBy.toStringOrderByElements(b);

        if (windowElement != null) {
            if (orderBy.getOrderByElements() != null) {
                b.append(' ');
            }
            b.append(windowElement);
        }

        b.append(")");

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
}

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
public class FilterOverImpl extends ASTNodeAccessImpl {
    private final OrderByClause orderBy = new OrderByClause();
    private final PartitionByClause partitionBy = new PartitionByClause();
    private AnalyticType analyticType = AnalyticType.FILTER_ONLY;
    private Expression filterExpression = null;
    private WindowElement windowElement = null;

    public AnalyticType getAnalyticType() {
        return analyticType;
    }

    public void setAnalyticType(AnalyticType analyticType) {
        this.analyticType = analyticType;
    }
    
    public FilterOverImpl withAnalyticType(AnalyticType analyticType) {
        this.setAnalyticType(analyticType);
        return this;
    }

    public List<OrderByElement> getOrderByElements() {
        return orderBy.getOrderByElements();
    }

    public void setOrderByElements(List<OrderByElement> orderByElements) {
        orderBy.setOrderByElements(orderByElements);
    }
    
    public FilterOverImpl withOrderByElements(List<OrderByElement> orderByElements) {
        this.setOrderByElements(orderByElements);
        return this;
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
    
    public Expression getFilterExpression() {
        return filterExpression;
    }

    public void setFilterExpression(Expression filterExpression) {
        this.filterExpression = filterExpression;
    }


    public FilterOverImpl withFilterExpression(Expression filterExpression) {
        this.setFilterExpression(filterExpression);
        return this;
    }

    public WindowElement getWindowElement() {
        return windowElement;
    }

    public void setWindowElement(WindowElement windowElement) {
        this.windowElement = windowElement;
    }
    
    public FilterOverImpl withWindowElement(WindowElement windowElement) {
        this.setWindowElement(windowElement);
        return this;
    }
    
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public StringBuilder append(StringBuilder builder) {
        if (filterExpression != null) {
            builder.append("FILTER (WHERE ");
            builder.append(filterExpression.toString());
            builder.append(")");
            if (analyticType != AnalyticType.FILTER_ONLY) {
                builder.append(" ");
            }
        }

        switch (analyticType) {
            case FILTER_ONLY:
                return builder;
            case WITHIN_GROUP:
                builder.append("WITHIN GROUP");
                break;
            default:
                builder.append("OVER");
        }
        builder.append(" (");

        partitionBy.toStringPartitionBy(builder);
        orderBy.toStringOrderByElements(builder);

        if (windowElement != null) {
            if (orderBy.getOrderByElements() != null) {
                builder.append(' ');
            }
            builder.append(windowElement);
        }

        builder.append(")");

        return builder;
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return append(builder).toString();
    }
}

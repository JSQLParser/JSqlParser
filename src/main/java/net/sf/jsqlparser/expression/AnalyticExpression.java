/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2013 JSQLParser
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.select.OrderByElement;

import java.util.List;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;

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
        partitionBy.setPartitionExpressionList(partitionExpressionList);
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
        return orderBy.getWindowElement();
    }

    public void setWindowElement(WindowElement windowElement) {
        orderBy.setWindowElement(windowElement);
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

        switch (type) {
            case WITHIN_GROUP:
                b.append("WITHIN GROUP");
                break;
            default:
                b.append("OVER");
        }
        b.append(" (");

        partitionBy.toStringPartitionBy(b);
        orderBy.toStringOrderByElements(b);

        b.append(")");

        return b.toString();
    }

    public boolean isAllColumns() {
        return allColumns;
    }

    public void setAllColumns(boolean allColumns) {
        this.allColumns = allColumns;
    }

}

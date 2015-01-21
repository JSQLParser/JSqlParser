/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2014 JSQLParser
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
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;

/**
 * Represents Oracle aggregate/analytic LISTAGG function
 * @see <a href="http://docs.oracle.com/cd/E11882_01/server.112/e41084/functions089.htm#SQLRF30030">
 * http://docs.oracle.com/cd/E11882_01/server.112/e41084/functions089.htm#SQLRF30030</a>
 * @author bkrahmer
 */
public class ListaggFunction implements SelectItem {

    private Expression measureExpression;
    private Expression delimiter;
    private OrderByElement orderByElement;
    private ExpressionList queryPartitionList;
    private Alias alias;
    
    @Override
    public void accept(SelectItemVisitor selectItemVisitor) {
    	selectItemVisitor.visit(this);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" LISTAGG (");
        sb.append(measureExpression);
        if (delimiter != null) {
            sb.append(", ");
            sb.append(delimiter);
        }
        sb.append(") WITHIN GROUP (ORDER BY ");
        sb.append(orderByElement);
        sb.append(")");
        if (queryPartitionList != null) {
            sb.append(" OVER (PARTITION BY ");
            sb.append(PlainSelect.getStringList(queryPartitionList.getExpressions(), true, false));
            sb.append(") ");
        }
        if (alias != null) {
            sb.append(" ");
            sb.append(alias);
        }
        return sb.toString();
    }

    public Expression getMeasureExpression() {
        return measureExpression;
    }

    public void setMeasureExpression(Expression measureExpression) {
        this.measureExpression = measureExpression;
    }

    public Expression getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(Expression delimiter) {
        this.delimiter = delimiter;
    }

    public OrderByElement getOrderByElement() {
        return orderByElement;
    }

    public void setOrderByElement(OrderByElement orderByElement) {
        this.orderByElement = orderByElement;
    }

    public ExpressionList getQueryPartitionList() {
        return queryPartitionList;
    }

    public void setQueryPartitionList(ExpressionList queryPartitionList) {
        this.queryPartitionList = queryPartitionList;
    }

    public Alias getAlias() {
        return alias;
    }

    public void setAlias(Alias alias) {
        this.alias = alias;
    }

}

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
import static java.util.stream.Collectors.joining;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.select.OrderByElement;

public class XMLSerializeExpr extends ASTNodeAccessImpl implements Expression {

    private Column column;
    private List<OrderByElement> orderByElements;
    private ColDataType dataType;

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    public Column getColumn() {
        return column;
    }

    public void setColumn(Column column) {
        this.column = column;
    }

    public List<OrderByElement> getOrderByElements() {
        return orderByElements;
    }

    public void setOrderByElements(List<OrderByElement> orderByElements) {
        this.orderByElements = orderByElements;
    }

    public ColDataType getDataType() {
        return dataType;
    }

    public void setDataType(ColDataType dataType) {
        this.dataType = dataType;
    }
    
    @Override
    public String toString() {
        return "xmlserialize(xmlagg(xmltext(" + column + ") ORDER BY " + 
                orderByElements.stream().map(item -> item.toString()).collect(joining(", ")) + 
                ") AS " + dataType + ")";
    }
}

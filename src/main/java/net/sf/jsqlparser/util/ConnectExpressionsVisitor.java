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
package net.sf.jsqlparser.util;

import java.util.LinkedList;
import java.util.List;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.WithItem;

/**
 * Connect all selected expressions with a binary expression. Out of select a,b from table one gets
 * select a || b as expr from table. The type of binary expression is set by overwriting this class
 * abstract method createBinaryExpression.
 *
 * @author tw
 */
public abstract class ConnectExpressionsVisitor implements SelectVisitor, SelectItemVisitor {

    private String alias = "expr";
    private final List<SelectExpressionItem> itemsExpr = new LinkedList<SelectExpressionItem>();

    public ConnectExpressionsVisitor() {}

    public ConnectExpressionsVisitor(String alias) {
        this.alias = alias;
    }

    /**
     * Create instances of this binary expression that connects all selected expressions.
     *
     * @return
     */
    protected abstract BinaryExpression createBinaryExpression();

    @Override
    public void visit(PlainSelect plainSelect) {
        for (SelectItem item : plainSelect.getSelectItems()) {
            item.accept(this);
        }

        if (itemsExpr.size() > 1) {
            BinaryExpression binExpr = createBinaryExpression();
            binExpr.setLeftExpression(itemsExpr.get(0).getExpression());
            for (int i = 1; i < itemsExpr.size() - 1; i++) {
                binExpr.setRightExpression(itemsExpr.get(i).getExpression());
                BinaryExpression binExpr2 = createBinaryExpression();
                binExpr2.setLeftExpression(binExpr);
                binExpr = binExpr2;
            }
            binExpr.setRightExpression(itemsExpr.get(itemsExpr.size() - 1).getExpression());

            SelectExpressionItem sei = new SelectExpressionItem();
            sei.setExpression(binExpr);

            plainSelect.getSelectItems().clear();
            plainSelect.getSelectItems().add(sei);
        }

        ((SelectExpressionItem) plainSelect.getSelectItems().get(0)).setAlias(new Alias(alias));
    }

    @Override
    public void visit(SetOperationList setOpList) {
        for (SelectBody select : setOpList.getSelects()) {
            select.accept(this);
        }
    }

    @Override
    public void visit(WithItem withItem) {}

    @Override
    public void visit(AllTableColumns allTableColumns) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(AllColumns allColumns) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        itemsExpr.add(selectExpressionItem);
    }
}

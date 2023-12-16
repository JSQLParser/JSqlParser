/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util;

import java.util.LinkedList;
import java.util.List;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.TableStatement;
import net.sf.jsqlparser.statement.select.Values;
import net.sf.jsqlparser.statement.select.WithItem;

/**
 * Connect all selected expressions with a binary expression. Out of select a,b from table one gets
 * select a || b as expr from table. The type of binary expression is set by overwriting this class
 * abstract method createBinaryExpression.
 *
 * @author tw
 */
@SuppressWarnings({"PMD.UncommentedEmptyMethodBody"})
public abstract class ConnectExpressionsVisitor implements SelectVisitor, SelectItemVisitor {

    private String alias = "expr";
    private final List<SelectItem> itemsExpr = new LinkedList<SelectItem>();

    public ConnectExpressionsVisitor() {}

    public ConnectExpressionsVisitor(String alias) {
        this.alias = alias;
    }

    protected abstract BinaryExpression createBinaryExpression();

    @Override
    public void visit(ParenthesedSelect parenthesedSelect) {
        parenthesedSelect.getSelect().accept(this);
    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {
        lateralSubSelect.getSelect().accept(this);
    }

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

            SelectItem sei = new SelectItem();
            sei.setExpression(binExpr);

            plainSelect.getSelectItems().clear();
            plainSelect.getSelectItems().add(sei);
        }

        plainSelect.getSelectItems().get(0).setAlias(new Alias(alias));
    }

    @Override
    public void visit(SetOperationList setOpList) {
        for (Select select : setOpList.getSelects()) {
            select.accept(this);
        }
    }

    @Override
    public void visit(WithItem withItem) {}

    @Override
    public void visit(SelectItem selectItem) {
        itemsExpr.add(selectItem);
    }

    @Override
    public void visit(Values aThis) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(TableStatement tableStatement) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

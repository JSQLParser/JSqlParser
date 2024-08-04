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
import net.sf.jsqlparser.expression.Expression;
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
public abstract class ConnectExpressionsVisitor<T>
        implements SelectVisitor<T>, SelectItemVisitor<T> {

    private final List<SelectItem<? extends Expression>> itemsExpr =
            new LinkedList<SelectItem<? extends Expression>>();
    private String alias = "expr";

    public ConnectExpressionsVisitor() {}

    public ConnectExpressionsVisitor(String alias) {
        this.alias = alias;
    }

    protected abstract BinaryExpression createBinaryExpression();

    @Override
    public <S> T visit(ParenthesedSelect parenthesedSelect, S context) {
        parenthesedSelect.getSelect().accept(this, context);
        return null;
    }

    @Override
    public <S> T visit(LateralSubSelect lateralSubSelect, S context) {
        lateralSubSelect.getSelect().accept(this, context);
        return null;
    }

    @Override
    public <S> T visit(PlainSelect plainSelect, S context) {
        for (SelectItem<?> item : plainSelect.getSelectItems()) {
            item.accept(this, context);
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

            SelectItem<Expression> sei = new SelectItem<>();
            sei.setExpression(binExpr);

            plainSelect.getSelectItems().clear();
            plainSelect.getSelectItems().add(sei);
        }

        plainSelect.getSelectItems().get(0).setAlias(new Alias(alias));
        return null;
    }

    @Override
    public <S> T visit(SetOperationList setOpList, S context) {
        for (Select select : setOpList.getSelects()) {
            select.accept(this, context);
        }
        return null;
    }

    @Override
    public <S> T visit(WithItem<?> withItem, S context) {
        return null;
    }

    @Override
    public <S> T visit(SelectItem<? extends Expression> selectItem, S context) {
        itemsExpr.add(selectItem);
        return null;
    }

    @Override
    public <S> T visit(Values aThis, S context) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <S> T visit(TableStatement tableStatement, S context) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

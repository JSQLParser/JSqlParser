/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2023 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.ParenthesedStatement;
import net.sf.jsqlparser.statement.StatementVisitor;

import java.util.Collection;
import java.util.List;

public class ParenthesedSelect extends Select implements FromItem, ParenthesedStatement {
    Alias alias;
    Pivot pivot;
    UnPivot unPivot;
    Select select;

    public ParenthesedSelect() {}

    public ParenthesedSelect(FromItem fromItem) {
        this.select = new PlainSelect(fromItem);
        this.alias = getAliasFromItem(fromItem);
    }

    public ParenthesedSelect(FromItem fromItem, Expression whereExpressions) {
        this.select = new PlainSelect(fromItem, whereExpressions);
        this.alias = getAliasFromItem(fromItem);
    }

    public ParenthesedSelect(FromItem fromItem, Collection<Expression> orderByExpressions) {
        this.select = new PlainSelect(fromItem, orderByExpressions);
        this.alias = getAliasFromItem(fromItem);
    }

    public ParenthesedSelect(FromItem fromItem, Expression whereExpressions,
            Collection<Expression> orderByExpressions) {
        this.select = new PlainSelect(fromItem, whereExpressions, orderByExpressions);
        this.alias = getAliasFromItem(fromItem);
    }

    public ParenthesedSelect(Collection<Expression> selectExpressions, FromItem fromItem) {
        this.select = new PlainSelect(selectExpressions, fromItem);
        this.alias = getAliasFromItem(fromItem);
    }

    public ParenthesedSelect(Collection<Expression> selectExpressions, FromItem fromItem,
            Expression whereExpressions) {
        this.select = new PlainSelect(selectExpressions, fromItem, whereExpressions);
        this.alias = getAliasFromItem(fromItem);
    }

    public ParenthesedSelect(Collection<Expression> selectExpressions, FromItem fromItem,
            Collection<Expression> orderByExpressions) {
        this.select = new PlainSelect(selectExpressions, fromItem, orderByExpressions);
        this.alias = getAliasFromItem(fromItem);
    }

    public ParenthesedSelect(Collection<Expression> selectExpressions, FromItem fromItem,
            Expression whereExpressions, Collection<Expression> orderByExpressions) {
        this.select =
                new PlainSelect(selectExpressions, fromItem, whereExpressions, orderByExpressions);
        this.alias = getAliasFromItem(fromItem);
    }

    private static Alias getAliasFromItem(FromItem fromItem) {
        if (fromItem instanceof Table && fromItem.getAlias() == null) {
            Table t = (Table) fromItem;
            return new Alias(t.getName(), true);
        } else {
            return new Alias(fromItem.getAlias().getName(), true);
        }
    }

    @Override
    public Alias getAlias() {
        return alias;
    }

    @Override
    public void setAlias(Alias alias) {
        this.alias = alias;
    }

    public ParenthesedSelect withAlias(Alias alias) {
        this.setAlias(alias);
        return this;
    }

    @Override
    public Pivot getPivot() {
        return pivot;
    }

    @Override
    public void setPivot(Pivot pivot) {
        this.pivot = pivot;
    }

    public UnPivot getUnPivot() {
        return unPivot;
    }

    public void setUnPivot(UnPivot unPivot) {
        this.unPivot = unPivot;
    }

    public Select getSelect() {
        return select;
    }

    public void setSelect(Select select) {
        this.select = select;
    }

    public Values getValues() {
        return (Values) select;
    }

    public PlainSelect getPlainSelect() {
        return (PlainSelect) select;
    }

    public SetOperationList getSetOperationList() {
        return (SetOperationList) select;
    }

    public ParenthesedSelect withSelect(Select selectBody) {
        setSelect(selectBody);
        return this;
    }

    public ParenthesedSelect withOrderByElements(List<OrderByElement> orderByElements) {
        this.select.setOrderByElements(orderByElements);
        return this;
    }

    @Override
    public <T, S> T accept(SelectVisitor<T> selectVisitor, S context) {
        return selectVisitor.visit(this, context);
    }

    @Override
    public <T, S> T accept(FromItemVisitor<T> fromItemVisitor, S context) {
        return fromItemVisitor.visit(this, context);
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    public StringBuilder appendSelectBodyTo(StringBuilder builder) {
        builder.append("(").append(select).append(")");
        appendTo(builder, alias, pivot, unPivot);
        return builder;
    }
}

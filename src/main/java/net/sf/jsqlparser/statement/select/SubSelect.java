/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

public class SubSelect extends ASTNodeAccessImpl implements FromItem, Expression, ItemsList {

    private SelectBody selectBody;
    private Alias alias;
    private boolean useBrackets = true;
    private List<WithItem> withItemsList;

    private Pivot pivot;
    private UnPivot unpivot;

    @Override
    public void accept(FromItemVisitor fromItemVisitor) {
        fromItemVisitor.visit(this);
    }

    public SelectBody getSelectBody() {
        return selectBody;
    }

    public void setSelectBody(SelectBody body) {
        selectBody = body;
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    @Override
    public Alias getAlias() {
        return alias;
    }

    @Override
    public void setAlias(Alias alias) {
        this.alias = alias;
    }

    @Override
    public Pivot getPivot() {
        return pivot;
    }

    @Override
    public void setPivot(Pivot pivot) {
        this.pivot = pivot;
    }

    @Override
    public UnPivot getUnPivot() {
        return this.unpivot;
    }

    @Override
    public void setUnPivot(UnPivot unpivot) {
        this.unpivot = unpivot;
    }

    public boolean isUseBrackets() {
        return useBrackets;
    }

    public void setUseBrackets(boolean useBrackets) {
        this.useBrackets = useBrackets;
    }

    public List<WithItem> getWithItemsList() {
        return withItemsList;
    }

    public void setWithItemsList(List<WithItem> withItemsList) {
        this.withItemsList = withItemsList;
    }

    @Override
    public void accept(ItemsListVisitor itemsListVisitor) {
        itemsListVisitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder retval = new StringBuilder();
        if (useBrackets) {
            retval.append("(");
        }
        if (withItemsList != null && !withItemsList.isEmpty()) {
            retval.append("WITH ");
            for (Iterator<WithItem> iter = withItemsList.iterator(); iter.hasNext();) {
                WithItem withItem = iter.next();
                retval.append(withItem);
                if (iter.hasNext()) {
                    retval.append(",");
                }
                retval.append(" ");
            }
        }
        retval.append(selectBody);
        if (useBrackets) {
            retval.append(")");
        }

        if (alias != null) {
            retval.append(alias.toString());
        }
        if (pivot != null) {
            retval.append(" ").append(pivot);
        }
        if (unpivot != null) {
            retval.append(" ").append(unpivot);
        }

        return retval.toString();
    }

    public SubSelect alias(Alias alias) {
        this.setAlias(alias);
        return this;
    }

    public SubSelect useBrackets(boolean useBrackets) {
        this.setUseBrackets(useBrackets);
        return this;
    }

    public SubSelect withItemsList(List<WithItem> withItemsList) {
        this.setWithItemsList(withItemsList);
        return this;
    }

    public SubSelect pivot(Pivot pivot) {
        this.setPivot(pivot);
        return this;
    }

    public SubSelect selectBody(SelectBody selectBody) {
        this.setSelectBody(selectBody);
        return this;
    }

    public SubSelect addWithItemsList(WithItem... withItemsList) {
        List<WithItem> collection = Optional.ofNullable(getWithItemsList()).orElseGet(ArrayList::new);
        Collections.addAll(collection, withItemsList);
        return this.withItemsList(collection);
    }

    public SubSelect addWithItemsList(Collection<? extends WithItem> withItemsList) {
        List<WithItem> collection = Optional.ofNullable(getWithItemsList()).orElseGet(ArrayList::new);
        collection.addAll(withItemsList);
        return this.withItemsList(collection);
    }
}

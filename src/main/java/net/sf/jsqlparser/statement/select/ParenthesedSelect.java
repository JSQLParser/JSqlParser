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

public class ParenthesedSelect extends Select implements FromItem {
    Alias alias;
    Pivot pivot;
    UnPivot unPivot;
    Select select;

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

    public ParenthesedSelect withSelect(Select selectBody) {
        setSelect(selectBody);
        return this;
    }

    @Override
    public void accept(SelectVisitor selectVisitor) {
        selectVisitor.visit(this);
    }

    @Override
    public void accept(FromItemVisitor fromItemVisitor) {
        fromItemVisitor.visit(this);
    }

    public StringBuilder appendSelectBodyTo(StringBuilder builder) {
        builder.append("(").append(select).append(")");
        if (alias != null) {
            builder.append(alias);
        }

        if (pivot != null) {
            builder.append(" ").append(pivot);
        }
        if (unPivot != null) {
            builder.append(" ").append(unPivot);
        }
        return builder;
    }
}

package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.expression.Alias;

public class ParenthesedSelectBody extends SelectBody implements FromItem {
    Alias alias;
    Pivot pivot;
    UnPivot unPivot;
    SelectBody selectBody;

    @Override
    public Alias getAlias() {
        return alias;
    }

    @Override
    public void setAlias(Alias alias) {
        this.alias = alias;
    }

    public ParenthesedSelectBody withAlias(Alias alias) {
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

    public SelectBody getSelectBody() {
        return selectBody;
    }

    public void setSelectBody(SelectBody selectBody) {
        this.selectBody = selectBody;
    }

    public ParenthesedSelectBody withSelectBody(SelectBody selectBody) {
        setSelectBody(selectBody);
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
        builder.append("(").append(selectBody).append(")");
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

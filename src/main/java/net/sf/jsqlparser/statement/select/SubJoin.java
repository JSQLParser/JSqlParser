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

import java.util.List;

import net.sf.jsqlparser.expression.Alias;

public class SubJoin implements FromItem {

    private FromItem left;
    private Alias alias;
    private Pivot pivot;
    private UnPivot unpivot;
    private List<Join> joinList;

    @Override
    public void accept(FromItemVisitor fromItemVisitor) {
        fromItemVisitor.visit(this);
    }

    public FromItem getLeft() {
        return left;
    }

    public SubJoin left(FromItem l) {
        setLeft(l);
        return this;
    }

    public void setLeft(FromItem l) {
        left = l;
    }

    public List<Join> getJoinList() {
        return joinList;
    }

    public void setJoinList(List<Join> joinList) {
        this.joinList = joinList;
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

    @Override
    public Alias getAlias() {
        return alias;
    }

    @Override
    public void setAlias(Alias alias) {
        this.alias = alias;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(left);
        for (Join j : joinList) {
            if (j.isSimple()) {
                sb.append(", ").append(j);
            } else {
                sb.append(" ").append(j);
            }
        }

        sb.append(")").append((alias != null) ? (" " + alias.toString()) : "")
                .append((pivot != null) ? " " + pivot : "").append((unpivot != null) ? " " + unpivot : "");
        return sb.toString();
    }

    public SubJoin alias(Alias alias) {
        this.setAlias(alias);
        return this;
    }

    public SubJoin pivot(Pivot pivot) {
        this.setPivot(pivot);
        return this;
    }

    public SubJoin joinList(List<Join> joinList) {
        this.setJoinList(joinList);
        return this;
    }
}

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

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ParenthesedFromItem extends ASTNodeAccessImpl implements FromItem {
    private FromItem fromItem;
    private List<Join> joins;
    private Alias alias;
    private Pivot pivot;
    private UnPivot unPivot;
    private SampleClause sampleClause;

    public ParenthesedFromItem() {}

    public ParenthesedFromItem(FromItem fromItem) {
        setFromItem(fromItem);
    }

    public FromItem getFromItem() {
        return fromItem;
    }

    public final void setFromItem(FromItem fromItem) {
        this.fromItem = fromItem;
    }

    public List<Join> getJoins() {
        return joins;
    }

    public void setJoins(List<Join> list) {
        joins = list;
    }

    public Join getJoin(int index) {
        return joins.get(index);
    }

    public FromItem addJoins(Join... joins) {
        List<Join> list = Optional.ofNullable(getJoins()).orElseGet(ArrayList::new);
        Collections.addAll(list, joins);
        return withJoins(list);
    }

    public FromItem withJoins(List<Join> joins) {
        this.setJoins(joins);
        return this;
    }

    @Override
    public <T, S> T accept(FromItemVisitor<T> fromItemVisitor, S context) {
        return fromItemVisitor.visit(this, context);
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("(");
        builder.append(fromItem);
        if (joins != null) {
            for (Join join : joins) {
                if (join.isSimple()) {
                    builder.append(", ").append(join);
                } else {
                    builder.append(" ").append(join);
                }
            }
        }
        builder.append(")");

        if (alias != null) {
            builder.append(alias);
        }

        if (pivot != null) {
            builder.append(pivot);
        }

        if (unPivot != null) {
            builder.append(unPivot);
        }

        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
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
        return unPivot;
    }

    @Override
    public void setUnPivot(UnPivot unpivot) {
        this.unPivot = unpivot;
    }

    @Override
    public SampleClause getSampleClause() {
        return sampleClause;
    }

    @Override
    public FromItem setSampleClause(SampleClause sampleClause) {
        this.sampleClause = sampleClause;
        return this;
    }

    public ParenthesedFromItem withSampleClause(SampleClause sampleClause) {
        this.sampleClause = sampleClause;
        return this;
    }

    public ParenthesedFromItem withFromItem(FromItem fromItem) {
        this.setFromItem(fromItem);
        return this;
    }

    @Override
    public ParenthesedFromItem withAlias(Alias alias) {
        this.setAlias(alias);
        return this;
    }

    public <E extends FromItem> E getFromItem(Class<E> type) {
        return type.cast(getFromItem());
    }
}

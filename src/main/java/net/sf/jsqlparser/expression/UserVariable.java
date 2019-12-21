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

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.Pivot;
import net.sf.jsqlparser.statement.select.UnPivot;

/**
 * Simple uservariables like @test.
 */
public class UserVariable extends ASTNodeAccessImpl implements Expression, FromItem {

    private String name;
    private boolean doubleAdd = false;

    private Alias alias;
    private Pivot pivot;
    private UnPivot unpivot;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    public boolean isDoubleAdd() {
        return doubleAdd;
    }

    public void setDoubleAdd(boolean doubleAdd) {
        this.doubleAdd = doubleAdd;
    }

    @Override
    public void accept(FromItemVisitor fromItemVisitor) {
        fromItemVisitor.visit(this);
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
        return unpivot;
    }

    @Override
    public void setUnPivot(UnPivot unpivot) {
        this.unpivot = unpivot;
    }

    @Override
    public String toString() {
        return getAtName()
                + ((alias != null) ? alias.toString() : "")
                + ((pivot != null) ? " " + pivot : "")
                + ((unpivot != null) ? " " + unpivot : "");
    }

    public String getAtName() {
        return "@" + (doubleAdd ? "@" : "") + name;
    }
}

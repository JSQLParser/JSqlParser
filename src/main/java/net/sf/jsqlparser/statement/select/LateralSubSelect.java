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

/**
 * lateral sub select
 * 
 * @author tobens
 */
public class LateralSubSelect extends ParenthesedSelect {
    private String prefix;

    public LateralSubSelect() {
        this("LATERAL");
    }

    public LateralSubSelect(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public LateralSubSelect withPrefix(String prefix) {
        this.setPrefix(prefix);
        return this;
    }

    public LateralSubSelect withSelect(Select select) {
        setSelect(select);
        return this;
    }

    public String toString() {
        return prefix + super.toString();
    }

    public void accept(SelectVisitor selectVisitor) {
        selectVisitor.visit(this);
    }

    @Override
    public void accept(FromItemVisitor fromItemVisitor) {
        fromItemVisitor.visit(this);
    }
}

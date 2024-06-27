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
        this(prefix, null, null);
    }

    public LateralSubSelect(String prefix, Select select) {
        this(prefix, select, null);
    }

    public LateralSubSelect(Select select, Alias alias) {
        this("LATERAL", select, alias);
    }

    public LateralSubSelect(String prefix, Select select, Alias alias) {
        this.prefix = prefix;
        this.select = select;
        this.alias = alias;
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

    public LateralSubSelect withAlias(Alias alias) {
        setAlias(alias);
        return this;
    }

    public String toString() {
        return prefix + super.toString();
    }

    @Override
    public <T, S> T accept(SelectVisitor<T> selectVisitor, S context) {
        return selectVisitor.visit(this, context);
    }

    @Override
    public <T, S> T accept(FromItemVisitor<T> fromItemVisitor, S context) {
        return fromItemVisitor.visit(this, context);
    }
}

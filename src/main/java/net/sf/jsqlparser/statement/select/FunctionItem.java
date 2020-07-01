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

import net.sf.jsqlparser.Model;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Function;

public class FunctionItem implements Model {

    private Function function;
    private Alias alias;

    public Alias getAlias() {
        return alias;
    }

    public void setAlias(Alias alias) {
        this.alias = alias;
    }

    public Function getFunction() {
        return function;
    }

    public void setFunction(Function function) {
        this.function = function;
    }

    @Override
    public String toString() {
        return function + ((alias != null) ? alias.toString() : "");
    }

    public FunctionItem function(Function function) {
        this.setFunction(function);
        return this;
    }

    public FunctionItem alias(Alias alias) {
        this.setAlias(alias);
        return this;
    }
}

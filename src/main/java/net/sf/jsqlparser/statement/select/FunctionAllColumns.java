/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.Function;


public class FunctionAllColumns extends AllColumns {
    private Function function;

    public FunctionAllColumns(Function function) {
        super(null, null, null);
        this.function = function;
    }

    public Function getFunction() {
        return function;
    }

    public FunctionAllColumns setFunction(Function function) {
        this.function = function;
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("(");
        builder.append(function);
        builder.append(").*");
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }
}

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
import java.util.List;
import net.sf.jsqlparser.expression.ColumnsTransformer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

public class AllColumns extends ASTNodeAccessImpl implements Expression {
    private List<ColumnsTransformer> transformers;

    public AllColumns() {
        this(null);
    }

    public AllColumns(List<ColumnsTransformer> transformers) {
        this.transformers = transformers;
    }

    public List<ColumnsTransformer> getTransformers() {
        if (transformers == null) {
            transformers = new ArrayList<>();
        }
        return transformers;
    }

    public AllColumns setTransformers(List<ColumnsTransformer> transformers) {
        this.transformers = transformers;
        return this;
    }

    public AllColumns addTransformer(ColumnsTransformer transformer) {
        getTransformers().add(transformer);
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("*");
        for (ColumnsTransformer transformer : getTransformers()) {
            builder.append(" ").append(transformer);
        }
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

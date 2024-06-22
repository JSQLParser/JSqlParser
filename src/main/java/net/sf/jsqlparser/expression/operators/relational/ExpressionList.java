/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression.operators.relational;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.parser.SimpleNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * A list of expressions, as in SELECT A FROM TAB WHERE B IN (expr1,expr2,expr3)
 */
public class ExpressionList<T extends Expression> extends ArrayList<T>
        implements Expression, Serializable {
    private transient SimpleNode node;

    public ExpressionList(Collection<? extends T> expressions) {
        addAll(expressions);
    }

    public ExpressionList(List<T> expressions) {
        super(expressions);
    }

    public ExpressionList(T... expressions) {
        this(Arrays.asList(expressions));
    }

    @Deprecated
    public boolean isUsingBrackets() {
        return false;
    }

    @Deprecated
    public List<T> getExpressions() {
        return this;
    }

    @Deprecated
    public void setExpressions(List<T> expressions) {
        this.clear();
        this.addAll(expressions);
    }

    public ExpressionList addExpression(T expression) {
        this.add(expression);
        return this;
    }

    public ExpressionList addExpressions(T... expressions) {
        addAll(Arrays.asList(expressions));
        return this;
    }

    public ExpressionList<?> addExpressions(Collection<T> expressions) {
        addAll(expressions);
        return this;
    }

    public ExpressionList withExpressions(T... expressions) {
        this.clear();
        return addExpressions(expressions);
    }

    public ExpressionList<?> withExpressions(Collection<T> expressions) {
        this.clear();
        return addExpressions(expressions);
    }

    public StringBuilder appendTo(StringBuilder builder) {
        for (int i = 0; i < size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(get(i));
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public <K, S> K accept(ExpressionVisitor<K> expressionVisitor, S arguments) {
        return expressionVisitor.visit(this, arguments);
    }

    @Override
    public SimpleNode getASTNode() {
        return node;
    }

    @Override
    public void setASTNode(SimpleNode node) {
        this.node = node;
    }
}

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
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

public class SelectItem<T extends Expression> extends ASTNodeAccessImpl {

    private T expression;
    private Alias alias;

    public SelectItem(T expression, Alias alias) {
        this.expression = expression;
        this.alias = alias;
    }

    public SelectItem(T expression, String aliasName) {
        this.expression = expression;
        this.alias = new Alias(aliasName);
    }

    public SelectItem(Long expression, String aliasName) {
        this((T) new LongValue(expression), aliasName);
    }

    public SelectItem(Integer expression, String aliasName) {
        this((T) new LongValue(expression), aliasName);
    }

    public SelectItem(Double expression, String aliasName) {
        this((T) new DoubleValue(expression), aliasName);
    }

    public SelectItem(String expression, String aliasName) {
        this((T) new StringValue(expression), aliasName);
    }

    public SelectItem() {
        this(null, (Alias) null);
    }

    public SelectItem(T expression) {
        this(expression, (Alias) null);
    }

    public static SelectItem<?> from(Expression expression, Alias alias) {
        return new SelectItem<>(expression, alias);
    }

    public static SelectItem<?> from(Expression expression) {
        return from(expression, null);
    }

    public Alias getAlias() {
        return alias;
    }

    public String getAliasName() {
        return alias != null ? alias.getName() : null;
    }

    public String getUnquotedAliasName() {
        return alias != null ? alias.getUnquotedName() : null;
    }

    public void setAlias(Alias alias) {
        this.alias = alias;
    }

    public T getExpression() {
        return expression;
    }

    public void setExpression(T expression) {
        this.expression = expression;
    }

    public <K, S> K accept(SelectItemVisitor<K> selectItemVisitor, S context) {
        return selectItemVisitor.visit(this, context);
    }

    @Override
    public String toString() {
        return expression + ((alias != null) ? alias.toString() : "");
    }

    public SelectItem<T> withExpression(T expression) {
        this.setExpression(expression);
        return this;
    }

    public SelectItem<?> withAlias(Alias alias) {
        this.setAlias(alias);
        return this;
    }

    public <E extends Expression> E getExpression(Class<E> type) {
        return type.cast(getExpression());
    }
}

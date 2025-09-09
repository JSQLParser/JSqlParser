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
import net.sf.jsqlparser.statement.ParenthesedStatement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.delete.ParenthesedDelete;
import net.sf.jsqlparser.statement.insert.ParenthesedInsert;
import net.sf.jsqlparser.statement.update.ParenthesedUpdate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class WithItem<K extends ParenthesedStatement> implements Serializable {
    private K statement;
    private Alias alias;
    private List<SelectItem<?>> withItemList;
    private WithFunctionDeclaration withFunctionDeclaration;
    private boolean recursive = false;
    private boolean usingNot = false;
    private boolean materialized = false;

    public WithItem(K statement, Alias alias) {
        this.statement = statement;
        this.alias = alias;
    }

    public WithItem() {
        this(null, (Alias) null);
    }

    public K getParenthesedStatement() {
        return statement;
    }

    public void setParenthesedStatement(K statement) {
        this.statement = statement;
    }

    public WithItem<K> withParenthesedStatement(K statement) {
        this.setParenthesedStatement(statement);
        return this;
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

    public WithItem<?> withAlias(Alias alias) {
        this.setAlias(alias);
        return this;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public boolean isMaterialized() {
        return materialized;
    }

    public void setMaterialized(boolean materialized) {
        this.materialized = materialized;
    }

    public K getStatement() {
        return statement;
    }

    public WithItem<K> setStatement(K statement) {
        this.statement = statement;
        return this;
    }

    public boolean isUsingNot() {
        return usingNot;
    }

    public WithItem<K> setUsingNot(boolean usingNot) {
        this.usingNot = usingNot;
        return this;
    }

    /**
     * The {@link SelectItem}s in this WITH (for example the A,B,C in "WITH mywith (A,B,C) AS ...")
     *
     * @return a list of {@link SelectItem}s
     */
    public List<SelectItem<?>> getWithItemList() {
        return withItemList;
    }

    public void setWithItemList(List<SelectItem<?>> withItemList) {
        this.withItemList = withItemList;
    }

    public WithFunctionDeclaration getWithFunctionDeclaration() {
        return withFunctionDeclaration;
    }

    public void setWithFunctionDeclaration(WithFunctionDeclaration withFunctionDeclaration) {
        this.withFunctionDeclaration = withFunctionDeclaration;
    }

    public WithItem<K> withWithFunctionDeclaration(WithFunctionDeclaration withFunctionDeclaration) {
        this.setWithFunctionDeclaration(withFunctionDeclaration);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (withFunctionDeclaration != null) {
            builder.append(withFunctionDeclaration);
        } else {
            builder.append(recursive ? "RECURSIVE " : "");
            if (alias != null) {
                builder.append(alias.getName());
            }
            if (withItemList != null) {
                builder.append("(");
                int size = withItemList.size();
                for (int i = 0; i < size; i++) {
                    builder.append(withItemList.get(i)).append(i < size - 1 ? "," : "");
                }
                builder.append(")");
            }
            builder.append(" AS ");
            if (materialized) {
                builder.append(usingNot
                        ? "NOT MATERIALIZED "
                        : "MATERIALIZED ");
            }
            builder.append(statement);
        }
        return builder.toString();
    }

    @Deprecated
    public <T, S> T accept(SelectVisitor<T> selectVisitor, S context) {
        return selectVisitor.visit(this, context);
    }

    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statement.accept(statementVisitor, context);
    }

    public WithItem<?> withWithItemList(List<SelectItem<?>> withItemList) {
        this.setWithItemList(withItemList);
        return this;
    }

    public WithItem<?> withRecursive(boolean recursive, boolean materialized) {
        this.setRecursive(recursive);
        this.setMaterialized(materialized);
        return this;
    }

    public WithItem<?> withRecursive(boolean recursive, boolean usingNot, boolean materialized) {
        this.setRecursive(recursive);
        this.setUsingNot(usingNot);
        this.setMaterialized(materialized);
        return this;
    }

    public WithItem<?> addWithItemList(SelectItem<?>... withItemList) {
        List<SelectItem<?>> collection =
                Optional.ofNullable(getWithItemList()).orElseGet(ArrayList::new);
        Collections.addAll(collection, withItemList);
        return this.withWithItemList(collection);
    }

    public WithItem<?> addWithItemList(Collection<? extends SelectItem<?>> withItemList) {
        List<SelectItem<?>> collection =
                Optional.ofNullable(getWithItemList()).orElseGet(ArrayList::new);
        collection.addAll(withItemList);
        return this.withWithItemList(collection);
    }

    public ParenthesedSelect getSelect() {
        return (ParenthesedSelect) statement;
    }

    public ParenthesedInsert getInsert() {
        return (ParenthesedInsert) statement;
    }

    public ParenthesedUpdate getUpdate() {
        return (ParenthesedUpdate) statement;
    }

    public ParenthesedDelete getDelete() {
        return (ParenthesedDelete) statement;
    }

    public void setSelect(ParenthesedSelect select) {
        this.statement = (K) select;
    }

}

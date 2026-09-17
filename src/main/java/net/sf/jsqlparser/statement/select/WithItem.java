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

import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.statement.ParenthesedStatement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.delete.ParenthesedDelete;
import net.sf.jsqlparser.statement.insert.ParenthesedInsert;
import net.sf.jsqlparser.statement.update.ParenthesedUpdate;

public class WithItem<K extends ParenthesedStatement> implements Serializable {
    private K statement;
    private Alias alias;
    private List<SelectItem<?>> withItemList;
    private WithFunctionDeclaration withFunctionDeclaration;
    private Expression expression;
    private WithSearchClause searchClause;
    private WithCycleClause cycleClause;
    private boolean recursive = false;
    private boolean usingNot = false;
    private boolean materialized = false;
    private ExpressionList<Expression> usingKeyExpressions;

    public WithItem(K statement, Alias alias) {
        this.statement = statement;
        this.alias = alias;
    }

    public WithItem() {
        this(null, (Alias) null);
    }

    /**
     * The aliased expression of a WITH item like ClickHouse's
     * {@code WITH <expression> AS <identifier>}, where the item does not hold a statement.
     *
     * @return the expression of this WITH item, or null for statement-based (CTE) items
     */
    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public WithItem<K> withExpression(Expression expression) {
        this.setExpression(expression);
        return this;
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

    public WithItem<K> withWithFunctionDeclaration(
            WithFunctionDeclaration withFunctionDeclaration) {
        this.setWithFunctionDeclaration(withFunctionDeclaration);
        return this;
    }

    public WithSearchClause getSearchClause() {
        return searchClause;
    }

    public void setSearchClause(WithSearchClause searchClause) {
        this.searchClause = searchClause;
    }

    public WithItem<K> withSearchClause(WithSearchClause searchClause) {
        this.setSearchClause(searchClause);
        return this;
    }

    public WithCycleClause getCycleClause() {
        return cycleClause;
    }

    public void setCycleClause(WithCycleClause cycleClause) {
        this.cycleClause = cycleClause;
    }

    public WithItem<K> withCycleClause(WithCycleClause cycleClause) {
        setCycleClause(cycleClause);
        return this;
    }

    /**
     * DuckDB 2.0 aggregates a recursive CTE by key:
     * {@code WITH RECURSIVE tbl (a, b) USING KEY (a, avg(b)) AS (...)}.
     */
    public ExpressionList<Expression> getUsingKeyExpressions() {
        return usingKeyExpressions;
    }

    public void setUsingKeyExpressions(ExpressionList<Expression> usingKeyExpressions) {
        this.usingKeyExpressions = usingKeyExpressions;
    }

    public WithItem<K> withUsingKeyExpressions(ExpressionList<Expression> usingKeyExpressions) {
        setUsingKeyExpressions(usingKeyExpressions);
        return this;
    }

    public StringBuilder appendRecursiveClausesTo(StringBuilder builder,
            Consumer<Expression> expressionPrinter) {
        if (searchClause != null) {
            builder.append(" ");
            searchClause.appendTo(builder, expressionPrinter);
        }
        if (cycleClause != null) {
            builder.append(" ");
            cycleClause.appendTo(builder, expressionPrinter);
        }
        return builder;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (withFunctionDeclaration != null) {
            builder.append(withFunctionDeclaration);
        } else if (expression != null) {
            builder.append(expression);
            if (alias != null) {
                builder.append(" AS ").append(alias.getName());
            }
            appendRecursiveClausesTo(builder, expr -> builder.append(expr));
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
            if (usingKeyExpressions != null) {
                builder.append(" USING KEY (").append(usingKeyExpressions).append(")");
            }
            builder.append(" AS ");
            if (materialized) {
                builder.append(usingNot
                        ? "NOT MATERIALIZED "
                        : "MATERIALIZED ");
            }
            builder.append(statement);
            appendRecursiveClausesTo(builder, expression -> builder.append(expression));
        }
        return builder.toString();
    }

    @Deprecated
    public <T, S> T accept(SelectVisitor<T> selectVisitor, S context) {
        return selectVisitor.visit(this, context);
    }

    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        if (statement != null) {
            return statement.accept(statementVisitor, context);
        }
        return null;
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

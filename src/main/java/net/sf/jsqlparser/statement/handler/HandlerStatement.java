/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.handler;

import java.util.Objects;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.Offset;

/**
 * MySQL {@code HANDLER}, which opens a table for direct row access, reads rows from it and closes
 * it again.<br>
 * Examples:<br>
 * HANDLER db.t OPEN AS h<br>
 * HANDLER h READ i1 &gt;= (1, 2) WHERE a &gt; 0 LIMIT 3<br>
 * HANDLER h READ i1 NEXT<br>
 * HANDLER h READ FIRST<br>
 * HANDLER h CLOSE<br>
 * <br>
 * {@link #getTable()} is the table that OPEN opens, with the handler name as its alias when one is
 * given. READ and CLOSE name the handler, which is that alias or the unqualified table name, so
 * their {@link #getTable()} holds the handler name rather than a table reference. <br>
 * A READ through an index either compares the index with a list of key values
 * ({@link #getKeyComparison()} and {@link #getKeyValues()}) or moves in index order
 * ({@link #getDirection()}). A READ without an index moves in storage order: FIRST or NEXT.
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/handler.html">MySQL HANDLER</a>
 */
public class HandlerStatement extends ASTNodeAccessImpl implements Statement {
    private Action action = Action.OPEN;
    private Table table;
    private String indexName;
    private KeyComparison keyComparison;
    private ExpressionList<?> keyValues;
    private Direction direction;
    private Expression where;
    private Limit limit;
    private Offset offset;

    public enum Action {
        OPEN, READ, CLOSE
    }

    /** Where a READ continues: FIRST and NEXT, and through an index also PREV and LAST. */
    public enum Direction {
        FIRST, NEXT, PREV, LAST
    }

    /** How a READ through an index compares the index with the key values. */
    public enum KeyComparison {
        /** {@code =} */
        EQUALS("="),
        /** {@code <=} */
        LESS_THAN_OR_EQUALS("<="),
        /** {@code >=} */
        GREATER_THAN_OR_EQUALS(">="),
        /** {@code <} */
        LESS_THAN("<"),
        /** {@code >} */
        GREATER_THAN(">");

        private final String operator;

        KeyComparison(String operator) {
            this.operator = operator;
        }

        public String getOperator() {
            return operator;
        }
    }

    public HandlerStatement() {
        // empty constructor
    }

    public HandlerStatement(Action action, Table table) {
        setAction(action);
        this.table = table;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = Objects.requireNonNull(action);
    }

    public HandlerStatement withAction(Action action) {
        setAction(action);
        return this;
    }

    /**
     * Returns the table that OPEN opens, whose alias is the handler name, or the handler that READ
     * and CLOSE use.
     */
    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public HandlerStatement withTable(Table table) {
        setTable(table);
        return this;
    }

    /** Returns the name of the handler: the alias that OPEN gives, or else the table name. */
    public String getHandlerName() {
        if (table == null) {
            return null;
        }
        Alias alias = table.getAlias();
        return alias != null ? alias.getName() : table.getName();
    }

    /** Returns the index a READ uses, or null when it reads in storage order. */
    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public HandlerStatement withIndexName(String indexName) {
        setIndexName(indexName);
        return this;
    }

    /** Returns how a READ compares the index with {@link #getKeyValues()}, or null. */
    public KeyComparison getKeyComparison() {
        return keyComparison;
    }

    public void setKeyComparison(KeyComparison keyComparison) {
        this.keyComparison = keyComparison;
    }

    public HandlerStatement withKeyComparison(KeyComparison keyComparison) {
        setKeyComparison(keyComparison);
        return this;
    }

    /** Returns the parenthesized key values a READ compares the index with, or null. */
    public ExpressionList<?> getKeyValues() {
        return keyValues;
    }

    public void setKeyValues(ExpressionList<?> keyValues) {
        this.keyValues = keyValues;
    }

    public HandlerStatement withKeyValues(ExpressionList<?> keyValues) {
        setKeyValues(keyValues);
        return this;
    }

    /** Returns where a READ continues, or null when it compares key values. */
    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public HandlerStatement withDirection(Direction direction) {
        setDirection(direction);
        return this;
    }

    public Expression getWhere() {
        return where;
    }

    public void setWhere(Expression where) {
        this.where = where;
    }

    public HandlerStatement withWhere(Expression where) {
        setWhere(where);
        return this;
    }

    public Limit getLimit() {
        return limit;
    }

    public void setLimit(Limit limit) {
        this.limit = limit;
    }

    public HandlerStatement withLimit(Limit limit) {
        setLimit(limit);
        return this;
    }

    /** Returns the OFFSET of {@code LIMIT row_count OFFSET offset}, or null. */
    public Offset getOffset() {
        return offset;
    }

    public void setOffset(Offset offset) {
        this.offset = offset;
    }

    public HandlerStatement withOffset(Offset offset) {
        setOffset(offset);
        return this;
    }

    /** Shares the statement syntax with deparsers, which render the WHERE and key values. */
    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressionRenderer) {
        builder.append("HANDLER ").append(table.getFullyQualifiedName()).append(' ')
                .append(action);
        if (action == Action.OPEN) {
            if (table.getAlias() != null) {
                builder.append(table.getAlias());
            }
        } else if (action == Action.READ) {
            appendReadTo(builder, expressionRenderer);
        }
        return builder;
    }

    private void appendReadTo(StringBuilder builder, Consumer<Expression> expressionRenderer) {
        if (indexName != null) {
            builder.append(' ').append(indexName);
        }
        if (keyComparison != null) {
            builder.append(' ').append(keyComparison.getOperator()).append(' ');
            expressionRenderer.accept(keyValues);
        } else {
            builder.append(' ').append(direction);
        }
        if (where != null) {
            builder.append(" WHERE ");
            expressionRenderer.accept(where);
        }
        if (limit != null) {
            builder.append(limit);
        }
        if (offset != null) {
            builder.append(offset);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, builder::append).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }
}

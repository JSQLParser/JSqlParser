/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.statistics;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/** PostgreSQL extended statistics over columns and parenthesized expressions. */
public class CreateStatistics implements Statement {
    public enum Kind {
        NDISTINCT, DEPENDENCIES, MCV
    }

    private String name;
    private boolean ifNotExists = false;
    private List<Kind> kinds = new ArrayList<>();
    private ExpressionList<Expression> expressions = new ExpressionList<>();
    private Table table;

    public String getName() {
        return name;
    }

    public CreateStatistics setName(String name) {
        this.name = name;
        return this;
    }

    public boolean isIfNotExists() {
        return ifNotExists;
    }

    public CreateStatistics setIfNotExists(boolean ifNotExists) {
        this.ifNotExists = ifNotExists;
        return this;
    }

    public List<Kind> getKinds() {
        return kinds;
    }

    public CreateStatistics setKinds(List<Kind> kinds) {
        this.kinds = kinds;
        return this;
    }

    public ExpressionList<Expression> getExpressions() {
        return expressions;
    }

    public CreateStatistics setExpressions(ExpressionList<Expression> expressions) {
        this.expressions = expressions;
        return this;
    }

    public Table getTable() {
        return table;
    }

    public CreateStatistics setTable(Table table) {
        this.table = table;
        return this;
    }

    public StringBuilder appendTo(StringBuilder sql, Consumer<Expression> printer) {
        sql.append("CREATE STATISTICS");
        if (ifNotExists) {
            sql.append(" IF NOT EXISTS");
        }
        if (name != null) {
            sql.append(' ').append(name);
        }
        if (kinds != null && !kinds.isEmpty()) {
            sql.append(" (");
            for (int i = 0; i < kinds.size(); i++) {
                if (i > 0) {
                    sql.append(", ");
                }
                sql.append(kinds.get(i).name().toLowerCase(java.util.Locale.ROOT));
            }
            sql.append(')');
        }
        sql.append(" ON ");
        printer.accept(expressions);
        return sql.append(" FROM ").append(table);
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();
        return appendTo(sql, sql::append).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }
}

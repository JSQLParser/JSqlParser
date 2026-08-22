/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 * DuckDB's {@code MAP {key: value, ...}} literal expression.
 *
 * @see <a href="https://duckdb.org/docs/sql/data_types/map.html">DuckDB MAP type</a>
 */
public class MapExpression extends ASTNodeAccessImpl implements Expression {

    private List<Map.Entry<Expression, Expression>> entries = new ArrayList<>();

    public MapExpression() {}

    public MapExpression(Collection<? extends Map.Entry<Expression, Expression>> entries) {
        this.entries.addAll(entries);
    }

    public List<Map.Entry<Expression, Expression>> getEntries() {
        return entries;
    }

    public void setEntries(List<Map.Entry<Expression, Expression>> entries) {
        this.entries = entries;
    }

    public MapExpression withEntries(List<Map.Entry<Expression, Expression>> entries) {
        setEntries(entries);
        return this;
    }

    public MapExpression addEntries(
            Collection<? extends Map.Entry<Expression, Expression>> entries) {
        this.entries.addAll(entries);
        return this;
    }

    public MapExpression addEntry(Expression key, Expression value) {
        entries.add(new AbstractMap.SimpleEntry<>(key, value));
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("MAP {");
        for (int i = 0; i < entries.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            Map.Entry<Expression, Expression> entry = entries.get(i);
            builder.append(entry.getKey()).append(": ").append(entry.getValue());
        }
        return builder.append("}");
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

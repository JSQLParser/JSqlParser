/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.collation;

import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.table.Index;

/** PostgreSQL collation copy or parameter definition, with mutually exclusive forms. */
public class CreateCollation implements Statement {
    private String name;
    private boolean ifNotExists;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        name = value;
    }

    public boolean isIfNotExists() {
        return ifNotExists;
    }

    public void setIfNotExists(boolean value) {
        ifNotExists = value;
    }

    private String sourceCollation;
    private List<Index.Option> options;

    public String getSourceCollation() {
        return sourceCollation;
    }

    public void setSourceCollation(String source) {
        sourceCollation = source;
        if (source != null) {
            options = null;
        }
    }

    public List<Index.Option> getOptions() {
        return options;
    }

    public void setOptions(List<Index.Option> options) {
        this.options = options;
        if (options != null) {
            sourceCollation = null;
        }
    }

    public StringBuilder appendTo(StringBuilder sql, Consumer<Expression> printer) {
        sql.append("CREATE COLLATION ");
        if (isIfNotExists()) {
            sql.append("IF NOT EXISTS ");
        }
        sql.append(getName());
        if (sourceCollation != null) {
            sql.append(" FROM ").append(sourceCollation);
        } else {
            sql.append(' ');
            Index.Option.appendListTo(sql, options, printer);
        }
        return sql;
    }

    public void visitExpressions(Consumer<Expression> visitor) {
        if (sourceCollation == null && options != null) {
            options.stream().map(Index.Option::getValue).filter(java.util.Objects::nonNull)
                    .forEach(visitor);
        }
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

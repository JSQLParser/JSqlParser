/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.trigger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * A PostgreSQL {@code CREATE EVENT TRIGGER} statement. Unlike {@link CreateTrigger}, it fires on a
 * database event such as {@code ddl_command_start} rather than on table rows. The function is
 * invoked like a {@link CreateTrigger} routine, with the same {@code FUNCTION} or {@code PROCEDURE}
 * spelling, but never takes arguments.
 */
public class CreateEventTrigger extends ASTNodeAccessImpl implements Statement {

    private String name;
    private String event;
    private final List<Filter> filters = new ArrayList<>();
    private CreateTrigger.ExecuteKeyword executeKeyword = CreateTrigger.ExecuteKeyword.FUNCTION;
    private Function function;

    /** A {@code WHEN} condition, for example {@code TAG IN ('CREATE TABLE', 'DROP TABLE')}. */
    public static class Filter implements Serializable {
        private String variable;
        private final List<StringValue> values = new ArrayList<>();

        public Filter() {
            // empty constructor
        }

        public Filter(String variable, Collection<StringValue> values) {
            this.variable = variable;
            setValues(values);
        }

        /** The filter variable as written, for example {@code TAG}. */
        public String getVariable() {
            return variable;
        }

        public void setVariable(String variable) {
            this.variable = variable;
        }

        /** The accepted values in source order. */
        public List<StringValue> getValues() {
            return Collections.unmodifiableList(values);
        }

        public void setValues(Collection<StringValue> values) {
            this.values.clear();
            if (values != null) {
                this.values.addAll(values);
            }
        }

        public Filter addValues(StringValue... values) {
            Collections.addAll(this.values, values);
            return this;
        }

        public StringBuilder appendTo(StringBuilder sql, Consumer<Expression> expressionWriter) {
            sql.append(variable).append(" IN (");
            for (int i = 0; i < values.size(); i++) {
                if (i > 0) {
                    sql.append(", ");
                }
                expressionWriter.accept(values.get(i));
            }
            return sql.append(')');
        }

        @Override
        public String toString() {
            StringBuilder sql = new StringBuilder();
            return appendTo(sql, sql::append).toString();
        }
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /** The event name as written, for example {@code ddl_command_start} or {@code sql_drop}. */
    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    /** The {@code WHEN} conditions in source order; all of them must match. */
    public List<Filter> getFilters() {
        return Collections.unmodifiableList(filters);
    }

    public void setFilters(Collection<Filter> filters) {
        this.filters.clear();
        if (filters != null) {
            this.filters.addAll(filters);
        }
    }

    public CreateEventTrigger addFilters(Filter... filters) {
        Collections.addAll(this.filters, filters);
        return this;
    }

    public CreateTrigger.ExecuteKeyword getExecuteKeyword() {
        return executeKeyword;
    }

    public void setExecuteKeyword(CreateTrigger.ExecuteKeyword executeKeyword) {
        this.executeKeyword = executeKeyword;
    }

    /** The invoked function, rendered with its empty argument list. */
    public Function getFunction() {
        return function;
    }

    public void setFunction(Function function) {
        this.function = function;
    }

    /** Visits the filter values and the invoked function. */
    public void visitExpressions(Consumer<Expression> visitor) {
        filters.forEach(filter -> filter.getValues().forEach(visitor));
        if (function != null) {
            visitor.accept(function);
        }
    }

    public StringBuilder appendTo(StringBuilder sql, Consumer<Expression> expressionWriter) {
        sql.append("CREATE EVENT TRIGGER ").append(name).append(" ON ").append(event);
        for (int i = 0; i < filters.size(); i++) {
            sql.append(i == 0 ? " WHEN " : " AND ");
            filters.get(i).appendTo(sql, expressionWriter);
        }
        sql.append(" EXECUTE ").append(executeKeyword).append(' ');
        expressionWriter.accept(function);
        return sql;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();
        return appendTo(sql, sql::append).toString();
    }

    public CreateEventTrigger withName(String name) {
        setName(name);
        return this;
    }

    public CreateEventTrigger withEvent(String event) {
        setEvent(event);
        return this;
    }

    public CreateEventTrigger withExecuteKeyword(CreateTrigger.ExecuteKeyword executeKeyword) {
        setExecuteKeyword(executeKeyword);
        return this;
    }

    public CreateEventTrigger withFunction(Function function) {
        setFunction(function);
        return this;
    }
}

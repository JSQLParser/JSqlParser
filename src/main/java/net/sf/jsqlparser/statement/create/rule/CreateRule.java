/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/** PostgreSQL rewrite rule whose actions reuse ordinary statement AST nodes. */
public class CreateRule implements Statement {
    public enum Event {
        SELECT, INSERT, UPDATE, DELETE
    }
    public enum Behavior {
        ALSO, INSTEAD
    }

    private String name;
    private boolean orReplace;
    private Event event;
    private Table table;
    private Expression whereExpression;
    private Behavior behavior;
    private boolean useParentheses;
    private List<Statement> actions = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String value) {
        name = value;
    }

    public boolean isOrReplace() {
        return orReplace;
    }

    public void setOrReplace(boolean value) {
        orReplace = value;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event value) {
        event = value;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table value) {
        table = value;
    }

    public Expression getWhereExpression() {
        return whereExpression;
    }

    public void setWhereExpression(Expression value) {
        whereExpression = value;
    }

    public Behavior getBehavior() {
        return behavior;
    }

    public void setBehavior(Behavior value) {
        behavior = value;
    }

    public boolean isUseParentheses() {
        return useParentheses;
    }

    public void setUseParentheses(boolean value) {
        useParentheses = value;
    }

    public List<Statement> getActions() {
        return actions;
    }

    public void setActions(List<Statement> value) {
        actions = value;
    }

    public boolean isNothing() {
        return actions.isEmpty() && !useParentheses;
    }

    public void setNothing(boolean nothing) {
        if (nothing) {
            actions.clear();
            useParentheses = false;
        }
    }

    public void visitExpressions(Consumer<Expression> visitor) {
        if (whereExpression != null) {
            visitor.accept(whereExpression);
        }
    }

    public void visitTables(Consumer<Table> visitor) {
        if (table != null) {
            visitor.accept(table);
        }
    }

    public StringBuilder appendTo(StringBuilder sql, Consumer<Expression> expressions,
            Consumer<Statement> statements) {
        sql.append("CREATE ");
        if (orReplace) {
            sql.append("OR REPLACE ");
        }
        sql.append("RULE ").append(name).append(" AS ON ").append(event).append(" TO ")
                .append(table);
        if (whereExpression != null) {
            sql.append(" WHERE ");
            expressions.accept(whereExpression);
        }
        sql.append(" DO ");
        if (behavior != null) {
            sql.append(behavior).append(' ');
        }
        if (isNothing()) {
            return sql.append("NOTHING");
        }
        boolean grouped = useParentheses || actions.size() != 1;
        if (grouped) {
            sql.append('(');
        }
        for (int i = 0; i < actions.size(); i++) {
            if (i > 0) {
                sql.append("; ");
            }
            statements.accept(actions.get(i));
        }
        if (grouped) {
            sql.append(')');
        }
        return sql;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();
        return appendTo(sql, sql::append, sql::append).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }
}

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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.statement.create.table.ConstraintAttributes;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/** MySQL and PostgreSQL trigger definitions with distinct body and routine-invocation forms. */
public class CreateTrigger implements Statement {

    public enum Timing {
        BEFORE, AFTER, INSTEAD_OF
    }

    public enum Event {
        INSERT, UPDATE, DELETE, TRUNCATE
    }

    public enum Order {
        FOLLOWS, PRECEDES
    }

    public enum Orientation {
        ROW, STATEMENT
    }
    public enum ExecuteKeyword {
        FUNCTION, PROCEDURE
    }

    private List<TriggerEvent> events = new ArrayList<>();
    private TriggerDefiner definer;
    private Table trigger;
    private Timing timing;
    private boolean orReplace;
    private boolean constraint;
    private Table referencedTable;
    private Orientation orientation;
    private boolean useEach;
    private Expression whenExpression;
    private ExecuteKeyword executeKeyword = ExecuteKeyword.FUNCTION;
    private Function routine;
    private Table table;
    private Order order;
    private Table otherTrigger;
    private Statement body;
    private ConstraintAttributes constraintAttributes = new ConstraintAttributes();
    private List<TransitionRelation> transitionRelations = new ArrayList<>();

    public List<TriggerEvent> getEvents() {
        return events;
    }

    public void setEvents(List<TriggerEvent> events) {
        this.events = events;
    }

    public boolean isOrReplace() {
        return orReplace;
    }

    public void setOrReplace(boolean orReplace) {
        this.orReplace = orReplace;
    }

    public boolean isConstraint() {
        return constraint;
    }

    public void setConstraint(boolean constraint) {
        this.constraint = constraint;
    }

    public Table getReferencedTable() {
        return referencedTable;
    }

    public void setReferencedTable(Table referencedTable) {
        this.referencedTable = referencedTable;
    }

    public ConstraintAttributes getConstraintAttributes() {
        return constraintAttributes;
    }

    public void setConstraintAttributes(ConstraintAttributes constraintAttributes) {
        this.constraintAttributes = constraintAttributes;
    }

    public List<TransitionRelation> getTransitionRelations() {
        return transitionRelations;
    }

    public void setTransitionRelations(List<TransitionRelation> transitionRelations) {
        this.transitionRelations = transitionRelations;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public boolean isUseEach() {
        return useEach;
    }

    public void setUseEach(boolean useEach) {
        this.useEach = useEach;
    }

    public Expression getWhenExpression() {
        return whenExpression;
    }

    public void setWhenExpression(Expression whenExpression) {
        this.whenExpression = whenExpression;
    }

    public ExecuteKeyword getExecuteKeyword() {
        return executeKeyword;
    }

    public void setExecuteKeyword(ExecuteKeyword executeKeyword) {
        this.executeKeyword = executeKeyword;
    }

    public Function getRoutine() {
        return routine;
    }

    public void setRoutine(Function routine) {
        this.routine = routine;
    }

    public TriggerDefiner getDefiner() {
        return definer;
    }

    public void setDefiner(TriggerDefiner definer) {
        this.definer = definer;
    }

    public Table getTrigger() {
        return trigger;
    }

    public void setTrigger(Table trigger) {
        this.trigger = trigger;
    }

    public Timing getTiming() {
        return timing;
    }

    public void setTiming(Timing timing) {
        this.timing = timing;
    }

    public Event getEvent() {
        return events.isEmpty() ? null : events.get(0).getEvent();
    }

    public void setEvent(Event event) {
        events = new ArrayList<>();
        if (event != null) {
            events.add(new TriggerEvent(event));
        }
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Table getOtherTrigger() {
        return otherTrigger;
    }

    public void setOtherTrigger(Table otherTrigger) {
        this.otherTrigger = otherTrigger;
    }

    public Statement getBody() {
        return body;
    }

    public void setBody(Statement body) {
        this.body = body;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    public void visit(Consumer<Table> tables, Consumer<Expression> expressions) {
        if (table != null) {
            tables.accept(table);
        }
        if (referencedTable != null) {
            tables.accept(referencedTable);
        }
        for (TriggerEvent triggerEvent : events) {
            if (triggerEvent.getColumns() != null) {
                triggerEvent.getColumns().forEach(expressions);
            }
        }
        if (whenExpression != null) {
            expressions.accept(whenExpression);
        }
        if (routine != null) {
            expressions.accept(routine);
        }
    }

    public void appendTo(StringBuilder sql, Consumer<Expression> visitor) {
        // MySQL's shorter form has neither a routine nor any of the standard clauses
        if (routine == null && transitionRelations.isEmpty() && !constraint
                && orientation != Orientation.STATEMENT) {
            sql.append(mysqlSql());
            return;
        }
        appendPostgreSqlHeader(sql, visitor);
        appendPostgreSqlTarget(sql);
        if (whenExpression != null) {
            sql.append(" WHEN (");
            visitor.accept(whenExpression);
            sql.append(')');
        }
        if (routine != null) {
            sql.append(" EXECUTE ").append(executeKeyword).append(' ');
            visitor.accept(routine);
        } else {
            // DuckDB 2.0 runs a statement rather than a function
            sql.append(' ').append(body);
        }
    }

    private void appendPostgreSqlHeader(StringBuilder sql, Consumer<Expression> visitor) {
        sql.append("CREATE ");
        if (orReplace) {
            sql.append("OR REPLACE ");
        }
        if (constraint) {
            sql.append("CONSTRAINT ");
        }
        sql.append("TRIGGER ").append(trigger).append(' ').append(timing.name().replace('_', ' '))
                .append(' ');
        for (int i = 0; i < events.size(); i++) {
            if (i > 0) {
                sql.append(" OR ");
            }
            events.get(i).appendTo(sql, visitor);
        }
    }

    private void appendPostgreSqlTarget(StringBuilder sql) {
        sql.append(" ON ").append(table);
        if (referencedTable != null) {
            sql.append(" FROM ").append(referencedTable);
        }
        constraintAttributes.appendTo(sql);
        if (!transitionRelations.isEmpty()) {
            sql.append(" REFERENCING");
            for (TransitionRelation relation : transitionRelations) {
                sql.append(' ').append(relation);
            }
        }
        if (orientation != null) {
            sql.append(" FOR ").append(useEach ? "EACH " : "").append(orientation);
        }
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();
        appendTo(sql, sql::append);
        return sql.toString();
    }

    private String mysqlSql() {
        StringBuilder builder = new StringBuilder("CREATE ");
        if (definer != null) {
            builder.append("DEFINER = ").append(definer).append(" ");
        }
        builder.append("TRIGGER ").append(trigger).append(" ").append(timing).append(" ")
                .append(getEvent()).append(" ON ").append(table).append(" FOR EACH ROW ");
        if (order != null) {
            builder.append(order).append(" ").append(otherTrigger).append(" ");
        }
        return builder.append(body).toString();
    }
}

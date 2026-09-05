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

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/** MySQL {@code CREATE TRIGGER} statement with a structured trigger definition. */
public class CreateTrigger implements Statement {

    public enum Timing {
        BEFORE, AFTER
    }

    public enum Event {
        INSERT, UPDATE, DELETE
    }

    public enum Order {
        FOLLOWS, PRECEDES
    }

    private TriggerDefiner definer;
    private Table trigger;
    private Timing timing;
    private Event event;
    private Table table;
    private Order order;
    private Table otherTrigger;
    private Statement body;

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
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("CREATE ");
        if (definer != null) {
            builder.append("DEFINER = ").append(definer).append(" ");
        }
        builder.append("TRIGGER ").append(trigger).append(" ").append(timing).append(" ")
                .append(event).append(" ON ").append(table).append(" FOR EACH ROW ");
        if (order != null) {
            builder.append(order).append(" ").append(otherTrigger).append(" ");
        }
        return builder.append(body).toString();
    }
}

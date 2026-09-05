/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.event;

import java.io.Serializable;
import net.sf.jsqlparser.expression.Expression;

/** Structured {@code ON SCHEDULE} clause of a MySQL event. */
public class EventSchedule implements Serializable {

    public enum Type {
        AT, EVERY
    }

    private Type type;
    private Expression executeAt;
    private Expression interval;
    private String intervalUnit;
    private Expression starts;
    private Expression ends;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Expression getExecuteAt() {
        return executeAt;
    }

    public void setExecuteAt(Expression executeAt) {
        this.executeAt = executeAt;
    }

    public Expression getInterval() {
        return interval;
    }

    public void setInterval(Expression interval) {
        this.interval = interval;
    }

    public String getIntervalUnit() {
        return intervalUnit;
    }

    public void setIntervalUnit(String intervalUnit) {
        this.intervalUnit = intervalUnit;
    }

    public Expression getStarts() {
        return starts;
    }

    public void setStarts(Expression starts) {
        this.starts = starts;
    }

    public Expression getEnds() {
        return ends;
    }

    public void setEnds(Expression ends) {
        this.ends = ends;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (type == Type.AT) {
            builder.append("AT ").append(executeAt);
        } else if (type == Type.EVERY) {
            builder.append("EVERY ").append(interval).append(" ").append(intervalUnit);
            if (starts != null) {
                builder.append(" STARTS ").append(starts);
            }
            if (ends != null) {
                builder.append(" ENDS ").append(ends);
            }
        }
        return builder.toString();
    }

    public EventSchedule withType(Type type) {
        setType(type);
        return this;
    }

    public EventSchedule withExecuteAt(Expression executeAt) {
        setExecuteAt(executeAt);
        return this;
    }

    public EventSchedule withInterval(Expression interval) {
        setInterval(interval);
        return this;
    }

    public EventSchedule withIntervalUnit(String intervalUnit) {
        setIntervalUnit(intervalUnit);
        return this;
    }

    public EventSchedule withStarts(Expression starts) {
        setStarts(starts);
        return this;
    }

    public EventSchedule withEnds(Expression ends) {
        setEnds(ends);
        return this;
    }
}

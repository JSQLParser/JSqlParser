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

import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;

/** Common structured properties of MySQL {@code CREATE EVENT} and {@code ALTER EVENT}. */
public abstract class EventStatement implements Statement {

    private Table event;
    private EventSchedule schedule;
    private Boolean onCompletionPreserve;
    private EventStatus status;
    private StringValue comment;
    private Statement body;

    public Table getEvent() {
        return event;
    }

    public void setEvent(Table event) {
        this.event = event;
    }

    public EventSchedule getSchedule() {
        return schedule;
    }

    public void setSchedule(EventSchedule schedule) {
        this.schedule = schedule;
    }

    /**
     * Returns {@code true} for {@code ON COMPLETION PRESERVE}, {@code false} for
     * {@code ON COMPLETION NOT PRESERVE}, or {@code null} when omitted.
     */
    public Boolean getOnCompletionPreserve() {
        return onCompletionPreserve;
    }

    public void setOnCompletionPreserve(Boolean onCompletionPreserve) {
        this.onCompletionPreserve = onCompletionPreserve;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public StringValue getComment() {
        return comment;
    }

    public void setComment(StringValue comment) {
        this.comment = comment;
    }

    public Statement getBody() {
        return body;
    }

    public void setBody(Statement body) {
        this.body = body;
    }

    protected void appendScheduleAndCompletion(StringBuilder builder) {
        if (schedule != null) {
            builder.append(" ON SCHEDULE ").append(schedule);
        }
        if (onCompletionPreserve != null) {
            builder.append(" ON COMPLETION ");
            if (!onCompletionPreserve) {
                builder.append("NOT ");
            }
            builder.append("PRESERVE");
        }
    }

    protected void appendStatusAndComment(StringBuilder builder) {
        if (status != null) {
            builder.append(" ").append(status);
        }
        if (comment != null) {
            builder.append(" COMMENT ").append(comment);
        }
    }
}

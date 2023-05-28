/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

public class KSQLJoinWindow extends ASTNodeAccessImpl {

    public enum TimeUnit {

        DAY("DAY"),
        HOUR("HOUR"),
        MINUTE("MINUTE"),
        SECOND("SECOND"),
        MILLISECOND("MILLISECOND"),
        DAYS("DAYS"),
        HOURS("HOURS"),
        MINUTES("MINUTES"),
        SECONDS("SECONDS"),
        MILLISECONDS("MILLISECONDS");

        private String timeUnit;

        TimeUnit(String timeUnit) {
            this.timeUnit = timeUnit;
        }

        public String getTimeUnit() {
            return timeUnit;
        }
    }

    private boolean beforeAfter;

    private long duration;

    private TimeUnit timeUnit;

    private long beforeDuration;

    private TimeUnit beforeTimeUnit;

    private long afterDuration;

    private TimeUnit afterTimeUnit;

    public KSQLJoinWindow() {
    }

    public boolean isBeforeAfterWindow() {
        return beforeAfter;
    }

    public void setBeforeAfterWindow(boolean beforeAfter) {
        this.beforeAfter = beforeAfter;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public long getBeforeDuration() {
        return beforeDuration;
    }

    public void setBeforeDuration(long beforeDuration) {
        this.beforeDuration = beforeDuration;
    }

    public TimeUnit getBeforeTimeUnit() {
        return beforeTimeUnit;
    }

    public void setBeforeTimeUnit(TimeUnit beforeTimeUnit) {
        this.beforeTimeUnit = beforeTimeUnit;
    }

    public long getAfterDuration() {
        return afterDuration;
    }

    public void setAfterDuration(long afterDuration) {
        this.afterDuration = afterDuration;
    }

    public TimeUnit getAfterTimeUnit() {
        return afterTimeUnit;
    }

    public void setAfterTimeUnit(TimeUnit afterTimeUnit) {
        this.afterTimeUnit = afterTimeUnit;
    }

    @Override
    public String toString() {
        if (isBeforeAfterWindow()) {
            return "(" + beforeDuration + " " + beforeTimeUnit + ", " + afterDuration + " " + afterTimeUnit + ")";
        }
        return "(" + duration + " " + timeUnit + ")";
    }

    public KSQLJoinWindow withDuration(long duration) {
        this.setDuration(duration);
        return this;
    }

    public KSQLJoinWindow withTimeUnit(TimeUnit timeUnit) {
        this.setTimeUnit(timeUnit);
        return this;
    }

    public KSQLJoinWindow withBeforeDuration(long beforeDuration) {
        this.setBeforeDuration(beforeDuration);
        return this;
    }

    public KSQLJoinWindow withBeforeTimeUnit(TimeUnit beforeTimeUnit) {
        this.setBeforeTimeUnit(beforeTimeUnit);
        return this;
    }

    public KSQLJoinWindow withAfterDuration(long afterDuration) {
        this.setAfterDuration(afterDuration);
        return this;
    }

    public KSQLJoinWindow withAfterTimeUnit(TimeUnit afterTimeUnit) {
        this.setAfterTimeUnit(afterTimeUnit);
        return this;
    }
}

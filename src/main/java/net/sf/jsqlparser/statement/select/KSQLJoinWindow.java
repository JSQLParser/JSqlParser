/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2018 JSQLParser
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

public class KSQLJoinWindow extends ASTNodeAccessImpl {

    public enum TimeUnit {
        DAY ("DAY"),
        HOUR ("HOUR"),
        MINUTE ("MINUTE"),
        SECOND ("SECOND"),
        MILLISECOND ("MILLISECOND"),
        DAYS ("DAYS"),
        HOURS ("HOURS"),
        MINUTES ("MINUTES"),
        SECONDS ("SECONDS"),
        MILLISECONDS ("MILLISECONDS");

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
}

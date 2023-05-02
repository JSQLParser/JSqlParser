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

public class KSQLWindow extends ASTNodeAccessImpl {

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

        public final static TimeUnit from(String timeUnitStr) {
            return Enum.valueOf(TimeUnit.class, timeUnitStr.toUpperCase());
        }
    }

    public enum WindowType {
        HOPPING ("HOPPING"),
        SESSION ("SESSION"),
        TUMBLING ("TUMBLING");

        private String windowType;

        WindowType(String windowType) {
            this.windowType = windowType;
        }

        public String getWindowType() {
            return windowType;
        }

        public static WindowType from(String type) {
            return Enum.valueOf(WindowType.class, type.toUpperCase());
        }
    }

    private boolean hopping;
    private boolean tumbling;
    private boolean session;
    private long sizeDuration;
    private TimeUnit sizeTimeUnit;
    private long advanceDuration;
    private TimeUnit advanceTimeUnit;

    public boolean isHoppingWindow() {
        return hopping;
    }

    public void setHoppingWindow(boolean hopping) {
        this.hopping = hopping;
    }

    public boolean isTumblingWindow() {
        return tumbling;
    }

    public void setTumblingWindow(boolean tumbling) {
        this.tumbling = tumbling;
    }

    public boolean isSessionWindow() {
        return session;
    }

    public void setSessionWindow(boolean session) {
        this.session = session;
    }

    public long getSizeDuration() {
        return sizeDuration;
    }

    public void setSizeDuration(long sizeDuration) {
        this.sizeDuration = sizeDuration;
    }

    public TimeUnit getSizeTimeUnit() {
        return sizeTimeUnit;
    }

    public void setSizeTimeUnit(TimeUnit sizeTimeUnit) {
        this.sizeTimeUnit = sizeTimeUnit;
    }

    public long getAdvanceDuration() {
        return advanceDuration;
    }

    public void setAdvanceDuration(long advanceDuration) {
        this.advanceDuration = advanceDuration;
    }

    public TimeUnit getAdvanceTimeUnit() {
        return advanceTimeUnit;
    }

    public void setAdvanceTimeUnit(TimeUnit advanceTimeUnit) {
        this.advanceTimeUnit = advanceTimeUnit;
    }

    public KSQLWindow() {
    }

    @Override
    public String toString() {
        if (isHoppingWindow()) {
            return "HOPPING (" + "SIZE " + sizeDuration + " " + sizeTimeUnit + ", " +
                    "ADVANCE BY " + advanceDuration + " " + advanceTimeUnit + ")";
        } else if (isSessionWindow()) {
            return "SESSION (" + sizeDuration + " " + sizeTimeUnit + ")";
        } else {
            return "TUMBLING (" + "SIZE " + sizeDuration + " " + sizeTimeUnit + ")";
        }
    }

    public KSQLWindow withSizeDuration(long sizeDuration) {
        this.setSizeDuration(sizeDuration);
        return this;
    }

    public KSQLWindow withSizeTimeUnit(TimeUnit sizeTimeUnit) {
        this.setSizeTimeUnit(sizeTimeUnit);
        return this;
    }

    public KSQLWindow withAdvanceDuration(long advanceDuration) {
        this.setAdvanceDuration(advanceDuration);
        return this;
    }

    public KSQLWindow withAdvanceTimeUnit(TimeUnit advanceTimeUnit) {
        this.setAdvanceTimeUnit(advanceTimeUnit);
        return this;
    }

}

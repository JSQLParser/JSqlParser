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

    private boolean hopping;
    private boolean tumbling;
    private boolean session;
    private long sizeDuration;
    private TimeUnit sizeTimeUnit;
    private long advanceDuration;
    private TimeUnit advanceTimeUnit;

    public KSQLWindow() {}

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

    public enum TimeUnit {
        DAY, HOUR, MINUTE, SECOND, MILLISECOND, DAYS, HOURS, MINUTES, SECONDS, MILLISECONDS;

        public static TimeUnit from(String unit) {
            return Enum.valueOf(TimeUnit.class, unit.toUpperCase());
        }
    }

    public enum WindowType {
        HOPPING("HOPPING"), SESSION("SESSION"), TUMBLING("TUMBLING");

        private String windowType;

        WindowType(String windowType) {
            this.windowType = windowType;
        }

        public static WindowType from(String type) {
            return Enum.valueOf(WindowType.class, type.toUpperCase());
        }

        public String getWindowType() {
            return windowType;
        }
    }

}

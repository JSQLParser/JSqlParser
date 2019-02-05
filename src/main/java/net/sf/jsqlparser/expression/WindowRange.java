/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2014 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

public class WindowRange {

    private WindowOffset start;
    private WindowOffset end;

    public WindowOffset getEnd() {
        return end;
    }

    public void setEnd(WindowOffset end) {
        this.end = end;
    }

    public WindowOffset getStart() {
        return start;
    }

    public void setStart(WindowOffset start) {
        this.start = start;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(" BETWEEN");
        buffer.append(start);
        buffer.append(" AND");
        buffer.append(end);
        return buffer.toString();
    }
}

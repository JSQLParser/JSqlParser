/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import java.io.Serializable;

public class WindowRange implements Serializable {

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
        return " BETWEEN" +
                start +
                " AND" +
                end;
    }

    public WindowRange withStart(WindowOffset start) {
        this.setStart(start);
        return this;
    }

    public WindowRange withEnd(WindowOffset end) {
        this.setEnd(end);
        return this;
    }
}

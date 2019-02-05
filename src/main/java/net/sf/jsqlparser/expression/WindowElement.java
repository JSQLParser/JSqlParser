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

public class WindowElement {

    public enum Type {

        ROWS,
        RANGE
    }

    private Type type;
    private WindowOffset offset;
    private WindowRange range;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public WindowOffset getOffset() {
        return offset;
    }

    public void setOffset(WindowOffset offset) {
        this.offset = offset;
    }

    public WindowRange getRange() {
        return range;
    }

    public void setRange(WindowRange range) {
        this.range = range;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder(type.toString());

        if (offset != null) {
            buffer.append(offset.toString());
        } else if (range != null) {
            buffer.append(range.toString());
        }

        return buffer.toString();
    }
}

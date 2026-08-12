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
import java.util.Locale;

public class WindowElement implements Serializable {

    private Type type;
    private WindowOffset offset;
    private WindowRange range;
    private Exclusion exclusion;

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

    public Exclusion getExclusion() {
        return exclusion;
    }

    public void setExclusion(Exclusion exclusion) {
        this.exclusion = exclusion;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder(type.toString());

        if (offset != null) {
            buffer.append(offset.toString());
        } else if (range != null) {
            buffer.append(range.toString());
        }

        if (exclusion != null) {
            buffer.append(" EXCLUDE ").append(exclusion);
        }

        return buffer.toString();
    }

    public WindowElement withType(Type type) {
        this.setType(type);
        return this;
    }

    public WindowElement withOffset(WindowOffset offset) {
        this.setOffset(offset);
        return this;
    }

    public WindowElement withRange(WindowRange range) {
        this.setRange(range);
        return this;
    }

    public WindowElement withExclusion(Exclusion exclusion) {
        this.setExclusion(exclusion);
        return this;
    }

    public enum Type {
        ROWS, RANGE, GROUPS;

        public static Type from(String type) {
            return Enum.valueOf(Type.class, type.toUpperCase(Locale.ROOT));
        }
    }

    public enum Exclusion {
        CURRENT_ROW("CURRENT ROW"), GROUP("GROUP"), TIES("TIES"), NO_OTHERS("NO OTHERS");

        private final String keyword;

        Exclusion(String keyword) {
            this.keyword = keyword;
        }

        @Override
        public String toString() {
            return keyword;
        }
    }

}

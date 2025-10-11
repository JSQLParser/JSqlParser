/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */

package net.sf.jsqlparser.expression;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author <a href="mailto:andreas@manticore-projects.com">Andreas Reichel</a>
 */

public class JsonKeyValuePair implements Serializable {
    private final Object key;
    private final Object value;
    private boolean usingKeyKeyword;
    private JsonKeyValuePairSeparator separator;
    private boolean usingFormatJson = false;

    /**
     * Please use the Constructor with {@link JsonKeyValuePairSeparator} parameter.
     */
    @Deprecated
    public JsonKeyValuePair(Object key, Object value, boolean usingKeyKeyword,
            boolean usingValueKeyword) {
        this(key, value, usingKeyKeyword, usingValueKeyword ? JsonKeyValuePairSeparator.VALUE
                : JsonKeyValuePairSeparator.COLON);
    }

    public JsonKeyValuePair(Object key, Object value, boolean usingKeyKeyword,
            JsonKeyValuePairSeparator separator) {
        this.key = Objects.requireNonNull(key, "The KEY of the Pair must not be null");
        this.value = value;
        this.usingKeyKeyword = usingKeyKeyword;
        this.separator = separator;
    }

    public boolean isUsingKeyKeyword() {
        return usingKeyKeyword;
    }

    public void setUsingKeyKeyword(boolean usingKeyKeyword) {
        this.usingKeyKeyword = usingKeyKeyword;
    }

    public JsonKeyValuePair withUsingKeyKeyword(boolean usingKeyKeyword) {
        this.setUsingKeyKeyword(usingKeyKeyword);
        return this;
    }

    /**
     * Use {@link #getSeparator()}
     */
    @Deprecated
    public boolean isUsingValueKeyword() {
        return separator == JsonKeyValuePairSeparator.VALUE;
    }

    /**
     * Use {@link #setSeparator(JsonKeyValuePairSeparator)}
     */
    @Deprecated
    public void setUsingValueKeyword(boolean usingValueKeyword) {
        separator = usingValueKeyword ? JsonKeyValuePairSeparator.VALUE
                : JsonKeyValuePairSeparator.COLON;
    }

    /**
     * Use {@link #withSeparator(JsonKeyValuePairSeparator)}
     */
    @Deprecated
    public JsonKeyValuePair withUsingValueKeyword(boolean usingValueKeyword) {
        this.setUsingValueKeyword(usingValueKeyword);
        return this;
    }

    public JsonKeyValuePairSeparator getSeparator() {
        return separator;
    }

    public void setSeparator(JsonKeyValuePairSeparator separator) {
        this.separator = separator;
    }

    public JsonKeyValuePair withSeparator(JsonKeyValuePairSeparator separator) {
        this.setSeparator(separator);
        return this;
    }

    public boolean isUsingFormatJson() {
        return usingFormatJson;
    }

    public void setUsingFormatJson(boolean usingFormatJson) {
        this.usingFormatJson = usingFormatJson;
    }

    public JsonKeyValuePair withUsingFormatJson(boolean usingFormatJson) {
        this.setUsingFormatJson(usingFormatJson);
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.key);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final JsonKeyValuePair other = (JsonKeyValuePair) obj;
        return Objects.equals(this.key, other.key);
    }

    public Object getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public StringBuilder append(StringBuilder builder) {
        if (isUsingValueKeyword()) {
            if (isUsingKeyKeyword()) {
                builder.append("KEY ");
            }
            builder.append(getKey()).append(" VALUE ").append(getValue());
        } else {
            builder.append(getKey()).append(":").append(getValue());
        }

        if (isUsingFormatJson()) {
            builder.append(" FORMAT JSON");
        }

        return builder;
    }

    @Override
    public String toString() {
        return append(new StringBuilder()).toString();
    }

}

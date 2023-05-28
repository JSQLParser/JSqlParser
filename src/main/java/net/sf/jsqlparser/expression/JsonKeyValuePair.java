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

    private final String key;

    private boolean usingKeyKeyword = false;

    private final Object value;

    private boolean usingValueKeyword = false;

    private boolean usingFormatJson = false;

    public JsonKeyValuePair(String key, Object value, boolean usingKeyKeyword, boolean usingValueKeyword) {
        this.key = Objects.requireNonNull(key, "The KEY of the Pair must not be null");
        this.value = value;
        this.usingKeyKeyword = usingKeyKeyword;
        this.usingValueKeyword = usingValueKeyword;
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

    public boolean isUsingValueKeyword() {
        return usingValueKeyword;
    }

    public void setUsingValueKeyword(boolean usingValueKeyword) {
        this.usingValueKeyword = usingValueKeyword;
    }

    public JsonKeyValuePair withUsingValueKeyword(boolean usingValueKeyword) {
        this.setUsingValueKeyword(usingValueKeyword);
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

    public String getKey() {
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

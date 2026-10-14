/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.user;

import java.io.Serializable;

/** One limit of the MySQL account {@code WITH} clause, such as {@code MAX_USER_CONNECTIONS 10}. */
public class ResourceLimit implements Serializable {

    public enum Kind {
        MAX_QUERIES_PER_HOUR, MAX_UPDATES_PER_HOUR, MAX_CONNECTIONS_PER_HOUR, MAX_USER_CONNECTIONS
    }

    private Kind kind;
    private long value;

    public ResourceLimit() {
        // empty constructor
    }

    public ResourceLimit(Kind kind, long value) {
        this.kind = kind;
        this.value = value;
    }

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return kind + " " + value;
    }

    public ResourceLimit withKind(Kind kind) {
        setKind(kind);
        return this;
    }

    public ResourceLimit withValue(long value) {
        setValue(value);
        return this;
    }
}

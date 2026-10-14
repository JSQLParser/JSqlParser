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
import net.sf.jsqlparser.expression.StringValue;

/**
 * One requirement of the MySQL account {@code REQUIRE} clause. {@code REQUIRE NONE} is a single
 * {@link Kind#NONE} option; {@code CIPHER}, {@code ISSUER} and {@code SUBJECT} carry a string.
 */
public class TlsOption implements Serializable {

    public enum Kind {
        NONE, SSL, X509, CIPHER, ISSUER, SUBJECT;

        /** Whether the option is followed by a string value. */
        public boolean hasValue() {
            return this == CIPHER || this == ISSUER || this == SUBJECT;
        }
    }

    private Kind kind;
    private StringValue value;
    private boolean andKeyword;

    public TlsOption() {
        // empty constructor
    }

    public TlsOption(Kind kind) {
        this.kind = kind;
    }

    public TlsOption(Kind kind, StringValue value) {
        this.kind = kind;
        this.value = value;
    }

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }

    public StringValue getValue() {
        return value;
    }

    public void setValue(StringValue value) {
        this.value = value;
    }

    /** Whether {@code AND} is written before this option. */
    public boolean isAndKeyword() {
        return andKeyword;
    }

    public void setAndKeyword(boolean andKeyword) {
        this.andKeyword = andKeyword;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (andKeyword) {
            builder.append("AND ");
        }
        builder.append(kind);
        if (value != null) {
            builder.append(' ').append(value);
        }
        return builder.toString();
    }

    public TlsOption withKind(Kind kind) {
        setKind(kind);
        return this;
    }

    public TlsOption withValue(StringValue value) {
        setValue(value);
        return this;
    }

    public TlsOption withAndKeyword(boolean andKeyword) {
        setAndKeyword(andKeyword);
        return this;
    }
}

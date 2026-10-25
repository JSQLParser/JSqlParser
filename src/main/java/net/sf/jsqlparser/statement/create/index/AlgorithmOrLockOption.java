/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.index;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * MySQL's {@code ALGORITHM [=] value} or {@code LOCK [=] value} clause of CREATE INDEX and DROP
 * INDEX. The value keeps its SQL spelling, such as INPLACE, COPY, NONE or SHARED.
 */
public class AlgorithmOrLockOption implements Serializable {

    public enum Kind {
        ALGORITHM, LOCK
    }

    private Kind kind;
    private String value;
    private boolean useEquals;

    public AlgorithmOrLockOption(Kind kind, String value, boolean useEquals) {
        this.kind = Objects.requireNonNull(kind, "kind");
        this.value = Objects.requireNonNull(value, "value");
        this.useEquals = useEquals;
    }

    public static AlgorithmOrLockOption algorithm(String value) {
        return new AlgorithmOrLockOption(Kind.ALGORITHM, value, true);
    }

    public static AlgorithmOrLockOption lock(String value) {
        return new AlgorithmOrLockOption(Kind.LOCK, value, true);
    }

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = Objects.requireNonNull(kind, "kind");
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = Objects.requireNonNull(value, "value");
    }

    public boolean isUseEquals() {
        return useEquals;
    }

    public void setUseEquals(boolean useEquals) {
        this.useEquals = useEquals;
    }

    /** Returns an unmodifiable token snapshot, such as {@code [ALGORITHM, =, INPLACE]}. */
    public List<String> getTokens() {
        List<String> tokens = new ArrayList<>();
        tokens.add(kind.name());
        if (useEquals) {
            tokens.add("=");
        }
        tokens.add(value);
        return Collections.unmodifiableList(tokens);
    }

    @Override
    public String toString() {
        return String.join(" ", getTokens());
    }
}

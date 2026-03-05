/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import java.util.Objects;

public class ReturningOutputAlias {
    private ReturningReferenceType referenceType;
    private String alias;

    public ReturningOutputAlias(ReturningReferenceType referenceType, String alias) {
        this.referenceType = referenceType;
        this.alias = alias;
    }

    public ReturningReferenceType getReferenceType() {
        return referenceType;
    }

    public ReturningOutputAlias setReferenceType(ReturningReferenceType referenceType) {
        this.referenceType = referenceType;
        return this;
    }

    public String getAlias() {
        return alias;
    }

    public ReturningOutputAlias setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        return builder.append(referenceType).append(" AS ").append(alias);
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReturningOutputAlias)) {
            return false;
        }
        ReturningOutputAlias that = (ReturningOutputAlias) o;
        return referenceType == that.referenceType && Objects.equals(alias, that.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(referenceType, alias);
    }
}

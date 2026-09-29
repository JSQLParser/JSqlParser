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

/** RELEASE [SAVEPOINT] name. */
public class ReleaseSavepointStatement implements Statement {
    private String savepointName;
    private boolean usingSavepointKeyword = true;

    public ReleaseSavepointStatement(String savepointName) {
        setSavepointName(savepointName);
    }

    public String getSavepointName() {
        return savepointName;
    }

    public void setSavepointName(String savepointName) {
        this.savepointName = Objects.requireNonNull(savepointName, "savepointName");
    }

    public ReleaseSavepointStatement withSavepointName(String savepointName) {
        setSavepointName(savepointName);
        return this;
    }

    public boolean isUsingSavepointKeyword() {
        return usingSavepointKeyword;
    }

    public void setUsingSavepointKeyword(boolean usingSavepointKeyword) {
        this.usingSavepointKeyword = usingSavepointKeyword;
    }

    public ReleaseSavepointStatement withUsingSavepointKeyword(boolean usingSavepointKeyword) {
        setUsingSavepointKeyword(usingSavepointKeyword);
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        return builder.append("RELEASE ").append(usingSavepointKeyword ? "SAVEPOINT " : "")
                .append(savepointName);
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }
}

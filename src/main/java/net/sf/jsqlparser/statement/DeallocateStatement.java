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

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 * {@code DEALLOCATE [PREPARE] name}, or MySQL's {@code DROP PREPARE name}, which drops a statement
 * prepared by {@link PrepareStatement}.
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/deallocate-prepare.html">MySQL DEALLOCATE
 *      PREPARE</a>
 */
public class DeallocateStatement extends ASTNodeAccessImpl implements Statement {
    private Keyword keyword = Keyword.DEALLOCATE;
    private String name;
    private boolean usingPrepareKeyword;

    public DeallocateStatement() {}

    public DeallocateStatement(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DeallocateStatement withName(String name) {
        setName(name);
        return this;
    }

    /** The leading keyword, {@code DEALLOCATE} unless MySQL's {@code DROP} was written. */
    public Keyword getKeyword() {
        return keyword;
    }

    public void setKeyword(Keyword keyword) {
        this.keyword = keyword == null ? Keyword.DEALLOCATE : keyword;
    }

    public DeallocateStatement withKeyword(Keyword keyword) {
        setKeyword(keyword);
        return this;
    }

    /** Whether {@code PREPARE} follows the keyword; {@code DROP} always takes it. */
    public boolean isUsingPrepareKeyword() {
        return usingPrepareKeyword || keyword == Keyword.DROP;
    }

    public void setUsingPrepareKeyword(boolean usingPrepareKeyword) {
        this.usingPrepareKeyword = usingPrepareKeyword;
    }

    public DeallocateStatement withUsingPrepareKeyword(boolean usingPrepareKeyword) {
        setUsingPrepareKeyword(usingPrepareKeyword);
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append(keyword).append(' ');
        if (isUsingPrepareKeyword()) {
            builder.append("PREPARE ");
        }
        builder.append(name);
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    /** The keyword that starts the statement. */
    public enum Keyword {
        DEALLOCATE, DROP
    }
}

/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

public class Commit implements Statement {
    private TransactionKeyword keyword;
    private TransactionChain chain;

    public TransactionKeyword getKeyword() {
        return keyword;
    }

    public void setKeyword(TransactionKeyword keyword) {
        this.keyword = keyword;
    }

    public Commit withKeyword(TransactionKeyword keyword) {
        setKeyword(keyword);
        return this;
    }

    public TransactionChain getChain() {
        return chain;
    }

    public void setChain(TransactionChain chain) {
        this.chain = chain;
    }

    public Commit withChain(TransactionChain chain) {
        setChain(chain);
        return this;
    }


    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("COMMIT");
        if (keyword != null) {
            builder.append(' ').append(keyword);
        }
        if (chain != null) {
            builder.append(' ').append(chain);
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}

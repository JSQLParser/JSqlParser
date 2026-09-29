/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
/*
 * Copyright (C) 2021 JSQLParser.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA
 */

package net.sf.jsqlparser.statement;

/**
 * @author are
 */
public class RollbackStatement implements Statement {
    private TransactionKeyword keyword;
    private TransactionChain chain;
    private boolean usingSavepointKeyword = false;
    private String savepointName = null;
    private String forceDistributedTransactionIdentifier = null;

    public TransactionKeyword getKeyword() {
        return keyword;
    }

    public void setKeyword(TransactionKeyword keyword) {
        this.keyword = keyword;
    }

    public RollbackStatement withKeyword(TransactionKeyword keyword) {
        setKeyword(keyword);
        return this;
    }

    public TransactionChain getChain() {
        return chain;
    }

    public void setChain(TransactionChain chain) {
        this.chain = chain;
    }

    public RollbackStatement withChain(TransactionChain chain) {
        setChain(chain);
        return this;
    }

    public boolean isUsingWorkKeyword() {
        return keyword == TransactionKeyword.WORK;
    }

    public void setUsingWorkKeyword(boolean usingWorkKeyword) {
        if (usingWorkKeyword) {
            keyword = TransactionKeyword.WORK;
        } else if (keyword == TransactionKeyword.WORK) {
            keyword = null;
        }
    }

    public RollbackStatement withUsingWorkKeyword(boolean usingWorkKeyword) {
        setUsingWorkKeyword(usingWorkKeyword);
        return this;
    }

    public boolean isUsingSavepointKeyword() {
        return usingSavepointKeyword;
    }

    public void setUsingSavepointKeyword(boolean usingSavepointKeyword) {
        this.usingSavepointKeyword = usingSavepointKeyword;
    }

    public RollbackStatement withUsingSavepointKeyword(boolean usingSavepointKeyword) {
        this.usingSavepointKeyword = usingSavepointKeyword;
        return this;
    }

    public String getSavepointName() {
        return savepointName;
    }

    public void setSavepointName(String savepointName) {
        this.savepointName = savepointName;
    }

    public RollbackStatement withSavepointName(String savepointName) {
        this.savepointName = savepointName;
        return this;
    }

    public String getForceDistributedTransactionIdentifier() {
        return forceDistributedTransactionIdentifier;
    }

    public void setForceDistributedTransactionIdentifier(
            String forceDistributedTransactionIdentifier) {
        this.forceDistributedTransactionIdentifier = forceDistributedTransactionIdentifier;
    }

    public RollbackStatement withForceDistributedTransactionIdentifier(
            String forceDistributedTransactionIdentifier) {
        this.forceDistributedTransactionIdentifier = forceDistributedTransactionIdentifier;
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("ROLLBACK");
        if (keyword != null) {
            builder.append(' ').append(keyword);
        }
        if (savepointName != null && !savepointName.trim().isEmpty()) {
            builder.append(" TO ").append(usingSavepointKeyword ? "SAVEPOINT " : "")
                    .append(savepointName);
        } else if (forceDistributedTransactionIdentifier != null
                && !forceDistributedTransactionIdentifier.trim().isEmpty()) {
            builder.append(" FORCE ").append(forceDistributedTransactionIdentifier);
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

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

}

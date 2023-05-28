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

    private boolean usingWorkKeyword = false;

    private boolean usingSavepointKeyword = false;

    private String savepointName = null;

    private String forceDistributedTransactionIdentifier = null;

    public boolean isUsingWorkKeyword() {
        return usingWorkKeyword;
    }

    public RollbackStatement withUsingWorkKeyword(boolean usingWorkKeyword) {
        this.usingWorkKeyword = usingWorkKeyword;
        return this;
    }

    public void setUsingWorkKeyword(boolean usingWorkKeyword) {
        this.usingWorkKeyword = usingWorkKeyword;
    }

    public boolean isUsingSavepointKeyword() {
        return usingSavepointKeyword;
    }

    public RollbackStatement withUsingSavepointKeyword(boolean usingSavepointKeyword) {
        this.usingSavepointKeyword = usingSavepointKeyword;
        return this;
    }

    public void setUsingSavepointKeyword(boolean usingSavepointKeyword) {
        this.usingSavepointKeyword = usingSavepointKeyword;
    }

    public String getSavepointName() {
        return savepointName;
    }

    public RollbackStatement withSavepointName(String savepointName) {
        this.savepointName = savepointName;
        return this;
    }

    public void setSavepointName(String savepointName) {
        this.savepointName = savepointName;
    }

    public String getForceDistributedTransactionIdentifier() {
        return forceDistributedTransactionIdentifier;
    }

    public RollbackStatement withForceDistributedTransactionIdentifier(String forceDistributedTransactionIdentifier) {
        this.forceDistributedTransactionIdentifier = forceDistributedTransactionIdentifier;
        return this;
    }

    public void setForceDistributedTransactionIdentifier(String forceDistributedTransactionIdentifier) {
        this.forceDistributedTransactionIdentifier = forceDistributedTransactionIdentifier;
    }

    @Override
    public String toString() {
        return "ROLLBACK " + (usingWorkKeyword ? "WORK " : "") + (savepointName != null && savepointName.trim().length() != 0 ? "TO " + (usingSavepointKeyword ? "SAVEPOINT " : "") + savepointName : forceDistributedTransactionIdentifier != null && forceDistributedTransactionIdentifier.trim().length() != 0 ? "FORCE " + forceDistributedTransactionIdentifier : "");
    }

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }
}

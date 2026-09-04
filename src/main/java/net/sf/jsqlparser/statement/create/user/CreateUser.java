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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/** MySQL {@code CREATE USER} statement with structured account and authentication data. */
public class CreateUser implements Statement {

    private boolean ifNotExists;
    private final List<UserAccount> accounts = new ArrayList<>();

    public boolean isIfNotExists() {
        return ifNotExists;
    }

    public void setIfNotExists(boolean ifNotExists) {
        this.ifNotExists = ifNotExists;
    }

    public List<UserAccount> getAccounts() {
        return Collections.unmodifiableList(accounts);
    }

    public void setAccounts(Collection<? extends UserAccount> accounts) {
        this.accounts.clear();
        if (accounts != null) {
            this.accounts.addAll(accounts);
        }
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    @Override
    public String toString() {
        return "CREATE USER " + (ifNotExists ? "IF NOT EXISTS " : "")
                + accounts.stream().map(Object::toString).collect(Collectors.joining(", "));
    }

    public CreateUser withIfNotExists(boolean ifNotExists) {
        setIfNotExists(ifNotExists);
        return this;
    }

    public CreateUser withAccounts(Collection<? extends UserAccount> accounts) {
        setAccounts(accounts);
        return this;
    }

    public CreateUser addAccounts(UserAccount... accounts) {
        Collections.addAll(this.accounts, accounts);
        return this;
    }
}

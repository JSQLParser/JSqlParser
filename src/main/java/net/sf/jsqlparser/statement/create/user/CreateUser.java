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
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * MySQL {@code CREATE USER} statement with structured account and authentication data. The
 * {@link UserOptions} after the account list are shared with
 * {@link net.sf.jsqlparser.statement.alter.AlterUser}.
 */
public class CreateUser extends ASTNodeAccessImpl implements Statement {

    private boolean ifNotExists;
    private final List<UserAccount> accounts = new ArrayList<>();
    private UserOptions options = new UserOptions();

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

    /** The account options after the account list; empty, never null, when none are given. */
    public UserOptions getOptions() {
        return options;
    }

    public void setOptions(UserOptions options) {
        this.options = options != null ? options : new UserOptions();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("CREATE USER ");
        if (ifNotExists) {
            builder.append("IF NOT EXISTS ");
        }
        builder.append(accounts.stream().map(Object::toString).collect(Collectors.joining(", ")));
        return options.appendTo(builder).toString();
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

    public CreateUser withOptions(UserOptions options) {
        setOptions(options);
        return this;
    }
}

/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.user.UserAccount;
import net.sf.jsqlparser.statement.create.user.UserAuthentication;
import net.sf.jsqlparser.statement.create.user.UserOptions;

/**
 * MySQL {@code ALTER USER} statement. Each account may change its authentication, including
 * {@code REPLACE}, {@code RETAIN CURRENT PASSWORD} and {@code DISCARD OLD PASSWORD}; the
 * {@link UserOptions} are those of {@link net.sf.jsqlparser.statement.create.user.CreateUser}.
 * {@code ALTER USER acct DEFAULT ROLE ...} keeps its roles in
 * {@link UserOptions#getDefaultRoles()}. {@code ALTER USER USER() ...} names the current user with
 * the unquoted account name {@code USER()}, see {@link #isUserFunction()}.
 */
public class AlterUser extends ASTNodeAccessImpl implements Statement {

    private static final String USER_FUNCTION = "USER()";

    private boolean ifExists;
    private final List<UserAccount> accounts = new ArrayList<>();
    private UserOptions options = new UserOptions();

    public boolean isIfExists() {
        return ifExists;
    }

    public void setIfExists(boolean ifExists) {
        this.ifExists = ifExists;
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

    /** Whether the statement alters the current user named by {@code USER()}. */
    public boolean isUserFunction() {
        if (accounts.size() != 1) {
            return false;
        }
        UserAccount account = accounts.get(0);
        StringValue user = account.getUser();
        return account.getHost() == null && user != null && "".equals(user.getQuoteStr())
                && USER_FUNCTION.equalsIgnoreCase(user.getValue());
    }

    /** Replaces the accounts with {@code USER()} and the given authentication change. */
    public AlterUser withUserFunction(UserAuthentication authentication) {
        UserAccount account = new UserAccount()
                .withUser(new StringValue(USER_FUNCTION).setQuoteStr(""))
                .withAuthentication(authentication);
        setAccounts(Collections.singletonList(account));
        return this;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("ALTER USER ");
        if (ifExists) {
            builder.append("IF EXISTS ");
        }
        builder.append(accounts.stream().map(Object::toString).collect(Collectors.joining(", ")));
        return options.appendTo(builder).toString();
    }

    public AlterUser withIfExists(boolean ifExists) {
        setIfExists(ifExists);
        return this;
    }

    public AlterUser withAccounts(Collection<? extends UserAccount> accounts) {
        setAccounts(accounts);
        return this;
    }

    public AlterUser addAccounts(UserAccount... accounts) {
        Collections.addAll(this.accounts, accounts);
        return this;
    }

    public AlterUser withOptions(UserOptions options) {
        setOptions(options);
        return this;
    }
}

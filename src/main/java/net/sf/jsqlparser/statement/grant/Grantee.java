/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.grant;

import java.io.Serializable;
import java.util.Locale;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.statement.create.user.UserAccount;

/**
 * A role specification in GRANT and REVOKE: a grantee, the PostgreSQL grantor, the MySQL {@code AS}
 * user or the MySQL proxied account.
 *
 * <p>
 * Named users and roles are held as a {@link UserAccount}: a MySQL account {@code user[@host]}, or
 * a PostgreSQL role name without host.
 */
public class Grantee implements Serializable {

    public enum Kind {
        /** A named user or role, see {@link Grantee#getAccount()}. */
        ACCOUNT,
        /** PostgreSQL {@code GROUP name}, see {@link Grantee#getAccount()}. */
        GROUP, PUBLIC, CURRENT_USER, CURRENT_ROLE, SESSION_USER
    }

    private Kind kind;
    private UserAccount account;
    private boolean currentUserParentheses;

    public Grantee(Kind kind) {
        this.kind = kind;
    }

    public Grantee(UserAccount account) {
        this.kind = Kind.ACCOUNT;
        this.account = account;
    }

    /**
     * Converts the text of the string-based API. Special role names are recognized; any other text
     * becomes an account whose user name is kept verbatim.
     */
    public static Grantee valueOf(String text) {
        String upper = text.trim().toUpperCase(Locale.ROOT);
        if (upper.startsWith("GROUP ")) {
            return new Grantee(Kind.GROUP)
                    .withAccount(accountOf(text.trim().substring("GROUP ".length()).trim()));
        }
        if ("CURRENT_USER()".equals(upper.replace(" ", ""))) {
            return new Grantee(Kind.CURRENT_USER).withCurrentUserParentheses(true);
        }
        for (Kind special : new Kind[] {Kind.PUBLIC, Kind.CURRENT_USER, Kind.CURRENT_ROLE,
                Kind.SESSION_USER}) {
            if (special.name().equals(upper)) {
                return new Grantee(special);
            }
        }
        return new Grantee(accountOf(text));
    }

    /** An account or role name kept as written, without a host. */
    static UserAccount accountOf(String name) {
        StringValue user = new StringValue();
        user.setValue(name);
        return new UserAccount().withUser(user.setQuoteStr(""));
    }

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }

    /** The named user or role of an {@link Kind#ACCOUNT} or {@link Kind#GROUP} grantee. */
    public UserAccount getAccount() {
        return account;
    }

    public void setAccount(UserAccount account) {
        this.account = account;
    }

    /** Whether MySQL {@code CURRENT_USER()} was written with parentheses. */
    public boolean isCurrentUserParentheses() {
        return currentUserParentheses;
    }

    public void setCurrentUserParentheses(boolean currentUserParentheses) {
        this.currentUserParentheses = currentUserParentheses;
    }

    public Grantee withAccount(UserAccount account) {
        setAccount(account);
        return this;
    }

    public Grantee withCurrentUserParentheses(boolean currentUserParentheses) {
        setCurrentUserParentheses(currentUserParentheses);
        return this;
    }

    @Override
    public String toString() {
        switch (kind) {
            case ACCOUNT:
                return String.valueOf(account);
            case GROUP:
                return "GROUP " + account;
            case CURRENT_USER:
                return currentUserParentheses ? "CURRENT_USER()" : "CURRENT_USER";
            default:
                return kind.name();
        }
    }
}

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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.sf.jsqlparser.expression.StringValue;

/**
 * A user and optional host together with its authentication clause. The current user may be named
 * {@code CURRENT_USER} or {@code CURRENT_USER()}. MySQL multi-factor authentication adds the second
 * and third factor of {@code CREATE USER ... IDENTIFIED ... AND IDENTIFIED ...} in
 * {@link #getAdditionalAuthentications()}, and the factor changes and registration steps of
 * {@code ALTER USER} in {@link #getFactorOperations()}.
 */
public class UserAccount implements Serializable {

    private StringValue user;
    private StringValue host;
    private UserAuthentication authentication;
    private boolean currentUserParentheses;
    private final List<UserAuthentication> additionalAuthentications = new ArrayList<>();
    private final List<UserFactorOperation> factorOperations = new ArrayList<>();

    public StringValue getUser() {
        return user;
    }

    public void setUser(StringValue user) {
        this.user = user;
    }

    public StringValue getHost() {
        return host;
    }

    public void setHost(StringValue host) {
        this.host = host;
    }

    /** Whether the user is written {@code CURRENT_USER()}. */
    public boolean isCurrentUserParentheses() {
        return currentUserParentheses;
    }

    public void setCurrentUserParentheses(boolean currentUserParentheses) {
        this.currentUserParentheses = currentUserParentheses;
    }

    public UserAuthentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(UserAuthentication authentication) {
        this.authentication = authentication;
    }

    /**
     * The authentication of the second and third factor, each written {@code AND IDENTIFIED ...}
     * after the first one in {@code CREATE USER}; empty, never null, when none are given.
     */
    public List<UserAuthentication> getAdditionalAuthentications() {
        return Collections.unmodifiableList(additionalAuthentications);
    }

    public void setAdditionalAuthentications(
            Collection<? extends UserAuthentication> additionalAuthentications) {
        this.additionalAuthentications.clear();
        if (additionalAuthentications != null) {
            this.additionalAuthentications.addAll(additionalAuthentications);
        }
    }

    /**
     * The {@code ADD}, {@code MODIFY} and {@code DROP} factor changes, or the single registration
     * step, of an {@code ALTER USER} account in source order; empty, never null, when none are
     * given.
     */
    public List<UserFactorOperation> getFactorOperations() {
        return Collections.unmodifiableList(factorOperations);
    }

    public void setFactorOperations(Collection<? extends UserFactorOperation> factorOperations) {
        this.factorOperations.clear();
        if (factorOperations != null) {
            this.factorOperations.addAll(factorOperations);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder().append(user);
        if (currentUserParentheses) {
            builder.append("()");
        }
        if (host != null) {
            builder.append("@").append(host);
        }
        if (authentication != null) {
            builder.append(" ").append(authentication);
        }
        for (UserAuthentication additional : additionalAuthentications) {
            builder.append(" AND ").append(additional);
        }
        for (UserFactorOperation operation : factorOperations) {
            builder.append(" ").append(operation);
        }
        return builder.toString();
    }

    public UserAccount withUser(StringValue user) {
        setUser(user);
        return this;
    }

    public UserAccount withHost(StringValue host) {
        setHost(host);
        return this;
    }

    public UserAccount withCurrentUserParentheses(boolean currentUserParentheses) {
        setCurrentUserParentheses(currentUserParentheses);
        return this;
    }

    public UserAccount withAuthentication(UserAuthentication authentication) {
        setAuthentication(authentication);
        return this;
    }

    public UserAccount withAdditionalAuthentications(
            Collection<? extends UserAuthentication> additionalAuthentications) {
        setAdditionalAuthentications(additionalAuthentications);
        return this;
    }

    public UserAccount addAdditionalAuthentications(
            UserAuthentication... additionalAuthentications) {
        Collections.addAll(this.additionalAuthentications, additionalAuthentications);
        return this;
    }

    public UserAccount withFactorOperations(
            Collection<? extends UserFactorOperation> factorOperations) {
        setFactorOperations(factorOperations);
        return this;
    }

    public UserAccount addFactorOperations(UserFactorOperation... factorOperations) {
        Collections.addAll(this.factorOperations, factorOperations);
        return this;
    }
}

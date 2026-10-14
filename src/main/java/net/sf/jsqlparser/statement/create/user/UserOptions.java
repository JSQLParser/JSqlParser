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
import java.util.stream.Collectors;
import net.sf.jsqlparser.expression.StringValue;

/**
 * The account options shared by MySQL {@code CREATE USER} and {@code ALTER USER}, rendered in the
 * clause order MySQL requires:
 *
 * <pre>
 * [DEFAULT ROLE ...] [REQUIRE ...] [WITH resource_limit ...]
 * [password_or_lock_option ...] [COMMENT '...' | ATTRIBUTE '...']
 * </pre>
 *
 * Absent clauses are null or empty; nothing is filled in with server defaults.
 */
public class UserOptions implements Serializable {

    private RoleSelection defaultRoles;
    private final List<TlsOption> tlsOptions = new ArrayList<>();
    private final List<ResourceLimit> resourceLimits = new ArrayList<>();
    private final List<PasswordLockOption> passwordLockOptions = new ArrayList<>();
    private StringValue comment;
    private StringValue attribute;

    /**
     * The {@code DEFAULT ROLE} clause: a role list for {@code CREATE USER}, also {@code NONE} or
     * {@code ALL} for {@code ALTER USER}. Null when omitted.
     */
    public RoleSelection getDefaultRoles() {
        return defaultRoles;
    }

    public void setDefaultRoles(RoleSelection defaultRoles) {
        this.defaultRoles = defaultRoles;
    }

    /** The {@code REQUIRE} options in source order; empty when the clause is omitted. */
    public List<TlsOption> getTlsOptions() {
        return Collections.unmodifiableList(tlsOptions);
    }

    public void setTlsOptions(Collection<? extends TlsOption> tlsOptions) {
        replace(this.tlsOptions, tlsOptions);
    }

    /** The {@code WITH} resource limits in source order; empty when the clause is omitted. */
    public List<ResourceLimit> getResourceLimits() {
        return Collections.unmodifiableList(resourceLimits);
    }

    public void setResourceLimits(Collection<? extends ResourceLimit> resourceLimits) {
        replace(this.resourceLimits, resourceLimits);
    }

    /** The password-management and locking options in source order, repetitions included. */
    public List<PasswordLockOption> getPasswordLockOptions() {
        return Collections.unmodifiableList(passwordLockOptions);
    }

    public void setPasswordLockOptions(
            Collection<? extends PasswordLockOption> passwordLockOptions) {
        replace(this.passwordLockOptions, passwordLockOptions);
    }

    public StringValue getComment() {
        return comment;
    }

    public void setComment(StringValue comment) {
        this.comment = comment;
    }

    /** The {@code ATTRIBUTE} JSON object string, which MySQL accepts instead of a comment. */
    public StringValue getAttribute() {
        return attribute;
    }

    public void setAttribute(StringValue attribute) {
        this.attribute = attribute;
    }

    /** Whether no clause is present. */
    public boolean isEmpty() {
        return defaultRoles == null && tlsOptions.isEmpty() && resourceLimits.isEmpty()
                && passwordLockOptions.isEmpty() && comment == null && attribute == null;
    }

    /** Appends each present clause preceded by a space. */
    public StringBuilder appendTo(StringBuilder builder) {
        if (defaultRoles != null) {
            builder.append(" DEFAULT ROLE ").append(defaultRoles);
        }
        if (!tlsOptions.isEmpty()) {
            builder.append(" REQUIRE ").append(join(tlsOptions));
        }
        if (!resourceLimits.isEmpty()) {
            builder.append(" WITH ").append(join(resourceLimits));
        }
        if (!passwordLockOptions.isEmpty()) {
            builder.append(' ').append(join(passwordLockOptions));
        }
        if (comment != null) {
            builder.append(" COMMENT ").append(comment);
        }
        if (attribute != null) {
            builder.append(" ATTRIBUTE ").append(attribute);
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString().trim();
    }

    public UserOptions withDefaultRoles(RoleSelection defaultRoles) {
        setDefaultRoles(defaultRoles);
        return this;
    }

    public UserOptions withTlsOptions(Collection<? extends TlsOption> tlsOptions) {
        setTlsOptions(tlsOptions);
        return this;
    }

    public UserOptions addTlsOptions(TlsOption... tlsOptions) {
        Collections.addAll(this.tlsOptions, tlsOptions);
        return this;
    }

    public UserOptions withResourceLimits(Collection<? extends ResourceLimit> resourceLimits) {
        setResourceLimits(resourceLimits);
        return this;
    }

    public UserOptions addResourceLimits(ResourceLimit... resourceLimits) {
        Collections.addAll(this.resourceLimits, resourceLimits);
        return this;
    }

    public UserOptions withPasswordLockOptions(
            Collection<? extends PasswordLockOption> passwordLockOptions) {
        setPasswordLockOptions(passwordLockOptions);
        return this;
    }

    public UserOptions addPasswordLockOptions(PasswordLockOption... passwordLockOptions) {
        Collections.addAll(this.passwordLockOptions, passwordLockOptions);
        return this;
    }

    public UserOptions withComment(StringValue comment) {
        setComment(comment);
        return this;
    }

    public UserOptions withAttribute(StringValue attribute) {
        setAttribute(attribute);
        return this;
    }

    private static <T> void replace(List<T> target, Collection<? extends T> source) {
        target.clear();
        if (source != null) {
            target.addAll(source);
        }
    }

    private static String join(List<?> items) {
        return items.stream().map(Object::toString).collect(Collectors.joining(" "));
    }
}

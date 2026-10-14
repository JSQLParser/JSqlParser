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

/**
 * The roles named by MySQL {@code SET ROLE}, {@code SET DEFAULT ROLE}, the {@code DEFAULT ROLE}
 * clause of {@code CREATE USER} and {@code ALTER USER}, and {@code GRANT ... AS user WITH ROLE}.
 * Role names use the account syntax, so each role is a {@link UserAccount} without authentication.
 */
public class RoleSelection implements Serializable {

    public enum Kind {
        /**
         * {@code DEFAULT}: the account's default roles, only in {@code SET ROLE} and
         * {@code WITH ROLE}.
         */
        DEFAULT,
        /** {@code NONE}. */
        NONE,
        /** {@code ALL}. */
        ALL,
        /** {@code ALL EXCEPT role [, role] ...}, only in {@code SET ROLE} and {@code WITH ROLE}. */
        ALL_EXCEPT,
        /** {@code role [, role] ...}. */
        ROLES
    }

    private Kind kind;
    private final List<UserAccount> roles = new ArrayList<>();

    public RoleSelection() {
        // empty constructor
    }

    public RoleSelection(Kind kind) {
        this.kind = kind;
    }

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }

    /** Returns the listed roles of {@link Kind#ROLES} and {@link Kind#ALL_EXCEPT}. */
    public List<UserAccount> getRoles() {
        return Collections.unmodifiableList(roles);
    }

    public void setRoles(Collection<? extends UserAccount> roles) {
        this.roles.clear();
        if (roles != null) {
            this.roles.addAll(roles);
        }
    }

    @Override
    public String toString() {
        String names = roles.stream().map(Object::toString).collect(Collectors.joining(", "));
        switch (kind) {
            case ROLES:
                return names;
            case ALL_EXCEPT:
                return "ALL EXCEPT " + names;
            default:
                return kind.name();
        }
    }

    public RoleSelection withKind(Kind kind) {
        setKind(kind);
        return this;
    }

    public RoleSelection withRoles(Collection<? extends UserAccount> roles) {
        setRoles(roles);
        return this;
    }

    public RoleSelection addRoles(UserAccount... roles) {
        Collections.addAll(this.roles, roles);
        return this;
    }
}

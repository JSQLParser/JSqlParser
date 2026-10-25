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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.RoutineReference;

/**
 * Targets have distinct kinds; a routine signature is not a function invocation. MySQL privilege
 * levels written with {@code *} are a {@link Wildcard}, and the account of MySQL
 * {@code PROXY ON user} is an {@link Kind#ACCOUNT} target.
 */
public class PrivilegeTarget implements Serializable {
    public enum Kind {
        TABLE, SEQUENCE, DATABASE, DOMAIN, FOREIGN_DATA_WRAPPER, FOREIGN_SERVER, FUNCTION, PROCEDURE, ROUTINE, LANGUAGE, LARGE_OBJECT, PARAMETER, SCHEMA, TABLESPACE, TYPE,
        /**
         * The proxied account of MySQL {@code PROXY ON user}, see
         * {@link PrivilegeTarget#getAccount()}.
         */
        ACCOUNT
    }

    /** MySQL privilege levels written with {@code *}. */
    public enum Wildcard {
        /** {@code *}: the default database. */
        DEFAULT_DATABASE,
        /** {@code *.*}: all databases. */
        GLOBAL,
        /**
         * {@code db.*}: the database named by the single entry of
         * {@link PrivilegeTarget#getNames()}.
         */
        DATABASE
    }

    private Kind kind = Kind.TABLE;
    private boolean explicitKind;
    private boolean allInSchema;
    private boolean defaultPrivileges;
    private Wildcard wildcard;
    private Grantee account;
    private List<List<String>> names = new ArrayList<>();
    private List<RoutineReference> routines = new ArrayList<>();

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }

    public boolean isExplicitKind() {
        return explicitKind;
    }

    public void setExplicitKind(boolean explicitKind) {
        this.explicitKind = explicitKind;
    }

    public boolean isAllInSchema() {
        return allInSchema;
    }

    public void setAllInSchema(boolean allInSchema) {
        this.allInSchema = allInSchema;
    }

    public boolean isDefaultPrivileges() {
        return defaultPrivileges;
    }

    public void setDefaultPrivileges(boolean defaultPrivileges) {
        this.defaultPrivileges = defaultPrivileges;
    }

    /** The MySQL wildcard privilege level, or null for named targets. */
    public Wildcard getWildcard() {
        return wildcard;
    }

    public void setWildcard(Wildcard wildcard) {
        this.wildcard = wildcard;
    }

    /** The proxied account of an {@link Kind#ACCOUNT} target. */
    public Grantee getAccount() {
        return account;
    }

    public void setAccount(Grantee account) {
        this.account = account;
    }

    public List<List<String>> getNames() {
        return names;
    }

    public void setNames(List<List<String>> names) {
        this.names = names;
    }

    public List<RoutineReference> getRoutines() {
        return routines;
    }

    public void setRoutines(List<RoutineReference> routines) {
        this.routines = routines;
    }

    public void visitTables(Consumer<Table> visitor) {
        if (kind == Kind.TABLE && wildcard == null && !allInSchema && !defaultPrivileges) {
            for (List<String> name : names) {
                if (!name.isEmpty()) {
                    visitor.accept(new Table(name));
                }
            }
        }
    }

    /** The target names as written after the optional kind, for example {@code db.*}. */
    String getTargetText() {
        if (kind == Kind.ACCOUNT) {
            return String.valueOf(account);
        }
        if (wildcard == Wildcard.DEFAULT_DATABASE) {
            return "*";
        }
        if (wildcard == Wildcard.GLOBAL) {
            return "*.*";
        }
        String targets = routines.isEmpty()
                ? names.stream().map(PrivilegeTarget::joinName).collect(Collectors.joining(", "))
                : routines.stream().map(Object::toString).collect(Collectors.joining(", "));
        return wildcard == Wildcard.DATABASE ? targets + ".*" : targets;
    }

    private static String joinName(List<String> name) {
        return name.stream().map(part -> part == null ? "" : part)
                .collect(Collectors.joining("."));
    }

    @Override
    public String toString() {
        String type = kind.name().replace('_', ' ');
        if (defaultPrivileges) {
            return type + "S";
        }
        if (allInSchema) {
            return "ALL " + type + "S IN SCHEMA " + getTargetText();
        }
        return (explicitKind ? type + " " : "") + getTargetText();
    }
}

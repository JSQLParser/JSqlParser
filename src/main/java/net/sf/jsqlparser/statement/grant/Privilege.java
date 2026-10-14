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
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;

/**
 * A privilege of GRANT and REVOKE. Static privileges are {@link Kind} constants, rendered with
 * spaces for underscores; MySQL dynamic privileges such as {@code BACKUP_ADMIN} are
 * {@link Kind#DYNAMIC} with {@link #getDynamicName()}.
 */
public class Privilege implements Serializable {
    public enum Kind {
        ALL, SELECT, INSERT, UPDATE, DELETE, TRUNCATE, REFERENCES, TRIGGER, MAINTAIN, USAGE, CREATE, CONNECT, TEMPORARY, TEMP, EXECUTE, SET, ALTER_SYSTEM, ALTER, DROP,
        // MySQL static privileges
        ALTER_ROUTINE, CREATE_ROLE, CREATE_ROUTINE, CREATE_TABLESPACE, CREATE_TEMPORARY_TABLES, CREATE_USER, CREATE_VIEW, DROP_ROLE, EVENT, FILE, GRANT_OPTION, INDEX, LOCK_TABLES, PROCESS, PROXY, RELOAD, REPLICATION_CLIENT, REPLICATION_SLAVE, SHOW_DATABASES, SHOW_VIEW, SHUTDOWN, SUPER,
        /** A MySQL dynamic privilege, see {@link Privilege#getDynamicName()}. */
        DYNAMIC
    }

    private String legacyName;
    private Kind kind;
    private String dynamicName;
    private boolean usePrivileges;
    private ExpressionList<Column> columns;

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }

    public boolean isUsePrivileges() {
        return usePrivileges;
    }

    public void setUsePrivileges(boolean usePrivileges) {
        this.usePrivileges = usePrivileges;
    }

    public ExpressionList<Column> getColumns() {
        return columns;
    }

    public void setColumns(ExpressionList<Column> columns) {
        this.columns = columns;
    }

    /** The identifier of a {@link Kind#DYNAMIC} privilege, as written. */
    public String getDynamicName() {
        return dynamicName;
    }

    public void setDynamicName(String dynamicName) {
        this.dynamicName = dynamicName;
    }

    /** The privilege name as rendered, for example {@code CREATE VIEW} or {@code BACKUP_ADMIN}. */
    public String getName() {
        if (kind == null) {
            return legacyName;
        }
        return kind == Kind.DYNAMIC ? dynamicName : kind.name().replace('_', ' ');
    }

    public Privilege(Kind kind) {
        this.kind = kind;
    }

    /**
     * Preserves values supplied through the pre-existing string-based Grant API. Names of static
     * privileges, such as {@code CREATE VIEW}, become typed; other text is kept as written.
     */
    public Privilege(String name) {
        this.kind = staticKind(name);
        if (kind == null) {
            this.legacyName = name;
        }
    }

    private static Kind staticKind(String name) {
        String constant = name.replace(' ', '_');
        for (Kind candidate : Kind.values()) {
            if (candidate != Kind.DYNAMIC && candidate.name().equals(constant)) {
                return candidate;
            }
        }
        return null;
    }

    public static Privilege dynamic(String name) {
        Privilege privilege = new Privilege(Kind.DYNAMIC);
        privilege.setDynamicName(name);
        return privilege;
    }

    public void appendTo(StringBuilder sql, Consumer<Expression> visitor) {
        sql.append(getName());
        if (usePrivileges) {
            sql.append(" PRIVILEGES");
        }
        if (columns != null) {
            sql.append(" (");
            for (int i = 0; i < columns.size(); i++) {
                if (i > 0) {
                    sql.append(", ");
                }
                visitor.accept(columns.get(i));
            }
            sql.append(')');
        }
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();
        appendTo(sql, sql::append);
        return sql.toString();
    }
}

/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.foreign;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.TableReference;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * PostgreSQL {@code IMPORT FOREIGN SCHEMA remote_schema [{LIMIT TO | EXCEPT} (table [, ...])] FROM
 * SERVER server INTO local_schema [OPTIONS (option 'value' [, ...])]}, which creates foreign tables
 * in the local schema for the tables of a schema on the foreign server.
 *
 * @see <a href="https://www.postgresql.org/docs/18/sql-importforeignschema.html">IMPORT FOREIGN
 *      SCHEMA</a>
 */
public class ImportForeignSchema extends ForeignDataStatement {
    private String remoteSchema;
    private Qualification qualification;
    private final List<TableReference> tables = new ArrayList<>();
    private String server;
    private String localSchema;

    /** How the table list restricts the import. */
    public enum Qualification {
        /** Import only the listed tables. */
        LIMIT_TO("LIMIT TO"),
        /** Import every table but the listed ones. */
        EXCEPT("EXCEPT");

        private final String sql;

        Qualification(String sql) {
            this.sql = sql;
        }

        @Override
        public String toString() {
            return sql;
        }
    }

    public String getRemoteSchema() {
        return remoteSchema;
    }

    public void setRemoteSchema(String remoteSchema) {
        this.remoteSchema = remoteSchema;
    }

    public ImportForeignSchema withRemoteSchema(String remoteSchema) {
        setRemoteSchema(remoteSchema);
        return this;
    }

    /** The kind of the table list, or null when the whole remote schema is imported. */
    public Qualification getQualification() {
        return qualification;
    }

    /**
     * The tables of the remote schema that LIMIT TO or EXCEPT lists; empty without a qualification.
     * The list is a read-only snapshot, change it with {@link #setTables(Qualification, List)}.
     */
    public List<TableReference> getTables() {
        return List.copyOf(tables);
    }

    /**
     * Restricts the import to the listed tables or excludes them.
     *
     * @param qualification the kind of the list, null to import the whole remote schema
     * @param tables the listed tables; at least one with a qualification, none without
     */
    public void setTables(Qualification qualification, List<TableReference> tables) {
        List<TableReference> copy = tables == null ? List.of() : List.copyOf(tables);
        if (qualification == null != copy.isEmpty()) {
            throw new IllegalArgumentException(
                    "LIMIT TO and EXCEPT need a table list, and a table list needs one of them");
        }
        this.qualification = qualification;
        this.tables.clear();
        this.tables.addAll(copy);
    }

    public ImportForeignSchema withTables(Qualification qualification,
            List<TableReference> tables) {
        setTables(qualification, tables);
        return this;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public ImportForeignSchema withServer(String server) {
        setServer(server);
        return this;
    }

    public String getLocalSchema() {
        return localSchema;
    }

    public void setLocalSchema(String localSchema) {
        this.localSchema = localSchema;
    }

    public ImportForeignSchema withLocalSchema(String localSchema) {
        setLocalSchema(localSchema);
        return this;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }

    @Override
    public StringBuilder appendTo(StringBuilder sql, Consumer<Expression> printer) {
        sql.append("IMPORT FOREIGN SCHEMA ").append(Objects.requireNonNull(remoteSchema));
        if (qualification != null) {
            sql.append(' ').append(qualification).append(" (");
            for (int i = 0; i < tables.size(); i++) {
                if (i > 0) {
                    sql.append(", ");
                }
                tables.get(i).appendTo(sql);
            }
            sql.append(')');
        }
        sql.append(" FROM SERVER ").append(Objects.requireNonNull(server));
        sql.append(" INTO ").append(Objects.requireNonNull(localSchema));
        appendOptionsTo(sql, printer);
        return sql;
    }
}

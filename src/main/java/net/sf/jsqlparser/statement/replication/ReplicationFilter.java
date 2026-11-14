/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.replication;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.schema.Table;

/**
 * A rule of MySQL's {@code CHANGE REPLICATION FILTER}: a filter name and a parenthesized, possibly
 * empty list. The list holds database names, qualified table names, quoted wildcard patterns or
 * pairs of database names, depending on {@link Kind#getValueType()}; the list of the other value
 * types stays empty.
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/change-replication-filter.html">MySQL
 *      CHANGE REPLICATION FILTER</a>
 */
public class ReplicationFilter implements Serializable {
    private final Kind kind;
    private final List<String> databases = new ArrayList<>();
    private final List<Table> tables = new ArrayList<>();
    private final List<StringValue> patterns = new ArrayList<>();
    private final List<DatabaseRewrite> rewrites = new ArrayList<>();

    /** What the list of a filter holds. */
    public enum ValueType {
        /** Database names: {@code (d1, d2)} */
        DATABASES,
        /** Database-qualified table names: {@code (d1.t1)} */
        TABLES,
        /** Quoted patterns of database and table names: {@code ('d%.t%')} */
        PATTERNS,
        /** Pairs of database names: {@code ((from_db, to_db))} */
        DATABASE_PAIRS
    }

    public enum Kind {
        /** Replicate only these databases */
        REPLICATE_DO_DB(ValueType.DATABASES),
        /** Skip these databases */
        REPLICATE_IGNORE_DB(ValueType.DATABASES),
        /** Replicate only these tables */
        REPLICATE_DO_TABLE(ValueType.TABLES),
        /** Skip these tables */
        REPLICATE_IGNORE_TABLE(ValueType.TABLES),
        /** Replicate only tables matching these patterns */
        REPLICATE_WILD_DO_TABLE(ValueType.PATTERNS),
        /** Skip tables matching these patterns */
        REPLICATE_WILD_IGNORE_TABLE(ValueType.PATTERNS),
        /** Apply the changes of one database to another */
        REPLICATE_REWRITE_DB(ValueType.DATABASE_PAIRS);

        private final ValueType valueType;

        Kind(ValueType valueType) {
            this.valueType = valueType;
        }

        public ValueType getValueType() {
            return valueType;
        }
    }

    /** A pair of {@code REPLICATE_REWRITE_DB}: the source database and its replacement. */
    public static class DatabaseRewrite implements Serializable {
        private final String from;
        private final String to;

        public DatabaseRewrite(String from, String to) {
            this.from = Objects.requireNonNull(from);
            this.to = Objects.requireNonNull(to);
        }

        public String getFrom() {
            return from;
        }

        public String getTo() {
            return to;
        }

        @Override
        public String toString() {
            return "(" + from + ", " + to + ")";
        }
    }

    public ReplicationFilter(Kind kind) {
        this.kind = Objects.requireNonNull(kind);
    }

    public Kind getKind() {
        return kind;
    }

    /** The database names of REPLICATE_DO_DB and REPLICATE_IGNORE_DB. */
    public List<String> getDatabases() {
        return databases;
    }

    /** The tables of REPLICATE_DO_TABLE and REPLICATE_IGNORE_TABLE. */
    public List<Table> getTables() {
        return tables;
    }

    /** The patterns of REPLICATE_WILD_DO_TABLE and REPLICATE_WILD_IGNORE_TABLE. */
    public List<StringValue> getPatterns() {
        return patterns;
    }

    /** The pairs of REPLICATE_REWRITE_DB. */
    public List<DatabaseRewrite> getRewrites() {
        return rewrites;
    }

    /** The list that {@link Kind#getValueType()} selects. */
    public List<?> getValues() {
        switch (kind.getValueType()) {
            case DATABASES:
                return databases;
            case TABLES:
                return tables;
            case PATTERNS:
                return patterns;
            default:
                return rewrites;
        }
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append(kind).append(" = (");
        List<?> values = getValues();
        for (int i = 0; i < values.size(); i++) {
            builder.append(i == 0 ? "" : ", ").append(values.get(i));
        }
        return builder.append(')');
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}

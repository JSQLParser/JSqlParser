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

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.replication.ReplicationSourceOption.ValueType;

/**
 * A condition of the {@code UNTIL} clause of MySQL's {@code START REPLICA}. The first condition may
 * be any kind; the ones after it give further log coordinates.
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/start-replica.html">MySQL START REPLICA</a>
 */
public class ReplicaUntilOption extends ReplicationOption<ReplicaUntilOption.Kind> {

    public enum Kind {
        /** A binary log file of the source */
        SOURCE_LOG_FILE(ValueType.STRING, true),
        /** A position in that binary log file */
        SOURCE_LOG_POS(ValueType.NUMBER, true),
        /** A relay log file */
        RELAY_LOG_FILE(ValueType.STRING, true),
        /** A position in that relay log file */
        RELAY_LOG_POS(ValueType.NUMBER, true),
        /** Stop before the first transaction of a GTID set */
        SQL_BEFORE_GTIDS(ValueType.STRING, false),
        /** Stop after the last transaction of a GTID set */
        SQL_AFTER_GTIDS(ValueType.STRING, false),
        /** Stop once a multithreaded applier has no gaps */
        SQL_AFTER_MTS_GAPS(ValueType.NONE, false);

        private final ValueType valueType;
        private final boolean logCoordinate;

        Kind(ValueType valueType, boolean logCoordinate) {
            this.valueType = valueType;
            this.logCoordinate = logCoordinate;
        }

        public ValueType getValueType() {
            return valueType;
        }

        /** Whether this is a log file name or position, which may follow another condition. */
        public boolean isLogCoordinate() {
            return logCoordinate;
        }
    }

    public ReplicaUntilOption(String name, Kind kind, Expression value) {
        super(name, kind, value);
    }
}

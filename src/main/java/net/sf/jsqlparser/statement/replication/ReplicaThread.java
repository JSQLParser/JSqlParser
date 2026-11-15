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

import java.util.List;

/**
 * A replication thread that MySQL's {@code START REPLICA} and {@code STOP REPLICA} name.
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/start-replica.html">MySQL START REPLICA</a>
 */
public enum ReplicaThread {
    /** The receiver thread, which reads the source's binary log into the relay log. */
    IO_THREAD,
    /** The applier thread, which executes the events of the relay log. */
    SQL_THREAD;

    static void appendTo(StringBuilder builder, List<ReplicaThread> threads) {
        for (int i = 0; i < threads.size(); i++) {
            builder.append(i == 0 ? " " : ", ").append(threads.get(i));
        }
    }
}

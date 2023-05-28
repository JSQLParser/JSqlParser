/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
/*
 * Copyright (C) 2021 JSQLParser.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA
 */
package net.sf.jsqlparser.statement.alter;

/**
 * @author <a href="mailto:andreas@manticore-projects.com">Andreas Reichel</a>
 * @see  <a href="https://docs.oracle.com/cd/B12037_01/server.101/b10759/statements_2013.htm">ALTER SESSION</a>
 */
public enum AlterSystemOperation {

    ARCHIVE_LOG("ARCHIVE LOG"),
    CHECKPOINT("CHECKPOINT"),
    CHECK_DATAFILES("CHECK DATAFILES"),
    DUMP_ACTIVE_SESSION_HISTORY("DUMP ACTIVE SESSION HISTORY"),
    ENABLE_DISTRIBUTED_RECOVERY("ENABLE DISTRIBUTED RECOVERY"),
    DISABLE_DISTRIBUTED_RECOVERY("DISABLE DISTRIBUTED RECOVERY"),
    ENABLE_RESTRICTED_SESSION("ENABLE RESTRICTED SESSION"),
    DISABLE_RESTRICTED_SESSION("DISABLE RESTRICTED SESSION"),
    FLUSH("FLUSH"),
    DISCONNECT_SESSION("DISCONNECT SESSION"),
    KILL_SESSION("KILL SESSION"),
    SWITCH("SWITCH"),
    SUSPEND("SUSPEND"),
    RESUME("RESUME"),
    QUIESCE("QUIESCE RESTRICTED"),
    UNQUIESCE("UNQUIESCE"),
    SHUTDOWN("SHUTDOWN"),
    REGISTER("REGISTER"),
    SET("SET"),
    RESET("RESET");

    private final String label;

    AlterSystemOperation(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}

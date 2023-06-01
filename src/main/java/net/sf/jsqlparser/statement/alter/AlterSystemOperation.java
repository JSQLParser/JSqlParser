/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */

package net.sf.jsqlparser.statement.alter;

/**
 *
 * @author <a href="mailto:andreas@manticore-projects.com">Andreas Reichel</a>
 * @see <a href="https://docs.oracle.com/cd/B12037_01/server.101/b10759/statements_2013.htm">ALTER
 *      SESSION</a>
 */

public enum AlterSystemOperation {
    ARCHIVE_LOG("ARCHIVE LOG"), CHECKPOINT("CHECKPOINT"), CHECK_DATAFILES(
            "CHECK DATAFILES"), DUMP_ACTIVE_SESSION_HISTORY(
                    "DUMP ACTIVE SESSION HISTORY"), ENABLE_DISTRIBUTED_RECOVERY(
                            "ENABLE DISTRIBUTED RECOVERY"), DISABLE_DISTRIBUTED_RECOVERY(
                                    "DISABLE DISTRIBUTED RECOVERY"), ENABLE_RESTRICTED_SESSION(
                                            "ENABLE RESTRICTED SESSION"), DISABLE_RESTRICTED_SESSION(
                                                    "DISABLE RESTRICTED SESSION"), FLUSH(
                                                            "FLUSH"), DISCONNECT_SESSION(
                                                                    "DISCONNECT SESSION"), KILL_SESSION(
                                                                            "KILL SESSION"), SWITCH(
                                                                                    "SWITCH"), SUSPEND(
                                                                                            "SUSPEND"), RESUME(
                                                                                                    "RESUME"), QUIESCE(
                                                                                                            "QUIESCE RESTRICTED"), UNQUIESCE(
                                                                                                                    "UNQUIESCE"), SHUTDOWN(
                                                                                                                            "SHUTDOWN"), REGISTER(
                                                                                                                                    "REGISTER"), SET(
                                                                                                                                            "SET"), RESET(
                                                                                                                                                    "RESET");

    private final String label;

    AlterSystemOperation(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

    public static AlterSystemOperation from(String operation) {
        // We can't use Enum.valueOf() since there White Space involved
        for (AlterSystemOperation alterSystemOperation : values()) {
            if (alterSystemOperation.toString().equals(operation)) {
                return alterSystemOperation;
            }
        }
        return null;
    }
}

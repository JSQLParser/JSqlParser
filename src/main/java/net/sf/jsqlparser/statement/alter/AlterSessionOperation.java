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
 * @author are
 */
public enum AlterSessionOperation {
    ADVISE_COMMIT, ADVISE_ROLLBACK, ADVISE_NOTHING, CLOSE_DATABASE_LINK, ENABLE_COMMIT_IN_PROCEDURE, DISABLE_COMMIT_IN_PROCEDURE, ENABLE_GUARD, DISABLE_GUARD, ENABLE_PARALLEL_DML, DISABLE_PARALLEL_DML, FORCE_PARALLEL_DML, ENABLE_PARALLEL_DDL, DISABLE_PARALLEL_DDL, FORCE_PARALLEL_DDL, ENABLE_PARALLEL_QUERY, DISABLE_PARALLEL_QUERY, FORCE_PARALLEL_QUERY, ENABLE_RESUMABLE, DISABLE_RESUMABLE, SET;

    public static AlterSessionOperation from(String operation) {
        return Enum.valueOf(AlterSessionOperation.class, operation.toUpperCase());
    }
}

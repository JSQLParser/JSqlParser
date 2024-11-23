/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

public enum AlterOperation {
    ADD, ALTER, DROP, DROP_PRIMARY_KEY, DROP_UNIQUE, DROP_FOREIGN_KEY, MODIFY, CHANGE, CONVERT, ALGORITHM, RENAME, RENAME_TABLE, RENAME_INDEX, RENAME_KEY, RENAME_CONSTRAINT, COMMENT, COMMENT_WITH_EQUAL_SIGN, UNSPECIFIC, ADD_PARTITION, DROP_PARTITION, TRUNCATE_PARTITION, SET_TABLE_OPTION, LOCK;

    public static AlterOperation from(String operation) {
        return Enum.valueOf(AlterOperation.class, operation.toUpperCase());
    }
}

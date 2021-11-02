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
    ADD, ALTER, DROP, DROP_PRIMARY_KEY, DROP_UNIQUE, DROP_FOREIGN_KEY, MODIFY, CHANGE, ALGORITHM, RENAME, RENAME_TABLE, COMMENT, UNSPECIFIC;
}

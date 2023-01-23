/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2023 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.upsert;

public enum UpsertType {
    UPSERT
    , REPLACE
    , REPLACE_SET
    , INSERT_OR_ABORT
    , INSERT_OR_FAIL
    , INSERT_OR_IGNORE
    , INSERT_OR_REPLACE
    , INSERT_OR_ROLLBACK
}

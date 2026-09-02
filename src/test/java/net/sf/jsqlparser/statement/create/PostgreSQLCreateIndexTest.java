/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import org.junit.jupiter.api.Test;

/** PostgreSQL {@code CREATE INDEX} syntax verified against PostgreSQL 18.6. */
public class PostgreSQLCreateIndexTest {

    @Test
    public void testCreateIndexConcurrently() throws JSQLParserException {
        String sql = "CREATE UNIQUE INDEX CONCURRENTLY IF NOT EXISTS pg_idx_email "
                + "ON pg_index_test USING btree (email)";

        CreateIndex createIndex = (CreateIndex) assertSqlCanBeParsedAndDeparsed(sql);

        assertTrue(createIndex.isConcurrently());
        assertTrue(createIndex.isUsingIfNotExists());
        assertFalse(createIndex.isOnly());
        assertEquals("pg_idx_email", createIndex.getIndex().getName());
    }

    @Test
    public void testCreateIndexWithOmittedNameAndOnly() throws JSQLParserException {
        String sql = "CREATE INDEX ON ONLY pg_index_test USING btree (id DESC)";

        CreateIndex createIndex = (CreateIndex) assertSqlCanBeParsedAndDeparsed(sql);

        assertNull(createIndex.getIndex().getName());
        assertTrue(createIndex.isOnly());
        assertFalse(createIndex.isConcurrently());
    }

    @Test
    public void testCreateNamedIndexOnOnlyTable() throws JSQLParserException {
        String sql = "CREATE INDEX pg_idx_only ON ONLY pg_index_test (id)";

        CreateIndex createIndex = (CreateIndex) assertSqlCanBeParsedAndDeparsed(sql);

        assertEquals("pg_idx_only", createIndex.getIndex().getName());
        assertTrue(createIndex.isOnly());
    }
}

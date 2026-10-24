/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.truncate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.CascadeBehavior;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class OracleTruncateTest {
    @ParameterizedTest
    @ValueSource(strings = {"samples", "hr.\"Sample.Table\""})
    void preservesLogStorageAndCascadeOptions(String table) throws JSQLParserException {
        for (Truncate.MaterializedViewLogOption log : new Truncate.MaterializedViewLogOption[] {
                null, Truncate.MaterializedViewLogOption.PRESERVE,
                Truncate.MaterializedViewLogOption.PURGE}) {
            for (Truncate.StorageOption storage : new Truncate.StorageOption[] {null,
                    Truncate.StorageOption.DROP, Truncate.StorageOption.DROP_ALL,
                    Truncate.StorageOption.REUSE}) {
                for (String cascade : new String[] {"", " CASCADE"}) {
                    String sql = "TRUNCATE TABLE " + table
                            + (log == null ? "" : " " + log + " MATERIALIZED VIEW LOG")
                            + (storage == null ? ""
                                    : storage == Truncate.StorageOption.DROP_ALL
                                            ? " DROP ALL STORAGE"
                                            : " " + storage + " STORAGE")
                            + cascade;
                    for (Dialect dialect : new Dialect[] {null, Dialect.ORACLE}) {
                        Truncate truncate = parse(sql, dialect);
                        assertEquals(table, truncate.getTable().getFullyQualifiedName());
                        assertEquals(log, truncate.getMaterializedViewLogOption());
                        assertEquals(storage, truncate.getStorageOption());
                        assertEquals(cascade.isEmpty() ? null : CascadeBehavior.CASCADE,
                                truncate.getDropBehavior());
                        assertNull(truncate.getIdentityOption());
                        assertEquals(sql, truncate.toString());
                        roundTrip(truncate, dialect);
                        assertEquals(2,
                                CCJSqlParserUtil.parseStatements(sql + "; SELECT 1").size());
                    }
                }
            }
        }
    }

    @Test
    void supportsModelConstructionAndClearingOptions() throws JSQLParserException {
        Truncate truncate = new Truncate().withTableToken(true).withTable(new Table("samples"))
                .withMaterializedViewLogOption(Truncate.MaterializedViewLogOption.PURGE)
                .withStorageOption(Truncate.StorageOption.DROP_ALL).withCascade(true);
        assertEquals("TRUNCATE TABLE samples PURGE MATERIALIZED VIEW LOG DROP ALL STORAGE CASCADE",
                truncate.toString());
        roundTrip(truncate, Dialect.ORACLE);
        truncate.setMaterializedViewLogOption(Truncate.MaterializedViewLogOption.PRESERVE);
        truncate.setStorageOption(Truncate.StorageOption.REUSE);
        assertEquals("TRUNCATE TABLE samples PRESERVE MATERIALIZED VIEW LOG REUSE STORAGE CASCADE",
                truncate.toString());
        roundTrip(truncate, null);
        truncate.setMaterializedViewLogOption(null);
        truncate.setStorageOption(null);
        assertEquals("TRUNCATE TABLE samples CASCADE", truncate.toString());
        roundTrip(truncate, null);
    }

    @ParameterizedTest
    @ValueSource(strings = {"DROP", "DROP ALL", "REUSE", "PRESERVE MATERIALIZED VIEW",
            "PURGE LOG", "DROP STORAGE REUSE STORAGE",
            "REUSE STORAGE PURGE MATERIALIZED VIEW LOG", "CASCADE REUSE STORAGE"})
    void rejectsIncompleteRepeatedAndOutOfOrderOptions(String options) {
        assertThrows(JSQLParserException.class,
                () -> parse("TRUNCATE TABLE samples " + options, Dialect.ORACLE));
    }

    private static Truncate parse(String sql, Dialect dialect) throws JSQLParserException {
        return (Truncate) CCJSqlParserUtil.parse(sql, parser -> {
            if (dialect != null) {
                parser.withDialect(dialect);
            }
        });
    }

    private static void roundTrip(Truncate truncate, Dialect dialect) throws JSQLParserException {
        StringBuilder output = new StringBuilder();
        truncate.accept(new StatementDeParser(output), null);
        assertEquals(truncate.toString(), output.toString());
        Truncate reparsed = parse(output.toString(), dialect);
        assertEquals(truncate.getMaterializedViewLogOption(),
                reparsed.getMaterializedViewLogOption());
        assertEquals(truncate.getStorageOption(), reparsed.getStorageOption());
        assertEquals(output.toString(), reparsed.toString());
    }
}

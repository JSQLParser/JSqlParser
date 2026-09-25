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

import static org.junit.jupiter.api.Assertions.*;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.sequence.AlterSequence;
import net.sf.jsqlparser.statement.create.sequence.CreateSequence;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlSequenceDdlTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE SEQUENCE IF NOT EXISTS s AS INTEGER START WITH 3",
            "CREATE TEMP SEQUENCE s AS INTEGER START WITH 3",
            "CREATE TEMP SEQUENCE IF NOT EXISTS s AS INTEGER START WITH 3",
            "CREATE TEMPORARY SEQUENCE s AS INTEGER START WITH 3",
            "CREATE TEMPORARY SEQUENCE IF NOT EXISTS s AS INTEGER START WITH 3",
            "CREATE UNLOGGED SEQUENCE s AS INTEGER START WITH 3",
            "CREATE UNLOGGED SEQUENCE IF NOT EXISTS s AS INTEGER START WITH 3",
            "CREATE SEQUENCE s OWNED BY t.id CACHE 5",
            "ALTER SEQUENCE s AS SMALLINT",
            "ALTER SEQUENCE s OWNED BY t.id INCREMENT BY 3",
            "ALTER SEQUENCE s SET UNLOGGED",
            "ALTER SEQUENCE s SET LOGGED",
            "ALTER SEQUENCE s OWNER TO CURRENT_USER",
            "ALTER SEQUENCE s RENAME TO s2",
            "ALTER SEQUENCE s SET SCHEMA ddl_aux",
            "ALTER SEQUENCE IF EXISTS s AS SMALLINT",
            "ALTER SEQUENCE IF EXISTS s RESTART",
            "ALTER SEQUENCE IF EXISTS s RESTART WITH 7",
            "ALTER SEQUENCE IF EXISTS s INCREMENT BY 2",
            "ALTER SEQUENCE IF EXISTS s MINVALUE -5 MAXVALUE 100",
            "ALTER SEQUENCE IF EXISTS s CACHE 3 NO CYCLE",
            "ALTER SEQUENCE IF EXISTS s OWNED BY t.id",
            "ALTER SEQUENCE IF EXISTS s OWNED BY t.id INCREMENT BY 3",
            "ALTER SEQUENCE IF EXISTS s SET UNLOGGED",
            "ALTER SEQUENCE IF EXISTS s SET LOGGED",
            "ALTER SEQUENCE IF EXISTS s OWNER TO CURRENT_USER",
            "ALTER SEQUENCE IF EXISTS s RENAME TO s2",
            "ALTER SEQUENCE IF EXISTS s SET SCHEMA ddl_aux",
            "CREATE SEQUENCE IF NOT EXISTS sequence_test",
            "CREATE TEMP SEQUENCE myseq2",
            "CREATE TEMP SEQUENCE myseq3",
            "CREATE UNLOGGED SEQUENCE sequence_test_unlogged",
            "CREATE TEMPORARY SEQUENCE sequence_test_temp1"})
    void postgresFormsRoundTrip(String sql) throws JSQLParserException {
        Statement statement = parse(sql);
        assertTrue(statement instanceof CreateSequence || statement instanceof AlterSequence);
        assertRoundTrip(statement);
        assertEquals(2, CCJSqlParserUtil.parseStatements(sql + "; SELECT 1",
                p -> p.withDialect(Dialect.POSTGRESQL)).size());
    }

    @Test
    void createOptionsCanBeModifiedWithoutStaleTokens() throws JSQLParserException {
        CreateSequence statement = (CreateSequence) parse(
                "CREATE TEMP SEQUENCE IF NOT EXISTS s OWNED BY t.id CACHE 5 AS INTEGER");
        assertTrue(statement.isIfNotExists());
        assertEquals(CreateSequence.Persistence.TEMP, statement.getPersistence());
        assertEquals("t.id", statement.getSequence().getOwnership().getColumn().toString());
        assertEquals("INTEGER", statement.getSequence().getDataType());
        statement.setPersistence(CreateSequence.Persistence.UNLOGGED);
        statement.setIfNotExists(false);
        statement.getSequence().setName("renamed");
        statement.getSequence().setOwnership(null);
        statement.getSequence().getParameters().get(0).setValue(10L);
        assertEquals("CREATE UNLOGGED SEQUENCE renamed AS INTEGER CACHE 10", statement.toString());
        assertRoundTrip(statement);
    }

    @Test
    void alterOptionsAndActionsRemainSeparate() throws JSQLParserException {
        AlterSequence statement = (AlterSequence) parse(
                "ALTER SEQUENCE IF EXISTS s OWNED BY t.id INCREMENT BY 3 AS SMALLINT");
        assertTrue(statement.isIfExists());
        assertEquals(AlterSequence.Action.PARAMETERS, statement.getAction());
        assertEquals("SMALLINT", statement.getSequence().getDataType());
        assertEquals(3L, statement.getSequence().getParameters().get(0).getValue());
        statement.setAction(AlterSequence.Action.RENAME);
        statement.setNewName("new_s");
        assertEquals("ALTER SEQUENCE IF EXISTS s RENAME TO new_s", statement.toString());
        assertRoundTrip(statement);
    }

    @ParameterizedTest
    @ValueSource(strings = {"LOCAL TEMP", "LOCAL TEMPORARY", "GLOBAL TEMP", "GLOBAL TEMPORARY"})
    void scopedTemporarySequence(String prefix) throws JSQLParserException {
        String sql = "CREATE " + prefix + " SEQUENCE s";
        assertEquals(sql, parse(sql).toString());
    }

    @Test
    void duplicateScalarOptionsFail() {
        assertThrows(JSQLParserException.class,
                () -> parse("CREATE SEQUENCE s AS INT AS BIGINT"));
        assertThrows(JSQLParserException.class,
                () -> parse("ALTER SEQUENCE s OWNED BY NONE OWNED BY t.id"));
    }

    private static Statement parse(String sql) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.POSTGRESQL));
    }

    private static void assertRoundTrip(Statement statement) throws JSQLParserException {
        StringBuilder sql = new StringBuilder();
        statement.accept(new StatementDeParser(sql), null);
        assertEquals(statement.toString(), sql.toString());
        assertEquals(statement.toString(), parse(sql.toString()).toString());
    }
}

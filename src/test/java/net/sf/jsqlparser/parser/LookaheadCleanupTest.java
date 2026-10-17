/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser;

import static org.junit.jupiter.api.Assertions.*;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.statement.ConnectionDefinition;
import net.sf.jsqlparser.statement.CloudConnectionDefinition;
import net.sf.jsqlparser.statement.export.DBMSDestination;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LookaheadCleanupTest {
    @ParameterizedTest
    @ValueSource(strings = {"EXA AT conn TABLE t", "ORA AT conn TABLE schemaName.tableName",
            "JDBC DRIVER = 'driverName' AT conn TABLE \"t\"",
            "EXA AT conn TABLE t (c1, c2) REPLACE TRUNCATE"})
    void dispatchesTableDestinationsWithCompleteConsumption(String sql) throws Exception {
        CCJSqlParser parser = CCJSqlParserUtil.newParser(sql);
        DBMSDestination destination = parser.DBMSDestination();
        assertNotNull(destination.getTable());
        assertNull(destination.getStatement());
        assertNotNull(destination.getConnectionDefinition());
        assertEquals(CCJSqlParserConstants.EOF, parser.getToken(1).kind);
        assertSqlCanBeParsedAndDeparsed("EXPORT sourceTable INTO " + sql, true,
                p -> p.withDialect(Dialect.EXASOL));
    }

    @ParameterizedTest
    @ValueSource(strings = {"EXA AT conn STATEMENT 'insert into t values (?)'",
            "ORA AT conn STATEMENT 'insert into t values (?)'"})
    void preservesStatementDestinations(String sql) throws Exception {
        CCJSqlParser parser = CCJSqlParserUtil.newParser(sql);
        DBMSDestination destination = parser.DBMSDestination();
        assertNull(destination.getTable());
        assertEquals("insert into t values (?)", destination.getStatement().getValue());
        assertEquals(CCJSqlParserConstants.EOF, parser.getToken(1).kind);
        assertSqlCanBeParsedAndDeparsed("EXPORT sourceTable INTO " + sql, true,
                p -> p.withDialect(Dialect.EXASOL));
    }

    @ParameterizedTest
    @ValueSource(strings = {"EXA AT conn TABLE", "ORA AT conn STATEMENT", "EXA AT conn SELECT t"})
    void rejectsIncompleteAndWrongDestinationPrefixes(String sql) {
        CCJSqlParser parser = CCJSqlParserUtil.newParser(sql);
        assertThrows(ParseException.class, parser::DBMSDestination);
        if (sql.endsWith("TABLE")) {
            assertEquals(CCJSqlParserConstants.K_TABLE, parser.getToken(0).kind);
            assertEquals(CCJSqlParserConstants.EOF, parser.getToken(1).kind);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"AT conn", "AT '127.0.0.1'", "AT CLOUD NONE conn",
            "AT CLOUD AZURE BLOBSTORAGE conn"})
    void preservesCloudAndOrdinaryConnections(String sql) throws Exception {
        CCJSqlParser parser = CCJSqlParserUtil.newParser(sql);
        ConnectionDefinition connection = parser.ConnectionOrCloudConnectionDefinition();
        if (sql.startsWith("AT CLOUD")) {
            assertInstanceOf(CloudConnectionDefinition.class, connection);
        } else {
            assertEquals(ConnectionDefinition.class, connection.getClass());
        }
        assertEquals(CCJSqlParserConstants.EOF, parser.getToken(1).kind);
        assertSqlCanBeParsedAndDeparsed("EXPORT sourceTable INTO CSV " + sql + " FILE 'file.csv'",
                true, p -> p.withDialect(Dialect.EXASOL));
    }
}

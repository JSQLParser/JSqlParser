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
import org.junit.jupiter.api.Test;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.MySqlSelectIntoClause;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.lock.LockStatement;
import net.sf.jsqlparser.statement.create.rule.CreateRule;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.notify.NotifyStatement;
import net.sf.jsqlparser.expression.LongValue;
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

    @ParameterizedTest
    @ValueSource(strings = {"SELECT id FROM t LIMIT 7 OFFSET 2",
            "SELECT id FROM t UNION ALL SELECT id FROM u LIMIT 7 OFFSET 2"})
    void boundedSelectTailsKeepTheirAstAndFollowingStatement(String sql) throws Exception {
        Select select = (Select) complete(sql);
        assertEquals(7, ((LongValue) select.getLimit().getRowCount()).getValue());
        assertEquals(2, ((LongValue) select.getOffset().getOffset()).getValue());
        assertEquals("SELECT 42", CCJSqlParserUtil.parseStatements(sql + "; SELECT 42").get(1)
                .toString());
    }

    @Test
    void boundedFetchKeepsItsCount() throws Exception {
        Select select = (Select) complete("SELECT id FROM t OFFSET 2 ROWS FETCH NEXT 7 ROWS ONLY");
        assertEquals(2, ((LongValue) select.getOffset().getOffset()).getValue());
        assertEquals(7, ((LongValue) select.getFetch().getExpression()).getValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"SELECT a INTO OUTFILE '/tmp/result.txt' FIELDS TERMINATED BY ',' "
            + "OPTIONALLY ENCLOSED BY '\"' LINES TERMINATED BY 'end' FROM t",
            "SELECT a FROM t INTO OUTFILE '/tmp/result.txt' COLUMNS TERMINATED BY ',' "
                    + "OPTIONALLY ENCLOSED BY '\"' LINES TERMINATED BY 'end'"})
    void boundedOutfileOptionsKeepTheirOrderingAndValues(String sql) throws Exception {
        MySqlSelectIntoClause into = ((Select) complete(sql)).getPlainSelect()
                .getMySqlSelectIntoClause();
        assertEquals(MySqlSelectIntoClause.Type.OUTFILE, into.getType());
        assertEquals("/tmp/result.txt", into.getFileName().getValue());
        assertEquals(",", into.getFieldsTerminatedBy().getValue());
        assertTrue(into.isFieldsOptionallyEnclosed());
        assertEquals("end", into.getLinesTerminatedBy().getValue());
    }

    @Test
    void boundedParenthesesKeepExecutionArgumentsAndRuleActions() throws Exception {
        Execute execute = (Execute) complete("CALL p (:a, :b)");
        assertEquals("p", execute.getName());
        assertEquals(2, execute.getExprList().size());
        assertEquals(":a", execute.getExprList().get(0).toString());
        assertEquals(":b", execute.getExprList().get(1).toString());
        CreateRule rule = (CreateRule) complete("CREATE RULE r AS ON INSERT TO t DO "
                + "(INSERT INTO log VALUES (9); NOTIFY ch, 'changed')");
        assertEquals(2, rule.getActions().size());
        assertEquals("log", ((Insert) rule.getActions().get(0)).getTable().getName());
        assertEquals("changed",
                ((NotifyStatement) rule.getActions().get(1)).getPayload().getValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"LOCK TABLE a IN SHARE MODE WAIT 5", "LOCK a IN SHARE MODE WAIT 5"})
    void boundedTableKeywordKeepsLockTargetAndWait(String sql) throws Exception {
        LockStatement lock = (LockStatement) complete(sql);
        assertEquals(sql.startsWith("LOCK TABLE"), lock.isUseTableKeyword());
        assertEquals("a", lock.getTable().getName());
        assertEquals("SHARE", lock.getLockMode().getValue());
        assertEquals(5L, lock.getWaitSeconds());
    }

    @ParameterizedTest
    @ValueSource(strings = {"SELECT id FROM t LIMIT", "SELECT id FROM t OFFSET",
            "SELECT id FROM t FETCH NEXT", "CALL p (", "CALL p (:a,)", "LOCK TABLE",
            "CREATE RULE r AS ON INSERT TO t DO (NOTIFY ch", "SELECT a FROM t INTO OUTFILE 'x' "
                    + "LINES TERMINATED BY 'end' FIELDS TERMINATED BY ','"})
    void boundedGuardsRejectIncompleteAndReorderedNeighbors(String sql) {
        assertThrows(net.sf.jsqlparser.JSQLParserException.class,
                () -> CCJSqlParserUtil.parse(sql));
    }

    private static Statement complete(String sql) throws Exception {
        CCJSqlParser parser = CCJSqlParserUtil.newParser(sql);
        Statement statement = parser.Statement();
        assertEquals(CCJSqlParserConstants.EOF, parser.getToken(1).kind);
        assertEquals(statement.toString(), CCJSqlParserUtil.parse(statement.toString()).toString());
        return statement;
    }

}

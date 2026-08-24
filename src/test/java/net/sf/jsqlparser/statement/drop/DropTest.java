/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.drop;

import java.io.StringReader;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import static net.sf.jsqlparser.test.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class DropTest {

    private final CCJSqlParserManager parserManager = new CCJSqlParserManager();

    @Test
    public void testDrop() throws JSQLParserException {
        String statement = "DROP TABLE mytab";
        Drop parsed = (Drop) parserManager.parse(new StringReader(statement));
        assertEquals("TABLE", parsed.getType());
        assertEquals("mytab", parsed.getName().getFullyQualifiedName());
        assertEquals(statement, "" + parsed);
        Drop created = new Drop().withType("TABLE").withName(new Table("mytab"));
        assertDeparse(created, statement);
        assertEqualsObjectTree(parsed, created);
    }

    @Test
    public void testDropIndex() throws JSQLParserException {
        String statement = "DROP INDEX myindex CASCADE";
        Drop parsed = (Drop) parserManager.parse(new StringReader(statement));
        assertEquals("INDEX", parsed.getType());
        assertEquals("myindex", parsed.getName().getFullyQualifiedName());
        assertEquals("CASCADE", parsed.getParameters().get(0));
        assertEquals(statement, "" + parsed);
        Drop created = new Drop().withType("INDEX").withName(new Table("myindex"))
                .addParameters("CASCADE");
        assertDeparse(created, statement);
        assertEqualsObjectTree(parsed, created);
    }

    @Test
    public void testDropIndexOnTable() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("DROP INDEX idx ON abc");
    }

    @Test
    public void testDropIndexOnQualifiedTable() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("DROP INDEX idx ON qual.tbl");
    }

    @Test
    public void testDropIndexOnDoubleQualifiedTable() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("DROP INDEX idx ON dbl.qual.tbl");
    }

    @Test
    public void testDrop2() throws JSQLParserException {
        Drop drop = (Drop) parserManager.parse(new StringReader("DROP TABLE \"testtable\""));
        assertEquals("TABLE", drop.getType());
        assertEquals("\"testtable\"", drop.getName().getFullyQualifiedName());
    }

    @Test
    public void testDropIfExists() throws JSQLParserException {
        String statement = "DROP TABLE IF EXISTS my_table";
        Statement parsed = assertSqlCanBeParsedAndDeparsed(statement);
        Drop created =
                new Drop().withType("TABLE").withIfExists(true).withName(new Table("my_table"));
        assertDeparse(created, statement);
        assertEqualsObjectTree(parsed, created);
    }

    @Test
    public void testDropRestrictIssue510() throws JSQLParserException {
        String statement = "DROP TABLE TABLE2 RESTRICT";
        Statement parsed = assertSqlCanBeParsedAndDeparsed(statement);
        Drop created = new Drop().withType("TABLE").withName(new Table("TABLE2"))
                .addParameters(asList("RESTRICT"));
        assertDeparse(created, statement);
        assertEqualsObjectTree(parsed, created);
    }

    @Test
    public void testDropViewIssue545() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("DROP VIEW myview");
    }

    @Test
    public void testDropViewIssue545_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("DROP VIEW IF EXISTS myview");
    }

    @Test
    public void testDropMaterializedView() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("DROP MATERIALIZED VIEW myview");
    }

    @Test
    public void testDropSchemaIssue855() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("DROP SCHEMA myschema");
        assertSqlCanBeParsedAndDeparsed("DROP SCHEMA unnamed.myschema");
    }

    @Test
    public void testDropSequence() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("DROP SEQUENCE mysequence");
    }

    @Test
    public void testOracleMultiColumnDrop() throws JSQLParserException {
        // assertSqlCanBeParsedAndDeparsed("ALTER TABLE foo DROP (bar, baz)");
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE foo DROP (bar, baz) CASCADE");
    }

    @Test
    public void testUniqueFunctionDrop() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("DROP FUNCTION myFunc");
    }

    @Test
    public void testZeroArgDropFunction() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("DROP FUNCTION myFunc()");
    }

    @Test
    public void testDropFunctionWithSimpleType() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("DROP FUNCTION myFunc(integer, varchar)");
    }

    @Test
    public void testDropFunctionWithNameAndType() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("DROP FUNCTION myFunc(amount integer, name varchar)");
    }

    @Test
    public void testDropFunctionWithNameAndParameterizedType() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("DROP FUNCTION myFunc(amount integer, name varchar(255))");
    }

    @Test
    void dropTemporaryTableTestIssue1712() throws JSQLParserException {
        String sqlStr = "drop temporary table if exists tmp_MwYT8N0z";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    public void testDropIndexAlgorithmAndLockOptionsIssue2490() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("DROP INDEX i15 ON t ALGORITHM = INPLACE LOCK = NONE");
        assertSqlCanBeParsedAndDeparsed("DROP INDEX i16 ON t ALGORITHM INPLACE");
        assertSqlCanBeParsedAndDeparsed("DROP INDEX i17 ON t LOCK = NONE");
    }

    @Test
    public void testDropTableFollowedByLockTableIssue2490() throws JSQLParserException {
        // LOCK must not be swallowed as a DROP argument when it starts the next statement.
        Statements statements = CCJSqlParserUtil.parseStatements(
                "DROP TABLE t1; LOCK TABLE t2 IN SHARE MODE;");
        assertEquals(2, statements.size());
    }
}

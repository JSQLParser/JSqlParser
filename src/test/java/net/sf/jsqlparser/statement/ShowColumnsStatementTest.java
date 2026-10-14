/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.show.ShowTablesStatement.SelectionMode;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 *
 * @author oshai
 */
public class ShowColumnsStatementTest {

    private static final List<Consumer<CCJSqlParser>> DIALECTS = List.of(parser -> {
    }, parser -> parser.withDialect(Dialect.MYSQL),
            parser -> parser.withDialect(Dialect.MARIADB));

    /** Parses without a dialect and with MySQL and MariaDB, then deparses and reparses. */
    private static ShowColumnsStatement roundTrip(String sql) throws JSQLParserException {
        ShowColumnsStatement result = null;
        for (Consumer<CCJSqlParser> dialect : DIALECTS) {
            Statement statement = assertSqlCanBeParsedAndDeparsed(sql, false, dialect);
            assertInstanceOf(ShowColumnsStatement.class, statement);
            assertDeparse(statement, sql);
            assertEquals(sql, CCJSqlParserUtil.parse(statement.toString(), dialect).toString());
            result = (ShowColumnsStatement) statement;
        }
        return result;
    }

    @Test
    public void testSimpleUse() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SHOW COLUMNS FROM mydatabase");
    }

    @Test
    void plainFormKeepsTheTableNameApi() throws JSQLParserException {
        ShowColumnsStatement show = roundTrip("SHOW COLUMNS FROM mytable");

        assertEquals("mytable", show.getTableName());
        assertEquals("mytable", show.getTable().getName());
        assertFalse(show.isExtended());
        assertFalse(show.isFull());
        assertFalse(show.isFieldsKeyword());
        assertEquals(SelectionMode.FROM, show.getTableSelectionMode());
        assertNull(show.getDbName());
        assertNull(show.getLikeExpression());
        assertNull(show.getWhereCondition());
    }

    @Test
    void fullColumnsWithDatabaseAndLike() throws JSQLParserException {
        ShowColumnsStatement show = roundTrip("SHOW FULL COLUMNS FROM gc20 FROM v LIKE 'n%'");

        assertTrue(show.isFull());
        assertEquals("gc20", show.getTableName());
        assertEquals(SelectionMode.FROM, show.getDbSelectionMode());
        assertEquals("v", show.getDbName());
        assertEquals("n%", ((StringValue) show.getLikeExpression()).getValue());
    }

    @Test
    void extendedFullFieldsInWithWhere() throws JSQLParserException {
        ShowColumnsStatement show =
                roundTrip("SHOW EXTENDED FULL FIELDS IN db.t IN `other` WHERE `Key` = 'PRI'");

        assertTrue(show.isExtended());
        assertTrue(show.isFull());
        assertTrue(show.isFieldsKeyword());
        assertEquals(SelectionMode.IN, show.getTableSelectionMode());
        assertEquals("db", show.getTable().getSchemaName());
        assertEquals("db.t", show.getTableName());
        assertEquals(SelectionMode.IN, show.getDbSelectionMode());
        assertEquals("`other`", show.getDbName());
        assertEquals("`Key` = 'PRI'", show.getWhereCondition().toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SHOW FIELDS FROM t",
            "SHOW FULL FIELDS FROM gc20",
            "SHOW COLUMNS IN t",
            "SHOW COLUMNS FROM `db`.`t`",
            "SHOW COLUMNS FROM t IN db",
            "SHOW EXTENDED COLUMNS FROM t",
            "SHOW COLUMNS FROM t LIKE 'a%'",
            "SHOW FULL COLUMNS FROM t WHERE Comment <> ''"
    })
    void parsesEveryMySqlForm(String sql) throws JSQLParserException {
        roundTrip(sql);
    }

    @Test
    void keywordsAreCaseInsensitive() throws JSQLParserException {
        assertEquals("SHOW FULL FIELDS IN t FROM db",
                CCJSqlParserUtil.parse("show full fields in t from db").toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SHOW COLUMNS IN SCHEMA s",
            "SHOW COLUMNS IN TABLE t",
            "SHOW COLUMNS LIKE 'x' IN TABLE t",
            "SHOW COLUMNS FROM t NOT LIKE 'x'",
            "SHOW COLUMNS FROM t FROM db LIMIT 3",
            "SHOW FULL PROCESSLIST"
    })
    void otherDialectFormsRemainUnsupported(String sql) throws JSQLParserException {
        Statement statement = assertSqlCanBeParsedAndDeparsed(sql, true);
        assertInstanceOf(UnsupportedStatement.class, statement);
    }

    @Test
    void endsAtTheStatementBoundary() throws JSQLParserException {
        Statements statements =
                CCJSqlParserUtil.parseStatements("SHOW FULL COLUMNS FROM t;\nSELECT 1");

        assertEquals(2, statements.size());
        assertInstanceOf(ShowColumnsStatement.class, statements.get(0));

        IfElseStatement ifElse = (IfElseStatement) CCJSqlParserUtil
                .parse("IF a = 1 SHOW COLUMNS FROM t ELSE SHOW TABLE STATUS");
        assertInstanceOf(ShowColumnsStatement.class, ifElse.getIfStatement());
        assertEquals("SHOW TABLE STATUS", ifElse.getElseStatement().toString());
    }

    @Test
    void findsTheTable() throws JSQLParserException {
        assertThat(TablesNamesFinder.findTables("SHOW FULL COLUMNS FROM s.t"))
                .containsExactly("s.t");
        // the trailing FROM db names the database of the table
        assertThat(TablesNamesFinder.findTables("SHOW COLUMNS FROM t FROM db"))
                .containsExactly("db.t");
    }

    @Test
    void legacyTableNameAccessors() {
        ShowColumnsStatement show = new ShowColumnsStatement("db.t");

        assertEquals("db", show.getTable().getSchemaName());
        assertEquals("db.t", show.getTableName());
        assertEquals("SHOW COLUMNS FROM db.t", show.toString());
        assertEquals("SHOW COLUMNS FROM t2", show.withTableName("t2").toString());
        show.setTableName(null);
        assertNull(show.getTable());
    }

    @Test
    void buildsAndDeparses() {
        ShowColumnsStatement show = new ShowColumnsStatement(new Table("t")).withFull(true)
                .withFieldsKeyword(true).withTableSelectionMode(SelectionMode.IN)
                .withDbName("db").withLikeExpression(new StringValue("a%"));

        assertDeparse(show, "SHOW FULL FIELDS IN t FROM db LIKE 'a%'");
    }

    @Test
    void reportsMetadataFeatures() throws JSQLParserException {
        StatementFeatures features =
                CCJSqlParserUtil.parse("SHOW FULL COLUMNS FROM t").getFeatures();

        assertThat(features.getCertain()).containsExactly(StmtFeature.RETURNS_RESULT_SET);
    }
}

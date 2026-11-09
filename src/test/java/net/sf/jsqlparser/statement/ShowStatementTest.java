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

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.show.ShowCreateStatement;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.function.Consumer;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author oshai
 */
public class ShowStatementTest {

    private static final Consumer<CCJSqlParser> POSTGRESQL =
            parser -> parser.withDialect(Dialect.POSTGRESQL);

    @Test
    public void testSimpleUse() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SHOW mydatabase");
    }

    @Test
    public void testSimpleUse2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SHOW transaction_isolation");
    }

    @Test
    void testShowIndexesFromTable() throws JSQLParserException {
        String sqlStr =
                "show indexes from my_table";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "SHOW TIME ZONE | Time Zone",
            "SHOW TRANSACTION ISOLATION LEVEL | TRANSACTION ISOLATION LEVEL",
            "SHOW SESSION AUTHORIZATION | SESSION AUTHORIZATION",
            "show time zone | Time Zone",
            "show transaction isolation level | TRANSACTION ISOLATION LEVEL"})
    void readsPostgreSqlParameterPhrasesWithPostgreSqlAndWithoutDialect(String sql, String name)
            throws JSQLParserException {
        for (Statement statement : new Statement[] {CCJSqlParserUtil.parse(sql, POSTGRESQL),
                CCJSqlParserUtil.parse(sql)}) {
            ShowStatement show = Assertions.assertInstanceOf(ShowStatement.class, statement);
            Assertions.assertEquals(name, show.getName());
            Assertions.assertEquals("SHOW " + name, show.toString());
            assertSqlCanBeParsedAndDeparsed(show.toString(), false, POSTGRESQL);
            assertThat(new TablesNamesFinder<>().getTables(statement)).isEmpty();
            assertThat(statement.getFeatures().getCertain())
                    .containsExactly(StmtFeature.RETURNS_RESULT_SET);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"SHOW TIME ZONE x", "SHOW TRANSACTION ISOLATION",
            "SHOW TRANSACTION ISOLATION LEVEL x", "SHOW DATE ZONE",
            "SHOW SESSION AUTHORIZATION ALL"})
    void otherWordsStayUnsupported(String sql) throws JSQLParserException {
        Assertions.assertInstanceOf(UnsupportedStatement.class,
                CCJSqlParserUtil.parse(sql, POSTGRESQL));
    }

    @Test
    void otherDialectsKeepTheGenericReading() throws JSQLParserException {
        for (Dialect dialect : new Dialect[] {Dialect.MYSQL, Dialect.ORACLE, Dialect.SNOWFLAKE}) {
            for (String sql : new String[] {"SHOW TIME ZONE", "SHOW TRANSACTION ISOLATION LEVEL",
                    "SHOW SESSION AUTHORIZATION"}) {
                Assertions.assertInstanceOf(UnsupportedStatement.class,
                        CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(dialect)), sql);
            }
        }
    }

    @Test
    void parameterPhrasesEndAtStatementBoundaries() throws JSQLParserException {
        Statements statements = CCJSqlParserUtil.parseStatements(
                "SHOW TIME ZONE; SHOW SESSION AUTHORIZATION\nSELECT 1;"
                        + "SHOW TRANSACTION ISOLATION LEVEL\nDISCARD ALL",
                POSTGRESQL);
        Assertions.assertEquals(5, statements.size());
        Assertions.assertEquals("SHOW SESSION AUTHORIZATION", statements.get(1).toString());
        Assertions.assertEquals("SHOW TRANSACTION ISOLATION LEVEL", statements.get(3).toString());
        Assertions.assertInstanceOf(DiscardStatement.class, statements.get(4));
    }

    @Test
    void testShowCreateTable() throws JSQLParserException {
        String sqlStr =
                "show create table my_table";
        Statement statement = assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        Assertions.assertTrue(statement instanceof ShowCreateStatement);
    }
}

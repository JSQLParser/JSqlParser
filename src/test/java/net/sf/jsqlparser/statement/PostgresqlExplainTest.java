/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.ExplainStatement.Option;
import net.sf.jsqlparser.statement.ExplainStatement.OptionType;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.test.TestUtils;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostgresqlExplainTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "EXPLAIN SELECT 1",
            "EXPLAIN ANALYZE VERBOSE SELECT 1",
            "EXPLAIN (ANALYZE, VERBOSE, COSTS, BUFFERS) SELECT 1",
            "EXPLAIN (ANALYZE TRUE, VERBOSE ON, COSTS OFF, BUFFERS FALSE) SELECT 1",
            "EXPLAIN (ANALYZE 1, COSTS 0, FORMAT TEXT) SELECT 1",
            "EXPLAIN (FORMAT XML) SELECT 1",
            "EXPLAIN (FORMAT JSON) SELECT 1",
            "EXPLAIN (FORMAT YAML) SELECT 1",
            "EXPLAIN (ANALYZE, TIMING, SUMMARY, SETTINGS, WAL) SELECT 1",
            "EXPLAIN (GENERIC_PLAN, SERIALIZE NONE, MEMORY) SELECT 1",
            "EXPLAIN (SERIALIZE TEXT, GENERIC_PLAN FALSE) SELECT 1",
            "EXPLAIN (ANALYZE, SERIALIZE BINARY) SELECT 1",
            "EXPLAIN (SERIALIZE) SELECT 1",
            "EXPLAIN (ANALYZE OFF, ANALYZE ON) SELECT 1",
            "EXPLAIN (ANALYZE) WITH t AS (SELECT * FROM source) SELECT * FROM t",
            "EXPLAIN (ANALYZE FALSE) INSERT INTO t VALUES (1)",
            "EXPLAIN (COSTS OFF) UPDATE t SET n = 2",
            "EXPLAIN (FORMAT JSON) DELETE FROM t WHERE n = 1",
            "EXPLAIN (COSTS) VALUES (1), (2)",
            "EXPLAIN (SELECT 1)"
    })
    void roundTrip(String sql) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sql, true,
                parser -> parser.withDialect(Dialect.POSTGRESQL));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "EXPLAIN () SELECT 1",
            "EXPLAIN ANALYZE TRUE SELECT 1",
            "EXPLAIN VERBOSE TRUE SELECT 1",
            "EXPLAIN BUFFERS SELECT 1",
            "EXPLAIN COSTS SELECT 1",
            "EXPLAIN FORMAT XML SELECT 1",
            "EXPLAIN VERBOSE ANALYZE SELECT 1",
            "EXPLAIN ANALYZE ANALYZE SELECT 1",
            "EXPLAIN PLAN FOR SELECT 1",
            "EXPLAIN (FORMAT) SELECT 1",
            "EXPLAIN (FORMAT LONGTEXT) SELECT 1",
            "EXPLAIN (ANALYZE 2) SELECT 1",
            "EXPLAIN (ANALYZE MAYBE) SELECT 1",
            "EXPLAIN (SERIALIZE JSON) SELECT 1",
            "EXPLAIN (UNKNOWN_OPTION) SELECT 1",
            "EXPLAIN (ANALYZE,) SELECT 1",
            "EXPLAIN (ANALYZE VERBOSE) SELECT 1",
            "EXPLAIN some_table"
    })
    void rejectInvalidPostgresqlOptions(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql,
                parser -> parser.withDialect(Dialect.POSTGRESQL).withUnsupportedStatements(false)));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "EXPLAIN EXECUTE q",
            "EXPLAIN EXECUTE q (42)",
            "EXPLAIN (FORMAT JSON) EXECUTE q (42)",
            "EXPLAIN ANALYZE VERBOSE EXECUTE q (42, 7)"
    })
    void explainPreparedExecution(String sql) throws JSQLParserException {
        ExplainStatement explain = assertInstanceOf(ExplainStatement.class,
                TestUtils.assertSqlCanBeParsedAndDeparsed(sql, true,
                        parser -> parser.withDialect(Dialect.POSTGRESQL)));
        Execute execute = assertInstanceOf(Execute.class, explain.getStatement());
        assertEquals("q", execute.getName());
        assertEquals(Execute.ExecType.EXECUTE, execute.getExecType());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SUMMARIZE EXECUTE q",
            "EXPLAIN EXECUTE q 42",
            "EXPLAIN EXECUTE q USING @x",
            "EXPLAIN CALL q()"
    })
    void rejectOtherExecutionForms(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql,
                parser -> parser.withDialect(Dialect.POSTGRESQL).withUnsupportedStatements(false)));
    }

    @Test
    void preserveRepetitionsAndLegacyMapAccess() throws JSQLParserException {
        ExplainStatement explain = (ExplainStatement) TestUtils.assertSqlCanBeParsedAndDeparsed(
                "EXPLAIN (ANALYZE OFF, COSTS, ANALYZE ON) SELECT * FROM accounts");
        assertTrue(explain.isParenthesizedOptions());
        assertEquals(3, explain.getOptionList().size());
        assertEquals(2, explain.getOptions().size());
        assertEquals("ON", explain.getOption(OptionType.ANALYZE).getValue());
        assertEquals("ON", explain.getOptions().get(OptionType.ANALYZE).getValue());
        explain.getOptionList().clear();
        explain.getOptions().clear();
        assertEquals(3, explain.getOptionList().size());
        explain.addOption(new Option(OptionType.ANALYZE).withValue("FALSE"));
        assertEquals(3, explain.getOptionList().size());
        assertEquals("FALSE", explain.getOption(OptionType.ANALYZE).getValue());
        assertEquals(List.of("accounts"), new TablesNamesFinder().getTableList(explain));
    }

    @Test
    void constructOptionsAndKeepOtherDialects() throws JSQLParserException {
        ExplainStatement explain = new ExplainStatement(CCJSqlParserUtil.parse("SELECT 1"));
        assertNull(explain.getOptions());
        explain.setParenthesizedOptions(true);
        explain.addOption(new Option(OptionType.GENERIC_PLAN));
        explain.addOption(new Option(OptionType.FORMAT).withValue("TEXT"));
        TestUtils.assertStatementCanBeDeparsedAs(explain,
                "EXPLAIN (GENERIC_PLAN, FORMAT TEXT) SELECT 1");
        TestUtils.assertSqlCanBeParsedAndDeparsed("EXPLAIN PLAN FOR SELECT 1");
        TestUtils.assertSqlCanBeParsedAndDeparsed("EXPLAIN BUFFERS FALSE SELECT 1");
        TestUtils.assertSqlCanBeParsedAndDeparsed("SUMMARIZE accounts");
    }
}

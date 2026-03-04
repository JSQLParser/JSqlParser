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

import static net.sf.jsqlparser.test.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.delete.Delete;
import org.junit.jupiter.api.Test;

public class ExplainTest {

    @Test
    public void testDescribe() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("EXPLAIN SELECT * FROM mytable");
    }

    @Test
    public void testAnalyze() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("EXPLAIN ANALYZE SELECT * FROM mytable");
    }

    @Test
    public void testBuffers() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("EXPLAIN BUFFERS SELECT * FROM mytable");
    }

    @Test
    public void testCosts() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("EXPLAIN COSTS SELECT * FROM mytable");
    }

    @Test
    public void testFormat() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("EXPLAIN FORMAT XML SELECT * FROM mytable");
    }

    @Test
    public void testVerbose() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("EXPLAIN VERBOSE SELECT * FROM mytable");
    }

    @Test
    public void testMultiOptions_orderPreserved() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "EXPLAIN VERBOSE ANALYZE BUFFERS COSTS SELECT * FROM mytable");
    }

    @Test
    public void getOption_returnsValues() throws JSQLParserException {
        ExplainStatement explain = (ExplainStatement) CCJSqlParserUtil
                .parse("EXPLAIN VERBOSE FORMAT JSON BUFFERS FALSE SELECT * FROM mytable");

        assertThat(explain.getOption(ExplainStatement.OptionType.ANALYZE)).isNull();
        assertThat(explain.getOption(ExplainStatement.OptionType.VERBOSE)).isNotNull();

        ExplainStatement.Option format = explain.getOption(ExplainStatement.OptionType.FORMAT);
        assertThat(format).isNotNull().extracting(ExplainStatement.Option::getValue)
                .isEqualTo("JSON");

        ExplainStatement.Option buffers = explain.getOption(ExplainStatement.OptionType.BUFFERS);
        assertThat(buffers).isNotNull().extracting(ExplainStatement.Option::getValue)
                .isEqualTo("FALSE");

        explain = (ExplainStatement) CCJSqlParserUtil.parse("EXPLAIN SELECT * FROM mytable");
        assertThat(explain.getOption(ExplainStatement.OptionType.ANALYZE)).isNull();
    }

    @Test
    public void testDelete() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("EXPLAIN DELETE FROM mytable");
    }

    @Test
    public void testUpdate() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("EXPLAIN UPDATE mytable SET col = 1");
    }

    @Test
    public void testInsert() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("EXPLAIN INSERT INTO mytable (col) VALUES (1)");
    }

    @Test
    public void explainDelete_usesGenericStatementSlot() throws JSQLParserException {
        ExplainStatement explain =
                (ExplainStatement) CCJSqlParserUtil.parse("EXPLAIN DELETE FROM mytable");
        assertThat(explain.getStatement()).isInstanceOf(Delete.class);
    }

    @Test
    public void testDeleteInStatementsList() throws JSQLParserException {
        Statements statements = CCJSqlParserUtil.parseStatements("EXPLAIN DELETE FROM mytable;");
        assertThat(statements).isNotNull();
        assertThat(statements).hasSize(1);
        assertThat(statements.get(0)).isInstanceOf(ExplainStatement.class);
    }
}

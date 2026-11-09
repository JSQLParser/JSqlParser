/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2022 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.analyze;

import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class AnalyzeTest {

    private final CCJSqlParserManager parserManager = new CCJSqlParserManager();

    @Test
    public void testAnalyze() throws JSQLParserException {
        String statement = "ANALYZE mytab";
        Analyze parsed = (Analyze) parserManager.parse(new StringReader(statement));
        assertEquals("mytab", parsed.getTable().getFullyQualifiedName());
        assertEquals(statement, "" + parsed);

        assertDeparse(new Analyze().withTable(new Table("mytab")), statement);
    }

    @Test
    public void testAnalyze2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ANALYZE mytable");
    }

    @Test
    public void testMySqlAnalyzeTableList() throws JSQLParserException {
        String sql = "ANALYZE TABLE t2, t3";
        Analyze analyze = (Analyze) parserManager.parse(new StringReader(sql));

        assertEquals(List.of("t2", "t3"), analyze.getTables().stream()
                .map(Table::getFullyQualifiedName).collect(Collectors.toList()));
        assertEquals("t2", analyze.getTable().getFullyQualifiedName());
        assertEquals(sql, analyze.toString());

        assertDeparse(new Analyze().withTableKeyword(true)
                .withTables(List.of(new Table("t2"), new Table("t3"))), sql);
    }

    @Test
    public void testMySqlAnalyzeUpdateHistogram() throws JSQLParserException {
        String sql = "ANALYZE TABLE t1 UPDATE HISTOGRAM ON c1, c2 WITH 64 BUCKETS";
        Analyze analyze = (Analyze) assertSqlCanBeParsedAndDeparsed(sql);

        assertEquals(Analyze.HistogramAction.UPDATE, analyze.getHistogramAction());
        assertEquals(List.of("c1", "c2"), analyze.getHistogramColumns().stream()
                .map(Column::getFullyQualifiedName).collect(Collectors.toList()));
        assertEquals(64, analyze.getHistogramBucketCount());
    }

    @Test
    public void testMySqlAnalyzeDropHistogram() throws JSQLParserException {
        String sql = "ANALYZE LOCAL TABLE t2 DROP HISTOGRAM ON c1";
        Analyze analyze = (Analyze) assertSqlCanBeParsedAndDeparsed(sql);

        assertEquals(Analyze.Modifier.LOCAL, analyze.getModifier());
        assertEquals(Analyze.HistogramAction.DROP, analyze.getHistogramAction());
        assertEquals("c1", analyze.getHistogramColumns().get(0).getFullyQualifiedName());
    }

    @Test
    public void testMySqlAnalyzeNoWriteToBinlog() throws JSQLParserException {
        String sql = "ANALYZE NO_WRITE_TO_BINLOG TABLE t1";
        Analyze analyze = (Analyze) assertSqlCanBeParsedAndDeparsed(sql);

        assertEquals(Analyze.Modifier.NO_WRITE_TO_BINLOG, analyze.getModifier());
    }

    @Test
    public void testMySqlAnalyzeHistogramUpdateMode() throws JSQLParserException {
        Analyze auto = (Analyze) assertSqlCanBeParsedAndDeparsed(
                "ANALYZE TABLE t1 UPDATE HISTOGRAM ON c1, c2 WITH 32 BUCKETS AUTO UPDATE");
        assertEquals(32, auto.getHistogramBucketCount());
        assertEquals(Analyze.HistogramUpdateMode.AUTO, auto.getHistogramUpdateMode());

        Analyze manual = (Analyze) assertSqlCanBeParsedAndDeparsed(
                "ANALYZE TABLE t1 UPDATE HISTOGRAM ON c1 MANUAL UPDATE");
        assertNull(manual.getHistogramBucketCount());
        assertEquals(Analyze.HistogramUpdateMode.MANUAL, manual.getHistogramUpdateMode());

        assertDeparse(new Analyze().withTableKeyword(true).withTable(new Table("t1"))
                .withHistogramAction(Analyze.HistogramAction.UPDATE)
                .addHistogramColumns(new Column("c1"))
                .withHistogramUpdateMode(Analyze.HistogramUpdateMode.AUTO),
                "ANALYZE TABLE t1 UPDATE HISTOGRAM ON c1 AUTO UPDATE");
    }

    @Test
    public void testMySqlAnalyzeHistogramUsingData() throws JSQLParserException {
        String json = "{\"buckets\": [[1, 0.5], [2, 1.0]], \"data-type\": \"int\"}";
        String sql = "ANALYZE NO_WRITE_TO_BINLOG TABLE t1 UPDATE HISTOGRAM ON c1 USING DATA '"
                + json + "'";
        Analyze analyze = (Analyze) assertSqlCanBeParsedAndDeparsed(sql);
        assertEquals(json, analyze.getHistogramData().getValue());
        assertNull(analyze.getHistogramUpdateMode());
        assertEquals(sql, CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.MYSQL))
                .toString());

        analyze.setHistogramData(new StringValue("{}"));
        assertEquals("ANALYZE NO_WRITE_TO_BINLOG TABLE t1 UPDATE HISTOGRAM ON c1 USING DATA '{}'",
                analyze.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ANALYZE TABLE t1 DROP HISTOGRAM ON c1 AUTO UPDATE",
            "ANALYZE TABLE t1 DROP HISTOGRAM ON c1 USING DATA '{}'",
            "ANALYZE TABLE t1 UPDATE HISTOGRAM ON c1 USING DATA '{}' MANUAL UPDATE",
            "ANALYZE TABLE t1 UPDATE HISTOGRAM ON c1 MANUAL UPDATE WITH 8 BUCKETS",
            "ANALYZE TABLE t1 UPDATE HISTOGRAM ON c1 AUTO",
            "ANALYZE TABLE t1 UPDATE HISTOGRAM ON c1 USING DATA"})
    public void testMySqlAnalyzeRejectsMisplacedHistogramOptions(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql));
    }
}

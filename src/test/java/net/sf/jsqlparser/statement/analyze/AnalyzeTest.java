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

import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import org.junit.jupiter.api.Test;

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
}

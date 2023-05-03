/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.values;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.select.Values;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;

public class ValuesTest {

    @Test
    public void testRowConstructor() throws JSQLParserException {
        String sqlStr = "VALUES (1,2), (3,4)";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    public void testDuplicateKey() throws JSQLParserException {
        String statement = "VALUES (1, 2, 'test')";
        assertSqlCanBeParsedAndDeparsed(statement);

        Values values = new Values()
                .addExpressions(
                        new LongValue(1), new LongValue(2), new StringValue("test"));
        assertDeparse(values, statement);
    }

    @Test
    public void testComplexWithQueryIssue561() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "WITH split (word, str, hascomma) AS (VALUES ('', 'Auto,A,1234444', 1) UNION ALL SELECT substr(str, 0, CASE WHEN instr(str, ',') THEN instr(str, ',') ELSE length(str) + 1 END), ltrim(substr(str, instr(str, ',')), ','), instr(str, ',') FROM split WHERE hascomma) SELECT trim(word) FROM split WHERE word != ''",
                true);
    }

    @Test
    public void testObject() {
        Values valuesStatement =
                new Values().addExpressions(new StringValue("1"), new StringValue("2"));
        valuesStatement.addExpressions(Arrays.asList(new StringValue("3"), new StringValue("4")));

        valuesStatement.accept(new StatementVisitorAdapter());
    }
}

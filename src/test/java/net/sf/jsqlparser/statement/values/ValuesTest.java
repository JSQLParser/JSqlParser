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
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import org.junit.Test;

public class ValuesTest {

    @Test
    public void testDuplicateKey() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("VALUES (1, 2, 'test')");
    }

    @Test
    public void testComplexWithQueryIssue561() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("WITH split (word, str, hascomma) AS (VALUES ('', 'Auto,A,1234444', 1) UNION ALL SELECT substr(str, 0, CASE WHEN instr(str, ',') THEN instr(str, ',') ELSE length(str) + 1 END), ltrim(substr(str, instr(str, ',')), ','), instr(str, ',') FROM split WHERE hascomma) SELECT trim(word) FROM split WHERE word != ''");
    }
}

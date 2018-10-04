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
        assertSqlCanBeParsedAndDeparsed("WITH split(word, str, hascomma) AS (values('', 'Auto,A,1234444', 1) UNION ALL SELECT substr(str, 0, case when instr(str, ',') then instr(str, ',') else length(str)+1 end), ltrim(substr(str, instr(str, ',')), ','), instr(str, ',') FROM split WHERE hascomma) SELECT trim(word) FROM split WHERE word!=''");
    }
}

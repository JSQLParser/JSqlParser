package net.sf.jsqlparser.statement.piped;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;


class SetOperationPipeOperatorTest {

    @Test
    void parseAndDeparseUnion() throws JSQLParserException {
        String sqlStr =
                "SELECT 3\n" +
                        "|> UNION ALL\n" +
                        "    (SELECT 1),\n" +
                        "    (SELECT 2);";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    // @todo: parse SELECT * FROM UNNEST(ARRAY<INT64>[2, 3, 3, 5]) AS number

    @Test
    void parseAndDeparseIntersect() throws JSQLParserException {
        String sqlStr =
                "SELECT * FROM UNNEST(ARRAY[1, 2, 3, 3, 4]) AS number\n" +
                        "|> INTERSECT DISTINCT\n" +
                        "    (SELECT * FROM UNNEST(ARRAY[2, 3, 3, 5]) AS number);";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void parseAndDeparseExcept() throws JSQLParserException {
        String sqlStr =
                "SELECT * FROM UNNEST(ARRAY[1, 2, 3, 3, 4]) AS number\n" +
                        "|> EXCEPT DISTINCT\n" +
                        "    (SELECT * FROM UNNEST(ARRAY[1, 2]) AS number);";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}

package net.sf.jsqlparser.expression.operators.relational;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MemberOfExpressionTest {
    @Test
    void testMemberOf() throws JSQLParserException {
        String sqlStr = "SELECT 17 MEMBER OF ( cxr_post_id->'$.value' ) ";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "SELECT 17 MEMBER OF ( '[23, \"abc\", 17, \"ab\", 10]' ) ";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}

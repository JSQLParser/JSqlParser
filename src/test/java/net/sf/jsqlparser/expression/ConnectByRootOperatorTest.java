package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;


class ConnectByRootOperatorTest {

    @Test
    void testCondition() throws JSQLParserException {
        //@formatter:off
        String sqlStr=
                "SELECT EMP_ID,  EMP_NAME,\n" +
                " \t  CONNECT_BY_ROOT (EMP_NAME || '_' || EMP_ID) AS ROOT_MANAGER,\n" +
                " \t  SYS_CONNECT_BY_PATH(EMP_NAME, ' -> ') AS PATH\n" +
                "    FROM EMPLOYEES\n" +
                "    START WITH MANAGER_ID IS NULL\n" +
                "    CONNECT BY PRIOR EMP_ID = MANAGER_ID";
        //@formatter:on
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}

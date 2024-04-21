package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParenthesedSelectTest {
    @Test
    void testConstructFromItem() throws JSQLParserException {
        String sqlStr = "select winsales.* from winsales;";

        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(sqlStr);
        select.setFromItem(new ParenthesedSelect(select.getFromItem()));

        TestUtils.assertStatementCanBeDeparsedAs(select,
                "select winsales.* from (select * from winsales) AS winsales;", true);

        sqlStr = "select a.* from winsales AS a;";

        select = (PlainSelect) CCJSqlParserUtil.parse(sqlStr);
        select.setFromItem(new ParenthesedSelect(select.getFromItem()));

        TestUtils.assertStatementCanBeDeparsedAs(select,
                "select a.* from (select * from winsales AS a) AS a;", true);
    }
}

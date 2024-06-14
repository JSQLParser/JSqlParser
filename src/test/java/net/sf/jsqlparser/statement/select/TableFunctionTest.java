/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2023 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TableFunctionTest {

    @Test
    void testLateralFlat() throws JSQLParserException {
        String sqlStr = "WITH t AS (\n" +
                "  SELECT \n" +
                "    'ABC' AS dim, \n" +
                "    ARRAY_CONSTRUCT('item1', 'item2', 'item3') AS user_items\n" +
                ")\n" +
                "SELECT DIM, count(value) as COUNT_\n" +
                "FROM t a,\n" +
                "LATERAL FLATTEN(input => a.user_items) b\n" +
                "group by 1";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    /**
     * The SQL keyword "OUTER" is a valid parameter name for Snowflake's FLATTEN table function.
     */
    @Test
    void testTableFunctionWithNamedParameterWhereNameIsOuterKeyword() throws JSQLParserException {
        String sqlStr =
                "INSERT INTO db.schema.target\n" +
                "     (Name, FriendParent)\n" +
                " SELECT\n" +
                "     i.DATA_VALUE:Name AS Name,\n" +
                "     f1.Value:Parent:Name AS FriendParent\n" +
                " FROM\n" +
                "     db.schema.source AS i,\n" +
                "     lateral flatten(input => i.DATA_VALUE:Friends, outer => true) AS f1;";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}

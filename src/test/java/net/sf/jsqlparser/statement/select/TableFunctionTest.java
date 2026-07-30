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

import static org.junit.jupiter.api.Assertions.*;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

    @ParameterizedTest
    @ValueSource(strings = {
            "OFFSET",
            "ORDINALITY"
    })
    void testTableFunctionWithSupportedWithClauses(String withClause) throws JSQLParserException {
        String sqlStr = "SELECT * FROM UNNEST(ARRAY[1, 2, 3]) WITH " + withClause + " AS t(a, b)";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testRowsFromTableFunction() throws JSQLParserException {
        String sqlStr = "SELECT *\n"
                + "FROM ROWS FROM (\n"
                + "    generate_series(1,3),\n"
                + "    generate_series(10,12)\n"
                + ") AS t(a,b)";

        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        TableFunction tableFunction = select.getFromItem(TableFunction.class);

        assertTrue(tableFunction.isRowsFrom());
        assertNotNull(tableFunction.getRowsFromFunctions());
        assertEquals(2, tableFunction.getRowsFromFunctions().size());
        assertEquals("generate_series", tableFunction.getRowsFromFunctions().get(0).getName());
        assertEquals("generate_series", tableFunction.getRowsFromFunctions().get(1).getName());
    }
}

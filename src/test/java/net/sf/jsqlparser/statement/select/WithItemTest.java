/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class WithItemTest {

    @Test
    void testNotMaterializedIssue2251() throws JSQLParserException {
        String sqlStr = "WITH devices AS NOT MATERIALIZED (\n"
                + "  SELECT\n"
                + "    d.uuid AS device_uuid\n"
                + "  FROM active_devices d\n"
                + ")\n"
                + "SELECT 1;";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "WITH\n" +
                    "  FUNCTION doubleup(x integer)\n" +
                    "    RETURNS integer\n" +
                    "    RETURN x * 2\n" +
                    "SELECT doubleup(21);\n",
            "WITH\n" +
                    "  FUNCTION doubleup(x integer)\n" +
                    "    RETURNS integer\n" +
                    "    RETURN x * 2,\n" +
                    "  FUNCTION doubleupplusone(x integer)\n" +
                    "    RETURNS integer\n" +
                    "    RETURN doubleup(x) + 1\n" +
                    "SELECT doubleupplusone(21);",
            "WITH\n" +
                    "  FUNCTION takesArray(x array<double>)\n" +
                    "    RETURNS double\n" +
                    "    RETURN x[1] + x[2] + x[3]\n" +
                    "SELECT takesArray(ARRAY[1.0, 2.0, 3.0]);"

    })
    void testWithFunction(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}

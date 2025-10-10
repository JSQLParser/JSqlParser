/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2024 JSQLParser
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

public class DB2Test {
    @Test
    void testDB2SpecialRegister() throws JSQLParserException {
        String sqlStr =
                "SELECT * FROM TABLE1 where COL_WITH_TIMESTAMP <= CURRENT TIMESTAMP - CURRENT TIMEZONE";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @ParameterizedTest
    @ValueSource( strings = {
            "SELECT * FROM table WITH UR",
            "SELECT * FROM table WITH UR FOR READ ONLY",
            "SELECT * FROM table FOR READ ONLY",
            "SELECT * FROM table FOR FETCH ONLY",
            "SELECT * FROM table FETCH FIRST 100 ROWS ONLY FOR READ ONLY"
    })
    void testWithIsolationLevelAndReadOnlyModes(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}

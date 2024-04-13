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

public class DB2Test {
    @Test
    void testDB2SpecialRegister() throws JSQLParserException {
        String sqlStr =
                "SELECT * FROM TABLE1 where COL_WITH_TIMESTAMP <= CURRENT TIMESTAMP - CURRENT TIMEZONE";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}

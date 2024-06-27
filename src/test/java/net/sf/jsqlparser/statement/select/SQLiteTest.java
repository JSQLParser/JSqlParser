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

public class SQLiteTest {
    @Test
    void testInsertOrReplaceUpsert() throws JSQLParserException {
        String sqlString = "INSERT OR REPLACE INTO kjobLocks VALUES (?, ?, ?)";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlString, true);
    }
}

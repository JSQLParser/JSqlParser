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
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class SQLiteTest {
    @Test
    void testInsertOrReplaceUpsert() throws JSQLParserException {
        String sqlString = "INSERT OR REPLACE INTO kjobLocks VALUES (?, ?, ?)";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlString, true);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ABORT", "FAIL", "IGNORE", "REPLACE", "ROLLBACK"})
    void testInsertOrConflictAlgorithm(String algorithm) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "INSERT OR " + algorithm + " INTO t (a, b) VALUES (1, 2)", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "insert or " + algorithm.toLowerCase(Locale.ROOT) + " into t select * from s", true);
    }

    @Test
    void testInsertOrUnknownAlgorithm() {
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse("INSERT OR NOTHING INTO t VALUES (1)"));
    }
}

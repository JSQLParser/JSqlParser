/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2023 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

class OracleHintTest {

    @Test
    void testSelect() throws JSQLParserException {
        String sqlString = "SELECT /*+parallel*/ * from dual";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlString, true);
    }

    @Test
    void testDelete() throws JSQLParserException {
        String sqlString = "DELETE /*+parallel*/ from dual";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlString, true);
    }

    @Test
    void testInsert() throws JSQLParserException {
        String sqlString = "INSERT /*+parallel*/ INTO dual VALUES(1)";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlString, true);
    }

    @Test
    void testUpdate() throws JSQLParserException {
        String sqlString = "UPDATE /*+parallel*/ dual SET a=b";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlString, true);
    }

    @Test
    void testMerge() throws JSQLParserException {
        String sqlString =
                "MERGE /*+parallel*/ INTO dual USING z ON (a=b) WHEN MATCHED THEN UPDATE SET a=b";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlString, true);
    }

    @Test
    void testCraftedHintCommentDoesNotBacktrack() {
        final StringBuilder sb = new StringBuilder("-- /*+ ");
        for (int i = 0; i < 100000; i++) {
            sb.append('*');
        }
        final String crafted = sb.toString();

        // a line comment carrying an unterminated /*+ marker (no closing */) used to make the
        // block hint pattern backtrack quadratically, and it is not an oracle hint anyway
        assertTimeoutPreemptively(Duration.ofSeconds(2),
                () -> assertFalse(OracleHint.isHintMatch(crafted)));
    }

}

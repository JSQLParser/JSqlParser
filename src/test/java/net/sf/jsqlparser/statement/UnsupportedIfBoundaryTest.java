/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UnsupportedIfBoundaryTest {
    @ParameterizedTest
    @ValueSource(strings = {"IF x > 0 SELECT 1 WHERE", "IF x > 0 SELECT * FROM"})
    void malformedIfBodyCannotBecomeAnOpaqueSuffix(String sql) {
        for (boolean unsupported : new boolean[] {false, true}) {
            for (String prefix : new String[] {"", "SELECT 0;"}) {
                assertThrowsExactly(JSQLParserException.class,
                        () -> CCJSqlParserUtil.parseStatements(prefix + sql + ";SELECT 2;",
                                p -> p.withUnsupportedStatements(unsupported)));
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"IF x > 0 SELECT 1 WHERE", "IF x > 0 SELECT * FROM"})
    void malformedIfBodyRecordsOneErrorWithoutPublishingPartialIf(String sql) throws Exception {
        for (boolean unsupported : new boolean[] {false, true}) {
            for (String prefix : new String[] {"", "SELECT 0;"}) {
                CCJSqlParser parser = CCJSqlParserUtil.newParser(prefix + sql + ";SELECT 2;")
                        .withUnsupportedStatements(unsupported).withErrorRecovery(true);
                Statements statements = parser.Statements();
                int index = prefix.isEmpty() ? 0 : 1;
                assertEquals(index + 2, statements.size());
                assertNull(statements.get(index));
                assertEquals("SELECT 2",
                        assertInstanceOf(Select.class, statements.get(index + 1)).toString());
                assertEquals(1, parser.getParseErrors().size());
            }
        }
    }
}

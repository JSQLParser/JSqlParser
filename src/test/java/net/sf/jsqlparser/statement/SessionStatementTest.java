/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SessionStatementTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "SESSION START 1234", "SESSION START", "SESSION APPLY 'test'", "SESSION APPLY",
            "SESSION DROP \"test\"", "SESSION DROP", "SESSION SHOW test", "SESSION SHOW",
            "SESSION DESCRIBE 1234", "SESSION DESCRIBE", "SESSION START unnamed.session1"
    })
    void testStartSession(String sqlStr) throws JSQLParserException {
        SessionStatement sessionStatement =
                (SessionStatement) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        Assertions.assertInstanceOf(SessionStatement.Action.class, sessionStatement.getAction());
    }

}

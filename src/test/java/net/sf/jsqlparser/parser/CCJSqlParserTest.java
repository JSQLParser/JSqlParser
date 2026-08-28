/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2023 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import net.sf.jsqlparser.JSQLParserException;
import org.junit.jupiter.api.Test;

import net.sf.jsqlparser.parser.feature.Feature;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class CCJSqlParserTest {
    @Test
    public void parserWithTimeout() throws Exception {
        CCJSqlParser parser = CCJSqlParserUtil.newParser("foo").withTimeOut(123L);

        Long timeOut = parser.getAsLong(Feature.timeOut);

        assertThat(timeOut).isEqualTo(123L);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "SELECT /* c */1                | SELECT /* c */ 1",
            "SELECT 1 /* c */+2             | SELECT 1 /* c */ +2",
            "SELECT 1/*x*/+2                | SELECT 1 /*x*/ +2",
            "SELECT /* a /* b */ c */1      | SELECT /* a /* b */ c */ 1",
            "SELECT * FROM t WHERE/*c*/x=1  | SELECT * FROM t WHERE /*c*/ x=1",
            "SELECT/*+ FULL(t) */*FROM t    | SELECT /*+ FULL(t) */ * FROM t"
    })
    void testBlockCommentFollowedByNonWhitespace(String tight, String spaced)
            throws JSQLParserException {
        // Issue #2509: the first character after */ was swallowed into the comment token,
        // so the tight form either failed to parse or silently lost that character.
        assertEquals(CCJSqlParserUtil.parse(spaced).toString(),
                     CCJSqlParserUtil.parse(tight).toString());
    }
}

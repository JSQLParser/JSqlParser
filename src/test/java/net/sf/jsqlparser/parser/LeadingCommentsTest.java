/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class LeadingCommentsTest {

    @Test
    public void blockCommentsBeforeStatementInSourceOrder() throws JSQLParserException {
        Select select = (Select) CCJSqlParserUtil
                .parse("/* keep orderby */ /*+ FULL(t) */ SELECT * FROM mytable");
        assertEquals(List.of("/* keep orderby */", "/*+ FULL(t) */"), select.getLeadingComments());
    }

    @ParameterizedTest
    @ValueSource(strings = {"\n", "\r\n", "\r"})
    public void lineCommentsExcludeLineTerminators(String lineTerminator)
            throws JSQLParserException {
        Select select =
                (Select) CCJSqlParserUtil.parse("-- paging hint" + lineTerminator + "SELECT 1");
        assertEquals(List.of("-- paging hint"), select.getLeadingComments());
    }

    @Test
    public void mixedCommentsRetainExactImagesAndSourceOrder() throws JSQLParserException {
        Select select = (Select) CCJSqlParserUtil
                .parse("/* a */ -- b\r\n// c\r/* d */ SELECT 1");
        assertEquals(List.of("/* a */", "-- b", "// c", "/* d */"), select.getLeadingComments());
    }

    @Test
    public void commentInsideStatementIsNotLeading() throws JSQLParserException {
        Select select = (Select) CCJSqlParserUtil.parse("SELECT /* mid comment */ 1");
        assertEquals(List.of(), select.getLeadingComments());
    }

    @Test
    public void statementWithoutComments() throws JSQLParserException {
        Select select = (Select) CCJSqlParserUtil.parse("SELECT * FROM mytable");
        assertEquals(List.of(), select.getLeadingComments());
    }

    @Test
    public void programmaticStatementHasNoLeadingComments() {
        assertEquals(List.of(), new PlainSelect().getLeadingComments());
    }

    @Test
    public void toStringDropsLeadingCommentsAsBefore() throws JSQLParserException {
        Select select = (Select) CCJSqlParserUtil
                .parse("/* keep orderby */ SELECT * FROM mytable");
        assertFalse(select.toString().contains("/*"));
    }

    @Test
    public void commentsSplitPerStatement() throws JSQLParserException {
        Statements statements = CCJSqlParserUtil
                .parseStatements("/* first */ SELECT 1; /* second */ SELECT 2");
        assertEquals(List.of("/* first */"), ((Select) statements.get(0)).getLeadingComments());
        assertEquals(List.of("/* second */"), ((Select) statements.get(1)).getLeadingComments());
    }
}

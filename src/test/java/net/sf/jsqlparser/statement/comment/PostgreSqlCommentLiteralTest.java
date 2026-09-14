/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.comment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.List;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlCommentLiteralTest {
    static Stream<Arguments> dollarBodies() {
        return Stream.of(Arguments.of("", "body"), Arguments.of("tag", "body"),
                Arguments.of("Tag_123", ""), Arguments.of("_", "quote's \"double\" \\path"),
                Arguments.of("한글", "한글 본문"), Arguments.of("étiquette", "$other$x$other$ $$ $1"),
                Arguments.of("tag", "line1\nline2; --sql\n/* comment */"),
                Arguments.of("tag", "$Tag$body$Tag$"));
    }

    @ParameterizedTest
    @MethodSource("dollarBodies")
    void preservesDollarBodyAndTagAcrossCommentTargets(String tag, String body) throws Exception {
        String quote = "$" + tag + "$";
        for (String target : List.of("TABLE t", "COLUMN t.c", "VIEW v")) {
            String sql = "COMMENT ON " + target + " IS " + quote + body + quote;
            Comment comment = parse(sql);
            StringBuilder buffer = new StringBuilder();
            comment.accept(new StatementDeParser(buffer), null);
            assertEquals(sql, buffer.toString());
            for (Comment tree : List.of(comment, parse(comment.toString()),
                    parse(buffer.toString()))) {
                assertEquals(body, tree.getComment().getValue());
                assertEquals(quote, tree.getComment().getQuoteStr());
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"'one'\n 'two'", "'one' -- line comment\n 'two'",
            "'one'\r\n 'two'"})
    void concatenatesNewlineSeparatedOrdinaryLiterals(String literal) throws Exception {
        Comment comment = parse("COMMENT ON TABLE t IS " + literal);
        assertEquals("onetwo", comment.getComment().getValue());
        assertEquals("onetwo", parse(comment.toString()).getComment().getValue());
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse("SELECT " + literal,
                p -> p.withDialect(Dialect.POSTGRESQL));
        assertEquals("onetwo", select.getSelectItem(0).getExpression(StringValue.class).getValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"E'quote\\'s'", "e'quote\\'s'", "E'a\\nb'", "E'\\\\path'"})
    void acceptsEscapeStringsWithoutChangingOrdinaryStringMode(String literal) throws Exception {
        Comment comment = parse("COMMENT ON TABLE t IS " + literal);
        assertEquals("E", comment.getComment().getPrefix());
        assertEquals(comment.getComment().toString(),
                parse(comment.toString()).getComment().toString());
        Statements statements = CCJSqlParserUtil.parseStatements(
                "COMMENT ON TABLE t IS " + literal + "; SELECT '\\' AS backslash; SELECT 2",
                p -> p.withDialect(Dialect.POSTGRESQL));
        assertEquals(3, statements.size());
        StringValue ordinary =
                ((PlainSelect) statements.get(1)).getSelectItem(0).getExpression(StringValue.class);
        assertEquals("\\", ordinary.getValue());
        assertNull(ordinary.getPrefix());
    }

    @Test
    void keepsSemicolonsInsideDollarBodiesAndFollowingStatements() throws Exception {
        Statements statements = CCJSqlParserUtil.parseStatements(
                "COMMENT ON TABLE t IS $tag$body; SELECT 0; --text\n$tag$; SELECT 42;",
                p -> p.withDialect(Dialect.POSTGRESQL));
        assertEquals(2, statements.size());
        assertEquals("body; SELECT 0; --text\n",
                ((Comment) statements.get(0)).getComment().getValue());
        assertEquals("SELECT 42", statements.get(1).toString());
    }

    @Test
    void keepsTaggedQuotesOptInAndSupportsExplicitFeature() throws Exception {
        String sql = "COMMENT ON TABLE t IS $tag$body$tag$";
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql));
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse(sql,
                        p -> p.withDialect(Dialect.POSTGRESQL).withDollarQuotedStringTags(false)));
        Comment comment =
                (Comment) CCJSqlParserUtil.parse(sql, p -> p.withDollarQuotedStringTags(true));
        assertEquals("body", comment.getComment().getValue());
        assertNull(parse("COMMENT ON TABLE t IS NULL").getComment());
        assertEquals("", parse("COMMENT ON TABLE t IS ''").getComment().getValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"'one' 'two'", "$t$one$t$\n $t$two$t$", "$t$one$t$\n 'two'",
            "'one'\n $t$two$t$", "'one'\n E'two'", "$Tag$body$tag$", "$tag$unterminated",
            "$1$body$1$"})
    void rejectsInvalidLiteralForms(String literal) {
        assertThrows(JSQLParserException.class, () -> parse("COMMENT ON TABLE t IS " + literal));
    }

    private static Comment parse(String sql) throws JSQLParserException {
        return (Comment) CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.POSTGRESQL));
    }
}

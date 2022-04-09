/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.OracleHint;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.Node;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.apache.commons.lang3.builder.MultilineRecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

/**
 *
 * @author toben
 */
public class TestUtils {

    private static final Pattern SQL_COMMENT_PATTERN = Pattern.
            compile("(--.*$)|(/\\*.*?\\*/)", Pattern.MULTILINE);

    private static final Pattern SQL_SANITATION_PATTERN
            = Pattern.compile("(\\s+)", Pattern.MULTILINE);

    // Assure SPACE around Syntax Characters
    private static final Pattern SQL_SANITATION_PATTERN2
            = Pattern.compile("\\s*([!/,()=+\\-*|\\]<>:])\\s*", Pattern.MULTILINE);

    /**
     * @param statement
     * @return the parsed {@link Statement}
     * @throws JSQLParserException
     */
    public static Statement assertSqlCanBeParsedAndDeparsed(String statement) throws JSQLParserException {
        return assertSqlCanBeParsedAndDeparsed(statement, false);
    }

    /**
     * Tries to parse and deparse the given statement.
     *
     * @param statement
     * @param laxDeparsingCheck removes all linefeeds from the original and removes all double spaces. The check is
     * caseinsensitive.
     * @return the parsed {@link Statement}
     * @throws JSQLParserException
     */
    public static Statement assertSqlCanBeParsedAndDeparsed(String statement, boolean laxDeparsingCheck)
            throws JSQLParserException {
        return assertSqlCanBeParsedAndDeparsed(statement, laxDeparsingCheck, null);
    }

    /**
     * @param statement
     * @param laxDeparsingCheck removes all linefeeds from the original and removes all double spaces. The check is
     * caseinsensitive.
     * @param consumer - a parser-consumer for parser-configurations from outside
     * @return the parsed {@link Statement}
     * @throws JSQLParserException
     */
    public static Statement assertSqlCanBeParsedAndDeparsed(String statement, boolean laxDeparsingCheck,
            Consumer<CCJSqlParser> consumer) throws JSQLParserException {
        Statement parsed = CCJSqlParserUtil.parse(statement, consumer);
        assertStatementCanBeDeparsedAs(parsed, statement, laxDeparsingCheck);
        return parsed;
    }

    public static void assertStatementCanBeDeparsedAs(Statement parsed, String statement) {
        assertStatementCanBeDeparsedAs(parsed, statement, false);
    }

    public static void assertStatementCanBeDeparsedAs(Statement parsed, String statement, boolean laxDeparsingCheck) {
        String sanitizedInputSqlStr = buildSqlString(parsed.toString(), laxDeparsingCheck);
        String sanitizedStatementStr = buildSqlString(statement, laxDeparsingCheck);

        assertEquals(sanitizedStatementStr, sanitizedInputSqlStr);

        StringBuilder builder = new StringBuilder();
        parsed.accept( new StatementDeParser(builder) );

        String sanitizedDeparsedStr = buildSqlString(builder.toString(), laxDeparsingCheck);

        assertEquals(sanitizedStatementStr, sanitizedDeparsedStr);
    }

    /**
     * Asserts that the {@link Statement} can be deparsed and deparsing results in given #statement
     *
     * @param stmt
     * @param statement
     */
    public static void assertDeparse(Statement stmt, String statement) {
        assertDeparse(stmt, statement, false);
    }

    /**
     * Compares the object-tree of a given parsed model and a created one.
     *
     * @param parsed
     * @param created
     */
    public static void assertEqualsObjectTree(Statement parsed, Statement created) {
        assertEquals(toReflectionString(parsed), toReflectionString(created));
    }

    /**
     * @param stmt
     * @return a {@link String} build by {@link ToStringBuilder} and {@link ObjectTreeToStringStyle#INSTANCE}
     */
    public static String toReflectionString(Statement stmt) {
        return toReflectionString(stmt, false);
    }

    /**
     * @param stmt
     * @return a {@link String} build by {@link ToStringBuilder} and {@link ObjectTreeToStringStyle#INSTANCE}
     */
    public static String toReflectionString(Statement stmt, boolean includingASTNode) {
        ReflectionToStringBuilder strb = new ReflectionToStringBuilder(stmt,
                includingASTNode ? ObjectTreeToStringStyle.INSTANCE_INCLUDING_AST : ObjectTreeToStringStyle.INSTANCE);
        return strb.build();
    }

    /**
     * Replacement of {@link Arrays#asList(Object...)} which returns java.util.Arrays$ArrayList not java.util.ArrayList,
     * the internal model uses java.util.ArrayList by default, which supports modification
     *
     * @param <T>
     * @param obj
     * @return a {@link ArrayList} of given items
     */
    @SafeVarargs
    public static <T> List<T> asList(T... obj) {
        return Stream.of(obj).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * <p>
     * {@code ToStringStyle} that outputs on multiple lines without identity hashcode.
     * </p>
     */
    private static final class ObjectTreeToStringStyle extends MultilineRecursiveToStringStyle {

        private static final long serialVersionUID = 1L;

        public static final ObjectTreeToStringStyle INSTANCE = new ObjectTreeToStringStyle(false);
        public static final ObjectTreeToStringStyle INSTANCE_INCLUDING_AST = new ObjectTreeToStringStyle(true);

        private boolean includingASTNode;

        /**
         * <p>
         * Constructor.
         * </p>
         *
         * <p>
         * Use the static constant rather than instantiating.
         * </p>
         */
        private ObjectTreeToStringStyle(boolean includingASTNode) {
            super();
            this.includingASTNode = includingASTNode;
            this.setUseClassName(true);
            this.setUseIdentityHashCode(false);
            ToStringBuilder.setDefaultStyle(this);
        }

        @Override
        public void append(final StringBuffer buffer, final String fieldName, final Object value,
                final Boolean fullDetail) {
            if (includingASTNode || !"node".equals(fieldName)) {
                super.append(buffer, fieldName, value, fullDetail);
            }
        }

        /**
         * empty {@link Collection}'s should be printed as <code>null</code>, otherwise the outcome cannot be compared
         */
        @Override
        protected void appendDetail(final StringBuffer buffer, final String fieldName, final Collection<?> coll) {
            if (coll.isEmpty()) {
                appendNullText(buffer, fieldName);
            } else {
                super.appendDetail(buffer, fieldName, coll);
            }
        }

        /**
         * empty {@link Map}'s should be printed as <code>null</code>, otherwise the outcome cannot be compared
         */
        @Override
        protected void appendDetail(final StringBuffer buffer, final String fieldName, final Map<?, ?> coll) {
            if (coll.isEmpty()) {
                appendNullText(buffer, fieldName);
            } else {
                super.appendDetail(buffer, fieldName, coll);
            }
        }

        @Override
        protected boolean accept(Class<?> clazz) {
            if (includingASTNode) {
                return super.accept(clazz);
            } else {
                return isNotANode(clazz) && super.accept(clazz);
            }
        }

        public boolean isNotANode(Class<?> clazz) {
            return !Node.class.isAssignableFrom(clazz);
        }

    }

    /**
     * Asserts that the {@link Statement} can be deparsed and deparsing results in given #statement
     *
     * @param stmt
     * @param statement
     * @param laxDeparsingCheck removes all linefeeds from the original and removes all double spaces. The check is
     * caseinsensitive.
     */
    public static void assertDeparse(Statement stmt, String statement, boolean laxDeparsingCheck) {
        StatementDeParser deParser = new StatementDeParser(new StringBuilder());
        stmt.accept(deParser);
        assertEquals(buildSqlString(statement, laxDeparsingCheck),
                buildSqlString(deParser.getBuffer().toString(), laxDeparsingCheck));
    }

    public static String buildSqlString(final String originalSql, boolean laxDeparsingCheck) {
        if (laxDeparsingCheck) {
            // remove comments
            String sanitizedSqlStr = SQL_COMMENT_PATTERN.matcher(originalSql).replaceAll("");

            // redundant white space
            sanitizedSqlStr = SQL_SANITATION_PATTERN.matcher(sanitizedSqlStr).replaceAll(" ");

            // assure spacing around Syntax Characters
            sanitizedSqlStr = SQL_SANITATION_PATTERN2.matcher(sanitizedSqlStr).replaceAll("$1");
            return sanitizedSqlStr.trim().toLowerCase();
        } else {
            // remove comments only
            return SQL_COMMENT_PATTERN.matcher(originalSql).replaceAll("");
        }
    }

    @Test
    public void testBuildSqlString() {
        assertEquals("select col from test", buildSqlString("   SELECT   col FROM  \r\n \t  TEST \n", true));
        assertEquals("select  col  from test", buildSqlString("select  col  from test", false));
    }

    public static void assertExpressionCanBeDeparsedAs(final Expression parsed, String expression) {
        ExpressionDeParser expressionDeParser = new ExpressionDeParser();
        StringBuilder stringBuilder = new StringBuilder();
        expressionDeParser.setBuffer(stringBuilder);
        SelectDeParser selectDeParser = new SelectDeParser(expressionDeParser, stringBuilder);
        expressionDeParser.setSelectVisitor(selectDeParser);
        parsed.accept(expressionDeParser);

        assertEquals(expression, stringBuilder.toString());
    }

    public static void assertExpressionCanBeParsedAndDeparsed(String expressionStr, boolean laxDeparsingCheck) throws JSQLParserException {
        Expression expression = CCJSqlParserUtil.parseExpression(expressionStr);
        assertEquals(buildSqlString(expressionStr, laxDeparsingCheck),
                buildSqlString(expression.toString(), laxDeparsingCheck));
    }

    public static void assertOracleHintExists(String sql, boolean assertDeparser, String... hints)
            throws JSQLParserException {
        if (assertDeparser) {
            assertSqlCanBeParsedAndDeparsed(sql, true);
        }

        Statement statement = CCJSqlParserUtil.parse(sql);
        if (statement instanceof Select) {
            Select stmt = (Select) statement;
            if (stmt.getSelectBody() instanceof PlainSelect) {
                PlainSelect ps = (PlainSelect) stmt.getSelectBody();
                OracleHint hint = ps.getOracleHint();
                assertNotNull(hint);
                assertEquals(hints[0], hint.getValue());
            } else if (stmt.getSelectBody() instanceof SetOperationList) {
                SetOperationList setop = (SetOperationList) stmt.getSelectBody();
                for (int i = 0; i < setop.getSelects().size(); i++) {
                    PlainSelect pselect = (PlainSelect) setop.getSelects().get(i);
                    OracleHint hint = pselect.getOracleHint();
                    if (hints[i] == null) {
                        assertNull(hint);
                    } else {
                        assertNotNull(hint);
                        assertEquals(hints[i], hint.getValue());
                    }
                }
            }
        } else if (statement instanceof Update) {
            Update stmt = (Update) statement;
            OracleHint hint = stmt.getOracleHint();
            assertNotNull(hint);
            assertEquals(hints[0], hint.getValue());
        } else if (statement instanceof Insert) {
            Insert stmt = (Insert) statement;
            OracleHint hint = stmt.getOracleHint();
            assertNotNull(hint);
            assertEquals(hints[0], hint.getValue());
        } else if (statement instanceof Delete) {
            Delete stmt = (Delete) statement;
            OracleHint hint = stmt.getOracleHint();
            assertNotNull(hint);
            assertEquals(hints[0], hint.getValue());
        }
    }
}

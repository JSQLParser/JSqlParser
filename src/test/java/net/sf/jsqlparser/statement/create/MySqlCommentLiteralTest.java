/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterExpression;
import net.sf.jsqlparser.statement.alter.AlterExpressionTableOption;
import net.sf.jsqlparser.statement.alter.AlterOperation;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.PartitionDefinition;
import net.sf.jsqlparser.statement.create.table.TableOption;
import net.sf.jsqlparser.util.TableDefinitionTraversal;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** MySQL table and partition COMMENT literals and the values a server stores for them. */
class MySqlCommentLiteralTest {

    /** Literals with the comment MySQL 8.4 stores for them. */
    static Stream<Arguments> mySqlLiterals() {
        return Stream.of(
                Arguments.of("'users'", "users"),
                Arguments.of("'사용자 테이블'", "사용자 테이블"),
                Arguments.of("'it''s'", "it's"),
                Arguments.of("'it\\'s'", "it's"),
                Arguments.of("\"dq\"", "dq"),
                Arguments.of("\"double \\\"quoted\\\" text\"", "double \"quoted\" text"),
                Arguments.of("''", ""),
                Arguments.of("'line1\\nline2'", "line1\nline2"),
                Arguments.of("'a\\\\b'", "a\\b"),
                Arguments.of("'tab\\there'", "tab\there"),
                Arguments.of("'percent\\% underscore\\_'", "percent\\% underscore\\_"),
                Arguments.of("'emoji 😀'", "emoji 😀"),
                Arguments.of("'first line\nsecond line'", "first line\nsecond line"));
    }

    /** Literals that read the same with and without a dialect. */
    static Stream<Arguments> portableLiterals() {
        return Stream.of(
                Arguments.of("'users'", "users"),
                Arguments.of("'it''s'", "it's"),
                Arguments.of("''", ""),
                Arguments.of("'사용자 테이블'", "사용자 테이블"),
                Arguments.of("'emoji 😀'", "emoji 😀"));
    }

    @ParameterizedTest
    @MethodSource("mySqlLiterals")
    void tableCommentsDecodeToTheStoredValue(String literal, String stored)
            throws JSQLParserException {
        assertTableComments(literal, stored, Dialect.MYSQL);
    }

    @ParameterizedTest
    @MethodSource("portableLiterals")
    void tableCommentsDecodeWithoutDialect(String literal, String stored)
            throws JSQLParserException {
        assertTableComments(literal, stored, null);
    }

    private static void assertTableComments(String literal, String stored, Dialect dialect)
            throws JSQLParserException {
        String sql = "CREATE TABLE t (id INT) ENGINE = InnoDB COMMENT = " + literal
                + " DEFAULT CHARSET = utf8mb4";
        CreateTable create = (CreateTable) parse(sql, dialect);
        assertComment(create.getTableOptions().get(1), literal, stored);
        assertEquals(List.of("ENGINE", "=", "InnoDB", "COMMENT", "=", literal, "DEFAULT",
                "CHARSET", "=", "utf8mb4"), create.getTableOptionsStrings());
        assertEquals(sql, create.toString());
        assertRoundTrip(create, dialect);

        String ctas = "CREATE TABLE t (id INT) COMMENT " + literal + " AS SELECT 1 AS id";
        CreateTable query = (CreateTable) parse(ctas, dialect);
        assertComment(query.getTableOptions().get(0), literal, stored);
        assertEquals(List.of("COMMENT", literal), query.getTableOptionsStrings());
        assertEquals(ctas, query.toString());
        assertRoundTrip(query, dialect);

        Alter alter = (Alter) parse("ALTER TABLE t COMMENT = " + literal, dialect);
        AlterExpressionTableOption action =
                (AlterExpressionTableOption) alter.getAlterExpressions().get(0);
        assertComment(action.getStructuredTableOption(), literal, stored);
        assertEquals(literal, action.getCommentText());
        assertEquals(AlterOperation.COMMENT_WITH_EQUAL_SIGN, action.getOperation());
        assertEquals("ALTER TABLE t COMMENT = " + literal, alter.toString());
        assertRoundTrip(alter, dialect);
    }

    private static void assertComment(TableOption option, String literal, String stored) {
        assertEquals(TableOption.Kind.COMMENT, option.getKind());
        assertEquals(stored, option.getComment().getNotExcapedValue());
        assertEquals(literal, option.getComment().toString());
        assertEquals(literal, option.getValue());
        assertEquals(literal, option.getTokens().get(option.getTokens().size() - 1));
    }

    @Test
    void mySqlEscapingIsRecordedOnTheLiteral() throws JSQLParserException {
        CreateTable mySql =
                (CreateTable) parse("CREATE TABLE t (id INT) COMMENT 'a\\\\b'", Dialect.MYSQL);
        assertEquals(StringValue.Escaping.MYSQL_BACKSLASH,
                mySql.getTableOptions().get(0).getComment().getEscaping());
        CreateTable standard = (CreateTable) parse("CREATE TABLE t (id INT) COMMENT 'a\\b'", null);
        assertEquals(StringValue.Escaping.STANDARD,
                standard.getTableOptions().get(0).getComment().getEscaping());
        assertEquals("a\\b", standard.getTableOptions().get(0).getComment().getNotExcapedValue());
    }

    @Test
    void alterCommentsKeepTheirPositionAmongOtherActions() throws JSQLParserException {
        Alter alter = (Alter) parse(
                "ALTER TABLE t MODIFY id INT COMMENT 'col', ENGINE=InnoDB, comment='it\\'s'",
                Dialect.MYSQL);
        List<AlterExpression> actions = alter.getAlterExpressions();
        TableOption comment =
                ((AlterExpressionTableOption) actions.get(2)).getStructuredTableOption();
        assertEquals("comment", comment.getName());
        assertTrue(comment.isUseEquals());
        assertEquals("it's", comment.getComment().getNotExcapedValue());
        assertNull(((AlterExpressionTableOption) actions.get(1)).getStructuredTableOption()
                .getComment());
        assertRoundTrip(alter, Dialect.MYSQL);

        Alter algorithm =
                (Alter) parse("ALTER TABLE t COMMENT 'x', ALGORITHM=INSTANT", Dialect.MYSQL);
        AlterExpressionTableOption action =
                (AlterExpressionTableOption) algorithm.getAlterExpressions().get(0);
        assertEquals(AlterOperation.COMMENT, action.getOperation());
        assertEquals("x", action.getStructuredTableOption().getComment().getNotExcapedValue());
        assertRoundTrip(algorithm, Dialect.MYSQL);
    }

    @Test
    void valuesThatAreNotStringLiteralsRemainPlainValues() throws JSQLParserException {
        // Accepted leniently; without the MySQL dialect "x" is a quoted identifier
        for (String sql : new String[] {"CREATE TABLE t (id INT) COMMENT = \"x\"",
                "CREATE TABLE t (id INT) COMMENT plain"}) {
            CreateTable create = (CreateTable) parse(sql, null);
            TableOption option = create.getTableOptions().get(0);
            assertEquals(TableOption.Kind.COMMENT, option.getKind());
            assertNull(option.getComment());
            assertEquals(sql.substring(sql.lastIndexOf(' ') + 1), option.getValue());
            assertRoundTrip(create, null);
        }
        CreateTable mySql =
                (CreateTable) parse("CREATE TABLE t (id INT) COMMENT = \"x\"", Dialect.MYSQL);
        assertEquals("x", mySql.getTableOptions().get(0).getComment().getNotExcapedValue());
    }

    @Test
    void commentLiteralEditsAreRenderedAndVisited() throws JSQLParserException {
        CreateTable create =
                (CreateTable) parse("CREATE TABLE t (id INT) comment = 'old'", Dialect.MYSQL);
        TableOption option = create.getTableOptions().get(0);
        StringValue replacement = new StringValue("'new'");
        option.setComment(replacement);
        assertSame(replacement, option.getComment());
        assertEquals("comment = 'new'", option.toString());
        replacement.setValue("it''s");
        assertEquals("CREATE TABLE t (id INT) comment = 'it''s'", create.toString());
        assertEquals(List.of("comment", "=", "'it''s'"), option.getTokens());
        assertRoundTrip(create, Dialect.MYSQL);

        StringBuilder customized = new StringBuilder();
        option.appendTo(customized, expression -> customized.append("'printed'"));
        assertEquals("comment = 'printed'", customized.toString());
        List<Expression> visited = new ArrayList<>();
        TableDefinitionTraversal.visit(option, visited::add, table -> {
        });
        assertEquals(List.of(replacement), visited);
        assertEquals(Set.of("t"), new TablesNamesFinder().getTables(create));
    }

    @Test
    void commentFactoryAndReplacementOfOtherOptions() throws JSQLParserException {
        TableOption created = TableOption.comment(new StringValue("'x'"));
        assertEquals(TableOption.Kind.COMMENT, created.getKind());
        assertEquals("COMMENT", created.getName());
        assertFalse(created.isUseEquals());
        assertEquals("COMMENT 'x'", created.toString());
        assertEquals("COMMENT = 'x'", created.withUseEquals(true).toString());

        CreateTable create =
                (CreateTable) parse("CREATE TABLE t (id INT) ENGINE=InnoDB", Dialect.MYSQL);
        TableOption option = create.getTableOptions().get(0);
        option.withComment(new StringValue("'x'"));
        assertEquals(TableOption.Kind.COMMENT, option.getKind());
        assertEquals("COMMENT", option.getName());
        assertTrue(option.isUseEquals());
        assertEquals("CREATE TABLE t (id INT) COMMENT = 'x'", create.toString());
        assertTrue(create.getTableOption(TableOption.Kind.ENGINE).isEmpty());
        assertRoundTrip(create, Dialect.MYSQL);

        option.setComment(null);
        assertEquals(TableOption.Kind.COMMENT, option.getKind());
        assertNull(option.getValue());
        assertEquals("COMMENT", option.toString());
    }

    @Test
    void scalarAndRawSettersReplaceTheLiteralWithoutParsing() throws JSQLParserException {
        CreateTable create =
                (CreateTable) parse("CREATE TABLE t (id INT) COMMENT='it\\'s'", Dialect.MYSQL);
        TableOption option = create.getTableOptions().get(0);
        option.setValue("'plain'");
        assertNull(option.getComment());
        assertEquals(TableOption.Kind.COMMENT, option.getKind());
        assertEquals("COMMENT = 'plain'", option.toString());
        assertRoundTrip(create, Dialect.MYSQL);

        option.setComment(new StringValue("'typed'"));
        option.setKind(TableOption.Kind.OTHER);
        assertNull(option.getComment());
        assertEquals("'typed'", option.getValue());
        option.setKind(TableOption.Kind.COMMENT);
        assertNull(option.getComment());
        assertEquals("COMMENT = 'typed'", option.toString());

        option.setComment(new StringValue("'typed'"));
        List<String> raw = new ArrayList<>(List.of("COMMENT", "'raw'"));
        option.setTokens(raw);
        assertNull(option.getComment());
        assertEquals(TableOption.Kind.OTHER, option.getKind());
        assertSame(raw, option.getTokens());
        option.setTokens(null);
        assertNull(option.getComment());

        option.setTokens(raw);
        option.setComment(new StringValue("'typed'"));
        assertEquals(List.of("COMMENT", "'typed'"), option.getTokens());
        assertRoundTrip(create, Dialect.MYSQL);

        AlterExpressionTableOption action = (AlterExpressionTableOption) ((Alter) parse(
                "ALTER TABLE t COMMENT 'old'", Dialect.MYSQL)).getAlterExpressions().get(0);
        action.setCommentText("'new'");
        assertNull(action.getStructuredTableOption().getComment());
        assertEquals("'new'", action.getCommentText());
    }

    @Test
    void partitionAndSubpartitionCommentsDecodeToTheStoredValue() throws JSQLParserException {
        String sql = "CREATE TABLE t (id INT, d INT) PARTITION BY RANGE (id) "
                + "SUBPARTITION BY HASH (d) (PARTITION p0 VALUES LESS THAN (10) "
                + "COMMENT = 'it\\'s' (SUBPARTITION s0 COMMENT 'sub0', "
                + "SUBPARTITION s1 COMMENT = 'line1\\nline2'), "
                + "PARTITION p1 VALUES LESS THAN MAXVALUE COMMENT '사용자 😀' "
                + "(SUBPARTITION s2, SUBPARTITION s3 COMMENT 'a\\\\b'))";
        CreateTable create = (CreateTable) parse(sql, Dialect.MYSQL);
        List<PartitionDefinition> partitions =
                create.getPartitioning().getPartitionDefinitions();
        assertPartitionComment(partitions.get(0), "'it\\'s'", "it's", true);
        assertPartitionComment(partitions.get(0).getSubPartitionDefinitions().get(0), "'sub0'",
                "sub0", false);
        assertPartitionComment(partitions.get(0).getSubPartitionDefinitions().get(1),
                "'line1\\nline2'", "line1\nline2", true);
        assertPartitionComment(partitions.get(1), "'사용자 😀'", "사용자 😀", false);
        assertNull(partitions.get(1).getSubPartitionDefinitions().get(0).getCommentValue());
        assertNull(partitions.get(1).getSubPartitionDefinitions().get(0).getComment());
        assertPartitionComment(partitions.get(1).getSubPartitionDefinitions().get(1), "'a\\\\b'",
                "a\\b", false);
        assertEquals(StringValue.Escaping.MYSQL_BACKSLASH,
                partitions.get(0).getCommentValue().getEscaping());
        assertEquals(sql, create.toString());
        assertRoundTrip(create, Dialect.MYSQL);
    }

    @Test
    void alterPartitionCommentsDecodeToTheStoredValue() throws JSQLParserException {
        for (Dialect dialect : new Dialect[] {Dialect.MYSQL, null}) {
            Alter add = (Alter) parse("ALTER TABLE t ADD PARTITION (PARTITION p1 VALUES "
                    + "LESS THAN (20) COMMENT 'it''s added')", dialect);
            PartitionDefinition added =
                    add.getAlterExpressions().get(0).getPartitionDefinitions().get(0);
            assertPartitionComment(added, "'it''s added'", "it's added", false);
            assertRoundTrip(add, dialect);

            Alter reorganize = (Alter) parse("ALTER TABLE t REORGANIZE PARTITION p1 INTO "
                    + "(PARTITION p1a VALUES LESS THAN (15) COMMENT = 'reorg', "
                    + "PARTITION p1b VALUES LESS THAN (20))", dialect);
            List<PartitionDefinition> definitions =
                    reorganize.getAlterExpressions().get(0).getPartitionDefinitions();
            assertPartitionComment(definitions.get(0), "'reorg'", "reorg", true);
            assertNull(definitions.get(1).getCommentValue());
            assertRoundTrip(reorganize, dialect);
        }
    }

    private static void assertPartitionComment(PartitionDefinition definition, String literal,
            String stored, boolean useEquals) {
        assertEquals(stored, definition.getCommentValue().getNotExcapedValue());
        assertEquals(literal, definition.getComment());
        assertEquals(useEquals, definition.isCommentUseEquals());
    }

    @Test
    void partitionCommentStringAccessorsConvertTheLiteral() throws JSQLParserException {
        CreateTable create = (CreateTable) parse("CREATE TABLE t (id INT) PARTITION BY "
                + "RANGE (id) (PARTITION p0 VALUES LESS THAN (10) COMMENT 'old')", Dialect.MYSQL);
        PartitionDefinition definition = create.getPartitioning().getPartitionDefinitions().get(0);

        definition.setComment("'it''s'");
        assertInstanceOf(StringValue.class, definition.getCommentValue());
        assertEquals(StringValue.Escaping.STANDARD, definition.getCommentValue().getEscaping());
        assertEquals("it's", definition.getCommentValue().getNotExcapedValue());
        assertEquals("'it''s'", definition.getComment());
        assertRoundTrip(create, Dialect.MYSQL);

        StringValue escaped = new StringValue("'a\\nb'")
                .withEscaping(StringValue.Escaping.MYSQL_BACKSLASH);
        definition.setCommentValue(escaped);
        definition.setCommentUseEquals(true);
        assertSame(escaped, definition.getCommentValue());
        assertEquals("'a\\nb'", definition.getComment());
        assertEquals("PARTITION p0 VALUES LESS THAN (10) COMMENT = 'a\\nb'",
                definition.toString());
        assertRoundTrip(create, Dialect.MYSQL);

        definition.setComment(null);
        assertNull(definition.getCommentValue());
        assertEquals("PARTITION p0 VALUES LESS THAN (10)", definition.toString());
    }

    private static Statement parse(String sql, Dialect dialect) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sql, parser -> {
            if (dialect != null) {
                parser.withDialect(dialect);
            }
        });
    }

    private static void assertRoundTrip(Statement statement, Dialect dialect)
            throws JSQLParserException {
        StringBuilder out = new StringBuilder();
        statement.accept(new StatementDeParser(out));
        assertEquals(statement.toString(), out.toString());
        assertEquals(out.toString(), parse(out.toString(), dialect).toString());
    }
}

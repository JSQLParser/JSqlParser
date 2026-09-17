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

import static org.junit.jupiter.api.Assertions.*;
import static net.sf.jsqlparser.util.validation.ValidationTestAsserts.validateNotAllowed;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.ColumnOption;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.GeneratedColumnDefinition;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class ColumnAttributeTest {
    static Stream<Arguments> attributes() {
        return Stream.of(
                Arguments.of("NOT NULL", ColumnOption.Kind.NULLABILITY),
                Arguments.of("NULL", ColumnOption.Kind.NULLABILITY),
                Arguments.of("AUTO_INCREMENT", ColumnOption.Kind.AUTO_INCREMENT),
                Arguments.of("VISIBLE", ColumnOption.Kind.VISIBILITY),
                Arguments.of("INVISIBLE", ColumnOption.Kind.VISIBILITY),
                Arguments.of("COLLATE utf8mb4_bin", ColumnOption.Kind.COLLATE),
                Arguments.of("COLLATE `a.b`", ColumnOption.Kind.COLLATE),
                Arguments.of("COMMENT 'quote''s \\n body'", ColumnOption.Kind.COMMENT),
                Arguments.of("COMMENT ''", ColumnOption.Kind.COMMENT),
                Arguments.of("ON UPDATE CURRENT_TIMESTAMP", ColumnOption.Kind.ON_UPDATE),
                Arguments.of("ON UPDATE CURRENT_TIMESTAMP(6)", ColumnOption.Kind.ON_UPDATE),
                Arguments.of("ON UPDATE NOW()", ColumnOption.Kind.ON_UPDATE),
                Arguments.of("PRIMARY KEY", ColumnOption.Kind.CONSTRAINT));
    }

    @ParameterizedTest
    @MethodSource("attributes")
    void createAndAlterStructureAttributes(String attribute, ColumnOption.Kind kind)
            throws Exception {
        for (String sql : statements("VARCHAR(20) " + attribute)) {
            for (Statement statement : roundTrip(sql, Dialect.MYSQL)) {
                ColumnOption option = column(statement).getColumnOptions().get(0);
                assertEquals(kind, option.getKind());
                assertEquals(attribute, option.toString());
                assertNull(option.getDefaultExpression());
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"x + 1", "COALESCE(x, 0) * 2", "CASE WHEN x > 0 THEN x ELSE 0 END",
            "CAST(x AS CHAR(10))", "CONCAT('prefix,', x)", "((x + 1) * 2)"})
    void generatedExpressionsKeepPrefixStorageAndFollowingOptions(String expression)
            throws Exception {
        for (String prefix : List.of("", "GENERATED ALWAYS ")) {
            for (String storage : List.of("", " STORED", " VIRTUAL")) {
                for (String sql : statements("INT " + prefix + "AS (" + expression + ")" + storage
                        + " NOT NULL COMMENT 'body'")) {
                    for (Statement statement : roundTrip(sql, Dialect.MYSQL)) {
                        List<ColumnOption> options = column(statement).getColumnOptions();
                        assertEquals(
                                List.of(ColumnOption.Kind.GENERATED, ColumnOption.Kind.NULLABILITY,
                                        ColumnOption.Kind.COMMENT),
                                options.stream().map(ColumnOption::getKind)
                                        .collect(Collectors.toList()));
                        GeneratedColumnDefinition generated =
                                options.get(0).getGeneratedDefinition();
                        assertEquals(!prefix.isEmpty(), generated.isGeneratedAlways());
                        assertEquals(
                                storage.isEmpty() ? null
                                        : GeneratedColumnDefinition.Storage.valueOf(storage.trim()),
                                generated.getStorage());
                        assertEquals(CCJSqlParserUtil.parseExpression(expression).toString(),
                                generated.getExpression().toString());
                    }
                }
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"\"C\"", "pg_catalog.\"C\"", "\"s.p\".\"a.b\""})
    void postgresCollationAndCharsetAreSeparateAttributes(String name) throws Exception {
        for (Statement statement : roundTrip(
                "CREATE TABLE t (c TEXT COLLATE " + name + " NOT NULL)", Dialect.POSTGRESQL)) {
            assertEquals(name, column(statement).getColumnOptions().get(0).getCollation());
        }
        Statement mysql =
                parse("CREATE TABLE t (c VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin)",
                        Dialect.MYSQL);
        assertEquals("utf8mb4", column(mysql).getColDataType().getCharacterSet());
        assertEquals("utf8mb4_bin", column(mysql).getColumnOptions().get(0).getCollation());
    }

    @Test
    void keepsIdentityReferencesDefaultsAndRawExtensionsDistinct() throws Exception {
        Statement statement = parse(
                "CREATE TABLE t (c INT NOT NULL DEFAULT 0 ON UPDATE CURRENT_TIMESTAMP(6) "
                        + "REFERENCES parent(id) ON UPDATE CASCADE COMMENT 'body' COLUMN_FORMAT FIXED)",
                Dialect.MYSQL);
        List<ColumnOption> options = column(statement).getColumnOptions();
        assertEquals(List.of(ColumnOption.Kind.NULLABILITY, ColumnOption.Kind.DEFAULT,
                ColumnOption.Kind.ON_UPDATE, ColumnOption.Kind.REFERENCE,
                ColumnOption.Kind.COMMENT),
                options.subList(0, 5).stream().map(ColumnOption::getKind)
                        .collect(Collectors.toList()));
        assertTrue(options.stream().skip(5).allMatch(o -> o.getKind() == ColumnOption.Kind.OTHER));
        for (String definition : List.of("INT GENERATED ALWAYS AS IDENTITY",
                "INT GENERATED BY DEFAULT AS IDENTITY")) {
            assertEquals(ColumnOption.Kind.IDENTITY,
                    column(parse("CREATE TABLE t (c " + definition + ")", Dialect.POSTGRESQL))
                            .getColumnOptions().get(0).getKind());
        }
    }

    @Test
    void mutationsUpdateLegacyTokensAndBothRenderers() throws Exception {
        Statement statement =
                parse("CREATE TABLE t (c INT NOT NULL INVISIBLE COMMENT 'old' AS (x + 1) STORED)",
                        Dialect.MYSQL);
        List<ColumnOption> options = column(statement).getColumnOptions();
        options.get(0).setNullable(true);
        options.get(1).setVisible(true);
        options.get(2).setComment(new StringValue("'new'"));
        options.get(3).getGeneratedDefinition().setExpression(new LongValue(42));
        options.get(3).getGeneratedDefinition()
                .setStorage(GeneratedColumnDefinition.Storage.VIRTUAL);
        assertEquals(List.of("NULL", "VISIBLE", "COMMENT", "'new'", "AS", "(42)", "VIRTUAL"),
                column(statement).getColumnSpecs());
        roundTrip(statement.toString(), Dialect.MYSQL);
        column(statement).addColumnSpecs("CUSTOM");
        assertEquals(ColumnOption.Kind.OTHER, options.get(options.size() - 1).getKind());
        column(statement).setColumnSpecs(List.of("NOT", "NULL"));
        assertNull(column(statement).getColumnOptions());
        assertEquals("CREATE TABLE t (c INT NOT NULL)", statement.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREATE TABLE t (c INT AS (1 + 2) COMMENT 'body')",
            "ALTER TABLE t ADD c INT AS (1 + 2) COMMENT 'body'"})
    void visitorsAndCustomDeparsersReachExpressionsWithContext(String sql) throws Exception {
        Statement statement = parse(sql, Dialect.MYSQL);
        List<String> visited = new ArrayList<>();
        ExpressionVisitorAdapter<Void> expressions = new ExpressionVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(LongValue value, S context) {
                assertEquals("ctx", context);
                visited.add(value.toString());
                return null;
            }

            @Override
            public <S> Void visit(StringValue value, S context) {
                assertEquals("ctx", context);
                visited.add(value.getValue());
                return null;
            }
        };
        statement.accept(new StatementVisitorAdapter<>(new SelectVisitorAdapter<>(expressions)),
                "ctx");
        assertEquals(List.of("1", "2", "body"), visited);
        StringBuilder builder = new StringBuilder();
        ExpressionDeParser printer = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(LongValue value, S context) {
                return getBuilder().append(value.getValue() + 10);
            }
        };
        statement.accept(new StatementDeParser(printer, new SelectDeParser(), builder), null);
        assertEquals(statement.toString().replace("1 + 2", "11 + 12"), builder.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"AS (?)", "ON UPDATE ?"})
    void validationReachesNewExpressions(String attribute) {
        validateNotAllowed("CREATE TABLE t (c INT " + attribute + ")", 1, 1, FeaturesAllowed.DDL,
                Feature.jdbcParameter);
    }

    @ParameterizedTest
    @ValueSource(
            strings = {"COMMENT", "COLLATE", "ON UPDATE", "AS (1 +)", "GENERATED ALWAYS AS (1 +)"})
    void rejectsIncompleteStructuredAttributes(String attribute) {
        assertThrows(JSQLParserException.class,
                () -> parse("CREATE TABLE t (c INT " + attribute + ")", Dialect.MYSQL));
    }

    private static List<String> statements(String definition) {
        return List.of("CREATE TABLE t (c " + definition + ")",
                "ALTER TABLE t ADD COLUMN c " + definition,
                "ALTER TABLE t MODIFY COLUMN c " + definition,
                "ALTER TABLE t CHANGE COLUMN old_c c " + definition);
    }

    private static Statement parse(String sql, Dialect dialect) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sql, p -> p.withDialect(dialect));
    }

    private static ColumnDefinition column(Statement statement) {
        return statement instanceof CreateTable
                ? ((CreateTable) statement).getColumnDefinitions().get(0)
                : ((Alter) statement).getAlterExpressions().get(0).getColDataTypeList().get(0);
    }

    private static List<Statement> roundTrip(String sql, Dialect dialect)
            throws JSQLParserException {
        Statement statement = parse(sql, dialect);
        StringBuilder builder = new StringBuilder();
        statement.accept(new StatementDeParser(builder), null);
        assertEquals(statement.toString(), builder.toString());
        return List.of(statement, parse(statement.toString(), dialect),
                parse(builder.toString(), dialect));
    }
}

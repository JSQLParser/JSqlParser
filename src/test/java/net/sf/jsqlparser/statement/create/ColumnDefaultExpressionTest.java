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

import static net.sf.jsqlparser.util.validation.ValidationTestAsserts.validateNotAllowed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.ColumnOption;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ColumnDefaultExpressionTest {
    @ParameterizedTest
    @ValueSource(strings = {"1", "-1", "NULL", "true", "CURRENT_TIMESTAMP(6)",
            "nextval('app.counter'::regclass)", "'value'::character varying", "ARRAY[1, 2]::int[]",
            "(1 + 2 * 3)", "(UUID_TO_BIN(UUID()))", "(JSON_ARRAY())",
            "(CURRENT_DATE + INTERVAL 1 YEAR)", "CASE WHEN 1 = 1 THEN 2 ELSE 3 END",
            "1 > 0", "1 NOT IN (2, 3)"})
    void createAndAlterKeepDefaultsSeparateFromFollowingConstraints(String value)
            throws JSQLParserException {
        Expression expected = CCJSqlParserUtil.parseExpression(value);
        for (String prefix : Arrays.asList("CREATE TABLE t (a INT DEFAULT ",
                "ALTER TABLE t ADD COLUMN a INT DEFAULT ",
                "ALTER TABLE t MODIFY COLUMN a INT DEFAULT ")) {
            Statement statement = CCJSqlParserUtil.parse(prefix + value + " NOT NULL"
                    + (prefix.startsWith("CREATE") ? ", b INT)" : ", ADD COLUMN b INT"));
            ColumnDefinition column = firstColumn(statement);
            ColumnOption option = defaultOption(column);
            assertEquals(expected.getClass(), option.getDefaultExpression().getClass());
            assertEquals(expected.toString(), option.getDefaultExpression().toString());
            assertThat(column.getColumnSpecs()).containsExactly("DEFAULT", expected.toString(),
                    "NOT", "NULL");
            assertRoundTrip(statement);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE TABLE t (a INT DEFAULT (1 + 2), b INT DEFAULT 3)",
            "ALTER TABLE t ADD (a INT DEFAULT (1 + 2), b INT DEFAULT 3)",
            "ALTER TABLE t MODIFY (a INT DEFAULT (1 + 2), b INT DEFAULT 3)",
            "ALTER TABLE t ADD a INT DEFAULT (1 + 2), MODIFY b INT DEFAULT 3"})
    void visitorReachesEachDefaultOnceWithContext(String sql) throws JSQLParserException {
        List<Long> values = new ArrayList<>();
        ExpressionVisitorAdapter<Void> expressions = new ExpressionVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(LongValue value, S context) {
                assertEquals("context", context);
                values.add(value.getValue());
                return null;
            }
        };
        CCJSqlParserUtil.parse(sql)
                .accept(new StatementVisitorAdapter<>(new SelectVisitorAdapter<>(expressions)),
                        "context");
        assertThat(values).containsExactly(1L, 2L, 3L);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE TABLE t (a INT DEFAULT (1 + 2) NOT NULL)",
            "CREATE TABLE t OF row_type (PRIMARY KEY (a), a WITH OPTIONS DEFAULT (1 + 2))",
            "ALTER TABLE t ADD COLUMN IF NOT EXISTS a INT DEFAULT (1 + 2)",
            "ALTER TABLE t ADD (a INT DEFAULT 1, b INT DEFAULT 2)",
            "ALTER TABLE t MODIFY (a INT DEFAULT 1, b INT DEFAULT 2)",
            "ALTER TABLE t MODIFY (a INT DEFAULT (1 + 2))",
            "ALTER TABLE t CHANGE COLUMN old_a a INT DEFAULT (1 + 2) AFTER id",
            "ALTER TABLE t MODIFY COLUMN a INT DEFAULT (1 + 2) FIRST",
            "ALTER TABLE t ALTER COLUMN a TYPE INT USING (1 + 2)"})
    void sharedRenderingUsesCustomExpressionPrinterAndPreservesAlterLayout(String sql)
            throws JSQLParserException {
        Statement statement = parse(sql);
        StringBuilder output = new StringBuilder();
        ExpressionDeParser expressions = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(LongValue value, S context) {
                return getBuilder().append(value.getValue() + 100);
            }
        };
        statement.accept(new StatementDeParser(expressions, new SelectDeParser(), output), null);
        assertEquals(statement.toString().replace("1", "101").replace("2", "102"),
                output.toString());
        assertRoundTrip(statement);
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREATE TABLE t (a INT DEFAULT (1 + 2) NOT NULL)",
            "ALTER TABLE t ADD COLUMN a INT DEFAULT (1 + 2) NOT NULL"})
    void replacingDefaultUpdatesLegacyTokensAndBothRenderers(String sql)
            throws JSQLParserException {
        Statement statement = parse(sql);
        ColumnDefinition column = firstColumn(statement);
        defaultOption(column).setDefaultExpression(new LongValue(42));
        assertThat(column.getColumnSpecs()).containsExactly("DEFAULT", "42", "NOT", "NULL");
        assertEquals(sql.replace("(1 + 2)", "42"), statement.toString());
        assertRoundTrip(statement);
        column.addColumnSpecs("UNIQUE");
        assertNotNull(defaultOption(column).getDefaultExpression());
        assertRoundTrip(statement);
        column.setColumnSpecs(Arrays.asList("DEFAULT", "9"));
        assertEquals(ColumnOption.Kind.OTHER, column.getColumnOptions().get(0).getKind());
        assertEquals(sql.replace("(1 + 2) NOT NULL", "9"), statement.toString());
        assertRoundTrip(statement);
    }

    @Test
    void identityAndSerialDefaultsKeepTheirOwnOptionKinds() throws JSQLParserException {
        ColumnDefinition identity = firstColumn(CCJSqlParserUtil.parse(
                "CREATE TABLE t (a INT GENERATED BY DEFAULT AS IDENTITY)"));
        assertEquals(ColumnOption.Kind.IDENTITY, identity.getColumnOptions().get(0).getKind());
        assertNull(identity.getColumnOptions().get(0).getDefaultExpression());
        ColumnDefinition serial = firstColumn(CCJSqlParserUtil.parse(
                "CREATE TABLE t (a INT SERIAL DEFAULT VALUE)"));
        assertEquals(ColumnOption.Kind.SERIAL_DEFAULT_VALUE,
                serial.getColumnOptions().get(0).getKind());
        assertNull(serial.getColumnOptions().get(0).getDefaultExpression());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE TABLE t (a TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)",
            "CREATE TABLE t (a INT DEFAULT ON NULL 1 NOT NULL)",
            "CREATE TABLE t (a INT WITH DEFAULT)",
            "CREATE TABLE t (a INT DEFAULT NOT NULL)",
            "CREATE TABLE t (a INT NOT NULL WITH DEFAULT, b INT DEFAULT)",
            "CREATE TABLE t (a INT CONSTRAINT df DEFAULT 1 REFERENCES parent(id))",
            "ALTER TABLE t MODIFY (COLUMN a DROP DEFAULT, COLUMN b DROP DEFAULT)"})
    void neighboringAndLegacyColumnOptionsStillRoundTrip(String sql) throws JSQLParserException {
        assertRoundTrip(CCJSqlParserUtil.parse(sql));
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREATE TABLE t (a INT DEFAULT (1 + 2",
            "CREATE TABLE t (a INT DEFAULT (1 +))",
            "ALTER TABLE t ADD a INT DEFAULT (1 +), ADD b INT"})
    void rejectsIncompleteDefaultExpressions(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql));
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREATE TABLE t (a INT DEFAULT ?)",
            "ALTER TABLE t ADD COLUMN a INT DEFAULT ?"})
    void validationChecksStructuredDefaultExpressions(String sql) {
        validateNotAllowed(sql, 1, 1, FeaturesAllowed.DDL, Feature.jdbcParameter);
    }

    private static Statement parse(String sql) throws JSQLParserException {
        return sql.contains(" OF ")
                ? CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(Dialect.POSTGRESQL))
                : CCJSqlParserUtil.parse(sql);
    }

    private static ColumnDefinition firstColumn(Statement statement) {
        return statement instanceof CreateTable
                ? ((CreateTable) statement).getColumnDefinitions().get(0)
                : ((Alter) statement).getAlterExpressions().get(0).getColDataTypeList().get(0);
    }

    private static ColumnOption defaultOption(ColumnDefinition column) {
        return column.getColumnOptions().stream()
                .filter(option -> option.getKind() == ColumnOption.Kind.DEFAULT)
                .findFirst().orElseThrow(AssertionError::new);
    }

    private static void assertRoundTrip(Statement statement) throws JSQLParserException {
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(output), null);
        assertEquals(statement.toString(), output.toString());
        assertEquals(statement.toString(), parse(output.toString()).toString());
    }
}

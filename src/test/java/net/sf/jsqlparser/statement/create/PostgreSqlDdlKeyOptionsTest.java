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

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.ExtractExpression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.parser.feature.FeatureConfiguration;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Sequence;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterExpressionPartition;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.IdentityDefinition;
import net.sf.jsqlparser.statement.create.table.KeyElement;
import net.sf.jsqlparser.statement.create.table.PartitionBound;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** PostgreSQL DDL options verified against PostgreSQL 18.6. */
class PostgreSqlDdlKeyOptionsTest {
    @Test
    void keepsSpecialIndexKeyExpressionsAndAttributes() throws JSQLParserException {
        CreateIndex index = (CreateIndex) checked("CREATE INDEX ix ON t "
                + "(CAST(n AS bigint) int8_ops DESC NULLS LAST, EXTRACT(YEAR FROM d), lower(label))");
        List<KeyElement> keys = index.getIndex().getColumns();
        assertEquals(3, keys.size());
        CastExpression cast = assertInstanceOf(CastExpression.class, keys.get(0).getExpression());
        assertEquals("n", assertInstanceOf(Column.class, cast.getLeftExpression()).getColumnName());
        assertEquals("bigint", cast.getColDataType().getDataType());
        assertEquals("int8_ops", keys.get(0).getOperatorClass());
        assertEquals(KeyElement.SortOrder.DESC, keys.get(0).getSortOrder());
        assertEquals(KeyElement.NullOrdering.LAST, keys.get(0).getNullOrdering());
        ExtractExpression extract =
                assertInstanceOf(ExtractExpression.class, keys.get(1).getExpression());
        assertEquals("YEAR", extract.getName());
        assertEquals("d", assertInstanceOf(Column.class, extract.getExpression()).getColumnName());
        assertInstanceOf(Function.class, keys.get(2).getExpression());
        keys.forEach(key -> assertFalse(key.isExpressionParenthesized()));
        assertEquals(List.of("n", "d", "label"), visitedColumns(index));
        assertEquals(java.util.Set.of("t"), new TablesNamesFinder<>().getTables(index));
        roundTrip(index);
    }

    @ParameterizedTest
    @ValueSource(strings = {"CAST(n AS bigint)", "EXTRACT(YEAR FROM d)", "lower(label)"})
    void sharesSpecialFunctionsWithPartitionKeys(String expression) throws JSQLParserException {
        CreateTable table = (CreateTable) checked("CREATE TABLE t (n integer, d date, label text) "
                + "PARTITION BY RANGE (" + expression + ")");
        Expression key = table.getPartitioning().getExpression();
        assertEquals(expression, key.toString());
        Class<? extends Expression> expressionType = expression.startsWith("CAST")
                ? CastExpression.class
                : expression.startsWith("EXTRACT") ? ExtractExpression.class : Function.class;
        assertInstanceOf(expressionType, key);
        roundTrip(table);
        String operatorClass = key instanceof CastExpression ? "int8_ops"
                : key instanceof ExtractExpression ? "numeric_ops" : "text_ops";
        CreateTable attributed =
                (CreateTable) checked("CREATE TABLE t (n integer, d date, label text) "
                        + "PARTITION BY RANGE (" + expression + " " + operatorClass + ")");
        assertEquals(operatorClass,
                attributed.getPartitioning().getKeyColumns().get(0).getOperatorClass());
        assertFalse(attributed.getPartitioning().getKeyColumns().get(0)
                .isExpressionParenthesized());
        roundTrip(attributed);
    }

    @Test
    void specialFunctionNamesStillWorkAsIdentifiers() throws JSQLParserException {
        CreateIndex index =
                (CreateIndex) checked("CREATE INDEX ix ON t (extract, \"CAST\", \"EXTRACT\")");
        assertEquals(List.of("extract", "\"CAST\"", "\"EXTRACT\""), index.getIndex().getColumns()
                .stream().map(KeyElement::getColumnName)
                .collect(java.util.stream.Collectors.toList()));
        index.getIndex().getColumns().forEach(key -> assertNull(key.getExpression()));
        roundTrip(checked("CREATE TABLE t (extract integer) PARTITION BY RANGE (extract)"));
        CreateIndex calls =
                (CreateIndex) checked("CREATE INDEX ix ON t (\"CAST\"(n), \"EXTRACT\"(d))");
        calls.getIndex().getColumns()
                .forEach(key -> assertInstanceOf(Function.class, key.getExpression()));
    }

    @Test
    void expressionVisitorsAndDeparsersReachSpecialKeys() throws JSQLParserException {
        for (String sql : new String[] {
                "CREATE INDEX ix ON t (CAST(n AS bigint), EXTRACT(YEAR FROM d))",
                "CREATE TABLE t (n integer, d date) PARTITION BY RANGE "
                        + "(CAST(n AS bigint), EXTRACT(YEAR FROM d))"}) {
            Statement statement = checked(sql);
            assertEquals(List.of("n", "d"), visitedColumns(statement));
            StringBuilder output = new StringBuilder();
            ExpressionDeParser expressions = new ExpressionDeParser() {
                @Override
                public <S> StringBuilder visit(Column column, S context) {
                    return getBuilder().append("mapped_").append(column.getColumnName());
                }
            };
            statement.accept(new StatementDeParser(expressions, new SelectDeParser(), output),
                    null);
            assertEquals(sql.replace("CAST(n ", "CAST(mapped_n ")
                    .replace("FROM d)", "FROM mapped_d)"), output.toString());
            roundTrip(parse(output.toString()));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"n + 1", "CAST(n AS bigint) + 1", "EXTRACT(YEAR FROM d) + 1"})
    void arbitraryBareExpressionsStillNeedParentheses(String expression)
            throws JSQLParserException {
        assertThrows(JSQLParserException.class,
                () -> parse("CREATE INDEX ix ON t (" + expression + ")"));
        assertThrows(JSQLParserException.class,
                () -> parse("CREATE TABLE t (n integer, d date) PARTITION BY RANGE ("
                        + expression + ")"));
        roundTrip(checked("CREATE INDEX ix ON t ((" + expression + "))"));
        roundTrip(checked("CREATE TABLE t (n integer, d date) PARTITION BY RANGE (("
                + expression + "))"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"MODULUS 8, REMAINDER 3", "REMAINDER 3, MODULUS 8"})
    void hashBoundsKeepTheirNamedValuesInEitherOrder(String options) throws JSQLParserException {
        for (Dialect dialect : new Dialect[] {null, Dialect.POSTGRESQL}) {
            for (boolean attach : new boolean[] {false, true}) {
                String prefix = attach ? "ALTER TABLE parent ATTACH PARTITION child "
                        : "CREATE TABLE child PARTITION OF parent ";
                Statement statement = parse(prefix + "FOR VALUES WITH (" + options + ")", dialect);
                PartitionBound bound = attach
                        ? ((AlterExpressionPartition) ((Alter) statement).getAlterExpressions()
                                .get(0))
                                .getPartitionBound()
                        : ((CreateTable) statement).getPartitionBound();
                assertEquals(PartitionBound.Type.HASH, bound.getType());
                assertEquals(8, assertInstanceOf(LongValue.class, bound.getModulus()).getValue());
                assertEquals(3, assertInstanceOf(LongValue.class, bound.getRemainder()).getValue());
                assertEquals(prefix + "FOR VALUES WITH (MODULUS 8, REMAINDER 3)",
                        statement.toString());
                roundTrip(statement, dialect);
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"MODULUS 8", "REMAINDER 3", "MODULUS 8, MODULUS 8",
            "REMAINDER 3, REMAINDER 3", "MODULUS 8, REMAINDER 3, MODULUS 8",
            "REMAINDER 3, MODULUS 8, REMAINDER 3"})
    void rejectsMissingAndDuplicateHashBoundOptions(String options) {
        for (String prefix : new String[] {"CREATE TABLE child PARTITION OF parent ",
                "ALTER TABLE parent ATTACH PARTITION child "}) {
            assertThrows(JSQLParserException.class,
                    () -> parse(prefix + "FOR VALUES WITH (" + options + ")"));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"LOGGED", "UNLOGGED"})
    void identityPersistenceIsAnOptionForCreateAndAdd(String persistence)
            throws JSQLParserException {
        String definition = "GENERATED ALWAYS AS IDENTITY (SEQUENCE NAME public.ids "
                + persistence + " START WITH 10 INCREMENT BY 3)";
        CreateTable table = (CreateTable) checked("CREATE TABLE t (id bigint " + definition + ")");
        Alter alter = (Alter) checked("ALTER TABLE t ALTER COLUMN id ADD " + definition);
        IdentityDefinition created = table.getColumnDefinitions().get(0).getColumnOptions().get(0)
                .getIdentityDefinition();
        IdentityDefinition added = alter.getAlterExpressions().get(0).getColDataTypeList().get(0)
                .getIdentityAlterations().get(0).getIdentityDefinition();
        for (IdentityDefinition identity : new IdentityDefinition[] {created, added}) {
            assertEquals(IdentityDefinition.GenerationMode.ALWAYS, identity.getGenerationMode());
            assertEquals(4, identity.getParameters().size());
            Sequence.Parameter option = identity.getParameters().get(1);
            assertEquals(persistence, option.getOption().name());
            assertNull(option.getValue());
            assertNull(option.getSequenceName());
            assertEquals("public.ids", identity.getParameters().get(0).getSequenceName()
                    .getFullyQualifiedName());
            assertEquals(10L, identity.getParameters().get(2).getValue());
            assertEquals(3L, identity.getParameters().get(3).getValue());
        }
        assertFalse(table.isUnlogged());
        roundTrip(table);
        roundTrip(alter);
        FeatureConfiguration configuration = new FeatureConfiguration()
                .setValue(Feature.dialect, Dialect.POSTGRESQL.name());
        assertTrue(new Validation(configuration, List.of(FeaturesAllowed.DDL), table.toString(),
                alter.toString()).validate().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"LOGGED UNLOGGED", "UNLOGGED LOGGED", "LOGGED LOGGED",
            "UNLOGGED UNLOGGED", "LOGGED 1", "UNLOGGED 1"})
    void rejectsConflictingAndMalformedIdentityPersistence(String options) {
        for (String sql : new String[] {
                "CREATE TABLE t (id bigint GENERATED ALWAYS AS IDENTITY (" + options + "))",
                "ALTER TABLE t ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (" + options
                        + ")"}) {
            assertThrows(JSQLParserException.class, () -> parse(sql), sql);
        }
    }

    @Test
    void identityOptionsDoNotChangeStandaloneSequenceGrammar() {
        for (String sql : new String[] {"CREATE SEQUENCE ids LOGGED",
                "CREATE SEQUENCE ids UNLOGGED", "ALTER SEQUENCE ids LOGGED",
                "ALTER SEQUENCE ids UNLOGGED"}) {
            assertThrows(JSQLParserException.class, () -> parse(sql), sql);
        }
    }

    private static List<String> visitedColumns(Statement statement) {
        List<String> columns = new ArrayList<>();
        ExpressionVisitorAdapter<Void> expressions = new ExpressionVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(Column column, S context) {
                assertEquals("context", context);
                columns.add(column.getColumnName());
                return null;
            }
        };
        statement.accept(new StatementVisitorAdapter<>(new SelectVisitorAdapter<>(expressions)),
                "context");
        return columns;
    }

    private static Statement checked(String sql) throws JSQLParserException {
        return assertSqlCanBeParsedAndDeparsed(sql, false,
                parser -> parser.withDialect(Dialect.POSTGRESQL));
    }

    private static Statement parse(String sql) throws JSQLParserException {
        return parse(sql, Dialect.POSTGRESQL);
    }

    private static Statement parse(String sql, Dialect dialect) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sql, parser -> {
            if (dialect != null) {
                parser.withDialect(dialect);
            }
        });
    }

    private static void roundTrip(Statement statement) throws JSQLParserException {
        roundTrip(statement, Dialect.POSTGRESQL);
    }

    private static void roundTrip(Statement statement, Dialect dialect) throws JSQLParserException {
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(output));
        assertEquals(statement.toString(), output.toString());
        assertEquals(output.toString(), parse(output.toString(), dialect).toString());
    }
}

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.SpannerInterleaveIn;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.select.JoinHint;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SampleClause;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

public class SerializationTest {
    @Test
    void serializeWithItem() throws JSQLParserException, IOException, ClassNotFoundException {
        String sqlStr =
                "with sample_data(day, value) as (values ((0, 13), (1, 12), (2, 15), (3, 4), (4, 8), (5, 16))), test2 as (values (1,2,3)) \n"
                        + "select day, value from sample_data as a";

        // Parse the SQL string into a PlainSelect object
        PlainSelect originalSelect = (PlainSelect) CCJSqlParserUtil.parse(sqlStr);

        // Serialize the object to a byte array
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(byteArrayOutputStream)) {
            out.writeObject(originalSelect);
        }

        // Deserialize the object from the byte array
        PlainSelect deserializedSelect;
        try (ObjectInputStream in = new ObjectInputStream(
                new ByteArrayInputStream(byteArrayOutputStream.toByteArray()))) {
            deserializedSelect = (PlainSelect) in.readObject();
        }

        // Verify that the original and deserialized objects are equal
        Assertions.assertEquals(originalSelect.toString(), deserializedSelect.toString(),
                "The deserialized object should be equal to the original");
    }

    @ParameterizedTest
    @ValueSource(strings = {"LOOP", "HASH", "MERGE", "REMOTE"})
    void serializeSqlServerJoinHint(String keyword) throws Exception {
        PlainSelect original = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT a.id FROM a INNER " + keyword + " JOIN b ON a.id = b.id");
        PlainSelect copy = roundTrip(original);
        JoinHint hint = copy.getJoins().get(0).getJoinHint();
        Assertions.assertEquals(keyword, hint.getKeyword());
        Assertions.assertEquals(JoinHint.Position.BEFORE_JOIN, hint.getPosition());
        Assertions.assertEquals("b", ((Table) copy.getJoins().get(0).getFromItem()).getName());
        Assertions.assertEquals("a.id = b.id",
                copy.getJoins().get(0).getOnExpressions().iterator().next().toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"shuffle", "BROADCAST"})
    void serializeDorisJoinHint(String keyword) throws Exception {
        PlainSelect original = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT a.id FROM a LEFT JOIN [" + keyword + "] b USING (id)", false,
                parser -> parser.withDialect(Dialect.DORIS));
        PlainSelect copy = roundTrip(original);
        JoinHint hint = copy.getJoins().get(0).getJoinHint();
        Assertions.assertEquals(keyword, hint.getKeyword());
        Assertions.assertEquals(JoinHint.Position.AFTER_JOIN, hint.getPosition());
        Assertions.assertEquals("id",
                copy.getJoins().get(0).getUsingColumns().get(0).getColumnName());
        Assertions.assertTrue(copy.getJoins().get(0).isLeft());
    }

    static Stream<Arguments> samplingClauses() {
        return Stream.of(
                Arguments.of("TABLESAMPLE BERNOULLI (10)",
                        new SampleClause("TABLESAMPLE", "BERNOULLI", 10L, null, null, null)),
                Arguments.of("TABLESAMPLE SYSTEM (10.5 PERCENT) REPEATABLE (7)",
                        new SampleClause("TABLESAMPLE", "SYSTEM", 10.5, "PERCENT", 7L, null)),
                Arguments.of("SAMPLE (99)",
                        new SampleClause("SAMPLE", null, 99L, null, null, null)),
                Arguments.of("SAMPLE BLOCK (99.1) SEED (10.1)",
                        new SampleClause("SAMPLE", "BLOCK", 99.1, null, null, 10.1)),
                Arguments.of("SAMPLE 0.1 OFFSET 1000",
                        new SampleClause("SAMPLE", null, 0.1, null, null, null, false, 1000L)),
                Arguments.of("USING SAMPLE 10%",
                        new SampleClause("USING SAMPLE", null, 10L, "%", null, null, false, null)),
                Arguments.of("USING SAMPLE 10 ROWS (system, 377)",
                        new SampleClause("USING SAMPLE", "SYSTEM", 10L, "ROWS", null, 377L,
                                false, null).setMethodInBrackets(true)),
                Arguments.of("USING SAMPLE RESERVOIR (50 ROWS)",
                        new SampleClause("USING SAMPLE", "RESERVOIR", 50L, "ROWS", null, null)));
    }

    @ParameterizedTest
    @MethodSource("samplingClauses")
    void serializeTableSampleClause(String clause, SampleClause expected) throws Exception {
        PlainSelect original = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT id FROM events " + clause + " WHERE id > 0");
        assertSampleClause(expected, ((Table) original.getFromItem()).getSampleClause());
        PlainSelect copy = roundTrip(original);
        assertSampleClause(expected, ((Table) copy.getFromItem()).getSampleClause());
        Assertions.assertEquals("events", ((Table) copy.getFromItem()).getName());
        Assertions.assertEquals("id > 0", copy.getWhere().toString());
        Assertions.assertEquals("id", copy.getSelectItem(0).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT * FROM (SELECT id FROM events) TABLESAMPLE SYSTEM (10 PERCENT)",
            "SELECT * FROM (events) TABLESAMPLE SYSTEM (10 PERCENT)",
            "SELECT * FROM events MATCH_RECOGNIZE (PATTERN (A) DEFINE A AS id > 0) "
                    + "TABLESAMPLE SYSTEM (10 PERCENT)"
    })
    void serializeOtherSampleClauseOwners(String sql) throws Exception {
        PlainSelect original = (PlainSelect) CCJSqlParserUtil.parse(sql);
        PlainSelect copy = roundTrip(original);
        assertSampleClause(new SampleClause("TABLESAMPLE", "SYSTEM", 10L, "PERCENT", null, null),
                copy.getFromItem().getSampleClause());
    }

    @Test
    void serializeSampleClauseAfterMutation() throws Exception {
        PlainSelect original = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM events SAMPLE BLOCK (10) SEED (7)");
        SampleClause sample = original.getFromItem().getSampleClause();
        sample.setPercentageArgument(new BigDecimal("12.5"));
        sample.setSeedArgument(new BigDecimal("7.5"));
        PlainSelect copy = roundTrip(original);
        SampleClause copiedSample = copy.getFromItem().getSampleClause();
        assertSampleClause(new SampleClause("SAMPLE", "BLOCK", new BigDecimal("12.5"), null,
                null, new BigDecimal("7.5")), copiedSample);
        Assertions.assertNotSame(sample, copiedSample);
        sample.setSeedArgument(99L);
        Assertions.assertEquals(new BigDecimal("7.5"), copiedSample.getSeedArgument());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ON DELETE CASCADE", " ON DELETE NO ACTION"})
    void serializeSpannerInterleaveIn(String action) throws Exception {
        CreateTable original = (CreateTable) TestUtils.assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE child (id INT) PRIMARY KEY (id), INTERLEAVE IN PARENT parent"
                        + action);
        CreateTable copy = roundTrip(original);
        SpannerInterleaveIn interleave = copy.getSpannerInterleaveIn();
        Assertions.assertEquals("parent", interleave.getTable().getName());
        SpannerInterleaveIn.OnDelete expected = action.isEmpty() ? null
                : action.endsWith("CASCADE") ? SpannerInterleaveIn.OnDelete.CASCADE
                        : SpannerInterleaveIn.OnDelete.NO_ACTION;
        Assertions.assertEquals(expected, interleave.getOnDelete());
        Assertions.assertEquals("child", copy.getTable().getName());
        Assertions.assertEquals("id", copy.getColumnDefinitions().get(0).getColumnName());
        Assertions.assertEquals("INT",
                copy.getColumnDefinitions().get(0).getColDataType().getDataType());
        original.getSpannerInterleaveIn().getTable().setName("changed");
        Assertions.assertEquals("parent", interleave.getTable().getName());
    }

    @Test
    void serializeCombinedHelpers() throws Exception {
        PlainSelect original = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT a.id FROM a TABLESAMPLE SYSTEM (10) INNER LOOP JOIN b "
                        + "ON a.id = b.id JOIN c ON b.id = c.id");
        PlainSelect copy = roundTrip(original);
        Assertions.assertEquals("LOOP", copy.getJoins().get(0).getJoinHint().getKeyword());
        assertSampleClause(new SampleClause("TABLESAMPLE", "SYSTEM", 10L, null, null, null),
                ((Table) copy.getFromItem()).getSampleClause());
        Assertions.assertNull(((Table) copy.getJoins().get(0).getFromItem()).getSampleClause());
        Assertions.assertNull(copy.getJoins().get(1).getJoinHint());
        Assertions.assertNull(((Table) copy.getJoins().get(1).getFromItem()).getSampleClause());
    }

    @Test
    void serializeSelectWithoutHelpers() throws Exception {
        PlainSelect original = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT a.id FROM a INNER JOIN b ON a.id = b.id WHERE b.id > 0");
        PlainSelect copy = roundTrip(original);
        Assertions.assertNull(copy.getJoins().get(0).getJoinHint());
        Assertions.assertNull(((Table) copy.getFromItem()).getSampleClause());
        Assertions.assertEquals("b.id > 0", copy.getWhere().toString());
    }

    @Test
    void serializeCreateTableWithoutInterleave() throws Exception {
        CreateTable original = (CreateTable) TestUtils.assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE ordinary (id INT)");
        CreateTable copy = roundTrip(original);
        Assertions.assertNull(copy.getSpannerInterleaveIn());
        Assertions.assertEquals("ordinary", copy.getTable().getName());
        Assertions.assertEquals("id", copy.getColumnDefinitions().get(0).getColumnName());
    }

    private static void assertSampleClause(SampleClause expected, SampleClause actual) {
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expected.getKeyword(), actual.getKeyword());
        Assertions.assertEquals(expected.getMethod(), actual.getMethod());
        Assertions.assertEquals(expected.getPercentageArgument(), actual.getPercentageArgument());
        Assertions.assertEquals(expected.getPercentageUnit(), actual.getPercentageUnit());
        Assertions.assertEquals(expected.isArgumentInBrackets(), actual.isArgumentInBrackets());
        Assertions.assertEquals(expected.isMethodInBrackets(), actual.isMethodInBrackets());
        Assertions.assertEquals(expected.getOffsetArgument(), actual.getOffsetArgument());
        Assertions.assertEquals(expected.getRepeatArgument(), actual.getRepeatArgument());
        Assertions.assertEquals(expected.getSeedArgument(), actual.getSeedArgument());
    }

    @SuppressWarnings("unchecked")
    private static <T extends Statement> T roundTrip(T original)
            throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(original);
        }
        T copy;
        try (ObjectInputStream in =
                new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            copy = (T) in.readObject();
        }
        Assertions.assertNotSame(original, copy);
        Assertions.assertEquals(original.toString(), copy.toString());
        return copy;
    }

}

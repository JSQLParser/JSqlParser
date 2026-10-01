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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;

class CreateIndexOptionStateTest {
    private static final String SQL = "CREATE UNIQUE INDEX ix ON t (id) INCLUDE (payload) "
            + "NULLS NOT DISTINCT WITH (fillfactor = 80) TABLESPACE fast_space";

    @Test
    void parsedOptionsAreSharedAndIndexEditsReachBothRenderers() throws Exception {
        CreateIndex statement = parse(SQL);
        Index index = statement.getIndex();
        assertSame(statement.getIncludeColumns(), index.getIncludeColumns());
        assertSame(statement.getStorageParameters(), index.getStorageParameters());
        assertEquals(Boolean.FALSE, index.getNullsDistinct());
        assertEquals("fast_space", index.getTableSpace());

        index.getIncludeColumns().add("extra");
        index.setNullsDistinct(true);
        index.getStorageParameters().get(0).setValue(new LongValue(90));
        index.setTableSpace("other_space");

        assertEquals(List.of("payload", "extra"), statement.getIncludeColumns());
        assertEquals(Boolean.TRUE, statement.getNullsDistinct());
        assertEquals("other_space", statement.getTableSpace());
        assertRoundTrip("CREATE UNIQUE INDEX ix ON t (id) INCLUDE (payload, extra) "
                + "NULLS DISTINCT WITH (fillfactor = 90) TABLESPACE other_space", statement);
    }

    @Test
    void statementSettersAndClearsUpdateTheIndexDefinition() throws Exception {
        CreateIndex statement = parse("CREATE UNIQUE INDEX ix ON t (id)");
        statement.setIncludeColumns(List.of("payload"));
        statement.setNullsDistinct(false);
        statement.setStorageParameters(List.of(option(80)));
        statement.setTableSpace("fast_space");
        Index index = statement.getIndex();
        assertSame(statement.getIncludeColumns(), index.getIncludeColumns());
        assertSame(statement.getStorageParameters(), index.getStorageParameters());
        assertEquals(Boolean.FALSE, index.getNullsDistinct());
        assertEquals("fast_space", index.getTableSpace());
        assertRoundTrip(SQL, statement);

        statement.setIncludeColumns(null);
        statement.setNullsDistinct(null);
        statement.setStorageParameters(null);
        statement.setTableSpace(null);
        assertNull(index.getIncludeColumns());
        assertNull(index.getNullsDistinct());
        assertNull(index.getStorageParameters());
        assertNull(index.getTableSpace());
        assertRoundTrip("CREATE UNIQUE INDEX ix ON t (id)", statement);
    }

    @Test
    void optionsCanBeConfiguredBeforeTheIndexWithoutLosingSuppliedOptions() throws Exception {
        CreateIndex statement = new CreateIndex().withTable(new Table("t"))
                .withIncludeColumns(List.of("payload"))
                .withNullsDistinct(false)
                .withStorageParameters(List.of(option(80)))
                .withTableSpace("staged_space");
        assertNull(statement.getIndex());
        assertEquals("staged_space", statement.getTableSpace());

        Index index = new Index().withType("UNIQUE").withName("ix")
                .withColumnsNames(List.of("id"));
        index.setTableSpace("fast_space");
        statement.setIndex(index);

        assertSame(index, statement.getIndex());
        assertSame(statement.getStorageParameters(), index.getStorageParameters());
        assertRoundTrip(SQL, statement);
    }

    @Test
    void replacementOptionsTakePrecedenceAndMissingOptionsSurviveDetachment() throws Exception {
        CreateIndex statement = parse(SQL);
        Index replacement = new Index().withType("UNIQUE").withName("replacement")
                .withColumnsNames(List.of("id"));
        replacement.setIncludeColumns(List.of("extra"));
        replacement.setStorageParameters(List.of(option(90)));
        statement.setIndex(replacement);
        statement.setIndex(replacement);
        assertSame(replacement.getIncludeColumns(), statement.getIncludeColumns());
        assertRoundTrip("CREATE UNIQUE INDEX replacement ON t (id) INCLUDE (extra) "
                + "NULLS NOT DISTINCT WITH (fillfactor = 90) TABLESPACE fast_space", statement);

        statement.setIndex(null);
        statement.setIndex(null);
        assertNull(statement.getIndex());
        assertEquals(List.of("extra"), statement.getIncludeColumns());
        assertEquals(Boolean.FALSE, statement.getNullsDistinct());
        assertEquals("fast_space", statement.getTableSpace());
        statement.setTableSpace(null);
        Index reattached = new Index().withType("UNIQUE").withName("reattached")
                .withColumnsNames(List.of("id"));
        statement.setIndex(reattached);
        assertSame(reattached.getStorageParameters(), statement.getStorageParameters());
        assertNull(reattached.getTableSpace());
        assertRoundTrip("CREATE UNIQUE INDEX reattached ON t (id) INCLUDE (extra) "
                + "NULLS NOT DISTINCT WITH (fillfactor = 90)", statement);
    }

    @Test
    void detachedOptionSettersDoNotMutateTheRemovedIndex() throws Exception {
        CreateIndex statement = parse(SQL);
        Index removed = statement.getIndex();
        statement.setIndex(null);

        assertNotSame(removed.getIncludeColumns(), statement.getIncludeColumns());
        assertNotSame(removed.getStorageParameters(), statement.getStorageParameters());
        assertSame(removed.getStorageParameters().get(0), statement.getStorageParameters().get(0));
        List<String> detachedColumns = statement.getIncludeColumns();
        List<Index.Option> detachedParameters = statement.getStorageParameters();
        statement.setIndex(null);
        assertSame(detachedColumns, statement.getIncludeColumns());
        assertSame(detachedParameters, statement.getStorageParameters());
        statement.getIncludeColumns().add("extra");
        assertEquals(List.of("payload", "extra"), statement.getIncludeColumns());
        assertEquals(List.of("payload"), removed.getIncludeColumns());

        statement.setTableSpace("detached_space");
        statement.setNullsDistinct(true);
        statement.setIncludeColumns(List.of("detached_payload"));
        statement.setStorageParameters(List.of(option(90)));
        assertEquals("fast_space", removed.getTableSpace());
        assertEquals(Boolean.FALSE, removed.getNullsDistinct());
        assertEquals(List.of("payload"), removed.getIncludeColumns());
        assertEquals("80", removed.getStorageParameters().get(0).getValue().toString());

        removed.setTableSpace("external_space");
        removed.setNullsDistinct(null);
        assertEquals("detached_space", statement.getTableSpace());
        assertEquals(Boolean.TRUE, statement.getNullsDistinct());
        statement.setIndex(new Index().withType("UNIQUE").withName("reattached")
                .withColumnsNames(List.of("id")));
        assertRoundTrip("CREATE UNIQUE INDEX reattached ON t (id) INCLUDE (detached_payload) "
                + "NULLS DISTINCT WITH (fillfactor = 90) TABLESPACE detached_space", statement);
    }

    @Test
    void optionsAreVisitedBeforeAttachmentAndAfterDetachment() throws Exception {
        for (CreateIndex statement : List.of(new CreateIndex(), parse(SQL))) {
            statement.setIndex(null);
            statement.setStorageParameters(List.of(option(80)));
            List<Long> values = new ArrayList<>();
            ExpressionVisitorAdapter<Void> expressions = new ExpressionVisitorAdapter<Void>() {
                @Override
                public <S> Void visit(LongValue value, S context) {
                    assertEquals("context", context);
                    values.add(value.getValue());
                    return null;
                }
            };
            statement.accept(new StatementVisitorAdapter<>(new SelectVisitorAdapter<>(expressions)),
                    "context");
            assertEquals(List.of(80L), values);
        }
    }

    @Test
    void indexOptionEditsAreVisitedOnceAndCanBeDeparsedThroughCustomVisitors() throws Exception {
        CreateIndex statement = parse(SQL);
        statement.getIndex().setStorageParameters(List.of(option(90)));
        List<Long> values = new ArrayList<>();
        ExpressionVisitorAdapter<Void> expressions = new ExpressionVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(LongValue value, S context) {
                assertEquals("context", context);
                values.add(value.getValue());
                return null;
            }
        };
        statement.accept(new StatementVisitorAdapter<>(new SelectVisitorAdapter<>(expressions)),
                "context");
        assertEquals(List.of(90L), values);

        StringBuilder output = new StringBuilder();
        ExpressionDeParser deparser = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(LongValue value, S context) {
                return getBuilder().append(value.getValue() + 1);
            }
        };
        statement.accept(new StatementDeParser(deparser, new SelectDeParser(), output));
        assertEquals(SQL.replace("80", "91"), output.toString());
        assertEquals(output.toString(), parse(output.toString()).toString());
        assertEquals(SQL.replace("80", "90"), statement.toString());
    }

    private static Index.Option option(long value) {
        return new Index.Option("fillfactor", new LongValue(value), true);
    }

    private static CreateIndex parse(String sql) throws Exception {
        return (CreateIndex) CCJSqlParserUtil.parse(sql,
                parser -> parser.withDialect(Dialect.POSTGRESQL));
    }

    private static void assertRoundTrip(String expected, CreateIndex statement) throws Exception {
        assertEquals(expected, statement.toString());
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(output));
        assertEquals(expected, output.toString());
        CreateIndex reparsed = parse(expected);
        assertEquals(expected, reparsed.toString());
        assertEquals(statement.getIncludeColumns(), reparsed.getIndex().getIncludeColumns());
        assertEquals(statement.getNullsDistinct(), reparsed.getIndex().getNullsDistinct());
        assertEquals(statement.getTableSpace(), reparsed.getIndex().getTableSpace());
    }
}

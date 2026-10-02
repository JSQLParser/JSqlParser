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

import net.sf.jsqlparser.statement.create.table.IndexOptions;
import net.sf.jsqlparser.statement.create.table.KeyElement;
import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.Index;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DialectIndexDefinitionTest {
    private static CreateIndex parse(String sql, Dialect dialect) throws JSQLParserException {
        return (CreateIndex) CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(dialect));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "UNIQUE ", "CLUSTERED ", "NONCLUSTERED ",
            "UNIQUE CLUSTERED ", "UNIQUE NONCLUSTERED "})
    void parsesSqlServerIndexModifiers(String modifiers) throws Exception {
        String sql = "CREATE " + modifiers
                + "INDEX [index name] ON [dbo].[store] ([manager_staff_id] DESC, [id] ASC)";
        CreateIndex statement = (CreateIndex) assertSqlCanBeParsedAndDeparsed(sql, true,
                parser -> parser.withDialect(Dialect.SQLSERVER));
        Index index = statement.getIndex();
        assertEquals(modifiers.contains("UNIQUE") ? "UNIQUE" : null, index.getType());
        assertEquals(modifiers.contains("NONCLUSTERED") ? IndexOptions.Clustering.NONCLUSTERED
                : modifiers.contains("CLUSTERED") ? IndexOptions.Clustering.CLUSTERED : null,
                index.getClustering());
        assertFalse(statement.isNullFiltered());
        assertEquals(index.getClustering(), parse(statement.toString(), Dialect.SQLSERVER)
                .getIndex().getClustering());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "UNIQUE ", "NULL_FILTERED ", "UNIQUE NULL_FILTERED "})
    void parsesSpannerIndexModifiersIssue652(String modifiers) throws Exception {
        String sql = "CREATE " + modifiers
                + "INDEX IF NOT EXISTS `index name` ON `Users` (`name` DESC, `id` ASC)";
        CreateIndex statement = (CreateIndex) assertSqlCanBeParsedAndDeparsed(sql, true,
                parser -> parser.withDialect(Dialect.SPANNER));
        assertEquals(modifiers.contains("UNIQUE") ? "UNIQUE" : null,
                statement.getIndex().getType());
        assertEquals(modifiers.contains("NULL_FILTERED"), statement.isNullFiltered());
        assertNull(statement.getIndex().getClustering());
        assertEquals(statement.isNullFiltered(),
                parse(statement.toString(), Dialect.SPANNER).isNullFiltered());
    }

    @Test
    void requiresDialectForCombinedModifiersAndPreservesLegacySingleTypes() throws Exception {
        for (String type : List.of("CLUSTERED", "NONCLUSTERED", "NULL_FILTERED", "mytype")) {
            String sql = "CREATE " + type + " INDEX ix ON t (id)";
            CreateIndex legacy = (CreateIndex) assertSqlCanBeParsedAndDeparsed(sql);
            assertEquals(type, legacy.getIndex().getType());
            assertNull(legacy.getIndex().getClustering());
            assertFalse(legacy.isNullFiltered());
        }
        for (Dialect target : List.of(Dialect.SQLSERVER, Dialect.SPANNER)) {
            String sql = "CREATE UNIQUE "
                    + (target == Dialect.SQLSERVER ? "NONCLUSTERED" : "NULL_FILTERED")
                    + " INDEX ix ON t (id)";
            assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql));
            for (Dialect dialect : Dialect.values()) {
                if (dialect != target) {
                    assertThrows(JSQLParserException.class, () -> parse(sql, dialect),
                            dialect.name());
                }
            }
        }
    }

    @Test
    void rejectsDuplicateOrReversedModifiers() {
        for (String modifiers : List.of("UNIQUE CLUSTERED NONCLUSTERED", "NONCLUSTERED UNIQUE",
                "UNIQUE UNIQUE", "UNIQUE NULL_FILTERED")) {
            assertThrows(JSQLParserException.class,
                    () -> parse("CREATE " + modifiers + " INDEX ix ON t (id)", Dialect.SQLSERVER));
        }
        for (String modifiers : List.of("NULL_FILTERED UNIQUE",
                "UNIQUE NULL_FILTERED NULL_FILTERED",
                "UNIQUE UNIQUE", "UNIQUE NONCLUSTERED")) {
            assertThrows(JSQLParserException.class,
                    () -> parse("CREATE " + modifiers + " INDEX ix ON t (id)", Dialect.SPANNER));
        }
    }

    @Test
    void rendersModifiedClusteringAndNullFiltering() throws Exception {
        CreateIndex sqlServer = parse("CREATE UNIQUE NONCLUSTERED INDEX ix ON t (id)",
                Dialect.SQLSERVER);
        sqlServer.getIndex().setClustering(IndexOptions.Clustering.CLUSTERED);
        assertDeparse(sqlServer, "CREATE UNIQUE CLUSTERED INDEX ix ON t (id)");
        sqlServer.getIndex().setClustering(null);
        assertDeparse(sqlServer, "CREATE UNIQUE INDEX ix ON t (id)");

        CreateIndex spanner = parse("CREATE UNIQUE NULL_FILTERED INDEX ix ON t (id)",
                Dialect.SPANNER);
        spanner.setNullFiltered(false);
        assertDeparse(spanner, "CREATE UNIQUE INDEX ix ON t (id)");
        assertDeparse(spanner.withNullFiltered(true),
                "CREATE UNIQUE NULL_FILTERED INDEX ix ON t (id)");
    }

    @Test
    void parsesSakilaIndexAndFollowingStatementsIssue1563() throws Exception {
        String sql =
                "CREATE UNIQUE NONCLUSTERED INDEX idx_fk_address_id ON store(manager_staff_id);";
        Statements statements = CCJSqlParserUtil.parseStatements(sql + "\nGO\nSELECT 1;",
                parser -> parser.withDialect(Dialect.SQLSERVER));
        assertEquals(2, statements.size());
        assertEquals(IndexOptions.Clustering.NONCLUSTERED,
                ((CreateIndex) statements.get(0)).getIndex().getClustering());
        assertEquals("SELECT 1", statements.get(1).toString());
    }

    @Test
    void parsesQualifiedPostgreSqlAttributesIssue1994() throws Exception {
        String sql = "CREATE INDEX \"index_keyword\" ON \"inter\".\"inter_ti_rec\" USING btree "
                + "(\"keyword\" COLLATE \"pg_catalog\".\"default\" \"pg_catalog\".\"text_ops\" ASC NULLS LAST)";
        CreateIndex statement = (CreateIndex) assertSqlCanBeParsedAndDeparsed(sql, true,
                parser -> parser.withDialect(Dialect.POSTGRESQL));
        KeyElement key = statement.getIndex().getColumns().get(0);
        assertEquals("\"pg_catalog\".\"default\"", key.getCollation());
        assertEquals("\"pg_catalog\".\"text_ops\"", key.getOperatorClass());
        assertEquals(KeyElement.SortOrder.ASC, key.getSortOrder());
        assertEquals(KeyElement.NullOrdering.LAST, key.getNullOrdering());
        assertNull(key.getParams());

        key.setCollation("public.\"C\"");
        key.setOperatorClass("public.text_pattern_ops");
        key.setSortOrder(KeyElement.SortOrder.DESC);
        key.setNullOrdering(KeyElement.NullOrdering.FIRST);
        assertDeparse(statement,
                "CREATE INDEX \"index_keyword\" ON \"inter\".\"inter_ti_rec\" USING btree "
                        + "(\"keyword\" COLLATE public.\"C\" public.text_pattern_ops DESC NULLS FIRST)");
        assertEquals(key.getOperatorClass(), parse(statement.toString(), Dialect.POSTGRESQL)
                .getIndex().getColumns().get(0).getOperatorClass());
        key.setCollation(null);
        key.setOperatorClass(null);
        key.setSortOrder(null);
        key.setNullOrdering(null);
        assertDeparse(statement,
                "CREATE INDEX \"index_keyword\" ON \"inter\".\"inter_ti_rec\" USING btree (\"keyword\")");
    }

    @Test
    void preservesQuotedNamePartsAndBareFunctionKeys() throws Exception {
        String sql = "CREATE INDEX ix ON t (lower(name) COLLATE \"schema.with.dot\".\"C\" "
                + "\"schema\".\"op\"\"class\" DESC NULLS FIRST)";
        CreateIndex statement = (CreateIndex) assertSqlCanBeParsedAndDeparsed(sql, true,
                parser -> parser.withDialect(Dialect.POSTGRESQL));
        KeyElement key = statement.getIndex().getColumns().get(0);
        assertTrue(key.isExpression());
        assertFalse(key.isExpressionParenthesized());
        assertEquals("\"schema.with.dot\".\"C\"", key.getCollation());
        assertEquals("\"schema\".\"op\"\"class\"", key.getOperatorClass());
        assertNull(key.getParams());
    }

    @Test
    void keepsPostgreSqlFunctionAndFollowingIndexSeparateIssue1994() throws Exception {
        String sql = "DROP FUNCTION IF EXISTS fin.restore_fund_data(in_fund_id int8); "
                + "CREATE OR REPLACE FUNCTION fin.restore_fund_data(in_fund_id int8) "
                + "RETURNS pg_catalog.varchar AS $BODY$ BEGIN RETURN 'success'; END $BODY$ "
                + "LANGUAGE plpgsql VOLATILE COST 100; "
                + "CREATE INDEX ix ON inter.inter_ti_rec "
                + "(keyword COLLATE pg_catalog.\"default\" pg_catalog.text_ops ASC NULLS LAST); "
                + "SELECT 1;";
        Statements statements = CCJSqlParserUtil.parseStatements(sql,
                parser -> parser.withDialect(Dialect.POSTGRESQL));
        assertEquals(4, statements.size());
        assertEquals("pg_catalog.text_ops", ((CreateIndex) statements.get(2))
                .getIndex().getColumns().get(0).getOperatorClass());
        assertEquals("SELECT 1", statements.get(3).toString());
    }

    @Test
    void gatesQualifiedAttributesAndRejectsRepeatedPostgreSqlAttributes() {
        for (String key : List.of("name COLLATE public.\"C\"", "name public.text_ops")) {
            String sql = "CREATE INDEX ix ON t (" + key + ")";
            assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql));
            for (Dialect dialect : Dialect.values()) {
                if (dialect != Dialect.POSTGRESQL) {
                    assertThrows(JSQLParserException.class, () -> parse(sql, dialect));
                }
            }
        }
        for (String key : List.of("name ASC DESC", "name NULLS FIRST NULLS LAST",
                "name COLLATE public.\"C\" COLLATE public.\"C\"")) {
            assertThrows(JSQLParserException.class,
                    () -> parse("CREATE INDEX ix ON t (" + key + ")", Dialect.POSTGRESQL));
        }
    }
}

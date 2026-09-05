/*-
 * Copyright (C) 2004 - 2026 JSQLParser
 * Licensed under the Apache License, Version 2.0.
 */
package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SelectItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The interesting assertions here are the <em>negative</em> ones. Any naive implementation gets
 * {@code SELECT} and {@code CREATE TABLE} right; what separates a correct analysis from a flag
 * union is that it says <b>no</b> to a result set for {@code INSERT .. SELECT} and to a mutation
 * for a pure read.
 */
class StatementFeatureVisitorTest {

    private static StatementFeatures analyse(String sql) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sql).getFeatures();
    }

    private static StatementFeatures analyse(String sql, String... pureFunctions)
            throws JSQLParserException {
        Set<String> pure = Set.of(pureFunctions);
        return StatementFeatureVisitor.analyse(CCJSqlParserUtil.parse(sql), pure::contains);
    }

    // ---- invariants -------------------------------------------------------------------------

    /**
     * <h4>What this pins down</h4>
     *
     * {@link StatementFeatures} splits its verdict into two sets:
     *
     * <ul>
     * <li>{@code getCertain()} - the grammar <em>proves</em> the statement has this feature;</li>
     * <li>{@code getUncertain()} - the grammar could not <em>exclude</em> it, but could not prove
     * it either.</li>
     * </ul>
     *
     * For the second set to be readable at all, it has to mean "unproven <b>and nothing more</b>".
     * If a feature could appear in both sets, then {@code getUncertain()} would answer the question
     * "is anything doubtful here?" with a yes even when the feature is settled, and printing it in
     * a rejection message - which is exactly what the read-only guard in the README does - would
     * report a proven mutation as a doubt. So the constructor normalises:
     * {@code uncertain.removeAll(certain)}. This test is that normalisation's only guard.
     *
     * <h4>Why the overlap is not hypothetical</h4>
     *
     * {@code UPDATE t SET a = f(b)} produces it on every run:
     *
     * <ul>
     * <li>{@code visit(Update)} adds {@code MODIFIES_DATA} to <b>certain</b>;</li>
     * <li>{@code FeatureExpressionVisitor#visit(Function)} cannot prove {@code f} pure, so it adds
     * {@code MODIFIES_DATA} <em>and</em> {@code MODIFIES_SCHEMA} to <b>uncertain</b>.</li>
     * </ul>
     *
     * Raw, that is {@code certain={MODIFIES_DATA}}, {@code uncertain={MODIFIES_DATA,
     * MODIFIES_SCHEMA}}. Normalised, {@code uncertain} must come out as {@code {MODIFIES_SCHEMA}}
     * alone - the schema change is the only open question; the data change is settled.
     *
     * <h4>Why the previous version of this test was broken</h4>
     *
     * It looped over four statements asserting
     * {@code assertThat(getCertain()).doesNotContainAnyElementsOf(getUncertain())}. Two problems:
     *
     * <ol>
     * <li><b>It threw.</b> AssertJ rejects an empty argument to {@code doesNotContainAnyElementsOf}
     * with "The iterable of values to look for should not be empty" - an
     * {@code IllegalArgumentException} about the assertion's own inputs, not a failed expectation.
     * {@code SELECT a FROM t} has nothing uncertain, so the first iteration blew up before testing
     * anything.</li>
     * <li><b>Three of the four inputs were dead weight.</b> Only the {@code f(b)} case can ever
     * produce an overlap; for the others the assertion held vacuously. A loop over inputs that
     * cannot exercise the invariant hides the one input that can.</li>
     * </ol>
     *
     * Both are fixed below by intersecting explicitly (an empty intersection is a legitimate
     * result, not a malformed assertion) and by testing the case that actually overlaps.
     */
    @Test
    @DisplayName("a proven feature is never also reported as merely possible")
    void certainAndUncertainAreDisjoint() throws JSQLParserException {
        StatementFeatures f = analyse("UPDATE t SET a = f(b)");

        // the overlap the constructor has to resolve
        assertThat(f.getCertain()).contains(StmtFeature.MODIFIES_DATA);
        assertThat(f.getUncertain())
                .as("MODIFIES_DATA is proven, so it must not also be listed as doubtful")
                .doesNotContain(StmtFeature.MODIFIES_DATA)
                .as("the unproven function leaves the schema question genuinely open")
                .contains(StmtFeature.MODIFIES_SCHEMA);

        assertDisjoint(f, "UPDATE t SET a = f(b)");
    }

    @Test
    @DisplayName("the invariant holds for statements with nothing uncertain too")
    void disjointWhenNothingIsUncertain() throws JSQLParserException {
        for (String sql : new String[] {"SELECT a FROM t", "DELETE FROM t RETURNING *",
                "CREATE TABLE t (a INT)", "UPDATE t SET a = f(b)"}) {
            assertDisjoint(analyse(sql), sql);
        }
    }

    @Test
    @DisplayName("normalisation happens in the constructor, not by luck of traversal order")
    void constructorNormalisesOverlappingSets() {
        // hand-built overlap, bypassing the visitor entirely: whatever order the visitor happens
        // to add features in, the value object must still come out normalised
        StatementFeatures f = new StatementFeatures(
                EnumSet.of(StmtFeature.MODIFIES_DATA, StmtFeature.READS_DATA),
                EnumSet.of(StmtFeature.MODIFIES_DATA, StmtFeature.MODIFIES_SCHEMA),
                Set.of("f"));

        assertThat(f.getCertain())
                .containsExactlyInAnyOrder(StmtFeature.MODIFIES_DATA, StmtFeature.READS_DATA);
        assertThat(f.getUncertain()).containsExactly(StmtFeature.MODIFIES_SCHEMA);

        // and the two accessors stay consistent with the predicates
        assertThat(f.is(StmtFeature.MODIFIES_DATA)).isTrue();
        assertThat(f.may(StmtFeature.MODIFIES_DATA)).isTrue();
        assertThat(f.is(StmtFeature.MODIFIES_SCHEMA)).isFalse();
        assertThat(f.may(StmtFeature.MODIFIES_SCHEMA)).isTrue();
    }

    /**
     * Intersects the two sets and asserts the result is empty.
     *
     * <p>
     * Deliberately not {@code doesNotContainAnyElementsOf}: that assertion treats an empty argument
     * as caller error and throws {@code IllegalArgumentException}, which turns the perfectly valid
     * case "this statement has no uncertainty at all" into a test error. An explicit intersection
     * has no such precondition - empty in, empty out, assertion passes.
     */
    private static void assertDisjoint(StatementFeatures f, String description) {
        EnumSet<StmtFeature> overlap = EnumSet.noneOf(StmtFeature.class);
        overlap.addAll(f.getCertain());
        overlap.retainAll(f.getUncertain());

        assertThat(overlap).as("features reported as both proven and doubtful for: %s", description)
                .isEmpty();
    }

    @Test
    @DisplayName("is() implies may(), never the reverse")
    void certainImpliesPossible() throws JSQLParserException {
        StatementFeatures f = analyse("DELETE FROM t WHERE a = f(1)");
        for (StmtFeature feature : StmtFeature.values()) {
            assertThat(!f.is(feature) || f.may(feature)).as(feature.name()).isTrue();
        }
    }

    @Test
    @DisplayName("not cached: mutating the AST changes the verdict")
    void notCached() throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse("DELETE FROM t WHERE a = 1");
        assertThat(statement.getFeatures().returnsResultSet()).isFalse();

        ReturningClause returning = new ReturningClause(ReturningClause.Keyword.RETURNING,
                List.of(new SelectItem<>(new Column("id"))));
        ((Delete) statement).setReturningClause(returning);

        assertThat(statement.getFeatures().returnsResultSet()).isTrue();
    }

    // ---- plain cases ------------------------------------------------------------------------

    @Test
    void plainSelectReadsAndReturns() throws JSQLParserException {
        StatementFeatures f = analyse("SELECT a FROM t");
        assertThat(f.returnsResultSet()).isTrue();
        assertThat(f.is(StmtFeature.READS_DATA)).isTrue();
        assertThat(f.mayModifyData()).isFalse();
        assertThat(f.modifiesSchema()).isFalse();
    }

    @Test
    void insertValuesMutatesOnly() throws JSQLParserException {
        StatementFeatures f = analyse("INSERT INTO t (a) VALUES (1)");
        assertThat(f.modifiesData()).isTrue();
        assertThat(f.returnsResultSet()).isFalse();
    }

    /**
     * DDL that only touches the catalogue. No rows are read, written or returned.
     */
    @Test
    void nonDestructiveDdlTouchesTheSchemaOnly() throws JSQLParserException {
        for (String sql : new String[] {"CREATE TABLE t (a INT)", "ALTER TABLE t ADD COLUMN b INT",
                "GRANT SELECT ON t TO u", "CREATE INDEX i ON t (a)", "COMMENT ON TABLE t IS 'x'"}) {
            StatementFeatures f = analyse(sql);
            assertThat(f.modifiesSchema()).as(sql).isTrue();
            assertThat(f.returnsResultSet()).as(sql).isFalse();
            assertThat(f.mayModifyData()).as(sql).isFalse();
        }
    }

    /**
     * DROP and TRUNCATE are DDL by every other measure - implicit commit, no row triggers,
     * catalogue change - but they destroy rows, so they carry {@code MODIFIES_DATA} as well.
     *
     * <p>
     * This is a deliberate call, not an oversight. The failure mode it prevents:
     *
     * <pre>
     * if (connection.isReadOnly() &amp;&amp; features.mayModifyData()) {
     *     throw new SQLException("read-only");
     * }
     * </pre>
     *
     * A guard written like that - and plenty are, because "modifies data" reads as the obvious
     * check - would wave {@code DROP TABLE t} straight through if DROP were classified as schema
     * only. Making the destructive DDL admit to destroying data means the naive guard is still
     * correct.
     *
     * <p>
     * The cost is on the other side: a caller counting "did this write rows?" for auditing gets a
     * yes for DROP, which is arguably wrong - no rows were written, they were removed along with
     * the table. If that matters more to you than the guard case, move DROP (not TRUNCATE, which
     * really is a bulk delete) back to schema-only and require guards to check both predicates.
     */
    @Test
    void destructiveDdlAlsoModifiesData() throws JSQLParserException {
        for (String sql : new String[] {"DROP TABLE t", "TRUNCATE TABLE t"}) {
            StatementFeatures f = analyse(sql);
            assertThat(f.modifiesSchema()).as(sql).isTrue();
            assertThat(f.modifiesData()).as(sql).isTrue();
            assertThat(f.returnsResultSet()).as(sql).isFalse();
        }
    }

    // ---- the cases a flag union gets wrong --------------------------------------------------

    @Test
    @DisplayName("INSERT .. SELECT contains a Select but returns no result set")
    void insertSelectDoesNotReturnResultSet() throws JSQLParserException {
        StatementFeatures f = analyse("INSERT INTO x (a) SELECT a FROM t");
        assertThat(f.modifiesData()).isTrue();
        assertThat(f.is(StmtFeature.READS_DATA)).isTrue();
        assertThat(f.returnsResultSet()).isFalse();
        assertThat(f.may(StmtFeature.RETURNS_RESULT_SET)).isFalse();
    }

    @Test
    @DisplayName("data-modifying CTE: top-level SELECT returns rows, DELETE still mutates")
    void dataModifyingCteAtTopLevel() throws JSQLParserException {
        StatementFeatures f =
                analyse("WITH c AS (DELETE FROM t RETURNING *) SELECT * FROM c");
        assertThat(f.returnsResultSet()).isTrue();
        assertThat(f.modifiesData()).isTrue();
    }

    @Test
    @DisplayName("RETURNING inside a nested CTE of an INSERT returns nothing to the client")
    void nestedReturningIsNotAResultSet() throws JSQLParserException {
        StatementFeatures f = analyse(
                "INSERT INTO x WITH c AS (DELETE FROM t RETURNING *) SELECT * FROM c");
        assertThat(f.modifiesData()).isTrue();
        assertThat(f.returnsResultSet()).isFalse();
    }

    @Test
    void dmlWithReturningIsBoth() throws JSQLParserException {
        StatementFeatures f = analyse("DELETE FROM t WHERE a = 1 RETURNING id, name");
        assertThat(f.modifiesData()).isTrue();
        assertThat(f.returnsResultSet()).isTrue();
    }

    // ---- pending: requires the expression / from-item traversal to be wired -----------------

    @Nested
    @DisplayName("subqueries and function purity")
    class PendingExpressionWiring {

        @Test
        void subqueryInWhereDoesNotAddAResultSet() throws JSQLParserException {
            StatementFeatures f = analyse("UPDATE t SET a = 1 WHERE b IN (SELECT b FROM u)");
            assertThat(f.modifiesData()).isTrue();
            assertThat(f.is(StmtFeature.READS_DATA)).isTrue();
            assertThat(f.returnsResultSet()).isFalse();
        }

        @Test
        void scalarSubqueryInSelectListStaysOneResultSet() throws JSQLParserException {
            StatementFeatures f = analyse("SELECT (SELECT max(x) FROM u) AS m FROM t");
            assertThat(f.returnsResultSet()).isTrue();
            assertThat(f.mayModifyData()).isTrue(); // max() unproven
        }

        @Test
        void unprovenFunctionIsPossibleNotCertain() throws JSQLParserException {
            StatementFeatures f = analyse("SELECT nextval('s')");
            assertThat(f.modifiesData()).isFalse();
            assertThat(f.mayModifyData()).isTrue();
            assertThat(f.getUnresolvedReferences()).contains("nextval");
        }

        @Test
        void allowListCollapsesTheUncertainty() throws JSQLParserException {
            StatementFeatures f = analyse("SELECT upper(name) FROM t", "upper");
            assertThat(f.mayModifyData()).isFalse();
            assertThat(f.getUnresolvedReferences()).isEmpty();
        }
    }

    @Nested
    @DisplayName("DDL and opaque statements")
    class PendingStatementOverrides {

        @Test
        void ctasIsSchemaAndReadButNotAResultSet() throws JSQLParserException {
            StatementFeatures f = analyse("CREATE TABLE t AS SELECT a FROM u");
            assertThat(f.modifiesSchema()).isTrue();
            assertThat(f.is(StmtFeature.READS_DATA)).isTrue();
            assertThat(f.returnsResultSet()).isFalse();
        }

        @Test
        void callIsOpaqueAndFailsSafeForGuardsOnly() throws JSQLParserException {
            StatementFeatures f = analyse("CALL do_something(1)");
            assertThat(f.isOpaque()).isTrue();
            assertThat(f.mayModifyData()).isTrue();
            assertThat(f.modifiesData()).isFalse(); // guards block it, dispatchers are not misled
            assertThat(f.getUnresolvedReferences()).isNotEmpty();
        }
    }

    // ---- the failure mode that must stay loud ----------------------------------------------

    @Test
    @DisplayName("an unclassified statement type degrades to OPAQUE, never to harmless")
    void unknownStatementTypeIsOpaque() {
        Statement unknown = new Statement() {
            @Override
            public <T, S> T accept(net.sf.jsqlparser.statement.StatementVisitor<T> visitor,
                    S context) {
                return null;
            }
        };
        StatementFeatures f = StatementFeatureVisitor.analyse(unknown,
                Collections.<String>emptySet()::contains);
        assertThat(f.isOpaque()).isTrue();
        assertThat(f.mayModifyData()).isTrue();
        assertThat(f.modifiesData()).isFalse();
    }

    @Test
    @DisplayName("purity predicate is consulted, not bypassed")
    void purityPredicateIsUsed() throws JSQLParserException {
        Predicate<String> nothingIsPure = name -> false;
        StatementFeatures f = StatementFeatureVisitor
                .analyse(CCJSqlParserUtil.parse("SELECT a FROM t"), nothingIsPure);
        assertThat(f.mayModifyData()).isFalse(); // no function call at all -> still clean
    }

    // ---- cases the adapter wiring makes reachable -------------------------------------------

    @Test
    @DisplayName("data-modifying CTE inside a subquery does not blow up the select path")
    void dataModifyingCteInsideASubquery() throws JSQLParserException {
        // SelectVisitorAdapter#visit(WithItem) would ClassCastException here via getSelect()
        StatementFeatures f = analyse(
                "SELECT * FROM u WHERE id IN ("
                        + "WITH c AS (DELETE FROM t RETURNING id) SELECT id FROM c)");
        assertThat(f.returnsResultSet()).isTrue();
        assertThat(f.modifiesData()).isTrue();
    }

    @Test
    @DisplayName("SELECT .. INTO materialises rows instead of returning them")
    void selectIntoIsNotAResultSet() throws JSQLParserException {
        StatementFeatures f = analyse("SELECT a INTO newtable FROM t");
        assertThat(f.returnsResultSet()).isFalse();
        assertThat(f.modifiesData()).isTrue();
        assertThat(f.modifiesSchema()).isTrue();
    }

    /**
     * <h4>Why the SQL looks the way it does</h4>
     *
     * {@code DELETE FROM t OUTPUT DELETED.id} does <b>not</b> parse. In {@code JSqlParserCC.jjt}
     * the DELETE production only offers the OUTPUT clause inside the optional
     * {@code (tables [OUTPUT ..] <K_FROM> | <K_FROM>)} group - i.e. the T-SQL
     * {@code DELETE <target> OUTPUT .. FROM <sources>} shape, where the target precedes OUTPUT and
     * FROM follows it. Put OUTPUT after {@code FROM t} and there is no production for it.
     *
     * <p>
     * The strings below are lifted from the upstream {@code DeleteTest#testDeleteOutputClause}, so
     * they are known to parse.
     *
     * <h4>Why the INTO case uses a table variable</h4>
     *
     * The grammar is {@code <K_INTO> ( UserVariable() | Table() )}. The {@code @MyTableVar} form
     * populates {@code getTableVariable()} and leaves {@code getOutputTable()} null - which is
     * exactly the case an implementation checking only {@code getOutputTable()} gets wrong.
     */
    @Test
    @DisplayName("OUTPUT returns rows; OUTPUT .. INTO redirects them")
    void outputClauseDistinction() throws JSQLParserException {
        StatementFeatures toClient =
                analyse("DELETE Sales.ShoppingCartItem OUTPUT DELETED.* FROM Sales");
        assertThat(toClient.returnsResultSet()).isTrue();

        StatementFeatures toTableVariable = analyse("DELETE Production.ProductProductPhoto "
                + "OUTPUT DELETED.ProductID INTO @MyTableVar "
                + "FROM Production.ProductProductPhoto AS ph");
        assertThat(toTableVariable.returnsResultSet())
                .as("rows go into @MyTableVar, not down the wire")
                .isFalse();
        assertThat(toTableVariable.modifiesData()).isTrue();
    }

    @Test
    @DisplayName("CREATE VIEW references tables without reading them")
    void createViewDoesNotRead() throws JSQLParserException {
        StatementFeatures f = analyse("CREATE VIEW v AS SELECT a FROM t");
        assertThat(f.modifiesSchema()).isTrue();
        assertThat(f.is(StmtFeature.READS_DATA)).isFalse();
        assertThat(f.returnsResultSet()).isFalse();
    }

    @Test
    @DisplayName("script: the aggregate verdict is a union over all statements")
    void scriptAggregateIsAUnion() throws JSQLParserException {
        Statements script =
                CCJSqlParserUtil.parseStatements("UPDATE t SET a = 1; SELECT a FROM t;");

        StatementFeatures f = StatementFeatureVisitor.analyse(script);

        // useful for "may this script write?" - useless for executeQuery/executeUpdate, because
        // the union does not say which statement returns the rows
        assertThat(f.modifiesData()).isTrue();
        assertThat(f.returnsResultSet()).isTrue();
    }

    @Test
    @DisplayName("script: each statement keeps its own result position")
    void scriptStatementsAreIndependentlyTopLevel() throws JSQLParserException {
        Statements script =
                CCJSqlParserUtil.parseStatements("UPDATE t SET a = 1; SELECT a FROM t;");

        List<StatementFeatures> each = StatementFeatureVisitor.analyseEach(script);

        assertThat(each).hasSize(2);

        // the UPDATE writes and returns nothing
        assertThat(each.get(0).modifiesData()).isTrue();
        assertThat(each.get(0).returnsResultSet()).isFalse();

        // the SELECT returns rows and writes nothing - no leakage from the statement before it
        assertThat(each.get(1).returnsResultSet()).isTrue();
        assertThat(each.get(1).mayModifyData()).isFalse();
    }

    @Test
    @DisplayName("SELECT 1 reads nothing")
    void selectWithoutFromReadsNothing() throws JSQLParserException {
        StatementFeatures f = analyse("SELECT 1");
        assertThat(f.returnsResultSet()).isTrue();
        assertThat(f.is(StmtFeature.READS_DATA)).isFalse();
    }

    @Test
    @DisplayName("SELECT .. FOR UPDATE takes locks")
    void selectForUpdateTouchesTheTransaction() throws JSQLParserException {
        StatementFeatures f = analyse("SELECT a FROM t FOR UPDATE");
        assertThat(f.is(StmtFeature.MODIFIES_TRANSACTION)).isTrue();
    }

    @Test
    @DisplayName("EXPLAIN returns rows and may execute (EXPLAIN ANALYZE)")
    void explainIsUncertain() throws JSQLParserException {
        StatementFeatures f = analyse("EXPLAIN DELETE FROM t");
        assertThat(f.returnsResultSet()).isTrue();
        assertThat(f.mayModifyData()).isTrue();
    }
}

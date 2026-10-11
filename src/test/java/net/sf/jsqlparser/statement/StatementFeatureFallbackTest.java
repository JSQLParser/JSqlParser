/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.create.function.CreateFunction;
import net.sf.jsqlparser.statement.oracle.OracleAssignment;
import net.sf.jsqlparser.statement.oracle.OracleNullStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class StatementFeatureFallbackTest {
    private static final class UnclassifiedStatement implements Statement {
        @Override
        public <T, S> T accept(StatementVisitor<T> visitor, S context) {
            return null;
        }
    }

    private static void assertOpaque(StatementFeatures features, String... references) {
        assertThat(features.getCertain()).containsExactly(StmtFeature.OPAQUE);
        assertThat(features.getUncertain()).containsExactlyInAnyOrder(StmtFeature.READS_DATA,
                StmtFeature.RETURNS_RESULT_SET, StmtFeature.MODIFIES_DATA,
                StmtFeature.MODIFIES_SCHEMA);
        assertThat(features.getUnresolvedReferences()).containsExactlyInAnyOrder(references);
    }

    @Test
    void unclassifiedStatementHasNoCatalogueReference() {
        Statement statement = new UnclassifiedStatement();
        assertOpaque(statement.getFeatures());
        assertOpaque(statement.getFeatures(name -> true));
        assertOpaque(StatementFeatureVisitor.analyse(statement));
    }

    @Test
    void anonymousStatementHasNoEmptyReference() {
        Statement statement = new Statement() {
            @Override
            public <T, S> T accept(StatementVisitor<T> visitor, S context) {
                return null;
            }
        };
        assertOpaque(statement.getFeatures());
    }

    @Test
    void inertOracleStatementRetainsConservativeFallback() {
        assertOpaque(new OracleNullStatement().getFeatures());
        assertOpaque(new OracleAssignment(new Column("x"), new LongValue(1)).getFeatures());
    }

    @Test
    void fallbackPreservesFunctionReferenceFoundBeforeClassification() {
        Function function = new Function();
        function.setName("Unproven_Function");
        Statement assignment = new OracleAssignment(new Column("x"), function);
        assertOpaque(assignment.getFeatures(), "unproven_function");
        assertOpaque(assignment.getFeatures(name -> name.equals("unproven_function")));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void aggregateFallbackHasNoContainerReference(int count) {
        Statements script = new Statements();
        for (int i = 0; i < count; i++) {
            script.add(new UnclassifiedStatement());
        }
        assertOpaque(StatementFeatureVisitor.analyse(script));
        assertOpaque(StatementFeatureVisitor.analyse(script, name -> true));
        List<StatementFeatures> each = StatementFeatureVisitor.analyseEach(script);
        assertThat(each).hasSize(count);
        each.forEach(StatementFeatureFallbackTest::assertOpaque);
    }

    @Test
    void perStatementAnalysisDoesNotLeakReferencesOrResultPositions() throws Exception {
        Statements script = new Statements();
        script.add(CCJSqlParserUtil.parse("CALL Do_Something(1)"));
        script.add(new UnclassifiedStatement());
        script.add(CCJSqlParserUtil.parse("SELECT 1"));
        List<StatementFeatures> each = StatementFeatureVisitor.analyseEach(script);
        assertOpaque(each.get(0), "do_something");
        assertOpaque(each.get(1));
        assertThat(each.get(2).getCertain()).containsExactly(StmtFeature.RETURNS_RESULT_SET);
        assertThat(each.get(2).getUncertain()).isEmpty();
        assertThat(each.get(2).getUnresolvedReferences()).isEmpty();
        StatementFeatures union = StatementFeatureVisitor.analyse(script);
        assertThat(union.getCertain()).containsExactlyInAnyOrder(StmtFeature.OPAQUE,
                StmtFeature.RETURNS_RESULT_SET);
        assertThat(union.getUncertain()).containsExactlyInAnyOrder(StmtFeature.READS_DATA,
                StmtFeature.MODIFIES_DATA, StmtFeature.MODIFIES_SCHEMA);
        assertThat(union.getUnresolvedReferences()).containsExactly("do_something");
    }

    @ParameterizedTest
    @ValueSource(strings = {"SELECT 1", "CREATE TABLE t (id INTEGER)"})
    void classifiedStatementsDoNotFallBack(String sql) throws Exception {
        StatementFeatures features = CCJSqlParserUtil.parse(sql).getFeatures();
        assertThat(features.getCertain()).containsExactly(sql.startsWith("SELECT")
                ? StmtFeature.RETURNS_RESULT_SET
                : StmtFeature.MODIFIES_SCHEMA);
        assertThat(features.getUncertain()).isEmpty();
        assertThat(features.getUnresolvedReferences()).isEmpty();
    }

    @Test
    void procedureAndDynamicSqlMarkersRemainResolvable() throws Exception {
        assertOpaque(CCJSqlParserUtil.parse("CALL Do_Something(1)").getFeatures(), "do_something");
        assertOpaque(CCJSqlParserUtil.parse("DO 'BEGIN NULL; END'",
                parser -> parser.withDialect(Dialect.POSTGRESQL)).getFeatures(), "do");
        assertOpaque(CCJSqlParserUtil.parse("some unsupported sql",
                parser -> parser.withUnsupportedStatements(true)).getFeatures(), "unsupported");
    }

    @Test
    void explainMarkerRemainsExplicit() throws Exception {
        StatementFeatures features = CCJSqlParserUtil.parse("EXPLAIN SELECT 1").getFeatures();
        assertThat(features.getCertain()).containsExactly(StmtFeature.RETURNS_RESULT_SET);
        assertThat(features.getUncertain()).containsExactlyInAnyOrder(StmtFeature.MODIFIES_DATA,
                StmtFeature.READS_DATA);
        assertThat(features.getUnresolvedReferences()).containsExactly("explain");
    }

    @Test
    void selectFunctionReferenceAndAllowListRemainEffective() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT Unproven_Function(1)");
        StatementFeatures features = statement.getFeatures();
        assertThat(features.getCertain()).containsExactly(StmtFeature.RETURNS_RESULT_SET);
        assertThat(features.getUncertain()).containsExactlyInAnyOrder(StmtFeature.MODIFIES_DATA,
                StmtFeature.MODIFIES_SCHEMA);
        assertThat(features.getUnresolvedReferences()).containsExactly("unproven_function");
        StatementFeatures pure = statement.getFeatures(name -> name.equals("unproven_function"));
        assertThat(pure.getCertain()).containsExactly(StmtFeature.RETURNS_RESULT_SET);
        assertThat(pure.getUncertain()).isEmpty();
        assertThat(pure.getUnresolvedReferences()).isEmpty();
    }

    @Test
    void storedRoutineExplicitMarkerIsOutsideGenericFallback() {
        StatementFeatures features = new CreateFunction().getFeatures();
        assertThat(features.getCertain()).containsExactly(StmtFeature.MODIFIES_SCHEMA);
        assertThat(features.getUncertain()).containsExactlyInAnyOrder(StmtFeature.MODIFIES_DATA,
                StmtFeature.READS_DATA);
        assertThat(features.getUnresolvedReferences()).containsExactly("createfunction");
    }
}

/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.maintenance;

import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.StmtFeature;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.statement.UtilityOption;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.maintenance.ClusterStatement.OptionKind;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ClusterStatementTest {

    private static final Consumer<CCJSqlParser> POSTGRESQL =
            parser -> parser.withDialect(Dialect.POSTGRESQL);

    private static ClusterStatement parse(String sql) throws JSQLParserException {
        Statement statement = assertSqlCanBeParsedAndDeparsed(sql, true, POSTGRESQL);
        assertEquals(sql, assertSqlCanBeParsedAndDeparsed(sql, true).toString());
        return (ClusterStatement) statement;
    }

    @Test
    void tableUsingIndex() throws JSQLParserException {
        ClusterStatement cluster = parse("CLUSTER t USING t_pkey");

        assertEquals("t", cluster.getTable().getFullyQualifiedName());
        assertEquals("t_pkey", cluster.getIndexName());
        assertTrue(cluster.getOptions().isEmpty());
    }

    @Test
    void verboseWithoutTable() throws JSQLParserException {
        ClusterStatement cluster = parse("CLUSTER VERBOSE");

        assertNull(cluster.getTable());
        assertFalse(cluster.isParenthesizedOptions());
        assertEquals(OptionKind.VERBOSE, cluster.getOptions().get(0).getKind(OptionKind.class));
    }

    @Test
    void otherForms() throws JSQLParserException {
        assertNull(parse("CLUSTER").getTable());
        ClusterStatement qualified = parse("CLUSTER VERBOSE s.t");
        assertEquals("s.t", qualified.getTable().getFullyQualifiedName());
        assertNull(qualified.getIndexName());

        ClusterStatement options = parse("CLUSTER (VERBOSE false) s.t USING i");
        assertTrue(options.isParenthesizedOptions());
        assertEquals(Boolean.FALSE, options.getOptions().get(0).getBooleanValue());
        assertEquals("i", options.getIndexName());
        assertEquals("CLUSTER (VERBOSE)", parse("CLUSTER (VERBOSE)").toString());
    }

    @Test
    void indexOnTable() throws JSQLParserException {
        ClusterStatement cluster = parse("CLUSTER t_pkey ON t");

        assertEquals(ClusterStatement.IndexSyntax.ON, cluster.getIndexSyntax());
        assertEquals("t_pkey", cluster.getIndexName());
        assertEquals("t", cluster.getTable().getFullyQualifiedName());
        assertTrue(cluster.getOptions().isEmpty());

        ClusterStatement verbose = parse("CLUSTER VERBOSE t_pkey ON s.t");
        assertEquals(OptionKind.VERBOSE, verbose.getOptions().get(0).getKind(OptionKind.class));
        assertEquals("s.t", verbose.getTable().getFullyQualifiedName());
        assertEquals(ClusterStatement.IndexSyntax.USING,
                parse("CLUSTER t USING t_pkey").getIndexSyntax());
    }

    @Test
    void acceptsEveryOptionOfTheGrammar() throws JSQLParserException {
        ClusterStatement cluster = parse("CLUSTER (FULL, VERBOSE maybe) t");

        assertNull(cluster.getOptions().get(0).getKind(OptionKind.class));
        assertEquals(OptionKind.VERBOSE, cluster.getOptions().get(1).getKind(OptionKind.class));
        assertNull(cluster.getOptions().get(1).getBooleanValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"CLUSTER VERBOSE (t)", "CLUSTER ONLY t", "CLUSTER t *",
            "CLUSTER t USING", "CLUSTER USING i", "CLUSTER t, s", "CLUSTER t (a)",
            "CLUSTER (VERBOSE) idx ON t", "CLUSTER s.idx ON t", "CLUSTER idx ON",
            "CLUSTER idx ON t USING i", "CLUSTER t USING s.i"})
    void rejectsFormsOutsidePostgreSqlGrammar(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, POSTGRESQL));
    }

    @Test
    void otherDialectsDoNotParseCluster() throws JSQLParserException {
        for (String sql : List.of("CLUSTER t", "CLUSTER idx ON t")) {
            assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql,
                    parser -> parser.withDialect(Dialect.MYSQL)), sql);
        }
        Statement legacy = CCJSqlParserUtil.parse("CLUSTER idx ON t",
                parser -> parser.withUnsupportedStatements(true));
        assertInstanceOf(ClusterStatement.class, legacy);
        Statement other = CCJSqlParserUtil.parse("CLUSTER idx FOO t",
                parser -> parser.withUnsupportedStatements(true));
        assertInstanceOf(UnsupportedStatement.class, other);
    }

    @Test
    void endsAtStatementBoundaries() throws JSQLParserException {
        Statements statements = CCJSqlParserUtil.parseStatements(
                "SHOW search_path; CLUSTER VERBOSE; CLUSTER t USING i\nCLUSTER i ON t\nSELECT 1",
                POSTGRESQL);

        assertEquals(5, statements.size());
        assertEquals("CLUSTER VERBOSE", statements.get(1).toString());
        assertEquals("CLUSTER t USING i", statements.get(2).toString());
        assertEquals("CLUSTER i ON t", statements.get(3).toString());
    }

    @Test
    void endsAnUnsupportedStatement() throws JSQLParserException {
        Statements statements = CCJSqlParserUtil.parseStatements("LISTEN ch; CLUSTER t",
                parser -> parser.withDialect(Dialect.POSTGRESQL).withUnsupportedStatements(true));

        assertEquals(2, statements.size());
        assertInstanceOf(ClusterStatement.class, statements.get(1));
    }

    @Test
    void buildsAndDeparses() {
        ClusterStatement cluster = new ClusterStatement().withTable(new Table("t"))
                .withIndexName("i").addOptions(new UtilityOption("VERBOSE"));
        assertDeparse(cluster, "CLUSTER VERBOSE t USING i");

        cluster.withParenthesizedOptions(true);
        assertDeparse(cluster, "CLUSTER (VERBOSE) t USING i");

        cluster.withOptions(List.of(UtilityOption.of("VERBOSE", false)));
        assertDeparse(cluster, "CLUSTER (VERBOSE FALSE) t USING i");

        cluster.setTable(null);
        assertNull(cluster.getIndexName());
        assertDeparse(cluster, "CLUSTER (VERBOSE FALSE)");
        assertThrows(IllegalStateException.class, () -> cluster.setIndexName("i"));
    }

    @Test
    void rendersTheOnFormOnlyWhenItCanExpressTheStatement() {
        ClusterStatement cluster = new ClusterStatement().withTable(new Table("t"))
                .withIndexName("i").withIndexSyntax(ClusterStatement.IndexSyntax.ON);
        assertDeparse(cluster, "CLUSTER i ON t");

        cluster.addOptions(new UtilityOption("VERBOSE"));
        assertDeparse(cluster, "CLUSTER VERBOSE i ON t");

        // the ON form takes no option list, so the USING form carries it
        cluster.withParenthesizedOptions(true);
        assertDeparse(cluster, "CLUSTER (VERBOSE) t USING i");

        cluster.withParenthesizedOptions(false).withIndexName(null);
        assertDeparse(cluster, "CLUSTER VERBOSE t");
        assertThrows(NullPointerException.class, () -> cluster.setIndexSyntax(null));
    }

    @Test
    void findsTheTableButNotTheIndex() throws JSQLParserException {
        assertThat(new TablesNamesFinder<>().getTables(
                CCJSqlParserUtil.parse("CLUSTER s.t USING i", POSTGRESQL)))
                .containsExactly("s.t");
        assertThat(new TablesNamesFinder<>().getTables(
                CCJSqlParserUtil.parse("CLUSTER i ON s.t", POSTGRESQL)))
                .containsExactly("s.t");
        assertThat(new TablesNamesFinder<>().getTables(
                CCJSqlParserUtil.parse("CLUSTER", POSTGRESQL))).isEmpty();
    }

    @Test
    void reportsStatementFeatures() throws JSQLParserException {
        assertThat(CCJSqlParserUtil.parse("CLUSTER t").getFeatures().getCertain())
                .containsExactlyInAnyOrder(StmtFeature.MODIFIES_SCHEMA, StmtFeature.READS_DATA);
    }

    @Test
    void wordRemainsAnIdentifier() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT cluster FROM cluster.cluster AS cluster", true,
                POSTGRESQL);
        assertSqlCanBeParsedAndDeparsed("CLUSTER cluster USING cluster", true, POSTGRESQL);
        assertSqlCanBeParsedAndDeparsed("CLUSTER cluster ON cluster", true, POSTGRESQL);
        assertInstanceOf(Alter.class, assertSqlCanBeParsedAndDeparsed(
                "ALTER TABLE t CLUSTER ON ix", true, POSTGRESQL));
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE t SET WITHOUT CLUSTER", true, POSTGRESQL);
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE ds.t (id INT64) CLUSTER BY id", true);
    }
}

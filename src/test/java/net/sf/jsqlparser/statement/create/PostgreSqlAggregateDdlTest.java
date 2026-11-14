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
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.function.Consumer;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.DefinitionArgument;
import net.sf.jsqlparser.statement.RoutineReference;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.StmtFeature;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.statement.alter.AlterAggregate;
import net.sf.jsqlparser.statement.create.aggregate.CreateAggregate;
import net.sf.jsqlparser.statement.create.function.CreateFunction;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import net.sf.jsqlparser.util.validation.feature.MySqlVersion;
import net.sf.jsqlparser.util.validation.feature.PostgresqlVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * PostgreSQL CREATE AGGREGATE and ALTER AGGREGATE, see
 * https://www.postgresql.org/docs/18/sql-createaggregate.html and
 * https://www.postgresql.org/docs/18/sql-alteraggregate.html.
 */
class PostgreSqlAggregateDdlTest extends ValidationTestAsserts {
    private static final Consumer<CCJSqlParser> POSTGRESQL =
            parser -> parser.withDialect(Dialect.POSTGRESQL);

    private static Statement assertStatement(String sql, Class<? extends Statement> type)
            throws Exception {
        Statement statement = assertSqlCanBeParsedAndDeparsed(sql, false, POSTGRESQL);
        assertThat(statement).isInstanceOf(type).hasToString(sql);
        assertThat(assertSqlCanBeParsedAndDeparsed(sql)).isInstanceOf(type);
        StatementDeParser deparser = new StatementDeParser(new StringBuilder());
        statement.accept(deparser);
        assertThat(deparser.getBuilder()).hasToString(sql);
        assertThat(new TablesNamesFinder<Void>().getTables(statement)).isEmpty();
        assertThat(statement.getFeatures().getCertain()).contains(StmtFeature.MODIFIES_SCHEMA)
                .doesNotContain(StmtFeature.READS_DATA, StmtFeature.RETURNS_RESULT_SET);
        validateNoErrors(sql, 1, PostgresqlVersion.V10, PostgresqlVersion.V14,
                DatabaseType.POSTGRESQL);

        Statements script = CCJSqlParserUtil.parseStatements(
                sql + "; SELECT 1; " + sql + ";\n" + sql, POSTGRESQL);
        assertThat(script).hasSize(4);
        assertThat(script.get(0)).isInstanceOf(type).hasToString(sql);
        assertThat(script.get(2)).isInstanceOf(type).hasToString(sql);
        assertThat(script.get(3)).isInstanceOf(type).hasToString(sql);
        return statement;
    }

    private static void assertRejected(String sql) {
        assertThatThrownBy(() -> CCJSqlParserUtil.parse(sql, POSTGRESQL))
                .isInstanceOf(JSQLParserException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE AGGREGATE agg(int) (SFUNC = f, STYPE = int, INITCOND = '0')",
            "CREATE OR REPLACE AGGREGATE agg(ORDER BY int) (SFUNC = f, STYPE = internal, "
                    + "FINALFUNC = g, HYPOTHETICAL)",
            "CREATE AGGREGATE s.agg(*) (SFUNC = f, STYPE = int, PARALLEL = SAFE)",
            "CREATE AGGREGATE agg2(int, text) (SFUNC = f, STYPE = int)",
            "CREATE AGGREGATE agg2(a int, b text) (SFUNC = f, STYPE = int)",
            "CREATE AGGREGATE agg2(VARIADIC a int[]) (SFUNC = f, STYPE = int)",
            "CREATE AGGREGATE agg2(VARIADIC \"any\") (SFUNC = f, STYPE = int)",
            "CREATE AGGREGATE agg2(IN a int) (SFUNC = f, STYPE = int)",
            "CREATE AGGREGATE agg2(float8 ORDER BY float8) (SFUNC = f, STYPE = internal, "
                    + "FINALFUNC = g, FINALFUNC_EXTRA)",
            "CREATE AGGREGATE agg2(a int, b text ORDER BY c int, VARIADIC d text[]) "
                    + "(SFUNC = f, STYPE = int)",
            "CREATE AGGREGATE agg2(varchar (3), int[], double precision) (SFUNC = f, STYPE = int)",
            "CREATE AGGREGATE agg2(int) (SFUNC = g, STYPE = int, SORTOP = <, MSFUNC = g, "
                    + "MINVFUNC = g, MSTYPE = int, MSSPACE = 8, SSPACE = 0, "
                    + "FINALFUNC_MODIFY = READ_ONLY, COMBINEFUNC = g)",
            "CREATE AGGREGATE agg2(int) (SFUNC = g, STYPE = int, "
                    + "SORTOP = OPERATOR(pg_catalog.>), INITCOND = '0')",
            "CREATE AGGREGATE agg2(int) (SFUNC = g, stype = int[], initcond = '{}', "
                    + "parallel = restricted)",
            "CREATE AGGREGATE \"Agg\"(int) (\"SFUNC\" = g, STYPE = int)",
            "CREATE AGGREGATE d.public.agg3(int) (SFUNC = g, STYPE = int)",
            "CREATE AGGREGATE agg7(int) (HYPOTHETICAL)",
            // the old form
            "CREATE AGGREGATE agg (BASETYPE = int, SFUNC = f, STYPE = int)",
            "CREATE AGGREGATE agg4 (sfunc = g, stype = int, basetype = int, initcond = '0')",
            "CREATE AGGREGATE agg5 (basetype = 'ANY', sfunc = g, stype = int)",
            "CREATE AGGREGATE agg5 (\"parallel\" = safe, sfunc = g, stype = int)",
            "CREATE AGGREGATE s.agg6 (sfunc1 = g, basetype = int, stype1 = int)",
            "CREATE OR REPLACE AGGREGATE agg (sfunc = g, stype = int, basetype = int)"})
    void testCreateAggregate(String sql) throws Exception {
        assertStatement(sql, CreateAggregate.class);
    }

    @Test
    void testAggregateNamedFunction() throws Exception {
        // FUNCTION is no reserved word in PostgreSQL, while MySQL has CREATE AGGREGATE FUNCTION
        String sql = "CREATE AGGREGATE function(int) (SFUNC = g, STYPE = int)";
        assertThat(assertSqlCanBeParsedAndDeparsed(sql, false, POSTGRESQL))
                .isInstanceOf(CreateAggregate.class);
        assertThat(CCJSqlParserUtil.parse(sql)).isInstanceOf(UnsupportedStatement.class);
    }

    @Test
    void testCreateAggregateFields() throws Exception {
        CreateAggregate aggregate = (CreateAggregate) CCJSqlParserUtil.parse(
                "CREATE OR REPLACE AGGREGATE app.agg (a int, VARIADIC b text[] ORDER BY c float8) "
                        + "(SFUNC = app.f, STYPE = internal, SORTOP = <, INITCOND = '0', "
                        + "HYPOTHETICAL)",
                POSTGRESQL);
        assertThat(aggregate.isOrReplace()).isTrue();
        assertThat(aggregate.isOldStyle()).isFalse();
        RoutineReference signature = aggregate.getAggregate();
        assertThat(signature.getName()).isEqualTo("app.agg");
        assertThat(signature.isAllArguments()).isFalse();
        assertThat(signature.getArguments()).hasSize(2);
        assertThat(signature.getArguments().get(0).getName()).isEqualTo("a");
        assertThat(signature.getArguments().get(1).getMode())
                .isEqualTo(RoutineReference.Argument.Mode.VARIADIC);
        assertThat(signature.getArguments().get(1).getDataType()).hasToString("text[]");
        assertThat(signature.getOrderByArguments()).hasSize(1);
        assertThat(aggregate.getDefinition()).hasSize(5);
        assertThat(aggregate.getDefinitionElement("sfunc").getArgument().getDataType()
                .getDataType()).isEqualTo("app.f");
        assertThat(aggregate.getDefinitionElement("SORTOP").getArgument().getKind())
                .isEqualTo(DefinitionArgument.Kind.OPERATOR);
        assertThat(aggregate.getDefinitionElement("INITCOND").getArgument().getString().getValue())
                .isEqualTo("0");
        assertThat(aggregate.getDefinitionElement("HYPOTHETICAL").getArgument()).isNull();
        assertThat(aggregate.getDefinitionElement("FINALFUNC")).isNull();
        assertThat(aggregate.getASTNode()).isNotNull();

        aggregate.setOrReplace(false);
        signature.setOrderByArguments(null);
        aggregate.getDefinition().subList(2, 5).clear();
        assertThat(aggregate).hasToString("CREATE AGGREGATE app.agg(a int, VARIADIC b text[]) "
                + "(SFUNC = app.f, STYPE = internal)");

        CreateAggregate all = (CreateAggregate) CCJSqlParserUtil.parse(
                "CREATE AGGREGATE cnt (*) (SFUNC = f, STYPE = int8)", POSTGRESQL);
        assertThat(all.getAggregate().isAllArguments()).isTrue();
        assertThat(all.isOldStyle()).isFalse();

        CreateAggregate old = (CreateAggregate) CCJSqlParserUtil.parse(
                "CREATE AGGREGATE app.agg (BASETYPE = int, SFUNC = f, STYPE = int)", POSTGRESQL);
        assertThat(old.isOldStyle()).isTrue();
        assertThat(old.isOrReplace()).isFalse();
        assertThat(old.getAggregate().getName()).isEqualTo("app.agg");
        assertThat(old.getAggregate().getArguments()).isNull();
        assertThat(old.getDefinitionElement("basetype").getArgument().getDataType())
                .hasToString("int");
        assertThat(new CreateAggregate().isOldStyle()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE AGGREGATE agg8 () (SFUNC = g, STYPE = int)",
            "CREATE AGGREGATE agg8 (int)",
            "CREATE AGGREGATE agg8 (int) ()",
            "CREATE AGGREGATE agg8",
            "CREATE AGGREGATE agg8 (int) (SFUNC = g, STYPE = )",
            "CREATE AGGREGATE agg8 (*, int) (SFUNC = g, STYPE = int)",
            "CREATE AGGREGATE agg8 (int ORDER BY) (SFUNC = g, STYPE = int)",
            "CREATE AGGREGATE agg8 (ORDER BY int ORDER BY int) (SFUNC = g, STYPE = int)",
            "CREATE AGGREGATE agg8 (int DEFAULT 1) (SFUNC = g, STYPE = int)",
            "CREATE AGGREGATE agg8 (DISTINCT int) (SFUNC = g, STYPE = int)",
            "CREATE AGGREGATE IF NOT EXISTS agg8 (int) (SFUNC = g, STYPE = int)",
            "CREATE AGGREGATE agg8 (int) (SFUNC = g, STYPE = int) OWNER TO u",
            "CREATE AGGREGATE agg8 (int) SFUNC = g, STYPE = int",
            "CREATE AGGREGATE agg8 (int) (SFUNC = g, STYPE = int) (x = 1)",
            "CREATE AGGREGATE FUNCTION agg8 (int) (SFUNC = g, STYPE = int)",
            "CREATE AGGREGATE FUNCTION f RETURNS STRING SONAME 'x.so'",
            // the old form takes identifiers with values only, and no keyword is an identifier
            "CREATE AGGREGATE agg8 (SFUNC = g, STYPE = int, HYPOTHETICAL)",
            "CREATE AGGREGATE agg8 (sfunc = g, parallel = safe, stype = int)",
            "CREATE AGGREGATE agg8 (parallel = safe, sfunc = g, stype = int)",
            "CREATE AGGREGATE agg8 (sfunc = g, type = int)",
            "CREATE AGGREGATE agg8 (sfunc = g, int = int)",
            "CREATE AGGREGATE agg8 (sfunc = g, between = int)",
            "CREATE AGGREGATE agg8 (sfunc = g, left = int)",
            "CREATE AGGREGATE agg8 (sfunc = g, select = int)"})
    void testRejectedCreateAggregate(String sql) {
        assertRejected(sql);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ALTER AGGREGATE agg(int) RENAME TO agg2",
            "ALTER AGGREGATE agg(VARIADIC a int ORDER BY b text) OWNER TO u",
            "ALTER AGGREGATE agg(*) SET SCHEMA s",
            "ALTER AGGREGATE s.agg(int, text) OWNER TO CURRENT_USER",
            "ALTER AGGREGATE agg(ORDER BY int) RENAME TO \"Agg 2\"",
            "ALTER AGGREGATE agg(a int, b int ORDER BY c int) SET SCHEMA t"})
    void testAlterAggregate(String sql) throws Exception {
        assertStatement(sql, AlterAggregate.class);
    }

    @Test
    void testAlterAggregateFields() throws Exception {
        AlterAggregate rename = (AlterAggregate) CCJSqlParserUtil.parse(
                "ALTER AGGREGATE app.agg (int, text) RENAME TO agg2", POSTGRESQL);
        assertThat(rename.getAggregate().getName()).isEqualTo("app.agg");
        assertThat(rename.getAggregate().getArguments()).hasSize(2);
        assertThat(rename.getAction()).isEqualTo(AlterAggregate.Action.RENAME);
        assertThat(rename.getNewName()).isEqualTo("agg2");
        rename.setAction(AlterAggregate.Action.OWNER);
        rename.setNewName("u");
        assertThat(rename).hasToString("ALTER AGGREGATE app.agg(int, text) OWNER TO u");
        rename.setAction(AlterAggregate.Action.SET_SCHEMA);
        assertThat(rename).hasToString("ALTER AGGREGATE app.agg(int, text) SET SCHEMA u");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ALTER AGGREGATE agg RENAME TO agg2",
            "ALTER AGGREGATE agg () RENAME TO agg2",
            "ALTER AGGREGATE agg (int)",
            "ALTER AGGREGATE agg (int) RENAME agg2",
            "ALTER AGGREGATE agg (int) RENAME TO s.agg2",
            "ALTER AGGREGATE agg (int) SET (x = 1)",
            "ALTER AGGREGATE agg (int) SET SCHEMA",
            "ALTER AGGREGATE agg (int), agg2 (int) OWNER TO u",
            "ALTER AGGREGATE IF EXISTS agg (int) RENAME TO agg2",
            "ALTER AGGREGATE agg (int) OWNER TO u, v",
            "ALTER AGGREGATE agg (int) DEPENDS ON EXTENSION e",
            "ALTER AGGREGATE agg (int) RENAME TO agg2 CASCADE"})
    void testRejectedAlterAggregate(String sql) {
        assertRejected(sql);
        assertThatThrownBy(() -> CCJSqlParserUtil.parse(sql))
                .isInstanceOf(JSQLParserException.class);
    }

    /** PostgreSQL's aggregate signature is "*" or has an argument, wherever one is read. */
    @ParameterizedTest
    @ValueSource(strings = {
            "DROP AGGREGATE agg ()",
            "DROP AGGREGATE agg (int), agg2 ()",
            "COMMENT ON AGGREGATE agg () IS 'x'"})
    void testEmptyAggregateSignatureIsRejected(String sql) {
        assertRejected(sql);
    }

    @Test
    void testOtherDialectsKeepTheirAggregates() throws Exception {
        String mysql = "CREATE AGGREGATE FUNCTION f RETURNS STRING SONAME 'x.so'";
        for (Statement statement : new Statement[] {CCJSqlParserUtil.parse(mysql),
                CCJSqlParserUtil.parse(mysql, parser -> parser.withDialect(Dialect.MYSQL))}) {
            assertThat(statement).isInstanceOf(CreateFunction.class).hasToString(mysql);
            assertThat(((CreateFunction) statement).getLoadableFunction()).isNotNull();
        }
        String sqlServer = "CREATE AGGREGATE dbo.Concatenate (@input nvarchar(200)) "
                + "RETURNS nvarchar(max) EXTERNAL NAME StringUtilities.Concatenate";
        assertThat(CCJSqlParserUtil.parse(sqlServer)).isInstanceOf(UnsupportedStatement.class);
        assertThat(CCJSqlParserUtil.parse(sqlServer,
                parser -> parser.withDialect(Dialect.SQLSERVER)))
                .isInstanceOf(UnsupportedStatement.class);
    }

    @Test
    void testVisitorsAndValidation() throws Exception {
        String create = "CREATE AGGREGATE agg(int) (SFUNC = f, STYPE = int)";
        validateNoErrors(create, 1, FeaturesAllowed.CREATE);
        validateNotAllowed(create, 1, 1, FeaturesAllowed.SELECT, Feature.createAggregate);
        validate(create, 1, 1, MySqlVersion.V8_0);
        String alter = "ALTER AGGREGATE agg(int) RENAME TO agg2";
        validateNoErrors(alter, 1, FeaturesAllowed.ALTER);
        validateNotAllowed(alter, 1, 1, FeaturesAllowed.SELECT, Feature.alterAggregate);
        validate(alter, 1, 1, MySqlVersion.V8_0);

        StatementVisitorAdapter<String> visitor = new StatementVisitorAdapter<String>() {
            @Override
            public <S> String visit(CreateAggregate statement, S context) {
                return context + ":" + statement.getAggregate().getName();
            }

            @Override
            public <S> String visit(AlterAggregate statement, S context) {
                return context + ":" + statement.getNewName();
            }
        };
        assertThat(CCJSqlParserUtil.parse(create, POSTGRESQL).accept(visitor, "c"))
                .isEqualTo("c:agg");
        assertThat(CCJSqlParserUtil.parse(alter, POSTGRESQL).accept(visitor, "c"))
                .isEqualTo("c:agg2");
        assertThat(CCJSqlParserUtil.parse(create, POSTGRESQL)
                .accept(new StatementVisitorAdapter<String>(), "c")).isNull();
    }
}

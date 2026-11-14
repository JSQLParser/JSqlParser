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

import java.util.Arrays;
import java.util.function.Consumer;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.ASTNodeAccess;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.Node;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.CastSignature;
import net.sf.jsqlparser.statement.RoutineReference;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.StmtFeature;
import net.sf.jsqlparser.statement.TransformSignature;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.statement.alter.AlterConversion;
import net.sf.jsqlparser.statement.create.cast.CreateCast;
import net.sf.jsqlparser.statement.create.conversion.CreateConversion;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.transform.CreateTransform;
import net.sf.jsqlparser.statement.drop.Drop;
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
 * PostgreSQL CREATE CAST, CREATE TRANSFORM, CREATE CONVERSION and ALTER CONVERSION, see
 * https://www.postgresql.org/docs/18/sql-createcast.html,
 * https://www.postgresql.org/docs/18/sql-createtransform.html,
 * https://www.postgresql.org/docs/18/sql-createconversion.html and
 * https://www.postgresql.org/docs/18/sql-alterconversion.html.
 */
class PostgreSqlCastTransformConversionTest extends ValidationTestAsserts {
    private static final Consumer<CCJSqlParser> POSTGRESQL =
            parser -> parser.withDialect(Dialect.POSTGRESQL);

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE CAST (int AS text) WITH INOUT AS ASSIGNMENT",
            "CREATE CAST (bigint AS int4) WITH FUNCTION int4(bigint) AS IMPLICIT",
            "CREATE CAST (a.t AS b.u) WITHOUT FUNCTION",
            "CREATE CAST (a.t AS b.u) WITH FUNCTION s.f",
            "CREATE CAST (int AS text) WITH FUNCTION f()",
            "CREATE CAST (int AS text) WITH FUNCTION \"F\"",
            "CREATE CAST (int AS text) WITH FUNCTION left",
            "CREATE CAST (int AS text) WITH FUNCTION pg_catalog.int4(bigint)",
            "CREATE CAST (int AS int[]) WITH FUNCTION s.f(IN a varchar, integer, boolean) AS ASSIGNMENT",
            "CREATE CAST (int AS text) WITH FUNCTION f(VARIADIC int[]) AS ASSIGNMENT",
            "CREATE CAST (int AS text) WITH FUNCTION f(OUT a int, INOUT b text)",
            "CREATE CAST (timestamp with time zone AS double precision) WITH FUNCTION f(internal)",
            "CREATE CAST (\"My T\" AS s.\"U\") WITHOUT FUNCTION AS IMPLICIT",
            "CREATE CAST (int AS text) WITH INOUT",
            "CREATE TRANSFORM FOR int LANGUAGE plpython3u (FROM SQL WITH FUNCTION f(internal), TO SQL WITH FUNCTION g(internal))",
            "CREATE OR REPLACE TRANSFORM FOR hstore LANGUAGE plpython3u (TO SQL WITH FUNCTION g(internal))",
            "CREATE TRANSFORM FOR s.hstore LANGUAGE \"L\" (TO SQL WITH FUNCTION s.g(internal), FROM SQL WITH FUNCTION s.f(internal))",
            "CREATE OR REPLACE TRANSFORM FOR int LANGUAGE l (TO SQL WITH FUNCTION g(internal), FROM SQL WITH FUNCTION f(internal))",
            "CREATE TRANSFORM FOR int LANGUAGE sql (FROM SQL WITH FUNCTION f)",
            "CREATE TRANSFORM FOR int LANGUAGE l (FROM SQL WITH FUNCTION f, TO SQL WITH FUNCTION g)",
            "CREATE TRANSFORM FOR int LANGUAGE l (FROM SQL WITH FUNCTION f(internal, int))",
            "CREATE TRANSFORM FOR int[] LANGUAGE l (TO SQL WITH FUNCTION f())",
            "CREATE CONVERSION c FOR 'UTF8' TO 'LATIN1' FROM f",
            "CREATE DEFAULT CONVERSION s.c FOR 'UTF8' TO 'LATIN1' FROM s.f",
            "CREATE CONVERSION \"C 1\" FOR E'UTF8' TO $$LATIN1$$ FROM pg_catalog.utf8_to_iso8859_1",
            "CREATE DEFAULT CONVERSION c FOR '' TO '' FROM f",
            "CREATE CONVERSION a.b.c FOR 'UTF8' TO 'LATIN1' FROM a.b.f",
            "CREATE CONVERSION c FOR 'UTF8' TO 'LATIN1' FROM \"F\"",
            "ALTER CONVERSION c RENAME TO c2",
            "ALTER CONVERSION c OWNER TO CURRENT_USER",
            "ALTER CONVERSION c OWNER TO CURRENT_ROLE",
            "ALTER CONVERSION c OWNER TO SESSION_USER",
            "ALTER CONVERSION c OWNER TO public",
            "ALTER CONVERSION c OWNER TO left",
            "ALTER CONVERSION c SET SCHEMA s",
            "ALTER CONVERSION s.\"C\" OWNER TO \"Role 1\"",
            "ALTER CONVERSION a.b.c SET SCHEMA s",
            "ALTER CONVERSION c RENAME TO \"select\"",
            "ALTER CONVERSION c RENAME TO conversion",
            "ALTER CONVERSION conversion SET SCHEMA public"})
    void testTypedStatementsRoundTrip(String sql) throws Exception {
        Statement statement = assertSqlCanBeParsedAndDeparsed(sql, false, POSTGRESQL);
        assertThat(statement).isInstanceOfAny(CreateCast.class, CreateTransform.class,
                CreateConversion.class, AlterConversion.class);
        // a statement in PostgreSQL's form is read the same without a dialect and in another one
        assertThat(assertSqlCanBeParsedAndDeparsed(sql)).isInstanceOf(statement.getClass());
        assertThat(CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(Dialect.ORACLE)))
                .isInstanceOf(statement.getClass()).hasToString(sql);

        StatementDeParser deparser = new StatementDeParser(new StringBuilder());
        statement.accept(deparser);
        assertThat(deparser.getBuilder()).hasToString(sql);
        assertThat(new TablesNamesFinder<Void>().getTables(statement)).isEmpty();
        assertThat(statement.getFeatures().getCertain()).contains(StmtFeature.MODIFIES_SCHEMA)
                .doesNotContain(StmtFeature.READS_DATA, StmtFeature.RETURNS_RESULT_SET);
        validateNoErrors(sql, 1, PostgresqlVersion.V10, PostgresqlVersion.V14,
                DatabaseType.POSTGRESQL);
        validateNoErrors(sql, 1, FeaturesAllowed.DDL);

        Node node = ((ASTNodeAccess) statement).getASTNode();
        assertThat(node.jjtGetFirstToken().beginColumn).isEqualTo(1);
        assertThat(node.jjtGetLastToken().endColumn).isEqualTo(sql.length());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "create cast (int as text) with inout as assignment",
            "create or replace transform for int language l (to sql with function g)",
            "create default conversion c for 'UTF8' to 'LATIN1' from f",
            "alter conversion c owner to r"})
    void testKeywordsInLowerCase(String sql) throws Exception {
        Statement statement = CCJSqlParserUtil.parse(sql, POSTGRESQL);
        assertThat(statement).isNotInstanceOf(UnsupportedStatement.class);
        assertThat(statement.toString()).isEqualToIgnoringCase(sql);
        assertThat(CCJSqlParserUtil.parse(statement.toString(), POSTGRESQL))
                .hasToString(statement.toString());
    }

    @Test
    void testStatementBoundaries() throws Exception {
        String script = "CREATE CAST (int AS text) WITH FUNCTION f; "
                + "CREATE CAST (int AS text) WITH INOUT AS IMPLICIT;\n"
                + "CREATE TRANSFORM FOR int LANGUAGE l (FROM SQL WITH FUNCTION f);"
                + "CREATE DEFAULT CONVERSION c FOR 'UTF8' TO 'LATIN1' FROM s.f; "
                + "ALTER CONVERSION c OWNER TO r; ALTER CONVERSION c RENAME TO d; "
                + "DROP CAST (int AS text); DROP TRANSFORM FOR int LANGUAGE l; "
                + "CREATE OPERATOR === (FUNCTION = f, LEFTARG = int); "
                + "CREATE AGGREGATE agg(int) (SFUNC = f, STYPE = int); SELECT 1";
        for (Consumer<CCJSqlParser> mode : Arrays.<Consumer<CCJSqlParser>>asList(POSTGRESQL,
                parser -> {
                })) {
            Statements statements = CCJSqlParserUtil.parseStatements(script, mode);
            assertThat(statements).hasSize(11);
            assertThat(statements).extracting(statement -> statement.getClass().getSimpleName())
                    .containsExactly("CreateCast", "CreateCast", "CreateTransform",
                            "CreateConversion", "AlterConversion", "AlterConversion", "Drop",
                            "Drop", "CreateOperator", "CreateAggregate", "PlainSelect");
            assertThat(statements.get(0)).hasToString("CREATE CAST (int AS text) WITH FUNCTION f");
            assertThat(statements.get(5)).hasToString("ALTER CONVERSION c RENAME TO d");
        }
    }

    @Test
    void testCastFields() throws Exception {
        CreateCast cast = (CreateCast) CCJSqlParserUtil.parse(
                "CREATE CAST (bigint AS s.t) WITH FUNCTION s.f(bigint, integer) AS ASSIGNMENT",
                POSTGRESQL);
        assertThat(cast.getCast().getSourceType().getDataType()).isEqualTo("bigint");
        assertThat(cast.getCast().getTargetType().getDataType()).isEqualTo("s.t");
        assertThat(cast.getMethod()).isEqualTo(CreateCast.Method.WITH_FUNCTION);
        assertThat(cast.getFunction().getName()).isEqualTo("s.f");
        assertThat(cast.getFunction().getArguments()).hasSize(2);
        assertThat(cast.getFunction().getArguments().get(1).getDataType().getDataType())
                .isEqualTo("integer");
        assertThat(cast.getContext()).isEqualTo(CreateCast.Context.ASSIGNMENT);

        // the cast is named by the signature object DROP CAST uses, with its own node
        Drop drop = (Drop) CCJSqlParserUtil.parse("DROP CAST (bigint AS s.t)", POSTGRESQL);
        assertThat(cast.getCast()).isInstanceOf(drop.getCast().getClass())
                .hasToString(drop.getCast().toString());
        Node node = cast.getCast().getASTNode();
        assertThat(node.jjtGetFirstToken().image).isEqualTo("(");
        assertThat(node.jjtGetLastToken().image).isEqualTo(")");

        CreateCast bare = (CreateCast) CCJSqlParserUtil
                .parse("CREATE CAST (int AS text) WITH FUNCTION f", POSTGRESQL);
        assertThat(bare.getFunction().getArguments()).isNull();
        assertThat(bare.getContext()).isNull();

        CreateCast inout = (CreateCast) CCJSqlParserUtil
                .parse("CREATE CAST (int AS text) WITH INOUT AS IMPLICIT", POSTGRESQL);
        assertThat(inout.getMethod()).isEqualTo(CreateCast.Method.WITH_INOUT);
        assertThat(inout.getFunction()).isNull();
        assertThat(inout.getContext()).isEqualTo(CreateCast.Context.IMPLICIT);

        CreateCast binary = (CreateCast) CCJSqlParserUtil
                .parse("CREATE CAST (int AS text) WITHOUT FUNCTION", POSTGRESQL);
        assertThat(binary.getMethod()).isEqualTo(CreateCast.Method.WITHOUT_FUNCTION);
        assertThat(binary.getFunction()).isNull();
    }

    @Test
    void testCastMethodAndFunctionStayConsistent() {
        RoutineReference function = new RoutineReference();
        function.setName("f");
        CreateCast cast = new CreateCast()
                .withCast(new CastSignature(new ColDataType("int"), new ColDataType("text")))
                .withFunction(function).withContext(CreateCast.Context.IMPLICIT);
        assertThat(cast.getMethod()).isEqualTo(CreateCast.Method.WITH_FUNCTION);
        assertThat(cast).hasToString("CREATE CAST (int AS text) WITH FUNCTION f AS IMPLICIT");

        cast.setMethod(CreateCast.Method.WITH_INOUT);
        assertThat(cast.getFunction()).isNull();
        assertThat(cast.withContext(null)).hasToString("CREATE CAST (int AS text) WITH INOUT");
        assertThat(cast.withMethod(CreateCast.Method.WITHOUT_FUNCTION))
                .hasToString("CREATE CAST (int AS text) WITHOUT FUNCTION");
        cast.setFunction(null);
        assertThat(cast.getMethod()).isEqualTo(CreateCast.Method.WITHOUT_FUNCTION);
    }

    @Test
    void testTransformFields() throws Exception {
        CreateTransform transform = (CreateTransform) CCJSqlParserUtil.parse(
                "CREATE OR REPLACE TRANSFORM FOR public.hstore LANGUAGE plpython3u "
                        + "(TO SQL WITH FUNCTION g(internal), FROM SQL WITH FUNCTION s.f(internal))",
                POSTGRESQL);
        assertThat(transform.isOrReplace()).isTrue();
        assertThat(transform.getTransform().getDataType().getDataType())
                .isEqualTo("public.hstore");
        assertThat(transform.getTransform().getLanguage()).isEqualTo("plpython3u");
        assertThat(transform.getFromSqlFunction().getName()).isEqualTo("s.f");
        assertThat(transform.getToSqlFunction().getName()).isEqualTo("g");
        assertThat(transform.getToSqlFunction().getArguments()).hasSize(1);
        assertThat(transform.isToSqlFirst()).isTrue();

        // the transform is named by the signature object DROP TRANSFORM uses, with its own node
        Drop drop = (Drop) CCJSqlParserUtil
                .parse("DROP TRANSFORM FOR public.hstore LANGUAGE plpython3u", POSTGRESQL);
        assertThat(transform.getTransform()).isInstanceOf(drop.getTransform().getClass())
                .hasToString(drop.getTransform().toString());
        Node node = transform.getTransform().getASTNode();
        assertThat(node.jjtGetFirstToken().image).isEqualTo("FOR");
        assertThat(node.jjtGetLastToken().image).isEqualTo("plpython3u");

        transform.setToSqlFirst(false);
        transform.setOrReplace(false);
        assertThat(transform).hasToString("CREATE TRANSFORM FOR public.hstore LANGUAGE plpython3u "
                + "(FROM SQL WITH FUNCTION s.f(internal), TO SQL WITH FUNCTION g(internal))");
        transform.setFromSqlFunction(null);
        assertThat(transform).hasToString("CREATE TRANSFORM FOR public.hstore LANGUAGE plpython3u "
                + "(TO SQL WITH FUNCTION g(internal))");

        CreateTransform single = (CreateTransform) CCJSqlParserUtil.parse(
                "CREATE TRANSFORM FOR int LANGUAGE l (FROM SQL WITH FUNCTION f)", POSTGRESQL);
        assertThat(single.isOrReplace()).isFalse();
        assertThat(single.getToSqlFunction()).isNull();
        assertThat(single.isToSqlFirst()).isFalse();
        assertThat(single.getFromSqlFunction().getArguments()).isNull();

        RoutineReference function = new RoutineReference();
        function.setName("g");
        assertThat(new CreateTransform().withOrReplace(true)
                .withTransform(new TransformSignature(new ColDataType("int"), "l"))
                .withToSqlFunction(function).withFromSqlFunction(function).withToSqlFirst(true))
                .hasToString("CREATE OR REPLACE TRANSFORM FOR int LANGUAGE l "
                        + "(TO SQL WITH FUNCTION g, FROM SQL WITH FUNCTION g)");
    }

    @Test
    void testConversionFields() throws Exception {
        CreateConversion conversion = (CreateConversion) CCJSqlParserUtil.parse(
                "CREATE DEFAULT CONVERSION s.\"C\" FOR 'UTF8' TO E'LATIN1' FROM pg_catalog.f",
                POSTGRESQL);
        assertThat(conversion.isDefaultConversion()).isTrue();
        assertThat(conversion.getName()).isEqualTo("s.\"C\"");
        assertThat(conversion.getSourceEncoding().getValue()).isEqualTo("UTF8");
        assertThat(conversion.getDestinationEncoding().getValue()).isEqualTo("LATIN1");
        assertThat(conversion.getDestinationEncoding().getPrefix()).isEqualTo("E");
        assertThat(conversion.getFunction()).isEqualTo("pg_catalog.f");
        assertThat(conversion.getSourceEncoding().getASTNode().jjtGetFirstToken().image)
                .isEqualTo("'UTF8'");

        // U&'...' is a string constant only for the PostgreSQL dialect's lexer
        String unicode = "CREATE CONVERSION c FOR U&'UTF8' TO $$LATIN1$$ FROM f";
        assertThat(assertSqlCanBeParsedAndDeparsed(unicode, false, POSTGRESQL))
                .isInstanceOf(CreateConversion.class);

        assertThat(((CreateConversion) CCJSqlParserUtil
                .parse("CREATE CONVERSION c FOR 'a' TO 'b' FROM f", POSTGRESQL))
                .isDefaultConversion()).isFalse();
        assertThat(new CreateConversion().withName("c").withSourceEncoding(new StringValue("a"))
                .withDestinationEncoding(new StringValue("b")).withFunction("f")
                .withDefaultConversion(true))
                .hasToString("CREATE DEFAULT CONVERSION c FOR 'a' TO 'b' FROM f");
    }

    @Test
    void testAlterConversionFields() throws Exception {
        AlterConversion rename = (AlterConversion) CCJSqlParserUtil
                .parse("ALTER CONVERSION s.c RENAME TO \"C 2\"", POSTGRESQL);
        assertThat(rename.getName()).isEqualTo("s.c");
        assertThat(rename.getAction()).isEqualTo(AlterConversion.Action.RENAME);
        assertThat(rename.getNewName()).isEqualTo("\"C 2\"");
        assertThat(rename.getOwner()).isNull();
        assertThat(rename.getSchemaName()).isNull();

        AlterConversion owner = (AlterConversion) CCJSqlParserUtil
                .parse("ALTER CONVERSION c OWNER TO CURRENT_USER", POSTGRESQL);
        assertThat(owner.getAction()).isEqualTo(AlterConversion.Action.OWNER);
        assertThat(owner.getOwner()).isEqualTo("CURRENT_USER");

        AlterConversion schema = (AlterConversion) CCJSqlParserUtil
                .parse("ALTER CONVERSION c SET SCHEMA app", POSTGRESQL);
        assertThat(schema.getAction()).isEqualTo(AlterConversion.Action.SET_SCHEMA);
        assertThat(schema.getSchemaName()).isEqualTo("app");

        assertThat(new AlterConversion().withName("c").withAction(AlterConversion.Action.RENAME)
                .withNewName("d")).hasToString("ALTER CONVERSION c RENAME TO d");
        assertThat(new AlterConversion().withName("c").withAction(AlterConversion.Action.OWNER)
                .withOwner("r")).hasToString("ALTER CONVERSION c OWNER TO r");
        assertThat(new AlterConversion().withName("c")
                .withAction(AlterConversion.Action.SET_SCHEMA).withSchemaName("s"))
                .hasToString("ALTER CONVERSION c SET SCHEMA s");
    }

    @Test
    void testVisitors() throws Exception {
        StatementVisitorAdapter<String> visitor = new StatementVisitorAdapter<String>() {
            @Override
            public <S> String visit(CreateCast statement, S context) {
                return context + ":cast";
            }

            @Override
            public <S> String visit(CreateTransform statement, S context) {
                return context + ":transform";
            }

            @Override
            public <S> String visit(CreateConversion statement, S context) {
                return context + ":conversion";
            }

            @Override
            public <S> String visit(AlterConversion statement, S context) {
                return context + ":alter";
            }
        };
        assertThat(parse("CREATE CAST (int AS text) WITH INOUT").accept(visitor, "c"))
                .isEqualTo("c:cast");
        assertThat(parse("CREATE TRANSFORM FOR int LANGUAGE l (TO SQL WITH FUNCTION f)")
                .accept(visitor, "c")).isEqualTo("c:transform");
        assertThat(parse("CREATE CONVERSION c FOR 'a' TO 'b' FROM f").accept(visitor, "c"))
                .isEqualTo("c:conversion");
        assertThat(parse("ALTER CONVERSION c SET SCHEMA s").accept(visitor, "c"))
                .isEqualTo("c:alter");

        // the default visitor methods return nothing and the one-argument forms delegate
        StatementVisitorAdapter<String> adapter = new StatementVisitorAdapter<>();
        for (String sql : new String[] {"CREATE CAST (int AS text) WITH INOUT",
                "CREATE TRANSFORM FOR int LANGUAGE l (TO SQL WITH FUNCTION f)",
                "CREATE CONVERSION c FOR 'a' TO 'b' FROM f", "ALTER CONVERSION c SET SCHEMA s"}) {
            assertThat(parse(sql).accept(adapter, null)).isNull();
        }
        adapter.visit((CreateCast) parse("CREATE CAST (int AS text) WITH INOUT"));
        adapter.visit((CreateTransform) parse(
                "CREATE TRANSFORM FOR int LANGUAGE l (TO SQL WITH FUNCTION f)"));
        adapter.visit((CreateConversion) parse("CREATE CONVERSION c FOR 'a' TO 'b' FROM f"));
        adapter.visit((AlterConversion) parse("ALTER CONVERSION c SET SCHEMA s"));
    }

    @Test
    void testValidation() throws Exception {
        String cast = "CREATE CAST (int AS text) WITH INOUT";
        validateNoErrors(cast, 1, FeaturesAllowed.CREATE);
        validateNotAllowed(cast, 1, 1, FeaturesAllowed.SELECT, Feature.createCast);
        validate(cast, 1, 1, MySqlVersion.V8_0);

        String transform = "CREATE TRANSFORM FOR int LANGUAGE l (TO SQL WITH FUNCTION f)";
        validateNoErrors(transform, 1, FeaturesAllowed.CREATE);
        validateNotAllowed(transform, 1, 1, FeaturesAllowed.SELECT, Feature.createTransform);
        validate(transform, 1, 1, MySqlVersion.V8_0);

        String conversion = "CREATE CONVERSION c FOR 'a' TO 'b' FROM f";
        validateNoErrors(conversion, 1, FeaturesAllowed.CREATE);
        validateNotAllowed(conversion, 1, 1, FeaturesAllowed.SELECT, Feature.createConversion);
        validate(conversion, 1, 1, MySqlVersion.V8_0);

        String alter = "ALTER CONVERSION c RENAME TO d";
        validateNoErrors(alter, 1, FeaturesAllowed.ALTER);
        validateNotAllowed(alter, 1, 1, FeaturesAllowed.SELECT, Feature.alterConversion);
        validate(alter, 1, 1, MySqlVersion.V8_0);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE OR REPLACE CAST (int AS text) WITH INOUT",
            "CREATE CAST (int AS text)",
            "CREATE CAST (int AS text) WITH FUNCTION",
            "CREATE CAST (int AS text) WITHOUT FUNCTION f(int)",
            "CREATE CAST (int AS text) WITH INOUT AS EXPLICIT",
            "CREATE CAST (int AS text) WITH INOUT AS",
            "CREATE CAST (int, text) WITH INOUT",
            "CREATE CAST int AS text WITH INOUT",
            "CREATE CAST (int AS text) WITH INOUT AS ASSIGNMENT AS IMPLICIT",
            "CREATE CAST (int AS text) WITH FUNCTION f AS ASSIGNMENT IMPLICIT",
            "CREATE CAST (int AS text) AS ASSIGNMENT WITH INOUT",
            "CREATE CAST (int AS text) WITHOUT INOUT",
            "CREATE CAST IF NOT EXISTS (int AS text) WITH INOUT",
            "CREATE CAST (int AS text) WITH SPECIFIC FUNCTION f",
            "CREATE CAST (int AS text) WITH FUNCTION f(*)",
            "CREATE CAST (int AS text) WITH FUNCTION f, g",
            "CREATE CAST (int AS text) WITH FUNCTION 'f'",
            "CREATE CAST (int AS) WITH INOUT",
            "CREATE CAST () WITH INOUT",
            "CREATE CAST s.c (int AS text) WITH INOUT",
            "CREATE CAST (int AS text) WITH INOUT CASCADE",
            "CREATE DEFAULT CAST (int AS text) WITH INOUT",
            "CREATE TRANSFORM FOR int LANGUAGE l ()",
            "CREATE TRANSFORM FOR int LANGUAGE l",
            "CREATE TRANSFORM FOR int LANGUAGE l (FROM SQL WITH FUNCTION f, FROM SQL WITH FUNCTION g)",
            "CREATE TRANSFORM FOR int LANGUAGE l (TO SQL WITH FUNCTION f, TO SQL WITH FUNCTION g)",
            "CREATE TRANSFORM FOR int LANGUAGE l (FROM SQL WITH FUNCTION f, TO SQL WITH FUNCTION g, FROM SQL WITH FUNCTION h)",
            "CREATE TRANSFORM FOR int LANGUAGE l (FROM SQL WITH FUNCTION f TO SQL WITH FUNCTION g)",
            "CREATE TRANSFORM FOR int (FROM SQL WITH FUNCTION f)",
            "CREATE TRANSFORM int LANGUAGE l (FROM SQL WITH FUNCTION f)",
            "CREATE TRANSFORM FOR int LANGUAGE l (FROM SQL FUNCTION f)",
            "CREATE TRANSFORM FOR int LANGUAGE l (WITH FUNCTION f)",
            "CREATE TRANSFORM FOR int LANGUAGE l (FROM SQL WITH FUNCTION f,)",
            "CREATE TRANSFORM FOR int LANGUAGE l FROM SQL WITH FUNCTION f",
            "CREATE TRANSFORM IF NOT EXISTS FOR int LANGUAGE l (FROM SQL WITH FUNCTION f)",
            "CREATE TRANSFORM FOR int LANGUAGE 'l' (FROM SQL WITH FUNCTION f)",
            "CREATE TRANSFORM FOR int LANGUAGE s.l (FROM SQL WITH FUNCTION f)",
            "CREATE TRANSFORM FOR int LANGUAGE l (FROM SQL WITHOUT FUNCTION)",
            "CREATE TRANSFORM FOR int LANGUAGE l (FROM SQL WITH INOUT)",
            "CREATE TRANSFORM FOR int LANGUAGE l (FROM SQL WITH FUNCTION f) CASCADE",
            "CREATE DEFAULT TRANSFORM FOR int LANGUAGE l (FROM SQL WITH FUNCTION f)",
            "CREATE OR REPLACE CONVERSION c FOR 'UTF8' TO 'LATIN1' FROM f",
            "CREATE OR REPLACE DEFAULT CONVERSION c FOR 'UTF8' TO 'LATIN1' FROM f",
            "CREATE CONVERSION c FOR UTF8 TO 'LATIN1' FROM f",
            "CREATE CONVERSION c FOR 'UTF8' TO LATIN1 FROM f",
            "CREATE CONVERSION c FOR 'UTF8' 'LATIN1' FROM f",
            "CREATE CONVERSION c TO 'LATIN1' FOR 'UTF8' FROM f",
            "CREATE CONVERSION c FOR 'UTF8' TO 'LATIN1'",
            "CREATE CONVERSION c FOR 'UTF8' TO 'LATIN1' FROM f(int)",
            "CREATE CONVERSION c FOR 'UTF8' TO 'LATIN1' FROM 'f'",
            "CREATE CONVERSION c FOR 'UTF8' TO 'LATIN1' FROM f, g",
            "CREATE CONVERSION c FOR 'UTF8' TO 'LATIN1' WITH FUNCTION f",
            "CREATE CONVERSION FOR 'UTF8' TO 'LATIN1' FROM f",
            "CREATE CONVERSION c FOR N'UTF8' TO 'LATIN1' FROM f",
            "CREATE CONVERSION c FOR B'01' TO 'LATIN1' FROM f",
            "CREATE CONVERSION c FOR X'01' TO 'LATIN1' FROM f",
            "CREATE CONVERSION IF NOT EXISTS c FOR 'UTF8' TO 'LATIN1' FROM f",
            "CREATE CONVERSION DEFAULT c FOR 'UTF8' TO 'LATIN1' FROM f",
            "CREATE CONVERSION c FOR 'UTF8' TO 'LATIN1' FROM f WITH x",
            "CREATE CONVERSION select FOR 'UTF8' TO 'LATIN1' FROM f",
            "CREATE CONVERSION c FOR 'UTF8' TO 'LATIN1' FROM select",
            "ALTER CONVERSION c",
            "ALTER CONVERSION c RENAME c2",
            "ALTER CONVERSION c RENAME TO s.c2",
            "ALTER CONVERSION c RENAME TO left",
            "ALTER CONVERSION c RENAME TO select",
            "ALTER CONVERSION c SET SCHEMA a.b",
            "ALTER CONVERSION c SET SCHEMA user",
            "ALTER CONVERSION c SET SCHEMA",
            "ALTER CONVERSION c SET (a = 1)",
            "ALTER CONVERSION c SET TABLESPACE x",
            "ALTER CONVERSION c OWNER TO a, b",
            "ALTER CONVERSION c OWNER TO user",
            "ALTER CONVERSION c OWNER TO 'r'",
            "ALTER CONVERSION c OWNER TO CURRENT_USER()",
            "ALTER CONVERSION c OWNER TO CURRENT_USER CASCADE",
            "ALTER CONVERSION c OWNER r",
            "ALTER CONVERSION c REFRESH VERSION",
            "ALTER CONVERSION IF EXISTS c RENAME TO c2",
            "ALTER CONVERSION c RENAME TO c2, OWNER TO r",
            "ALTER CONVERSION c, d RENAME TO c2",
            "ALTER CONVERSION select RENAME TO c2",
            "ALTER DEFAULT CONVERSION c RENAME TO c2",
            "ALTER CONVERSION c DEPENDS ON EXTENSION e"})
    void testRejectedWithPostgreSqlDialect(String sql) {
        assertThatThrownBy(() -> CCJSqlParserUtil.parse(sql, POSTGRESQL))
                .isInstanceOf(JSQLParserException.class);
    }

    /**
     * Without the PostgreSQL dialect, statements of another product that start with the same words
     * but do not have PostgreSQL's form stay unsupported statements, as they were before.
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE CAST (a AS b) WITH SPECIFIC FUNCTION f",
            "CREATE CAST (percent AS decimal)",
            "CREATE OR REPLACE CAST (a AS b) WITH INOUT",
            "CREATE TRANSFORM FOR t g1 (TO SQL WITH FUNCTION f, FROM SQL WITH FUNCTION g)",
            "CREATE OR REPLACE CONVERSION c FOR 'UTF8' TO 'LATIN1' FROM f",
            "CREATE DEFAULT conversion AS 1",
            "CREATE DEFAULT x",
            "CREATE CONVERSION c",
            "ALTER CONVERSION c REFRESH",
            "ALTER CONVERSION c"})
    void testOtherFormsStayUnsupportedWithoutThePostgreSqlDialect(String sql) throws Exception {
        assertThatThrownBy(() -> CCJSqlParserUtil.parse(sql, POSTGRESQL))
                .isInstanceOf(JSQLParserException.class);
        assertThat(CCJSqlParserUtil.parse(sql)).isInstanceOf(UnsupportedStatement.class);
        assertThat(CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(Dialect.ORACLE)))
                .isInstanceOf(UnsupportedStatement.class);
    }

    /** Only the PostgreSQL dialect applies the name rules of PostgreSQL's grammar. */
    @ParameterizedTest
    @ValueSource(strings = {
            "ALTER CONVERSION c SET SCHEMA user",
            "CREATE CONVERSION c FOR N'UTF8' TO 'LATIN1' FROM f"})
    void testPostgreSqlNameRulesNeedTheDialect(String sql) throws Exception {
        assertThat(CCJSqlParserUtil.parse(sql)).isNotInstanceOf(UnsupportedStatement.class)
                .hasToString(sql);
    }

    private static Statement parse(String sql) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sql, POSTGRESQL);
    }
}

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
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.StmtFeature;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.statement.alter.AlterOperator;
import net.sf.jsqlparser.statement.alter.AlterOperatorClass;
import net.sf.jsqlparser.statement.alter.AlterOperatorFamily;
import net.sf.jsqlparser.statement.comment.Comment;
import net.sf.jsqlparser.statement.create.operator.CreateOperator;
import net.sf.jsqlparser.statement.create.operator.CreateOperatorClass;
import net.sf.jsqlparser.statement.create.operator.CreateOperatorFamily;
import net.sf.jsqlparser.statement.create.operator.OperatorClassItem;
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
 * PostgreSQL CREATE and ALTER OPERATOR, OPERATOR CLASS and OPERATOR FAMILY, see
 * https://www.postgresql.org/docs/18/sql-createoperator.html,
 * https://www.postgresql.org/docs/18/sql-alteroperator.html,
 * https://www.postgresql.org/docs/18/sql-createopclass.html,
 * https://www.postgresql.org/docs/18/sql-alteropclass.html,
 * https://www.postgresql.org/docs/18/sql-createopfamily.html and
 * https://www.postgresql.org/docs/18/sql-alteropfamily.html.
 */
class PostgreSqlOperatorDdlTest extends ValidationTestAsserts {
    private static final Consumer<CCJSqlParser> POSTGRESQL =
            parser -> parser.withDialect(Dialect.POSTGRESQL);

    private static Statement assertStatement(String sql, Class<? extends Statement> type)
            throws Exception {
        assertThat(assertSqlCanBeParsedAndDeparsed(sql)).isInstanceOf(type);
        return assertPostgreSqlStatement(sql, type);
    }

    /** For an operator such as ##, which only the PostgreSQL dialect does not lex as a name. */
    private static Statement assertPostgreSqlStatement(String sql,
            Class<? extends Statement> type) throws Exception {
        Statement statement = assertSqlCanBeParsedAndDeparsed(sql, false, POSTGRESQL);
        assertThat(statement).isInstanceOf(type).hasToString(sql);
        StatementDeParser deparser = new StatementDeParser(new StringBuilder());
        statement.accept(deparser);
        assertThat(deparser.getBuilder()).hasToString(sql);
        assertThat(new TablesNamesFinder<Void>().getTables(statement)).isEmpty();
        assertThat(statement.getFeatures().getCertain()).contains(StmtFeature.MODIFIES_SCHEMA)
                .doesNotContain(StmtFeature.READS_DATA, StmtFeature.RETURNS_RESULT_SET);

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
            "CREATE OPERATOR === (FUNCTION = f, LEFTARG = int, RIGHTARG = int)",
            "CREATE OPERATOR s.<-> (PROCEDURE = s.f, LEFTARG = point, RIGHTARG = point, "
                    + "COMMUTATOR = <->, NEGATOR = OPERATOR(s.<!>), RESTRICT = eqsel, "
                    + "JOIN = eqjoinsel, HASHES, MERGES)",
            "CREATE OPERATOR public.!== (function = f, leftarg = int, rightarg = int, negator = ===)",
            "CREATE OPERATOR d.public.=== (FUNCTION = f, LEFTARG = int, RIGHTARG = int)",
            "CREATE OPERATOR \"s\".@@@ (FUNCTION = f, LEFTARG = int, RIGHTARG = int)",
            "CREATE OPERATOR class.+ (FUNCTION = f)",
            "CREATE OPERATOR family.+ (FUNCTION = f)",
            "CREATE OPERATOR <#> (FUNCTION = f)",
            "CREATE OPERATOR <#> (HASHES)",
            "CREATE OPERATOR <#> (FUNCTION = f, LEFTARG = varchar (3), RIGHTARG = int[], "
                    + "\"Quoted\" = 'x', n = 1, m = -1.5, k = true, j = NONE, i = SETOF int)",
            "CREATE OPERATOR <#> (select = f, table = int)"})
    void testCreateOperator(String sql) throws Exception {
        assertStatement(sql, CreateOperator.class);
        validateNoErrors(sql, 1, PostgresqlVersion.V10, PostgresqlVersion.V14,
                DatabaseType.POSTGRESQL);
    }

    /** PostgreSQL reads any run of operator characters as one operator. */
    @ParameterizedTest
    @ValueSource(strings = {
            "+", "-", "*", "/", "%", "^", "<", ">", "=", "~", "!", "@", "#", "&", "|", "?",
            "<=", ">=", "<>", "!=", "==", "===", "<->", "<==>", "@@", "@@@", "@#@", "@#", "#@",
            "<<|", "&<|", "|>>", "~=", "<^", ">^", "?#", "?-|", "?||", "##", "@-@", "!!", "|/",
            "||/", "*<>", "~<~", "^@", "<<=", "-|-", "&&&", "~~*", "!~~*", "#-", "<%", "%>",
            "<<%", "->>", "#>>", "?&", "%%", "|&>", "&<", "&>", ">>=", "<<->>", "=*", "*=",
            "<=>", "@>", "<@", ">>", "<<", "|>", "||", "&&", "^^", "**", "~*", "^=", "-#",
            "@?", "#>", "@-", "~+", "@+", "~-", "-+-|", "+*", "-="})
    void testOperatorNameIsOneNameWithItsExactText(String operator) throws Exception {
        String create = "CREATE OPERATOR s." + operator
                + " (FUNCTION = f, COMMUTATOR = " + operator + ", NEGATOR = OPERATOR(s."
                + operator + "))";
        CreateOperator created =
                (CreateOperator) assertPostgreSqlStatement(create, CreateOperator.class);
        // without the dialect, the lexer reads # before # or @ as the start of a name
        if (!operator.startsWith("##") && !operator.startsWith("#@")) {
            assertThat(assertSqlCanBeParsedAndDeparsed(create)).isInstanceOf(CreateOperator.class);
        }
        assertThat(created.getOperator().getSchemaName()).isEqualTo("s");
        assertThat(created.getOperator().getName()).isEqualTo(operator);
        assertThat(created.getDefinitionElement("commutator").getArgument().getOperator())
                .isEqualTo(operator);
        assertThat(created.getDefinitionElement("NEGATOR").getArgument().getOperator())
                .isEqualTo("s." + operator);

        String alter = "ALTER OPERATOR " + operator + " (int, int) OWNER TO u";
        AlterOperator altered =
                (AlterOperator) assertPostgreSqlStatement(alter, AlterOperator.class);
        assertThat(altered.getOperator().getOperator().getName()).isEqualTo(operator);

        String family = "ALTER OPERATOR FAMILY f USING btree ADD OPERATOR 1 " + operator
                + " (int, int), OPERATOR 2 " + operator;
        AlterOperatorFamily added = (AlterOperatorFamily) assertPostgreSqlStatement(family,
                AlterOperatorFamily.class);
        assertThat(added.getItems().get(0).getOperator().getName()).isEqualTo(operator);
        assertThat(added.getItems().get(1).getOperator().getName()).isEqualTo(operator);

        String drop = "DROP OPERATOR " + operator + " (int, int)";
        Drop dropped = (Drop) assertSqlCanBeParsedAndDeparsed(drop, false, POSTGRESQL);
        assertThat(dropped.getOperators().get(0).getOperator().getName()).isEqualTo(operator);
    }

    /**
     * Texts PostgreSQL's lexer does not take as one operator: a name of several characters ends in
     * + or - only if it has one of ~ ! @ # % ^ &amp; | ` ?, and =&gt; is a token of its own.
     */
    @ParameterizedTest
    @ValueSource(strings = {"=>", "=-", "+-", "*-", "-+-", "<=+", "= =", "< =", "::", ":=", "..",
            "myop", "@#x", "OPERATOR(===)", "\"===\"", "'==='", "(", ""})
    void testRejectedOperatorNames(String operator) {
        assertRejected("CREATE OPERATOR " + operator + " (FUNCTION = f)");
        assertRejected("CREATE OPERATOR s." + operator + " (FUNCTION = f)");
        assertRejected("CREATE OPERATOR === (FUNCTION = f, COMMUTATOR = OPERATOR(" + operator
                + "))");
        assertRejected("ALTER OPERATOR " + operator + " (int, int) OWNER TO u");
        assertRejected("ALTER OPERATOR FAMILY f USING btree ADD OPERATOR 1 " + operator
                + " (int, int)");
        assertRejected("DROP OPERATOR " + operator + " (int, int)");
        assertRejected("COMMENT ON OPERATOR " + operator + " (int, int) IS 'x'");
    }

    @Test
    void testOperatorNameLength() throws Exception {
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < 63; i++) {
            name.append('<');
        }
        assertStatement("CREATE OPERATOR " + name + " (FUNCTION = f)", CreateOperator.class);
        assertRejected("CREATE OPERATOR " + name + "< (FUNCTION = f)");
    }

    @Test
    void testSharedOperatorReaders() throws Exception {
        Drop drop = (Drop) assertSqlCanBeParsedAndDeparsed(
                "DROP OPERATOR d.public.<==> (int, int), @#@ (NONE, int)", false, POSTGRESQL);
        assertThat(drop.getOperators().get(0).getOperator().getSchemaName())
                .isEqualTo("d.public");
        assertThat(drop.getOperators().get(0).getOperator().getName()).isEqualTo("<==>");
        assertThat(drop.getOperators().get(1).getOperator().getName()).isEqualTo("@#@");
        assertThat(assertSqlCanBeParsedAndDeparsed("COMMENT ON OPERATOR d.public.<==> (int, int)"
                + " IS 'x'", false, POSTGRESQL)).isInstanceOf(Comment.class);
        assertSqlCanBeParsedAndDeparsed("CREATE TYPE t (INPUT = f, x = <==>, y = OPERATOR(a.b.@#))",
                false, POSTGRESQL);
        // an Oracle operator keeps its name
        assertThat(((Drop) CCJSqlParserUtil.parse("DROP OPERATOR a.b.c")).getOperators())
                .isEmpty();
    }

    @Test
    void testCreateOperatorFields() throws Exception {
        CreateOperator operator = (CreateOperator) CCJSqlParserUtil.parse(
                "CREATE OPERATOR app.=== (FUNCTION = app.eq, LEFTARG = int, COMMUTATOR = ===, "
                        + "NEGATOR = OPERATOR(app.!==), HASHES)",
                POSTGRESQL);
        assertThat(operator.getOperator().getSchemaName()).isEqualTo("app");
        assertThat(operator.getOperator().getName()).isEqualTo("===");
        assertThat(operator.getOperator().getQualifiedName()).isEqualTo("app.===");
        assertThat(operator.getDefinition()).hasSize(5);
        DefinitionArgument function = operator.getDefinitionElement("function").getArgument();
        assertThat(function.getKind()).isEqualTo(DefinitionArgument.Kind.TYPE);
        assertThat(function.getDataType().getDataType()).isEqualTo("app.eq");
        DefinitionArgument negator = operator.getDefinitionElement("NEGATOR").getArgument();
        assertThat(negator.getKind()).isEqualTo(DefinitionArgument.Kind.OPERATOR);
        assertThat(negator.isOperatorKeyword()).isTrue();
        assertThat(negator.getOperator()).isEqualTo("app.!==");
        assertThat(operator.getDefinitionElement("HASHES").getArgument()).isNull();
        assertThat(operator.getDefinitionElement("MERGES")).isNull();
        assertThat(operator.getASTNode()).isNotNull();

        operator.getOperator().setName("<=>");
        operator.getDefinition().remove(4);
        assertThat(operator).hasToString("CREATE OPERATOR app.<=> (FUNCTION = app.eq, "
                + "LEFTARG = int, COMMUTATOR = ===, NEGATOR = OPERATOR(app.!==))");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE OPERATOR === ()",
            "CREATE OPERATOR ===",
            "CREATE OPERATOR === (FUNCTION = f,)",
            "CREATE OPERATOR === (FUNCTION f)",
            "CREATE OPERATOR === (FUNCTION = )",
            "CREATE OPERATOR === (int, int) (FUNCTION = f)",
            "CREATE OPERATOR === (FUNCTION = f) OWNER TO u",
            "CREATE OR REPLACE OPERATOR === (FUNCTION = f, LEFTARG = int, RIGHTARG = int)",
            "CREATE OPERATOR IF NOT EXISTS === (FUNCTION = f, LEFTARG = int, RIGHTARG = int)",
            "CREATE OPERATOR === (FUNCTION = f, COMMUTATOR = +-)",
            "CREATE OPERATOR === (FUNCTION = f, COMMUTATOR = =>)",
            "CREATE OPERATOR eq BINDING (VARCHAR2, VARCHAR2) RETURN NUMBER USING eq_f"})
    void testRejectedCreateOperator(String sql) {
        assertRejected(sql);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ALTER OPERATOR + (int, int) OWNER TO u",
            "ALTER OPERATOR === (int, int) OWNER TO CURRENT_USER",
            "ALTER OPERATOR === (int, int) OWNER TO SESSION_USER",
            "ALTER OPERATOR === (int, int) OWNER TO CURRENT_ROLE",
            "ALTER OPERATOR s.@@ (NONE, text) SET SCHEMA t",
            "ALTER OPERATOR t.=== (int, int) SET SCHEMA public",
            "ALTER OPERATOR === (int, int) SET (RESTRICT = eqsel, JOIN = NONE, HASHES)",
            "ALTER OPERATOR === (int, int) SET (RESTRICT = NONE)",
            "ALTER OPERATOR === (int, int) SET (COMMUTATOR = OPERATOR(public.===), NEGATOR = !==, "
                    + "MERGES = false, HASHES = true)",
            "ALTER OPERATOR === (int, int) SET (x = 'a', y = 1, z = s.f2)",
            "ALTER OPERATOR - (NONE, int) OWNER TO u",
            "ALTER OPERATOR ! (bigint, NONE) OWNER TO u",
            "ALTER OPERATOR d.public.=== (int, int) OWNER TO u",
            "ALTER OPERATOR family.+ (varchar (3), int[]) OWNER TO u"})
    void testAlterOperator(String sql) throws Exception {
        assertStatement(sql, AlterOperator.class);
        validateNoErrors(sql, 1, PostgresqlVersion.V10, PostgresqlVersion.V14,
                DatabaseType.POSTGRESQL);
    }

    @Test
    void testAlterOperatorFields() throws Exception {
        AlterOperator owner = (AlterOperator) CCJSqlParserUtil.parse(
                "ALTER OPERATOR app.@@ (NONE, text) OWNER TO u", POSTGRESQL);
        assertThat(owner.getOperator().getOperator().getQualifiedName()).isEqualTo("app.@@");
        assertThat(owner.getOperator().getLeftType()).isNull();
        assertThat(owner.getOperator().getRightType()).hasToString("text");
        assertThat(owner.getOperator().getASTNode()).isNotNull();
        assertThat(owner.getAction()).isEqualTo(AlterOperator.Action.OWNER);
        assertThat(owner.getNewName()).isEqualTo("u");
        assertThat(owner.getProperties()).isEmpty();

        AlterOperator schema = (AlterOperator) CCJSqlParserUtil.parse(
                "ALTER OPERATOR + (int, int) SET SCHEMA \"S\"", POSTGRESQL);
        assertThat(schema.getAction()).isEqualTo(AlterOperator.Action.SET_SCHEMA);
        assertThat(schema.getNewName()).isEqualTo("\"S\"");

        AlterOperator properties = (AlterOperator) CCJSqlParserUtil.parse(
                "ALTER OPERATOR + (int, int) SET (RESTRICT = eqsel, JOIN = NONE, HASHES)",
                POSTGRESQL);
        assertThat(properties.getAction()).isEqualTo(AlterOperator.Action.SET_PROPERTIES);
        assertThat(properties.getNewName()).isNull();
        assertThat(properties.getProperties()).hasSize(3);
        assertThat(properties.getProperties().get(1).getArgument().getKind())
                .isEqualTo(DefinitionArgument.Kind.NONE);
        properties.getProperties().remove(2);
        properties.getOperator().setLeftType(null);
        assertThat(properties).hasToString(
                "ALTER OPERATOR + (NONE, int) SET (RESTRICT = eqsel, JOIN = NONE)");
        properties.setAction(null);
        assertThatThrownBy(properties::toString).isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ALTER OPERATOR === (int, int) RENAME TO ====",
            "ALTER OPERATOR === (int, int) RENAME TO x",
            "ALTER OPERATOR === OWNER TO u",
            "ALTER OPERATOR === (int) OWNER TO u",
            "ALTER OPERATOR === (NONE, NONE) OWNER TO u",
            "ALTER OPERATOR === (int, int, int) OWNER TO u",
            "ALTER OPERATOR === (int, int) SET ()",
            "ALTER OPERATOR === (int, int) SET (RESTRICT = )",
            "ALTER OPERATOR === (int, int) SET RESTRICT = eqsel",
            "ALTER OPERATOR === (int, int) OWNER u",
            "ALTER OPERATOR === (int, int) SET SCHEMA",
            "ALTER OPERATOR === (int, int)",
            "ALTER OPERATOR IF EXISTS === (int, int) OWNER TO u",
            "ALTER OPERATOR === (int, int), !== (int, int) OWNER TO u",
            "ALTER OPERATOR myop (int, int) OWNER TO u",
            "ALTER OPERATOR === (int, int) OWNER TO u CASCADE",
            "ALTER OPERATOR eq COMPILE"})
    void testRejectedAlterOperator(String sql) {
        assertRejected(sql);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE OPERATOR CLASS oc DEFAULT FOR TYPE int USING btree AS OPERATOR 1 <, "
                    + "FUNCTION 1 f(int, int)",
            "CREATE OPERATOR CLASS oc FOR TYPE int USING gist FAMILY fam AS OPERATOR 1 < (int, int) "
                    + "FOR ORDER BY float_ops, FUNCTION 1 (int, int) f(int, int), STORAGE int",
            "CREATE OPERATOR CLASS s.oc2 FOR TYPE int4 USING btree FAMILY public.fam AS "
                    + "OPERATOR 1 < FOR SEARCH, OPERATOR 2 <=, OPERATOR 3 =, OPERATOR 4 >=, "
                    + "OPERATOR 5 >, FUNCTION 1 cmp(int, int)",
            "CREATE OPERATOR CLASS oc3 FOR TYPE varchar (3) USING hash AS STORAGE int[]",
            "CREATE OPERATOR CLASS oc3 FOR TYPE int USING btree AS FUNCTION 1 cmp",
            "CREATE OPERATOR CLASS oc3 FOR TYPE int USING btree AS FUNCTION 1 s.cmp",
            "CREATE OPERATOR CLASS oc3 FOR TYPE int USING btree AS FUNCTION 1 (int) cmp(int, int)",
            "CREATE OPERATOR CLASS oc3 FOR TYPE int USING btree AS FUNCTION 1 cmp()",
            "CREATE OPERATOR CLASS oc3 FOR TYPE int USING btree AS "
                    + "FUNCTION 2 (int, text) s.f(IN a int, VARIADIC b text[])",
            "CREATE OPERATOR CLASS oc3 FOR TYPE int USING btree AS "
                    + "OPERATOR 1 pg_catalog.< (int, int) FOR ORDER BY pg_catalog.float_ops",
            "CREATE OPERATOR CLASS oc3 FOR TYPE int USING btree AS OPERATOR 1 pg_catalog.<",
            "CREATE OPERATOR CLASS oc3 FOR TYPE int USING btree AS OPERATOR 1 - (NONE, int)",
            "CREATE OPERATOR CLASS oc3 FOR TYPE int USING btree AS OPERATOR 1 ! (int, NONE)",
            "CREATE OPERATOR CLASS oc3 FOR TYPE timestamp with time zone USING btree AS "
                    + "OPERATOR 1 <",
            "CREATE OPERATOR CLASS oc3 FOR TYPE double precision USING btree AS "
                    + "STORAGE double precision",
            "CREATE OPERATOR CLASS \"Oc\" FOR TYPE s.\"T\" USING \"btree\" AS OPERATOR 1 <",
            "CREATE OPERATOR CLASS class FOR TYPE int USING btree AS OPERATOR 1 <",
            "CREATE OPERATOR CLASS family.oc FOR TYPE int USING btree AS OPERATOR 1 <",
            "CREATE OPERATOR CLASS oc3 FOR TYPE int USING btree AS OPERATOR 0 <"})
    void testCreateOperatorClass(String sql) throws Exception {
        assertStatement(sql, CreateOperatorClass.class);
        validateNoErrors(sql, 1, PostgresqlVersion.V10, PostgresqlVersion.V14,
                DatabaseType.POSTGRESQL);
    }

    @Test
    void testCreateOperatorClassFields() throws Exception {
        CreateOperatorClass operatorClass = (CreateOperatorClass) CCJSqlParserUtil.parse(
                "CREATE OPERATOR CLASS app.oc DEFAULT FOR TYPE varchar(3) USING gist "
                        + "FAMILY app.fam AS OPERATOR 1 app.<< (NONE, int) FOR ORDER BY s.float_ops, "
                        + "OPERATOR 2 = FOR SEARCH, OPERATOR 3 <, "
                        + "FUNCTION 4 (int, text) app.f(int, text), FUNCTION 5 g, STORAGE int[]",
                POSTGRESQL);
        assertThat(operatorClass.getName()).isEqualTo("app.oc");
        assertThat(operatorClass.isDefaultClass()).isTrue();
        assertThat(operatorClass.getDataType()).hasToString("varchar (3)");
        assertThat(operatorClass.getDataType().getASTNode()).isNotNull();
        assertThat(operatorClass.getAccessMethod()).isEqualTo("gist");
        assertThat(operatorClass.getFamily()).isEqualTo("app.fam");
        assertThat(operatorClass.getItems()).hasSize(6);

        OperatorClassItem ordering = operatorClass.getItems().get(0);
        assertThat(ordering.getKind()).isEqualTo(OperatorClassItem.Kind.OPERATOR);
        assertThat(ordering.getNumber()).isEqualTo(1);
        assertThat(ordering.getOperator().getSchemaName()).isEqualTo("app");
        assertThat(ordering.getOperator().getName()).isEqualTo("<<");
        assertThat(ordering.getOperandTypes()).hasSize(2);
        assertThat(ordering.getOperandTypes().get(0)).isNull();
        assertThat(ordering.getOperandTypes().get(1)).hasToString("int");
        assertThat(ordering.getPurpose()).isEqualTo(OperatorClassItem.Purpose.ORDER_BY);
        assertThat(ordering.getSortFamily()).isEqualTo("s.float_ops");
        assertThat(ordering.getFunction()).isNull();

        OperatorClassItem search = operatorClass.getItems().get(1);
        assertThat(search.getOperandTypes()).isNull();
        assertThat(search.getPurpose()).isEqualTo(OperatorClassItem.Purpose.SEARCH);
        assertThat(search.getSortFamily()).isNull();
        assertThat(operatorClass.getItems().get(2).getPurpose()).isNull();

        OperatorClassItem function = operatorClass.getItems().get(3);
        assertThat(function.getKind()).isEqualTo(OperatorClassItem.Kind.FUNCTION);
        assertThat(function.getNumber()).isEqualTo(4);
        assertThat(function.getOperandTypes()).hasSize(2);
        assertThat(function.getFunction().getName()).isEqualTo("app.f");
        assertThat(function.getFunction().getArguments()).hasSize(2);
        assertThat(function.getOperator()).isNull();
        assertThat(operatorClass.getItems().get(4).getFunction().getArguments()).isNull();
        assertThat(operatorClass.getItems().get(4).getOperandTypes()).isNull();

        OperatorClassItem storage = operatorClass.getItems().get(5);
        assertThat(storage.getKind()).isEqualTo(OperatorClassItem.Kind.STORAGE);
        assertThat(storage.getNumber()).isNull();
        assertThat(storage.getStorageType()).hasToString("int[]");

        operatorClass.setDefaultClass(false);
        operatorClass.setFamily(null);
        operatorClass.getItems().subList(1, 6).clear();
        ordering.setPurpose(null);
        OperatorClassItem added = new OperatorClassItem(OperatorClassItem.Kind.STORAGE);
        added.setStorageType(storage.getStorageType());
        operatorClass.getItems().add(added);
        assertThat(operatorClass).hasToString("CREATE OPERATOR CLASS app.oc FOR TYPE varchar (3) "
                + "USING gist AS OPERATOR 1 app.<< (NONE, int), STORAGE int[]");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE OPERATOR CLASS oc3 FOR TYPE int USING btree",
            "CREATE OPERATOR CLASS oc3 FOR TYPE int USING btree AS",
            "CREATE OPERATOR CLASS oc3 FOR TYPE int AS OPERATOR 1 <",
            "CREATE OPERATOR CLASS oc3 USING btree AS OPERATOR 1 <",
            "CREATE OPERATOR CLASS oc3 FOR int USING btree AS OPERATOR 1 <",
            "CREATE OPERATOR CLASS oc3 FOR TYPE int DEFAULT USING btree AS OPERATOR 1 <",
            "CREATE OPERATOR CLASS oc3 FOR TYPE int USING s.btree AS OPERATOR 1 <",
            "CREATE OPERATOR CLASS oc3 FOR TYPE int USING btree AS OPERATOR <",
            "CREATE OPERATOR CLASS oc3 FOR TYPE int USING btree AS OPERATOR 1",
            "CREATE OPERATOR CLASS oc3 FOR TYPE int USING btree AS OPERATOR 1.5 <",
            "CREATE OPERATOR CLASS oc3 FOR TYPE int USING btree AS OPERATOR -1 <",
            "CREATE OPERATOR CLASS oc3 FOR TYPE int USING btree AS OPERATOR 99999999999 <",
            "CREATE OPERATOR CLASS oc3 FOR TYPE int USING btree AS OPERATOR 1 < (int)",
            "CREATE OPERATOR CLASS oc3 FOR TYPE int USING btree AS OPERATOR 1 < (NONE, NONE)",
            "CREATE OPERATOR CLASS oc3 FOR TYPE int USING btree AS OPERATOR 1 < FOR ORDER float_ops",
            "CREATE OPERATOR CLASS oc3 FOR TYPE int USING btree AS OPERATOR 1 < FOR",
            "CREATE OPERATOR CLASS oc3 FOR TYPE int USING btree AS OPERATOR 1 < RECHECK",
            "CREATE OPERATOR CLASS oc3 FOR TYPE int USING btree AS OPERATOR 1 OPERATOR(<)",
            "CREATE OPERATOR CLASS oc3 FOR TYPE int USING btree AS OPERATOR 1 +-",
            "CREATE OPERATOR CLASS oc3 FOR TYPE int USING btree AS FUNCTION cmp(int, int)",
            "CREATE OPERATOR CLASS oc3 FOR TYPE int USING btree AS FUNCTION 1",
            "CREATE OPERATOR CLASS oc3 FOR TYPE int USING btree AS FUNCTION 1 () cmp(int, int)",
            "CREATE OPERATOR CLASS oc3 FOR TYPE int USING btree AS "
                    + "FUNCTION 1 (NONE, int) cmp(int, int)",
            "CREATE OPERATOR CLASS oc3 FOR TYPE int USING btree AS "
                    + "FUNCTION 1 cmp(int, int) FOR SEARCH",
            "CREATE OPERATOR CLASS oc3 FOR TYPE int USING btree AS STORAGE",
            "CREATE OPERATOR CLASS oc3 FOR TYPE int USING btree AS STORAGE int,",
            "CREATE OPERATOR CLASS oc3 FOR TYPE int USING btree AS OPERATOR 1 < OPERATOR 2 <=",
            "CREATE OPERATOR CLASS oc3 FOR TYPE int USING btree FAMILY AS OPERATOR 1 <",
            "CREATE OR REPLACE OPERATOR CLASS oc3 FOR TYPE int USING btree AS OPERATOR 1 <",
            "CREATE OPERATOR CLASS IF NOT EXISTS oc3 FOR TYPE int USING btree AS OPERATOR 1 <"})
    void testRejectedCreateOperatorClass(String sql) {
        assertRejected(sql);
        assertThatThrownBy(() -> CCJSqlParserUtil.parse(sql))
                .isInstanceOf(JSQLParserException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ALTER OPERATOR CLASS oc USING btree RENAME TO oc2",
            "ALTER OPERATOR CLASS s.oc USING btree OWNER TO u",
            "ALTER OPERATOR CLASS oc USING btree OWNER TO CURRENT_USER",
            "ALTER OPERATOR CLASS oc USING btree SET SCHEMA s",
            "ALTER OPERATOR CLASS \"Oc\" USING \"btree\" RENAME TO \"Oc2\"",
            "ALTER OPERATOR CLASS class USING btree RENAME TO family"})
    void testAlterOperatorClass(String sql) throws Exception {
        assertStatement(sql, AlterOperatorClass.class);
        validateNoErrors(sql, 1, PostgresqlVersion.V10, PostgresqlVersion.V14,
                DatabaseType.POSTGRESQL);
    }

    @Test
    void testAlterOperatorClassFields() throws Exception {
        AlterOperatorClass rename = (AlterOperatorClass) CCJSqlParserUtil.parse(
                "ALTER OPERATOR CLASS app.oc USING btree RENAME TO oc2", POSTGRESQL);
        assertThat(rename.getName()).isEqualTo("app.oc");
        assertThat(rename.getAccessMethod()).isEqualTo("btree");
        assertThat(rename.getAction()).isEqualTo(AlterOperatorClass.Action.RENAME);
        assertThat(rename.getNewName()).isEqualTo("oc2");
        rename.setAction(AlterOperatorClass.Action.OWNER);
        rename.setNewName("u");
        assertThat(rename).hasToString("ALTER OPERATOR CLASS app.oc USING btree OWNER TO u");
        rename.setAction(AlterOperatorClass.Action.SET_SCHEMA);
        rename.setName("oc");
        rename.setAccessMethod("hash");
        assertThat(rename).hasToString("ALTER OPERATOR CLASS oc USING hash SET SCHEMA u");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ALTER OPERATOR CLASS oc RENAME TO oc2",
            "ALTER OPERATOR CLASS oc USING btree",
            "ALTER OPERATOR CLASS oc USING btree RENAME oc2",
            "ALTER OPERATOR CLASS oc USING btree RENAME TO s.oc2",
            "ALTER OPERATOR CLASS oc USING btree SET (x = 1)",
            "ALTER OPERATOR CLASS oc USING btree ADD OPERATOR 1 <",
            "ALTER OPERATOR CLASS IF EXISTS oc USING btree RENAME TO oc2",
            "ALTER OPERATOR CLASS oc USING btree SET SCHEMA s.t",
            "ALTER OPERATOR CLASS oc USING btree RENAME TO oc2 CASCADE"})
    void testRejectedAlterOperatorClass(String sql) {
        assertRejected(sql);
        assertThatThrownBy(() -> CCJSqlParserUtil.parse(sql))
                .isInstanceOf(JSQLParserException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE OPERATOR FAMILY f USING btree",
            "CREATE OPERATOR FAMILY s.f USING hash",
            "CREATE OPERATOR FAMILY \"F 1\" USING \"btree\"",
            "CREATE OPERATOR FAMILY family USING btree"})
    void testCreateOperatorFamily(String sql) throws Exception {
        assertStatement(sql, CreateOperatorFamily.class);
        validateNoErrors(sql, 1, PostgresqlVersion.V10, PostgresqlVersion.V14,
                DatabaseType.POSTGRESQL);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE OPERATOR FAMILY f",
            "CREATE OPERATOR FAMILY f USING",
            "CREATE OPERATOR FAMILY f USING s.btree",
            "CREATE OPERATOR FAMILY f USING btree AS OPERATOR 1 <",
            "CREATE OPERATOR FAMILY IF NOT EXISTS f USING btree",
            "CREATE OR REPLACE OPERATOR FAMILY f USING btree",
            "CREATE OPERATOR FAMILY f USING btree, hash",
            "CREATE OPERATOR FAMILY f FOR TYPE int USING btree"})
    void testRejectedCreateOperatorFamily(String sql) {
        assertRejected(sql);
        assertThatThrownBy(() -> CCJSqlParserUtil.parse(sql))
                .isInstanceOf(JSQLParserException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ALTER OPERATOR FAMILY f USING btree ADD OPERATOR 1 < (int, int), "
                    + "FUNCTION 1 (int, int) g(int, int)",
            "ALTER OPERATOR FAMILY f USING btree ADD OPERATOR 1 < (int4, int2) FOR SEARCH, "
                    + "OPERATOR 2 <= (int4, int2) FOR ORDER BY s.fam2",
            "ALTER OPERATOR FAMILY f USING btree ADD FUNCTION 1 cmp(int, int)",
            "ALTER OPERATOR FAMILY f USING btree ADD FUNCTION 1 (int4) cmp(int, int), "
                    + "FUNCTION 2 (int4, int2) s.cmp2(internal)",
            "ALTER OPERATOR FAMILY f USING btree ADD OPERATOR 1 <",
            "ALTER OPERATOR FAMILY f USING btree ADD STORAGE int",
            "ALTER OPERATOR FAMILY f USING btree DROP OPERATOR 1 (int, int), FUNCTION 1 (int, int)",
            "ALTER OPERATOR FAMILY f USING btree DROP OPERATOR 1 (int4)",
            "ALTER OPERATOR FAMILY f USING btree DROP FUNCTION 2 (int4, int2, int8)",
            "ALTER OPERATOR FAMILY s.f USING hash DROP FUNCTION 1 (varchar (3), int[])",
            "ALTER OPERATOR FAMILY f USING btree OWNER TO u",
            "ALTER OPERATOR FAMILY f USING btree OWNER TO SESSION_USER",
            "ALTER OPERATOR FAMILY f USING btree RENAME TO f2",
            "ALTER OPERATOR FAMILY f USING btree SET SCHEMA s",
            "ALTER OPERATOR FAMILY family USING btree RENAME TO class"})
    void testAlterOperatorFamily(String sql) throws Exception {
        assertStatement(sql, AlterOperatorFamily.class);
        validateNoErrors(sql, 1, PostgresqlVersion.V10, PostgresqlVersion.V14,
                DatabaseType.POSTGRESQL);
    }

    @Test
    void testOperatorFamilyFields() throws Exception {
        CreateOperatorFamily family = (CreateOperatorFamily) CCJSqlParserUtil.parse(
                "CREATE OPERATOR FAMILY app.fam USING btree", POSTGRESQL);
        assertThat(family.getName()).isEqualTo("app.fam");
        assertThat(family.getAccessMethod()).isEqualTo("btree");
        family.setName("fam");
        family.setAccessMethod("hash");
        assertThat(family).hasToString("CREATE OPERATOR FAMILY fam USING hash");

        AlterOperatorFamily add = (AlterOperatorFamily) CCJSqlParserUtil.parse(
                "ALTER OPERATOR FAMILY app.fam USING btree ADD OPERATOR 1 < (int4, int2), "
                        + "FUNCTION 1 (int4, int2) btint42cmp(int4, int2)",
                POSTGRESQL);
        assertThat(add.getName()).isEqualTo("app.fam");
        assertThat(add.getAccessMethod()).isEqualTo("btree");
        assertThat(add.getAction()).isEqualTo(AlterOperatorFamily.Action.ADD);
        assertThat(add.getNewName()).isNull();
        assertThat(add.getItems()).hasSize(2);
        assertThat(add.getItems().get(0).getOperator().getName()).isEqualTo("<");
        assertThat(add.getItems().get(1).getFunction().getName()).isEqualTo("btint42cmp");

        AlterOperatorFamily drop = (AlterOperatorFamily) CCJSqlParserUtil.parse(
                "ALTER OPERATOR FAMILY fam USING btree DROP OPERATOR 1 (int4, int2), "
                        + "FUNCTION 2 (int4)",
                POSTGRESQL);
        assertThat(drop.getAction()).isEqualTo(AlterOperatorFamily.Action.DROP);
        OperatorClassItem operator = drop.getItems().get(0);
        assertThat(operator.getKind()).isEqualTo(OperatorClassItem.Kind.OPERATOR);
        assertThat(operator.getNumber()).isEqualTo(1);
        assertThat(operator.getOperator()).isNull();
        assertThat(operator.getOperandTypes()).hasSize(2);
        OperatorClassItem function = drop.getItems().get(1);
        assertThat(function.getKind()).isEqualTo(OperatorClassItem.Kind.FUNCTION);
        assertThat(function.getFunction()).isNull();
        assertThat(function.getOperandTypes()).hasSize(1);
        drop.getItems().remove(0);
        function.setNumber(3);
        assertThat(drop)
                .hasToString("ALTER OPERATOR FAMILY fam USING btree DROP FUNCTION 3 (int4)");

        AlterOperatorFamily owner = (AlterOperatorFamily) CCJSqlParserUtil.parse(
                "ALTER OPERATOR FAMILY fam USING btree OWNER TO u", POSTGRESQL);
        assertThat(owner.getAction()).isEqualTo(AlterOperatorFamily.Action.OWNER);
        assertThat(owner.getNewName()).isEqualTo("u");
        assertThat(owner.getItems()).isEmpty();
        owner.setAction(AlterOperatorFamily.Action.RENAME);
        assertThat(owner).hasToString("ALTER OPERATOR FAMILY fam USING btree RENAME TO u");
        owner.setAction(AlterOperatorFamily.Action.SET_SCHEMA);
        assertThat(owner).hasToString("ALTER OPERATOR FAMILY fam USING btree SET SCHEMA u");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ALTER OPERATOR FAMILY f USING btree ADD",
            "ALTER OPERATOR FAMILY f USING btree DROP",
            "ALTER OPERATOR FAMILY f USING btree DROP OPERATOR 1",
            "ALTER OPERATOR FAMILY f USING btree DROP OPERATOR 1 < (int, int)",
            "ALTER OPERATOR FAMILY f USING btree DROP OPERATOR 1 ()",
            "ALTER OPERATOR FAMILY f USING btree DROP OPERATOR 1 (NONE, int)",
            "ALTER OPERATOR FAMILY f USING btree DROP FUNCTION 1 cmp(int, int)",
            "ALTER OPERATOR FAMILY f USING btree DROP FUNCTION (int, int)",
            "ALTER OPERATOR FAMILY f USING btree DROP STORAGE int",
            "ALTER OPERATOR FAMILY f USING btree DROP OPERATOR 1 (int, int) CASCADE",
            "ALTER OPERATOR FAMILY f ADD OPERATOR 1 < (int, int)",
            "ALTER OPERATOR FAMILY f USING btree ADD OPERATOR 1 < (int, int) "
                    + "DROP OPERATOR 2 (int, int)",
            "ALTER OPERATOR FAMILY f USING btree ADD OPERATOR 1 < (int, int), "
                    + "DROP OPERATOR 2 (int, int)",
            "ALTER OPERATOR FAMILY f USING btree SET (x = 1)",
            "ALTER OPERATOR FAMILY f USING btree RENAME TO",
            "ALTER OPERATOR FAMILY IF EXISTS f USING btree RENAME TO f2",
            "ALTER OPERATOR FAMILY f USING btree"})
    void testRejectedAlterOperatorFamily(String sql) {
        assertRejected(sql);
        assertThatThrownBy(() -> CCJSqlParserUtil.parse(sql))
                .isInstanceOf(JSQLParserException.class);
    }

    @Test
    void testOtherDialectsKeepOracleOperators() throws Exception {
        for (String sql : new String[] {
                "CREATE OPERATOR eq_op BINDING (VARCHAR2, VARCHAR2) RETURN NUMBER USING eq_f",
                "CREATE OR REPLACE OPERATOR s.eq_op BINDING (VARCHAR2) RETURN NUMBER USING eq_f",
                "CREATE OPERATOR class BINDING (VARCHAR2) RETURN NUMBER USING eq_f",
                "ALTER OPERATOR eq_op COMPILE",
                "ALTER OPERATOR class COMPILE",
                "ALTER OPERATOR eq_op ADD BINDING (NUMBER) RETURN NUMBER USING f",
                "ALTER OPERATOR family ADD BINDING (NUMBER) RETURN NUMBER USING f"}) {
            assertThat(CCJSqlParserUtil.parse(sql)).isInstanceOf(UnsupportedStatement.class);
            assertThat(CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(Dialect.ORACLE)))
                    .isInstanceOf(UnsupportedStatement.class);
        }
    }

    @Test
    void testVisitorsAndValidation() throws Exception {
        String[][] statements = {
                {"CREATE OPERATOR === (FUNCTION = f)", "createOperator"},
                {"ALTER OPERATOR === (int, int) OWNER TO u", "alterOperator"},
                {"CREATE OPERATOR CLASS oc FOR TYPE int USING btree AS OPERATOR 1 <",
                        "createOperatorClass"},
                {"ALTER OPERATOR CLASS oc USING btree RENAME TO oc2", "alterOperatorClass"},
                {"CREATE OPERATOR FAMILY f USING btree", "createOperatorFamily"},
                {"ALTER OPERATOR FAMILY f USING btree RENAME TO f2", "alterOperatorFamily"}};
        for (String[] entry : statements) {
            String sql = entry[0];
            Feature feature = Feature.valueOf(entry[1]);
            validateNoErrors(sql, 1,
                    feature.name().startsWith("create") ? FeaturesAllowed.CREATE
                            : FeaturesAllowed.ALTER);
            validateNotAllowed(sql, 1, 1, FeaturesAllowed.SELECT, feature);
            validate(sql, 1, 1, MySqlVersion.V8_0);
        }

        StatementVisitorAdapter<String> visitor = new StatementVisitorAdapter<String>() {
            @Override
            public <S> String visit(CreateOperator statement, S context) {
                return context + ":" + statement.getOperator().getName();
            }

            @Override
            public <S> String visit(AlterOperator statement, S context) {
                return context + ":" + statement.getAction();
            }

            @Override
            public <S> String visit(CreateOperatorClass statement, S context) {
                return context + ":" + statement.getName();
            }

            @Override
            public <S> String visit(AlterOperatorClass statement, S context) {
                return context + ":" + statement.getNewName();
            }

            @Override
            public <S> String visit(CreateOperatorFamily statement, S context) {
                return context + ":" + statement.getAccessMethod();
            }

            @Override
            public <S> String visit(AlterOperatorFamily statement, S context) {
                return context + ":" + statement.getAction();
            }
        };
        String[] expected = {"c:===", "c:OWNER", "c:oc", "c:oc2", "c:btree", "c:RENAME"};
        for (int i = 0; i < statements.length; i++) {
            Statement statement = CCJSqlParserUtil.parse(statements[i][0], POSTGRESQL);
            assertThat(statement.accept(visitor, "c")).isEqualTo(expected[i]);
            assertThat(statement.accept(new StatementVisitorAdapter<String>(), "c")).isNull();
        }
    }
}

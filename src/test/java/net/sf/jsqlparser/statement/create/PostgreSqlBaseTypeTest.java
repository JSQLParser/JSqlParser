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
import static net.sf.jsqlparser.util.validation.ValidationTestAsserts.validateNoErrors;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.SignedExpression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.DefinitionArgument;
import net.sf.jsqlparser.statement.DefinitionArgument.Kind;
import net.sf.jsqlparser.statement.DefinitionElement;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.StmtFeature;
import net.sf.jsqlparser.statement.alter.AlterType;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.type.BaseTypeDefinition;
import net.sf.jsqlparser.statement.create.type.CompositeTypeDefinition;
import net.sf.jsqlparser.statement.create.type.CreateType;
import net.sf.jsqlparser.statement.create.type.EnumTypeDefinition;
import net.sf.jsqlparser.statement.create.type.RangeTypeDefinition;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * PostgreSQL base types: CREATE TYPE name (definition) and ALTER TYPE name SET (definition), see
 * https://www.postgresql.org/docs/18/sql-createtype.html and
 * https://www.postgresql.org/docs/18/sql-altertype.html.
 */
class PostgreSqlBaseTypeTest {
    private static final Consumer<CCJSqlParser> POSTGRESQL =
            parser -> parser.withDialect(Dialect.POSTGRESQL);

    private static List<DefinitionElement> elements(String sql) throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql, POSTGRESQL);
        return statement instanceof CreateType
                ? ((BaseTypeDefinition) ((CreateType) statement).getDefinition()).getElements()
                : ((AlterType) statement).getProperties();
    }

    private static String deparse(Statement statement) {
        StatementDeParser deparser = new StatementDeParser(new StringBuilder());
        statement.accept(deparser);
        return deparser.getBuilder().toString();
    }

    private static DefinitionArgument argument(String value) throws JSQLParserException {
        return elements("CREATE TYPE b1 (x = " + value + ")").get(0).getArgument();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE TYPE box3 (INPUT = box3_in, OUTPUT = box3_out, INTERNALLENGTH = 16)",
            "CREATE TYPE box3 (INPUT = box3_in, OUTPUT = box3_out, INTERNALLENGTH = 4, PASSEDBYVALUE)",
            "CREATE TYPE b1 (INPUT = box3_in, OUTPUT = box3_out, RECEIVE = box3_recv, SEND = box3_send, "
                    + "TYPMOD_IN = a, TYPMOD_OUT = b, ANALYZE = c, SUBSCRIPT = d, "
                    + "INTERNALLENGTH = VARIABLE, PASSEDBYVALUE, ALIGNMENT = double, "
                    + "STORAGE = extended, LIKE = int4, CATEGORY = 'U', PREFERRED = true, "
                    + "DEFAULT = 'x', ELEMENT = int4, DELIMITER = ',', COLLATABLE = false)",
            "CREATE TYPE b1 (INPUT = public.box3_in, OUTPUT = box3_out, ELEMENT = int4[], "
                    + "LIKE = double precision, DEFAULT = NULL, INTERNALLENGTH = -1)",
            "CREATE TYPE s.b1 (INPUT = box3_in)",
            "CREATE TYPE a.b.c (x = 1)",
            "CREATE TYPE b1 (foo = bar)",
            "CREATE TYPE b1 (foo)",
            "CREATE TYPE b1 (select = 1, table, from = x)",
            "CREATE TYPE b1 (x = NONE, y = +, z = OPERATOR(pg_catalog.<=), w = 1.5, v = +2, u = -3)",
            "CREATE TYPE b1 (x = 1e3, y = .5)",
            "CREATE TYPE b1 (x = OPERATOR(+), y = operator)",
            "CREATE TYPE b1 (x = OPERATOR(a.b.+))",
            "CREATE TYPE b1 (x = OPERATOR(=), y = =, z = <>, w = !=, v = <=, u = >=, t = ||, "
                    + "s = ->>, r = @>, q = ~~*, p = %, o = ^, n = /, m = *, l = <, k = >)",
            "CREATE TYPE b1 (x = @)",
            "CREATE TYPE b1 (x = <@>)",
            "CREATE TYPE b1 (x = select, y = user, z = true, w = null, v = default, u = on, t = table)",
            "CREATE TYPE b1 (x = null, y = array, z = any)",
            "CREATE TYPE b1 (x = left, y = inner, z = like, w = authorization)",
            "CREATE TYPE b1 (x = extended, y = key, z = type, w = value)",
            "CREATE TYPE b1 (x = int, y = char, z = bit, w = json, v = interval, u = timestamp)",
            "CREATE TYPE b1 (x = a.b.c.d)",
            "CREATE TYPE b1 (x = a.b.c%TYPE)",
            "CREATE TYPE b1 (x = SETOF int[])",
            "CREATE TYPE b1 (x = SETOF int, y = t.a%TYPE, z = SETOF t.a%TYPE, s = int ARRAY[3])",
            "CREATE TYPE b1 (x = char varying, z = time without time zone)",
            "ALTER TYPE box3 SET (SEND = box3_send, RECEIVE = box3_recv)",
            "ALTER TYPE box3 SET (SEND = NONE)",
            "ALTER TYPE box3 SET (SEND)",
            "ALTER TYPE box3 SET (SEND = NONE, SEND = NONE)",
            "ALTER TYPE box3 SET (STORAGE = plain)",
            "ALTER TYPE box3 SET (STORAGE = extended)",
            "ALTER TYPE box3 SET (STORAGE = 'plain')",
            "ALTER TYPE box3 SET (STORAGE = 1)",
            "ALTER TYPE box3 SET (ANALYZE = public.box3_an, TYPMOD_IN = NONE, TYPMOD_OUT = NONE, "
                    + "SUBSCRIPT = NONE)",
            "ALTER TYPE box3 SET (SEND = 'x')",
            "ALTER TYPE box3 SET (SEND = +)",
            "ALTER TYPE box3 SET (SEND = OPERATOR(pg_catalog.+))",
            "ALTER TYPE box3 SET (SEND = select)",
            "ALTER TYPE box3 SET (SEND = true)",
            "ALTER TYPE box3 SET (SEND = int4[])",
            "ALTER TYPE box3 SET (SEND = SETOF t.a%TYPE)",
            "ALTER TYPE box3 SET (SEND = double precision)",
            "ALTER TYPE s.box3 SET (SEND = box3_send)",
            "ALTER TYPE nosuch SET (foo = bar)",
            "ALTER TYPE nosuch SET (foo)",
            "ALTER TYPE nosuch SET (SEND = 1)",
            "ALTER TYPE nosuch SET (SEND = -1.5)",
            "ALTER TYPE nosuch SET (select = x)"})
    void testBaseTypeDefinitions(String sql) throws Exception {
        // the text is kept as written, with the dialect and without one
        Statement statement = assertSqlCanBeParsedAndDeparsed(sql, false, POSTGRESQL);
        assertThat(statement).isInstanceOfAny(CreateType.class, AlterType.class);
        assertThat(assertSqlCanBeParsedAndDeparsed(sql)).hasToString(sql);

        assertThat(deparse(statement)).isEqualTo(sql);
        assertThat(new TablesNamesFinder<Void>().getTables(statement)).isEmpty();
        assertThat(statement.getFeatures().getCertain()).contains(StmtFeature.MODIFIES_SCHEMA)
                .doesNotContain(StmtFeature.READS_DATA, StmtFeature.RETURNS_RESULT_SET);
        validateNoErrors(sql, 1, DatabaseType.POSTGRESQL);

        Statements script = CCJSqlParserUtil.parseStatements(sql + "; SELECT 1; " + sql,
                POSTGRESQL);
        assertThat(script).hasSize(3);
        assertThat(script.get(0)).hasToString(sql);
        assertThat(script.get(1)).isInstanceOf(Select.class);
        assertThat(script.get(2)).hasToString(sql);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE TYPE b1(x=1) | CREATE TYPE b1 (x = 1)",
            "CREATE TYPE b1 (x = none) | CREATE TYPE b1 (x = NONE)",
            "CREATE TYPE \"B 1\" (\"In Put\" = 1) | CREATE TYPE \"B 1\" (\"In Put\" = 1)",
            "CREATE TYPE b1 (z = - 1, w = + 2.5) | CREATE TYPE b1 (z = -1, w = +2.5)",
            "CREATE TYPE b1 (q = E'a\\nb', r = $$x$$, p = U&'d\\0061t') "
                    + "| CREATE TYPE b1 (q = E'a\\nb', r = $$x$$, p = U&'d\\0061t')",
            "CREATE TYPE b1 (x = setof int, w = varchar(10), q = numeric(10,2), r = \"char\") "
                    + "| CREATE TYPE b1 (x = SETOF int, w = varchar (10), q = numeric (10, 2), r = \"char\")",
            "CREATE TYPE b1 (v = timestamp(3) with time zone, y = character(3), w = \"X\".y) "
                    + "| CREATE TYPE b1 (v = timestamp(3) with time zone, y = character (3), w = \"X\".y)",
            "ALTER TYPE box3 SET (\"send\" = NONE) | ALTER TYPE box3 SET (\"send\" = NONE)",
            "ALTER TYPE box3 SET (SEND = varchar(10)) | ALTER TYPE box3 SET (SEND = varchar (10))"})
    void testNormalizedRendering(String pair) throws Exception {
        String[] parts = pair.split(" \\| ");
        Statement statement = CCJSqlParserUtil.parse(parts[0], POSTGRESQL);
        assertThat(statement).hasToString(parts[1]);
        assertThat(deparse(statement)).isEqualTo(parts[1]);
        assertThat(CCJSqlParserUtil.parse(parts[1], POSTGRESQL)).hasToString(parts[1]);
    }

    @Test
    void testElementsAndArgumentKinds() throws Exception {
        CreateType type = (CreateType) CCJSqlParserUtil.parse(
                "CREATE TYPE app.box3 (INPUT = app.box3_in, PASSEDBYVALUE, internallength = 16, "
                        + "DEFAULT = 'x', PREFERRED = true, SEND = NONE)",
                POSTGRESQL);
        assertThat(type.getName()).isEqualTo("app.box3");
        BaseTypeDefinition definition = (BaseTypeDefinition) type.getDefinition();
        assertThat(definition.getElements()).extracting(DefinitionElement::getName).containsExactly(
                "INPUT", "PASSEDBYVALUE", "internallength", "DEFAULT", "PREFERRED", "SEND");
        assertThat(definition.getElements()).extracting(
                element -> element.getArgument() == null ? null : element.getArgument().getKind())
                .containsExactly(Kind.TYPE, null, Kind.NUMBER, Kind.STRING, Kind.KEYWORD,
                        Kind.NONE);

        DefinitionArgument input = definition.getElement("input").getArgument();
        assertThat(input.getDataType().getDataType()).isEqualTo("app.box3_in");
        assertThat(input.isSetOf()).isFalse();
        assertThat(input.isTypeReference()).isFalse();
        assertThat(definition.getElement("PassedByValue").getArgument()).isNull();
        assertThat(definition.getElement("INTERNALLENGTH").getArgument().getNumber())
                .isEqualTo(new LongValue(16));
        assertThat(definition.getElement("DEFAULT").getArgument().getString().getValue())
                .isEqualTo("x");
        assertThat(definition.getElement("PREFERRED").getArgument().getKeyword())
                .isEqualTo("true");
        assertThat(definition.getElement("OUTPUT")).isNull();

        // other kinds leave the getters of a kind empty
        DefinitionArgument none = definition.getElement("SEND").getArgument();
        assertThat(none.getDataType()).isNull();
        assertThat(none.getKeyword()).isNull();
        assertThat(none.getOperator()).isNull();
        assertThat(none.getNumber()).isNull();
        assertThat(none.getString()).isNull();
    }

    @Test
    void testArgumentForms() throws Exception {
        assertThat(argument("int4[]").getDataType().getArrayData()).hasSize(1);
        assertThat(argument("double precision").getDataType()).hasToString("double precision");
        assertThat(argument("extended").getDataType().getDataType()).isEqualTo("extended");
        assertThat(argument("\"none\"").getKind()).isEqualTo(Kind.TYPE);

        DefinitionArgument reference = argument("setof s.t.a%TYPE");
        assertThat(reference.isSetOf()).isTrue();
        assertThat(reference.isTypeReference()).isTrue();
        assertThat(reference.getDataType().getDataType()).isEqualTo("s.t.a");

        DefinitionArgument operator = argument("OPERATOR(pg_catalog.<=)");
        assertThat(operator.getKind()).isEqualTo(Kind.OPERATOR);
        assertThat(operator.getOperator()).isEqualTo("pg_catalog.<=");
        assertThat(operator.isOperatorKeyword()).isTrue();
        assertThat(argument("<=").isOperatorKeyword()).isFalse();
        assertThat(argument("<=").getOperator()).isEqualTo("<=");

        assertThat(argument("1.5").getNumber()).isInstanceOf(DoubleValue.class);
        SignedExpression negative = (SignedExpression) argument("- 1").getNumber();
        assertThat(negative.getSign()).isEqualTo('-');
        assertThat(negative.getExpression()).isEqualTo(new LongValue(1));

        StringValue escaped = argument("E'a\\nb'").getString();
        assertThat(escaped.getPrefix()).isEqualTo("E");
        assertThat(argument("NULL").getKeyword()).isEqualTo("NULL");
        assertThat(argument("None").getKind()).isEqualTo(Kind.NONE);
        assertThat(argument("None")).hasToString("NONE");
    }

    @Test
    void testAlterTypeProperties() throws Exception {
        AlterType alteration = (AlterType) CCJSqlParserUtil.parse(
                "ALTER TYPE app.box3 SET (SEND = app.box3_send, RECEIVE = NONE, STORAGE = plain, "
                        + "SUBSCRIPT)",
                POSTGRESQL);
        assertThat(alteration.getName()).isEqualTo("app.box3");
        assertThat(alteration.getAction()).isEqualTo(AlterType.Action.SET_PROPERTIES);
        assertThat(alteration.getProperties()).extracting(DefinitionElement::getName)
                .containsExactly("SEND", "RECEIVE", "STORAGE", "SUBSCRIPT");
        assertThat(alteration.getProperties().get(0).getArgument().getDataType().getDataType())
                .isEqualTo("app.box3_send");
        assertThat(alteration.getProperties().get(1).getArgument().getKind())
                .isEqualTo(Kind.NONE);
        assertThat(alteration.getProperties().get(3).getArgument()).isNull();

        // the other actions carry no properties and keep their reading
        AlterType schema = (AlterType) CCJSqlParserUtil.parse("ALTER TYPE box3 SET SCHEMA app");
        assertThat(schema.getAction()).isEqualTo(AlterType.Action.SET_SCHEMA);
        assertThat(schema.getNewName()).isEqualTo("app");
        assertThat(schema.getProperties()).isEmpty();
    }

    @Test
    void testConstruction() throws Exception {
        CreateType type = new CreateType();
        type.setName("box3");
        BaseTypeDefinition definition = new BaseTypeDefinition();
        definition.setElements(Arrays.asList(
                new DefinitionElement("INPUT",
                        DefinitionArgument.type(new ColDataType("box3_in"))),
                new DefinitionElement("ELEMENT",
                        DefinitionArgument.type(new ColDataType("float8"), true, false)),
                new DefinitionElement("INTERNALLENGTH",
                        DefinitionArgument.number(new LongValue(16))),
                new DefinitionElement("DELIMITER", DefinitionArgument.string(new StringValue(";"))),
                new DefinitionElement("COLLATABLE", DefinitionArgument.keyword("false")),
                new DefinitionElement("x", DefinitionArgument.operator("pg_catalog.+", true)),
                new DefinitionElement("y", DefinitionArgument.operator("+", false)),
                new DefinitionElement("SEND", DefinitionArgument.none())));
        DefinitionElement bare = new DefinitionElement();
        bare.setName("PASSEDBYVALUE");
        bare.setArgument(null);
        type.setDefinition(definition);
        String sql = "CREATE TYPE box3 (INPUT = box3_in, ELEMENT = SETOF float8, "
                + "INTERNALLENGTH = 16, DELIMITER = ';', COLLATABLE = false, "
                + "x = OPERATOR(pg_catalog.+), y = +, SEND = NONE)";
        assertThat(type).hasToString(sql);
        assertThat(CCJSqlParserUtil.parse(sql, POSTGRESQL)).hasToString(sql);
        assertThat(bare).hasToString("PASSEDBYVALUE");

        AlterType alteration = new AlterType();
        alteration.setName("box3");
        alteration.setAction(AlterType.Action.SET_PROPERTIES);
        alteration.setProperties(Arrays.asList(bare,
                new DefinitionElement("SEND", DefinitionArgument.none())));
        assertThat(alteration).hasToString("ALTER TYPE box3 SET (PASSEDBYVALUE, SEND = NONE)");
        assertThat(new BaseTypeDefinition(alteration.getProperties()))
                .hasToString("(PASSEDBYVALUE, SEND = NONE)");
    }

    @Test
    void testOtherTypeDefinitionsKeepTheirReading() throws Exception {
        assertThat(((CreateType) assertSqlCanBeParsedAndDeparsed("CREATE TYPE shell_t"))
                .getDefinition()).isNull();
        assertThat(
                ((CreateType) assertSqlCanBeParsedAndDeparsed("CREATE TYPE c AS (a int, b text)"))
                        .getDefinition())
                .isInstanceOf(CompositeTypeDefinition.class);
        assertThat(((CreateType) assertSqlCanBeParsedAndDeparsed("CREATE TYPE c AS ()"))
                .getDefinition()).isInstanceOf(CompositeTypeDefinition.class);
        assertThat(((CreateType) assertSqlCanBeParsedAndDeparsed("CREATE TYPE e AS ENUM ('a')"))
                .getDefinition()).isInstanceOf(EnumTypeDefinition.class);
        assertThat(((CreateType) assertSqlCanBeParsedAndDeparsed(
                "CREATE TYPE r AS RANGE (SUBTYPE = int4)")).getDefinition())
                .isInstanceOf(RangeTypeDefinition.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE TYPE b1 ()",
            "CREATE TYPE b1 (x = 1,)",
            "CREATE TYPE b1 (x := 1)",
            "CREATE TYPE b1 (x 1)",
            "CREATE TYPE b1 (x =)",
            "CREATE TYPE b1 (a.b = 1)",
            "CREATE TYPE b1 ('x' = 1)",
            "CREATE TYPE b1 AS (x = 1)",
            "CREATE TYPE IF NOT EXISTS b1 (x = 1)",
            "CREATE OR REPLACE TYPE b1 (x = 1)",
            "CREATE TYPE b1 (x = 1) WITH (y)",
            "CREATE TYPE b1 (x = 'a' 'b')",
            "CREATE TYPE b1 (x = a b)",
            "CREATE TYPE b1 (x = 1 + 2)",
            "CREATE TYPE b1 (x = (1))",
            "CREATE TYPE b1 (x = f())",
            "CREATE TYPE b1 (x = -x)",
            "CREATE TYPE b1 (x = a.+)",
            "CREATE TYPE b1 (o = B'101')",
            "CREATE TYPE b1 (n = X'1F')",
            "CREATE TYPE b1 (n = N'x')",
            "CREATE TYPE b1 (x = between)",
            "CREATE TYPE b1 (x = values)",
            "CREATE TYPE b1 (x = coalesce)",
            "CREATE TYPE b1 (x = setof)",
            "CREATE TYPE b1 (x = precision)",
            "CREATE TYPE b1 (x = national)",
            "CREATE TYPE b1 (x = a%TYPE)",
            "ALTER TYPE box3 SET ()",
            "ALTER TYPE box3 SET (a.b = x)",
            "ALTER TYPE box3 SET (SEND = box3_send,)",
            "ALTER TYPE box3 SET (SEND = box3_send) CASCADE",
            "ALTER TYPE box3 SET SEND = box3_send",
            "ALTER TYPE IF EXISTS box3 SET (SEND = box3_send)",
            "ALTER TYPE nosuch SET (SEND = between)",
            "ALTER TYPE nosuch SET (SEND = B'1')"})
    void testRejectedForms(String sql) {
        assertThatThrownBy(() -> CCJSqlParserUtil.parse(sql, POSTGRESQL))
                .isInstanceOf(JSQLParserException.class);
        assertThatThrownBy(() -> CCJSqlParserUtil.parse(sql))
                .isInstanceOf(JSQLParserException.class);
    }
}

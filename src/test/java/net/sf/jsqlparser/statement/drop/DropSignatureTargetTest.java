/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.drop;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Collectors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.ASTNodeAccess;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.Node;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.CascadeBehavior;
import net.sf.jsqlparser.statement.CastSignature;
import net.sf.jsqlparser.statement.OperatorSignature;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.TransformSignature;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ExclusionOperator;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** PostgreSQL DROP CAST, DROP OPERATOR and DROP TRANSFORM name their targets by types. */
class DropSignatureTargetTest {

    @ParameterizedTest
    @ValueSource(strings = {"DROP CAST (text AS integer)",
            "DROP CAST IF EXISTS (bigint AS int4) CASCADE",
            "DROP CAST (int[] AS public.mytype) RESTRICT",
            "DROP CAST (double precision AS timestamp with time zone)",
            "DROP OPERATOR === (integer, integer)",
            "DROP OPERATOR IF EXISTS ~ (NONE, bigint) CASCADE",
            "DROP OPERATOR public.=== (integer, integer), ~ (NONE, bigint)",
            "DROP OPERATOR @@ (text, text)", "DROP OPERATOR <-> (point, point) RESTRICT",
            "DROP OPERATOR ! (bigint, NONE)", "DROP OPERATOR \"my schema\".+ (int, int)",
            "DROP OPERATOR - (NONE, integer), || (text, text), ? (jsonb, text)",
            "DROP OPERATOR <<= (inet, inet), -|- (int4range, int4range), ?| (jsonb, text[])",
            "DROP OPERATOR * (int, int), / (int, int), = (int, int), >= (int, int)",
            "DROP OPERATOR @-@ (NONE, lseg), public.#- (jsonb, text[]), <#> (point, point)",
            "DROP TRANSFORM FOR hstore LANGUAGE plpython3u",
            "DROP TRANSFORM IF EXISTS FOR public.hstore LANGUAGE plpython3u RESTRICT",
            "DROP TRANSFORM FOR integer[] LANGUAGE plperl CASCADE",
            "DROP TRANSFORM FOR character varying LANGUAGE \"plpython3u\""})
    void signatureTargetsRoundTrip(String sql) throws JSQLParserException {
        for (Drop drop : roundTrip(sql, Dialect.POSTGRESQL)) {
            assertEquals(sql, drop.toString());
            assertNotEquals(Drop.ObjectType.OTHER, drop.getObjectType());
            // no tokens are left over: CASCADE or RESTRICT is the only legacy parameter
            assertEquals(drop.getBehavior() == null ? null : List.of(drop.getBehavior().name()),
                    drop.getParameters());
            assertTrue(new TablesNamesFinder().getTables(drop).isEmpty());
        }
        assertEquals(sql, CCJSqlParserUtil.parse(sql).toString());
        assertEquals(2, CCJSqlParserUtil.parseStatements(sql + "; SELECT 1",
                parser -> parser.withDialect(Dialect.POSTGRESQL)).size());
    }

    @Test
    void castFields() throws JSQLParserException {
        Drop drop = parse("DROP CAST IF EXISTS (bigint AS int4) CASCADE");
        assertEquals(Drop.ObjectType.CAST, drop.getObjectType());
        assertEquals("CAST", drop.getType());
        assertTrue(drop.isIfExists());
        assertEquals(CascadeBehavior.CASCADE, drop.getBehavior());
        CastSignature cast = drop.getCast();
        assertEquals("bigint", cast.getSourceType().getDataType());
        assertEquals("int4", cast.getTargetType().getDataType());
        assertTrue(drop.getNames().isEmpty());
        assertNull(drop.getName());
        assertTrue(drop.getOperators().isEmpty());
        assertNull(drop.getTransform());

        cast.setTargetType(new ColDataType("integer"));
        assertEquals("DROP CAST IF EXISTS (bigint AS integer) CASCADE", drop.toString());
        assertRendersAlike(drop);

        assertEquals("DROP CAST (text AS integer)",
                new Drop().withObjectType(Drop.ObjectType.CAST)
                        .withCast(new CastSignature(new ColDataType("text"),
                                new ColDataType("integer")))
                        .toString());
    }

    @Test
    void operatorFields() throws JSQLParserException {
        Drop drop =
                parse("DROP OPERATOR IF EXISTS public.=== (integer, integer), ~ (NONE, bigint)");
        assertEquals(Drop.ObjectType.OPERATOR, drop.getObjectType());
        assertEquals(2, drop.getOperators().size());

        OperatorSignature first = drop.getOperators().get(0);
        assertEquals("public", first.getOperator().getSchemaName());
        assertEquals("===", first.getOperator().getName());
        assertFalse(first.getOperator().isUseOperatorKeyword());
        assertEquals("integer", first.getLeftType().getDataType());
        assertEquals("integer", first.getRightType().getDataType());

        OperatorSignature prefix = drop.getOperators().get(1);
        assertNull(prefix.getOperator().getSchemaName());
        assertEquals("~", prefix.getOperator().getName());
        assertNull(prefix.getLeftType(), "NONE is a null operand type");
        assertEquals("bigint", prefix.getRightType().getDataType());

        assertEquals(List.of("public.===", "~"), names(drop));
        assertThrows(UnsupportedOperationException.class,
                () -> drop.getNames().add(new Table("x")));
        assertThrows(UnsupportedOperationException.class,
                () -> drop.getOperators().add(new OperatorSignature()));
        assertNull(drop.getCast());

        prefix.setLeftType(new ColDataType("bigint"));
        first.setRightType(null);
        assertEquals("DROP OPERATOR IF EXISTS public.=== (integer, NONE), ~ (bigint, bigint)",
                drop.toString());
        assertRendersAlike(drop);

        // ## is an operator with the PostgreSQL dialect; without one it is an identifier, as in
        // SQL Server's ##global_temp
        Drop hash = parse("DROP OPERATOR ## (lseg, lseg)");
        assertEquals("##", hash.getOperators().get(0).getOperator().getName());
        assertEquals("DROP OPERATOR ## (lseg, lseg)", hash.toString());

        assertEquals("DROP OPERATOR @@ (NONE, text) CASCADE", new Drop()
                .withObjectType(Drop.ObjectType.OPERATOR)
                .addOperators(new OperatorSignature(new ExclusionOperator().withName("@@"), null,
                        new ColDataType("text")))
                .withBehavior(CascadeBehavior.CASCADE).toString());
    }

    @Test
    void transformFields() throws JSQLParserException {
        Drop drop = parse(
                "DROP TRANSFORM IF EXISTS FOR public.hstore LANGUAGE plpython3u RESTRICT");
        assertEquals(Drop.ObjectType.TRANSFORM, drop.getObjectType());
        TransformSignature transform = drop.getTransform();
        assertEquals("public.hstore", transform.getDataType().getDataType());
        assertEquals("plpython3u", transform.getLanguage());
        assertEquals(CascadeBehavior.RESTRICT, drop.getBehavior());
        assertTrue(drop.getNames().isEmpty());

        transform.setLanguage("plperl");
        assertEquals("DROP TRANSFORM IF EXISTS FOR public.hstore LANGUAGE plperl RESTRICT",
                drop.toString());
        assertRendersAlike(drop);

        assertEquals("DROP TRANSFORM FOR hstore LANGUAGE plperl",
                new Drop().withObjectType(Drop.ObjectType.TRANSFORM)
                        .withTransform(new TransformSignature(new ColDataType("hstore"), "plperl"))
                        .toString());
    }

    @Test
    void signaturesSpanTheirSource() throws JSQLParserException {
        String sql = "DROP CAST IF EXISTS (bigint AS int4) CASCADE";
        assertSpans("(bigint AS int4)", sql, parse(sql).getCast());

        sql = "DROP OPERATOR public.=== (integer, integer), ~ (NONE, bigint) RESTRICT";
        Drop operators = parse(sql);
        assertSpans("public.=== (integer, integer)", sql, operators.getOperators().get(0));
        assertSpans("~ (NONE, bigint)", sql, operators.getOperators().get(1));
        assertSpans("bigint", sql, operators.getOperators().get(1).getRightType());

        sql = "DROP TRANSFORM FOR public.hstore LANGUAGE plpython3u";
        assertSpans("FOR public.hstore LANGUAGE plpython3u", sql, parse(sql).getTransform());
        assertSpans(sql, sql, parse(sql));
    }

    @Test
    void targetKindsReplaceEachOther() throws JSQLParserException {
        Drop drop = parse("DROP OPERATOR === (integer, integer)");
        drop.withObjectType(Drop.ObjectType.CAST)
                .setCast(new CastSignature(new ColDataType("text"), new ColDataType("int")));
        assertTrue(drop.getOperators().isEmpty());
        assertEquals("DROP CAST (text AS int)", drop.toString());

        drop.withObjectType(Drop.ObjectType.TRANSFORM)
                .setTransform(new TransformSignature(new ColDataType("hstore"), "plperl"));
        assertNull(drop.getCast());
        assertEquals("DROP TRANSFORM FOR hstore LANGUAGE plperl", drop.toString());

        drop.withObjectType(Drop.ObjectType.OPERATOR).setOperators(List.of(
                new OperatorSignature(new ExclusionOperator().withName("+"), null,
                        new ColDataType("int"))));
        assertNull(drop.getTransform());
        assertEquals("DROP OPERATOR + (NONE, int)", drop.toString());

        // the legacy name setters switch to plain names
        drop.setName(new Table("op"));
        assertTrue(drop.getOperators().isEmpty());
        assertEquals("DROP OPERATOR op", drop.toString());

        Drop cast = parse("DROP CAST (text AS integer) CASCADE");
        cast.addNames(new Table("c"));
        assertNull(cast.getCast());
        assertEquals("DROP CAST c CASCADE", cast.toString());
    }

    @Test
    void operatorNamesStayNamesOutsidePostgreSql() throws JSQLParserException {
        Drop oracle = (Drop) CCJSqlParserUtil.parse("DROP OPERATOR app.eq_op",
                parser -> parser.withDialect(Dialect.ORACLE));
        assertEquals(Drop.ObjectType.OPERATOR, oracle.getObjectType());
        assertEquals(List.of("app.eq_op"), names(oracle));
        assertTrue(oracle.getOperators().isEmpty());
        assertEquals("DROP OPERATOR app.eq_op", oracle.toString());

        for (String sql : List.of("DROP OPERATOR IF EXISTS eq_op CASCADE", "DROP TRANSFORM t")) {
            assertSqlCanBeParsedAndDeparsed(sql);
            assertThrows(JSQLParserException.class,
                    () -> parse(sql), sql + " needs a signature in PostgreSQL");
        }

        // identifiers that start with an operator character or a quote are names too
        assertEquals(List.of("`eq_op`"),
                names((Drop) assertSqlCanBeParsedAndDeparsed("DROP OPERATOR `eq_op`")));
        Drop temporary = (Drop) CCJSqlParserUtil.parse("DROP OPERATOR #tmp",
                parser -> parser.withDialect(Dialect.SQLSERVER));
        assertEquals(List.of("#tmp"), names(temporary));
        assertTrue(temporary.getOperators().isEmpty());
    }

    @Test
    void schemasNamedLikeTypeWordsQualifyOperators() throws JSQLParserException {
        Drop drop = parse("DROP OPERATOR class.+ (int, int), family.- (NONE, int)");
        assertEquals(Drop.ObjectType.OPERATOR, drop.getObjectType());
        assertEquals(List.of("class.+", "family.-"), names(drop));
        assertEquals(Drop.ObjectType.OPERATOR_CLASS,
                parse("DROP OPERATOR CLASS c USING btree").getObjectType());
    }

    @ParameterizedTest
    @ValueSource(strings = {"DROP CAST text AS integer", "DROP CAST (text)",
            "DROP CAST (text AS integer), (int AS text)", "DROP CAST (text AS integer) foo",
            "DROP CAST IF EXISTS", "DROP CAST (text AS integer) LOCK = NONE",
            "DROP CAST (text AS integer) CASCADE RESTRICT", "DROP OPERATOR === (integer)",
            "DROP OPERATOR === (NONE, NONE)", "DROP OPERATOR ===", "DROP OPERATOR === ()",
            "DROP OPERATOR === (int, int, int)", "DROP OPERATOR foo (int, int)",
            "DROP OPERATOR foo", "DROP OPERATOR === (int, int) foo",
            "DROP OPERATOR = = (int, int)", "DROP OPERATOR === (int, int) ON t",
            "DROP OPERATOR === (int, int),", "DROP OPERATOR === (int, int) USING btree",
            "DROP OPERATOR IF EXISTS", "DROP TRANSFORM FOR hstore",
            "DROP TRANSFORM hstore LANGUAGE plpython3u", "DROP TRANSFORM FOR hstore LANGUAGE",
            "DROP TRANSFORM FOR hstore LANGUAGE a, b",
            "DROP TRANSFORM FOR hstore LANGUAGE plpython3u foo", "DROP TRANSFORM foo"})
    void rejectsMalformedSignatures(String sql) {
        assertThrows(JSQLParserException.class, () -> parse(sql), sql);
    }

    @Test
    void pgDumpCleanScriptParses() throws JSQLParserException {
        String script = "SET statement_timeout = 0;\n"
                + "SELECT pg_catalog.set_config('search_path', '', false);\n"
                + "DROP TRANSFORM IF EXISTS FOR public.hstore LANGUAGE plpython3u;\n"
                + "DROP CAST IF EXISTS (text AS public.mood);\n"
                + "DROP OPERATOR IF EXISTS public.~ (NONE, bigint);\n"
                + "DROP OPERATOR IF EXISTS public.=== (integer, integer);\n"
                + "DROP OPERATOR CLASS IF EXISTS public.myops USING btree;\n"
                + "DROP FUNCTION IF EXISTS public.mood_from_text(text);\n"
                + "DROP TYPE IF EXISTS public.mood;\n"
                + "DROP EXTENSION IF EXISTS hstore;\n"
                + "DROP SCHEMA IF EXISTS public;\n";
        Statements statements = CCJSqlParserUtil.parseStatements(script,
                parser -> parser.withDialect(Dialect.POSTGRESQL));
        assertEquals(11, statements.size());
        assertEquals(List.of(Drop.ObjectType.TRANSFORM, Drop.ObjectType.CAST,
                Drop.ObjectType.OPERATOR, Drop.ObjectType.OPERATOR),
                statements.subList(2, 6).stream().map(statement -> ((Drop) statement)
                        .getObjectType()).collect(Collectors.toList()));
        assertEquals("DROP OPERATOR IF EXISTS public.=== (integer, integer)",
                statements.get(5).toString());
    }

    private static void assertSpans(String expected, String sql, ASTNodeAccess part) {
        Node node = part.getASTNode();
        assertEquals(expected, sql.substring(node.jjtGetFirstToken().absoluteBegin - 1,
                node.jjtGetLastToken().absoluteEnd - 1));
    }

    private static List<String> names(Drop drop) {
        return drop.getNames().stream().map(Table::getFullyQualifiedName)
                .collect(Collectors.toList());
    }

    private static void assertRendersAlike(Drop drop) {
        StringBuilder builder = new StringBuilder();
        drop.accept(new StatementDeParser(builder), null);
        assertEquals(drop.toString(), builder.toString());
    }

    private static Drop parse(String sql) throws JSQLParserException {
        return (Drop) CCJSqlParserUtil.parse(sql,
                parser -> parser.withDialect(Dialect.POSTGRESQL));
    }

    private static List<Drop> roundTrip(String sql, Dialect dialect) throws JSQLParserException {
        Drop drop = (Drop) assertSqlCanBeParsedAndDeparsed(sql, true,
                parser -> parser.withDialect(dialect));
        assertRendersAlike(drop);
        return List.of(drop, (Drop) CCJSqlParserUtil.parse(drop.toString(),
                parser -> parser.withDialect(dialect)));
    }
}

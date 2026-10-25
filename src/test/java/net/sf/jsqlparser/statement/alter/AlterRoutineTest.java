/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static net.sf.jsqlparser.util.validation.ValidationTestAsserts.validateNoErrors;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.ConfigurationParameterChange;
import net.sf.jsqlparser.statement.RoutineCharacteristic;
import net.sf.jsqlparser.statement.RoutineCharacteristic.Kind;
import net.sf.jsqlparser.statement.RoutineCharacteristic.SecurityClause;
import net.sf.jsqlparser.statement.RoutineReference;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.statement.create.function.CreateFunction;
import net.sf.jsqlparser.statement.create.procedure.CreateProcedure;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class AlterRoutineTest {

    @ParameterizedTest
    @ValueSource(strings = {"ALTER FUNCTION f2(int) OWNER TO u1",
            "ALTER FUNCTION public.f2(integer, text) OWNER TO CURRENT_ROLE",
            "ALTER FUNCTION f2 OWNER TO SESSION_USER", "ALTER FUNCTION f2(int) RENAME TO g2",
            "ALTER PROCEDURE p1() SET SCHEMA archive",
            "ALTER ROUTINE \"My Func\"(IN a int, VARIADIC b text[]) RENAME TO \"Other\"",
            "ALTER FUNCTION f2(int) DEPENDS ON EXTENSION ext",
            "ALTER ROUTINE f2(int) NO DEPENDS ON EXTENSION ext",
            "ALTER FUNCTION f2(int) SET search_path = public",
            "ALTER FUNCTION f2(int) SET search_path TO app, public SET work_mem = '64MB'",
            "ALTER FUNCTION f2(int) SET work_mem TO DEFAULT RESET search_path RESET ALL",
            "ALTER PROCEDURE p1(int) SET search_path FROM CURRENT",
            "ALTER PROCEDURE p1(int) SECURITY DEFINER",
            "ALTER PROCEDURE p1 EXTERNAL SECURITY INVOKER",
            "ALTER ROUTINE f2(int) IMMUTABLE", "ALTER FUNCTION f2(int) STABLE NOT LEAKPROOF",
            "ALTER FUNCTION f2(int) VOLATILE LEAKPROOF STRICT RESTRICT",
            "ALTER FUNCTION f2(int) CALLED ON NULL INPUT",
            "ALTER FUNCTION f2(int) RETURNS NULL ON NULL INPUT",
            "ALTER FUNCTION f2(int) PARALLEL SAFE PARALLEL RESTRICTED PARALLEL UNSAFE",
            "ALTER FUNCTION f2(int) COST 100 ROWS 2.5 SUPPORT pg_catalog.my_support",
            "ALTER FUNCTION f2(int) COST -1 ROWS +10"})
    void testPostgreSqlForms(String sql) throws JSQLParserException {
        assertRoundTrip(sql, Dialect.POSTGRESQL);
        assertRoundTrip(sql, null);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALTER PROCEDURE p1 COMMENT 'x' SQL SECURITY INVOKER",
            "ALTER FUNCTION f1 READS SQL DATA", "ALTER FUNCTION db1.f1 LANGUAGE SQL CONTAINS SQL",
            "ALTER PROCEDURE `p 1` NO SQL MODIFIES SQL DATA SQL SECURITY DEFINER",
            "ALTER FUNCTION f1 COMMENT 'it''s'"})
    void testMySqlForms(String sql) throws JSQLParserException {
        assertRoundTrip(sql, Dialect.MYSQL);
        assertRoundTrip(sql, Dialect.MARIADB);
        assertRoundTrip(sql, null);
    }

    @Test
    void testPostgreSqlActions() throws JSQLParserException {
        AlterRoutine owner = parsePostgreSql("ALTER FUNCTION s.f2(IN a int, text) OWNER TO u1");
        assertEquals(AlterRoutine.RoutineType.FUNCTION, owner.getRoutineType());
        assertEquals(AlterRoutine.Action.OWNER, owner.getAction());
        assertEquals("u1", owner.getOwner());
        RoutineReference routine = owner.getRoutine();
        assertEquals("s.f2", routine.getName());
        assertEquals(2, routine.getArguments().size());
        assertEquals(RoutineReference.Argument.Mode.IN, routine.getArguments().get(0).getMode());
        assertEquals("a", routine.getArguments().get(0).getName());
        assertEquals("text", routine.getArguments().get(1).getDataType().toString());
        assertTrue(owner.getCharacteristics().isEmpty());

        AlterRoutine rename = parsePostgreSql("ALTER ROUTINE f2 RENAME TO g2");
        assertEquals(AlterRoutine.RoutineType.ROUTINE, rename.getRoutineType());
        assertEquals(AlterRoutine.Action.RENAME, rename.getAction());
        assertEquals("g2", rename.getNewName());
        assertNull(rename.getRoutine().getArguments());

        AlterRoutine schema = parsePostgreSql("ALTER PROCEDURE p1() SET SCHEMA archive");
        assertEquals(AlterRoutine.RoutineType.PROCEDURE, schema.getRoutineType());
        assertEquals(AlterRoutine.Action.SET_SCHEMA, schema.getAction());
        assertEquals("archive", schema.getSchemaName());
        assertTrue(schema.getRoutine().getArguments().isEmpty());

        AlterRoutine dependency =
                parsePostgreSql("ALTER FUNCTION f2(int) NO DEPENDS ON EXTENSION ext");
        assertEquals(AlterRoutine.Action.DEPENDS_ON_EXTENSION, dependency.getAction());
        assertEquals("ext", dependency.getExtensionName());
        assertTrue(dependency.isNoDependency());

        // A configuration parameter named "schema" is a SET, not SET SCHEMA.
        AlterRoutine parameter = parsePostgreSql("ALTER FUNCTION f2(int) SET schema = 'x'");
        assertEquals(AlterRoutine.Action.CHARACTERISTICS, parameter.getAction());
        assertEquals("schema", parameter.getCharacteristics().get(0).getParameterChange()
                .getParameter());
    }

    @Test
    void testPostgreSqlCharacteristics() throws JSQLParserException {
        AlterRoutine statement = parsePostgreSql("ALTER FUNCTION f2(int) IMMUTABLE STRICT "
                + "EXTERNAL SECURITY DEFINER PARALLEL SAFE COST 10 ROWS 5 SUPPORT s.sup "
                + "SET search_path TO app, public RESET ALL RESTRICT");
        assertEquals(AlterRoutine.Action.CHARACTERISTICS, statement.getAction());
        assertTrue(statement.isRestrict());
        List<RoutineCharacteristic> characteristics = statement.getCharacteristics();
        assertEquals(List.of(Kind.IMMUTABLE, Kind.STRICT, Kind.SECURITY_DEFINER,
                Kind.PARALLEL_SAFE, Kind.COST, Kind.ROWS, Kind.SUPPORT,
                Kind.CONFIGURATION_PARAMETER, Kind.CONFIGURATION_PARAMETER),
                characteristics.stream().map(RoutineCharacteristic::getKind)
                        .collect(java.util.stream.Collectors.toList()));
        assertNull(characteristics.get(0).getSecurityClause());
        assertEquals(SecurityClause.EXTERNAL_SECURITY,
                characteristics.get(2).getSecurityClause());
        assertEquals("10", statement.getCharacteristic(Kind.COST).orElseThrow().getValue());
        assertEquals("5", statement.getCharacteristic(Kind.ROWS).orElseThrow().getValue());
        assertEquals("s.sup", statement.getCharacteristic(Kind.SUPPORT).orElseThrow().getValue());

        ConfigurationParameterChange set = characteristics.get(7).getParameterChange();
        assertEquals(ConfigurationParameterChange.Action.SET, set.getAction());
        assertEquals("search_path", set.getParameter());
        assertEquals(2, set.getValues().size());
        ConfigurationParameterChange reset = characteristics.get(8).getParameterChange();
        assertEquals(ConfigurationParameterChange.Action.RESET, reset.getAction());
        assertEquals("ALL", reset.getParameter());

        AlterRoutine plainSecurity = parsePostgreSql("ALTER PROCEDURE p1(int) SECURITY INVOKER");
        RoutineCharacteristic security = plainSecurity.getCharacteristics().get(0);
        assertEquals(Kind.SECURITY_INVOKER, security.getKind());
        assertEquals(SecurityClause.SECURITY, security.getSecurityClause());
        assertFalse(plainSecurity.isRestrict());
    }

    @Test
    void testMySqlCharacteristics() throws JSQLParserException {
        AlterRoutine statement = (AlterRoutine) CCJSqlParserUtil.parse(
                "ALTER PROCEDURE db1.p1 COMMENT 'x' LANGUAGE SQL READS SQL DATA SQL SECURITY INVOKER",
                parser -> parser.withDialect(Dialect.MYSQL));
        assertEquals(AlterRoutine.RoutineType.PROCEDURE, statement.getRoutineType());
        assertEquals("db1.p1", statement.getRoutine().getName());
        assertNull(statement.getRoutine().getArguments());
        List<RoutineCharacteristic> characteristics = statement.getCharacteristics();
        assertEquals(4, characteristics.size());
        assertEquals(Kind.COMMENT, characteristics.get(0).getKind());
        assertEquals("'x'", characteristics.get(0).getValue());
        assertEquals(Kind.LANGUAGE_SQL, characteristics.get(1).getKind());
        assertEquals(Kind.READS_SQL_DATA, characteristics.get(2).getKind());
        assertEquals(Kind.SECURITY_INVOKER, characteristics.get(3).getKind());
        assertEquals(SecurityClause.SQL_SECURITY, characteristics.get(3).getSecurityClause());
        assertFalse(statement.isRestrict());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', quoteCharacter = '`', value = {
            "'it\\'s'|it's", "'it''s'|it's", "\"dq\"|dq", "'a\\\\b'|a\\b",
            "'line1\\nline2'|`line1\nline2`", "'tab\\there'|`tab\there`",
            "'percent\\% underscore\\_'|percent\\% underscore\\_",
            "'한글 설명 😀'|한글 설명 😀"})
    void testMySqlCommentKeepsLiteralAndStoredText(String literal, String stored)
            throws JSQLParserException {
        String sql = "ALTER PROCEDURE p1 COMMENT " + literal + " SQL SECURITY INVOKER";
        AlterRoutine statement = (AlterRoutine) assertSqlCanBeParsedAndDeparsed(sql, false,
                parser -> parser.withDialect(Dialect.MYSQL));
        RoutineCharacteristic comment = statement.getCharacteristic(Kind.COMMENT).orElseThrow();
        assertEquals(literal, comment.getValue());
        assertEquals(literal, comment.getComment().toString());
        assertEquals(stored, comment.getComment().getNotExcapedValue());
    }

    @Test
    void testCommentMutation() throws JSQLParserException {
        AlterRoutine statement = (AlterRoutine) CCJSqlParserUtil.parse(
                "ALTER FUNCTION f1 COMMENT 'it\\'s' NO SQL",
                parser -> parser.withDialect(Dialect.MYSQL));
        RoutineCharacteristic comment = statement.getCharacteristics().get(0);
        assertEquals(StringValue.Escaping.MYSQL_BACKSLASH, comment.getComment().getEscaping());

        comment.getComment().setValue("changed");
        assertEquals("ALTER FUNCTION f1 COMMENT 'changed' NO SQL", statement.toString());
        comment.setValue("'x''y'");
        assertEquals("x'y", comment.getComment().getNotExcapedValue());
        comment.setComment(new StringValue("plain"));
        assertEquals("'plain'", comment.getValue());

        RoutineCharacteristic created = RoutineCharacteristic.comment(new StringValue("note"));
        assertEquals(Kind.COMMENT, created.getKind());
        assertEquals("COMMENT 'note'", created.toString());
        assertEquals("COMMENT 'v'", new RoutineCharacteristic(Kind.COMMENT, "'v'").toString());
        assertNull(new RoutineCharacteristic(Kind.COST, "5").getComment());
    }

    @Test
    void testMutationAndActionSwitch() throws JSQLParserException {
        AlterRoutine statement = parsePostgreSql("ALTER FUNCTION f2(int) RENAME TO g2");
        statement.setAction(AlterRoutine.Action.OWNER);
        statement.setOwner("CURRENT_USER");
        assertEquals("ALTER FUNCTION f2(int) OWNER TO CURRENT_USER", statement.toString());
        statement.setAction(AlterRoutine.Action.RENAME);
        assertEquals("ALTER FUNCTION f2(int) RENAME TO g2", statement.toString());

        statement.setAction(AlterRoutine.Action.CHARACTERISTICS);
        statement.setRoutineType(AlterRoutine.RoutineType.ROUTINE);
        statement.getCharacteristics().add(new RoutineCharacteristic(Kind.SECURITY_DEFINER));
        statement.getCharacteristics().add(new RoutineCharacteristic(Kind.COST, "5"));
        statement.getCharacteristics().add(new RoutineCharacteristic(
                new ConfigurationParameterChange(ConfigurationParameterChange.Action.SET, "a")
                        .withValues(new ExpressionList<>(new StringValue("b")))));
        statement.setRestrict(true);
        assertEquals("ALTER ROUTINE f2(int) SECURITY DEFINER COST 5 SET a TO 'b' RESTRICT",
                statement.toString());

        RoutineCharacteristic security = statement.getCharacteristics().get(0);
        security.setSecurityClause(SecurityClause.SQL_SECURITY);
        security.setKind(Kind.SECURITY_INVOKER);
        assertEquals("SQL SECURITY INVOKER", security.toString());

        RoutineCharacteristic missing = new RoutineCharacteristic(Kind.CONFIGURATION_PARAMETER);
        assertThrows(IllegalStateException.class, missing::toString);
    }

    @Test
    void testVisitorsAndDeparserUseExpressions() throws JSQLParserException {
        AlterRoutine statement =
                parsePostgreSql("ALTER FUNCTION f2(int) SET search_path TO app, public");
        assertEquals("context:f2", statement.accept(new StatementVisitorAdapter<String>() {
            @Override
            public <S> String visit(AlterRoutine routine, S context) {
                return context + ":" + routine.getRoutine().getName();
            }
        }, "context"));
        assertTrue(new TablesNamesFinder().getTables((Statement) statement).isEmpty());

        StringBuilder deparsed = new StringBuilder();
        new StatementDeParser(deparsed).visit(statement, null);
        assertEquals("ALTER FUNCTION f2(int) SET search_path TO app, public",
                deparsed.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALTER FUNCTION f", "ALTER PROCEDURE p1",
            "ALTER FUNCTION f COMPILE", "ALTER FUNCTION f(int) NOT FENCED",
            "ALTER FUNCTION f(int) EXTERNAL NAME 'lib'", "ALTER FUNCTION f(int) SET SECURE",
            "ALTER FUNCTION IF EXISTS f(int) RENAME TO g", "ALTER PROCEDURE dbo.p AS SELECT 1",
            "ALTER FUNCTION dbo.f(@a int) RETURNS int AS BEGIN RETURN 1 END",
            "ALTER FUNCTION f(int) PARALLEL FAST", "ALTER FUNCTION f(int) SECURITY OWNER"})
    void testOtherFormsRemainUnsupported(String sql) throws JSQLParserException {
        assertInstanceOf(UnsupportedStatement.class, CCJSqlParserUtil.parse(sql));
        assertInstanceOf(UnsupportedStatement.class, CCJSqlParserUtil.parse(sql,
                parser -> parser.withDialect(Dialect.POSTGRESQL)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALTER FUNCTION f(int) IMMUTABLE, STRICT",
            "ALTER FUNCTION f(int) RENAME TO g RESTRICT",
            "ALTER FUNCTION f(int) OWNER TO u IMMUTABLE",
            "ALTER FUNCTION f COMMENT 'x' IMMUTABLE", "ALTER FUNCTION f IMMUTABLE COMMENT 'x'",
            "ALTER FUNCTION f(int) COMMENT 'x'", "ALTER ROUTINE r COMMENT 'x'",
            "ALTER FUNCTION f COMMENT 'x' RESTRICT"})
    void testRejectInvalidCombinations(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql));
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql,
                parser -> parser.withDialect(Dialect.MYSQL)));
    }

    @Test
    void testSqlServerRoutinesAreUnchanged() throws JSQLParserException {
        Consumer<CCJSqlParser> sqlServer = parser -> parser.withDialect(Dialect.SQLSERVER);
        assertInstanceOf(CreateFunction.class, CCJSqlParserUtil.parse(
                "ALTER FUNCTION dbo.f(@a int) RETURNS int AS BEGIN RETURN @a END", sqlServer));
        assertInstanceOf(CreateProcedure.class,
                CCJSqlParserUtil.parse("ALTER PROCEDURE dbo.p AS SELECT 1", sqlServer));
    }

    @Test
    void testValidation() {
        validateNoErrors("ALTER FUNCTION f2(int) OWNER TO u1", 1, DatabaseType.POSTGRESQL);
        validateNoErrors("ALTER PROCEDURE p1(int) SET search_path = public", 1,
                DatabaseType.POSTGRESQL);
        validateNoErrors("ALTER ROUTINE f2(int) IMMUTABLE", 1, DatabaseType.POSTGRESQL);
        validateNoErrors("ALTER FUNCTION f1 READS SQL DATA", 1, DatabaseType.MYSQL,
                DatabaseType.MARIADB);
        validateNoErrors("ALTER PROCEDURE p1 COMMENT 'x'", 1, DatabaseType.MYSQL,
                DatabaseType.MARIADB);
        assertFalse(new Validation(List.of(DatabaseType.MYSQL), "ALTER ROUTINE f2(int) STABLE")
                .validate().isEmpty());
        assertFalse(new Validation(List.of(FeaturesAllowed.SELECT),
                "ALTER FUNCTION f2(int) STABLE").validate().isEmpty());
        assertTrue(new Validation(List.of(new FeaturesAllowed().add(Feature.alterProcedure)),
                "ALTER PROCEDURE p1(int) RESET ALL").validate().isEmpty());
    }

    /** Parses, deparses and reparses with the dialect, or without one if it is null. */
    private static void assertRoundTrip(String sql, Dialect dialect) throws JSQLParserException {
        Consumer<CCJSqlParser> consumer = dialect == null ? parser -> {
        } : parser -> parser.withDialect(dialect);
        Statement statement = assertSqlCanBeParsedAndDeparsed(sql, false, consumer);
        assertInstanceOf(AlterRoutine.class, statement);
        StringBuilder deparsed = new StringBuilder();
        statement.accept(new StatementDeParser(deparsed));
        assertEquals(sql, deparsed.toString());
        assertEquals(sql, CCJSqlParserUtil.parse(deparsed.toString(), consumer).toString());
        assertTrue(new TablesNamesFinder().getTables(statement).isEmpty());
        assertEquals(2, CCJSqlParserUtil.parseStatements(sql + ";\nSELECT 1", consumer).size());
    }

    private static AlterRoutine parsePostgreSql(String sql) throws JSQLParserException {
        return (AlterRoutine) CCJSqlParserUtil.parse(sql,
                parser -> parser.withDialect(Dialect.POSTGRESQL));
    }
}

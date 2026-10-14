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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.CascadeBehavior;
import net.sf.jsqlparser.statement.RoutineReference;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DropRoutineSignatureTest {

    @ParameterizedTest
    @ValueSource(strings = {"DROP PROCEDURE p1(int)", "DROP PROCEDURE p1()",
            "DROP FUNCTION IF EXISTS f2(int), g2(text) CASCADE",
            "DROP FUNCTION f2(int), g2(text)", "DROP FUNCTION a, b(int)",
            "DROP ROUTINE f4", "DROP ROUTINE IF EXISTS s.f4(IN a int, OUT b text) RESTRICT",
            "DROP FUNCTION f(VARIADIC a int[], double precision)",
            "DROP AGGREGATE agg(int)", "DROP AGGREGATE agg(*)",
            "DROP AGGREGATE IF EXISTS agg(int ORDER BY text), agg2(*) CASCADE",
            "DROP FUNCTION IF EXISTS \"fin\".\"restore_fund_data\"(\"in_fund_id\" int8)"})
    void postgreSqlSignaturesSurviveRoundTrips(String sql) throws JSQLParserException {
        for (Drop drop : roundTrip(sql, Dialect.POSTGRESQL)) {
            assertEquals(sql, drop.toString());
            assertFalse(drop.getRoutines().isEmpty());
            assertTrue(drop.getObjectType().hasRoutineTargets());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"DROP PROCEDURE IF EXISTS p1", "DROP FUNCTION IF EXISTS db1.f1",
            "DROP PROCEDURE `db.x`.`p.y`"})
    void mySqlRoutinesWithoutSignatures(String sql) throws JSQLParserException {
        for (Drop drop : roundTrip(sql, Dialect.MYSQL)) {
            assertEquals(sql, drop.toString());
            assertNull(drop.getRoutines().get(0).getArguments());
        }
    }

    @Test
    void procedureSignatureIsKeptInTheAst() throws JSQLParserException {
        Drop drop = parse("DROP PROCEDURE p1(int)");
        RoutineReference procedure = drop.getRoutines().get(0);
        assertEquals("p1", procedure.getName());
        assertEquals(1, procedure.getArguments().size());
        assertEquals("int", procedure.getArguments().get(0).getDataType().toString());
        assertEquals(Drop.ObjectType.PROCEDURE, drop.getObjectType());
    }

    @Test
    void everyTargetKeepsItsOwnSignature() throws JSQLParserException {
        Drop drop = parse("DROP FUNCTION IF EXISTS f2(int), g2(text), h CASCADE");
        assertEquals(List.of("f2(int)", "g2(text)", "h"), drop.getRoutines().stream()
                .map(Object::toString).collect(Collectors.toList()));
        assertNull(drop.getRoutines().get(2).getArguments());
        assertTrue(drop.isIfExists());
        assertEquals(CascadeBehavior.CASCADE, drop.getBehavior());

        RoutineReference.Argument argument = drop.getRoutines().get(1).getArguments().get(0);
        argument.setName("label");
        argument.setDataType(new ColDataType("varchar"));
        drop.getRoutines().get(0).setName("app.f2");
        assertEquals("DROP FUNCTION IF EXISTS app.f2(int), g2(label varchar), h CASCADE",
                drop.toString());
        assertRendersAlike(drop);
    }

    @Test
    void aggregateSignatureKeepsStarAndOrderBy() throws JSQLParserException {
        Drop drop = parse("DROP AGGREGATE agg(int ORDER BY text), agg2(*)");
        assertEquals(Drop.ObjectType.AGGREGATE, drop.getObjectType());
        assertEquals("text", drop.getRoutines().get(0).getOrderByArguments().get(0)
                .getDataType().toString());
        assertTrue(drop.getRoutines().get(1).isAllArguments());
    }

    @Test
    void legacyNameAndParameterAccessorsAreDerived() throws JSQLParserException {
        Drop function =
                parse("DROP FUNCTION IF EXISTS \"fin\".\"restore_fund_data\"(in_fund_id int8)");
        Table name = function.getName();
        assertEquals("\"fin\"", name.getSchemaName());
        assertEquals("\"restore_fund_data\"", name.getName());
        assertEquals(List.of("in_fund_id int8"), function.getParamsByType("FUNCTION"));
        assertEquals(Map.of("FUNCTION", List.of("in_fund_id int8")),
                function.getTypeToParameters());
        assertThrows(UnsupportedOperationException.class,
                () -> function.getTypeToParameters().put("FUNCTION", List.of()));
        assertThrows(UnsupportedOperationException.class,
                () -> function.getNames().add(new Table("x")));

        // The legacy model holds one signature, written after the last name.
        Drop several = parse("DROP FUNCTION a, b(int)");
        assertEquals(List.of("a", "b"), names(several));
        assertEquals(List.of("int"), several.getParamsByType("FUNCTION"));

        assertEquals(List.of("int"), parse("DROP PROCEDURE p1(int)").getParamsByType("PROCEDURE"));
        assertEquals(List.of(), parse("DROP FUNCTION f()").getParamsByType("FUNCTION"));
        Drop omitted = parse("DROP FUNCTION f");
        assertTrue(omitted.getTypeToParameters().containsKey("FUNCTION"));
        assertNull(omitted.getParamsByType("FUNCTION"));
        assertEquals(List.of("int"), parse("drop function f(int)").getParamsByType("FUNCTION"));
        assertEquals(List.of("*"), parse("DROP AGGREGATE agg(*)").getParamsByType("AGGREGATE"));
    }

    @Test
    void legacySettersSwitchToTheLegacyRepresentation() throws JSQLParserException {
        Drop drop = parse("DROP FUNCTION f(int) CASCADE");
        drop.setName(new Table("g"));
        assertTrue(drop.getRoutines().isEmpty());
        assertEquals("DROP FUNCTION g(int) CASCADE", drop.toString());
        drop.getTypeToParameters().put("FUNCTION", List.of("bigint"));
        assertEquals("DROP FUNCTION g(bigint) CASCADE", drop.toString());

        Drop renamed = parse("DROP FUNCTION f(int)");
        renamed.setNames(List.of(new Table("app", "f"), new Table("app", "g")));
        assertEquals("DROP FUNCTION app.f, app.g(int)", renamed.toString());

        Drop replaced = parse("DROP FUNCTION f(int)");
        replaced.setTypeToParameters(new HashMap<>(Map.of("FUNCTION", List.of("bigint"))));
        assertEquals(List.of("f"), names(replaced));
        assertEquals("DROP FUNCTION f(bigint)", replaced.toString());

        replaced.setRoutines(List.of(routine("f2", "text")));
        assertEquals("DROP FUNCTION f2(text)", replaced.toString());
        assertEquals(Map.of("FUNCTION", List.of("text")), replaced.getTypeToParameters());
    }

    @Test
    void buildersRenderBothRepresentations() {
        Drop legacy = new Drop().withType("FUNCTION").withName(new Table("myFunc"));
        legacy.getTypeToParameters().put("FUNCTION", List.of("integer", "name varchar(255)"));
        assertEquals("DROP FUNCTION myFunc(integer, name varchar(255))", legacy.toString());
        assertTrue(legacy.getRoutines().isEmpty());

        Drop typed = new Drop().withObjectType(Drop.ObjectType.ROUTINE).withIfExists(true)
                .addRoutines(routine("r1", "int"), routine("r2"))
                .withBehavior(CascadeBehavior.RESTRICT);
        assertEquals("DROP ROUTINE IF EXISTS r1(int), r2() RESTRICT", typed.toString());
        assertRendersAlike(typed);

        Drop switched = new Drop().withType("FUNCTION").withName(new Table("old"))
                .withRoutines(List.of(routine("f", "int")));
        assertEquals(List.of("f"), names(switched));
        assertEquals("DROP FUNCTION f(int)", switched.toString());
        switched.addRoutines();
        assertEquals("DROP FUNCTION f(int)", switched.toString());
    }

    @Test
    void routineNamesAreNotReportedAsTables() throws JSQLParserException {
        for (String sql : List.of("DROP FUNCTION f(int)", "DROP PROCEDURE app.p(text)",
                "DROP AGGREGATE agg(*)")) {
            assertTrue(new TablesNamesFinder().getTables(parse(sql)).isEmpty());
        }
    }

    @Test
    void rejectsSignaturesOutsideTheirPlace() {
        for (String sql : List.of("DROP AGGREGATE agg", "DROP FUNCTION f(*)",
                "DROP FUNCTION f(int ORDER BY int)", "DROP TABLE t(int)", "DROP INDEX i(a)",
                "DROP FUNCTION f(int) CASCADE RESTRICT", "DROP FUNCTION f(int), ")) {
            assertThrows(JSQLParserException.class, () -> parse(sql), sql);
        }
    }

    @Test
    void scriptsKeepFollowingStatements() throws JSQLParserException {
        assertEquals(3, CCJSqlParserUtil.parseStatements(
                "DROP PROCEDURE p1(int); DROP FUNCTION f(int), g(text) CASCADE; SELECT 1",
                parser -> parser.withDialect(Dialect.POSTGRESQL)).size());
        assertSqlCanBeParsedAndDeparsed("DROP FUNCTION myFunc(amount integer, name varchar(255))",
                true, parser -> parser.withDialect(Dialect.POSTGRESQL));
    }

    private static RoutineReference routine(String name, String... types) {
        RoutineReference routine = new RoutineReference();
        routine.setName(name);
        List<RoutineReference.Argument> arguments = new ArrayList<>();
        for (String type : types) {
            RoutineReference.Argument argument = new RoutineReference.Argument();
            argument.setDataType(new ColDataType(type));
            arguments.add(argument);
        }
        routine.setArguments(arguments);
        return routine;
    }

    private static List<String> names(Drop drop) {
        return drop.getNames().stream().map(Table::getFullyQualifiedName)
                .collect(Collectors.toList());
    }

    private static Drop parse(String sql) throws JSQLParserException {
        return parse(sql, Dialect.POSTGRESQL);
    }

    private static Drop parse(String sql, Dialect dialect) throws JSQLParserException {
        return (Drop) CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(dialect));
    }

    private static void assertRendersAlike(Drop drop) {
        StringBuilder builder = new StringBuilder();
        drop.accept(new StatementDeParser(builder), null);
        assertEquals(drop.toString(), builder.toString());
    }

    private static List<Drop> roundTrip(String sql, Dialect dialect) throws JSQLParserException {
        Drop drop = (Drop) assertSqlCanBeParsedAndDeparsed(sql, true,
                parser -> parser.withDialect(dialect));
        assertRendersAlike(drop);
        return List.of(drop, parse(drop.toString(), dialect));
    }
}

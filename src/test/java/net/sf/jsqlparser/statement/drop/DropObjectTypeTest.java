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
import java.util.Set;
import java.util.stream.Collectors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.CascadeBehavior;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DropObjectTypeTest {

    @ParameterizedTest
    @ValueSource(strings = {"DROP TRIGGER IF EXISTS trg ON users CASCADE",
            "DROP TRIGGER trg ON app.users", "DROP RULE r1 ON v",
            "DROP RULE IF EXISTS r1 ON v RESTRICT"})
    void triggerAndRuleOwnersAreTables(String sql) throws JSQLParserException {
        for (Drop drop : roundTrip(sql, Dialect.POSTGRESQL)) {
            assertEquals(sql, drop.toString());
            String owner = sql.replaceAll(".* ON (\\S+).*", "$1");
            assertEquals(owner, drop.getTable().getFullyQualifiedName());
            assertEquals(Set.of(owner), new TablesNamesFinder().getTables(drop));
        }
    }

    @Test
    void ownerTableReplacesLegacyOnTokens() throws JSQLParserException {
        Drop drop = parse("DROP TRIGGER IF EXISTS trg ON users CASCADE", Dialect.POSTGRESQL);
        assertEquals("trg", drop.getName().getName());
        assertEquals(CascadeBehavior.CASCADE, drop.getBehavior());
        assertEquals(List.of("ON", "users", "CASCADE"), drop.getParameters());

        drop.getTable().setName("accounts");
        drop.setBehavior(CascadeBehavior.RESTRICT);
        assertEquals("DROP TRIGGER IF EXISTS trg ON accounts RESTRICT", drop.toString());
        assertEquals(List.of("ON", "accounts", "RESTRICT"), drop.getParameters());

        StringBuilder builder = new StringBuilder();
        SelectDeParser tables = new SelectDeParser() {
            @Override
            public <S> StringBuilder visit(Table table, S context) {
                return getBuilder().append("renamed");
            }
        };
        drop.accept(new StatementDeParser(new ExpressionDeParser(), tables, builder), null);
        assertEquals("DROP TRIGGER IF EXISTS trg ON renamed RESTRICT", builder.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"DROP TRIGGER safety ON DATABASE", "DROP TRIGGER safety ON ALL SERVER"})
    void sqlServerTriggerScopesAreNotTables(String sql) throws JSQLParserException {
        Drop drop = parse(sql, Dialect.SQLSERVER);
        assertNull(drop.getTable());
        assertTrue(new TablesNamesFinder().getTables(drop).isEmpty());
        assertEquals(sql, drop.toString());
    }

    @Test
    void cascadeAndRestrictAreTyped() throws JSQLParserException {
        Drop table = parse("DROP TABLE IF EXISTS t1, t2 RESTRICT", Dialect.MYSQL);
        assertEquals(CascadeBehavior.RESTRICT, table.getBehavior());
        assertEquals(List.of("RESTRICT"), table.getParameters());

        Drop index = parse("DROP INDEX a_idx, b_idx CASCADE", Dialect.POSTGRESQL);
        assertEquals(CascadeBehavior.CASCADE, index.getBehavior());
        index.setBehavior(null);
        assertEquals("DROP INDEX a_idx, b_idx", index.toString());
        assertNull(index.getParameters());

        Drop plain = parse("DROP SCHEMA s", Dialect.POSTGRESQL);
        assertNull(plain.getBehavior());
        assertNull(plain.getParameters());
    }

    @Test
    void rawParametersAndBehaviorFollowExplicitRules() throws JSQLParserException {
        Drop drop = parse("DROP INDEX i ON t CASCADE", Dialect.MYSQL);
        drop.addParameters("LOCK", "NONE");
        assertEquals(List.of("ON", "t", "LOCK", "NONE", "CASCADE"), drop.getParameters());
        assertEquals("DROP INDEX i ON t LOCK NONE CASCADE", drop.toString());

        drop.setParameters(List.of("RESTRICT"));
        assertNull(drop.getBehavior());
        assertNull(drop.getTable());
        assertEquals("DROP INDEX i RESTRICT", drop.toString());

        // Raw tokens are not parsed: the builder spelling renders the same SQL.
        Drop legacy = new Drop().withType("VIEW").withName(new Table("v")).addParameters("CASCADE");
        assertNull(legacy.getBehavior());
        assertEquals("DROP VIEW v CASCADE", legacy.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"DROP TYPE IF EXISTS mood CASCADE", "DROP TYPE mood, app.color",
            "DROP DOMAIN d1", "DROP DOMAIN IF EXISTS d1 RESTRICT", "DROP EVENT TRIGGER etg",
            "DROP EVENT TRIGGER IF EXISTS etg CASCADE", "DROP TABLESPACE IF EXISTS fast",
            "DROP DATABASE IF EXISTS mydb WITH (FORCE)", "DROP DATABASE mydb WITH (FORCE)"})
    void postgreSqlObjectTypes(String sql) throws JSQLParserException {
        for (Drop drop : roundTrip(sql, Dialect.POSTGRESQL)) {
            assertEquals(sql, drop.toString());
            assertNotEquals(Drop.ObjectType.OTHER, drop.getObjectType());
            assertTrue(new TablesNamesFinder().getTables(drop).isEmpty());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"DROP ACCESS METHOD heptree",
            "DROP ACCESS METHOD IF EXISTS heptree CASCADE", "DROP PROCEDURAL LANGUAGE plsample",
            "DROP PROCEDURAL LANGUAGE IF EXISTS plsample RESTRICT",
            "DROP LANGUAGE IF EXISTS plsample CASCADE",
            "DROP OPERATOR CLASS IF EXISTS public.myops USING btree CASCADE",
            "DROP OPERATOR FAMILY myfam USING hash",
            "DROP TEXT SEARCH CONFIGURATION IF EXISTS my_cfg CASCADE",
            "DROP TEXT SEARCH DICTIONARY english_stem, public.french_stem",
            "DROP TEXT SEARCH PARSER my_parser",
            "DROP TEXT SEARCH TEMPLATE IF EXISTS my_tmpl RESTRICT"})
    void postgreSqlMultiWordObjectTypes(String sql) throws JSQLParserException {
        for (Drop drop : roundTrip(sql, Dialect.POSTGRESQL)) {
            assertEquals(sql, drop.toString());
            assertNotEquals(Drop.ObjectType.OTHER, drop.getObjectType());
            assertTrue(new TablesNamesFinder().getTables(drop).isEmpty());
        }
        assertEquals(sql, CCJSqlParserUtil.parse(sql).toString());
        assertEquals(2, CCJSqlParserUtil.parseStatements(sql + "; SELECT 1",
                parser -> parser.withDialect(Dialect.POSTGRESQL)).size());
    }

    @Test
    void multiWordObjectTypeFields() throws JSQLParserException {
        Drop accessMethod =
                parse("DROP ACCESS METHOD IF EXISTS heptree CASCADE", Dialect.POSTGRESQL);
        assertEquals(Drop.ObjectType.ACCESS_METHOD, accessMethod.getObjectType());
        assertEquals("ACCESS METHOD", accessMethod.getType());
        assertEquals(List.of("heptree"), names(accessMethod));
        assertTrue(accessMethod.isIfExists());
        assertEquals(CascadeBehavior.CASCADE, accessMethod.getBehavior());

        Drop language = parse("DROP PROCEDURAL LANGUAGE plsample", Dialect.POSTGRESQL);
        assertEquals(Drop.ObjectType.LANGUAGE, language.getObjectType());
        assertTrue(language.isProcedural());
        assertEquals(List.of("plsample"), names(language));
        language.setProcedural(false);
        assertEquals("DROP LANGUAGE plsample", language.toString());
        assertFalse(parse("DROP LANGUAGE plsample", Dialect.POSTGRESQL).isProcedural());

        Drop operatorClass = parse("DROP OPERATOR CLASS IF EXISTS public.myops USING btree",
                Dialect.POSTGRESQL);
        assertEquals(Drop.ObjectType.OPERATOR_CLASS, operatorClass.getObjectType());
        assertEquals(List.of("public.myops"), names(operatorClass));
        assertEquals("btree", operatorClass.getAccessMethod());
        assertNull(operatorClass.getParameters());
        operatorClass.setAccessMethod("hash");
        assertEquals("DROP OPERATOR CLASS IF EXISTS public.myops USING hash",
                operatorClass.toString());
        assertEquals(Drop.ObjectType.OPERATOR_FAMILY,
                parse("DROP OPERATOR FAMILY myfam USING gist", Dialect.POSTGRESQL).getObjectType());

        Drop dictionary = parse("DROP TEXT SEARCH DICTIONARY english_stem, public.french_stem",
                Dialect.POSTGRESQL);
        assertEquals(Drop.ObjectType.TEXT_SEARCH_DICTIONARY, dictionary.getObjectType());
        assertEquals(List.of("english_stem", "public.french_stem"), names(dictionary));

        assertEquals("DROP OPERATOR FAMILY f USING btree CASCADE",
                new Drop().withObjectType(Drop.ObjectType.OPERATOR_FAMILY)
                        .withName(new Table("f")).withAccessMethod("btree")
                        .withBehavior(CascadeBehavior.CASCADE).toString());
        assertEquals("DROP PROCEDURAL LANGUAGE l", new Drop()
                .withObjectType(Drop.ObjectType.LANGUAGE).withProcedural(true)
                .withName(new Table("l")).toString());
    }

    @Test
    void multiWordTypeWordsRemainNames() throws JSQLParserException {
        assertEquals(List.of("procedural"),
                names(parse("DROP LANGUAGE procedural", Dialect.POSTGRESQL)));
        assertEquals(List.of("method"),
                names(parse("DROP ACCESS METHOD method", Dialect.POSTGRESQL)));
        assertEquals(List.of("access"), names(parse("DROP TABLE access", Dialect.POSTGRESQL)));
        assertEquals(List.of("family"),
                names(parse("DROP OPERATOR CLASS family USING btree", Dialect.POSTGRESQL)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"DROP OPERATOR CLASS myops", "DROP OPERATOR FAMILY f1, f2 USING btree",
            "DROP OPERATOR CLASS myops USING", "DROP ACCESS METHOD",
            "DROP ACCESS METHOD IF EXISTS", "DROP TEXT SEARCH INDEX x", "DROP TEXT SEARCH",
            "DROP PROCEDURAL LANGUAGE", "DROP ACCESS METHOD heptree USING btree"})
    void rejectsIncompleteMultiWordTypes(String sql) {
        assertThrows(JSQLParserException.class, () -> parse(sql, Dialect.POSTGRESQL), sql);
    }

    @ParameterizedTest
    @ValueSource(strings = {"DROP EVENT IF EXISTS ev1", "DROP EVENT db1.ev1", "DROP TABLESPACE ts1",
            "DROP TABLESPACE ts1 ENGINE = InnoDB", "DROP UNDO TABLESPACE ts1",
            "DROP UNDO TABLESPACE ts1 ENGINE InnoDB"})
    void mySqlObjectTypes(String sql) throws JSQLParserException {
        for (Drop drop : roundTrip(sql, Dialect.MYSQL)) {
            assertEquals(sql, drop.toString());
        }
    }

    @Test
    void objectTypeFields() throws JSQLParserException {
        Drop eventTrigger = parse("DROP EVENT TRIGGER etg", Dialect.POSTGRESQL);
        assertEquals(Drop.ObjectType.EVENT_TRIGGER, eventTrigger.getObjectType());
        assertEquals("EVENT TRIGGER", eventTrigger.getType());
        assertEquals("etg", eventTrigger.getName().getName());

        Drop event = parse("DROP EVENT IF EXISTS ev1", Dialect.MYSQL);
        assertEquals(Drop.ObjectType.EVENT, event.getObjectType());

        Drop type = parse("DROP TYPE mood, app.color CASCADE", Dialect.POSTGRESQL);
        assertEquals(Drop.ObjectType.TYPE, type.getObjectType());
        assertEquals("app", type.getNames().get(1).getSchemaName());

        Drop tablespace = parse("DROP UNDO TABLESPACE ts1 ENGINE=InnoDB", Dialect.MYSQL);
        assertEquals(Drop.ObjectType.TABLESPACE, tablespace.getObjectType());
        assertTrue(tablespace.isUndo());
        assertEquals("InnoDB", tablespace.getEngine());
        assertTrue(tablespace.isEngineUseEquals());
        assertEquals("DROP UNDO TABLESPACE ts1 ENGINE = InnoDB", tablespace.toString());
        tablespace.withUndo(false).withEngine("NDB").setEngineUseEquals(false);
        assertEquals("DROP TABLESPACE ts1 ENGINE NDB", tablespace.toString());

        Drop database = parse("DROP DATABASE mydb (FORCE)", Dialect.POSTGRESQL);
        assertTrue(database.isForce());
        assertEquals("DROP DATABASE mydb WITH (FORCE)", database.toString());
        database.setForce(false);
        assertEquals("DROP DATABASE mydb", database.toString());

        assertEquals("DROP DATABASE IF EXISTS d WITH (FORCE)", new Drop()
                .withObjectType(Drop.ObjectType.DATABASE).withIfExists(true)
                .withName(new Table("d")).withForce(true).toString());
    }

    @Test
    void clauseWordsRemainNames() throws JSQLParserException {
        assertEquals("engine",
                parse("DROP TABLESPACE engine", Dialect.MYSQL).getName().getName());
        assertEquals("undo", parse("DROP TABLE undo", Dialect.MYSQL).getName().getName());
        assertEquals("owned", parse("DROP TABLE owned", Dialect.POSTGRESQL).getName().getName());
        assertEquals(2, CCJSqlParserUtil.parseStatements(
                "DROP DATABASE d; WITH x AS (SELECT 1) SELECT * FROM x",
                parser -> parser.withDialect(Dialect.POSTGRESQL)).size());
    }

    @Test
    void rejectsMisplacedClauses() {
        for (String sql : List.of("DROP TABLE t WITH (FORCE)", "DROP DATABASE d WITH (FORCE",
                "DROP TABLE t ENGINE = InnoDB", "DROP TABLESPACE ts ENGINE",
                "DROP TABLE t CASCADE x",
                "DROP TRIGGER t ON a ON b", "DROP EVENT TRIGGER", "DROP TYPE")) {
            assertThrows(JSQLParserException.class, () -> parse(sql, Dialect.MYSQL), sql);
        }
    }

    private static List<String> names(Drop drop) {
        return drop.getNames().stream().map(Table::getFullyQualifiedName)
                .collect(Collectors.toList());
    }

    private static Drop parse(String sql, Dialect dialect) throws JSQLParserException {
        return (Drop) CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(dialect));
    }

    private static List<Drop> roundTrip(String sql, Dialect dialect) throws JSQLParserException {
        Drop drop = (Drop) assertSqlCanBeParsedAndDeparsed(sql, true,
                parser -> parser.withDialect(dialect));
        StringBuilder builder = new StringBuilder();
        drop.accept(new StatementDeParser(builder), null);
        assertEquals(drop.toString(), builder.toString());
        return List.of(drop, parse(drop.toString(), dialect));
    }
}

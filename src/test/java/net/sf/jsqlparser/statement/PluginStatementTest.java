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

import static net.sf.jsqlparser.statement.TransactionStatementTest.assertSpans;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.parser.ASTNodeAccess;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.parser.feature.FeatureConfiguration;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.ValidationCapability;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import net.sf.jsqlparser.util.validation.feature.MariaDbVersion;
import net.sf.jsqlparser.util.validation.feature.MySqlVersion;
import net.sf.jsqlparser.util.validation.feature.PostgresqlVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/** MySQL INSTALL and UNINSTALL PLUGIN and COMPONENT. */
class PluginStatementTest {
    private static final Dialect[] MYSQL_FAMILY = {Dialect.MYSQL, Dialect.MARIADB, null};
    private static final Dialect[] MYSQL_OR_NONE = {Dialect.MYSQL, null};

    private static Statement parse(String sql, Dialect dialect) throws JSQLParserException {
        return dialect == null ? CCJSqlParserUtil.parse(sql)
                : CCJSqlParserUtil.parse(sql, p -> p.withDialect(dialect));
    }

    /**
     * Parses {@code sql}, which must print as written, and checks the statement span, the deparser,
     * the reparse, the feature analysis and the table finder.
     */
    private static PluginStatement roundTrip(String sql, Dialect dialect) throws Exception {
        PluginStatement statement = assertInstanceOf(PluginStatement.class, parse(sql, dialect));
        assertEquals(sql, statement.toString());
        assertSpans(sql, sql, statement);
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(output), null);
        assertEquals(sql, output.toString());
        assertEquals(sql, parse(output.toString(), dialect).toString());
        assertEquals(Set.of(StmtFeature.MODIFIES_SESSION), statement.getFeatures().getCertain());
        assertTrue(statement.getFeatures().getUncertain().isEmpty());
        assertTrue(new TablesNamesFinder<>().getTables((Statement) statement).isEmpty());
        return statement;
    }

    // ---- plugins -------------------------------------------------------------------------------

    @Test
    void installPluginNamesThePluginAndItsLibrary() throws Exception {
        String sql = "INSTALL PLUGIN myplugin SONAME 'somelib.so'";
        for (Dialect dialect : MYSQL_FAMILY) {
            PluginStatement statement = roundTrip(sql, dialect);
            assertEquals(PluginStatement.Action.INSTALL, statement.getAction());
            assertEquals(PluginStatement.ObjectType.PLUGIN, statement.getObjectType());
            assertEquals("myplugin", statement.getPluginName());
            assertEquals("somelib.so", statement.getLibrary().getValue());
            assertSpans("'somelib.so'", sql, statement.getLibrary());
            assertTrue(statement.getComponents().isEmpty());
            assertTrue(statement.getVariables().isEmpty());
        }
    }

    @Test
    void uninstallPluginNamesThePlugin() throws Exception {
        for (Dialect dialect : MYSQL_FAMILY) {
            PluginStatement statement = roundTrip("UNINSTALL PLUGIN myplugin", dialect);
            assertEquals(PluginStatement.Action.UNINSTALL, statement.getAction());
            assertEquals(PluginStatement.ObjectType.PLUGIN, statement.getObjectType());
            assertEquals("myplugin", statement.getPluginName());
            assertNull(statement.getLibrary());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"INSTALL PLUGIN `my plugin` SONAME 'somelib.so'",
            "INSTALL PLUGIN rpl_semi_sync_source SONAME \"nosuch_semisync_source.so\"",
            "INSTALL PLUGIN plugin SONAME ''", "INSTALL PLUGIN soname SONAME 'x'",
            "UNINSTALL PLUGIN `my plugin`", "UNINSTALL PLUGIN plugin",
            "UNINSTALL PLUGIN component"})
    void pluginNamesAndLibrariesAreKeptAsWritten(String sql) throws Exception {
        roundTrip(sql, Dialect.MYSQL);
        PluginStatement lower =
                (PluginStatement) parse(sql.toLowerCase(java.util.Locale.ROOT), Dialect.MYSQL);
        assertEquals(sql.toLowerCase(java.util.Locale.ROOT),
                lower.toString().toLowerCase(java.util.Locale.ROOT));
    }

    // ---- components ----------------------------------------------------------------------------

    @Test
    void installComponentListsUrnsAndVariables() throws Exception {
        String sql = "INSTALL COMPONENT 'file://component1', 'file://component2'"
                + " SET GLOBAL component1.var1 = 12 + 3, PERSIST component1.var3 = 'x'";
        for (Dialect dialect : MYSQL_OR_NONE) {
            PluginStatement statement = roundTrip(sql, dialect);
            assertEquals(PluginStatement.Action.INSTALL, statement.getAction());
            assertEquals(PluginStatement.ObjectType.COMPONENT, statement.getObjectType());
            assertNull(statement.getPluginName());
            assertNull(statement.getLibrary());
            assertEquals(Arrays.asList("file://component1", "file://component2"),
                    statement.getComponents().stream().map(StringValue::getValue)
                            .collect(Collectors.toList()));
            assertSpans("'file://component2'", sql, statement.getComponents().get(1));
            assertEquals(2, statement.getVariables().size());
            ComponentVariable first = statement.getVariables().get(0);
            assertEquals(SetScope.GLOBAL, first.getScope());
            assertEquals("component1.var1", first.getName());
            assertEquals(SetStatement.AssignmentOperator.EQUALS, first.getOperator());
            assertSpans("12 + 3", sql, assertInstanceOf(Addition.class, first.getValue()));
            ComponentVariable second = statement.getVariables().get(1);
            assertEquals(SetScope.PERSIST, second.getScope());
            assertEquals("component1.var3", second.getName());
            assertEquals("x", assertInstanceOf(StringValue.class, second.getValue()).getValue());
            assertEquals("PERSIST component1.var3 = 'x'", second.toString());
        }
    }

    @Test
    void uninstallComponentListsUrns() throws Exception {
        for (Dialect dialect : MYSQL_OR_NONE) {
            PluginStatement statement =
                    roundTrip("UNINSTALL COMPONENT 'file://component1'", dialect);
            assertEquals(PluginStatement.Action.UNINSTALL, statement.getAction());
            assertEquals(PluginStatement.ObjectType.COMPONENT, statement.getObjectType());
            assertEquals(1, statement.getComponents().size());
            assertTrue(statement.getVariables().isEmpty());
        }
        assertEquals(2, roundTrip("UNINSTALL COMPONENT 'file://c', 'file://d'", Dialect.MYSQL)
                .getComponents().size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"INSTALL COMPONENT 'file://component_validate_password'",
            "INSTALL COMPONENT 'file://c' SET v = 1", "INSTALL COMPONENT 'file://c' SET c.v := 1",
            "INSTALL COMPONENT 'file://c' SET GLOBAL c.v = ON",
            "INSTALL COMPONENT 'file://c' SET PERSIST v = OFF, w = 1",
            "INSTALL COMPONENT 'file://c' SET DEFAULT.v = 1",
            "INSTALL COMPONENT 'file://c' SET GLOBAL DEFAULT.v = 1",
            "INSTALL COMPONENT 'file://c' SET persist.v = 1, PERSIST persist.w = 2, persist_only.x = 3",
            "INSTALL COMPONENT 'file://c' SET `global` = 1, c.global = 2, c.`default` = 3",
            "INSTALL COMPONENT 'file://c' SET v = (1 + 2) * 3, w = CONCAT('a', 'b'), x = NOW(), y = @a + 1, z = @@global.max_connections",
            "INSTALL COMPONENT 'file://c' SET v = other, w = a.b, x = NULL, y = true, z = -1.5e3",
            "INSTALL COMPONENT 'file://c' SET v = a AND b OR NOT c, w = CASE WHEN 1 THEN 2 ELSE 3 END, x = VALUES(a), y = DEFAULT(a)",
            "INSTALL COMPONENT 'file://c', 'file://d' SET GLOBAL v = 1, GLOBAL w = 2, x = 3",
            "INSTALL COMPONENT \"file://c\"", "INSTALL COMPONENT ''"})
    void componentFormsOfTheServerGrammarRoundTrip(String sql) throws Exception {
        roundTrip(sql, Dialect.MYSQL);
    }

    @Test
    void variablesKeepScopeNameOperatorAndValue() throws Exception {
        String sql = "INSTALL COMPONENT 'file://c' SET c.v := 1, DEFAULT.w = ON, persist.x = 2,"
                + " PERSIST y = OFF";
        List<ComponentVariable> variables = roundTrip(sql, Dialect.MYSQL).getVariables();
        assertEquals(SetScope.NONE, variables.get(0).getScope());
        assertEquals("c.v", variables.get(0).getName());
        assertEquals(SetStatement.AssignmentOperator.COLON_EQUALS, variables.get(0).getOperator());
        assertEquals(1L, assertInstanceOf(LongValue.class, variables.get(0).getValue()).getValue());
        assertEquals("DEFAULT.w", variables.get(1).getName());
        Column on = assertInstanceOf(Column.class, variables.get(1).getValue());
        assertEquals("ON", on.getColumnName());
        assertSpans("ON", sql, on);
        assertEquals(SetScope.NONE, variables.get(2).getScope());
        assertEquals("persist.x", variables.get(2).getName());
        assertEquals(SetScope.PERSIST, variables.get(3).getScope());
        assertEquals("y", variables.get(3).getName());
    }

    // ---- rejected ------------------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {"INSTALL PLUGIN myplugin", "INSTALL PLUGIN SONAME 'somelib.so'",
            "INSTALL PLUGIN myplugin SONAME somelib",
            "INSTALL PLUGIN myplugin SONAME N'somelib.so'",
            "INSTALL PLUGIN 'myplugin' SONAME 'somelib.so'",
            "INSTALL PLUGIN a.b SONAME 'somelib.so'",
            "INSTALL PLUGIN IF NOT EXISTS myplugin SONAME 'somelib.so'",
            "INSTALL PLUGIN select SONAME 'x'", "INSTALL SONAME 'somelib.so'", "INSTALL myplugin",
            "INSTALL myplugin FROM community", "FORCE INSTALL myplugin", "INSTALL", "UNINSTALL",
            "UNINSTALL PLUGIN", "UNINSTALL PLUGIN 'myplugin'",
            "UNINSTALL PLUGIN myplugin SONAME 'somelib.so'", "UNINSTALL PLUGIN IF EXISTS myplugin",
            "UNINSTALL PLUGIN a, b", "UNINSTALL SONAME 'somelib.so'", "UNINSTALL myplugin",
            "INSTALL COMPONENT", "INSTALL COMPONENT component1", "INSTALL COMPONENT 'file://c',",
            "INSTALL COMPONENT N'file://c'", "INSTALL COMPONENT X'41'",
            "INSTALL COMPONENT 'file://c' 'file://d'", "INSTALL COMPONENT 'file://c' SET",
            "INSTALL COMPONENT 'file://c' SET @@GLOBAL.validate_password.length = 12",
            "INSTALL COMPONENT 'file://c' SET @v = 1",
            "INSTALL COMPONENT 'file://c' SET SESSION v = 1",
            "INSTALL COMPONENT 'file://c' SET LOCAL v = 1",
            "INSTALL COMPONENT 'file://c' SET PERSIST_ONLY v = 1",
            "INSTALL COMPONENT 'file://c' SET global.v = 1",
            "INSTALL COMPONENT 'file://c' SET session.v = 1",
            "INSTALL COMPONENT 'file://c' SET local.v = 1",
            "INSTALL COMPONENT 'file://c' SET `session`.v = 1",
            "INSTALL COMPONENT 'file://c' SET GLOBAL global.v = 1",
            "INSTALL COMPONENT 'file://c' SET GLOBAL = 1",
            "INSTALL COMPONENT 'file://c' SET persist = 1",
            "INSTALL COMPONENT 'file://c' SET persist_only = 1",
            "INSTALL COMPONENT 'file://c' SET v",
            "INSTALL COMPONENT 'file://c' SET v =", "INSTALL COMPONENT 'file://c' SET a.b.c = 1",
            "INSTALL COMPONENT 'file://c' SET DEFAULT = 1",
            "INSTALL COMPONENT 'file://c' SET v TO 1",
            "INSTALL COMPONENT 'file://c' SET GLOBAL PERSIST v = 1",
            "INSTALL COMPONENT 'file://c' SET v = DEFAULT",
            "INSTALL COMPONENT 'file://c' SET v = ALL",
            "INSTALL COMPONENT 'file://c' SET v = ON + 1",
            "INSTALL COMPONENT 'file://c' SET v = (SELECT 1)",
            "INSTALL COMPONENT 'file://c' SET v = 1 + (SELECT 1)",
            "INSTALL COMPONENT 'file://c' SET v = EXISTS (SELECT 1)",
            "INSTALL COMPONENT 'file://c' SET v = 1 IN (SELECT 1)",
            "INSTALL COMPONENT 'file://c' SET v = (TABLE t)",
            "INSTALL COMPONENT 'file://c' SET v = (VALUES ROW(1))",
            "INSTALL COMPONENT 'file://c' SET v = 1,",
            "INSTALL COMPONENT 'file://c' SET v = 1 w = 2",
            "UNINSTALL COMPONENT", "UNINSTALL COMPONENT c",
            "UNINSTALL COMPONENT 'file://c' SET v = 1"})
    void mySqlRejectsWhatTheServerRejects(String sql) {
        assertThrows(JSQLParserException.class, () -> parse(sql, Dialect.MYSQL), sql);
    }

    @ParameterizedTest
    @ValueSource(strings = {"INSTALL COMPONENT 'file://c'", "UNINSTALL COMPONENT 'file://c'",
            "INSTALL COMPONENT 'file://c' SET v = 1"})
    void mariaDbHasNoComponents(String sql) {
        assertThrows(JSQLParserException.class, () -> parse(sql, Dialect.MARIADB), sql);
    }

    @ParameterizedTest
    @ValueSource(strings = {"INSTALL PLUGIN myplugin SONAME 'somelib.so'",
            "UNINSTALL PLUGIN myplugin", "INSTALL COMPONENT 'file://c'",
            "UNINSTALL COMPONENT 'file://c'"})
    void otherDialectsKeepRejectingTheStatements(String sql) {
        for (Dialect dialect : new Dialect[] {Dialect.POSTGRESQL, Dialect.ORACLE,
                Dialect.SQLSERVER}) {
            assertThrows(JSQLParserException.class, () -> parse(sql, dialect), sql);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"UNINSTALL myplugin", "INSTALL PLUGIN myplugin SONAME",
            "INSTALL PLUGIN myplugin SONAME x", "INSTALL COMPONENT 'file://c' SET v = (SELECT 1)",
            "UNINSTALL PLUGIN myplugin SONAME 'x'", "UNINSTALL COMPONENT c"})
    void noDialectRejectsMalformedStatements(String sql) {
        assertThrows(JSQLParserException.class, () -> parse(sql, null), sql);
    }

    // ---- other readings ------------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {"INSTALL plugin", "INSTALL plugin FROM community", "INSTALL component",
            "INSTALL component FROM 'http://example.org'", "FORCE INSTALL plugin",
            "INSTALL spatial"})
    void duckDbExtensionsKeepTheirReading(String sql) throws Exception {
        for (Dialect dialect : new Dialect[] {null, Dialect.ORACLE, Dialect.POSTGRESQL}) {
            ExtensionStatement statement =
                    assertInstanceOf(ExtensionStatement.class, parse(sql, dialect));
            assertEquals(ExtensionStatement.Operation.INSTALL, statement.getOperation());
            assertEquals(sql, statement.toString());
        }
    }

    @Test
    void statementWordsRemainIdentifiers() throws Exception {
        String sql = "SELECT uninstall, plugin, component, soname FROM install";
        for (Dialect dialect : MYSQL_FAMILY) {
            PlainSelect select = assertInstanceOf(PlainSelect.class, parse(sql, dialect));
            assertEquals(sql, select.toString());
            assertEquals(Set.of("install"),
                    new TablesNamesFinder<>().getTables((Statement) select));
        }
        assertEquals("CREATE TABLE uninstall (plugin INT, component INT)",
                parse("CREATE TABLE uninstall (plugin INT, component INT)", Dialect.MYSQL)
                        .toString());
    }

    // ---- statement boundaries ------------------------------------------------------------------

    private static Stream<Arguments> newStatementStarts() {
        return Stream.of(
                Arguments.of(Dialect.MYSQL, "INSTALL PLUGIN myplugin SONAME 'somelib.so'"),
                Arguments.of(Dialect.MARIADB, "UNINSTALL PLUGIN myplugin"),
                Arguments.of(null, "UNINSTALL PLUGIN myplugin"),
                Arguments.of(Dialect.MYSQL, "INSTALL COMPONENT 'file://c' SET GLOBAL c.v = 1"),
                Arguments.of(Dialect.MYSQL, "UNINSTALL COMPONENT 'file://c', 'file://d'"),
                Arguments.of(null, "UNINSTALL COMPONENT 'file://c'"));
    }

    @ParameterizedTest
    @MethodSource("newStatementStarts")
    void newStatementStartsEndThePreviousStatement(Dialect dialect, String sql) throws Exception {
        // COMMIT on its own line is closed by the next statement start rather than a semicolon
        String script = "SELECT 1;\nCOMMIT\n" + sql + "\n;SAVEPOINT sp";
        for (boolean opaque : new boolean[] {false, true}) {
            Statements statements = CCJSqlParserUtil.parseStatements(script, p -> {
                if (dialect != null) {
                    p.withDialect(dialect);
                }
                p.withUnsupportedStatements(opaque);
            });
            assertEquals(4, statements.size(), script);
            assertEquals("COMMIT", statements.get(1).toString());
            assertInstanceOf(PluginStatement.class, statements.get(2));
            assertEquals(sql, statements.get(2).toString());
            assertSpans(sql, script, (ASTNodeAccess) statements.get(2));
            assertInstanceOf(SavepointStatement.class, statements.get(3));
        }
    }

    @Test
    void scriptsKeepEachStatement() throws Exception {
        String script = "INSTALL COMPONENT 'file://c' SET c.v = 1; SET GLOBAL x = 2;"
                + " INSTALL PLUGIN p SONAME 'p.so'; SELECT * FROM t; UNINSTALL PLUGIN p;"
                + " UNINSTALL COMPONENT 'file://c'";
        Statements statements =
                CCJSqlParserUtil.parseStatements(script, p -> p.withDialect(Dialect.MYSQL));
        assertEquals(6, statements.size());
        assertEquals(1, ((PluginStatement) statements.get(0)).getVariables().size());
        assertInstanceOf(SetStatement.class, statements.get(1));
        assertEquals(Set.of("t"), new TablesNamesFinder<>().getTables(statements.get(3)));
        assertEquals(PluginStatement.Action.UNINSTALL,
                ((PluginStatement) statements.get(5)).getAction());
        assertEquals(statements.toString(), CCJSqlParserUtil
                .parseStatements(statements.toString(), p -> p.withDialect(Dialect.MYSQL))
                .toString());
    }

    // ---- visitors and validation ---------------------------------------------------------------

    @Test
    void visitorDispatchPassesContextAndResult() throws Exception {
        StatementVisitorAdapter<String> visitor = new StatementVisitorAdapter<>() {
            @Override
            public <S> String visit(PluginStatement statement, S context) {
                return context + ":" + statement.getAction() + " " + statement.getObjectType();
            }
        };
        Statement statement = parse("UNINSTALL PLUGIN p", null);
        assertEquals("ctx:UNINSTALL PLUGIN", statement.accept(visitor, "ctx"));
        assertNull(statement.accept(new StatementVisitorAdapter<>(), null));
        StringBuilder seen = new StringBuilder();
        new StatementVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(PluginStatement plugin, S context) {
                seen.append(plugin.getPluginName());
                return null;
            }
        }.visit((PluginStatement) statement);
        assertEquals("p", seen.toString());
    }

    @Test
    void expressionsReachTheVisitors() throws Exception {
        PluginStatement statement = (PluginStatement) parse(
                "INSTALL COMPONENT 'file://c', 'file://d' SET v = a + 1, w = 'x'", Dialect.MYSQL);
        StringBuilder seen = new StringBuilder();
        statement.visitExpressions(expression -> seen.append(expression).append('|'));
        assertEquals("'file://c'|'file://d'|a + 1|'x'|", seen.toString());
        seen.setLength(0);
        ((PluginStatement) parse("INSTALL PLUGIN p SONAME 'p.so'", null))
                .visitExpressions(expression -> seen.append(expression));
        assertEquals("'p.so'", seen.toString());
        ((PluginStatement) parse("UNINSTALL PLUGIN p", null))
                .visitExpressions(expression -> fail("no expression"));
    }

    private static boolean isValid(Dialect dialect, String sql,
            ValidationCapability... capabilities) {
        FeatureConfiguration configuration = new FeatureConfiguration();
        if (dialect != null) {
            configuration.setValue(Feature.dialect, dialect.name());
        }
        return new Validation(configuration, Arrays.asList(capabilities), sql).validate()
                .isEmpty();
    }

    @Test
    void validationKnowsWhichServersTakeWhichForm() {
        String plugin = "INSTALL PLUGIN p SONAME 'p.so'; UNINSTALL PLUGIN p";
        String component = "INSTALL COMPONENT 'file://c' SET v = 1; UNINSTALL COMPONENT 'file://c'";
        assertTrue(isValid(Dialect.MYSQL, plugin, MySqlVersion.V8_0));
        assertTrue(isValid(Dialect.MYSQL, component, MySqlVersion.V8_0));
        assertTrue(isValid(Dialect.MARIADB, plugin, MariaDbVersion.V10_5_4));
        assertFalse(isValid(null, "UNINSTALL COMPONENT 'file://c'", MariaDbVersion.V10_5_4));
        assertFalse(isValid(null, "UNINSTALL PLUGIN p", PostgresqlVersion.V14));
        assertTrue(isValid(null, plugin, new FeaturesAllowed(Feature.installPlugin)));
        assertFalse(isValid(null, component, new FeaturesAllowed(Feature.installPlugin)));
        assertTrue(isValid(null, component, new FeaturesAllowed(Feature.installComponent)));
        assertFalse(isValid(null, "INSTALL PLUGIN p SONAME 'p.so'",
                new FeaturesAllowed(Feature.installExtension)));
    }

    // ---- the model -----------------------------------------------------------------------------

    @Test
    void statementsCanBeBuilt() {
        PluginStatement install = new PluginStatement(PluginStatement.Action.INSTALL,
                PluginStatement.ObjectType.PLUGIN).withPluginName("p")
                .withLibrary(new StringValue("'p.so'"));
        assertEquals("INSTALL PLUGIN p SONAME 'p.so'", install.toString());
        install.setAction(PluginStatement.Action.UNINSTALL);
        install.setLibrary(null);
        assertEquals("UNINSTALL PLUGIN p", install.toString());

        PluginStatement component = new PluginStatement(PluginStatement.Action.INSTALL,
                PluginStatement.ObjectType.PLUGIN);
        component.setObjectType(PluginStatement.ObjectType.COMPONENT);
        component.addComponents(new StringValue("'file://c'"), new StringValue("'file://d'"))
                .addVariables(
                        new ComponentVariable("c.v", new LongValue(1)).withScope(SetScope.PERSIST),
                        new ComponentVariable("w", new Column("ON"))
                                .withOperator(SetStatement.AssignmentOperator.COLON_EQUALS));
        assertEquals("INSTALL COMPONENT 'file://c', 'file://d' SET PERSIST c.v = 1, w := ON",
                component.toString());
        component.setComponents(new java.util.ArrayList<>(component.getComponents().subList(0, 1)));
        component.setVariables(new java.util.ArrayList<>());
        assertEquals("INSTALL COMPONENT 'file://c'", component.toString());
        assertThrows(NullPointerException.class, () -> component.setAction(null));
        assertThrows(NullPointerException.class, () -> component.setObjectType(null));
        assertThrows(NullPointerException.class, () -> component.setComponents(null));
        assertThrows(NullPointerException.class, () -> component.setVariables(null));
    }

    @Test
    void componentVariableKeepsItsContract() {
        ComponentVariable variable = new ComponentVariable("v", new LongValue(1));
        assertEquals(SetScope.NONE, variable.getScope());
        assertEquals("v = 1", variable.toString());
        variable.setScope(null);
        assertEquals(SetScope.NONE, variable.getScope());
        variable.setScope(SetScope.GLOBAL);
        variable.setName("c.v");
        variable.setValue(new StringValue("'x'"));
        variable.setOperator(SetStatement.AssignmentOperator.COLON_EQUALS);
        assertEquals("GLOBAL c.v := 'x'", variable.toString());
        assertThrows(IllegalArgumentException.class, () -> variable.setScope(SetScope.SESSION));
        assertThrows(IllegalArgumentException.class,
                () -> variable.setScope(SetScope.PERSIST_ONLY));
        assertThrows(IllegalArgumentException.class,
                () -> variable.setOperator(SetStatement.AssignmentOperator.TO));
        assertThrows(IllegalArgumentException.class, () -> variable.setOperator(null));
        assertThrows(NullPointerException.class, () -> variable.setName(null));
        assertThrows(NullPointerException.class, () -> variable.setValue(null));
        assertThrows(NullPointerException.class, () -> new ComponentVariable(null, null));
    }
}

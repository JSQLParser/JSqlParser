/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.resourcegroup;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.HexValue;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.ASTNodeAccess;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.Node;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.parser.feature.FeatureConfiguration;
import net.sf.jsqlparser.statement.SetStatement;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.StmtFeature;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.resourcegroup.ResourceGroupDefinition.State;
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

/** MySQL CREATE, ALTER, DROP and SET RESOURCE GROUP. */
class ResourceGroupTest {
    /** Resource groups are read with the MySQL dialect or without one. */
    private static final Dialect[] MYSQL = {Dialect.MYSQL, null};
    private static final Dialect[] OTHERS =
            {Dialect.MARIADB, Dialect.POSTGRESQL, Dialect.ORACLE, Dialect.SQLSERVER, Dialect.H2};

    private static Statement parse(String sql, Dialect dialect) throws JSQLParserException {
        return dialect == null ? CCJSqlParserUtil.parse(sql)
                : CCJSqlParserUtil.parse(sql, p -> p.withDialect(dialect));
    }

    private static void assertSpans(String expected, String source, ASTNodeAccess access) {
        Node node = access.getASTNode();
        assertNotNull(node, expected);
        assertEquals(expected, source.substring(node.jjtGetFirstToken().absoluteBegin - 1,
                node.jjtGetLastToken().absoluteEnd - 1));
    }

    /**
     * Parses {@code sql}, which must print as {@code expected}, and checks the statement span, the
     * deparser, the reparse, the feature analysis and the table finder.
     */
    private static <T extends Statement> T roundTrip(Class<T> type, String sql, String expected,
            Dialect dialect, StmtFeature... features) throws Exception {
        T statement = assertInstanceOf(type, parse(sql, dialect), sql);
        assertEquals(expected, statement.toString());
        assertSpans(sql, sql, (ASTNodeAccess) statement);
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(output), null);
        assertEquals(expected, output.toString());
        Statement reparsed = parse(expected, dialect);
        assertEquals(type, reparsed.getClass());
        assertEquals(expected, reparsed.toString());
        assertEquals(Set.of(features), statement.getFeatures().getCertain());
        assertTrue(statement.getFeatures().getUncertain().isEmpty());
        assertTrue(new TablesNamesFinder<>().getTables(statement).isEmpty());
        return statement;
    }

    private static <T extends Statement> T roundTrip(Class<T> type, String sql, Dialect dialect)
            throws Exception {
        return roundTrip(type, sql, sql, dialect, features(type));
    }

    /** DROP RESOURCE GROUP is a Drop, which the analysis reports like every other DROP. */
    private static StmtFeature[] features(Class<? extends Statement> type) {
        if (type == SetResourceGroup.class) {
            return new StmtFeature[] {StmtFeature.MODIFIES_SESSION};
        }
        return type == Drop.class
                ? new StmtFeature[] {StmtFeature.MODIFIES_SCHEMA, StmtFeature.MODIFIES_DATA}
                : new StmtFeature[] {StmtFeature.MODIFIES_SCHEMA};
    }

    private static void assertRejected(String sql) {
        for (Dialect dialect : MYSQL) {
            assertThrows(JSQLParserException.class, () -> parse(sql, dialect), sql);
        }
    }

    // ---- CREATE RESOURCE GROUP -----------------------------------------------------------------

    @Test
    void createKeepsTypeVcpusPriorityAndState() throws Exception {
        for (Dialect dialect : MYSQL) {
            CreateResourceGroup group = roundTrip(CreateResourceGroup.class,
                    "CREATE RESOURCE GROUP rg1 TYPE = USER VCPU = 0-3, 5 THREAD_PRIORITY = 10 ENABLE",
                    dialect);
            assertEquals("rg1", group.getName());
            assertEquals(CreateResourceGroup.Type.USER, group.getType());
            assertEquals(OptionAssignment.EQUALS, group.getTypeAssignment());
            assertEquals(List.of(new VcpuRange(0, 3), new VcpuRange(5)), group.getVcpus());
            assertTrue(group.getVcpus().get(0).isRange());
            assertEquals(3, group.getVcpus().get(0).getLast());
            assertFalse(group.getVcpus().get(1).isRange());
            assertNull(group.getVcpus().get(1).getEnd());
            assertEquals(5, group.getVcpus().get(1).getLast());
            assertEquals(OptionAssignment.EQUALS, group.getVcpuAssignment());
            assertEquals(10, group.getThreadPriority());
            assertEquals(OptionAssignment.EQUALS, group.getThreadPriorityAssignment());
            assertEquals(State.ENABLE, group.getState());

            group = roundTrip(CreateResourceGroup.class, "CREATE RESOURCE GROUP rg2 TYPE = SYSTEM",
                    dialect);
            assertEquals(CreateResourceGroup.Type.SYSTEM, group.getType());
            assertTrue(group.getVcpus().isEmpty());
            assertNull(group.getThreadPriority());
            assertNull(group.getState());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREATE RESOURCE GROUP rg3 TYPE = USER VCPU = 0 THREAD_PRIORITY = 19",
            "CREATE RESOURCE GROUP rg4 TYPE = SYSTEM THREAD_PRIORITY = -20 DISABLE",
            "CREATE RESOURCE GROUP rg6 TYPE USER VCPU 1, 3 THREAD_PRIORITY 5",
            "CREATE RESOURCE GROUP rg7 TYPE := USER VCPU := 0 THREAD_PRIORITY := 1",
            "CREATE RESOURCE GROUP rg11 TYPE = USER VCPU = 3-1",
            "CREATE RESOURCE GROUP rg12 TYPE = USER VCPU = 2147483647",
            "CREATE RESOURCE GROUP rg13 TYPE = USER THREAD_PRIORITY = 2147483647",
            "CREATE RESOURCE GROUP rg14 TYPE = USER THREAD_PRIORITY = -2147483647",
            "CREATE RESOURCE GROUP `rg 16` TYPE = USER",
            "CREATE RESOURCE GROUP type TYPE = USER",
            "CREATE RESOURCE GROUP vcpu TYPE = USER VCPU = 0",
            "CREATE RESOURCE GROUP rg17 TYPE = USER DISABLE"})
    void createPrintsAsWritten(String sql) throws Exception {
        for (Dialect dialect : MYSQL) {
            roundTrip(CreateResourceGroup.class, sql, dialect);
        }
    }

    @Test
    void optionAssignmentsAreKeptPerOption() throws Exception {
        CreateResourceGroup group = roundTrip(CreateResourceGroup.class,
                "CREATE RESOURCE GROUP rg TYPE USER VCPU := 1 THREAD_PRIORITY = -5", Dialect.MYSQL);
        assertEquals(OptionAssignment.NONE, group.getTypeAssignment());
        assertEquals(OptionAssignment.COLON_EQUALS, group.getVcpuAssignment());
        assertEquals(OptionAssignment.EQUALS, group.getThreadPriorityAssignment());
        assertEquals(-5, group.getThreadPriority());
        assertEquals("", OptionAssignment.NONE.getSymbol());
        assertEquals("=", OptionAssignment.EQUALS.getSymbol());
        assertEquals(":=", OptionAssignment.COLON_EQUALS.getSymbol());
    }

    static Stream<Arguments> normalizedForms() {
        return Stream.of(
                // the commas of the VCPU and thread ID lists are optional
                Arguments.of("CREATE RESOURCE GROUP rg5 TYPE = USER VCPU = 0-3,9-10 ENABLE",
                        "CREATE RESOURCE GROUP rg5 TYPE = USER VCPU = 0-3, 9-10 ENABLE"),
                Arguments.of("CREATE RESOURCE GROUP rg8 TYPE = USER VCPU = 0 1 2-3",
                        "CREATE RESOURCE GROUP rg8 TYPE = USER VCPU = 0, 1, 2-3"),
                Arguments.of("CREATE RESOURCE GROUP rg9 TYPE = USER VCPU = 0 - 1",
                        "CREATE RESOURCE GROUP rg9 TYPE = USER VCPU = 0-1"),
                Arguments.of("CREATE RESOURCE GROUP rg10 TYPE = USER THREAD_PRIORITY = - 5",
                        "CREATE RESOURCE GROUP rg10 TYPE = USER THREAD_PRIORITY = -5"),
                Arguments.of("CREATE RESOURCE GROUP rg15 TYPE = USER VCPU = 007",
                        "CREATE RESOURCE GROUP rg15 TYPE = USER VCPU = 7"),
                Arguments.of(
                        "create resource group rg18 type = user vcpu = 0 thread_priority = 1 enable",
                        "CREATE RESOURCE GROUP rg18 TYPE = USER VCPU = 0 THREAD_PRIORITY = 1 ENABLE"),
                Arguments.of("ALTER RESOURCE GROUP rg1 VCPU = 2,3 THREAD_PRIORITY = 10",
                        "ALTER RESOURCE GROUP rg1 VCPU = 2, 3 THREAD_PRIORITY = 10"),
                Arguments.of("ALTER RESOURCE GROUP rg1 VCPU 0 1",
                        "ALTER RESOURCE GROUP rg1 VCPU 0, 1"),
                Arguments.of("SET RESOURCE GROUP rg1 FOR 14 78 4",
                        "SET RESOURCE GROUP rg1 FOR 14, 78, 4"),
                Arguments.of("set resource group rg1 for 14", "SET RESOURCE GROUP rg1 FOR 14"));
    }

    @ParameterizedTest
    @MethodSource("normalizedForms")
    void optionalCommasAndSpacingAreNormalized(String sql, String expected) throws Exception {
        for (Dialect dialect : MYSQL) {
            Statement statement = parse(sql, dialect);
            roundTrip(statement.getClass(), sql, expected, dialect,
                    features(statement.getClass()));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREATE RESOURCE GROUP rg20", "CREATE RESOURCE GROUP rg20 TYPE = OTHER",
            "CREATE RESOURCE GROUP rg20 VCPU = 0 TYPE = USER",
            "CREATE RESOURCE GROUP rg20 TYPE = USER THREAD_PRIORITY = 1 VCPU = 0",
            "CREATE RESOURCE GROUP rg20 TYPE = USER ENABLE THREAD_PRIORITY = 1",
            "CREATE RESOURCE GROUP rg20 TYPE = USER ENABLE DISABLE",
            "CREATE RESOURCE GROUP rg20 TYPE = USER VCPU",
            "CREATE RESOURCE GROUP rg20 TYPE = USER VCPU = 0-",
            "CREATE RESOURCE GROUP rg20 TYPE = USER VCPU = 0-1-2",
            "CREATE RESOURCE GROUP rg20 TYPE = USER VCPU = -1",
            "CREATE RESOURCE GROUP rg20 TYPE = USER VCPU = 0,",
            "CREATE RESOURCE GROUP rg20 TYPE = USER VCPU = 0,,1",
            "CREATE RESOURCE GROUP rg20 TYPE = USER VCPU = 1.5",
            "CREATE RESOURCE GROUP rg20 TYPE = USER VCPU = 2147483648",
            "CREATE RESOURCE GROUP rg20 TYPE = USER VCPU = 1_0",
            "CREATE RESOURCE GROUP rg20 TYPE = USER VCPU = (0)",
            "CREATE RESOURCE GROUP rg20 TYPE = USER THREAD_PRIORITY = +5",
            "CREATE RESOURCE GROUP rg20 TYPE = USER THREAD_PRIORITY = 2147483648",
            "CREATE RESOURCE GROUP rg20 TYPE = USER THREAD_PRIORITY = 1.5",
            "CREATE RESOURCE GROUP rg20 TYPE = USER THREAD_PRIORITY",
            "CREATE RESOURCE GROUP rg20 TYPE = USER FORCE",
            "CREATE RESOURCE GROUP rg20 TYPE = = USER",
            "CREATE RESOURCE GROUP IF NOT EXISTS rg20 TYPE = USER",
            "CREATE RESOURCE GROUP d.rg20 TYPE = USER", "CREATE RESOURCE GROUP 'rg20' TYPE = USER",
            "CREATE RESOURCE GROUP force TYPE = USER", "CREATE RESOURCE GROUP"})
    void createRejectsWhatMySqlRejects(String sql) {
        assertRejected(sql);
    }

    // ---- ALTER RESOURCE GROUP ------------------------------------------------------------------

    @Test
    void alterKeepsOptionsAndForce() throws Exception {
        for (Dialect dialect : MYSQL) {
            AlterResourceGroup group = roundTrip(AlterResourceGroup.class,
                    "ALTER RESOURCE GROUP rg1 VCPU = 0-1 THREAD_PRIORITY = 5 DISABLE FORCE",
                    dialect);
            assertEquals("rg1", group.getName());
            assertEquals(List.of(new VcpuRange(0, 1)), group.getVcpus());
            assertEquals(5, group.getThreadPriority());
            assertEquals(State.DISABLE, group.getState());
            assertTrue(group.isForce());

            group = roundTrip(AlterResourceGroup.class, "ALTER RESOURCE GROUP rg1", dialect);
            assertTrue(group.getVcpus().isEmpty());
            assertNull(group.getThreadPriority());
            assertNull(group.getState());
            assertFalse(group.isForce());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALTER RESOURCE GROUP rg1 DISABLE FORCE",
            "ALTER RESOURCE GROUP rg1 ENABLE", "ALTER RESOURCE GROUP rg1 ENABLE FORCE",
            "ALTER RESOURCE GROUP rg1 FORCE", "ALTER RESOURCE GROUP rg1 THREAD_PRIORITY -3",
            "ALTER RESOURCE GROUP rg1 VCPU := 0 THREAD_PRIORITY := 2 FORCE",
            "ALTER RESOURCE GROUP `rg 16` VCPU = 0, 2-3"})
    void alterPrintsAsWritten(String sql) throws Exception {
        for (Dialect dialect : MYSQL) {
            roundTrip(AlterResourceGroup.class, sql, dialect);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALTER RESOURCE GROUP rg1 TYPE = USER",
            "ALTER RESOURCE GROUP rg1 FORCE DISABLE",
            "ALTER RESOURCE GROUP rg1 THREAD_PRIORITY = 5 VCPU = 0",
            "ALTER RESOURCE GROUP rg1 DISABLE VCPU = 0", "ALTER RESOURCE GROUP rg1 FORCE FORCE",
            "ALTER RESOURCE GROUP rg1, rg2 DISABLE", "ALTER RESOURCE GROUP",
            "ALTER RESOURCE GROUP IF EXISTS rg1 DISABLE", "ALTER RESOURCE GROUP force DISABLE",
            "ALTER RESOURCE GROUP rg1 VCPU = 2147483648"})
    void alterRejectsWhatMySqlRejects(String sql) {
        assertRejected(sql);
    }

    // ---- SET RESOURCE GROUP --------------------------------------------------------------------

    @Test
    void setNamesTheGroupAndTheThreads() throws Exception {
        for (Dialect dialect : MYSQL) {
            String sql = "SET RESOURCE GROUP rg1 FOR 14, 78, 4";
            SetResourceGroup set = roundTrip(SetResourceGroup.class, sql, dialect);
            assertEquals("rg1", set.getName());
            assertEquals(3, set.getThreadIds().size());
            assertEquals(78L, assertInstanceOf(LongValue.class, set.getThreadIds().get(1))
                    .getValue());
            assertSpans("78", sql, (ASTNodeAccess) set.getThreadIds().get(1));
            assertSpans("14, 78, 4", sql, set.getThreadIds());

            set = roundTrip(SetResourceGroup.class, "SET RESOURCE GROUP rg1", dialect);
            assertNull(set.getThreadIds());

            sql = "SET RESOURCE GROUP `rg 16` FOR 0x1F, X'1f', 18446744073709551615";
            set = roundTrip(SetResourceGroup.class, sql, dialect);
            assertEquals("`rg 16`", set.getName());
            assertEquals("0x1F", assertInstanceOf(HexValue.class, set.getThreadIds().get(0))
                    .getValue());
            assertSpans("X'1f'", sql, (ASTNodeAccess) set.getThreadIds().get(1));
            assertEquals("18446744073709551615",
                    ((LongValue) set.getThreadIds().get(2)).getStringValue());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"SET RESOURCE GROUP", "SET RESOURCE GROUP rg1 FOR",
            "SET RESOURCE GROUP rg1 FOR -1", "SET RESOURCE GROUP rg1 FOR 1,",
            "SET RESOURCE GROUP rg1 FOR 1.5", "SET RESOURCE GROUP rg1 FOR 18446744073709551616",
            "SET RESOURCE GROUP rg1 FOR 0X1F", "SET RESOURCE GROUP rg1 FOR X'1'",
            "SET RESOURCE GROUP rg1 FOR '14'", "SET RESOURCE GROUP rg1 FOR (14)",
            "SET RESOURCE GROUP rg1 FOR @id", "SET RESOURCE GROUP rg1 FOR 1_0",
            "SET RESOURCE GROUP rg1, rg2", "SET RESOURCE GROUP = rg1",
            "SET RESOURCE GROUP rg1 FORCE", "SET RESOURCE GROUP force",
            "SET GLOBAL RESOURCE GROUP rg1"})
    void setRejectsWhatMySqlRejects(String sql) {
        assertRejected(sql);
    }

    @Test
    void otherSetFormsKeepTheirReading() throws Exception {
        for (Dialect dialect : new Dialect[] {Dialect.MYSQL, null, Dialect.POSTGRESQL}) {
            for (String sql : List.of("SET resource = 1", "SET resource.grp = 1",
                    "SET @resource = 1", "SET autocommit = 0")) {
                assertEquals(sql, assertInstanceOf(SetStatement.class, parse(sql, dialect))
                        .toString());
            }
        }
    }

    // ---- DROP RESOURCE GROUP -------------------------------------------------------------------

    @Test
    void dropIsADropOfAResourceGroup() throws Exception {
        for (Dialect dialect : MYSQL) {
            Drop drop = roundTrip(Drop.class, "DROP RESOURCE GROUP rg1 FORCE", dialect);
            assertEquals(Drop.ObjectType.RESOURCE_GROUP, drop.getObjectType());
            assertEquals("RESOURCE GROUP", drop.getType());
            assertEquals("rg1", drop.getName().getName());
            assertEquals(1, drop.getNames().size());
            assertTrue(drop.isForce());
            assertNull(drop.getParameters());

            drop = roundTrip(Drop.class, "DROP RESOURCE GROUP rg2", dialect);
            assertEquals(Drop.ObjectType.RESOURCE_GROUP, drop.getObjectType());
            assertFalse(drop.isForce());

            drop = roundTrip(Drop.class, "DROP RESOURCE GROUP `rg 16`", dialect);
            assertEquals("`rg 16`", drop.getName().getName());
            roundTrip(Drop.class, "drop resource group rg1 force", "DROP RESOURCE GROUP rg1 FORCE",
                    dialect, features(Drop.class));
        }
        assertEquals("DROP RESOURCE GROUP rg1 FORCE",
                new Drop().withObjectType(Drop.ObjectType.RESOURCE_GROUP)
                        .withName(new net.sf.jsqlparser.schema.Table("rg1")).withForce(true)
                        .toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"DROP RESOURCE GROUP IF EXISTS rg3", "DROP RESOURCE GROUP rg3, rg4",
            "DROP RESOURCE GROUP rg3 FORCE FORCE", "DROP RESOURCE GROUP rg3 CASCADE",
            "DROP RESOURCE GROUP rg3 RESTRICT", "DROP RESOURCE GROUP d.rg3",
            "DROP RESOURCE GROUP 'rg3'", "DROP RESOURCE GROUP rg3 extra",
            "DROP RESOURCE GROUP rg3 ON t", "DROP RESOURCE GROUP FORCE",
            "DROP TEMPORARY RESOURCE GROUP rg3", "DROP MATERIALIZED RESOURCE GROUP rg3"})
    void dropRejectsWhatMySqlRejects(String sql) {
        assertRejected(sql);
    }

    @Test
    void dropWithoutANameIsRejectedByMySql() throws Exception {
        assertThrows(JSQLParserException.class, () -> parse("DROP RESOURCE GROUP", Dialect.MYSQL));
        // without a dialect it keeps dropping an object called GROUP
        Drop drop = assertInstanceOf(Drop.class, parse("DROP RESOURCE GROUP", null));
        assertEquals(Drop.ObjectType.OTHER, drop.getObjectType());
        assertEquals("RESOURCE", drop.getType());
    }

    // ---- other dialects ------------------------------------------------------------------------

    @Test
    void otherDialectsKeepTheirReading() throws Exception {
        for (Dialect dialect : OTHERS) {
            Drop drop = assertInstanceOf(Drop.class, parse("DROP RESOURCE GROUP rg1", dialect));
            assertEquals(Drop.ObjectType.OTHER, drop.getObjectType());
            assertEquals("RESOURCE", drop.getType());
            assertEquals("GROUP", drop.getName().getName());
            assertEquals(List.of("rg1"), drop.getParameters());
            assertThrows(JSQLParserException.class,
                    () -> parse("DROP RESOURCE GROUP rg1 FORCE", dialect));
            assertThrows(JSQLParserException.class,
                    () -> parse("SET RESOURCE GROUP rg1", dialect));
            assertThrows(JSQLParserException.class,
                    () -> parse("SET RESOURCE GROUP rg1 FOR 14", dialect));
            assertInstanceOf(UnsupportedStatement.class,
                    parse("CREATE RESOURCE GROUP rg1 TYPE = USER", dialect));
            assertInstanceOf(UnsupportedStatement.class,
                    parse("ALTER RESOURCE GROUP rg1 DISABLE FORCE", dialect));
        }
    }

    @Test
    void theWordsRemainIdentifiers() throws Exception {
        for (Dialect dialect : MYSQL) {
            for (String sql : List.of("SELECT resource, vcpu, thread_priority FROM resource",
                    "CREATE TABLE resource (vcpu INT, thread_priority INT)",
                    "ALTER TABLE resource ADD COLUMN vcpu INT", "DROP TABLE resource",
                    "INSERT INTO resource (vcpu) VALUES (1)")) {
                assertEquals(sql, parse(sql, dialect).toString());
            }
        }
    }

    // ---- statement boundaries, visitors and validation -----------------------------------------

    static Stream<Arguments> statements() {
        return Stream.of(
                Arguments.of("CREATE RESOURCE GROUP rg1 TYPE = USER VCPU = 0-3, 5 ENABLE",
                        CreateResourceGroup.class),
                Arguments.of("CREATE RESOURCE GROUP rg1 TYPE = SYSTEM", CreateResourceGroup.class),
                Arguments.of("ALTER RESOURCE GROUP rg1 THREAD_PRIORITY = 5 DISABLE FORCE",
                        AlterResourceGroup.class),
                Arguments.of("ALTER RESOURCE GROUP rg1", AlterResourceGroup.class),
                Arguments.of("SET RESOURCE GROUP rg1 FOR 14, 78", SetResourceGroup.class),
                Arguments.of("SET RESOURCE GROUP rg1", SetResourceGroup.class),
                Arguments.of("DROP RESOURCE GROUP rg1 FORCE", Drop.class),
                Arguments.of("DROP RESOURCE GROUP rg1", Drop.class));
    }

    @ParameterizedTest
    @MethodSource("statements")
    void statementsEndAtTheStatementBoundary(String sql, Class<? extends Statement> type)
            throws Exception {
        // the statement on its own line is closed by the next statement start
        String script = "SELECT 1;\nCOMMIT\n" + sql + "\nSELECT 2;" + sql + ";\nSAVEPOINT sp";
        for (Dialect dialect : MYSQL) {
            for (boolean opaque : new boolean[] {false, true}) {
                Statements statements = CCJSqlParserUtil.parseStatements(script, p -> {
                    if (dialect != null) {
                        p.withDialect(dialect);
                    }
                    p.withUnsupportedStatements(opaque);
                });
                assertEquals(6, statements.size(), script);
                assertEquals("COMMIT", statements.get(1).toString());
                assertInstanceOf(type, statements.get(2));
                assertEquals(sql, statements.get(2).toString());
                assertSpans(sql, script, (ASTNodeAccess) statements.get(2));
                assertEquals("SELECT 2", statements.get(3).toString());
                assertEquals(sql, statements.get(4).toString());
                assertEquals("SAVEPOINT sp", statements.get(5).toString());
            }
        }
    }

    @Test
    void forceInstallStartsTheNextStatement() throws Exception {
        Statements statements = CCJSqlParserUtil.parseStatements(
                "SELECT 1; ALTER RESOURCE GROUP rg1 DISABLE\nFORCE INSTALL httpfs");
        assertEquals(3, statements.size());
        assertFalse(((AlterResourceGroup) statements.get(1)).isForce());
    }

    @Test
    void scriptsKeepEachStatement() throws Exception {
        String script = "CREATE RESOURCE GROUP rg1 TYPE = USER VCPU = 0-3 THREAD_PRIORITY = 10;"
                + " SET RESOURCE GROUP rg1; INSERT INTO t VALUES (1);"
                + " ALTER RESOURCE GROUP rg1 DISABLE FORCE; DROP RESOURCE GROUP rg1 FORCE";
        Statements statements =
                CCJSqlParserUtil.parseStatements(script, p -> p.withDialect(Dialect.MYSQL));
        assertEquals(5, statements.size());
        assertEquals(Set.of("t"), new TablesNamesFinder<>().getTables(statements.get(2)));
        assertInstanceOf(AlterResourceGroup.class, statements.get(3));
        assertEquals(statements.toString(), CCJSqlParserUtil
                .parseStatements(statements.toString(), p -> p.withDialect(Dialect.MYSQL))
                .toString());
    }

    @Test
    void visitorDispatchPassesContextAndResult() throws Exception {
        StatementVisitorAdapter<String> visitor = new StatementVisitorAdapter<>() {
            @Override
            public <S> String visit(CreateResourceGroup statement, S context) {
                return context + ":create:" + statement.getType();
            }

            @Override
            public <S> String visit(AlterResourceGroup statement, S context) {
                return context + ":alter:" + statement.getState();
            }

            @Override
            public <S> String visit(SetResourceGroup statement, S context) {
                return context + ":set:" + statement.getThreadIds();
            }
        };
        String create = "CREATE RESOURCE GROUP rg TYPE = SYSTEM";
        String alter = "ALTER RESOURCE GROUP rg ENABLE";
        String set = "SET RESOURCE GROUP rg FOR 1, 2";
        assertEquals("ctx:create:SYSTEM", parse(create, null).accept(visitor, "ctx"));
        assertEquals("ctx:alter:ENABLE", parse(alter, null).accept(visitor, "ctx"));
        assertEquals("ctx:set:1, 2", parse(set, null).accept(visitor, "ctx"));
        for (String sql : List.of(create, alter, set)) {
            assertNull(parse(sql, null).accept(new StatementVisitorAdapter<>(), null));
            Statement statement = parse(sql, null);
            statement.accept(new StatementVisitorAdapter<Void>());
        }
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
    void validationKnowsMySqlTakesResourceGroups() {
        String script = "CREATE RESOURCE GROUP rg TYPE = USER; ALTER RESOURCE GROUP rg DISABLE;"
                + " SET RESOURCE GROUP rg; DROP RESOURCE GROUP rg FORCE";
        assertTrue(isValid(Dialect.MYSQL, script, MySqlVersion.V8_0));
        assertTrue(isValid(null, script, MySqlVersion.V8_0));
        assertTrue(isValid(null, script, new FeaturesAllowed(Feature.createResourceGroup,
                Feature.alterResourceGroup, Feature.setResourceGroup, Feature.drop)));
        for (String sql : script.split("; ")) {
            if (!sql.startsWith("DROP")) {
                assertFalse(isValid(null, sql, MariaDbVersion.V10_5_4), sql);
                assertFalse(isValid(null, sql, PostgresqlVersion.V14), sql);
            }
        }
        assertFalse(isValid(null, "SET RESOURCE GROUP rg", new FeaturesAllowed(Feature.set)));
        assertFalse(isValid(null, "CREATE RESOURCE GROUP rg TYPE = USER",
                new FeaturesAllowed(Feature.alterResourceGroup)));
        assertFalse(isValid(null, "ALTER RESOURCE GROUP rg ENABLE",
                new FeaturesAllowed(Feature.createResourceGroup)));
    }

    // ---- the model -----------------------------------------------------------------------------

    @Test
    void statementsAreBuiltWithoutTheParser() {
        CreateResourceGroup create = new CreateResourceGroup("rg", CreateResourceGroup.Type.USER)
                .withTypeAssignment(OptionAssignment.NONE)
                .withVcpus(new java.util.ArrayList<>(List.of(new VcpuRange(0, 3))))
                .addVcpuRanges(List.of(new VcpuRange(5))).withThreadPriority(-3)
                .withState(State.DISABLE);
        create.setVcpuAssignment(OptionAssignment.NONE);
        create.setThreadPriorityAssignment(OptionAssignment.COLON_EQUALS);
        assertEquals("CREATE RESOURCE GROUP rg TYPE USER VCPU 0-3, 5 THREAD_PRIORITY := -3 DISABLE",
                create.toString());
        create.setVcpus(null);
        create.addVcpus(new VcpuRange(1));
        assertEquals(List.of(new VcpuRange(1)), create.getVcpus());

        AlterResourceGroup alter = new AlterResourceGroup("rg")
                .withVcpus(new java.util.ArrayList<>()).addVcpuRanges(List.of(new VcpuRange(2)))
                .withThreadPriority(1).withState(State.ENABLE).withForce(true);
        assertEquals("ALTER RESOURCE GROUP rg VCPU = 2 THREAD_PRIORITY = 1 ENABLE FORCE",
                alter.toString());

        SetResourceGroup set = new SetResourceGroup("rg").addThreadIds(new LongValue(1))
                .addThreadIds(new HexValue("0x1F"));
        assertEquals("SET RESOURCE GROUP rg FOR 1, 0x1F", set.toString());
        set.withThreadIds(new ExpressionList<>(new LongValue(7), new LongValue(8)));
        assertEquals("SET RESOURCE GROUP rg FOR 7, 8", set.toString());
        set.setThreadIds(new ExpressionList<>());
        assertEquals("SET RESOURCE GROUP rg", set.toString());
        set.addThreadIds(new LongValue(1), new HexValue("0x1F"));
        assertEquals("SET RESOURCE GROUP rg FOR 1, 0x1F", set.toString());
        set.setThreadIds(null);
        assertEquals("SET RESOURCE GROUP rg", set.toString());

        assertThrows(NullPointerException.class, () -> new CreateResourceGroup("rg", null));
        assertThrows(NullPointerException.class, () -> new AlterResourceGroup(null));
        assertThrows(NullPointerException.class, () -> new SetResourceGroup(null));
    }

    @Test
    void vcpuRangesCompareByTheirNumbers() {
        VcpuRange range = new VcpuRange(0, 3);
        assertEquals("0-3", range.toString());
        assertEquals(range, range);
        assertEquals(new VcpuRange(0, 3), range);
        assertEquals(new VcpuRange(0, 3).hashCode(), range.hashCode());
        assertNotEquals(new VcpuRange(0), range);
        assertNotEquals(new VcpuRange(1, 3), range);
        assertNotEquals(new VcpuRange(0, 0), new VcpuRange(0));
        assertNotEquals("0-3", range);
        range.setStart(2);
        range.setEnd(null);
        assertEquals("2", range.toString());
        assertThrows(IllegalArgumentException.class, () -> new VcpuRange(-1));
        assertThrows(IllegalArgumentException.class, () -> new VcpuRange(0, -1));
    }
}

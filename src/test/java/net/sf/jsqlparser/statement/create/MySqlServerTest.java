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

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.HexValue;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.ASTNodeAccess;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.Node;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.parser.feature.FeatureConfiguration;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.StmtFeature;
import net.sf.jsqlparser.statement.alter.AlterServer;
import net.sf.jsqlparser.statement.alter.ForeignObjectAlterAction;
import net.sf.jsqlparser.statement.create.server.CreateServer;
import net.sf.jsqlparser.statement.create.table.ForeignDataOption;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.foreign.ForeignDataStatement;
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
import org.junit.jupiter.params.provider.ValueSource;

/** MySQL CREATE SERVER and ALTER SERVER: numeric option values and quoted names. */
class MySqlServerTest {
    private static final Dialect[] MYSQL_FAMILY = {Dialect.MYSQL, Dialect.MARIADB, null};

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
     * Parses {@code sql}, which must print as written, and checks the statement span, the deparser,
     * the reparse, the feature analysis and the table finder.
     */
    private static ForeignDataStatement roundTrip(String sql, Dialect dialect) throws Exception {
        ForeignDataStatement statement =
                assertInstanceOf(ForeignDataStatement.class, parse(sql, dialect));
        assertEquals(sql, statement.toString());
        assertSpans(sql, sql, statement);
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(output), null);
        assertEquals(sql, output.toString());
        assertEquals(sql, parse(output.toString(), dialect).toString());
        assertEquals(Set.of(StmtFeature.MODIFIES_SCHEMA), statement.getFeatures().getCertain());
        assertTrue(new TablesNamesFinder<>().getTables((Statement) statement).isEmpty());
        return statement;
    }

    @Test
    void createServerTakesANumericPort() throws Exception {
        String sql = "CREATE SERVER s FOREIGN DATA WRAPPER mysql"
                + " OPTIONS (USER 'u', HOST 'h', DATABASE 'd', PORT 3306)";
        for (Dialect dialect : MYSQL_FAMILY) {
            CreateServer server = (CreateServer) roundTrip(sql, dialect);
            assertEquals("s", server.getName());
            assertEquals("mysql", server.getForeignDataWrapper());
            List<ForeignDataOption> options = server.getOptions();
            assertEquals(4, options.size());
            assertEquals("USER", options.get(0).getName());
            assertEquals("u", options.get(0).getValue().getValue());
            assertSame(options.get(0).getValue(), options.get(0).getValueExpression());
            ForeignDataOption port = options.get(3);
            assertEquals("PORT", port.getName());
            assertNull(port.getAction());
            assertNull(port.getValue());
            LongValue number = assertInstanceOf(LongValue.class, port.getValueExpression());
            assertEquals(3306L, number.getValue());
            assertSpans("3306", sql, number);
            assertEquals("PORT 3306", port.toString());
        }
    }

    @Test
    void alterServerTakesANumericPort() throws Exception {
        String sql = "ALTER SERVER s OPTIONS (USER 'u2', PORT 3307)";
        for (Dialect dialect : MYSQL_FAMILY) {
            AlterServer server = (AlterServer) roundTrip(sql, dialect);
            assertEquals("s", server.getName());
            assertEquals(ForeignObjectAlterAction.OPTIONS, server.getAction());
            assertEquals(3307L, assertInstanceOf(LongValue.class,
                    server.getOptions().get(1).getValueExpression()).getValue());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE SERVER s2 FOREIGN DATA WRAPPER mysql OPTIONS (HOST 'h', DATABASE 'db', USER 'u', PASSWORD 'p', PORT 3306, SOCKET '/tmp/mysql.sock', OWNER 'o')",
            "CREATE SERVER `s-1` FOREIGN DATA WRAPPER 'mysql' OPTIONS (USER 'u')",
            "CREATE SERVER 's3' FOREIGN DATA WRAPPER mysql OPTIONS (USER 'u')",
            "CREATE SERVER s4 FOREIGN DATA WRAPPER `mysql` OPTIONS (user 'u', port 1)",
            "CREATE SERVER s5 FOREIGN DATA WRAPPER mysql OPTIONS (USER 'a', USER 'b')",
            "CREATE SERVER s6 FOREIGN DATA WRAPPER mysql OPTIONS (PORT 3306.5)",
            "CREATE SERVER s7 FOREIGN DATA WRAPPER mysql OPTIONS (PORT 1e3)",
            "CREATE SERVER s8 FOREIGN DATA WRAPPER mysql OPTIONS (PORT 0x10)",
            "CREATE SERVER s9 FOREIGN DATA WRAPPER mysql OPTIONS (PORT X'10')",
            "CREATE SERVER s10 FOREIGN DATA WRAPPER mysql OPTIONS (PORT 99999999999999999999)",
            "CREATE SERVER s11 FOREIGN DATA WRAPPER mysql OPTIONS (USER \"u\")",
            "CREATE SERVER s12 FOREIGN DATA WRAPPER \"mysql\" OPTIONS (PORT .5)",
            "CREATE SERVER \"s13\" FOREIGN DATA WRAPPER mysql OPTIONS (USER 'u')",
            "CREATE SERVER '' FOREIGN DATA WRAPPER mysql OPTIONS (USER 'u')",
            "ALTER SERVER s OPTIONS (HOST '10.0.0.2', PORT 3307)",
            "ALTER SERVER 's3' OPTIONS (SOCKET '/tmp/x')"})
    void mySqlServerFormsRoundTrip(String sql) throws Exception {
        roundTrip(sql, Dialect.MYSQL);
    }

    @Test
    void numericValuesKeepTheirLiteralKind() throws Exception {
        String sql = "CREATE SERVER s FOREIGN DATA WRAPPER mysql"
                + " OPTIONS (PORT 3306.5, PORT 0x10, PORT X'10')";
        List<ForeignDataOption> options = roundTrip(sql, Dialect.MYSQL).getOptions();
        assertSpans("3306.5", sql,
                assertInstanceOf(DoubleValue.class, options.get(0).getValueExpression()));
        assertSpans("0x10", sql,
                assertInstanceOf(HexValue.class, options.get(1).getValueExpression()));
        assertSpans("X'10'", sql,
                assertInstanceOf(HexValue.class, options.get(2).getValueExpression()));
        StringBuilder seen = new StringBuilder();
        ForeignDataOption.visitExpressions(options, expression -> seen.append(expression));
        assertEquals("3306.50x10X'10'", seen.toString());
    }

    @Test
    void quotedNamesAreKeptAsWritten() throws Exception {
        for (Dialect dialect : MYSQL_FAMILY) {
            CreateServer server = (CreateServer) roundTrip(
                    "CREATE SERVER 's' FOREIGN DATA WRAPPER 'mysql' OPTIONS (PORT 3306)", dialect);
            assertEquals("'s'", server.getName());
            assertEquals("'mysql'", server.getForeignDataWrapper());
            assertEquals("'s'", ((AlterServer) roundTrip("ALTER SERVER 's' OPTIONS (PORT 1)",
                    dialect)).getName());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREATE SERVER r1 FOREIGN DATA WRAPPER mysql",
            "CREATE SERVER r2 FOREIGN DATA WRAPPER mysql OPTIONS ()",
            "CREATE SERVER r3 FOREIGN DATA WRAPPER mysql OPTIONS (PORT '3306')",
            "CREATE SERVER r4 FOREIGN DATA WRAPPER mysql OPTIONS (PORT -1)",
            "CREATE SERVER r5 FOREIGN DATA WRAPPER mysql OPTIONS (USER 1)",
            "CREATE SERVER r6 FOREIGN DATA WRAPPER mysql OPTIONS (USER u)",
            "CREATE SERVER r7 FOREIGN DATA WRAPPER mysql OPTIONS (USER N'u')",
            "CREATE SERVER r8 FOREIGN DATA WRAPPER mysql OPTIONS (USER _utf8mb4'u')",
            "CREATE SERVER r9 FOREIGN DATA WRAPPER mysql OPTIONS (dbname 'd')",
            "CREATE SERVER r10 FOREIGN DATA WRAPPER mysql OPTIONS (`USER` 'u')",
            "CREATE SERVER r11 FOREIGN DATA WRAPPER mysql OPTIONS (ADD USER 'u')",
            "CREATE SERVER IF NOT EXISTS r12 FOREIGN DATA WRAPPER mysql OPTIONS (USER 'u')",
            "CREATE SERVER r13 TYPE 't' FOREIGN DATA WRAPPER mysql OPTIONS (USER 'u')",
            "CREATE SERVER r14 VERSION '1' FOREIGN DATA WRAPPER mysql OPTIONS (USER 'u')",
            "CREATE SERVER r15 FOREIGN DATA WRAPPER mysql OPTIONS (USER 'u',)",
            "CREATE SERVER r16 FOREIGN DATA WRAPPER mysql OPTIONS (USER 'u' PORT 1)",
            "CREATE SERVER db.r17 FOREIGN DATA WRAPPER mysql OPTIONS (USER 'u')",
            "CREATE OR REPLACE SERVER r18 FOREIGN DATA WRAPPER mysql OPTIONS (USER 'u')",
            "CREATE SERVER r19 FOREIGN DATA WRAPPER N'mysql' OPTIONS (USER 'u')",
            "CREATE SERVER r20 FOREIGN DATA WRAPPER mysql OPTIONS (PORT 0X10)",
            "CREATE SERVER r21 FOREIGN DATA WRAPPER mysql OPTIONS (PORT x'1')",
            "CREATE SERVER r22 FOREIGN DATA WRAPPER mysql OPTIONS (PORT 1_0)",
            "CREATE SERVER r23 FOREIGN DATA WRAPPER mysql OPTIONS (PORT +1)",
            "CREATE SERVER r24 FOREIGN DATA WRAPPER mysql OPTIONS (USER 'u') COMMENT 'c'",
            "ALTER SERVER s", "ALTER SERVER s OPTIONS ()", "ALTER SERVER s OPTIONS (SET USER 'u')",
            "ALTER SERVER s OPTIONS (DROP USER)", "ALTER SERVER s OPTIONS (PORT '1')",
            "ALTER SERVER s VERSION '2'", "ALTER SERVER s VERSION '2' OPTIONS (USER 'u')",
            "ALTER SERVER s RENAME TO t", "ALTER SERVER s OWNER TO o"})
    void mySqlRejectsWhatTheServerRejects(String sql) {
        assertThrows(JSQLParserException.class, () -> parse(sql, Dialect.MYSQL), sql);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE SERVER IF NOT EXISTS s TYPE 't' VERSION '1' FOREIGN DATA WRAPPER w OPTIONS (host 'h', dbname 'd')",
            "CREATE SERVER s FOREIGN DATA WRAPPER w",
            "ALTER SERVER s VERSION '2' OPTIONS (SET host 'h', ADD port '5432', DROP dbname)",
            "ALTER SERVER s RENAME TO t", "ALTER SERVER s OWNER TO o"})
    void postgreSqlFormsKeepTheirReading(String sql) throws Exception {
        for (Dialect dialect : new Dialect[] {Dialect.POSTGRESQL, null}) {
            ForeignDataStatement statement = roundTrip(sql, dialect);
            if (statement.getOptions() != null) {
                for (ForeignDataOption option : statement.getOptions()) {
                    assertSame(option.getValue(), option.getValueExpression());
                }
            }
        }
    }

    @Test
    void mariaDbAndNoDialectKeepTheGenericOptions() throws Exception {
        roundTrip("CREATE SERVER IF NOT EXISTS 's' FOREIGN DATA WRAPPER mysql"
                + " OPTIONS (USER 'u', PORT 3306)", Dialect.MARIADB);
        roundTrip("ALTER SERVER s OPTIONS (PORT 3307)", Dialect.MARIADB);
        AlterServer server = (AlterServer) roundTrip(
                "ALTER SERVER s VERSION '2' OPTIONS (SET port 5432, ADD host 'h', DROP dbname)",
                null);
        assertEquals(ForeignDataOption.Action.SET, server.getOptions().get(0).getAction());
        assertEquals(5432L, assertInstanceOf(LongValue.class,
                server.getOptions().get(0).getValueExpression()).getValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREATE SERVER s FOREIGN DATA WRAPPER w OPTIONS (port 5432)",
            "ALTER SERVER s OPTIONS (SET port 5432)", "CREATE SERVER 's' FOREIGN DATA WRAPPER w",
            "CREATE SERVER s FOREIGN DATA WRAPPER 'w'", "ALTER SERVER 's' OPTIONS (SET host 'h')",
            "CREATE USER MAPPING FOR u SERVER s OPTIONS (port 5432)"})
    void postgreSqlKeepsRejectingNumbersAndQuotedNames(String sql) {
        assertThrows(JSQLParserException.class, () -> parse(sql, Dialect.POSTGRESQL), sql);
        assertThrows(JSQLParserException.class, () -> parse(sql, Dialect.ORACLE), sql);
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREATE USER MAPPING FOR u SERVER s OPTIONS (port 5432)",
            "ALTER USER MAPPING FOR u SERVER s OPTIONS (SET port 5432)",
            "CREATE FOREIGN TABLE t (a INT) SERVER s OPTIONS (port 5432)",
            "ALTER FOREIGN TABLE t OPTIONS (ADD port 5432)",
            "CREATE FOREIGN DATA WRAPPER w OPTIONS (port 5432)",
            "CREATE SERVER s FOREIGN DATA WRAPPER w OPTIONS (port -1)",
            "CREATE SERVER s FOREIGN DATA WRAPPER w OPTIONS (port 1 + 1)",
            "CREATE SERVER s FOREIGN DATA WRAPPER w OPTIONS (port)",
            "CREATE SERVER N's' FOREIGN DATA WRAPPER w"})
    void otherOptionListsKeepStringValues(String sql) {
        for (Dialect dialect : MYSQL_FAMILY) {
            assertThrows(JSQLParserException.class, () -> parse(sql, dialect), sql);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"DROP SERVER IF EXISTS s", "DROP SERVER s2",
            "DROP SERVER IF EXISTS 's3'", "DROP SERVER `s-1`"})
    void dropServerKeepsTheGenericDrop(String sql) throws Exception {
        for (Dialect dialect : MYSQL_FAMILY) {
            Drop drop = assertInstanceOf(Drop.class, parse(sql, dialect));
            assertEquals("SERVER", drop.getType());
            assertEquals(sql, drop.toString());
        }
    }

    @Test
    void scriptsKeepEachStatement() throws Exception {
        String script = "CREATE SERVER s FOREIGN DATA WRAPPER mysql OPTIONS (HOST 'h', PORT 3306);"
                + " ALTER SERVER s OPTIONS (PORT 3307); SELECT * FROM t; DROP SERVER IF EXISTS s";
        for (boolean opaque : new boolean[] {false, true}) {
            Statements statements = CCJSqlParserUtil.parseStatements(script,
                    p -> p.withDialect(Dialect.MYSQL).withUnsupportedStatements(opaque));
            assertEquals(4, statements.size());
            assertInstanceOf(CreateServer.class, statements.get(0));
            assertInstanceOf(AlterServer.class, statements.get(1));
            assertEquals(Set.of("t"), new TablesNamesFinder<>().getTables(statements.get(2)));
            assertInstanceOf(Drop.class, statements.get(3));
            assertEquals(statements.toString(), CCJSqlParserUtil
                    .parseStatements(statements.toString(), p -> p.withDialect(Dialect.MYSQL))
                    .toString());
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
    void validationKnowsTheServerStatements() {
        String sql = "CREATE SERVER s FOREIGN DATA WRAPPER mysql OPTIONS (PORT 3306);"
                + " ALTER SERVER s OPTIONS (PORT 3307)";
        assertTrue(isValid(Dialect.MYSQL, sql, MySqlVersion.V8_0));
        assertTrue(isValid(Dialect.MARIADB, sql, MariaDbVersion.V10_5_4));
        assertTrue(isValid(null, sql, PostgresqlVersion.V14));
        assertTrue(isValid(null, sql,
                new FeaturesAllowed(Feature.createServer, Feature.alterServer)));
        assertFalse(isValid(null, sql, new FeaturesAllowed(Feature.createServer)));
    }

    @Test
    void optionValueAccessorsShareOneValue() {
        ForeignDataOption option = new ForeignDataOption("PORT", null);
        assertNull(option.getValueExpression());
        assertEquals("PORT", option.toString());
        assertSame(option, option.withValueExpression(new LongValue(3306)));
        assertNull(option.getValue());
        assertEquals("PORT 3306", option.toString());
        StringValue text = new StringValue("'3306'");
        option.setValue(text);
        assertSame(text, option.getValue());
        assertSame(text, option.getValueExpression());
        option.setValueExpression(new LongValue(1));
        option.setAction(ForeignDataOption.Action.DROP);
        assertNull(option.getValueExpression());
        assertEquals("DROP PORT", option.toString());
    }
}

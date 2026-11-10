/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.foreign;

import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.schema.TableReference;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementFeatures;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.StmtFeature;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.table.ForeignDataOption;
import net.sf.jsqlparser.statement.foreign.ImportForeignSchema.Qualification;
import net.sf.jsqlparser.statement.imprt.Import;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import net.sf.jsqlparser.util.validation.feature.MySqlVersion;
import net.sf.jsqlparser.util.validation.feature.PostgresqlVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ImportForeignSchemaTest {

    private static final Consumer<CCJSqlParser> POSTGRESQL =
            parser -> parser.withDialect(Dialect.POSTGRESQL);
    private static final List<Consumer<CCJSqlParser>> POSTGRESQL_AND_NO_DIALECT =
            Arrays.asList(POSTGRESQL, parser -> {
            });

    private static List<String> names(ImportForeignSchema statement) {
        return statement.getTables().stream().map(TableReference::toString)
                .collect(Collectors.toList());
    }

    @ParameterizedTest
    @ValueSource(strings = {"IMPORT FOREIGN SCHEMA remote FROM SERVER srv INTO local",
            "IMPORT FOREIGN SCHEMA remote LIMIT TO (t1, t2) FROM SERVER srv INTO local "
                    + "OPTIONS (import_default 'true')",
            "IMPORT FOREIGN SCHEMA remote EXCEPT (t3) FROM SERVER srv INTO local",
            "IMPORT FOREIGN SCHEMA remote LIMIT TO (t1) FROM SERVER srv INTO local",
            "IMPORT FOREIGN SCHEMA remote EXCEPT (t1, t2, t3) FROM SERVER srv INTO local "
                    + "OPTIONS (import_collate 'false', import_not_null 'true')",
            "IMPORT FOREIGN SCHEMA remote FROM SERVER srv INTO local "
                    + "OPTIONS (import_default 'true', fetch_size '100')",
            "IMPORT FOREIGN SCHEMA \"Remote Schema\" LIMIT TO (\"T 1\", t2) "
                    + "FROM SERVER \"My Srv\" INTO \"Local Schema\"",
            "IMPORT FOREIGN SCHEMA remote LIMIT TO (s.t1, d.s.t2) FROM SERVER srv INTO local",
            "IMPORT FOREIGN SCHEMA remote LIMIT TO (ONLY t1, t2 *) FROM SERVER srv INTO local",
            "IMPORT FOREIGN SCHEMA remote EXCEPT (ONLY (t1)) FROM SERVER srv INTO local",
            "IMPORT FOREIGN SCHEMA remote FROM SERVER srv INTO local "
                    + "OPTIONS (\"Import Default\" 'true')",
            "IMPORT FOREIGN SCHEMA remote FROM SERVER srv INTO local "
                    + "OPTIONS (import_default E'tr\\165e')",
            "IMPORT FOREIGN SCHEMA remote FROM SERVER srv INTO local "
                    + "OPTIONS (import_default $$true$$)",
            "IMPORT FOREIGN SCHEMA remote FROM SERVER srv INTO local OPTIONS (user 'x')",
            "IMPORT FOREIGN SCHEMA server FROM SERVER server INTO server",
            "IMPORT FOREIGN SCHEMA options FROM SERVER options INTO options OPTIONS (options 'x')",
            "IMPORT FOREIGN SCHEMA import FROM SERVER srv INTO schema"})
    void parsesAndDeparsesWithPostgreSqlAndWithoutDialect(String sql)
            throws JSQLParserException {
        for (Consumer<CCJSqlParser> mode : POSTGRESQL_AND_NO_DIALECT) {
            Statement statement = assertSqlCanBeParsedAndDeparsed(sql, false, mode);
            assertInstanceOf(ImportForeignSchema.class, statement);
            assertEquals(sql, statement.toString());
            StringBuilder deparsed = new StringBuilder();
            statement.accept(new StatementDeParser(deparsed));
            assertEquals(sql, deparsed.toString());
        }
    }

    @Test
    void keepsEveryClause() throws JSQLParserException {
        ImportForeignSchema statement = (ImportForeignSchema) CCJSqlParserUtil.parse(
                "import foreign schema remote limit to (t1, s.t2) from server srv into local "
                        + "options (import_default 'true', import_collate 'false')",
                POSTGRESQL);
        assertEquals("remote", statement.getRemoteSchema());
        assertEquals(Qualification.LIMIT_TO, statement.getQualification());
        assertEquals(List.of("t1", "s.t2"), names(statement));
        assertEquals("s", statement.getTables().get(1).getTable().getSchemaName());
        assertEquals("srv", statement.getServer());
        assertEquals("local", statement.getLocalSchema());
        assertEquals(2, statement.getOptions().size());
        ForeignDataOption option = statement.getOptions().get(0);
        assertNull(option.getAction());
        assertEquals("import_default", option.getName());
        assertEquals("true", option.getValue().getValue());
        assertEquals("IMPORT FOREIGN SCHEMA remote LIMIT TO (t1, s.t2) FROM SERVER srv INTO local "
                + "OPTIONS (import_default 'true', import_collate 'false')", statement.toString());

        ImportForeignSchema except = (ImportForeignSchema) CCJSqlParserUtil.parse(
                "IMPORT FOREIGN SCHEMA remote EXCEPT (ONLY (t1), t2 *, ONLY t3) "
                        + "FROM SERVER srv INTO local",
                POSTGRESQL);
        assertEquals(Qualification.EXCEPT, except.getQualification());
        assertEquals(List.of("ONLY (t1)", "t2 *", "ONLY t3"), names(except));
        assertTrue(except.getTables().get(0).isParenthesized());
        assertTrue(except.getTables().get(1).isIncludeDescendants());
        assertTrue(except.getTables().get(2).isOnly());
        assertNull(except.getOptions());

        ImportForeignSchema whole = (ImportForeignSchema) CCJSqlParserUtil.parse(
                "IMPORT FOREIGN SCHEMA \"Remote\" FROM SERVER \"Srv\" INTO \"Local\"");
        assertEquals("\"Remote\"", whole.getRemoteSchema());
        assertEquals("\"Srv\"", whole.getServer());
        assertEquals("\"Local\"", whole.getLocalSchema());
        assertNull(whole.getQualification());
        assertThat(whole.getTables()).isEmpty();
    }

    @Test
    void buildsAndDeparses() {
        ImportForeignSchema statement = new ImportForeignSchema().withRemoteSchema("remote")
                .withServer("srv").withLocalSchema("local");
        assertDeparse(statement, "IMPORT FOREIGN SCHEMA remote FROM SERVER srv INTO local");

        List<TableReference> tables = new ArrayList<>();
        tables.add(new TableReference(new Table("t1")));
        statement.withTables(Qualification.EXCEPT, tables);
        tables.add(new TableReference(new Table("ignored")));
        assertDeparse(statement,
                "IMPORT FOREIGN SCHEMA remote EXCEPT (t1) FROM SERVER srv INTO local");
        assertThrows(UnsupportedOperationException.class,
                () -> statement.getTables().add(new TableReference(new Table("t2"))));

        statement.setTables(Qualification.LIMIT_TO, tables);
        List<ForeignDataOption> options = new ArrayList<>();
        options.add(new ForeignDataOption("import_default", new StringValue("true")));
        statement.setOptions(options);
        assertDeparse(statement, "IMPORT FOREIGN SCHEMA remote LIMIT TO (t1, ignored) "
                + "FROM SERVER srv INTO local OPTIONS (import_default 'true')");

        assertThrows(IllegalArgumentException.class,
                () -> statement.setTables(Qualification.LIMIT_TO, List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> statement.setTables(Qualification.EXCEPT, null));
        assertThrows(IllegalArgumentException.class, () -> statement.setTables(null, tables));
        assertEquals(Qualification.LIMIT_TO, statement.getQualification());

        statement.setTables(null, null);
        statement.setOptions(null);
        assertNull(statement.getQualification());
        assertDeparse(statement, "IMPORT FOREIGN SCHEMA remote FROM SERVER srv INTO local");
        assertThrows(NullPointerException.class, () -> new ImportForeignSchema().toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"IMPORT FOREIGN SCHEMA remote FROM SERVER srv",
            "IMPORT FOREIGN SCHEMA remote INTO local",
            "IMPORT FOREIGN SCHEMA FROM SERVER srv INTO local",
            "IMPORT FOREIGN SCHEMA remote FROM srv INTO local",
            "IMPORT SCHEMA remote FROM SERVER srv INTO local",
            "IMPORT FOREIGN TABLE remote FROM SERVER srv INTO local",
            "IMPORT FOREIGN SCHEMA remote LIMIT (t1) FROM SERVER srv INTO local",
            "IMPORT FOREIGN SCHEMA remote LIMIT TO t1 FROM SERVER srv INTO local",
            "IMPORT FOREIGN SCHEMA remote LIMIT TO () FROM SERVER srv INTO local",
            "IMPORT FOREIGN SCHEMA remote EXCEPT () FROM SERVER srv INTO local",
            "IMPORT FOREIGN SCHEMA remote EXCEPT FROM SERVER srv INTO local",
            "IMPORT FOREIGN SCHEMA remote LIMIT TO (t1) EXCEPT (t2) FROM SERVER srv INTO local",
            "IMPORT FOREIGN SCHEMA remote LIMIT TO (t1,) FROM SERVER srv INTO local",
            "IMPORT FOREIGN SCHEMA remote LIMIT TO (ONLY t1 *) FROM SERVER srv INTO local",
            "IMPORT FOREIGN SCHEMA remote LIMIT TO ((t1)) FROM SERVER srv INTO local",
            "IMPORT FOREIGN SCHEMA remote FROM SERVER srv INTO local LIMIT TO (t1)",
            "IMPORT FOREIGN SCHEMA remote FROM SERVER srv INTO local OPTIONS ()",
            "IMPORT FOREIGN SCHEMA remote FROM SERVER srv INTO local OPTIONS (import_default)",
            "IMPORT FOREIGN SCHEMA remote FROM SERVER srv INTO local OPTIONS (import_default true)",
            "IMPORT FOREIGN SCHEMA remote FROM SERVER srv INTO local OPTIONS (import_default 1)",
            "IMPORT FOREIGN SCHEMA remote FROM SERVER srv INTO local OPTIONS (a = 'true')",
            "IMPORT FOREIGN SCHEMA remote FROM SERVER srv INTO local OPTIONS (ADD a 'true')",
            "IMPORT FOREIGN SCHEMA remote FROM SERVER srv INTO local OPTIONS (SET a 'true')",
            "IMPORT FOREIGN SCHEMA remote FROM SERVER srv INTO local OPTIONS (DROP a)",
            "IMPORT FOREIGN SCHEMA remote FROM SERVER srv INTO local OPTIONS a 'true'",
            "IMPORT FOREIGN SCHEMA remote FROM SERVER srv INTO local OPTIONS (a 'b') "
                    + "OPTIONS (c 'd')",
            "IMPORT FOREIGN SCHEMA a.remote FROM SERVER srv INTO local",
            "IMPORT FOREIGN SCHEMA remote FROM SERVER a.srv INTO local",
            "IMPORT FOREIGN SCHEMA remote FROM SERVER srv INTO a.local",
            "IMPORT FOREIGN SCHEMA remote FROM SERVER srv INTO local CASCADE",
            "IMPORT FOREIGN SCHEMA IF EXISTS remote FROM SERVER srv INTO local",
            "IMPORT FOREIGN SCHEMA limit FROM SERVER srv INTO local",
            "IMPORT FOREIGN SCHEMA remote FROM SERVER srv INTO select",
            "IMPORT FOREIGN SCHEMA remote FROM SERVER 'srv' INTO local",
            "IMPORT FOREIGN SCHEMA remote, remote2 FROM SERVER srv INTO local"})
    void rejectsWhatPostgreSqlRejects(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, POSTGRESQL));
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql));
    }

    @Test
    void otherDialectsAndOtherUsesKeepTheirReading() throws JSQLParserException {
        String sql = "IMPORT FOREIGN SCHEMA remote FROM SERVER srv INTO local";
        for (Dialect dialect : new Dialect[] {Dialect.MYSQL, Dialect.MARIADB, Dialect.ORACLE,
                Dialect.SQLSERVER, Dialect.EXASOL}) {
            assertThrows(JSQLParserException.class,
                    () -> CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(dialect)),
                    dialect.name());
        }
        assertInstanceOf(UnsupportedStatement.class, CCJSqlParserUtil.parse(sql,
                parser -> parser.withDialect(Dialect.MYSQL).withUnsupportedStatements(true)));
        // IMPORT not followed by FOREIGN stays unsupported outside EXASOL
        assertInstanceOf(UnsupportedStatement.class, CCJSqlParserUtil.parse(
                "IMPORT INTO t FROM LOCAL CSV FILE 'f.csv'",
                parser -> parser.withUnsupportedStatements(true)));
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse("IMPORT INTO t FROM LOCAL CSV FILE 'f.csv'",
                        POSTGRESQL));

        Consumer<CCJSqlParser> exasol = parser -> parser.withDialect(Dialect.EXASOL);
        assertInstanceOf(Import.class, assertSqlCanBeParsedAndDeparsed(
                "IMPORT INTO schemaName.tableName FROM LOCAL CSV FILE 'file.csv'", true, exasol));
        assertInstanceOf(Select.class, assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM (IMPORT INTO (c1 INT) FROM LOCAL CSV FILE 'file.csv')", true,
                exasol));
        assertInstanceOf(Alter.class, assertSqlCanBeParsedAndDeparsed(
                "ALTER TABLE t IMPORT TABLESPACE", true,
                parser -> parser.withDialect(Dialect.MYSQL)));
    }

    @Test
    void endsAtStatementBoundaries() throws JSQLParserException {
        for (Consumer<CCJSqlParser> mode : POSTGRESQL_AND_NO_DIALECT) {
            Statements statements = CCJSqlParserUtil.parseStatements(
                    "IMPORT FOREIGN SCHEMA remote FROM SERVER srv INTO local; SELECT 1;\n"
                            + "IMPORT FOREIGN SCHEMA r EXCEPT (t) FROM SERVER s INTO l OPTIONS (a 'b');"
                            + "IMPORT FOREIGN SCHEMA r FROM SERVER s INTO l\n"
                            + "IMPORT FOREIGN SCHEMA r FROM SERVER s INTO l OPTIONS (a 'b')\n"
                            + "SELECT 2",
                    mode);
            assertEquals(6, statements.size());
            assertEquals("IMPORT FOREIGN SCHEMA remote FROM SERVER srv INTO local",
                    statements.get(0).toString());
            assertInstanceOf(Select.class, statements.get(1));
            assertEquals("IMPORT FOREIGN SCHEMA r EXCEPT (t) FROM SERVER s INTO l OPTIONS (a 'b')",
                    statements.get(2).toString());
            assertEquals("IMPORT FOREIGN SCHEMA r FROM SERVER s INTO l",
                    statements.get(3).toString());
            assertEquals("IMPORT FOREIGN SCHEMA r FROM SERVER s INTO l OPTIONS (a 'b')",
                    statements.get(4).toString());
            assertInstanceOf(Select.class, statements.get(5));
        }

        Statements afterUnsupported = CCJSqlParserUtil.parseStatements(
                "FOO bar baz; IMPORT FOREIGN SCHEMA r FROM SERVER s INTO l",
                parser -> parser.withUnsupportedStatements(true));
        assertEquals(2, afterUnsupported.size());
        assertInstanceOf(UnsupportedStatement.class, afterUnsupported.get(0));
        assertInstanceOf(ImportForeignSchema.class, afterUnsupported.get(1));
    }

    @Test
    void reachesVisitorsAndReportsNoLocalTables() throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(
                "IMPORT FOREIGN SCHEMA remote LIMIT TO (t1, t2) FROM SERVER srv INTO local "
                        + "OPTIONS (import_default 'true')",
                POSTGRESQL);
        String visited = statement.accept(new StatementVisitorAdapter<String>() {
            @Override
            public <S> String visit(ImportForeignSchema importForeignSchema, S context) {
                return importForeignSchema.getServer();
            }
        }, null);
        assertEquals("srv", visited);
        assertNull(statement.accept(new StatementVisitorAdapter<String>(), null));

        List<Expression> expressions = new ArrayList<>();
        ((ImportForeignSchema) statement).visitExpressions(expressions::add);
        assertEquals(1, expressions.size());
        assertInstanceOf(StringValue.class, expressions.get(0));

        // the listed names belong to the remote schema
        assertThat(new TablesNamesFinder<>().getTables(statement)).isEmpty();
        assertNotNull(((ImportForeignSchema) statement).getASTNode());
    }

    @Test
    void modifiesTheSchema() throws JSQLParserException {
        StatementFeatures features = CCJSqlParserUtil
                .parse("IMPORT FOREIGN SCHEMA remote FROM SERVER srv INTO local").getFeatures();
        assertThat(features.getCertain()).containsExactly(StmtFeature.MODIFIES_SCHEMA);
        assertThat(features.getUncertain()).isEmpty();
    }

    @Test
    void validatesTheFeature() {
        String sql = "IMPORT FOREIGN SCHEMA remote EXCEPT (t1) FROM SERVER srv INTO local "
                + "OPTIONS (import_default 'true')";
        ValidationTestAsserts.validateNoErrors(sql, 1, PostgresqlVersion.V14);
        ValidationTestAsserts.validateNoErrors(sql, 1, FeaturesAllowed.DDL);
        ValidationTestAsserts.validateNotAllowed(sql, 1, 1, FeaturesAllowed.SELECT,
                Feature.importForeignSchema);
        assertFalse(new Validation(List.of(MySqlVersion.V8_0), sql).validate().isEmpty());
        assertFalse(new Validation(List.of(FeaturesAllowed.DML), sql).validate().isEmpty());
    }
}

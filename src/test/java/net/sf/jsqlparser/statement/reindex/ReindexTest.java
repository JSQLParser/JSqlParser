/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.reindex;

import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.StmtFeature;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.statement.reindex.Reindex.Option;
import net.sf.jsqlparser.statement.reindex.Reindex.Target;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.feature.MySqlVersion;
import net.sf.jsqlparser.util.validation.feature.PostgresqlVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ReindexTest {
    private static final Consumer<CCJSqlParser> POSTGRESQL =
            parser -> parser.withDialect(Dialect.POSTGRESQL);

    private static Reindex roundTrip(String sql) throws JSQLParserException {
        Reindex reindex = assertInstanceOf(Reindex.class,
                assertSqlCanBeParsedAndDeparsed(sql, false, POSTGRESQL));
        assertEquals(sql, CCJSqlParserUtil.parse(reindex.toString(), POSTGRESQL).toString());
        return reindex;
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "REINDEX INDEX CONCURRENTLY idx",
            "REINDEX TABLE users",
            "REINDEX TABLE CONCURRENTLY app.users",
            "REINDEX (VERBOSE) INDEX app.idx",
            "REINDEX (VERBOSE false, TABLESPACE fast_space, CONCURRENTLY) SCHEMA app",
            "REINDEX (CONCURRENTLY ON, VERBOSE 1) TABLE t",
            "REINDEX (TABLESPACE 'pg_default') TABLE t",
            "REINDEX SCHEMA CONCURRENTLY app",
            "REINDEX DATABASE",
            "REINDEX DATABASE CONCURRENTLY mydb",
            "REINDEX SYSTEM",
            "REINDEX SYSTEM mydb"})
    void parsesReindexStatements(String sql) throws JSQLParserException {
        roundTrip(sql);
        assertInstanceOf(Reindex.class, CCJSqlParserUtil.parse(sql));
        assertEquals(2, CCJSqlParserUtil.parseStatements(sql + ";\nSELECT 1", POSTGRESQL).size());
    }

    @Test
    void keepsTargetNameAndOptions() throws JSQLParserException {
        Reindex reindex =
                roundTrip("REINDEX (VERBOSE, TABLESPACE fast_space) TABLE CONCURRENTLY app.users");

        assertEquals(Target.TABLE, reindex.getTarget());
        assertTrue(reindex.isConcurrently());
        assertEquals("app.users", reindex.getName().getFullyQualifiedName());
        assertEquals("app.users", reindex.getTable().getFullyQualifiedName());
        assertEquals(2, reindex.getOptions().size());
        assertEquals(Option.Name.VERBOSE, reindex.getOptions().get(0).getName());
        assertNull(reindex.getOptions().get(0).getValue());
        assertEquals(Option.Name.TABLESPACE, reindex.getOptions().get(1).getName());
        assertEquals("fast_space", reindex.getOptions().get(1).getValue());
    }

    @Test
    void namesOnlyTheReindexedObject() throws JSQLParserException {
        Reindex index = roundTrip("REINDEX INDEX CONCURRENTLY idx");
        assertEquals(Target.INDEX, index.getTarget());
        assertEquals("idx", index.getName().getFullyQualifiedName());
        assertNull(index.getTable());
        assertTrue(new TablesNamesFinder().getTables((Statement) index).isEmpty());

        Reindex database = roundTrip("REINDEX DATABASE");
        assertEquals(Target.DATABASE, database.getTarget());
        assertNull(database.getName());
        assertFalse(database.isConcurrently());
        assertTrue(database.getOptions().isEmpty());

        assertThat(TablesNamesFinder.findTables("REINDEX TABLE app.users"))
                .containsExactly("app.users");
    }

    @Test
    void buildsAReindexStatement() {
        Reindex reindex = new Reindex(Target.SCHEMA, new Table("app")).withConcurrently(true)
                .addOptions(new Option(Option.Name.VERBOSE, null));

        assertDeparse(reindex, "REINDEX (VERBOSE) SCHEMA CONCURRENTLY app");
    }

    @Test
    void validationAndFeatures() throws JSQLParserException {
        assertTrue(new Validation(List.of(PostgresqlVersion.V10), "REINDEX TABLE t").validate()
                .isEmpty());
        assertThat(new Validation(List.of(PostgresqlVersion.V10),
                "REINDEX TABLE CONCURRENTLY t").validate()).isNotEmpty();
        assertThat(new Validation(List.of(PostgresqlVersion.V10),
                "REINDEX (CONCURRENTLY) TABLE t").validate()).isNotEmpty();
        assertTrue(new Validation(List.of(PostgresqlVersion.V12),
                "REINDEX TABLE CONCURRENTLY t").validate().isEmpty());
        assertThat(new Validation(List.of(MySqlVersion.V8_0), "REINDEX TABLE t").validate())
                .isNotEmpty();

        assertThat(CCJSqlParserUtil.parse("REINDEX TABLE t").getFeatures().getCertain())
                .containsExactlyInAnyOrder(StmtFeature.MODIFIES_SCHEMA, StmtFeature.READS_DATA);
    }

    @Test
    void startsAStatementAmongUnsupportedStatements() throws JSQLParserException {
        Statements statements = CCJSqlParserUtil.parseStatements(
                "VACUUM t; REINDEX TABLE t; REINDEX t",
                parser -> parser.withUnsupportedStatements(true));

        assertEquals(3, statements.size());
        assertInstanceOf(UnsupportedStatement.class, statements.get(0));
        assertInstanceOf(Reindex.class, statements.get(1));
        assertInstanceOf(UnsupportedStatement.class, statements.get(2));
    }

    @Test
    void reindexRemainsAnIdentifier() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT reindex FROM reindex", false, POSTGRESQL);
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE reindex (reindex int)", false, POSTGRESQL);
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE t ADD COLUMN reindex int", false, POSTGRESQL);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "REINDEX TABLE",
            "REINDEX SCHEMA",
            "REINDEX INDEX CONCURRENTLY",
            "REINDEX INDEX a, b",
            "REINDEX VIEW v",
            "REINDEX () TABLE t",
            "REINDEX (FORCE) TABLE t",
            "REINDEX (TABLESPACE) TABLE t",
            "REINDEX SCHEMA app.sub"})
    void rejectsInvalidReindexStatements(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, POSTGRESQL));
    }
}

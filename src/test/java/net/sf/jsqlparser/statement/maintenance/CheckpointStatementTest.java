/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.maintenance;

import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.Consumer;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.StmtFeature;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CheckpointStatementTest {

    private static final Consumer<CCJSqlParser> POSTGRESQL =
            parser -> parser.withDialect(Dialect.POSTGRESQL);

    @Test
    void parsesWithPostgreSqlAndWithoutDialect() throws JSQLParserException {
        assertInstanceOf(CheckpointStatement.class,
                assertSqlCanBeParsedAndDeparsed("CHECKPOINT", true, POSTGRESQL));
        assertInstanceOf(CheckpointStatement.class,
                assertSqlCanBeParsedAndDeparsed("checkpoint", true));
        assertDeparse(new CheckpointStatement(), "CHECKPOINT");
    }

    @ParameterizedTest
    @ValueSource(strings = {"CHECKPOINT (FLUSH_UNLOGGED)", "CHECKPOINT 10", "CHECKPOINT t"})
    void rejectsArguments(String sql) {
        // PostgreSQL 18 accepts no options; CHECKPOINT (...) arrives in a later release
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, POSTGRESQL));
    }

    @Test
    void otherProductsVariantsStayUnsupported() throws JSQLParserException {
        for (String sql : new String[] {"CHECKPOINT 10", "CHECKPOINT SYNC"}) {
            assertInstanceOf(UnsupportedStatement.class, CCJSqlParserUtil.parse(sql,
                    parser -> parser.withUnsupportedStatements(true)), sql);
        }
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse("CHECKPOINT",
                parser -> parser.withDialect(Dialect.SQLSERVER)));
    }

    @Test
    void endsAtStatementBoundaries() throws JSQLParserException {
        Statements statements = CCJSqlParserUtil.parseStatements(
                "SHOW search_path; CHECKPOINT\nCHECKPOINT;CHECKPOINT", POSTGRESQL);
        assertEquals(4, statements.size());

        Statements afterUnsupported = CCJSqlParserUtil.parseStatements(
                "IMPORT INTO t FROM LOCAL CSV FILE 'f.csv'; CHECKPOINT",
                parser -> parser.withDialect(Dialect.POSTGRESQL).withUnsupportedStatements(true));
        assertEquals(2, afterUnsupported.size());
        assertInstanceOf(UnsupportedStatement.class, afterUnsupported.get(0));
        assertInstanceOf(CheckpointStatement.class, afterUnsupported.get(1));
    }

    @Test
    void namesNoTableAndModifiesNoData() throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse("CHECKPOINT", POSTGRESQL);

        assertThat(new TablesNamesFinder<>().getTables(statement)).isEmpty();
        assertThat(statement.getFeatures().getCertain())
                .containsExactly(StmtFeature.MODIFIES_SESSION);
    }

    @Test
    void wordRemainsAnIdentifier() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT checkpoint FROM checkpoint AS checkpoint", true,
                POSTGRESQL);
        assertSqlCanBeParsedAndDeparsed("ALTER SYSTEM CHECKPOINT", true);
    }
}

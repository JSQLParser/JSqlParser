/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.show;

import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.ShowStatement;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementFeatures;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.StmtFeature;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.statement.show.ShowTablesStatement.SelectionMode;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ShowTableStatusStatementTest {

    private static final List<Consumer<CCJSqlParser>> DIALECTS = List.of(parser -> {
    }, parser -> parser.withDialect(Dialect.MYSQL),
            parser -> parser.withDialect(Dialect.MARIADB));

    /** Parses without a dialect and with MySQL and MariaDB, then deparses and reparses. */
    private static ShowTableStatusStatement roundTrip(String sql) throws JSQLParserException {
        ShowTableStatusStatement result = null;
        for (Consumer<CCJSqlParser> dialect : DIALECTS) {
            Statement statement = assertSqlCanBeParsedAndDeparsed(sql, false, dialect);
            assertInstanceOf(ShowTableStatusStatement.class, statement);
            assertDeparse(statement, sql);
            assertEquals(sql, CCJSqlParserUtil.parse(statement.toString(), dialect).toString());
            result = (ShowTableStatusStatement) statement;
        }
        return result;
    }

    @Test
    void currentDatabase() throws JSQLParserException {
        ShowTableStatusStatement show = roundTrip("SHOW TABLE STATUS");

        assertNull(show.getDbName());
        assertNull(show.getLikeExpression());
        assertNull(show.getWhereCondition());
    }

    @Test
    void likePattern() throws JSQLParserException {
        ShowTableStatusStatement show = roundTrip("SHOW TABLE STATUS LIKE 'c0%'");

        assertEquals("c0%", ((StringValue) show.getLikeExpression()).getValue());
    }

    @Test
    void databaseAndWhere() throws JSQLParserException {
        ShowTableStatusStatement show = roundTrip("SHOW TABLE STATUS FROM v WHERE Comment <> ''");

        assertEquals(SelectionMode.FROM, show.getSelectionMode());
        assertEquals("v", show.getDbName());
        assertEquals("Comment <> ''", show.getWhereCondition().toString());
    }

    @Test
    void inDatabaseAndLike() throws JSQLParserException {
        ShowTableStatusStatement show = roundTrip("SHOW TABLE STATUS IN `db` LIKE 'a%'");

        assertEquals(SelectionMode.IN, show.getSelectionMode());
        assertEquals("`db`", show.getDbName());
    }

    @Test
    void keywordsAreCaseInsensitive() throws JSQLParserException {
        assertEquals("SHOW TABLE STATUS IN db",
                CCJSqlParserUtil.parse("show table status in db").toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SHOW TABLE foo",
            "SHOW TABLE EXTENDED IN db LIKE 'x'",
            "SHOW TABLE STATUS FROM db LIMIT 3"
    })
    void otherTableFormsRemainUnsupported(String sql) throws JSQLParserException {
        Statement statement = assertSqlCanBeParsedAndDeparsed(sql, true);
        assertInstanceOf(UnsupportedStatement.class, statement);
    }

    @Test
    void neighbouringShowFormsAreUnchanged() throws JSQLParserException {
        assertInstanceOf(ShowTablesStatement.class, CCJSqlParserUtil.parse("SHOW TABLES"));
        assertInstanceOf(ShowStatement.class, CCJSqlParserUtil.parse("SHOW STATUS"));
        assertSqlCanBeParsedAndDeparsed("SELECT status FROM t WHERE status = 1");
    }

    @Test
    void endsAtTheStatementBoundary() throws JSQLParserException {
        Statements statements =
                CCJSqlParserUtil.parseStatements("SHOW TABLE STATUS;\nSHOW TABLES");

        assertEquals(2, statements.size());
        assertInstanceOf(ShowTableStatusStatement.class, statements.get(0));
    }

    @Test
    void namesNoTable() throws JSQLParserException {
        assertThat(TablesNamesFinder.findTables("SHOW TABLE STATUS FROM db LIKE 't%'")).isEmpty();
    }

    @Test
    void buildsAndDeparses() {
        ShowTableStatusStatement show = new ShowTableStatusStatement()
                .withSelectionMode(SelectionMode.IN).withDbName("db")
                .withLikeExpression(new StringValue("t%"));

        assertDeparse(show, "SHOW TABLE STATUS IN db LIKE 't%'");
    }

    @Test
    void reportsMetadataFeatures() throws JSQLParserException {
        StatementFeatures features = CCJSqlParserUtil.parse("SHOW TABLE STATUS").getFeatures();

        assertThat(features.getCertain()).containsExactly(StmtFeature.RETURNS_RESULT_SET);
    }
}

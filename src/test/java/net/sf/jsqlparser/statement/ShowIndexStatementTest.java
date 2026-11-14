/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.show.ShowDbSelection;
import net.sf.jsqlparser.statement.show.ShowIndexStatement;
import net.sf.jsqlparser.statement.show.ShowIndexStatement.Keyword;
import net.sf.jsqlparser.statement.show.ShowTablesStatement.SelectionMode;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 *
 * @author Jayant Kumar Yadav
 */

public class ShowIndexStatementTest {

    private static final List<Consumer<CCJSqlParser>> DIALECTS = List.of(parser -> {
    }, parser -> parser.withDialect(Dialect.MYSQL),
            parser -> parser.withDialect(Dialect.MARIADB));

    /** Parses without a dialect and with MySQL and MariaDB, then deparses and reparses. */
    private static ShowIndexStatement roundTrip(String sql) throws JSQLParserException {
        ShowIndexStatement result = null;
        for (Consumer<CCJSqlParser> dialect : DIALECTS) {
            Statement statement = assertSqlCanBeParsedAndDeparsed(sql, false, dialect);
            assertInstanceOf(ShowIndexStatement.class, statement);
            assertDeparse(statement, sql);
            assertEquals(sql, CCJSqlParserUtil.parse(statement.toString(), dialect).toString());
            result = (ShowIndexStatement) statement;
        }
        return result;
    }

    @Test
    public void testSimpleUse() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SHOW INDEX FROM mydatabase");
    }

    @Test
    void legacyForm() throws JSQLParserException {
        ShowIndexStatement show = roundTrip("SHOW INDEX FROM mytable");

        assertEquals("mytable", show.getTableName());
        assertEquals(Keyword.INDEX, show.getKeyword());
        assertEquals(SelectionMode.FROM, show.getTableSelectionMode());
        assertFalse(show.isExtended());
        assertNull(show.getDbSelection());
        assertNull(show.getWhereCondition());
    }

    @Test
    void trailingDatabase() throws JSQLParserException {
        ShowIndexStatement show = roundTrip("SHOW INDEX FROM t FROM v");

        assertEquals("t", show.getTableName());
        assertEquals("v", show.getDbName());
        assertEquals(SelectionMode.FROM, show.getDbSelection().getSelectionMode());
        assertEquals("v.t", show.getQualifiedTable().getFullyQualifiedName());
    }

    @Test
    void extendedIndexesInTable() throws JSQLParserException {
        ShowIndexStatement show = roundTrip("SHOW EXTENDED INDEXES IN t");

        assertTrue(show.isExtended());
        assertEquals(Keyword.INDEXES, show.getKeyword());
        assertEquals(SelectionMode.IN, show.getTableSelectionMode());
    }

    @Test
    void keysOfQualifiedTableWithWhere() throws JSQLParserException {
        ShowIndexStatement show =
                roundTrip("SHOW KEYS FROM db.t IN `other` WHERE Key_name = 'PRIMARY'");

        assertEquals(Keyword.KEYS, show.getKeyword());
        assertEquals("db", show.getTable().getSchemaName());
        assertEquals(SelectionMode.IN, show.getDbSelection().getSelectionMode());
        assertEquals("`other`.t", show.getQualifiedTable().getFullyQualifiedName());
        assertEquals("Key_name = 'PRIMARY'", show.getWhereCondition().toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"SHOW INDEXES FROM t", "SHOW KEYS IN t", "SHOW EXTENDED INDEX FROM t",
            "SHOW INDEX IN t IN db",
            "SHOW EXTENDED KEYS FROM `t` FROM `db` WHERE Seq_in_index > 1"})
    void variants(String sql) throws JSQLParserException {
        roundTrip(sql);
    }

    @Test
    void keywordsAreCaseInsensitive() throws JSQLParserException {
        assertEquals("SHOW INDEXES FROM my_table",
                CCJSqlParserUtil.parse("show indexes from my_table").toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"SHOW INDEXES IN TABLE t", "SHOW INDEX FROM t LIKE 'x'",
            "SHOW FULL INDEX FROM t"})
    void otherFormsRemainUnsupported(String sql) throws JSQLParserException {
        Statement statement = assertSqlCanBeParsedAndDeparsed(sql, true);
        assertInstanceOf(UnsupportedStatement.class, statement);
        assertInstanceOf(ShowStatement.class, CCJSqlParserUtil.parse("SHOW INDEXES"));
    }

    @Test
    void endsAtTheStatementBoundary() throws JSQLParserException {
        Statements statements =
                CCJSqlParserUtil.parseStatements("SHOW INDEX FROM t FROM db;\nSELECT 1");

        assertEquals(2, statements.size());
        assertInstanceOf(ShowIndexStatement.class, statements.get(0));
    }

    @Test
    void findsTheTable() throws JSQLParserException {
        // used to throw UnsupportedOperationException
        assertThat(TablesNamesFinder.findTables("SHOW INDEX FROM s.t")).containsExactly("s.t");
        assertThat(TablesNamesFinder.findTables("SHOW KEYS FROM t IN db")).containsExactly("db.t");
    }

    @Test
    void legacyAccessorsAndBuilders() {
        ShowIndexStatement show = new ShowIndexStatement("db.t");

        assertEquals("db", show.getTable().getSchemaName());
        assertEquals("SHOW INDEX FROM db.t", show.toString());
        assertEquals("SHOW INDEX FROM t2", show.withTableName("t2").toString());

        show = new ShowIndexStatement(new Table("t")).withExtended(true)
                .withKeyword(Keyword.KEYS).withTableSelectionMode(SelectionMode.IN)
                .withDbSelection(new ShowDbSelection("db"))
                .withWhereCondition(new Column("Non_unique"));
        assertDeparse(show, "SHOW EXTENDED KEYS IN t FROM db WHERE Non_unique");

        show.setKeyword(null);
        show.setTableSelectionMode(null);
        assertEquals("SHOW EXTENDED INDEX FROM t FROM db WHERE Non_unique", show.toString());
        show.setTableName(null);
        assertNull(show.getTable());
        assertNull(show.getQualifiedTable());
    }

    @Test
    void reportsMetadataFeatures() throws JSQLParserException {
        StatementFeatures features =
                CCJSqlParserUtil.parse("SHOW INDEX FROM t FROM db").getFeatures();

        assertThat(features.getCertain()).containsExactly(StmtFeature.RETURNS_RESULT_SET);
    }
}

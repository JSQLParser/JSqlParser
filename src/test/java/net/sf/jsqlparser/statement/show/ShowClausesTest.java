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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.statement.ShowColumnsStatement;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.statement.show.ShowTablesStatement.Modifiers;
import net.sf.jsqlparser.statement.show.ShowTablesStatement.SelectionMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** The shared {FROM | IN} db and LIKE | WHERE clauses of the SHOW statements. */
class ShowClausesTest {

    @Test
    void showTablesHoldsTheSharedClauses() throws JSQLParserException {
        ShowTablesStatement show = (ShowTablesStatement) assertSqlCanBeParsedAndDeparsed(
                "SHOW FULL TABLES IN v WHERE Table_type = 'VIEW'");

        assertEquals(SelectionMode.IN, show.getDbSelection().getSelectionMode());
        assertEquals("v", show.getDbSelection().getName());
        assertEquals(SelectionMode.IN, show.getSelectionMode());
        assertEquals("v", show.getDbName());
        assertEquals(ShowFilter.Kind.WHERE, show.getFilter().getKind());
        assertSame(show.getFilter().getExpression(), show.getWhereCondition());
        assertNull(show.getLikeExpression());

        show = (ShowTablesStatement) assertSqlCanBeParsedAndDeparsed("SHOW TABLES");
        assertNull(show.getSelectionMode());
        assertNull(show.getDbSelection());
        assertNull(show.getFilter());
    }

    @ParameterizedTest
    @ValueSource(strings = {"SHOW INDEXES IN TABLE t", "SHOW INDEX FROM t LIMIT 3",
            "SHOW INDEX FROM t FROM d FROM e", "SHOW COLUMNS FROM t LIMIT 3"})
    void otherFormsFallBackInsteadOfFailing(String sql) throws JSQLParserException {
        for (Dialect dialect : new Dialect[] {Dialect.SNOWFLAKE, Dialect.MYSQL}) {
            Statement statement = assertSqlCanBeParsedAndDeparsed(sql, true,
                    parser -> parser.withDialect(dialect));
            assertInstanceOf(UnsupportedStatement.class, statement);
        }
    }

    @Test
    void mySqlTakesAStringAsTheLikePattern() throws JSQLParserException {
        for (Dialect dialect : new Dialect[] {Dialect.MYSQL, Dialect.MARIADB}) {
            ShowColumnsStatement columns = (ShowColumnsStatement) assertSqlCanBeParsedAndDeparsed(
                    "SHOW COLUMNS FROM t LIKE \"a%\"", true,
                    parser -> parser.withDialect(dialect));
            assertEquals("a%", ((StringValue) columns.getLikeExpression()).getValue());

            // MySQL reports a syntax error for these
            for (String sql : new String[] {"SHOW COLUMNS FROM t LIKE a",
                    "SHOW TABLE STATUS LIKE 1", "SHOW TABLE STATUS LIKE CONCAT('a', '%')"}) {
                assertInstanceOf(UnsupportedStatement.class, assertSqlCanBeParsedAndDeparsed(sql,
                        true, parser -> parser.withDialect(dialect)), sql);
            }
        }
        // without a dialect the pattern is any simple expression, as before
        ShowTableStatusStatement status =
                (ShowTableStatusStatement) assertSqlCanBeParsedAndDeparsed(
                        "SHOW TABLE STATUS LIKE CONCAT('a', '%')", true);
        assertInstanceOf(Expression.class, status.getLikeExpression());
    }

    @Test
    void showTablesRendersWithoutModifiersOrSelectionMode() {
        ShowTablesStatement show = new ShowTablesStatement();
        assertEquals("SHOW TABLES", show.toString());

        show.setDbName("db");
        assertEquals("SHOW TABLES FROM db", show.toString());

        show.setModifiers(EnumSet.of(Modifiers.FULL));
        show.setSelectionMode(SelectionMode.IN);
        assertDeparse(show, "SHOW FULL TABLES IN db");
    }

    @Test
    void legacySettersWorkInEitherOrder() {
        ShowTablesStatement tables = new ShowTablesStatement();
        tables.setSelectionMode(SelectionMode.IN);
        assertEquals(SelectionMode.IN, tables.getSelectionMode());
        assertNull(tables.getDbName());
        assertEquals("SHOW TABLES", tables.toString());
        tables.setDbName("db");
        assertEquals("SHOW TABLES IN db", tables.toString());
        tables.setDbName(null);
        tables.setSelectionMode(null);
        assertNull(tables.getSelectionMode());
        assertNull(tables.getDbSelection());

        ShowTableStatusStatement status = new ShowTableStatusStatement().withDbName("db")
                .withSelectionMode(SelectionMode.IN);
        assertEquals("SHOW TABLE STATUS IN db", status.toString());
        assertEquals(SelectionMode.FROM, new ShowTableStatusStatement().getSelectionMode());

        ShowColumnsStatement columns = new ShowColumnsStatement("t");
        columns.setDbSelectionMode(SelectionMode.IN);
        columns.setDbName("db");
        assertEquals("SHOW COLUMNS FROM t IN db", columns.toString());
        assertEquals(new ShowDbSelection(SelectionMode.IN, "db").toString(),
                columns.getDbSelection().toString());
    }

    @Test
    void legacyFilterSettersReplaceTheFilter() {
        ShowTablesStatement show = new ShowTablesStatement();
        show.setLikeExpression(new StringValue("a%"));
        assertEquals("SHOW TABLES LIKE 'a%'", show.toString());

        Expression condition = new Column("Tables_in_db");
        show.setWhereCondition(condition);
        assertNull(show.getLikeExpression());
        assertSame(condition, show.getWhereCondition());

        // clearing the other kind keeps the filter
        show.setLikeExpression(null);
        assertSame(condition, show.getWhereCondition());
        show.setWhereCondition(null);
        assertNull(show.getFilter());

        ShowTableStatusStatement status =
                new ShowTableStatusStatement().withLikeExpression(new StringValue("t%"));
        status.setWhereCondition(new Column("Rows"));
        assertEquals("SHOW TABLE STATUS WHERE Rows", status.toString());
    }

    @Test
    void sharedClauseValues() {
        ShowFilter filter = ShowFilter.where(new Column("a"));
        assertEquals(ShowFilter.Kind.WHERE, filter.getKind());
        assertEquals("WHERE a", filter.toString());
        assertNull(ShowFilter.likeExpression(filter));
        assertEquals("a", ShowFilter.whereCondition(filter).toString());
        assertEquals(ShowFilter.Kind.LIKE, new ShowFilter().withKind(null).getKind());
        assertEquals("LIKE 'x'",
                new ShowFilter().withExpression(new StringValue("x")).toString());

        ShowDbSelection selection = new ShowDbSelection(null, "db");
        assertEquals(SelectionMode.FROM, selection.getSelectionMode());
        assertTrue(ShowDbSelection.isPresent(selection));
        assertFalse(ShowDbSelection.isPresent(new ShowDbSelection()));
        assertEquals("IN db", selection.withSelectionMode(SelectionMode.IN).toString());
        assertEquals("FROM x", new ShowDbSelection().withName("x").toString());
    }

    @Test
    void statementDeParserPrintsFiltersWithTheExpressionDeParser()
            throws JSQLParserException {
        assertEquals("SHOW TABLES LIKE <a%>", ShowExpressionDeParsing.deparse(
                "SHOW TABLES LIKE 'a%'"));
        assertEquals("SHOW FULL COLUMNS FROM t LIKE <c%>", ShowExpressionDeParsing.deparse(
                "SHOW FULL COLUMNS FROM t LIKE 'c%'"));
        assertEquals("SHOW TABLE STATUS FROM db WHERE Comment = <x>",
                ShowExpressionDeParsing.deparse("SHOW TABLE STATUS FROM db WHERE Comment = 'x'"));
        assertEquals("SHOW INDEX FROM t WHERE Key_name = <PRIMARY>",
                ShowExpressionDeParsing.deparse("SHOW INDEX FROM t WHERE Key_name = 'PRIMARY'"));
    }
}

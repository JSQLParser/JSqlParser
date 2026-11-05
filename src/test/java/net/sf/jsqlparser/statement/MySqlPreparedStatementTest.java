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

import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.UserVariable;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.ValidationError;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import net.sf.jsqlparser.util.validation.feature.MariaDbVersion;
import net.sf.jsqlparser.util.validation.feature.MySqlVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** MySQL's PREPARE ... FROM, EXECUTE ... USING and DROP PREPARE. */
class MySqlPreparedStatementTest {

    private static Statement mysql(String sql) throws JSQLParserException {
        Statement statement =
                assertSqlCanBeParsedAndDeparsed(sql, true, p -> p.withDialect(Dialect.MYSQL));
        // these forms are read without a dialect too
        assertEquals(sql, assertSqlCanBeParsedAndDeparsed(sql, true).toString());
        return statement;
    }

    private static void assertRejected(String sql, Dialect dialect) {
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse(sql, p -> p.withDialect(dialect)), sql);
    }

    @Test
    void prepareFromString() throws JSQLParserException {
        PrepareStatement prepare = (PrepareStatement) mysql("PREPARE s FROM 'SELECT ?'");
        assertEquals("s", prepare.getName());
        assertEquals("SELECT ?",
                assertInstanceOf(StringValue.class, prepare.getSource()).getValue());
        assertNull(prepare.getStatement());
        assertNull(prepare.getParameterTypes());

        prepare = (PrepareStatement) CCJSqlParserUtil.parse("PREPARE s FROM \"SELECT 1\"",
                p -> p.withDialect(Dialect.MYSQL));
        assertEquals("SELECT 1", ((StringValue) prepare.getSource()).getValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"PREPARE s FROM @sql", "PREPARE s FROM @'the sql'",
            "PREPARE `my stmt` FROM @sql"})
    void prepareFromVariable(String sql) throws JSQLParserException {
        PrepareStatement prepare = (PrepareStatement) mysql(sql);
        assertInstanceOf(UserVariable.class, prepare.getSource());
        assertNull(prepare.getStatement());
    }

    @Test
    void prepareSourceAndStatementExcludeEachOther() throws JSQLParserException {
        PrepareStatement prepare = (PrepareStatement) mysql("PREPARE s FROM @sql");
        prepare.setStatement(CCJSqlParserUtil.parse("SELECT 1"));
        assertNull(prepare.getSource());
        assertEquals("PREPARE s AS SELECT 1", prepare.toString());
        prepare.withSource(new UserVariable("q"));
        assertNull(prepare.getStatement());
        assertEquals("PREPARE s FROM @q", prepare.toString());
        assertDeparse(prepare, "PREPARE s FROM @q");
    }

    @Test
    void executeUsing() throws JSQLParserException {
        Execute execute = (Execute) mysql("EXECUTE s USING @a, @b");
        assertEquals(Execute.ExecType.EXECUTE, execute.getExecType());
        assertEquals("s", execute.getName());
        assertNull(execute.getExprList());
        assertEquals(List.of("a", "b"), execute.getUsingVariables().stream()
                .map(UserVariable::getName).collect(Collectors.toList()));

        execute = (Execute) mysql("EXECUTE s USING @'first value'");
        assertEquals(1, execute.getUsingVariables().size());
        assertNull(((Execute) mysql("EXECUTE s")).getUsingVariables());

        Execute built = new Execute().withName("s").withUsingVariables(
                new ExpressionList<>(new UserVariable("x"), new UserVariable("y")));
        assertEquals("EXECUTE s USING @x, @y", built.toString());
        assertDeparse(built, "EXECUTE s USING @x, @y");
    }

    @Test
    void dropPrepare() throws JSQLParserException {
        DeallocateStatement drop = (DeallocateStatement) mysql("DROP PREPARE s");
        assertEquals(DeallocateStatement.Keyword.DROP, drop.getKeyword());
        assertTrue(drop.isUsingPrepareKeyword());
        assertEquals("s", drop.getName());

        DeallocateStatement deallocate = (DeallocateStatement) mysql("DEALLOCATE PREPARE s");
        assertEquals(DeallocateStatement.Keyword.DEALLOCATE, deallocate.getKeyword());
        assertTrue(deallocate.isUsingPrepareKeyword());

        deallocate.withKeyword(DeallocateStatement.Keyword.DROP).setUsingPrepareKeyword(false);
        assertEquals("DROP PREPARE s", deallocate.toString());
        deallocate.setKeyword(null);
        assertEquals("DEALLOCATE s", deallocate.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"PREPARE s FROM SELECT 1", "PREPARE s FROM 'a' 'b'",
            "PREPARE s FROM @@sql", "PREPARE s FROM sql", "PREPARE s (INT) FROM 'SELECT ?'",
            "PREPARE s FROM", "EXECUTE s USING 1", "EXECUTE s USING @a,", "EXECUTE s USING",
            "EXECUTE s USING @@a", "EXECUTE s USING a", "CALL p USING @a",
            "EXECUTE s (1) USING @a", "DROP PREPARE", "DROP PREPARE s t"})
    void rejectedForms(String sql) {
        assertRejected(sql, Dialect.MYSQL);
    }

    @Test
    void otherDialectsAreUnchanged() throws JSQLParserException {
        for (Dialect dialect : List.of(Dialect.POSTGRESQL, Dialect.ORACLE, Dialect.SQLSERVER)) {
            assertRejected("PREPARE s FROM 'SELECT 1'", dialect);
            assertRejected("EXECUTE s USING @a", dialect);
            assertRejected("DROP PREPARE s", dialect);
        }
        assertSqlCanBeParsedAndDeparsed("PREPARE p(bigint) AS SELECT $1", true,
                p -> p.withDialect(Dialect.POSTGRESQL));
        // forms the MySQL dialect read before keep their reading
        for (String sql : List.of("DEALLOCATE s", "EXECUTE s (1, 2)", "PREPARE p AS SELECT 1")) {
            assertSqlCanBeParsedAndDeparsed(sql, true, p -> p.withDialect(Dialect.MYSQL));
        }
    }

    @Test
    void statementBoundaries() throws JSQLParserException {
        Statements statements = CCJSqlParserUtil.parseStatements(
                "PREPARE s FROM 'SELECT ? + ?'; SET @a = 1; EXECUTE s USING @a, @a;\n"
                        + "DROP PREPARE s; DEALLOCATE PREPARE s",
                p -> p.withDialect(Dialect.MYSQL));
        assertEquals(List.of("PREPARE s FROM 'SELECT ? + ?'", "SET @a = 1",
                "EXECUTE s USING @a, @a", "DROP PREPARE s", "DEALLOCATE PREPARE s"),
                statements.stream().map(Object::toString).collect(Collectors.toList()));
    }

    @Test
    void tablesAndFeatures() throws JSQLParserException {
        // the tables of a statement held in a string or a variable are unknown
        Statement prepare = CCJSqlParserUtil.parse("PREPARE s FROM 'SELECT * FROM t'");
        assertThrows(UnsupportedOperationException.class,
                () -> new TablesNamesFinder<>().getTables(prepare));
        Statement execute = CCJSqlParserUtil.parse("EXECUTE s USING @a");
        assertThrows(UnsupportedOperationException.class,
                () -> new TablesNamesFinder<>().getTables(execute));
        assertThat(TablesNamesFinder.findTables("DROP PREPARE s")).isEmpty();
        assertThat(TablesNamesFinder.findTables("PREPARE p AS SELECT * FROM t"))
                .containsExactly("t");

        assertTrue(prepare.getFeatures().is(StmtFeature.MODIFIES_SESSION));
        assertFalse(prepare.getFeatures().isOpaque());
        assertTrue(CCJSqlParserUtil.parse("DROP PREPARE s").getFeatures()
                .is(StmtFeature.MODIFIES_SESSION));
        assertTrue(execute.getFeatures().isOpaque());
    }

    @Test
    void visitorsReachVariables() throws JSQLParserException {
        List<String> names = new ArrayList<>();
        StatementVisitorAdapter<Void> visitor = new StatementVisitorAdapter<>(
                new SelectVisitorAdapter<>(new ExpressionVisitorAdapter<Void>() {
                    @Override
                    public <S> Void visit(UserVariable variable, S context) {
                        names.add(variable.getName());
                        return null;
                    }
                }));
        CCJSqlParserUtil.parse("EXECUTE s USING @a, @b").accept(visitor, null);
        CCJSqlParserUtil.parse("PREPARE s FROM @c").accept(visitor, null);
        assertEquals(List.of("a", "b", "c"), names);
    }

    @Test
    void validation() {
        for (String sql : List.of("PREPARE s FROM 'SELECT ?'", "PREPARE s FROM @sql",
                "EXECUTE s USING @a, @b", "EXECUTE s", "DROP PREPARE s")) {
            assertTrue(new Validation(List.of(MySqlVersion.V8_0), sql).validate().isEmpty(), sql);
            assertTrue(new Validation(List.of(MariaDbVersion.V10_5_4), sql).validate().isEmpty(),
                    sql);
        }
        List<ValidationError> errors = new Validation(List.of(FeaturesAllowed.SELECT),
                "EXECUTE s USING @a").validate();
        assertThat(errors.get(0).getErrors()).anyMatch(
                error -> error.getMessage().contains(Feature.executeUsing.name()));
        errors = new Validation(List.of(FeaturesAllowed.SELECT), "PREPARE s FROM @sql").validate();
        assertThat(errors.get(0).getErrors())
                .anyMatch(error -> error.getMessage().contains(Feature.prepare.name()));
    }
}

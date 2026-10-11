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

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.create.function.CreateFunction;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CheckConstraint;
import net.sf.jsqlparser.statement.create.table.NamedConstraint;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import net.sf.jsqlparser.util.validation.feature.SqlServerVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SqlServerRoutineTest {
    private static CreateFunctionalStatement parse(String sql) throws Exception {
        CreateFunctionalStatement statement =
                (CreateFunctionalStatement) CCJSqlParserUtil.parse(sql,
                        p -> p.withDialect(Dialect.SQLSERVER));
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(output));
        assertEquals(statement.toString(), output.toString());
        assertEquals(statement.toString(), CCJSqlParserUtil.parse(output.toString(),
                p -> p.withDialect(Dialect.SQLSERVER)).toString());
        return statement;
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREATE", "ALTER", "CREATE OR ALTER"})
    void supportsIssue1978AndSharedOperations(String prefix) throws Exception {
        CreateFunctionalStatement function = parse(
                prefix + " FUNCTION getPayments() RETURNS TABLE AS RETURN SELECT * from Payments;");
        assertEquals(prefix.replace(' ', '_'), function.getOperation().name());
        assertTrue(function.getReturnType().isTable());
        assertNull(function.getReturnType().getTableElements());
        CreateFunctionalStatement procedure = parse(prefix
                + " PROCEDURE SPPayment AS SET NOCOUNT ON; BEGIN SELECT * FROM Payments; END");
        assertEquals("PROCEDURE", procedure.getKind());
        assertNull(procedure.getReturnType());
        assertTrue(procedure.formatDeclaration().contains("NOCOUNT ON; BEGIN"));
        assertTrue(procedure.getFeatures().modifiesSchema());
        assertThrows(UnsupportedOperationException.class,
                () -> new TablesNamesFinder().getTables(procedure));
    }

    @Test
    void exposesIssue715ReturnTableAndConstraintExpressions() throws Exception {
        CreateFunctionalStatement statement = parse(
                "CREATE OR ALTER FUNCTION dbo.f(@id int = 1) RETURNS @result TABLE (id int NOT NULL, amount decimal(10, 2), PRIMARY KEY (id), CHECK (amount > 0)) AS BEGIN INSERT INTO @result SELECT id, amount FROM payments; RETURN; END;");
        assertEquals("@result", statement.getReturnType().getTableVariable());
        assertEquals(4, statement.getReturnType().getTableElements().size());
        List<ColumnDefinition> columns =
                statement.getReturnType().getTableElements(ColumnDefinition.class);
        assertEquals(2, columns.size());
        assertEquals(2, statement.getReturnType().getTableElements(NamedConstraint.class).size());
        assertEquals("id", columns.get(0).getColumnName());
        List<Long> visited = new ArrayList<>();
        ExpressionVisitorAdapter<Void> expressions = new ExpressionVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(LongValue value, S context) {
                assertEquals("ctx", context);
                visited.add(value.getValue());
                return null;
            }
        };
        statement.accept(new StatementVisitorAdapter<>(new SelectVisitorAdapter<>(expressions)),
                "ctx");
        assertEquals(List.of(0L), visited);
        columns.get(0).setColumnName("payment_id");
        statement.getReturnType().setTableVariable("@rows");
        assertTrue(statement.toString().contains("RETURNS @rows TABLE (payment_id int NOT NULL"));
        CheckConstraint check =
                statement.getReturnType().getTableElements(CheckConstraint.class).get(0);
        check.setExpression(CCJSqlParserUtil.parseCondExpression("amount > 10"));
        StringBuilder output = new StringBuilder();
        ExpressionDeParser deparser = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(LongValue value, S context) {
                return getBuilder().append(value.getValue() + 1);
            }
        };
        statement.accept(new StatementDeParser(deparser, new SelectDeParser(), output));
        assertTrue(output.toString().contains("CHECK (amount > 11)"));
        parse(output.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE FUNCTION dbo.f() RETURNS int AS BEGIN RETURN CASE WHEN 1 = 1 THEN 1 ELSE 0 END; END;",
            "ALTER FUNCTION dbo.f() RETURNS @r TABLE (id int) AS BEGIN BEGIN INSERT INTO @r SELECT 1; END; RETURN; END;",
            "CREATE FUNCTION dbo.f() RETURNS TABLE (id int) AS EXTERNAL NAME assembly.class.method;"
    })
    void preservesScalarClrAndNestedBodies(String sql) throws Exception {
        parse(sql);
    }

    @Test
    void retainsFollowingStatements() throws Exception {
        for (String sql : List.of(
                "ALTER FUNCTION f() RETURNS TABLE AS RETURN SELECT 1 AS id; SELECT 2;",
                "ALTER FUNCTION f() RETURNS int AS BEGIN RETURN CASE WHEN 1=1 THEN 1 END; END; SELECT 2;")) {
            assertEquals(2, CCJSqlParserUtil
                    .parseStatements(sql, p -> p.withDialect(Dialect.SQLSERVER)).size());
        }
    }

    @Test
    void preservesTheEntireProcedureBatch() throws Exception {
        String sql = "ALTER PROCEDURE p AS BEGIN TRY BEGIN TRANSACTION; SELECT 1; COMMIT; END TRY; "
                + "BEGIN CATCH ROLLBACK; END CATCH; SELECT 2;";
        CreateFunctionalStatement statement = parse(sql);
        assertTrue(statement.formatDeclaration().endsWith("END CATCH; SELECT 2;"));
        assertEquals(1, CCJSqlParserUtil.parseStatements(sql, p -> p.withDialect(Dialect.SQLSERVER))
                .size());
    }

    @Test
    void keepsDefaultAndOtherDialectsUnchanged() throws Exception {
        CreateFunction old = (CreateFunction) CCJSqlParserUtil
                .parse("CREATE FUNCTION f() RETURNS @r TABLE (id int) AS BEGIN RETURN; END;");
        assertNull(old.getReturnType());
        String pg = "CREATE OR REPLACE FUNCTION f() RETURNS int AS $$ SELECT 1; $$ LANGUAGE sql;";
        assertTrue(CCJSqlParserUtil.parse(pg, p -> p.withDialect(Dialect.POSTGRESQL)).toString()
                .startsWith("CREATE OR REPLACE FUNCTION"));
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil
                .parse("CREATE OR ALTER FUNCTION f() RETURNS TABLE AS RETURN SELECT 1"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREATE FUNCTION f() AS RETURN 1",
            "ALTER FUNCTION f() RETURNS @r TABLE AS BEGIN RETURN; END",
            "CREATE FUNCTION f() RETURNS @r TABLE () AS BEGIN RETURN; END",
            "ALTER FUNCTION f() RETURNS int AS BEGIN RETURN 1;",
            "ALTER FUNCTION f() RETURNS TABLE"})
    void rejectsIncompleteDeclarations(String sql) {
        assertThrows(JSQLParserException.class, () -> parse(sql));
    }

    @Test
    void validatesOperationCapabilities() {
        for (String prefix : List.of("CREATE", "ALTER", "CREATE OR ALTER")) {
            assertTrue(new Validation(
                    CCJSqlParserUtil.newParser("SELECT 1").withDialect(Dialect.SQLSERVER)
                            .getConfiguration(),
                    List.of(SqlServerVersion.V2019),
                    prefix + " FUNCTION f() RETURNS TABLE AS RETURN SELECT 1").validate()
                    .isEmpty());
        }
        assertFalse(new Validation(
                CCJSqlParserUtil.newParser("SELECT 1").withDialect(Dialect.SQLSERVER)
                        .getConfiguration(),
                List.of(new FeaturesAllowed(Feature.functionalStatement, Feature.createFunction)),
                "ALTER FUNCTION f() RETURNS TABLE AS RETURN SELECT 1").validate().isEmpty());
    }
}

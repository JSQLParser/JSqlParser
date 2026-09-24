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
import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.ColumnOption;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.NotNullConstraint;
import net.sf.jsqlparser.util.TableDefinitionTraversal;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlColumnNotNullTest {
    @ParameterizedTest
    @ValueSource(strings = {"NOT NULL NO INHERIT", "CONSTRAINT nn NOT NULL",
            "CONSTRAINT nn NOT NULL NO INHERIT", "CONSTRAINT \"Named NN\" NOT NULL NO INHERIT"})
    void reusesNotNullConstraintsInCreateAndAlterColumns(String clause) throws JSQLParserException {
        for (String prefix : List.of("CREATE TABLE t (", "ALTER TABLE t ADD COLUMN ")) {
            Statement statement = parse(prefix + "id INT " + clause + " DEFAULT 7"
                    + (prefix.startsWith("CREATE") ? ", value INT)" : ", ADD COLUMN value INT"));
            ColumnDefinition column = column(statement);
            NotNullConstraint constraint = assertInstanceOf(NotNullConstraint.class,
                    column.getColumnOptions().get(0).getConstraint());
            assertTrue(constraint.isColumnConstraint());
            assertNull(constraint.getColumn());
            assertEquals(clause.endsWith("NO INHERIT"), constraint.isNoInherit());
            assertEquals(ColumnOption.Kind.DEFAULT, column.getColumnOptions().get(1).getKind());
            roundTrip(statement);
            constraint.setName("replacement");
            constraint.setNoInherit(!constraint.isNoInherit());
            assertTrue(statement.toString().contains("CONSTRAINT replacement NOT NULL"));
            roundTrip(statement);
        }
    }

    @Test
    void retainsPlainNullabilityAndUsesTheExistingTableModel() throws JSQLParserException {
        ColumnOption option =
                column(parse("CREATE TABLE t (id INT NOT NULL)")).getColumnOptions().get(0);
        assertEquals(ColumnOption.Kind.NULLABILITY, option.getKind());
        assertEquals(false, option.getNullable());
        NotNullConstraint table = (NotNullConstraint) ((CreateTable) parse(
                "CREATE TABLE t (id INT, CONSTRAINT nn NOT NULL id NO INHERIT)")).getIndexes()
                .get(0);
        assertFalse(table.isColumnConstraint());
        assertEquals("id", table.getColumn().getColumnName());
        assertEquals("CONSTRAINT nn NOT NULL NO INHERIT", new NotNullConstraint()
                .withColumnConstraint(true).withName("nn").withNoInherit(true).toString());
    }

    @Test
    void doesNotInventASecondColumnExpressionForAnImplicitTarget() throws JSQLParserException {
        CreateTable table =
                (CreateTable) parse("CREATE TABLE t (id INT DEFAULT 7 NOT NULL NO INHERIT)");
        List<Expression> expressions = new ArrayList<>();
        TableDefinitionTraversal.visit(table, expressions::add, t -> {
        });
        assertEquals(1, expressions.size());
        assertEquals("7", expressions.get(0).toString());
        roundTrip(table);
        assertEquals(2, CCJSqlParserUtil.parseStatements(table + "; SELECT 1",
                p -> p.withDialect(Dialect.POSTGRESQL)).size());
    }

    private static ColumnDefinition column(Statement statement) {
        return statement instanceof CreateTable
                ? ((CreateTable) statement).getColumnDefinitions().get(0)
                : ((Alter) statement).getAlterExpressions().get(0).getColDataTypeList().get(0);
    }

    private static Statement parse(String sql) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.POSTGRESQL));
    }

    private static void roundTrip(Statement statement) throws JSQLParserException {
        StringBuilder out = new StringBuilder();
        statement.accept(new StatementDeParser(out));
        assertEquals(statement.toString(), out.toString());
        assertEquals(out.toString(), parse(out.toString()).toString());
    }
}

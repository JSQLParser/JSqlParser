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
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.table.CheckConstraint;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.ColumnOption;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MySqlInlineCheckTest {
    @ParameterizedTest
    @ValueSource(strings = {"", " ENFORCED", " NOT ENFORCED"})
    void sharesChecksAcrossCreateAndColumnAlterations(String enforcement)
            throws JSQLParserException {
        for (Dialect dialect : new Dialect[] {null, Dialect.MYSQL}) {
            for (String prefix : new String[] {"CREATE TABLE t (", "ALTER TABLE t ADD COLUMN ",
                    "ALTER TABLE t MODIFY COLUMN ", "ALTER TABLE t CHANGE COLUMN old_a "}) {
                String sql = prefix + "a INT CONSTRAINT ck CHECK (a > 7)" + enforcement;
                if (prefix.startsWith("CREATE")) {
                    sql += ")";
                }
                Statement statement = parse(sql, dialect);
                ColumnDefinition column = statement instanceof CreateTable
                        ? ((CreateTable) statement).getColumnDefinitions().get(0)
                        : ((Alter) statement).getAlterExpressions().get(0).getColDataTypeList()
                                .get(0);
                assertEquals(1, column.getColumnOptions().size());
                ColumnOption option = column.getColumnOptions().get(0);
                assertEquals(ColumnOption.Kind.CONSTRAINT, option.getKind());
                CheckConstraint check =
                        assertInstanceOf(CheckConstraint.class, option.getConstraint());
                assertEquals("ck", check.getName());
                assertEquals(enforcement.isEmpty() ? null : !enforcement.contains("NOT"),
                        check.getEnforced());
                List<Long> seen = new ArrayList<>();
                ExpressionDeParser expression = new ExpressionDeParser() {
                    @Override
                    public <S> StringBuilder visit(LongValue value, S context) {
                        seen.add(value.getValue());
                        return getBuilder().append(value.getValue() + 100);
                    }
                };
                StringBuilder out = new StringBuilder();
                statement.accept(new StatementDeParser(expression, new SelectDeParser(), out));
                assertEquals(List.of(7L), seen, sql);
                assertTrue(out.toString().contains("a > 107"));
                assertTrue(statement.toString().contains("a > 7"));
                check.setEnforced(false);
                check.setExpression(CCJSqlParserUtil.parseExpression("a > 9"));
                roundTrip(statement, dialect);
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"CHECK (a > 0) NOT NULL", "CHECK (a > 0) CHECK (a < 100)",
            "CHECK (a > 0) NOT ENFORCED COMMENT 'check; note'"})
    void preservesFollowingColumnOptions(String tail) throws JSQLParserException {
        roundTrip(parse("CREATE TABLE t (a INT " + tail + ", b INT)", Dialect.MYSQL),
                Dialect.MYSQL);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " NO INHERIT"})
    void preservesPostgreSql18InlineCheckEnforcement(String inheritance)
            throws JSQLParserException {
        CreateTable table = (CreateTable) parse(
                "CREATE TABLE t (a INT CHECK (a > 0)" + inheritance + " NOT ENFORCED)",
                Dialect.POSTGRESQL);
        CheckConstraint check = assertInstanceOf(CheckConstraint.class,
                table.getColumnDefinitions().get(0).getColumnOptions().get(0).getConstraint());
        assertEquals(false, check.getEnforced());
        assertEquals(!inheritance.isEmpty(), check.isNoInherit());
        roundTrip(table, Dialect.POSTGRESQL);
    }

    private static Statement parse(String sql, Dialect dialect) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sql, p -> {
            if (dialect != null) {
                p.withDialect(dialect);
            }
        });
    }

    private static void roundTrip(Statement statement, Dialect dialect) throws JSQLParserException {
        StringBuilder out = new StringBuilder();
        statement.accept(new StatementDeParser(out));
        assertEquals(statement.toString(), out.toString());
        assertEquals(out.toString(), parse(out.toString(), dialect).toString());
    }
}

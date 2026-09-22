/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.CheckConstraint;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.util.TableDefinitionTraversal;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlCheckNoInheritTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "ALTER TABLE t ADD CONSTRAINT ck CHECK (id > 0) NO INHERIT",
            "ALTER TABLE t ADD CHECK (id > 0) NO INHERIT NOT VALID",
            "ALTER TABLE t ADD CONSTRAINT ck CHECK (id > 0) NO INHERIT NOT VALID, ADD COLUMN extra INT",
            "CREATE TABLE t (id INT, CONSTRAINT ck CHECK (id > 0) NO INHERIT)",
            "CREATE TABLE t (id INT CHECK (id > 0) NO INHERIT)",
            "CREATE TABLE t (id INT CONSTRAINT ck CHECK (id > 0) NO INHERIT)"})
    void roundTripsSharedCreateAndAlterCheck(String sql) throws JSQLParserException {
        Statement statement = parse(sql);
        assertEquals(sql, statement.toString());
        roundTrip(statement);
    }

    @Test
    void exposesIndependentInheritanceAndValidationFlags() throws JSQLParserException {
        Alter alter = (Alter) parse(
                "ALTER TABLE t ADD CONSTRAINT ck CHECK (id > 0) NO INHERIT NOT VALID");
        CheckConstraint constraint =
                (CheckConstraint) alter.getAlterExpressions().get(0).getIndex();
        assertTrue(constraint.isNoInherit());
        assertTrue(constraint.getConstraintAttributes().isNotValid());
        constraint.setNoInherit(false);
        constraint.setExpression(CCJSqlParserUtil.parseExpression("id > 10"));
        assertEquals("ALTER TABLE t ADD CONSTRAINT ck CHECK (id > 10) NOT VALID", alter.toString());
        roundTrip(alter);
        List<Expression> visited = new ArrayList<>();
        TableDefinitionTraversal.visit(alter.getAlterExpressions().get(0), visited::add, table -> {
        });
        assertEquals(List.of(constraint.getExpression()), visited);
        alter.getAlterExpressions().get(0).setIndex(new CheckConstraint().withName("new_ck")
                .withExpression(CCJSqlParserUtil.parseExpression("id > 20")).withNoInherit(true));
        assertEquals("ALTER TABLE t ADD CONSTRAINT new_ck CHECK (id > 20) NO INHERIT",
                alter.toString());
        roundTrip(alter);
    }

    @Test
    void preservesExistingCheckOptions() throws JSQLParserException {
        CreateTable table = (CreateTable) parse("CREATE TABLE t (id INT, CHECK (id > 0))");
        assertFalse(((CheckConstraint) table.getIndexes().get(0)).isNoInherit());
        for (String suffix : new String[] {"", " ENFORCED", " NOT ENFORCED"}) {
            String sql = "CREATE TABLE t (id INT, CHECK (id > 0)" + suffix + ")";
            assertEquals(sql, CCJSqlParserUtil.parse(sql).toString());
            assertEquals(sql,
                    CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.MYSQL)).toString());
        }
    }

    private static Statement parse(String sql) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.POSTGRESQL));
    }

    private static void roundTrip(Statement statement) throws JSQLParserException {
        StringBuilder sql = new StringBuilder();
        statement.accept(new StatementDeParser(sql), null);
        assertEquals(statement.toString(), sql.toString());
        assertEquals(sql.toString(), parse(sql.toString()).toString());
    }
}

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

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.table.*;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlColumnKeyMutationTest {
    @ParameterizedTest
    @ValueSource(strings = {"PRIMARY KEY", "UNIQUE", "UNIQUE NULLS NOT DISTINCT"})
    void keyNameAndAttributesAreOwnedByOneNode(String key) throws JSQLParserException {
        for (boolean alter : new boolean[] {false, true}) {
            String prefix = alter ? "ALTER TABLE t ADD COLUMN " : "CREATE TABLE t (";
            String suffix = alter ? "" : ")";
            Statement statement = parse(prefix + "id INT CONSTRAINT old_name " + key
                    + " DEFERRABLE INITIALLY DEFERRED NOT NULL" + suffix);
            ColumnDefinition column = alter
                    ? ((Alter) statement).getAlterExpressions().get(0).getColDataTypeList().get(0)
                    : ((CreateTable) statement).getColumnDefinitions().get(0);
            assertEquals(2, column.getColumnOptions().size());
            Index constraint = column.getColumnOptions().get(0).getConstraint();
            assertEquals("old_name", constraint.getName());
            assertTrue(constraint.getConstraintAttributes().getDeferrable());
            constraint.setName("new_name");
            constraint.getConstraintAttributes().setDeferrable(false);
            constraint.getConstraintAttributes()
                    .setInitially(ConstraintAttributes.Initially.IMMEDIATE);
            assertSql(statement, prefix + "id INT CONSTRAINT new_name " + key
                    + " NOT DEFERRABLE INITIALLY IMMEDIATE NOT NULL" + suffix);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREATE TABLE t (id INT CONSTRAINT old_fk REFERENCES parent(id))",
            "ALTER TABLE t ADD COLUMN id INT CONSTRAINT old_fk REFERENCES parent(id)"})
    void referenceNameCanBeReplacedAndRemoved(String sql) throws JSQLParserException {
        Statement statement = parse(sql);
        ForeignKeyReference reference = statement instanceof CreateTable
                ? ((CreateTable) statement).getColumnDefinitions().get(0).getForeignKeyReference()
                : ((Alter) statement).getAlterExpressions().get(0).getColDataTypeList().get(0)
                        .getForeignKeyReference();
        assertEquals("old_fk", reference.getConstraintName());
        reference.setConstraintName("new_fk");
        assertSql(statement, sql.replace("old_fk", "new_fk"));
        reference.setConstraintName(null);
        assertSql(statement, sql.replace("CONSTRAINT old_fk ", ""));
    }

    @ParameterizedTest
    @ValueSource(strings = {"NOT ENFORCED NO INHERIT", "NO INHERIT NOT ENFORCED",
            "NOT VALID NO INHERIT NOT ENFORCED", "NOT ENFORCED NOT VALID NO INHERIT"})
    void tableCheckAllowsInheritanceAmongAttributes(String attributes) throws JSQLParserException {
        Alter alter = (Alter) parse("ALTER TABLE t ADD CHECK (id > 0) " + attributes);
        CheckConstraint check = (CheckConstraint) alter.getAlterExpressions().get(0).getIndex();
        assertTrue(check.isNoInherit());
        assertFalse(check.getEnforced());
        assertEquals(attributes.contains("NOT VALID"),
                check.getConstraintAttributes().isNotValid());
        assertSql(alter, "ALTER TABLE t ADD CHECK (id > 0) NO INHERIT NOT ENFORCED"
                + (attributes.contains("NOT VALID") ? " NOT VALID" : ""));
    }

    private static Statement parse(String sql) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.POSTGRESQL));
    }

    private static void assertSql(Statement statement, String expected) throws JSQLParserException {
        assertEquals(expected, statement.toString());
        StringBuilder buffer = new StringBuilder();
        statement.accept(new StatementDeParser(buffer), null);
        assertEquals(expected, buffer.toString());
        assertEquals(expected, parse(expected).toString());
    }
}

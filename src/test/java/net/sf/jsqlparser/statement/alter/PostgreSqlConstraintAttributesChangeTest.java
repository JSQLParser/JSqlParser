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

import java.util.Set;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.ConstraintAttributes;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlConstraintAttributesChangeTest {
    @ParameterizedTest
    @ValueSource(strings = {"DEFERRABLE", "NOT DEFERRABLE", "INITIALLY DEFERRED",
            "INITIALLY IMMEDIATE", "DEFERRABLE INITIALLY DEFERRED",
            "INITIALLY DEFERRED DEFERRABLE", "ENFORCED", "NOT ENFORCED",
            "DEFERRABLE INITIALLY DEFERRED NOT ENFORCED",
            "NOT ENFORCED INITIALLY IMMEDIATE NOT DEFERRABLE"})
    void sharesCreateAddAndAlterAttributes(String attributes) throws JSQLParserException {
        String sql = "ALTER TABLE t ALTER CONSTRAINT fk " + attributes + ", ADD COLUMN z INT";
        Alter table = (Alter) parse(sql);
        AlterConstraintAttributes change = assertInstanceOf(AlterConstraintAttributes.class,
                table.getAlterExpressions().get(0));
        assertEquals(AlterOperation.ALTER, change.getOperation());
        assertEquals("CONSTRAINT", change.getConstraintType());
        assertEquals("fk", change.getConstraintSymbol());
        assertEquals("fk", change.getConstraintName());
        String declaration = "CONSTRAINT fk FOREIGN KEY(id) REFERENCES p(id) " + attributes;
        CreateTable create = (CreateTable) parse("CREATE TABLE t(id INT, "
                + declaration + ")");
        Alter add = (Alter) parse("ALTER TABLE t ADD " + declaration);
        assertEquals(create.getIndexes().get(0).getConstraintAttributes().toString(),
                change.getAttributes().toString());
        assertEquals(
                add.getAlterExpressions().get(0).getIndex().getConstraintAttributes().toString(),
                change.getAttributes().toString());
        assertEquals(2, table.getAlterExpressions().size());
        roundTrip(table);
        assertEquals(2, CCJSqlParserUtil.parseStatements(sql + "; SELECT 1").size());
    }

    @Test
    void legacyAndStructuredMutationsStayConsistent() throws JSQLParserException {
        Alter table = (Alter) parse("ALTER TABLE t ALTER CONSTRAINT fk ENFORCED");
        AlterConstraintAttributes action =
                (AlterConstraintAttributes) table.getAlterExpressions().get(0);
        action.setConstraintName("\"new fk\"");
        assertEquals("\"new fk\"", action.getConstraintSymbol());
        action.setEnforced(false);
        assertEquals(Boolean.FALSE, action.getAttributes().getEnforced());
        action.getAttributes().setDeferrable(true);
        action.getAttributes().setInitially(ConstraintAttributes.Initially.DEFERRED);
        assertEquals(
                "ALTER TABLE t ALTER CONSTRAINT \"new fk\" DEFERRABLE INITIALLY DEFERRED NOT ENFORCED",
                table.toString());
        action.getAttributes().setEnforced(true);
        assertTrue(action.isEnforced());
        assertEquals(Set.of("t"), new TablesNamesFinder().getTables(table));
        roundTrip(table);
    }

    @ParameterizedTest
    @ValueSource(strings = {"NOT VALID", "DEFERRABLE NOT VALID", "INITIALLY UNKNOWN",
            "DEFERRABLE NOT DEFERRABLE", "ENFORCED NOT ENFORCED",
            "INITIALLY DEFERRED INITIALLY IMMEDIATE", "DEFERRABLE,"})
    void rejectsWrongOrDuplicateAlterAttributes(String attributes) {
        assertThrows(JSQLParserException.class,
                () -> parse("ALTER TABLE t ALTER CONSTRAINT fk " + attributes));
    }

    private static net.sf.jsqlparser.statement.Statement parse(String sql)
            throws JSQLParserException {
        return CCJSqlParserUtil.parse(sql,
                parser -> parser.withDialect(
                        net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect.POSTGRESQL));
    }

    private static void roundTrip(Alter table) throws JSQLParserException {
        StringBuilder output = new StringBuilder();
        table.accept(new StatementDeParser(output));
        assertEquals(table.toString(), output.toString());
        assertEquals(output.toString(), parse(output.toString()).toString());
    }
}

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
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MySqlDropConstraintTest {
    @ParameterizedTest
    @ValueSource(strings = {"DROP FOREIGN KEY fk1", "DROP FOREIGN KEY `fk name`",
            "DROP CHECK c1", "DROP CHECK `check name`",
            "DROP FOREIGN KEY fk1, DROP CHECK c1",
            "DROP FOREIGN KEY fk1, ALGORITHM = INPLACE, LOCK = NONE"})
    void roundTripsNamedDrops(String actions) throws JSQLParserException {
        Alter alter = parse("ALTER TABLE t " + actions);
        assertEquals("ALTER TABLE t " + actions, alter.toString());
        assertRoundTrip(alter);
    }

    @Test
    void namesAndKindsAreEditableWithoutColumnLists() throws JSQLParserException {
        Alter alter = parse("ALTER TABLE t DROP FOREIGN KEY fk1, DROP CHECK c1");
        AlterExpression foreign = alter.getAlterExpressions().get(0);
        AlterExpression check = alter.getAlterExpressions().get(1);
        assertEquals(AlterOperation.DROP_FOREIGN_KEY, foreign.getOperation());
        assertEquals("fk1", foreign.getConstraintName());
        assertNull(foreign.getPkColumns());
        assertEquals(AlterOperation.DROP_CHECK, check.getOperation());
        assertEquals("c1", check.getConstraintName());
        check.setConstraintName("`new check`");
        foreign.setConstraintName("fk2");
        assertEquals("ALTER TABLE t DROP FOREIGN KEY fk2, DROP CHECK `new check`",
                alter.toString());
        assertRoundTrip(alter);
    }

    @Test
    void constructedActionsUseTheSameRendererAsParsedActions() throws JSQLParserException {
        for (AlterOperation operation : List.of(AlterOperation.DROP_FOREIGN_KEY,
                AlterOperation.DROP_CHECK)) {
            Alter alter = new Alter();
            alter.setTable(new Table("t"));
            alter.addAlterExpression(new AlterExpression().withOperation(operation)
                    .withConstraintName("`target constraint`"));
            Alter parsed = parse(alter.toString());
            assertEquals(operation, parsed.getAlterExpressions().get(0).getOperation());
            assertEquals("`target constraint`",
                    parsed.getAlterExpressions().get(0).getConstraintName());
            assertRoundTrip(alter);
            // Editing the operation on either concrete class must have the same effect.
            parsed.getAlterExpressions().get(0).setOperation(AlterOperation.DROP_CHECK);
            assertEquals("ALTER TABLE t DROP CHECK `target constraint`", parsed.toString());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"DROP FOREIGN KEY (id)", "DROP UNIQUE (a, b)",
            "DROP PRIMARY KEY", "DROP CONSTRAINT IF EXISTS ck CASCADE", "DROP (a, b)",
            "DROP COLUMN c", "DROP INDEX idx"})
    void preservesDefaultDropForms(String action) throws JSQLParserException {
        String sql = "ALTER TABLE t " + action;
        Alter alter = (Alter) CCJSqlParserUtil.parse(sql);
        assertEquals(sql, alter.toString().trim());
    }

    private static Alter parse(String sql) throws JSQLParserException {
        return (Alter) CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.MYSQL));
    }

    private static void assertRoundTrip(Alter alter) throws JSQLParserException {
        StringBuilder sql = new StringBuilder();
        alter.accept(new StatementDeParser(sql), null);
        assertEquals(alter.toString(), sql.toString());
        assertEquals(alter.toString(), parse(sql.toString()).toString());
    }
}

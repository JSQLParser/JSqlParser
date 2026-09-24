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
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.table.*;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlConstraintEnforcementTest {
    @ParameterizedTest
    @ValueSource(strings = {"ENFORCED", "NOT ENFORCED",
            "DEFERRABLE INITIALLY DEFERRED NOT ENFORCED",
            "NOT ENFORCED DEFERRABLE INITIALLY DEFERRED",
            "INITIALLY IMMEDIATE NOT DEFERRABLE ENFORCED"})
    void sharesForeignKeyAttributesAcrossCreateAndAlter(String attributes)
            throws JSQLParserException {
        for (String prefix : List.of("CREATE TABLE t (id INT, ", "ALTER TABLE t ADD ")) {
            Statement statement =
                    parse(prefix + "CONSTRAINT fk FOREIGN KEY (id) REFERENCES public.p (id) "
                            + "MATCH SIMPLE ON DELETE CASCADE " + attributes
                            + (prefix.startsWith("CREATE") ? ")" : ", ADD COLUMN extra INT"));
            ForeignKeyIndex fk = (ForeignKeyIndex) (statement instanceof CreateTable
                    ? ((CreateTable) statement).getIndexes().get(0)
                    : ((Alter) statement).getAlterExpressions().get(0).getIndex());
            assertEquals(!attributes.contains("NOT ENFORCED"),
                    fk.getConstraintAttributes().getEnforced());
            roundTrip(statement);
            fk.getConstraintAttributes().setEnforced(true);
            roundTrip(statement);
            assertFalse(statement.toString().contains("NOT ENFORCED"));
            fk.getConstraintAttributes().setEnforced(null);
            roundTrip(statement);
            assertFalse(statement.toString().contains("ENFORCED"));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"ENFORCED", "NOT ENFORCED DEFERRABLE INITIALLY DEFERRED"})
    void columnReferencesRetainTheirOwnAttributesAndFollowingOptions(String attributes)
            throws JSQLParserException {
        for (String prefix : List.of("CREATE TABLE t (", "ALTER TABLE t ADD COLUMN ")) {
            Statement statement = parse(prefix + "id INT REFERENCES public.p (id) " + attributes
                    + " NOT NULL"
                    + (prefix.startsWith("CREATE") ? ", value INT)" : ", ADD COLUMN value INT"));
            ColumnDefinition column = statement instanceof CreateTable
                    ? ((CreateTable) statement).getColumnDefinitions().get(0)
                    : ((Alter) statement).getAlterExpressions().get(0).getColDataTypeList().get(0);
            ForeignKeyReference reference =
                    column.getColumnOptions().get(0).getForeignKeyReference();
            assertNotNull(reference.getConstraintAttributes());
            assertEquals(!attributes.contains("NOT ENFORCED"),
                    reference.getConstraintAttributes().getEnforced());
            assertEquals(ColumnOption.Kind.NULLABILITY, column.getColumnOptions().get(1).getKind());
            roundTrip(statement);
            reference.getConstraintAttributes().setEnforced(null);
            roundTrip(statement);
        }
    }

    @Test
    void checkLegacyAccessorsUseTheSharedAttributeState() throws JSQLParserException {
        for (String prefix : List.of("CREATE TABLE t (id INT, ", "ALTER TABLE t ADD ")) {
            Statement statement = parse(prefix + "CHECK (id > 0) NOT ENFORCED"
                    + (prefix.startsWith("CREATE") ? ")" : " NOT VALID"));
            CheckConstraint check = (CheckConstraint) (statement instanceof CreateTable
                    ? ((CreateTable) statement).getIndexes().get(0)
                    : ((Alter) statement).getAlterExpressions().get(0).getIndex());
            assertEquals(false, check.getConstraintAttributes().getEnforced());
            check.getConstraintAttributes().setEnforced(true);
            assertEquals(true, check.getEnforced());
            check.setEnforced(false);
            assertEquals(false, check.getConstraintAttributes().getEnforced());
            roundTrip(statement);
            check.setEnforced(null);
            assertNull(check.getEnforced());
            roundTrip(statement);
        }
        assertNull(new CheckConstraint().withEnforced(null).getConstraintAttributes());
    }

    @Test
    void preservesAlterEnforcementAndStatementBoundaries() throws JSQLParserException {
        for (String flag : List.of("ENFORCED", "NOT ENFORCED")) {
            Alter alter = (Alter) parse("ALTER TABLE t ALTER CONSTRAINT fk " + flag);
            assertEquals(!flag.startsWith("NOT"), alter.getAlterExpressions().get(0).isEnforced());
            roundTrip(alter);
        }
        assertEquals(2, CCJSqlParserUtil.parseStatements(
                "CREATE TABLE t(id INT REFERENCES p NOT ENFORCED); SELECT 1").size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"FOREIGN KEY(id) REFERENCES p ENFORCED NOT ENFORCED",
            "FOREIGN KEY(id) REFERENCES p DEFERRABLE NOT DEFERRABLE",
            "FOREIGN KEY(id) REFERENCES p ENFORCED INITIALLY wrong",
            "PRIMARY KEY(id) NOT ENFORCED", "CHECK(id > 0) ENFORCED ENFORCED"})
    void rejectsMalformedAttributeTails(String constraint) {
        assertThrows(JSQLParserException.class,
                () -> parse("CREATE TABLE t(id INT, " + constraint + ")"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREATE TABLE t(id INT REFERENCES p ENFORCED NOT VALID)",
            "ALTER TABLE t ADD COLUMN id INT REFERENCES p NOT VALID"})
    void rejectsNotValidOnColumnReferences(String sql) {
        assertThrows(JSQLParserException.class, () -> parse(sql));
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

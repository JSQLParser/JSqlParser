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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.ReferentialAction;
import net.sf.jsqlparser.statement.ReferentialAction.Action;
import net.sf.jsqlparser.statement.ReferentialAction.Type;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.table.ConstraintAttributes;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.ForeignKeyConstraint;
import net.sf.jsqlparser.statement.create.table.ForeignKeyReference;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ForeignKeyReferenceMutationTest {
    @Test
    void keepsLegacyAndStructuredAccessOnTheSameValues() {
        ForeignKeyConstraint index = new ForeignKeyConstraint();
        Table table = new Table("parent");
        List<String> columns = new ArrayList<>(List.of("id"));
        index.setTable(table);
        index.setReferencedColumnNames(columns);
        index.setReferentialAction(Type.DELETE, Action.CASCADE);

        ForeignKeyReference reference = index.getReference();
        assertSame(table, reference.getTable());
        assertSame(columns, reference.getReferencedColumnNames());
        assertEquals(Action.CASCADE, reference.getReferentialAction(Type.DELETE).getAction());

        Table replacement = new Table("other_parent");
        reference.setTable(replacement);
        reference.setReferencedColumnNames(new ArrayList<>(List.of("other_id")));
        reference.setReferentialAction(Type.DELETE, Action.RESTRICT);
        assertSame(replacement, index.getTable());
        assertSame(reference.getReferencedColumnNames(), index.getReferencedColumnNames());
        assertEquals(Action.RESTRICT, index.getReferentialAction(Type.DELETE).getAction());

        index.addReferencedColumnNames("tenant_id");
        index.getReferencedColumnNames().remove("other_id");
        assertEquals(List.of("tenant_id"), reference.getReferencedColumnNames());
        index.removeReferentialAction(Type.DELETE);
        assertNull(reference.getReferentialAction(Type.DELETE));
        reference.getReferentialActions().add(new ReferentialAction(Type.UPDATE, Action.CASCADE));
        assertSame(reference.getReferentialAction(Type.UPDATE),
                index.getReferentialAction(Type.UPDATE));
    }

    @Test
    void preservesNullableLegacyFieldsAndAnEmptyReference() {
        ForeignKeyConstraint index = new ForeignKeyConstraint();
        index.setReference(null);
        assertNull(index.getTable());
        assertNull(index.getReferencedColumnNames());
        assertNull(index.getReferentialAction(Type.DELETE));
        assertNull(index.getMatchType());
        index.setTable(new Table("parent"));
        index.setReferencedColumnNames(new ArrayList<>(List.of("id")));
        index.setTable(null);
        index.setReferencedColumnNames(null);
        assertNull(index.getReference().getTable());
        assertNull(index.getReference().getReferencedColumnNames());
    }

    @Test
    void clearingReferenceRetainsLegacyValuesAndDiscardsReferenceOnlyOptions() {
        ForeignKeyReference reference = new ForeignKeyReference()
                .withTable(new Table("parent"))
                .withReferencedColumnNames(new ArrayList<>(List.of("id")))
                .withMatchType(ForeignKeyReference.MatchType.FULL)
                .withUsingPeriod(true)
                .withReferentialAction(Type.DELETE, Action.CASCADE);
        reference.setConstraintName("column_fk");
        reference.setConstraintAttributes(new ConstraintAttributes());
        ForeignKeyConstraint index = new ForeignKeyConstraint().withReference(reference);

        index.setReference(null);

        assertSame(reference.getTable(), index.getTable());
        assertSame(reference.getReferencedColumnNames(), index.getReferencedColumnNames());
        assertEquals(Action.CASCADE, index.getReferentialAction(Type.DELETE).getAction());
        assertNull(index.getMatchType());
        ForeignKeyReference detached = index.getReference();
        assertNotSame(reference, detached);
        assertFalse(detached.isUsingPeriod());
        assertNull(detached.getConstraintName());
        assertNull(detached.getConstraintAttributes());
    }

    @Test
    void clearingReferenceRetainsTheLatestStructuredValues() {
        ForeignKeyReference reference = new ForeignKeyReference()
                .withTable(new Table("parent"))
                .withReferencedColumnNames(new ArrayList<>(List.of("id")));
        ForeignKeyConstraint index = new ForeignKeyConstraint().withReference(reference);
        Table replacement = new Table("other_parent");
        List<String> columns = new ArrayList<>(List.of("other_id"));
        reference.setTable(replacement);
        reference.setReferencedColumnNames(columns);
        reference.setReferentialAction(Type.DELETE, Action.SET_NULL);
        reference.getReferentialAction(Type.DELETE).setColumnNames(List.of("child_id"));

        index.setReference(null);

        assertSame(replacement, index.getTable());
        assertSame(columns, index.getReferencedColumnNames());
        assertEquals(List.of("child_id"), index.getReferentialAction(Type.DELETE).getColumnNames());
        assertEquals(List.of("child_id"),
                index.getReference().getReferentialAction(Type.DELETE).getColumnNames());
    }

    @Test
    void accessingReferenceDoesNotReplaceActionsOrDiscardTheirColumns() {
        ForeignKeyConstraint index = new ForeignKeyConstraint();
        index.setReferentialAction(Type.DELETE, Action.SET_NULL);
        ReferentialAction action = index.getReferentialAction(Type.DELETE);
        action.setColumnNames(List.of("child_id"));

        assertSame(action, index.getReference().getReferentialAction(Type.DELETE));
        assertEquals(List.of("child_id"), index.getReferentialAction(Type.DELETE).getColumnNames());
        action.setAction(Action.SET_DEFAULT);
        assertEquals(Action.SET_DEFAULT,
                index.getReference().getReferentialAction(Type.DELETE).getAction());
    }

    @Test
    @SuppressWarnings("deprecation")
    void deprecatedActionSettersUpdateStructuredReference() {
        ForeignKeyConstraint index = new ForeignKeyConstraint();
        ForeignKeyReference reference = index.getReference();
        index.setOnDeleteReferenceOption("CASCADE");
        index.setOnUpdateReferenceOption("RESTRICT");
        assertEquals(Action.CASCADE, reference.getReferentialAction(Type.DELETE).getAction());
        assertEquals("RESTRICT", index.getOnUpdateReferenceOption());
        reference.setReferentialAction(Type.UPDATE, Action.CASCADE);
        assertEquals("CASCADE", index.getOnUpdateReferenceOption());
        index.setOnDeleteReferenceOption(null);
        index.setOnUpdateReferenceOption(null);
        assertNull(reference.getReferentialAction(Type.DELETE));
        assertNull(reference.getReferentialAction(Type.UPDATE));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void mutationsSurviveBothRenderersAndReparsing(boolean create) throws JSQLParserException {
        String prefix = create ? "CREATE TABLE child (parent_id INT, " : "ALTER TABLE child ADD ";
        Statement statement = CCJSqlParserUtil.parse(prefix
                + "CONSTRAINT fk FOREIGN KEY (parent_id) REFERENCES parent (id) ON DELETE CASCADE"
                + (create ? ")" : ""));
        ForeignKeyConstraint index = foreignKey(statement);
        ForeignKeyReference reference = index.getReference();
        reference.setTable(new Table("other_parent"));
        reference.getReferencedColumnNames().set(0, "other_id");
        index.setReferentialAction(Type.DELETE, Action.SET_NULL);
        index.getReferentialAction(Type.DELETE).setColumnNames(List.of("parent_id"));
        index.setMatchType(ForeignKeyReference.MatchType.SIMPLE);

        StringBuilder deparsed = new StringBuilder();
        statement.accept(new StatementDeParser(deparsed));
        assertEquals(statement.toString(), deparsed.toString());
        Statement reparsed = CCJSqlParserUtil.parse(deparsed.toString());
        assertEquals(deparsed.toString(), reparsed.toString());
        ForeignKeyConstraint result = foreignKey(reparsed);
        assertEquals("other_parent", result.getTable().getName());
        assertEquals(List.of("other_id"), result.getReferencedColumnNames());
        assertEquals(List.of("parent_id"),
                result.getReferentialAction(Type.DELETE).getColumnNames());
        assertEquals(ForeignKeyReference.MatchType.SIMPLE, result.getMatchType());
    }

    private static ForeignKeyConstraint foreignKey(Statement statement) {
        return (ForeignKeyConstraint) (statement instanceof CreateTable
                ? ((CreateTable) statement).getTableConstraints().get(0)
                : ((Alter) statement).getAlterExpressions().get(0).getConstraint());
    }
}

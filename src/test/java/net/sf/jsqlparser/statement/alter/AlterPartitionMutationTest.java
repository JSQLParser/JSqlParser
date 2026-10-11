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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.create.table.PartitionDefinition;
import net.sf.jsqlparser.statement.create.table.TablePartitioning;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;

class AlterPartitionMutationTest {

    @Test
    void legacyExpressionSetterChangesParsedPartitioning() throws JSQLParserException {
        Alter alter = parse("ALTER TABLE sales PARTITION BY HASH (id) PARTITIONS 8");
        AlterExpression legacy = action(alter);
        Column replacement = new Column("other");
        legacy.setPartitionExpression(replacement);

        assertSame(replacement, action(alter).getPartitioning().getExpression());
        assertRoundTrip(alter, "ALTER TABLE sales PARTITION BY HASH (other) PARTITIONS 8");
    }

    @Test
    void legacyColumnsReplaceExpressionsWithAppropriateSyntax() throws JSQLParserException {
        Alter alter = parse("ALTER TABLE sales PARTITION BY LINEAR HASH (id) PARTITIONS 8");
        AlterExpression legacy = action(alter);
        legacy.setPartitionType("key");
        legacy.setPartitionColumns(List.of("other"));

        assertNull(legacy.getPartitionExpression());
        assertFalse(action(alter).getPartitioning().isColumnsSyntax());
        assertRoundTrip(alter, "ALTER TABLE sales PARTITION BY LINEAR KEY (other) PARTITIONS 8");
    }

    @Test
    void legacyRangeColumnsAndExpressionSettersReplaceOneAnother() throws JSQLParserException {
        Alter alter = parse("ALTER TABLE sales PARTITION BY RANGE (id) "
                + "(PARTITION p0 VALUES LESS THAN (100), PARTITION pmax VALUES LESS THAN MAXVALUE)");
        AlterExpression legacy = action(alter);
        legacy.setPartitionColumns(List.of("other"));

        assertNull(legacy.getPartitionExpression());
        assertTrue(action(alter).getPartitioning().isColumnsSyntax());
        assertRoundTrip(alter, "ALTER TABLE sales PARTITION BY RANGE COLUMNS (other) "
                + "(PARTITION p0 VALUES LESS THAN (100), PARTITION pmax VALUES LESS THAN MAXVALUE)");

        legacy.setPartitionExpression(new Column("id"));
        assertNull(legacy.getPartitionColumns());
        assertFalse(action(alter).getPartitioning().isColumnsSyntax());
        assertRoundTrip(alter, "ALTER TABLE sales PARTITION BY RANGE (id) "
                + "(PARTITION p0 VALUES LESS THAN (100), PARTITION pmax VALUES LESS THAN MAXVALUE)");
    }

    @Test
    void legacyColumnNamesAreAMutableViewOfStructuredColumns() throws JSQLParserException {
        Alter alter = parse("ALTER TABLE sales PARTITION BY KEY (`id`, other) PARTITIONS 8");
        AlterExpressionPartition action = action(alter);
        List<String> names = action.getPartitionColumns();
        assertEquals("`id`", names.set(0, "`new_id`"));
        names.add("extra");
        assertEquals("other", names.remove(1));
        action.getPartitioning().getColumns().get(1).setColumnName("last_id");

        assertEquals(List.of("`new_id`", "last_id"), names);
        assertRoundTrip(alter,
                "ALTER TABLE sales PARTITION BY KEY (`new_id`, last_id) PARTITIONS 8");
        names.clear();
        assertTrue(action.getPartitioning().getColumns().isEmpty());
        names.add("id");
        assertRoundTrip(alter, "ALTER TABLE sales PARTITION BY KEY (id) PARTITIONS 8");
    }

    @Test
    void structuredMutationsAreVisibleThroughLegacyAccessors() throws JSQLParserException {
        Alter alter = parse("ALTER TABLE sales PARTITION BY HASH (id) PARTITIONS 8");
        AlterExpressionPartition action = action(alter);
        TablePartitioning partitioning = action.getPartitioning();
        partitioning.setType(TablePartitioning.Type.KEY);
        partitioning.setColumns(new ExpressionList<>(new Column("other")));

        assertEquals("KEY", action.getPartitionType());
        assertEquals(List.of("other"), action.getPartitionColumns());
        assertNull(action.getPartitionExpression());
        assertRoundTrip(alter, "ALTER TABLE sales PARTITION BY KEY (other) PARTITIONS 8");

        partitioning.setType(TablePartitioning.Type.HASH);
        Column id = new Column("id");
        partitioning.setExpression(id);
        assertSame(id, action.getPartitionExpression());
        assertNull(action.getPartitionColumns());
        assertRoundTrip(alter, "ALTER TABLE sales PARTITION BY HASH (id) PARTITIONS 8");
    }

    @Test
    void partitionDefinitionsShareTheStructuredList() throws JSQLParserException {
        Alter alter = parse("ALTER TABLE sales PARTITION BY RANGE (id) "
                + "(PARTITION p0 VALUES LESS THAN (100))");
        AlterExpressionPartition action = action(alter);
        List<PartitionDefinition> replacements = new ArrayList<>(action(parse(
                "ALTER TABLE sales ADD PARTITION (PARTITION p1 VALUES LESS THAN (200))"))
                .getPartitionDefinitions());
        action.setPartitionDefinitions(replacements);
        assertSame(replacements, action.getPartitioning().getPartitionDefinitions());
        action.addPartitionDefinitions(action(parse("ALTER TABLE sales ADD PARTITION "
                + "(PARTITION pmax VALUES LESS THAN MAXVALUE)")).getPartitionDefinitions());
        assertRoundTrip(alter, "ALTER TABLE sales PARTITION BY RANGE (id) "
                + "(PARTITION p1 VALUES LESS THAN (200), PARTITION pmax VALUES LESS THAN MAXVALUE)");

        action.getPartitioning().setPartitionDefinitions(null);
        assertNull(action.getPartitionDefinitions());
        action.setPartitionDefinitions(replacements);
        action.getPartitionDefinitions().remove(0);
        assertRoundTrip(alter, "ALTER TABLE sales PARTITION BY RANGE (id) "
                + "(PARTITION pmax VALUES LESS THAN MAXVALUE)");
    }

    @Test
    void reorganizeUsesTheSameDefinitionsAsTheStructuredClause() throws JSQLParserException {
        Alter alter = parse("ALTER TABLE sales PARTITION BY RANGE (id) "
                + "(PARTITION p0 VALUES LESS THAN (100))");
        AlterExpressionPartition action = action(alter);
        action.setOperation(AlterOperation.REORGANIZE_PARTITION);
        action.setPartitionNames(List.of("p_old"));
        action.getPartitioning().setPartitionDefinitions(action(parse(
                "ALTER TABLE sales ADD PARTITION (PARTITION p1 VALUES LESS THAN (200))"))
                .getPartitionDefinitions());
        assertRoundTrip(alter, "ALTER TABLE sales REORGANIZE PARTITION p_old INTO "
                + "(PARTITION p1 VALUES LESS THAN (200))");
    }

    @Test
    void clearingLegacyExpressionAlsoClearsAKeyListWithoutResurrectingOldKeys() {
        AlterExpressionPartition action = new AlterExpressionPartition()
                .withPartitioning(new TablePartitioning(TablePartitioning.Type.RANGE));
        ExpressionList<Expression> keys =
                new ExpressionList<>(new Column("id"), new Column("other"));
        action.setPartitionExpression(keys);
        assertSame(keys, action.getPartitioning().getExpressionList());
        assertSame(keys, action.getPartitionExpression());
        action.setPartitionColumns(null);
        assertSame(keys, action.getPartitionExpression());
        action.setPartitionExpression(null);
        assertNull(action.getPartitioning().getExpressionList());
        assertNull(action.getPartitionExpression());

        action.setPartitionColumns(List.of("id"));
        action.setPartitionExpression(null);
        assertEquals(List.of("id"), action.getPartitionColumns());
        action.setPartitionColumns(null);
        assertNull(action.getPartitionColumns());
    }

    @Test
    void replacingAndClearingPartitioningDoesNotLeaveLegacySnapshots() throws JSQLParserException {
        Alter alter = parse("ALTER TABLE sales PARTITION BY RANGE COLUMNS (id) "
                + "(PARTITION p0 VALUES LESS THAN (100))");
        AlterExpressionPartition action = action(alter);
        TablePartitioning replacement = new TablePartitioning(TablePartitioning.Type.HASH)
                .withExpression(new Column("other")).withPartitions(4L);
        action.setPartitioning(replacement);
        assertNull(action.getPartitionColumns());
        assertNull(action.getPartitionDefinitions());
        assertRoundTrip(alter, "ALTER TABLE sales PARTITION BY HASH (other) PARTITIONS 4");

        action.setPartitioning(null);
        assertNull(action.getPartitionType());
        assertNull(action.getPartitionExpression());
        assertNull(action.getPartitionColumns());
        assertNull(action.getPartitionDefinitions());
        action.setPartitionType("RANGE");
        action.setPartitionExpression(new Column("id"));
        action.setPartitionDefinitions(action(parse("ALTER TABLE sales ADD PARTITION "
                + "(PARTITION pmax VALUES LESS THAN MAXVALUE)")).getPartitionDefinitions());
        assertRoundTrip(alter, "ALTER TABLE sales PARTITION BY RANGE (id) "
                + "(PARTITION pmax VALUES LESS THAN MAXVALUE)");
    }

    @Test
    void quotedDotsRemainPartOfTheColumnNameInLegacySettersAndListEdits()
            throws JSQLParserException {
        Alter alter = parse("ALTER TABLE sales PARTITION BY KEY (id) PARTITIONS 8");
        AlterExpressionPartition action = action(alter);
        action.setPartitionColumns(List.of("`event.date`"));
        Column column = action.getPartitioning().getColumns().get(0);
        assertNull(column.getTable());
        assertEquals("`event.date`", column.getColumnName());
        assertRendering(alter, "ALTER TABLE sales PARTITION BY KEY (`event.date`) PARTITIONS 8");

        List<String> names = action.getPartitionColumns();
        assertEquals("`event.date`", names.set(0, "`renamed.value`"));
        names.add("`other.column`");
        assertEquals(List.of("`renamed.value`", "`other.column`"), names);
        action.getPartitioning().getColumns().forEach(item -> assertNull(item.getTable()));
        assertRendering(alter, "ALTER TABLE sales PARTITION BY KEY "
                + "(`renamed.value`, `other.column`) PARTITIONS 8");
    }

    private static Alter parse(String sql) throws JSQLParserException {
        return (Alter) CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(Dialect.MYSQL));
    }

    private static AlterExpressionPartition action(Alter alter) {
        return (AlterExpressionPartition) alter.getAlterExpressions().get(0);
    }

    private static void assertRoundTrip(Alter alter, String expected) throws JSQLParserException {
        assertRendering(alter, expected);
        assertEquals(expected, parse(alter.toString()).toString());
    }

    private static void assertRendering(Alter alter, String expected) {
        assertEquals(expected, alter.toString());
        StringBuilder output = new StringBuilder();
        alter.accept(new StatementDeParser(output), null);
        assertEquals(expected, output.toString());
    }
}

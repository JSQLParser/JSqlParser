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

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.TableMaintenanceStatement.Modifier;
import net.sf.jsqlparser.statement.create.table.PartitionDefinition;
import net.sf.jsqlparser.statement.create.table.TablePartitioning;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/** MySQL 8.4 {@code ALTER TABLE} partition syntax verified against the MySQL manual. */
public class MySQLAlterTablePartitionTest {

    private static final List<String> MYSQL84_PARTITION_OPERATIONS = List.of(
            "ALTER TABLE sales ADD PARTITION "
                    + "(PARTITION p2027 VALUES LESS THAN (2028) ENGINE = InnoDB)",
            "ALTER TABLE sales DROP PARTITION p2023, `p2024`",
            "ALTER TABLE sales DISCARD PARTITION p2024 TABLESPACE",
            "ALTER TABLE sales IMPORT PARTITION ALL TABLESPACE",
            "ALTER TABLE sales TRUNCATE PARTITION ALL",
            "ALTER TABLE sales COALESCE PARTITION 2",
            "ALTER TABLE sales REORGANIZE PARTITION p0, p1 INTO "
                    + "(PARTITION p_low VALUES LESS THAN (100), "
                    + "PARTITION p_high VALUES LESS THAN MAXVALUE)",
            "ALTER TABLE sales EXCHANGE PARTITION p0 WITH TABLE archive.sales_2024",
            "ALTER TABLE sales EXCHANGE PARTITION p0 WITH TABLE archive.sales_2024 "
                    + "WITH VALIDATION",
            "ALTER TABLE sales EXCHANGE PARTITION p0 WITH TABLE archive.sales_2024 "
                    + "WITHOUT VALIDATION",
            "ALTER TABLE sales ANALYZE PARTITION p0, p1",
            "ALTER TABLE sales CHECK PARTITION ALL",
            "ALTER TABLE sales OPTIMIZE PARTITION p0",
            "ALTER TABLE sales REBUILD PARTITION p0",
            "ALTER TABLE sales REPAIR PARTITION ALL",
            "ALTER TABLE sales REMOVE PARTITIONING",
            "ALTER TABLE clients ADD PARTITION PARTITIONS 6",
            "ALTER TABLE clients ADD PARTITION NO_WRITE_TO_BINLOG PARTITIONS 6",
            "ALTER TABLE clients ADD PARTITION LOCAL PARTITIONS 6",
            "ALTER TABLE sales ADD PARTITION NO_WRITE_TO_BINLOG "
                    + "(PARTITION p2027 VALUES LESS THAN (2028))",
            "ALTER TABLE sales ADD PARTITION LOCAL (PARTITION p2027 VALUES LESS THAN (2028))",
            "ALTER TABLE clients COALESCE PARTITION NO_WRITE_TO_BINLOG 2",
            "ALTER TABLE clients COALESCE PARTITION LOCAL 2",
            "ALTER TABLE sales REORGANIZE PARTITION NO_WRITE_TO_BINLOG p0 INTO "
                    + "(PARTITION p0a VALUES LESS THAN (50), "
                    + "PARTITION p0b VALUES LESS THAN (100))",
            "ALTER TABLE sales ANALYZE PARTITION NO_WRITE_TO_BINLOG p0, p1",
            "ALTER TABLE sales OPTIMIZE PARTITION LOCAL ALL",
            "ALTER TABLE sales REBUILD PARTITION NO_WRITE_TO_BINLOG ALL",
            "ALTER TABLE sales REPAIR PARTITION LOCAL p0");

    private static final List<String> MYSQL84_PARTITIONING_CLAUSES = List.of(
            "ALTER TABLE sales PARTITION BY HASH (id) PARTITIONS 8",
            "ALTER TABLE sales PARTITION BY LINEAR KEY ALGORITHM = 1 (id) PARTITIONS 4",
            "ALTER TABLE sales PARTITION BY RANGE (YEAR(created_at)) "
                    + "SUBPARTITION BY HASH (id) SUBPARTITIONS 2 "
                    + "(PARTITION p2026 VALUES LESS THAN (2027), "
                    + "PARTITION pmax VALUES LESS THAN MAXVALUE)",
            "ALTER TABLE sales PARTITION BY LIST COLUMNS (region, tier) "
                    + "(PARTITION p_eu VALUES IN (('eu', 1), ('eu', 2)), "
                    + "PARTITION p_us VALUES IN (('us', 1)))");

    private static Stream<String> mysql84PartitionOperations() {
        return MYSQL84_PARTITION_OPERATIONS.stream();
    }

    private static Stream<String> mysql84PartitioningClauses() {
        return MYSQL84_PARTITIONING_CLAUSES.stream();
    }

    @ParameterizedTest
    @MethodSource("mysql84PartitionOperations")
    public void testPartitionOperationsUseStructuredSubtype(String sql)
            throws JSQLParserException {
        assertInstanceOf(AlterExpressionPartition.class, parseExpression(sql));
        assertSqlCanBeParsedAndDeparsed(sql);
    }

    @ParameterizedTest
    @MethodSource("mysql84PartitioningClauses")
    public void testPartitionByUsesCreateTablePartitioningModel(String sql)
            throws JSQLParserException {
        AlterExpressionPartition expression = parseExpression(sql);
        assertEquals(AlterOperation.PARTITION_BY, expression.getOperation());
        assertNotNull(expression.getPartitioning());
        assertSqlCanBeParsedAndDeparsed(sql);
    }

    @Test
    public void testAddPartitionDefinitionAst() throws JSQLParserException {
        AlterExpressionPartition expression = parseExpression(
                "ALTER TABLE sales ADD PARTITION "
                        + "(PARTITION p2027 VALUES LESS THAN (2028) ENGINE = InnoDB "
                        + "COMMENT = 'archive')");

        assertEquals(AlterOperation.ADD_PARTITION, expression.getOperation());
        PartitionDefinition definition = expression.getPartitionDefinitions().get(0);
        assertEquals("p2027", definition.getPartitionName());
        assertEquals(PartitionDefinition.ValueOperator.LESS_THAN,
                definition.getValueOperator());
        assertEquals(List.of("2028"), definition.getValues());
        assertEquals("InnoDB", definition.getStorageEngine());
        assertEquals("'archive'", definition.getComment());
    }

    @Test
    public void testAllPartitionsAndCoalesceCountAst() throws JSQLParserException {
        AlterExpressionPartition all =
                parseExpression("ALTER TABLE sales TRUNCATE PARTITION ALL");
        assertTrue(all.isAllPartitions());
        assertEquals(List.of("ALL"), all.getPartitionNames());
        assertNull(all.getCoalescePartitionCount());

        AlterExpressionPartition coalesce =
                parseExpression("ALTER TABLE sales COALESCE PARTITION 2");
        assertFalse(coalesce.isAllPartitions());
        assertEquals(2, coalesce.getCoalescePartitionCount());
        assertEquals(2, coalesce.getCoalescePartitionNumber());
    }

    @Test
    public void testAddPartitionCountAst() throws JSQLParserException {
        AlterExpressionPartition expression =
                parseExpression("ALTER TABLE clients ADD PARTITION PARTITIONS 6");

        assertEquals(AlterOperation.ADD_PARTITION, expression.getOperation());
        assertEquals(6, expression.getPartitionCount());
        assertNull(expression.getPartitionDefinitions());
        assertNull(expression.getModifier());

        Alter alter = (Alter) CCJSqlParserUtil.parse(
                "ALTER TABLE clients ADD PARTITION LOCAL PARTITIONS 6",
                parser -> parser.withDialect(Dialect.MYSQL));
        AlterExpressionPartition local = assertInstanceOf(AlterExpressionPartition.class,
                alter.getAlterExpressions().get(0));
        assertEquals(6, local.getPartitionCount());
        assertEquals(Modifier.LOCAL, local.getModifier());
    }

    @Test
    public void testBinlogModifierAst() throws JSQLParserException {
        AlterExpressionPartition coalesce =
                parseExpression("ALTER TABLE clients COALESCE PARTITION NO_WRITE_TO_BINLOG 2");
        assertEquals(Modifier.NO_WRITE_TO_BINLOG, coalesce.getModifier());
        assertEquals(2, coalesce.getCoalescePartitionCount());

        AlterExpressionPartition analyze =
                parseExpression("ALTER TABLE sales ANALYZE PARTITION LOCAL ALL");
        assertEquals(Modifier.LOCAL, analyze.getModifier());
        assertTrue(analyze.isAllPartitions());

        AlterExpressionPartition definitions = parseExpression(
                "ALTER TABLE sales ADD PARTITION NO_WRITE_TO_BINLOG "
                        + "(PARTITION p2027 VALUES LESS THAN (2028))");
        assertEquals(Modifier.NO_WRITE_TO_BINLOG, definitions.getModifier());
        assertNull(definitions.getPartitionCount());
        assertEquals("p2027", definitions.getPartitionDefinitions().get(0).getPartitionName());
    }

    @Test
    public void testLocalRemainsAPartitionNameWithoutFollowingNames()
            throws JSQLParserException {
        AlterExpressionPartition single =
                parseExpression("ALTER TABLE sales ANALYZE PARTITION local");
        assertNull(single.getModifier());
        assertEquals(List.of("local"), single.getPartitionNames());

        AlterExpressionPartition list =
                parseExpression("ALTER TABLE sales ANALYZE PARTITION local, p1");
        assertNull(list.getModifier());
        assertEquals(List.of("local", "p1"), list.getPartitionNames());

        AlterExpressionPartition reorganize = parseExpression(
                "ALTER TABLE sales REORGANIZE PARTITION local INTO "
                        + "(PARTITION p0 VALUES LESS THAN (100))");
        assertNull(reorganize.getModifier());
        assertEquals(List.of("local"), reorganize.getPartitionNames());
    }

    @Test
    public void testAddPartitionFormsRequireAdd() {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(
                "ALTER TABLE sales MODIFY PARTITION (PARTITION p3 VALUES LESS THAN (2002))"));
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(
                "ALTER TABLE sales ALTER PARTITION (PARTITION p3 VALUES LESS THAN (2002))"));
    }

    @Test
    public void testLegacyCoalesceAccessorsShareThePartitionCount() {
        AlterExpressionPartition expression = new AlterExpressionPartition()
                .withOperation(AlterOperation.COALESCE_PARTITION);
        assertEquals("COALESCE PARTITION 0", expression.toString());

        expression.setCoalescePartitionNumber(3);
        assertEquals(3, expression.getPartitionCount());
        assertEquals(3, expression.getCoalescePartitionCount());
        assertEquals("COALESCE PARTITION 3", expression.toString());

        expression.setPartitionCount(4);
        assertEquals(4, expression.getCoalescePartitionNumber());
        expression.setCoalescePartitionCount(null);
        assertEquals(0, expression.getCoalescePartitionNumber());
    }

    @Test
    public void testExchangePartitionAst() throws JSQLParserException {
        AlterExpressionPartition expression = parseExpression(
                "ALTER TABLE sales EXCHANGE PARTITION `p0` WITH TABLE "
                        + "`archive`.`sales_2024` WITHOUT VALIDATION");

        assertEquals(List.of("`p0`"), expression.getPartitionNames());
        assertEquals("`archive`.`sales_2024`",
                expression.getExchangeTable().getFullyQualifiedName());
        assertEquals(AlterExpressionPartition.ExchangeValidationMode.WITHOUT,
                expression.getExchangeValidationMode());
        assertTrue(expression.isExchangePartitionWithoutValidation());
        assertFalse(expression.isExchangePartitionWithValidation());
        assertEquals("`archive`.`sales_2024`", expression.getExchangePartitionTableName());
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE sales " + expression);
    }

    @Test
    public void testPartitionByAst() throws JSQLParserException {
        AlterExpressionPartition expression = parseExpression(
                "ALTER TABLE sales PARTITION BY LIST COLUMNS (region, tier) "
                        + "(PARTITION p_eu VALUES IN (('eu', 1), ('eu', 2)), "
                        + "PARTITION p_us VALUES IN (('us', 1)))");

        TablePartitioning partitioning = expression.getPartitioning();
        assertEquals(TablePartitioning.Type.LIST, partitioning.getType());
        assertTrue(partitioning.isColumnsSyntax());
        assertEquals("region", partitioning.getColumns().get(0).getColumnName());
        assertEquals("tier", partitioning.getColumns().get(1).getColumnName());
        assertEquals(PartitionDefinition.ValueOperator.IN,
                partitioning.getPartitionDefinitions().get(0).getValueOperator());

        // Legacy accessors remain populated for source compatibility.
        assertEquals("LIST", expression.getPartitionType());
        assertEquals(List.of("region", "tier"), expression.getPartitionColumns());
        assertEquals(partitioning.getPartitionDefinitions(), expression.getPartitionDefinitions());
    }

    @Test
    public void testProgrammaticConstruction() {
        AlterExpressionPartition expression = new AlterExpressionPartition()
                .withOperation(AlterOperation.EXCHANGE_PARTITION)
                .addPartitionNames("p0")
                .withExchangeTable(new Table("archive", "sales_2024"))
                .withExchangeValidationMode(
                        AlterExpressionPartition.ExchangeValidationMode.WITHOUT);

        assertEquals("EXCHANGE PARTITION p0 WITH TABLE archive.sales_2024 WITHOUT VALIDATION",
                expression.toString());

        AlterExpressionPartition add = new AlterExpressionPartition()
                .withOperation(AlterOperation.ADD_PARTITION)
                .withModifier(Modifier.NO_WRITE_TO_BINLOG)
                .withPartitionCount(4);
        assertEquals("ADD PARTITION NO_WRITE_TO_BINLOG PARTITIONS 4", add.toString());
    }

    private static AlterExpressionPartition parseExpression(String sql)
            throws JSQLParserException {
        Alter alter = (Alter) CCJSqlParserUtil.parse(sql);
        return assertInstanceOf(AlterExpressionPartition.class,
                alter.getAlterExpressions().get(0));
    }
}

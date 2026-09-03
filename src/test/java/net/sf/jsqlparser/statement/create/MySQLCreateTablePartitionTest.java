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

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.PartitionDefinition;
import net.sf.jsqlparser.statement.create.table.TablePartitioning;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/** MySQL 8.4 {@code CREATE TABLE} partition syntax verified against MySQL 8.4.11. */
public class MySQLCreateTablePartitionTest {

    private static final List<String> MYSQL84_PARTITION_STATEMENTS = List.of(
            "CREATE TABLE mysql_partition_hash (id INT) "
                    + "PARTITION BY HASH (id) PARTITIONS 4",
            "CREATE TABLE mysql_partition_linear_hash (id INT) "
                    + "PARTITION BY LINEAR HASH (id) PARTITIONS 4",
            "CREATE TABLE mysql_partition_key (id INT) "
                    + "PARTITION BY LINEAR KEY ALGORITHM = 1 (id) PARTITIONS 4",
            "CREATE TABLE mysql_partition_range (id INT) PARTITION BY RANGE (id) "
                    + "(PARTITION p0 VALUES LESS THAN (10), "
                    + "PARTITION p1 VALUES LESS THAN (20), "
                    + "PARTITION pmax VALUES LESS THAN MAXVALUE)",
            "CREATE TABLE mysql_partition_range_columns (a INT, b INT) "
                    + "PARTITION BY RANGE COLUMNS (a, b) "
                    + "(PARTITION p0 VALUES LESS THAN (5, 12), "
                    + "PARTITION pmax VALUES LESS THAN (MAXVALUE, MAXVALUE))",
            "CREATE TABLE mysql_partition_list (id INT) PARTITION BY LIST (id) "
                    + "(PARTITION p0 VALUES IN (1, 2), "
                    + "PARTITION p1 VALUES IN (3, 4))",
            "CREATE TABLE mysql_partition_list_columns_multi "
                    + "(region VARCHAR (16), tier INT) "
                    + "PARTITION BY LIST COLUMNS (region, tier) "
                    + "(PARTITION p0 VALUES IN (('eu', 1), ('us', 2)), "
                    + "PARTITION p1 VALUES IN (('apac', 3)))",
            "CREATE TABLE mysql_partition_sub_count (id INT, d DATE) "
                    + "PARTITION BY RANGE (YEAR(d)) "
                    + "SUBPARTITION BY HASH (id) SUBPARTITIONS 2 "
                    + "(PARTITION p0 VALUES LESS THAN (2025), "
                    + "PARTITION pmax VALUES LESS THAN MAXVALUE)",
            "CREATE TABLE mysql_partition_sub_explicit (id INT, d DATE) "
                    + "PARTITION BY RANGE (YEAR(d)) "
                    + "SUBPARTITION BY LINEAR KEY ALGORITHM = 2 (id) "
                    + "(PARTITION p0 VALUES LESS THAN (2025) "
                    + "(SUBPARTITION p0s0 ENGINE = InnoDB, "
                    + "SUBPARTITION p0s1 ENGINE = InnoDB), "
                    + "PARTITION pmax VALUES LESS THAN MAXVALUE "
                    + "(SUBPARTITION pmaxs0 ENGINE = InnoDB, "
                    + "SUBPARTITION pmaxs1 ENGINE = InnoDB))",
            "CREATE TABLE mysql_partition_options (id INT) ENGINE = InnoDB "
                    + "PARTITION BY RANGE (id) "
                    + "(PARTITION p0 VALUES LESS THAN (10) ENGINE = InnoDB "
                    + "COMMENT = 'hot' MAX_ROWS = 100 MIN_ROWS = 1, "
                    + "PARTITION pmax VALUES LESS THAN MAXVALUE "
                    + "ENGINE InnoDB COMMENT 'cold')",
            "CREATE TABLE mysql_partition_key_empty "
                    + "(id INT NOT NULL PRIMARY KEY, name VARCHAR (20)) "
                    + "PARTITION BY KEY () PARTITIONS 2",
            "CREATE TABLE mysql_partition_hash_named (id INT) "
                    + "PARTITION BY HASH (id) PARTITIONS 2 "
                    + "(PARTITION p0 STORAGE ENGINE = InnoDB, "
                    + "PARTITION p1 ENGINE InnoDB)");

    private static Stream<String> mysql84PartitionStatements() {
        return MYSQL84_PARTITION_STATEMENTS.stream();
    }

    @ParameterizedTest
    @MethodSource("mysql84PartitionStatements")
    public void testMySQL84CreateTablePartitions(String sql) throws JSQLParserException {
        assertInstanceOf(CreateTable.class, assertSqlCanBeParsedAndDeparsed(sql));
    }

    @Test
    public void testHashAndKeyPartitioningAst() throws JSQLParserException {
        CreateTable hash = parse(MYSQL84_PARTITION_STATEMENTS.get(1));
        TablePartitioning hashPartitioning = hash.getPartitioning();
        assertEquals(TablePartitioning.Type.HASH, hashPartitioning.getType());
        assertTrue(hashPartitioning.isLinear());
        assertEquals("id", hashPartitioning.getExpression().toString());
        assertEquals(4L, hashPartitioning.getPartitions());

        CreateTable key = parse(MYSQL84_PARTITION_STATEMENTS.get(2));
        TablePartitioning keyPartitioning = key.getPartitioning();
        assertEquals(TablePartitioning.Type.KEY, keyPartitioning.getType());
        assertTrue(keyPartitioning.isLinear());
        assertEquals(1, keyPartitioning.getAlgorithm());
        assertTrue(keyPartitioning.isAlgorithmUseEquals());
        assertEquals("id", keyPartitioning.getColumns().get(0).getColumnName());

        CreateTable emptyKey = parse(MYSQL84_PARTITION_STATEMENTS.get(10));
        assertTrue(emptyKey.getPartitioning().getColumns().isEmpty());
    }

    @Test
    public void testRangeAndListPartitionDefinitionsAst() throws JSQLParserException {
        CreateTable range = parse(MYSQL84_PARTITION_STATEMENTS.get(4));
        TablePartitioning rangePartitioning = range.getPartitioning();
        assertEquals(TablePartitioning.Type.RANGE, rangePartitioning.getType());
        assertTrue(rangePartitioning.isColumnsSyntax());
        assertEquals("a", rangePartitioning.getColumns().get(0).getColumnName());
        assertEquals("b", rangePartitioning.getColumns().get(1).getColumnName());
        PartitionDefinition maxPartition = rangePartitioning.getPartitionDefinitions().get(1);
        assertEquals(PartitionDefinition.ValueOperator.LESS_THAN,
                maxPartition.getValueOperator());
        assertEquals(List.of("MAXVALUE", "MAXVALUE"), maxPartition.getValues());
        assertFalse(maxPartition.isMaxValue());

        CreateTable list = parse(MYSQL84_PARTITION_STATEMENTS.get(5));
        PartitionDefinition listPartition = list.getPartitioning().getPartitionDefinitions().get(0);
        assertEquals(PartitionDefinition.ValueOperator.IN, listPartition.getValueOperator());
        assertEquals(List.of("1", "2"), listPartition.getValues());
    }

    @Test
    public void testSubPartitioningAst() throws JSQLParserException {
        CreateTable createTable = parse(MYSQL84_PARTITION_STATEMENTS.get(8));
        TablePartitioning subPartitioning = createTable.getPartitioning().getSubPartitioning();
        assertNotNull(subPartitioning);
        assertEquals(TablePartitioning.Type.KEY, subPartitioning.getType());
        assertTrue(subPartitioning.isLinear());
        assertEquals(2, subPartitioning.getAlgorithm());
        assertEquals("id", subPartitioning.getColumns().get(0).getColumnName());

        PartitionDefinition first = createTable.getPartitioning().getPartitionDefinitions().get(0);
        assertEquals(2, first.getSubPartitionDefinitions().size());
        assertTrue(first.getSubPartitionDefinitions().get(0).isSubPartition());
        assertEquals("InnoDB", first.getSubPartitionDefinitions().get(0).getStorageEngine());
    }

    @Test
    public void testPartitionOptionsAst() throws JSQLParserException {
        CreateTable createTable = parse(MYSQL84_PARTITION_STATEMENTS.get(9));
        PartitionDefinition first = createTable.getPartitioning().getPartitionDefinitions().get(0);
        assertEquals("InnoDB", first.getStorageEngine());
        assertTrue(first.isStorageEngineUseEquals());
        assertEquals("'hot'", first.getComment());
        assertTrue(first.isCommentUseEquals());
        assertEquals("100", first.getMaxRows());
        assertEquals("1", first.getMinRows());

        PartitionDefinition max = createTable.getPartitioning().getPartitionDefinitions().get(1);
        assertTrue(max.isMaxValue());
        assertFalse(max.isStorageEngineUseEquals());
        assertFalse(max.isCommentUseEquals());

        PartitionDefinition namedHash = parse(MYSQL84_PARTITION_STATEMENTS.get(11))
                .getPartitioning().getPartitionDefinitions().get(0);
        assertTrue(namedHash.isStorageKeyword());
    }

    private static CreateTable parse(String sql) throws JSQLParserException {
        return (CreateTable) CCJSqlParserUtil.parse(sql);
    }
}

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
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.create.table.TablePartitioning;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;

class PartitionKeyMutationTest {
    @Test
    void replacingSingleKeyWithSeveralKeysChangesBothRenderers() throws JSQLParserException {
        CreateTable table = parse("CREATE TABLE t(id INT,other INT) PARTITION BY RANGE(id)",
                Dialect.POSTGRESQL);
        table.getPartitioning().setExpressionList(
                new ExpressionList<Expression>(new Column("other"), new Column("id")));
        assertRoundTrip(table,
                "CREATE TABLE t (id INT, other INT) PARTITION BY RANGE (other, id)",
                Dialect.POSTGRESQL);
        assertNull(table.getPartitioning().getExpression());
    }

    @Test
    void switchingHashToKeyUsesNewColumnNames() throws JSQLParserException {
        CreateTable table = parse("CREATE TABLE t(id INT,other INT) PARTITION BY HASH(id)",
                Dialect.MYSQL);
        TablePartitioning partition = table.getPartitioning();
        partition.setType(TablePartitioning.Type.KEY);
        partition.setColumns(new ExpressionList<>(new Column("other")));
        assertRoundTrip(table, "CREATE TABLE t (id INT, other INT) PARTITION BY KEY (other)",
                Dialect.MYSQL);
        partition.setType(TablePartitioning.Type.HASH);
        partition.setExpression(new Column("id"));
        assertNull(partition.getColumns());
        assertRoundTrip(table, "CREATE TABLE t (id INT, other INT) PARTITION BY HASH (id)",
                Dialect.MYSQL);
    }

    @Test
    void attributedKeysReplaceAndAreReplacedByPlainKeys() throws JSQLParserException {
        CreateTable table = parse("CREATE TABLE t(id INT,other INT) PARTITION BY RANGE(id,other)",
                Dialect.POSTGRESQL);
        TablePartitioning partition = table.getPartitioning();
        partition.setKeyColumns(
                List.of(new Index.ColumnParams("other").withOperatorClass("int4_ops")));
        assertNull(partition.getExpressionList());
        assertRoundTrip(table,
                "CREATE TABLE t (id INT, other INT) PARTITION BY RANGE (other int4_ops)",
                Dialect.POSTGRESQL);
        partition.setExpression(new Column("id"));
        assertNull(partition.getKeyColumns());
        assertRoundTrip(table, "CREATE TABLE t (id INT, other INT) PARTITION BY RANGE (id)",
                Dialect.POSTGRESQL);
        partition.setColumns(null);
        partition.setExpressionList(null);
        partition.setKeyColumns(null);
        assertRoundTrip(table, "CREATE TABLE t (id INT, other INT) PARTITION BY RANGE (id)",
                Dialect.POSTGRESQL);
    }

    private static CreateTable parse(String sql, Dialect dialect) throws JSQLParserException {
        return (CreateTable) CCJSqlParserUtil.parse(sql, p -> p.withDialect(dialect));
    }

    private static void assertRoundTrip(CreateTable table, String expected, Dialect dialect)
            throws JSQLParserException {
        assertEquals(expected, table.toString());
        StringBuilder output = new StringBuilder();
        table.accept(new StatementDeParser(output), null);
        assertEquals(expected, output.toString());
        assertEquals(expected, parse(output.toString(), dialect).toString());
    }
}

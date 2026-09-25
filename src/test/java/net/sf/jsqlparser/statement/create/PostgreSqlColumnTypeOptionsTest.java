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
import java.util.Arrays;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.table.*;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlColumnTypeOptionsTest {
    @ParameterizedTest
    @ValueSource(strings = {"INT", "TEXT", "NUMERIC(10,2)", "TIMESTAMP(3) WITH TIME ZONE",
            "DOUBLE PRECISION", "public.mood"})
    void arrayKeywordBelongsToDataType(String type) throws JSQLParserException {
        for (boolean alter : new boolean[] {false, true}) {
            for (String dimensions : new String[] {"", "[4]"}) {
                Statement statement = parse(definition(alter, "a " + type + " ARRAY" + dimensions));
                ColumnDefinition column = column(statement);
                ColDataType dataType = column.getColDataType();
                assertTrue(dataType.isUsingArrayKeyword());
                assertEquals(dimensions.isEmpty() ? Arrays.asList((Integer) null) : List.of(4),
                        dataType.getArrayData());
                assertNull(column.getColumnOptions());
                dataType.setArrayData(List.of(8));
                assertTrue(statement.toString().contains(" ARRAY[8]"));
                assertRoundTrip(statement);
                dataType.setUsingArrayKeyword(false);
                assertTrue(statement.toString().contains("[8]"));
                assertFalse(statement.toString().contains(" ARRAY"));
                assertRoundTrip(statement);
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"PLAIN", "EXTERNAL", "EXTENDED", "MAIN", "DEFAULT"})
    void storageAndCompressionAreSharedByCreateAndAddColumn(String storage)
            throws JSQLParserException {
        for (boolean alter : new boolean[] {false, true}) {
            Statement statement = parse(definition(alter,
                    "a TEXT STORAGE " + storage + " COMPRESSION pglz COLLATE \"C\" DEFAULT 'x'"));
            List<ColumnOption> options = column(statement).getColumnOptions();
            assertEquals(ColumnOption.Storage.valueOf(storage), options.get(0).getStorage());
            assertEquals("pglz", options.get(1).getCompression());
            options.get(0).setStorage(ColumnOption.Storage.EXTENDED);
            options.get(1).setCompression("default");
            assertTrue(statement.toString().contains("STORAGE EXTENDED COMPRESSION default"));
            assertRoundTrip(statement);
            options.remove(0);
            assertFalse(statement.toString().contains("STORAGE"));
            assertRoundTrip(statement);
        }
    }

    @Test
    void bracketArraysAndMysqlColumnStorageRemainValid() throws JSQLParserException {
        CreateTable table = (CreateTable) parse("CREATE TABLE t (a INT[3][4], b TEXT[])");
        assertEquals(List.of(3, 4),
                table.getColumnDefinitions().get(0).getColDataType().getArrayData());
        assertFalse(table.getColumnDefinitions().get(0).getColDataType().isUsingArrayKeyword());
        for (String value : new String[] {"DISK", "MEMORY"}) {
            Statement statement =
                    CCJSqlParserUtil.parse("CREATE TABLE t (a INT STORAGE " + value + ")",
                            p -> p.withDialect(Dialect.MYSQL));
            assertTrue(statement.toString().contains("STORAGE " + value));
        }
    }

    private static String definition(boolean alter, String column) {
        return alter ? "ALTER TABLE t ADD COLUMN " + column : "CREATE TABLE t (" + column + ")";
    }

    private static ColumnDefinition column(Statement statement) {
        return statement instanceof CreateTable
                ? ((CreateTable) statement).getColumnDefinitions().get(0)
                : ((Alter) statement).getAlterExpressions().get(0).getColDataTypeList().get(0);
    }

    private static Statement parse(String sql) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.POSTGRESQL));
    }

    private static void assertRoundTrip(Statement statement) throws JSQLParserException {
        StringBuilder sql = new StringBuilder();
        statement.accept(new StatementDeParser(sql), null);
        assertEquals(statement.toString(), sql.toString());
        assertEquals(statement.toString(), parse(sql.toString()).toString());
    }
}

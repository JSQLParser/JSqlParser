/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterExpressionTableOption;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.TableOption;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class MySqlSharedTableOptionTest {
    @ParameterizedTest
    @CsvSource(value = {"ENCRYPTION|'N'|ENCRYPTION", "PASSWORD|'ignored'|PASSWORD",
            "DATA DIRECTORY|'/tmp/data dir'|DATA_DIRECTORY",
            "INDEX DIRECTORY|'/tmp/index dir'|INDEX_DIRECTORY",
            "AUTO_INCREMENT|18446744073709551614|AUTO_INCREMENT"}, delimiter = '|',
            quoteCharacter = '"')
    void sharesCreateAndAlterModels(String name, String value, String kind)
            throws JSQLParserException {
        for (String equals : new String[] {" ", " = "}) {
            String optionSql = name + equals + value;
            CreateTable create =
                    (CreateTable) CCJSqlParserUtil.parse("CREATE TABLE t (id BIGINT) " + optionSql);
            Alter alter = (Alter) CCJSqlParserUtil
                    .parse("ALTER TABLE t " + optionSql + ", ADD COLUMN extra INT");
            AlterExpressionTableOption action = assertInstanceOf(AlterExpressionTableOption.class,
                    alter.getAlterExpressions().get(0));
            TableOption option = action.getStructuredTableOption();
            assertEquals(TableOption.Kind.valueOf(kind), option.getKind());
            assertEquals(create.getTableOptions().get(0).toString(), option.toString());
            assertEquals(value, option.getValue());
            assertEquals(equals.contains("="), action.getUseEqual());
            assertEquals(optionSql, action.getTableOption());
            assertEquals(2, alter.getAlterExpressions().size());
            roundTrip(create);
            roundTrip(alter);
            option.setUseEquals(!option.isUseEquals());
            assertEquals(option.isUseEquals(), action.getUseEqual());
            roundTrip(alter);
            action.setUseEqual(true);
            assertTrue(option.isUseEquals());
            action.setTableOption("ENCRYPTION 'N'");
            assertNull(action.getStructuredTableOption());
            assertEquals("ENCRYPTION 'N'", action.toString());
        }
    }

    @Test
    void commasAndQuerySourceStayOutsideOptions() throws JSQLParserException {
        CreateTable table = (CreateTable) CCJSqlParserUtil.parse(
                "CREATE TABLE t (id INT) ENGINE=InnoDB, ENCRYPTION='N', PASSWORD='a,b' AS SELECT 1 AS id");
        assertEquals(3, table.getTableOptions().size());
        assertNotNull(table.getSelect());
        table.getTableOptions().get(1).setValue("'Y'");
        assertTrue(table.getTableOptionsStrings().contains("'Y'"));
        roundTrip(table);
        table.setTableOptionsStrings(List.of("ENCRYPTION", "=", "'N'"));
        assertNull(table.getTableOptions());
        roundTrip(table);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ENCRYPTION=", "PASSWORD=1", "DATA DIRECTORY='/tmp' INDEX DIRECTORY=",
            "AUTO_INCREMENT=-1", "AUTO_INCREMENT='5'", "ENCRYPTION='N',"})
    void rejectsMissingOrWrongCreateValues(String option) {
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse("CREATE TABLE t (id INT) " + option));
    }

    @ParameterizedTest
    @ValueSource(strings = {"VECTOR", "VECTOR(0)", "VECTOR(1)", "VECTOR(2048)", "VECTOR(16383)"})
    void preservesMySql9VectorColumnsBeforeTableOptions(String type) throws JSQLParserException {
        CreateTable table = (CreateTable) CCJSqlParserUtil.parse(
                "CREATE TABLE t (v " + type + ") ENCRYPTION='N'");
        assertEquals(type, table.getColumnDefinitions().get(0).getColDataType().toString()
                .replace(" ", ""));
        assertEquals(TableOption.Kind.ENCRYPTION, table.getTableOptions().get(0).getKind());
        roundTrip(table);
    }

    private static void roundTrip(Statement statement) throws JSQLParserException {
        StringBuilder out = new StringBuilder();
        statement.accept(new StatementDeParser(out));
        assertEquals(statement.toString(), out.toString());
        assertEquals(out.toString(), CCJSqlParserUtil.parse(out.toString()).toString());
    }
}

/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.junit.jupiter.api.Test;

class MySqlProcedureAnalyseTest {

    @Test
    void parseWithoutParameters() throws JSQLParserException {
        Select select = (Select) assertSqlCanBeParsedAndDeparsed(
                "SELECT col1, col2 FROM heavy_table PROCEDURE ANALYSE()");

        MySqlProcedureAnalyse procedureAnalyse = select.getMySqlProcedureAnalyse();
        assertNotNull(procedureAnalyse);
        assertNull(procedureAnalyse.getMaxElements());
        assertNull(procedureAnalyse.getMaxMemory());
    }

    @Test
    void parseOneParameter() throws JSQLParserException {
        Select select = (Select) assertSqlCanBeParsedAndDeparsed(
                "SELECT col1 FROM heavy_table PROCEDURE ANALYSE(10)");

        MySqlProcedureAnalyse procedureAnalyse = select.getMySqlProcedureAnalyse();
        assertEquals("10", procedureAnalyse.getMaxElements().getStringValue());
        assertNull(procedureAnalyse.getMaxMemory());
    }

    @Test
    void parseTwoParametersAfterLimit() throws JSQLParserException {
        Select select = (Select) assertSqlCanBeParsedAndDeparsed(
                "SELECT col1 FROM heavy_table LIMIT 20 PROCEDURE ANALYSE(10, 256)");

        MySqlProcedureAnalyse procedureAnalyse = select.getMySqlProcedureAnalyse();
        assertEquals("10", procedureAnalyse.getMaxElements().getStringValue());
        assertEquals("256", procedureAnalyse.getMaxMemory().getStringValue());
    }

    @Test
    void keepAnalyseAndProcedureAvailableAsIdentifiers() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT analyse FROM heavy_table");
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM heavy_table procedure");
    }

    @Test
    void rejectInvalidForms() {
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse("SELECT * FROM t PROCEDURE ANALYSE"));
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse("SELECT * FROM t PROCEDURE ANALYSE(-1)"));
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse("SELECT * FROM t PROCEDURE ANALYSE(1, 2, 3)"));
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse("SELECT * FROM t PROCEDURE ANALYZE()"));
    }
}

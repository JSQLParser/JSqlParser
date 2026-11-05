/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.UtilityOption.ValueType;
import net.sf.jsqlparser.statement.maintenance.VacuumStatement;
import net.sf.jsqlparser.statement.maintenance.VacuumStatement.OptionKind;
import org.junit.jupiter.api.Test;

class UtilityOptionTest {

    private enum IndexCleanup {
        AUTO, ON, OFF
    }

    @Test
    void factoriesWriteTypedValues() {
        assertEquals("VERBOSE", new UtilityOption("VERBOSE").toString());
        assertEquals("SKIP_LOCKED TRUE", UtilityOption.of("SKIP_LOCKED", true).toString());
        assertEquals("PARALLEL 3", UtilityOption.of("PARALLEL", 3).toString());
        assertEquals("BUFFER_USAGE_LIMIT 'it''s'",
                UtilityOption.ofString("BUFFER_USAGE_LIMIT", "it's").toString());
        assertEquals(ValueType.WORD, UtilityOption.ofWord("INDEX_CLEANUP", "auto").getValueType());
        assertEquals(ValueType.BOOLEAN,
                UtilityOption.ofWord("INDEX_CLEANUP", "off").getValueType());
    }

    @Test
    void readsValuesAsPostgreSqlDoes() {
        UtilityOption option = new UtilityOption("VERBOSE");
        assertFalse(option.hasValue());
        assertNull(option.getBooleanValue());
        assertNull(option.getStringValue());
        assertNull(option.getEnumValue(IndexCleanup.class));

        assertEquals(Boolean.TRUE, option.withValue(ValueType.BOOLEAN, "on").getBooleanValue());
        assertEquals(Boolean.FALSE, option.withValue(ValueType.NUMBER, "0").getBooleanValue());
        assertNull(option.withValue(ValueType.NUMBER, "2").getBooleanValue());
        assertEquals(Boolean.TRUE, option.withValue(ValueType.STRING, "'true'").getBooleanValue());
        assertNull(option.withValue(ValueType.WORD, "yes").getBooleanValue());
        assertEquals(-4L, option.withValue(ValueType.NUMBER, "-4").getLongValue());
        assertNull(option.withValue(ValueType.STRING, "'4'").getLongValue());
        assertEquals("a\nb", option.withValue(ValueType.STRING, "E'a\\nb'").getStringValue());
        assertEquals(IndexCleanup.AUTO,
                option.withValue(ValueType.WORD, "auto").getEnumValue(IndexCleanup.class));

        option.removeValue();
        assertFalse(option.hasValue());
        assertThrows(IllegalArgumentException.class, () -> option.setValue(ValueType.WORD, null));
        assertThrows(NullPointerException.class, () -> option.setName(null));
    }

    @Test
    void namesAndKinds() {
        UtilityOption analyse = new UtilityOption("analyse");
        assertTrue(analyse.isNamed("ANALYZE"));
        assertEquals(OptionKind.ANALYZE, analyse.getKind(OptionKind.class));
        assertNull(new UtilityOption("FORMAT").getKind(OptionKind.class));
        assertEquals(VacuumStatement.OptionKind.PARALLEL,
                analyse.withName("Parallel").getKind(OptionKind.class));

        // PostgreSQL keeps the case of a quoted name and compares it with the lower-case name
        UtilityOption quoted = new UtilityOption("\"verbose\"");
        assertEquals(OptionKind.VERBOSE, quoted.getKind(OptionKind.class));
        assertTrue(quoted.isNamed("VERBOSE"));
        assertNull(quoted.withName("\"VERBOSE\"").getKind(OptionKind.class));
        assertNull(quoted.withName("\"analyse\"").getKind(OptionKind.class));
        assertEquals("a\"b", quoted.withValue(ValueType.WORD, "\"a\"\"b\"").getStringValue());
    }

    @Test
    void rendersParenthesizedOrKeywordForms() {
        String[] keywords = {"FULL", "VERBOSE"};
        StringBuilder builder = new StringBuilder("X");
        UtilityOption.appendOptions(builder, List.of(), false, keywords);
        assertEquals("X", builder.toString());

        builder = new StringBuilder("X");
        UtilityOption.appendOptions(builder,
                List.of(new UtilityOption("verbose"), new UtilityOption("full")), false, keywords);
        assertEquals("X full verbose", builder.toString());

        builder = new StringBuilder("X");
        UtilityOption.appendOptions(builder, List.of(UtilityOption.of("FULL", true)), false,
                keywords);
        assertEquals("X (FULL TRUE)", builder.toString());

        builder = new StringBuilder("X");
        UtilityOption.appendOptions(builder, List.of(new UtilityOption("SKIP_LOCKED")), false,
                keywords);
        assertEquals("X (SKIP_LOCKED)", builder.toString());

        // a quoted name would read as a table name without parentheses
        builder = new StringBuilder("X");
        UtilityOption.appendOptions(builder, List.of(new UtilityOption("\"full\"")), false,
                keywords);
        assertEquals("X (\"full\")", builder.toString());
        assertTrue(UtilityOption.isKeywordForm(List.of(), keywords));
        assertFalse(UtilityOption.isKeywordForm(
                List.of(new UtilityOption("FULL"), new UtilityOption("full")), keywords));
    }

    @Test
    void explainReadsTheSharedOptionList() throws JSQLParserException {
        for (String sql : List.of("EXPLAIN (ANALYZE, BUFFERS, FORMAT JSON) SELECT * FROM t",
                "EXPLAIN (ANALYZE false, COSTS 0, SERIALIZE TEXT, MEMORY) SELECT 1")) {
            assertEquals(sql, CCJSqlParserUtil.parse(sql).toString());
        }
        // EXPLAIN keeps its own option names and values, which accept no more than before
        for (String sql : List.of("EXPLAIN (FULL) SELECT 1", "EXPLAIN (COSTS 'on') SELECT 1",
                "EXPLAIN (FORMAT 'json') SELECT 1", "EXPLAIN (ANALYZE +1) SELECT 1",
                "EXPLAIN (FOO) SELECT 1", "EXPLAIN (\"costs\") SELECT 1",
                "EXPLAIN (COSTS \"on\") SELECT 1", "EXPLAIN (COSTS 1.0) SELECT 1",
                "EXPLAIN (COSTS -1) SELECT 1", "EXPLAIN (ANALYSE) SELECT 1")) {
            assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql), sql);
        }
    }
}

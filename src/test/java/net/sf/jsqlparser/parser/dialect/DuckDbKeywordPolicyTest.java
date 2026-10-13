/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser.dialect;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * With {@link Dialect#DUCKDB}, every DuckDB keyword parses in each name position exactly when
 * DuckDB 1.5.5 itself parses it ({@code duckdb-keyword-names.csv}), except the exemptions below.
 * Function names are not compared: DuckDB's grammar has dedicated syntax for many keyword-named
 * functions (COALESCE, NULLIF, IF, ...), so whether a call parses depends on the function and its
 * arguments rather than on keyword reservation.
 */
public class DuckDbKeywordPolicyTest {
    private static final Consumer<CCJSqlParser> DUCKDB =
            parser -> parser.withDialect(Dialect.DUCKDB);

    private static final Map<String, String> POSITIONS = new LinkedHashMap<>();
    static {
        POSITIONS.put("column", "SELECT %s FROM t");
        POSITIONS.put("qualified_column", "SELECT t.%s FROM t");
        POSITIONS.put("as_alias", "SELECT 1 AS %s");
        POSITIONS.put("bare_alias", "SELECT 1 %s");
        POSITIONS.put("table", "SELECT * FROM %s");
        POSITIONS.put("table_alias", "SELECT * FROM t %s");
        POSITIONS.put("cte", "WITH %1$s AS (SELECT 1) SELECT * FROM %1$s");
    }

    /**
     * Differences that do not come from keyword reservation: JSqlParser parses these words as
     * dedicated syntax at that position, or as a date unit.
     */
    private static final Map<String, List<String>> EXEMPTIONS = new HashMap<>();
    static {
        // constructors, casts and expressions: MAP {...}, STRUCT(...), TRY_CAST(...), COLUMNS(...)
        for (String keyword : Arrays.asList("map", "struct", "try_cast", "columns", "generated")) {
            EXEMPTIONS.put(keyword, Arrays.asList("column", "table", "table_alias", "cte"));
        }
        EXEMPTIONS.put("default", Arrays.asList("column"));
        EXEMPTIONS.put("prior", Arrays.asList("column"));
        // WITH RECURSIVE, FETCH ... WITH TIES, UPDATE ... SET, a new SELECT after AS
        EXEMPTIONS.put("recursive", Arrays.asList("cte"));
        EXEMPTIONS.put("ties", Arrays.asList("cte"));
        EXEMPTIONS.put("set", Arrays.asList("table_alias"));
        EXEMPTIONS.put("select", Arrays.asList("as_alias"));
        // 1 DAY is a date unit, not an alias
        for (String keyword : Arrays.asList("day", "hour", "minute", "month", "second", "year")) {
            EXEMPTIONS.put(keyword, Arrays.asList("bare_alias"));
        }
    }

    static Stream<Arguments> duckDbKeywordNames() throws IOException {
        List<Arguments> arguments = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                DuckDbKeywordPolicyTest.class.getResourceAsStream("duckdb-keyword-names.csv"),
                StandardCharsets.UTF_8))) {
            List<String> header = null;
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                if (line.startsWith("#")) {
                    continue;
                }
                List<String> cells = Arrays.asList(line.split(","));
                if (header == null) {
                    header = cells;
                    continue;
                }
                String keyword = cells.get(0);
                for (int i = 1; i < cells.size(); i++) {
                    String position = header.get(i);
                    if (!EXEMPTIONS.getOrDefault(keyword, List.of()).contains(position)) {
                        arguments.add(Arguments.of(keyword, position, "1".equals(cells.get(i))));
                    }
                }
            }
        }
        return arguments.stream();
    }

    @ParameterizedTest(name = "{0} as {1}: {2}")
    @MethodSource("duckDbKeywordNames")
    void parsesLikeDuckDb(String keyword, String position, boolean duckDbParses) {
        String sql = String.format(POSITIONS.get(position), keyword);
        boolean parses;
        try {
            CCJSqlParserUtil.parse(sql, DUCKDB);
            parses = true;
        } catch (JSQLParserException e) {
            parses = false;
        }
        assertEquals(duckDbParses, parses, sql);
    }

    @Test
    void keepsDefaultReservationWithoutTheDialect() throws JSQLParserException {
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse("SELECT settings, file FROM script"));
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM t PREWHERE a = 1 WHERE b = 2", true);
        assertSqlCanBeParsedAndDeparsed("SELECT 1 FROM t MINUS SELECT 2 FROM u", true);
        assertSqlCanBeParsedAndDeparsed("SELECT a desc FROM t", true);
    }

    @Test
    void parsesDuckDbSyntaxUsingItsKeywords() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT settings, file FROM script s WHERE s.top > 1", true,
                DUCKDB);
        assertSqlCanBeParsedAndDeparsed("UPDATE t SET a = 1", true, DUCKDB);
        assertSqlCanBeParsedAndDeparsed("SELECT a FROM t USING SAMPLE 10%", true, DUCKDB);
        assertSqlCanBeParsedAndDeparsed("COPY t TO 'out.csv' (FORMAT csv)", true, DUCKDB);
        assertSqlCanBeParsedAndDeparsed("SELECT a AS from, t.where FROM t ORDER BY a DESC", true,
                DUCKDB);
    }

    @Test
    void rejectsOnlyDuckDbReservedKeywordsAsFunctionNames() throws JSQLParserException {
        for (String sql : Arrays.asList("SELECT order(1)", "SELECT where(1)", "SELECT table(1)")) {
            assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, DUCKDB), sql);
        }
        assertSqlCanBeParsedAndDeparsed(
                "SELECT coalesce(a, b), nullif(a, b), if(a > 1, 1, 2), greatest(a, b) FROM t", true,
                DUCKDB);
    }

    @Test
    void rejectsDuckDbReservedKeywordsAsNames() {
        for (String sql : Arrays.asList("SELECT desc FROM t", "SELECT 1 name",
                "SELECT * FROM table")) {
            assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, DUCKDB), sql);
        }
    }
}

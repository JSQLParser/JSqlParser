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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.TableOption;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MySqlTableOptionsTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "STATS_PERSISTENT=1, STATS_AUTO_RECALC=1",
            "STATS_PERSISTENT DEFAULT, STATS_AUTO_RECALC=0 STATS_SAMPLE_PAGES=32",
            "ENGINE=InnoDB, DEFAULT CHARSET=utf8mb4",
            "ENGINE=InnoDB, COMMENT='commas, in text', AUTO_INCREMENT=10",
            "UNION=(t1,t2) ENGINE=MRG_MyISAM",
            "ENGINE=MRG_MyISAM, UNION (db.t1, `db`.`t2`)",
            "UNION=() ENGINE=MRG_MyISAM",
            "ENGINE=InnoDB, STATS_PERSISTENT=1 PARTITION BY HASH(id) PARTITIONS 2",
            "ENGINE=InnoDB, STATS_AUTO_RECALC=DEFAULT AS SELECT 1 AS other"
    })
    void parsesOptionSeparatorsAndValues(String options) throws JSQLParserException {
        CreateTable table = parse("CREATE TABLE t (id INT) " + options);
        assertRoundTrip(table);
        assertEquals(table.toString(), CCJSqlParserUtil.parse(table.toString()).toString());
    }

    @Test
    void statisticsOptionsHaveIndependentValues() throws JSQLParserException {
        CreateTable table = parse("CREATE TABLE t (id INT) "
                + "STATS_PERSISTENT=1, STATS_AUTO_RECALC=DEFAULT");
        TableOption persistent =
                table.getTableOption(TableOption.Kind.STATS_PERSISTENT).orElseThrow();
        assertEquals("1", persistent.getValue());
        persistent.setValue("0");
        assertThat(table.toString()).contains("STATS_PERSISTENT = 0 STATS_AUTO_RECALC = DEFAULT");
        assertRoundTrip(table);
    }

    @Test
    void unionSourcesAreTraversableAndMutable() throws JSQLParserException {
        CreateTable table =
                parse("CREATE TABLE merged (id INT) UNION=(db.t1,t2) ENGINE=MRG_MyISAM");
        TableOption union = table.getTableOption(TableOption.Kind.UNION).orElseThrow();
        assertEquals(2, union.getUnionTables().size());
        assertEquals("db", union.getUnionTables().get(0).getSchemaName());
        union.getUnionTables().get(0).setName("changed");
        assertEquals("(db.changed, t2)", union.getValue());
        assertThat(table.getTableOptionsStrings()).contains("(db.changed, t2)");
        assertThat(new TablesNamesFinder().getTables(table))
                .containsExactlyInAnyOrder("merged", "db.changed", "t2");
        assertRoundTrip(table);
        union.getUnionTables().clear();
        assertEquals("()", union.getValue());
        assertRoundTrip(table);
        union.setValue("(replacement)");
        assertEquals("(replacement)", union.getValue());
        assertRoundTrip(table);
    }

    @ParameterizedTest
    @ValueSource(strings = {", ENGINE=InnoDB", "ENGINE=InnoDB,", "ENGINE=InnoDB,, COMMENT='x'",
            "UNION=(t1,)", "UNION=", "STATS_PERSISTENT=2", "STATS_AUTO_RECALC=-1"})
    void rejectsIncompleteListsAndInvalidBooleanOptions(String options) {
        assertThrows(JSQLParserException.class, () -> parse("CREATE TABLE t (id INT) " + options));
    }

    private static CreateTable parse(String sql) throws JSQLParserException {
        return (CreateTable) CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.MYSQL));
    }

    private static void assertRoundTrip(CreateTable table) throws JSQLParserException {
        StringBuilder buffer = new StringBuilder();
        table.accept(new StatementDeParser(buffer), null);
        assertEquals(table.toString(), buffer.toString());
        assertEquals(table.toString(), parse(buffer.toString()).toString());
    }
}

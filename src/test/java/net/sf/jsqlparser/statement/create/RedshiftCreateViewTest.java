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

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.view.AutoRefreshOption;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.test.TestUtils;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RedshiftCreateViewTest {
    @ParameterizedTest
    @ValueSource(strings = {"", " BACKUP YES", " BACKUP NO"})
    void preserveBackupAndOmission(String clause) throws JSQLParserException {
        String sql = "CREATE MATERIALIZED VIEW myview" + clause + " AS SELECT * FROM mytab";
        CreateView view = (CreateView) TestUtils.assertSqlCanBeParsedAndDeparsed(sql);
        assertEquals(clause.isEmpty() ? null : clause.endsWith("YES"), view.getBackup());
        CreateView reparsed = (CreateView) CCJSqlParserUtil.parse(view.toString());
        assertEquals(view.getBackup(), reparsed.getBackup());
        assertTrue(TablesNamesFinder.findTables(sql).contains("mytab"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"YES", "NO"})
    void combineWithAutoRefresh(String value) throws JSQLParserException {
        CreateView view = (CreateView) TestUtils.assertSqlCanBeParsedAndDeparsed(
                "CREATE MATERIALIZED VIEW report.mv BACKUP NO AUTO REFRESH " + value
                        + " AS SELECT * FROM source");
        assertEquals(Boolean.FALSE, view.getBackup());
        assertEquals(AutoRefreshOption.from(value), view.getAutoRefresh());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE MATERIALIZED VIEW mv BACKUP AS SELECT 1",
            "CREATE MATERIALIZED VIEW mv BACKUP TRUE AS SELECT 1",
            "CREATE MATERIALIZED VIEW mv BACKUP YES BACKUP NO AS SELECT 1",
            "CREATE MATERIALIZED VIEW mv AUTO REFRESH YES BACKUP NO AS SELECT 1",
            "CREATE VIEW mv BACKUP NO AS SELECT 1"
    })
    void rejectMalformedOptions(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql,
                parser -> parser.withUnsupportedStatements(false)));
    }

    @Test
    void constructAndClearBackup() throws JSQLParserException {
        CreateView view = new CreateView().withMaterialized(true).withBackup(false)
                .withView(new Table("mv"))
                .withSelect((Select) CCJSqlParserUtil.parse("SELECT * FROM source"));
        TestUtils.assertStatementCanBeDeparsedAs(view,
                "CREATE MATERIALIZED VIEW mv BACKUP NO AS SELECT * FROM source");
        view.setBackup(null);
        TestUtils.assertStatementCanBeDeparsedAs(view,
                "CREATE MATERIALIZED VIEW mv AS SELECT * FROM source");
        view.setMaterialized(false);
        view.setBackup(true);
        assertThrows(IllegalArgumentException.class, view::validateOptions);
    }

    @Test
    void backupRemainsAnIdentifier() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "CREATE MATERIALIZED VIEW backup BACKUP YES AS SELECT backup FROM backup");
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT backup FROM backup");
    }

    @Test
    void lateBindingView() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "CREATE VIEW v AS (SELECT a FROM t) WITH NO SCHEMA BINDING");
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "CREATE OR REPLACE VIEW s.v (a) AS SELECT a FROM s.t WITH NO SCHEMA BINDING");
        CreateView view = (CreateView) CCJSqlParserUtil
                .parse("CREATE VIEW v AS SELECT 1 WITH NO SCHEMA BINDING");
        assertTrue(view.isWithNoSchemaBinding());
    }
}

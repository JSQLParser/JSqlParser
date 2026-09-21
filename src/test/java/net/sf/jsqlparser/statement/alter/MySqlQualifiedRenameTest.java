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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MySqlQualifiedRenameTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "ALTER TABLE db1.t RENAME TO db2.t2",
            "ALTER TABLE db1.t RENAME AS db2.t2",
            "ALTER TABLE db1.t RENAME db2.t2",
            "ALTER TABLE `db1`.`t` RENAME TO `db2`.`t2`",
            "ALTER TABLE db1.t RENAME TO `db.with.dot`.`table.with.dot`",
            "ALTER TABLE db1.t RENAME TO db2.t2, ADD COLUMN other INT",
            "ALTER TABLE db1.t RENAME TO t2"
    })
    void preservesQualifiedTargetsAndRenameKeywords(String sql) throws JSQLParserException {
        Alter alter = parse(sql);
        assertEquals(sql, alter.toString());
        assertRoundTrip(alter);
        assertEquals(sql, CCJSqlParserUtil.parse(sql).toString());
    }

    @Test
    void structuredTargetAndLegacyNameStayInSync() throws JSQLParserException {
        Alter alter = parse("ALTER TABLE db1.t RENAME TO db2.t2");
        AlterExpression rename = alter.getAlterExpressions().get(0);
        assertEquals("db2", rename.getNewTable().getSchemaName());
        assertEquals("t2", rename.getNewTable().getName());
        rename.getNewTable().setName("renamed");
        assertEquals("db2.renamed", rename.getNewTableName());
        assertThat(new TablesNamesFinder().getTables(alter))
                .containsExactlyInAnyOrder("db1.t", "db2.renamed");
        assertRoundTrip(alter);
        rename.setNewTableName("legacy_name");
        assertEquals("legacy_name", rename.getNewTable().getName());
        assertNull(rename.getNewTable().getSchemaName());
        assertRoundTrip(alter);
        rename.withNewTable(new Table("db3", "replacement"));
        assertEquals("db3.replacement", rename.getNewTableName());
        assertRoundTrip(alter);
        rename.setNewTableName(null);
        assertNull(rename.getNewTable());
        assertNull(rename.getNewTableName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"RENAME COLUMN old_name TO new_name", "RENAME INDEX old_idx TO new_idx",
            "RENAME KEY old_idx TO new_idx"})
    void doesNotConfuseOtherRenameOperations(String operation) throws JSQLParserException {
        Alter alter = parse("ALTER TABLE t " + operation);
        assertNull(alter.getAlterExpressions().get(0).getNewTable());
        assertRoundTrip(alter);
    }

    @ParameterizedTest
    @ValueSource(strings = {"RENAME TO", "RENAME AS", "RENAME TO db2.", "RENAME TO db2.t2."})
    void rejectsIncompleteTargetNames(String operation) {
        assertThrows(JSQLParserException.class, () -> parse("ALTER TABLE t " + operation));
    }

    private static Alter parse(String sql) throws JSQLParserException {
        return (Alter) CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.MYSQL));
    }

    private static void assertRoundTrip(Alter alter) throws JSQLParserException {
        StringBuilder buffer = new StringBuilder();
        alter.accept(new StatementDeParser(buffer), null);
        assertEquals(alter.toString(), buffer.toString());
        assertEquals(alter.toString(), parse(buffer.toString()).toString());
    }
}

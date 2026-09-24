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

import static org.junit.jupiter.api.Assertions.*;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.create.table.TableOption;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class IndexOptionBoundaryTest {
    @ParameterizedTest
    @ValueSource(strings = {"UNIQUE", "PRIMARY KEY"})
    void tablespaceBelongsToConstraintOptions(String key) throws JSQLParserException {
        Alter alter = (Alter) parse("ALTER TABLE t ADD CONSTRAINT k " + key
                + " (id) USING INDEX TABLESPACE pg_default DEFERRABLE", Dialect.POSTGRESQL);
        Index index = alter.getAlterExpressions().get(0).getIndex();
        assertEquals("pg_default", index.getTableSpace());
        assertNull(index.getUsing());
        assertTrue(index.getConstraintAttributes().getDeferrable());
        index.setTableSpace("other_space");
        assertEquals("ALTER TABLE t ADD CONSTRAINT k " + key
                + " (id) USING INDEX TABLESPACE other_space DEFERRABLE", alter.toString());
        assertRoundTrip(alter, Dialect.POSTGRESQL);
    }

    @ParameterizedTest
    @ValueSource(strings = {"SECONDARY_ENGINE_ATTRIBUTE = '{}'",
            "SECONDARY_ENGINE_ATTRIBUTE '{\"key\": \"value\"}'",
            "SECONDARY_ENGINE_ATTRIBUTE = '{}' VISIBLE COMMENT 'index'"})
    void mysqlAddIndexAttributes(String options) throws JSQLParserException {
        Alter alter = (Alter) parse("ALTER TABLE t ADD INDEX ix (id) " + options, Dialect.MYSQL);
        assertEquals("ix", alter.getAlterExpressions().get(0).getIndex().getName());
        assertEquals("ALTER TABLE t ADD  INDEX ix (id) " + options, alter.toString());
        assertRoundTrip(alter, Dialect.MYSQL);
    }

    @Test
    void sharedTableAttributeRemainsMutable() throws JSQLParserException {
        Alter alter =
                (Alter) parse("ALTER TABLE t SECONDARY_ENGINE_ATTRIBUTE = '{}'", Dialect.MYSQL);
        AlterExpressionTableOption action =
                (AlterExpressionTableOption) alter.getAlterExpressions().get(0);
        TableOption option = action.getStructuredTableOption();
        assertEquals(TableOption.Kind.SECONDARY_ENGINE_ATTRIBUTE, option.getKind());
        option.setValue("'{\"key\":1}'");
        assertEquals("ALTER TABLE t SECONDARY_ENGINE_ATTRIBUTE = '{\"key\":1}'", alter.toString());
        assertRoundTrip(alter, Dialect.MYSQL);
    }

    private static Statement parse(String sql, Dialect dialect) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sql, p -> p.withDialect(dialect));
    }

    private static void assertRoundTrip(Statement statement, Dialect dialect)
            throws JSQLParserException {
        StringBuilder sql = new StringBuilder();
        statement.accept(new StatementDeParser(sql), null);
        assertEquals(statement.toString(), sql.toString());
        assertEquals(statement.toString(), parse(sql.toString(), dialect).toString());
    }
}

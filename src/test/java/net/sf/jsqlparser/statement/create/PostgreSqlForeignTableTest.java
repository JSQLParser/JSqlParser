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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterForeignDataOptions;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.ForeignDataOption;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.util.TableDefinitionTraversal;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlForeignTableTest {
    @ParameterizedTest
    @ValueSource(strings = {"CREATE FOREIGN TABLE ft(id INT,label TEXT) SERVER ddl_audit_server",
            "CREATE FOREIGN TABLE ft(id INT OPTIONS(column_name 'remote_id'),label TEXT) SERVER ddl_audit_server OPTIONS(schema_name 'remote',table_name 'remote_t')",
            "CREATE FOREIGN TABLE ft(id INT CONSTRAINT nn NOT NULL NO INHERIT,label TEXT) SERVER ddl_audit_server",
            "CREATE FOREIGN TABLE ft(id INT,label TEXT,CONSTRAINT ck CHECK(id>0)) SERVER ddl_audit_server",
            "CREATE FOREIGN TABLE ft(LIKE t INCLUDING ALL) SERVER ddl_audit_server",
            "CREATE FOREIGN TABLE ft PARTITION OF t FOR VALUES FROM(0) TO(100) SERVER ddl_audit_server",
            "ALTER FOREIGN TABLE ft ADD COLUMN extra INT DEFAULT 1",
            "ALTER FOREIGN TABLE ft DROP COLUMN IF EXISTS label",
            "ALTER FOREIGN TABLE ft ALTER COLUMN id TYPE BIGINT",
            "ALTER FOREIGN TABLE ft ALTER COLUMN id SET DEFAULT 2",
            "ALTER FOREIGN TABLE ft ALTER COLUMN id SET NOT NULL",
            "ALTER FOREIGN TABLE ft ALTER COLUMN id SET STATISTICS 200",
            "ALTER FOREIGN TABLE ft ALTER COLUMN label SET STORAGE MAIN",
            "ALTER FOREIGN TABLE ft ALTER COLUMN id OPTIONS(SET column_name 'new_id')",
            "ALTER FOREIGN TABLE ft OPTIONS(SET table_name 'renamed', ADD schema_name 'public')",
            "ALTER FOREIGN TABLE ft RENAME COLUMN id TO new_id",
            "ALTER FOREIGN TABLE ft RENAME TO ft2",
            "ALTER FOREIGN TABLE ft SET SCHEMA ddl_aux",
            "ALTER FOREIGN TABLE ft ADD CONSTRAINT ck CHECK(id>0) NOT VALID",
            "ALTER FOREIGN TABLE ft VALIDATE CONSTRAINT ck",
            "DROP FOREIGN TABLE ft",
            "DROP FOREIGN TABLE IF EXISTS ft CASCADE",
            "ALTER FOREIGN TABLE IF EXISTS ONLY ft ALTER COLUMN id SET DEFAULT 2",
            "ALTER FOREIGN TABLE ft * ALTER COLUMN id SET DEFAULT 2",
            "ALTER FOREIGN TABLE ft OPTIONS(DROP table_name)",
            "ALTER FOREIGN TABLE ft ALTER id OPTIONS(column_name 'remote')",
            "ALTER FOREIGN TABLE ft ADD COLUMN id2 INT OPTIONS(column_name 'remote2')",
            "CREATE FOREIGN TABLE ft() SERVER srv",
            "DROP FOREIGN TABLE ft, public.ft2 RESTRICT"})
    void foreignDefinitionsAndActionsRoundTrip(String sql) throws JSQLParserException {
        Statement statement = parse(sql);
        assertRoundTrip(statement);
        assertEquals(2, CCJSqlParserUtil.parseStatements(sql + "; SELECT 1",
                p -> p.withDialect(Dialect.POSTGRESQL)).size());
        if (statement instanceof CreateTable) {
            assertNotNull(((CreateTable) statement).getForeignTableOptions());
        } else if (statement instanceof Alter) {
            assertTrue(((Alter) statement).isForeignTable());
        } else {
            assertEquals(Drop.ObjectType.FOREIGN_TABLE, ((Drop) statement).getObjectType());
        }
    }

    @Test
    void serverAndColumnOptionsAreMutableAndVisited() throws JSQLParserException {
        CreateTable table = (CreateTable) parse(
                "CREATE FOREIGN TABLE ft(id INT OPTIONS(column_name 'remote')) SERVER srv OPTIONS(table_name 'source')");
        table.getForeignTableOptions().setServer("new_srv");
        table.getForeignTableOptions().getOptions().get(0).setValue(new StringValue("target"));
        table.getColumnDefinitions().get(0).getColumnOptions().get(0).getForeignOptions()
                .get(0).setValue(new StringValue("remote_id"));
        assertEquals(
                "CREATE FOREIGN TABLE ft (id INT OPTIONS (column_name 'remote_id')) SERVER new_srv OPTIONS (table_name 'target')",
                table.toString());
        List<Expression> visited = new ArrayList<>();
        TableDefinitionTraversal.visit(table, visited::add, ignored -> {
        });
        assertEquals(2, visited.size());
        assertEquals(Set.of("ft"), new TablesNamesFinder().getTables(table));
        assertRoundTrip(table);
    }

    @Test
    void replacingLegacyOptionsRemovesTheOldServer() throws JSQLParserException {
        CreateTable table = (CreateTable) parse(
                "CREATE FOREIGN TABLE ft(id INT) SERVER old_srv OPTIONS(table_name 'old')");
        assertTrue(table.getTableOptionsStrings().contains("SERVER"));
        table.setTableOptionsStrings(List.of("SERVER", "new_srv"));
        assertNull(table.getForeignTableOptions());
        assertEquals("CREATE FOREIGN TABLE ft (id INT) SERVER new_srv", table.toString());
        assertRoundTrip(table);
    }

    @Test
    void optionActionChangesDropStaleValues() throws JSQLParserException {
        Alter alter = (Alter) parse(
                "ALTER FOREIGN TABLE ft ALTER COLUMN id OPTIONS(SET column_name 'old')");
        AlterForeignDataOptions action =
                (AlterForeignDataOptions) alter.getAlterExpressions().get(0);
        assertEquals("id", action.getColumnName());
        ForeignDataOption option = action.getOptions().get(0);
        option.setAction(ForeignDataOption.Action.DROP);
        assertNull(option.getValue());
        assertEquals("ALTER FOREIGN TABLE ft ALTER COLUMN id OPTIONS (DROP column_name)",
                alter.toString());
        assertRoundTrip(alter);
        option.setAction(ForeignDataOption.Action.ADD);
        option.setValue(new StringValue("new"));
        List<Expression> visited = new ArrayList<>();
        TableDefinitionTraversal.visit(action, visited::add, ignored -> {
        });
        assertEquals(1, visited.size());
        assertRoundTrip(alter);
        Drop drop = (Drop) parse("DROP FOREIGN TABLE ft, other.ft2 CASCADE");
        assertEquals(Set.of("ft", "other.ft2"), new TablesNamesFinder().getTables(drop));
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREATE FOREIGN TABLE ft(id INT)",
            "CREATE FOREIGN TABLE ft(id INT) SERVER srv OPTIONS(ADD name 'value')",
            "CREATE FOREIGN TABLE ft(id INT OPTIONS(column_name=1)) SERVER srv",
            "ALTER FOREIGN TABLE ft OPTIONS(SET name)",
            "ALTER FOREIGN TABLE ft OPTIONS(DROP name 'value')",
            "ALTER FOREIGN TABLE ft OPTIONS()", "ALTER TABLE t OPTIONS(name 'value')",
            "ALTER FOREIGN TABLE ONLY ft * ADD COLUMN extra INT"})
    void invalidForeignOptionsFail(String sql) {
        assertThrows(JSQLParserException.class, () -> parse(sql));
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

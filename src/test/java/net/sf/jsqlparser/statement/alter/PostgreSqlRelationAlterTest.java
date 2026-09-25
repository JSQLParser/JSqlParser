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
import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.util.TableDefinitionTraversal;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlRelationAlterTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "ALTER TABLE t CLUSTER ON ix", "ALTER TABLE t SET WITHOUT CLUSTER",
            "ALTER TABLE t SET WITHOUT OIDS", "ALTER TABLE t SET LOGGED",
            "ALTER TABLE t SET UNLOGGED",
            "ALTER TABLE t OF public.typ", "ALTER TABLE t NOT OF",
            "ALTER TABLE t ENABLE ALWAYS TRIGGER trg", "ALTER TABLE t ENABLE REPLICA TRIGGER trg",
            "ALTER TABLE t DISABLE TRIGGER ALL", "ALTER TABLE t ENABLE TRIGGER USER",
            "ALTER TABLE t ENABLE TRIGGER \"ALL\"",
            "ALTER MATERIALIZED VIEW mv CLUSTER ON ix",
            "ALTER MATERIALIZED VIEW mv SET WITHOUT CLUSTER",
            "ALTER INDEX ix RENAME TO ix2",
            "ALTER INDEX ix SET TABLESPACE pg_default",
            "ALTER INDEX ix SET (fillfactor=80)",
            "ALTER INDEX ix RESET (fillfactor)",
            "ALTER INDEX ix ALTER COLUMN 1 SET STATISTICS 200",
            "ALTER INDEX ix ALTER 1 SET STATISTICS 200",
            "ALTER INDEX IF EXISTS ix RENAME TO ix2",
            "ALTER INDEX IF EXISTS ix SET TABLESPACE pg_default",
            "ALTER INDEX IF EXISTS ix SET (fillfactor=80)",
            "ALTER INDEX IF EXISTS ix RESET (fillfactor)",
            "ALTER INDEX IF EXISTS ix ALTER COLUMN 1 SET STATISTICS 200",
            "ALTER INDEX IF EXISTS ix ALTER 1 SET STATISTICS 200",
            "ALTER INDEX ix ATTACH PARTITION child_ix",
            "ALTER VIEW v ALTER COLUMN id SET DEFAULT 1",
            "ALTER VIEW v ALTER COLUMN id DROP DEFAULT",
            "ALTER VIEW v SET (security_invoker=true)",
            "ALTER VIEW v RESET (security_invoker)",
            "ALTER VIEW v OWNER TO CURRENT_USER",
            "ALTER VIEW v RENAME COLUMN id TO new_id",
            "ALTER VIEW v RENAME TO v2",
            "ALTER VIEW v SET SCHEMA ddl_aux",
            "ALTER MATERIALIZED VIEW mv RENAME TO mv2",
            "ALTER MATERIALIZED VIEW mv RENAME COLUMN id TO new_id",
            "ALTER MATERIALIZED VIEW mv OWNER TO CURRENT_USER",
            "ALTER MATERIALIZED VIEW mv SET SCHEMA ddl_aux",
            "ALTER MATERIALIZED VIEW mv SET (fillfactor=80)",
            "ALTER MATERIALIZED VIEW mv RESET (fillfactor)",
            "ALTER MATERIALIZED VIEW mv SET TABLESPACE pg_default",
            "ALTER MATERIALIZED VIEW mv SET ACCESS METHOD heap",
            "ALTER MATERIALIZED VIEW mv ALTER COLUMN id SET STATISTICS 200",
            "ALTER MATERIALIZED VIEW mv ALTER COLUMN label SET STORAGE EXTENDED",
            "ALTER TABLE t ALTER COLUMN gen DROP EXPRESSION IF EXISTS",
            "ALTER TABLE t ALTER CONSTRAINT ck INHERIT",
            "ALTER TABLE t ALTER CONSTRAINT ck NO INHERIT",
            "ALTER TABLE t ALTER CONSTRAINT nn INHERIT",
            "ALTER TABLE t ALTER CONSTRAINT nn NO INHERIT",
            "ALTER TABLE t VALIDATE CONSTRAINT ck",
            "ALTER TABLE t SET (toast.autovacuum_enabled=false,fillfactor=70)",
            "ALTER TABLE t RESET(toast.autovacuum_enabled,fillfactor)",
            "ALTER TABLE t INHERIT p",
            "ALTER TABLE t NO INHERIT p",
            "ALTER TABLE t REPLICA IDENTITY USING INDEX ix",
            "ALTER TABLE t SET ACCESS METHOD heap"})
    void auditedActionsAreStructuredAndRoundTrip(String sql) throws JSQLParserException {
        Statement statement = parse(sql);
        if (statement instanceof Alter) {
            assertInstanceOf(RelationAlterAction.class,
                    ((Alter) statement).getAlterExpressions().get(0));
        } else {
            AlterRelation relation = assertInstanceOf(AlterRelation.class, statement);
            assertFalse(relation.getActions().isEmpty());
        }
        assertRoundTrip(statement);
        assertEquals(2, CCJSqlParserUtil.parseStatements(sql + "; SELECT 1",
                p -> p.withDialect(Dialect.POSTGRESQL)).size());
    }

    @Test
    void indexNameAndActionParametersCanBeReplaced() throws JSQLParserException {
        AlterRelation index =
                (AlterRelation) parse("ALTER INDEX IF EXISTS ix ALTER COLUMN 1 SET STATISTICS 200");
        assertEquals(AlterRelation.ObjectType.INDEX, index.getObjectType());
        assertTrue(index.isIfExists());
        assertEquals(1, index.getActions().get(0).getColumnNumber());
        index.setRelation(new Table("new_ix"));
        index.getActions().get(0).setStatistics(100L);
        assertEquals("ALTER INDEX IF EXISTS new_ix ALTER COLUMN 1 SET STATISTICS 100",
                index.toString());
        assertTrue(new TablesNamesFinder().getTables(index).isEmpty());
        assertRoundTrip(index);
        assertEquals("INDEX", index.accept(new StatementVisitorAdapter<String>() {
            @Override
            public <S> String visit(AlterRelation statement, S context) {
                return statement.getObjectType().name();
            }
        }, null));
    }

    @Test
    void tableInheritanceAndGeneratedExpressionFlagsAreEditable() throws JSQLParserException {
        Alter inherit = (Alter) parse("ALTER TABLE t NO INHERIT p");
        RelationAlterAction action = (RelationAlterAction) inherit.getAlterExpressions().get(0);
        action.setNoInherit(false);
        action.setRelation(new Table("parent"));
        assertEquals("ALTER TABLE t INHERIT parent", inherit.toString());
        assertEquals(java.util.Set.of("t", "parent"), new TablesNamesFinder().getTables(inherit));
        Alter drop =
                (Alter) parse("ALTER TABLE t ALTER COLUMN generated DROP EXPRESSION IF EXISTS");
        action = (RelationAlterAction) drop.getAlterExpressions().get(0);
        assertEquals(RelationAlterAction.ColumnAction.DROP_EXPRESSION, action.getColumnAction());
        assertTrue(action.isUsingIfExists());
        action.setUsingIfExists(false);
        assertEquals("ALTER TABLE t ALTER COLUMN generated DROP EXPRESSION", drop.toString());
        assertRoundTrip(drop);
    }

    @Test
    void viewDefaultsAndTableParametersAreVisited() throws JSQLParserException {
        AlterRelation view = (AlterRelation) parse("ALTER VIEW v ALTER COLUMN id SET DEFAULT 1");
        RelationAlterAction action = view.getActions().get(0);
        action.setDefaultExpression(new LongValue(2));
        assertEquals("ALTER VIEW v ALTER COLUMN id SET DEFAULT 2", view.toString());
        List<Expression> visited = new ArrayList<>();
        action.visitExpressions(visited::add);
        assertEquals(List.of(action.getDefaultExpression()), visited);
        assertEquals(java.util.Set.of("v"), new TablesNamesFinder().getTables(view));
        assertRoundTrip(view);
        Alter table =
                (Alter) parse("ALTER TABLE t SET (toast.autovacuum_enabled=false, fillfactor=70)");
        action = (RelationAlterAction) table.getAlterExpressions().get(0);
        assertEquals("toast.autovacuum_enabled", action.getOptions().get(0).getName());
        action.getOptions().get(1).setValue(new LongValue(80));
        visited.clear();
        TableDefinitionTraversal.visit(action, visited::add, ignored -> {
        });
        assertEquals(2, visited.size());
        assertTrue(table.toString().endsWith("fillfactor = 80)"));
        assertRoundTrip(table);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALTER INDEX ix OWNER TO me", "ALTER VIEW v SET TABLESPACE pg_default",
            "ALTER MATERIALIZED VIEW v ALTER COLUMN id SET DEFAULT 1",
            "ALTER INDEX ix ALTER COLUMN id SET STATISTICS 10",
            "ALTER TABLE t ALTER COLUMN 1 SET STATISTICS 10",
            "ALTER VIEW v RENAME TO v2, SET (security_invoker=true)"})
    void objectBoundariesAreEnforced(String sql) {
        assertThrows(JSQLParserException.class, () -> parse(sql));
    }

    @Test
    void triggerTargetsAndTypeNamesAreMutableWithoutInventingTables() throws JSQLParserException {
        Alter statement = (Alter) parse("ALTER TABLE t ENABLE REPLICA TRIGGER trg");
        RelationAlterAction action = (RelationAlterAction) statement.getAlterExpressions().get(0);
        assertEquals(RelationAlterAction.TriggerState.ENABLE_REPLICA, action.getTriggerState());
        action.setTriggerState(RelationAlterAction.TriggerState.DISABLE);
        action.setTriggerTarget(RelationAlterAction.TriggerTarget.USER);
        assertEquals("ALTER TABLE t DISABLE TRIGGER USER", statement.toString());
        assertRoundTrip(statement);
        statement = (Alter) parse("ALTER TABLE t OF public.typ");
        action = (RelationAlterAction) statement.getAlterExpressions().get(0);
        action.setValue("other.typ");
        assertEquals(java.util.Set.of("t"), new TablesNamesFinder().getTables(statement));
        assertRoundTrip(statement);
        for (String sql : List.of("ALTER TABLE t ENABLE ALWAYS TRIGGER ALL",
                "ALTER INDEX ix SET LOGGED", "ALTER VIEW v CLUSTER ON ix")) {
            assertThrows(JSQLParserException.class, () -> parse(sql));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"TABLE", "INDEX", "MATERIALIZED VIEW"})
    void bulkTablespaceMovesShareOneModel(String objectType) throws JSQLParserException {
        AlterTablespaceMove statement = (AlterTablespaceMove) parse("ALTER " + objectType
                + " ALL IN TABLESPACE old_space OWNED BY alice, \"Bob\" SET TABLESPACE new_space NOWAIT");
        assertEquals(objectType.replace(' ', '_'), statement.getObjectType().name());
        assertEquals(List.of("alice", "\"Bob\""), statement.getOwners());
        assertEquals("old_space", statement.getSourceTablespace());
        assertTrue(statement.isNoWait());
        assertTrue(new TablesNamesFinder().getTables(statement).isEmpty());
        statement.setTargetTablespace("pg_default");
        statement.getOwners().clear();
        statement.setNoWait(false);
        assertEquals(
                "ALTER " + objectType + " ALL IN TABLESPACE old_space SET TABLESPACE pg_default",
                statement.toString());
        assertRoundTrip(statement);
        assertEquals(2, CCJSqlParserUtil.parseStatements(statement + "; SELECT 1").size());
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

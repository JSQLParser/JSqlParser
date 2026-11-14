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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.Node;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.alter.AlterExpressionPartition.ExchangeValidationMode;
import net.sf.jsqlparser.statement.create.table.TableOption;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import net.sf.jsqlparser.util.validation.feature.MySqlVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** MySQL's WITH | WITHOUT VALIDATION modifier in the ALTER TABLE action list. */
class MySqlAlterValidationModifierTest {
    /** The modifier is read with these dialects or without one. */
    private static final Dialect[] MYSQL = {Dialect.MYSQL, Dialect.MARIADB, null};
    private static final Dialect[] OTHERS =
            {Dialect.POSTGRESQL, Dialect.ORACLE, Dialect.SQLSERVER, Dialect.H2};

    private static Statement parse(String sql, Dialect dialect) throws JSQLParserException {
        return dialect == null ? CCJSqlParserUtil.parse(sql)
                : CCJSqlParserUtil.parse(sql, p -> p.withDialect(dialect));
    }

    /** Parses, prints through toString and the deparser, and reparses. */
    private static Alter roundTrip(String sql, Dialect dialect, AlterOperation... operations)
            throws Exception {
        Alter alter = assertInstanceOf(Alter.class, parse(sql, dialect), sql);
        assertEquals(Arrays.asList(operations), operations(alter), dialect + ": " + sql);
        assertEquals(sql, alter.toString());
        Node node = alter.getASTNode();
        assertNotNull(node, sql);
        assertEquals(sql, sql.substring(node.jjtGetFirstToken().absoluteBegin - 1,
                node.jjtGetLastToken().absoluteEnd - 1));
        StringBuilder output = new StringBuilder();
        alter.accept(new StatementDeParser(output), null);
        assertEquals(sql, output.toString());
        Alter reparsed = assertInstanceOf(Alter.class, parse(alter.toString(), dialect));
        assertEquals(operations(alter), operations(reparsed));
        assertEquals(sql, reparsed.toString());
        return alter;
    }

    private static List<AlterOperation> operations(Alter alter) {
        List<AlterOperation> operations = new ArrayList<>();
        for (AlterExpression action : alter.getAlterExpressions()) {
            operations.add(action.getOperation());
        }
        return operations;
    }

    private static ExchangeValidationMode mode(Alter alter, int index) {
        AlterExpressionTableOption action = assertInstanceOf(AlterExpressionTableOption.class,
                alter.getAlterExpressions().get(index));
        assertNull(action.getOptionalSpecifier());
        assertNull(action.getStructuredTableOption());
        return action.getValidationMode();
    }

    @Test
    void modifierBeforeExchangePartitionIsItsOwnAction() throws Exception {
        for (Dialect dialect : MYSQL) {
            Alter alter = roundTrip(
                    "ALTER TABLE t1 WITHOUT VALIDATION, EXCHANGE PARTITION p0 WITH TABLE t2",
                    dialect, AlterOperation.VALIDATION, AlterOperation.EXCHANGE_PARTITION);
            assertEquals(ExchangeValidationMode.WITHOUT, mode(alter, 0));
            AlterExpressionPartition exchange = assertInstanceOf(AlterExpressionPartition.class,
                    alter.getAlterExpressions().get(1));
            assertEquals(Arrays.asList("p0"), exchange.getPartitions());
            assertEquals("t2", exchange.getExchangeTable().getName());
            assertNull(exchange.getExchangeValidationMode());
            assertEquals(Arrays.asList("t1", "t2"),
                    new TablesNamesFinder<>().getTableList((Statement) alter));

            // the mode after WITH TABLE still belongs to the exchange
            alter = roundTrip("ALTER TABLE t1 WITH VALIDATION, EXCHANGE PARTITION p0 WITH TABLE t2"
                    + " WITHOUT VALIDATION", dialect, AlterOperation.VALIDATION,
                    AlterOperation.EXCHANGE_PARTITION);
            assertEquals(ExchangeValidationMode.WITH, mode(alter, 0));
            assertEquals(ExchangeValidationMode.WITHOUT,
                    ((AlterExpressionPartition) alter.getAlterExpressions().get(1))
                            .getExchangeValidationMode());

            alter = roundTrip("ALTER TABLE t1 EXCHANGE PARTITION p0 WITH TABLE t2 WITH VALIDATION",
                    dialect, AlterOperation.EXCHANGE_PARTITION);
            assertEquals(ExchangeValidationMode.WITH,
                    ((AlterExpressionPartition) alter.getAlterExpressions().get(0))
                            .getExchangeValidationMode());

            roundTrip("ALTER TABLE t1 WITHOUT VALIDATION, WITH VALIDATION,"
                    + " EXCHANGE PARTITION p0 WITH TABLE t2 WITH VALIDATION", dialect,
                    AlterOperation.VALIDATION, AlterOperation.VALIDATION,
                    AlterOperation.EXCHANGE_PARTITION);
            roundTrip("ALTER TABLE t1 ALGORITHM = INPLACE, WITHOUT VALIDATION, LOCK = NONE,"
                    + " EXCHANGE PARTITION p0 WITH TABLE t2", dialect, AlterOperation.ALGORITHM,
                    AlterOperation.VALIDATION, AlterOperation.LOCK,
                    AlterOperation.EXCHANGE_PARTITION);
        }
    }

    @Test
    void modifierAloneAndAmongModifiers() throws Exception {
        for (Dialect dialect : MYSQL) {
            assertEquals(ExchangeValidationMode.WITH,
                    mode(roundTrip("ALTER TABLE t1 WITH VALIDATION", dialect,
                            AlterOperation.VALIDATION), 0));
            assertEquals(ExchangeValidationMode.WITHOUT,
                    mode(roundTrip("ALTER TABLE t1 WITHOUT VALIDATION", dialect,
                            AlterOperation.VALIDATION), 0));
            Alter alter = roundTrip("ALTER TABLE t1 WITH VALIDATION, WITHOUT VALIDATION", dialect,
                    AlterOperation.VALIDATION, AlterOperation.VALIDATION);
            assertEquals(ExchangeValidationMode.WITHOUT, mode(alter, 1));
            roundTrip("ALTER TABLE t1 WITH VALIDATION, ALGORITHM = COPY, LOCK = SHARED", dialect,
                    AlterOperation.VALIDATION, AlterOperation.ALGORITHM, AlterOperation.LOCK);
            roundTrip("ALTER TABLE t1 ALGORITHM = COPY, WITH VALIDATION", dialect,
                    AlterOperation.ALGORITHM, AlterOperation.VALIDATION);
        }
        Alter alter = assertInstanceOf(Alter.class,
                parse("alter table t1 with validation, add column c int", Dialect.MYSQL));
        assertEquals("ALTER TABLE t1 WITH VALIDATION, ADD COLUMN c int", alter.toString());
    }

    @Test
    void modifierBetweenAlterListItems() throws Exception {
        for (Dialect dialect : MYSQL) {
            Alter alter = roundTrip("ALTER TABLE t1 WITH VALIDATION, ADD COLUMN c INT", dialect,
                    AlterOperation.VALIDATION, AlterOperation.ADD);
            assertEquals(ExchangeValidationMode.WITH, mode(alter, 0));
            assertEquals("c",
                    alter.getAlterExpressions().get(1).getColDataTypeList().get(0).getColumnName());
            alter = roundTrip("ALTER TABLE t1 ADD COLUMN c INT, WITHOUT VALIDATION", dialect,
                    AlterOperation.ADD, AlterOperation.VALIDATION);
            assertEquals(ExchangeValidationMode.WITHOUT, mode(alter, 1));
            roundTrip("ALTER TABLE t1 WITH VALIDATION, ADD COLUMN c INT, WITHOUT VALIDATION,"
                    + " ADD COLUMN d INT, WITH VALIDATION", dialect, AlterOperation.VALIDATION,
                    AlterOperation.ADD, AlterOperation.VALIDATION, AlterOperation.ADD,
                    AlterOperation.VALIDATION);
            roundTrip("ALTER TABLE t1 ADD COLUMN c INT, ALGORITHM = COPY, WITH VALIDATION,"
                    + " LOCK = SHARED", dialect, AlterOperation.ADD, AlterOperation.ALGORITHM,
                    AlterOperation.VALIDATION, AlterOperation.LOCK);
            roundTrip("ALTER TABLE t1 ADD COLUMN g INT AS (a + 1) STORED, WITHOUT VALIDATION",
                    dialect, AlterOperation.ADD, AlterOperation.VALIDATION);
            roundTrip("ALTER TABLE t1 MODIFY COLUMN a BIGINT, WITH VALIDATION", dialect,
                    AlterOperation.MODIFY, AlterOperation.VALIDATION);
            roundTrip("ALTER TABLE t1 ENGINE = InnoDB, WITH VALIDATION", dialect,
                    AlterOperation.ENGINE, AlterOperation.VALIDATION);
            roundTrip("ALTER TABLE t1 WITH VALIDATION, ENGINE = InnoDB", dialect,
                    AlterOperation.VALIDATION, AlterOperation.ENGINE);
            roundTrip("ALTER TABLE t1 WITH VALIDATION, FORCE", dialect, AlterOperation.VALIDATION,
                    AlterOperation.FORCE);
            roundTrip("ALTER TABLE t1 RENAME TO t4, WITH VALIDATION", dialect,
                    AlterOperation.RENAME_TABLE, AlterOperation.VALIDATION);
            roundTrip("ALTER TABLE t1 ADD INDEX i1 (a), WITHOUT VALIDATION", dialect,
                    AlterOperation.ADD, AlterOperation.VALIDATION);
            roundTrip("ALTER TABLE t1 DROP COLUMN a, WITH VALIDATION", dialect,
                    AlterOperation.DROP, AlterOperation.VALIDATION);
            roundTrip("ALTER TABLE t1 CONVERT TO CHARACTER SET utf8mb4, WITH VALIDATION", dialect,
                    AlterOperation.CONVERT, AlterOperation.VALIDATION);
        }
    }

    @Test
    void modifierBeforePartitionOptionsAndStandaloneCommands() throws Exception {
        for (Dialect dialect : MYSQL) {
            roundTrip("ALTER TABLE t1 WITH VALIDATION PARTITION BY HASH (id) PARTITIONS 2",
                    dialect, AlterOperation.VALIDATION, AlterOperation.PARTITION_BY);
            roundTrip("ALTER TABLE t1 ADD COLUMN c INT, WITH VALIDATION"
                    + " PARTITION BY HASH (id) PARTITIONS 2", dialect, AlterOperation.ADD,
                    AlterOperation.VALIDATION, AlterOperation.PARTITION_BY);
            roundTrip("ALTER TABLE t1 WITHOUT VALIDATION REMOVE PARTITIONING", dialect,
                    AlterOperation.VALIDATION, AlterOperation.REMOVE_PARTITIONING);
            roundTrip("ALTER TABLE t1 WITH VALIDATION, TRUNCATE PARTITION p0", dialect,
                    AlterOperation.VALIDATION, AlterOperation.TRUNCATE_PARTITION);
            roundTrip("ALTER TABLE t1 WITH VALIDATION, ANALYZE PARTITION p0", dialect,
                    AlterOperation.VALIDATION, AlterOperation.ANALYZE_PARTITION);
            roundTrip("ALTER TABLE t1 WITHOUT VALIDATION, DROP PARTITION p1", dialect,
                    AlterOperation.VALIDATION, AlterOperation.DROP_PARTITION);
            roundTrip("ALTER TABLE t1 WITH VALIDATION,"
                    + " ADD PARTITION (PARTITION p2 VALUES LESS THAN (200))", dialect,
                    AlterOperation.VALIDATION, AlterOperation.ADD_PARTITION);
            roundTrip("ALTER TABLE t1 WITH VALIDATION, REORGANIZE PARTITION p1 INTO"
                    + " (PARTITION p1 VALUES LESS THAN (200),"
                    + " PARTITION p2 VALUES LESS THAN MAXVALUE)", dialect,
                    AlterOperation.VALIDATION, AlterOperation.REORGANIZE_PARTITION);
            roundTrip("ALTER TABLE t1 WITH VALIDATION, DISCARD TABLESPACE", dialect,
                    AlterOperation.VALIDATION, AlterOperation.DISCARD_TABLESPACE);
            roundTrip("ALTER TABLE t1 WITHOUT VALIDATION, IMPORT TABLESPACE", dialect,
                    AlterOperation.VALIDATION, AlterOperation.IMPORT_TABLESPACE);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ALTER TABLE t1 EXCHANGE PARTITION p0 WITH TABLE t2, WITHOUT VALIDATION",
            "ALTER TABLE t1 EXCHANGE PARTITION p0 WITH TABLE t2 WITHOUT VALIDATION, WITH VALIDATION",
            "ALTER TABLE t1 WITH VALIDATION, EXCHANGE PARTITION p0 WITH TABLE t2,"
                    + " WITHOUT VALIDATION",
            "ALTER TABLE t1 EXCHANGE PARTITION p0 WITH TABLE t2 WITHOUT VALIDATION WITH VALIDATION",
            "ALTER TABLE t1 EXCHANGE PARTITION p0 WITH TABLE t2 VALIDATION",
            "ALTER TABLE t1 WITHOUT VALIDATION EXCHANGE PARTITION p0 WITH TABLE t2",
            "ALTER TABLE t1 TRUNCATE PARTITION p0, WITH VALIDATION",
            "ALTER TABLE t1 DROP PARTITION p1, WITH VALIDATION",
            "ALTER TABLE t1 COALESCE PARTITION 1, WITH VALIDATION",
            "ALTER TABLE t1 ADD PARTITION (PARTITION p2 VALUES LESS THAN (200)), WITH VALIDATION",
            "ALTER TABLE t1 REORGANIZE PARTITION p1 INTO (PARTITION p1 VALUES LESS THAN (200),"
                    + " PARTITION p2 VALUES LESS THAN MAXVALUE), WITH VALIDATION",
            "ALTER TABLE t1 DISCARD TABLESPACE, WITH VALIDATION",
            "ALTER TABLE t1 IMPORT TABLESPACE, WITHOUT VALIDATION",
            "ALTER TABLE t1 PARTITION BY HASH (id) PARTITIONS 2, WITH VALIDATION",
            "ALTER TABLE t1 PARTITION BY HASH (id) PARTITIONS 2 WITH VALIDATION",
            "ALTER TABLE t1 REMOVE PARTITIONING, WITH VALIDATION",
            "ALTER TABLE t1 REMOVE PARTITIONING WITH VALIDATION",
            "ALTER TABLE t1 WITH VALIDATION WITHOUT VALIDATION",
            "ALTER TABLE t1 WITH VALIDATION ADD COLUMN c INT",
            "ALTER TABLE t1 ADD COLUMN c INT WITHOUT VALIDATION",
            "ALTER TABLE t1 ADD COLUMN c INT, WITH VALIDATION,",
            "ALTER TABLE t1 WITH VALIDATION,",
            "ALTER TABLE t1 WITH",
            "ALTER TABLE t1 WITHOUT",
            "ALTER TABLE t1 WITH NO VALIDATION",
            "ALTER TABLE t1 WITH VALIDATION = 1",
            "ALTER TABLE t1 ORDER BY a, WITH VALIDATION"})
    void mySqlRejectsWhatTheServerRejects(String sql) {
        assertThrows(JSQLParserException.class, () -> parse(sql, Dialect.MYSQL), sql);
    }

    @Test
    void mariaDbAndNoDialectKeepWhatTheyAccepted() throws Exception {
        for (Dialect dialect : new Dialect[] {Dialect.MARIADB, null}) {
            // accepted before, now with a typed modifier
            Alter alter = roundTrip(
                    "ALTER TABLE t1 EXCHANGE PARTITION p0 WITH TABLE t2, WITHOUT VALIDATION",
                    dialect, AlterOperation.EXCHANGE_PARTITION, AlterOperation.VALIDATION);
            assertEquals(ExchangeValidationMode.WITHOUT, mode(alter, 1));
            // not the whole item of the list: raw text as before
            for (String sql : new String[] {"ALTER TABLE t1 WITH VALIDATION foo",
                    "ALTER TABLE t1 WITH NOCHECK ADD CONSTRAINT c CHECK (a > 1)",
                    "ALTER TABLE t1 WITH"}) {
                alter = assertInstanceOf(Alter.class, parse(sql, dialect));
                assertEquals(Arrays.asList(AlterOperation.UNSPECIFIC), operations(alter), sql);
            }
        }
    }

    @Test
    void otherDialectsKeepTheirReading() throws Exception {
        for (Dialect dialect : OTHERS) {
            Alter alter = assertInstanceOf(Alter.class, parse(
                    "ALTER TABLE t1 WITHOUT VALIDATION, EXCHANGE PARTITION p0 WITH TABLE t2",
                    dialect));
            assertEquals(Arrays.asList(AlterOperation.UNSPECIFIC), operations(alter));
            assertEquals("WITHOUT VALIDATION , EXCHANGE PARTITION p0 WITH TABLE t2",
                    alter.getAlterExpressions().get(0).getOptionalSpecifier());
            alter = assertInstanceOf(Alter.class, parse("ALTER TABLE t1 WITH VALIDATION", dialect));
            assertEquals(Arrays.asList(AlterOperation.UNSPECIFIC), operations(alter));
            alter = assertInstanceOf(Alter.class, parse(
                    "ALTER TABLE t1 EXCHANGE PARTITION p0 WITH TABLE t2 WITHOUT VALIDATION",
                    dialect));
            assertEquals(Arrays.asList(AlterOperation.EXCHANGE_PARTITION), operations(alter));
            assertEquals(ExchangeValidationMode.WITHOUT,
                    ((AlterExpressionPartition) alter.getAlterExpressions().get(0))
                            .getExchangeValidationMode());
        }
    }

    @Test
    void statementBoundaries() throws Exception {
        for (Dialect dialect : MYSQL) {
            String script =
                    "ALTER TABLE t1 WITHOUT VALIDATION, EXCHANGE PARTITION p0 WITH TABLE t2;"
                            + " ALTER TABLE t1 ADD COLUMN c INT, WITH VALIDATION; SELECT 1;"
                            + " ALTER TABLE t1 WITH VALIDATION";
            Statements statements = dialect == null ? CCJSqlParserUtil.parseStatements(script)
                    : CCJSqlParserUtil.parseStatements(script, p -> p.withDialect(dialect));
            assertEquals(4, statements.size());
            assertEquals(
                    Arrays.asList(AlterOperation.VALIDATION, AlterOperation.EXCHANGE_PARTITION),
                    operations((Alter) statements.get(0)));
            assertEquals(Arrays.asList(AlterOperation.ADD, AlterOperation.VALIDATION),
                    operations((Alter) statements.get(1)));
            assertEquals("SELECT 1", statements.get(2).toString());
            assertEquals(Arrays.asList(AlterOperation.VALIDATION),
                    operations((Alter) statements.get(3)));
            assertEquals(Arrays.asList(AlterOperation.VALIDATION), operations(
                    (Alter) parse("ALTER TABLE t1 WITH VALIDATION;", dialect)));
        }
    }

    @Test
    void modelBuildsAndReplacesTheModifier() {
        AlterExpressionTableOption action =
                new AlterExpressionTableOption().withValidationMode(ExchangeValidationMode.WITHOUT);
        assertEquals(AlterOperation.VALIDATION, action.getOperation());
        assertEquals("WITHOUT VALIDATION", action.toString());
        action.setValidationMode(ExchangeValidationMode.WITH);
        assertEquals("WITH VALIDATION", action.toString());
        assertThrows(NullPointerException.class, () -> action.setValidationMode(null));
        assertEquals(AlterOperation.VALIDATION, AlterOperation.from("validation"));

        Alter alter = new Alter();
        alter.setTable(new net.sf.jsqlparser.schema.Table("t1"));
        alter.addAlterExpression(action);
        assertEquals("ALTER TABLE t1 WITH VALIDATION", alter.toString());

        // another table option replaces the modifier, and the modifier replaces it
        action.setStructuredTableOption(
                new TableOption(TableOption.Kind.ENGINE, "ENGINE", "InnoDB", true));
        assertNull(action.getValidationMode());
        assertEquals(AlterOperation.ENGINE, action.getOperation());
        action.setValidationMode(ExchangeValidationMode.WITHOUT);
        assertNull(action.getStructuredTableOption());
        assertEquals("WITHOUT VALIDATION", action.toString());
        assertNull(new AlterExpressionTableOption().getValidationMode());
    }

    @Test
    void visitorsAndValidation() throws Exception {
        String sql = "ALTER TABLE t1 WITHOUT VALIDATION, EXCHANGE PARTITION p0 WITH TABLE t2";
        List<String> visited = new ArrayList<>();
        parse(sql, Dialect.MYSQL).accept(new StatementVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(Alter alter, S context) {
                visited.add(alter.getTable().getName());
                return null;
            }
        }, null);
        assertEquals(Arrays.asList("t1"), visited);
        assertTrue(Validation.validate(Arrays.asList(MySqlVersion.V8_0), sql).isEmpty());
        assertTrue(Validation.validate(Arrays.asList(new FeaturesAllowed(Feature.alterTable)), sql)
                .isEmpty());
    }
}

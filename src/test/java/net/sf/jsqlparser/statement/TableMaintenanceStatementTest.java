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

import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.TableMaintenanceStatement.Modifier;
import net.sf.jsqlparser.statement.TableMaintenanceStatement.Operation;
import net.sf.jsqlparser.statement.TableMaintenanceStatement.Option;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TableMaintenanceStatementTest {

    private static TableMaintenanceStatement parse(String sql) throws JSQLParserException {
        return (TableMaintenanceStatement) assertSqlCanBeParsedAndDeparsed(sql, true);
    }

    private static List<String> tableNames(TableMaintenanceStatement statement) {
        return statement.getTables().stream().map(Table::getFullyQualifiedName)
                .collect(Collectors.toList());
    }

    @Test
    void optimizeTable() throws JSQLParserException {
        TableMaintenanceStatement statement = parse("OPTIMIZE TABLE t");

        assertEquals(Operation.OPTIMIZE, statement.getOperation());
        assertNull(statement.getModifier());
        assertFalse(statement.isTablesKeyword());
        assertEquals(List.of("t"), tableNames(statement));
        assertTrue(statement.getOptions().isEmpty());
    }

    @Test
    void optimizeModifierAndTableList() throws JSQLParserException {
        TableMaintenanceStatement statement =
                parse("OPTIMIZE NO_WRITE_TO_BINLOG TABLE db.t1, `t 2`");

        assertEquals(Modifier.NO_WRITE_TO_BINLOG, statement.getModifier());
        assertEquals(List.of("db.t1", "`t 2`"), tableNames(statement));
    }

    @Test
    void localModifierAndTablesKeyword() throws JSQLParserException {
        TableMaintenanceStatement statement = parse("OPTIMIZE LOCAL TABLES t1, t2");

        assertEquals(Modifier.LOCAL, statement.getModifier());
        assertTrue(statement.isTablesKeyword());
        assertEquals("OPTIMIZE LOCAL TABLES t1, t2", statement.toString());
    }

    @Test
    void repairOptions() throws JSQLParserException {
        TableMaintenanceStatement statement =
                parse("REPAIR NO_WRITE_TO_BINLOG TABLE t QUICK EXTENDED USE_FRM");

        assertEquals(Operation.REPAIR, statement.getOperation());
        assertEquals(List.of(Option.QUICK, Option.EXTENDED, Option.USE_FRM),
                statement.getOptions());
    }

    @Test
    void checkOptionsKeepSourceOrder() throws JSQLParserException {
        TableMaintenanceStatement statement =
                parse("CHECK TABLE t1, t2 FOR UPGRADE QUICK FAST MEDIUM EXTENDED CHANGED");

        assertEquals(Operation.CHECK, statement.getOperation());
        assertEquals(List.of(Option.FOR_UPGRADE, Option.QUICK, Option.FAST, Option.MEDIUM,
                Option.EXTENDED, Option.CHANGED), statement.getOptions());
        assertEquals(List.of("t1", "t2"), tableNames(statement));
    }

    @Test
    void keywordsAreCaseInsensitive() throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse("check tables t medium for upgrade");

        assertEquals("CHECK TABLES t MEDIUM FOR UPGRADE", statement.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CHECK NO_WRITE_TO_BINLOG TABLE t",
            "CHECK LOCAL TABLE t",
            "CHECK TABLE t USE_FRM",
            "OPTIMIZE TABLE t QUICK",
            "REPAIR TABLE t FAST",
            "REPAIR TABLE t FOR UPGRADE",
            "OPTIMIZE t",
            "REPAIR TABLE"
    })
    void rejectsSyntaxOutsideTheStatementDefinition(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql));
    }

    @Test
    void parsesInMySqlAndMariaDbDialects() throws JSQLParserException {
        for (Dialect dialect : List.of(Dialect.MYSQL, Dialect.MARIADB)) {
            Statement statement = CCJSqlParserUtil.parse("OPTIMIZE TABLE t",
                    parser -> parser.withDialect(dialect));
            assertInstanceOf(TableMaintenanceStatement.class, statement, dialect.name());
        }
    }

    @Test
    void buildsAndDeparses() {
        TableMaintenanceStatement statement = new TableMaintenanceStatement(Operation.REPAIR)
                .withModifier(Modifier.LOCAL).addTables(new Table("t1"), new Table("t2"))
                .addOptions(Option.QUICK);

        assertDeparse(statement, "REPAIR LOCAL TABLE t1, t2 QUICK");
    }

    @Test
    void findsTables() throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse("CHECK TABLE a, s.b QUICK");

        assertThat(TablesNamesFinder.findTables(statement.toString()))
                .containsExactlyInAnyOrder("a", "s.b");
    }

    @Test
    void wordsRemainUsableOutsideTheStatement() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT optimize, repair, quick, fast, medium, changed "
                + "FROM maintenance");
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE t OPTIMIZE PARTITION p1");
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE t CHECK PARTITION p1");
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE t REPAIR PARTITION p1");
    }

    @Test
    void domainCheckConstraintIsNotATableCheck() throws JSQLParserException {
        Statements statements = CCJSqlParserUtil.parseStatements(
                "CREATE DOMAIN d AS INT CHECK (VALUE > 0);\nCHECK TABLE t;",
                parser -> parser.withDialect(Dialect.POSTGRESQL));

        assertEquals(2, statements.size());
        assertEquals("CREATE DOMAIN d AS INT CHECK (VALUE > 0)",
                statements.get(0).toString());
        assertInstanceOf(TableMaintenanceStatement.class, statements.get(1));
    }

    @Test
    void reportsStatementFeatures() throws JSQLParserException {
        StatementFeatures check = CCJSqlParserUtil.parse("CHECK TABLE t").getFeatures();
        assertThat(check.getCertain()).containsExactlyInAnyOrder(StmtFeature.READS_DATA,
                StmtFeature.RETURNS_RESULT_SET);

        StatementFeatures optimize = CCJSqlParserUtil.parse("OPTIMIZE TABLE t").getFeatures();
        assertThat(optimize.getCertain()).contains(StmtFeature.MODIFIES_SCHEMA);
        assertFalse(optimize.may(StmtFeature.MODIFIES_DATA));

        StatementFeatures repair = CCJSqlParserUtil.parse("REPAIR TABLE t").getFeatures();
        assertThat(repair.getCertain()).contains(StmtFeature.MODIFIES_SCHEMA);
        assertThat(repair.getUncertain()).contains(StmtFeature.MODIFIES_DATA);
    }
}

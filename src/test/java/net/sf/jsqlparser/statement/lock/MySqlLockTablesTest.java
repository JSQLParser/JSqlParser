/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.lock;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.parser.ASTNodeAccess;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.Node;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementFeatures;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.StmtFeature;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.statement.alter.AlterUser;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MySqlLockTablesTest {
    private static final Consumer<CCJSqlParser> MYSQL = parser -> parser.withDialect(Dialect.MYSQL);
    private static final Consumer<CCJSqlParser> MARIADB =
            parser -> parser.withDialect(Dialect.MARIADB);
    private static final Consumer<CCJSqlParser> POSTGRESQL =
            parser -> parser.withDialect(Dialect.POSTGRESQL);
    private static final Consumer<CCJSqlParser> ORACLE =
            parser -> parser.withDialect(Dialect.ORACLE);
    private static final Consumer<CCJSqlParser> NO_DIALECT = parser -> {
    };

    @ParameterizedTest
    @ValueSource(strings = {
            "LOCK TABLES `t` WRITE",
            "LOCK TABLES t READ, s AS x WRITE",
            "LOCK TABLES t READ LOCAL",
            "LOCK TABLES t AS a READ LOCAL, u LOW_PRIORITY WRITE",
            "LOCK TABLES db.t READ, `db`.`u` AS `a` LOW_PRIORITY WRITE",
            "LOCK TABLES t a READ, t b WRITE",
            "LOCK TABLES nowait WRITE, tables READ",
            "LOCK TABLE t READ",
            "LOCK TABLE t WRITE",
            "UNLOCK TABLES",
            "UNLOCK TABLE",
            "LOCK INSTANCE FOR BACKUP",
            "UNLOCK INSTANCE"})
    void parsesMySqlFormsWithAndWithoutDialect(String sql) throws JSQLParserException {
        for (Consumer<CCJSqlParser> dialect : List.of(NO_DIALECT, MYSQL)) {
            Statement statement = assertSqlCanBeParsedAndDeparsed(sql, false, dialect);
            assertEquals(sql, deparse(statement));
            assertEquals(sql, CCJSqlParserUtil.parse(statement.toString(), dialect).toString());
        }
    }

    @Test
    void keepsTheLockTypeAndAliasOfEachTable() throws JSQLParserException {
        LockStatement lock =
                parse("LOCK TABLES t AS a READ LOCAL, db.u LOW_PRIORITY WRITE, v b WRITE",
                        MYSQL);

        assertTrue(lock.isTablesKeyword());
        assertTrue(lock.isUseTableKeyword());
        assertNull(lock.getLockMode());
        assertFalse(lock.isNoWait());
        assertNull(lock.getWaitSeconds());
        assertEquals(3, lock.getTargets().size());

        LockStatement.Target first = lock.getTargets().get(0);
        assertEquals("t", first.getTable().getName());
        assertEquals("a", first.getTable().getAlias().getName());
        assertTrue(first.getTable().getAlias().isUseAs());
        assertEquals(TableLockType.READ_LOCAL, first.getLockType());
        assertEquals(LockStatement.Scope.DEFAULT, first.getScope());
        assertEquals("t", lock.getTable().getName());

        LockStatement.Target second = lock.getTargets().get(1);
        assertEquals("db.u", second.getTable().getFullyQualifiedName());
        assertNull(second.getTable().getAlias());
        assertEquals(TableLockType.LOW_PRIORITY_WRITE, second.getLockType());

        LockStatement.Target third = lock.getTargets().get(2);
        assertEquals("b", third.getTable().getAlias().getName());
        assertFalse(third.getTable().getAlias().isUseAs());
        assertEquals(TableLockType.WRITE, third.getLockType());
    }

    @Test
    void keepsTheSpellingOfTableAndTables() throws JSQLParserException {
        LockStatement lock = parse("LOCK TABLE t READ", NO_DIALECT);
        assertTrue(lock.isUseTableKeyword());
        assertFalse(lock.isTablesKeyword());
        assertEquals(TableLockType.READ, lock.getTargets().get(0).getLockType());

        UnlockStatement unlock = (UnlockStatement) CCJSqlParserUtil.parse("UNLOCK TABLE");
        assertFalse(unlock.isTablesKeyword());
        assertTrue(((UnlockStatement) CCJSqlParserUtil.parse("unlock tables")).isTablesKeyword());
    }

    @Test
    void parsesBackupLocks() throws JSQLParserException {
        BackupLockStatement lock = (BackupLockStatement) CCJSqlParserUtil.parse(
                "lock instance for backup", MYSQL);
        assertEquals(BackupLockStatement.Action.LOCK, lock.getAction());
        assertEquals("LOCK INSTANCE FOR BACKUP", lock.toString());

        BackupLockStatement unlock =
                (BackupLockStatement) CCJSqlParserUtil.parse("UNLOCK INSTANCE");
        assertEquals(BackupLockStatement.Action.UNLOCK, unlock.getAction());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "LOCK TABLES t WRITE CONCURRENT",
            "LOCK TABLES t READ, u WRITE CONCURRENT WAIT 10",
            "LOCK TABLES t READ NOWAIT",
            "LOCK TABLE t AS a WRITE WAIT 5",
            "LOCK TABLES t LOW_PRIORITY WRITE",
            "UNLOCK TABLES"})
    void parsesMariaDbForms(String sql) throws JSQLParserException {
        Statement statement = assertSqlCanBeParsedAndDeparsed(sql, false, MARIADB);
        assertEquals(sql, deparse(statement));
    }

    @Test
    void mariaDbWaitReusesTheStatementWait() throws JSQLParserException {
        LockStatement lock = parse("LOCK TABLES t READ, u WRITE CONCURRENT WAIT 10", MARIADB);
        assertEquals(TableLockType.WRITE_CONCURRENT, lock.getTargets().get(1).getLockType());
        assertEquals(10L, lock.getWaitSeconds());

        assertTrue(parse("LOCK TABLES t READ NOWAIT", MARIADB).isNoWait());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "LOCK TABLES t WRITE CONCURRENT",
            "LOCK TABLES t WRITE NOWAIT",
            "LOCK TABLES t READ WAIT 5"})
    void rejectsMariaDbOnlyFormsInMySql(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, MYSQL));
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql));
    }

    @ParameterizedTest
    @ValueSource(strings = {"LOCK INSTANCE FOR BACKUP", "UNLOCK INSTANCE"})
    void rejectsBackupLocksInMariaDb(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, MARIADB));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "LOCK TABLES t",
            "LOCK TABLES t READ,",
            "LOCK TABLES t READ, u",
            "LOCK TABLES t READ IN SHARE MODE",
            "LOCK TABLES t READ LOCAL LOCAL",
            "LOCK TABLES t LOW_PRIORITY READ",
            "LOCK TABLES t AS READ",
            "LOCK TABLES t * WRITE",
            "LOCK TABLES only * WRITE",
            "UNLOCK",
            "UNLOCK TABLES t",
            "LOCK INSTANCE FOR",
            "UNLOCK INSTANCE FOR BACKUP"})
    void rejectsIncompleteForms(String sql) {
        for (Consumer<CCJSqlParser> dialect : List.of(NO_DIALECT, MYSQL, MARIADB)) {
            assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, dialect),
                    sql);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "LOCK TABLES t WRITE",
            "LOCK TABLE t READ",
            "UNLOCK TABLES",
            "LOCK INSTANCE FOR BACKUP",
            "UNLOCK INSTANCE"})
    void otherDialectsKeepRejectingMySqlForms(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, POSTGRESQL));
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, ORACLE));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "LOCK tables",
            "LOCK tables NOWAIT",
            "LOCK tables WAIT 5",
            "LOCK tables IN SHARE MODE",
            "LOCK tables.x IN SHARE MODE",
            "LOCK TABLE tables, instance",
            "LOCK instance",
            "LOCK TABLE t, u IN ACCESS EXCLUSIVE MODE NOWAIT",
            "LOCK TABLE ONLY t, u *",
            "LOCK TABLE ONLY t IN SHARE MODE",
            "LOCK ONLY (t) NOWAIT"})
    void postgreSqlAndOracleReadingsAreUnchanged(String sql) throws JSQLParserException {
        for (Consumer<CCJSqlParser> dialect : List.of(NO_DIALECT, MYSQL, MARIADB, POSTGRESQL)) {
            LockStatement lock = (LockStatement) assertSqlCanBeParsedAndDeparsed(sql, false,
                    dialect);
            assertFalse(lock.isTablesKeyword());
            lock.getTargets().forEach(target -> {
                assertNull(target.getLockType());
                assertNull(target.getTable().getAlias());
            });
        }
        LockStatement tables = parse("LOCK tables NOWAIT", MYSQL);
        assertFalse(tables.isUseTableKeyword());
        assertEquals("tables", tables.getTable().getName());
        assertTrue(tables.isNoWait());

        for (Consumer<CCJSqlParser> dialect : List.of(NO_DIALECT, MYSQL, POSTGRESQL, ORACLE)) {
            LockStatement only = (LockStatement) assertSqlCanBeParsedAndDeparsed(
                    "LOCK TABLE ONLY t IN SHARE MODE", false, dialect);
            assertEquals(LockStatement.Scope.ONLY, only.getTargets().get(0).getScope());
            assertEquals("t", only.getTable().getName());
            assertEquals(LockMode.Share, only.getLockMode());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "LOCK TABLES only WRITE",
            "LOCK TABLES only t WRITE",
            "LOCK TABLES only AS t READ",
            "LOCK TABLES t READ, only WRITE",
            "LOCK TABLES only READ LOCAL, only AS o LOW_PRIORITY WRITE",
            "LOCK TABLE ONLY t READ",
            "LOCK TABLES only.x WRITE",
            "LOCK TABLES `only` t WRITE",
            "LOCK TABLES t AS only WRITE"})
    void readsOnlyAsATableName(String sql) throws JSQLParserException {
        // MySQL has no ONLY keyword in LOCK TABLES, so only is the name of a table
        for (Consumer<CCJSqlParser> dialect : List.of(NO_DIALECT, MYSQL, MARIADB)) {
            LockStatement lock = parse(sql, dialect);
            assertEquals(sql, deparse(lock));
            assertEquals(sql, CCJSqlParserUtil.parse(lock.toString(), dialect).toString());
            lock.getTargets().forEach(target -> {
                assertEquals(LockStatement.Scope.DEFAULT, target.getScope());
                assertNotNull(target.getLockType());
            });
        }
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, POSTGRESQL));
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, ORACLE));
    }

    @Test
    void keepsTheTableNamedOnlyAndItsAlias() throws JSQLParserException {
        LockStatement lock = parse("LOCK TABLES only t WRITE, s READ, only AS o READ", MYSQL);
        Table first = lock.getTargets().get(0).getTable();
        assertEquals("only", first.getName());
        assertEquals("t", first.getAlias().getName());
        assertFalse(first.getAlias().isUseAs());
        assertEquals("only", lock.getTargets().get(2).getTable().getName());
        assertEquals("o", lock.getTargets().get(2).getTable().getAlias().getName());
        assertThat(new TablesNamesFinder().getTables(lock))
                .containsExactlyInAnyOrder("only", "s");

        LockStatement keyword = parse("LOCK TABLE ONLY t READ", NO_DIALECT);
        assertEquals("ONLY", keyword.getTable().getName());
        assertEquals("t", keyword.getTable().getAlias().getName());
        assertTrue(keyword.isUseTableKeyword());
        assertFalse(keyword.isTablesKeyword());

        String sql = "LOCK TABLES only t WRITE";
        assertEquals("only", text(sql, parse(sql, MYSQL).getTable()));
    }

    @Test
    void wordsRemainUsableAsNames() throws JSQLParserException {
        for (Consumer<CCJSqlParser> dialect : List.of(NO_DIALECT, MYSQL, POSTGRESQL)) {
            assertSqlCanBeParsedAndDeparsed(
                    "SELECT unlock, instance, backup, concurrent FROM tables", true, dialect);
            assertSqlCanBeParsedAndDeparsed("SELECT * FROM t unlock", true, dialect);
            assertSqlCanBeParsedAndDeparsed("SELECT unlock.a FROM unlock.unlock AS unlock", true,
                    dialect);
            assertInstanceOf(CreateTable.class, assertSqlCanBeParsedAndDeparsed(
                    "CREATE TABLE unlock (instance INT, unlock INT)", true, dialect));
        }
        AlterUser alter = (AlterUser) assertSqlCanBeParsedAndDeparsed(
                "ALTER USER u ACCOUNT UNLOCK", true, MYSQL);
        assertEquals("ALTER USER u ACCOUNT UNLOCK", alter.toString());
    }

    @Test
    void parsesAMysqldumpScript() throws JSQLParserException {
        String script = "--\n-- Table structure for table `t`\n--\n\n"
                + "DROP TABLE IF EXISTS `t`;\n"
                + "CREATE TABLE `t` (\n"
                + "  `id` int NOT NULL AUTO_INCREMENT,\n"
                + "  `name` varchar(20) DEFAULT NULL,\n"
                + "  PRIMARY KEY (`id`)\n"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;\n\n"
                + "--\n-- Dumping data for table `t`\n--\n\n"
                + "LOCK TABLES `t` WRITE;\n"
                + "/*!40000 ALTER TABLE `t` DISABLE KEYS */;\n"
                + "INSERT INTO `t` VALUES (1,'a'),(2,'b');\n"
                + "/*!40000 ALTER TABLE `t` ENABLE KEYS */;\n"
                + "UNLOCK TABLES;\n";
        // only the MySQL dialect runs the /*!40000 ... */ statements of a server of version 8.4
        assertEquals(List.of("Drop", "CreateTable", "LockStatement", "Alter", "Insert", "Alter",
                "UnlockStatement"), dumpStatements(script, MYSQL));
        for (Consumer<CCJSqlParser> dialect : List.of(NO_DIALECT, MARIADB)) {
            assertEquals(List.of("Drop", "CreateTable", "LockStatement", "Insert",
                    "UnlockStatement"), dumpStatements(script, dialect));
        }
    }

    private static List<String> dumpStatements(String script, Consumer<CCJSqlParser> dialect)
            throws JSQLParserException {
        Statements statements = CCJSqlParserUtil.parseStatements(script,
                dialect.andThen(parser -> parser.withMySqlServerVersion(80400)));
        LockStatement lock = assertInstanceOf(LockStatement.class, statements.get(2));
        assertEquals("LOCK TABLES `t` WRITE", lock.toString());
        assertEquals(TableLockType.WRITE, lock.getTargets().get(0).getLockType());
        assertInstanceOf(UnlockStatement.class, statements.get(statements.size() - 1));
        return statements.stream().map(statement -> statement.getClass().getSimpleName())
                .collect(Collectors.toList());
    }

    @Test
    void endsTheStatementsAroundIt() throws JSQLParserException {
        Statements statements = CCJSqlParserUtil.parseStatements(
                "SELECT 1; LOCK TABLE t READ; UNLOCK TABLES; LOCK INSTANCE FOR BACKUP; UNLOCK INSTANCE");
        assertEquals(List.of("PlainSelect", "LockStatement", "UnlockStatement",
                "BackupLockStatement", "BackupLockStatement"),
                statements.stream().map(statement -> statement.getClass().getSimpleName())
                        .collect(Collectors.toList()));
    }

    @Test
    void isAStatementStartForUnsupportedStatements() throws JSQLParserException {
        Statements statements = CCJSqlParserUtil.parseStatements(
                "FOO BAR; UNLOCK TABLES; BAZ; UNLOCK INSTANCE",
                parser -> parser.withUnsupportedStatements(true));
        assertEquals(4, statements.size());
        assertInstanceOf(UnsupportedStatement.class, statements.get(0));
        assertInstanceOf(UnlockStatement.class, statements.get(1));
        assertInstanceOf(UnsupportedStatement.class, statements.get(2));
        assertInstanceOf(BackupLockStatement.class, statements.get(3));

        Statements postgres = CCJSqlParserUtil.parseStatements("UNLOCK TABLES",
                parser -> parser.withDialect(Dialect.POSTGRESQL).withUnsupportedStatements(true));
        assertInstanceOf(UnsupportedStatement.class, postgres.get(0));
    }

    @Test
    void findsTheLockedTablesButNotTheAliases() throws JSQLParserException {
        Statement lock = CCJSqlParserUtil.parse("LOCK TABLES t AS a READ, db.u b WRITE, t c READ");
        assertThat(new TablesNamesFinder().getTables(lock))
                .containsExactlyInAnyOrder("t", "db.u");
        assertThat(new TablesNamesFinder().getTables(CCJSqlParserUtil.parse("UNLOCK TABLES")))
                .isEmpty();
        assertThat(new TablesNamesFinder()
                .getTables(CCJSqlParserUtil.parse("LOCK INSTANCE FOR BACKUP"))).isEmpty();
    }

    @Test
    void reportsTransactionFeatures() throws JSQLParserException {
        for (String sql : new String[] {"LOCK TABLES t WRITE", "UNLOCK TABLES",
                "LOCK INSTANCE FOR BACKUP", "UNLOCK INSTANCE"}) {
            StatementFeatures features = CCJSqlParserUtil.parse(sql).getFeatures();
            assertThat(features.getCertain()).containsExactly(StmtFeature.MODIFIES_TRANSACTION);
        }
    }

    @Test
    void nodesSpanTheStatementAndItsTables() throws JSQLParserException {
        String sql = "LOCK TABLES db.t AS a READ LOCAL, u WRITE";
        LockStatement lock = parse(sql, MYSQL);
        assertEquals(sql, text(sql, lock));
        assertEquals("db.t", text(sql, lock.getTargets().get(0).getTable()));
        assertEquals("u", text(sql, lock.getTargets().get(1).getTable()));

        for (String other : new String[] {"UNLOCK TABLES", "LOCK INSTANCE FOR BACKUP",
                "UNLOCK INSTANCE"}) {
            assertEquals(other, text(other, (ASTNodeAccess) CCJSqlParserUtil.parse(other)));
        }
    }

    @Test
    void buildsStatementsInCode() {
        LockStatement lock = new LockStatement(new Table("t"), null);
        lock.setTablesKeyword(true);
        lock.getTargets().get(0).setLockType(TableLockType.READ);
        Table aliased = new Table("u");
        aliased.setAlias(new Alias("a"));
        lock.getTargets().add(
                new LockStatement.Target(aliased).withLockType(TableLockType.WRITE_CONCURRENT));
        assertEquals("LOCK TABLES t READ, u AS a WRITE CONCURRENT", lock.toString());
        assertEquals(lock.toString(), deparse(lock));

        lock.setUseTableKeyword(false);
        assertFalse(lock.isTablesKeyword());
        assertEquals("LOCK t READ, u AS a WRITE CONCURRENT", lock.toString());
        lock.setTablesKeyword(true);
        assertTrue(lock.isUseTableKeyword());

        UnlockStatement unlock = new UnlockStatement();
        assertEquals("UNLOCK TABLES", unlock.toString());
        unlock.setTablesKeyword(false);
        assertEquals("UNLOCK TABLE", deparse(unlock));
        assertEquals("UNLOCK TABLE", new UnlockStatement(false).toString());

        BackupLockStatement backup = new BackupLockStatement();
        assertEquals("LOCK INSTANCE FOR BACKUP", deparse(backup));
        backup.setAction(BackupLockStatement.Action.UNLOCK);
        assertEquals("UNLOCK INSTANCE", backup.toString());
        assertEquals("UNLOCK INSTANCE",
                new BackupLockStatement(BackupLockStatement.Action.UNLOCK).toString());
        assertThrows(NullPointerException.class, () -> backup.setAction(null));
    }

    private static LockStatement parse(String sql, Consumer<CCJSqlParser> dialect)
            throws JSQLParserException {
        return (LockStatement) assertSqlCanBeParsedAndDeparsed(sql, false, dialect);
    }

    private static String deparse(Statement statement) {
        StringBuilder builder = new StringBuilder();
        statement.accept(new StatementDeParser(builder), null);
        return builder.toString();
    }

    private static String text(String sql, ASTNodeAccess access) {
        Node node = access.getASTNode();
        return sql.substring(node.jjtGetFirstToken().absoluteBegin - 1,
                node.jjtGetLastToken().absoluteEnd - 1);
    }
}

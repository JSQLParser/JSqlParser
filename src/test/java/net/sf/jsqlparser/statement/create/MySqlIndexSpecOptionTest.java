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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.CascadeBehavior;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterExpression;
import net.sf.jsqlparser.statement.create.index.AlgorithmOrLockOption;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.create.table.IndexOptionSource;
import net.sf.jsqlparser.statement.create.table.IndexSpecOption;
import net.sf.jsqlparser.statement.create.table.IndexSpecOption.Kind;
import net.sf.jsqlparser.statement.create.table.KeyConstraint;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class MySqlIndexSpecOptionTest {
    private static final String OPTIONS = "KEY_BLOCK_SIZE = 8 USING HASH WITH PARSER ngram "
            + "COMMENT 'c' INVISIBLE ENGINE_ATTRIBUTE '{}' SECONDARY_ENGINE_ATTRIBUTE = '{}'";

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void createIndexKeepsIndexOptionsOnItsIndexAndAlgorithmLockOnTheStatement(boolean mysql)
            throws Exception {
        String sql = "CREATE UNIQUE INDEX uk ON t1 (a DESC, b (10)) USING BTREE COMMENT 'x' "
                + "ALGORITHM = INPLACE LOCK = NONE";
        CreateIndex create = (CreateIndex) roundTrip(sql, mysql);
        Index index = create.getIndex();

        assertNull(index.getUsing());
        assertOptions(index, "INDEX_TYPE BTREE", "COMMENT 'x'");
        assertEquals(2, create.getAlgorithmOrLockOptions().size());
        assertAlgorithmOrLock(create.getAlgorithmOrLockOptions().get(0),
                AlgorithmOrLockOption.Kind.ALGORITHM, "INPLACE");
        assertAlgorithmOrLock(create.getAlgorithmOrLockOptions().get(1),
                AlgorithmOrLockOption.Kind.LOCK, "NONE");
        assertEquals(List.of("USING", "BTREE", "COMMENT", "'x'"), index.getIndexSpec());
        assertEquals(List.of("USING", "BTREE", "COMMENT", "'x'", "ALGORITHM", "=", "INPLACE",
                "LOCK", "=", "NONE"), create.getTailParameters());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "CREATE FULLTEXT INDEX ft ON t1 (body) WITH PARSER ngram|PARSER ngram",
            "CREATE INDEX i ON t1 (a) INVISIBLE|VISIBILITY false",
            "CREATE INDEX i ON t1 (a) VISIBLE|VISIBILITY true",
            "CREATE INDEX i ON db1.t1 (a, b) KEY_BLOCK_SIZE = 8|KEY_BLOCK_SIZE 8 =",
            "CREATE INDEX i ON t1 (a) KEY_BLOCK_SIZE 8 ENGINE_ATTRIBUTE '{}'"
                    + "|KEY_BLOCK_SIZE 8,ENGINE_ATTRIBUTE '{}'"})
    void createIndexOptionsAreTyped(String sql, String expected) throws Exception {
        for (boolean mysql : List.of(true, false)) {
            CreateIndex create = (CreateIndex) roundTrip(sql, mysql);
            assertOptions(create.getIndex(), expected.split(","));
            assertTrue(create.getAlgorithmOrLockOptions().isEmpty());
        }
    }

    @Test
    void indexTypeKeepsItsPositionRelativeToTheKeyList() throws Exception {
        CreateIndex beforeOn = (CreateIndex) roundTrip("CREATE INDEX i USING HASH ON t1 (a)", true);
        assertEquals("HASH", beforeOn.getIndex().getUsing());
        assertTrue(beforeOn.isIndexTypeBeforeOn());
        assertNull(beforeOn.getIndex().getSpecOptions());

        CreateTable table = (CreateTable) roundTrip("CREATE TABLE t20 (a INT, b VARCHAR (100), "
                + "KEY idx_a USING BTREE (a) KEY_BLOCK_SIZE = 4 COMMENT 'idx', "
                + "UNIQUE INDEX uk_b (b (20)) USING HASH)", true);
        Index key = table.getIndexes().get(0);
        assertEquals("BTREE", key.getUsing());
        assertOptions(key, "KEY_BLOCK_SIZE 4 =", "COMMENT 'idx'");
        assertEquals(List.of("KEY_BLOCK_SIZE", "=", "4", "COMMENT", "'idx'"), key.getIndexSpec());
        KeyConstraint unique = (KeyConstraint) table.getTableConstraints().get(0);
        assertNull(unique.getUsing());
        assertOptions(unique, "INDEX_TYPE HASH");
        assertEquals(List.of("USING", "HASH"), unique.getIndexSpec());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALTER TABLE t1 ADD CONSTRAINT pk_t1 PRIMARY KEY USING BTREE (id)",
            "ALTER TABLE t1 ADD PRIMARY KEY USING BTREE (id)",
            "ALTER TABLE t1 ADD CONSTRAINT uk UNIQUE KEY uk_idx USING BTREE (id)",
            "CREATE TABLE t1 (id INT, PRIMARY KEY USING BTREE (id) COMMENT 'pk')",
            "CREATE TABLE t1 (id INT, CONSTRAINT pk_t1 PRIMARY KEY USING HASH (id))"})
    void keysAcceptTheIndexTypeBeforeTheKeyList(String sql) throws Exception {
        KeyConstraint key = keyConstraint(roundTrip(sql, true));
        assertEquals(sql.contains("HASH") ? "HASH" : "BTREE", key.getUsing());
        assertEquals(List.of("id"), key.getColumnsNames());
        assertFalse(key.getSpecOptions().stream()
                .anyMatch(option -> option.getKind() == Kind.INDEX_TYPE));
    }

    @Test
    void defaultModeAcceptsTheIndexTypeBeforeNamedConstraintKeys() throws Exception {
        for (String sql : List.of(
                "ALTER TABLE t1 ADD CONSTRAINT pk_t1 PRIMARY KEY USING BTREE (id)",
                "ALTER TABLE t1 ADD CONSTRAINT uk UNIQUE KEY USING BTREE (id)")) {
            Statement statement = roundTrip(sql, false);
            KeyConstraint key = keyConstraint(statement);
            assertEquals("BTREE", key.getUsing());
            key.setUsing("HASH");
            assertRendered(statement, sql.replace("BTREE", "HASH"));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREATE INDEX ix ON t (a) %s",
            "CREATE TABLE t (a INT, KEY ix (a) %s)",
            "CREATE TABLE t (a INT, UNIQUE KEY ix (a) %s)",
            "ALTER TABLE t ADD INDEX ix (a) %s", "ALTER TABLE t ADD UNIQUE KEY ix (a) %s",
            "ALTER TABLE t ADD CONSTRAINT pk PRIMARY KEY (a) %s"})
    void everyDefinitionSharesTheSameOptionModel(String template) throws Exception {
        IndexOptionSource source = definition(roundTrip(String.format(template, OPTIONS), true));
        assertOptions(source, "KEY_BLOCK_SIZE 8 =", "INDEX_TYPE HASH", "PARSER ngram",
                "COMMENT 'c'", "VISIBILITY false", "ENGINE_ATTRIBUTE '{}'",
                "SECONDARY_ENGINE_ATTRIBUTE '{}' =");
        assertEquals(List.of("KEY_BLOCK_SIZE", "=", "8", "USING", "HASH", "WITH", "PARSER",
                "ngram", "COMMENT", "'c'", "INVISIBLE", "ENGINE_ATTRIBUTE", "'{}'",
                "SECONDARY_ENGINE_ATTRIBUTE", "=", "'{}'"), source.getIndexSpec());
    }

    @Test
    void alterKeyBlockSizeWithoutEqualsKeepsItsSeparator() throws Exception {
        // ALTER used to render this option as KEY_BLOCK_SIZE8
        Alter alter = (Alter) roundTrip("ALTER TABLE t ADD INDEX i (a) KEY_BLOCK_SIZE 8", true);
        assertOptions(alter.getAlterExpressions().get(0).getIndex(), "KEY_BLOCK_SIZE 8");
    }

    @Test
    void commentsAreStringValuesThatDecodeTheStoredText() throws Exception {
        String[][] comments = {{"'it''s'", "it's"}, {"'it\\'s'", "it's"}, {"\"dq\"", "dq"},
                {"'a\\\\b'", "a\\b"}, {"'인덱스 설명'", "인덱스 설명"}};
        for (String template : List.of("CREATE INDEX ix ON t (a) COMMENT %s",
                "CREATE TABLE t (a INT, KEY ix (a) COMMENT %s)",
                "ALTER TABLE t ADD INDEX ix (a) COMMENT %s")) {
            for (String[] comment : comments) {
                String sql = String.format(template, comment[0]);
                IndexSpecOption option =
                        definition(roundTrip(sql, true)).getSpecOptions().get(0);
                assertEquals(Kind.COMMENT, option.getKind());
                assertEquals(comment[0], option.getValue(), sql);
                assertEquals(comment[0], option.getComment().toString(), sql);
                assertEquals(comment[1], option.getComment().getNotExcapedValue(), sql);
            }
        }

        Alter alter = (Alter) roundTrip("ALTER TABLE t ADD INDEX ix (a) COMMENT 'it\\'s'", true);
        IndexSpecOption comment = alter.getAlterExpressions().get(0).getIndex()
                .getSpecOptions().get(0);
        assertEquals(StringValue.Escaping.MYSQL_BACKSLASH, comment.getComment().getEscaping());
        comment.setValue("'don\\'t'");
        assertEquals("don't", comment.getComment().getNotExcapedValue());
        comment.setComment(new StringValue("'changed'"));
        assertRendered(alter, "ALTER TABLE t ADD INDEX ix (a) COMMENT 'changed'");
        assertEquals(List.of("COMMENT", "'changed'"),
                alter.getAlterExpressions().get(0).getIndex().getIndexSpec());
    }

    @Test
    void typedEditsReachBothRenderers() throws Exception {
        Alter alter = (Alter) roundTrip(
                "ALTER TABLE t1 ADD INDEX i3 (a) USING BTREE COMMENT 'c' INVISIBLE", true);
        List<IndexSpecOption> options = alter.getAlterExpressions().get(0).getIndex()
                .getSpecOptions();
        options.get(0).setValue("HASH");
        options.get(1).setValue("'changed'");
        options.get(2).setVisible(true);
        options.add(0, IndexSpecOption.of(Kind.KEY_BLOCK_SIZE, "16", true));
        assertRendered(alter, "ALTER TABLE t1 ADD INDEX i3 (a) KEY_BLOCK_SIZE = 16 USING HASH "
                + "COMMENT 'changed' VISIBLE");

        CreateIndex create = (CreateIndex) roundTrip(
                "CREATE INDEX i ON t (a) LOCK EXCLUSIVE ALGORITHM DEFAULT", true);
        create.getAlgorithmOrLockOptions().get(0).setValue("SHARED");
        create.getAlgorithmOrLockOptions().get(1).setUseEquals(true);
        create.getIndex().getIndexOptions().addSpecOptions(IndexSpecOption.of(Kind.COMMENT, "'x'"));
        assertRendered(create,
                "CREATE INDEX i ON t (a) COMMENT 'x' LOCK SHARED ALGORITHM = DEFAULT");
        assertEquals(List.of("COMMENT", "'x'", "LOCK", "SHARED", "ALGORITHM", "=", "DEFAULT"),
                create.getTailParameters());
    }

    @Test
    void legacyAccessorsAreSnapshotsAndSettersReplaceWithOpaqueOptions() throws Exception {
        CreateIndex create = (CreateIndex) roundTrip(
                "CREATE INDEX i ON t (a) parallel compress nologging", false);
        assertEquals(List.of("parallel", "compress", "nologging"), create.getTailParameters());
        assertTrue(create.getIndex().getSpecOptions().stream()
                .allMatch(option -> option.getKind() == Kind.OTHER));
        assertThrows(UnsupportedOperationException.class,
                () -> create.getTailParameters().add("x"));
        assertThrows(UnsupportedOperationException.class,
                () -> create.getIndex().getIndexSpec().add("x"));

        create.addAlgorithmOrLockOptions(AlgorithmOrLockOption.lock("NONE"));
        List<String> tail = new ArrayList<>(List.of("KEY_BLOCK_SIZE", "=", "4"));
        create.setTailParameters(tail);
        tail.clear();
        assertTrue(create.getAlgorithmOrLockOptions().isEmpty());
        assertEquals(Kind.OTHER, create.getIndex().getSpecOptions().get(0).getKind());
        assertRendered(create, "CREATE INDEX i ON t (a) KEY_BLOCK_SIZE = 4");

        Index index = new Index().withName("i").withColumnsNames(List.of("a"));
        assertNull(index.getIndexSpec());
        index.setIndexSpec(List.of("USING", "BTREE"));
        assertEquals(List.of("USING", "BTREE"), index.getIndexSpec());
        assertEquals("i (a) USING BTREE", index.toString());
        index.setIndexSpec(List.of());
        assertTrue(index.getSpecOptions().isEmpty());
        index.setIndexSpec(null);
        assertNull(index.getSpecOptions());
        assertEquals(List.of(), new CreateIndex().getTailParameters());
    }

    @Test
    void typedOptionsRejectPayloadsOfOtherKinds() {
        IndexSpecOption comment = IndexSpecOption.comment(new StringValue("'c'"));
        assertEquals("'c'", comment.getValue());
        assertThrows(IllegalStateException.class,
                () -> IndexSpecOption.of(Kind.PARSER, "ngram").setComment(new StringValue("'x'")));
        assertNull(IndexSpecOption.of(Kind.PARSER, "ngram").getComment());
        assertThrows(NullPointerException.class, () -> comment.setComment(null));
        assertThrows(IllegalStateException.class, () -> comment.setTokens(List.of("x")));
        assertThrows(IllegalStateException.class, () -> comment.setUseEquals(true));
        assertThrows(IllegalStateException.class, () -> comment.setVisible(true));
        assertNull(comment.getVisible());
        IndexSpecOption visible = IndexSpecOption.visibility(true);
        assertThrows(IllegalStateException.class, () -> visible.setValue("x"));
        assertEquals(List.of("VISIBLE"), visible.getTokens());
        assertThrows(IllegalStateException.class,
                () -> IndexSpecOption.of(Kind.OTHER, "x"));
        assertThrows(NullPointerException.class, () -> IndexSpecOption.of(Kind.PARSER, null));

        List<String> tokens = new ArrayList<>(List.of("ON", "[PRIMARY]"));
        IndexSpecOption raw = IndexSpecOption.raw(tokens);
        tokens.set(1, "ignored");
        assertEquals("ON [PRIMARY]", raw.toString());
        assertThrows(UnsupportedOperationException.class, () -> raw.getTokens().add("x"));
        assertThrows(IllegalStateException.class, () -> raw.setValue("x"));
        raw.setTokens(List.of("ON", "fg"));
        assertEquals(List.of("ON", "fg"), raw.getTokens());
    }

    @Test
    void dropIndexAlgorithmAndLockAreTyped() throws Exception {
        Drop drop = (Drop) roundTrip("DROP INDEX idx ON t1 ALGORITHM = INPLACE LOCK = NONE", true);
        assertEquals("t1", drop.getTable().getName());
        assertAlgorithmOrLock(drop.getAlgorithmOrLockOptions().get(0),
                AlgorithmOrLockOption.Kind.ALGORITHM, "INPLACE");
        assertAlgorithmOrLock(drop.getAlgorithmOrLockOptions().get(1),
                AlgorithmOrLockOption.Kind.LOCK, "NONE");
        assertEquals(List.of("ON", "t1", "ALGORITHM", "=", "INPLACE", "LOCK", "=", "NONE"),
                drop.getParameters());

        drop.getAlgorithmOrLockOptions().remove(1);
        drop.getAlgorithmOrLockOptions().get(0).setValue("COPY");
        drop.setBehavior(CascadeBehavior.RESTRICT);
        assertRendered(drop, "DROP INDEX idx ON t1 ALGORITHM = COPY RESTRICT");
        assertEquals(List.of("ON", "t1", "ALGORITHM", "=", "COPY", "RESTRICT"),
                drop.getParameters());

        drop.addParameters("legacy");
        drop.setAlgorithmOrLockOptions(List.of(AlgorithmOrLockOption.lock("SHARED")));
        assertRendered(drop, "DROP INDEX idx ON t1 legacy LOCK = SHARED RESTRICT");
        drop.setAlgorithmOrLockOptions(List.of(AlgorithmOrLockOption.lock("SHARED")), 0);
        assertEquals(List.of("ON", "t1", "LOCK", "=", "SHARED", "legacy", "RESTRICT"),
                drop.getParameters());
        assertThrows(IllegalArgumentException.class,
                () -> drop.setAlgorithmOrLockOptions(List.of(), 2));

        drop.setParameters(List.of("CASCADE"));
        assertTrue(drop.getAlgorithmOrLockOptions().isEmpty());
        assertNull(drop.getBehavior());
        assertNull(drop.getTable());
        assertEquals("DROP INDEX idx CASCADE", drop.toString());

        Drop single = (Drop) roundTrip("DROP INDEX idx ON t1 ALGORITHM = INPLACE", false);
        assertEquals(1, single.getAlgorithmOrLockOptions().size());
    }

    @Test
    void replacingTheCreateIndexDefinitionKeepsOrInheritsItsOptions() throws Exception {
        CreateIndex create = (CreateIndex) roundTrip(
                "CREATE INDEX i ON t (a) KEY_BLOCK_SIZE = 8", true);
        Index removed = create.getIndex();
        IndexSpecOption keyBlockSize = removed.getSpecOptions().get(0);
        create.setIndex(null);
        assertEquals(List.of("KEY_BLOCK_SIZE", "=", "8"), create.getTailParameters());

        create.setIndex(new Index().withName("j").withColumnsNames(List.of("b")));
        assertSame(keyBlockSize, create.getIndex().getSpecOptions().get(0));
        assertNotSame(removed.getSpecOptions(), create.getIndex().getSpecOptions());
        assertRendered(create, "CREATE INDEX j ON t (b) KEY_BLOCK_SIZE = 8");

        create.setIndex(new Index().withName("k").withColumnsNames(List.of("c"))
                .withSpecOptions(List.of(IndexSpecOption.visibility(false))));
        assertRendered(create, "CREATE INDEX k ON t (c) INVISIBLE");
    }

    @Test
    void postgreSqlIndexOptionsKeepTheirProperties() throws Exception {
        String sql = "CREATE UNIQUE INDEX CONCURRENTLY ix ON ONLY t USING btree (id) "
                + "INCLUDE (v) NULLS NOT DISTINCT WITH (fillfactor = 80) TABLESPACE fast "
                + "WHERE active";
        CreateIndex create = (CreateIndex) CCJSqlParserUtil.parse(sql,
                parser -> parser.withDialect(Dialect.POSTGRESQL));
        assertEquals(sql, create.toString());
        assertEquals("btree", create.getIndex().getUsing());
        assertEquals(List.of("v"), create.getIncludeColumns());
        assertEquals("fast", create.getTableSpace());
        assertNull(create.getIndex().getSpecOptions());
        assertTrue(create.getTailParameters().isEmpty());

        Alter alter = (Alter) CCJSqlParserUtil.parse("ALTER TABLE t ADD CONSTRAINT uk UNIQUE (id) "
                + "INCLUDE (v) WITH (fillfactor = 70) USING INDEX TABLESPACE fast",
                parser -> parser.withDialect(Dialect.POSTGRESQL));
        KeyConstraint key = alter.getAlterExpressions().get(0).getKeyConstraint();
        assertEquals("fast", key.getTableSpace());
        assertEquals(1, key.getStorageParameters().size());
        assertTrue(key.getSpecOptions().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALTER TABLE t ADD PRIMARY KEY USING (id)",
            "ALTER TABLE t ADD PRIMARY KEY USING BTREE USING HASH (id)",
            "ALTER TABLE t ADD INDEX i (a) KEY_BLOCK_SIZE",
            "ALTER TABLE t ADD INDEX i (a) WITH PARSER",
            "ALTER TABLE t ADD INDEX i (a) COMMENT",
            "CREATE INDEX i ON t (a) LOCK = NONE ALGORITHM"})
    void rejectsIncompleteOptions(String sql) {
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(Dialect.MYSQL)));
    }

    private static void assertOptions(IndexOptionSource source, String... expected) {
        List<String> actual = new ArrayList<>();
        for (IndexSpecOption option : source.getSpecOptions()) {
            Object payload = option.getKind() == Kind.VISIBILITY ? option.getVisible()
                    : option.getValue();
            actual.add(option.getKind() + " " + payload + (option.isUseEquals() ? " =" : ""));
        }
        assertEquals(List.of(expected), actual);
    }

    private static void assertAlgorithmOrLock(AlgorithmOrLockOption option,
            AlgorithmOrLockOption.Kind kind, String value) {
        assertEquals(kind, option.getKind());
        assertEquals(value, option.getValue());
        assertTrue(option.isUseEquals());
    }

    private static IndexOptionSource definition(Statement statement) {
        if (statement instanceof CreateIndex) {
            return ((CreateIndex) statement).getIndex();
        }
        if (statement instanceof CreateTable) {
            CreateTable table = (CreateTable) statement;
            return (IndexOptionSource) (table.getIndexes().isEmpty()
                    ? table.getTableConstraints().get(0)
                    : table.getIndexes().get(0));
        }
        AlterExpression action = ((Alter) statement).getAlterExpressions().get(0);
        return (IndexOptionSource) (action.getConstraint() != null ? action.getConstraint()
                : action.getIndex());
    }

    private static KeyConstraint keyConstraint(Statement statement) {
        return (KeyConstraint) definition(statement);
    }

    private static Statement roundTrip(String sql, boolean mysql) throws Exception {
        Consumer<CCJSqlParser> dialect = mysql ? parser -> parser.withDialect(Dialect.MYSQL)
                : parser -> {
                };
        Statement statement = CCJSqlParserUtil.parse(sql, dialect);
        assertEquals(sql, statement.toString());
        StringBuilder deparsed = new StringBuilder();
        statement.accept(new StatementDeParser(deparsed));
        assertEquals(sql, deparsed.toString());
        assertEquals(sql, CCJSqlParserUtil.parse(sql, dialect).toString());
        return statement;
    }

    private static void assertRendered(Statement statement, String expected) throws Exception {
        assertEquals(expected, statement.toString());
        StringBuilder deparsed = new StringBuilder();
        statement.accept(new StatementDeParser(deparsed));
        assertEquals(expected, deparsed.toString());
        assertEquals(expected, CCJSqlParserUtil.parse(expected,
                parser -> parser.withDialect(Dialect.MYSQL)).toString());
    }
}

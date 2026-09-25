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

import java.util.List;
import java.util.Set;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColumnOption;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.TableOption;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MySqlCommonTableOptionsTest {
    @ParameterizedTest
    @ValueSource(strings = {"ENGINE='InnoDB'", "ENGINE=\"InnoDB\"", "AVG_ROW_LENGTH=100",
            "MAX_ROWS=18446744073709551615", "MIN_ROWS 0", "KEY_BLOCK_SIZE=8",
            "TABLESPACE=innodb_file_per_table", "TABLESPACE `space name` STORAGE DISK",
            "TABLESPACE ts STORAGE MEMORY", "UNION=(db.m1,db.m2)", "UNION=()"})
    void sharesTableOptions(String sql) throws JSQLParserException {
        CreateTable create = (CreateTable) CCJSqlParserUtil.parse("CREATE TABLE t(id INT) " + sql);
        Alter alter = parse(sql + ", ADD COLUMN extra INT");
        AlterExpressionTableOption action =
                assertInstanceOf(AlterExpressionTableOption.class,
                        alter.getAlterExpressions().get(0));
        TableOption option = action.getStructuredTableOption();
        assertNotEquals(TableOption.Kind.OTHER, option.getKind());
        assertEquals(create.getTableOptions().get(0).toString(), option.toString());
        assertEquals(2, alter.getAlterExpressions().size());
        roundTrip(create);
        roundTrip(alter);
    }

    @Test
    void optionPairsKeepActionAndStatementBoundaries() throws JSQLParserException {
        List<String> options = List.of("ENGINE='InnoDB'", "ROW_FORMAT=DYNAMIC",
                "STATS_PERSISTENT=1", "COMMENT='x,y'", "AVG_ROW_LENGTH=100",
                "TABLESPACE=innodb_file_per_table", "UNION=(m1,m2)");
        for (String left : options) {
            for (String right : options) {
                if (left.equals(right)) {
                    continue;
                }
                for (String separator : new String[] {" ", ", "}) {
                    String sql = "ALTER TABLE t " + left + separator + right
                            + ", ADD COLUMN z INT, ALGORITHM=DEFAULT, LOCK=DEFAULT";
                    Alter alter = (Alter) CCJSqlParserUtil.parse(sql);
                    assertEquals(5, alter.getAlterExpressions().size(), sql);
                    assertEquals(AlterOperation.ADD,
                            alter.getAlterExpressions().get(2).getOperation());
                    assertEquals(AlterOperation.ALGORITHM,
                            alter.getAlterExpressions().get(3).getOperation());
                    assertEquals(AlterOperation.LOCK,
                            alter.getAlterExpressions().get(4).getOperation());
                    roundTrip(alter);
                    assertEquals(2, CCJSqlParserUtil.parseStatements(sql + "; SELECT 1").size());
                }
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"DEFAULT CHARACTER SET", "CHARACTER SET", "CHAR SET", "CHARSET",
            "DEFAULT CHARSET", "DEFAULT CHAR SET"})
    void quotedCharsetAndCollationKeepLegacyAccess(String name) throws JSQLParserException {
        Alter alter = parse(name + "='utf8mb4' COLLATE='utf8mb4_bin' ROW_FORMAT=DYNAMIC");
        AlterExpression action = alter.getAlterExpressions().get(0);
        assertEquals(AlterOperation.CONVERT, action.getOperation());
        assertEquals("'utf8mb4'", action.getCharacterSet());
        assertEquals("'utf8mb4_bin'", action.getCollation());
        assertEquals(2, alter.getAlterExpressions().size());
        action.setCharacterSet("utf8mb4");
        action.setCollation("utf8mb4_general_ci");
        roundTrip(alter);
    }

    @Test
    void structuredAndLegacyMutationsUseTheSameValues() throws JSQLParserException {
        Alter alter = parse("ENGINE=InnoDB KEY_BLOCK_SIZE=8 COMMENT='old'");
        AlterExpressionTableOption engine =
                (AlterExpressionTableOption) alter.getAlterExpressions().get(0);
        assertEquals(AlterOperation.ENGINE, engine.getOperation());
        engine.setEngineOption("MyISAM");
        assertEquals("MyISAM", engine.getStructuredTableOption().getValue());
        engine.getStructuredTableOption().setValue("InnoDB");
        assertEquals("InnoDB", engine.getEngineOption());
        AlterExpressionTableOption block =
                (AlterExpressionTableOption) alter.getAlterExpressions().get(1);
        assertEquals(AlterOperation.KEY_BLOCK_SIZE, block.getOperation());
        block.setKeyBlockSize(4);
        assertEquals("4", block.getStructuredTableOption().getValue());
        block.getStructuredTableOption().setValue("16");
        assertEquals(16, block.getKeyBlockSize());
        AlterExpressionTableOption comment =
                (AlterExpressionTableOption) alter.getAlterExpressions().get(2);
        comment.setCommentText("'new'");
        assertEquals("'new'", comment.getStructuredTableOption().getValue());
        comment.getStructuredTableOption().setUseEquals(false);
        assertEquals(AlterOperation.COMMENT, comment.getOperation());
        roundTrip(alter);
    }

    @Test
    void visitsAndChangesUnionSources() throws JSQLParserException {
        Alter alter = parse("UNION=(m1,m2)");
        TableOption option = ((AlterExpressionTableOption) alter.getAlterExpressions().get(0))
                .getStructuredTableOption();
        assertEquals(Set.of("t", "m1", "m2"), new TablesNamesFinder().getTables(alter));
        option.getUnionTables().set(1, new Table("m3"));
        assertEquals(Set.of("t", "m1", "m3"), new TablesNamesFinder().getTables(alter));
        assertTrue(alter.toString().contains("UNION = (m1, m3)"));
        roundTrip(alter);
    }

    @Test
    void tablespaceStorageHasStructuredAndLegacyProjections() throws JSQLParserException {
        CreateTable create = (CreateTable) CCJSqlParserUtil.parse(
                "CREATE TABLE t(id INT) TABLESPACE=ts STORAGE DISK");
        TableOption option = create.getTableOptions().get(0);
        assertEquals("ts", option.getValue());
        assertEquals(ColumnOption.Storage.DISK, option.getTablespaceStorage());
        option.setValue("new_ts");
        option.setTablespaceStorage(ColumnOption.Storage.MEMORY);
        assertEquals(List.of("TABLESPACE", "=", "new_ts", "STORAGE", "MEMORY"), option.getTokens());
        roundTrip(create);
        option.setKind(TableOption.Kind.ENGINE);
        assertNull(option.getTablespaceStorage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ENGINE=", "AVG_ROW_LENGTH=-1", "MAX_ROWS='1'", "TABLESPACE=",
            "TABLESPACE ts STORAGE UNKNOWN", "ENGINE=InnoDB ADD COLUMN z INT",
            "ENGINE=InnoDB LOCK=NONE", "ALGORITHM=DEFAULT ROW_FORMAT=DYNAMIC"})
    void rejectsIncompleteOptionsAndMissingActionCommas(String option) {
        assertThrows(JSQLParserException.class, () -> parse(option));
    }

    private static Alter parse(String sql) throws JSQLParserException {
        return (Alter) CCJSqlParserUtil.parse("ALTER TABLE t " + sql);
    }

    private static void roundTrip(Statement statement) throws JSQLParserException {
        StringBuilder out = new StringBuilder();
        statement.accept(new StatementDeParser(out));
        assertEquals(statement.toString(), out.toString());
        assertEquals(out.toString(), CCJSqlParserUtil.parse(out.toString()).toString());
    }
}

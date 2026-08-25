/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import org.junit.jupiter.api.Test;

public class CreateIndexTest {

    private final CCJSqlParserManager parserManager = new CCJSqlParserManager();

    @Test
    public void testCreateIndex() throws JSQLParserException {
        String statement = "CREATE INDEX myindex ON mytab (mycol, mycol2)";
        CreateIndex createIndex = (CreateIndex) parserManager.parse(new StringReader(statement));
        assertEquals(2, createIndex.getIndex().getColumnsNames().size());
        assertEquals("myindex", createIndex.getIndex().getName());
        assertNull(createIndex.getIndex().getType());
        assertEquals("mytab", createIndex.getTable().getFullyQualifiedName());
        assertEquals("mycol", createIndex.getIndex().getColumnsNames().get(0));
        assertEquals(statement, "" + createIndex);
    }

    @Test
    public void testCreateIndex2() throws JSQLParserException {
        String statement = "CREATE mytype INDEX myindex ON mytab (mycol, mycol2)";
        CreateIndex createIndex = (CreateIndex) parserManager.parse(new StringReader(statement));
        assertEquals(2, createIndex.getIndex().getColumnsNames().size());
        assertEquals("myindex", createIndex.getIndex().getName());
        assertEquals("mytype", createIndex.getIndex().getType());
        assertEquals("mytab", createIndex.getTable().getFullyQualifiedName());
        assertEquals("mycol2", createIndex.getIndex().getColumnsNames().get(1));
        assertEquals(statement, "" + createIndex);
    }

    @Test
    public void testCreateIndex3() throws JSQLParserException {
        String statement = "CREATE mytype INDEX myindex ON mytab (mycol ASC, mycol2, mycol3)";
        CreateIndex createIndex = (CreateIndex) parserManager.parse(new StringReader(statement));
        assertEquals(3, createIndex.getIndex().getColumnsNames().size());
        assertEquals("myindex", createIndex.getIndex().getName());
        assertEquals("mytype", createIndex.getIndex().getType());
        assertEquals("mytab", createIndex.getTable().getFullyQualifiedName());
        assertEquals("mycol3", createIndex.getIndex().getColumnsNames().get(2));
    }

    @Test
    public void testCreateIndex4() throws JSQLParserException {
        String statement = "CREATE mytype INDEX myindex ON mytab (mycol ASC, mycol2 (75), mycol3)";
        CreateIndex createIndex = (CreateIndex) parserManager.parse(new StringReader(statement));
        assertEquals(3, createIndex.getIndex().getColumnsNames().size());
        assertEquals("myindex", createIndex.getIndex().getName());
        assertEquals("mytype", createIndex.getIndex().getType());
        assertEquals("mytab", createIndex.getTable().getFullyQualifiedName());
        assertEquals("mycol3", createIndex.getIndex().getColumnsNames().get(2));
    }

    @Test
    public void testCreateIndex5() throws JSQLParserException {
        String statement =
                "CREATE mytype INDEX myindex ON mytab (mycol ASC, mycol2 (75), mycol3) mymodifiers";
        CreateIndex createIndex = (CreateIndex) parserManager.parse(new StringReader(statement));
        assertEquals(3, createIndex.getIndex().getColumnsNames().size());
        assertEquals("myindex", createIndex.getIndex().getName());
        assertEquals("mytype", createIndex.getIndex().getType());
        assertEquals("mytab", createIndex.getTable().getFullyQualifiedName());
        assertEquals("mycol3", createIndex.getIndex().getColumnsNames().get(2));
    }

    @Test
    public void testCreateIndex6() throws JSQLParserException {
        String stmt = "CREATE INDEX myindex ON mytab (mycol, mycol2)";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testCreateIndex7() throws JSQLParserException {
        String statement = "CREATE INDEX myindex1 ON mytab USING GIST (mycol)";
        CreateIndex createIndex = (CreateIndex) parserManager.parse(new StringReader(statement));
        assertEquals(1, createIndex.getIndex().getColumnsNames().size());
        assertEquals("myindex1", createIndex.getIndex().getName());
        assertNull(createIndex.getIndex().getType());
        assertEquals("mytab", createIndex.getTable().getFullyQualifiedName());
        assertEquals("mycol", createIndex.getIndex().getColumnsNames().get(0));
        assertEquals("GIST", createIndex.getIndex().getUsing());
        assertEquals(statement, "" + createIndex);
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testCreateIndexIssue633() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE INDEX idx_american_football_action_plays_1 ON american_football_action_plays USING btree (play_type)");
    }

    @Test
    public void testFullIndexNameIssue936() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE INDEX \"TS\".\"IDX\" ON \"TEST\" (\"ID\" ASC) TABLESPACE \"TS\"");
    }

    @Test
    public void testFullIndexNameIssue936_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE INDEX \"TS\".\"IDX\" ON \"TEST\" (\"ID\") TABLESPACE \"TS\"");
    }

    @Test
    public void testCreateIndexTrailingOptions() throws JSQLParserException {
        String statement = "CREATE UNIQUE INDEX cfe.version_info_idx2\n"
                + "    ON cfe.version_info ( major_version\n"
                + "                            , minor_version\n"
                + "                            , patch_level ) parallel compress nologging\n"
                + ";";
        CreateIndex createIndex = (CreateIndex) parserManager.parse(new StringReader(statement));
        List<String> tailParameters = createIndex.getTailParameters();
        assertEquals(3, tailParameters.size());
        assertEquals(tailParameters.get(0), "parallel");
        assertEquals(tailParameters.get(1), "compress");
        assertEquals(tailParameters.get(2), "nologging");
    }

    @Test
    void testIfNotExistsIssue1861() throws JSQLParserException {
        String sqlStr =
                "CREATE INDEX IF NOT EXISTS test_test_idx ON test.test USING btree (\"time\")";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testCreateIndexIssue1814() throws JSQLParserException {
        String sqlStr =
                "CREATE INDEX idx_operationlog_operatetime_regioncode USING BTREE ON operation_log (operate_time,region_biz_code)";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    public void testCreateIndexWithFunctionalKeyParts() throws JSQLParserException {
        String statement =
                "CREATE INDEX fAdd ON PPK_OLPN ((b + c), (COALESCE(PK, b)) DESC)";
        CreateIndex createIndex = (CreateIndex) parserManager.parse(new StringReader(statement));

        assertEquals(2, createIndex.getIndex().getColumns().size());
        assertTrue(createIndex.getIndex().getColumns().get(0).isExpression());
        assertEquals("b + c", createIndex.getIndex().getColumns().get(0).getColumnName());
        assertTrue(createIndex.getIndex().getColumns().get(1).isExpression());
        assertEquals("COALESCE(PK, b)", createIndex.getIndex().getColumns().get(1).getColumnName());
        assertNotNull(createIndex.getIndex().getColumns().get(1).getParams());
        assertEquals("DESC", createIndex.getIndex().getColumns().get(1).getParams().get(0));
        assertEquals(statement, createIndex.toString());

        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testCreateIndexVisibility() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE INDEX idx_a ON t1 (a) INVISIBLE", true);
        assertSqlCanBeParsedAndDeparsed("CREATE INDEX idx_a ON t1 (a) VISIBLE", true);
    }

    @Test
    public void testCreateIndexIncludeIssue2459() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE INDEX idx_a ON t1 (a) INCLUDE (b, c)");
    }

    @Test
    public void testCreateIndexKeyPartWithPrefixLengthAndDirectionIssue2490()
            throws JSQLParserException {
        // MySQL writes the prefix length without a space, JSqlParser deparses it with one.
        String statement = "CREATE INDEX i03 ON t (c1(20) DESC)";
        CreateIndex createIndex = (CreateIndex) parserManager.parse(new StringReader(statement));

        List<String> params = createIndex.getIndex().getColumns().get(0).getParams();
        assertEquals(2, params.size());
        assertEquals("(20)", params.get(0));
        assertEquals("DESC", params.get(1));

        assertSqlCanBeParsedAndDeparsed("CREATE INDEX i03 ON t (c1 (20) DESC)");
        assertSqlCanBeParsedAndDeparsed("CREATE INDEX i04 ON t (c1 (20) ASC, c2 (10) DESC)");
        assertSqlCanBeParsedAndDeparsed("CREATE UNIQUE INDEX i25 ON t (c1 (10) DESC)");
    }

    @Test
    public void testCreateIndexKeyBlockSizeIssue2490() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE INDEX i08 ON t (c1) KEY_BLOCK_SIZE = 8");
        assertSqlCanBeParsedAndDeparsed("CREATE INDEX i09 ON t (c1) KEY_BLOCK_SIZE 8");
        assertSqlCanBeParsedAndDeparsed(
                "CREATE INDEX i14 ON t (c1) USING BTREE KEY_BLOCK_SIZE = 8 COMMENT 'combo' INVISIBLE");
    }

    @Test
    public void testCreateIndexAlgorithmAndLockOptionsIssue2490() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE INDEX i10 ON t (c1) ALGORITHM = INPLACE LOCK = NONE");
        assertSqlCanBeParsedAndDeparsed("CREATE INDEX i11 ON t (c1) ALGORITHM INPLACE LOCK NONE");
        assertSqlCanBeParsedAndDeparsed("CREATE INDEX i12 ON t (c1) ALGORITHM = INPLACE");
        assertSqlCanBeParsedAndDeparsed("CREATE INDEX i13 ON t (c1) LOCK = NONE");

        CreateIndex createIndex = (CreateIndex) parserManager
                .parse(new StringReader("CREATE INDEX i10 ON t (c1) ALGORITHM=INPLACE LOCK=NONE"));
        assertEquals(List.of("ALGORITHM", "=", "INPLACE", "LOCK", "=", "NONE"),
                createIndex.getTailParameters());
    }

    @Test
    public void testCreateFullTextAndSpatialIndexIssue2490() throws JSQLParserException {
        // These used to fall back to UnsupportedStatement instead of producing a CreateIndex.
        CreateIndex fullText = (CreateIndex) parserManager
                .parse(new StringReader("CREATE FULLTEXT INDEX i17 ON t (body)"));
        assertEquals("FULLTEXT", fullText.getIndex().getType());

        CreateIndex spatial = (CreateIndex) parserManager
                .parse(new StringReader("CREATE SPATIAL INDEX i19 ON t (g)"));
        assertEquals("SPATIAL", spatial.getIndex().getType());

        assertSqlCanBeParsedAndDeparsed("CREATE FULLTEXT INDEX i17 ON t (body)");
        assertSqlCanBeParsedAndDeparsed("CREATE FULLTEXT INDEX i18 ON t (body) WITH PARSER ngram");
        assertSqlCanBeParsedAndDeparsed("CREATE SPATIAL INDEX i19 ON t (g)");
    }

    @Test
    public void testCreateMultiValuedIndexIssue2490() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE INDEX i20 ON t ((CAST(data -> '$.zips' AS UNSIGNED ARRAY)))");
    }
}

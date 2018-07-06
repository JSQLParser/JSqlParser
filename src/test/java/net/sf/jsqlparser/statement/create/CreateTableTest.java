package net.sf.jsqlparser.statement.create;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import junit.framework.TestCase;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.test.TestException;
import net.sf.jsqlparser.util.TablesNamesFinder;
import static net.sf.jsqlparser.test.TestUtils.*;

public class CreateTableTest extends TestCase {

    private CCJSqlParserManager parserManager = new CCJSqlParserManager();

    public CreateTableTest(String arg0) {
        super(arg0);
    }

    public void testCreateTable2() throws JSQLParserException {
        String statement = "CREATE TABLE testtab (\"test\" varchar (255))";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    public void testCreateTable3() throws JSQLParserException {
        String statement = "CREATE TABLE testtab (\"test\" varchar (255), \"test2\" varchar (255))";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    public void testCreateTableAsSelect() throws JSQLParserException, JSQLParserException, JSQLParserException, JSQLParserException {
        String statement = "CREATE TABLE a AS SELECT col1, col2 FROM b";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    public void testCreateTableAsSelect2() throws JSQLParserException {
        String statement = "CREATE TABLE newtable AS WITH a AS (SELECT col1, col3 FROM testtable) SELECT col1, col2, col3 FROM b INNER JOIN a ON b.col1 = a.col1";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    public void testCreateTable() throws JSQLParserException {
        String statement = "CREATE TABLE mytab (mycol a (10, 20) c nm g, mycol2 mypar1 mypar2 (23,323,3) asdf ('23','123') dasd, "
                + "PRIMARY KEY (mycol2, mycol)) type = myisam";
        CreateTable createTable = (CreateTable) parserManager.parse(new StringReader(statement));
        assertEquals(2, createTable.getColumnDefinitions().size());
        assertFalse(createTable.isUnlogged());
        assertEquals("mycol", ((ColumnDefinition) createTable.getColumnDefinitions().get(0)).
                getColumnName());
        assertEquals("mycol2", ((ColumnDefinition) createTable.getColumnDefinitions().get(1)).
                getColumnName());
        assertEquals("PRIMARY KEY", ((Index) createTable.getIndexes().get(0)).getType());
        assertEquals("mycol", ((Index) createTable.getIndexes().get(0)).getColumnsNames().get(1));
        assertEquals(statement, "" + createTable);
    }

    public void testCreateTableUnlogged() throws JSQLParserException {
        String statement = "CREATE UNLOGGED TABLE mytab (mycol a (10, 20) c nm g, mycol2 mypar1 mypar2 (23,323,3) asdf ('23','123') dasd, "
                + "PRIMARY KEY (mycol2, mycol)) type = myisam";
        CreateTable createTable = (CreateTable) parserManager.parse(new StringReader(statement));
        assertEquals(2, createTable.getColumnDefinitions().size());
        assertTrue(createTable.isUnlogged());
        assertEquals("mycol", ((ColumnDefinition) createTable.getColumnDefinitions().get(0)).
                getColumnName());
        assertEquals("mycol2", ((ColumnDefinition) createTable.getColumnDefinitions().get(1)).
                getColumnName());
        assertEquals("PRIMARY KEY", ((Index) createTable.getIndexes().get(0)).getType());
        assertEquals("mycol", ((Index) createTable.getIndexes().get(0)).getColumnsNames().get(1));
        assertEquals(statement, "" + createTable);
    }

    public void testCreateTableUnlogged2() throws JSQLParserException {
        String statement = "CREATE UNLOGGED TABLE mytab (mycol a (10, 20) c nm g, mycol2 mypar1 mypar2 (23,323,3) asdf ('23','123') dasd, PRIMARY KEY (mycol2, mycol))";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    public void testCreateTableForeignKey() throws JSQLParserException {
        String statement = "CREATE TABLE test (id INT UNSIGNED NOT NULL AUTO_INCREMENT, string VARCHAR (20), user_id INT UNSIGNED, PRIMARY KEY (id), FOREIGN KEY (user_id) REFERENCES ra_user(id))";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    public void testCreateTableForeignKey2() throws JSQLParserException {
        String statement = "CREATE TABLE test (id INT UNSIGNED NOT NULL AUTO_INCREMENT, string VARCHAR (20), user_id INT UNSIGNED, PRIMARY KEY (id), CONSTRAINT fkIdx FOREIGN KEY (user_id) REFERENCES ra_user(id))";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    public void testCreateTableForeignKey3() throws JSQLParserException {
        String statement = "CREATE TABLE test (id INT UNSIGNED NOT NULL AUTO_INCREMENT, string VARCHAR (20), user_id INT UNSIGNED REFERENCES ra_user(id), PRIMARY KEY (id))";
        assertSqlCanBeParsedAndDeparsed(statement, true);
    }

    public void testCreateTableForeignKey4() throws JSQLParserException {
        String statement = "CREATE TABLE test (id INT UNSIGNED NOT NULL AUTO_INCREMENT, string VARCHAR (20), user_id INT UNSIGNED FOREIGN KEY REFERENCES ra_user(id), PRIMARY KEY (id))";
        assertSqlCanBeParsedAndDeparsed(statement, true);
    }

    public void testCreateTablePrimaryKey() throws JSQLParserException {
        String statement = "CREATE TABLE test (id INT UNSIGNED NOT NULL AUTO_INCREMENT, string VARCHAR (20), user_id INT UNSIGNED, CONSTRAINT pk_name PRIMARY KEY (id))";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    public void testCreateTableParams() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TEMPORARY TABLE T1 (PROCESSID VARCHAR (32)) ON COMMIT PRESERVE ROWS");
    }

    public void testCreateTableUniqueConstraint() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE Activities (_id INTEGER PRIMARY KEY AUTOINCREMENT,uuid VARCHAR(255),user_id INTEGER,sound_id INTEGER,sound_type INTEGER,comment_id INTEGER,type String,tags VARCHAR(255),created_at INTEGER,content_id INTEGER,sharing_note_text VARCHAR(255),sharing_note_created_at INTEGER,UNIQUE (created_at, type, content_id, sound_id, user_id))", true);
    }

    public void testCreateTableDefault() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE T1 (id integer default -1)");
    }

    public void testCreateTableDefault2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE T1 (id integer default 1)");
    }

    public void testCreateTableIfNotExists() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE IF NOT EXISTS animals (id INT NOT NULL)");
    }

    public void testCreateTableInlinePrimaryKey() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE animals (id INT PRIMARY KEY NOT NULL)");
    }

    public void testCreateTableWithRange() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE foo (name character varying (255), range character varying (255), start_range integer, end_range integer)");
    }

    public void testCreateTableWithKey() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE bar (key character varying (255) NOT NULL)");
    }

    public void testCreateTableWithUniqueKey() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE animals (id INT NOT NULL, name VARCHAR (100) UNIQUE KEY (id))");
    }

    public void testCreateTableVeryComplex() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE `wp_commentmeta` ( `meta_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT, `comment_id` bigint(20) unsigned NOT NULL DEFAULT '0', `meta_key` varchar(255) DEFAULT NULL, `meta_value` longtext, PRIMARY KEY (`meta_id`), KEY `comment_id` (`comment_id`), KEY `meta_key` (`meta_key`) ) ENGINE=InnoDB DEFAULT CHARSET=utf8", true);
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE `wp_comments` ( `comment_ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT, `comment_post_ID` bigint(20) unsigned NOT NULL DEFAULT '0', `comment_author` tinytext NOT NULL, `comment_author_email` varchar(100) NOT NULL DEFAULT '', `comment_author_url` varchar(200) NOT NULL DEFAULT '', `comment_author_IP` varchar(100) NOT NULL DEFAULT '', `comment_date` datetime NOT NULL DEFAULT '0000-00-00 00:00:00', `comment_date_gmt` datetime NOT NULL DEFAULT '0000-00-00 00:00:00', `comment_content` text NOT NULL, `comment_karma` int(11) NOT NULL DEFAULT '0', `comment_approved` varchar(20) NOT NULL DEFAULT '1', `comment_agent` varchar(255) NOT NULL DEFAULT '', `comment_type` varchar(20) NOT NULL DEFAULT '', `comment_parent` bigint(20) unsigned NOT NULL DEFAULT '0', `user_id` bigint(20) unsigned NOT NULL DEFAULT '0', PRIMARY KEY (`comment_ID`), KEY `comment_post_ID` (`comment_post_ID`), KEY `comment_approved_date_gmt` (`comment_approved`,`comment_date_gmt`), KEY `comment_date_gmt` (`comment_date_gmt`), KEY `comment_parent` (`comment_parent`) ) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8", true);
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE `wp_links` ( `link_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT, `link_url` varchar(255) NOT NULL DEFAULT '', `link_name` varchar(255) NOT NULL DEFAULT '', `link_image` varchar(255) NOT NULL DEFAULT '', `link_target` varchar(25) NOT NULL DEFAULT '', `link_description` varchar(255) NOT NULL DEFAULT '', `link_visible` varchar(20) NOT NULL DEFAULT 'Y', `link_owner` bigint(20) unsigned NOT NULL DEFAULT '1', `link_rating` int(11) NOT NULL DEFAULT '0', `link_updated` datetime NOT NULL DEFAULT '0000-00-00 00:00:00', `link_rel` varchar(255) NOT NULL DEFAULT '', `link_notes` mediumtext NOT NULL, `link_rss` varchar(255) NOT NULL DEFAULT '', PRIMARY KEY (`link_id`), KEY `link_visible` (`link_visible`) ) ENGINE=InnoDB DEFAULT CHARSET=utf8", true);
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE `wp_options` ( `option_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT, `option_name` varchar(64) NOT NULL DEFAULT '', `option_value` longtext NOT NULL, `autoload` varchar(20) NOT NULL DEFAULT 'yes', PRIMARY KEY (`option_id`), UNIQUE KEY `option_name` (`option_name`) ) ENGINE=InnoDB AUTO_INCREMENT=402 DEFAULT CHARSET=utf8", true);
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE `wp_postmeta` ( `meta_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT, `post_id` bigint(20) unsigned NOT NULL DEFAULT '0', `meta_key` varchar(255) DEFAULT NULL, `meta_value` longtext, PRIMARY KEY (`meta_id`), KEY `post_id` (`post_id`), KEY `meta_key` (`meta_key`) ) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8", true);
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE `wp_posts` ( `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT, `post_author` bigint(20) unsigned NOT NULL DEFAULT '0', `post_date` datetime NOT NULL DEFAULT '0000-00-00 00:00:00', `post_date_gmt` datetime NOT NULL DEFAULT '0000-00-00 00:00:00', `post_content` longtext NOT NULL, `post_title` text NOT NULL, `post_excerpt` text NOT NULL, `post_status` varchar(20) NOT NULL DEFAULT 'publish', `comment_status` varchar(20) NOT NULL DEFAULT 'open', `ping_status` varchar(20) NOT NULL DEFAULT 'open', `post_password` varchar(20) NOT NULL DEFAULT '', `post_name` varchar(200) NOT NULL DEFAULT '', `to_ping` text NOT NULL, `pinged` text NOT NULL, `post_modified` datetime NOT NULL DEFAULT '0000-00-00 00:00:00', `post_modified_gmt` datetime NOT NULL DEFAULT '0000-00-00 00:00:00', `post_content_filtered` longtext NOT NULL, `post_parent` bigint(20) unsigned NOT NULL DEFAULT '0', `guid` varchar(255) NOT NULL DEFAULT '', `menu_order` int(11) NOT NULL DEFAULT '0', `post_type` varchar(20) NOT NULL DEFAULT 'post', `post_mime_type` varchar(100) NOT NULL DEFAULT '', `comment_count` bigint(20) NOT NULL DEFAULT '0', PRIMARY KEY (`ID`), KEY `post_name` (`post_name`), KEY `type_status_date` (`post_type`,`post_status`,`post_date`,`ID`), KEY `post_parent` (`post_parent`), KEY `post_author` (`post_author`) ) ENGINE=InnoDB AUTO_INCREMENT=55004 DEFAULT CHARSET=utf8", true);
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE `wp_term_relationships` ( `object_id` bigint(20) unsigned NOT NULL DEFAULT '0', `term_taxonomy_id` bigint(20) unsigned NOT NULL DEFAULT '0', `term_order` int(11) NOT NULL DEFAULT '0', PRIMARY KEY (`object_id`,`term_taxonomy_id`), KEY `term_taxonomy_id` (`term_taxonomy_id`) ) ENGINE=InnoDB DEFAULT CHARSET=utf8", true);
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE `wp_term_taxonomy` ( `term_taxonomy_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT, `term_id` bigint(20) unsigned NOT NULL DEFAULT '0', `taxonomy` varchar(32) NOT NULL DEFAULT '', `description` longtext NOT NULL, `parent` bigint(20) unsigned NOT NULL DEFAULT '0', `count` bigint(20) NOT NULL DEFAULT '0', PRIMARY KEY (`term_taxonomy_id`), UNIQUE KEY `term_id_taxonomy` (`term_id`,`taxonomy`), KEY `taxonomy` (`taxonomy`) ) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8", true);
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE `wp_terms` ( `term_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT, `name` varchar(200) NOT NULL DEFAULT '', `slug` varchar(200) NOT NULL DEFAULT '', `term_group` bigint(10) NOT NULL DEFAULT '0', PRIMARY KEY (`term_id`), UNIQUE KEY `slug` (`slug`), KEY `name` (`name`) ) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8", true);
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE `wp_usermeta` ( `umeta_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT, `user_id` bigint(20) unsigned NOT NULL DEFAULT '0', `meta_key` varchar(255) DEFAULT NULL, `meta_value` longtext, PRIMARY KEY (`umeta_id`), KEY `user_id` (`user_id`), KEY `meta_key` (`meta_key`) ) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8", true);
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE `wp_users` ( `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT, `user_login` varchar(60) NOT NULL DEFAULT '', `user_pass` varchar(64) NOT NULL DEFAULT '', `user_nicename` varchar(50) NOT NULL DEFAULT '', `user_email` varchar(100) NOT NULL DEFAULT '', `user_url` varchar(100) NOT NULL DEFAULT '', `user_registered` datetime NOT NULL DEFAULT '0000-00-00 00:00:00', `user_activation_key` varchar(60) NOT NULL DEFAULT '', `user_status` int(11) NOT NULL DEFAULT '0', `display_name` varchar(250) NOT NULL DEFAULT '', PRIMARY KEY (`ID`), KEY `user_login_key` (`user_login`), KEY `user_nicename` (`user_nicename`) ) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8", true);
    }

    public void testCreateTableArrays() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE sal_emp (name text, pay_by_quarter integer[], schedule text[][])");
    }

    public void testCreateTableArrays2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE sal_emp (name text, pay_by_quarter integer[5], schedule text[3][2])");
    }

    public void testCreateTableColumnValues() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE mytable1 (values INTEGER)");
    }

    public void testCreateTableColumnValue() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE mytable1 (value INTEGER)");
    }

    public void testCreateTableForeignKey5() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE IF NOT EXISTS table1 (id INTEGER PRIMARY KEY AUTO_INCREMENT, aid INTEGER REFERENCES accounts ON aid ON DELETE CASCADE, name STRING, lastname STRING)");
    }

    public void testCreateTableForeignKey6() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE test (id long, fkey long references another_table (id))");
    }

    public void testMySqlCreateTableOnUpdateCurrentTimestamp() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE test (applied timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)");
    }

    public void testMySqlCreateTableWithConstraintWithCascade() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE table1 (id INT (10) UNSIGNED NOT NULL AUTO_INCREMENT, t2_id INT (10) UNSIGNED DEFAULT NULL, t3_id INT (10) UNSIGNED DEFAULT NULL, t4_id INT (10) UNSIGNED NOT NULL, PRIMARY KEY (id), KEY fkc_table1_t4 (t4_id), KEY fkc_table1_t2 (t2_id), KEY fkc_table1_t3 (t3_id), CONSTRAINT fkc_table1_t2 FOREIGN KEY (t2_id) REFERENCES table_two(t2o_id) ON DELETE CASCADE ON UPDATE CASCADE, CONSTRAINT fkc_table1_t3 FOREIGN KEY (t3_id) REFERENCES table_three(t3o_id) ON UPDATE CASCADE, CONSTRAINT fkc_table1_t4 FOREIGN KEY (t4_id) REFERENCES table_four(t4o_id) ON DELETE CASCADE) ENGINE = InnoDB AUTO_INCREMENT = 8761 DEFAULT CHARSET = utf8");
    }

    public void testMySqlCreateTableWithConstraintWithNoAction() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE table1 (id INT (10) UNSIGNED NOT NULL AUTO_INCREMENT, t2_id INT (10) UNSIGNED DEFAULT NULL, t3_id INT (10) UNSIGNED DEFAULT NULL, t4_id INT (10) UNSIGNED NOT NULL, PRIMARY KEY (id), KEY fkc_table1_t4 (t4_id), KEY fkc_table1_t2 (t2_id), KEY fkc_table1_t3 (t3_id), CONSTRAINT fkc_table1_t2 FOREIGN KEY (t2_id) REFERENCES table_two(t2o_id) ON DELETE NO ACTION ON UPDATE NO ACTION, CONSTRAINT fkc_table1_t3 FOREIGN KEY (t3_id) REFERENCES table_three(t3o_id) ON UPDATE NO ACTION, CONSTRAINT fkc_table1_t4 FOREIGN KEY (t4_id) REFERENCES table_four(t4o_id) ON DELETE NO ACTION) ENGINE = InnoDB AUTO_INCREMENT = 8761 DEFAULT CHARSET = utf8");
    }

    public void testMySqlCreateTableWithTextIndexes() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE table2 (id INT (10) UNSIGNED NOT NULL AUTO_INCREMENT, name TEXT, url TEXT, created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, PRIMARY KEY (id), FULLTEXT KEY idx_table2_name (name)) ENGINE = InnoDB AUTO_INCREMENT = 7334 DEFAULT CHARSET = utf8");
    }

    public void testCreateTableWithCheck() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE table2 (id INT (10) NOT NULL, name TEXT, url TEXT, CONSTRAINT name_not_empty CHECK (name <> ''))");
    }

    public void testCreateTableIssue270() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE item (i_item_sk integer NOT NULL, i_item_id character (16) NOT NULL, i_rec_start_date date, i_rec_end_date date, i_item_desc character varying(200), i_current_price numeric(7,2), i_wholesale_cost numeric(7,2), i_brand_id integer, i_brand character(50), i_class_id integer, i_class character(50), i_category_id integer, i_category character(50), i_manufact_id integer, i_manufact character(50), i_size character(20), i_formulation character(20), i_color character(20), i_units character(10), i_container character(10), i_manager_id integer, i_product_name character(50) )", true);
    }

    public void testCreateTableIssue270_1() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE item (i_item_sk integer NOT NULL, i_item_id character (16))");
    }

    public void testCreateTempTableIssue293() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE GLOBAL TEMPORARY TABLE T1 (PROCESSID VARCHAR (32))");
    }

    public void testCreateTableWithTablespaceIssue247() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE TABLE1 (COLUMN1 VARCHAR2 (15), COLUMN2 VARCHAR2 (15), CONSTRAINT P_PK PRIMARY KEY (COLUMN1) USING INDEX TABLESPACE \"T_INDEX\") TABLESPACE \"T_SPACE\"");
    }

    public void testCreateTableWithTablespaceIssue247_1() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE TABLE1 (COLUMN1 VARCHAR2 (15), COLUMN2 VARCHAR2 (15), CONSTRAINT P_PK PRIMARY KEY (COLUMN1) USING INDEX TABLESPACE \"T_INDEX\")");
    }

    public void testOnDeleteSetNull() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE inventory (inventory_id INT PRIMARY KEY, product_id INT, CONSTRAINT fk_inv_product_id FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE SET NULL)");
    }

    public void testColumnCheck() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE table1 (col1 INTEGER CHECK (col1 > 100))");
    }

    public void testTableReferenceWithSchema() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE table1 (col1 INTEGER REFERENCES schema1.table1)");
    }

    public void testNamedColumnConstraint() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE foo (col1 integer CONSTRAINT no_null NOT NULL)");
    }

    public void testColumnConstraintWith() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE foo (col1 integer) WITH (fillfactor=70)");
    }

    public void testExcludeWhereConstraint() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE foo (col1 integer, EXCLUDE WHERE (col1 > 100))");
    }

    public void testTimestampWithoutTimezone() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE abc.tabc (transaction_date TIMESTAMP WITHOUT TIME ZONE)");
    }

    public void testCreateUnitonIssue402() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE temp.abc AS SELECT sku FROM temp.a UNION SELECT sku FROM temp.b");
    }

    public void testCreateUnitonIssue402_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE temp.abc AS (SELECT sku FROM temp.a UNION SELECT sku FROM temp.b)");
    }
    
    public void testTimestampWithTimezone() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE country_region (" +
                "regionid BIGINT NOT NULL CONSTRAINT pk_auth_region PRIMARY KEY, " +
                "region_name VARCHAR (100) NOT NULL, " +
                "creation_date TIMESTAMP (0) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP (0) NOT NULL, " +
                "last_change_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP (0), " +
                "CONSTRAINT region_name_unique UNIQUE (region_name))");
    }
    
    public void testCreateTableAsSelect3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE public.sales1 AS (SELECT * FROM public.sales)");
    }
    
    public void testQuotedPKColumnsIssue491() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE `FOO` (`ID` INT64, `NAME` STRING (100)) PRIMARY KEY (`ID`)");
    }
    
    public void testQuotedPKColumnsIssue491_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE `FOO` (`ID` INT64, `NAME` STRING (100), PRIMARY KEY (`ID`))");
    }
    
    public void testKeySyntaxWithLengthColumnParameter() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE basic (BASIC_TITLE varchar (255) NOT NULL, KEY BASIC_TITLE (BASIC_TITLE(255)))");
    }

    public void testRUBiSCreateList() throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(CreateTableTest.class.
                getResourceAsStream("/RUBiS-create-requests.txt")));
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();

        try {
            int numSt = 1;
            while (true) {
                String line = getLine(in);
                if (line == null) {
                    break;
                }

                if (!"#begin".equals(line)) {
                    break;
                }
                line = getLine(in);
                StringBuilder buf = new StringBuilder(line);
                while (true) {
                    line = getLine(in);
                    if ("#end".equals(line)) {
                        break;
                    }
                    buf.append("\n");
                    buf.append(line);
                }

                String query = buf.toString();
                if (!getLine(in).equals("true")) {
                    continue;
                }

                String tableName = getLine(in);
                String cols = getLine(in);
                try {
                    CreateTable createTable = (CreateTable) parserManager.
                            parse(new StringReader(query));
                    String[] colsList = null;
                    if ("null".equals(cols)) {
                        colsList = new String[0];
                    } else {
                        StringTokenizer tokenizer = new StringTokenizer(cols, " ");

                        List colsListList = new ArrayList();
                        while (tokenizer.hasMoreTokens()) {
                            colsListList.add(tokenizer.nextToken());
                        }

                        colsList = (String[]) colsListList.toArray(new String[colsListList.size()]);

                    }
                    List colsFound = new ArrayList();
                    if (createTable.getColumnDefinitions() != null) {
                        for (Iterator iter = createTable.getColumnDefinitions().iterator(); iter.
                                hasNext();) {
                            ColumnDefinition columnDefinition = (ColumnDefinition) iter.next();
                            String colName = columnDefinition.getColumnName();
                            boolean unique = false;
                            if (createTable.getIndexes() != null) {
                                for (Iterator iterator = createTable.getIndexes().iterator(); iterator.
                                        hasNext();) {
                                    Index index = (Index) iterator.next();
                                    if (index.getType().equals("PRIMARY KEY") && index.
                                            getColumnsNames().size() == 1
                                            && index.getColumnsNames().get(0).equals(colName)) {
                                        unique = true;
                                    }

                                }
                            }

                            if (!unique) {
                                if (columnDefinition.getColumnSpecStrings() != null) {
                                    for (Iterator iterator = columnDefinition.getColumnSpecStrings().
                                            iterator(); iterator
                                                    .hasNext();) {
                                        String par = (String) iterator.next();
                                        if (par.equals("UNIQUE")) {
                                            unique = true;
                                        } else if (par.equals("PRIMARY") && iterator.hasNext()
                                                && iterator.next().equals("KEY")) {
                                            unique = true;
                                        }
                                    }
                                }
                            }
                            if (unique) {
                                colName += ".unique";
                            }
                            colsFound.add(colName.toLowerCase());
                        }
                    }

                    assertEquals("stm:" + query, colsList.length, colsFound.size());

                    for (int i = 0; i < colsList.length; i++) {
                        assertEquals("stm:" + query, colsList[i], colsFound.get(i));

                    }
                } catch (Exception e) {
                    throw new TestException("error at stm num: " + numSt + "  " + query, e);
                }
                numSt++;

            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    private String getLine(BufferedReader in) throws Exception {
        String line = null;
        while (true) {
            line = in.readLine();
            if (line != null) {
                if ((line.length() != 0)
                        && ((line.length() < 2) || (line.length() >= 2)
                        && !(line.charAt(0) == '/' && line.charAt(1) == '/'))) {
                    break;
                }
            } else {
                break;
            }

        }

        return line;
    }
}

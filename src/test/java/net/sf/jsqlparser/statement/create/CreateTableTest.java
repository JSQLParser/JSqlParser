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

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.ExcludeConstraint;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.create.table.RowMovementMode;
import net.sf.jsqlparser.test.TestException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreateTableTest {

    private final CCJSqlParserManager parserManager = new CCJSqlParserManager();

    @Test
    public void testCreateTableOrReplace() throws JSQLParserException {
        String statement = "CREATE OR REPLACE TABLE testtab (\"test\" varchar (255))";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testCreateTable2() throws JSQLParserException {
        String statement = "CREATE TABLE testtab (\"test\" varchar (255))";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testCreateTable3() throws JSQLParserException {
        String statement = "CREATE TABLE testtab (\"test\" varchar (255), \"test2\" varchar (255))";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testCreateTableAsSelect()
            throws JSQLParserException {
        String statement = "CREATE TABLE a AS SELECT col1, col2 FROM b";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testCreateTableAsSelect2() throws JSQLParserException {
        String statement =
                "CREATE TABLE newtable AS WITH a AS (SELECT col1, col3 FROM testtable) SELECT col1, col2, col3 FROM b INNER JOIN a ON b.col1 = a.col1";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testCreateTable() throws JSQLParserException {
        String statement =
                "CREATE TABLE mytab (mycol a (10, 20) c nm g, mycol2 mypar1 mypar2 (23,323,3) asdf ('23','123') dasd, "
                        + "PRIMARY KEY (mycol2, mycol)) type = myisam";
        CreateTable createTable = (CreateTable) parserManager.parse(new StringReader(statement));
        assertEquals(2, createTable.getColumnDefinitions().size());
        assertFalse(createTable.isUnlogged());
        assertEquals("mycol", createTable.getColumnDefinitions().get(0).getColumnName());
        assertEquals("mycol2", createTable.getColumnDefinitions().get(1).getColumnName());
        assertEquals("PRIMARY KEY", createTable.getIndexes().get(0).getType());
        assertEquals("mycol", createTable.getIndexes().get(0).getColumnsNames().get(1));
        assertEquals(statement, "" + createTable);
    }

    @Test
    public void testCreateTableUnlogged() throws JSQLParserException {
        String statement =
                "CREATE UNLOGGED TABLE mytab (mycol a (10, 20) c nm g, mycol2 mypar1 mypar2 (23,323,3) asdf ('23','123') dasd, "
                        + "PRIMARY KEY (mycol2, mycol)) type = myisam";
        CreateTable createTable = (CreateTable) parserManager.parse(new StringReader(statement));
        assertEquals(2, createTable.getColumnDefinitions().size());
        assertTrue(createTable.isUnlogged());
        assertEquals("mycol", createTable.getColumnDefinitions().get(0).getColumnName());
        assertEquals("mycol2", createTable.getColumnDefinitions().get(1).getColumnName());
        assertEquals("PRIMARY KEY", createTable.getIndexes().get(0).getType());
        assertEquals("mycol", createTable.getIndexes().get(0).getColumnsNames().get(1));
        assertEquals(statement, "" + createTable);
    }

    @Test
    public void testCreateTableUnlogged2() throws JSQLParserException {
        String statement =
                "CREATE UNLOGGED TABLE mytab (mycol a (10, 20) c nm g, mycol2 mypar1 mypar2 (23,323,3) asdf ('23','123') dasd, PRIMARY KEY (mycol2, mycol))";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testCreateTableForeignKey() throws JSQLParserException {
        String statement =
                "CREATE TABLE test (id INT UNSIGNED NOT NULL AUTO_INCREMENT, string VARCHAR (20), user_id INT UNSIGNED, PRIMARY KEY (id), FOREIGN KEY (user_id) REFERENCES ra_user(id))";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testCreateTableForeignKey2() throws JSQLParserException {
        String statement =
                "CREATE TABLE test (id INT UNSIGNED NOT NULL AUTO_INCREMENT, string VARCHAR (20), user_id INT UNSIGNED, PRIMARY KEY (id), CONSTRAINT fkIdx FOREIGN KEY (user_id) REFERENCES ra_user(id))";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testCreateTableForeignKey3() throws JSQLParserException {
        String statement =
                "CREATE TABLE test (id INT UNSIGNED NOT NULL AUTO_INCREMENT, string VARCHAR (20), user_id INT UNSIGNED REFERENCES ra_user(id), PRIMARY KEY (id))";
        assertSqlCanBeParsedAndDeparsed(statement, true);
    }

    @Test
    public void testCreateTableForeignKey4() throws JSQLParserException {
        String statement =
                "CREATE TABLE test (id INT UNSIGNED NOT NULL AUTO_INCREMENT, string VARCHAR (20), user_id INT UNSIGNED FOREIGN KEY REFERENCES ra_user(id), PRIMARY KEY (id))";
        assertSqlCanBeParsedAndDeparsed(statement, true);
    }

    @Test
    public void testCreateTablePrimaryKey() throws JSQLParserException {
        String statement =
                "CREATE TABLE test (id INT UNSIGNED NOT NULL AUTO_INCREMENT, string VARCHAR (20), user_id INT UNSIGNED, CONSTRAINT pk_name PRIMARY KEY (id))";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testCreateTableParams() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TEMPORARY TABLE T1 (PROCESSID VARCHAR (32)) ON COMMIT PRESERVE ROWS");
    }

    @Test
    public void testCreateTableParams2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TEMPORARY TABLE t1 WITH (APPENDONLY=true,ORIENTATION=column,COMPRESSTYPE=zlib,OIDS=FALSE) ON COMMIT DROP AS SELECT column FROM t2");
    }

    @Test
    public void testCreateTableUniqueConstraint() throws JSQLParserException {
        String sqlStr =
                "CREATE TABLE Activities ("
                        + "_id INTEGER PRIMARY KEY AUTOINCREMENT"
                        + ",uuid VARCHAR(255)"
                        + ",user_id INTEGER"
                        + ",sound_id INTEGER"
                        + ",sound_type INTEGER"
                        + ",comment_id INTEGER"
                        + ",type String,tags VARCHAR(255)"
                        + ",created_at INTEGER"
                        + ",content_id INTEGER"
                        + ",sharing_note_text VARCHAR(255)"
                        + ",sharing_note_created_at INTEGER"
                        + ",UNIQUE (created_at, type, content_id, sound_id, user_id)"
                        + ")";

        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        assertTrue(CCJSqlParserUtil.parseStatements(sqlStr).getStatements()
                .get(0) instanceof CreateTable);
    }

    @Test
    public void testCreateTableUniqueConstraintAfterPrimaryKey() throws JSQLParserException {
        String sqlStr = "-- UniqueConstraintAfterPrimaryKey\n"
                + "CREATE TABLE employees (\n"
                + "    employee_number    int         NOT NULL\n"
                + "    , employee_name    char (50)   NOT NULL\n"
                + "    , department_id    int\n"
                + "    , salary           int\n"
                + "    , PRIMARY KEY (employee_number)\n"
                + "    , UNIQUE (employee_name)\n"
                + "    , FOREIGN KEY (department_id)\n"
                + "        REFERENCES departments(department_id)\n"
                + "  ) parallel compress nologging";

        assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        CreateTable createTable =
                (CreateTable) CCJSqlParserUtil.parseStatements(sqlStr).getStatements().get(0);

        assertEquals("PRIMARY KEY", createTable.getIndexes().get(0).getType());
        assertEquals("UNIQUE", createTable.getIndexes().get(1).getType());
        assertEquals("FOREIGN KEY", createTable.getIndexes().get(2).getType());
    }

    @Test
    public void testCreateTableDefault() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE T1 (id integer default -1)");
    }

    @Test
    public void testCreateTableDefault2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE T1 (id integer default 1)");
    }

    @Test
    public void testCreateTableClickHouseMaterializedColumn() throws JSQLParserException {
        String statement = "CREATE TABLE t (\n"
                + "    url String,\n"
                + "    domain String MATERIALIZED regexpExtract(url, '^(?:https?://)?([^/]+)', 1)\n"
                + ")\n"
                + "ENGINE = MergeTree()\n"
                + "ORDER BY tuple()";
        assertSqlCanBeParsedAndDeparsed(statement, true);
    }

    @Test
    public void testCreateTableClickHouseSampleBy() throws JSQLParserException {
        String statement = "CREATE TABLE tmp.events (\n"
                + "    id UInt64,\n"
                + "    user_id UInt32,\n"
                + "    timestamp DateTime\n"
                + ")\n"
                + "ENGINE = MergeTree()\n"
                + "ORDER BY id\n"
                + "SAMPLE BY id";
        assertSqlCanBeParsedAndDeparsed(statement, true);
    }

    @Test
    public void testCreateTableIfNotExists() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE IF NOT EXISTS animals (id INT NOT NULL)");
    }

    @Test
    public void testCreateTableInlinePrimaryKey() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE animals (id INT PRIMARY KEY NOT NULL)");
    }

    @Test
    public void testCreateTableWithRange() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE foo (name character varying (255), range character varying (255), start_range integer, end_range integer)");
    }

    @Test
    public void testCreateTableWithKey() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE bar (key character varying (255) NOT NULL)");
    }

    @Test
    public void testCreateTableWithUniqueKey() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE animals (id INT NOT NULL, name VARCHAR (100) UNIQUE KEY (id))");
    }

    @Test
    public void testCreateTableVeryComplex() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE `wp_commentmeta` ( `meta_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT, `comment_id` bigint(20) unsigned NOT NULL DEFAULT '0', `meta_key` varchar(255) DEFAULT NULL, `meta_value` longtext, PRIMARY KEY (`meta_id`), KEY `comment_id` (`comment_id`), KEY `meta_key` (`meta_key`) ) ENGINE=InnoDB DEFAULT CHARSET=utf8",
                true);
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE `wp_comments` ( `comment_ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT, `comment_post_ID` bigint(20) unsigned NOT NULL DEFAULT '0', `comment_author` tinytext NOT NULL, `comment_author_email` varchar(100) NOT NULL DEFAULT '', `comment_author_url` varchar(200) NOT NULL DEFAULT '', `comment_author_IP` varchar(100) NOT NULL DEFAULT '', `comment_date` datetime NOT NULL DEFAULT '0000-00-00 00:00:00', `comment_date_gmt` datetime NOT NULL DEFAULT '0000-00-00 00:00:00', `comment_content` text NOT NULL, `comment_karma` int(11) NOT NULL DEFAULT '0', `comment_approved` varchar(20) NOT NULL DEFAULT '1', `comment_agent` varchar(255) NOT NULL DEFAULT '', `comment_type` varchar(20) NOT NULL DEFAULT '', `comment_parent` bigint(20) unsigned NOT NULL DEFAULT '0', `user_id` bigint(20) unsigned NOT NULL DEFAULT '0', PRIMARY KEY (`comment_ID`), KEY `comment_post_ID` (`comment_post_ID`), KEY `comment_approved_date_gmt` (`comment_approved`,`comment_date_gmt`), KEY `comment_date_gmt` (`comment_date_gmt`), KEY `comment_parent` (`comment_parent`) ) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8",
                true);
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE `wp_links` ( `link_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT, `link_url` varchar(255) NOT NULL DEFAULT '', `link_name` varchar(255) NOT NULL DEFAULT '', `link_image` varchar(255) NOT NULL DEFAULT '', `link_target` varchar(25) NOT NULL DEFAULT '', `link_description` varchar(255) NOT NULL DEFAULT '', `link_visible` varchar(20) NOT NULL DEFAULT 'Y', `link_owner` bigint(20) unsigned NOT NULL DEFAULT '1', `link_rating` int(11) NOT NULL DEFAULT '0', `link_updated` datetime NOT NULL DEFAULT '0000-00-00 00:00:00', `link_rel` varchar(255) NOT NULL DEFAULT '', `link_notes` mediumtext NOT NULL, `link_rss` varchar(255) NOT NULL DEFAULT '', PRIMARY KEY (`link_id`), KEY `link_visible` (`link_visible`) ) ENGINE=InnoDB DEFAULT CHARSET=utf8",
                true);
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE `wp_options` ( `option_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT, `option_name` varchar(64) NOT NULL DEFAULT '', `option_value` longtext NOT NULL, `autoload` varchar(20) NOT NULL DEFAULT 'yes', PRIMARY KEY (`option_id`), UNIQUE KEY `option_name` (`option_name`) ) ENGINE=InnoDB AUTO_INCREMENT=402 DEFAULT CHARSET=utf8",
                true);
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE `wp_postmeta` ( `meta_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT, `post_id` bigint(20) unsigned NOT NULL DEFAULT '0', `meta_key` varchar(255) DEFAULT NULL, `meta_value` longtext, PRIMARY KEY (`meta_id`), KEY `post_id` (`post_id`), KEY `meta_key` (`meta_key`) ) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8",
                true);
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE `wp_posts` ( `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT, `post_author` bigint(20) unsigned NOT NULL DEFAULT '0', `post_date` datetime NOT NULL DEFAULT '0000-00-00 00:00:00', `post_date_gmt` datetime NOT NULL DEFAULT '0000-00-00 00:00:00', `post_content` longtext NOT NULL, `post_title` text NOT NULL, `post_excerpt` text NOT NULL, `post_status` varchar(20) NOT NULL DEFAULT 'publish', `comment_status` varchar(20) NOT NULL DEFAULT 'open', `ping_status` varchar(20) NOT NULL DEFAULT 'open', `post_password` varchar(20) NOT NULL DEFAULT '', `post_name` varchar(200) NOT NULL DEFAULT '', `to_ping` text NOT NULL, `pinged` text NOT NULL, `post_modified` datetime NOT NULL DEFAULT '0000-00-00 00:00:00', `post_modified_gmt` datetime NOT NULL DEFAULT '0000-00-00 00:00:00', `post_content_filtered` longtext NOT NULL, `post_parent` bigint(20) unsigned NOT NULL DEFAULT '0', `guid` varchar(255) NOT NULL DEFAULT '', `menu_order` int(11) NOT NULL DEFAULT '0', `post_type` varchar(20) NOT NULL DEFAULT 'post', `post_mime_type` varchar(100) NOT NULL DEFAULT '', `comment_count` bigint(20) NOT NULL DEFAULT '0', PRIMARY KEY (`ID`), KEY `post_name` (`post_name`), KEY `type_status_date` (`post_type`,`post_status`,`post_date`,`ID`), KEY `post_parent` (`post_parent`), KEY `post_author` (`post_author`) ) ENGINE=InnoDB AUTO_INCREMENT=55004 DEFAULT CHARSET=utf8",
                true);
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE `wp_term_relationships` ( `object_id` bigint(20) unsigned NOT NULL DEFAULT '0', `term_taxonomy_id` bigint(20) unsigned NOT NULL DEFAULT '0', `term_order` int(11) NOT NULL DEFAULT '0', PRIMARY KEY (`object_id`,`term_taxonomy_id`), KEY `term_taxonomy_id` (`term_taxonomy_id`) ) ENGINE=InnoDB DEFAULT CHARSET=utf8",
                true);
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE `wp_term_taxonomy` ( `term_taxonomy_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT, `term_id` bigint(20) unsigned NOT NULL DEFAULT '0', `taxonomy` varchar(32) NOT NULL DEFAULT '', `description` longtext NOT NULL, `parent` bigint(20) unsigned NOT NULL DEFAULT '0', `count` bigint(20) NOT NULL DEFAULT '0', PRIMARY KEY (`term_taxonomy_id`), UNIQUE KEY `term_id_taxonomy` (`term_id`,`taxonomy`), KEY `taxonomy` (`taxonomy`) ) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8",
                true);
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE `wp_terms` ( `term_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT, `name` varchar(200) NOT NULL DEFAULT '', `slug` varchar(200) NOT NULL DEFAULT '', `term_group` bigint(10) NOT NULL DEFAULT '0', PRIMARY KEY (`term_id`), UNIQUE KEY `slug` (`slug`), KEY `name` (`name`) ) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8",
                true);
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE `wp_usermeta` ( `umeta_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT, `user_id` bigint(20) unsigned NOT NULL DEFAULT '0', `meta_key` varchar(255) DEFAULT NULL, `meta_value` longtext, PRIMARY KEY (`umeta_id`), KEY `user_id` (`user_id`), KEY `meta_key` (`meta_key`) ) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8",
                true);
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE `wp_users` ( `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT, `user_login` varchar(60) NOT NULL DEFAULT '', `user_pass` varchar(64) NOT NULL DEFAULT '', `user_nicename` varchar(50) NOT NULL DEFAULT '', `user_email` varchar(100) NOT NULL DEFAULT '', `user_url` varchar(100) NOT NULL DEFAULT '', `user_registered` datetime NOT NULL DEFAULT '0000-00-00 00:00:00', `user_activation_key` varchar(60) NOT NULL DEFAULT '', `user_status` int(11) NOT NULL DEFAULT '0', `display_name` varchar(250) NOT NULL DEFAULT '', PRIMARY KEY (`ID`), KEY `user_login_key` (`user_login`), KEY `user_nicename` (`user_nicename`) ) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8",
                true);
    }

    @Test
    public void testCreateTableArrays() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE sal_emp (name text, pay_by_quarter integer[], schedule text[][])");
    }

    @Test
    public void testCreateTableArrays2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE sal_emp (name text, pay_by_quarter integer[5], schedule text[3][2])");
    }

    @Test
    public void testCreateTableColumnValues() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE mytable1 (values INTEGER)");
    }

    @Test
    public void testCreateTableColumnValue() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE mytable1 (value INTEGER)");
    }

    @Test
    public void testCreateTableForeignKey5() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE IF NOT EXISTS table1 (id INTEGER PRIMARY KEY AUTO_INCREMENT, aid INTEGER REFERENCES accounts ON aid ON DELETE CASCADE, name STRING, lastname STRING)");
    }

    @Test
    public void testCreateTableForeignKey6() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE test (id long, fkey long references another_table (id))");
    }

    @Test
    public void testMySqlCreateTableOnUpdateCurrentTimestamp() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE test (applied timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)");
    }

    @Test
    public void testMySqlCreateTableWithConstraintWithCascade() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE table1 (id INT (10) UNSIGNED NOT NULL AUTO_INCREMENT, t2_id INT (10) UNSIGNED DEFAULT NULL, t3_id INT (10) UNSIGNED DEFAULT NULL, t4_id INT (10) UNSIGNED NOT NULL, PRIMARY KEY (id), KEY fkc_table1_t4 (t4_id), KEY fkc_table1_t2 (t2_id), KEY fkc_table1_t3 (t3_id), CONSTRAINT fkc_table1_t2 FOREIGN KEY (t2_id) REFERENCES table_two(t2o_id) ON DELETE CASCADE ON UPDATE CASCADE, CONSTRAINT fkc_table1_t3 FOREIGN KEY (t3_id) REFERENCES table_three(t3o_id) ON UPDATE CASCADE, CONSTRAINT fkc_table1_t4 FOREIGN KEY (t4_id) REFERENCES table_four(t4o_id) ON DELETE CASCADE) ENGINE = InnoDB AUTO_INCREMENT = 8761 DEFAULT CHARSET = utf8");
    }

    @Test
    public void testMySqlCreateTableWithConstraintWithNoAction() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE table1 (id INT (10) UNSIGNED NOT NULL AUTO_INCREMENT, t2_id INT (10) UNSIGNED DEFAULT NULL, t3_id INT (10) UNSIGNED DEFAULT NULL, t4_id INT (10) UNSIGNED NOT NULL, PRIMARY KEY (id), KEY fkc_table1_t4 (t4_id), KEY fkc_table1_t2 (t2_id), KEY fkc_table1_t3 (t3_id), CONSTRAINT fkc_table1_t2 FOREIGN KEY (t2_id) REFERENCES table_two(t2o_id) ON DELETE NO ACTION ON UPDATE NO ACTION, CONSTRAINT fkc_table1_t3 FOREIGN KEY (t3_id) REFERENCES table_three(t3o_id) ON UPDATE NO ACTION, CONSTRAINT fkc_table1_t4 FOREIGN KEY (t4_id) REFERENCES table_four(t4o_id) ON DELETE NO ACTION) ENGINE = InnoDB AUTO_INCREMENT = 8761 DEFAULT CHARSET = utf8");
    }

    @Test
    public void testMySqlCreateTableWithTextIndexes() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE table2 (id INT (10) UNSIGNED NOT NULL AUTO_INCREMENT, name TEXT, url TEXT, created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, PRIMARY KEY (id), FULLTEXT KEY idx_table2_name (name)) ENGINE = InnoDB AUTO_INCREMENT = 7334 DEFAULT CHARSET = utf8");
    }

    @Test
    public void testMySqlCreateTableWithSpatialIndex() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE places (id INT NOT NULL, location GEOMETRY NOT NULL, SPATIAL KEY sp_idx_location (location))");
    }

    @Test
    public void testMySqlCreateTableIssue2367()
            throws JSQLParserException {
        String sql = "CREATE TABLE test (\n"
                + "id int(11) NOT NULL COMMENT 'data id',\n"
                + "code varchar(100) NOT NULL COMMENT 'code',\n"
                + "name varchar(300) DEFAULT NULL COMMENT 'name',\n"
                + "geo geometry NOT NULL,\n"
                + "PRIMARY KEY (id),\n"
                + "UNIQUE KEY index_code (code) USING HASH COMMENT 'unique index on code',\n"
                + "UNIQUE KEY inx_code_name (code,name) USING BTREE COMMENT 'unique index on code and name',\n"
                + "UNIQUE KEY inx_id_code_name (id,code,name) USING BTREE COMMENT 'index 1',\n"
                + "SPATIAL KEY SPATIAL_geo (geo),\n"
                + "KEY NORMAL_name (name) COMMENT 'normal index',\n"
                + "FULLTEXT KEY fulltext_name (name)\n"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='test table'";
        assertSqlCanBeParsedAndDeparsed(sql);
    }

    @Test
    public void testCreateTableWithCheck() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE table2 (id INT (10) NOT NULL, name TEXT, url TEXT, CONSTRAINT name_not_empty CHECK (name <> ''))");
    }

    @Test
    public void testCreateTableWithCheckNotNull() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE table2 (id INT (10) NOT NULL, name TEXT, url TEXT, CONSTRAINT name_not_null CHECK (name IS NOT NULL))");
    }

    @Test
    public void testCreateTableIssue270() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE item (i_item_sk integer NOT NULL, i_item_id character (16) NOT NULL, i_rec_start_date date, i_rec_end_date date, i_item_desc character varying(200), i_current_price numeric(7,2), i_wholesale_cost numeric(7,2), i_brand_id integer, i_brand character(50), i_class_id integer, i_class character(50), i_category_id integer, i_category character(50), i_manufact_id integer, i_manufact character(50), i_size character(20), i_formulation character(20), i_color character(20), i_units character(10), i_container character(10), i_manager_id integer, i_product_name character(50) )",
                true);
    }

    @Test
    public void testCreateTableIssue270_1() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE item (i_item_sk integer NOT NULL, i_item_id character (16))");
    }

    @Test
    public void testCreateTempTableIssue293() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE GLOBAL TEMPORARY TABLE T1 (PROCESSID VARCHAR (32))");
    }

    @Test
    public void testCreateTableWithTablespaceIssue247() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE TABLE1 (COLUMN1 VARCHAR2 (15), COLUMN2 VARCHAR2 (15), CONSTRAINT P_PK PRIMARY KEY (COLUMN1) USING INDEX TABLESPACE \"T_INDEX\") TABLESPACE \"T_SPACE\"");
    }

    @Test
    public void testCreateTableWithTablespaceIssue247_1() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE TABLE1 (COLUMN1 VARCHAR2 (15), COLUMN2 VARCHAR2 (15), CONSTRAINT P_PK PRIMARY KEY (COLUMN1) USING INDEX TABLESPACE \"T_INDEX\")");
    }

    @Test
    public void testOnDeleteSetNull() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE inventory (inventory_id INT PRIMARY KEY, product_id INT, CONSTRAINT fk_inv_product_id FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE SET NULL)");
    }

    @Test
    public void testColumnCheck() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE table1 (col1 INTEGER CHECK (col1 > 100))");
    }

    @Test
    public void testTableReferenceWithSchema() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE table1 (col1 INTEGER REFERENCES schema1.table1)");
    }

    @Test
    public void testNamedColumnConstraint() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE foo (col1 integer CONSTRAINT no_null NOT NULL)");
    }

    @Test
    public void testColumnConstraintWith() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE foo (col1 integer) WITH (fillfactor=70)");
    }

    @Test
    public void testExcludeWhereConstraint() throws JSQLParserException {
        String statement = "CREATE TABLE foo (col1 integer, EXCLUDE WHERE (col1 > 100))";
        assertSqlCanBeParsedAndDeparsed(statement);
        assertDeparse(
                new CreateTable()
                        .withTable(new Table("foo"))
                        .addIndexes(
                                new ExcludeConstraint()
                                        .withExpression(
                                                new GreaterThan()
                                                        .withLeftExpression(new Column("col1"))
                                                        .withRightExpression(new LongValue(100))))
                        .addColumnDefinitions(
                                new ColumnDefinition("col1", new ColDataType("integer"))),
                statement);
    }

    @Test
    public void testTimestampWithoutTimezone() throws JSQLParserException {
        String statement = "CREATE TABLE abc.tabc (transaction_date TIMESTAMP WITHOUT TIME ZONE)";
        assertSqlCanBeParsedAndDeparsed(statement);
        assertDeparse(
                new CreateTable()
                        .withTable(new Table(Arrays.asList("abc", "tabc")))
                        .addColumnDefinitions(
                                new ColumnDefinition(
                                        "transaction_date",
                                        new ColDataType("TIMESTAMP WITHOUT TIME ZONE"))),
                statement);
    }

    @Test
    public void testCreateUnitonIssue402() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE temp.abc AS SELECT sku FROM temp.a UNION SELECT sku FROM temp.b");
    }

    @Test
    public void testCreateUnitonIssue402_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE temp.abc AS (SELECT sku FROM temp.a UNION SELECT sku FROM temp.b)");
    }

    @Test
    public void testCreateUnionIssue() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE temp.abc AS (SELECT c FROM t1) UNION (SELECT c FROM t2)");
    }

    @Test
    public void testTimestampWithTimezone() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE country_region ("
                        + "regionid BIGINT NOT NULL CONSTRAINT pk_auth_region PRIMARY KEY, "
                        + "region_name VARCHAR (100) NOT NULL, "
                        + "creation_date TIMESTAMP (0) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP (0) NOT NULL, "
                        + "last_change_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP (0), "
                        + "CONSTRAINT region_name_unique UNIQUE (region_name))");
    }

    @Test
    public void testCreateTableAsSelect3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE public.sales1 AS (SELECT * FROM public.sales)");
    }

    @Test
    public void testQuotedPKColumnsIssue491() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE `FOO` (`ID` INT64, `NAME` STRING (100)) PRIMARY KEY (`ID`)");
    }

    @Test
    public void testQuotedPKColumnsIssue491_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE `FOO` (`ID` INT64, `NAME` STRING (100), PRIMARY KEY (`ID`))");
    }

    @Test
    public void testKeySyntaxWithLengthColumnParameter() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE basic (BASIC_TITLE varchar (255) NOT NULL, KEY BASIC_TITLE (BASIC_TITLE (255)))");
    }

    @Test
    public void testIssue273Varchar2Byte() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE IF NOT EXISTS \"TABLE_OK\" (\"SOME_FIELD\" VARCHAR2 (256 BYTE))");
    }

    @Test
    public void testIssue273Varchar2Char() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE IF NOT EXISTS \"TABLE_OK\" (\"SOME_FIELD\" VARCHAR2 (256 CHAR))");
    }

    @Test
    public void testIssue661Partition() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE T_TEST_PARTITION (PART_COLUMN VARCHAR2 (32) NOT NULL, OTHER_COLS VARCHAR2 (10) NOT NULL) TABLESPACE TBS_DATA_01 PARTITION BY HASH (PART_COLUMN) PARTITIONS 4 STORE IN (TBS_DATA_01) COMPRESS");
    }

    @Test
    public void testIssue770Using() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE `department_region` (`ID` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键', `DEPARTMENT_ID` int(10) unsigned NOT NULL COMMENT '部门ID', PRIMARY KEY (`ID`) KEY `DISTRICT_CODE` (`DISTRICT_CODE`)  USING BTREE) ENGINE=InnoDB AUTO_INCREMENT=420 DEFAULT CHARSET=utf8",
                true);
    }

    @Test
    public void testRUBiSCreateList() throws Exception {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        CreateTableTest.class.getResourceAsStream("/RUBiS-create-requests.txt")));

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
                    CreateTable createTable =
                            (CreateTable) parserManager.parse(new StringReader(query));
                    String[] colsList = null;
                    if ("null".equals(cols)) {
                        colsList = new String[0];
                    } else {
                        StringTokenizer tokenizer = new StringTokenizer(cols, " ");

                        List<String> colsListList = new ArrayList<>();
                        while (tokenizer.hasMoreTokens()) {
                            colsListList.add(tokenizer.nextToken());
                        }

                        colsList = colsListList.toArray(new String[colsListList.size()]);
                    }
                    List<String> colsFound = new ArrayList<>();
                    if (createTable.getColumnDefinitions() != null) {
                        for (ColumnDefinition columnDefinition : createTable
                                .getColumnDefinitions()) {
                            String colName = columnDefinition.getColumnName();
                            boolean unique = false;
                            if (createTable.getIndexes() != null) {
                                for (Index index : createTable.getIndexes()) {
                                    if (index.getType().equals("PRIMARY KEY")
                                            && index.getColumnsNames().size() == 1
                                            && index.getColumnsNames().get(0).equals(colName)) {
                                        unique = true;
                                    }
                                }
                            }

                            if (!unique) {
                                if (columnDefinition.getColumnSpecs() != null) {
                                    for (Iterator<String> iterator =
                                            columnDefinition.getColumnSpecs().iterator(); iterator
                                                    .hasNext();) {
                                        String par = iterator.next();
                                        if (par.equals("UNIQUE")) {
                                            unique = true;
                                        } else if (par.equals("PRIMARY")
                                                && iterator.hasNext()
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

                    assertEquals(colsList.length, colsFound.size(), "stm:" + query);

                    for (int i = 0; i < colsList.length; i++) {
                        assertEquals(colsList[i], colsFound.get(i), "stm:" + query);
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
                if (line.length() != 0
                        && (line.length() < 2
                                || line.length() >= 2
                                        && !(line.charAt(0) == '/' && line.charAt(1) == '/'))) {
                    break;
                }
            } else {
                break;
            }
        }

        return line;
    }

    @Test
    public void testCollateUtf8Issue785() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE DEMO_SQL (SHARE_PWD varchar (128) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT 'COMMENT') ENGINE = InnoDB AUTO_INCREMENT = 34 DEFAULT CHARSET = utf8 COLLATE = utf8_bin COMMENT = 'COMMENT'");
    }

    @Test
    public void testCreateTableWithSetTypeIssue796() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE `tables_priv` (`Host` char (60) COLLATE utf8_bin NOT NULL DEFAULT '', `Table_priv` set ('Select', 'Insert', 'Update', 'Delete', 'Create', 'Drop', 'Grant', 'References', 'Index', 'Alter', 'Create View', 'Show view', 'Trigger') CHARACTER SET utf8 NOT NULL DEFAULT '') ENGINE = MyISAM DEFAULT CHARSET = utf8 COLLATE = utf8_bin COMMENT = 'Table privileges'");
    }

    @Test
    public void testCreateTableIssue798() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE `comment` (`text_hash` varchar (32) COLLATE utf8_bin)");
    }

    @Test
    public void testCreateTableIssue798_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE parent (\n"
                        + "PARENT_ID int(11) NOT NULL AUTO_INCREMENT,\n"
                        + "PCN varchar(100) NOT NULL,\n"
                        + "IS_DELETED char(1) NOT NULL,\n"
                        + "STRUCTURE_ID int(11) NOT NULL,\n"
                        + "DIRTY_STATUS char(1) NOT NULL,\n"
                        + "BIOLOGICAL char(1) NOT NULL,\n"
                        + "STRUCTURE_TYPE int(11) NOT NULL,\n"
                        + "CST_ORIGINAL varchar(1000) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,\n"
                        + "MWT decimal(14,6) DEFAULT NULL,\n"
                        + "RESTRICTED int(11) NOT NULL,\n"
                        + "INIT_DATE datetime DEFAULT NULL,\n"
                        + "MOD_DATE datetime DEFAULT NULL,\n"
                        + "CREATED_BY varchar(255) NOT NULL,\n"
                        + "MODIFIED_BY varchar(255) NOT NULL,\n"
                        + "CHEMIST_ID varchar(255) NOT NULL,\n"
                        + "UNKNOWN_ID int(11) DEFAULT NULL,\n"
                        + "STEREOCHEMISTRY varchar(256) DEFAULT NULL,\n"
                        + "GEOMETRIC_ISOMERISM varchar(256) DEFAULT NULL,\n"
                        + "PRIMARY KEY (PARENT_ID),\n"
                        + "UNIQUE KEY PARENT_PCN_IDX (PCN),\n"
                        + "KEY PARENT_SID_IDX (STRUCTURE_ID),\n"
                        + "KEY PARENT_DIRTY_IDX (DIRTY_STATUS)\n"
                        + ") ENGINE=InnoDB AUTO_INCREMENT=2663 DEFAULT CHARSET=utf8",
                true);
    }

    @Test
    public void testCreateTableIssue113() throws JSQLParserException {
        String statement =
                "CREATE TABLE foo (reason character varying (255) DEFAULT 'Test' :: character varying NOT NULL)";
        assertSqlCanBeParsedAndDeparsed(statement);
        assertDeparse(
                new CreateTable()
                        .withTable(new Table().withName("foo"))
                        .withColumnDefinitions(
                                Arrays.asList(
                                        new ColumnDefinition()
                                                .withColumnName("reason")
                                                .withColDataType(
                                                        new ColDataType()
                                                                .withDataType("character varying")
                                                                .addArgumentsStringList(
                                                                        Arrays.asList("255")))
                                                .addColumnSpecs(
                                                        "DEFAULT 'Test' :: character varying",
                                                        "NOT NULL"))),
                statement);
    }

    @Test
    public void testCreateTableIssue830() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE testyesr (id int, yy year)");
    }

    @Test
    public void testCreateTableIssue830_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE testyesr (id int, yy year, mm month, dd day)");
    }

    @Test
    public void testSettingCharacterSetIssue829() throws JSQLParserException {
        String sql =
                "CREATE TABLE test (id int (11) NOT NULL, name varchar (64) CHARACTER SET GBK NOT NULL, age int (11) NOT NULL, score decimal (8, 2) DEFAULT NULL, description varchar (64) DEFAULT NULL, creationDate datetime DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (id)) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4";
        assertSqlCanBeParsedAndDeparsed(sql);
        CreateTable stmt = (CreateTable) CCJSqlParserUtil.parse(sql);

        ColumnDefinition colName = stmt.getColumnDefinitions().stream()
                .filter(col -> col.getColumnName().equals("name"))
                .findFirst()
                .orElse(null);

        assertNotNull(colName);

        assertEquals("GBK", colName.getColDataType().getCharacterSet());
    }

    @Test
    public void testCreateTableIssue924() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE test_descending_indexes (c1 INT, c2 INT, INDEX idx1 (c1 ASC, c2 DESC))");
    }

    @Test
    public void testCreateTableIssue924_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE test_descending_indexes (c1 INT, c2 INT, INDEX idx1 (c1 ASC, c2 ASC), INDEX idx2 (c1 ASC, c2 DESC), INDEX idx3 (c1 DESC, c2 ASC), INDEX idx4 (c1 DESC, c2 DESC))");
    }

    @Test
    public void testCreateTableIssue921() throws JSQLParserException {
        String statement = "CREATE TABLE binary_test (c1 binary (10))";
        assertSqlCanBeParsedAndDeparsed(statement);
        assertDeparse(
                new CreateTable()
                        .withTable(new Table().withName("binary_test"))
                        .addColumnDefinitions(
                                new ColumnDefinition(
                                        "c1",
                                        new ColDataType().withDataType("binary")
                                                .addArgumentsStringList("10"),
                                        null)),
                statement);
    }

    @Test
    public void testCreateTableWithComments() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE IF NOT EXISTS `eai_applications`(\n"
                        + "  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'comment',\n"
                        + "  `name` varchar(64) NOT NULL COMMENT 'comment',\n"
                        + "  `logo` varchar(128) DEFAULT NULL COMMENT 'comment',\n"
                        + "  `description` varchar(128) DEFAULT NULL COMMENT 'comment',\n"
                        + "  `type` int(11) NOT NULL COMMENT 'comment',\n"
                        + "  `status` tinyint(2) NOT NULL COMMENT 'comment',\n"
                        + "  `creator_id` bigint(20) NOT NULL COMMENT 'comment',\n"
                        + "  `created_at` datetime NOT NULL COMMENT 'comment',\n"
                        + "  `updated_at` datetime NOT NULL COMMENT 'comment',\n"
                        + "  PRIMARY KEY (`id`)\n"
                        + ") COMMENT='comment'",
                true);
    }

    @Test
    public void testCreateTableWithCommentIssue922() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE index_with_comment_test (\n"
                        + "id int(11) NOT NULL,\n"
                        + "name varchar(60) DEFAULT NULL,\n"
                        + "KEY name_ind (name) COMMENT 'comment for the name index'\n"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8",
                true);
    }

    @Test
    public void testEnableRowMovementOption() throws JSQLParserException {
        String sql = "CREATE TABLE test (startdate DATE) ENABLE ROW MOVEMENT";

        CreateTable createTable = (CreateTable) CCJSqlParserUtil.parse(sql);
        Assertions.assertThat(createTable.getRowMovement()).isNotNull();
        Assertions.assertThat(createTable.getRowMovement().getMode())
                .isEqualTo(RowMovementMode.ENABLE);

        assertSqlCanBeParsedAndDeparsed(sql);
    }

    @Test
    public void testDisableRowMovementOption() throws JSQLParserException {
        String sql = "CREATE TABLE test (startdate DATE) DISABLE ROW MOVEMENT";

        CreateTable createTable = (CreateTable) CCJSqlParserUtil.parse(sql);
        Assertions.assertThat(createTable.getRowMovement()).isNotNull();
        Assertions.assertThat(createTable.getRowMovement().getMode())
                .isEqualTo(RowMovementMode.DISABLE);

        assertSqlCanBeParsedAndDeparsed(sql);
    }

    @Test
    public void tableMovementWithAS() throws JSQLParserException {
        String sql =
                "CREATE TABLE test (startdate DATE) DISABLE ROW MOVEMENT AS SELECT 1 FROM dual";
        assertSqlCanBeParsedAndDeparsed(sql);
    }

    @Test
    public void testCreateTableWithCommentIssue413() throws JSQLParserException {
        String statement = "CREATE TABLE a LIKE b";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testCreateTableWithCommentIssue413_2() throws JSQLParserException {
        String statement = "CREATE TABLE a LIKE (b)";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testCreateTableWithParameterDefaultFalseIssue1089() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "create table ADDRESS_TYPE ( address_type CHAR(1) not null, at_name VARCHAR(250) not null, is_disabled BOOL not null default FALSE, constraint PK_ADDRESS_TYPE primary key (address_type) )",
                true);
    }

    @Test
    public void testDefaultArray() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE t (f1 text[] DEFAULT ARRAY[] :: text[] NOT NULL, f2 int[] DEFAULT ARRAY[1, 2])");
    }

    @Test
    public void testCreateTemporaryTableAsSelect() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TEMPORARY TABLE T1 (C1, C2) AS SELECT C3, C4 FROM T2");
    }

    @Test
    public void testCreateTempTableAsSelect() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TEMP TABLE T1 (C1, C2) AS SELECT C3, C4 FROM T2");
    }

    @Test
    public void testCreateTableIssue1230() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE TABLE_HISTORY (ID bigint generated by default as identity, CREATED_AT timestamp not null, TEXT varchar (255), primary key (ID))");
    }

    @Test
    public void testCreateUnionIssue1309() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE temp.abc AS (SELECT c FROM t1) UNION (SELECT c FROM t2)");
    }

    @Test
    public void testCreateTableBinaryIssue1518() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE `s` (`a` enum ('a', 'b', 'c') CHARACTER SET binary COLLATE binary)");
    }

    @Test
    public void testCreateTableIssue1488() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE u_call_record (\n"
                + "card_user_id int(11) NOT NULL,\n"
                + "device_id int(11) NOT NULL,\n"
                + "call_start_at int(11) NOT NULL DEFAULT CURRENT_TIMESTAMP(11),\n"
                + "card_user_name varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,\n"
                + "sim_id varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,\n"
                + "called_number varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,\n"
                + "called_nickname varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,\n"
                + "talk_time smallint(8) NULL DEFAULT NULL,\n"
                + "area_name varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,\n"
                + "area_service_id int(11) NULL DEFAULT NULL,\n"
                + "operator_id int(4) NULL DEFAULT NULL,\n"
                + "status tinyint(4) NULL DEFAULT NULL,\n"
                + "create_at timestamp NULL DEFAULT NULL,\n"
                + "place_user varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,\n"
                + "PRIMARY KEY (card_user_id, device_id, call_start_at) USING BTREE,\n"
                + "INDEX ucr_index_area_name(area_name) USING BTREE,\n"
                + "INDEX ucr_index_area_service_id(area_service_id) USING BTREE,\n"
                + "INDEX ucr_index_called_number(called_number) USING BTREE,\n"
                + "INDEX ucr_index_create_at(create_at) USING BTREE,\n"
                + "INDEX ucr_index_operator_id(operator_id) USING BTREE,\n"
                + "INDEX ucr_index_place_user(place_user) USING BTREE,\n"
                + "INDEX ucr_index_sim_id(sim_id) USING BTREE,\n"
                + "INDEX ucr_index_status(status) USING BTREE,\n"
                + "INDEX ucr_index_talk_time(talk_time) USING BTREE\n"
                + ") ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic",
                true);
    }

    @Test
    public void testCreateTableBinaryIssue1596() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE student2 ("
                + "id int (10) NOT NULL COMMENT 'ID', "
                + "name varchar (20) COLLATE utf8mb4_bin DEFAULT NULL, "
                + "birth year (4) DEFAULT NULL, "
                + "department varchar (20) COLLATE utf8mb4_bin DEFAULT NULL, "
                + "address varchar (50) COLLATE utf8mb4_bin DEFAULT NULL, "
                + "PRIMARY KEY (id), "
                + "UNIQUE KEY id (id), "
                + "INDEX name (name) USING BTREE"
                + ") ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_bin");
    }

    @Test
    public void testCreateTableSpanner() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE COMMAND (\n" +
                        "   DATASET_ID      INT64       NOT NULL,\n" +
                        "   COMMAND_ID      STRING(MAX) NOT NULL,\n" +
                        "   VAL_BOOL        BOOL,\n" +
                        "   VAL_BYTES       BYTES(1024),\n" +
                        "   VAL_DATE        DATE,\n" +
                        "   VAL_TIMESTAMP   TIMESTAMP,\n" +
                        "   VAL_COMMIT_TIMESTAMP   TIMESTAMP NOT NULL OPTIONS (allow_commit_timestamp = true),\n"
                        +
                        "   VAL_FLOAT64     FLOAT64,\n" +
                        "   VAL_JSON        JSON(2048),\n" +
                        "   VAL_NUMERIC     NUMERIC,\n" +
                        "   VAL_STRING      STRING(MAX),\n" +
                        "   VAL_TIMESTAMP   TIMESTAMP,\n" +
                        "   ARR_BOOL        ARRAY<BOOL>,\n" +
                        "   ARR_BYTES       ARRAY<BYTES(1024)>,\n" +
                        "   ARR_DATE        ARRAY<DATE>,\n" +
                        "   ARR_TIMESTAMP   ARRAY<TIMESTAMP>,\n" +
                        "   ARR_FLOAT64     ARRAY<FLOAT64>,\n" +
                        "   ARR_JSON        ARRAY<JSON(2048)>,\n" +
                        "   ARR_NUMERIC     ARRAY<NUMERIC>,\n" +
                        "   ARR_STRING      ARRAY<STRING(MAX)>,\n" +
                        "   ARR_TIMESTAMP   ARRAY<TIMESTAMP>,\n" +
                        "   PAYLOAD         STRING(MAX),\n" +
                        "   AUTHOR          STRING(MAX) NOT NULL,\n" +
                        "   SEARCH          STRING(MAX) AS (UPPER(AUTHOR)) STORED\n" +
                        " ) PRIMARY KEY ( DATASET_ID, COMMAND_ID )\n" +
                        ",   INTERLEAVE IN PARENT DATASET ON DELETE CASCADE",
                true);
    }


    @Test
    void testCreateTableWithStartWithNumber() throws JSQLParserException {
        String sqlStr =
                "CREATE TABLE locations\n"
                        + "  (\n"
                        + "    location_id NUMBER GENERATED BY DEFAULT AS IDENTITY START WITH 24 \n"
                        + "                PRIMARY KEY       ,\n"
                        + "    address     VARCHAR2( 255 ) NOT NULL,\n"
                        + "    postal_code VARCHAR2( 20 )          ,\n"
                        + "    city        VARCHAR2( 50 )          ,\n"
                        + "    state       VARCHAR2( 50 )          ,\n"
                        + "    country_id  CHAR( 2 )               , -- fk\n"
                        + "    CONSTRAINT fk_locations_countries \n"
                        + "      FOREIGN KEY( country_id )\n"
                        + "      REFERENCES countries( country_id ) \n"
                        + "      ON DELETE CASCADE\n"
                        + "  )";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testCreateTableWithNextValueFor() throws JSQLParserException {
        String sqlStr =
                "CREATE TABLE public.actor (\n"
                        + "    actor_id integer DEFAULT nextval('public.actor_actor_id_seq'::regclass) NOT NULL\n"
                        + ")";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr =
                " CREATE TABLE myschema.tableName (\n"
                        + "                id bigint NOT NULL DEFAULT nextval('myschema.mysequence'::regclass), \n"
                        + "                bool_col boolean NOT NULL DEFAULT false, \n"
                        + "                int_col integer NOT NULL DEFAULT 0)";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr =
                " CREATE TABLE t1 (\n"
                        + "  -- literal defaults\n"
                        + "  i INT         DEFAULT 0,\n"
                        + "  c VARCHAR(10) DEFAULT '',\n"
                        + "  -- expression defaults\n"
                        + "  f FLOAT       DEFAULT (RAND() * RAND()),\n"
                        + "  b BINARY(16)  DEFAULT (UUID_TO_BIN(UUID())),\n"
                        + "  d DATE        DEFAULT (CURRENT_DATE + INTERVAL 1 YEAR),\n"
                        + "  p POINT       DEFAULT (Point(0,0)),\n"
                        + "  j JSON        DEFAULT (JSON_ARRAY())\n"
                        + ")";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr =
                " CREATE TABLE pagila_dev.actor (\n"
                        + "actor_id integer DEFAULT nextval('pagila_dev.actor_actor_id_seq'::regclass) NOT NULL,\n"
                        + "first_name text NOT NULL,\n"
                        + "last_name text NOT NULL,\n"
                        + "last_update timestamp with time zone DEFAULT now() NOT NULL\n"
                        + ")";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr =
                "CREATE TABLE \"public\".\"device_bayonet_copy1\" ( "
                        + "\"id\" int8 NOT NULL"
                        + ", \"device_code\" varchar(128) COLLATE \"pg_catalog\".\"default\""
                        + ", \"longitude_latitude\" varchar(128) COLLATE \"pg_catalog\".\"default\""
                        + ", \"longitude_latitude_gis\" \"public\".\"geometry\""
                        + ", \"direction\" varchar(128) COLLATE \"pg_catalog\".\"default\""
                        + ", \"brand\" varchar(128) COLLATE \"pg_catalog\".\"default\""
                        + ", \"test\" \"information_schema\".\"time_stamp\""
                        + ", CONSTRAINT \"device_bayonet_copy1_pkey\" PRIMARY KEY (\"id\") "
                        + ")";

        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testIssue1858() throws JSQLParserException {
        String sqlStr = "CREATE TABLE \"foo\"\n"
                + "(\n"
                + "    event_sk               bigint identity             NOT NULL encode RAW\n"
                + ") compound sortkey (      date_key      )";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testIssue1864() throws JSQLParserException {
        String sqlStr = "ALTER TABLE `test`.`test_table` "
                + "MODIFY COLUMN `test` varchar(251) "
                + " CHARACTER SET armscii8 COLLATE armscii8_bin NULL DEFAULT NULL FIRST";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testUniqueAfterForeignKeyIssue2082() throws JSQLParserException {
        String sqlStr =
                "CREATE TABLE employees (\n" +
                        "employee_number    int         NOT NULL\n" +
                        ", employee_name    char (50)   NOT NULL\n" +
                        ", department_id    int\n" +
                        ", salary           int\n" +
                        ", PRIMARY KEY (employee_number)\n" +
                        ", FOREIGN KEY (department_id) REFERENCES departments(id)\n" +
                        ", UNIQUE (employee_name));";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testWithCatalog() throws JSQLParserException {
        String sqlStr = "CREATE TABLE UNNAMED.session1.a (b VARCHAR (1))";
        CreateTable st = (CreateTable) assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Table t = st.getTable();
        assertEquals("UNNAMED", t.getCatalogName());
        assertEquals("session1", t.getSchemaName());
        assertEquals("a", t.getUnquotedName());
    }
}

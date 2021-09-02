/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import static net.sf.jsqlparser.test.TestUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.ReferentialAction;
import net.sf.jsqlparser.statement.ReferentialAction.Action;
import net.sf.jsqlparser.statement.ReferentialAction.Type;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.AlterExpression.ColumnDataType;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.CheckConstraint;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.create.table.NamedConstraint;
import net.sf.jsqlparser.statement.create.table.Index.ColumnParams;
import net.sf.jsqlparser.statement.create.table.ForeignKeyIndex;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import org.junit.Test;

public class AlterTest {

    @Test
    public void testAlterTableAddColumn() throws JSQLParserException {
        Statement stmt = CCJSqlParserUtil.
                parse("ALTER TABLE mytable ADD COLUMN mycolumn varchar (255)");
        assertTrue(stmt instanceof Alter);
        Alter alter = (Alter) stmt;
        assertEquals("mytable", alter.getTable().getFullyQualifiedName());
        AlterExpression alterExp = alter.getAlterExpressions().get(0);
        assertNotNull(alterExp);
        List<ColumnDataType> colDataTypes = alterExp.getColDataTypeList();
        assertEquals("mycolumn", colDataTypes.get(0).getColumnName());
        assertEquals("varchar (255)", colDataTypes.get(0).getColDataType().toString());
    }

    @Test
    public void testAlterTableAddColumn_ColumnKeyWordImplicit() throws JSQLParserException {
        Statement stmt = CCJSqlParserUtil.parse("ALTER TABLE mytable ADD mycolumn varchar (255)");
        assertTrue(stmt instanceof Alter);
        Alter alter = (Alter) stmt;
        assertEquals("mytable", alter.getTable().getFullyQualifiedName());
        AlterExpression alterExp = alter.getAlterExpressions().get(0);
        assertNotNull(alterExp);
        List<ColumnDataType> colDataTypes = alterExp.getColDataTypeList();
        assertEquals("mycolumn", colDataTypes.get(0).getColumnName());
        assertEquals("varchar (255)", colDataTypes.get(0).getColDataType().toString());
    }

    @Test
    public void testAlterTablePrimaryKey() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE animals ADD PRIMARY KEY (id)");
    }

    @Test
    public void testAlterTablePrimaryKeyDeferrable() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE animals ADD PRIMARY KEY (id) DEFERRABLE");
    }

    @Test
    public void testAlterTablePrimaryKeyNotDeferrable() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE animals ADD PRIMARY KEY (id) NOT DEFERRABLE");
    }

    @Test
    public void testAlterTablePrimaryKeyValidate() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE animals ADD PRIMARY KEY (id) VALIDATE");
    }

    @Test
    public void testAlterTablePrimaryKeyNoValidate() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE animals ADD PRIMARY KEY (id) NOVALIDATE");
    }

    @Test
    public void testAlterTablePrimaryKeyDeferrableValidate() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE animals ADD PRIMARY KEY (id) DEFERRABLE VALIDATE");
    }

    @Test
    public void testAlterTablePrimaryKeyDeferrableDisableNoValidate() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE animals ADD PRIMARY KEY (id) DEFERRABLE DISABLE NOVALIDATE");
    }

    @Test
    public void testAlterTableUniqueKey() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE `schema_migrations` ADD UNIQUE KEY `unique_schema_migrations` (`version`)");
    }

    @Test
    public void testAlterTableForgeignKey() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE test ADD FOREIGN KEY (user_id) REFERENCES ra_user (id) ON DELETE CASCADE");
    }

    @Test
    public void testAlterTableAddConstraint() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE RESOURCELINKTYPE ADD CONSTRAINT FK_RESOURCELINKTYPE_PARENTTYPE_PRIMARYKEY FOREIGN KEY (PARENTTYPE_PRIMARYKEY) REFERENCES RESOURCETYPE(PRIMARYKEY)");
    }

    @Test
    public void testAlterTableAddConstraintWithConstraintState() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE RESOURCELINKTYPE ADD CONSTRAINT FK_RESOURCELINKTYPE_PARENTTYPE_PRIMARYKEY FOREIGN KEY (PARENTTYPE_PRIMARYKEY) REFERENCES RESOURCETYPE(PRIMARYKEY) DEFERRABLE DISABLE NOVALIDATE");
    }

    @Test
    public void testAlterTableAddConstraintWithConstraintState2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE RESOURCELINKTYPE ADD CONSTRAINT RESOURCELINKTYPE_PRIMARYKEY PRIMARY KEY (PRIMARYKEY) DEFERRABLE NOVALIDATE");
    }

    @Test
    public void testAlterTableAddUniqueConstraint() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE Persons ADD UNIQUE (ID)");
    }

    @Test
    public void testAlterTableForgeignKey2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE test ADD FOREIGN KEY (user_id) REFERENCES ra_user (id)");
    }

    @Test
    public void testAlterTableForgeignKey3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE test ADD FOREIGN KEY (user_id) REFERENCES ra_user (id) ON DELETE RESTRICT");
    }

    @Test
    public void testAlterTableForgeignKey4() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE test ADD FOREIGN KEY (user_id) REFERENCES ra_user (id) ON DELETE SET NULL");
    }
    
    @Test
    public void testAlterTableForgeignWithFkSchema() throws JSQLParserException {
      final String FK_SCHEMA_NAME = "my_schema";
      final String FK_TABLE_NAME= "ra_user";
      String sql = "ALTER TABLE test ADD FOREIGN KEY (user_id) REFERENCES " + FK_SCHEMA_NAME +"." + FK_TABLE_NAME + " (id) ON DELETE SET NULL";
      assertSqlCanBeParsedAndDeparsed(sql);
      
      Alter alter = (Alter) CCJSqlParserUtil.parse(sql);
      AlterExpression alterExpression = alter.getAlterExpressions().get(0);

      assertEquals(alterExpression.getFkSourceSchema(), FK_SCHEMA_NAME);
      assertEquals(alterExpression.getFkSourceTable(), FK_TABLE_NAME);
    }

    @Test
    public void testAlterTableDropColumn() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE test DROP COLUMN YYY");
    }

    @Test
    public void testAlterTableDropColumn2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE mytable DROP COLUMN col1, DROP COLUMN col2");

        Statement stmt = CCJSqlParserUtil.
                parse("ALTER TABLE mytable DROP COLUMN col1, DROP COLUMN col2");
        Alter alter = (Alter) stmt;
        List<AlterExpression> alterExps = alter.getAlterExpressions();
        AlterExpression col1Exp = alterExps.get(0);
        AlterExpression col2Exp = alterExps.get(1);
        assertEquals("col1", col1Exp.getColumnName());
        assertEquals("col2", col2Exp.getColumnName());
    }

    @Test
    public void testAlterTableDropConstraint() throws JSQLParserException {
        final String sql = "ALTER TABLE test DROP CONSTRAINT YYY";
        Statement stmt = CCJSqlParserUtil.parse(sql);
        assertStatementCanBeDeparsedAs(stmt, sql);
        AlterExpression alterExpression = ((Alter) stmt).getAlterExpressions().get(0);
        assertEquals(alterExpression.getConstraintName(), "YYY");
    }

    @Test
    public void testAlterTableDropConstraintIfExists() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE Persons DROP CONSTRAINT IF EXISTS UC_Person");
    }

    @Test
    public void testAlterTablePK() throws JSQLParserException {
        final String sql = "ALTER TABLE `Author` ADD CONSTRAINT `AuthorPK` PRIMARY KEY (`ID`)";
        Statement stmt = CCJSqlParserUtil.parse(sql);
        assertStatementCanBeDeparsedAs(stmt, sql);
        AlterExpression alterExpression = ((Alter) stmt).getAlterExpressions().get(0);
        assertNull(alterExpression.getConstraintName());
        // TODO: should this pass? ==>        assertEquals(alterExpression.getPkColumns().get(0), "ID");
        assertEquals(alterExpression.getIndex().getColumnsNames().get(0), "`ID`");
    }

    @Test
    public void testAlterTableFK() throws JSQLParserException {
        String sql = "ALTER TABLE `Novels` ADD FOREIGN KEY (AuthorID) REFERENCES Author (ID)";
        Statement stmt = CCJSqlParserUtil.parse(sql);
        assertStatementCanBeDeparsedAs(stmt, sql);
        AlterExpression alterExpression = ((Alter) stmt).getAlterExpressions().get(0);
        assertEquals(alterExpression.getFkColumns().size(), 1);
        assertEquals(alterExpression.getFkColumns().get(0), "AuthorID");
        assertEquals(alterExpression.getFkSourceTable(), "Author");
        assertEquals(alterExpression.getFkSourceColumns().size(), 1);
        assertEquals(alterExpression.getFkSourceColumns().get(0), "ID");
    }

    @Test
    public void testAlterTableCheckConstraint() throws JSQLParserException {
        String statement = "ALTER TABLE `Author` ADD CONSTRAINT name_not_empty CHECK (`NAME` <> '')";
        Statement parsed = assertSqlCanBeParsedAndDeparsed(statement);
        Alter created = new Alter().withTable(new Table("`Author`"))
                .addAlterExpressions(Collections.singleton(
                        new AlterExpression().withOperation(AlterOperation.ADD).withIndex(new CheckConstraint()
                                .withName("name_not_empty")
                                .withExpression(new NotEqualsTo().withLeftExpression(new Column("`NAME`"))
                                        .withRightExpression(new StringValue())))));
        assertDeparse(created, statement);
        assertEqualsObjectTree(parsed, created);
    }

    @Test
    public void testAlterTableAddColumn2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE animals ADD (col1 integer, col2 integer)");
    }

    @Test
    public void testAlterTableAddColumn3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE mytable ADD COLUMN mycolumn varchar (255)");
    }

    @Test
    public void testAlterTableAddColumn4() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE mytable ADD COLUMN col1 varchar (255), ADD COLUMN col2 integer");

        Statement stmt = CCJSqlParserUtil.
                parse("ALTER TABLE mytable ADD COLUMN col1 varchar (255), ADD COLUMN col2 integer");
        Alter alter = (Alter) stmt;
        List<AlterExpression> alterExps = alter.getAlterExpressions();
        AlterExpression col1Exp = alterExps.get(0);
        AlterExpression col2Exp = alterExps.get(1);
        List<ColumnDataType> col1DataTypes = col1Exp.getColDataTypeList();
        List<ColumnDataType> col2DataTypes = col2Exp.getColDataTypeList();
        assertEquals("col1", col1DataTypes.get(0).getColumnName());
        assertEquals("col2", col2DataTypes.get(0).getColumnName());
        assertEquals("varchar (255)", col1DataTypes.get(0).getColDataType().toString());
        assertEquals("integer", col2DataTypes.get(0).getColDataType().toString());
    }

    @Test
    public void testAlterTableAddColumn5() throws JSQLParserException {
        Statement stmt = CCJSqlParserUtil.parse("ALTER TABLE mytable ADD col1 timestamp (3)");

        // COLUMN keyword DOES NOT appear in deparsed statement
        assertStatementCanBeDeparsedAs(stmt, "ALTER TABLE mytable ADD col1 timestamp (3)");

        Alter alter = (Alter) stmt;
        List<AlterExpression> alterExps = alter.getAlterExpressions();
        AlterExpression col1Exp = alterExps.get(0);
        List<ColumnDataType> col1DataTypes = col1Exp.getColDataTypeList();
        assertEquals("col1", col1DataTypes.get(0).getColumnName());
        assertEquals("timestamp (3)", col1DataTypes.get(0).getColDataType().toString());

        assertEquals(col1Exp.hasColumn(), false);
    }

    @Test
    public void testAlterTableAddColumn6() throws JSQLParserException {
        final String sql = "ALTER TABLE mytable ADD COLUMN col1 timestamp (3) not null";
        Statement stmt = CCJSqlParserUtil.parse(sql);
        assertStatementCanBeDeparsedAs(stmt, sql);
        Alter alter = (Alter) stmt;
        List<AlterExpression> alterExps = alter.getAlterExpressions();
        AlterExpression col1Exp = alterExps.get(0);
        assertEquals("not", col1Exp.getColDataTypeList().get(0).getColumnSpecs().get(0));
        assertEquals("null", col1Exp.getColDataTypeList().get(0).getColumnSpecs().get(1));
        
        assertEquals(col1Exp.hasColumn(), true);
    }

    @Test
    public void testAlterTableModifyColumn1() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE animals MODIFY (col1 integer, col2 number (8, 2))");
    }

    @Test
    public void testAlterTableModifyColumn2() throws JSQLParserException {
        Alter alter = (Alter) CCJSqlParserUtil.parse("ALTER TABLE mytable modify col1 timestamp (6)");
        AlterExpression alterExpression = alter.getAlterExpressions().get(0);

        // COLUMN keyword DOES NOT appear in deparsed statement, modify becomes all caps
        assertStatementCanBeDeparsedAs(alter, "ALTER TABLE mytable MODIFY col1 timestamp (6)");

        assertEquals(AlterOperation.MODIFY,  alterExpression.getOperation());
        
        assertEquals(alterExpression.hasColumn(), false);
    }

    @Test
    public void testAlterTableAlterColumn() throws JSQLParserException {
        // http://www.postgresqltutorial.com/postgresql-change-column-type/
        String sql = "ALTER TABLE table_name ALTER COLUMN column_name_1 TYPE TIMESTAMP, ALTER COLUMN column_name_2 TYPE BOOLEAN";
        assertSqlCanBeParsedAndDeparsed(sql);
        
        Alter alter = (Alter) CCJSqlParserUtil.parse(sql);
        AlterExpression alterExpression = alter.getAlterExpressions().get(0);
        
        assertEquals(AlterOperation.ALTER,  alterExpression.getOperation());
        
        assertEquals(alterExpression.hasColumn(), true);
    }

    @Test
    public void testAlterTableChangeColumn1() throws JSQLParserException {
        Statement stmt = CCJSqlParserUtil.parse("ALTER TABLE tb_test CHANGE COLUMN c1 c2 INT (10)");
        Alter alter = (Alter) stmt;
        assertEquals(AlterOperation.CHANGE, alter.getAlterExpressions().get(0).getOperation());
        assertEquals("c1", alter.getAlterExpressions().get(0).getColOldName());
        assertEquals("COLUMN", alter.getAlterExpressions().get(0).getOptionalSpecifier());
    }

    @Test
    public void testAlterTableChangeColumn2() throws JSQLParserException {
        Statement stmt = CCJSqlParserUtil.parse("ALTER TABLE tb_test CHANGE c1 c2 INT (10)");
        Alter alter = (Alter) stmt;
        assertEquals(AlterOperation.CHANGE, alter.getAlterExpressions().get(0).getOperation());
        assertEquals("c1", alter.getAlterExpressions().get(0).getColOldName());
        assertNull(alter.getAlterExpressions().get(0).getOptionalSpecifier());
    }

    @Test
    public void testAlterTableChangeColumn3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE tb_test CHANGE COLUMN c1 c2 INT (10)");
    }

    @Test
    public void testAlterTableChangeColumn4() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE tb_test CHANGE c1 c2 INT (10)");
    }

    @Test
    public void testAlterTableAddColumnWithZone() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE mytable ADD COLUMN col1 timestamp with time zone");
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE mytable ADD COLUMN col1 timestamp without time zone");
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE mytable ADD COLUMN col1 date with time zone");
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE mytable ADD COLUMN col1 date without time zone");

        Statement stmt = CCJSqlParserUtil.
                parse("ALTER TABLE mytable ADD COLUMN col1 timestamp with time zone");
        Alter alter = (Alter) stmt;
        List<AlterExpression> alterExps = alter.getAlterExpressions();
        AlterExpression col1Exp = alterExps.get(0);
        List<ColumnDataType> col1DataTypes = col1Exp.getColDataTypeList();
        assertEquals("timestamp with time zone", col1DataTypes.get(0).getColDataType().toString());
    }

    @Test
    public void testAlterTableAddColumnKeywordTypes() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE mytable ADD COLUMN col1 xml");
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE mytable ADD COLUMN col1 interval");
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE mytable ADD COLUMN col1 bit varying");
    }

    @Test
    public void testDropColumnRestrictIssue510() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE TABLE1 DROP COLUMN NewColumn CASCADE");
    }

    @Test
    public void testDropColumnRestrictIssue551() throws JSQLParserException {
        Statement stmt = CCJSqlParserUtil.parse("ALTER TABLE table1 DROP NewColumn");

        // COLUMN keyword DOES NOT appear in deparsed statement, drop becomes all caps
        assertStatementCanBeDeparsedAs(stmt, "ALTER TABLE table1 DROP NewColumn");

    }

    @Test
    public void testAddConstraintKeyIssue320() throws JSQLParserException {
        String tableName = "table1";
        String columnName1 = "col1";
        String columnName2 = "col2";
        String columnName3 = "col3";
        String columnName4 = "col4";
        String constraintName1 = "table1_constraint_1";
        String constraintName2 = "table1_constraint_2";

        for (String constraintType : Arrays.asList("UNIQUE KEY", "KEY")) {
            assertSqlCanBeParsedAndDeparsed("ALTER TABLE " + tableName + " ADD CONSTRAINT " + constraintName1 + " "
                    + constraintType + " (" + columnName1 + ")");

            assertSqlCanBeParsedAndDeparsed("ALTER TABLE " + tableName + " ADD CONSTRAINT " + constraintName1 + " "
                    + constraintType + " (" + columnName1 + ", " + columnName2 + ")");

            assertSqlCanBeParsedAndDeparsed("ALTER TABLE " + tableName + " ADD CONSTRAINT " + constraintName1 + " "
                    + constraintType + " (" + columnName1 + ", " + columnName2 + "), ADD CONSTRAINT "
                    + constraintName2 + " " + constraintType + " (" + columnName3 + ", " + columnName4 + ")");
        }
    }

    @Test
    public void testIssue633() throws JSQLParserException, JSQLParserException, JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE team_phases ADD CONSTRAINT team_phases_id_key UNIQUE (id)");
    }

    @Test
    public void testIssue679() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE tb_session_status ADD INDEX idx_user_id_name (user_id, user_name(10)), ADD INDEX idx_user_name (user_name)");
    }

    @Test
    public void testAlterTableIndex586() throws Exception {
        Statement result = CCJSqlParserUtil.parse("ALTER TABLE biz_add_fee DROP INDEX operation_time, " +
                "ADD UNIQUE INDEX operation_time (`operation_time`, `warehouse_code`, `customerid`, `fees_type`, `external_no`) " +
                "USING BTREE, ALGORITHM = INPLACE");
        assertEquals("ALTER TABLE biz_add_fee DROP INDEX operation_time , " +
                "ADD UNIQUE INDEX operation_time (`operation_time`, `warehouse_code`, `customerid`, `fees_type`, `external_no`) " +
                "USING BTREE, ALGORITHM = INPLACE", result.toString());
    }

    @Test
    public void testIssue259() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE feature_v2 ADD COLUMN third_user_id int (10) unsigned DEFAULT '0' COMMENT '第三方用户id' after kdt_id");
    }

    @Test
    public void testIssue633_2() throws JSQLParserException {
        String statement = "CREATE INDEX idx_american_football_action_plays_1 ON american_football_action_plays USING btree (play_type)";
        Statement parsed = assertSqlCanBeParsedAndDeparsed(statement);
        CreateIndex created = new CreateIndex()
                .withTable(new Table("american_football_action_plays"))
                .withIndex(
                        new Index().withName("idx_american_football_action_plays_1")
                        .addColumns(new ColumnParams("play_type", null)).withUsing("btree")
                        );
        assertDeparse(created, statement);
        assertEqualsObjectTree(parsed, created);
    }

    @Test
    public void testAlterOnlyIssue928() throws JSQLParserException {
        String statement = "ALTER TABLE ONLY categories ADD CONSTRAINT pk_categories PRIMARY KEY (category_id)";
        Statement parsed = assertSqlCanBeParsedAndDeparsed(statement);
        Alter created = new Alter().withUseOnly(true).withTable(new Table("categories")).addAlterExpressions(
                new AlterExpression().withOperation(AlterOperation.ADD).withIndex(new NamedConstraint()
                        .withName(Arrays.asList("pk_categories")).withType("PRIMARY KEY")
                        .addColumns(new ColumnParams("category_id"))));
        assertDeparse(created, statement);
        assertEqualsObjectTree(parsed, created);
    }

    @Test
    public void testAlterConstraintWithoutFKSourceColumnsIssue929() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE orders ADD CONSTRAINT fk_orders_customers FOREIGN KEY (customer_id) REFERENCES customers");
    }

    public void testAlterTableAlterColumnDropNotNullIssue918() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE \"user_table_t\" ALTER COLUMN name DROP NOT NULL");
    }

    @Test
    public void testAlterTableRenameColumn() throws JSQLParserException {
        String sql = "ALTER TABLE \"test_table\" RENAME COLUMN \"test_column\" TO \"test_c\"";
        assertSqlCanBeParsedAndDeparsed(sql);

        Alter alter = (Alter) CCJSqlParserUtil.parse(sql);
        AlterExpression expression = alter.getAlterExpressions().get(0);
        assertEquals(expression.getOperation(), AlterOperation.RENAME);
        assertEquals(expression.getColOldName(), "\"test_column\"");
        assertEquals(expression.getColumnName(), "\"test_c\"");
    }

    @Test
    public void testAlterTableForeignKeyIssue981() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "ALTER TABLE atconfigpro " +
                "ADD CONSTRAINT atconfigpro_atconfignow_id_foreign FOREIGN KEY (atconfignow_id) REFERENCES atconfignow(id) ON DELETE CASCADE, " +
                "ADD CONSTRAINT atconfigpro_attariff_id_foreign FOREIGN KEY (attariff_id) REFERENCES attariff(id) ON DELETE CASCADE");
    }

    @Test
    public void testAlterTableForeignKeyIssue981_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "ALTER TABLE atconfigpro " +
                        "ADD CONSTRAINT atconfigpro_atconfignow_id_foreign FOREIGN KEY (atconfignow_id) REFERENCES atconfignow(id) ON DELETE CASCADE");
    }

    @Test
    public void testAlterTableTableCommentIssue984() throws JSQLParserException {
        String statement = "ALTER TABLE texto_fichero COMMENT 'This is a sample comment'";
        Statement parsed = assertSqlCanBeParsedAndDeparsed(statement);
        Alter created = new Alter().withTable(new Table("texto_fichero"))
                .addAlterExpressions(new AlterExpression().withOperation(AlterOperation.COMMENT)
                        .withCommentText("'This is a sample comment'"));
        assertDeparse(created, statement);
        assertEqualsObjectTree(parsed, created);
    }

    @Test
    public void testAlterTableColumnCommentIssue984() throws JSQLParserException {
        String statement = "ALTER TABLE texto_fichero MODIFY id COMMENT 'some comment'";
        Statement parsed = assertSqlCanBeParsedAndDeparsed(
                statement);
        Alter created = new Alter().withTable(new Table("texto_fichero"))
                .addAlterExpressions(new AlterExpression().withOperation(AlterOperation.MODIFY).withColumnName("id")
                        .withCommentText("'some comment'"));
        assertDeparse(created, statement);
        assertEqualsObjectTree(parsed, created);
    }

    @Test
    public void testAlterOnUpdateCascade() throws JSQLParserException {
        String statement = "ALTER TABLE mytab ADD CONSTRAINT fk_mytab FOREIGN KEY (col) "
                + "REFERENCES reftab(id) ON UPDATE CASCADE";
        Alter parsed = (Alter) CCJSqlParserUtil.parse(statement);
        assertStatementCanBeDeparsedAs(parsed, statement, true);
        assertReferentialActionOnConstraint(parsed, Action.CASCADE, null);

        statement = "ALTER TABLE mytab ADD FOREIGN KEY (col) "
                + "REFERENCES reftab(id) ON UPDATE CASCADE";
        parsed = (Alter) CCJSqlParserUtil.parse(statement);
        assertStatementCanBeDeparsedAs(parsed, statement, true);
        assertReferentialAction(parsed, Action.CASCADE, null);
    }

    @Test
    public void testAlterOnUpdateSetNull() throws JSQLParserException {
        String statement = "ALTER TABLE mytab ADD CONSTRAINT fk_mytab FOREIGN KEY (col) "
                + "REFERENCES reftab(id) ON UPDATE SET NULL";
        Alter parsed = (Alter) CCJSqlParserUtil.parse(statement);
        assertStatementCanBeDeparsedAs(parsed, statement, true);
        assertReferentialActionOnConstraint(parsed, Action.SET_NULL, null);

        statement = "ALTER TABLE mytab ADD FOREIGN KEY (col) "
                + "REFERENCES reftab(id) ON UPDATE SET NULL";
        parsed = (Alter) CCJSqlParserUtil.parse(statement);
        assertStatementCanBeDeparsedAs(parsed, statement, true);
        assertReferentialAction(parsed, Action.SET_NULL, null);
    }

    @Test
    public void testAlterOnUpdateRestrict() throws JSQLParserException {
        String statement = "ALTER TABLE mytab ADD CONSTRAINT fk_mytab FOREIGN KEY (col) "
                + "REFERENCES reftab(id) ON UPDATE RESTRICT";
        Alter parsed = (Alter) CCJSqlParserUtil.parse(statement);
        assertStatementCanBeDeparsedAs(parsed, statement, true);
        assertReferentialActionOnConstraint(parsed, Action.RESTRICT, null);

        statement = "ALTER TABLE mytab ADD FOREIGN KEY (col) "
                + "REFERENCES reftab(id) ON UPDATE RESTRICT";
        parsed = (Alter) CCJSqlParserUtil.parse(statement);
        assertStatementCanBeDeparsedAs(parsed, statement, true);
        assertReferentialAction(parsed, Action.RESTRICT, null);
    }

    @Test
    public void testAlterOnUpdateSetDefault() throws JSQLParserException {
        String statement = "ALTER TABLE mytab ADD CONSTRAINT fk_mytab FOREIGN KEY (col) "
                + "REFERENCES reftab(id) ON UPDATE SET DEFAULT";
        Alter parsed = (Alter) CCJSqlParserUtil.parse(statement);
        assertStatementCanBeDeparsedAs(parsed, statement, true);
        assertReferentialActionOnConstraint(parsed, Action.SET_DEFAULT, null);

        statement = "ALTER TABLE mytab ADD FOREIGN KEY (col) "
                + "REFERENCES reftab(id) ON UPDATE SET DEFAULT";
        parsed = (Alter) CCJSqlParserUtil.parse(statement);
        assertStatementCanBeDeparsedAs(parsed, statement, true);
        assertReferentialAction(parsed, Action.SET_DEFAULT, null);
    }

    @Test
    public void testAlterOnUpdateNoAction() throws JSQLParserException {
        String statement = "ALTER TABLE mytab ADD CONSTRAINT fk_mytab FOREIGN KEY (col) "
                + "REFERENCES reftab(id) ON UPDATE NO ACTION";
        Alter parsed = (Alter) CCJSqlParserUtil.parse(statement);
        assertStatementCanBeDeparsedAs(parsed, statement, true);
        assertReferentialActionOnConstraint(parsed, Action.NO_ACTION, null);

        statement = "ALTER TABLE mytab ADD FOREIGN KEY (col) "
                + "REFERENCES reftab(id) ON UPDATE NO ACTION";
        parsed = (Alter) CCJSqlParserUtil.parse(statement);
        assertStatementCanBeDeparsedAs(parsed, statement, true);
        assertReferentialAction(parsed, Action.NO_ACTION, null);
    }

    @Test
    public void testAlterOnDeleteSetDefault() throws JSQLParserException {
        String statement = "ALTER TABLE mytab ADD CONSTRAINT fk_mytab FOREIGN KEY (col) "
                + "REFERENCES reftab(id) ON DELETE SET DEFAULT";
        Alter parsed = (Alter) CCJSqlParserUtil.parse(statement);
        assertStatementCanBeDeparsedAs(parsed, statement, true);
        assertReferentialActionOnConstraint(parsed, null, Action.SET_DEFAULT);

        statement = "ALTER TABLE mytab ADD FOREIGN KEY (col) "
                + "REFERENCES reftab(id) ON DELETE SET DEFAULT";
        parsed = (Alter) CCJSqlParserUtil.parse(statement);
        assertStatementCanBeDeparsedAs(parsed, statement, true);
        assertReferentialAction(parsed, null, Action.SET_DEFAULT);
    }

    @Test
    public void testAlterOnDeleteNoAction() throws JSQLParserException {
        String statement = "ALTER TABLE mytab ADD CONSTRAINT fk_mytab FOREIGN KEY (col) "
                + "REFERENCES reftab(id) ON DELETE NO ACTION";
        Alter parsed = (Alter) CCJSqlParserUtil.parse(statement);
        assertStatementCanBeDeparsedAs(parsed, statement, true);
        assertReferentialActionOnConstraint(parsed, null, Action.NO_ACTION);

        statement = "ALTER TABLE mytab ADD FOREIGN KEY (col) "
                + "REFERENCES reftab(id) ON DELETE NO ACTION";
        parsed = (Alter) CCJSqlParserUtil.parse(statement);
        assertStatementCanBeDeparsedAs(parsed, statement, true);
        assertReferentialAction(parsed, null, Action.NO_ACTION);
    }

    @Test
    public void testOnUpdateOnDeleteOrOnDeleteOnUpdate() throws JSQLParserException {
        String onUpdateOnDelete = "ON UPDATE CASCADE ON DELETE SET NULL";
        String onDeleteonUpdate = "ON UPDATE CASCADE ON DELETE SET NULL";
        String constraint = "ALTER TABLE mytab ADD CONSTRAINT fk_mytab FOREIGN KEY (col) "
                + "REFERENCES reftab (id) ";
        String fk = "ALTER TABLE mytab ADD FOREIGN KEY (col) "
                + "REFERENCES reftab (id) ";

        String statement = constraint + onUpdateOnDelete;
        Alter parsed = (Alter) CCJSqlParserUtil.parse(statement);
        assertStatementCanBeDeparsedAs(parsed, statement, true);
        assertReferentialActionOnConstraint(parsed, Action.CASCADE, Action.SET_NULL);

        statement = constraint + onDeleteonUpdate;
        parsed = (Alter) CCJSqlParserUtil.parse(statement);
        assertStatementCanBeDeparsedAs(parsed, statement, true);
        assertReferentialActionOnConstraint(parsed, Action.CASCADE, Action.SET_NULL);

        statement = fk + onUpdateOnDelete;
        parsed = (Alter) CCJSqlParserUtil.parse(statement);
        assertStatementCanBeDeparsedAs(parsed, statement, true);
        assertReferentialAction(parsed, Action.CASCADE, Action.SET_NULL);

        statement = fk + onDeleteonUpdate;
        parsed = (Alter) CCJSqlParserUtil.parse(statement);
        assertStatementCanBeDeparsedAs(parsed, statement, true);
        assertReferentialAction(parsed, Action.CASCADE, Action.SET_NULL);
    }

    @Test
    public void testIssue985_1() throws JSQLParserException {
        String statement = "ALTER TABLE texto_fichero " +
                "ADD CONSTRAINT texto_fichero_fichero_id_foreign FOREIGN KEY (fichero_id) "
                + "REFERENCES fichero (id) ON DELETE SET DEFAULT ON UPDATE CASCADE, "
                + "ADD CONSTRAINT texto_fichero_texto_id_foreign FOREIGN KEY (texto_id) "
                + "REFERENCES texto(id) ON DELETE SET DEFAULT ON UPDATE CASCADE";
        Alter parsed = (Alter) CCJSqlParserUtil.parse(statement);
        assertStatementCanBeDeparsedAs(parsed, statement, true);
        assertReferentialActionOnConstraint(parsed, Action.CASCADE, Action.SET_DEFAULT);

        statement = "ALTER TABLE texto_fichero " +
                "ADD FOREIGN KEY (fichero_id) "
                + "REFERENCES fichero (id) ON DELETE SET DEFAULT ON UPDATE CASCADE, "
                + "ADD FOREIGN KEY (texto_id) "
                + "REFERENCES texto(id) ON DELETE SET DEFAULT ON UPDATE CASCADE";
        parsed = (Alter) CCJSqlParserUtil.parse(statement);
        assertStatementCanBeDeparsedAs(parsed, statement, true);
        assertReferentialAction(parsed, Action.CASCADE, Action.SET_DEFAULT);
    }

    @Test
    public void testIssue985_2() throws JSQLParserException {
        String statement = "ALTER TABLE texto " +
                "ADD CONSTRAINT texto_autor_id_foreign FOREIGN KEY (autor_id) "
                + "REFERENCES users (id) ON UPDATE CASCADE, "
                + "ADD CONSTRAINT texto_tipotexto_id_foreign FOREIGN KEY (tipotexto_id) "
                + "REFERENCES tipotexto(id) ON UPDATE CASCADE";
        Alter parsed = (Alter) CCJSqlParserUtil.parse(statement);
        assertStatementCanBeDeparsedAs(parsed, statement, true);
        assertReferentialActionOnConstraint(parsed, Action.CASCADE, null);
    }
    
    @Test
    public void testAlterTableDefaultValueTrueIssue926() throws JSQLParserException {
        Alter parsed = (Alter) CCJSqlParserUtil.parse("ALTER TABLE my_table ADD some_column BOOLEAN DEFAULT FALSE");
        
        // There shall be no COLUMN where there is no COLUMN
        assertStatementCanBeDeparsedAs(parsed, "ALTER TABLE my_table ADD some_column BOOLEAN DEFAULT FALSE");
    }

    private void assertReferentialActionOnConstraint(Alter parsed, Action onUpdate,
            Action onDelete) {
        AlterExpression alterExpression = parsed.getAlterExpressions().get(0);
        ForeignKeyIndex index = (ForeignKeyIndex) alterExpression.getIndex();

        // remove line if deprecated methods are removed.
        index.setOnDeleteReferenceOption(index.getOnDeleteReferenceOption());
        if (onDelete != null) {
            assertEquals(new ReferentialAction(Type.DELETE, onDelete), index.getReferentialAction(Type.DELETE));
        } else {
            assertNull(index.getReferentialAction(Type.DELETE));
        }

        // remove line if deprecated methods are removed.
        index.setOnUpdateReferenceOption(index.getOnUpdateReferenceOption());
        if (onUpdate != null) {
            assertEquals(new ReferentialAction(Type.UPDATE, onUpdate), index.getReferentialAction(Type.UPDATE));
        } else {
            assertNull(index.getReferentialAction(Type.UPDATE));
        }
    }

    private void assertReferentialAction(Alter parsed, Action onUpdate, Action onDelete) {
        AlterExpression alterExpression = parsed.getAlterExpressions().get(0);

        if (onDelete != null) {
            ReferentialAction actual = alterExpression.getReferentialAction(Type.DELETE);
            assertEquals(new ReferentialAction(Type.DELETE, onDelete), actual);
            // remove line if deprecated methods are removed.
            if (Action.CASCADE.equals(actual.getAction())) {
                alterExpression.setOnDeleteCascade(alterExpression.isOnDeleteCascade());
            }
            if (Action.RESTRICT.equals(actual.getAction())) {
                alterExpression.setOnDeleteRestrict(alterExpression.isOnDeleteRestrict());
            }
            if (Action.SET_NULL.equals(actual.getAction())) {
                alterExpression.setOnDeleteSetNull(alterExpression.isOnDeleteSetNull());
            }
        } else {
            assertNull(alterExpression.getReferentialAction(Type.DELETE));
        }

        if (onUpdate != null) {
            // remove line if deprecated methods are removed.
            assertEquals(new ReferentialAction(Type.UPDATE, onUpdate),
                    alterExpression.getReferentialAction(Type.UPDATE));
        } else {
            assertNull(alterExpression.getReferentialAction(Type.UPDATE));
        }
    }
    
    @Test
    public void testRowFormatKeywordIssue1033() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE basic_test_case "
            + "ADD COLUMN display_name varchar(512) NOT NULL DEFAULT '' AFTER name"
            + ", ADD KEY test_case_status (test_case_status)"
            + ", add KEY display_name (display_name), ROW_FORMAT=DYNAMIC", true);
        
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE t1 MOVE TABLESPACE users", true);
        
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE test_tab MOVE PARTITION test_tab_q2 COMPRESS", true);
    }

}

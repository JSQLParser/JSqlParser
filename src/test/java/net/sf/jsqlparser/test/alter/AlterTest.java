package net.sf.jsqlparser.test.alter;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static net.sf.jsqlparser.test.TestUtils.assertStatementCanBeDeparsedAs;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterExpression;
import net.sf.jsqlparser.statement.alter.AlterExpression.ColumnDataType;
import net.sf.jsqlparser.statement.alter.AlterOperation;

public class AlterTest extends TestCase {

    public AlterTest(String arg0) {
        super(arg0);
    }

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

    public void testAlterTablePrimaryKey() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE animals ADD PRIMARY KEY (id)");
    }

    public void testAlterTablePrimaryKeyDeferrable() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE animals ADD PRIMARY KEY (id) DEFERRABLE");
    }

    public void testAlterTablePrimaryKeyNotDeferrable() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE animals ADD PRIMARY KEY (id) NOT DEFERRABLE");
    }

    public void testAlterTablePrimaryKeyValidate() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE animals ADD PRIMARY KEY (id) VALIDATE");
    }

    public void testAlterTablePrimaryKeyNoValidate() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE animals ADD PRIMARY KEY (id) NOVALIDATE");
    }

    public void testAlterTablePrimaryKeyDeferrableValidate() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE animals ADD PRIMARY KEY (id) DEFERRABLE VALIDATE");
    }

    public void testAlterTablePrimaryKeyDeferrableDisableNoValidate() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE animals ADD PRIMARY KEY (id) DEFERRABLE DISABLE NOVALIDATE");
    }

    public void testAlterTableUniqueKey() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE `schema_migrations` ADD UNIQUE KEY `unique_schema_migrations` (`version`)");
    }

    public void testAlterTableForgeignKey() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE test ADD FOREIGN KEY (user_id) REFERENCES ra_user (id) ON DELETE CASCADE");
    }

    public void testAlterTableAddConstraint() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE RESOURCELINKTYPE ADD CONSTRAINT FK_RESOURCELINKTYPE_PARENTTYPE_PRIMARYKEY FOREIGN KEY (PARENTTYPE_PRIMARYKEY) REFERENCES RESOURCETYPE(PRIMARYKEY)");
    }

    public void testAlterTableAddConstraintWithConstraintState() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE RESOURCELINKTYPE ADD CONSTRAINT FK_RESOURCELINKTYPE_PARENTTYPE_PRIMARYKEY FOREIGN KEY (PARENTTYPE_PRIMARYKEY) REFERENCES RESOURCETYPE(PRIMARYKEY) DEFERRABLE DISABLE NOVALIDATE");
    }
    
    public void testAlterTableAddConstraintWithConstraintState2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE RESOURCELINKTYPE ADD CONSTRAINT RESOURCELINKTYPE_PRIMARYKEY PRIMARY KEY (PRIMARYKEY) DEFERRABLE NOVALIDATE");
    }

    public void testAlterTableForgeignKey2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE test ADD FOREIGN KEY (user_id) REFERENCES ra_user (id)");
    }

    public void testAlterTableForgeignKey3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE test ADD FOREIGN KEY (user_id) REFERENCES ra_user (id) ON DELETE RESTRICT");
    }

    public void testAlterTableForgeignKey4() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE test ADD FOREIGN KEY (user_id) REFERENCES ra_user (id) ON DELETE SET NULL");
    }

    public void testAlterTableDropColumn() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE test DROP COLUMN YYY");
    }

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

    public void testAlterTableDropConstraint() throws JSQLParserException {
        final String sql = "ALTER TABLE test DROP CONSTRAINT YYY";
        Statement stmt = CCJSqlParserUtil.parse(sql);
        assertStatementCanBeDeparsedAs(stmt, sql);
        AlterExpression alterExpression = ((Alter) stmt).getAlterExpressions().get(0);
        assertEquals(alterExpression.getConstraintName(), "YYY");
    }

    public void testAlterTablePK() throws JSQLParserException {
        final String sql = "ALTER TABLE `Author` ADD CONSTRAINT `AuthorPK` PRIMARY KEY (`ID`)";
        Statement stmt = CCJSqlParserUtil.parse(sql);
        assertStatementCanBeDeparsedAs(stmt, sql);
        AlterExpression alterExpression = ((Alter) stmt).getAlterExpressions().get(0);
        assertNull(alterExpression.getConstraintName());
        // TODO: should this pass? ==>        assertEquals(alterExpression.getPkColumns().get(0), "ID");
        assertEquals(alterExpression.getIndex().getColumnsNames().get(0), "`ID`");
    }

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

    public void testAlterTableCheckConstraint() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE `Author` ADD CONSTRAINT name_not_empty CHECK (`NAME` <> '')");
    }

    public void testAlterTableAddColumn2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE animals ADD (col1 integer, col2 integer)");
    }

    public void testAlterTableAddColumn3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE mytable ADD COLUMN mycolumn varchar (255)");
    }

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

    public void testAlterTableAddColumn5() throws JSQLParserException {
        Statement stmt = CCJSqlParserUtil.parse("ALTER TABLE mytable ADD col1 timestamp (3)");

        // COLUMN keyword appears in deparsed statement
        assertStatementCanBeDeparsedAs(stmt, "ALTER TABLE mytable ADD COLUMN col1 timestamp (3)");

        Alter alter = (Alter) stmt;
        List<AlterExpression> alterExps = alter.getAlterExpressions();
        AlterExpression col1Exp = alterExps.get(0);
        List<ColumnDataType> col1DataTypes = col1Exp.getColDataTypeList();
        assertEquals("col1", col1DataTypes.get(0).getColumnName());
        assertEquals("timestamp (3)", col1DataTypes.get(0).getColDataType().toString());
    }

    public void testAlterTableAddColumn6() throws JSQLParserException {
        final String sql = "ALTER TABLE mytable ADD COLUMN col1 timestamp (3) not null";
        Statement stmt = CCJSqlParserUtil.parse(sql);
        assertStatementCanBeDeparsedAs(stmt, sql);
        Alter alter = (Alter) stmt;
        List<AlterExpression> alterExps = alter.getAlterExpressions();
        AlterExpression col1Exp = alterExps.get(0);
        assertEquals("not", col1Exp.getColDataTypeList().get(0).getColumnSpecs().get(0));
        assertEquals("null", col1Exp.getColDataTypeList().get(0).getColumnSpecs().get(1));
    }

    public void testAlterTableModifyColumn1() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE animals MODIFY (col1 integer, col2 number (8, 2))");
    }

    public void testAlterTableModifyColumn2() throws JSQLParserException {
        Statement stmt = CCJSqlParserUtil.parse("ALTER TABLE mytable modify col1 timestamp (6)");

        // COLUMN keyword appears in deparsed statement, modify becomes all caps
        assertStatementCanBeDeparsedAs(stmt, "ALTER TABLE mytable MODIFY COLUMN col1 timestamp (6)");

        assertEquals(AlterOperation.MODIFY, ((Alter) stmt).getAlterExpressions().get(0).
                getOperation());
    }

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

    public void testAlterTableAddColumnKeywordTypes() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE mytable ADD COLUMN col1 xml");
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE mytable ADD COLUMN col1 interval");
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE mytable ADD COLUMN col1 bit varying");
    }
    
    public void testDropColumnRestrictIssue510() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE TABLE1 DROP COLUMN NewColumn CASCADE");
    }
    
    public void testDropColumnRestrictIssue551() throws JSQLParserException {
        Statement stmt = CCJSqlParserUtil.parse("ALTER TABLE table1 DROP NewColumn");
        
        // COLUMN keyword appears in deparsed statement, drop becomes all caps
        assertStatementCanBeDeparsedAs(stmt, "ALTER TABLE table1 DROP COLUMN NewColumn");
        
    }
    
    public void testAddConstraintKeyIssue320() throws JSQLParserException {
        String tableName = "table1";
        String columnName1 = "col1";
        String columnName2 = "col2";
        String columnName3 = "col3";
        String columnName4 = "col4";
        String constraintName1 = "table1_constraint_1";
        String constraintName2 = "table1_constraint_2";

        for(String constraintType : Arrays.asList("UNIQUE KEY", "KEY")) {
            assertSqlCanBeParsedAndDeparsed("ALTER TABLE " + tableName + " ADD CONSTRAINT " + constraintName1 + " " 
                    + constraintType + " (" + columnName1 + ")");

            assertSqlCanBeParsedAndDeparsed("ALTER TABLE " + tableName + " ADD CONSTRAINT " + constraintName1 + " " 
                    + constraintType + " (" + columnName1 + ", " + columnName2 + ")");

            assertSqlCanBeParsedAndDeparsed("ALTER TABLE " + tableName + " ADD CONSTRAINT " + constraintName1 + " " 
                    + constraintType + " (" + columnName1 + ", " + columnName2 + "), ADD CONSTRAINT " 
                    + constraintName2 + " " + constraintType + " (" + columnName3 + ", " + columnName4 + ")");
        }
    }
}

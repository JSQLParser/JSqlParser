package net.sf.jsqlparser.test.alter;


import java.util.List;
import junit.framework.TestCase;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterExpression;
import net.sf.jsqlparser.statement.alter.AlterExpression.ColumnDataType;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;

public class AlterTest extends TestCase {

	public AlterTest(String arg0) {
		super(arg0);
	}

	public void testAlterTableAddColumn() throws JSQLParserException {
		Statement stmt = CCJSqlParserUtil.parse("ALTER TABLE mytable ADD COLUMN mycolumn varchar (255)");
		assertTrue(stmt instanceof Alter);
		Alter alter = (Alter)stmt;
		assertEquals("mytable",alter.getTable().getFullyQualifiedName());
        AlterExpression alterExp = alter.getAlterExpressions().get(0);
        assertNotNull(alterExp);
        List<ColumnDataType> colDataTypes = alterExp.getColDataTypeList();
		assertEquals("mycolumn", colDataTypes.get(0).getColumnName());
		assertEquals("varchar (255)", colDataTypes.get(0).getColDataType().toString());
	}
    
    public void testAlterTablePrimaryKey() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE animals ADD PRIMARY KEY (id)");
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

		Statement stmt = CCJSqlParserUtil.parse("ALTER TABLE mytable DROP COLUMN col1, DROP COLUMN col2");
		Alter alter = (Alter)stmt;
		List<AlterExpression> alterExps = alter.getAlterExpressions();
		AlterExpression col1Exp = alterExps.get(0);
		AlterExpression col2Exp = alterExps.get(1);
		assertEquals("col1", col1Exp.getColumnName());
		assertEquals("col2", col2Exp.getColumnName());
	}

    public void testAlterTableDropConstraint() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE test DROP CONSTRAINT YYY");
    }

    public void testAlterTablePK() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE `Author` ADD CONSTRAINT `AuthorPK` PRIMARY KEY (`ID`)");
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

		Statement stmt = CCJSqlParserUtil.parse("ALTER TABLE mytable ADD COLUMN col1 varchar (255), ADD COLUMN col2 integer");
		Alter alter = (Alter)stmt;
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

}

package net.sf.jsqlparser.test.alter;


import junit.framework.TestCase;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
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
		assertEquals("mycolumn", alter.getColumnName());
		assertEquals("varchar (255)", alter.getDataType().toString());
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

    public void testAlterTableDropConstraint() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE test DROP CONSTRAINT YYY");
    }
    
    public void testAlterTablePK() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE `Author` ADD CONSTRAINT `AuthorPK` PRIMARY KEY (`ID`)");
    }

    public void testAlterTableCheckConstraint() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE `Author` ADD CONSTRAINT name_not_empty CHECK (`NAME` <> '')");
    }

}

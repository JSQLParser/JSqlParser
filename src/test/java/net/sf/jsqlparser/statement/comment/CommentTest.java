package net.sf.jsqlparser.statement.comment;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

import org.junit.Test;

public class CommentTest {

    private final CCJSqlParserManager parserManager = new CCJSqlParserManager();

    @Test
    public void testCommentTable() throws JSQLParserException {
        String statement = "COMMENT ON TABLE table1 IS 'comment1'";
        Comment comment = (Comment) parserManager.parse(new StringReader(statement));
        Table table = comment.getTable();
        assertEquals("table1", table.getName());
        assertEquals("comment1", comment.getComment().getValue());
        assertEquals(statement, "" + comment);
    }

    @Test
    public void testCommentTable2() throws JSQLParserException {
        String statement = "COMMENT ON TABLE schema1.table1 IS 'comment1'";
        Comment comment = (Comment) parserManager.parse(new StringReader(statement));
        Table table = comment.getTable();
        assertEquals("schema1", table.getSchemaName());
        assertEquals("table1", table.getName());
        assertEquals("comment1", comment.getComment().getValue());
        assertEquals(statement, "" + comment);
    }

    @Test
    public void testCommentTableDeparse() throws JSQLParserException {
        String statement = "COMMENT ON TABLE table1 IS 'comment1'";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testCommentColumn() throws JSQLParserException {
        String statement = "COMMENT ON COLUMN table1.column1 IS 'comment1'";
        Comment comment = (Comment) parserManager.parse(new StringReader(statement));
        Column column = comment.getColumn();
        assertEquals("table1", column.getTable().getName());
        assertEquals("column1", column.getColumnName());
        assertEquals("comment1", comment.getComment().getValue());
        assertEquals(statement, "" + comment);
    }

    @Test
    public void testCommentColumnDeparse() throws JSQLParserException {
        String statement = "COMMENT ON COLUMN table1.column1 IS 'comment1'";
        assertSqlCanBeParsedAndDeparsed(statement);
    }

    @Test
    public void testToString() {
        Comment comment = new Comment();
        assertEquals("COMMENT ON IS null", comment.toString());
    }
}

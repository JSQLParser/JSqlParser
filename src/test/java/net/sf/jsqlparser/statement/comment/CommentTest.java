package net.sf.jsqlparser.statement.comment;

import java.io.StringReader;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class CommentTest {

    @Test
    public void testCommentTable() throws JSQLParserException {
        String statement = "COMMENT ON TABLE table1 IS 'comment1'";
        Comment comment = (Comment) CCJSqlParserUtil.parse(new StringReader(statement));
        Table table = comment.getTable();
        assertEquals("table1", table.getName());
        assertEquals("comment1", comment.getComment().getValue());
        assertEquals(statement, "" + comment);
    }

    @Test
    public void testCommentTable2() throws JSQLParserException {
        String statement = "COMMENT ON TABLE schema1.table1 IS 'comment1'";
        Comment comment = (Comment) CCJSqlParserUtil.parse(new StringReader(statement));
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
        Comment comment = (Comment) CCJSqlParserUtil.parse(new StringReader(statement));
        Column column = comment.getColumn();
        assertEquals("table1", column.getTable().getName());
        assertEquals("column1", column.getColumnName());
        assertEquals("comment1", comment.getComment().getValue());
        assertEquals(statement, "" + comment);
    }

    @Test
    public void testCommentColumnDeparse() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("COMMENT ON COLUMN table1.column1 IS 'comment1'");
    }

    @Test
    public void testToString() {
        Comment comment = new Comment();
        assertEquals("COMMENT ON IS null", comment.toString());
    }

    @Test
    public void testCommentColumnDeparseIssue696() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("COMMENT ON COLUMN hotels.hotelid IS 'Primary key of the table'");
    }
}

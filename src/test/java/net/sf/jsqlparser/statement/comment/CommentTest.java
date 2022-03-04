/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.comment;

import java.io.StringReader;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import static net.sf.jsqlparser.test.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

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

        Comment c = new Comment().withTable(new Table("table1")).withComment(new StringValue("comment1"));
        assertEquals("table1", c.getTable().getName());
        assertEquals("comment1", c.getComment().getValue());
        assertDeparse(c, statement, false);
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

        Comment c = new Comment().withColumn(new Column(new Table("table1"), "column1"))
                .withComment(new StringValue("comment1"));
        assertDeparse(c, statement, false);
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

    @Test
    public void testCommentTableColumnDiffersIssue984() throws JSQLParserException {
        Comment comment = (Comment) CCJSqlParserUtil.parse("COMMENT ON COLUMN myTable.myColumn is 'Some comment'");
        assertThat(comment.getTable()).isNull();
        assertThat(comment.getColumn().getColumnName()).isEqualTo("myColumn");
        assertThat(comment.getColumn().getTable().getFullyQualifiedName()).isEqualTo("myTable");
    }

    @Test
    public void testCommentTableColumnDiffersIssue984_2() throws JSQLParserException {
        Comment comment = (Comment) CCJSqlParserUtil.parse("COMMENT ON COLUMN mySchema.myTable.myColumn is 'Some comment'");
        assertThat(comment.getTable()).isNull();
        assertThat(comment.getColumn().getColumnName()).isEqualTo("myColumn");
        assertThat(comment.getColumn().getTable().getFullyQualifiedName()).isEqualTo("mySchema.myTable");
        assertThat(comment.getColumn().getTable().getName()).isEqualTo("myTable");
        assertThat(comment.getColumn().getTable().getSchemaName()).isEqualTo("mySchema");
    }

    @Test
    public void testCommentOnView() throws JSQLParserException {
        String statement = "COMMENT ON VIEW myschema.myView IS 'myComment'";
        Comment comment = (Comment) CCJSqlParserUtil.parse(statement);
        assertThat(comment.getTable()).isNull();
        assertThat(comment.getColumn()).isNull();
        assertThat(comment.getView().getFullyQualifiedName()).isEqualTo("myschema.myView");
        assertStatementCanBeDeparsedAs(comment, statement);
    }
}

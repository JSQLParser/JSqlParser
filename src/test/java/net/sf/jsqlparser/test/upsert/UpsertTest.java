package net.sf.jsqlparser.test.upsert;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.upsert.Upsert;
import org.junit.Test;

import java.io.StringReader;

/**
 * 文件描述：
 * 作者： NewPidian
 * 时间： 2017/1/3
 */
public class UpsertTest {
    CCJSqlParserManager parserManager = new CCJSqlParserManager();

    @Test
    public void testUpsert() throws JSQLParserException {
        String sql="UPSERT INTO TEST(NAME,ID) VALUES('foo',123);";
        Upsert upsert= (Upsert)parserManager.parse(new StringReader(sql));
        System.out.println(upsert.getColumns());
        System.out.println(upsert.getTable().getName());
        System.out.println(upsert.getItemsList() );
        System.out.println(upsert.toString());
    }

    @Test
    public void testUpsertDuplicate() throws JSQLParserException {
        String sql="UPSERT INTO TEST(ID, COUNTER) VALUES(123, 0) ON DUPLICATE KEY UPDATE COUNTER = COUNTER + 1;";
        Upsert upsert= (Upsert)parserManager.parse(new StringReader(sql));
        System.out.println(upsert.getDuplicateUpdateColumns()+" "+upsert.getDuplicateUpdateExpressionList());
    }

    @Test
    public void testUpsertSelect() throws JSQLParserException {
        String sql="UPSERT INTO test.targetTable(col1, col2) SELECT col3, col4 FROM test.sourceTable WHERE col5 < 100";
        Upsert upsert= (Upsert)parserManager.parse(new StringReader(sql));
        System.out.println(upsert);
        System.out.println(upsert.getSelect());
    }

    @Test
    public void testUpsertN() throws JSQLParserException {
        String sql="UPSERT INTO TEST VALUES('foo','bar',3);";
        Upsert upsert= (Upsert)parserManager.parse(new StringReader(sql));
        System.out.println(upsert.getColumns());
        System.out.println(upsert);
        System.out.println(upsert.getItemsList());
    }

}
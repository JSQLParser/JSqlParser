/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserDefaultVisitor;
import net.sf.jsqlparser.parser.CCJSqlParserTreeConstants;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.SimpleNode;
import net.sf.jsqlparser.parser.Token;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

/**
 *
 * @author toben
 */
public class SelectASTTest {

    @Test
    public void testSelectASTColumn() throws JSQLParserException {
        String sql = "SELECT  a,  b FROM  mytable  order by   b,  c";
        StringBuilder b = new StringBuilder(sql);
        Statement stmt = CCJSqlParserUtil.parse(sql);
        Select select = (Select) stmt;
        PlainSelect ps = (PlainSelect) select.getSelectBody();
        for (SelectItem item : ps.getSelectItems()) {
            SelectExpressionItem sei = (SelectExpressionItem) item;
            Column c = sei.getExpression(Column.class);
            SimpleNode astNode = c.getASTNode();
            assertNotNull(astNode);
            b.setCharAt(astNode.jjtGetFirstToken().beginColumn - 1, '*');
        }
        for (OrderByElement item : ps.getOrderByElements()) {
            Column c = item.getExpression(Column.class);
            SimpleNode astNode = c.getASTNode();
            assertNotNull(astNode);
            b.setCharAt(astNode.jjtGetFirstToken().beginColumn - 1, '#');
        }
        assertEquals("SELECT  *,  * FROM  mytable  order by   #,  #", b.toString());
    }

    @Test
    public void testSelectASTNode() throws JSQLParserException {
        String sql = "SELECT  a,  b FROM  mytable  order by   b,  c";
        SimpleNode node = (SimpleNode) CCJSqlParserUtil.parseAST(sql);
        node.dump("*");
        assertEquals(CCJSqlParserTreeConstants.JJTSTATEMENT, node.getId());
    }

    private Token subSelectStart;
    private Token subSelectEnd;

    @Test
    public void testSelectASTNodeSubSelect() throws JSQLParserException {
        String sql = "SELECT * FROM  mytable  where 0<(select count(*) from mytable2)";
        SimpleNode node = (SimpleNode) CCJSqlParserUtil.parseAST(sql);
        node.dump("*");
        assertEquals(CCJSqlParserTreeConstants.JJTSTATEMENT, node.getId());
        node.jjtAccept(new CCJSqlParserDefaultVisitor() {
            @Override
            public Object visit(SimpleNode node, Object data) {
                if (node.getId() == CCJSqlParserTreeConstants.JJTSUBSELECT) {
                    subSelectStart = node.jjtGetFirstToken();
                    subSelectEnd = node.jjtGetLastToken();
                    return super.visit(node, data);
                } else {
                    return super.visit(node, data);
                }
            }
        }, null);

        assertNotNull(subSelectStart);
        assertNotNull(subSelectEnd);
        assertEquals(34, subSelectStart.beginColumn);
        assertEquals(62, subSelectEnd.endColumn);
    }

    @Test
    public void testSelectASTColumnLF() throws JSQLParserException {
        String sql = "SELECT  a,  b FROM  mytable \n order by   b,  c";
        StringBuilder b = new StringBuilder(sql);
        Statement stmt = CCJSqlParserUtil.parse(sql);
        Select select = (Select) stmt;
        PlainSelect ps = (PlainSelect) select.getSelectBody();
        for (SelectItem item : ps.getSelectItems()) {
            SelectExpressionItem sei = (SelectExpressionItem) item;
            Column c = sei.getExpression(Column.class);
            SimpleNode astNode = c.getASTNode();
            assertNotNull(astNode);
            b.setCharAt(astNode.jjtGetFirstToken().absoluteBegin - 1, '*');
        }
        for (OrderByElement item : ps.getOrderByElements()) {
            Column c = item.getExpression(Column.class);
            SimpleNode astNode = c.getASTNode();
            assertNotNull(astNode);
            b.setCharAt(astNode.jjtGetFirstToken().absoluteBegin - 1, '#');
        }
        assertEquals("SELECT  *,  * FROM  mytable \n order by   #,  #", b.toString());
    }

    @Test
    public void testSelectASTCommentLF() throws JSQLParserException {
        String sql = "SELECT  /* testcomment */ \n a,  b FROM  -- testcomment2 \n mytable \n order by   b,  c";
        StringBuilder b = new StringBuilder(sql);
        Statement stmt = CCJSqlParserUtil.parse(sql);
        Select select = (Select) stmt;
        PlainSelect ps = (PlainSelect) select.getSelectBody();
        for (SelectItem item : ps.getSelectItems()) {
            SelectExpressionItem sei = (SelectExpressionItem) item;
            Column c = sei.getExpression(Column.class);
            SimpleNode astNode = c.getASTNode();
            assertNotNull(astNode);
            b.setCharAt(astNode.jjtGetFirstToken().absoluteBegin - 1, '*');
        }
        for (OrderByElement item : ps.getOrderByElements()) {
            Column c = item.getExpression(Column.class);
            SimpleNode astNode = c.getASTNode();
            assertNotNull(astNode);
            b.setCharAt(astNode.jjtGetFirstToken().absoluteBegin - 1, '#');
        }
        assertEquals("SELECT  /* testcomment */ \n *,  * FROM  -- testcomment2 \n mytable \n order by   #,  #", b.toString());
    }

    @Test
    public void testSelectASTCommentCRLF() throws JSQLParserException {
        String sql = "SELECT  /* testcomment */ \r\n a,  b FROM  -- testcomment2 \r\n mytable \r\n order by   b,  c";
        StringBuilder b = new StringBuilder(sql);
        Statement stmt = CCJSqlParserUtil.parse(sql);
        Select select = (Select) stmt;
        PlainSelect ps = (PlainSelect) select.getSelectBody();
        for (SelectItem item : ps.getSelectItems()) {
            SelectExpressionItem sei = (SelectExpressionItem) item;
            Column c = sei.getExpression(Column.class);
            SimpleNode astNode = c.getASTNode();
            assertNotNull(astNode);
            b.setCharAt(astNode.jjtGetFirstToken().absoluteBegin - 1, '*');
        }
        for (OrderByElement item : ps.getOrderByElements()) {
            Column c = item.getExpression(Column.class);
            SimpleNode astNode = c.getASTNode();
            assertNotNull(astNode);
            b.setCharAt(astNode.jjtGetFirstToken().absoluteBegin - 1, '#');
        }
        assertEquals("SELECT  /* testcomment */ \r\n *,  * FROM  -- testcomment2 \r\n mytable \r\n order by   #,  #", b.toString());
    }

    @Test
    public void testDetectInExpressions() throws JSQLParserException {
        String sql = "SELECT * FROM  mytable WHERE a IN (1,2,3,4,5,6,7)";
        SimpleNode node = (SimpleNode) CCJSqlParserUtil.parseAST(sql);
        node.dump("*");
        assertEquals(CCJSqlParserTreeConstants.JJTSTATEMENT, node.getId());
        node.jjtAccept(new CCJSqlParserDefaultVisitor() {
            @Override
            public Object visit(SimpleNode node, Object data) {
                if (node.getId() == CCJSqlParserTreeConstants.JJTINEXPRESSION) {
                    subSelectStart = node.jjtGetFirstToken();
                    subSelectEnd = node.jjtGetLastToken();
                    return super.visit(node, data);
                } else {
                    return super.visit(node, data);
                }
            }
        }, null);

        assertNotNull(subSelectStart);
        assertNotNull(subSelectEnd);
        assertEquals(30, subSelectStart.beginColumn);
        assertEquals(49, subSelectEnd.endColumn);
    }

    @Test
    public void testSelectASTExtractWithCommentsIssue1580() throws JSQLParserException {
        String sql = "SELECT  /* testcomment */ \r\n a,  b FROM  -- testcomment2 \r\n mytable \r\n order by   b,  c";
        SimpleNode root = (SimpleNode) CCJSqlParserUtil.parseAST(sql);
        List<Token> comments = new ArrayList<>();

        root.jjtAccept(new CCJSqlParserDefaultVisitor() {
            @Override
            public Object visit(SimpleNode node, Object data) {
                if (node.jjtGetFirstToken().specialToken != null) {
                    //needed since for different nodes we got the same first token
                    if (!comments.contains(node.jjtGetFirstToken().specialToken)) {
                        comments.add(node.jjtGetFirstToken().specialToken);
                    }
                }
                return super.visit(node, data);
            }
        }, null);

        assertThat(comments)
                .extracting(token -> token.image)
                .containsExactly("/* testcomment */", "-- testcomment2 ");
    }

    @Test
    public void testSelectASTExtractWithCommentsIssue1580_2() throws JSQLParserException {
        String sql = "/* I want this comment */\n"
                + "SELECT order_detail_id, quantity\n"
                + "/* But ignore this one safely */\n"
                + "FROM order_details;";
        SimpleNode root = (SimpleNode) CCJSqlParserUtil.parseAST(sql);

        assertThat(root.jjtGetFirstToken().specialToken.image).isEqualTo("/* I want this comment */");
    }
}

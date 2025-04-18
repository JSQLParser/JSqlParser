/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2024 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ASTNodeAccessImplTest {
    @Test
    void testGetParent() throws JSQLParserException {
        String sqlStr = "select listagg(sellerid)\n"
                + "within group (order by sellerid)\n"
                + "over() AS list from winsales;";

        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(sqlStr);
        AnalyticExpression expression =
                (AnalyticExpression) select.getSelectItem(0).getExpression();

        assertInstanceOf(SelectItem.class, expression.getParent());
        assertEquals(select, expression.getParent(Select.class));
    }

    @Test
    void testGetWherePositionIssue1339() throws JSQLParserException {
        // WHERE expression at line 4 column 7
        String sqlStr = "select listagg(sellerid)\n"
                + "within group (order by sellerid)\n"
                + "over() AS list from winsales\n"
                + "WHERE a=b\n"
                + "ORDER BY 1;";
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(sqlStr);
        Expression whereExpression = select.getWhere();

        final SimpleNode node = whereExpression.getASTNode();
        if (node != null) {
            Token token = node.jjtGetFirstToken();
            Assertions.assertEquals(4, token.beginLine);
            Assertions.assertEquals(7, token.beginColumn);
        } else {
            throw new RuntimeException("Node not found.");
        }
    }
}

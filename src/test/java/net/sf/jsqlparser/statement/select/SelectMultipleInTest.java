/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.junit.Test;

public class SelectMultipleInTest {

    @Test
    public void foo() throws Exception {
        Statement parsed = CCJSqlParserUtil.parse("select a,b from foo where (a,b) in ((1,2),(3,4),(5,6),(7,8))");
        System.out.println(parsed);
        Select select = (Select) parsed;
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();

        Expression whereExpression = plainSelect.getWhere();
        InExpression inExpression = (InExpression) whereExpression;


        System.out.println(inExpression.getMultiExpressionList());
    }
}

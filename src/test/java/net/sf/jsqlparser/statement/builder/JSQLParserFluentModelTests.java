/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.builder;

import java.util.List;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.deparser.StatementDeParser;

public class JSQLParserFluentModelTests {

    @Test
    public void testParseAndBuild() throws JSQLParserException {
        String sql = "SELECT * FROM tab1 t1 " //
                + "JOIN tab2 t2 ON t1.ref = t2.id WHERE (t1.col1 = ? OR t1.col2 = ?) AND t1.col3 IN ('A')";
        Statement stmt = CCJSqlParserUtil.parse(sql);
        StringBuilder buffer = new StringBuilder();
        stmt.accept(new StatementDeParser(buffer));
        assertEquals(sql, buffer.toString());

        Table t1 = new Table("tab1").withAlias(new Alias("t1", false));
        Table t2 = new Table("tab2").withAlias(new Alias("t2", false));

        Select select = new Select().withSelectBody( //
                new PlainSelect() //
                .addSelectItems(new AllColumns()) //
                .withFromItem(t1) //
                .addJoins(new Join().withRightItem(t2)
                        .withOnExpression(new EqualsTo(new Column(t1, "ref"), new Column(t2, "id"))))
                        .withWhere(new AndExpression( //
                        new Parenthesis(new OrExpression( //
                                new EqualsTo(new Column(t1, "col1"), new JdbcParameter()),
                                new EqualsTo(new Column(t1, "col2"), new JdbcParameter()) //
                                )), //
                        new InExpression() //
                        .withLeftExpression(new Column(t1, "col3"))
                        .withRightItemsList(
                                new ExpressionList().addExpressions(new StringValue("A"))))));

        ExpressionList list = select.getSelectBody(PlainSelect.class).getWhere(AndExpression.class)
                .getRightExpression(InExpression.class).getRightItemsList(ExpressionList.class);
        List<Expression> elist = list.getExpressions();
        list.setExpressions(elist);

        buffer = new StringBuilder();
        select.accept(new StatementDeParser(buffer));
        assertEquals(sql, buffer.toString());
    }

}

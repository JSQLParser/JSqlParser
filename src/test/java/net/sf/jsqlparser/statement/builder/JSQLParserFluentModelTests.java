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

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.conditional.XorExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.Test;

import java.util.List;

import static net.sf.jsqlparser.test.TestUtils.*;

public class JSQLParserFluentModelTests {

    @Test
    public void testParseAndBuild() throws JSQLParserException {
        String statement = "SELECT * FROM tab1 AS t1 " //
                + "JOIN tab2 t2 ON t1.ref = t2.id WHERE (t1.col1 = ? OR t1.col2 = ?) AND t1.col3 IN ('A')";

        Statement parsed = TestUtils.assertSqlCanBeParsedAndDeparsed(statement);

        Table t1 = new Table("tab1").withAlias(new Alias("t1").withUseAs(true));
        Table t2 = new Table("tab2").withAlias(new Alias("t2", false));

        AndExpression where = new AndExpression().withLeftExpression(
                new Parenthesis(new OrExpression().withLeftExpression(
                        new EqualsTo()
                                .withLeftExpression(new Column(asList("t1", "col1")))
                                .withRightExpression(new JdbcParameter().withIndex(1)))
                        .withRightExpression(new EqualsTo(
                                new Column(asList("t1", "col2")),
                                new JdbcParameter().withIndex(
                                        2)))
                )).withRightExpression(
                new InExpression()
                        .withLeftExpression(new Column(asList("t1", "col3")))
                        .withRightItemsList(new ExpressionList(new StringValue("A"))));

        Select select = new Select().withSelectBody(new PlainSelect().addSelectItems(new AllColumns()).withFromItem(t1)
                .addJoins(new Join().withRightItem(t2)
                        .withOnExpression(
                                new EqualsTo(new Column(asList("t1", "ref")), new Column(asList("t2", "id")))))
                .withWhere(where));

        ExpressionList list = select.getSelectBody(PlainSelect.class).getWhere(AndExpression.class)
                .getRightExpression(InExpression.class).getRightItemsList(ExpressionList.class);
        List<Expression> elist = list.getExpressions();
        list.setExpressions(elist);

        assertDeparse(select, statement);
        assertEqualsObjectTree(parsed, select);
    }

    @Test
    public void testParseAndBuildForXOR() throws JSQLParserException {
        String statement = "SELECT * FROM tab1 AS t1 JOIN tab2 t2 ON t1.ref = t2.id " +
                "WHERE (t1.col1 XOR t2.col2) AND t1.col3 IN ('B', 'C') XOR t2.col4";

        Statement parsed = TestUtils.assertSqlCanBeParsedAndDeparsed(statement);

        Table t1 = new Table("tab1").withAlias(new Alias("t1", true));
        Table t2 = new Table("tab2").withAlias(new Alias("t2", false));

        XorExpression where = new XorExpression()
                .withLeftExpression(new AndExpression()
                        .withLeftExpression(
                                new Parenthesis(
                                        new XorExpression()
                                                .withLeftExpression(new Column(asList("t1", "col1")))
                                                .withRightExpression(new Column(asList("t2", "col2")))))
                        .withRightExpression(
                                new InExpression()
                                        .withLeftExpression(new Column(asList("t1", "col3")))
                                        .withRightItemsList(new ExpressionList(new StringValue("B"), new StringValue("C")))))
                .withRightExpression(new Column(asList("t2", "col4")));

        Select select = new Select().withSelectBody(new PlainSelect().addSelectItems(new AllColumns()).withFromItem(t1)
                .addJoins(new Join().withRightItem(t2)
                        .withOnExpression(
                                new EqualsTo(new Column(asList("t1", "ref")), new Column(asList("t2", "id")))))
                .withWhere(where));

        ExpressionList list = select.getSelectBody(PlainSelect.class).getWhere(XorExpression.class)
                .getLeftExpression(AndExpression.class)
                .getRightExpression(InExpression.class)
                .getRightItemsList(ExpressionList.class);
        List<Expression> elist = list.getExpressions();
        list.setExpressions(elist);

        assertDeparse(select, statement);
        assertEqualsObjectTree(parsed, select);
    }

    @Test
    public void testParseAndBuildForXORComplexCondition() throws JSQLParserException {
        String statement = "SELECT * FROM tab1 AS t1 WHERE " +
                "a AND b OR c XOR d";

        Statement parsed = TestUtils.assertSqlCanBeParsedAndDeparsed(statement);

        Table t1 = new Table("tab1").withAlias(new Alias("t1", true));

        XorExpression where = new XorExpression()
                .withLeftExpression(
                        new OrExpression()
                                .withLeftExpression(
                                        new AndExpression()
                                                .withLeftExpression(new Column("a"))
                                                .withRightExpression(new Column("b"))
                                )
                                .withRightExpression(new Column("c")))
                .withRightExpression(new Column("d")
                );

        Select select = new Select().withSelectBody(new PlainSelect().addSelectItems(new AllColumns()).withFromItem(t1)
                .withWhere(where));

        assertDeparse(select, statement);
        assertEqualsObjectTree(select, parsed);
    }

    @Test
    public void testParseAndBuildForXORs() throws JSQLParserException {
        String statement = "SELECT * FROM tab1 AS t1 WHERE " +
                "a XOR b XOR c";

        Statement parsed = TestUtils.assertSqlCanBeParsedAndDeparsed(statement);

        Table t1 = new Table("tab1").withAlias(new Alias("t1", true));

        XorExpression where = new XorExpression()
                .withLeftExpression(
                        new XorExpression()
                                .withLeftExpression(new Column("a"))
                                .withRightExpression(new Column("b")))
                .withRightExpression(new Column("c"));

        Select select = new Select().withSelectBody(new PlainSelect().addSelectItems(new AllColumns()).withFromItem(t1)
                .withWhere(where));

        assertDeparse(select, statement);
        assertEqualsObjectTree(select, parsed);
    }
}

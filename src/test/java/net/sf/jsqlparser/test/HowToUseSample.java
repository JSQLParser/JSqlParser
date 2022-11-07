/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2022 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.test;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("PMD")
public class HowToUseSample {
    /*
     SQL Text
      └─Statements: net.sf.jsqlparser.statement.select.Select
         └─selectBody: net.sf.jsqlparser.statement.select.PlainSelect
            ├─selectItems -> Collection<SelectExpressionItem>
            │  └─selectItems: net.sf.jsqlparser.statement.select.SelectExpressionItem
            │     └─LongValue: 1
            ├─Table: dual
            └─where: net.sf.jsqlparser.expression.operators.relational.EqualsTo
               ├─Column: a
               └─Column: b
     */

    @Test
    public void howToParseStatement() throws JSQLParserException {
        String sqlStr="select 1 from dual where a=b";

        Statement statement = CCJSqlParserUtil.parse(sqlStr);
        if (statement instanceof Select) {
            Select select = (Select) statement;
            PlainSelect plainSelect = (PlainSelect)  select.getSelectBody();

            SelectExpressionItem selectExpressionItem = (SelectExpressionItem) plainSelect.getSelectItems().get(0);
            Assertions.assertEquals( new LongValue(1), selectExpressionItem.getExpression());

            Table table = (Table) plainSelect.getFromItem();
            Assertions.assertEquals("dual", table.getName());

            EqualsTo equalsTo = (EqualsTo) plainSelect.getWhere();
            Column a = (Column) equalsTo.getLeftExpression();
            Column b = (Column) equalsTo.getRightExpression();
            Assertions.assertEquals("a", a.getColumnName());
            Assertions.assertEquals("b", b.getColumnName());
        }
    }

    @Test
    public void howToUseVisitors() throws JSQLParserException {

        // Define an Expression Visitor reacting on any Expression
        // Overwrite the visit() methods for each Expression Class
        ExpressionVisitorAdapter expressionVisitorAdapter = new ExpressionVisitorAdapter() {
            public void visit(EqualsTo equalsTo) {
                equalsTo.getLeftExpression().accept(this);
                equalsTo.getRightExpression().accept(this);
            }
            public void visit(Column column) {
                System.out.println("Found a Column " + column.getColumnName());
            }
        };

        // Define a Select Visitor reacting on a Plain Select invoking the Expression Visitor on the Where Clause
        SelectVisitorAdapter selectVisitorAdapter = new SelectVisitorAdapter() {
            @Override
            public void visit(PlainSelect plainSelect) {
                plainSelect.getWhere().accept(expressionVisitorAdapter);
            }
        };

        // Define a Statement Visitor for dispatching the Statements
        StatementVisitorAdapter statementVisitor = new StatementVisitorAdapter() {
            public void visit(Select select) {
                select.getSelectBody().accept(selectVisitorAdapter);
            }
        };

        String sqlStr="select 1 from dual where a=b";
        Statement stmt = CCJSqlParserUtil.parse(sqlStr);

        // Invoke the Statement Visitor
        stmt.accept(statementVisitor);
    }

    @Test
    public void howToUseFeatures() throws JSQLParserException {

        String sqlStr="select 1 from [sample_table] where [a]=[b]";

        // T-SQL Square Bracket Quotation
        Statement stmt = CCJSqlParserUtil.parse(
                sqlStr
                , parser -> parser
                                    .withSquareBracketQuotation(true)
        );

        // Set Parser Timeout to 6000 ms
        Statement stmt1 = CCJSqlParserUtil.parse(
                sqlStr
                , parser -> parser
                                    .withSquareBracketQuotation(true)
                                    .withTimeOut(6000)
        );

        // Allow Complex Parsing (which allows nested Expressions, but is much slower)
        Statement stmt2 = CCJSqlParserUtil.parse(
                sqlStr
                , parser -> parser
                                    .withSquareBracketQuotation(true)
                                    .withAllowComplexParsing(true)
                                    .withTimeOut(6000)
        );
    }
}

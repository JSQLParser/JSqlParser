/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.ExpressionListItem;
import net.sf.jsqlparser.statement.select.FunctionItem;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.Pivot;
import net.sf.jsqlparser.statement.select.PivotVisitor;
import net.sf.jsqlparser.statement.select.PivotXml;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.UnPivot;
import net.sf.jsqlparser.statement.select.WithItem;

public class ExpressionVisitorAdapter implements ExpressionVisitor, ItemsListVisitor, PivotVisitor, SelectItemVisitor {

    private SelectVisitor selectVisitor;

    public SelectVisitor getSelectVisitor() {
        return selectVisitor;
    }

    public void setSelectVisitor(SelectVisitor selectVisitor) {
        this.selectVisitor = selectVisitor;
    }

    @Override
    public void visit(NullValue value) {

    }

    @Override
    public void visit(Function function) {
        if (function.getParameters() != null) {
            function.getParameters().accept(this);
        }
        if (function.getKeep() != null) {
            function.getKeep().accept(this);
        }
    }

    @Override
    public void visit(SignedExpression expr) {
        expr.getExpression().accept(this);
    }

    @Override
    public void visit(JdbcParameter parameter) {

    }

    @Override
    public void visit(JdbcNamedParameter parameter) {

    }

    @Override
    public void visit(DoubleValue value) {

    }

    @Override
    public void visit(LongValue value) {

    }

    @Override
    public void visit(DateValue value) {

    }

    @Override
    public void visit(TimeValue value) {

    }

    @Override
    public void visit(TimestampValue value) {

    }

    @Override
    public void visit(Parenthesis parenthesis) {
        parenthesis.getExpression().accept(this);
    }

    @Override
    public void visit(StringValue value) {

    }

    @Override
    public void visit(Addition expr) {
        visitBinaryExpression(expr);
    }

    @Override
    public void visit(Division expr) {
        visitBinaryExpression(expr);
    }

    @Override
    public void visit(IntegerDivision expr) {
        visitBinaryExpression(expr);
    }

    @Override
    public void visit(Multiplication expr) {
        visitBinaryExpression(expr);
    }

    @Override
    public void visit(Subtraction expr) {
        visitBinaryExpression(expr);
    }

    @Override
    public void visit(AndExpression expr) {
        visitBinaryExpression(expr);
    }

    @Override
    public void visit(OrExpression expr) {
        visitBinaryExpression(expr);
    }

    @Override
    public void visit(Between expr) {
        expr.getLeftExpression().accept(this);
        expr.getBetweenExpressionStart().accept(this);
        expr.getBetweenExpressionEnd().accept(this);
    }

    @Override
    public void visit(EqualsTo expr) {
        visitBinaryExpression(expr);
    }

    @Override
    public void visit(GreaterThan expr) {
        visitBinaryExpression(expr);
    }

    @Override
    public void visit(GreaterThanEquals expr) {
        visitBinaryExpression(expr);
    }

    @Override
    public void visit(InExpression expr) {
        if (expr.getLeftExpression() != null) {
            expr.getLeftExpression().accept(this);
        } else if (expr.getLeftItemsList() != null) {
            expr.getLeftItemsList().accept(this);
        }
        if (expr.getRightExpression() != null) {
            expr.getRightExpression().accept(this);
        } else if (expr.getRightItemsList() != null) {
            expr.getRightItemsList().accept(this);
        } else {
            expr.getMultiExpressionList().accept(this);
        }
    }

    @Override
    public void visit(IsNullExpression expr) {
        expr.getLeftExpression().accept(this);
    }

    @Override
    public void visit(FullTextSearch expr) {
        for (Column col : expr.getMatchColumns()) {
            col.accept(this);
        }
    }

    @Override
    public void visit(IsBooleanExpression expr) {
        expr.getLeftExpression().accept(this);
    }

    @Override
    public void visit(LikeExpression expr) {
        visitBinaryExpression(expr);
    }

    @Override
    public void visit(MinorThan expr) {
        visitBinaryExpression(expr);
    }

    @Override
    public void visit(MinorThanEquals expr) {
        visitBinaryExpression(expr);
    }

    @Override
    public void visit(NotEqualsTo expr) {
        visitBinaryExpression(expr);
    }

    @Override
    public void visit(Column column) {

    }

    @Override
    public void visit(SubSelect subSelect) {
        if (selectVisitor != null) {
            if (subSelect.getWithItemsList() != null) {
                for (WithItem item : subSelect.getWithItemsList()) {
                    item.accept(selectVisitor);
                }
            }
            subSelect.getSelectBody().accept(selectVisitor);
        }
        if (subSelect.getPivot() != null) {
            subSelect.getPivot().accept(this);
        }
    }

    @Override
    public void visit(CaseExpression expr) {
        if (expr.getSwitchExpression() != null) {
            expr.getSwitchExpression().accept(this);
        }
        for (Expression x : expr.getWhenClauses()) {
            x.accept(this);
        }
        if (expr.getElseExpression() != null) {
            expr.getElseExpression().accept(this);
        }
    }

    @Override
    public void visit(WhenClause expr) {
        expr.getWhenExpression().accept(this);
        expr.getThenExpression().accept(this);
    }

    @Override
    public void visit(ExistsExpression expr) {
        expr.getRightExpression().accept(this);
    }

    @Override
    public void visit(AllComparisonExpression expr) {

    }

    @Override
    public void visit(AnyComparisonExpression expr) {

    }

    @Override
    public void visit(Concat expr) {
        visitBinaryExpression(expr);
    }

    @Override
    public void visit(Matches expr) {
        visitBinaryExpression(expr);
    }

    @Override
    public void visit(BitwiseAnd expr) {
        visitBinaryExpression(expr);
    }

    @Override
    public void visit(BitwiseOr expr) {
        visitBinaryExpression(expr);
    }

    @Override
    public void visit(BitwiseXor expr) {
        visitBinaryExpression(expr);
    }

    @Override
    public void visit(CastExpression expr) {
        expr.getLeftExpression().accept(this);
    }

    @Override
    public void visit(Modulo expr) {
        visitBinaryExpression(expr);
    }

    @Override
    public void visit(AnalyticExpression expr) {
        if (expr.getExpression() != null) {
            expr.getExpression().accept(this);
        }
        if (expr.getDefaultValue() != null) {
            expr.getDefaultValue().accept(this);
        }
        if (expr.getOffset() != null) {
            expr.getOffset().accept(this);
        }
        if (expr.getKeep() != null) {
            expr.getKeep().accept(this);
        }
        for (OrderByElement element : expr.getOrderByElements()) {
            element.getExpression().accept(this);
        }

        if (expr.getWindowElement() != null) {
            expr.getWindowElement().getRange().getStart().getExpression().accept(this);
            expr.getWindowElement().getRange().getEnd().getExpression().accept(this);
            expr.getWindowElement().getOffset().getExpression().accept(this);
        }
    }

    @Override
    public void visit(ExtractExpression expr) {
        expr.getExpression().accept(this);
    }

    @Override
    public void visit(IntervalExpression expr) {
    }

    @Override
    public void visit(OracleHierarchicalExpression expr) {
        expr.getConnectExpression().accept(this);
        expr.getStartExpression().accept(this);
    }

    @Override
    public void visit(RegExpMatchOperator expr) {
        visitBinaryExpression(expr);
    }

    @Override
    public void visit(ExpressionList expressionList) {
        for (Expression expr : expressionList.getExpressions()) {
            expr.accept(this);
        }
    }

    @Override
    public void visit(NamedExpressionList namedExpressionList) {
        for (Expression expr : namedExpressionList.getExpressions()) {
            expr.accept(this);
        }
    }

    @Override
    public void visit(MultiExpressionList multiExprList) {
        for (ExpressionList list : multiExprList.getExprList()) {
            visit(list);
        }
    }

    @Override
    public void visit(NotExpression notExpr) {
        notExpr.getExpression().accept(this);
    }

    @Override
    public void visit(BitwiseRightShift expr) {
        visitBinaryExpression(expr);
    }

    @Override
    public void visit(BitwiseLeftShift expr) {
        visitBinaryExpression(expr);
    }

    protected void visitBinaryExpression(BinaryExpression expr) {
        expr.getLeftExpression().accept(this);
        expr.getRightExpression().accept(this);
    }

    @Override
    public void visit(JsonExpression jsonExpr) {
        visit(jsonExpr.getColumn());
    }

    @Override
    public void visit(JsonOperator expr) {
        visitBinaryExpression(expr);
    }

    @Override
    public void visit(RegExpMySQLOperator expr) {
        visitBinaryExpression(expr);
    }

    @Override
    public void visit(UserVariable var) {

    }

    @Override
    public void visit(NumericBind bind) {

    }

    @Override
    public void visit(KeepExpression expr) {
        for (OrderByElement element : expr.getOrderByElements()) {
            element.getExpression().accept(this);
        }
    }

    @Override
    public void visit(MySQLGroupConcat groupConcat) {
        for (Expression expr : groupConcat.getExpressionList().getExpressions()) {
            expr.accept(this);
        }
        if (groupConcat.getOrderByElements() != null) {
            for (OrderByElement element : groupConcat.getOrderByElements()) {
                element.getExpression().accept(this);
            }
        }
    }

    @Override
    public void visit(ValueListExpression valueListExpression) {
        for (Expression expr : valueListExpression.getExpressionList().getExpressions()) {
            expr.accept(this);
        }
    }

    @Override
    public void visit(Pivot pivot) {
        for (FunctionItem item : pivot.getFunctionItems()) {
            item.getFunction().accept(this);
        }
        for (Column col : pivot.getForColumns()) {
            col.accept(this);
        }
        if (pivot.getSingleInItems() != null) {
            for (SelectExpressionItem item : pivot.getSingleInItems()) {
                item.accept(this);
            }
        }

        if (pivot.getMultiInItems() != null) {
            for (ExpressionListItem item : pivot.getMultiInItems()) {
                item.getExpressionList().accept(this);
            }
        }
    }

    @Override
    public void visit(PivotXml pivot) {
        for (FunctionItem item : pivot.getFunctionItems()) {
            item.getFunction().accept(this);
        }
        for (Column col : pivot.getForColumns()) {
            col.accept(this);
        }
        if (pivot.getInSelect() != null && selectVisitor != null) {
            pivot.getInSelect().accept(selectVisitor);
        }
    }

    @Override
    public void visit(UnPivot unpivot) {
        unpivot.accept(this);
    }

    @Override
    public void visit(AllColumns allColumns) {

    }

    @Override
    public void visit(AllTableColumns allTableColumns) {

    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        selectExpressionItem.getExpression().accept(this);
    }

    @Override
    public void visit(RowConstructor rowConstructor) {
        for (Expression expr : rowConstructor.getExprList().getExpressions()) {
            expr.accept(this);
        }
    }

    @Override
    public void visit(HexValue hexValue) {

    }

    @Override
    public void visit(OracleHint hint) {

    }

    @Override
    public void visit(TimeKeyExpression timeKeyExpression) {

    }

    @Override
    public void visit(DateTimeLiteralExpression literal) {
    }

    @Override
    public void visit(NextValExpression nextVal) {
    }

    @Override
    public void visit(CollateExpression col) {
        col.getLeftExpression().accept(this);
    }

    @Override
    public void visit(SimilarToExpression expr) {
        visitBinaryExpression(expr);
    }

    @Override
    public void visit(ArrayExpression array) {
        array.getObjExpression().accept(this);
        array.getIndexExpression().accept(this);
    }

    @Override
    public void visit(VariableAssignment var) {
        var.getVariable().accept(this);
        var.getExpression().accept(this);
    }
}

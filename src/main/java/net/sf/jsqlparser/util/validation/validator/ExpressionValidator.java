/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation.validator;

import java.util.List;

import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.ArrayExpression;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.CollateExpression;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExtractExpression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.HexValue;
import net.sf.jsqlparser.expression.IntervalExpression;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.JsonExpression;
import net.sf.jsqlparser.expression.KeepExpression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.MySQLGroupConcat;
import net.sf.jsqlparser.expression.NextValExpression;
import net.sf.jsqlparser.expression.NotExpression;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.NumericBind;
import net.sf.jsqlparser.expression.OracleHierarchicalExpression;
import net.sf.jsqlparser.expression.OracleHint;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.RowConstructor;
import net.sf.jsqlparser.expression.SignedExpression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeKeyExpression;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.UserVariable;
import net.sf.jsqlparser.expression.ValueListExpression;
import net.sf.jsqlparser.expression.VariableAssignment;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.WindowElement;
import net.sf.jsqlparser.expression.WindowOffset;
import net.sf.jsqlparser.expression.WindowRange;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseLeftShift;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseRightShift;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.IntegerDivision;
import net.sf.jsqlparser.expression.operators.arithmetic.Modulo;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.FullTextSearch;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsBooleanExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.JsonOperator;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.expression.operators.relational.OldOracleJoinBinaryExpression;
import net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperator;
import net.sf.jsqlparser.expression.operators.relational.RegExpMySQLOperator;
import net.sf.jsqlparser.expression.operators.relational.SimilarToExpression;
import net.sf.jsqlparser.expression.operators.relational.SupportsOldOracleJoinSyntax;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.util.validation.ValidationCapability;
import net.sf.jsqlparser.util.validation.metadata.NamedObject;

/**
 * @author gitmotte
 */
public class ExpressionValidator extends AbstractValidator<Expression> implements ExpressionVisitor {


    @Override
    public void visit(Addition addition) {
        visitBinaryExpression(addition, " + ");
    }

    @Override
    public void visit(AndExpression andExpression) {
        visitBinaryExpression(andExpression, andExpression.isUseOperator() ? " && " : " AND ");
    }

    @Override
    public void visit(Between between) {
        between.getLeftExpression().accept(this);
        between.getBetweenExpressionStart().accept(this);
        between.getBetweenExpressionEnd().accept(this);
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        visitOldOracleJoinBinaryExpression(equalsTo, " = ");
    }

    @Override
    public void visit(Division division) {
        visitBinaryExpression(division, " / ");
    }

    @Override
    public void visit(IntegerDivision division) {
        visitBinaryExpression(division, " DIV ");
    }

    @Override
    public void visit(DoubleValue doubleValue) {
        // nothing to validate
    }

    @Override
    public void visit(HexValue hexValue) {
        // nothing to validate
    }

    @Override
    public void visit(NotExpression notExpr) {
        notExpr.getExpression().accept(this);
    }

    @Override
    public void visit(BitwiseRightShift expr) {
        visitBinaryExpression(expr, " >> ");
    }

    @Override
    public void visit(BitwiseLeftShift expr) {
        visitBinaryExpression(expr, " << ");
    }

    public void visitOldOracleJoinBinaryExpression(OldOracleJoinBinaryExpression expression, String operator) {
        for (ValidationCapability c : getCapabilities()) {
            validateOptionalExpression(expression.getLeftExpression(), this);
            if (expression.getOldOracleJoinSyntax() != SupportsOldOracleJoinSyntax.NO_ORACLE_JOIN) {
                validateFeature(c, Feature.oracleOldJoinSyntax);
            }
            validateOptionalExpression(expression.getRightExpression(), this);
            if (expression.getOraclePriorPosition() != SupportsOldOracleJoinSyntax.NO_ORACLE_PRIOR) {
                validateFeature(c, Feature.oraclePriorPosition);
            }
        }
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        visitOldOracleJoinBinaryExpression(greaterThan, " > ");
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        visitOldOracleJoinBinaryExpression(greaterThanEquals, " >= ");

    }

    @Override
    public void visit(InExpression inExpression) {
        for (ValidationCapability c : getCapabilities()) {
            validateOptionalExpression(inExpression.getLeftExpression(), this);
            if (inExpression.getOldOracleJoinSyntax() != SupportsOldOracleJoinSyntax.NO_ORACLE_JOIN) {
                validateFeature(c, Feature.oracleOldJoinSyntax);
            }
        }

        validateOptionalMultiExpressionList(inExpression.getMultiExpressionList());
        validateOptionalExpression(inExpression.getRightExpression(), this);
        validateOptionalItemsList(inExpression.getRightItemsList());
    }

    /**
     * a multi-expression in clause: {@code ((a, b), (c, d))}
     */
    public void validateOptionalMultiExpressionList(MultiExpressionList multiExprList) {
        if (multiExprList != null) {
            multiExprList.getExpressionLists().stream().map(ExpressionList::getExpressions)
            .flatMap(List::stream).forEach(e -> e.accept(this));
        }
    }

    @Override
    public void visit(FullTextSearch fullTextSearch) {
        validateOptionalExpressions(fullTextSearch.getMatchColumns());
    }

    @Override
    public void visit(SignedExpression signedExpression) {
        signedExpression.getExpression().accept(this);
    }

    @Override
    public void visit(IsNullExpression isNullExpression) {
        isNullExpression.getLeftExpression().accept(this);
    }

    @Override
    public void visit(IsBooleanExpression isBooleanExpression) {
        isBooleanExpression.getLeftExpression().accept(this);
    }

    @Override
    public void visit(JdbcParameter jdbcParameter) {
        validateFeature(Feature.jdbcParameter);
    }

    @Override
    public void visit(LikeExpression likeExpression) {
        visitBinaryExpression(likeExpression,
                (likeExpression.isNot() ? " NOT" : "")
                + (likeExpression.isCaseInsensitive() ? " ILIKE " : " LIKE "));
    }

    @Override
    public void visit(ExistsExpression existsExpression) {
        existsExpression.getRightExpression().accept(this);
    }

    @Override
    public void visit(LongValue longValue) {
        // nothing to validate
    }

    @Override
    public void visit(MinorThan minorThan) {
        visitOldOracleJoinBinaryExpression(minorThan, " < ");

    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        visitOldOracleJoinBinaryExpression(minorThanEquals, " <= ");

    }

    @Override
    public void visit(Multiplication multiplication) {
        visitBinaryExpression(multiplication, " * ");

    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        visitOldOracleJoinBinaryExpression(notEqualsTo, " " + notEqualsTo.getStringExpression() + " ");
    }

    @Override
    public void visit(NullValue nullValue) {
        // nothing to validate
    }

    @Override
    public void visit(OrExpression orExpression) {
        visitBinaryExpression(orExpression, " OR ");

    }

    @Override
    public void visit(Parenthesis parenthesis) {
        parenthesis.getExpression().accept(this);
    }

    @Override
    public void visit(StringValue stringValue) {
        // nothing to validate
    }

    @Override
    public void visit(Subtraction subtraction) {
        visitBinaryExpression(subtraction, " - ");
    }

    protected void visitBinaryExpression(BinaryExpression binaryExpression, String operator) {
        binaryExpression.getLeftExpression().accept(this);
        binaryExpression.getRightExpression().accept(this);
    }

    @Override
    public void visit(SubSelect subSelect) {
        validateOptionalFromItem(subSelect);
    }

    @Override
    public void visit(Column tableColumn) {
        validateName(NamedObject.column, tableColumn.getFullyQualifiedName());
        validateOptionalFromItem(tableColumn.getTable());
    }

    @Override
    public void visit(Function function) {
        validateFeature(Feature.function);

        validateOptionalItemsList(function.getNamedParameters());
        validateOptionalItemsList(function.getParameters());
        validateOptionalExpression(function.getAttribute(), this);
        validateOptionalExpression(function.getKeep(), this);
    }

    @Override
    public void visit(DateValue dateValue) {
        // nothing to validate
    }

    @Override
    public void visit(TimestampValue timestampValue) {
        // nothing to validate
    }

    @Override
    public void visit(TimeValue timeValue) {
        // nothing to validate
    }

    @Override
    public void visit(CaseExpression caseExpression) {
        Expression switchExp = caseExpression.getSwitchExpression();
        if (switchExp != null) {
            switchExp.accept(this);
        }

        caseExpression.getWhenClauses().forEach(wc -> wc.accept(this));

        Expression elseExp = caseExpression.getElseExpression();
        if (elseExp != null) {
            elseExp.accept(this);
        }
    }

    @Override
    public void visit(WhenClause whenClause) {
        whenClause.getWhenExpression().accept(this);
        whenClause.getThenExpression().accept(this);
    }

    @Override
    public void visit(AllComparisonExpression allComparisonExpression) {
        allComparisonExpression.getSubSelect().accept(this);
    }

    @Override
    public void visit(AnyComparisonExpression anyComparisonExpression) {
        anyComparisonExpression.getSubSelect().accept(this);
    }

    @Override
    public void visit(Concat concat) {
        visitBinaryExpression(concat, " || ");
    }

    @Override
    public void visit(Matches matches) {
        visitOldOracleJoinBinaryExpression(matches, " @@ ");
    }

    @Override
    public void visit(BitwiseAnd bitwiseAnd) {
        visitBinaryExpression(bitwiseAnd, " & ");
    }

    @Override
    public void visit(BitwiseOr bitwiseOr) {
        visitBinaryExpression(bitwiseOr, " | ");
    }

    @Override
    public void visit(BitwiseXor bitwiseXor) {
        visitBinaryExpression(bitwiseXor, " ^ ");
    }

    @Override
    public void visit(CastExpression cast) {
        cast.getLeftExpression().accept(this);
    }

    @Override
    public void visit(Modulo modulo) {
        visitBinaryExpression(modulo, " % ");
    }

    @Override
    public void visit(AnalyticExpression aexpr) {
        validateOptionalExpression(aexpr.getExpression(), this);
        validateOptionalExpression(aexpr.getOffset(), this);
        validateOptionalExpression(aexpr.getDefaultValue(), this);
        validateOptionalExpression(aexpr.getKeep(), this);
        validateOptionalExpressionList(aexpr.getPartitionExpressionList());
        validateOptionalOrderByElements(aexpr.getOrderByElements());
        WindowElement windowElement = aexpr.getWindowElement();
        if (windowElement != null) {
            validateOptionalWindowOffset(windowElement.getOffset());
            WindowRange range = windowElement.getRange();
            if (range != null) {
                validateOptionalWindowOffset(range.getStart());
                validateOptionalWindowOffset(range.getEnd());
            }
        }
        validateOptionalExpression(aexpr.getFilterExpression());
    }

    private void validateOptionalWindowOffset(WindowOffset offset) {
        if (offset != null) {
            validateOptionalExpression(offset.getExpression());
        }
    }

    @Override
    public void visit(ExtractExpression eexpr) {
        eexpr.getExpression().accept(this);
    }

    @Override
    public void visit(IntervalExpression iexpr) {
        validateOptionalExpression(iexpr.getExpression());
    }

    @Override
    public void visit(JdbcNamedParameter jdbcNamedParameter) {
        validateFeature(Feature.jdbcNamedParameter);
    }

    @Override
    public void visit(OracleHierarchicalExpression oexpr) {
        validateFeature(Feature.oracleHierarchicalExpression);
    }

    @Override
    public void visit(RegExpMatchOperator rexpr) {
        visitBinaryExpression(rexpr, " " + rexpr.getStringExpression() + " ");
    }

    @Override
    public void visit(RegExpMySQLOperator rexpr) {
        visitBinaryExpression(rexpr, " " + rexpr.getStringExpression() + " ");
    }

    @Override
    public void visit(JsonExpression jsonExpr) {
        validateOptionalExpression(jsonExpr.getColumn());
    }

    @Override
    public void visit(JsonOperator jsonExpr) {
        visitBinaryExpression(jsonExpr, " " + jsonExpr.getStringExpression() + " ");
    }

    @Override
    public void visit(UserVariable var) {
        // nothing to validate
    }

    @Override
    public void visit(NumericBind bind) {
        // nothing to validate
    }

    @Override
    public void visit(KeepExpression aexpr) {
        validateOptionalOrderByElements(aexpr.getOrderByElements());
    }

    @Override
    public void visit(MySQLGroupConcat groupConcat) {
        validateOptionalExpressionList(groupConcat.getExpressionList());
        validateOptionalOrderByElements(groupConcat.getOrderByElements());
    }

    private void validateOptionalExpressionList(ExpressionList expressionList) {
        if (expressionList != null) {
            expressionList.accept(getValidator(ItemsListValidator.class));
        }
    }

    @Override
    public void visit(ValueListExpression valueList) {
        validateOptionalExpressionList(valueList.getExpressionList());
    }

    @Override
    public void visit(RowConstructor rowConstructor) {
        validateOptionalExpressionList(rowConstructor.getExprList());
    }

    @Override
    public void visit(OracleHint hint) {
        // nothing to validate
    }

    @Override
    public void visit(TimeKeyExpression timeKeyExpression) {
        // nothing to validate
    }
    @Override
    public void visit(DateTimeLiteralExpression literal) {
        // nothing to validate
    }

    @Override
    public void visit(NextValExpression nextVal) {
        validateName(NamedObject.sequence, nextVal.getName());
    }

    @Override
    public void visit(CollateExpression col) {
        validateOptionalExpression(col.getLeftExpression());
    }

    @Override
    public void visit(SimilarToExpression expr) {
        visitBinaryExpression(expr, (expr.isNot() ? " NOT" : "") + " SIMILAR TO ");
    }

    @Override
    public void visit(ArrayExpression array) {
        array.getObjExpression().accept(this);
        array.getIndexExpression().accept(this);
    }

    @Override
    public void validate(Expression expression) {
        expression.accept(this);
    }

    @Override
    public void visit(VariableAssignment a) {
        validateOptionalExpression(a.getExpression());
        if (a.getVariable() != null) {
            a.getVariable().accept(this);
        }
    }

}

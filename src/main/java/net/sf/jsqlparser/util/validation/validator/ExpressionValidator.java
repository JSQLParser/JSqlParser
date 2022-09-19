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

import net.sf.jsqlparser.expression.*;
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
import net.sf.jsqlparser.expression.operators.conditional.XorExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.FullTextSearch;
import net.sf.jsqlparser.expression.operators.relational.GeometryDistance;
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
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.expression.operators.relational.OldOracleJoinBinaryExpression;
import net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperator;
import net.sf.jsqlparser.expression.operators.relational.RegExpMySQLOperator;
import net.sf.jsqlparser.expression.operators.relational.SimilarToExpression;
import net.sf.jsqlparser.expression.operators.relational.IsDistinctExpression;
import net.sf.jsqlparser.expression.operators.relational.SupportsOldOracleJoinSyntax;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.util.validation.ValidationCapability;
import net.sf.jsqlparser.util.validation.metadata.NamedObject;

/**
 * @author gitmotte
 */
@SuppressWarnings({"PMD.CyclomaticComplexity"})
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
    public void visit(OverlapsCondition overlapsCondition) {
        validateOptionalExpressionList(overlapsCondition.getLeft());
        validateOptionalExpressionList(overlapsCondition.getRight());
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
        validateOptionalExpression(inExpression.getRightExpression(), this);
        validateOptionalItemsList(inExpression.getRightItemsList());
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
        validateFeature(Feature.exprLike);
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
    public void visit(XorExpression xorExpression) {
        visitBinaryExpression(xorExpression, " XOR ");

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
    }

    @Override
    public void visit(Function function) {
        validateFeature(Feature.function);

        validateOptionalItemsList(function.getNamedParameters());
        validateOptionalItemsList(function.getParameters());
        validateOptionalExpression(function.getAttribute(), this);
        validateOptionalExpression(function.getKeep(), this);
        validateOptionalOrderByElements(function.getOrderByElements());
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
    public void visit(TryCastExpression cast) {
        cast.getLeftExpression().accept(this);
    }

    @Override
    public void visit(SafeCastExpression cast) {
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
        validateOptionalExpression(jsonExpr.getExpression());
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
        if (rowConstructor.getColumnDefinitions().isEmpty()) {
            validateOptionalExpressionList(rowConstructor.getExprList());
        } else {
            for (ColumnDefinition columnDefinition: rowConstructor.getColumnDefinitions()) {
                validateName(NamedObject.column, columnDefinition.getColumnName());
            }
        }
    }

    @Override
    public void visit(RowGetExpression rowGetExpression) {
        rowGetExpression.getExpression().accept(this);
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
        validateFeature(Feature.exprSimilarTo);
        visitBinaryExpression(expr, (expr.isNot() ? " NOT" : "") + " SIMILAR TO ");
    }

    @Override
    public void visit(ArrayExpression array) {
        array.getObjExpression().accept(this);
        if (array.getIndexExpression() != null) {
            array.getIndexExpression().accept(this);
        }
        if (array.getStartIndexExpression() != null) {
            array.getStartIndexExpression().accept(this);
        }
        if (array.getStopIndexExpression() != null) {
            array.getStopIndexExpression().accept(this);
        }
    }

    @Override
    public void visit(ArrayConstructor aThis) {
        for (Expression expression : aThis.getExpressions()) {
            expression.accept(this);
        }
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

    @Override
    public void visit(TimezoneExpression a) {
        validateOptionalExpression(a.getLeftExpression());
    }

    @Override
    public void visit(XMLSerializeExpr xml) {
        // TODO this feature seams very close to a jsqlparser-user usecase
    }

    @Override
    public void visit(JsonAggregateFunction expression) {
        // no idea what this is good for
    }

    @Override
    public void visit(JsonFunction expression) {
        // no idea what this is good for
    }

    @Override
    public void visit(ConnectByRootOperator connectByRootOperator) {
        connectByRootOperator.getColumn().accept(this);
    }
    
    @Override
    public void visit(OracleNamedFunctionParameter oracleNamedFunctionParameter) {
        oracleNamedFunctionParameter.getExpression().accept(this);
    }

    @Override
    public void visit(AllColumns allColumns) {
    }

    @Override
    public void visit(AllTableColumns allTableColumns) {
    }

    @Override
    public void visit(AllValue allValue) {

    }

    @Override
    public void visit(IsDistinctExpression isDistinctExpression) {
        isDistinctExpression.getLeftExpression().accept(this);
        isDistinctExpression.getRightExpression().accept(this);
    }

    @Override
    public void visit(GeometryDistance geometryDistance) {
        visitOldOracleJoinBinaryExpression(geometryDistance, " <-> ");
    }
}

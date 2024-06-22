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

import net.sf.jsqlparser.expression.AllValue;
import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.ArrayConstructor;
import net.sf.jsqlparser.expression.ArrayExpression;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.CollateExpression;
import net.sf.jsqlparser.expression.ConnectByRootOperator;
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
import net.sf.jsqlparser.expression.JsonAggregateFunction;
import net.sf.jsqlparser.expression.JsonExpression;
import net.sf.jsqlparser.expression.JsonFunction;
import net.sf.jsqlparser.expression.KeepExpression;
import net.sf.jsqlparser.expression.LambdaExpression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.MySQLGroupConcat;
import net.sf.jsqlparser.expression.NextValExpression;
import net.sf.jsqlparser.expression.NotExpression;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.NumericBind;
import net.sf.jsqlparser.expression.OracleHierarchicalExpression;
import net.sf.jsqlparser.expression.OracleHint;
import net.sf.jsqlparser.expression.OracleNamedFunctionParameter;
import net.sf.jsqlparser.expression.OverlapsCondition;
import net.sf.jsqlparser.expression.RangeExpression;
import net.sf.jsqlparser.expression.RowConstructor;
import net.sf.jsqlparser.expression.RowGetExpression;
import net.sf.jsqlparser.expression.SignedExpression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.StructType;
import net.sf.jsqlparser.expression.TimeKeyExpression;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.TimezoneExpression;
import net.sf.jsqlparser.expression.TranscodingFunction;
import net.sf.jsqlparser.expression.TrimFunction;
import net.sf.jsqlparser.expression.UserVariable;
import net.sf.jsqlparser.expression.VariableAssignment;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.WindowElement;
import net.sf.jsqlparser.expression.WindowOffset;
import net.sf.jsqlparser.expression.WindowRange;
import net.sf.jsqlparser.expression.XMLSerializeExpr;
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
import net.sf.jsqlparser.expression.operators.relational.ContainedBy;
import net.sf.jsqlparser.expression.operators.relational.Contains;
import net.sf.jsqlparser.expression.operators.relational.DoubleAnd;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExcludesExpression;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.FullTextSearch;
import net.sf.jsqlparser.expression.operators.relational.GeometryDistance;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IncludesExpression;
import net.sf.jsqlparser.expression.operators.relational.IsBooleanExpression;
import net.sf.jsqlparser.expression.operators.relational.IsDistinctExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.JsonOperator;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MemberOfExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.expression.operators.relational.OldOracleJoinBinaryExpression;
import net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperator;
import net.sf.jsqlparser.expression.operators.relational.SimilarToExpression;
import net.sf.jsqlparser.expression.operators.relational.SupportsOldOracleJoinSyntax;
import net.sf.jsqlparser.expression.operators.relational.TSQLLeftJoin;
import net.sf.jsqlparser.expression.operators.relational.TSQLRightJoin;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.util.validation.ValidationCapability;
import net.sf.jsqlparser.util.validation.metadata.NamedObject;

/**
 * @author gitmotte
 */
@SuppressWarnings({"PMD.CyclomaticComplexity"})
public class ExpressionValidator extends AbstractValidator<Expression>
        implements ExpressionVisitor<Void> {
    @Override
    public <S> Void visit(Addition addition, S parameters) {
        visitBinaryExpression(addition, " + ");
        return null;
    }

    @Override
    public <S> Void visit(AndExpression andExpression, S parameters) {
        visitBinaryExpression(andExpression, andExpression.isUseOperator() ? " && " : " AND ");
        return null;
    }

    @Override
    public <S> Void visit(Between between, S parameters) {
        between.getLeftExpression().accept(this, parameters);
        between.getBetweenExpressionStart().accept(this, parameters);
        between.getBetweenExpressionEnd().accept(this, parameters);
        return null;
    }

    @Override
    public <S> Void visit(OverlapsCondition overlapsCondition, S parameters) {
        validateOptionalExpressionList(overlapsCondition.getLeft());
        validateOptionalExpressionList(overlapsCondition.getRight());
        return null;
    }


    @Override
    public <S> Void visit(EqualsTo equalsTo, S parameters) {
        visitOldOracleJoinBinaryExpression(equalsTo, " = ", parameters);
        return null;
    }

    @Override
    public <S> Void visit(Division division, S parameters) {
        visitBinaryExpression(division, " / ");
        return null;
    }

    @Override
    public <S> Void visit(IntegerDivision division, S parameters) {
        visitBinaryExpression(division, " DIV ");
        return null;
    }

    @Override
    public <S> Void visit(DoubleValue doubleValue, S parameters) {
        // nothing to validate
        return null;
    }

    @Override
    public <S> Void visit(HexValue hexValue, S parameters) {
        // nothing to validate
        return null;
    }

    @Override
    public <S> Void visit(NotExpression notExpr, S parameters) {
        notExpr.getExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> Void visit(BitwiseRightShift expr, S parameters) {
        visitBinaryExpression(expr, " >> ");
        return null;
    }

    @Override
    public <S> Void visit(BitwiseLeftShift expr, S parameters) {
        visitBinaryExpression(expr, " << ");
        return null;
    }

    public <S> Void visitOldOracleJoinBinaryExpression(OldOracleJoinBinaryExpression expression,
            String operator, S parameters) {
        for (ValidationCapability c : getCapabilities()) {
            validateOptionalExpression(expression.getLeftExpression(), this);
            if (expression.getOldOracleJoinSyntax() != SupportsOldOracleJoinSyntax.NO_ORACLE_JOIN) {
                validateFeature(c, Feature.oracleOldJoinSyntax);
            }
            validateOptionalExpression(expression.getRightExpression(), this);
            if (expression
                    .getOraclePriorPosition() != SupportsOldOracleJoinSyntax.NO_ORACLE_PRIOR) {
                validateFeature(c, Feature.oraclePriorPosition);
            }
        }
        return null;
    }

    @Override
    public <S> Void visit(GreaterThan greaterThan, S parameters) {
        visitOldOracleJoinBinaryExpression(greaterThan, " > ", parameters);
        return null;
    }

    @Override
    public <S> Void visit(GreaterThanEquals greaterThanEquals, S parameters) {
        visitOldOracleJoinBinaryExpression(greaterThanEquals, " >= ", parameters);

        return null;
    }

    @Override
    public <S> Void visit(InExpression inExpression, S parameters) {
        for (ValidationCapability c : getCapabilities()) {
            validateOptionalExpression(inExpression.getLeftExpression(), this);
            if (inExpression
                    .getOldOracleJoinSyntax() != SupportsOldOracleJoinSyntax.NO_ORACLE_JOIN) {
                validateFeature(c, Feature.oracleOldJoinSyntax);
            }
        }
        validateOptionalExpression(inExpression.getRightExpression(), this);
        return null;
    }

    @Override
    public <S> Void visit(IncludesExpression includesExpression, S parameters) {
        validateOptionalExpression(includesExpression.getLeftExpression(), this);
        validateOptionalExpression(includesExpression.getRightExpression(), this);
        return null;
    }

    @Override
    public <S> Void visit(ExcludesExpression excludesExpression, S parameters) {
        validateOptionalExpression(excludesExpression.getLeftExpression(), this);
        validateOptionalExpression(excludesExpression.getRightExpression(), this);
        return null;
    }

    @Override
    public <S> Void visit(FullTextSearch fullTextSearch, S parameters) {
        validateOptionalExpressions(fullTextSearch.getMatchColumns());
        return null;
    }

    @Override
    public <S> Void visit(SignedExpression signedExpression, S parameters) {
        signedExpression.getExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> Void visit(IsNullExpression isNullExpression, S parameters) {
        isNullExpression.getLeftExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> Void visit(IsBooleanExpression isBooleanExpression, S parameters) {
        isBooleanExpression.getLeftExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> Void visit(JdbcParameter jdbcParameter, S parameters) {
        validateFeature(Feature.jdbcParameter);
        return null;
    }

    @Override
    public <S> Void visit(LikeExpression likeExpression, S parameters) {
        validateFeature(Feature.exprLike);
        visitBinaryExpression(likeExpression, (likeExpression.isNot() ? " NOT" : "")
                + (likeExpression.isCaseInsensitive() ? " ILIKE " : " LIKE "));
        return null;
    }

    @Override
    public <S> Void visit(ExistsExpression existsExpression, S parameters) {
        existsExpression.getRightExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> Void visit(MemberOfExpression memberOfExpression, S parameters) {
        memberOfExpression.getLeftExpression().accept(this, parameters);
        memberOfExpression.getRightExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> Void visit(LongValue longValue, S parameters) {
        // nothing to validate
        return null;
    }

    @Override
    public <S> Void visit(MinorThan minorThan, S parameters) {
        visitOldOracleJoinBinaryExpression(minorThan, " < ", parameters);

        return null;
    }

    @Override
    public <S> Void visit(MinorThanEquals minorThanEquals, S parameters) {
        visitOldOracleJoinBinaryExpression(minorThanEquals, " <= ", parameters);

        return null;
    }

    @Override
    public <S> Void visit(Multiplication multiplication, S parameters) {
        visitBinaryExpression(multiplication, " * ");

        return null;
    }

    @Override
    public <S> Void visit(NotEqualsTo notEqualsTo, S parameters) {
        visitOldOracleJoinBinaryExpression(notEqualsTo,
                " " + notEqualsTo.getStringExpression() + " ", parameters);
        return null;
    }

    @Override
    public <S> Void visit(DoubleAnd doubleAnd, S parameters) {

        return null;
    }

    @Override
    public <S> Void visit(Contains contains, S parameters) {

        return null;
    }

    @Override
    public <S> Void visit(ContainedBy containedBy, S parameters) {

        return null;
    }

    @Override
    public <S> Void visit(NullValue nullValue, S parameters) {
        // nothing to validate
        return null;
    }

    @Override
    public <S> Void visit(OrExpression orExpression, S parameters) {
        visitBinaryExpression(orExpression, " OR ");

        return null;
    }

    @Override
    public <S> Void visit(XorExpression xorExpression, S parameters) {
        visitBinaryExpression(xorExpression, " XOR ");

        return null;
    }

    @Override
    public <S> Void visit(StringValue stringValue, S parameters) {
        // nothing to validate
        return null;
    }

    @Override
    public <S> Void visit(Subtraction subtraction, S parameters) {
        visitBinaryExpression(subtraction, " - ");
        return null;
    }

    protected void visitBinaryExpression(BinaryExpression binaryExpression, String operator) {
        binaryExpression.getLeftExpression().accept(this, null);
        binaryExpression.getRightExpression().accept(this, null);
    }

    @Override
    public <S> Void visit(ParenthesedSelect selectBody, S parameters) {
        validateOptionalFromItem(selectBody);
        return null;
    }

    @Override
    public <S> Void visit(Column tableColumn, S parameters) {
        validateName(NamedObject.column, tableColumn.getFullyQualifiedName());
        return null;
    }

    @Override
    public <S> Void visit(Function function, S parameters) {
        validateFeature(Feature.function);

        validateOptionalExpressionList(function.getNamedParameters());
        validateOptionalExpressionList(function.getParameters());

        Object attribute = function.getAttribute();
        if (attribute instanceof Expression) {
            validateOptionalExpression((Expression) attribute, this);
        }

        validateOptionalExpression(function.getKeep(), this);
        validateOptionalOrderByElements(function.getOrderByElements());
        return null;
    }

    @Override
    public <S> Void visit(DateValue dateValue, S parameters) {
        // nothing to validate
        return null;
    }

    @Override
    public <S> Void visit(TimestampValue timestampValue, S parameters) {
        // nothing to validate
        return null;
    }

    @Override
    public <S> Void visit(TimeValue timeValue, S parameters) {
        // nothing to validate
        return null;
    }

    @Override
    public <S> Void visit(CaseExpression caseExpression, S parameters) {
        Expression switchExp = caseExpression.getSwitchExpression();
        if (switchExp != null) {
            switchExp.accept(this, parameters);
        }

        caseExpression.getWhenClauses().forEach(wc -> wc.accept(this, parameters));

        Expression elseExp = caseExpression.getElseExpression();
        if (elseExp != null) {
            elseExp.accept(this, parameters);
        }
        return null;
    }

    @Override
    public <S> Void visit(WhenClause whenClause, S parameters) {
        whenClause.getWhenExpression().accept(this, parameters);
        whenClause.getThenExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> Void visit(AnyComparisonExpression anyComparisonExpression, S parameters) {
        anyComparisonExpression.getSelect().accept(this, parameters);
        return null;
    }

    @Override
    public <S> Void visit(Concat concat, S parameters) {
        visitBinaryExpression(concat, " || ");
        return null;
    }

    @Override
    public <S> Void visit(Matches matches, S parameters) {
        visitOldOracleJoinBinaryExpression(matches, " @@ ", parameters);
        return null;
    }

    @Override
    public <S> Void visit(BitwiseAnd bitwiseAnd, S parameters) {
        visitBinaryExpression(bitwiseAnd, " & ");
        return null;
    }

    @Override
    public <S> Void visit(BitwiseOr bitwiseOr, S parameters) {
        visitBinaryExpression(bitwiseOr, " | ");
        return null;
    }

    @Override
    public <S> Void visit(BitwiseXor bitwiseXor, S parameters) {
        visitBinaryExpression(bitwiseXor, " ^ ");
        return null;
    }

    @Override
    public <S> Void visit(CastExpression cast, S parameters) {
        cast.getLeftExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> Void visit(Modulo modulo, S parameters) {
        visitBinaryExpression(modulo, " % ");
        return null;
    }

    @Override
    public <S> Void visit(AnalyticExpression aexpr, S parameters) {
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
        return null;
    }

    private void validateOptionalWindowOffset(WindowOffset offset) {
        if (offset != null) {
            validateOptionalExpression(offset.getExpression());
        }
    }

    @Override
    public <S> Void visit(ExtractExpression eexpr, S parameters) {
        eexpr.getExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> Void visit(IntervalExpression iexpr, S parameters) {
        validateOptionalExpression(iexpr.getExpression());
        return null;
    }

    @Override
    public <S> Void visit(JdbcNamedParameter jdbcNamedParameter, S parameters) {
        validateFeature(Feature.jdbcNamedParameter);
        return null;
    }

    @Override
    public <S> Void visit(OracleHierarchicalExpression oexpr, S parameters) {
        validateFeature(Feature.oracleHierarchicalExpression);
        return null;
    }

    @Override
    public <S> Void visit(RegExpMatchOperator rexpr, S parameters) {
        visitBinaryExpression(rexpr, " " + rexpr.getStringExpression() + " ");
        return null;
    }

    @Override
    public <S> Void visit(JsonExpression jsonExpr, S parameters) {
        validateOptionalExpression(jsonExpr.getExpression());
        return null;
    }

    @Override
    public <S> Void visit(JsonOperator jsonExpr, S parameters) {
        visitBinaryExpression(jsonExpr, " " + jsonExpr.getStringExpression() + " ");
        return null;
    }

    @Override
    public <S> Void visit(UserVariable var, S parameters) {
        // nothing to validate
        return null;
    }

    @Override
    public <S> Void visit(NumericBind bind, S parameters) {
        // nothing to validate
        return null;
    }

    @Override
    public <S> Void visit(KeepExpression aexpr, S parameters) {
        validateOptionalOrderByElements(aexpr.getOrderByElements());
        return null;
    }

    @Override
    public <S> Void visit(MySQLGroupConcat groupConcat, S parameters) {
        validateOptionalExpressionList(groupConcat.getExpressionList());
        validateOptionalOrderByElements(groupConcat.getOrderByElements());
        return null;
    }

    private void validateOptionalExpressionList(ExpressionList<?> expressionList) {
        if (expressionList != null) {
            for (Expression expression : expressionList) {
                expression.accept(this, null);
            }
        }
    }

    @Override
    public <S> Void visit(ExpressionList<?> expressionList, S parameters) {
        validateOptionalExpressionList(expressionList);
        return null;
    }

    @Override
    public <S> Void visit(RowConstructor<?> rowConstructor, S parameters) {
        validateOptionalExpressionList(rowConstructor);
        return null;
    }

    @Override
    public <S> Void visit(RowGetExpression rowGetExpression, S parameters) {
        rowGetExpression.getExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> Void visit(OracleHint hint, S parameters) {
        // nothing to validate
        return null;
    }

    @Override
    public <S> Void visit(TimeKeyExpression timeKeyExpression, S parameters) {
        // nothing to validate
        return null;
    }

    @Override
    public <S> Void visit(DateTimeLiteralExpression literal, S parameters) {
        // nothing to validate
        return null;
    }

    @Override
    public <S> Void visit(NextValExpression nextVal, S parameters) {
        validateName(NamedObject.sequence, nextVal.getName());
        return null;
    }

    @Override
    public <S> Void visit(CollateExpression col, S parameters) {
        validateOptionalExpression(col.getLeftExpression());
        return null;
    }

    @Override
    public <S> Void visit(SimilarToExpression expr, S parameters) {
        validateFeature(Feature.exprSimilarTo);
        visitBinaryExpression(expr, (expr.isNot() ? " NOT" : "") + " SIMILAR TO ");
        return null;
    }

    @Override
    public <S> Void visit(ArrayExpression array, S parameters) {
        array.getObjExpression().accept(this, parameters);
        if (array.getIndexExpression() != null) {
            array.getIndexExpression().accept(this, parameters);
        }
        if (array.getStartIndexExpression() != null) {
            array.getStartIndexExpression().accept(this, parameters);
        }
        if (array.getStopIndexExpression() != null) {
            array.getStopIndexExpression().accept(this, parameters);
        }
        return null;
    }

    @Override
    public <S> Void visit(ArrayConstructor aThis, S parameters) {
        for (Expression expression : aThis.getExpressions()) {
            expression.accept(this, parameters);
        }
        return null;
    }

    @Override
    public void validate(Expression expression) {
        expression.accept(this, null);
    }

    @Override
    public <S> Void visit(VariableAssignment a, S parameters) {
        validateOptionalExpression(a.getExpression());
        if (a.getVariable() != null) {
            a.getVariable().accept(this, parameters);
        }
        return null;
    }

    @Override
    public <S> Void visit(TimezoneExpression a, S parameters) {
        validateOptionalExpression(a.getLeftExpression());
        return null;
    }

    @Override
    public <S> Void visit(XMLSerializeExpr xml, S parameters) {
        return null;
    }

    @Override
    public <S> Void visit(JsonAggregateFunction expression, S parameters) {
        // no idea what this is good for
        return null;
    }

    @Override
    public <S> Void visit(JsonFunction expression, S parameters) {
        // no idea what this is good for
        return null;
    }

    @Override
    public <S> Void visit(ConnectByRootOperator connectByRootOperator, S parameters) {
        connectByRootOperator.getColumn().accept(this, parameters);
        return null;
    }

    @Override
    public <S> Void visit(OracleNamedFunctionParameter oracleNamedFunctionParameter, S parameters) {
        oracleNamedFunctionParameter.getExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> Void visit(AllColumns allColumns, S parameters) {
        return null;
    }

    @Override
    public <S> Void visit(AllTableColumns allTableColumns, S parameters) {
        return null;
    }

    @Override
    public <S> Void visit(AllValue allValue, S parameters) {
        return null;
    }

    @Override
    public <S> Void visit(IsDistinctExpression isDistinctExpression, S parameters) {
        isDistinctExpression.getLeftExpression().accept(this, parameters);
        isDistinctExpression.getRightExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> Void visit(GeometryDistance geometryDistance, S parameters) {
        visitOldOracleJoinBinaryExpression(geometryDistance, " <-> ", parameters);
        return null;
    }

    @Override
    public <S> Void visit(Select select, S parameters) {
        return null;
    }

    @Override
    public <S> Void visit(TranscodingFunction transcodingFunction, S parameters) {
        transcodingFunction.getExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> Void visit(TrimFunction trimFunction, S parameters) {
        if (trimFunction.getExpression() != null) {
            trimFunction.getExpression().accept(this, parameters);
        }
        if (trimFunction.getFromExpression() != null) {
            trimFunction.getFromExpression().accept(this, parameters);
        }
        return null;
    }

    @Override
    public <S> Void visit(RangeExpression rangeExpression, S parameters) {
        rangeExpression.getStartExpression().accept(this, parameters);
        rangeExpression.getEndExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> Void visit(TSQLLeftJoin tsqlLeftJoin, S parameters) {
        tsqlLeftJoin.getLeftExpression().accept(this, parameters);
        tsqlLeftJoin.getRightExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> Void visit(TSQLRightJoin tsqlRightJoin, S parameters) {
        tsqlRightJoin.getLeftExpression().accept(this, parameters);
        tsqlRightJoin.getRightExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> Void visit(StructType structType, S parameters) {
        if (structType.getArguments() != null) {
            for (SelectItem<?> selectItem : structType.getArguments()) {
                selectItem.getExpression().accept(this, parameters);
            }
        }
        return null;
    }

    @Override
    public <S> Void visit(LambdaExpression lambdaExpression, S parameters) {
        lambdaExpression.getExpression().accept(this, parameters);
        return null;
    }
}

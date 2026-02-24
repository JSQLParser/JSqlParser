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
import net.sf.jsqlparser.expression.BooleanValue;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.CollateExpression;
import net.sf.jsqlparser.expression.ConnectByRootOperator;
import net.sf.jsqlparser.expression.ConnectByPriorOperator;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import net.sf.jsqlparser.expression.DateUnitExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExtractExpression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.HexValue;
import net.sf.jsqlparser.expression.HighExpression;
import net.sf.jsqlparser.expression.IntervalExpression;
import net.sf.jsqlparser.expression.Inverse;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.JsonAggregateFunction;
import net.sf.jsqlparser.expression.JsonExpression;
import net.sf.jsqlparser.expression.JsonFunction;
import net.sf.jsqlparser.expression.KeepExpression;
import net.sf.jsqlparser.expression.LambdaExpression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.LowExpression;
import net.sf.jsqlparser.expression.MySQLGroupConcat;
import net.sf.jsqlparser.expression.NextValExpression;
import net.sf.jsqlparser.expression.NotExpression;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.NumericBind;
import net.sf.jsqlparser.expression.OracleHierarchicalExpression;
import net.sf.jsqlparser.expression.OracleHint;
import net.sf.jsqlparser.expression.OracleNamedFunctionParameter;
import net.sf.jsqlparser.expression.OverlapsCondition;
import net.sf.jsqlparser.expression.PostgresNamedFunctionParameter;
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
import net.sf.jsqlparser.expression.operators.relational.CosineSimilarity;
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
import net.sf.jsqlparser.expression.operators.relational.IsUnknownExpression;
import net.sf.jsqlparser.expression.operators.relational.JsonOperator;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MemberOfExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.expression.operators.relational.OldOracleJoinBinaryExpression;
import net.sf.jsqlparser.expression.operators.relational.Plus;
import net.sf.jsqlparser.expression.operators.relational.PriorTo;
import net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperator;
import net.sf.jsqlparser.expression.operators.relational.SimilarToExpression;
import net.sf.jsqlparser.expression.operators.relational.SupportsOldOracleJoinSyntax;
import net.sf.jsqlparser.expression.operators.relational.TSQLLeftJoin;
import net.sf.jsqlparser.expression.operators.relational.TSQLRightJoin;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.piped.FromQuery;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.FunctionAllColumns;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
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
    public <S> Void visit(Addition addition, S context) {
        visitBinaryExpression(addition, " + ");
        return null;
    }

    @Override
    public <S> Void visit(AndExpression andExpression, S context) {
        visitBinaryExpression(andExpression, andExpression.isUseOperator() ? " && " : " AND ");
        return null;
    }

    @Override
    public <S> Void visit(Between between, S context) {
        between.getLeftExpression().accept(this, context);
        between.getBetweenExpressionStart().accept(this, context);
        between.getBetweenExpressionEnd().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(OverlapsCondition overlapsCondition, S context) {
        validateOptionalExpressionList(overlapsCondition.getLeft());
        validateOptionalExpressionList(overlapsCondition.getRight());
        return null;
    }


    @Override
    public <S> Void visit(EqualsTo equalsTo, S context) {
        validateOldOracleJoinBinaryExpression(equalsTo, " = ", context);
        return null;
    }

    @Override
    public <S> Void visit(Division division, S context) {
        visitBinaryExpression(division, " / ");
        return null;
    }

    @Override
    public <S> Void visit(IntegerDivision division, S context) {
        visitBinaryExpression(division, " DIV ");
        return null;
    }

    @Override
    public <S> Void visit(DoubleValue doubleValue, S context) {
        // nothing to validate
        return null;
    }

    @Override
    public <S> Void visit(HexValue hexValue, S context) {
        // nothing to validate
        return null;
    }

    @Override
    public <S> Void visit(NotExpression notExpr, S context) {
        notExpr.getExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(BitwiseRightShift expr, S context) {
        visitBinaryExpression(expr, " >> ");
        return null;
    }

    @Override
    public <S> Void visit(BitwiseLeftShift expr, S context) {
        visitBinaryExpression(expr, " << ");
        return null;
    }

    public <S> void validateOldOracleJoinBinaryExpression(OldOracleJoinBinaryExpression expression,
            String operator, S context) {
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
    }

    @Override
    public <S> Void visit(GreaterThan greaterThan, S context) {
        validateOldOracleJoinBinaryExpression(greaterThan, " > ", context);
        return null;
    }

    @Override
    public <S> Void visit(GreaterThanEquals greaterThanEquals, S context) {
        validateOldOracleJoinBinaryExpression(greaterThanEquals, " >= ", context);

        return null;
    }

    @Override
    public <S> Void visit(InExpression inExpression, S context) {
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
    public <S> Void visit(IncludesExpression includesExpression, S context) {
        validateOptionalExpression(includesExpression.getLeftExpression(), this);
        validateOptionalExpression(includesExpression.getRightExpression(), this);
        return null;
    }

    @Override
    public <S> Void visit(ExcludesExpression excludesExpression, S context) {
        validateOptionalExpression(excludesExpression.getLeftExpression(), this);
        validateOptionalExpression(excludesExpression.getRightExpression(), this);
        return null;
    }

    @Override
    public <S> Void visit(FullTextSearch fullTextSearch, S context) {
        validateOptionalExpressions(fullTextSearch.getMatchColumns());
        return null;
    }

    @Override
    public <S> Void visit(SignedExpression signedExpression, S context) {
        signedExpression.getExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(IsNullExpression isNullExpression, S context) {
        isNullExpression.getLeftExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(IsBooleanExpression isBooleanExpression, S context) {
        isBooleanExpression.getLeftExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(IsUnknownExpression isUnknownExpression, S context) {
        isUnknownExpression.getLeftExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(JdbcParameter jdbcParameter, S context) {
        validateFeature(Feature.jdbcParameter);
        return null;
    }

    public void visit(PlainSelect plainSelect) {
        visit(plainSelect, null); // Call the parametrized visit method with null context
    }

    public void visit(Addition addition) {
        visit(addition, null); // Call the parametrized visit method with null context
    }

    public void visit(AndExpression andExpression) {
        visit(andExpression, null); // Call the parametrized visit method with null context
    }

    public void visit(Between between) {
        visit(between, null); // Call the parametrized visit method with null context
    }

    public void visit(OverlapsCondition overlapsCondition) {
        visit(overlapsCondition, null); // Call the parametrized visit method with null context
    }

    public void visit(EqualsTo equalsTo) {
        visit(equalsTo, null); // Call the parametrized visit method with null context
    }

    public void visit(Division division) {
        visit(division, null); // Call the parametrized visit method with null context
    }

    public void visit(IntegerDivision division) {
        visit(division, null); // Call the parametrized visit method with null context
    }

    public void visit(DoubleValue doubleValue) {
        visit(doubleValue, null); // Call the parametrized visit method with null context
    }

    public void visit(HexValue hexValue) {
        visit(hexValue, null); // Call the parametrized visit method with null context
    }

    public void visit(NotExpression notExpr) {
        visit(notExpr, null); // Call the parametrized visit method with null context
    }

    public void visit(BitwiseRightShift expr) {
        visit(expr, null); // Call the parametrized visit method with null context
    }

    public void visit(BitwiseLeftShift expr) {
        visit(expr, null); // Call the parametrized visit method with null context
    }

    public void visit(GreaterThan greaterThan) {
        visit(greaterThan, null); // Call the parametrized visit method with null context
    }

    public void visit(GreaterThanEquals greaterThanEquals) {
        visit(greaterThanEquals, null); // Call the parametrized visit method with null context
    }

    public void visit(InExpression inExpression) {
        visit(inExpression, null); // Call the parametrized visit method with null context
    }

    public void visit(IncludesExpression includesExpression) {
        visit(includesExpression, null); // Call the parametrized visit method with null context
    }

    public void visit(ExcludesExpression excludesExpression) {
        visit(excludesExpression, null); // Call the parametrized visit method with null context
    }

    public void visit(FullTextSearch fullTextSearch) {
        visit(fullTextSearch, null); // Call the parametrized visit method with null context
    }

    public void visit(SignedExpression signedExpression) {
        visit(signedExpression, null); // Call the parametrized visit method with null context
    }

    public void visit(IsNullExpression isNullExpression) {
        visit(isNullExpression, null); // Call the parametrized visit method with null context
    }

    public void visit(IsBooleanExpression isBooleanExpression) {
        visit(isBooleanExpression, null); // Call the parametrized visit method with null context
    }

    public void visit(IsUnknownExpression isUnknownExpression) {
        visit(isUnknownExpression, null); // Call the parametrized visit method with null context
    }

    public void visit(JdbcParameter jdbcParameter) {
        visit(jdbcParameter, null); // Call the parametrized visit method with null context
    }


    @Override
    public <S> Void visit(LikeExpression likeExpression, S context) {
        validateFeature(Feature.exprLike);
        visitBinaryExpression(likeExpression, (likeExpression.isNot() ? " NOT" : "")
                + (likeExpression.isCaseInsensitive() ? " ILIKE " : " LIKE "));
        return null;
    }

    @Override
    public <S> Void visit(ExistsExpression existsExpression, S context) {
        existsExpression.getRightExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(MemberOfExpression memberOfExpression, S context) {
        memberOfExpression.getLeftExpression().accept(this, context);
        memberOfExpression.getRightExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(LongValue longValue, S context) {
        // nothing to validate
        return null;
    }

    @Override
    public <S> Void visit(MinorThan minorThan, S context) {
        validateOldOracleJoinBinaryExpression(minorThan, " < ", context);

        return null;
    }

    @Override
    public <S> Void visit(MinorThanEquals minorThanEquals, S context) {
        validateOldOracleJoinBinaryExpression(minorThanEquals, " <= ", context);

        return null;
    }

    @Override
    public <S> Void visit(Multiplication multiplication, S context) {
        visitBinaryExpression(multiplication, " * ");

        return null;
    }

    @Override
    public <S> Void visit(NotEqualsTo notEqualsTo, S context) {
        validateOldOracleJoinBinaryExpression(notEqualsTo,
                " " + notEqualsTo.getStringExpression() + " ", context);
        return null;
    }

    @Override
    public <S> Void visit(DoubleAnd doubleAnd, S context) {

        return null;
    }

    @Override
    public <S> Void visit(Contains contains, S context) {

        return null;
    }

    @Override
    public <S> Void visit(ContainedBy containedBy, S context) {

        return null;
    }

    @Override
    public <S> Void visit(NullValue nullValue, S context) {
        // nothing to validate
        return null;
    }

    @Override
    public <S> Void visit(OrExpression orExpression, S context) {
        visitBinaryExpression(orExpression, " OR ");

        return null;
    }

    @Override
    public <S> Void visit(XorExpression xorExpression, S context) {
        visitBinaryExpression(xorExpression, " XOR ");

        return null;
    }

    @Override
    public <S> Void visit(StringValue stringValue, S context) {
        // nothing to validate
        return null;
    }

    @Override
    public <S> Void visit(BooleanValue booleanValue, S context) {
        // nothing to validate
        return null;
    }

    @Override
    public <S> Void visit(Subtraction subtraction, S context) {
        visitBinaryExpression(subtraction, " - ");
        return null;
    }

    protected void visitBinaryExpression(BinaryExpression binaryExpression, String operator) {
        binaryExpression.getLeftExpression().accept(this, null);
        binaryExpression.getRightExpression().accept(this, null);
    }

    @Override
    public <S> Void visit(ParenthesedSelect selectBody, S context) {
        validateOptionalFromItem(selectBody);
        return null;
    }

    @Override
    public <S> Void visit(Column tableColumn, S context) {
        if (tableColumn
                .getOldOracleJoinSyntax() != SupportsOldOracleJoinSyntax.NO_ORACLE_JOIN) {
            validateFeature(Feature.oracleOldJoinSyntax);
        }
        validateName(NamedObject.column, tableColumn.getFullyQualifiedName());
        return null;
    }

    @Override
    public <S> Void visit(Function function, S context) {
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
    public <S> Void visit(DateValue dateValue, S context) {
        // nothing to validate
        return null;
    }

    @Override
    public <S> Void visit(TimestampValue timestampValue, S context) {
        // nothing to validate
        return null;
    }

    @Override
    public <S> Void visit(TimeValue timeValue, S context) {
        // nothing to validate
        return null;
    }

    @Override
    public <S> Void visit(CaseExpression caseExpression, S context) {
        Expression switchExp = caseExpression.getSwitchExpression();
        if (switchExp != null) {
            switchExp.accept(this, context);
        }

        caseExpression.getWhenClauses().forEach(wc -> wc.accept(this, context));

        Expression elseExp = caseExpression.getElseExpression();
        if (elseExp != null) {
            elseExp.accept(this, context);
        }
        return null;
    }

    public void visit(LikeExpression likeExpression) {
        visit(likeExpression, null);
    }

    public void visit(ExistsExpression existsExpression) {
        visit(existsExpression, null);
    }

    public void visit(MemberOfExpression memberOfExpression) {
        visit(memberOfExpression, null);
    }

    public void visit(LongValue longValue) {
        visit(longValue, null);
    }

    public void visit(MinorThan minorThan) {
        visit(minorThan, null);
    }

    public void visit(MinorThanEquals minorThanEquals) {
        visit(minorThanEquals, null);
    }

    public void visit(Multiplication multiplication) {
        visit(multiplication, null);
    }

    public void visit(NotEqualsTo notEqualsTo) {
        visit(notEqualsTo, null);
    }

    public void visit(DoubleAnd doubleAnd) {
        visit(doubleAnd, null);
    }

    public void visit(Contains contains) {
        visit(contains, null);
    }

    public void visit(ContainedBy containedBy) {
        visit(containedBy, null);
    }

    public void visit(NullValue nullValue) {
        visit(nullValue, null);
    }

    public void visit(OrExpression orExpression) {
        visit(orExpression, null);
    }

    public void visit(XorExpression xorExpression) {
        visit(xorExpression, null);
    }

    public void visit(StringValue stringValue) {
        visit(stringValue, null);
    }

    public void visit(BooleanValue booleanValue) {
        visit(booleanValue, null);
    }

    public void visit(Subtraction subtraction) {
        visit(subtraction, null);
    }

    public void visit(ParenthesedSelect selectBody) {
        visit(selectBody, null);
    }

    public void visit(Column tableColumn) {
        visit(tableColumn, null);
    }

    public void visit(Function function) {
        visit(function, null);
    }

    public void visit(DateValue dateValue) {
        visit(dateValue, null);
    }

    public void visit(TimestampValue timestampValue) {
        visit(timestampValue, null);
    }

    public void visit(TimeValue timeValue) {
        visit(timeValue, null);
    }

    public void visit(CaseExpression caseExpression) {
        visit(caseExpression, null);
    }


    @Override
    public <S> Void visit(WhenClause whenClause, S context) {
        whenClause.getWhenExpression().accept(this, context);
        whenClause.getThenExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(AnyComparisonExpression anyComparisonExpression, S context) {
        anyComparisonExpression.getSelect().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(Concat concat, S context) {
        visitBinaryExpression(concat, " || ");
        return null;
    }

    @Override
    public <S> Void visit(Matches matches, S context) {
        validateOldOracleJoinBinaryExpression(matches, " @@ ", context);
        return null;
    }

    @Override
    public <S> Void visit(BitwiseAnd bitwiseAnd, S context) {
        visitBinaryExpression(bitwiseAnd, " & ");
        return null;
    }

    @Override
    public <S> Void visit(BitwiseOr bitwiseOr, S context) {
        visitBinaryExpression(bitwiseOr, " | ");
        return null;
    }

    @Override
    public <S> Void visit(BitwiseXor bitwiseXor, S context) {
        visitBinaryExpression(bitwiseXor, " ^ ");
        return null;
    }

    @Override
    public <S> Void visit(CastExpression cast, S context) {
        cast.getLeftExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(Modulo modulo, S context) {
        visitBinaryExpression(modulo, " % ");
        return null;
    }

    @Override
    public <S> Void visit(AnalyticExpression aexpr, S context) {
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
    public <S> Void visit(ExtractExpression eexpr, S context) {
        eexpr.getExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(IntervalExpression iexpr, S context) {
        validateOptionalExpression(iexpr.getExpression());
        return null;
    }

    @Override
    public <S> Void visit(JdbcNamedParameter jdbcNamedParameter, S context) {
        validateFeature(Feature.jdbcNamedParameter);
        return null;
    }

    @Override
    public <S> Void visit(OracleHierarchicalExpression oexpr, S context) {
        validateFeature(Feature.oracleHierarchicalExpression);
        return null;
    }

    @Override
    public <S> Void visit(RegExpMatchOperator rexpr, S context) {
        visitBinaryExpression(rexpr, " " + rexpr.getStringExpression() + " ");
        return null;
    }

    @Override
    public <S> Void visit(JsonExpression jsonExpr, S context) {
        validateOptionalExpression(jsonExpr.getExpression());
        return null;
    }

    @Override
    public <S> Void visit(JsonOperator jsonExpr, S context) {
        visitBinaryExpression(jsonExpr, " " + jsonExpr.getStringExpression() + " ");
        return null;
    }

    @Override
    public <S> Void visit(UserVariable var, S context) {
        // nothing to validate
        return null;
    }

    @Override
    public <S> Void visit(NumericBind bind, S context) {
        // nothing to validate
        return null;
    }

    @Override
    public <S> Void visit(KeepExpression aexpr, S context) {
        validateOptionalOrderByElements(aexpr.getOrderByElements());
        return null;
    }

    @Override
    public <S> Void visit(MySQLGroupConcat groupConcat, S context) {
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

    public void visit(WhenClause whenClause) {
        visit(whenClause, null);
    }

    public void visit(AnyComparisonExpression anyComparisonExpression) {
        visit(anyComparisonExpression, null);
    }

    public void visit(Concat concat) {
        visit(concat, null);
    }

    public void visit(Matches matches) {
        visit(matches, null);
    }

    public void visit(BitwiseAnd bitwiseAnd) {
        visit(bitwiseAnd, null);
    }

    public void visit(BitwiseOr bitwiseOr) {
        visit(bitwiseOr, null);
    }

    public void visit(BitwiseXor bitwiseXor) {
        visit(bitwiseXor, null);
    }

    public void visit(CastExpression cast) {
        visit(cast, null);
    }

    public void visit(Modulo modulo) {
        visit(modulo, null);
    }

    public void visit(AnalyticExpression aexpr) {
        visit(aexpr, null);
    }

    public void visit(ExtractExpression eexpr) {
        visit(eexpr, null);
    }

    public void visit(IntervalExpression iexpr) {
        visit(iexpr, null);
    }

    public void visit(JdbcNamedParameter jdbcNamedParameter) {
        visit(jdbcNamedParameter, null);
    }

    public void visit(OracleHierarchicalExpression oexpr) {
        visit(oexpr, null);
    }

    public void visit(RegExpMatchOperator rexpr) {
        visit(rexpr, null);
    }

    public void visit(JsonExpression jsonExpr) {
        visit(jsonExpr, null);
    }

    public void visit(JsonOperator jsonExpr) {
        visit(jsonExpr, null);
    }

    public void visit(UserVariable var) {
        visit(var, null);
    }

    public void visit(NumericBind bind) {
        visit(bind, null);
    }

    public void visit(KeepExpression aexpr) {
        visit(aexpr, null);
    }

    public void visit(MySQLGroupConcat groupConcat) {
        visit(groupConcat, null);
    }

    @Override
    public <S> Void visit(ExpressionList<?> expressionList, S context) {
        validateOptionalExpressionList(expressionList);
        return null;
    }

    @Override
    public <S> Void visit(RowConstructor<?> rowConstructor, S context) {
        validateOptionalExpressionList(rowConstructor);
        return null;
    }

    @Override
    public <S> Void visit(RowGetExpression rowGetExpression, S context) {
        rowGetExpression.getExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(OracleHint hint, S context) {
        // nothing to validate
        return null;
    }

    @Override
    public <S> Void visit(TimeKeyExpression timeKeyExpression, S context) {
        // nothing to validate
        return null;
    }

    @Override
    public <S> Void visit(DateTimeLiteralExpression literal, S context) {
        // nothing to validate
        return null;
    }

    @Override
    public <S> Void visit(NextValExpression nextVal, S context) {
        validateName(NamedObject.sequence, nextVal.getName());
        return null;
    }

    @Override
    public <S> Void visit(CollateExpression col, S context) {
        validateOptionalExpression(col.getLeftExpression());
        return null;
    }

    @Override
    public <S> Void visit(SimilarToExpression expr, S context) {
        validateFeature(Feature.exprSimilarTo);
        visitBinaryExpression(expr, (expr.isNot() ? " NOT" : "") + " SIMILAR TO ");
        return null;
    }

    @Override
    public <S> Void visit(ArrayExpression array, S context) {
        array.getObjExpression().accept(this, context);
        if (array.getIndexExpression() != null) {
            array.getIndexExpression().accept(this, context);
        }
        if (array.getStartIndexExpression() != null) {
            array.getStartIndexExpression().accept(this, context);
        }
        if (array.getStopIndexExpression() != null) {
            array.getStopIndexExpression().accept(this, context);
        }
        return null;
    }

    @Override
    public <S> Void visit(ArrayConstructor aThis, S context) {
        for (Expression expression : aThis.getExpressions()) {
            expression.accept(this, context);
        }
        return null;
    }

    @Override
    public void validate(Expression expression) {
        expression.accept(this, null);
    }

    @Override
    public <S> Void visit(VariableAssignment a, S context) {
        validateOptionalExpression(a.getExpression());
        if (a.getVariable() != null) {
            a.getVariable().accept(this, context);
        }
        return null;
    }

    @Override
    public <S> Void visit(TimezoneExpression a, S context) {
        validateOptionalExpression(a.getLeftExpression());
        return null;
    }

    @Override
    public <S> Void visit(XMLSerializeExpr xml, S context) {
        return null;
    }

    @Override
    public <S> Void visit(JsonAggregateFunction expression, S context) {
        // no idea what this is good for
        return null;
    }

    @Override
    public <S> Void visit(JsonFunction expression, S context) {
        // no idea what this is good for
        return null;
    }

    @Override
    public <S> Void visit(ConnectByRootOperator connectByRootOperator, S context) {
        connectByRootOperator.getColumn().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(ConnectByPriorOperator connectByPriorOperator, S context) {
        connectByPriorOperator.getColumn().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(OracleNamedFunctionParameter oracleNamedFunctionParameter, S context) {
        oracleNamedFunctionParameter.getExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(PostgresNamedFunctionParameter postgresNamedFunctionParameter,
            S context) {
        postgresNamedFunctionParameter.getExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(AllColumns allColumns, S context) {
        return null;
    }

    @Override
    public <S> Void visit(AllTableColumns allTableColumns, S context) {
        return null;
    }

    @Override
    public <S> Void visit(FunctionAllColumns functionColumns, S context) {
        return null;
    }

    @Override
    public <S> Void visit(AllValue allValue, S context) {
        return null;
    }

    @Override
    public <S> Void visit(IsDistinctExpression isDistinctExpression, S context) {
        isDistinctExpression.getLeftExpression().accept(this, context);
        isDistinctExpression.getRightExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(GeometryDistance geometryDistance, S context) {
        validateOldOracleJoinBinaryExpression(geometryDistance, " <-> ", context);
        return null;
    }

    @Override
    public <S> Void visit(Select select, S context) {
        return null;
    }

    @Override
    public <S> Void visit(TranscodingFunction transcodingFunction, S context) {
        transcodingFunction.getExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(TrimFunction trimFunction, S context) {
        if (trimFunction.getExpression() != null) {
            trimFunction.getExpression().accept(this, context);
        }
        if (trimFunction.getFromExpression() != null) {
            trimFunction.getFromExpression().accept(this, context);
        }
        return null;
    }

    @Override
    public <S> Void visit(RangeExpression rangeExpression, S context) {
        rangeExpression.getStartExpression().accept(this, context);
        rangeExpression.getEndExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(TSQLLeftJoin tsqlLeftJoin, S context) {
        tsqlLeftJoin.getLeftExpression().accept(this, context);
        tsqlLeftJoin.getRightExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(TSQLRightJoin tsqlRightJoin, S context) {
        tsqlRightJoin.getLeftExpression().accept(this, context);
        tsqlRightJoin.getRightExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(StructType structType, S context) {
        if (structType.getArguments() != null) {
            for (SelectItem<?> selectItem : structType.getArguments()) {
                selectItem.getExpression().accept(this, context);
            }
        }
        return null;
    }

    @Override
    public <S> Void visit(LambdaExpression lambdaExpression, S context) {
        lambdaExpression.getExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(HighExpression highExpression, S context) {
        highExpression.getExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(LowExpression lowExpression, S context) {
        lowExpression.getExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(Plus plus, S context) {
        visitBinaryExpression(plus, " PLUS ");
        return null;
    }

    @Override
    public <S> Void visit(PriorTo priorTo, S context) {
        visitBinaryExpression(priorTo, " PLUS ");
        return null;
    }

    @Override
    public <S> Void visit(Inverse inverse, S context) {
        inverse.getExpression().accept(this, context);
        return null;
    }

    public void visit(TimeKeyExpression timeKeyExpression) {
        visit(timeKeyExpression, null);
    }

    public void visit(DateTimeLiteralExpression literal) {
        visit(literal, null);
    }

    public void visit(NextValExpression nextVal) {
        visit(nextVal, null);
    }

    public void visit(CollateExpression col) {
        visit(col, null);
    }

    public void visit(SimilarToExpression expr) {
        visit(expr, null);
    }

    public void visit(ArrayExpression array) {
        visit(array, null);
    }

    public void visit(ArrayConstructor aThis) {
        visit(aThis, null);
    }


    public void visit(VariableAssignment a) {
        visit(a, null);
    }

    public void visit(TimezoneExpression a) {
        visit(a, null);
    }

    public void visit(XMLSerializeExpr xml) {
        visit(xml, null);
    }

    public void visit(JsonAggregateFunction expression) {
        visit(expression, null);
    }

    public void visit(JsonFunction expression) {
        visit(expression, null);
    }

    public void visit(ConnectByRootOperator connectByRootOperator) {
        visit(connectByRootOperator, null);
    }

    public void visit(OracleNamedFunctionParameter oracleNamedFunctionParameter) {
        visit(oracleNamedFunctionParameter, null);
    }

    public void visit(AllColumns allColumns) {
        visit(allColumns, null);
    }

    public void visit(AllTableColumns allTableColumns) {
        visit(allTableColumns, null);
    }

    public void visit(AllValue allValue) {
        visit(allValue, null);
    }

    public void visit(IsDistinctExpression isDistinctExpression) {
        visit(isDistinctExpression, null);
    }

    public void visit(GeometryDistance geometryDistance) {
        visit(geometryDistance, null);
    }

    public void visit(Select select) {
        visit(select, null);
    }

    public void visit(TranscodingFunction transcodingFunction) {
        visit(transcodingFunction, null);
    }

    public void visit(TrimFunction trimFunction) {
        visit(trimFunction, null);
    }

    public void visit(RangeExpression rangeExpression) {
        visit(rangeExpression, null);
    }

    public void visit(TSQLLeftJoin tsqlLeftJoin) {
        visit(tsqlLeftJoin, null);
    }

    public void visit(TSQLRightJoin tsqlRightJoin) {
        visit(tsqlRightJoin, null);
    }

    public void visit(StructType structType) {
        visit(structType, null);
    }

    public void visit(LambdaExpression lambdaExpression) {
        visit(lambdaExpression, null);
    }

    public void visit(HighExpression highExpression) {
        visit(highExpression, null);
    }

    public void visit(LowExpression lowExpression) {
        visit(lowExpression, null);
    }

    public void visit(Plus plus) {
        visit(plus, null);
    }

    public void visit(PriorTo priorTo) {
        visit(priorTo, null);
    }

    public void visit(Inverse inverse) {
        visit(inverse, null);
    }

    @Override
    public <S> Void visit(CosineSimilarity cosineSimilarity, S context) {
        cosineSimilarity.getLeftExpression().accept(this, context);
        cosineSimilarity.getRightExpression().accept(this, context);
        return null;
    }

    @Override
    public <S> Void visit(FromQuery fromQuery, S context) {
        return null;
    }

    @Override
    public <S> Void visit(DateUnitExpression dateUnitExpression, S context) {
        return null;
    }

}

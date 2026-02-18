/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.deparser;

import net.sf.jsqlparser.expression.AllValue;
import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.AnalyticType;
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
import net.sf.jsqlparser.expression.JsonTableFunction;
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
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.piped.FromQuery;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.FunctionAllColumns;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.WithItem;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;

@SuppressWarnings({"PMD.CyclomaticComplexity"})
public class ExpressionDeParser extends AbstractDeParser<Expression>
        // FIXME maybe we should implement an ItemsListDeparser too?
        implements ExpressionVisitor<StringBuilder> {

    private static final String NOT = "NOT ";
    private SelectVisitor<StringBuilder> selectVisitor;
    private OrderByDeParser orderByDeParser = new OrderByDeParser();

    public ExpressionDeParser() {
        super(new StringBuilder());
    }

    public ExpressionDeParser(SelectVisitor<StringBuilder> selectVisitor, StringBuilder buffer) {
        this(selectVisitor, buffer, new OrderByDeParser());
    }

    ExpressionDeParser(SelectVisitor<StringBuilder> selectVisitor, StringBuilder buffer,
            OrderByDeParser orderByDeParser) {
        super(buffer);
        this.selectVisitor = selectVisitor;
        this.orderByDeParser = orderByDeParser;
    }

    @Override
    public <S> StringBuilder visit(Addition addition, S context) {
        deparse(addition, " + ", null);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(AndExpression andExpression, S context) {
        deparse(andExpression, andExpression.isUseOperator() ? " && " : " AND ",
                null);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(Between between, S context) {
        between.getLeftExpression().accept(this, context);
        if (between.isNot()) {
            builder.append(" NOT");
        }

        builder.append(" BETWEEN ");

        if (between.isUsingSymmetric()) {
            builder.append("SYMMETRIC ");
        } else if (between.isUsingAsymmetric()) {
            builder.append("ASYMMETRIC ");
        }

        between.getBetweenExpressionStart().accept(this, context);
        builder.append(" AND ");
        between.getBetweenExpressionEnd().accept(this, context);

        return builder;
    }

    @Override
    public <S> StringBuilder visit(OverlapsCondition overlapsCondition, S context) {
        builder.append(overlapsCondition.toString());
        return builder;
    }

    @Override
    public <S> StringBuilder visit(EqualsTo equalsTo, S context) {
        deparse(equalsTo, " = ", null);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(Division division, S context) {
        deparse(division, " / ", null);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(IntegerDivision division, S context) {
        deparse(division, " DIV ", null);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(DoubleValue doubleValue, S context) {
        builder.append(doubleValue.toString());
        return builder;
    }

    @Override
    public <S> StringBuilder visit(HexValue hexValue, S context) {
        builder.append(hexValue.toString());
        return builder;
    }

    @Override
    public <S> StringBuilder visit(NotExpression notExpr, S context) {
        if (notExpr.isExclamationMark()) {
            builder.append("! ");
        } else {
            builder.append(NOT);
        }
        notExpr.getExpression().accept(this, context);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(BitwiseRightShift expr, S context) {
        deparse(expr, " >> ", null);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(BitwiseLeftShift expr, S context) {
        deparse(expr, " << ", null);
        return builder;
    }

    public <S> StringBuilder deparse(
            OldOracleJoinBinaryExpression expression,
            String operator, S context) {
        // if (expression.isNot()) {
        // buffer.append(NOT);
        // }
        expression.getLeftExpression().accept(this, context);
        if (expression.getOldOracleJoinSyntax() == EqualsTo.ORACLE_JOIN_RIGHT) {
            builder.append("(+)");
        }
        builder.append(operator);
        expression.getRightExpression().accept(this, context);
        if (expression.getOldOracleJoinSyntax() == EqualsTo.ORACLE_JOIN_LEFT) {
            builder.append("(+)");
        }

        return builder;
    }

    @Override
    public <S> StringBuilder visit(GreaterThan greaterThan, S context) {
        deparse(greaterThan, " > ", null);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(GreaterThanEquals greaterThanEquals, S context) {
        deparse(greaterThanEquals, " >= ", null);

        return builder;
    }

    public void visit(Addition addition) {
        visit(addition, null);
    }

    public void visit(AndExpression andExpression) {
        visit(andExpression, null);
    }

    public void visit(Between between) {
        visit(between, null);
    }

    public void visit(OverlapsCondition overlapsCondition) {
        visit(overlapsCondition, null);
    }

    public void visit(EqualsTo equalsTo) {
        visit(equalsTo, null);
    }

    public void visit(Division division) {
        visit(division, null);
    }

    public void visit(IntegerDivision division) {
        visit(division, null);
    }

    public void visit(DoubleValue doubleValue) {
        visit(doubleValue, null);
    }

    public void visit(HexValue hexValue) {
        visit(hexValue, null);
    }

    public void visit(NotExpression notExpr) {
        visit(notExpr, null);
    }

    public void visit(BitwiseRightShift expr) {
        visit(expr, null);
    }

    public void visit(BitwiseLeftShift expr) {
        visit(expr, null);
    }


    @Override
    public <S> StringBuilder visit(InExpression inExpression, S context) {
        inExpression.getLeftExpression().accept(this, context);
        if (inExpression
                .getOldOracleJoinSyntax() == SupportsOldOracleJoinSyntax.ORACLE_JOIN_RIGHT) {
            builder.append("(+)");
        }
        if (inExpression.isGlobal()) {
            builder.append(" GLOBAL");
        }
        if (inExpression.isNot()) {
            builder.append(" NOT");
        }
        builder.append(" IN ");
        inExpression.getRightExpression().accept(this, context);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(IncludesExpression includesExpression, S context) {
        includesExpression.getLeftExpression().accept(this, context);
        builder.append(" INCLUDES ");
        includesExpression.getRightExpression().accept(this, context);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(ExcludesExpression excludesExpression, S context) {
        excludesExpression.getLeftExpression().accept(this, context);
        builder.append(" EXCLUDES ");
        excludesExpression.getRightExpression().accept(this, context);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(FullTextSearch fullTextSearch, S context) {
        // Build a list of matched columns
        StringBuilder columnsListCommaSeperated = new StringBuilder();
        Iterator<Column> iterator = fullTextSearch.getMatchColumns().iterator();
        while (iterator.hasNext()) {
            Column col = iterator.next();
            columnsListCommaSeperated.append(col.getFullyQualifiedName());
            if (iterator.hasNext()) {
                columnsListCommaSeperated.append(",");
            }
        }
        builder.append("MATCH (").append(columnsListCommaSeperated).append(") AGAINST (")
                .append(fullTextSearch.getAgainstValue())
                .append(fullTextSearch.getSearchModifier() != null
                        ? " " + fullTextSearch.getSearchModifier()
                        : "")
                .append(")");
        return builder;
    }

    @Override
    public <S> StringBuilder visit(SignedExpression signedExpression, S context) {
        builder.append(signedExpression.getSign());
        signedExpression.getExpression().accept(this, context);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(IsNullExpression isNullExpression, S context) {
        isNullExpression.getLeftExpression().accept(this, context);
        if (isNullExpression.isUseNotNull()) {
            builder.append(" NOTNULL");
        } else if (isNullExpression.isUseIsNull()) {
            if (isNullExpression.isNot()) {
                builder.append(" NOT ISNULL");
            } else {
                builder.append(" ISNULL");
            }
        } else {
            if (isNullExpression.isNot()) {
                builder.append(" IS NOT NULL");
            } else {
                builder.append(" IS NULL");
            }
        }
        return builder;
    }

    @Override
    public <S> StringBuilder visit(IsBooleanExpression isBooleanExpression, S context) {
        isBooleanExpression.getLeftExpression().accept(this, context);
        if (isBooleanExpression.isTrue()) {
            if (isBooleanExpression.isNot()) {
                builder.append(" IS NOT TRUE");
            } else {
                builder.append(" IS TRUE");
            }
        } else {
            if (isBooleanExpression.isNot()) {
                builder.append(" IS NOT FALSE");
            } else {
                builder.append(" IS FALSE");
            }
        }
        return builder;
    }

    @Override
    public <S> StringBuilder visit(IsUnknownExpression isUnknownExpression, S context) {
        isUnknownExpression.getLeftExpression().accept(this, context);
        if (isUnknownExpression.isNot()) {
            builder.append(" IS NOT UNKNOWN");
        } else {
            builder.append(" IS UNKNOWN");
        }
        return builder;
    }

    @Override
    public <S> StringBuilder visit(JdbcParameter jdbcParameter, S context) {
        builder.append(jdbcParameter.getParameterCharacter());
        if (jdbcParameter.isUseFixedIndex()) {
            builder.append(jdbcParameter.getIndex());
        }

        return builder;
    }

    @Override
    public <S> StringBuilder visit(LikeExpression likeExpression, S context) {
        String keywordStr = likeExpression.getLikeKeyWord() == LikeExpression.KeyWord.SIMILAR_TO
                ? " SIMILAR TO"
                : likeExpression.getLikeKeyWord().toString();

        likeExpression.getLeftExpression().accept(this, context);
        builder.append(" ");
        if (likeExpression.isNot()) {
            builder.append("NOT ");
        }
        builder.append(keywordStr).append(" ");
        if (likeExpression.isUseBinary()) {
            builder.append("BINARY ");
        }
        likeExpression.getRightExpression().accept(this, context);

        Expression escape = likeExpression.getEscape();
        if (escape != null) {
            builder.append(" ESCAPE ");
            likeExpression.getEscape().accept(this, context);
        }
        return builder;
    }

    @Override
    public <S> StringBuilder visit(ExistsExpression existsExpression, S context) {
        if (existsExpression.isNot()) {
            builder.append("NOT EXISTS ");
        } else {
            builder.append("EXISTS ");
        }
        existsExpression.getRightExpression().accept(this, context);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(MemberOfExpression memberOfExpression, S context) {
        memberOfExpression.getLeftExpression().accept(this, context);
        if (memberOfExpression.isNot()) {
            builder.append(" NOT MEMBER OF ");
        } else {
            builder.append(" MEMBER OF ");
        }
        memberOfExpression.getRightExpression().accept(this, context);
        return builder;
    }

    public void visit(InExpression inExpression) {
        visit(inExpression, null);
    }

    public void visit(IncludesExpression includesExpression) {
        visit(includesExpression, null);
    }

    public void visit(ExcludesExpression excludesExpression) {
        visit(excludesExpression, null);
    }

    public void visit(FullTextSearch fullTextSearch) {
        visit(fullTextSearch, null);
    }

    public void visit(SignedExpression signedExpression) {
        visit(signedExpression, null);
    }

    public void visit(IsNullExpression isNullExpression) {
        visit(isNullExpression, null);
    }

    public void visit(IsBooleanExpression isBooleanExpression) {
        visit(isBooleanExpression, null);
    }

    public void visit(IsUnknownExpression isUnknownExpression) {
        visit(isUnknownExpression, null);
    }

    public void visit(JdbcParameter jdbcParameter) {
        visit(jdbcParameter, null);
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


    @Override
    public <S> StringBuilder visit(LongValue longValue, S context) {
        builder.append(longValue.getStringValue());

        return builder;
    }

    @Override
    public <S> StringBuilder visit(MinorThan minorThan, S context) {
        deparse(minorThan, " < ", null);

        return builder;
    }

    @Override
    public <S> StringBuilder visit(MinorThanEquals minorThanEquals, S context) {
        deparse(minorThanEquals, " <= ", null);

        return builder;
    }

    @Override
    public <S> StringBuilder visit(Multiplication multiplication, S context) {
        deparse(multiplication, " * ", null);

        return builder;
    }

    @Override
    public <S> StringBuilder visit(NotEqualsTo notEqualsTo, S context) {
        deparse(notEqualsTo,
                " " + notEqualsTo.getStringExpression() + " ", null);

        return builder;
    }

    @Override
    public <S> StringBuilder visit(DoubleAnd doubleAnd, S context) {
        deparse(doubleAnd, " " + doubleAnd.getStringExpression() + " ",
                null);

        return builder;
    }

    @Override
    public <S> StringBuilder visit(Contains contains, S context) {
        deparse(contains, " " + contains.getStringExpression() + " ",
                null);

        return builder;
    }

    @Override
    public <S> StringBuilder visit(ContainedBy containedBy, S context) {
        deparse(containedBy,
                " " + containedBy.getStringExpression() + " ", null);

        return builder;
    }

    @Override
    public <S> StringBuilder visit(NullValue nullValue, S context) {
        builder.append(nullValue.toString());

        return builder;
    }

    @Override
    public <S> StringBuilder visit(OrExpression orExpression, S context) {
        deparse(orExpression, " OR ", null);

        return builder;
    }

    @Override
    public <S> StringBuilder visit(XorExpression xorExpression, S context) {
        deparse(xorExpression, " XOR ", null);

        return builder;
    }

    @Override
    public <S> StringBuilder visit(StringValue stringValue, S context) {
        if (stringValue.getPrefix() != null) {
            builder.append(stringValue.getPrefix());
        }
        builder.append(stringValue.getQuoteStr()).append(stringValue.getValue())
                .append(stringValue.getQuoteStr());

        return builder;
    }

    @Override
    public <S> StringBuilder visit(BooleanValue booleanValue, S context) {
        builder.append(booleanValue.getValue());

        return builder;
    }

    @Override
    public <S> StringBuilder visit(Subtraction subtraction, S context) {
        deparse(subtraction, " - ", null);
        return builder;
    }

    protected <S> void deparse(BinaryExpression binaryExpression,
            String operator, S context) {
        binaryExpression.getLeftExpression().accept(this, context);
        builder.append(operator);
        binaryExpression.getRightExpression().accept(this, context);

    }

    @Override
    public <S> StringBuilder visit(Select select, S context) {
        if (selectVisitor != null) {
            if (select.getWithItemsList() != null) {
                builder.append("WITH ");
                for (Iterator<WithItem<?>> iter = select.getWithItemsList().iterator(); iter
                        .hasNext();) {
                    iter.next().accept(selectVisitor, null);
                    if (iter.hasNext()) {
                        builder.append(", ");
                    }
                    builder.append(" ");
                }
                builder.append(" ");
            }

            select.accept(selectVisitor, null);
        }
        return builder;
    }

    @Override
    public <S> StringBuilder visit(TranscodingFunction transcodingFunction, S context) {
        if (transcodingFunction.isTranscodeStyle()) {
            builder.append(transcodingFunction.getKeyword());
            builder.append("( ");
            transcodingFunction.getExpression().accept(this, context);
            builder.append(" USING ")
                    .append(transcodingFunction.getTranscodingName())
                    .append(" )");
        } else {
            builder
                    .append(transcodingFunction.getKeyword())
                    .append("( ")
                    .append(transcodingFunction.getColDataType())
                    .append(", ");
            transcodingFunction.getExpression().accept(this, context);

            String transCodingName = transcodingFunction.getTranscodingName();
            if (transCodingName != null && !transCodingName.isEmpty()) {
                builder.append(", ").append(transCodingName);
            }
            builder.append(" )");
        }

        return builder;
    }

    public <S> StringBuilder visit(TrimFunction trimFunction, S context) {
        builder.append("Trim(");

        if (trimFunction.getTrimSpecification() != null) {
            builder.append(" ").append(trimFunction.getTrimSpecification());
        }

        if (trimFunction.getExpression() != null) {
            builder.append(" ");
            trimFunction.getExpression().accept(this, context);
        }

        if (trimFunction.getFromExpression() != null) {
            builder.append(trimFunction.isUsingFromKeyword() ? " FROM " : ", ");
            trimFunction.getFromExpression().accept(this, context);
        }
        builder.append(" )");
        return builder;
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

    public void visit(Select select) {
        visit(select, null);
    }

    public void visit(TranscodingFunction transcodingFunction) {
        visit(transcodingFunction, null);
    }

    public void visit(TrimFunction trimFunction) {
        visit(trimFunction, null);
    }


    @Override
    public <S> StringBuilder visit(RangeExpression rangeExpression, S context) {
        rangeExpression.getStartExpression().accept(this, context);
        builder.append(":");
        rangeExpression.getEndExpression().accept(this, context);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(Column tableColumn, S context) {
        final Table table = tableColumn.getTable();
        String tableName = null;
        if (table != null) {
            if (table.getAlias() != null) {
                tableName = table.getAlias().getName();
            } else {
                tableName = table.getFullyQualifiedName();
            }
        }
        if (tableName != null && !tableName.isEmpty()) {
            builder.append(tableName).append(tableColumn.getTableDelimiter());
        }

        builder.append(tableColumn.getColumnName());

        if (tableColumn.getArrayConstructor() != null) {
            tableColumn.getArrayConstructor().accept(this, context);
        }

        if (tableColumn.getCommentText() != null) {
            builder.append(" /* ").append(tableColumn.getCommentText()).append("*/ ");
        }

        return builder;
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public <S> StringBuilder visit(Function function, S context) {
        if (function.isEscaped()) {
            builder.append("{fn ");
        }

        builder.append(function.getName());
        if (function.getParameters() == null && function.getNamedParameters() == null) {
            builder.append("()");
        } else {
            builder.append("(");
            if (function.isDistinct()) {
                builder.append("DISTINCT ");
            } else if (function.isAllColumns()) {
                builder.append("ALL ");
            } else if (function.isUnique()) {
                builder.append("UNIQUE ");
            }

            if (function.getExtraKeyword() != null) {
                builder.append(function.getExtraKeyword()).append(" ");
            }

            if (function.getNamedParameters() != null) {
                function.getNamedParameters().accept(this, context);
            }
            if (function.getParameters() != null) {
                function.getParameters().accept(this, context);
            }

            Function.HavingClause havingClause = function.getHavingClause();
            if (havingClause != null) {
                builder.append(" HAVING ").append(havingClause.getHavingType()).append(" ");
                havingClause.getExpression().accept(this, context);
            }

            if (function.getNullHandling() != null && !function.isIgnoreNullsOutside()) {
                switch (function.getNullHandling()) {
                    case IGNORE_NULLS:
                        builder.append(" IGNORE NULLS");
                        break;
                    case RESPECT_NULLS:
                        builder.append(" RESPECT NULLS");
                        break;
                }
            }
            if (function.getOrderByElements() != null) {
                builder.append(" ORDER BY ");
                boolean comma = false;
                orderByDeParser.setExpressionVisitor(this);
                orderByDeParser.setBuilder(builder);
                for (OrderByElement orderByElement : function.getOrderByElements()) {
                    if (comma) {
                        builder.append(", ");
                    } else {
                        comma = true;
                    }
                    orderByDeParser.deParseElement(orderByElement);
                }
            }

            if (function.getOnOverflowTruncate() != null) {
                builder.append(" ON OVERFLOW ").append(function.getOnOverflowTruncate());
            }

            if (function.getLimit() != null) {
                new LimitDeparser(this, builder).deParse(function.getLimit());
            }
            builder.append(")");
        }

        if (function.getNullHandling() != null && function.isIgnoreNullsOutside()) {
            switch (function.getNullHandling()) {
                case IGNORE_NULLS:
                    builder.append(" IGNORE NULLS");
                    break;
                case RESPECT_NULLS:
                    builder.append(" RESPECT NULLS");
                    break;
            }
        }

        if (function.getAttribute() != null) {
            builder.append(".").append(function.getAttribute());
        }
        if (function.getKeep() != null) {
            builder.append(" ").append(function.getKeep());
        }

        if (function.isEscaped()) {
            builder.append("}");
        }
        return builder;
    }

    @Override
    public <S> StringBuilder visit(ParenthesedSelect selectBody, S context) {
        selectBody.getSelect().accept(this, context);
        return builder;
    }

    public SelectVisitor<StringBuilder> getSelectVisitor() {
        return selectVisitor;
    }

    public void setSelectVisitor(SelectVisitor<StringBuilder> visitor) {
        selectVisitor = visitor;
    }

    @Override
    public <S> StringBuilder visit(DateValue dateValue, S context) {
        builder.append("{d '").append(dateValue.getValue().toString()).append("'}");
        return builder;
    }

    @Override
    public <S> StringBuilder visit(TimestampValue timestampValue, S context) {
        builder.append("{ts '").append(timestampValue.getValue().toString()).append("'}");
        return builder;
    }

    @Override
    public <S> StringBuilder visit(TimeValue timeValue, S context) {
        builder.append("{t '").append(timeValue.getValue().toString()).append("'}");
        return builder;
    }

    @Override
    public <S> StringBuilder visit(CaseExpression caseExpression, S context) {
        builder.append(caseExpression.isUsingBrackets() ? "(" : "").append("CASE ");
        Expression switchExp = caseExpression.getSwitchExpression();
        if (switchExp != null) {
            switchExp.accept(this, context);
            builder.append(" ");
        }

        for (Expression exp : caseExpression.getWhenClauses()) {
            exp.accept(this, context);
        }

        Expression elseExp = caseExpression.getElseExpression();
        if (elseExp != null) {
            builder.append("ELSE ");
            elseExp.accept(this, context);
            builder.append(" ");
        }

        builder.append("END").append(caseExpression.isUsingBrackets() ? ")" : "");
        return builder;
    }

    @Override
    public <S> StringBuilder visit(WhenClause whenClause, S context) {
        builder.append("WHEN ");
        whenClause.getWhenExpression().accept(this, context);
        builder.append(" THEN ");
        whenClause.getThenExpression().accept(this, context);
        builder.append(" ");
        return builder;
    }

    @Override
    public <S> StringBuilder visit(AnyComparisonExpression anyComparisonExpression, S context) {
        builder.append(anyComparisonExpression.getAnyType().name());

        // VALUES or SELECT
        anyComparisonExpression.getSelect().accept(this, context);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(Concat concat, S context) {
        deparse(concat, " || ", null);
        return builder;
    }

    public void visit(RangeExpression rangeExpression) {
        visit(rangeExpression, null);
    }

    public void visit(Column tableColumn) {
        visit(tableColumn, null);
    }

    public void visit(Function function) {
        visit(function, null);
    }

    public void visit(ParenthesedSelect selectBody) {
        visit(selectBody, null);
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

    public void visit(WhenClause whenClause) {
        visit(whenClause, null);
    }

    public void visit(AnyComparisonExpression anyComparisonExpression) {
        visit(anyComparisonExpression, null);
    }

    public void visit(Concat concat) {
        visit(concat, null);
    }


    @Override
    public <S> StringBuilder visit(Matches matches, S context) {
        deparse(matches, " @@ ", null);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(BitwiseAnd bitwiseAnd, S context) {
        deparse(bitwiseAnd, " & ", null);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(BitwiseOr bitwiseOr, S context) {
        deparse(bitwiseOr, " | ", null);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(BitwiseXor bitwiseXor, S context) {
        deparse(bitwiseXor, " ^ ", null);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(CastExpression cast, S context) {
        if (cast.isImplicitCast()) {
            builder.append(cast.getColDataType()).append(" ");
            cast.getLeftExpression().accept(this, context);
        } else if (cast.isUseCastKeyword()) {
            String formatStr = cast.getFormat() != null && !cast.getFormat().isEmpty()
                    ? " FORMAT " + cast.getFormat()
                    : "";

            builder.append(cast.keyword).append("(");
            cast.getLeftExpression().accept(this, context);
            builder.append(" AS ");
            builder.append(
                    cast.getColumnDefinitions().size() > 1
                            ? "ROW(" + Select.getStringList(cast.getColumnDefinitions()) + ")"
                            : cast.getColDataType().toString());
            builder.append(formatStr);
            builder.append(")");
        } else {
            cast.getLeftExpression().accept(this, context);
            builder.append("::");
            builder.append(cast.getColDataType());
        }
        return builder;
    }

    @Override
    public <S> StringBuilder visit(Modulo modulo, S context) {
        deparse(modulo, " % ", null);
        return builder;
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ExcessiveMethodLength",
            "PMD.MissingBreakInSwitch"})
    public <S> StringBuilder visit(AnalyticExpression analyticExpression, S context) {
        String name = analyticExpression.getName();
        Expression expression = analyticExpression.getExpression();
        Expression offset = analyticExpression.getOffset();
        Expression defaultValue = analyticExpression.getDefaultValue();
        boolean isAllColumns = analyticExpression.isAllColumns();
        KeepExpression keep = analyticExpression.getKeep();
        ExpressionList<?> partitionExpressionList = analyticExpression.getPartitionExpressionList();
        List<OrderByElement> orderByElements = analyticExpression.getOrderByElements();
        WindowElement windowElement = analyticExpression.getWindowElement();

        builder.append(name).append("(");
        if (analyticExpression.isDistinct()) {
            builder.append("DISTINCT ");
        }
        if (analyticExpression.isUnique()) {
            builder.append("UNIQUE ");
        }
        if (expression != null) {
            expression.accept(this, context);
            if (offset != null) {
                builder.append(", ");
                offset.accept(this, context);
                if (defaultValue != null) {
                    builder.append(", ");
                    defaultValue.accept(this, context);
                }
            }
        } else if (isAllColumns) {
            builder.append("*");
        }
        Function.HavingClause havingClause = analyticExpression.getHavingClause();
        if (havingClause != null) {
            builder.append(" HAVING ").append(havingClause.getHavingType()).append(" ");
            havingClause.getExpression().accept(this, context);
        }

        if (analyticExpression.getNullHandling() != null
                && !analyticExpression.isIgnoreNullsOutside()) {
            switch (analyticExpression.getNullHandling()) {
                case IGNORE_NULLS:
                    builder.append(" IGNORE NULLS");
                    break;
                case RESPECT_NULLS:
                    builder.append(" RESPECT NULLS");
                    break;
            }
        }
        if (analyticExpression.getFuncOrderBy() != null) {
            builder.append(" ORDER BY ");
            builder.append(
                    analyticExpression.getFuncOrderBy().stream().map(OrderByElement::toString)
                            .collect(joining(", ")));
        }

        if (analyticExpression.getOnOverflowTruncate() != null) {
            builder.append(" ON OVERFLOW ").append(analyticExpression.getOnOverflowTruncate());
        }

        if (analyticExpression.getLimit() != null) {
            new LimitDeparser(this, builder).deParse(analyticExpression.getLimit());
        }

        builder.append(") ");
        if (keep != null) {
            keep.accept(this, context);
            builder.append(" ");
        }

        if (analyticExpression.getFilterExpression() != null) {
            builder.append("FILTER (WHERE ");
            analyticExpression.getFilterExpression().accept(this, context);
            builder.append(")");
            if (analyticExpression.getType() != AnalyticType.FILTER_ONLY) {
                builder.append(" ");
            }
        }

        if (analyticExpression.getNullHandling() != null
                && analyticExpression.isIgnoreNullsOutside()) {
            switch (analyticExpression.getNullHandling()) {
                case IGNORE_NULLS:
                    builder.append(" IGNORE NULLS ");
                    break;
                case RESPECT_NULLS:
                    builder.append(" RESPECT NULLS ");
                    break;
            }
        }

        switch (analyticExpression.getType()) {
            case FILTER_ONLY:
                return null;
            case WITHIN_GROUP:
                builder.append("WITHIN GROUP");
                break;
            case WITHIN_GROUP_OVER:
                builder.append("WITHIN GROUP (");
                analyticExpression.getWindowDefinition().getOrderBy()
                        .toStringOrderByElements(builder);
                builder.append(") OVER (");
                analyticExpression.getWindowDefinition().getPartitionBy()
                        .toStringPartitionBy(builder);
                builder.append(")");
                break;
            default:
                builder.append("OVER");
        }

        if (analyticExpression.getWindowName() != null) {
            builder.append(" ").append(analyticExpression.getWindowName());
        } else if (analyticExpression.getType() != AnalyticType.WITHIN_GROUP_OVER) {
            builder.append(" (");

            if (partitionExpressionList != null
                    && !partitionExpressionList.isEmpty()) {
                builder.append("PARTITION BY ");
                if (analyticExpression.isPartitionByBrackets()) {
                    builder.append("(");
                }
                for (int i = 0; i < ((List<?>) partitionExpressionList).size(); i++) {
                    if (i > 0) {
                        builder.append(", ");
                    }
                    ((List<Expression>) partitionExpressionList).get(i).accept(this, context);
                }
                if (analyticExpression.isPartitionByBrackets()) {
                    builder.append(")");
                }
                builder.append(" ");
            }
            if (orderByElements != null && !orderByElements.isEmpty()) {
                builder.append("ORDER BY ");
                orderByDeParser.setExpressionVisitor(this);
                orderByDeParser.setBuilder(builder);
                for (int i = 0; i < orderByElements.size(); i++) {
                    if (i > 0) {
                        builder.append(", ");
                    }
                    orderByDeParser.deParseElement(orderByElements.get(i));
                }
            }

            if (windowElement != null) {
                if (orderByElements != null && !orderByElements.isEmpty()) {
                    builder.append(' ');
                }
                builder.append(windowElement);
            }

            builder.append(")");
        }
        return builder;
    }

    @Override
    public <S> StringBuilder visit(ExtractExpression extractExpression, S context) {
        builder.append("EXTRACT(").append(extractExpression.getName());
        builder.append(" FROM ");
        extractExpression.getExpression().accept(this, context);
        builder.append(')');
        return builder;
    }

    @Override
    public <S> StringBuilder visit(IntervalExpression intervalExpression, S context) {
        if (intervalExpression.isUsingIntervalKeyword()) {
            builder.append("INTERVAL ");
        }
        if (intervalExpression.getExpression() != null) {
            intervalExpression.getExpression().accept(this, context);
        } else {
            builder.append(intervalExpression.getParameter());
        }
        if (intervalExpression.getIntervalType() != null) {
            builder.append(" ").append(intervalExpression.getIntervalType());
        }
        return builder;
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

    public void visit(AnalyticExpression analyticExpression) {
        visit(analyticExpression, null);
    }

    public void visit(ExtractExpression extractExpression) {
        visit(extractExpression, null);
    }

    public void visit(IntervalExpression intervalExpression) {
        visit(intervalExpression, null);
    }


    @Override
    public <S> StringBuilder visit(JdbcNamedParameter jdbcNamedParameter, S context) {
        builder.append(jdbcNamedParameter.toString());
        return builder;
    }

    @Override
    public <S> StringBuilder visit(OracleHierarchicalExpression hierarchicalExpression, S context) {
        builder.append(hierarchicalExpression.toString());
        return builder;
    }

    @Override
    public <S> StringBuilder visit(RegExpMatchOperator regExpMatchOperator, S context) {
        deparse(regExpMatchOperator, " " + regExpMatchOperator.getStringExpression() + " ", null);
        return builder;
    }


    @Override
    public <S> StringBuilder visit(JsonExpression jsonExpr, S context) {
        builder.append(jsonExpr.toString());
        return builder;
    }

    @Override
    public <S> StringBuilder visit(JsonOperator jsonExpr, S context) {
        deparse(jsonExpr, " " + jsonExpr.getStringExpression() + " ", null);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(UserVariable var, S context) {
        builder.append(var.toString());
        return builder;
    }

    @Override
    public <S> StringBuilder visit(NumericBind bind, S context) {
        builder.append(bind.toString());
        return builder;
    }

    @Override
    public <S> StringBuilder visit(KeepExpression keepExpression, S context) {
        builder.append(keepExpression.toString());
        return builder;
    }

    @Override
    public <S> StringBuilder visit(MySQLGroupConcat groupConcat, S context) {
        builder.append(groupConcat.toString());
        return builder;
    }

    @Override
    public <S> StringBuilder visit(ExpressionList<? extends Expression> expressionList, S context) {
        ExpressionListDeParser<?> expressionListDeParser =
                new ExpressionListDeParser<>(this, builder);
        expressionListDeParser.deParse(expressionList);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(RowConstructor<?> rowConstructor, S context) {
        if (rowConstructor.getName() != null) {
            builder.append(rowConstructor.getName());
        }
        ExpressionListDeParser<?> expressionListDeParser =
                new ExpressionListDeParser<>(this, builder);
        expressionListDeParser.deParse(rowConstructor);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(RowGetExpression rowGetExpression, S context) {
        rowGetExpression.getExpression().accept(this, context);
        builder.append(".").append(rowGetExpression.getColumnName());
        return null;
    }

    @Override
    public <S> StringBuilder visit(OracleHint hint, S context) {
        builder.append(hint.toString());
        return builder;
    }

    @Override
    public <S> StringBuilder visit(TimeKeyExpression timeKeyExpression, S context) {
        builder.append(timeKeyExpression.toString());
        return builder;
    }

    @Override
    public <S> StringBuilder visit(DateTimeLiteralExpression literal, S context) {
        builder.append(literal.toString());
        return builder;
    }

    @Override
    public <S> StringBuilder visit(NextValExpression nextVal, S context) {
        builder.append(nextVal.isUsingNextValueFor() ? "NEXT VALUE FOR " : "NEXTVAL FOR ")
                .append(nextVal.getName());
        return builder;
    }

    @Override
    public <S> StringBuilder visit(CollateExpression col, S context) {
        builder.append(col.getLeftExpression().toString()).append(" COLLATE ")
                .append(col.getCollate());
        return builder;
    }

    @Override
    public <S> StringBuilder visit(SimilarToExpression expr, S context) {
        deparse(expr, (expr.isNot() ? " NOT" : "") + " SIMILAR TO ", null);
        return builder;
    }

    public void visit(JdbcNamedParameter jdbcNamedParameter) {
        visit(jdbcNamedParameter, null);
    }

    public void visit(OracleHierarchicalExpression hierarchicalExpression) {
        visit(hierarchicalExpression, null);
    }

    public void visit(RegExpMatchOperator regExpMatchOperator) {
        visit(regExpMatchOperator, null);
    }

    public void visit(JsonExpression jsonExpr) {
        visit(jsonExpr, null);
    }

    public void visit(JsonOperator jsonExpr) {
        visit(jsonExpr, null);
    }

    public void visit(UserVariable userVariable) {
        visit(userVariable, null);
    }

    public void visit(NumericBind numericBind) {
        visit(numericBind, null);
    }

    public void visit(KeepExpression keepExpression) {
        visit(keepExpression, null);
    }

    public void visit(MySQLGroupConcat groupConcat) {
        visit(groupConcat, null);
    }

    public void visit(ExpressionList<?> expressionList) {
        visit(expressionList, null);
    }

    public void visit(RowConstructor<?> rowConstructor) {
        visit(rowConstructor, null);
    }

    public void visit(RowGetExpression rowGetExpression) {
        visit(rowGetExpression, null);
    }

    public void visit(OracleHint hint) {
        visit(hint, null);
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


    @Override
    public <S> StringBuilder visit(ArrayExpression array, S context) {
        array.getObjExpression().accept(this, context);
        builder.append("[");
        if (array.getIndexExpression() != null) {
            array.getIndexExpression().accept(this, context);
        } else {
            if (array.getStartIndexExpression() != null) {
                array.getStartIndexExpression().accept(this, context);
            }
            builder.append(":");
            if (array.getStopIndexExpression() != null) {
                array.getStopIndexExpression().accept(this, context);
            }
        }

        builder.append("]");
        return builder;
    }

    @Override
    public <S> StringBuilder visit(ArrayConstructor arrayConstructor, S context) {
        if (arrayConstructor.isArrayKeyword()) {
            builder.append("ARRAY");

            ColDataType dataType = arrayConstructor.getDataType();
            if (dataType != null) {
                builder.append("<").append(dataType).append(">");
            }
        }
        builder.append("[");
        boolean first = true;
        for (Expression expression : arrayConstructor.getExpressions()) {
            if (!first) {
                builder.append(", ");
            } else {
                first = false;
            }
            expression.accept(this, context);
        }
        builder.append("]");
        return builder;
    }

    @Override
    void deParse(Expression statement) {
        statement.accept(this, null);
    }

    @Override
    public <S> StringBuilder visit(VariableAssignment var, S context) {
        var.getVariable().accept(this, context);
        builder.append(" ").append(var.getOperation()).append(" ");
        var.getExpression().accept(this, context);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(XMLSerializeExpr expr, S context) {
        // xmlserialize(xmlagg(xmltext(COMMENT_LINE) ORDER BY COMMENT_SEQUENCE) as varchar(1024))
        builder.append("xmlserialize(xmlagg(xmltext(");
        expr.getExpression().accept(this, context);
        builder.append(")");
        if (expr.getOrderByElements() != null) {
            builder.append(" ORDER BY ");
            for (Iterator<OrderByElement> i = expr.getOrderByElements().iterator(); i.hasNext();) {
                builder.append(i.next().toString());
                if (i.hasNext()) {
                    builder.append(", ");
                }
            }
        }
        builder.append(") AS ").append(expr.getDataType()).append(")");
        return builder;
    }

    @Override
    public <S> StringBuilder visit(TimezoneExpression var, S context) {
        var.getLeftExpression().accept(this, context);

        for (Expression expr : var.getTimezoneExpressions()) {
            builder.append(" AT TIME ZONE ");
            expr.accept(this, context);
        }
        return builder;
    }

    @Override
    public <S> StringBuilder visit(JsonAggregateFunction expression, S context) {
        expression.append(builder);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(JsonFunction expression, S context) {
        expression.append(builder);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(JsonTableFunction expression, S context) {
        builder.append(expression);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(ConnectByRootOperator connectByRootOperator, S context) {
        builder.append("CONNECT_BY_ROOT ");
        connectByRootOperator.getColumn().accept(this, context);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(ConnectByPriorOperator connectByPriorOperator, S context) {
        builder.append("PRIOR ");
        connectByPriorOperator.getColumn().accept(this, context);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(OracleNamedFunctionParameter oracleNamedFunctionParameter,
            S context) {
        builder.append(oracleNamedFunctionParameter.getName()).append(" => ");

        oracleNamedFunctionParameter.getExpression().accept(this, context);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(AllColumns allColumns, S context) {
        builder.append(allColumns.toString());
        return builder;
    }

    @Override
    public <S> StringBuilder visit(AllTableColumns allTableColumns, S context) {
        builder.append(allTableColumns.toString());
        return builder;
    }

    @Override
    public <S> StringBuilder visit(FunctionAllColumns functionAllColumns, S context) {
        builder.append(functionAllColumns.toString());
        return builder;
    }

    @Override
    public <S> StringBuilder visit(AllValue allValue, S context) {
        builder.append(allValue);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(IsDistinctExpression isDistinctExpression, S context) {
        builder.append(isDistinctExpression.getLeftExpression())
                .append(isDistinctExpression.getStringExpression())
                .append(isDistinctExpression.getRightExpression());
        return builder;
    }

    @Override
    public <S> StringBuilder visit(GeometryDistance geometryDistance, S context) {
        deparse(geometryDistance,
                " " + geometryDistance.getStringExpression() + " ", null);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(TSQLLeftJoin tsqlLeftJoin, S context) {
        this.deparse(tsqlLeftJoin, " *= ", null);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(TSQLRightJoin tsqlRightJoin, S context) {
        this.deparse(tsqlRightJoin, " =* ", null);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(StructType structType, S context) {
        if (structType.getDialect() != StructType.Dialect.DUCKDB
                && structType.getKeyword() != null) {
            builder.append(structType.getKeyword());
        }

        if (structType.getDialect() != StructType.Dialect.DUCKDB
                && structType.getParameters() != null && !structType.getParameters().isEmpty()) {
            builder.append("<");
            int i = 0;
            for (Map.Entry<String, ColDataType> e : structType.getParameters()) {
                if (0 < i++) {
                    builder.append(",");
                }
                // optional name
                if (e.getKey() != null && !e.getKey().isEmpty()) {
                    builder.append(e.getKey()).append(" ");
                }

                // mandatory type
                builder.append(e.getValue());
            }

            builder.append(">");
        }

        if (structType.getArguments() != null && !structType.getArguments().isEmpty()) {
            if (structType.getDialect() == StructType.Dialect.DUCKDB) {
                builder.append("{ ");
                int i = 0;
                for (SelectItem<?> e : structType.getArguments()) {
                    if (0 < i++) {
                        builder.append(",");
                    }
                    builder.append(e.getAlias().getName());
                    builder.append(" : ");
                    e.getExpression().accept(this, context);
                }
                builder.append(" }");
            } else {
                builder.append("(");
                int i = 0;
                for (SelectItem<?> e : structType.getArguments()) {
                    if (0 < i++) {
                        builder.append(",");
                    }
                    e.getExpression().accept(this, context);
                    if (e.getAlias() != null) {
                        builder.append(" as ");
                        builder.append(e.getAlias().getName());
                    }
                }
                builder.append(")");
            }
        }

        if (structType.getDialect() == StructType.Dialect.DUCKDB
                && structType.getParameters() != null && !structType.getParameters().isEmpty()) {
            builder.append("::STRUCT( ");
            int i = 0;
            for (Map.Entry<String, ColDataType> e : structType.getParameters()) {
                if (0 < i++) {
                    builder.append(",");
                }
                builder.append(e.getKey()).append(" ");
                builder.append(e.getValue());
            }
            builder.append(")");
        }
        return builder;
    }

    @Override
    public <S> StringBuilder visit(LambdaExpression lambdaExpression, S context) {
        if (lambdaExpression.getIdentifiers().size() == 1) {
            builder.append(lambdaExpression.getIdentifiers().get(0));
        } else {
            int i = 0;
            builder.append("( ");
            for (String s : lambdaExpression.getIdentifiers()) {
                builder.append(i++ > 0 ? ", " : "").append(s);
            }
            builder.append(" )");
        }

        builder.append(" -> ");
        lambdaExpression.getExpression().accept(this, context);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(HighExpression highExpression, S context) {
        return builder.append(highExpression.toString());
    }

    @Override
    public <S> StringBuilder visit(LowExpression lowExpression, S context) {
        return builder.append(lowExpression.toString());
    }

    @Override
    public <S> StringBuilder visit(Plus plus, S context) {
        return builder.append(plus.toString());
    }

    @Override
    public <S> StringBuilder visit(PriorTo priorTo, S context) {
        return builder.append(priorTo.toString());
    }

    @Override
    public <S> StringBuilder visit(Inverse inverse, S context) {
        return builder.append(inverse.toString());
    }

    @Override
    public <S> StringBuilder visit(CosineSimilarity cosineSimilarity, S context) {
        deparse(cosineSimilarity,
                " " + cosineSimilarity.getStringExpression() + " ", context);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(FromQuery fromQuery, S context) {
        return null;
    }

    @Override
    public <S> StringBuilder visit(DateUnitExpression dateUnitExpression, S context) {
        return builder.append(dateUnitExpression.toString());
    }

    @Override
    public <S> StringBuilder visit(PostgresNamedFunctionParameter postgresNamedFunctionParameter,
            S context) {
        builder.append(postgresNamedFunctionParameter.getName()).append(" := ");

        postgresNamedFunctionParameter.getExpression().accept(this, context);
        return builder;
    }
}

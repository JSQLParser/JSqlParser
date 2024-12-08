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
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
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
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(AndExpression andExpression, S context) {
        deparse(andExpression, andExpression.isUseOperator() ? " && " : " AND ",
                null);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(Between between, S context) {
        between.getLeftExpression().accept(this, context);
        if (between.isNot()) {
            buffer.append(" NOT");
        }

        buffer.append(" BETWEEN ");
        between.getBetweenExpressionStart().accept(this, context);
        buffer.append(" AND ");
        between.getBetweenExpressionEnd().accept(this, context);

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(OverlapsCondition overlapsCondition, S context) {
        buffer.append(overlapsCondition.toString());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(EqualsTo equalsTo, S context) {
        deparse(equalsTo, " = ", null);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(Division division, S context) {
        deparse(division, " / ", null);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(IntegerDivision division, S context) {
        deparse(division, " DIV ", null);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(DoubleValue doubleValue, S context) {
        buffer.append(doubleValue.toString());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(HexValue hexValue, S context) {
        buffer.append(hexValue.toString());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(NotExpression notExpr, S context) {
        if (notExpr.isExclamationMark()) {
            buffer.append("! ");
        } else {
            buffer.append(NOT);
        }
        notExpr.getExpression().accept(this, context);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(BitwiseRightShift expr, S context) {
        deparse(expr, " >> ", null);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(BitwiseLeftShift expr, S context) {
        deparse(expr, " << ", null);
        return buffer;
    }

    public <S> StringBuilder deparse(
            OldOracleJoinBinaryExpression expression,
            String operator, S context) {
        // if (expression.isNot()) {
        // buffer.append(NOT);
        // }
        expression.getLeftExpression().accept(this, context);
        if (expression.getOldOracleJoinSyntax() == EqualsTo.ORACLE_JOIN_RIGHT) {
            buffer.append("(+)");
        }
        buffer.append(operator);
        expression.getRightExpression().accept(this, context);
        if (expression.getOldOracleJoinSyntax() == EqualsTo.ORACLE_JOIN_LEFT) {
            buffer.append("(+)");
        }

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(GreaterThan greaterThan, S context) {
        deparse(greaterThan, " > ", null);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(GreaterThanEquals greaterThanEquals, S context) {
        deparse(greaterThanEquals, " >= ", null);

        return buffer;
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
            buffer.append("(+)");
        }
        if (inExpression.isGlobal()) {
            buffer.append(" GLOBAL");
        }
        if (inExpression.isNot()) {
            buffer.append(" NOT");
        }
        buffer.append(" IN ");
        inExpression.getRightExpression().accept(this, context);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(IncludesExpression includesExpression, S context) {
        includesExpression.getLeftExpression().accept(this, context);
        buffer.append(" INCLUDES ");
        includesExpression.getRightExpression().accept(this, context);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(ExcludesExpression excludesExpression, S context) {
        excludesExpression.getLeftExpression().accept(this, context);
        buffer.append(" EXCLUDES ");
        excludesExpression.getRightExpression().accept(this, context);
        return buffer;
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
        buffer.append("MATCH (").append(columnsListCommaSeperated).append(") AGAINST (")
                .append(fullTextSearch.getAgainstValue())
                .append(fullTextSearch.getSearchModifier() != null
                        ? " " + fullTextSearch.getSearchModifier()
                        : "")
                .append(")");
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(SignedExpression signedExpression, S context) {
        buffer.append(signedExpression.getSign());
        signedExpression.getExpression().accept(this, context);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(IsNullExpression isNullExpression, S context) {
        isNullExpression.getLeftExpression().accept(this, context);
        if (isNullExpression.isUseNotNull()) {
            buffer.append(" NOTNULL");
        } else if (isNullExpression.isUseIsNull()) {
            if (isNullExpression.isNot()) {
                buffer.append(" NOT ISNULL");
            } else {
                buffer.append(" ISNULL");
            }
        } else {
            if (isNullExpression.isNot()) {
                buffer.append(" IS NOT NULL");
            } else {
                buffer.append(" IS NULL");
            }
        }
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(IsBooleanExpression isBooleanExpression, S context) {
        isBooleanExpression.getLeftExpression().accept(this, context);
        if (isBooleanExpression.isTrue()) {
            if (isBooleanExpression.isNot()) {
                buffer.append(" IS NOT TRUE");
            } else {
                buffer.append(" IS TRUE");
            }
        } else {
            if (isBooleanExpression.isNot()) {
                buffer.append(" IS NOT FALSE");
            } else {
                buffer.append(" IS FALSE");
            }
        }
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(JdbcParameter jdbcParameter, S context) {
        buffer.append(jdbcParameter.getParameterCharacter());
        if (jdbcParameter.isUseFixedIndex()) {
            buffer.append(jdbcParameter.getIndex());
        }

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(LikeExpression likeExpression, S context) {
        String keywordStr = likeExpression.getLikeKeyWord() == LikeExpression.KeyWord.SIMILAR_TO
                ? " SIMILAR TO"
                : likeExpression.getLikeKeyWord().toString();

        likeExpression.getLeftExpression().accept(this, context);
        buffer.append(" ");
        if (likeExpression.isNot()) {
            buffer.append("NOT ");
        }
        buffer.append(keywordStr).append(" ");
        if (likeExpression.isUseBinary()) {
            buffer.append("BINARY ");
        }
        likeExpression.getRightExpression().accept(this, context);

        Expression escape = likeExpression.getEscape();
        if (escape != null) {
            buffer.append(" ESCAPE ");
            likeExpression.getEscape().accept(this, context);
        }
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(ExistsExpression existsExpression, S context) {
        if (existsExpression.isNot()) {
            buffer.append("NOT EXISTS ");
        } else {
            buffer.append("EXISTS ");
        }
        existsExpression.getRightExpression().accept(this, context);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(MemberOfExpression memberOfExpression, S context) {
        memberOfExpression.getLeftExpression().accept(this, context);
        if (memberOfExpression.isNot()) {
            buffer.append(" NOT MEMBER OF ");
        } else {
            buffer.append(" MEMBER OF ");
        }
        memberOfExpression.getRightExpression().accept(this, context);
        return buffer;
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
        buffer.append(longValue.getStringValue());

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(MinorThan minorThan, S context) {
        deparse(minorThan, " < ", null);

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(MinorThanEquals minorThanEquals, S context) {
        deparse(minorThanEquals, " <= ", null);

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(Multiplication multiplication, S context) {
        deparse(multiplication, " * ", null);

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(NotEqualsTo notEqualsTo, S context) {
        deparse(notEqualsTo,
                " " + notEqualsTo.getStringExpression() + " ", null);

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(DoubleAnd doubleAnd, S context) {
        deparse(doubleAnd, " " + doubleAnd.getStringExpression() + " ",
                null);

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(Contains contains, S context) {
        deparse(contains, " " + contains.getStringExpression() + " ",
                null);

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(ContainedBy containedBy, S context) {
        deparse(containedBy,
                " " + containedBy.getStringExpression() + " ", null);

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(NullValue nullValue, S context) {
        buffer.append(nullValue.toString());

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(OrExpression orExpression, S context) {
        deparse(orExpression, " OR ", null);

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(XorExpression xorExpression, S context) {
        deparse(xorExpression, " XOR ", null);

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(StringValue stringValue, S context) {
        if (stringValue.getPrefix() != null) {
            buffer.append(stringValue.getPrefix());
        }
        buffer.append("'").append(stringValue.getValue()).append("'");

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(BooleanValue booleanValue, S context) {
        buffer.append(booleanValue.getValue());

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(Subtraction subtraction, S context) {
        deparse(subtraction, " - ", null);
        return buffer;
    }

    protected <S> void deparse(BinaryExpression binaryExpression,
            String operator, S context) {
        binaryExpression.getLeftExpression().accept(this, context);
        buffer.append(operator);
        binaryExpression.getRightExpression().accept(this, context);

    }

    @Override
    public <S> StringBuilder visit(Select select, S context) {
        if (selectVisitor != null) {
            if (select.getWithItemsList() != null) {
                buffer.append("WITH ");
                for (Iterator<WithItem<?>> iter = select.getWithItemsList().iterator(); iter
                        .hasNext();) {
                    iter.next().accept(selectVisitor, null);
                    if (iter.hasNext()) {
                        buffer.append(", ");
                    }
                    buffer.append(" ");
                }
                buffer.append(" ");
            }

            select.accept(selectVisitor, null);
        }
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(TranscodingFunction transcodingFunction, S context) {
        if (transcodingFunction.isTranscodeStyle()) {
            buffer.append("CONVERT( ");
            transcodingFunction.getExpression().accept(this, context);
            buffer.append(" USING ")
                    .append(transcodingFunction.getTranscodingName())
                    .append(" )");
        } else {
            buffer
                    .append("CONVERT( ")
                    .append(transcodingFunction.getColDataType())
                    .append(", ");
            transcodingFunction.getExpression().accept(this, context);

            String transCodingName = transcodingFunction.getTranscodingName();
            if (transCodingName != null && !transCodingName.isEmpty()) {
                buffer.append(", ").append(transCodingName);
            }
            buffer.append(" )");
        }

        return buffer;
    }

    public <S> StringBuilder visit(TrimFunction trimFunction, S context) {
        buffer.append("Trim(");

        if (trimFunction.getTrimSpecification() != null) {
            buffer.append(" ").append(trimFunction.getTrimSpecification());
        }

        if (trimFunction.getExpression() != null) {
            buffer.append(" ");
            trimFunction.getExpression().accept(this, context);
        }

        if (trimFunction.getFromExpression() != null) {
            buffer.append(trimFunction.isUsingFromKeyword() ? " FROM " : ", ");
            trimFunction.getFromExpression().accept(this, context);
        }
        buffer.append(" )");
        return buffer;
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
        buffer.append(":");
        rangeExpression.getEndExpression().accept(this, context);
        return buffer;
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
            buffer.append(tableName).append(tableColumn.getTableDelimiter());
        }

        buffer.append(tableColumn.getColumnName());

        if (tableColumn.getArrayConstructor() != null) {
            tableColumn.getArrayConstructor().accept(this, context);
        }

        if (tableColumn.getCommentText() != null) {
            buffer.append(" /* ").append(tableColumn.getCommentText()).append("*/ ");
        }

        return buffer;
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public <S> StringBuilder visit(Function function, S context) {
        if (function.isEscaped()) {
            buffer.append("{fn ");
        }

        buffer.append(function.getName());
        if (function.getParameters() == null && function.getNamedParameters() == null) {
            buffer.append("()");
        } else {
            buffer.append("(");
            if (function.isDistinct()) {
                buffer.append("DISTINCT ");
            } else if (function.isAllColumns()) {
                buffer.append("ALL ");
            } else if (function.isUnique()) {
                buffer.append("UNIQUE ");
            }

            if (function.getExtraKeyword() != null) {
                buffer.append(function.getExtraKeyword()).append(" ");
            }

            if (function.getNamedParameters() != null) {
                function.getNamedParameters().accept(this, context);
            }
            if (function.getParameters() != null) {
                function.getParameters().accept(this, context);
            }

            Function.HavingClause havingClause = function.getHavingClause();
            if (havingClause != null) {
                buffer.append(" HAVING ").append(havingClause.getHavingType()).append(" ");
                havingClause.getExpression().accept(this, context);
            }

            if (function.getNullHandling() != null && !function.isIgnoreNullsOutside()) {
                switch (function.getNullHandling()) {
                    case IGNORE_NULLS:
                        buffer.append(" IGNORE NULLS");
                        break;
                    case RESPECT_NULLS:
                        buffer.append(" RESPECT NULLS");
                        break;
                }
            }
            if (function.getOrderByElements() != null) {
                buffer.append(" ORDER BY ");
                boolean comma = false;
                orderByDeParser.setExpressionVisitor(this);
                orderByDeParser.setBuffer(buffer);
                for (OrderByElement orderByElement : function.getOrderByElements()) {
                    if (comma) {
                        buffer.append(", ");
                    } else {
                        comma = true;
                    }
                    orderByDeParser.deParseElement(orderByElement);
                }
            }

            if (function.getOnOverflowTruncate() != null) {
                buffer.append(" ON OVERFLOW ").append(function.getOnOverflowTruncate());
            }

            if (function.getLimit() != null) {
                new LimitDeparser(this, buffer).deParse(function.getLimit());
            }
            buffer.append(")");
        }

        if (function.getNullHandling() != null && function.isIgnoreNullsOutside()) {
            switch (function.getNullHandling()) {
                case IGNORE_NULLS:
                    buffer.append(" IGNORE NULLS");
                    break;
                case RESPECT_NULLS:
                    buffer.append(" RESPECT NULLS");
                    break;
            }
        }

        if (function.getAttribute() != null) {
            buffer.append(".").append(function.getAttribute());
        }
        if (function.getKeep() != null) {
            buffer.append(" ").append(function.getKeep());
        }

        if (function.isEscaped()) {
            buffer.append("}");
        }
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(ParenthesedSelect selectBody, S context) {
        selectBody.getSelect().accept(this, context);
        return buffer;
    }

    public SelectVisitor<StringBuilder> getSelectVisitor() {
        return selectVisitor;
    }

    public void setSelectVisitor(SelectVisitor<StringBuilder> visitor) {
        selectVisitor = visitor;
    }

    @Override
    public <S> StringBuilder visit(DateValue dateValue, S context) {
        buffer.append("{d '").append(dateValue.getValue().toString()).append("'}");
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(TimestampValue timestampValue, S context) {
        buffer.append("{ts '").append(timestampValue.getValue().toString()).append("'}");
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(TimeValue timeValue, S context) {
        buffer.append("{t '").append(timeValue.getValue().toString()).append("'}");
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(CaseExpression caseExpression, S context) {
        buffer.append(caseExpression.isUsingBrackets() ? "(" : "").append("CASE ");
        Expression switchExp = caseExpression.getSwitchExpression();
        if (switchExp != null) {
            switchExp.accept(this, context);
            buffer.append(" ");
        }

        for (Expression exp : caseExpression.getWhenClauses()) {
            exp.accept(this, context);
        }

        Expression elseExp = caseExpression.getElseExpression();
        if (elseExp != null) {
            buffer.append("ELSE ");
            elseExp.accept(this, context);
            buffer.append(" ");
        }

        buffer.append("END").append(caseExpression.isUsingBrackets() ? ")" : "");
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(WhenClause whenClause, S context) {
        buffer.append("WHEN ");
        whenClause.getWhenExpression().accept(this, context);
        buffer.append(" THEN ");
        whenClause.getThenExpression().accept(this, context);
        buffer.append(" ");
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(AnyComparisonExpression anyComparisonExpression, S context) {
        buffer.append(anyComparisonExpression.getAnyType().name());

        // VALUES or SELECT
        anyComparisonExpression.getSelect().accept(this, context);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(Concat concat, S context) {
        deparse(concat, " || ", null);
        return buffer;
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
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(BitwiseAnd bitwiseAnd, S context) {
        deparse(bitwiseAnd, " & ", null);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(BitwiseOr bitwiseOr, S context) {
        deparse(bitwiseOr, " | ", null);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(BitwiseXor bitwiseXor, S context) {
        deparse(bitwiseXor, " ^ ", null);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(CastExpression cast, S context) {
        if (cast.isImplicitCast()) {
            buffer.append(cast.getColDataType()).append(" ");
            cast.getLeftExpression().accept(this, context);
        } else if (cast.isUseCastKeyword()) {
            String formatStr = cast.getFormat() != null && !cast.getFormat().isEmpty()
                    ? " FORMAT " + cast.getFormat()
                    : "";

            buffer.append(cast.keyword).append("(");
            cast.getLeftExpression().accept(this, context);
            buffer.append(" AS ");
            buffer.append(
                    cast.getColumnDefinitions().size() > 1
                            ? "ROW(" + Select.getStringList(cast.getColumnDefinitions()) + ")"
                            : cast.getColDataType().toString());
            buffer.append(formatStr);
            buffer.append(")");
        } else {
            cast.getLeftExpression().accept(this, context);
            buffer.append("::");
            buffer.append(cast.getColDataType());
        }
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(Modulo modulo, S context) {
        deparse(modulo, " % ", null);
        return buffer;
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

        buffer.append(name).append("(");
        if (analyticExpression.isDistinct()) {
            buffer.append("DISTINCT ");
        }
        if (analyticExpression.isUnique()) {
            buffer.append("UNIQUE ");
        }
        if (expression != null) {
            expression.accept(this, context);
            if (offset != null) {
                buffer.append(", ");
                offset.accept(this, context);
                if (defaultValue != null) {
                    buffer.append(", ");
                    defaultValue.accept(this, context);
                }
            }
        } else if (isAllColumns) {
            buffer.append("*");
        }
        Function.HavingClause havingClause = analyticExpression.getHavingClause();
        if (havingClause != null) {
            buffer.append(" HAVING ").append(havingClause.getHavingType()).append(" ");
            havingClause.getExpression().accept(this, context);
        }

        if (analyticExpression.getNullHandling() != null
                && !analyticExpression.isIgnoreNullsOutside()) {
            switch (analyticExpression.getNullHandling()) {
                case IGNORE_NULLS:
                    buffer.append(" IGNORE NULLS");
                    break;
                case RESPECT_NULLS:
                    buffer.append(" RESPECT NULLS");
                    break;
            }
        }
        if (analyticExpression.getFuncOrderBy() != null) {
            buffer.append(" ORDER BY ");
            buffer.append(analyticExpression.getFuncOrderBy().stream().map(OrderByElement::toString)
                    .collect(joining(", ")));
        }

        if (analyticExpression.getOnOverflowTruncate() != null) {
            buffer.append(" ON OVERFLOW ").append(analyticExpression.getOnOverflowTruncate());
        }

        if (analyticExpression.getLimit() != null) {
            new LimitDeparser(this, buffer).deParse(analyticExpression.getLimit());
        }

        buffer.append(") ");
        if (keep != null) {
            keep.accept(this, context);
            buffer.append(" ");
        }

        if (analyticExpression.getFilterExpression() != null) {
            buffer.append("FILTER (WHERE ");
            analyticExpression.getFilterExpression().accept(this, context);
            buffer.append(")");
            if (analyticExpression.getType() != AnalyticType.FILTER_ONLY) {
                buffer.append(" ");
            }
        }

        if (analyticExpression.getNullHandling() != null
                && analyticExpression.isIgnoreNullsOutside()) {
            switch (analyticExpression.getNullHandling()) {
                case IGNORE_NULLS:
                    buffer.append(" IGNORE NULLS ");
                    break;
                case RESPECT_NULLS:
                    buffer.append(" RESPECT NULLS ");
                    break;
            }
        }

        switch (analyticExpression.getType()) {
            case FILTER_ONLY:
                return null;
            case WITHIN_GROUP:
                buffer.append("WITHIN GROUP");
                break;
            case WITHIN_GROUP_OVER:
                buffer.append("WITHIN GROUP (");
                analyticExpression.getWindowDefinition().getOrderBy()
                        .toStringOrderByElements(buffer);
                buffer.append(") OVER (");
                analyticExpression.getWindowDefinition().getPartitionBy()
                        .toStringPartitionBy(buffer);
                buffer.append(")");
                break;
            default:
                buffer.append("OVER");
        }

        if (analyticExpression.getWindowName() != null) {
            buffer.append(" ").append(analyticExpression.getWindowName());
        } else if (analyticExpression.getType() != AnalyticType.WITHIN_GROUP_OVER) {
            buffer.append(" (");

            if (partitionExpressionList != null
                    && !partitionExpressionList.isEmpty()) {
                buffer.append("PARTITION BY ");
                if (analyticExpression.isPartitionByBrackets()) {
                    buffer.append("(");
                }
                for (int i = 0; i < ((List<?>) partitionExpressionList).size(); i++) {
                    if (i > 0) {
                        buffer.append(", ");
                    }
                    ((List<Expression>) partitionExpressionList).get(i).accept(this, context);
                }
                if (analyticExpression.isPartitionByBrackets()) {
                    buffer.append(")");
                }
                buffer.append(" ");
            }
            if (orderByElements != null && !orderByElements.isEmpty()) {
                buffer.append("ORDER BY ");
                orderByDeParser.setExpressionVisitor(this);
                orderByDeParser.setBuffer(buffer);
                for (int i = 0; i < orderByElements.size(); i++) {
                    if (i > 0) {
                        buffer.append(", ");
                    }
                    orderByDeParser.deParseElement(orderByElements.get(i));
                }
            }

            if (windowElement != null) {
                if (orderByElements != null && !orderByElements.isEmpty()) {
                    buffer.append(' ');
                }
                buffer.append(windowElement);
            }

            buffer.append(")");
        }
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(ExtractExpression extractExpression, S context) {
        buffer.append("EXTRACT(").append(extractExpression.getName());
        buffer.append(" FROM ");
        extractExpression.getExpression().accept(this, context);
        buffer.append(')');
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(IntervalExpression intervalExpression, S context) {
        if (intervalExpression.isUsingIntervalKeyword()) {
            buffer.append("INTERVAL ");
        }
        if (intervalExpression.getExpression() != null) {
            intervalExpression.getExpression().accept(this, context);
        } else {
            buffer.append(intervalExpression.getParameter());
        }
        if (intervalExpression.getIntervalType() != null) {
            buffer.append(" ").append(intervalExpression.getIntervalType());
        }
        return buffer;
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
        buffer.append(jdbcNamedParameter.toString());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(OracleHierarchicalExpression hierarchicalExpression, S context) {
        buffer.append(hierarchicalExpression.toString());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(RegExpMatchOperator regExpMatchOperator, S context) {
        deparse(regExpMatchOperator, " " + regExpMatchOperator.getStringExpression() + " ", null);
        return buffer;
    }


    @Override
    public <S> StringBuilder visit(JsonExpression jsonExpr, S context) {
        buffer.append(jsonExpr.toString());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(JsonOperator jsonExpr, S context) {
        deparse(jsonExpr, " " + jsonExpr.getStringExpression() + " ", null);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(UserVariable var, S context) {
        buffer.append(var.toString());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(NumericBind bind, S context) {
        buffer.append(bind.toString());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(KeepExpression keepExpression, S context) {
        buffer.append(keepExpression.toString());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(MySQLGroupConcat groupConcat, S context) {
        buffer.append(groupConcat.toString());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(ExpressionList<? extends Expression> expressionList, S context) {
        ExpressionListDeParser<?> expressionListDeParser =
                new ExpressionListDeParser<>(this, buffer);
        expressionListDeParser.deParse(expressionList);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(RowConstructor<?> rowConstructor, S context) {
        if (rowConstructor.getName() != null) {
            buffer.append(rowConstructor.getName());
        }
        ExpressionListDeParser<?> expressionListDeParser =
                new ExpressionListDeParser<>(this, buffer);
        expressionListDeParser.deParse(rowConstructor);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(RowGetExpression rowGetExpression, S context) {
        rowGetExpression.getExpression().accept(this, context);
        buffer.append(".").append(rowGetExpression.getColumnName());
        return null;
    }

    @Override
    public <S> StringBuilder visit(OracleHint hint, S context) {
        buffer.append(hint.toString());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(TimeKeyExpression timeKeyExpression, S context) {
        buffer.append(timeKeyExpression.toString());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(DateTimeLiteralExpression literal, S context) {
        buffer.append(literal.toString());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(NextValExpression nextVal, S context) {
        buffer.append(nextVal.isUsingNextValueFor() ? "NEXT VALUE FOR " : "NEXTVAL FOR ")
                .append(nextVal.getName());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(CollateExpression col, S context) {
        buffer.append(col.getLeftExpression().toString()).append(" COLLATE ")
                .append(col.getCollate());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(SimilarToExpression expr, S context) {
        deparse(expr, (expr.isNot() ? " NOT" : "") + " SIMILAR TO ", null);
        return buffer;
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
        buffer.append("[");
        if (array.getIndexExpression() != null) {
            array.getIndexExpression().accept(this, context);
        } else {
            if (array.getStartIndexExpression() != null) {
                array.getStartIndexExpression().accept(this, context);
            }
            buffer.append(":");
            if (array.getStopIndexExpression() != null) {
                array.getStopIndexExpression().accept(this, context);
            }
        }

        buffer.append("]");
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(ArrayConstructor aThis, S context) {
        if (aThis.isArrayKeyword()) {
            buffer.append("ARRAY");
        }
        buffer.append("[");
        boolean first = true;
        for (Expression expression : aThis.getExpressions()) {
            if (!first) {
                buffer.append(", ");
            } else {
                first = false;
            }
            expression.accept(this, context);
        }
        buffer.append("]");
        return buffer;
    }

    @Override
    void deParse(Expression statement) {
        statement.accept(this, null);
    }

    @Override
    public <S> StringBuilder visit(VariableAssignment var, S context) {
        var.getVariable().accept(this, context);
        buffer.append(" ").append(var.getOperation()).append(" ");
        var.getExpression().accept(this, context);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(XMLSerializeExpr expr, S context) {
        // xmlserialize(xmlagg(xmltext(COMMENT_LINE) ORDER BY COMMENT_SEQUENCE) as varchar(1024))
        buffer.append("xmlserialize(xmlagg(xmltext(");
        expr.getExpression().accept(this, context);
        buffer.append(")");
        if (expr.getOrderByElements() != null) {
            buffer.append(" ORDER BY ");
            for (Iterator<OrderByElement> i = expr.getOrderByElements().iterator(); i.hasNext();) {
                buffer.append(i.next().toString());
                if (i.hasNext()) {
                    buffer.append(", ");
                }
            }
        }
        buffer.append(") AS ").append(expr.getDataType()).append(")");
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(TimezoneExpression var, S context) {
        var.getLeftExpression().accept(this, context);

        for (Expression expr : var.getTimezoneExpressions()) {
            buffer.append(" AT TIME ZONE ");
            expr.accept(this, context);
        }
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(JsonAggregateFunction expression, S context) {
        expression.append(buffer);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(JsonFunction expression, S context) {
        expression.append(buffer);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(ConnectByRootOperator connectByRootOperator, S context) {
        buffer.append("CONNECT_BY_ROOT ");
        connectByRootOperator.getColumn().accept(this, context);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(ConnectByPriorOperator connectByPriorOperator, S context) {
        buffer.append("PRIOR ");
        connectByPriorOperator.getColumn().accept(this, context);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(OracleNamedFunctionParameter oracleNamedFunctionParameter,
            S context) {
        buffer.append(oracleNamedFunctionParameter.getName()).append(" => ");

        oracleNamedFunctionParameter.getExpression().accept(this, context);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(AllColumns allColumns, S context) {
        buffer.append(allColumns.toString());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(AllTableColumns allTableColumns, S context) {
        buffer.append(allTableColumns.toString());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(AllValue allValue, S context) {
        buffer.append(allValue);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(IsDistinctExpression isDistinctExpression, S context) {
        buffer.append(isDistinctExpression.getLeftExpression())
                .append(isDistinctExpression.getStringExpression())
                .append(isDistinctExpression.getRightExpression());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(GeometryDistance geometryDistance, S context) {
        deparse(geometryDistance,
                " " + geometryDistance.getStringExpression() + " ", null);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(TSQLLeftJoin tsqlLeftJoin, S context) {
        this.deparse(tsqlLeftJoin, " *= ", null);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(TSQLRightJoin tsqlRightJoin, S context) {
        this.deparse(tsqlRightJoin, " =* ", null);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(StructType structType, S context) {
        if (structType.getDialect() != StructType.Dialect.DUCKDB
                && structType.getKeyword() != null) {
            buffer.append(structType.getKeyword());
        }

        if (structType.getDialect() != StructType.Dialect.DUCKDB
                && structType.getParameters() != null && !structType.getParameters().isEmpty()) {
            buffer.append("<");
            int i = 0;
            for (Map.Entry<String, ColDataType> e : structType.getParameters()) {
                if (0 < i++) {
                    buffer.append(",");
                }
                // optional name
                if (e.getKey() != null && !e.getKey().isEmpty()) {
                    buffer.append(e.getKey()).append(" ");
                }

                // mandatory type
                buffer.append(e.getValue());
            }

            buffer.append(">");
        }

        if (structType.getArguments() != null && !structType.getArguments().isEmpty()) {
            if (structType.getDialect() == StructType.Dialect.DUCKDB) {
                buffer.append("{ ");
                int i = 0;
                for (SelectItem<?> e : structType.getArguments()) {
                    if (0 < i++) {
                        buffer.append(",");
                    }
                    buffer.append(e.getAlias().getName());
                    buffer.append(" : ");
                    e.getExpression().accept(this, context);
                }
                buffer.append(" }");
            } else {
                buffer.append("(");
                int i = 0;
                for (SelectItem<?> e : structType.getArguments()) {
                    if (0 < i++) {
                        buffer.append(",");
                    }
                    e.getExpression().accept(this, context);
                    if (e.getAlias() != null) {
                        buffer.append(" as ");
                        buffer.append(e.getAlias().getName());
                    }
                }
                buffer.append(")");
            }
        }

        if (structType.getDialect() == StructType.Dialect.DUCKDB
                && structType.getParameters() != null && !structType.getParameters().isEmpty()) {
            buffer.append("::STRUCT( ");
            int i = 0;
            for (Map.Entry<String, ColDataType> e : structType.getParameters()) {
                if (0 < i++) {
                    buffer.append(",");
                }
                buffer.append(e.getKey()).append(" ");
                buffer.append(e.getValue());
            }
            buffer.append(")");
        }
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(LambdaExpression lambdaExpression, S context) {
        if (lambdaExpression.getIdentifiers().size() == 1) {
            buffer.append(lambdaExpression.getIdentifiers().get(0));
        } else {
            int i = 0;
            buffer.append("( ");
            for (String s : lambdaExpression.getIdentifiers()) {
                buffer.append(i++ > 0 ? ", " : "").append(s);
            }
            buffer.append(" )");
        }

        buffer.append(" -> ");
        lambdaExpression.getExpression().accept(this, context);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(HighExpression highExpression, S context) {
        return buffer.append(highExpression.toString());
    }

    @Override
    public <S> StringBuilder visit(LowExpression lowExpression, S context) {
        return buffer.append(lowExpression.toString());
    }

    @Override
    public <S> StringBuilder visit(Plus plus, S context) {
        return buffer.append(plus.toString());
    }

    @Override
    public <S> StringBuilder visit(PriorTo priorTo, S context) {
        return buffer.append(priorTo.toString());
    }

    @Override
    public <S> StringBuilder visit(Inverse inverse, S context) {
        return buffer.append(inverse.toString());
    }

    @Override
    public <S> StringBuilder visit(CosineSimilarity cosineSimilarity, S context) {
        deparse(cosineSimilarity,
                " " + cosineSimilarity.getStringExpression() + " ", context);
        return buffer;
    }
}

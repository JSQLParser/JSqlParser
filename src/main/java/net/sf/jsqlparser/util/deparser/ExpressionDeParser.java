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
    public <S> StringBuilder visit(Addition addition, S parameters) {
        visitBinaryExpression(addition, " + ", null);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(AndExpression andExpression, S parameters) {
        visitBinaryExpression(andExpression, andExpression.isUseOperator() ? " && " : " AND ",
                null);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(Between between, S parameters) {
        between.getLeftExpression().accept(this, parameters);
        if (between.isNot()) {
            buffer.append(" NOT");
        }

        buffer.append(" BETWEEN ");
        between.getBetweenExpressionStart().accept(this, parameters);
        buffer.append(" AND ");
        between.getBetweenExpressionEnd().accept(this, parameters);

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(OverlapsCondition overlapsCondition, S parameters) {
        buffer.append(overlapsCondition.toString());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(EqualsTo equalsTo, S parameters) {
        visitOldOracleJoinBinaryExpression(equalsTo, " = ", null);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(Division division, S parameters) {
        visitBinaryExpression(division, " / ", null);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(IntegerDivision division, S parameters) {
        visitBinaryExpression(division, " DIV ", null);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(DoubleValue doubleValue, S parameters) {
        buffer.append(doubleValue.toString());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(HexValue hexValue, S parameters) {
        buffer.append(hexValue.toString());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(NotExpression notExpr, S parameters) {
        if (notExpr.isExclamationMark()) {
            buffer.append("! ");
        } else {
            buffer.append(NOT);
        }
        notExpr.getExpression().accept(this, parameters);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(BitwiseRightShift expr, S parameters) {
        visitBinaryExpression(expr, " >> ", null);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(BitwiseLeftShift expr, S parameters) {
        visitBinaryExpression(expr, " << ", null);
        return buffer;
    }

    public <S> StringBuilder visitOldOracleJoinBinaryExpression(
            OldOracleJoinBinaryExpression expression,
            String operator, S parameters) {
        // if (expression.isNot()) {
        // buffer.append(NOT);
        // }
        expression.getLeftExpression().accept(this, parameters);
        if (expression.getOldOracleJoinSyntax() == EqualsTo.ORACLE_JOIN_RIGHT) {
            buffer.append("(+)");
        }
        buffer.append(operator);
        expression.getRightExpression().accept(this, parameters);
        if (expression.getOldOracleJoinSyntax() == EqualsTo.ORACLE_JOIN_LEFT) {
            buffer.append("(+)");
        }

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(GreaterThan greaterThan, S parameters) {
        visitOldOracleJoinBinaryExpression(greaterThan, " > ", null);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(GreaterThanEquals greaterThanEquals, S parameters) {
        visitOldOracleJoinBinaryExpression(greaterThanEquals, " >= ", null);

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(InExpression inExpression, S parameters) {
        inExpression.getLeftExpression().accept(this, parameters);
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
        inExpression.getRightExpression().accept(this, parameters);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(IncludesExpression includesExpression, S parameters) {
        includesExpression.getLeftExpression().accept(this, parameters);
        buffer.append(" INCLUDES ");
        includesExpression.getRightExpression().accept(this, parameters);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(ExcludesExpression excludesExpression, S parameters) {
        excludesExpression.getLeftExpression().accept(this, parameters);
        buffer.append(" EXCLUDES ");
        excludesExpression.getRightExpression().accept(this, parameters);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(FullTextSearch fullTextSearch, S parameters) {
        // Build a list of matched columns
        String columnsListCommaSeperated = "";
        Iterator<Column> iterator = fullTextSearch.getMatchColumns().iterator();
        while (iterator.hasNext()) {
            Column col = iterator.next();
            columnsListCommaSeperated += col.getFullyQualifiedName();
            if (iterator.hasNext()) {
                columnsListCommaSeperated += ",";
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
    public <S> StringBuilder visit(SignedExpression signedExpression, S parameters) {
        buffer.append(signedExpression.getSign());
        signedExpression.getExpression().accept(this, parameters);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(IsNullExpression isNullExpression, S parameters) {
        isNullExpression.getLeftExpression().accept(this, parameters);
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
    public <S> StringBuilder visit(IsBooleanExpression isBooleanExpression, S parameters) {
        isBooleanExpression.getLeftExpression().accept(this, parameters);
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
    public <S> StringBuilder visit(JdbcParameter jdbcParameter, S parameters) {
        buffer.append(jdbcParameter.getParameterCharacter());
        if (jdbcParameter.isUseFixedIndex()) {
            buffer.append(jdbcParameter.getIndex());
        }

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(LikeExpression likeExpression, S parameters) {
        String keywordStr = likeExpression.getLikeKeyWord() == LikeExpression.KeyWord.SIMILAR_TO
                ? " SIMILAR TO"
                : likeExpression.getLikeKeyWord().toString();

        likeExpression.getLeftExpression().accept(this, parameters);
        buffer.append(" ");
        if (likeExpression.isNot()) {
            buffer.append("NOT ");
        }
        buffer.append(keywordStr).append(" ");
        if (likeExpression.isUseBinary()) {
            buffer.append("BINARY ");
        }
        likeExpression.getRightExpression().accept(this, parameters);

        Expression escape = likeExpression.getEscape();
        if (escape != null) {
            buffer.append(" ESCAPE ");
            likeExpression.getEscape().accept(this, parameters);
        }
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(ExistsExpression existsExpression, S parameters) {
        if (existsExpression.isNot()) {
            buffer.append("NOT EXISTS ");
        } else {
            buffer.append("EXISTS ");
        }
        existsExpression.getRightExpression().accept(this, parameters);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(MemberOfExpression memberOfExpression, S parameters) {
        memberOfExpression.getLeftExpression().accept(this, parameters);
        if (memberOfExpression.isNot()) {
            buffer.append(" NOT MEMBER OF ");
        } else {
            buffer.append(" MEMBER OF ");
        }
        memberOfExpression.getRightExpression().accept(this, parameters);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(LongValue longValue, S parameters) {
        buffer.append(longValue.getStringValue());

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(MinorThan minorThan, S parameters) {
        visitOldOracleJoinBinaryExpression(minorThan, " < ", null);

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(MinorThanEquals minorThanEquals, S parameters) {
        visitOldOracleJoinBinaryExpression(minorThanEquals, " <= ", null);

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(Multiplication multiplication, S parameters) {
        visitBinaryExpression(multiplication, " * ", null);

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(NotEqualsTo notEqualsTo, S parameters) {
        visitOldOracleJoinBinaryExpression(notEqualsTo,
                " " + notEqualsTo.getStringExpression() + " ", null);

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(DoubleAnd doubleAnd, S parameters) {
        visitOldOracleJoinBinaryExpression(doubleAnd, " " + doubleAnd.getStringExpression() + " ",
                null);

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(Contains contains, S parameters) {
        visitOldOracleJoinBinaryExpression(contains, " " + contains.getStringExpression() + " ",
                null);

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(ContainedBy containedBy, S parameters) {
        visitOldOracleJoinBinaryExpression(containedBy,
                " " + containedBy.getStringExpression() + " ", null);

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(NullValue nullValue, S parameters) {
        buffer.append(nullValue.toString());

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(OrExpression orExpression, S parameters) {
        visitBinaryExpression(orExpression, " OR ", null);

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(XorExpression xorExpression, S parameters) {
        visitBinaryExpression(xorExpression, " XOR ", null);

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(StringValue stringValue, S parameters) {
        if (stringValue.getPrefix() != null) {
            buffer.append(stringValue.getPrefix());
        }
        buffer.append("'").append(stringValue.getValue()).append("'");

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(Subtraction subtraction, S parameters) {
        visitBinaryExpression(subtraction, " - ", null);
        return buffer;
    }

    protected <S> StringBuilder visitBinaryExpression(BinaryExpression binaryExpression,
            String operator, S parameters) {
        binaryExpression.getLeftExpression().accept(this, parameters);
        buffer.append(operator);
        binaryExpression.getRightExpression().accept(this, parameters);

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(Select select, S parameters) {
        if (selectVisitor != null) {
            if (select.getWithItemsList() != null) {
                buffer.append("WITH ");
                for (Iterator<WithItem> iter = select.getWithItemsList().iterator(); iter
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
    public <S> StringBuilder visit(TranscodingFunction transcodingFunction, S parameters) {
        if (transcodingFunction.isTranscodeStyle()) {
            buffer.append("CONVERT( ");
            transcodingFunction.getExpression().accept(this, parameters);
            buffer.append(" USING ")
                    .append(transcodingFunction.getTranscodingName())
                    .append(" )");
        } else {
            buffer
                    .append("CONVERT( ")
                    .append(transcodingFunction.getColDataType())
                    .append(", ");
            transcodingFunction.getExpression().accept(this, parameters);

            String transCodingName = transcodingFunction.getTranscodingName();
            if (transCodingName != null && !transCodingName.isEmpty()) {
                buffer.append(", ").append(transCodingName);
            }
            buffer.append(" )");
        }

        return buffer;
    }

    public <S> StringBuilder visit(TrimFunction trimFunction, S parameters) {
        buffer.append("Trim(");

        if (trimFunction.getTrimSpecification() != null) {
            buffer.append(" ").append(trimFunction.getTrimSpecification());
        }

        if (trimFunction.getExpression() != null) {
            buffer.append(" ");
            trimFunction.getExpression().accept(this, parameters);
        }

        if (trimFunction.getFromExpression() != null) {
            buffer.append(trimFunction.isUsingFromKeyword() ? " FROM " : ", ");
            trimFunction.getFromExpression().accept(this, parameters);
        }
        buffer.append(" )");
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(RangeExpression rangeExpression, S parameters) {
        rangeExpression.getStartExpression().accept(this, parameters);
        buffer.append(":");
        rangeExpression.getEndExpression().accept(this, parameters);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(Column tableColumn, S parameters) {
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
            tableColumn.getArrayConstructor().accept(this, parameters);
        }
        return buffer;
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public <S> StringBuilder visit(Function function, S parameters) {
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
            if (function.getNamedParameters() != null) {
                function.getNamedParameters().accept(this, parameters);
            }
            if (function.getParameters() != null) {
                function.getParameters().accept(this, parameters);
            }

            Function.HavingClause havingClause = function.getHavingClause();
            if (havingClause != null) {
                buffer.append(" HAVING ").append(havingClause.getHavingType()).append(" ");
                havingClause.getExpression().accept(this, parameters);
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
    public <S> StringBuilder visit(ParenthesedSelect selectBody, S parameters) {
        selectBody.getSelect().accept(this, parameters);
        return buffer;
    }

    public SelectVisitor<StringBuilder> getSelectVisitor() {
        return selectVisitor;
    }

    public void setSelectVisitor(SelectVisitor<StringBuilder> visitor) {
        selectVisitor = visitor;
    }

    @Override
    public <S> StringBuilder visit(DateValue dateValue, S parameters) {
        buffer.append("{d '").append(dateValue.getValue().toString()).append("'}");
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(TimestampValue timestampValue, S parameters) {
        buffer.append("{ts '").append(timestampValue.getValue().toString()).append("'}");
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(TimeValue timeValue, S parameters) {
        buffer.append("{t '").append(timeValue.getValue().toString()).append("'}");
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(CaseExpression caseExpression, S parameters) {
        buffer.append(caseExpression.isUsingBrackets() ? "(" : "").append("CASE ");
        Expression switchExp = caseExpression.getSwitchExpression();
        if (switchExp != null) {
            switchExp.accept(this, parameters);
            buffer.append(" ");
        }

        for (Expression exp : caseExpression.getWhenClauses()) {
            exp.accept(this, parameters);
        }

        Expression elseExp = caseExpression.getElseExpression();
        if (elseExp != null) {
            buffer.append("ELSE ");
            elseExp.accept(this, parameters);
            buffer.append(" ");
        }

        buffer.append("END").append(caseExpression.isUsingBrackets() ? ")" : "");
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(WhenClause whenClause, S parameters) {
        buffer.append("WHEN ");
        whenClause.getWhenExpression().accept(this, parameters);
        buffer.append(" THEN ");
        whenClause.getThenExpression().accept(this, parameters);
        buffer.append(" ");
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(AnyComparisonExpression anyComparisonExpression, S parameters) {
        buffer.append(anyComparisonExpression.getAnyType().name());

        // VALUES or SELECT
        anyComparisonExpression.getSelect().accept(this, parameters);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(Concat concat, S parameters) {
        visitBinaryExpression(concat, " || ", null);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(Matches matches, S parameters) {
        visitOldOracleJoinBinaryExpression(matches, " @@ ", null);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(BitwiseAnd bitwiseAnd, S parameters) {
        visitBinaryExpression(bitwiseAnd, " & ", null);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(BitwiseOr bitwiseOr, S parameters) {
        visitBinaryExpression(bitwiseOr, " | ", null);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(BitwiseXor bitwiseXor, S parameters) {
        visitBinaryExpression(bitwiseXor, " ^ ", null);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(CastExpression cast, S parameters) {
        if (cast.isImplicitCast()) {
            buffer.append(cast.getColDataType()).append(" ");
            cast.getLeftExpression().accept(this, parameters);
        } else if (cast.isUseCastKeyword()) {
            String formatStr = cast.getFormat() != null && !cast.getFormat().isEmpty()
                    ? " FORMAT " + cast.getFormat()
                    : "";

            buffer.append(cast.keyword).append("(");
            cast.getLeftExpression().accept(this, parameters);
            buffer.append(" AS ");
            buffer.append(
                    cast.getColumnDefinitions().size() > 1
                            ? "ROW(" + Select.getStringList(cast.getColumnDefinitions()) + ")"
                            : cast.getColDataType().toString());
            buffer.append(formatStr);
            buffer.append(")");
        } else {
            cast.getLeftExpression().accept(this, parameters);
            buffer.append("::");
            buffer.append(cast.getColDataType());
        }
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(Modulo modulo, S parameters) {
        visitBinaryExpression(modulo, " % ", null);
        return buffer;
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ExcessiveMethodLength",
            "PMD.MissingBreakInSwitch"})
    public <S> StringBuilder visit(AnalyticExpression aexpr, S parameters) {
        String name = aexpr.getName();
        Expression expression = aexpr.getExpression();
        Expression offset = aexpr.getOffset();
        Expression defaultValue = aexpr.getDefaultValue();
        boolean isAllColumns = aexpr.isAllColumns();
        KeepExpression keep = aexpr.getKeep();
        ExpressionList partitionExpressionList = aexpr.getPartitionExpressionList();
        List<OrderByElement> orderByElements = aexpr.getOrderByElements();
        WindowElement windowElement = aexpr.getWindowElement();

        buffer.append(name).append("(");
        if (aexpr.isDistinct()) {
            buffer.append("DISTINCT ");
        }
        if (aexpr.isUnique()) {
            buffer.append("UNIQUE ");
        }
        if (expression != null) {
            expression.accept(this, parameters);
            if (offset != null) {
                buffer.append(", ");
                offset.accept(this, parameters);
                if (defaultValue != null) {
                    buffer.append(", ");
                    defaultValue.accept(this, parameters);
                }
            }
        } else if (isAllColumns) {
            buffer.append("*");
        }
        Function.HavingClause havingClause = aexpr.getHavingClause();
        if (havingClause != null) {
            buffer.append(" HAVING ").append(havingClause.getHavingType()).append(" ");
            havingClause.getExpression().accept(this, parameters);
        }

        if (aexpr.getNullHandling() != null && !aexpr.isIgnoreNullsOutside()) {
            switch (aexpr.getNullHandling()) {
                case IGNORE_NULLS:
                    buffer.append(" IGNORE NULLS");
                    break;
                case RESPECT_NULLS:
                    buffer.append(" RESPECT NULLS");
                    break;
            }
        }
        if (aexpr.getFuncOrderBy() != null) {
            buffer.append(" ORDER BY ");
            buffer.append(aexpr.getFuncOrderBy().stream().map(OrderByElement::toString)
                    .collect(joining(", ")));
        }

        if (aexpr.getLimit() != null) {
            new LimitDeparser(this, buffer).deParse(aexpr.getLimit());
        }

        buffer.append(") ");
        if (keep != null) {
            keep.accept(this, parameters);
            buffer.append(" ");
        }

        if (aexpr.getFilterExpression() != null) {
            buffer.append("FILTER (WHERE ");
            aexpr.getFilterExpression().accept(this, parameters);
            buffer.append(")");
            if (aexpr.getType() != AnalyticType.FILTER_ONLY) {
                buffer.append(" ");
            }
        }

        if (aexpr.getNullHandling() != null && aexpr.isIgnoreNullsOutside()) {
            switch (aexpr.getNullHandling()) {
                case IGNORE_NULLS:
                    buffer.append(" IGNORE NULLS ");
                    break;
                case RESPECT_NULLS:
                    buffer.append(" RESPECT NULLS ");
                    break;
            }
        }

        switch (aexpr.getType()) {
            case FILTER_ONLY:
                return null;
            case WITHIN_GROUP:
                buffer.append("WITHIN GROUP");
                break;
            case WITHIN_GROUP_OVER:
                buffer.append("WITHIN GROUP (");
                aexpr.getWindowDefinition().getOrderBy().toStringOrderByElements(buffer);
                buffer.append(") OVER (");
                aexpr.getWindowDefinition().getPartitionBy().toStringPartitionBy(buffer);
                buffer.append(")");
                break;
            default:
                buffer.append("OVER");
        }

        if (aexpr.getWindowName() != null) {
            buffer.append(" ").append(aexpr.getWindowName());
        } else if (aexpr.getType() != AnalyticType.WITHIN_GROUP_OVER) {
            buffer.append(" (");

            if (partitionExpressionList != null
                    && !partitionExpressionList.getExpressions().isEmpty()) {
                buffer.append("PARTITION BY ");
                if (aexpr.isPartitionByBrackets()) {
                    buffer.append("(");
                }
                List<Expression> expressions = partitionExpressionList.getExpressions();
                for (int i = 0; i < expressions.size(); i++) {
                    if (i > 0) {
                        buffer.append(", ");
                    }
                    expressions.get(i).accept(this, parameters);
                }
                if (aexpr.isPartitionByBrackets()) {
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
    public <S> StringBuilder visit(ExtractExpression eexpr, S parameters) {
        buffer.append("EXTRACT(").append(eexpr.getName());
        buffer.append(" FROM ");
        eexpr.getExpression().accept(this, parameters);
        buffer.append(')');
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(IntervalExpression intervalExpression, S parameters) {
        if (intervalExpression.isUsingIntervalKeyword()) {
            buffer.append("INTERVAL ");
        }
        if (intervalExpression.getExpression() != null) {
            intervalExpression.getExpression().accept(this, parameters);
        } else {
            buffer.append(intervalExpression.getParameter());
        }
        if (intervalExpression.getIntervalType() != null) {
            buffer.append(" ").append(intervalExpression.getIntervalType());
        }
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(JdbcNamedParameter jdbcNamedParameter, S parameters) {
        buffer.append(jdbcNamedParameter.toString());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(OracleHierarchicalExpression oexpr, S parameters) {
        buffer.append(oexpr.toString());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(RegExpMatchOperator rexpr, S parameters) {
        visitBinaryExpression(rexpr, " " + rexpr.getStringExpression() + " ", null);
        return buffer;
    }


    @Override
    public <S> StringBuilder visit(JsonExpression jsonExpr, S parameters) {
        buffer.append(jsonExpr.toString());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(JsonOperator jsonExpr, S parameters) {
        visitBinaryExpression(jsonExpr, " " + jsonExpr.getStringExpression() + " ", null);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(UserVariable var, S parameters) {
        buffer.append(var.toString());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(NumericBind bind, S parameters) {
        buffer.append(bind.toString());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(KeepExpression aexpr, S parameters) {
        buffer.append(aexpr.toString());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(MySQLGroupConcat groupConcat, S parameters) {
        buffer.append(groupConcat.toString());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(ExpressionList<?> expressionList, S parameters) {
        ExpressionListDeParser<?> expressionListDeParser =
                new ExpressionListDeParser<>(this, buffer);
        expressionListDeParser.deParse(expressionList);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(RowConstructor<?> rowConstructor, S parameters) {
        if (rowConstructor.getName() != null) {
            buffer.append(rowConstructor.getName());
        }
        ExpressionListDeParser<?> expressionListDeParser =
                new ExpressionListDeParser<>(this, buffer);
        expressionListDeParser.deParse(rowConstructor);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(RowGetExpression rowGetExpression, S parameters) {
        rowGetExpression.getExpression().accept(this, parameters);
        buffer.append(".").append(rowGetExpression.getColumnName());
        return null;
    }

    @Override
    public <S> StringBuilder visit(OracleHint hint, S parameters) {
        buffer.append(hint.toString());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(TimeKeyExpression timeKeyExpression, S parameters) {
        buffer.append(timeKeyExpression.toString());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(DateTimeLiteralExpression literal, S parameters) {
        buffer.append(literal.toString());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(NextValExpression nextVal, S parameters) {
        buffer.append(nextVal.isUsingNextValueFor() ? "NEXT VALUE FOR " : "NEXTVAL FOR ")
                .append(nextVal.getName());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(CollateExpression col, S parameters) {
        buffer.append(col.getLeftExpression().toString()).append(" COLLATE ")
                .append(col.getCollate());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(SimilarToExpression expr, S parameters) {
        visitBinaryExpression(expr, (expr.isNot() ? " NOT" : "") + " SIMILAR TO ", null);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(ArrayExpression array, S parameters) {
        array.getObjExpression().accept(this, parameters);
        buffer.append("[");
        if (array.getIndexExpression() != null) {
            array.getIndexExpression().accept(this, parameters);
        } else {
            if (array.getStartIndexExpression() != null) {
                array.getStartIndexExpression().accept(this, parameters);
            }
            buffer.append(":");
            if (array.getStopIndexExpression() != null) {
                array.getStopIndexExpression().accept(this, parameters);
            }
        }

        buffer.append("]");
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(ArrayConstructor aThis, S parameters) {
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
            expression.accept(this, parameters);
        }
        buffer.append("]");
        return buffer;
    }

    @Override
    void deParse(Expression statement) {
        statement.accept(this, null);
    }

    @Override
    public <S> StringBuilder visit(VariableAssignment var, S parameters) {
        var.getVariable().accept(this, parameters);
        buffer.append(" ").append(var.getOperation()).append(" ");
        var.getExpression().accept(this, parameters);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(XMLSerializeExpr expr, S parameters) {
        // xmlserialize(xmlagg(xmltext(COMMENT_LINE) ORDER BY COMMENT_SEQUENCE) as varchar(1024))
        buffer.append("xmlserialize(xmlagg(xmltext(");
        expr.getExpression().accept(this, parameters);
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
    public <S> StringBuilder visit(TimezoneExpression var, S parameters) {
        var.getLeftExpression().accept(this, parameters);

        for (Expression expr : var.getTimezoneExpressions()) {
            buffer.append(" AT TIME ZONE ");
            expr.accept(this, parameters);
        }
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(JsonAggregateFunction expression, S parameters) {
        expression.append(buffer);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(JsonFunction expression, S parameters) {
        expression.append(buffer);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(ConnectByRootOperator connectByRootOperator, S parameters) {
        buffer.append("CONNECT_BY_ROOT ");
        connectByRootOperator.getColumn().accept(this, parameters);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(OracleNamedFunctionParameter oracleNamedFunctionParameter,
            S parameters) {
        buffer.append(oracleNamedFunctionParameter.getName()).append(" => ");

        oracleNamedFunctionParameter.getExpression().accept(this, parameters);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(AllColumns allColumns, S parameters) {
        buffer.append(allColumns.toString());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(AllTableColumns allTableColumns, S parameters) {
        buffer.append(allTableColumns.toString());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(AllValue allValue, S parameters) {
        buffer.append(allValue);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(IsDistinctExpression isDistinctExpression, S parameters) {
        buffer.append(isDistinctExpression.getLeftExpression())
                .append(isDistinctExpression.getStringExpression())
                .append(isDistinctExpression.getRightExpression());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(GeometryDistance geometryDistance, S parameters) {
        visitOldOracleJoinBinaryExpression(geometryDistance,
                " " + geometryDistance.getStringExpression() + " ", null);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(TSQLLeftJoin tsqlLeftJoin, S parameters) {
        visitBinaryExpression(tsqlLeftJoin, " *= ", null);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(TSQLRightJoin tsqlRightJoin, S parameters) {
        visitBinaryExpression(tsqlRightJoin, " =* ", null);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(StructType structType, S parameters) {
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
                    e.getExpression().accept(this, parameters);
                }
                buffer.append(" }");
            } else {
                buffer.append("(");
                int i = 0;
                for (SelectItem<?> e : structType.getArguments()) {
                    if (0 < i++) {
                        buffer.append(",");
                    }
                    e.getExpression().accept(this, parameters);
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
    public <S> StringBuilder visit(LambdaExpression lambdaExpression, S parameters) {
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
        lambdaExpression.getExpression().accept(this, parameters);
        return buffer;
    }

}

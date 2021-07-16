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

import java.util.Iterator;
import java.util.List;
import static java.util.stream.Collectors.joining;

import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.AnalyticType;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.ArrayExpression;
import net.sf.jsqlparser.expression.ArrayConstructor;
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
import net.sf.jsqlparser.expression.JsonAggregateFunction;
import net.sf.jsqlparser.expression.JsonExpression;
import net.sf.jsqlparser.expression.JsonFunction;
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
import net.sf.jsqlparser.expression.RowGetExpression;
import net.sf.jsqlparser.expression.SignedExpression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeKeyExpression;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.TimezoneExpression;
import net.sf.jsqlparser.expression.UserVariable;
import net.sf.jsqlparser.expression.ValueListExpression;
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
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.FullTextSearch;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsBooleanExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.expression.operators.relational.JsonOperator;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NamedExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.expression.operators.relational.OldOracleJoinBinaryExpression;
import net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperator;
import net.sf.jsqlparser.expression.operators.relational.RegExpMySQLOperator;
import net.sf.jsqlparser.expression.operators.relational.SimilarToExpression;
import net.sf.jsqlparser.expression.operators.relational.SupportsOldOracleJoinSyntax;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.WithItem;

@SuppressWarnings({"PMD.CyclomaticComplexity"})
public class ExpressionDeParser extends AbstractDeParser<Expression>
        // FIXME maybe we should implement an ItemsListDeparser too?
        implements ExpressionVisitor, ItemsListVisitor {

    private static final String NOT = "NOT ";
    private SelectVisitor selectVisitor;
    private OrderByDeParser orderByDeParser = new OrderByDeParser();

    public ExpressionDeParser() {
        super(new StringBuilder());
    }

    public ExpressionDeParser(SelectVisitor selectVisitor, StringBuilder buffer) {
        this(selectVisitor, buffer, new OrderByDeParser());
    }

    ExpressionDeParser(SelectVisitor selectVisitor, StringBuilder buffer, OrderByDeParser orderByDeParser) {
        super(buffer);
        this.selectVisitor = selectVisitor;
        this.orderByDeParser = orderByDeParser;
    }

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
        if (between.isNot()) {
            buffer.append(" NOT");
        }

        buffer.append(" BETWEEN ");
        between.getBetweenExpressionStart().accept(this);
        buffer.append(" AND ");
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
        buffer.append(doubleValue.toString());
    }

    @Override
    public void visit(HexValue hexValue) {
        buffer.append(hexValue.toString());
    }

    @Override
    public void visit(NotExpression notExpr) {
        if (notExpr.isExclamationMark()) {
            buffer.append("! ");
        } else {
            buffer.append(NOT);
        }
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
//        if (expression.isNot()) {
//            buffer.append(NOT);
//        }
        expression.getLeftExpression().accept(this);
        if (expression.getOldOracleJoinSyntax() == EqualsTo.ORACLE_JOIN_RIGHT) {
            buffer.append("(+)");
        }
        buffer.append(operator);
        expression.getRightExpression().accept(this);
        if (expression.getOldOracleJoinSyntax() == EqualsTo.ORACLE_JOIN_LEFT) {
            buffer.append("(+)");
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
        inExpression.getLeftExpression().accept(this);
        if (inExpression.getOldOracleJoinSyntax() == SupportsOldOracleJoinSyntax.ORACLE_JOIN_RIGHT) {
            buffer.append("(+)");
        }
        if (inExpression.isNot()) {
            buffer.append(" NOT");
        }
        buffer.append(" IN ");

        if (inExpression.getMultiExpressionList() != null) {
            parseMultiExpressionList(inExpression);
        } else {
            if (inExpression.getRightExpression() != null) {
                inExpression.getRightExpression().accept(this);
            } else {
                inExpression.getRightItemsList().accept(this);
            }
        }
    }

    /**
     * Produces a multi-expression in clause: {@code ((a, b), (c, d))}
     */
    private void parseMultiExpressionList(InExpression inExpression) {
        MultiExpressionList multiExprList = inExpression.getMultiExpressionList();
        buffer.append("(");
        for (Iterator<ExpressionList> it = multiExprList.getExprList().iterator(); it.hasNext();) {
            buffer.append("(");
            for (Iterator<Expression> iter = it.next().getExpressions().iterator(); iter.hasNext();) {
                Expression expression = iter.next();
                expression.accept(this);
                if (iter.hasNext()) {
                    buffer.append(", ");
                }
            }
            buffer.append(")");
            if (it.hasNext()) {
                buffer.append(", ");
            }
        }
        buffer.append(")");
    }

    @Override
    public void visit(FullTextSearch fullTextSearch) {
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
        buffer.append("MATCH (" + columnsListCommaSeperated + ") AGAINST (" + fullTextSearch.getAgainstValue()
                + (fullTextSearch.getSearchModifier() != null ? " " + fullTextSearch.getSearchModifier() : "") + ")");
    }

    @Override
    public void visit(SignedExpression signedExpression) {
        buffer.append(signedExpression.getSign());
        signedExpression.getExpression().accept(this);
    }

    @Override
    public void visit(IsNullExpression isNullExpression) {
        isNullExpression.getLeftExpression().accept(this);
        if (isNullExpression.isUseIsNull()) {
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
    }

    @Override
    public void visit(IsBooleanExpression isBooleanExpression) {
        isBooleanExpression.getLeftExpression().accept(this);
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
    }

    @Override
    public void visit(JdbcParameter jdbcParameter) {
        buffer.append("?");
        if (jdbcParameter.isUseFixedIndex()) {
            buffer.append(jdbcParameter.getIndex());
        }

    }

    @Override
    public void visit(LikeExpression likeExpression) {
        visitBinaryExpression(likeExpression,
                (likeExpression.isNot() ? " NOT" : "") + (likeExpression.isCaseInsensitive() ? " ILIKE " : " LIKE "));
        String escape = likeExpression.getEscape();
        if (escape != null) {
            buffer.append(" ESCAPE '").append(escape).append('\'');
        }
    }

    @Override
    public void visit(ExistsExpression existsExpression) {
        if (existsExpression.isNot()) {
            buffer.append("NOT EXISTS ");
        } else {
            buffer.append("EXISTS ");
        }
        existsExpression.getRightExpression().accept(this);
    }

    @Override
    public void visit(LongValue longValue) {
        buffer.append(longValue.getStringValue());

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
        buffer.append(nullValue.toString());

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
        buffer.append("(");
        parenthesis.getExpression().accept(this);
        buffer.append(")");
    }

    @Override
    public void visit(StringValue stringValue) {
        if (stringValue.getPrefix() != null) {
            buffer.append(stringValue.getPrefix());
        }
        buffer.append("'").append(stringValue.getValue()).append("'");

    }

    @Override
    public void visit(Subtraction subtraction) {
        visitBinaryExpression(subtraction, " - ");
    }

    protected void visitBinaryExpression(BinaryExpression binaryExpression, String operator) {
        binaryExpression.getLeftExpression().accept(this);
        buffer.append(operator);
        binaryExpression.getRightExpression().accept(this);

    }

    @Override
    public void visit(SubSelect subSelect) {
        if (subSelect.isUseBrackets()) {
            buffer.append("(");
        }
        if (selectVisitor != null) {
            if (subSelect.getWithItemsList() != null) {
                buffer.append("WITH ");
                for (Iterator<WithItem> iter = subSelect.getWithItemsList().iterator(); iter.hasNext();) {
                    iter.next().accept(selectVisitor);
                    if (iter.hasNext()) {
                        buffer.append(", ");
                    }
                    buffer.append(" ");
                }
                buffer.append(" ");
            }

            subSelect.getSelectBody().accept(selectVisitor);
        }
        if (subSelect.isUseBrackets()) {
            buffer.append(")");
        }
    }

    @Override
    public void visit(Column tableColumn) {
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
            buffer.append(tableName).append(".");
        }

        buffer.append(tableColumn.getColumnName());
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public void visit(Function function) {
        if (function.isEscaped()) {
            buffer.append("{fn ");
        }

        buffer.append(function.getName());
        if (function.isAllColumns() && function.getParameters() == null) {
            buffer.append("(*)");
        } else if (function.getParameters() == null && function.getNamedParameters() == null) {
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
                visit(function.getNamedParameters());
            }
            if (function.getParameters() != null) {
                visit(function.getParameters());
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
            buffer.append(")");
        }

        if (function.getAttribute() != null) {
            buffer.append(".").append(function.getAttribute());
        } else if (function.getAttributeName() != null) {
            buffer.append(".").append(function.getAttributeName());
        }
        if (function.getKeep() != null) {
            buffer.append(" ").append(function.getKeep());
        }

        if (function.isEscaped()) {
            buffer.append("}");
        }
    }

    @Override
    public void visit(ExpressionList expressionList) {
        if (expressionList.isUsingBrackets()) {
            buffer.append("(");
        }
        for (Iterator<Expression> iter = expressionList.getExpressions().iterator(); iter.hasNext();) {
            Expression expression = iter.next();
            expression.accept(this);
            if (iter.hasNext()) {
                buffer.append(", ");
            }
        }
        if (expressionList.isUsingBrackets()) {
            buffer.append(")");
        }
    }

    @Override
    public void visit(NamedExpressionList namedExpressionList) {
        List<String> names = namedExpressionList.getNames();
        List<Expression> expressions = namedExpressionList.getExpressions();
        for (int i = 0; i < names.size(); i++) {
            if (i > 0) {
                buffer.append(" ");
            }
            String name = names.get(i);
            if (!name.equals("")) {
                buffer.append(name);
                buffer.append(" ");
            }
            expressions.get(i).accept(this);
        }
    }

    public SelectVisitor getSelectVisitor() {
        return selectVisitor;
    }

    public void setSelectVisitor(SelectVisitor visitor) {
        selectVisitor = visitor;
    }

    @Override
    public void visit(DateValue dateValue) {
        buffer.append("{d '").append(dateValue.getValue().toString()).append("'}");
    }

    @Override
    public void visit(TimestampValue timestampValue) {
        buffer.append("{ts '").append(timestampValue.getValue().toString()).append("'}");
    }

    @Override
    public void visit(TimeValue timeValue) {
        buffer.append("{t '").append(timeValue.getValue().toString()).append("'}");
    }

    @Override
    public void visit(CaseExpression caseExpression) {
        buffer.append(caseExpression.isUsingBrackets() ? "(" : "").append("CASE ");
        Expression switchExp = caseExpression.getSwitchExpression();
        if (switchExp != null) {
            switchExp.accept(this);
            buffer.append(" ");
        }

        for (Expression exp : caseExpression.getWhenClauses()) {
            exp.accept(this);
        }

        Expression elseExp = caseExpression.getElseExpression();
        if (elseExp != null) {
            buffer.append("ELSE ");
            elseExp.accept(this);
            buffer.append(" ");
        }

        buffer.append("END").append(caseExpression.isUsingBrackets() ? ")" : "");
    }

    @Override
    public void visit(WhenClause whenClause) {
        buffer.append("WHEN ");
        whenClause.getWhenExpression().accept(this);
        buffer.append(" THEN ");
        whenClause.getThenExpression().accept(this);
        buffer.append(" ");
    }

    @Override
    public void visit(AnyComparisonExpression anyComparisonExpression) {
        buffer.append(anyComparisonExpression.getAnyType().name()).append(" ( ");
        SubSelect subSelect = anyComparisonExpression.getSubSelect();
        if (subSelect!=null) {
            subSelect.accept((ExpressionVisitor) this);
        } else {
            ExpressionList expressionList = (ExpressionList) anyComparisonExpression.getItemsList();
            buffer.append("VALUES ");
            buffer.append(
                    PlainSelect.getStringList(expressionList.getExpressions(), true, anyComparisonExpression.isUsingBracketsForValues()));
        }
        buffer.append(" ) ");     
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
        if (cast.isUseCastKeyword()) {
            buffer.append("CAST(");
            cast.getLeftExpression().accept(this);
            buffer.append(" AS ");
            buffer.append(cast.getType());
            buffer.append(")");
        } else {
            cast.getLeftExpression().accept(this);
            buffer.append("::");
            buffer.append(cast.getType());
        }
    }

    @Override
    public void visit(Modulo modulo) {
        visitBinaryExpression(modulo, " % ");
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ExcessiveMethodLength", "PMD.MissingBreakInSwitch"})
    public void visit(AnalyticExpression aexpr) {
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
            expression.accept(this);
            if (offset != null) {
                buffer.append(", ");
                offset.accept(this);
                if (defaultValue != null) {
                    buffer.append(", ");
                    defaultValue.accept(this);
                }
            }
        } else if (isAllColumns) {
            buffer.append("*");
        }
        if (aexpr.isIgnoreNulls()) {
            buffer.append(" IGNORE NULLS");
        }
        if (aexpr.getFuncOrderBy() != null) {
            buffer.append(" ORDER BY ");
            buffer.append( aexpr.getFuncOrderBy().stream().map(OrderByElement::toString).collect(joining(", ")));
        }
        
        buffer.append(") ");
        if (keep != null) {
            keep.accept(this);
            buffer.append(" ");
        }

        if (aexpr.getFilterExpression() != null) {
            buffer.append("FILTER (WHERE ");
            aexpr.getFilterExpression().accept(this);
            buffer.append(")");
            if (aexpr.getType() != AnalyticType.FILTER_ONLY) {
                buffer.append(" ");
            }
        }

        switch (aexpr.getType()) {
            case FILTER_ONLY:
                return;
            case WITHIN_GROUP:
                buffer.append("WITHIN GROUP");
                break;
            default:
                buffer.append("OVER");
        }
        buffer.append(" (");

        if (partitionExpressionList != null && !partitionExpressionList.getExpressions().isEmpty()) {
            buffer.append("PARTITION BY ");
            if (aexpr.isPartitionByBrackets()) {
                buffer.append("(");
            }
            List<Expression> expressions = partitionExpressionList.getExpressions();
            for (int i = 0; i < expressions.size(); i++) {
                if (i > 0) {
                    buffer.append(", ");
                }
                expressions.get(i).accept(this);
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

    @Override
    public void visit(ExtractExpression eexpr) {
        buffer.append("EXTRACT(").append(eexpr.getName());
        buffer.append(" FROM ");
        eexpr.getExpression().accept(this);
        buffer.append(')');
    }

    @Override
    public void visit(MultiExpressionList multiExprList) {
        for (Iterator<ExpressionList> it = multiExprList.getExprList().iterator(); it.hasNext();) {
            it.next().accept(this);
            if (it.hasNext()) {
                buffer.append(", ");
            }
        }
    }

    @Override
    public void visit(IntervalExpression iexpr) {
        buffer.append(iexpr.toString());
    }

    @Override
    public void visit(JdbcNamedParameter jdbcNamedParameter) {
        buffer.append(jdbcNamedParameter.toString());
    }

    @Override
    public void visit(OracleHierarchicalExpression oexpr) {
        buffer.append(oexpr.toString());
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
        buffer.append(jsonExpr.toString());
    }

    @Override
    public void visit(JsonOperator jsonExpr) {
        visitBinaryExpression(jsonExpr, " " + jsonExpr.getStringExpression() + " ");
    }

    @Override
    public void visit(UserVariable var) {
        buffer.append(var.toString());
    }

    @Override
    public void visit(NumericBind bind) {
        buffer.append(bind.toString());
    }

    @Override
    public void visit(KeepExpression aexpr) {
        buffer.append(aexpr.toString());
    }

    @Override
    public void visit(MySQLGroupConcat groupConcat) {
        buffer.append(groupConcat.toString());
    }

    @Override
    public void visit(ValueListExpression valueList) {
        buffer.append(valueList.toString());
    }

    @Override
    public void visit(RowConstructor rowConstructor) {
        if (rowConstructor.getName() != null) {
            buffer.append(rowConstructor.getName());
        }
        buffer.append("(");
        boolean first = true;
        for (Expression expr : rowConstructor.getExprList().getExpressions()) {
            if (first) {
                first = false;
            } else {
                buffer.append(", ");
            }
            expr.accept(this);
        }
        buffer.append(")");
    }

    @Override
    public void visit(RowGetExpression rowGetExpression) {
        rowGetExpression.getExpression().accept(this);
        buffer.append(".").append(rowGetExpression.getColumnName());
    }

    @Override
    public void visit(OracleHint hint) {
        buffer.append(hint.toString());
    }

    @Override
    public void visit(TimeKeyExpression timeKeyExpression) {
        buffer.append(timeKeyExpression.toString());
    }

    @Override
    public void visit(DateTimeLiteralExpression literal) {
        buffer.append(literal.toString());
    }

    @Override
    public void visit(NextValExpression nextVal) {
        buffer.append(nextVal.isUsingNextValueFor()  ? "NEXT VALUE FOR " : "NEXTVAL FOR ").append(nextVal.getName());
    }

    @Override
    public void visit(CollateExpression col) {
        buffer.append(col.getLeftExpression().toString()).append(" COLLATE ").append(col.getCollate());
    }

    @Override
    public void visit(SimilarToExpression expr) {
        visitBinaryExpression(expr, (expr.isNot() ? " NOT" : "") + " SIMILAR TO ");
    }

    @Override
    public void visit(ArrayExpression array) {
        array.getObjExpression().accept(this);
        buffer.append("[");
        if (array.getIndexExpression() != null) {
            array.getIndexExpression().accept(this);
        } else {
            if (array.getStartIndexExpression() != null) {
                array.getStartIndexExpression().accept(this);
            }
            buffer.append(":");
            if (array.getStopIndexExpression() != null) {
                array.getStopIndexExpression().accept(this);
            }
        }

        buffer.append("]");
    }

    @Override
    public void visit(ArrayConstructor aThis) {
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
            expression.accept(this);
        }
        buffer.append("]");
    }

    @Override
    void deParse(Expression statement) {
        statement.accept(this);
    }

    @Override
    public void visit(VariableAssignment var) {
        var.getVariable().accept(this);
        buffer.append(" ").append(var.getOperation()).append(" ");
        var.getExpression().accept(this);
    }

    @Override
    public void visit(XMLSerializeExpr expr) {
        //xmlserialize(xmlagg(xmltext(COMMENT_LINE) ORDER BY COMMENT_SEQUENCE) as varchar(1024))
        buffer.append("xmlserialize(xmlagg(xmltext(");
        expr.getExpression().accept(this);
        buffer.append(")");
        if (expr.getOrderByElements() != null){
            buffer.append(" ORDER BY ");
            for (Iterator<OrderByElement> i = expr.getOrderByElements().iterator(); i.hasNext();) {
                buffer.append(i.next().toString());
                if (i.hasNext()) {
                    buffer.append(", ");
                }
            }
        }
        buffer.append(") AS ").append(expr.getDataType()).append(")");
    }

    @Override
    public void visit(TimezoneExpression var) {
        var.getLeftExpression().accept(this);

        for (String expr : var.getTimezoneExpressions()) {
            buffer.append(" AT TIME ZONE " + expr);
        }
    }

    @Override
    public void visit(JsonAggregateFunction expression) {
        expression.append(buffer);
    }

    @Override
    public void visit(JsonFunction expression) {
         expression.append(buffer);
    }
}

/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
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
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.WithItem;

public class ExpressionValidator extends AbstractValidator<Expression> implements ExpressionVisitor, ItemsListVisitor {


    @Override
    public void visit(Addition addition) {
        visitBinaryExpression(addition, " + ");
    }

    @Override
    public void visit(AndExpression andExpression) {
        visitBinaryExpression(andExpression, andExpression.isUseOperator()?" && ":" AND ");
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
    }

    @Override
    public void visit(HexValue hexValue) {
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
        expression.getLeftExpression().accept(this);
        if (expression.getOldOracleJoinSyntax() != SupportsOldOracleJoinSyntax.NO_ORACLE_JOIN) {
            Validation.mapAllExcept(errors, EnumSet.of(DatabaseType.oracle),
                    String.format("oldOracleJoinSyntax=%d not supported", expression.getOldOracleJoinSyntax()));
        }
        expression.getRightExpression().accept(this);
        if (expression.getOraclePriorPosition() != SupportsOldOracleJoinSyntax.NO_ORACLE_PRIOR) {
            Validation.mapAllExcept(errors, EnumSet.of(DatabaseType.oracle),
                    String.format("oraclePriorPosition=%d not supported", expression.getOraclePriorPosition()));
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
        if (inExpression.getLeftExpression() == null) {
            inExpression.getLeftItemsList().accept(this);
        } else {
            inExpression.getLeftExpression().accept(this);
            if (inExpression.getOldOracleJoinSyntax() == SupportsOldOracleJoinSyntax.ORACLE_JOIN_RIGHT) {
            }
        }
        if (inExpression.isNot()) {
        }

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
        for (Iterator<ExpressionList> it = multiExprList.getExprList().iterator(); it.hasNext();) {
            for (Iterator<Expression> iter = it.next().getExpressions().iterator(); iter.hasNext();) {
                Expression expression = iter.next();
                expression.accept(this);
                if (iter.hasNext()) {
                }
            }
            if (it.hasNext()) {
            }
        }
    }

    @Override
    public void visit(FullTextSearch fullTextSearch) {
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
    }

    @Override
    public void visit(LikeExpression likeExpression) {
        visitBinaryExpression(likeExpression,
                (likeExpression.isNot() ? " NOT" : "")
                + (likeExpression.isCaseInsensitive() ? " ILIKE " : " LIKE "));
    }

    @Override
    public void visit(ExistsExpression existsExpression) {
        if (existsExpression.isNot()) {
        } else {
        }
        existsExpression.getRightExpression().accept(this);
    }

    @Override
    public void visit(LongValue longValue) {

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
        if (stringValue.getPrefix() != null) {
        }

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
        if (subSelect.isUseBrackets()) {
        }
        if (subSelect.getWithItemsList() != null) {
            for (Iterator<WithItem> iter = subSelect.getWithItemsList().iterator(); iter.
                    hasNext();) {
                iter.next().accept(getValidator(SelectValidator.class));
                if (iter.hasNext()) {
                }
            }
        }

        subSelect.getSelectBody().accept(getValidator(SelectValidator.class));
        if (subSelect.isUseBrackets()) {
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
        }

    }

    @Override
    public void visit(Function function) {
        if (function.isEscaped()) {
        }

        if (function.isAllColumns() && function.getParameters() == null) {
        } else if (function.getParameters() == null && function.getNamedParameters() == null) {
        } else {
            if (function.getNamedParameters() != null) {
                visit(function.getNamedParameters());
            }
            if (function.getParameters() != null) {
                visit(function.getParameters());
            }
        }

        if (function.getAttribute() != null) {
        } else if (function.getAttributeName() != null) {
        }
        if (function.getKeep() != null) {
        }
        if (function.isEscaped()) {
        }
    }

    @Override
    public void visit(ExpressionList expressionList) {
        for (Iterator<Expression> iter = expressionList.getExpressions().iterator(); iter.hasNext();) {
            Expression expression = iter.next();
            expression.accept(this);
            if (iter.hasNext()) {
            }
        }
    }

    @Override
    public void visit(NamedExpressionList namedExpressionList) {
        List<String> names = namedExpressionList.getNames();
        List<Expression> expressions = namedExpressionList.getExpressions();
        for (int i = 0; i < names.size(); i++) {
            if (i > 0) {
            }
            String name = names.get(i);
            if (!name.equals("")) {
            }
            expressions.get(i).accept(this);
        }
    }

    @Override
    public void visit(DateValue dateValue) {
    }

    @Override
    public void visit(TimestampValue timestampValue) {
    }

    @Override
    public void visit(TimeValue timeValue) {
    }

    @Override
    public void visit(CaseExpression caseExpression) {
        Expression switchExp = caseExpression.getSwitchExpression();
        if (switchExp != null) {
            switchExp.accept(this);
        }

        for (Expression exp : caseExpression.getWhenClauses()) {
            exp.accept(this);
        }

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
        allComparisonExpression.getSubSelect().accept((ExpressionVisitor) this);
    }

    @Override
    public void visit(AnyComparisonExpression anyComparisonExpression) {
        anyComparisonExpression.getSubSelect().accept((ExpressionVisitor) this);
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
            cast.getLeftExpression().accept(this);
        } else {
            cast.getLeftExpression().accept(this);
        }
    }

    @Override
    public void visit(Modulo modulo) {
        visitBinaryExpression(modulo, " % ");
    }

    @Override
    public void visit(AnalyticExpression aexpr) {
        Expression expression = aexpr.getExpression();
        Expression offset = aexpr.getOffset();
        Expression defaultValue = aexpr.getDefaultValue();
        boolean isAllColumns = aexpr.isAllColumns();
        KeepExpression keep = aexpr.getKeep();
        ExpressionList partitionExpressionList = aexpr.getPartitionExpressionList();
        List<OrderByElement> orderByElements = aexpr.getOrderByElements();
        WindowElement windowElement = aexpr.getWindowElement();

        if (aexpr.isDistinct()) {
        }
        if (expression != null) {
            expression.accept(this);
            if (offset != null) {
                offset.accept(this);
                if (defaultValue != null) {
                    defaultValue.accept(this);
                }
            }
        } else if (isAllColumns) {
        }
        if (aexpr.isIgnoreNulls()) {
        }
        if (keep != null) {
            keep.accept(this);
        }

        if (aexpr.getFilterExpression() != null) {
            aexpr.getFilterExpression().accept(this);
        }

        switch (aexpr.getType()) {
        case WITHIN_GROUP:
            break;
        default:
        }

        if (partitionExpressionList != null && !partitionExpressionList.getExpressions().isEmpty()) {
            if (aexpr.isPartitionByBrackets()) {
            }
            List<Expression> expressions = partitionExpressionList.getExpressions();
            for (int i = 0; i < expressions.size(); i++) {
                if (i > 0) {
                }
                expressions.get(i).accept(this);
            }
            if (aexpr.isPartitionByBrackets()) {
            }
        }
        if (orderByElements != null && !orderByElements.isEmpty()) {
            //            orderByDeValidator.setExpressionVisitor(this);
            //            orderByDeValidator.setBuffer(errors);
            for (int i = 0; i < orderByElements.size(); i++) {
                if (i > 0) {
                }
                //                orderByDeParser.deParseElement(orderByElements.get(i));
            }
        }

        if (windowElement != null) {
            if (orderByElements != null && !orderByElements.isEmpty()) {
            }
        }

    }

    @Override
    public void visit(ExtractExpression eexpr) {
        eexpr.getExpression().accept(this);
    }

    @Override
    public void visit(MultiExpressionList multiExprList) {
        for (Iterator<ExpressionList> it = multiExprList.getExprList().iterator(); it.hasNext();) {
            it.next().accept(this);
            if (it.hasNext()) {
            }
        }
    }

    @Override
    public void visit(IntervalExpression iexpr) {
    }

    @Override
    public void visit(JdbcNamedParameter jdbcNamedParameter) {
    }

    @Override
    public void visit(OracleHierarchicalExpression oexpr) {
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
    }

    @Override
    public void visit(JsonOperator jsonExpr) {
        visitBinaryExpression(jsonExpr, " " + jsonExpr.getStringExpression() + " ");
    }

    @Override
    public void visit(UserVariable var) {
    }

    @Override
    public void visit(NumericBind bind) {
    }

    @Override
    public void visit(KeepExpression aexpr) {
    }

    @Override
    public void visit(MySQLGroupConcat groupConcat) {
    }

    @Override
    public void visit(ValueListExpression valueList) {
    }

    @Override
    public void visit(RowConstructor rowConstructor) {
        if (rowConstructor.getName() != null) {
        }
        boolean first = true;
        for (Expression expr : rowConstructor.getExprList().getExpressions()) {
            if (first) {
                first = false;
            } else {
            }
            expr.accept(this);
        }
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
    public void validate(Expression statement) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(VariableAssignment aThis) {
        // TODO Auto-generated method stub

    }

}

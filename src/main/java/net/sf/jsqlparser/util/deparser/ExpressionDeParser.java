/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2013 JSQLParser
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package net.sf.jsqlparser.util.deparser;

import java.util.Iterator;

import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExtractExpression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.IntervalExpression;
import net.sf.jsqlparser.expression.InverseExpression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Modulo;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SubSelect;

/**
 * A class to de-parse (that is, tranform from JSqlParser hierarchy into a
 * string) an {@link net.sf.jsqlparser.expression.Expression}
 */
public class ExpressionDeParser implements ExpressionVisitor, ItemsListVisitor {

	protected StringBuilder buffer;
	protected SelectVisitor selectVisitor;
	protected boolean useBracketsInExprList = true;

	public ExpressionDeParser() {
	}

	/**
	 * @param selectVisitor a SelectVisitor to de-parse SubSelects. It has to
	 * share the same<br> StringBuilder as this object in order to work, as:
	 *
	 * <pre>
	 * <code>
	 * StringBuilder myBuf = new StringBuilder();
	 * MySelectDeparser selectDeparser = new  MySelectDeparser();
	 * selectDeparser.setBuffer(myBuf);
	 * ExpressionDeParser expressionDeParser = new ExpressionDeParser(selectDeparser, myBuf);
	 * </code>
	 * </pre>
	 * @param buffer the buffer that will be filled with the expression
	 */
	public ExpressionDeParser(SelectVisitor selectVisitor, StringBuilder buffer) {
		this.selectVisitor = selectVisitor;
		this.buffer = buffer;
	}

	public StringBuilder getBuffer() {
		return buffer;
	}

	public void setBuffer(StringBuilder buffer) {
		this.buffer = buffer;
	}

	@Override
	public void visit(Addition addition) {
		visitBinaryExpression(addition, " + ");
	}

	@Override
	public void visit(AndExpression andExpression) {
		visitBinaryExpression(andExpression, " AND ");
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
	public void visit(Division division) {
		visitBinaryExpression(division, " / ");

	}

	@Override
	public void visit(DoubleValue doubleValue) {
		buffer.append(doubleValue.toString());

	}

	@Override
	public void visit(EqualsTo equalsTo) {
		if (equalsTo.isNot()) {
			buffer.append(" NOT ");
		}
		equalsTo.getLeftExpression().accept(this);
		if (equalsTo.getOldOracleJoinSyntax() == EqualsTo.ORACLE_JOIN_RIGHT) {
			buffer.append("(+)");
		}
		buffer.append(" = ");
		equalsTo.getRightExpression().accept(this);
		if (equalsTo.getOldOracleJoinSyntax() == EqualsTo.ORACLE_JOIN_LEFT) {
			buffer.append("(+)");
		}
	}

	@Override
	public void visit(GreaterThan greaterThan) {
		visitBinaryExpression(greaterThan, " > ");
	}

	@Override
	public void visit(GreaterThanEquals greaterThanEquals) {
		visitBinaryExpression(greaterThanEquals, " >= ");

	}

	@Override
	public void visit(InExpression inExpression) {
		if (inExpression.getLeftExpression() == null) {
			inExpression.getLeftItemsList().accept(this);
		} else {
			inExpression.getLeftExpression().accept(this);
		}
		if (inExpression.isNot()) {
			buffer.append(" NOT");
		}
		buffer.append(" IN ");

		inExpression.getRightItemsList().accept(this);
	}

	@Override
	public void visit(InverseExpression inverseExpression) {
		buffer.append("-");
		inverseExpression.getExpression().accept(this);
	}

	@Override
	public void visit(IsNullExpression isNullExpression) {
		isNullExpression.getLeftExpression().accept(this);
		if (isNullExpression.isNot()) {
			buffer.append(" IS NOT NULL");
		} else {
			buffer.append(" IS NULL");
		}
	}

	@Override
	public void visit(JdbcParameter jdbcParameter) {
		buffer.append("?");

	}

	@Override
	public void visit(LikeExpression likeExpression) {
		visitBinaryExpression(likeExpression, " LIKE ");
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
		visitBinaryExpression(minorThan, " < ");

	}

	@Override
	public void visit(MinorThanEquals minorThanEquals) {
		visitBinaryExpression(minorThanEquals, " <= ");

	}

	@Override
	public void visit(Multiplication multiplication) {
		visitBinaryExpression(multiplication, " * ");

	}

	@Override
	public void visit(NotEqualsTo notEqualsTo) {
		visitBinaryExpression(notEqualsTo, " <> ");

	}

	@Override
	public void visit(NullValue nullValue) {
		buffer.append("NULL");

	}

	@Override
	public void visit(OrExpression orExpression) {
		visitBinaryExpression(orExpression, " OR ");

	}

	@Override
	public void visit(Parenthesis parenthesis) {
		if (parenthesis.isNot()) {
			buffer.append(" NOT ");
		}

		buffer.append("(");
		parenthesis.getExpression().accept(this);
		buffer.append(")");

	}

	@Override
	public void visit(StringValue stringValue) {
		buffer.append("'").append(stringValue.getValue()).append("'");

	}

	@Override
	public void visit(Subtraction subtraction) {
		visitBinaryExpression(subtraction, " - ");

	}

	private void visitBinaryExpression(BinaryExpression binaryExpression, String operator) {
		if (binaryExpression.isNot()) {
			buffer.append(" NOT ");
		}
		binaryExpression.getLeftExpression().accept(this);
		buffer.append(operator);
		binaryExpression.getRightExpression().accept(this);

	}

	@Override
	public void visit(SubSelect subSelect) {
		buffer.append("(");
		subSelect.getSelectBody().accept(selectVisitor);
		buffer.append(")");
	}

	@Override
	public void visit(Column tableColumn) {
		String tableName = tableColumn.getTable().getAlias();
		if (tableName == null) {
			tableName = tableColumn.getTable().getWholeTableName();
		}
		if (tableName != null) {
			buffer.append(tableName).append(".");
		}

		buffer.append(tableColumn.getColumnName());
	}

	@Override
	public void visit(Function function) {
		if (function.isEscaped()) {
			buffer.append("{fn ");
		}

		buffer.append(function.getName());
		if (function.isAllColumns()) {
			buffer.append("(*)");
		} else if (function.getParameters() == null) {
			buffer.append("()");
		} else {
			boolean oldUseBracketsInExprList = useBracketsInExprList;
			if (function.isDistinct()) {
				useBracketsInExprList = false;
				buffer.append("(DISTINCT ");
			}
			visit(function.getParameters());
			useBracketsInExprList = oldUseBracketsInExprList;
			if (function.isDistinct()) {
				buffer.append(")");
			}
		}

		if (function.isEscaped()) {
			buffer.append("}");
		}

	}

	@Override
	public void visit(ExpressionList expressionList) {
		if (useBracketsInExprList) {
			buffer.append("(");
		}
		for (Iterator<Expression> iter = expressionList.getExpressions().iterator(); iter.hasNext();) {
			Expression expression = iter.next();
			expression.accept(this);
			if (iter.hasNext()) {
				buffer.append(", ");
			}
		}
		if (useBracketsInExprList) {
			buffer.append(")");
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
		buffer.append("CASE ");
		Expression switchExp = caseExpression.getSwitchExpression();
		if (switchExp != null) {
			switchExp.accept(this);
			buffer.append(" ");
		}

		for (Iterator<Expression> iter = caseExpression.getWhenClauses().iterator(); iter.hasNext();) {
			Expression exp = (Expression) iter.next();
			exp.accept(this);
		}

		Expression elseExp = caseExpression.getElseExpression();
		if (elseExp != null) {
			buffer.append("ELSE ");
			elseExp.accept(this);
			buffer.append(" ");
		}

		buffer.append("END");
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
	public void visit(AllComparisonExpression allComparisonExpression) {
		buffer.append(" ALL ");
		allComparisonExpression.GetSubSelect().accept((ExpressionVisitor) this);
	}

	@Override
	public void visit(AnyComparisonExpression anyComparisonExpression) {
		buffer.append(" ANY ");
		anyComparisonExpression.GetSubSelect().accept((ExpressionVisitor) this);
	}

	@Override
	public void visit(Concat concat) {
		visitBinaryExpression(concat, " || ");
	}

	@Override
	public void visit(Matches matches) {
		visitBinaryExpression(matches, " @@ ");
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
			buffer.append(cast.getLeftExpression());
			buffer.append(" AS ");
			buffer.append(cast.getType());
			buffer.append(")");
		} else {
			buffer.append(cast.getLeftExpression());
			buffer.append("::");
			buffer.append(cast.getType());
		}
	}

	@Override
	public void visit(Modulo modulo) {
		visitBinaryExpression(modulo, " % ");
	}

	@Override
	public void visit(AnalyticExpression aexpr) {
		buffer.append(aexpr.toString());
	}

	@Override
	public void visit(ExtractExpression eexpr) {
		buffer.append(eexpr.toString());
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
}

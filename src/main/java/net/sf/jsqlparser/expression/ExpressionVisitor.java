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
package net.sf.jsqlparser.expression;

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
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;

public interface ExpressionVisitor {

	public void visit(NullValue nullValue);

	public void visit(Function function);

	public void visit(InverseExpression inverseExpression);

	public void visit(JdbcParameter jdbcParameter);

	public void visit(DoubleValue doubleValue);

	public void visit(LongValue longValue);

	public void visit(DateValue dateValue);

	public void visit(TimeValue timeValue);

	public void visit(TimestampValue timestampValue);

	public void visit(Parenthesis parenthesis);

	public void visit(StringValue stringValue);

	public void visit(Addition addition);

	public void visit(Division division);

	public void visit(Multiplication multiplication);

	public void visit(Subtraction subtraction);

	public void visit(AndExpression andExpression);

	public void visit(OrExpression orExpression);

	public void visit(Between between);

	public void visit(EqualsTo equalsTo);

	public void visit(GreaterThan greaterThan);

	public void visit(GreaterThanEquals greaterThanEquals);

	public void visit(InExpression inExpression);

	public void visit(IsNullExpression isNullExpression);

	public void visit(LikeExpression likeExpression);

	public void visit(MinorThan minorThan);

	public void visit(MinorThanEquals minorThanEquals);

	public void visit(NotEqualsTo notEqualsTo);

	public void visit(Column tableColumn);

	public void visit(SubSelect subSelect);

	public void visit(CaseExpression caseExpression);

	public void visit(WhenClause whenClause);

	public void visit(ExistsExpression existsExpression);

	public void visit(AllComparisonExpression allComparisonExpression);

	public void visit(AnyComparisonExpression anyComparisonExpression);

	public void visit(Concat concat);

	public void visit(Matches matches);

	public void visit(BitwiseAnd bitwiseAnd);

	public void visit(BitwiseOr bitwiseOr);

	public void visit(BitwiseXor bitwiseXor);

	public void visit(CastExpression cast);

	public void visit(Modulo modulo);

	public void visit(AnalyticExpression aexpr);

	public void visit(ExtractExpression eexpr);
}

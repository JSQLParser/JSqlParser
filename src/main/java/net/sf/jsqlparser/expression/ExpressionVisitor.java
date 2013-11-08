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
import net.sf.jsqlparser.expression.operators.relational.RegExpCaseSensitiveMatch;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;

public interface ExpressionVisitor {

	void visit(NullValue nullValue);

	void visit(Function function);

	void visit(InverseExpression inverseExpression);

	void visit(JdbcParameter jdbcParameter);
        
        void visit(JdbcNamedParameter jdbcNamedParameter);

	void visit(DoubleValue doubleValue);

	void visit(LongValue longValue);

	void visit(DateValue dateValue);

	void visit(TimeValue timeValue);

	void visit(TimestampValue timestampValue);

	void visit(Parenthesis parenthesis);

	void visit(StringValue stringValue);

	void visit(Addition addition);

	void visit(Division division);

	void visit(Multiplication multiplication);

	void visit(Subtraction subtraction);

	void visit(AndExpression andExpression);

	void visit(OrExpression orExpression);

	void visit(Between between);

	void visit(EqualsTo equalsTo);

	void visit(GreaterThan greaterThan);

	void visit(GreaterThanEquals greaterThanEquals);

	void visit(InExpression inExpression);

	void visit(IsNullExpression isNullExpression);

	void visit(LikeExpression likeExpression);

	void visit(MinorThan minorThan);

	void visit(MinorThanEquals minorThanEquals);

	void visit(NotEqualsTo notEqualsTo);

	void visit(Column tableColumn);

	void visit(SubSelect subSelect);

	void visit(CaseExpression caseExpression);

	void visit(WhenClause whenClause);

	void visit(ExistsExpression existsExpression);

	void visit(AllComparisonExpression allComparisonExpression);

	void visit(AnyComparisonExpression anyComparisonExpression);

	void visit(Concat concat);

	void visit(Matches matches);

	void visit(BitwiseAnd bitwiseAnd);

	void visit(BitwiseOr bitwiseOr);

	void visit(BitwiseXor bitwiseXor);

	void visit(CastExpression cast);

	void visit(Modulo modulo);

	void visit(AnalyticExpression aexpr);

	void visit(ExtractExpression eexpr);
	
	void visit(IntervalExpression iexpr);
	
	void visit(OracleHierarchicalExpression oexpr);
	
	void visit(RegExpCaseSensitiveMatch rexpr);
}

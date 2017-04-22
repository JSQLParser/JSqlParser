/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2017 JSQLParser
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
package net.sf.jsqlparser.statement.select;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.NotExpression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.MultiAndExpression;
import net.sf.jsqlparser.expression.operators.conditional.MultiOrExpression;
import net.sf.jsqlparser.expression.operators.conditional.MultipleExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;

/**
 * This class is mainly used for handling the cloning of an expression tree.
 * Note this is the shallow copy of the tree. That means I do not modify
 * or copy the expression other than these expressions:
 * AND, OR, NOT, (), MULTI-AND, MULTI-OR.
 * Since the CNF conversion only change the condition part of the tree. 
 * @author messfish
 *
 */
public class CloneHelper {

	/**
	 * This method is used for changing the logical structure of the tree.
	 * The main method is to convert and operator and or operator to let
	 * them have multiple children (reflected in MultipleExpression.java
	 * from the conditional package). Note if the value from the conditional
	 * operator has a not attached to it we need to append an not operator
	 * ahead of it since the not operator needed to be pushed down during
	 * the second step. Also, I will leave out all the parenthesis expression
	 * which is connected to the conditional operator.
	 * @param express the expression that will be modified
	 * @return the modified expression.
	 *
	 */
	public Expression modify(Expression express) {
		if(express instanceof NotExpression)
			return new NotExpression(modify(((NotExpression)express).getExpression()));
		if(express instanceof Parenthesis) {
			Parenthesis parenthesis = (Parenthesis)express;
			Expression result = modify(parenthesis.getExpression());
			if(parenthesis.isNot())
				return new NotExpression(result);
			return result;
		}
		if(express instanceof AndExpression) {
			AndExpression and = (AndExpression)express;
			List<Expression> list = new ArrayList<Expression>();
			list.add(modify(and.getLeftExpression()));
			list.add(modify(and.getRightExpression()));
			MultiAndExpression result = new MultiAndExpression(list);
			if(and.isNot()) return new NotExpression(result);
			return result;
		}
		if(express instanceof OrExpression) {
			OrExpression or = (OrExpression)express;
			List<Expression> list = new ArrayList<Expression>();
			list.add(modify(or.getLeftExpression()));
			list.add(modify(or.getRightExpression()));
			MultiOrExpression result = new MultiOrExpression(list);
			if(or.isNot()) return new NotExpression(result);
			return result;
		}
		/* at this stage, there is no need to modify, just simply return. */
		return express;
	}
	
	/**
	 * This method is used to copy the expression which happens at 
	 * step four. I only copy the conditional expressions since the 
	 * CNF only changes the conditional part.
	 * @param express the expression that will be copied.
	 * @return the copied expression.
	 */
	public Expression shallowCopy(Expression express) {
		if(express instanceof MultipleExpression) {
			MultipleExpression multi = (MultipleExpression)express;
			List<Expression> list = new ArrayList<Expression>();
			for(int i=0;i<multi.size();i++)
				list.add(shallowCopy(multi.getChild(i)));
			if(express instanceof MultiAndExpression)
				return new MultiAndExpression(list);
			/* since there only two possibilities of the multiple expression,
			 * so after the if condition, it is certain this is a multi-or. */
			return new MultiOrExpression(list);
		}
		return express;
	}
	
	/**
	 * This helper method is used to change the multiple expression into
	 * the binary form, respectively and return the root of the expression tree.
	 * @param isMultiOr variable tells whether the expression is or.
	 * @param exp the expression that needs to be converted.
	 * @return the root of the expression tree.
	 */
	public Expression changeBack(Boolean isMultiOr, Expression exp) {
		if(!(exp instanceof MultipleExpression)) return exp;
		MultipleExpression changed = (MultipleExpression)exp;
		Expression result = changed.getChild(0);
		for(int i=1;i<changed.size();i++) {
			Expression left = result;
			Expression right = changed.getChild(i);
			if(isMultiOr)
				result = new OrExpression(left, right);
			else
				result = new AndExpression(left, right);
		}
		if(isMultiOr) return new Parenthesis(result);
		return result;
	}
	
}

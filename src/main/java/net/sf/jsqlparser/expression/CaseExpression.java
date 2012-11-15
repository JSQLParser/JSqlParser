/* ================================================================
 * JSQLParser : java based sql parser
 * ================================================================
 *
 * Project Info:  http://jsqlparser.sourceforge.net
 * Project Lead:  Leonardo Francalanci (leoonardoo@yahoo.it);
 *
 * (C) Copyright 2004, by Leonardo Francalanci
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package net.sf.jsqlparser.expression;

import java.util.List;

import net.sf.jsqlparser.statement.select.PlainSelect;

/**
 * CASE/WHEN expression.
 * 
 * Syntax: <code><pre>
 * CASE 
 * WHEN condition THEN expression
 * [WHEN condition THEN expression]...
 * [ELSE expression]
 * END
 * </pre></code>
 * 
 * <br/>
 * or <br/>
 * <br/>
 * 
 * <code><pre>
 * CASE expression 
 * WHEN condition THEN expression
 * [WHEN condition THEN expression]...
 * [ELSE expression]
 * END
 * </pre></code>
 * 
 * See also: https://aurora.vcu.edu/db2help/db2s0/frame3.htm#casexp
 * http://sybooks.sybase.com/onlinebooks/group-as/asg1251e
 * /commands/@ebt-link;pt=5954?target=%25N%15_52628_START_RESTART_N%25
 * 
 * 
 * @author Havard Rast Blok
 */
public class CaseExpression implements Expression {

	private Expression switchExpression;

	private List<Expression> whenClauses;

	private Expression elseExpression;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.Expression#accept(net.sf.jsqlparser.expression.ExpressionVisitor)
	 */
	@Override
	public void accept(ExpressionVisitor expressionVisitor) {
		expressionVisitor.visit(this);
	}

	/**
	 * @return Returns the switchExpression.
	 */
	public Expression getSwitchExpression() {
		return switchExpression;
	}

	/**
	 * @param switchExpression
	 *            The switchExpression to set.
	 */
	public void setSwitchExpression(Expression switchExpression) {
		this.switchExpression = switchExpression;
	}

	/**
	 * @return Returns the elseExpression.
	 */
	public Expression getElseExpression() {
		return elseExpression;
	}

	/**
	 * @param elseExpression
	 *            The elseExpression to set.
	 */
	public void setElseExpression(Expression elseExpression) {
		this.elseExpression = elseExpression;
	}

	/**
	 * @return Returns the whenClauses.
	 */
	public List<Expression> getWhenClauses() {
		return whenClauses;
	}

	/**
	 * @param whenClauses
	 *            The whenClauses to set.
	 */
	public void setWhenClauses(List<Expression> whenClauses) {
		this.whenClauses = whenClauses;
	}

	@Override
	public String toString() {
		return "CASE " + ((switchExpression != null) ? switchExpression + " " : "")
				+ PlainSelect.getStringList(whenClauses, false, false) + " "
				+ ((elseExpression != null) ? "ELSE " + elseExpression + " " : "") + "END";
	}
}

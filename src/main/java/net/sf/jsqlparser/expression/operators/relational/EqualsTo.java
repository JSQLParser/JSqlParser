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

package net.sf.jsqlparser.expression.operators.relational;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.ExpressionVisitor;

public class EqualsTo extends BinaryExpression {
	@Override
	public void accept(ExpressionVisitor expressionVisitor) {
		expressionVisitor.visit(this);
	}

	@Override
	public String getStringExpression() {
		return "=";
	}
	
	public static final int NO_ORACLE_JOIN = 0;
	public static final int ORACLE_JOIN_RIGHT = 1;
	public static final int ORACLE_JOIN_LEFT = 2;

	private int oldOracleJoinSyntax = NO_ORACLE_JOIN;

	public void setOldOracleJoinSyntax(int oldOracleJoinSyntax) {
		this.oldOracleJoinSyntax = oldOracleJoinSyntax;
		if (oldOracleJoinSyntax<0 || oldOracleJoinSyntax>2) {
			throw new IllegalArgumentException("unknown join type for oracle found (type=" + oldOracleJoinSyntax + ")");
		}
	}
	
	@Override
	public String toString() {
		return (isNot()? "NOT ":"") + getLeftExpression()+ (oldOracleJoinSyntax==ORACLE_JOIN_RIGHT?"(+)":"") + " "+getStringExpression()+" "+getRightExpression() +(oldOracleJoinSyntax==ORACLE_JOIN_LEFT?"(+)":"");
	}
	
	public int getOldOracleJoinSyntax() {
		return oldOracleJoinSyntax;
	}
}

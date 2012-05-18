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
package net.sf.jsqlparser.statement.select;

import java.util.ArrayList;
import java.util.List;

/**
 * Eine Set Operation. Diese besteht aus einer Liste von plainSelects verknüpft
 * mit Set-Operationen (UNION,INTERSECT,MINUS,EXCEPT). Diese Operationen haben
 * die gleiche Priorität, s.d. die Datenbanken dies von Links nach rechts
 * abarbeiten.
 *
 */
public class SetOperationList implements SelectBody {

	private List plainSelects;
	private List operations;
	private List orderByElements;
	private Limit limit;

	@Override
	public void accept(SelectVisitor selectVisitor) {
		selectVisitor.visit(this);
	}

	public List getOrderByElements() {
		return orderByElements;
	}

	/**
	 * the list of {@link PlainSelect}s in this SetOperationList
	 *
	 * @return the list of {@link PlainSelect}s
	 */
	public List getPlainSelects() {
		return plainSelects;
	}
	
	public List getOperations() {
		return operations;
	}

	public void setOrderByElements(List orderByElements) {
		this.orderByElements = orderByElements;
	}

	public void setOpsAndSelects(List select,List ops) {
		plainSelects = select;
		operations=ops;
		
		if (select.size()-1!=ops.size())
			throw new IllegalArgumentException("list sizes are not valid");
	}

	public Limit getLimit() {
		return limit;
	}

	public void setLimit(Limit limit) {
		this.limit = limit;
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		
		for (int i = 0; i < plainSelects.size(); i++) {
			if (i!=0)
				buffer.append(" ").append(operations.get(i-1).toString()).append(" ");
			buffer.append("(").append(plainSelects.get(i).toString()).append(")");
		}

		if (orderByElements!=null)
			buffer.append(PlainSelect.orderByToString(orderByElements));
		if (limit!=null)
			buffer.append(limit.toString());
		return buffer.toString();
	}

	/**
	 * Unterstützte Set-Operationen.
	 */
	public enum SetOperationType {

		INTERSECT,
		EXCEPT,
		MINUS,
		UNION
	}
}

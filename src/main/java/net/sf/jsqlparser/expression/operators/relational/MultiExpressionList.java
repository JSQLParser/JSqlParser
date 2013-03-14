/*
 * Copyright (C) 2013 toben.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package net.sf.jsqlparser.expression.operators.relational;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A list of ExpressionList items. e.g. multi values of insert statements. This one allows
 * only equally sized ExpressionList.
 * @author toben
 */
public class MultiExpressionList implements ItemsList {

	List<ExpressionList> exprList;

	public MultiExpressionList() {
		this.exprList = new ArrayList<ExpressionList>();
	}
	
	@Override
	public void accept(ItemsListVisitor itemsListVisitor) {
		itemsListVisitor.visit(this);
	}

	public List<ExpressionList> getExprList() {
		return exprList;
	}

	public void addExpressionList(ExpressionList el) {
		if (!exprList.isEmpty()) {
			if (exprList.get(0).getExpressions().size() != el.getExpressions().size()) {
				throw new IllegalArgumentException("different count of parameters");
			}
		}
		exprList.add(el);
	}
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		for (Iterator<ExpressionList> it = exprList.iterator(); it.hasNext() ;) {
			b.append(it.next().toString());
			if (it.hasNext()) b.append(", ");
		}
		return b.toString();
	}
}

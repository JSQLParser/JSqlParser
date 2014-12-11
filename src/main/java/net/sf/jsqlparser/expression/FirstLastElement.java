/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2014 JSQLParser
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

import java.util.List;

import net.sf.jsqlparser.expression.WindowElement.Type;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;

public class FirstLastElement {
	public enum Type {
		FIRST, 
		LAST
	}
	
	private Type type;
	private List<OrderByElement> orderByElements = null;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

	public List<OrderByElement> getOrderByElements() {
		return orderByElements;
	}

	public void setOrderByElements(List<OrderByElement> orderByElements) {
		this.orderByElements = orderByElements;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(" KEEP (DENSE_RANK ");
		sb.append(type.toString());
		sb.append(" ");
		if (orderByElements != null) {
			sb.append(PlainSelect.orderByToString(orderByElements));
		}
		sb.append(")");
		return sb.toString();
	}
}

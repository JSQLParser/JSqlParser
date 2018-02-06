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
package net.sf.jsqlparser.statement.select;

import java.util.Iterator;
import java.util.List;

import lombok.Data;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 * A subselect followed by an optional alias.
 */
@Data
public class SubSelect extends ASTNodeAccessImpl implements FromItem, Expression, ItemsList {

	private SelectBody selectBody;
	private Alias alias;
	private boolean useBrackets = true;
	private List<WithItem> withItemsList;

	private Pivot pivot;

	@Override
	public void accept(FromItemVisitor fromItemVisitor) {
		fromItemVisitor.visit(this);
	}

	@Override
	public void accept(ExpressionVisitor expressionVisitor) {
		expressionVisitor.visit(this);
	}

	@Override
	public void accept(ItemsListVisitor itemsListVisitor) {
		itemsListVisitor.visit(this);
	}

	@Override
	public String toString() {
		StringBuilder retval = new StringBuilder();
		if (useBrackets) {
			retval.append("(");
		}
		if (withItemsList != null && !withItemsList.isEmpty()) {
			retval.append("WITH ");
			for (Iterator<WithItem> iter = withItemsList.iterator(); iter.hasNext();) {
				WithItem withItem = iter.next();
				retval.append(withItem);
				if (iter.hasNext()) {
					retval.append(",");
				}
				retval.append(" ");
			}
		}
		retval.append(selectBody);
		if (useBrackets) {
			retval.append(")");
		}
		if (alias != null) {
			retval.append(alias.toString());
		}
		if (pivot != null) {
			retval.append(" ").append(pivot);
		}
		return retval.toString();
	}
}

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

import lombok.Data;
import net.sf.jsqlparser.expression.Alias;

/**
 * A lateral subselect followed by an alias.
 *
 * @author Tobias Warneke
 */
@Data
public class LateralSubSelect implements FromItem {

	private SubSelect subSelect;
	private Alias alias;
	private Pivot pivot;

	@Override
	public void accept(FromItemVisitor fromItemVisitor) {
		fromItemVisitor.visit(this);
	}

	@Override
	public String toString() {
		return "LATERAL" + subSelect.toString()
			+ ((alias != null) ? alias.toString() : "")
			+ ((pivot != null) ? " " + pivot : "");
	}
}

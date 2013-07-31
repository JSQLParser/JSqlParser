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
package net.sf.jsqlparser.schema;

import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.IntoTableVisitor;
import net.sf.jsqlparser.statement.select.Pivot;

/**
 * A table. It can have an alias and the schema name it belongs to.
 */
public class Table implements FromItem {

	private String schemaName;
	private String name;
	private String alias;
    private Pivot pivot;

	public Table() {
	}

	public Table(String schemaName, String name) {
		this.schemaName = schemaName;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setName(String string) {
		name = string;
	}

	public void setSchemaName(String string) {
		schemaName = string;
	}

	@Override
	public String getAlias() {
		return alias;
	}

	@Override
	public void setAlias(String string) {
		alias = string;
	}

    public String getWholeTableName() {

		String tableWholeName = null;
		if (name == null) {
			return null;
		}
		if (schemaName != null) {
			tableWholeName = schemaName + "." + name;
		} else {
			tableWholeName = name;
		}

		return tableWholeName;

	}

	@Override
	public void accept(FromItemVisitor fromItemVisitor) {
		fromItemVisitor.visit(this);
	}

	public void accept(IntoTableVisitor intoTableVisitor) {
		intoTableVisitor.visit(this);
	}

    public Pivot getPivot() {
        return pivot;
    }

    public void setPivot(Pivot pivot) {
        this.pivot = pivot;
    }

    @Override
	public String toString() {
		return getWholeTableName() +
                ((pivot != null) ? " "+pivot : "") +
                ((alias != null) ? " AS " + alias : "");
	}
}

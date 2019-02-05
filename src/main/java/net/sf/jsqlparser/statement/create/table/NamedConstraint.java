/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2014 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
 /*
 * Copyright (C) 2014 JSQLParser.
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
package net.sf.jsqlparser.statement.create.table;

import net.sf.jsqlparser.statement.select.PlainSelect;

/**
 *
 * @author toben
 */
public class NamedConstraint extends Index {

    @Override
    public String toString() {
        String idxSpecText = PlainSelect.getStringList(getIndexSpec(), false, false);
        return (getName() != null ? "CONSTRAINT " + getName() + " " : "")
                + getType() + " " + PlainSelect.getStringList(getColumnsNames(), true, true) + (!"".
                equals(idxSpecText) ? " " + idxSpecText : "");
    }
}

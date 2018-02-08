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

import java.util.List;

import lombok.Data;
import net.sf.jsqlparser.expression.Alias;

/**
 * A table created by "(tab1 [join tab2]* )".
 */
@Data
public class SubJoin implements FromItem {

    private FromItem left;
    private Alias alias;
    private Pivot pivot;
    private List<Join> joinList;

    @Override
    public void accept(FromItemVisitor fromItemVisitor) {
        fromItemVisitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(left);
        for (Join j : joinList) {
            sb.append(" ").append(j);
        }

        sb.append(")").append((alias != null) ? (" " + alias.toString()) : "").append((pivot != null) ? " " + pivot : "");
        return sb.toString();
    }
}

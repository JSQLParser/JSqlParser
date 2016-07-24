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

package net.sf.jsqlparser.expression;

import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.schema.Column;

/**
 *
 * @author toben
 */
public class JsonExpression  implements Expression {

    private Column column;
    
    private List<String> idents = new ArrayList<String>();
    
    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    public Column getColumn() {
        return column;
    }

    public void setColumn(Column column) {
        this.column = column;
    }

    public List<String> getIdents() {
        return idents;
    }

    public void setIdents(List<String> idents) {
        this.idents = idents;
    }
    
    public void addIdent(String ident) {
        idents.add(ident);
    }
    
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(column.toString());
        for (String ident : idents) {
            b.append("->").append(ident);
        }
        return b.toString();
    }
}

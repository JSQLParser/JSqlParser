/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2015 JSQLParser
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

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;

public class TableFunction implements FromItem {

    private String name;
    private ExpressionList parameters;

    /**
     * The name of he procedure, i.e. "UNWIND_TOP"
     *
     * @return the name of he procedure
     */
    public String getName() {
        return name;
    }

    public void setName(String string) {
        name = string;
    }

    /**
     * The list of parameters of the tableFunction (if any, else null)
     *
     * @return the list of parameters of the tableFunction (if any, else null)
     */
    public ExpressionList getParameters() {
        return parameters;
    }

    public void setParameters(ExpressionList list) {
        parameters = list;
    }

    @Override
    public void accept(FromItemVisitor fromItemVisitor) {
        fromItemVisitor.visit(this);
    }

    @Override
    public Alias getAlias() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAlias(Alias alias) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Pivot getPivot() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPivot(Pivot pivot) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String toString() {
        String params;

        if (parameters != null) {
            params = parameters.toString();
        }else {
            params = "()";
        }

        String ans = name + "" + params + "";

        return ans;
    }

}

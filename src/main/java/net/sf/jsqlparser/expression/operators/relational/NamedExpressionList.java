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
package net.sf.jsqlparser.expression.operators.relational;

import java.util.Arrays;
import java.util.List;

import net.sf.jsqlparser.expression.Expression;

/**
 * A list of named expressions, as in 
 * as in select substr('xyzzy' from 2 for 3)
 */
public class NamedExpressionList implements ItemsList {

    private List<Expression> expressions;
    private List<String> names;

    public NamedExpressionList() {
    }

    public NamedExpressionList(List<Expression> expressions) {
        this.expressions = expressions;
    }
    
    public NamedExpressionList(Expression ... expressions) {
        this.expressions = Arrays.asList(expressions);
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    public List<String> getNames() {
        return names;
    }


    public void setExpressions(List<Expression> list) {
        expressions = list;
    }

    public void setNames(List<String> list) {
        names = list;
    }


    @Override
    public void accept(ItemsListVisitor itemsListVisitor) {
        itemsListVisitor.visit(this);
    }

    @Override
    public String toString() {

        StringBuilder ret = new StringBuilder();
        ret.append("(");
        for(int i=0; i<expressions.size(); i++){
            if(i>0){
                ret.append(" ");
            }
            if(! names.get(i).equals("")){
                ret.append(names.get(i)).append(" ").append(expressions.get(i));
            }else{
                ret.append(expressions.get(i));
            }
        }
        ret.append(")");

        return ret.toString();
    }
}

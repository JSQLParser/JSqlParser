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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import lombok.Data;
import net.sf.jsqlparser.expression.Expression;

/**
 * A list of ExpressionList items. e.g. multi values of insert statements. This one allows only
 * equally sized ExpressionList.
 *
 * @author toben
 */
@Data
public class MultiExpressionList implements ItemsList {

    private List<ExpressionList> exprList;

    public MultiExpressionList() {
        this.exprList = new ArrayList<ExpressionList>();
    }

    @Override
    public void accept(ItemsListVisitor itemsListVisitor) {
        itemsListVisitor.visit(this);
    }

    public void addExpressionList(ExpressionList el) {
        if (!exprList.isEmpty()
                && exprList.get(0).getExpressions().size() != el.getExpressions().size()) {
            throw new IllegalArgumentException("different count of parameters");
        }
        exprList.add(el);
    }

    public void addExpressionList(List<Expression> list) {
        addExpressionList(new ExpressionList(list));
    }

    public void addExpressionList(Expression expr) {
        addExpressionList(new ExpressionList(Arrays.asList(expr)));
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (Iterator<ExpressionList> it = exprList.iterator(); it.hasNext();) {
            b.append(it.next().toString());
            if (it.hasNext()) {
                b.append(", ");
            }
        }
        return b.toString();
    }
}

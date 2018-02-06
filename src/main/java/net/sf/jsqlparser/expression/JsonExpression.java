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

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Column;

/**
 *
 * @author toben
 */
@Data
public class JsonExpression extends ASTNodeAccessImpl implements Expression {

    private Column column;

    private List<String> idents = new ArrayList<String>();
    private List<String> operators = new ArrayList<String>();

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    //    public List<String> getIdents() {
    //        return idents;
    //    }
    //
    //    public void setIdents(List<String> idents) {
    //        this.idents = idents;
    //        operators = new ArrayList<String>();
    //        for (String ident : idents) {
    //            operators.add("->");
    //        }
    //    }
    //    
    //    public void addIdent(String ident) {
    //        addIdent(ident, "->");
    //    }
    public void addIdent(String ident, String operator) {
        idents.add(ident);
        operators.add(operator);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(column.toString());
        for (int i = 0; i < idents.size(); i++) {
            b.append(operators.get(i)).append(idents.get(i));
        }
        return b.toString();
    }
}

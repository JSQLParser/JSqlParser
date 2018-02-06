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
package net.sf.jsqlparser.expression;

import lombok.Data;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 * It represents a "-" or "+" or "~" before an expression
 */
@Data
public class SignedExpression extends ASTNodeAccessImpl implements Expression {

    private char sign;
    private Expression expression;

    public SignedExpression(char sign, Expression expression) {
        setSign(sign);
        setExpression(expression);
    }

    public final void setSign(char sign) {
        this.sign = sign;
        if (sign != '+' && sign != '-' && sign != '~') {
            throw new IllegalArgumentException("illegal sign character, only + - ~ allowed");
        }
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    @Override
    public String toString() {
        return getSign() + expression.toString();
    }
}

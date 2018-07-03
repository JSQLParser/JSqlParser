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

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.create.table.ColDataType;

/**
 *
 * @author tw
 */
public class CastExpression extends ASTNodeAccessImpl implements Expression {

    private Expression leftExpression;
    private ColDataType type;
    private boolean useCastKeyword = true;
    private String format;
    private String named;
    private String title;

    public ColDataType getType() {
        return type;
    }

    public void setType(ColDataType type) {
        this.type = type;
    }

    public Expression getLeftExpression() {
        return leftExpression;
    }

    public void setLeftExpression(Expression expression) {
        leftExpression = expression;
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    public boolean isUseCastKeyword() {
        return useCastKeyword;
    }

    public void setUseCastKeyword(boolean useCastKeyword) {
        this.useCastKeyword = useCastKeyword;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getNamed() {
        return named;
    }

    public void setNamed(String named) {
        this.named = named;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        if (useCastKeyword) {
            StringBuilder sb = new StringBuilder("CAST(");
            sb.append(leftExpression);
            sb.append(" AS");
            if (type != null) {
                sb.append(" ").append(type.toString());
            }
            if (format != null) {
                sb.append(" FORMAT ").append(format);
            }
            if (named != null) {
                sb.append(" NAMED ").append(named);
            }
            if (title != null) {
                sb.append(" TITLE ").append(title);
            }

            sb.append(")");
            return sb.toString();
        } else {
            return leftExpression + "::" + type.toString();
        }
    }
}

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

/**
 * A '?' in a statement or a ?<number> e.g. ?4
 */
public class JdbcParameter extends ASTNodeAccessImpl implements Expression {

    private Integer index;
    private boolean useFixedIndex = false;

    public JdbcParameter() {}

    public JdbcParameter(Integer index, boolean useFixedIndex) {
        this.index = index;
        this.useFixedIndex = useFixedIndex;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public boolean isUseFixedIndex() {
        return useFixedIndex;
    }

    public void setUseFixedIndex(boolean useFixedIndex) {
        this.useFixedIndex = useFixedIndex;
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    @Override
    public String toString() {
        return useFixedIndex ? "?" + index : "?";
    }
}

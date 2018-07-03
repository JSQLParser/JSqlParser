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

import net.sf.jsqlparser.expression.Alias;

/**
 * It represents an expression like "(" expression ")"
 */
public class ParenthesisFromItem implements FromItem {

    private FromItem fromItem;

    private Alias alias;

    public ParenthesisFromItem() {}

    public ParenthesisFromItem(FromItem fromItem) {
        setFromItem(fromItem);
    }

    public FromItem getFromItem() {
        return fromItem;
    }

    public final void setFromItem(FromItem fromItem) {
        this.fromItem = fromItem;
    }

    @Override
    public void accept(FromItemVisitor fromItemVisitor) {
        fromItemVisitor.visit(this);
    }

    @Override
    public String toString() {
        return "(" + fromItem + ")" + (alias != null ? alias.toString() : "");
    }

    @Override
    public Alias getAlias() {
        return alias;
    }

    @Override
    public void setAlias(Alias alias) {
        this.alias = alias;
    }

    @Override
    public Pivot getPivot() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setPivot(Pivot pivot) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

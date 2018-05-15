/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2017 JSQLParser
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
package net.sf.jsqlparser.statement.alter;

public class DeferrableConstraint implements ConstraintState {

    private boolean not;

    public DeferrableConstraint(boolean not) {
        this.not = not;
    }

    public boolean isNot() {
        return not;
    }

    public void setNot(boolean not) {
        this.not = not;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        if (not) {
            b.append("NOT ");
        }
        b.append("DEFERRABLE");
        return b.toString();
    }
}

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
package net.sf.jsqlparser.expression;

import java.util.List;

public class SQLServerTableHint {

    private final String hint;
    private final boolean indexEq;
    private final List<String> indexNames;
    private final boolean noexpand;

    public SQLServerTableHint(String hint, boolean indexEq, List<String> indexNames, boolean noexpand) {
        this.hint = hint;
        this.indexEq = indexEq;
        this.indexNames = indexNames;
        this.noexpand = noexpand;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(noexpand ? "NOEXPAND " : "").append(hint);
        if (indexNames != null && indexNames.size() > 0) {
            buffer.append(indexEq ? "=" : "").append("(");
            for (int i = 0; i < indexNames.size(); i++) {
                if (i > 0) {
                    buffer.append(",");
                }
                buffer.append(indexNames.get(i));
            }
            buffer.append(")");
        }
        return buffer.toString();
    }
}

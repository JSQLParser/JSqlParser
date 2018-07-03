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
package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.expression.Expression;

public class Commit extends Statement.Default {

    private String image = "COMMIT"; // default commit (other possibilities are ET/END TRANSACTION)
    private String options;
    private Expression userVar;

    public Commit(String image, String opts, Expression userVar) {
        this.image = image;
        if (opts != null && !opts.isEmpty()) {
            this.options = opts;
        } else {
            this.options = null;
        }

        this.userVar = userVar;
    }

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    @Override
    public String toString() {
        return image + (options != null ? (" " + options) : "") + (userVar != null ? (" " + userVar.toString()) : "");
    }
}

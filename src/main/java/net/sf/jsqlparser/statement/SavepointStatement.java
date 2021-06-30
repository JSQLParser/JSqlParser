/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
/*
 * Copyright (C) 2021 JSQLParser.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA
 */

package net.sf.jsqlparser.statement;

import java.util.Objects;

/**
 *
 * @author are
 */
public class SavepointStatement implements Statement {
    private String savepointName;

    public String getSavepointName() {
        return savepointName;
    }

    public void setSavepointName(String savepointName) {
        this.savepointName = Objects.requireNonNull(savepointName, "The Savepoint Name must not be NULL.");
    }

    public SavepointStatement(String savepointName) {
        this.savepointName = Objects.requireNonNull(savepointName, "The Savepoint Name must not be NULL.");
    }

    @Override
    public String toString() {
        return "SAVEPOINT " + savepointName;
    }

    @Override
    public void accept(StatementVisitor statementVisitor) {
         statementVisitor.visit(this);
    }
}

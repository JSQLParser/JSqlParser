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
package net.sf.jsqlparser.statement;

/**
 * An operation on the db (SELECT, UPDATE ecc.)
 */
public interface Statement {

    public abstract class Default implements Statement {

        protected UsingParams usingParams = null;

        @Override
        public boolean hasUsingParams() {
            return this.usingParams != null;
        }

        @Override
        public void setUsingParams(UsingParams up) {
            this.usingParams = up;

        }

        @Override
        public UsingParams getUsingParams() {
            return this.usingParams;
        }

    }

    void accept(StatementVisitor statementVisitor);

    boolean hasUsingParams();

    void setUsingParams(UsingParams up);

    UsingParams getUsingParams();
}

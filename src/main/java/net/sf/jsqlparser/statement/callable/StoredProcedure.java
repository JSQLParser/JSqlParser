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
package net.sf.jsqlparser.statement.callable;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

public class StoredProcedure extends Statement.Default {

    private Expression retval;
    private Function callableFunction;
    private String options;
    private boolean isOracle = false;
    private boolean withBrackets = true;

    public boolean isOracle() {
        return isOracle;
    }

    public void setOracle(boolean isOracle) {
        this.isOracle = isOracle;
    }

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    public void setCalledWithReturn(Expression retval) {
        this.retval = retval;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public void setFunction(Function callableFunction) {
        this.callableFunction = callableFunction;
    }

    public Function getFunction() {
        return this.callableFunction;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (isOracle) {
            sb.append("BEGIN ");
            if (retval != null) {
                sb.append(retval).append(" := ");
            }
            sb.append(callableFunction).append("; END;");
            if (options != null) {
                sb.append(" OPTION (" + options + ")");
            }
        } else {
            if (withBrackets) {
                sb.append("{");
            }
            if (retval != null) {
                sb.append(retval).append(" = ");
            }
            sb.append("CALL ").append(callableFunction);
            if (withBrackets) {
                sb.append("}");
            }
            if (options != null) {
                sb.append(" OPTION (" + options + ")");
            }
        }
        return sb.toString();
    }

    // used for comparing two stored procedure calls
    public String getFunctionSignature() {
        if (callableFunction == null) {
            return null;
        }

        if (retval != null) {
            return isOracle ? ("BEGIN " + retval + " := " + callableFunction.toString() + "; END;") : ("{" + retval + "=" + callableFunction.toString() + "}");
        } else {
            return isOracle ? ("BEGIN " + callableFunction.toString() + "; END;") : ((withBrackets ? "{" : "") + callableFunction.toString() + (withBrackets ? "}" : ""));
        }
    }

    public boolean isWithBrackets() {
        return withBrackets;
    }

    public void setWithBrackets(boolean withBrackets) {
        this.withBrackets = withBrackets;
    }

}

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

import net.sf.jsqlparser.expression.operators.relational.ExpressionList;

/**
 * A function as MAX,COUNT...
 */
public class Function implements Expression {

    private String name;
    private ExpressionList parameters;
    private boolean allColumns = false;
    private boolean distinct = false;
    private boolean isEscaped = false;
    private String attribute;

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    /**
     * The name of he function, i.e. "MAX"
     *
     * @return the name of he function
     */
    public String getName() {
        return name;
    }

    public void setName(String string) {
        name = string;
    }

    /**
     * true if the parameter to the function is "*"
     *
     * @return true if the parameter to the function is "*"
     */
    public boolean isAllColumns() {
        return allColumns;
    }

    public void setAllColumns(boolean b) {
        allColumns = b;
    }

    /**
     * true if the function is "distinct"
     *
     * @return true if the function is "distinct"
     */
    public boolean isDistinct() {
        return distinct;
    }

    public void setDistinct(boolean b) {
        distinct = b;
    }

    /**
     * The list of parameters of the function (if any, else null) If the
     * parameter is "*", allColumns is set to true
     *
     * @return the list of parameters of the function (if any, else null)
     */
    public ExpressionList getParameters() {
        return parameters;
    }

    public void setParameters(ExpressionList list) {
        parameters = list;
    }

    /**
     * Return true if it's in the form "{fn function_body() }"
     *
     * @return true if it's java-escaped
     */
    public boolean isEscaped() {
        return isEscaped;
    }

    public void setEscaped(boolean isEscaped) {
        this.isEscaped = isEscaped;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    @Override
    public String toString() {
        String params;

        if (parameters != null) {
            params = parameters.toString();
            if (isDistinct()) {
                params = params.replaceFirst("\\(", "(DISTINCT ");
            } else if (isAllColumns()) {
                params = params.replaceFirst("\\(", "(ALL ");
            }
        } else if (isAllColumns()) {
            params = "(*)";
        } else {
            params = "()";
        }

        String ans = name + "" + params + "";

        if (attribute != null) {
            ans += "." + attribute;
        }

        if (isEscaped) {
            ans = "{fn " + ans + "}";
        }

        return ans;
    }
}

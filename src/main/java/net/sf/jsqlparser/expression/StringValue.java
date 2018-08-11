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

import java.util.Arrays;
import java.util.List;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 * A string as in 'example_string'
 */
public final class StringValue extends ASTNodeAccessImpl implements Expression {

    private String value = "";
    private String prefix = null;

    /*
    N - SQLServer Unicode encoding
    U - Oracle Unicode encoding
    E - Postgresql Unicode encoding
    R - Cloud Spanner Raw string
    B - Cloud Spanner Byte string
    RB - Cloud Spanner Raw Byte string 
     */
    public static final List<String> ALLOWED_PREFIXES = Arrays.asList("N", "U", "E", "R", "B", "RB");

    public StringValue(String escapedValue) {
        // romoving "'" at the start and at the end
        if (escapedValue.startsWith("'") && escapedValue.endsWith("'")) {
            value = escapedValue.substring(1, escapedValue.length() - 1);
            return;
        }

        if (escapedValue.length() > 2) {
            for(String p : ALLOWED_PREFIXES) {
                if(escapedValue.length() > p.length() && escapedValue.substring(0, p.length()).equalsIgnoreCase(p) && escapedValue.charAt(p.length())=='\'') {
                    this.prefix = p;
                    value = escapedValue.substring(p.length() + 1, escapedValue.length() - 1);
                    return;
                }
            }
        }

        value = escapedValue;
    }

    public String getValue() {
        return value;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getNotExcapedValue() {
        StringBuilder buffer = new StringBuilder(value);
        int index = 0;
        int deletesNum = 0;
        while ((index = value.indexOf("''", index)) != -1) {
            buffer.deleteCharAt(index - deletesNum);
            index += 2;
            deletesNum++;
        }
        return buffer.toString();
    }

    public void setValue(String string) {
        value = string;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    @Override
    public String toString() {
        return (prefix != null ? prefix : "") + "'" + value + "'";
    }
}

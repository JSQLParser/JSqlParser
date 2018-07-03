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
    private Character prefix = null;

    /*
    N - SQLServer Unicode encoding
    U - Oracle Unicode encoding
    E - Postgresql Unicode encoding
     */
    public static final List<Character> ALLOWED_PREFIXES = Arrays.asList('N', 'U', 'E');

    public StringValue(String escapedValue) {
        // romoving "'" at the start and at the end
        if (escapedValue.startsWith("'") && escapedValue.endsWith("'")) {
            value = escapedValue.substring(1, escapedValue.length() - 1);
            return;
        }

        if (escapedValue.length() > 2) {
            char p = Character.toUpperCase(escapedValue.charAt(0));
            if (ALLOWED_PREFIXES.contains(p) && escapedValue.charAt(1) == '\'' && escapedValue.endsWith("'")) {
                this.prefix = p;
                value = escapedValue.substring(2, escapedValue.length() - 1);
                return;
            }
        }

        value = escapedValue;
    }

    // copy constructor
    public StringValue(String value, Character prefix) {
        this.value = value;
        this.prefix = prefix;
    }

    public String getValue() {
        return value;
    }

    public Character getPrefix() {
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

    public void setPrefix(Character prefix) {
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

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

/**
 * A string as in 'example_string'
 */
public class StringValue implements Expression {

    private String value = "";
    private String prefix = null;

    public StringValue(String escapedValue) {
      // removing "'" at the start and at the end
      if (escapedValue.startsWith("'") && escapedValue.endsWith("'")) {
          value = escapedValue.substring(1, escapedValue.length() - 1);
      } else if( (escapedValue.startsWith("N'") || escapedValue.startsWith("n'")) && escapedValue.endsWith("'")) {
        prefix = escapedValue.substring(0, 1);
        value = escapedValue.substring(2, escapedValue.length() - 1);
      } else {
          value = escapedValue;
      }
  }    

    public String getValue() {
        return value;
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

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sbString = new StringBuilder();

        if(prefix != null) {
          sbString.append(prefix);
        }

        sbString.append('\'')
        .append(value)
        .append('\'');

        return sbString.toString();
    }
}

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

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Oracle Hint Expression
 *
 * @author valdo
 */
public class OracleHint extends ASTNodeAccessImpl implements Expression {

    private static final Pattern SINGLE_LINE = Pattern.compile("--\\+ *([^ ].*[^ ])");
    private static final Pattern MULTI_LINE = Pattern.
            compile("\\/\\*\\+ *([^ ].*[^ ]) *\\*+\\/", Pattern.MULTILINE | Pattern.DOTALL);

    private String value;
    private boolean singleLine = false;

    public static boolean isHintMatch(String comment) {
        return SINGLE_LINE.matcher(comment).find()
                || MULTI_LINE.matcher(comment).find();
    }

    public final void setComment(String comment) {
        Matcher m;
        m = SINGLE_LINE.matcher(comment);
        if (m.find()) {
            this.value = m.group(1);
            this.singleLine = true;
            return;
        }
        m = MULTI_LINE.matcher(comment);
        if (m.find()) {
            this.value = m.group(1);
            this.singleLine = false;
        }
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isSingleLine() {
        return singleLine;
    }

    public void setSingleLine(boolean singleLine) {
        this.singleLine = singleLine;
    }

    @Override
    public void accept(ExpressionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        if (singleLine) {
            return "--+ " + value + "\n";
        } else {
            return "/*+ " + value + " */";
        }
    }

}

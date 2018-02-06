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
package net.sf.jsqlparser.statement.select;

import java.util.List;

import lombok.Data;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Column;

/**
 * A join clause
 */
@Data
public class Join extends ASTNodeAccessImpl {

    /**
     * Whether is a "OUTER" join
     */
    private boolean outer = false;
    /**
     * Whether is a "RIGHT" join
     */
    private boolean right = false;
    /**
     * Whether is a "LEFT" join
     */
    private boolean left = false;
    /**
     * Whether is a "NATURAL" join
     */
    private boolean natural = false;
    /**
     * Whether is a "FULL" join
     */
    private boolean full = false;
    /**
     * Whether is a "INNER" join
     */
    private boolean inner = false;
    /**
     * Whether is a tab1,tab2 join
     */
    private boolean simple = false;
    private boolean cross = false;
    /**
     * Whether is a "SEMI" join
     */
    private boolean semi = false;
    /**
     * The right item of the join
     */
    private FromItem rightItem;
    /**
     * The "ON" expression (if any)
     */
    private Expression onExpression;
    /**
     * The "USING" list of {@link net.sf.jsqlparser.schema.Column}s (if any)
     */
    private List<Column> usingColumns;

    @Override
    public String toString() {
        if (isSimple()) {
            return "" + rightItem;
        } else {
            String type = "";

            if (isRight()) {
                type += "RIGHT ";
            } else if (isNatural()) {
                type += "NATURAL ";
            } else if (isFull()) {
                type += "FULL ";
            } else if (isLeft()) {
                type += "LEFT ";
            } else if (isCross()) {
                type += "CROSS ";
            }

            if (isOuter()) {
                type += "OUTER ";
            } else if (isInner()) {
                type += "INNER ";
            } else if (isSemi()) {
                type += "SEMI ";
            }

            return type + "JOIN " + rightItem + ((onExpression != null) ? " ON " + onExpression + "" : "")
                + PlainSelect.getFormatedList(usingColumns, "USING", true, true);
        }
    }
}

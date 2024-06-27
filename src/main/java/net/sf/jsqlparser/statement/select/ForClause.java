/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2023 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

public class ForClause extends ASTNodeAccessImpl {
    private ForOption forOption;

    public ForOption getForOption() {
        return forOption;
    }

    public ForClause setForOption(String forOption) {
        this.forOption = ForOption.from(forOption);
        return this;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    public enum ForOption {
        BROWSE, XML, JSON;

        public static ForOption from(String option) {
            return Enum.valueOf(ForOption.class, option.toUpperCase());
        }
    }
}

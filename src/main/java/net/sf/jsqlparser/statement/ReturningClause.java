/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2023 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.statement.select.SelectItem;

import java.util.ArrayList;
import java.util.List;

/**
 * RETURNING clause according to <a href=
 * "https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/DELETE.html#GUID-156845A5-B626-412B-9F95-8869B988ABD7"
 * /> Part of UPDATE, INSERT, DELETE statements
 */

public class ReturningClause extends ArrayList<SelectItem<?>> {
    /**
     * List of output targets like Table or UserVariable
     */
    private final List<Object> dataItems;
    private Keyword keyword;

    public ReturningClause(Keyword keyword, List<SelectItem<?>> selectItems,
            List<Object> dataItems) {
        this.keyword = keyword;
        this.addAll(selectItems);
        this.dataItems = dataItems;
    }

    public ReturningClause(String keyword, List<SelectItem<?>> selectItems,
            List<Object> dataItems) {
        this(Keyword.from(keyword), selectItems, dataItems);
    }

    public ReturningClause(Keyword keyword, List<SelectItem<?>> selectItems) {
        this(keyword, selectItems, null);
    }

    public ReturningClause(String keyword, List<SelectItem<?>> selectItems) {
        this(Keyword.valueOf(keyword), selectItems, null);
    }

    public Keyword getKeyword() {
        return keyword;
    }

    public ReturningClause setKeyword(Keyword keyword) {
        this.keyword = keyword;
        return this;
    }

    public List<?> getDataItems() {
        return dataItems;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append(" ").append(keyword).append(" ");
        for (int i = 0; i < size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(get(i));
        }

        if (dataItems != null && !dataItems.isEmpty()) {
            builder.append(" INTO ");
            for (int i = 0; i < dataItems.size(); i++) {
                if (i > 0) {
                    builder.append(" ,");
                }
                builder.append(dataItems.get(i));
            }
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    public enum Keyword {
        RETURN, RETURNING;

        public static Keyword from(String keyword) {
            return Enum.valueOf(Keyword.class, keyword.toUpperCase());
        }
    }
}

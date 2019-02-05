/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2017 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import java.util.List;

public class MySQLIndexHint  {

    private final String action;
    private final String indexQualifier;
    private final List<String> indexNames;

    public MySQLIndexHint(String action, String indexQualifier, List<String> indexNames) {
        this.action = action;
        this.indexQualifier = indexQualifier;
        this.indexNames = indexNames;
    }

    @Override
    public String toString() {
        // use|ignore|force key|index (index1,...,indexN)
        StringBuilder buffer = new StringBuilder();
        buffer.append(" ").append(action).append(" ").append(indexQualifier).append(" (");
        for (int i = 0; i < indexNames.size(); i++) {
            if (i > 0) {
                buffer.append(",");
            }
            buffer.append(indexNames.get(i));
        }
        buffer.append(")");
        return buffer.toString();
    }
}

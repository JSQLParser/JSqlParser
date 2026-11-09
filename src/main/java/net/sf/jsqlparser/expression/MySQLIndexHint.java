/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import java.io.Serializable;
import java.util.List;

/**
 * A MySQL index hint after a table reference:
 * {@code {USE|IGNORE|FORCE} {INDEX|KEY} [FOR {JOIN|ORDER BY|GROUP BY}] (index_list)}.
 * <p>
 * A table may carry several hints, see {@code Table.getIndexHints()}. {@code USE INDEX ()} has an
 * empty index list.
 */
public class MySQLIndexHint implements Serializable {

    private final String action;
    private final String indexQualifier;
    private final Scope scope;
    private final List<String> indexNames;

    public MySQLIndexHint(String action, String indexQualifier, List<String> indexNames) {
        this(action, indexQualifier, null, indexNames);
    }

    /**
     * @param action {@code USE}, {@code IGNORE} or {@code FORCE}
     * @param indexQualifier {@code INDEX} or {@code KEY}
     * @param scope the {@code FOR JOIN|ORDER BY|GROUP BY} scope, or {@code null} for every use
     * @param indexNames the index names, empty for {@code USE INDEX ()}
     */
    public MySQLIndexHint(String action, String indexQualifier, Scope scope,
            List<String> indexNames) {
        this.action = action;
        this.indexQualifier = indexQualifier;
        this.scope = scope;
        this.indexNames = indexNames;
    }

    public String getAction() {
        return action;
    }

    public String getIndexQualifier() {
        return indexQualifier;
    }

    /**
     * Returns the part of the query the hint applies to, or {@code null} when no {@code FOR} clause
     * was given.
     */
    public Scope getScope() {
        return scope;
    }

    public List<String> getIndexNames() {
        return indexNames;
    }

    /** Appends {@code " USE INDEX [FOR scope] (i1,i2)"}, with a leading space. */
    public StringBuilder appendTo(StringBuilder builder) {
        // use|ignore|force key|index [for join|order by|group by] (index1,...,indexN)
        builder.append(" ").append(action).append(" ").append(indexQualifier);
        if (scope != null) {
            builder.append(" FOR ").append(scope.getKeywords());
        }
        builder.append(" (");
        for (int i = 0; i < indexNames.size(); i++) {
            if (i > 0) {
                builder.append(",");
            }
            builder.append(indexNames.get(i));
        }
        return builder.append(")");
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    /** The {@code FOR} clause of an index hint. */
    public enum Scope {
        JOIN("JOIN"), ORDER_BY("ORDER BY"), GROUP_BY("GROUP BY");

        private final String keywords;

        Scope(String keywords) {
            this.keywords = keywords;
        }

        /** Returns the keywords after {@code FOR}, for example {@code ORDER BY}. */
        public String getKeywords() {
            return keywords;
        }
    }
}

/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2024 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.merge;

import net.sf.jsqlparser.expression.Expression;

import java.util.function.Consumer;

/**
 * The match condition that starts a {@code WHEN} clause of {@code MERGE}. The standard knows
 * {@code WHEN [NOT] MATCHED}; PostgreSQL, SQL Server and BigQuery also qualify {@code NOT MATCHED}
 * with {@code BY SOURCE} or {@code BY TARGET}.
 */
public enum MergeWhen {
    MATCHED("WHEN MATCHED", null), NOT_MATCHED("WHEN NOT MATCHED", null), NOT_MATCHED_BY_SOURCE(
            "WHEN NOT MATCHED BY SOURCE", MergeSide.SOURCE), NOT_MATCHED_BY_TARGET(
                    "WHEN NOT MATCHED BY TARGET", MergeSide.TARGET);

    private final String text;
    private final MergeSide side;

    MergeWhen(String text, MergeSide side) {
        this.text = text;
        this.side = side;
    }

    /** The condition of a clause that acts on rows of the target: an UPDATE or a DELETE. */
    public static MergeWhen ofTargetRow(MergeSide side) {
        return side == MergeSide.SOURCE ? NOT_MATCHED_BY_SOURCE : MATCHED;
    }

    /** The condition of a clause that acts on rows of the source: an INSERT. */
    public static MergeWhen ofSourceRow(MergeSide side) {
        return side == MergeSide.TARGET ? NOT_MATCHED_BY_TARGET : NOT_MATCHED;
    }

    /** The condition of {@code WHEN NOT MATCHED [BY side]}. */
    public static MergeWhen ofNotMatched(MergeSide side) {
        return side == MergeSide.SOURCE ? NOT_MATCHED_BY_SOURCE : ofSourceRow(side);
    }

    /** The {@code BY SOURCE} or {@code BY TARGET} qualifier, null when the clause has none. */
    public MergeSide getSide() {
        return side;
    }

    /**
     * Appends {@code  WHEN ... [AND predicate] THEN } with a leading and a trailing space, letting
     * the renderer write the predicate.
     */
    public StringBuilder appendTo(StringBuilder builder, Expression andPredicate,
            Consumer<Expression> expressionRenderer) {
        builder.append(' ').append(text);
        if (andPredicate != null) {
            builder.append(" AND ");
            expressionRenderer.accept(andPredicate);
        }
        return builder.append(" THEN ");
    }

    @Override
    public String toString() {
        return text;
    }
}

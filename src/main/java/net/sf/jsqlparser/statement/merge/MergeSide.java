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

/**
 * Identifies the side of a {@code MERGE ... WHEN NOT MATCHED [BY TARGET|BY SOURCE]} clause.
 *
 * <p>
 * Standard SQL only knows {@code WHEN [NOT] MATCHED}; SQL Server and BigQuery additionally allow
 * the NOT MATCHED clause to be qualified with {@code BY TARGET} (the default, allows
 * {@code INSERT}) or {@code BY SOURCE} (allows {@code UPDATE}/{@code DELETE}).
 * </p>
 */
public enum MergeSide {
    TARGET, SOURCE;

    /**
     * Parses a {@code BY TARGET}/{@code BY SOURCE} qualifier, case-insensitively.
     *
     * @param image the raw identifier image following {@code BY}
     * @return the matching {@link MergeSide}
     * @throws IllegalArgumentException if {@code image} is neither {@code TARGET} nor
     *         {@code SOURCE}
     */
    public static MergeSide fromImage(String image) {
        for (MergeSide value : values()) {
            if (value.name().equalsIgnoreCase(image)) {
                return value;
            }
        }
        throw new IllegalArgumentException(
                "Expected TARGET or SOURCE after BY but found: " + image);
    }
}

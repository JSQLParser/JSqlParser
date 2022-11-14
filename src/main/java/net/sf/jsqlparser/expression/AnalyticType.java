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

public enum AnalyticType {
    OVER,
    WITHIN_GROUP,

    WITHIN_GROUP_OVER,
    FILTER_ONLY
}

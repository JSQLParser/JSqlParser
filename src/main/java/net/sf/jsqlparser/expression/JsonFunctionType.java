/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */

package net.sf.jsqlparser.expression;

/**
 * @author <a href="mailto:andreas@manticore-projects.com">Andreas Reichel</a>
 */
public enum JsonFunctionType {
    OBJECT, ARRAY,

    /**
     * Not used anymore
     */
    @Deprecated
    POSTGRES_OBJECT,

    /**
     * Not used anymore
     */
    @Deprecated
    MYSQL_OBJECT;

    public static JsonFunctionType from(String type) {
        return Enum.valueOf(JsonFunctionType.class, type.toUpperCase());
    }
}

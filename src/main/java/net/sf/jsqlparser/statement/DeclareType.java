/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

/**
 * @author tobens
 */
public enum DeclareType {
    TABLE, AS, TYPE;

    public static DeclareType from(String type) {
        return Enum.valueOf(DeclareType.class, type.toUpperCase());
    }
}

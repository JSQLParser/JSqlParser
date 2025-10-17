/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
/*
 * Copyright (C) 2021 JSQLParser.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA
 */

package net.sf.jsqlparser.expression.json;

/**
 *
 */
public enum JsonReturnType {
    VARCHAR2("VARCHAR2"), CLOB("CLOB"), BLOB("BLOB"), NUMBER("NUMBER"), DATE("DATE"), TIMESTAMP(
            "TIMESTAMP"), TIMESTAMP_WITH_TIMEZONE(
                    "TIMESTAMP WITH TIMEZONE"), BOOLEAN("BOOLEAN"), VECTOR("VECTOR"), JSON("JSON"),

    // VARCHAR2( x BYTE)
    VARCHAR2_BYTE("VARCHAR2"),

    // VARCHAR2( x CHAR)
    VARCHAR2_CHAR("VARCHAR2"),
    ;

    private final String value;

    JsonReturnType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static JsonReturnType from(String type) {
        return Enum.valueOf(JsonReturnType.class, type.toUpperCase());
    }

    /**
     * @see "https://docs.oracle.com/en/database/oracle/oracle-database/26/sqlrf/JSON_QUERY.html#GUID-6D396EC4-D2AA-43D2-8F5D-08D646A4A2D9__CJADJIIJ"
     */
    public boolean isValidForJsonQueryReturnType() {
        switch (this) {
            case VARCHAR2:
            case CLOB:
            case BLOB:
            case JSON:
            case VECTOR:
                return true;
            default:
                return false;
        }
    }

}

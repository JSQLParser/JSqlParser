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

package net.sf.jsqlparser.expression;

/**
 *
 */
public enum JsonQueryWrapperType {
    WITHOUT_WRAPPER("WITHOUT WRAPPER"), WITHOUT_ARRAY_WRAPPER(
            "WITHOUT ARRAY WRAPPER"), WITH_WRAPPER("WITH WRAPPER"), WITH_ARRAY_WRAPPER(
                    "WITH ARRAY WRAPPER"), WITH_UNCONDITIONAL_WRAPPER(
                            "WITH UNCONDITIONAL WRAPPER"), WITH_UNCONDITIONAL_ARRAY_WRAPPER(
                                    "WITH UNCONDITIONAL ARRAY WRAPPER"), WITH_CONDITIONAL_WRAPPER(
                                            "WITH CONDITIONAL WRAPPER"), WITH_CONDITIONAL_ARRAY_WRAPPER(
                                                    "WITH CONDITIONAL ARRAY WRAPPER");

    private final String value;

    JsonQueryWrapperType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static JsonQueryWrapperType from(String type) {
        return Enum.valueOf(JsonQueryWrapperType.class, type.toUpperCase());
    }

    public static JsonQueryWrapperType fromWithParts(boolean isArray, boolean isConditional,
            boolean isUnconditional) {
        if (isArray) {
            if (isConditional) {
                return JsonQueryWrapperType.WITH_CONDITIONAL_ARRAY_WRAPPER;
            } else if (isUnconditional) {
                return JsonQueryWrapperType.WITH_UNCONDITIONAL_ARRAY_WRAPPER;
            } else {
                return JsonQueryWrapperType.WITH_ARRAY_WRAPPER;
            }
        } else {
            if (isConditional) {
                return JsonQueryWrapperType.WITH_CONDITIONAL_WRAPPER;
            } else if (isUnconditional) {
                return JsonQueryWrapperType.WITH_UNCONDITIONAL_WRAPPER;
            } else {
                return JsonQueryWrapperType.WITH_WRAPPER;
            }
        }
    }
}

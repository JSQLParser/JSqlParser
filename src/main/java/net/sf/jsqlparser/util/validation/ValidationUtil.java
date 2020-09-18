/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ValidationUtil {

    private ValidationUtil() {
        // no construction allowed
    }

    /**
     * @param prefix
     * @param values
     * @return a list of strings prefixed with given prefix and "." as separator, or
     *         <code>null</code> if
     *         given collection is null.
     */
    public static List<String> concat(String prefix, Collection<String> values) {
        return values == null ? null : ValidationUtil.concat(prefix, values.stream());
    }

    /**
     * @param prefix
     * @param values
     * @return a list of strings prefixed with given prefix and "." as separator.
     */
    public static List<String> concat(String prefix, Stream<String> values) {
        return values.map(v -> ValidationUtil.concat(prefix, v)).collect(Collectors.toList());
    }

    /**
     * @param prefix
     * @param name
     * @return if given name is not <code>null</code>, the name is prefixed by the
     *         given prefix and "." as separator, otherwise <code>null</code>
     */
    public static String concat(String prefix, String name) {
        return name == null ? null : new StringBuilder(prefix).append(".").append(name).toString();
    }

}

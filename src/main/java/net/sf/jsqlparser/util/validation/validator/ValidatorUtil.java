package net.sf.jsqlparser.util.validation.validator;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ValidatorUtil {

    private ValidatorUtil() {
        // no construction allowed
    }

    /**
     * @param prefix
     * @param values
     * @return a list of strings prefixed with given prefix, or <code>null</code> if
     *         given collection is null.
     */
    public static List<String> concat(String prefix, Collection<String> values) {
        return values == null ? null : ValidatorUtil.concat(prefix, values.stream());
    }

    /**
     * @param prefix
     * @param values
     * @return a list of strings prefixed with given prefix.
     */
    public static List<String> concat(String prefix, Stream<String> values) {
        return values.map(v -> ValidatorUtil.concat(prefix, v)).collect(Collectors.toList());
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

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

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface Validation {

    /**
     * @param databaseTypes
     * @return <code>true</code>, if the given databaseTypes support this feature.
     *         <code>false</code> otherwise.
     */
    default boolean isSupported() {
        return getValidationErrors().isEmpty();
    }

    default boolean isSupported(DatabaseType... databaseTypes) {
        return getValidationErrors(databaseTypes).isEmpty();
    }

    /**
     * Overwrite this method, if the feature support is not given on any
     * {@link DatabaseType}
     * 
     * @param databaseTypes
     * @return the requested databaseTypes mapped to a list of error-messages
     *         (containing hints to used sql-string
     *         snippets not supported).
     */
    public Map<DatabaseType, Set<String>> getValidationErrors();

    default Map<DatabaseType, Set<String>> getValidationErrors(DatabaseType... databaseTypes) {
        return getValidationErrors(Arrays.asList(databaseTypes));
    }

    default Map<DatabaseType, Set<String>> getValidationErrors(Collection<DatabaseType> databaseTypes) {
        Map<DatabaseType, Set<String>> map = new EnumMap<>(DatabaseType.class);

        EnumSet<DatabaseType> notRequested = EnumSet.allOf(DatabaseType.class);
        notRequested.removeAll(databaseTypes);

        map.putAll(getValidationErrors());

        for (DatabaseType type : notRequested) {
            map.remove(type);
        }
        return map;
    }

    /**
     * Utility-function to map all {@link DatabaseType}'s except the supported ones
     * to error-messages (containing hints to used sql-string
     * snippets not supported).
     * 
     * @param databaseTypes
     * @param unsupportedSqlOrFeature
     * @return
     */
    static Map<DatabaseType, Set<String>> mapAllExcept(EnumSet<DatabaseType> databaseTypes,
            String... unsupportedSqlOrFeature) {
        EnumSet<DatabaseType> set = EnumSet.allOf(DatabaseType.class);
        set.removeAll(databaseTypes);
        List<String> asList = Arrays.asList(unsupportedSqlOrFeature);
        return set.stream().collect(
                Collectors.toMap(Function.identity(), k -> new HashSet<>(asList)));
    }

    /**
     * Utility-function to map all {@link DatabaseType}'s except the supported ones
     * to error-messages (containing hints to used sql-string
     * snippets not supported).
     * 
     * @param target
     * @param databaseTypes
     * @param unsupportedSqlOrFeature
     * @return the given target-map containing the errors
     */
    static Map<DatabaseType, Set<String>> mapAllExcept(Map<DatabaseType, Set<String>> target,
            EnumSet<DatabaseType> databaseTypes, String... unsupportedSqlOrFeature) {
        return mergeTo(mapAllExcept(databaseTypes, unsupportedSqlOrFeature), target);
    }

    /**
     * merge source-map to target-map
     * 
     * @param source
     * @param target
     * @return
     */
    static Map<DatabaseType, Set<String>> mergeTo(Map<DatabaseType, Set<String>> source,
            Map<DatabaseType, Set<String>> target) {
        for (Entry<DatabaseType, Set<String>> errors : source.entrySet()) {
            target.computeIfAbsent(errors.getKey(), k -> errors.getValue()).addAll(errors.getValue());
        }
        return target;
    }

}

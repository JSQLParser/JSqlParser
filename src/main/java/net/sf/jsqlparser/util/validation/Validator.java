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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.jsqlparser.parser.feature.FeatureConfiguration;

import java.util.Set;

/**
 * @author gitmotte
 * @param <S>
 */
public interface Validator<S> {

    /**
     * @return <code>true</code>, all {@link ValidationCapability}'s have no errors
     */
    default boolean isValid() {
        return getValidationErrors().isEmpty();
    }

    /**
     * @param capabilities
     * @return <code>true</code>, if the given {@link ValidationCapability}'s have no errors.
     *         <code>false</code> otherwise.
     */
    default boolean isValid(ValidationCapability... capabilities) {
        return getValidationErrors(capabilities).isEmpty();
    }

    /**
     * @return the {@link ValidationCapability}'s requested mapped to a set of error-messages
     */
    public Map<ValidationCapability, Set<ValidationException>> getValidationErrors();

    /**
     * @param capabilities
     * @return the filtered view of requested {@link ValidationCapability}'s mapped to a set
     *         of error-messages
     */
    default Map<ValidationCapability, Set<ValidationException>> getValidationErrors(
            ValidationCapability... capabilities) {
        return getValidationErrors(Arrays.asList(capabilities));
    }

    /**
     * @param capabilities
     * @return the filtered view of requested {@link ValidationCapability}'s mapped
     *         to a set of error-messages
     */
    default Map<ValidationCapability, Set<ValidationException>> getValidationErrors(
            Collection<ValidationCapability> capabilities) {
        Map<ValidationCapability, Set<ValidationException>> map = new HashMap<>();
        for (Entry<ValidationCapability, Set<ValidationException>> e : getValidationErrors().entrySet()) {
            if (capabilities.contains(e.getKey())) {
                map.put(e.getKey(), e.getValue());
            }
        }
        return map;
    }

    /**
     * Set the {@link ValidationCapability}'s this {@link Validator} should check.
     *
     * @param capabilities
     */
    public void setCapabilities(Collection<ValidationCapability> capabilities);

    /**
     * @param configuration
     */
    public void setConfiguration(FeatureConfiguration configuration);

    /**
     * @param ctx
     */
    public void setContext(ValidationContext ctx);

    /**
     * validates given statement.
     *
     * @param statement
     * @see #getValidationErrors()
     * @see #getValidationErrors(Collection)
     * @see #getValidationErrors(ValidationCapability...)
     */
    public abstract void validate(S statement);

}

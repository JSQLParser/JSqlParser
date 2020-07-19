/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation.validator;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.parser.feature.FeatureConfiguration;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.util.validation.ValidationCapability;
import net.sf.jsqlparser.util.validation.ValidationContext;
import net.sf.jsqlparser.util.validation.Validator;
import net.sf.jsqlparser.util.validation.feature.FeatureContext;
import net.sf.jsqlparser.util.validation.feature.FeatureSetValidation;
import net.sf.jsqlparser.util.validation.metadata.DatabaseMetaDataValidation;
import net.sf.jsqlparser.util.validation.metadata.MetadataContext;
import net.sf.jsqlparser.util.validation.metadata.NamedObject;

/**
 * A abstract base for a Validation
 *
 * @param <S> the type of statement this DeParser supports
 * @author gitmotte
 */
public abstract class AbstractValidator<S> implements Validator<S> {

    private ValidationContext context = new ValidationContext();

    private Map<ValidationCapability, Set<String>> errors = new HashMap<>();

    private Map<Class<? extends AbstractValidator<?>>, AbstractValidator<?>> validatorForwards = new HashMap<>();

    public <T extends AbstractValidator<?>> T getValidator(Class<T> type) {
        return type.cast(validatorForwards.computeIfAbsent(type, this::newObject));
    }

    private <E extends Validator<?>> E newObject(Class<E> type) {
        try {
            E e = type.cast(type.getConstructor().newInstance());
            e.setContext(context());
            return e;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Type " + type + " cannot be constructed by empty constructor!");
        }
    }

    protected Consumer<String> getMessageConsumer(ValidationCapability c) {
        return s -> putError(c, s);
    }

    protected ValidationContext context() {
        return context(true);
    }

    protected ValidationContext context(boolean reInit) {
        return context.reinit(reInit);
    }

    protected void putError(ValidationCapability c, String error) {
        errors.computeIfAbsent(c, k -> new HashSet<>()).add(error);
    }

    @Override
    public final Map<ValidationCapability, Set<String>> getValidationErrors() {
        Map<ValidationCapability, Set<String>> map = new HashMap<>();
        map.putAll(errors);
        for (AbstractValidator<?> v : validatorForwards.values()) {
            map.putAll(v.getValidationErrors());
        }
        return map;
    }

    @Override
    public final void setCapabilities(Collection<ValidationCapability> capabilities) {
        context().setCapabilities(capabilities);
    }

    @Override
    public final void setConfiguration(FeatureConfiguration configuration) {
        context().setConfiguration(configuration);
    }

    public Collection<ValidationCapability> getCapabilities() {
        return context().getCapabilities();
    }

    @Override
    public final void setContext(ValidationContext context) {
        this.context = context;
    }

    protected void validateOptionalExpressions(List<Expression> expressions) {
        if (expressions != null && !expressions.isEmpty()) {
            ExpressionValidator v = getValidator(ExpressionValidator.class);
            expressions.forEach(v::validate);
        }
    }

    protected void validateOptionalColumns(List<Column> columns) {
        if (columns != null && !columns.isEmpty()) {
            ExpressionValidator e = getValidator(ExpressionValidator.class);
            columns.forEach(c -> c.accept(e));
        }
    }

    /**
     * Iterates through all {@link ValidationCapability} and validates the feature
     * with {@link #validateFeature(ValidationCapability, Feature)
     *
     * @param feature
     */
    protected void validateFeature(Feature feature) {
        for (ValidationCapability c : getCapabilities()) {
            validateFeature(c, feature);
        }
    }

    /**
     * Iterates through all {@link ValidationCapability} and validates
     * <ul>
     * <li>the name with
     * {@link #validateName(ValidationCapability, NamedObject, String)}</li>
     * <li>the feature with
     * {@link #validateFeature(ValidationCapability, Feature)}</li>
     * </ul>
     *
     * @param feature
     * @param namedObject
     * @param fqn
     */
    protected void validateFeatureAndName(Feature feature, NamedObject namedObject, String fqn) {
        for (ValidationCapability c : getCapabilities()) {
            validateFeature(c, feature);
            validateName(c, namedObject, fqn);
        }
    }

    /**
     * Iterates through all {@link ValidationCapability} and validates for the name
     * with {@link #validateName(ValidationCapability, NamedObject, String)}
     *
     * @param namedObject
     * @param fqn
     */
    protected void validateName(NamedObject namedObject, String fqn) {
        for (ValidationCapability c : getCapabilities()) {
            validateName(c, namedObject, fqn);
        }
    }

    /**
     * Validates the feature if given {@link ValidationCapability} is a
     * {@link FeatureSetValidation}
     *
     * @param capability
     * @param feature
     */
    protected void validateFeature(ValidationCapability capability, Feature feature) {
        if (capability instanceof FeatureSetValidation) {
            capability.validate(context().put(FeatureContext.feature, feature),
                    getMessageConsumer(capability));
        }
    }

    /**
     * Validates the feature if given {@link ValidationCapability} is a
     * {@link DatabaseMetaDataValidation}
     *
     * @param capability
     * @param feature
     */
    protected void validateName(ValidationCapability capability, NamedObject namedObject, String fqn) {
        if (capability instanceof DatabaseMetaDataValidation) {
            capability.validate(context()
                    .put(MetadataContext.namedobject, namedObject)
                    .put(MetadataContext.fqn, fqn),
                    getMessageConsumer(capability));
        }
    }

    protected void validateOptionalColumnName(String name, ValidationCapability c) {
        validateOptionalName(name, NamedObject.column, c);
    }

    protected void validateOptionalColumnNames(List<String> columnNames, ValidationCapability c) {
        if (columnNames != null) {
            columnNames.forEach(n -> validateOptionalName(n, NamedObject.column, c));
        }
    }

    protected void validateOptionalName(String name, NamedObject namedObject, ValidationCapability c) {
        if (name != null) {
            validateName(c, namedObject, name);
        }
    }

}

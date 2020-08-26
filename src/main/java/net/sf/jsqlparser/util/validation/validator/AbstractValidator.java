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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.parser.feature.FeatureConfiguration;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.util.validation.ValidationCapability;
import net.sf.jsqlparser.util.validation.ValidationContext;
import net.sf.jsqlparser.util.validation.ValidationException;
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

    private Map<ValidationCapability, Set<ValidationException>> errors = new HashMap<>();

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

    protected Consumer<ValidationException> getMessageConsumer(ValidationCapability c) {
        return s -> putError(c, s);
    }

    protected ValidationContext context() {
        return context(true);
    }

    protected ValidationContext context(boolean reInit) {
        return context.reinit(reInit);
    }

    /**
     * adds an error for this {@link ValidationCapability}
     *
     * @param capability
     * @param error
     */
    protected void putError(ValidationCapability capability, ValidationException error) {
        errors.computeIfAbsent(capability, k -> new HashSet<>()).add(error);
    }

    @Override
    public final Map<ValidationCapability, Set<ValidationException>> getValidationErrors() {
        Map<ValidationCapability, Set<ValidationException>> map = new HashMap<>();
        map.putAll(errors);
        for (AbstractValidator<?> v : validatorForwards.values()) {
            for (Entry<ValidationCapability, Set<ValidationException>> e : v.getValidationErrors().entrySet()) {
                Set<ValidationException> set = map.get(e.getKey());
                if (set == null) {
                    map.put(e.getKey(), e.getValue());
                } else {
                    set.addAll(e.getValue());
                }
            }
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

    protected <E> void validateOptional(E element, Consumer<E> elementConsumer) {
        if (element != null) {
            elementConsumer.accept(element);
        }
    }

    protected <E, V extends Validator<?>> void validateOptionalList(
            List<E> elementList, Supplier<V> validatorSupplier, BiConsumer<E, V> elementConsumer) {
        if (elementList != null && !elementList.isEmpty()) {
            V validator = validatorSupplier.get();
            elementList.forEach(e -> elementConsumer.accept(e, validator));
        }
    }

    protected void validateOptionalExpression(Expression expression) {
        validateOptional(expression, e -> e.accept(getValidator(ExpressionValidator.class)));
    }

    protected void validateOptionalExpression(Expression expression, ExpressionValidator v) {
        validateOptional(expression, e -> e.accept(v));
    }

    protected void validateOptionalExpressions(List<? extends Expression> expressions) {
        validateOptionalList(expressions, () -> getValidator(ExpressionValidator.class), (o, v) -> o.accept(v));
    }

    protected void validateOptionalFromItems(FromItem... fromItems) {
        validateOptionalFromItems(Arrays.asList(fromItems));
    }

    protected void validateOptionalFromItems(List<? extends FromItem> fromItems) {
        validateOptionalList(fromItems, () -> getValidator(SelectValidator.class),
                this::validateOptionalFromItem);
    }

    protected void validateOptionalOrderByElements(List<OrderByElement> orderByElements) {
        validateOptionalList(orderByElements, () -> getValidator(OrderByValidator.class), (o, v) -> o.accept(v));
    }

    protected void validateOptionalFromItem(FromItem fromItem) {
        validateOptional(fromItem, i -> i.accept(getValidator(SelectValidator.class)));
    }

    protected void validateOptionalFromItem(FromItem fromItem, SelectValidator v) {
        validateOptional(fromItem, i -> i.accept(v));
    }

    protected void validateOptionalItemsList(ItemsList itemsList) {
        validateOptional(itemsList, i -> i.accept(getValidator(ItemsListValidator.class)));
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
     * {@link FeatureSetValidation} and condition is <code>true</code>
     *
     * @param capability
     * @param condition
     * @param feature
     */
    protected void validateFeature(ValidationCapability capability, boolean condition, Feature feature) {
        if (condition) {
            validateFeature(capability, feature);
        }
    }

    /**
     * validates for the feature if given elements is not empty - see
     * {@link #isNotEmpty(Collection)}
     * 
     * @param c
     * @param element
     * @param feature
     */
    protected void validateOptionalFeature(ValidationCapability c, List<?> elements, Feature feature) {
        validateFeature(c, isNotEmpty(elements), feature);
    }

    /**
     * validates for the feature if given element is not <code>null</code>
     * 
     * @param c
     * @param element
     * @param feature
     */
    protected void validateOptionalFeature(ValidationCapability c, Object element, Feature feature) {
        validateFeature(c, element != null, feature);
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

    protected boolean isNotEmpty(Collection<?> c) {
        return c != null && !c.isEmpty();
    }

    protected boolean isNotEmpty(String c) {
        return c != null && !c.isEmpty();
    }

}

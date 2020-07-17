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

import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jsqlparser.expression.Expression;

/**
 * A abstract base for a Validation
 *
 * @param <S> the type of statement this DeParser supports
 */
public abstract class AbstractValidator<S> implements Validation {

    protected Map<DatabaseType, Set<String>> errors = new EnumMap<>(DatabaseType.class);

    private Map<Class<? extends AbstractValidator<?>>, AbstractValidator<?>> validatorForwards = new HashMap<>();

    public <T extends AbstractValidator<?>> T getValidator(Class<T> type) {
        return type.cast(validatorForwards.computeIfAbsent(type, AbstractValidator::newObject));
    }

    private static <E> E newObject(Class<E> type) {
        try {
            return type.cast(type.getConstructor().newInstance());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Type " + type + " cannot be constructed by empty constructor!");
        }
    }

    @Override
    public final Map<DatabaseType, Set<String>> getValidationErrors() {
        Map<DatabaseType, Set<String>> map = new EnumMap<>(DatabaseType.class);
        map.putAll(errors);
        for (AbstractValidator<?> v : validatorForwards.values()) {
            Validation.mergeTo(v.getValidationErrors(), map);
        }
        return map;
    }

    protected void validate(List<Expression> expressions) {
        ExpressionValidator v = getValidator(ExpressionValidator.class);
        expressions.forEach(v::validate);
    }

    public abstract void validate(S statement);
}

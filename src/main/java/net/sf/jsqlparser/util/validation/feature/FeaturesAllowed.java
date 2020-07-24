/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation.feature;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.parser.feature.FeatureSet;
import net.sf.jsqlparser.util.validation.ValidationException;

/**
 * Privileges/Features allowed
 *
 * @author gitmotte
 */
public class FeaturesAllowed implements FeatureSetValidation {

    /**
     * all {@link Feature}' within SQL SELECT without modification features like
     * {@link Feature#selectInto}, but jdbc-features like
     * {@link Feature#jdbcParameter} and {@link Feature#jdbcNamedParameter}
     */
    public static final FeaturesAllowed SELECT = new FeaturesAllowed(
            // always allowed if used with jdbc
            Feature.jdbcParameter,
            Feature.jdbcNamedParameter,
            // select features
            Feature.select,
            Feature.selectGroupBy,
            Feature.selectHaving,
            Feature.limit,
            Feature.limitNull,
            Feature.limitAll,
            Feature.limitOffset,
            Feature.offset,
            Feature.offsetParam,
            Feature.fetch,
            Feature.fetchFirst,
            Feature.fetchNext,
            Feature.skip,
            Feature.first,
            Feature.top,
            Feature.optimizeFor,
            Feature.selectUnique,
            Feature.distinct,
            Feature.distinctOn,
            Feature.orderBy,
            Feature.orderByNullOrdering).unmodifyable();

    /**
     * all {@link Feature}' for SQL INSERT including {@link #SELECT} and
     * {@link Feature#selectInto}
     */
    public static final FeaturesAllowed INSERT = new FeaturesAllowed(Feature.insert,
            Feature.insertValues, Feature.selectInto).add(SELECT).unmodifyable();

    /**
     * all {@link Feature}' for SQL UPDATE including {@link #SELECT}
     */
    public static final FeaturesAllowed UPDATE = new FeaturesAllowed(Feature.update)
            .add(SELECT).unmodifyable();

    /**
     * all {@link Feature}' for SQL UPDATE including {@link #SELECT}
     */
    public static final FeaturesAllowed DELETE = new FeaturesAllowed(Feature.delete)
            .add(SELECT).unmodifyable();

    public static final FeaturesAllowed EXECUTE = new FeaturesAllowed(Feature.execute).unmodifyable();
    public static final FeaturesAllowed ALTER = new FeaturesAllowed(Feature.alter).unmodifyable();
    public static final FeaturesAllowed DROP = new FeaturesAllowed(Feature.drop).unmodifyable();

    private Set<Feature> features = new HashSet<>();

    public FeaturesAllowed(FeatureSet... s) {
        add(s);
    }

    public FeaturesAllowed(Feature... f) {
        add(f);
    }

    public FeaturesAllowed add(FeatureSet... s) {
        Stream.of(s).map(FeatureSet::getFeatures).forEach(fs -> features.addAll(fs));
        return this;
    }

    public FeaturesAllowed add(Feature... f) {
        Stream.of(f).forEach(features::add);
        return this;
    }

    private FeaturesAllowed unmodifyable() {
        this.features = Collections.unmodifiableSet(features);
        return this;
    }

    /**
     * @return <code>featureName + " not allowed."</code>
     */
    @Override
    public ValidationException getMessage(Feature feature) {
        return toError(feature.name() + " not allowed.");
    }

    @Override
    public Set<Feature> getFeatures() {
        return features;
    }

}

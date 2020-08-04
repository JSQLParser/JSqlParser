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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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

    private static final String SEPERATOR_REGEX = " \\+ ";
    private static final String SEPERATOR = " + ";

    public static final FeaturesAllowed JDBC = new FeaturesAllowed("jdbc",
            // always allowed if used with jdbc
            Feature.jdbcParameter,
            Feature.jdbcNamedParameter).unmodifyable();

    /**
     * all {@link Feature}' within SQL SELECT without modification features like
     * {@link Feature#selectInto}, but jdbc-features like
     * {@link Feature#jdbcParameter} and {@link Feature#jdbcNamedParameter}
     */
    public static final FeaturesAllowed SELECT = new FeaturesAllowed("SELECT",
            // select features
            Feature.select,
            Feature.selectGroupBy,
            Feature.selectHaving,

            Feature.join,
            Feature.joinOuterSimple,
            Feature.joinSimple,
            Feature.joinRight,
            Feature.joinNatural,
            Feature.joinFull,
            Feature.joinLeft,
            Feature.joinCross,
            Feature.joinOuter,
            Feature.joinSemi,
            Feature.joinInner,
            Feature.joinStaight,
            Feature.joinApply,
            Feature.joinWindow,
            Feature.joinUsingColumns,

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
    public static final FeaturesAllowed INSERT = new FeaturesAllowed("INSERT", Feature.insert,
            Feature.insertValues, Feature.selectInto).add(SELECT).unmodifyable();

    /**
     * all {@link Feature}' for SQL UPDATE including {@link #SELECT}
     */
    public static final FeaturesAllowed UPDATE = new FeaturesAllowed("UPDATE", Feature.update)
            .add(SELECT).unmodifyable();

    /**
     * all {@link Feature}' for SQL UPDATE including {@link #SELECT}
     */
    public static final FeaturesAllowed DELETE = new FeaturesAllowed("DELETE", Feature.delete)
            .add(SELECT).unmodifyable();

    public static final FeaturesAllowed EXECUTE = new FeaturesAllowed("EXECUTE", Feature.execute).unmodifyable();
    public static final FeaturesAllowed ALTER = new FeaturesAllowed("ALTER", Feature.alter).unmodifyable();
    public static final FeaturesAllowed DROP = new FeaturesAllowed("DROP", Feature.drop).unmodifyable();

    private Set<String> names = new LinkedHashSet<>();
    private Set<Feature> features = new HashSet<>();

    /**
     * @param features
     */
    public FeaturesAllowed(Feature... features) {
        add(features);
    }

    /**
     * @param features
     */
    public FeaturesAllowed(String name, Feature... features) {
        this.names.add(name);
        add(features);
    }

    /**
     * @param featureSets
     * @return <code>this</code>
     */
    public FeaturesAllowed add(FeatureSet... featureSets) {
        Stream.of(featureSets).forEach(fs -> {
            features.addAll(fs.getFeatures());
            if (fs instanceof FeatureSetValidation) {
                names.addAll(collectNames(fs));
            }
        });
        return this;
    }

    /**
     * @param features
     * @return <code>this</code>
     */
    public FeaturesAllowed add(Feature... features) {
        Collections.addAll(this.features, features);
        return this;
    }

    /**
     * @param features
     * @return <code>this</code>
     */
    public FeaturesAllowed add(Collection<Feature> features) {
        this.features.addAll(features);
        return this;
    }

    /**
     * @param featureSets
     * @return <code>this</code>
     */
    public FeaturesAllowed remove(FeatureSet... featureSets) {
        Stream.of(featureSets).forEach(fs -> {
            features.removeAll(fs.getFeatures());
            if (fs instanceof FeatureSetValidation) {
                names.removeAll(collectNames(fs));
            }
        });
        return this;
    }

    /**
     * @param features
     * @return <code>this</code>
     */
    public FeaturesAllowed remove(Feature... features) {
        this.features.removeAll(Arrays.asList(features));
        return this;
    }

    /**
     * @param features
     * @return <code>this</code>
     */
    public FeaturesAllowed remove(Collection<Feature> features) {
        this.features.removeAll(features);
        return this;
    }

    /**
     * @return returns a modifiable copy of this {@link FeaturesAllowed} object
     * @see #unmodifyable()
     */
    public FeaturesAllowed copy() {
        return new FeaturesAllowed().add(this);
    }

    /**
     * makes the inner {@link Feature}-set unmodifiable
     *
     * @return <code>this</code>
     * @see #copy()
     */
    public FeaturesAllowed unmodifyable() {
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
    public String getName() {
        return names.isEmpty() ? FeatureSetValidation.super.getName() : names.stream().collect(Collectors.joining(SEPERATOR));
    }


    @Override
    public Set<Feature> getFeatures() {
        return features;
    }

    private List<String> collectNames(FeatureSet fs) {
        String name = ((FeatureSetValidation) fs).getName();
        List<String> collect = Stream.of(name.split(SEPERATOR_REGEX)).map(String::trim).collect(Collectors.toList());
        return collect;
    }

}

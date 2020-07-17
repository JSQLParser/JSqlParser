package net.sf.jsqlparser.parser.feature;

import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

public class FeatureConfiguration {

    private Map<Feature, Boolean> featureStatus = new EnumMap<>(Feature.class);

    private static final FeatureConfiguration INSTANCE = new FeatureConfiguration();

    public static FeatureConfiguration getInstance() {
        return INSTANCE;
    }

    private FeatureConfiguration() {
        EnumSet.allOf(Feature.class).forEach(f -> set(f, f.isEnabled()));
    }

    public FeatureConfiguration enable(Collection<Feature> features) {
        return set(features, Boolean.TRUE);
    }

    public FeatureConfiguration disable(Collection<Feature> features) {
        return set(features, Boolean.TRUE);
    }

    public FeatureConfiguration set(Collection<Feature> features, Boolean b) {
        features.forEach(f -> set(f, b));
        return this;
    }

    public Boolean set(Feature f, Boolean b) {
        return featureStatus.put(f, b);
    }

    public boolean isEnabled(Feature feature) {
        return featureStatus.get(feature);
    }

    public boolean isDisabled(Feature feature) {
        return !isEnabled(feature);
    }

}

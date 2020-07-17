package net.sf.jsqlparser.util.validation;

import java.util.Set;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.parser.feature.FeatureSet;

public interface FeatureSetValidation extends ValidationCapability, FeatureSet {

    /**
     * @param feature
     * @return <code>true</code>, if the given feature is not supported
     */
    default boolean isNotValid(Feature feature) {
        return !getFeatures().contains(feature);
    }

    /**
     * @return all supported {@link Feature}'s
     */
    @Override
    public Set<Feature> getFeatures();

    /**
     * @return <code>featureName + " not supported."</code>
     */
    @Override
    default String getErrorMessage(String featureName) {
        return featureName + " not supported.";
    }

    /**
     * @return <code>getErrorMessage(feature.name())"</code>
     */
    default String getErrorMessage(Feature feature) {
        return getErrorMessage(feature.name());
    }

}

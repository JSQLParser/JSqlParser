package net.sf.jsqlparser.util.validation.feature;

import net.sf.jsqlparser.parser.feature.Feature;

public interface Version extends FeatureSetValidation {

    /**
     * @return the version string
     */
    public String getVersionString();

    /**
     * @return <code>featureName + " not supported."</code>
     */
    @Override
    default String getMessage(Feature feature) {
        return feature.name() + " not supported.";
    }

}
package net.sf.jsqlparser.util.validation.feature;

import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.util.validation.ValidationException;

public interface Version extends FeatureSetValidation {

    /**
     * @return the version string
     */
    public String getVersionString();

    /**
     * @return <code>featureName + " not supported."</code>
     */
    @Override
    default ValidationException getMessage(Feature feature) {
        return toError(feature.name() + " not supported.");
    }

}
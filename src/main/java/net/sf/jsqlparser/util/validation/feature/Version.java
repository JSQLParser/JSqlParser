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

import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.util.validation.ValidationException;

public interface Version extends FeatureSetValidation {

    /**
     * @return the version string
     */
    String getVersionString();

    /**
     * @return <code>featureName + " not supported."</code>
     */
    @Override
    default ValidationException getMessage(Feature feature) {
        return toError(feature.name() + " not supported.");
    }


}

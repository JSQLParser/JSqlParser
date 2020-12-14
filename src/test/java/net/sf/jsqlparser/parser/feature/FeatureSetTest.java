/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser.feature;

import java.util.EnumSet;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;

public class FeatureSetTest {

    @Test
    public void testGetNotContained() {
        assertEquals(EnumSet.of(Feature.select), new FeaturesAllowed(Feature.select, Feature.update) //
                .getNotContained(new FeaturesAllowed(Feature.update, Feature.delete).getFeatures()));
    }

    @Test
    public void testRetainAll() {
        assertEquals(EnumSet.of(Feature.update), new FeaturesAllowed(Feature.select, Feature.update) //
                .retainAll(new FeaturesAllowed(Feature.update, Feature.delete).getFeatures()));
    }

}

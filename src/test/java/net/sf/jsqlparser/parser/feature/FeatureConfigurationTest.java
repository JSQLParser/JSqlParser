/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2023 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser.feature;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FeatureConfigurationTest {
    @Test
    public void getAsLong() {
        FeatureConfiguration featureConfiguration = new FeatureConfiguration();
        featureConfiguration.setValue(Feature.timeOut, 123L);

        Long timeOut = featureConfiguration.getAsLong(Feature.timeOut);

        assertThat(timeOut).isEqualTo(123L);
    }
}

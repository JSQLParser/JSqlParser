package net.sf.jsqlparser.parser.feature;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class FeatureConfigurationTest {
    @Test
    public void getAsLong() {
        FeatureConfiguration featureConfiguration = new FeatureConfiguration();
        featureConfiguration.setValue(Feature.timeOut, 123L);

        Long timeOut = featureConfiguration.getAsLong(Feature.timeOut);

        assertThat(timeOut).isEqualTo(123L);
    }
}

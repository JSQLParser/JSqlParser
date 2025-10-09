package net.sf.jsqlparser.schema;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MultiPartNameTest {

    @Test
    void replaceBackticksWithDoubleQuotes() {
        Assertions.assertThat("\"starbake\".\"customers\"").isEqualToIgnoringCase(
                MultiPartName.replaceBackticksWithDoubleQuotes("`starbake`.`customers`"));
    }
}

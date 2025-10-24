/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
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

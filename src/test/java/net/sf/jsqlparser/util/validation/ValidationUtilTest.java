/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation;

import java.util.Arrays;
import net.sf.jsqlparser.schema.Column;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class ValidationUtilTest extends ValidationTestAsserts {

    @Test
    public void testMap() {
        assertEquals(Arrays.asList("col2", "col1"),
                ValidationUtil.map(Arrays.asList(new Column("col2"), new Column("col1")),
                        Column::getColumnName));
    }

}

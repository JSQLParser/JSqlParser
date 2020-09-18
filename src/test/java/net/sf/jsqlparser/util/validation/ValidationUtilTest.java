package net.sf.jsqlparser.util.validation;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class ValidationUtilTest extends ValidationTestAsserts {

    @Test
    public void testConcat() {
        assertEquals(Arrays.asList("a.b", "a.c", "a.c"), ValidationUtil.concat("a", Arrays.asList("b", "c", "c")));
        assertEquals(null, ValidationUtil.concat("a", (Collection<String>) null));
        assertEquals(Arrays.asList("a.b", "a.c", "a.d"),
                ValidationUtil.concat("a", Arrays.asList("b", "c", "d").stream()));
        assertEquals("a.col", ValidationUtil.concat("a", "col"));
        assertEquals(null, ValidationUtil.concat("a", (String) null));
    }

}

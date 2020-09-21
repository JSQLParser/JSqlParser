/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation.validator;

import java.util.Arrays;
import org.junit.Test;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;

public class GrantValidatorTest extends ValidationTestAsserts {

    @Test
    public void testValidateGrant() throws JSQLParserException {
        for (String sql : Arrays.asList("GRANT SELECT ON t1 TO u", "GRANT SELECT, INSERT ON t1 TO u, u2",
                "GRANT role1 TO u, u2", "GRANT SELECT, INSERT, UPDATE, DELETE ON T1 TO ADMIN_ROLE",
                "GRANT ROLE_1 TO TEST_ROLE_1, TEST_ROLE_2")) {
            validateNoErrors(sql, 1, DatabaseType.DATABASES);
        }
    }
    
    @Test
    public void testValidateGrantNotAllowed() throws JSQLParserException {
        for (String sql : Arrays.asList("GRANT SELECT ON t1 TO u", "GRANT SELECT, INSERT ON t1 TO u, u2",
                "GRANT role1 TO u, u2", "GRANT SELECT, INSERT, UPDATE, DELETE ON T1 TO ADMIN_ROLE",
                "GRANT ROLE_1 TO TEST_ROLE_1, TEST_ROLE_2")) {
            validateNotAllowed(sql, 1, 1, FeaturesAllowed.DML, Feature.grant);
        }
    }

}

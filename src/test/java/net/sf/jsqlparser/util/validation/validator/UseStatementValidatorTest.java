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

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import org.junit.jupiter.api.Test;

public class UseStatementValidatorTest extends ValidationTestAsserts {

    @Test
    public void testValidateUse() throws JSQLParserException {
        validateNoErrors("USE my_schema", 1, DatabaseType.SQLSERVER, DatabaseType.MARIADB, DatabaseType.MYSQL);
    }
}

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

import org.junit.Test;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;

public class DeclareStatementValidatorTest extends ValidationTestAsserts {

    @Test
    public void testValidateDeclare() throws JSQLParserException {
        validateNoErrors("DECLARE @find nvarchar (30)", 1, DatabaseType.SQLSERVER);
    }

}

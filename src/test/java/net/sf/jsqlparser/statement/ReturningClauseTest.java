/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2023 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

class ReturningClauseTest {
    @Test
    void returnIntoTest() throws JSQLParserException {
        String sqlStr = "  insert into emp\n"
                + "  (empno, ename)\n"
                + "  values\n"
                + "  (seq_emp.nextval, 'morgan')\n"
                + "  returning empno\n"
                + "  into x";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

}

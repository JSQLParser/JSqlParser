/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.JSQLParserException;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import org.junit.Test;

/**
 *
 * @author tw
 */
public class DeclareStatementTest {
    
    public DeclareStatementTest() {
    }

    @Test
    public void testDeclareType() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("DECLARE @find nvarchar (30)");
    }
    
    @Test
    public void testDeclareTypeWithDefault() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("DECLARE @find varchar (30) = 'Man%'");
    }

    @Test
    public void testDeclareTypeList() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("DECLARE @group nvarchar (50), @sales money");
    }
    
    @Test
    public void testDeclareTypeList2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("DECLARE @group nvarchar (50), @sales varchar (50)");
    }
    
    @Test
    public void testDeclareTable() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("DECLARE @MyTableVar TABLE (EmpID int NOT NULL, OldVacationHours int, NewVacationHours int, ModifiedDate datetime)");
    }
    
    @Test
    public void testDeclareAs() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("DECLARE @LocationTVP AS LocationTableType");
    }
}

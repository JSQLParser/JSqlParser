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

import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Test;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.UserVariable;
import net.sf.jsqlparser.statement.DeclareStatement.TypeDefExpr;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;

/**
 *
 * @author tw
 */
public class DeclareStatementTest {

    public DeclareStatementTest() {
    }

    @Test
    public void testDeclareType() throws JSQLParserException {
        String statement = "DECLARE @find nvarchar (30)";
        assertSqlCanBeParsedAndDeparsed(statement);
        assertDeparse(new DeclareStatement()
                .addTypeDefExprList(
                        new TypeDefExpr(new UserVariable().withName("find"),
                                new ColDataType().withDataType("nvarchar").addArgumentsStringList("30"), null))
                .withDeclareType(DeclareType.TYPE), statement);
    }

    @Test
    public void testDeclareTypeWithDefault() throws JSQLParserException {
        String statement = "DECLARE @find varchar (30) = 'Man%'";
        assertSqlCanBeParsedAndDeparsed(statement);
        assertDeparse(new DeclareStatement().addTypeDefExprList(new TypeDefExpr(new UserVariable().withName("find"),
                new ColDataType().withDataType("varchar").addArgumentsStringList("30"),
                new StringValue().withValue("Man%")))
                .withDeclareType(DeclareType.TYPE), statement);
    }

    @Test
    public void testDeclareTypeList() throws JSQLParserException {
        String statement = "DECLARE @group nvarchar (50), @sales money";
        assertSqlCanBeParsedAndDeparsed(statement);
        assertDeparse(
                new DeclareStatement().addTypeDefExprList(Arrays.asList( //
                        new TypeDefExpr(
                                new UserVariable().withName("group"),
                                new ColDataType().withDataType("nvarchar").addArgumentsStringList("50"),
                                null),
                        new TypeDefExpr(new UserVariable().withName("sales"),
                                new ColDataType().withDataType("money"), null)))
                .withDeclareType(DeclareType.TYPE),
                statement);
    }

    @Test
    public void testDeclareTypeList2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("DECLARE @group nvarchar (50), @sales varchar (50)");
    }

    @Test
    public void testDeclareTable() throws JSQLParserException {
        String statement = "DECLARE @MyTableVar TABLE (EmpID int NOT NULL, OldVacationHours int, NewVacationHours int, ModifiedDate datetime)";
        assertSqlCanBeParsedAndDeparsed(statement);
        assertDeparse(new DeclareStatement().withUserVariable(new UserVariable("MyTableVar"))
                .withColumnDefinitions(new ArrayList<>())
                .addColumnDefinitions(
                        new ColumnDefinition("EmpID", new ColDataType().withDataType("int"), Arrays.asList("NOT NULL")),
                        new ColumnDefinition("OldVacationHours", new ColDataType("int")))
                .addColumnDefinitions(
                        Arrays.asList(
                                new ColumnDefinition("NewVacationHours", new ColDataType("int")),
                                new ColumnDefinition("ModifiedDate", new ColDataType("datetime"))))
                .withDeclareType(DeclareType.TABLE), statement);
    }

    @Test
    public void testDeclareAs() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("DECLARE @LocationTVP AS LocationTableType");
    }
}

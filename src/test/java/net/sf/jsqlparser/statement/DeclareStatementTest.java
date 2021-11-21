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

import java.util.ArrayList;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.UserVariable;
import net.sf.jsqlparser.statement.DeclareStatement.TypeDefExpr;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import static net.sf.jsqlparser.test.TestUtils.asList;
import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertEqualsObjectTree;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import org.junit.jupiter.api.Test;

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
        Statement parsed = assertSqlCanBeParsedAndDeparsed(statement);
        DeclareStatement created = new DeclareStatement()
                .addTypeDefExprList(
                        new TypeDefExpr(new UserVariable().withName("find"),
                                new ColDataType().withDataType("nvarchar").addArgumentsStringList("30"), null))
                .withDeclareType(DeclareType.TYPE);
        assertDeparse(created, statement);
        assertEqualsObjectTree(parsed, created);
    }

    @Test
    public void testDeclareTypeWithDefault() throws JSQLParserException {
        String statement = "DECLARE @find varchar (30) = 'Man%'";
        Statement parsed = assertSqlCanBeParsedAndDeparsed(statement);
        DeclareStatement created = new DeclareStatement()
                .addTypeDefExprList(new TypeDefExpr(new UserVariable().withName("find"),
                        new ColDataType().withDataType("varchar").addArgumentsStringList("30"),
                        new StringValue().withValue("Man%")))
                .withDeclareType(DeclareType.TYPE);
        assertDeparse(created, statement);
        assertEqualsObjectTree(parsed, created);
    }

    @Test
    public void testDeclareTypeList() throws JSQLParserException {
        String statement = "DECLARE @group nvarchar (50), @sales money";
        Statement parsed = assertSqlCanBeParsedAndDeparsed(statement);
        DeclareStatement created = new DeclareStatement().addTypeDefExprList(asList( //
                new TypeDefExpr(
                        new UserVariable().withName("group"),
                        new ColDataType().withDataType("nvarchar").addArgumentsStringList("50"),
                        null),
                new TypeDefExpr(new UserVariable().withName("sales"),
                        new ColDataType().withDataType("money"), null)))
                .withDeclareType(DeclareType.TYPE);
        assertDeparse(created, statement);
        assertEqualsObjectTree(parsed, created);
    }

    @Test
    public void testDeclareTypeList2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("DECLARE @group nvarchar (50), @sales varchar (50)");
    }

    @Test
    public void testDeclareTable() throws JSQLParserException {
        String statement = "DECLARE @MyTableVar TABLE (EmpID int NOT NULL, OldVacationHours int, NewVacationHours int, ModifiedDate datetime)";
        Statement parsed = assertSqlCanBeParsedAndDeparsed(statement);
        DeclareStatement created = new DeclareStatement().withUserVariable(new UserVariable("MyTableVar"))
                .withColumnDefinitions(new ArrayList<>())
                .addColumnDefinitions(
                        new ColumnDefinition("EmpID", new ColDataType().withDataType("int"),
                                asList("NOT", "NULL")),
                        new ColumnDefinition("OldVacationHours", new ColDataType("int")))
                .addColumnDefinitions(
                        asList(
                                new ColumnDefinition("NewVacationHours", new ColDataType("int")),
                                new ColumnDefinition("ModifiedDate", new ColDataType("datetime"))))
                .withDeclareType(DeclareType.TABLE);
        assertDeparse(created, statement);
        assertEqualsObjectTree(parsed, created);
    }

    @Test
    public void testDeclareAs() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("DECLARE @LocationTVP AS LocationTableType");
    }
}

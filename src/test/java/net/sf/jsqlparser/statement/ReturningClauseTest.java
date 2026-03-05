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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.update.Update;
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

    @Test
    void returningOldNewDefaultReferencesTest() throws JSQLParserException {
        String sqlStr = "UPDATE products SET price = price * 1.10 "
                + "RETURNING old.price AS old_price, new.price AS new_price, new.*";
        Update update = (Update) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        ReturningClause returningClause = update.getReturningClause();
        assertNull(returningClause.getOutputAliases());

        Column oldPrice = returningClause.get(0).getExpression(Column.class);
        assertNull(oldPrice.getTable());
        assertEquals(ReturningReferenceType.OLD, oldPrice.getReturningReferenceType());
        assertEquals("old", oldPrice.getReturningQualifier());

        Column newPrice = returningClause.get(1).getExpression(Column.class);
        assertNull(newPrice.getTable());
        assertEquals(ReturningReferenceType.NEW, newPrice.getReturningReferenceType());
        assertEquals("new", newPrice.getReturningQualifier());

        AllTableColumns allNew = returningClause.get(2).getExpression(AllTableColumns.class);
        assertNull(allNew.getTable());
        assertEquals(ReturningReferenceType.NEW, allNew.getReturningReferenceType());
        assertEquals("new", allNew.getReturningQualifier());
    }

    @Test
    void returningWithOutputAliasesTest() throws JSQLParserException {
        String sqlStr = "INSERT INTO products (price) VALUES (99.99) "
                + "RETURNING WITH (OLD AS o, NEW AS n) o.price AS old_price, n.price AS new_price, n.*";
        Insert insert = (Insert) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        ReturningClause returningClause = insert.getReturningClause();
        assertEquals(2, returningClause.getOutputAliases().size());
        assertEquals(ReturningReferenceType.OLD,
                returningClause.getOutputAliases().get(0).getReferenceType());
        assertEquals("o", returningClause.getOutputAliases().get(0).getAlias());
        assertEquals(ReturningReferenceType.NEW,
                returningClause.getOutputAliases().get(1).getReferenceType());
        assertEquals("n", returningClause.getOutputAliases().get(1).getAlias());

        Column oldPrice = returningClause.get(0).getExpression(Column.class);
        assertNull(oldPrice.getTable());
        assertEquals(ReturningReferenceType.OLD, oldPrice.getReturningReferenceType());
        assertEquals("o", oldPrice.getReturningQualifier());

        Column newPrice = returningClause.get(1).getExpression(Column.class);
        assertNull(newPrice.getTable());
        assertEquals(ReturningReferenceType.NEW, newPrice.getReturningReferenceType());
        assertEquals("n", newPrice.getReturningQualifier());

        AllTableColumns allNew = returningClause.get(2).getExpression(AllTableColumns.class);
        assertNull(allNew.getTable());
        assertEquals(ReturningReferenceType.NEW, allNew.getReturningReferenceType());
        assertEquals("n", allNew.getReturningQualifier());
    }

}

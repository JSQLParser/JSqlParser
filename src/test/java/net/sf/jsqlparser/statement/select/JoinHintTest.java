/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2023 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class JoinHintTest {
    public static Stream<String> sqlStrings() {
        return Stream.of(
                "SELECT p.Name, pr.ProductReviewID  \n"
                        + "FROM Production.Product AS p  \n"
                        + "LEFT OUTER HASH JOIN Production.ProductReview AS pr  \n"
                        + "ON p.ProductID = pr.ProductID  \n"
                        + "ORDER BY ProductReviewID DESC",

                "DELETE spqh   \n"
                        + "FROM Sales.SalesPersonQuotaHistory AS spqh  \n"
                        + "    INNER LOOP JOIN Sales.SalesPerson AS sp  \n"
                        + "    ON spqh.SalesPersonID = sp.SalesPersonID  \n"
                        + "WHERE sp.SalesYTD > 2500000.00",

                "SELECT poh.PurchaseOrderID, poh.OrderDate, pod.ProductID, pod.DueDate, poh.VendorID   \n"
                        + "FROM Purchasing.PurchaseOrderHeader AS poh  \n"
                        + "INNER MERGE JOIN Purchasing.PurchaseOrderDetail AS pod   \n"
                        + "    ON poh.PurchaseOrderID = pod.PurchaseOrderID");
    }

    @ParameterizedTest
    @MethodSource("sqlStrings")
    void testJoinHint(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}

package net.sf.jsqlparser.statement.piped;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FromQueryTest {
    @Test
    void testParseAndDeparse() throws JSQLParserException {
        // formatter:off
        String sqlStr = "FROM Produce\n"
                + "|> WHERE\n"
                + "    item != 'bananas'\n"
                + "    AND category IN ('fruit', 'nut')\n"
                + "|> AGGREGATE COUNT(*) AS num_items, SUM(sales) AS total_sales\n"
                + "   GROUP BY item\n"
                + "|> ORDER BY item DESC;";
        // formatter:on
        FromQuery fromQuery = (FromQuery) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertInstanceOf(WherePipeOperator.class, fromQuery.get(0));
        Assertions.assertInstanceOf(AggregatePipeOperator.class, fromQuery.get(1));
        Assertions.assertInstanceOf(OrderByPipeOperator.class, fromQuery.get(2));
    }

    @Test
    void testParseAndDeparseJoin() throws JSQLParserException {
        // formatter:off
        String sqlStr =
                "FROM Produce INNER JOIN Price USING(id_product) \n"
                        + "|> WHERE\n"
                        + "    item != 'bananas'\n"
                        + "    AND category IN ('fruit', 'nut')\n"
                        + "|> AGGREGATE COUNT(*) AS num_items, SUM(sales) AS total_sales\n"
                        + "   GROUP BY item\n"
                        + "|> ORDER BY item DESC;";
        // formatter:on

        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testParseAndDeparseWithIssue73() throws JSQLParserException {
        // formatter:off
        String sqlStr =
                "with client_info as (\n" +
                        "  with client as (\n" +
                        "    select 1 as client_id\n" +
                        "    |> UNION ALL\n" +
                        "      (select 2),\n" +
                        "      (select 3)\n" +
                        "  ), basket as (\n" +
                        "    select 1 as basket_id, 1 as client_id\n" +
                        "    |> UNION ALL\n" +
                        "      (select 2, 2)\n" +
                        "  ), basket_item as (\n" +
                        "    select 1 as item_id, 1 as basket_id\n" +
                        "    |> UNION ALL\n" +
                        "      (select 2, 1),\n" +
                        "      (select 3, 1),\n" +
                        "      (select 4, 2)\n" +
                        "  ), item as (\n" +
                        "    select 1 as item_id, 'milk' as name\n" +
                        "    |> UNION ALL\n" +
                        "      (select 2, \"chocolate\"),\n" +
                        "      (select 3, \"donut\"),\n" +
                        "      (select 4, \"croissant\")\n" +
                        "  ), wrapper as (\n" +
                        "    FROM client c\n" +
                        "    |> aggregate count(i.item_id) as bought_item\n" +
                        "       group by c.client_id, i.item_id, i.name\n" +
                        "    |> aggregate array_agg((select as struct item_id, name, bought_item)) as items_info\n"
                        +
                        "       group by client_id\n" +
                        "  )\n" +
                        "  select * from wrapper\n" +
                        ")\n" +
                        "select * from client_info";
        // formatter:on
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testParseAndDeparseWithJoinIssue72() throws JSQLParserException {
        // formatter:off
        String sqlStr =
                "with client as (\n" +
                        "  select 1 as client_id\n" +
                        "  |> UNION ALL\n" +
                        "    (select 2),\n" +
                        "    (select 3)\n" +
                        "), basket as (\n" +
                        "  select 1 as basket_id, 1 as client_id\n" +
                        "  |> UNION ALL\n" +
                        "    (select 2, 2)\n" +
                        "), basket_item as (\n" +
                        "  select 1 as item_id, 1 as basket_id\n" +
                        "  |> UNION ALL\n" +
                        "    (select 2, 1),\n" +
                        "    (select 3, 1),\n" +
                        "    (select 4, 2)\n" +
                        "), item as (\n" +
                        "  select 1 as item_id, 'milk' as name\n" +
                        "  |> UNION ALL\n" +
                        "    (select 2, \"chocolate\"),\n" +
                        "    (select 3, \"donut\"),\n" +
                        "    (select 4, \"croissant\")\n" +
                        ")\n" +
                        "FROM client c\n" +
                        "  left join basket b using(client_id)\n" +
                        "  left join basket_item bi using(basket_id)\n" +
                        "  left join item i on i.item_id = bi.item_id\n" +
                        "|> aggregate count(i.item_id) as bought_item\n" +
                        "   group by c.client_id, i.item_id, i.name\n" +
                        "|> aggregate array_agg((select as struct item_id, name, bought_item)) as items_info\n" +
                        "   group by client_id";
        // formatter:on
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}

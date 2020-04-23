package issues;

import org.junit.Test;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.select.Select;

public class Issue482Test {

    @Test
    public void test() throws Exception {
        String sql = "SELECT SUM(orderTotalValue) as value, MONTH(invoiceDate) as month, YEAR(invoiceDate) as year FROM invoice.Invoices WHERE projectID = 1 GROUP BY MONTH(invoiceDate), YEAR(invoiceDate) ORDER BY YEAR(invoiceDate) DESC, MONTH(invoiceDate) DESC";
        Select select = (Select) CCJSqlParserUtil.parse(sql);
        System.out.println(select.toString());

        Statements stmts = CCJSqlParserUtil.parseStatements(sql);
        System.out.println(stmts.toString());
    }

}

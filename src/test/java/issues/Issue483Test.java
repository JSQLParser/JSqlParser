package issues;

import org.junit.Test;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.Select;

public class Issue483Test {

    @Test
    public void test() throws Exception{
        String sql = "select * from ((TABLE_A), (TABLE_b as b))";
        Select select = (Select) CCJSqlParserUtil.parse(sql);
        System.out.println(select.toString());
    }

}

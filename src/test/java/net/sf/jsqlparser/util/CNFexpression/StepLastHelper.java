package net.sf.jsqlparser.util.CNFexpression;

import java.io.StringReader;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

public class StepLastHelper {
    
    /**
     * This is the case for building the final CNF form.
     */
    public static Expression buildFinalStep1() {
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        String statement = "SELECT * FROM S WHERE "
                + "(NOT 1.2 < 2.3 OR NOT 1.1 <> 2.5) AND (NOT 1.2 < 2.3 OR NOT 8.0 >= 7.2) AND"
                + " (NOT 3.5 = 4.6 OR NOT 1.1 <> 2.5) AND (NOT 3.5 = 4.6 OR NOT 8.0 >= 7.2)";
        try {
            Select select = (Select) parserManager.parse(new StringReader(statement));
            return ((PlainSelect) select.getSelectBody()).getWhere();
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * This is the case for building the final CNF form.
     */
    public static Expression buildFinalStep2() {
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        String statement = "SELECT * FROM S WHERE "
                + "(1.1 >= 2.3 OR S.A LIKE '\"%%%\"') AND (1.1 >= 2.3 OR S.B = '\"orz\"')"
                + " AND (NOT 3.3 < 4.5 OR S.A LIKE '\"%%%\"') AND (NOT 3.3 < 4.5 OR S.B = '\"orz\"')";
        try {
            Select select = (Select) parserManager.parse(new StringReader(statement));
            return ((PlainSelect) select.getSelectBody()).getWhere();
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * This is the case for building the final CNF form.
     */
    public static Expression buildFinalStep3() {
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        String statement = "SELECT * FROM S WHERE "
                + "(3.0 >= 4.0 OR 7.0 < 8.0 OR NOT 13.0 <> 14.0) AND "
                + "(3.0 >= 4.0 OR 7.0 < 8.0 OR NOT 15.0 = 16.0 OR NOT 17.0 = 18.0) AND "
                + "(3.0 >= 4.0 OR 7.0 < 8.0 OR NOT 15.0 = 16.0 OR NOT 19.0 > 20.0) AND "
                + "(3.0 >= 4.0 OR 9.0 > 10.0 OR NOT 13.0 <> 14.0) AND "
                + "(3.0 >= 4.0 OR 9.0 > 10.0 OR NOT 15.0 = 16.0 OR NOT 17.0 = 18.0) AND "
                + "(3.0 >= 4.0 OR 9.0 > 10.0 OR NOT 15.0 = 16.0 OR NOT 19.0 > 20.0) AND "
                + "(3.0 >= 4.0 OR 11.0 = 12.0 OR NOT 13.0 <> 14.0) AND "
                + "(3.0 >= 4.0 OR 11.0 = 12.0 OR NOT 15.0 = 16.0 OR NOT 17.0 = 18.0) AND "
                + "(3.0 >= 4.0 OR 11.0 = 12.0 OR NOT 15.0 = 16.0 OR NOT 19.0 > 20.0) AND "
                + "(5.0 <= 6.0 OR 7.0 < 8.0 OR NOT 13.0 <> 14.0) AND "
                + "(5.0 <= 6.0 OR 7.0 < 8.0 OR NOT 15.0 = 16.0 OR NOT 17.0 = 18.0) AND "
                + "(5.0 <= 6.0 OR 7.0 < 8.0 OR NOT 15.0 = 16.0 OR NOT 19.0 > 20.0) AND "
                + "(5.0 <= 6.0 OR 9.0 > 10.0 OR NOT 13.0 <> 14.0) AND "
                + "(5.0 <= 6.0 OR 9.0 > 10.0 OR NOT 15.0 = 16.0 OR NOT 17.0 = 18.0) AND "
                + "(5.0 <= 6.0 OR 9.0 > 10.0 OR NOT 15.0 = 16.0 OR NOT 19.0 > 20.0) AND "
                + "(5.0 <= 6.0 OR 11.0 = 12.0 OR NOT 13.0 <> 14.0) AND "
                + "(5.0 <= 6.0 OR 11.0 = 12.0 OR NOT 15.0 = 16.0 OR NOT 17.0 = 18.0) AND "
                + "(5.0 <= 6.0 OR 11.0 = 12.0 OR NOT 15.0 = 16.0 OR NOT 19.0 > 20.0)";
        try {
            Select select = (Select) parserManager.parse(new StringReader(statement));
            return ((PlainSelect) select.getSelectBody()).getWhere();
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * This is the case for building the final CNF form.
     */
    public static Expression buildFinalStep4() {
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        String statement = "SELECT * FROM S WHERE "
                + "NOT S.D > {d '2017-03-25'}";
        try {
            Select select = (Select) parserManager.parse(new StringReader(statement));
            return ((PlainSelect) select.getSelectBody()).getWhere();
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * This is the case for building the final CNF form.
     */
    public static Expression buildFinalStep5() {
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        String statement = "SELECT * FROM S WHERE "
                + "S.A > 3.5 AND S.B < 4 AND S.C NOT LIKE '\"%%\"' "
                + "AND NOT S.D = {t '12:04:34'}";
        try {
            Select select = (Select) parserManager.parse(new StringReader(statement));
            return ((PlainSelect) select.getSelectBody()).getWhere();
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
        return null;
    }
    
}

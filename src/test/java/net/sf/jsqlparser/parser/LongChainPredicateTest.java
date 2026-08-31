package net.sf.jsqlparser.parser;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.jupiter.api.Test;

/**
 * Guards the threshold-gated chain-walk memoization in isFunctionAhead() /
 * isAllTableColumnsAhead(): chains on both sides of CHAIN_CACHE_THRESHOLD and inside the memoized
 * path must keep the exact same AST shapes as the plain walk. The speed-up itself is
 * constant-factor only and is carried by the measured numbers, not by a CI timing assertion.
 */
public class LongChainPredicateTest {

    private static String chain(int innerDelimiters, String last) {
        // innerDelimiters = dots between s0..sN (chain length before the last part)
        StringBuilder sb = new StringBuilder("s0");
        for (int i = 1; i <= innerDelimiters; i++) {
            sb.append(".s").append(i);
        }
        return sb.append(".").append(last).toString();
    }

    private Object firstExpression(String sql) throws Exception {
        Select select = (Select) CCJSqlParserUtil.parse(sql);
        return ((PlainSelect) select).getSelectItems().get(0).getExpression();
    }

    @Test
    void functionOnShortChainFastPath() throws Exception {
        // total 8 delimiters: the walk stays inside the plain non-cached path
        String sql = "SELECT " + chain(7, "f(1)") + " FROM t";
        assertSqlCanBeParsedAndDeparsed(sql);
        assertTrue(firstExpression(sql) instanceof Function,
                "8-delimiter chain ending in ( must stay a Function");
    }

    @Test
    void functionOnLongChainMemoPath() throws Exception {
        // total 9 delimiters: the walk aborts past the threshold and goes through the cache
        String sql = "SELECT " + chain(8, "f(1)") + " FROM t";
        assertSqlCanBeParsedAndDeparsed(sql);
        assertTrue(firstExpression(sql) instanceof Function,
                "9-delimiter chain ending in ( must stay a Function on the memoized path");
    }

    @Test
    void columnOnShortChainFastPath() throws Exception {
        String sql = "SELECT " + chain(7, "col") + " FROM t";
        assertSqlCanBeParsedAndDeparsed(sql);
        assertTrue(firstExpression(sql) instanceof Column);
        assertFalse(firstExpression(sql) instanceof Function);
    }

    @Test
    void columnOnLongChainMemoPath() throws Exception {
        String sql = "SELECT " + chain(8, "col") + " FROM t";
        assertSqlCanBeParsedAndDeparsed(sql);
        assertTrue(firstExpression(sql) instanceof Column);
        assertFalse(firstExpression(sql) instanceof Function);
    }

    @Test
    void columnOnVeryLongChainMemoPath() throws Exception {
        String sql = "SELECT " + chain(40, "col") + " FROM t";
        assertSqlCanBeParsedAndDeparsed(sql);
        assertTrue(firstExpression(sql) instanceof Column);
    }

    @Test
    void allTableColumnsAcrossThreshold() throws Exception {
        String shortChain = "SELECT " + chain(7, "*") + " FROM t";
        assertSqlCanBeParsedAndDeparsed(shortChain);
        assertTrue(firstExpression(shortChain) instanceof AllTableColumns);

        String longChain = "SELECT " + chain(8, "*") + " FROM t";
        assertSqlCanBeParsedAndDeparsed(longChain);
        assertTrue(firstExpression(longChain) instanceof AllTableColumns);
    }

    @Test
    void oracleOuterJoinColumnPlusOnMemoPath() throws Exception {
        // the column(+) exclusion must survive the memoized walk
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM a, b WHERE " + chain(8, "x(+)") + " = b.x");
    }
}

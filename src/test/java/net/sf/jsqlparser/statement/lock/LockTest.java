package net.sf.jsqlparser.statement.lock;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.CONCURRENT)
public class LockTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "LOCK TABLE a IN EXCLUSIVE MODE",
            "LOCK TABLE a IN ROW EXCLUSIVE MODE",
            "LOCK TABLE a IN ROW SHARE MODE",
            "LOCK TABLE a IN SHARE MODE",
            "LOCK TABLE a IN SHARE UPDATE MODE",
            "LOCK TABLE a IN SHARE ROW EXCLUSIVE MODE",
            "LOCK TABLE a IN EXCLUSIVE MODE NOWAIT",
            "LOCK TABLE a IN SHARE ROW EXCLUSIVE MODE NOWAIT",
            "LOCK TABLE a IN SHARE ROW EXCLUSIVE MODE WAIT 10",
            "LOCK TABLE a IN EXCLUSIVE MODE WAIT 23",
    })
    void testLockStatementsParseDeparse(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr);
    }

    @Test
    void testLockExclusiveMode() throws JSQLParserException {
        String sqlStr = "LOCK TABLE a IN EXCLUSIVE MODE";
        Statement statement = CCJSqlParserUtil.parse(sqlStr);
        assertInstanceOf(LockStatement.class, statement);

        LockStatement ls = (LockStatement) statement;
        assertEquals(LockMode.Exclusive, ls.getLockMode());
        assertFalse(ls.isNoWait());
    }

    @Test
    void testNoWait() throws JSQLParserException {
        String sqlStr = "LOCK TABLE a IN SHARE MODE NOWAIT";
        Statement statement = CCJSqlParserUtil.parse(sqlStr);
        assertInstanceOf(LockStatement.class, statement);

        LockStatement ls = (LockStatement) statement;
        assertEquals(LockMode.Share, ls.getLockMode());
        assertTrue(ls.isNoWait());
    }

    @Test
    void testWaitTimeout() throws JSQLParserException {
        String sqlStr = "LOCK TABLE a IN SHARE MODE WAIT 300";
        Statement statement = CCJSqlParserUtil.parse(sqlStr);
        assertInstanceOf(LockStatement.class, statement);

        LockStatement ls = (LockStatement) statement;
        assertEquals(LockMode.Share, ls.getLockMode());
        assertTrue(ls.isWait());
        assertEquals(300, ls.getWaitTimeout());
    }

    @Test
    void testCreateLockStatement() {
        Table t = new Table("a");

        LockStatement ls = new LockStatement(t, LockMode.Exclusive);
        assertEquals("LOCK TABLE a IN EXCLUSIVE MODE", ls.toString());

        ls.setLockMode(LockMode.Share);
        assertEquals("LOCK TABLE a IN SHARE MODE", ls.toString());

        ls.setNoWait(true);
        assertEquals("LOCK TABLE a IN SHARE MODE NOWAIT", ls.toString());

        ls.setWaitSeconds(60);
        assertEquals("LOCK TABLE a IN SHARE MODE WAIT 60", ls.toString());

        ls.setNoWait(false);
        assertEquals("LOCK TABLE a IN SHARE MODE", ls.toString());

        ls.setTable(new Table("b"));
        assertEquals("LOCK TABLE b IN SHARE MODE", ls.toString());
    }


}

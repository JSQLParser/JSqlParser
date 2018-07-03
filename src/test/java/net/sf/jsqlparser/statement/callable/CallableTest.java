package net.sf.jsqlparser.statement.callable;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;

import junit.framework.TestCase;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;

public class CallableTest extends TestCase {

    CCJSqlParserManager parserManager = new CCJSqlParserManager();

    public CallableTest(String arg0) {
        super(arg0);
    }

    public void testCallable1() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("{call proc(?, '1', ?) }", true);
    }

    public void testCallable2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("{call proc(?, '1', ?)}", true);
    }

    public void testCallable3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("{Call proc(?, '1', ?)}", true);
    }

    public void testCallable4() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("{? = CALL proc(?, '1', ?)}", true);
    }

    public void testCallable4a() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("{? = CALL proc}", true);
    }

    public void testCallable5() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("{call proc() }", true);
    }

    public void testCallable5a() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("{call proc}", true);
    }

    public void testCallableOracle1() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("BEGIN abc(4,?) ;  END;", true);
    }

    public void testCallableOracle2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("begin ? := abc(4,?) ;   END;", true);
    }

    public void testCallableOracle4() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("begin ? := abc(:param1,?) ;   END;", true);
    }

    public void testCallableOracle5() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("begin ? := abc(a => ?,  b => 'fdf') ;   END;", true);
    }

}

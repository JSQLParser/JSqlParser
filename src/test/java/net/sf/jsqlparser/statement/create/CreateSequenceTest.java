package net.sf.jsqlparser.statement.create;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.statement.create.sequence.CreateSequence;
import org.junit.Test;

public class CreateSequenceTest {

    @Test
    public void testCreateSequence() throws JSQLParserException {
        CreateSequence statement = (CreateSequence) CCJSqlParserUtil.parse("CREATE SEQUENCE");
        System.out.println(statement);
    }
}

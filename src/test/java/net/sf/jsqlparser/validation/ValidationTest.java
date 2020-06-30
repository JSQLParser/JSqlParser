package net.sf.jsqlparser.validation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.validation.DatabaseType;
import net.sf.jsqlparser.util.validation.StatementValidator;

public class ValidationTest {

    //    public static class ValidationVisitor implements ModelVisitor
    //    {
    //
    //    }

    @Test
    public void testValidaton() throws JSQLParserException {
        String sql = "SELECT * FROM tab1, tab2 WHERE tab1.id (+) = tab2.ref";
        Statement stmt = CCJSqlParserUtil.parse(sql);

        StatementValidator validator = new StatementValidator();
        stmt.accept(validator);

        Map<DatabaseType, Set<String>> unsupportedErrors = validator.getValidationErrors(DatabaseType.sqlserver);
        assertNotNull(unsupportedErrors);
        assertEquals(1, unsupportedErrors.size());
        assertEquals(new HashSet<>(Arrays.asList("oldOracleJoinSyntax=1 not supported")),
                unsupportedErrors.get(DatabaseType.sqlserver));
    }

}

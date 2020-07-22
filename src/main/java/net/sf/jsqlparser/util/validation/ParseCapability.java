package net.sf.jsqlparser.util.validation;

import java.util.function.Consumer;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;

/**
 * package - private class for {@link ValidationUtil} to parse the statements
 * within it's own {@link ValidationCapability}
 *
 * @author gitmotte
 */
final class ParseCapability implements ValidationCapability {

    public static final String NAME = "parsing";

    private Statement stmt;

    public Statement getStatement() {
        return stmt;
    }

    @Override
    public void validate(ValidationContext context, Consumer<ValidationException> errorConsumer) {
        String statement = context.get(ParseContext.statement, String.class);
        try {
            stmt = CCJSqlParserUtil.parseStatement(
                    CCJSqlParserUtil.newParser(statement).withConfiguration(context.getConfiguration()));
        } catch (JSQLParserException e) {
            errorConsumer.accept(new ParseException("Cannot parse statement: " + e.getMessage(), e));
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

}
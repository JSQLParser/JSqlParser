/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
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

    private String statement;
    private Statement parsedStatement;

    public ParseCapability(String statement) {
        this.statement = statement;
    }

    public String getStatement() {
        return statement;
    }

    /**
     * @return <code>null</code> on parse error, otherwise the {@link Statement}
     *         parsed.
     */
    public Statement getParsedStatement() {
        return parsedStatement;
    }

    @Override
    public void validate(ValidationContext context, Consumer<ValidationException> errorConsumer) {
        try {
            this.parsedStatement = CCJSqlParserUtil.parseStatement(
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

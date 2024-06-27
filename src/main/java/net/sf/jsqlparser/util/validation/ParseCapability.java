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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statements;

/**
 * package - private class for {@link Validation} to parse the statements within it's own
 * {@link ValidationCapability}
 *
 * @author gitmotte
 */
final class ParseCapability implements ValidationCapability {

    public static final String NAME = "parsing";

    private String statements;
    private Statements parsedStatement;

    public ParseCapability(String statements) {
        this.statements = statements;
    }

    public String getStatements() {
        return statements;
    }

    /**
     * @return <code>null</code> on parse error, otherwise the {@link Statements} parsed.
     */
    public Statements getParsedStatements() {
        return parsedStatement;
    }

    @Override
    public void validate(ValidationContext context, Consumer<ValidationException> errorConsumer) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            this.parsedStatement = CCJSqlParserUtil.parseStatements(
                    CCJSqlParserUtil.newParser(statements)
                            .withConfiguration(context.getConfiguration()),
                    executorService);
        } catch (JSQLParserException e) {
            errorConsumer
                    .accept(new ParseException("Cannot parse statement: " + e.getMessage(), e));
        } finally {
            executorService.shutdown();
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

}

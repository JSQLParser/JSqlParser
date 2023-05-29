/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;

/**
 * Toolfunctions to start and use JSqlParser.
 *
 * @author toben
 */

@SuppressWarnings("PMD.CyclomaticComplexity")
public final class CCJSqlParserUtil {
    public final static Logger LOGGER = Logger.getLogger(CCJSqlParserUtil.class.getName());
    static {
        LOGGER.setLevel(Level.OFF);
    }

    public final static int ALLOWED_NESTING_DEPTH = 10;

    private CCJSqlParserUtil() {}

    public static Statement parse(Reader statementReader) throws JSQLParserException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Statement statement = null;
        CCJSqlParser parser = new CCJSqlParser(new StreamProvider(statementReader));
        try {
            statement = parseStatement(parser, executorService);
        } finally {
            executorService.shutdown();
        }
        return statement;
    }

    public static Statement parse(String sql) throws JSQLParserException {
        return parse(sql, null);
    }

    /**
     * Parses an sql statement while allowing via consumer to configure the used parser before.
     *
     * For instance to activate SQLServer bracket quotation on could use:
     *
     * {@code
     * CCJSqlParserUtil.parse("select * from [mytable]", parser -> parser.withSquareBracketQuotation(true));
     * }
     *
     * @param sql
     * @param consumer
     * @return
     * @throws JSQLParserException
     */
    public static Statement parse(String sql, Consumer<CCJSqlParser> consumer)
            throws JSQLParserException {

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Statement statement = null;
        try {
            statement = parse(sql, executorService, consumer);
        } finally {
            executorService.shutdown();
        }
        return statement;
    }

    public static Statement parse(String sql, ExecutorService executorService,
            Consumer<CCJSqlParser> consumer)
            throws JSQLParserException {
        Statement statement = null;

        // first, try to parse fast and simple
        CCJSqlParser parser = newParser(sql);
        if (consumer != null) {
            consumer.accept(parser);
        }
        boolean allowComplex = parser.getConfiguration().getAsBoolean(Feature.allowComplexParsing);
        LOGGER.info("Allowed Complex Parsing: " + allowComplex);
        try {
            LOGGER.info("Trying SIMPLE parsing " + (allowComplex ? "first" : "only"));
            statement = parseStatement(parser.withAllowComplexParsing(false), executorService);
        } catch (JSQLParserException ex) {
            LOGGER.info("Nesting Depth" + getNestingDepth(sql));
            if (allowComplex && getNestingDepth(sql) <= ALLOWED_NESTING_DEPTH) {
                LOGGER.info("Trying COMPLEX parsing when SIMPLE parsing failed");
                // beware: the parser must not be reused, but needs to be re-initiated
                parser = newParser(sql);
                if (consumer != null) {
                    consumer.accept(parser);
                }
                statement = parseStatement(parser.withAllowComplexParsing(true), executorService);
            } else {
                throw ex;
            }
        }
        return statement;
    }

    public static CCJSqlParser newParser(String sql) {
        return new CCJSqlParser(new StringProvider(sql));
    }

    public static CCJSqlParser newParser(InputStream is) throws IOException {
        return new CCJSqlParser(new StreamProvider(is));
    }

    public static CCJSqlParser newParser(InputStream is, String encoding) throws IOException {
        return new CCJSqlParser(new StreamProvider(is, encoding));
    }

    public static Node parseAST(String sql) throws JSQLParserException {
        CCJSqlParser parser = newParser(sql);
        try {
            parser.Statement();
            return parser.jjtree.rootNode();
        } catch (Exception ex) {
            throw new JSQLParserException(ex);
        }
    }

    public static Statement parse(InputStream is) throws JSQLParserException {
        try {
            CCJSqlParser parser = newParser(is);
            return parser.Statement();
        } catch (Exception ex) {
            throw new JSQLParserException(ex);
        }
    }

    public static Statement parse(InputStream is, String encoding) throws JSQLParserException {
        try {
            CCJSqlParser parser = newParser(is, encoding);
            return parser.Statement();
        } catch (Exception ex) {
            throw new JSQLParserException(ex);
        }
    }

    public static Expression parseExpression(String expression) throws JSQLParserException {
        return parseExpression(expression, true);
    }

    public static Expression parseExpression(String expression, boolean allowPartialParse)
            throws JSQLParserException {
        return parseExpression(expression, allowPartialParse, p -> {
        });
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    public static Expression parseExpression(String expressionStr, boolean allowPartialParse,
            Consumer<CCJSqlParser> consumer) throws JSQLParserException {
        Expression expression = null;

        // first, try to parse fast and simple
        try {
            CCJSqlParser parser = newParser(expressionStr).withAllowComplexParsing(false);
            if (consumer != null) {
                consumer.accept(parser);
            }
            try {
                expression = parser.Expression();
                if (parser.getNextToken().kind != CCJSqlParserTokenManager.EOF) {
                    throw new JSQLParserException(
                            "could only parse partial expression " + expression.toString());
                }
            } catch (ParseException ex) {
                throw new JSQLParserException(ex);
            }
        } catch (JSQLParserException ex1) {
            // when fast simple parsing fails, try complex parsing but only if it has a chance to
            // succeed
            if (getNestingDepth(expressionStr) <= ALLOWED_NESTING_DEPTH) {
                CCJSqlParser parser = newParser(expressionStr).withAllowComplexParsing(true);
                if (consumer != null) {
                    consumer.accept(parser);
                }
                try {
                    expression = parser.Expression();
                    if (!allowPartialParse
                            && parser.getNextToken().kind != CCJSqlParserTokenManager.EOF) {
                        throw new JSQLParserException(
                                "could only parse partial expression " + expression.toString());
                    }
                } catch (JSQLParserException ex) {
                    throw ex;
                } catch (ParseException ex) {
                    throw new JSQLParserException(ex);
                }
            }
        }
        return expression;
    }

    /**
     * Parse an conditional expression. This is the expression after a where clause. Partial parsing
     * is enabled.
     *
     * @param condExpr
     * @return the expression parsed
     * @see #parseCondExpression(String, boolean)
     */
    public static Expression parseCondExpression(String condExpr) throws JSQLParserException {
        return parseCondExpression(condExpr, true);
    }

    /**
     * Parse an conditional expression. This is the expression after a where clause.
     *
     * @param condExpr
     * @param allowPartialParse false: needs the whole string to be processed.
     * @return the expression parsed
     * @see #parseCondExpression(String)
     */
    public static Expression parseCondExpression(String condExpr, boolean allowPartialParse)
            throws JSQLParserException {
        return parseCondExpression(condExpr, allowPartialParse, p -> {
        });
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    public static Expression parseCondExpression(String conditionalExpressionStr,
            boolean allowPartialParse, Consumer<CCJSqlParser> consumer) throws JSQLParserException {
        Expression expression = null;

        // first, try to parse fast and simple
        try {
            CCJSqlParser parser =
                    newParser(conditionalExpressionStr).withAllowComplexParsing(false);
            if (consumer != null) {
                consumer.accept(parser);
            }
            try {
                expression = parser.Expression();
                if (parser.getNextToken().kind != CCJSqlParserTokenManager.EOF) {
                    throw new JSQLParserException(
                            "could only parse partial expression " + expression.toString());
                }
            } catch (ParseException ex) {
                throw new JSQLParserException(ex);
            }
        } catch (JSQLParserException ex1) {
            if (getNestingDepth(conditionalExpressionStr) <= ALLOWED_NESTING_DEPTH) {
                CCJSqlParser parser =
                        newParser(conditionalExpressionStr).withAllowComplexParsing(true);
                if (consumer != null) {
                    consumer.accept(parser);
                }
                try {
                    expression = parser.Expression();
                    if (!allowPartialParse
                            && parser.getNextToken().kind != CCJSqlParserTokenManager.EOF) {
                        throw new JSQLParserException(
                                "could only parse partial expression " + expression.toString());
                    }
                } catch (JSQLParserException ex) {
                    throw ex;
                } catch (ParseException ex) {
                    throw new JSQLParserException(ex);
                }
            }
        }
        return expression;
    }

    /**
     * @param parser the Parser armed with a Statement text
     * @param executorService the Executor Service for parsing within a Thread
     * @return the parsed Statement
     * @throws JSQLParserException when either the Statement can't be parsed or the configured
     *         timeout is reached
     */

    public static Statement parseStatement(CCJSqlParser parser, ExecutorService executorService)
            throws JSQLParserException {
        Statement statement = null;
        Future<Statement> future = executorService.submit(new Callable<Statement>() {
            @Override
            public Statement call() throws ParseException {
                return parser.Statement();
            }
        });
        try {
            statement = future.get(parser.getConfiguration().getAsLong(Feature.timeOut),
                    TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            parser.interrupted = true;
            future.cancel(true);
            throw new JSQLParserException("Time out occurred.", ex);
        } catch (Exception ex) {
            throw new JSQLParserException(ex);
        }
        return statement;
    }

    /**
     * Parse a statement list.
     *
     * @return the statements parsed
     */
    public static Statements parseStatements(String sqls) throws JSQLParserException {
        return parseStatements(sqls, null);
    }

    public static Statements parseStatements(String sqls, Consumer<CCJSqlParser> consumer)
            throws JSQLParserException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        final Statements statements = parseStatements(sqls, executorService, consumer);
        executorService.shutdown();

        return statements;
    }

    /**
     * Parse a statement list.
     *
     * @return the statements parsed
     */
    public static Statements parseStatements(String sqls, ExecutorService executorService,
            Consumer<CCJSqlParser> consumer)
            throws JSQLParserException {
        Statements statements = null;

        CCJSqlParser parser = newParser(sqls);
        if (consumer != null) {
            consumer.accept(parser);
        }
        boolean allowComplex = parser.getConfiguration().getAsBoolean(Feature.allowComplexParsing);

        // first, try to parse fast and simple
        try {
            statements = parseStatements(parser.withAllowComplexParsing(false), executorService);
        } catch (JSQLParserException ex) {
            // when fast simple parsing fails, try complex parsing but only if it has a chance to
            // succeed
            if (allowComplex && getNestingDepth(sqls) <= ALLOWED_NESTING_DEPTH) {
                // beware: parser must not be re-used but needs to be re-initiated
                parser = newParser(sqls);
                if (consumer != null) {
                    consumer.accept(parser);
                }
                statements = parseStatements(parser.withAllowComplexParsing(true), executorService);
            }
        }
        return statements;
    }

    /**
     * @param parser the Parser armed with a Statement text
     * @param executorService the Executor Service for parsing within a Thread
     * @return the Statements (representing a List of single statements)
     * @throws JSQLParserException when either the Statement can't be parsed or the configured
     *         timeout is reached
     */
    public static Statements parseStatements(CCJSqlParser parser, ExecutorService executorService)
            throws JSQLParserException {
        Statements statements = null;
        Future<Statements> future = executorService.submit(new Callable<Statements>() {
            @Override
            public Statements call() throws ParseException {
                return parser.Statements();
            }
        });
        try {
            statements = future.get(parser.getConfiguration().getAsLong(Feature.timeOut),
                    TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            parser.interrupted = true;
            future.cancel(true);
            throw new JSQLParserException("Time out occurred.", ex);
        } catch (Exception ex) {
            throw new JSQLParserException(ex);
        }
        return statements;
    }

    public static void streamStatements(StatementListener listener, InputStream is, String encoding)
            throws JSQLParserException {
        try {
            CCJSqlParser parser = newParser(is, encoding);
            while (true) {
                Statement stmt = parser.SingleStatement();
                listener.accept(stmt);
                if (parser.getToken(1).kind == CCJSqlParserTokenManager.ST_SEMICOLON) {
                    parser.getNextToken();
                }

                if (parser.getToken(1).kind == CCJSqlParserTokenManager.EOF) {
                    break;
                }
            }
        } catch (Exception ex) {
            throw new JSQLParserException(ex);
        }
    }

    public static int getNestingDepth(String sql) {
        int maxlevel = 0;
        int level = 0;

        char[] chars = sql.toCharArray();
        for (char c : chars) {
            switch (c) {
                case '(':
                    level++;
                    break;
                case ')':
                    if (maxlevel < level) {
                        maxlevel = level;
                    }
                    level--;
                    break;
                default:
                    // Codazy/PMD insists in a Default statement
            }
        }
        return maxlevel;
    }
}

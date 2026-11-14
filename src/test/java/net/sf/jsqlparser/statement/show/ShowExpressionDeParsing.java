/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.show;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;

/** Deparses a SHOW statement with an expression deparser that marks the values it prints. */
final class ShowExpressionDeParsing {

    private ShowExpressionDeParsing() {
        // utility class
    }

    /** Strings print as {@code <value>} and numbers as {@code #value}. */
    static String deparse(String sql) throws JSQLParserException {
        ExpressionDeParser expressions = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(StringValue value, S context) {
                return getBuilder().append('<').append(value.getValue()).append('>');
            }

            @Override
            public <S> StringBuilder visit(LongValue value, S context) {
                return getBuilder().append('#').append(value.getValue());
            }
        };
        StringBuilder builder = new StringBuilder();
        CCJSqlParserUtil.parse(sql)
                .accept(new StatementDeParser(expressions, new SelectDeParser(), builder));
        return builder.toString();
    }
}

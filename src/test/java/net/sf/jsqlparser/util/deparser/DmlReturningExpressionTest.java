/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.deparser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DmlReturningExpressionTest {
    @ParameterizedTest
    @ValueSource(strings = {"UPDATE t SET a = 7 RETURNING a + 8 AS result",
            "DELETE FROM t WHERE a = 7 RETURNING a + 8 AS result",
            "INSERT INTO t(a) VALUES (7) RETURNING a + 8 AS result",
            "UPDATE t SET a = 7 RETURNING (SELECT b + 8 FROM source) AS result"})
    void visitsReturnedExpressionsExactlyOnce(String sql) throws Exception {
        var statement = CCJSqlParserUtil.parse(sql);
        String original = statement.toString();
        List<Long> seen = new ArrayList<>();
        ExpressionDeParser expressions = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(LongValue value, S context) {
                seen.add(value.getValue());
                return getBuilder().append(value.getValue() + 100);
            }
        };
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(expressions, new SelectDeParser(), output), null);
        assertEquals(List.of(7L, 8L), seen);
        assertEquals(original.replace("7", "107").replace("8", "108"), output.toString());
        assertEquals(original, statement.toString());
        assertEquals(output.toString(), CCJSqlParserUtil.parse(output.toString()).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"INSERT INTO t(a) VALUES (1) RETURNING *",
            "DELETE FROM t RETURNING t.*, a AS result",
            "UPDATE t SET a = 1 RETURNING a, b INTO x, y",
            "DELETE FROM t RETURNING WITH (OLD AS o, NEW AS n) o.*, n.id"})
    void preservesWildcardsAliasesAndOutputTargets(String sql) throws Exception {
        var statement = CCJSqlParserUtil.parse(sql);
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(output), null);
        assertEquals(statement.toString(), output.toString());
        assertEquals(statement.toString(), CCJSqlParserUtil.parse(output.toString()).toString());
    }
}

/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.refresh.RefreshMaterializedViewStatement;
import net.sf.jsqlparser.statement.refresh.RefreshMode;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.RefreshMaterializedViewStatementDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 *
 * @author jxnu-liguobin
 */

public class RefreshMaterializedViewStatementTest {

    @ParameterizedTest
    @ValueSource(strings = {"REFRESH MATERIALIZED VIEW my_view",
            "REFRESH MATERIALIZED VIEW my_view WITH DATA",
            "REFRESH MATERIALIZED VIEW my_view WITH NO DATA",
            "REFRESH MATERIALIZED VIEW CONCURRENTLY my_view",
            "REFRESH MATERIALIZED VIEW CONCURRENTLY my_view WITH DATA"})
    public void testRefreshModes(String sql) throws JSQLParserException {
        RefreshMaterializedViewStatement statement =
                (RefreshMaterializedViewStatement) assertSqlCanBeParsedAndDeparsed(sql);
        StringBuilder output = new StringBuilder();
        new RefreshMaterializedViewStatementDeParser(output).deParse(statement);
        assertEquals(sql, output.toString());
        assertEquals(2, CCJSqlParserUtil.parseStatements(sql + "; SELECT 1").size());
    }

    @Test
    void preservesModeWhenVisitingViewName() throws JSQLParserException {
        RefreshMaterializedViewStatement statement =
                (RefreshMaterializedViewStatement) CCJSqlParserUtil
                        .parse("REFRESH MATERIALIZED VIEW old_schema.mv WITH NO DATA");
        StringBuilder output = new StringBuilder();
        ExpressionDeParser expressions = new ExpressionDeParser();
        SelectDeParser selects = new SelectDeParser(expressions, output) {
            @Override
            public <S> StringBuilder visit(Table table, S context) {
                assertEquals("context", context);
                getBuilder().append("new_schema.\"renamed view\"");
                return getBuilder();
            }
        };
        StringBuilder original = new StringBuilder();
        StatementDeParser deparser = new StatementDeParser(expressions, selects, original);
        deparser.setBuilder(output);
        assertSame(output, statement.accept(deparser, "context"));
        assertEquals("", original.toString());
        assertEquals("REFRESH MATERIALIZED VIEW new_schema.\"renamed view\" WITH NO DATA",
                output.toString());
        RefreshMaterializedViewStatement reparsed =
                (RefreshMaterializedViewStatement) CCJSqlParserUtil.parse(output.toString());
        assertEquals(RefreshMode.WITH_NO_DATA, reparsed.getRefreshMode());
        assertEquals("new_schema", reparsed.getView().getSchemaName());
    }

    @Test
    void statementDeparserUsesReplacementOutputBuffer() throws JSQLParserException {
        StringBuilder original = new StringBuilder("old output");
        StringBuilder replacement = new StringBuilder();
        StatementDeParser deparser = new StatementDeParser(original);
        deparser.setBuilder(replacement);
        RefreshMaterializedViewStatement statement =
                (RefreshMaterializedViewStatement) CCJSqlParserUtil
                        .parse("REFRESH MATERIALIZED VIEW schema_name.mv WITH NO DATA");
        assertSame(replacement, statement.accept(deparser, null));
        assertEquals("old output", original.toString());
        assertEquals(statement.toString(), replacement.toString());
    }

    @Test
    void respectsReplacementOutputBuffer() throws JSQLParserException {
        StringBuilder original = new StringBuilder();
        StringBuilder replacement = new StringBuilder();
        RefreshMaterializedViewStatementDeParser deparser =
                new RefreshMaterializedViewStatementDeParser(original);
        deparser.setBuilder(replacement);
        deparser.deParse((RefreshMaterializedViewStatement) CCJSqlParserUtil
                .parse("REFRESH MATERIALIZED VIEW mv WITH NO DATA"));
        assertEquals("", original.toString());
        assertEquals("REFRESH MATERIALIZED VIEW mv WITH NO DATA", replacement.toString());
    }

    @Test
    void rendersDefaultModeLikeOmittedMode() {
        RefreshMaterializedViewStatement statement = new RefreshMaterializedViewStatement(
                new Table("mv"), true, RefreshMode.DEFAULT);
        StringBuilder output = new StringBuilder();
        new RefreshMaterializedViewStatementDeParser(output).deParse(statement);
        assertEquals("REFRESH MATERIALIZED VIEW CONCURRENTLY mv", output.toString());
        assertEquals(statement.toString(), output.toString());
    }
}

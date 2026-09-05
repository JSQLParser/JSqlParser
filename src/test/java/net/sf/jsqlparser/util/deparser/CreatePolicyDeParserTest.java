/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.deparser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.create.policy.CreatePolicy;

import org.junit.jupiter.api.Test;

public class CreatePolicyDeParserTest {

    @Test
    public void testUseExternalExpressionDeParser() throws JSQLParserException {
        StringBuilder builder = new StringBuilder();
        ExpressionDeParser expressionDeParser = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(Column column, S context) {
                getBuilder().append('"').append(column.getColumnName()).append('"');
                return getBuilder();
            }
        };
        expressionDeParser.setBuilder(builder);

        CreatePolicy policy = (CreatePolicy) CCJSqlParserUtil.parse(
                "CREATE POLICY tenant_policy ON users AS RESTRICTIVE USING (tenant_id = owner_id)");
        new CreatePolicyDeParser(expressionDeParser, builder).deParse(policy);

        assertEquals("CREATE POLICY tenant_policy ON users AS RESTRICTIVE "
                + "USING (\"tenant_id\" = \"owner_id\")", builder.toString());
    }
}

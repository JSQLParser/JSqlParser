/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;

import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MySqlAlterAddGroupTest {
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void parsesColumnsAndConstraintInOneGroup(boolean mysql) throws Exception {
        String sql = "ALTER TABLE edge_t ADD COLUMN (a INT, b INT, "
                + "CONSTRAINT edge_ck CHECK (a < b) NOT ENFORCED)";
        Alter alter = (Alter) assertSqlCanBeParsedAndDeparsed(sql, true, parser -> {
            if (mysql) {
                parser.withDialect(Dialect.MYSQL);
            }
        });
        assertEquals(1, alter.getAlterExpressions().size());
        assertEquals(2, alter.getAlterExpressions().get(0).getColDataTypeList().size());
    }
}

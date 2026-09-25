/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter.database;

import static org.junit.jupiter.api.Assertions.*;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.create.database.DatabaseOption;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AlterDatabaseTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "ALTER DATABASE d CHARACTER SET utf8mb4 COLLATE utf8mb4_bin",
            "ALTER SCHEMA `d` DEFAULT CHARACTER SET = utf8mb4 DEFAULT COLLATE = utf8mb4_bin",
            "ALTER DATABASE d DEFAULT ENCRYPTION = 'N'",
            "ALTER DATABASE d READ ONLY = 0", "ALTER DATABASE d READ ONLY 1",
            "ALTER DATABASE READ ONLY = DEFAULT", "ALTER SCHEMA DEFAULT CHARACTER SET utf8mb4"})
    void roundTripAndStatementBoundary(String sql) throws JSQLParserException {
        AlterDatabase statement = parse(sql);
        assertEquals(sql, statement.toString());
        StringBuilder buffer = new StringBuilder();
        statement.accept(new StatementDeParser(buffer), null);
        assertEquals(sql, buffer.toString());
        assertEquals(sql, parse(buffer.toString()).toString());
        assertTrue(new TablesNamesFinder().getTables(statement).isEmpty());
        assertEquals(2, CCJSqlParserUtil.parseStatements(sql + "; SELECT 1",
                p -> p.withDialect(Dialect.MYSQL)).size());
    }

    @Test
    void optionMutationAndVisitorDispatch() throws JSQLParserException {
        AlterDatabase statement = parse("ALTER DATABASE old_db READ ONLY = 0");
        statement.setDatabaseName("new_db");
        statement.getOption(DatabaseOption.Kind.READ_ONLY).orElseThrow().setValue("1");
        assertEquals("ALTER DATABASE new_db READ ONLY = 1", statement.toString());
        assertEquals("context:new_db", statement.accept(new StatementVisitorAdapter<String>() {
            @Override
            public <S> String visit(AlterDatabase database, S context) {
                return context + ":" + database.getDatabaseName();
            }
        }, "context"));
    }

    @Test
    void rejectIncompleteAndInvalidReadOnly() {
        for (String sql : new String[] {"ALTER DATABASE d", "ALTER DATABASE d READ ONLY = 2",
                "ALTER DATABASE d DEFAULT READ ONLY = 0"}) {
            assertThrows(JSQLParserException.class, () -> parse(sql));
        }
    }

    private static AlterDatabase parse(String sql) throws JSQLParserException {
        return (AlterDatabase) CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.MYSQL));
    }
}

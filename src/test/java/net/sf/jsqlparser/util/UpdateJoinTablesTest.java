/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import java.util.Set;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.update.Update;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UpdateJoinTablesTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "UPDATE target t JOIN source s ON s.id IN (SELECT id FROM hidden) SET t.a = 1",
            "UPDATE target t LEFT JOIN source s ON EXISTS (SELECT 1 FROM hidden h WHERE h.id = s.id) SET t.a = 1",
            "UPDATE target SET a = 1 FROM source s JOIN target t ON s.id IN (SELECT id FROM hidden)",
            "WITH h AS (SELECT id FROM hidden) UPDATE target t JOIN source s ON s.id IN (SELECT id FROM h) SET t.a = 1"})
    void includesTablesInsideJoinConditions(String sql) throws Exception {
        assertEquals(Set.of("target", "source", "hidden"), TablesNamesFinder.findTables(sql));
    }

    @Test
    void preservesContextAndVisitsEachSourceOnce() throws Exception {
        Update update = (Update) CCJSqlParserUtil.parse(
                "UPDATE target t JOIN source s ON s.id IN (SELECT id FROM hidden) SET t.a = 1");
        Object context = new Object();
        java.util.List<String> seen = new java.util.ArrayList<>();
        TablesNamesFinder<Void> finder = new TablesNamesFinder<Void>() {
            {
                init(false);
            }

            @Override
            public <S> Void visit(Table table, S actual) {
                assertSame(context, actual);
                seen.add(table.getName());
                return null;
            }
        };
        update.accept(finder, context);
        assertEquals(java.util.List.of("target", "source", "hidden"), seen);
    }
}

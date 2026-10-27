/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

public class JoinUsingDeParseTest {

    private static String usingSelect(int columnCount) {
        StringBuilder sb = new StringBuilder("SELECT * FROM a JOIN b USING (");
        for (int i = 0; i < columnCount; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("c").append(i);
        }
        return sb.append(")").toString();
    }

    @Test
    void deParseOfLargeUsingColumnListIsLinear() throws Exception {
        // usingColumns is a LinkedList, so deparsing the clause through
        // Select.appendStringListTo used to read it by index (get(i)), which is O(n^2).
        String sql = usingSelect(200000);
        Statement statement = CCJSqlParserUtil.parse(sql);
        assertEquals(sql, assertTimeoutPreemptively(Duration.ofSeconds(5), statement::toString));
    }

    @Test
    void deParseOfUsingColumnListIsUnchanged() throws Exception {
        String sql = "SELECT * FROM a JOIN b USING (c0, c1, c2)";
        assertEquals(sql, CCJSqlParserUtil.parse(sql).toString());
    }
}

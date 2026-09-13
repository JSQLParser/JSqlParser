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

import static org.junit.jupiter.api.Assertions.*;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.feature.Feature;
import org.junit.jupiter.api.Test;

class DialectPresetIsolationTest {
    @Test
    void callersCannotMutateSharedPresets() {
        for (Dialect dialect : Dialect.values()) {
            assertThrows(UnsupportedOperationException.class,
                    () -> dialect.getLexerFeatures().add(Feature.allowHashLineComments));
            assertThrows(UnsupportedOperationException.class,
                    () -> dialect.getLexerFeatures().clear());
        }
    }

    @Test
    void explicitParserOverridesAreLocalAndDoNotModifyPresetDefaults() {
        CCJSqlParser first = CCJSqlParserUtil.newParser("SELECT 1").withDialect(Dialect.MYSQL)
                .withHashLineComments(false);
        CCJSqlParser second = CCJSqlParserUtil.newParser("SELECT 1").withDialect(Dialect.MYSQL);
        assertFalse(first.getAsBoolean(Feature.allowHashLineComments));
        assertTrue(second.getAsBoolean(Feature.allowHashLineComments));
        assertTrue(Dialect.MYSQL.getLexerFeatures().contains(Feature.allowHashLineComments));
    }

    @Test
    void preservesAdditivePresetApplicationAndExplicitOverrideOrder() {
        CCJSqlParser parser = CCJSqlParserUtil.newParser("SELECT 1").withDialect(Dialect.SQLSERVER)
                .withDialect(Dialect.POSTGRESQL);
        assertTrue(parser.getAsBoolean(Feature.allowSquareBracketQuotation));
        parser.withSquareBracketQuotation(false);
        assertFalse(parser.getAsBoolean(Feature.allowSquareBracketQuotation));
        assertFalse(CCJSqlParserUtil.newParser("SELECT 1").withDialect(Dialect.POSTGRESQL)
                .getAsBoolean(Feature.allowSquareBracketQuotation));
    }
}

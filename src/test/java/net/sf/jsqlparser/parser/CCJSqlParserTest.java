/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2023 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import net.sf.jsqlparser.parser.feature.Feature;

public class CCJSqlParserTest {
    @Test
    public void parserWithTimeout() throws Exception {
        CCJSqlParser parser = CCJSqlParserUtil.newParser("foo").withTimeOut(123L);
        
        Long timeOut = parser.getAsLong(Feature.timeOut);

        assertThat(timeOut).isEqualTo(123L);
    }
}

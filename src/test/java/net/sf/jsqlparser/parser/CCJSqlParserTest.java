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

package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.ParserKeywordsUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;

/**
 *
 * @author <a href="mailto:andreas@manticore-projects.com">Andreas Reichel</a>
 */

@RunWith(Parameterized.class)
public class ConditionalKeywordsTest {
    public final static Logger LOGGER = Logger.getLogger(ConditionalKeywordsTest.class.getName());

    @Parameters(name = "Keyword {0}")
    public final static Iterable<String> KEY_WORDS() {
        List<String> keywords = new ArrayList<>();
        try {
            try {
                keywords.addAll(ParserKeywordsUtils.getDefinedKeywords());
                for (String reserved: ParserKeywordsUtils.getReservedKeywords(
                        // get all PARSER RESTRICTED without the ALIAS RESTRICTED
                        ParserKeywordsUtils.RESTRICTED_JSQLPARSER
                        & ~ParserKeywordsUtils.RESTRICTED_ALIAS
                        )) {
                    keywords.remove(reserved);
                }
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Failed to generate the Keyword List", ex);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to generate the Keyword List", ex);
        }
        return keywords;
    }

    protected String keyword;

    public ConditionalKeywordsTest(String keyword) {
        this.keyword = keyword;
    }

    @Test
    public void testRelObjectNameExt() throws JSQLParserException {
        String sqlStr = String.format("SELECT %1$s.%1$s.%1$s AS \"%1$s\" from %1$s ORDER BY %1$s ",  keyword);
        LOGGER.fine(sqlStr);
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}

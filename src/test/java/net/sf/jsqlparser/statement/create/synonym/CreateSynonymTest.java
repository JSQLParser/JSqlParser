package net.sf.jsqlparser.statement.create.synonym;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;

public class CreateSynonymTest {

    @Test
    public void createPublic() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE PUBLIC SYNONYM TBL_TABLE_NAME FOR SCHEMA.T_TBL_NAME");
    }

    @Test
    public void createWithReplace() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE OR REPLACE SYNONYM TBL_TABLE_NAME FOR SCHEMA.T_TBL_NAME");
    }

    @Test
    public void createWithReplacePublic() throws Exception {
        assertSqlCanBeParsedAndDeparsed("CREATE OR REPLACE PUBLIC SYNONYM TBL_TABLE_NAME FOR SCHEMA.T_TBL_NAME");
    }

    /**
     * The dblink should be parsed as a regular name
     *
     * @throws Exception
     */
    @Test
    public void createWithDbLink() throws Exception {
        assertSqlCanBeParsedAndDeparsed("CREATE PUBLIC SYNONYM emp_table FOR hr.employees@remote.us.oracle.com");
    }

    @Test
    public void synonymAttributes() throws Exception {
        final CreateSynonym createSynonym = (CreateSynonym) CCJSqlParserUtil.parse("CREATE OR REPLACE PUBLIC SYNONYM TBL_TABLE_NAME FOR SCHEMA.T_TBL_NAME");

        Assertions.assertThat(createSynonym.isOrReplace()).isTrue();
        Assertions.assertThat(createSynonym.isPublicSynonym()).isTrue();
        Assertions.assertThat(createSynonym.getSynonym().getFullyQualifiedName()).isEqualTo("TBL_TABLE_NAME");
        Assertions.assertThat(createSynonym.getFor()).isEqualTo("SCHEMA.T_TBL_NAME");
    }
}

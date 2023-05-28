/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.synonym;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Synonym;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

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
        assertThat(createSynonym.isOrReplace()).isTrue();
        assertThat(createSynonym.isPublicSynonym()).isTrue();
        assertThat(createSynonym.getSynonym().getFullyQualifiedName()).isEqualTo("TBL_TABLE_NAME");
        assertThat(createSynonym.getFor()).isEqualTo("SCHEMA.T_TBL_NAME");
        assertEquals(2, createSynonym.getForList().size());
        assertEquals("NEW_TBL_TABLE_NAME", createSynonym.withSynonym(new Synonym().withName("NEW_TBL_TABLE_NAME")).getSynonym().getName());
    }
}

/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.schema.Sequence;
import net.sf.jsqlparser.schema.Sequence.Parameter;
import net.sf.jsqlparser.schema.Sequence.ParameterType;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.sequence.AlterSequence;
import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertEqualsObjectTree;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import org.junit.jupiter.api.Test;

public class AlterSequenceTest {

    @Test
    public void testCreateSequence_withIncrement() throws JSQLParserException {
        String statement = "ALTER SEQUENCE my_seq CACHE 100";
        Statement parsed = assertSqlCanBeParsedAndDeparsed(statement);
        AlterSequence created = new AlterSequence().withSequence(new Sequence().withName("my_seq").addParameters(new Parameter(ParameterType.CACHE).withValue(100L)));
        assertDeparse(created, statement);
        assertEqualsObjectTree(parsed, created);
    }
}

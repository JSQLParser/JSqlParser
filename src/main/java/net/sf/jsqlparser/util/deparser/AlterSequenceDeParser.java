/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.deparser;

import net.sf.jsqlparser.statement.alter.sequence.AlterSequence;

/**
 * A class to de-parse (that is, transform from JSqlParser hierarchy into a string) a
 * {@link net.sf.jsqlparser.statement.alter.sequence.AlterSequence}
 */
public class AlterSequenceDeParser extends AbstractDeParser<AlterSequence> {

    /**
     * @param buffer the buffer that will be filled with the AlterSequence
     */
    public AlterSequenceDeParser(StringBuilder buffer) {
        super(buffer);
    }

    @Override
    public void deParse(AlterSequence statement) {
        builder.append("ALTER SEQUENCE ");
        builder.append(statement.getSequence());
    }
}

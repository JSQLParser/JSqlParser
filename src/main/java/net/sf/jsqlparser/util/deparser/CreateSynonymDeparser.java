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

import net.sf.jsqlparser.statement.create.synonym.CreateSynonym;

/**
 * A class to de-parse (that is, transform from JSqlParser hierarchy into a string) a
 * {@link CreateSynonym}
 */
public class CreateSynonymDeparser extends AbstractDeParser<CreateSynonym> {

    public CreateSynonymDeparser(StringBuilder buffer) {
        super(buffer);
    }

    @Override
    void deParse(CreateSynonym createSynonym) {
        buffer.append("CREATE ");
        if (createSynonym.isOrReplace()) {
            buffer.append("OR REPLACE ");
        }
        if (createSynonym.isPublicSynonym()) {
            buffer.append("PUBLIC ");
        }
        buffer.append("SYNONYM " + createSynonym.getSynonym());
        buffer.append(' ');
        buffer.append("FOR " + createSynonym.getFor());
    }
}

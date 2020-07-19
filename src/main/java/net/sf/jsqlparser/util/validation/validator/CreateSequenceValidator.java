/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation.validator;

import net.sf.jsqlparser.statement.create.sequence.CreateSequence;

/**
 * A class to de-parse (that is, transform from JSqlParser hierarchy into a string) a
 * {@link net.sf.jsqlparser.statement.create.sequence.CreateSequence}
 */
public class CreateSequenceValidator extends AbstractValidator<CreateSequence> {


    @Override
    public void validate(CreateSequence statement) {
        //        buffer.append("CREATE SEQUENCE ");
        //        buffer.append(statement.getSequence());
    }
}

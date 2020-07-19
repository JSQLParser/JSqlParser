/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation.validator;

import net.sf.jsqlparser.statement.ShowColumnsStatement;

public class ShowColumnsStatementValidator extends AbstractValidator<ShowColumnsStatement> {

    @Override
    public void validate(ShowColumnsStatement show) {
        //        buffer.append("SHOW COLUMNS FROM ").append(show.getTableName());
    }
}

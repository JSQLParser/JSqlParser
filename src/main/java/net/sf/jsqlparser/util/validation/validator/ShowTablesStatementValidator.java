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

import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.show.ShowTablesStatement;
import net.sf.jsqlparser.util.validation.metadata.NamedObject;

/**
 * @author gitmotte
 */
public class ShowTablesStatementValidator extends AbstractValidator<ShowTablesStatement> {

    @Override
    public void validate(ShowTablesStatement show) {
        validateFeatureAndName(Feature.showTables, NamedObject.database, show.getDbName());
    }
}

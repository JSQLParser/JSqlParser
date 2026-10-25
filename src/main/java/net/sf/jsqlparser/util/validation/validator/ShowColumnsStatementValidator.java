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
import net.sf.jsqlparser.statement.ShowColumnsStatement;
import net.sf.jsqlparser.util.validation.metadata.NamedObject;

/**
 * @author gitmotte
 */
public class ShowColumnsStatementValidator extends AbstractValidator<ShowColumnsStatement> {

    @Override
    public void validate(ShowColumnsStatement show) {
        String tableName = show.getTableName();
        if (show.getDbName() != null && show.getTable() != null) {
            // MySQL: the trailing FROM db names the database of the table
            tableName = show.getDbName() + "." + show.getTable().getName();
        }
        validateFeatureAndName(Feature.showColumns, NamedObject.table, tableName);
    }
}

/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation.validator;

import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.show.ShowCreateStatement;
import net.sf.jsqlparser.util.validation.ValidationCapability;
import net.sf.jsqlparser.util.validation.metadata.NamedObject;

public class ShowCreateStatementValidator extends AbstractValidator<ShowCreateStatement> {

    @Override
    public void validate(ShowCreateStatement show) {
        for (ValidationCapability c : getCapabilities()) {
            validateFeature(c, Feature.showCreate);
            NamedObject namedObject = namedObject(show.getObjectType());
            if (namedObject == NamedObject.database) {
                validateOptionalName(c, namedObject, show.getDatabaseName());
            } else if (namedObject != null && show.getName() != null) {
                validateName(c, namedObject, show.getName().getFullyQualifiedName());
            }
        }
    }

    /** The metadata kind checked for the shown object, or null if there is none. */
    private static NamedObject namedObject(ShowCreateStatement.ObjectType type) {
        if (type == null) {
            return null;
        }
        switch (type) {
            case TABLE:
                return NamedObject.table;
            case VIEW:
                return NamedObject.view;
            case PROCEDURE:
                return NamedObject.procedure;
            case DATABASE:
                return NamedObject.database;
            default:
                // functions, triggers, events and accounts are not looked up
                return null;
        }
    }
}

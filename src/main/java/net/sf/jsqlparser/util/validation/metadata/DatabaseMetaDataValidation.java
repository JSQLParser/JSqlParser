/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation;

import java.util.function.Consumer;

public interface DatabaseMetaDataValidation extends ValidationCapability {

    public enum Keys implements ContextKey {
        /**
         * @see NamedObject
         */
        namedobject,
        /**
         * the fully qualified name
         */
        fqn
    }

    public enum NamedObject {
        /**
         * a name constisting of max. 1 identifiers, i.e. [database]
         */
        database,
        /**
         * a name constisting of max. 2 identifiers, i.e. [database].[schema]
         */
        schema,
        /**
         * a name constisting of max. 3 identifiers, i.e. [catalog].[schema].[table]
         */
        table,
        /**
         * a name constisting of max. 3 identifiers, i.e. [catalog].[schema].[view]
         */
        view,
        /**
         * a name constisting of max. 4 identifiers, i.e.
         * [catalog].[schema].[table].[columnName]
         */
        column,
        index,
        constraint,
        uniqueConstraint,
    }

    /**
     * @param o
     * @param fqn - fully qualified name
     * @throws ValidationException
     */
    @Override
    default void validate(ValidationContext ctx, Consumer<String> errorMessageConsumer) throws ValidationException {
        try {
            String fqn = ctx.get(Keys.fqn, String.class);
            NamedObject namedObject = ctx.get(Keys.namedobject, NamedObject.class);
            if (!exists(namedObject, fqn)) {
                errorMessageConsumer.accept(getErrorMessage(fqn));
            }
        } catch (UnsupportedOperationException uoe) {
            // should we log this on a trace level?
        }
    }

    /**
     * @param o
     * @param fqn
     * @return
     * @throws UnsupportedOperationException - if testing of given
     *                                       {@link NamedObject} is not supported.
     */
    public boolean exists(NamedObject o, String fqn) throws ValidationException;

    /**
     * @param fqn
     * @return
     */
    default String getErrorMessage(String fqn) {
        return fqn + " does not exist.";
    }

}

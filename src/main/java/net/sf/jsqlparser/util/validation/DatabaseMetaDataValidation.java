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
        database, schema, table, column, index, constraint
    }

    /**
     * @param o
     * @param fqn - fully qualified name
     */
    @Override
    default void validate(ValidationContext ctx, Consumer<String> errorMessageConsumer) {
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
    public boolean exists(NamedObject o, String fqn);

    /**
     * @param fqn
     * @return
     */
    default String getErrorMessage(String fqn) {
        return fqn + " does not exist.";
    }

}

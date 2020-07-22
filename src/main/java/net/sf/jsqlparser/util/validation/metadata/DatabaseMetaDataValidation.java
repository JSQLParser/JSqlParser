/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation.metadata;

import java.util.function.Consumer;

import net.sf.jsqlparser.util.validation.ValidationCapability;
import net.sf.jsqlparser.util.validation.ValidationContext;
import net.sf.jsqlparser.util.validation.ValidationException;

public interface DatabaseMetaDataValidation extends ValidationCapability {

    public static final String NAME = "meta data";

    /**
     * @param o
     * @param fqn - fully qualified name
     * @throws ValidationException
     */
    @Override
    default void validate(ValidationContext ctx, Consumer<String> errorMessageConsumer) {
        String fqn = ctx.get(MetadataContext.fqn, String.class);
        try {
            NamedObject namedObject = ctx.get(MetadataContext.namedobject, NamedObject.class);
            if (!exists(namedObject, fqn)) {
                errorMessageConsumer.accept(getErrorMessage(fqn));
            }
        } catch (ValidationException e) {
            errorMessageConsumer.accept(getErrorMessage(fqn, e));
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

    /**
     * @param fqn
     * @return
     */
    default String getErrorMessage(String fqn, ValidationException e) {
        return fqn + " does not exist. " + e.getMessage();
    }

    @Override
    default String getName() {
        return NAME;
    }

}

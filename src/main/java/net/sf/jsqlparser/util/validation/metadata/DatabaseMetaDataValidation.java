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

import java.sql.SQLException;
import java.util.function.Consumer;
import net.sf.jsqlparser.util.validation.UnexpectedValidationException;
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
    default void validate(ValidationContext context, Consumer<ValidationException> errorConsumer) {
        String fqn = context.get(MetadataContext.fqn, String.class);
        NamedObject namedObject = context.get(MetadataContext.namedobject, NamedObject.class);
        boolean checkForExists = context.get(MetadataContext.exists, Boolean.class);
        try {
            boolean exists = exists(namedObject, fqn);
            if (exists ^ checkForExists) { // XOR
                errorConsumer.accept(getErrorMessage(fqn, checkForExists));
            }
        } catch (ValidationException ve) {
            errorConsumer.accept(ve);
        } catch (UnsupportedOperationException uoe) {
            // should we log this on a trace level?
        } catch (Exception e) {
            errorConsumer.accept(getUnexpectedErrorMessage(fqn, namedObject, e));
        }
    }

    /**
     * @param o
     * @param fqn
     * @return <code>true</code>, if the object exists, <code>false</code>
     *         otherwise.
     * @throws ValidationException           - on specific errors like
     *                                       {@link DatabaseException} on
     *                                       database-errors wrapping a
     *                                       {@link SQLException} or
     *                                       PersistenceException
     * @throws UnsupportedOperationException - if testing of given
     *                                       {@link NamedObject} is not supported.
     */
    public boolean exists(NamedObject o, String fqn);

    /**
     * @param fqn
     * @return
     */
    default ValidationException getErrorMessage(String fqn, boolean checkForExists) {
        return toError(String.format("%s does %sexist.", fqn, checkForExists ? "not " : ""));
    }

    /**
     * @param fqn
     * @return
     */
    default ValidationException getUnexpectedErrorMessage(String fqn, NamedObject namedObject, Exception e) {
        return new UnexpectedValidationException(
                fqn + ": cannot validate " + namedObject + "-name. detail: " + e.getMessage(), e);
    }

    @Override
    default String getName() {
        return NAME;
    }

}

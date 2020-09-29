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
     * @param named - fully qualified name
     * @throws ValidationException
     */
    @Override
    default void validate(ValidationContext context, Consumer<ValidationException> errorConsumer) {
        Named named = context.get(MetadataContext.named, Named.class);
        boolean checkForExists = context.get(MetadataContext.exists, Boolean.class);
        try {
            boolean exists = exists(named);
            if (exists ^ checkForExists) { // XOR
                errorConsumer.accept(getErrorMessage(named, checkForExists));
            }
        } catch (ValidationException ve) {
            errorConsumer.accept(ve);
        } catch (UnsupportedOperationException uoe) {
            // should we log this on a trace level?
        } catch (Exception e) {
            errorConsumer.accept(getUnexpectedErrorMessage(named, e));
        }
    }

    /**
     * @param named
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
    public boolean exists(Named named);

    /**
     * @param named
     * @param checkForExists
     * @return a new {@link ValidationException}
     */
    default ValidationException getErrorMessage(Named named, boolean checkForExists) {
        return toError(String.format("%s does %sexist.", named.getFqn(), checkForExists ? "not " : ""));
    }

    /**
     * @param named
     * @param cause
     * @return a new {@link ValidationException}
     */
    default ValidationException getUnexpectedErrorMessage(Named named, Exception cause) {
        return new UnexpectedValidationException(
                named.getFqn() + ": cannot validate " + named.getNamedObject() + "-name. detail: " + cause.getMessage(), cause);
    }

    @Override
    default String getName() {
        return NAME;
    }

}

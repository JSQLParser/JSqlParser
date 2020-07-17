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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;

public class ValidationUtil {

    private ValidationUtil() {
    }

    /**
     * @param capabilities
     * @param statements
     * @return a list of {@link ValidationError}'s
     */
    public static List<ValidationError> validate(Collection<ValidationCapability> capabilities, String... statements) {
        return validate(capabilities, Arrays.asList(statements));
    }

    /**
     * @param capabilities
     * @param statements
     * @return a list of {@link ValidationError}'s
     */
    public static List<ValidationError> validate(Collection<ValidationCapability> capabilities,
            List<String> statements) {
        List<ValidationError> errors = new ArrayList<>();
        for (String statement : statements) {
            Statement stmt = null;
            try {
                stmt = CCJSqlParserUtil.parse(statement);
                if (!capabilities.isEmpty()) {
                    StatementValidator validator = new StatementValidator();
                    validator.setCapabilities(capabilities);
                    stmt.accept(validator);

                    for (Entry<ValidationCapability, Set<String>> e : validator
                            .getValidationErrors().entrySet()) {
                        errors.add(new ValidationError(statement).withCapability(e.getKey())
                                .addErrors(e.getValue()));
                    }
                }
            } catch (JSQLParserException e) {
                errors.add(new ValidationError(statement).withException(e)
                        .addError("Cannot parse statement: " + e.getMessage()));
            }
        }
        return errors;
    }

}

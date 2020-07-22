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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.jsqlparser.parser.feature.FeatureConfiguration;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.validation.validator.StatementValidator;

public class ValidationUtil {

    private ValidationUtil() {
    }

    /**
     * @param capabilities
     * @param statements
     * @return a list of {@link ValidationError}'s
     */
    public static List<ValidationError> validate(Collection<? extends ValidationCapability> capabilities,
            String... statements) {
        return validate(capabilities, Arrays.asList(statements));
    }

    /**
     * @param capabilities
     * @param statements
     * @return a list of {@link ValidationError}'s
     */
    public static List<ValidationError> validate(
            Collection<? extends ValidationCapability> capabilities,
            List<String> statements) {
        return validate(new FeatureConfiguration(), capabilities, statements);
    }

    /**
     * @param config
     * @param capabilities
     * @param statements
     * @return a list of {@link ValidationError}'s
     */
    public static List<ValidationError> validate(FeatureConfiguration config,
            Collection<? extends ValidationCapability> capabilities, List<String> statements) {
        List<ValidationError> errors = new ArrayList<>();

        ParseCapability parse = new ParseCapability();

        ValidationContext context = createValidationContext(config, capabilities);
        for (String statement : statements) {

            parse.validate(context.put(ParseContext.statement, statement),
                    e -> errors.add(new ValidationError(statement).withCapability(parse).addError(e)));

            Statement stmt = parse.getStatement();
            if (!capabilities.isEmpty()) {
                Map<ValidationCapability, Set<ValidationException>> errorMap = validate(stmt, context);
                errors.addAll(toValidationErrors(statement, errorMap));
            }

        }
        return errors;
    }

    /**
     * @param config
     * @param capabilities
     * @return a {@link ValidationContext} of the given config and capabilities
     */
    public static ValidationContext createValidationContext(FeatureConfiguration config,
            Collection<? extends ValidationCapability> capabilities) {
        ValidationContext context = new ValidationContext();
        context.setCapabilities(new ArrayList<>(capabilities));
        context.setConfiguration(config);
        return context;
    }

    /**
     * @param statement
     * @param errorMap
     * @return a list of {@link ValidationError}'
     */
    public static List<ValidationError> toValidationErrors(String statement,
            Map<ValidationCapability, Set<ValidationException>> errorMap) {
        List<ValidationError> errors = new ArrayList<>();
        for (Entry<ValidationCapability, Set<ValidationException>> e : errorMap.entrySet()) {
            errors.add(new ValidationError(statement)
                    .withCapability(e.getKey()).addErrors(e.getValue()));
        }
        return errors;
    }

    /**
     * @param stmt
     * @param context
     * @return
     */
    public static Map<ValidationCapability, Set<ValidationException>> validate(Statement stmt,
            ValidationContext context) {
        StatementValidator validator = new StatementValidator();
        validator.setContext(context);
        validator.validate(stmt);
        return validator.getValidationErrors();
    }

}

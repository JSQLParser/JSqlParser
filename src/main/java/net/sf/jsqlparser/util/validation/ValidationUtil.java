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
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;

public class ValidationUtil {

    private ValidationUtil() {
    }

    /**
     * @param databaseTypes
     * @param statements
     * @return a list of {@link ValidationError}'s
     */
    public static List<ValidationError> validate(List<DatabaseType> databaseTypes, String... statements) {
        return validate(databaseTypes, Arrays.asList(statements));
    }

    /**
     * @param databaseTypes
     * @param statements
     * @return a list of {@link ValidationError}'s
     */
    public static List<ValidationError> validate(List<DatabaseType> databaseTypes, List<String> statements) {
        List<ValidationError> errors = new ArrayList<>();
        for (String statement : statements) {
            Statement stmt = null;
            try {
                stmt = CCJSqlParserUtil.parse(statement);
                if (!databaseTypes.isEmpty()) {
                    StatementValidator validator = new StatementValidator();
                    stmt.accept(validator);

                    for (Entry<DatabaseType, Set<String>> e : validator
                            .getValidationErrors(databaseTypes).entrySet()) {
                        errors.add(new ValidationError(statement).withDatabaseType(e.getKey())
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

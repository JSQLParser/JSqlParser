package net.sf.jsqlparser.util.validation.validator;

import org.junit.Test;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;

public class InsertValidatorTest extends ValidationTestAsserts {

    @Test
    public void testValidationInsert() throws JSQLParserException {
        String sql = "INSERT INTO tab1 (a, b) VALUES (5, 'val')";
        validateNoErrors(sql, 1, DatabaseType.values());
    }

    @Test
    public void testValidationInsertNotAllowed() throws JSQLParserException {
        String sql = "INSERT INTO tab1 (a, b, c) VALUES (5, 'val', ?)";
        validateNotAllowed(sql, 1, 1, FeaturesAllowed.SELECT.copy().add(FeaturesAllowed.JDBC), Feature.insert,
                Feature.insertValues);
    }


}

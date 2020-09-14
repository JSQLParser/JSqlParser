package net.sf.jsqlparser.util.validation.validator;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.util.validation.ValidationError;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.ValidationUtil;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;

public class DeleteValidatorTest extends ValidationTestAsserts {

    @Test
    public void testValidationDelete() throws JSQLParserException {
        String sql = "DELETE FROM tab1 WHERE ref IN (SELECT id FROM tab2 WHERE criteria = ?); "
                + "DELETE FROM tab2 t2 WHERE t2.criteria = ?;";

        validateNoErrors(sql, 2, DatabaseType.DATABASES);
    }

    @Test
    public void testValidationDeleteNotAllowed() throws JSQLParserException {
        String sql = "DELETE FROM tab2 t2 WHERE t2.criteria = ?;";

        ValidationUtil validation = new ValidationUtil( //
                Arrays.asList(FeaturesAllowed.SELECT.copy().add(FeaturesAllowed.JDBC)), sql);
        List<ValidationError> errors = validation.validate();

        assertErrorsSize(errors, 1);
        assertEquals(1, validation.getParsedStatements().getStatements().size());
        assertNotAllowed(errors.get(0).getErrors(), Feature.delete);
    }

    @Test
    public void testValidationDeleteSupportedAndNotSupported() throws JSQLParserException {
        String sql = "DELETE a1, a2 FROM t1 AS a1 INNER JOIN t2 AS a2 WHERE a1.id = a2.id;";

        ValidationUtil validation = new ValidationUtil( //
                Arrays.asList(DatabaseType.H2), sql);
        List<ValidationError> errors = validation.validate();

        assertErrorsSize(errors, 1);
        assertEquals(1, validation.getParsedStatements().getStatements().size());
        assertErrorsSize(errors.get(0).getErrors(), 2);
        assertNotSupported(errors.get(0).getErrors(), Feature.deleteTables, Feature.deleteJoin);

        validateNoErrors(sql, 1, DatabaseType.MARIADB, DatabaseType.MYSQL);
    }

    @Test
    public void testValidationDeleteLimitOrderBy() throws JSQLParserException {
        String sql = "DELETE FROM table t WHERE t.criteria > 5 ORDER BY t.criteria LIMIT 1;";

        validateNoErrors(sql, 1, DatabaseType.MARIADB, DatabaseType.MYSQL);
    }

}

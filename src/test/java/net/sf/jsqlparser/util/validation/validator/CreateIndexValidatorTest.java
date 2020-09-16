package net.sf.jsqlparser.util.validation.validator;

import java.util.Arrays;
import org.junit.Test;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;

public class CreateIndexValidatorTest extends ValidationTestAsserts {

    @Test
    public void testValidateCreateIndex() throws JSQLParserException {
        for (String sql : Arrays.asList(
                "CREATE INDEX idx_american_football_action_plays_1 ON american_football_action_plays USING btree (play_type)")) {
            validateNoErrors(sql, 1, DatabaseType.DATABASES);
        }
    }

    @Test
    public void testValidateCreateIndexNotAllowed() throws JSQLParserException {
        for (String sql : Arrays.asList(
                "CREATE INDEX idx_american_football_action_plays_1 ON american_football_action_plays USING btree (play_type)")) {
            validateNotAllowed(sql, 1, 1, FeaturesAllowed.DML, Feature.createIndex);
        }
    }

}

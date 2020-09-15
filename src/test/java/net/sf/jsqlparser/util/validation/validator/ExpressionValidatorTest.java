package net.sf.jsqlparser.util.validation.validator;

import org.junit.Test;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;

public class ExpressionValidatorTest extends ValidationTestAsserts {

    private static final FeaturesAllowed EXPRESSIONS = FeaturesAllowed.SELECT.copy().add(FeaturesAllowed.EXPRESSIONS);

    @Test
    public void testAddition() {
        validateNoErrors("SELECT 1 + a", 1, EXPRESSIONS);
    }

    @Test
    public void testBitwiseAnd() {
        validateNoErrors("SELECT a & b", 1, EXPRESSIONS);
    }

    @Test
    public void testAndOr() {
        validateNoErrors("SELECT CASE WHEN a AND b THEN c ELSE d END", 1, EXPRESSIONS);
        validateNoErrors("SELECT CASE WHEN a && b THEN c ELSE d END", 1, EXPRESSIONS);
        validateNoErrors("SELECT CASE WHEN a OR b THEN c ELSE d END", 1, EXPRESSIONS);
    }

    @Test
    public void testBetween() {
        validateNoErrors("SELECT * FROM tab WHERE a BETWEEN 1 AND 5", 1, EXPRESSIONS);
    }

    @Test
    public void testEquals() {
        validateNoErrors("SELECT CASE WHEN a = b THEN c ELSE d END", 1, EXPRESSIONS);
        validateNoErrors("SELECT CASE WHEN a != b THEN c ELSE d END", 1, EXPRESSIONS);
        validateNoErrors("SELECT CASE WHEN a <> b THEN c ELSE d END", 1, EXPRESSIONS);
    }

    @Test
    public void testParenthesis() {
        validateNoErrors("SELECT CASE WHEN ((a = b) OR b = c) AND (d <> a) AND d <> c THEN c ELSE d END", 1,
                EXPRESSIONS);
    }

    @Test
    public void testMatches() throws JSQLParserException {
        validateNoErrors("SELECT * FROM team WHERE team.search_column @@ to_tsquery('new & york & yankees')", 1,
                EXPRESSIONS);
    }

    @Test
    public void testNot() {
        validateNoErrors("SELECT CASE WHEN !a AND !b THEN c ELSE d END", 1, EXPRESSIONS);
    }

    @Test
    public void testGreaterLower() {
        validateNoErrors("SELECT CASE WHEN a > b THEN c ELSE d END", 1, EXPRESSIONS);
        validateNoErrors("SELECT CASE WHEN a >= b THEN c ELSE d END", 1, EXPRESSIONS);
        validateNoErrors("SELECT CASE WHEN a < b THEN c ELSE d END", 1, EXPRESSIONS);
        validateNoErrors("SELECT CASE WHEN a <= b THEN c ELSE d END", 1, EXPRESSIONS);
    }

    @Test
    public void testBitwiseLeftShift() {
        validateNoErrors("SELECT a << b", 1, EXPRESSIONS);
    }

    @Test
    public void testBitwiseOr() {
        validateNoErrors("SELECT a | b as a_or_b", 1, EXPRESSIONS);
    }

    @Test
    public void testBitwiseRightShift() {
        validateNoErrors("SELECT a >> b", 1, EXPRESSIONS);
    }

    @Test
    public void testBitwiseXor() {
        validateNoErrors("SELECT a ^ b as a_xor_b", 1, EXPRESSIONS);
    }

    @Test
    public void testConcat() {
        validateNoErrors("SELECT a || b FROM table", 1, EXPRESSIONS);
    }

    @Test
    public void testDivision() {
        validateNoErrors("SELECT a / b", 1, EXPRESSIONS);
    }

    @Test
    public void testJdbcParameter() {
        validateNoErrors("SELECT ?, * FROM tab WHERE param = ?", 1,
                EXPRESSIONS.copy().add(FeaturesAllowed.JDBC));
    }

    @Test
    public void testJdbcNamedParameter() {
        validateNoErrors("SELECT func (:param1, :param2) ", 1, EXPRESSIONS.copy().add(FeaturesAllowed.JDBC));
    }

    @Test
    public void testIntegerDivision() {
        validateNoErrors("SELECT 4 DIV 2", 1, EXPRESSIONS);
    }

    @Test
    public void testModulo() {
        validateNoErrors("SELECT 3 % 2", 1, EXPRESSIONS);
    }

    @Test
    public void testMultiplication() {
        validateNoErrors("SELECT 5 * 2", 1, EXPRESSIONS);
    }

    @Test
    public void testSignedExpression() {
        validateNoErrors("SELECT 5 * -2", 1, EXPRESSIONS);
    }

    @Test
    public void testSubtraction() {
        validateNoErrors("SELECT 5 - 3", 1, EXPRESSIONS);
    }

    @Test
    public void testIsNull() {
        validateNoErrors("SELECT * FROM tab t WHERE t.col IS NULL", 1, EXPRESSIONS);
        validateNoErrors("SELECT * FROM tab t WHERE t.col IS NOT NULL", 1, EXPRESSIONS);
    }

    @Test
    public void testLike() {
        validateNoErrors("SELECT * FROM tab t WHERE t.col LIKE '%search for%'", 1, EXPRESSIONS);
        validateNoErrors("SELECT * FROM tab t WHERE t.col NOT LIKE '%search for%'", 1, EXPRESSIONS);
    }

    @Test
    public void testExists() {
        validateNoErrors("SELECT * FROM tab t WHERE EXISTS (select 1 FROM tab2 t2 WHERE t2.id = t.id)", 1,
                EXPRESSIONS);
    }

    @Test
    public void testInterval() throws JSQLParserException {
        validateNoErrors("SELECT DATE_ADD(start_date, INTERVAL duration MINUTE) AS end_datetime FROM appointment", 1,
                EXPRESSIONS);
        validateNoErrors("SELECT 5 + INTERVAL '3 days'", 1,
                EXPRESSIONS);
    }

    @Test
    public void testExtract() throws JSQLParserException {
        validateNoErrors("SELECT (EXTRACT(epoch FROM age(d1, d2)) / 2)::numeric", 1, EXPRESSIONS);
    }

    @Test
    public void testPostgreSQLRegExpCaseSensitiveMatch() throws JSQLParserException {
        validateNoErrors("SELECT a, b FROM foo WHERE a ~* '[help].*'", 1, EXPRESSIONS);
    }

    @Test
    public void testRlike() throws JSQLParserException {
        validateNoErrors("SELECT * FROM mytable WHERE first_name RLIKE '^Ste(v|ph)en$'", 1,
                EXPRESSIONS);
    }

    @Test
    public void testSimilarTo() throws JSQLParserException {
        validateNoErrors(
                "SELECT * FROM mytable WHERE (w_id NOT SIMILAR TO '/foo/__/bar/(left|right)/[0-9]{4}-[0-9]{2}-[0-9]{2}(/[0-9]*)?')",
                1, EXPRESSIONS);
    }

    @Test
    public void testOneColumnFullTextSearchMySQL() throws JSQLParserException {
        validateNoErrors("SELECT MATCH (col1) AGAINST ('test' IN NATURAL LANGUAGE MODE) relevance FROM tbl", 1,
                EXPRESSIONS);
    }

    @Test
    public void testAnalyticFunctionFilter() throws JSQLParserException {
        validateNoErrors("SELECT COUNT(*) FILTER (WHERE name = 'Raj') OVER (PARTITION BY name ) FROM table", 1,
                EXPRESSIONS);
    }


}

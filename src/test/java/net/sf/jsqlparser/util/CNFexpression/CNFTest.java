package net.sf.jsqlparser.util.CNFexpression;

import static org.junit.Assert.*;

import java.io.StringReader;

import org.junit.Test;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

/**
 * this class is mainly used for testing whether we generate the 
 * correct CNF form of an expression tree. We use the name of 
 * variables that is reflected in the steps defined in the class.
 * @author messfish
 *
 */
public class CNFTest {

    private CCJSqlParserManager parserManager = new CCJSqlParserManager();
    private Expression original1;
    private Expression original2;
    private Expression original3;
    private Expression original4;
    private Expression original5;
    // these expressions stores the original expression tree.
    private Expression steplast1;
    private Expression steplast2;
    private Expression steplast3;
    private Expression steplast4;
    private Expression steplast5;
    // these expression stores the last step of the expression tree.
    private CNFConverter cnf1;
    private CNFConverter cnf2;
    private CNFConverter cnf3;
    private CNFConverter cnf4;
    private CNFConverter cnf5;
    // generate three CNF converter objects to handle the 
    // conversion of three expression trees.
    
    /**
     * Constructor: this constructor will be served to build several
     * original construction tree and several expected trees that
     * will be used in those test cases.
     */
    public CNFTest() {
        /* Here is the original part of the expression tree. */
        buildOriginalExpression1();
        buildOriginalExpression2();
        buildOriginalExpression3();
        buildOriginalExpression4();
        buildOriginalExpression5();
        steplast1 = StepLastHelper.buildFinalStep1();
        steplast2 = StepLastHelper.buildFinalStep2();
        steplast3 = StepLastHelper.buildFinalStep3();
        steplast4 = StepLastHelper.buildFinalStep4();
        steplast5 = StepLastHelper.buildFinalStep5();
        cnf1 = new CNFConverter();
        cnf2 = new CNFConverter();
        cnf3 = new CNFConverter();
        cnf4 = new CNFConverter();
        cnf5 = new CNFConverter();
    }
    
    /**
     * this method is used to test the full progress from the original
     * expression tree to the expression tree in CNF form. In other words,
     * this is used to test the last case.
     */
    @Test
    public void testAll() {
        cnf1.convert(original1);
        assertEquals(steplast1.toString(), cnf1.getRoot().toString());
        cnf2.convert(original2);
        assertEquals(steplast2.toString(), cnf2.getRoot().toString());
        cnf3.convert(original3);
        assertEquals(steplast3.toString(), cnf3.getRoot().toString());
        cnf4.convert(original4);
        assertEquals(steplast4.toString(), cnf4.getRoot().toString());
        cnf5.convert(original5);
        assertEquals(steplast5.toString(), cnf5.getRoot().toString());
    }
    
    /**
     * this method is used to build the original expression tree 1.
     * The purpose of this method is to check when there is a Not Operator
     * at the root. Which means the root must be switched.          
     */
    private void buildOriginalExpression1() {
        String statement = "SELECT * FROM S " + 
                  "WHERE NOT ((1.2 < 2.3 OR 3.5 = 4.6) AND (1.1 <> 2.5 OR 8.0 >= 7.2))";
        try {
            Select select = (Select) parserManager.parse(new StringReader(statement));
            original1 = ((PlainSelect) select.getSelectBody()).getWhere();
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * this method is used to build the original expression tree 2.
     * The purpose is to test the double negation law. As you can 
     * see when you build the tree, there will be two Not Operators 
     * together on the line. It is there when we use the double negation law.
     */
    private void buildOriginalExpression2() {
        String statement = "SELECT * FROM S " + 
                "WHERE ((NOT (NOT 1.1 >= 2.3 OR 3.3 < 4.5)) OR "
                + "(S.A LIKE '\"%%%\"' AND S.B = '\"orz\"'))";
        try {
            Select select = (Select) parserManager.parse(new StringReader(statement));
            original2 = ((PlainSelect) select.getSelectBody()).getWhere();
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * this method is used to build the original expression tree 3.
     * This is the case when we test a more complex tree structure,
     * Notice you could see the amount of line to build up the CNF tree.
     * You could tell how complicated the CNF could be.
     */
    private void buildOriginalExpression3() {
        String statement = "SELECT * FROM S WHERE (3.0 >= 4.0 AND 5.0 <= 6.0) OR "
                + "(((7.0 < 8.0 AND 9.0 > 10.0) AND 11.0 = 12.0) OR "
                + "NOT (13.0 <> 14.0 OR (15.0 = 16.0 AND (17.0 = 18.0 OR 19.0 > 20.0))))";
        try {
            Select select = (Select) parserManager.parse(new StringReader(statement));
            original3 = ((PlainSelect) select.getSelectBody()).getWhere();
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * This method is used for building the original expression tree 4.
     * This is the case when we test a very simple tree structure that 
     * has neither AND operator or OR operator.
     */
    private void buildOriginalExpression4() {
        String statement = "SELECT * FROM S WHERE NOT S.D > {d '2017-03-25'}";
        try {
            Select select = (Select) parserManager.parse(new StringReader(statement));
            original4 = ((PlainSelect) select.getSelectBody()).getWhere();
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * This method is used for building the original expression tree 5.
     * This is the case when we test the tree that only contains AND
     * operator without having an OR operator.
     */
    private void buildOriginalExpression5() {
        String statement = "SELECT * FROM S WHERE NOT ((NOT (S.A > 3.5 AND S.B < 4)) OR "
                + "(S.C LIKE '\"%%\"' OR S.D = {t '12:04:34'}))";
        try {
            Select select = (Select) parserManager.parse(new StringReader(statement));
            original5 = ((PlainSelect) select.getSelectBody()).getWhere();
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
    }
    
}

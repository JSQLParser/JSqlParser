package net.sf.jsqlparser.statement.simpleparsing;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;

import org.junit.Test;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.create.CreateTableTest;

public class TeradataParsingTests {

    @Test
    public void testParse() throws Exception {
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        BufferedReader in = new BufferedReader(new InputStreamReader(CreateTableTest.class.getResourceAsStream("/teradata.txt")));

        String statement = "";
        while (true) {
            try {
                System.out.println();

                statement = TeradataParsingTests.getStatement(in);
                if (statement == null) {
                    break;
                }

                System.out.println(statement.toString());

                parserManager.parse(new StringReader(statement));

                System.out.println("=> SUCCESS");

            } catch (JSQLParserException e) {

                System.out.println("=> FAILURE");

                //e.printStackTrace();
                //throw new TestException("impossible to parse statement: " + statement, e);
            }
        }
    }

    public static String getStatement(BufferedReader in) throws Exception {
        StringBuilder buf = new StringBuilder();
        String line = null;
        while ((line = TeradataParsingTests.getLine(in)) != null) {

            if (line.trim().length() == 0) {
                break;
            }

            if (line.trim().startsWith(";")) {
                System.out.println(line);
                continue;
            }

            buf.append(line);
            buf.append("\n");
        }

        if (buf.length() > 0) {
            return buf.toString();
        } else {
            return null;
        }

    }

    public static String getLine(BufferedReader in) throws Exception {
        String line = null;
        while (true) {
            line = in.readLine();
            if (line != null) {
                line.trim();
                if ((line.length() < 2) || (line.length() >= 2) && !(line.charAt(0) == '/' && line.charAt(1) == '/')) {
                    break;
                }
            } else {
                break;
            }
        }

        return line;
    }
}

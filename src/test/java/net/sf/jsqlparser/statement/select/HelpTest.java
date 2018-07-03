package net.sf.jsqlparser.statement.select;

import java.io.StringReader;

import junit.framework.TestCase;
import net.sf.jsqlparser.parser.CCJSqlParserManager;

public class HelpTest extends TestCase {

    private CCJSqlParserManager parserManager = new CCJSqlParserManager();

    public HelpTest(String arg0) {
        super(arg0);
    }

    public void testHelpDatabase() throws Exception {
        final String statement = "help database schemaName";
        Help select = (Help) parserManager.parse(new StringReader(statement));
    }

    public void testHelp() throws Exception {
        final String statement = "help column schemaName.table.col1";
        Help select = (Help) parserManager.parse(new StringReader(statement));
    }

    public void testProc() throws Exception {
        final String statement = "HELP PROCEDURE udtblobset;";
        Help select = (Help) parserManager.parse(new StringReader(statement));
    }

    public void testSession() throws Exception {
        final String statement = "HELP SESSION CONSTRAINT;";
        Help select = (Help) parserManager.parse(new StringReader(statement));
    }

    public void testMacro() throws Exception {
        final String statement = "HELP MACRO blabla.blublu;";
        Help select = (Help) parserManager.parse(new StringReader(statement));
    }

    /* public void testHelpWithServerNameAndDatabaseNameAndSchemaName() throws Exception {
        final String statement = "help database [server-name\\server-instance].databaseName.schemaName.tableName";
        Help select = (Help) parserManager.parse(new StringReader(statement));
    }*/

}

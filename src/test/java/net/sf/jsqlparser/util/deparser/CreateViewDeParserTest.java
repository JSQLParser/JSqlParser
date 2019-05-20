/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.deparser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserDefaultVisitor;
import net.sf.jsqlparser.parser.CCJSqlParserTreeConstants;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.SimpleNode;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.view.CreateView;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author tw
 */
public class CreateViewDeParserTest {

    public CreateViewDeParserTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of deParse method, of class CreateViewDeParser.
     */
    @Test
    public void testUseExtrnalExpressionDeparser() throws JSQLParserException {
        StringBuilder b = new StringBuilder();
        SelectDeParser selectDeParser = new SelectDeParser();
        selectDeParser.setBuffer(b);
        ExpressionDeParser expressionDeParser = new ExpressionDeParser(selectDeParser, b) {

            @Override
            public void visit(Column tableColumn) {
                final Table table = tableColumn.getTable();
                String tableName = null;
                if (table != null) {
                    if (table.getAlias() != null) {
                        tableName = table.getAlias().getName();
                    } else {
                        tableName = table.getFullyQualifiedName();
                    }
                }
                if (tableName != null && !tableName.isEmpty()) {
                    getBuffer().append("\"").append(tableName).append("\"").append(".");
                }

                getBuffer().append("\"").append(tableColumn.getColumnName()).append("\"");
            }
        };

        selectDeParser.setExpressionVisitor(expressionDeParser);

        CreateViewDeParser instance = new CreateViewDeParser(b, selectDeParser);
        CreateView vc = (CreateView) CCJSqlParserUtil.
                parse("CREATE VIEW test AS SELECT a, b FROM mytable");
        instance.deParse(vc);

        assertEquals("CREATE VIEW test AS SELECT a, b FROM mytable", vc.toString());
        assertEquals("CREATE VIEW test AS SELECT \"a\", \"b\" FROM mytable", instance.getBuffer().
                toString());
    }

    @Test
    public void testCreateViewASTNode() throws JSQLParserException {
        String sql = "CREATE VIEW test AS SELECT a, b FROM mytable";
        final StringBuilder b = new StringBuilder(sql);
        SimpleNode node = (SimpleNode) CCJSqlParserUtil.parseAST(sql);
        node.dump("*");
        assertEquals(CCJSqlParserTreeConstants.JJTSTATEMENT, node.getId());

        node.jjtAccept(new CCJSqlParserDefaultVisitor() {
            int idxDelta = 0;

            @Override
            public Object visit(SimpleNode node, Object data) {
                if (CCJSqlParserTreeConstants.JJTCOLUMN == node.getId()) {
                    b.insert(node.jjtGetFirstToken().beginColumn - 1 + idxDelta, '"');
                    idxDelta++;
                    b.insert(node.jjtGetLastToken().endColumn + idxDelta, '"');
                    idxDelta++;
                }
                return super.visit(node, data);
            }
        }, null);

        assertEquals("CREATE VIEW test AS SELECT \"a\", \"b\" FROM mytable", b.toString());
    }
}

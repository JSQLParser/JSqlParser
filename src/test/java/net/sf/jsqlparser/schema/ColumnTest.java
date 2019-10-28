/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.schema;

import net.sf.jsqlparser.expression.Alias;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Modifier;

import static org.junit.Assert.*;

/**
 * @author tw
 */
public class ColumnTest {

    public ColumnTest() {
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

    @Test
    public void testMissingTableAlias() {
        Table myTable = new Table("myTable");
        myTable.setAlias(new Alias("tb"));
        Column myColumn = new Column(myTable, "myColumn");
        assertEquals("tb.myColumn", myColumn.toString());
    }

    @Test
    public void testColumnNotFinal() {
        Class<Column> columnClass = Column.class;
        assertFalse(
                String.format("%s should not be final", columnClass.getCanonicalName()),
                Modifier.isFinal(columnClass.getModifiers())
        );
    }


}

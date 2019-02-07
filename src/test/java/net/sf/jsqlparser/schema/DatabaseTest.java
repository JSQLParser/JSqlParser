/*
 * Copyright (C) 2016 JSQLParser.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package net.sf.jsqlparser.schema;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author schwitters
 */
public class DatabaseTest {

    public DatabaseTest() {
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
    public void testDatabaseSimple() {
        String databaseName = "db1";
        Database database = new Database(databaseName);
        assertEquals(databaseName, database.getFullyQualifiedName());
    }

    @Test
    public void testDatabaseAndServer() {
        final Server server = new Server("SERVER", "INSTANCE");
        String databaseName = "db1";
        Database database = new Database(server, databaseName);
        assertEquals("[SERVER\\INSTANCE].db1", database.getFullyQualifiedName());
        assertSame(server, database.getServer());
        assertEquals(databaseName, database.getDatabaseName());
        assertEquals("[SERVER\\INSTANCE].db1", database.toString());
    }

    @Test
    public void testNullDatabaseAndServer() {
        final Server server = new Server("SERVER", "INSTANCE");
        Database database = new Database(server, null);
        assertEquals("[SERVER\\INSTANCE].", database.getFullyQualifiedName());
        assertSame(server, database.getServer());
    }

}

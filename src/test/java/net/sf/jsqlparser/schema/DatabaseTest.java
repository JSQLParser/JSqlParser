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





import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.Test;


/**
 *
 * @author schwitters
 */
public class DatabaseTest {

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

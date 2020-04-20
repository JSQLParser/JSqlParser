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

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ServerTest {

    @Test
    public void testServerNameParsing() throws Exception {
        final String serverName = "LOCALHOST";

        final String fullServerName = String.format("[%s]", serverName);
        final Server server = new Server(fullServerName);

        assertEquals(serverName, server.getServerName());
        assertEquals(fullServerName, server.toString());
    }

    @Test
    public void testServerNameAndInstanceParsing() throws Exception {
        final String serverName = "LOCALHOST";
        final String serverInstanceName = "SQLSERVER";

        final String fullServerName = String.format("[%s\\%s]", serverName, serverInstanceName);
        final Server server = new Server(fullServerName);

        assertEquals(serverName, server.getServerName());
        assertEquals(serverInstanceName, server.getInstanceName());
        assertEquals(fullServerName, server.toString());

    }

    @Test
    public void testServerNameAndInstanceParsing2() throws Exception {
        String simpleName = "LOCALHOST";
        final Server server = new Server(simpleName);
        assertEquals(simpleName, server.getFullyQualifiedName());
    }

    @Test
    public void testServerNameAndInstanceParsingNull() throws Exception {
        final Server server = new Server(null);
        assertEquals("", server.getFullyQualifiedName());
    }

    @Test
    public void testServerNameAndInstancePassValues() throws Exception {
        final Server server = new Server("SERVER", "INSTANCE");
        assertEquals("SERVER", server.getServerName());
        assertEquals("INSTANCE", server.getInstanceName());
        assertEquals(String.format("[%s\\%s]", "SERVER", "INSTANCE"), server.getFullyQualifiedName());
    }

    @Test
    public void testServerNameNull() throws Exception {
        final Server server = new Server(null, "INSTANCE");
        assertEquals(null, server.getServerName());
        assertEquals("INSTANCE", server.getInstanceName());
        assertEquals("", server.getFullyQualifiedName());
    }

    @Test
    public void testServerNameEmpty() throws Exception {
        final Server server = new Server("", "INSTANCE");
        assertEquals("", server.getServerName());
        assertEquals("INSTANCE", server.getInstanceName());
        assertEquals("", server.getFullyQualifiedName());
    }

    @Test
    public void testInstanceNameNull() throws Exception {
        final Server server = new Server("LOCALHOST", null);
        assertEquals("LOCALHOST", server.getServerName());
        assertEquals(null, server.getInstanceName());
        assertEquals("[LOCALHOST]", server.getFullyQualifiedName());
    }

    @Test
    public void testInstanceNameEmpty() throws Exception {
        final Server server = new Server("LOCALHOST", "");
        assertEquals("LOCALHOST", server.getServerName());
        assertEquals("", server.getInstanceName());
        assertEquals("[LOCALHOST]", server.getFullyQualifiedName());
    }
}

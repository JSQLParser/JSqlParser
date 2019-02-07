package net.sf.jsqlparser.schema;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2013 JSQLParser
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
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

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

    public void testServerNameAndInstanceParsing() throws Exception {
        final String serverName = "LOCALHOST";
        final String serverInstanceName = "SQLSERVER";

        final String fullServerName = String.format("[%s\\%s]", serverName, serverInstanceName);
        final Server server = new Server(fullServerName);

        assertEquals(serverName, server.getServerName());
        assertEquals(serverInstanceName, server.getInstanceName());
        assertEquals(fullServerName, server.toString());

    }
}

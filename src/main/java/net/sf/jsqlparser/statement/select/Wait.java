/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import java.io.Serializable;

public class Wait implements Serializable {

    private long timeout;

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * Returns a String containing the WAIT clause and its timeout, where TIMEOUT is specified by
     * {@link #getTimeout()}. The returned string will null     be:<code>
     * &quot; WAIT &lt;TIMEOUT&gt;&quot;
     * </code>
     */
    @Override
    public String toString() {
        return " WAIT " + timeout;
    }

    public Wait withTimeout(long timeout) {
        this.setTimeout(timeout);
        return this;
    }
}

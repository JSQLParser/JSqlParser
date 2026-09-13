/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.benchmark;


import java.util.concurrent.ExecutorService;

public class LatestClasspathRunner implements SqlParserRunner {

    @Override
    public Object parseStatements(String sql,
            ExecutorService executorService,
            Configuration configuration) throws Exception {
        return net.sf.jsqlparser.parser.CCJSqlParserUtil.parseStatements(sql, executorService,
                configuration == null ? null : configuration.currentParserConfiguration);
    }
}


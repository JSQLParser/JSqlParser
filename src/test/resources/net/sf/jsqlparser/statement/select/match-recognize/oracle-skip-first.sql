/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
WITH events AS (SELECT 1 seq, 10 price FROM dual UNION ALL SELECT 2, 20 FROM dual UNION ALL SELECT 3, -5 FROM dual UNION ALL SELECT 4, 15 FROM dual)
SELECT * FROM events MATCH_RECOGNIZE (ORDER BY seq MEASURES COUNT(*) AS n  AFTER MATCH SKIP TO FIRST B PATTERN (A+ B)  DEFINE A AS price > 0, B AS price <= 0) ;

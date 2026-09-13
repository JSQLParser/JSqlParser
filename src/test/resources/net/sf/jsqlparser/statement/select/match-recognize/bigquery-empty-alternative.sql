/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
WITH events AS (SELECT 1 AS seq, 10 AS price UNION ALL SELECT 2, 20 UNION ALL SELECT 3, 30 UNION ALL SELECT 4, -5)
SELECT * FROM events MATCH_RECOGNIZE (ORDER BY seq MEASURES FIRST(seq) AS first_seq, LAST(seq) AS last_seq, COUNT(*) AS n  PATTERN (A|) DEFINE A AS price > 0 ) ORDER BY first_seq, last_seq;

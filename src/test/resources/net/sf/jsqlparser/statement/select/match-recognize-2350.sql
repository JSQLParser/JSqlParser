/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
WITH Operations AS (
  SELECT 1 AS OperationID, 120.00 AS Amount, 'C001' AS CatalogID,
         DATE '2025-01-03' AS OperationDate UNION ALL
  SELECT 2, 20.00, 'C001', DATE '2025-01-04' UNION ALL
  SELECT 3, 175.00, 'C001', DATE '2025-01-05' UNION ALL
  SELECT 4,  30.00, 'C001', DATE '2025-01-10' UNION ALL
  SELECT 5, 190.00, 'C001', DATE '2025-01-11' UNION ALL
  SELECT 6, 250.00, 'C001', DATE '2025-01-12'
)

SELECT *
FROM Operations
MATCH_RECOGNIZE (
  PARTITION BY CatalogID
  ORDER BY OperationDate ASC
  MEASURES
    FIRST(OperationDate) AS START_DT,
    LAST(OperationDate)  AS END_DT,
    SUM(Amount)          AS TOTAL_AMOUNT,
    COUNT(*)             AS ROW_COUNT
  AFTER MATCH SKIP  PAST LAST ROW
  PATTERN (low mid+ high+)
  DEFINE
    low AS Amount < 50,
    mid AS Amount between 100 and 200,
    high AS Amount > 200
  OPTIONS ( use_longest_match = FALSE )
)
ORDER BY CatalogID, START_DT;

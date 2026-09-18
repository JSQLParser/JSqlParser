SELECT * FROM ROWS FROM (json_to_recordset('[{"a":1}]') AS (a integer)) WITH ORDINALITY
SELECT * FROM ROWS FROM (json_to_recordset('[{"a":1,"b":"x"}]') AS (a integer, b text)) AS r
SELECT * FROM ROWS FROM (json_to_recordset('[{"a":1}]') AS (a integer), generate_series(1, 2)) WITH ORDINALITY AS r(a, n, ord)
SELECT * FROM ROWS FROM (generate_series(1, 2), jsonb_to_recordset('[{"a":1.25}]'::jsonb) AS (a numeric(10, 2))) AS r(n, amount)
SELECT * FROM ROWS FROM (json_to_record('{"a":"x"}') AS (a text COLLATE "C"))
SELECT * FROM ROWS FROM (json_to_record('{"a":"x"}') AS (a text COLLATE pg_catalog."C"))
SELECT * FROM ROWS FROM (json_to_record('{"a":[1,2]}') AS (a integer[]))
SELECT * FROM ROWS FROM (json_to_record('{"a":"2026-01-01T12:00:00Z"}') AS (a timestamp(6) with time zone))
SELECT * FROM ROWS FROM (json_to_record('{"odd name":1}') AS ("odd name" integer)) AS r("renamed")
SELECT r.* FROM (VALUES ('[{"a":1}]'::json)) AS src(payload) CROSS JOIN LATERAL ROWS FROM (json_to_recordset(src.payload) AS (a integer)) WITH ORDINALITY AS r
SELECT * FROM ROWS FROM (generate_series(1, 2), generate_series(1, 1)) AS r(a, b)
SELECT * FROM ROWS FROM (json_to_record('{"a":1}') AS (a integer), json_to_record('{"b":2}') AS (b integer))

---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
WITH REFS
AS
(
    SELECT DISTINCT
       c.constraint_name
       -- max(a.constraint_name) as constraint_name
       -- , c.constraint_name
       -- , max(r.constraint_name) as r_constraint_name
       , c.owner                     as owner
       , c.table_name                as table_name       
       -- , c.column_name          as column_name
       -- , ' => ' as "ARW"
       , r.owner           as r_owner
       , r.table_name      as r_table_name       
       -- , max(r.column_name)     as r_column_name
       -- , max(a.constraint_type)
   FROM
	sys.all_constraints a
   JOIN sys.all_cons_columns c ON (c.constraint_name = a.constraint_name AND c.owner = a.owner)
   JOIN sys.all_cons_columns r ON (r.constraint_name = a.r_constraint_name AND r.owner = a.r_owner AND r.position = c.position)
   WHERE
         a.Owner = 'SCHEMA_TEST'  -- :f1<char[101]>
--     AND a.Table_Name = 'T_MN_1' -- :f2<char[101]>
     AND a.constraint_type = 'R'
)
SELECT  -- REFS.*
	REFS.table_name
	, REFS.r_table_name
	, CONNECT_BY_ISCYCLE CYCLE
	, LEVEL -- level this is strange case, this is not a comment but a column alias
	-- , SYS_CONNECT_BY_PATH(table_name, '/') "Path"
FROM REFS
CONNECT BY NOCYCLE PRIOR r_owner = owner AND PRIOR r_table_name = table_name

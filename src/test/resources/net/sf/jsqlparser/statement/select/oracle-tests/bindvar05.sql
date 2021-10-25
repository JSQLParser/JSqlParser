---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select object_name, object_id,
 decode(status, 'INVALID', 'TRUE', 'FALSE') invalid, 
 'TRUE' runnable,
 plsql_debug
from sys.dba_objects o, dba_plsql_object_settings s
where o.owner = :schema
and s.owner = :schema
and s.name = o.object_name
and s.type = 'PACKAGE'
and object_type = 'PACKAGE'
and subobject_name is null
and object_id not in ( select purge_object from recyclebin )
and upper(object_name) in upper(:name)
 

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT object_name,object_id,decode(status,'INVALID','TRUE','FALSE')invalid,'TRUE' runnable,plsql_debug FROM sys.dba_objects o,dba_plsql_object_settings s WHERE o.owner=:schema AND s.owner=:schema AND s.name=o.object_name AND s.type='PACKAGE' AND object_type='PACKAGE' AND subobject_name IS NULL AND object_id NOT IN(SELECT purge_object FROM recyclebin)AND upper(object_name)IN upper(:name) recorded first on 25 Oct 2021, 18:46:41
--@FAILURE: select object_name,object_id,decode(status,'invalid','true','false')invalid,'true' runnable,plsql_debug from sys.dba_objects o,dba_plsql_object_settings s where o.owner=:schema and s.owner=:schema and s.name=o.object_name and s.type='package' and object_type='package' and subobject_name is null and object_id not in(select purge_object from recyclebin)and upper(object_name)in upper(:name) recorded first on 25 Oct 2021, 18:55:26
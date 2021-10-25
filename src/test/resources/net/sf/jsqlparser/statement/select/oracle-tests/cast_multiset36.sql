---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select owner
     ,      object_type
     ,      set(
               cast(
                  collect(object_name)
                     as varchar2_ntt)) as object_names
     from   all_objects
     group  by
            owner
     ,      object_type

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT owner,object_type,set(CAST(collect(object_name)AS varchar2_ntt))AS object_names FROM all_objects GROUP BY owner,object_type recorded first on 25 Oct 2021, 18:46:41
--@FAILURE: select owner,object_type,set(cast(collect(object_name)as varchar2_ntt))as object_names from all_objects group by owner,object_type recorded first on 25 Oct 2021, 18:55:26
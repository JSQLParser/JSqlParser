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
     ,      cast(
               collect(distinct object_name)
                  as varchar2_ntt) as object_names
     from   all_objects
     group  by
            owner
     ,      object_type

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: select owner,object_type,cast(collect(distinct object_name)as varchar2_ntt)as object_names from all_objects group by recorded first on 1 May 2023, 23:34:56
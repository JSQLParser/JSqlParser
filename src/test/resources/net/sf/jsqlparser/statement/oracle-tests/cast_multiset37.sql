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
               collect(object_name)
                  as varchar2_ntt
               ) multiset union distinct varchar2_ntt() as object_names
     from   all_objects
     group  by
            owner
     ,      object_type

--@FAILURE: Encountered unexpected token: "varchar2_ntt" <S_IDENTIFIER> recorded first on Jul 21, 2021 9:47:13 AM
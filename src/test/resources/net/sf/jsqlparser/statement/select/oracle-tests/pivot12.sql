---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
 select value from
 (
    (
        select
            'a' v1,
            'e' v2,
            'i' v3,
            'o' v4,
            'u' v5
        from dual
    )
    unpivot include nulls
    (
        value
        for value_type in
            (v1, v2,v3,v4,v5) -- Also can give ANY here.
    )
 )

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT value FROM((SELECT 'a' v1,'e' v2,'i' v3,'o' v4,'u' v5 FROM dual)UNPIVOT INCLUDE NULLS(value FOR value_type IN(v1,v2,v3,v4,v5))) recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: select value from((select 'a' v1,'e' v2,'i' v3,'o' v4,'u' v5 from dual)unpivot include nulls(value for value_type in(v1,v2,v3,v4,v5))) recorded first on 25 Oct 2021, 18:55:27
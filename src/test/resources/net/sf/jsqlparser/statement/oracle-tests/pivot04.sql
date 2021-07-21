---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select value
from
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
 unpivot
 (
 value
 for value_type in
 (v1,v2,v3,v4,v5)
 )
 )


--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Jul 21, 2021 9:47:12 AM
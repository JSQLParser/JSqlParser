---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select *
from hdr a
inner join sh s
inner join ca c
on c.id = s.id
on a.va = s.va


--@FAILURE: Encountered unexpected token: "on" "ON" recorded first on Jul 21, 2021 9:47:13 AM
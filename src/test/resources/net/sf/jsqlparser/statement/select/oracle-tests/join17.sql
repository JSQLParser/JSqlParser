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

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 14, 2021 9:00:57 PM
--@FAILURE: SELECT*FROM hdr a INNER JOIN sh s INNER JOIN ca c ON c.id=s.id ON a.va=s.va recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: select*from hdr a inner join sh s inner join ca c on c.id=s.id on a.va=s.va recorded first on 25 Oct 2021, 18:55:26
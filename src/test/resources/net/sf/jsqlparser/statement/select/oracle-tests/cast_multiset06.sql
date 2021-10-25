---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select
	cast(collect(cattr(aname, op, to_char(val), support, confidence)) as cattrs) cl_attrs
from a


--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT CAST(collect(cattr(aname,op,to_char(val),support,confidence))AS cattrs)cl_attrs FROM a recorded first on 25 Oct 2021, 18:46:41
--@FAILURE: select cast(collect(cattr(aname,op,to_char(val),support,confidence))as cattrs)cl_attrs from a recorded first on 25 Oct 2021, 18:55:26
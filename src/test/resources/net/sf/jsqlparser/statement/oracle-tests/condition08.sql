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
from "p"
where
-- note there are no parens around 231092	 
( ( "p"."id" in 231092 ) )    

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Jul 21, 2021 9:47:13 AM
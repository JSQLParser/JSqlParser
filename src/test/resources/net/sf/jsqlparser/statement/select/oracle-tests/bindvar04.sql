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
from 
(
    select *
    from "rme" "rm" 
    where "rm".a-interval:"sys_b_07" day(:"sys_b_08") to second(:"sys_b_09")
)

--@FAILURE: Encountered unexpected token: "(" "(" recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: net.sf.jsqlparser.parser.ParseException: Encountered unexpected token: "(" "(" recorded first on 21 Dec 2021, 15:15:16
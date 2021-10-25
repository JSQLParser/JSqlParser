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
from append
where
-- note space between '>' and '='
(length(w.numer) > = 8)

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT*FROM append WHERE(length(w.numer)>=8) recorded first on 25 Oct 2021, 18:46:41
--@FAILURE: select*from append where(length(w.numer)>=8) recorded first on 25 Oct 2021, 18:55:26
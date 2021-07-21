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
  sum(nvl(pl.qty,0)) 
  from 
  oline ol, 
  pline pl,
  blocation bl
  where 
  ol.id = pl.id
  and pl.no = pl.no
  and bl.id = pl.id
  and
  (
  	(select count(*) from la.sa where pl.id like sa.bid) > 0
	or
	(select count(*) from la.sa where bl.id like sa.id) > 0
  )

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Jul 21, 2021 9:47:13 AM
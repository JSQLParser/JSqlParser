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

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT sum(nvl(pl.qty,0))FROM oline ol,pline pl,blocation bl WHERE ol.id=pl.id AND pl.no=pl.no AND bl.id=pl.id AND((SELECT count(*)FROM la.sa WHERE pl.id LIKE sa.bid)>0 OR(SELECT count(*)FROM la.sa WHERE bl.id LIKE sa.id)>0) recorded first on 25 Oct 2021, 18:46:41
--@FAILURE: select sum(nvl(pl.qty,0))from oline ol,pline pl,blocation bl where ol.id=pl.id and pl.no=pl.no and bl.id=pl.id and((select count(*)from la.sa where pl.id like sa.bid)>0 or(select count(*)from la.sa where bl.id like sa.id)>0) recorded first on 25 Oct 2021, 18:55:26
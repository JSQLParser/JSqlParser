---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
with liste as (
  select substr(:liste, instr(','||:liste||',', ',', 1, rn),
  instr(','||:liste||',', ',', 1, rn+1) -
  instr(','||:liste||',', ',', 1, rn)-1) valeur
from (
  select rownum rn from dual
  connect by level<=length(:liste) - length(replace(:liste,',',''))+1))
select trim(valeur)
from liste

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:07 AM
--@FAILURE: WITH liste AS(SELECT substr(:liste,instr(','||:liste||',',',',1,rn),instr(','||:liste||',',',',1,rn+1)-instr(','||:liste||',',',',1,rn)-1)valeur FROM(SELECT rownum rn FROM dual CONNECT BY level<=length(:liste)-length(replace(:liste,',',''))+1))SELECT trim(valeur)FROM liste recorded first on 25 Oct 2021, 18:46:41
--@FAILURE: with liste as(select substr(:liste,instr(','||:liste||',',',',1,rn),instr(','||:liste||',',',',1,rn+1)-instr(','||:liste||',',',',1,rn)-1)valeur from(select rownum rn from dual connect by level<=length(:liste)-length(replace(:liste,',',''))+1))select trim(valeur)from liste recorded first on 25 Oct 2021, 18:55:26
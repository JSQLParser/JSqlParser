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

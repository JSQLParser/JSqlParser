---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select distinct X
from X,Y,Z
where
    X.id = Z.id (+) 
and nvl(X.cid, '^') = nvl(Y.clientid (+), '^') 
and 0 = Lib.SKU(X.sid, nvl(Z.cid, '^')) 


--@FAILURE: Encountered unexpected token: "(" "(" recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: select distinct x from x,y,z where x.id=z.id(+)and nvl(x.cid,'^')=nvl recorded first on 23 Apr 2022, 16:44:21
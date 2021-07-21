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


--@FAILURE: Encountered unexpected token: "(" "(" recorded first on Jul 21, 2021 9:47:13 AM
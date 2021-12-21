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
--@FAILURE: net.sf.jsqlparser.parser.ParseException: Encountered unexpected token: "(" "(" recorded first on 21 Dec 2021, 15:15:16
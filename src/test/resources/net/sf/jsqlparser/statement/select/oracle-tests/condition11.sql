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
--@FAILURE: Encountered: "(" / "(", at line 14, column 26, in lexical state DEFAULT. recorded first on 15 May 2025, 16:24:08
--@FAILURE: Encountered: <OPENING_BRACKET> / "(", at line 14, column 26, in lexical state DEFAULT. recorded first on 9 Jul 2025, 17:09:17
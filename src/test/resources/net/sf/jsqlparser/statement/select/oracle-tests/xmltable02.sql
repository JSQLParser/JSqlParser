---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select xmlelement("other_attrs", xmlelement("parsing_user_id", parsing_user_id)).getClobVal() other
from f

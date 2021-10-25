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

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT xmlelement("other_attrs",xmlelement("parsing_user_id",parsing_user_id)).getClobVal()other FROM f recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: select xmlelement("other_attrs",xmlelement("parsing_user_id",parsing_user_id)).getclobval()other from f recorded first on 25 Oct 2021, 18:55:27
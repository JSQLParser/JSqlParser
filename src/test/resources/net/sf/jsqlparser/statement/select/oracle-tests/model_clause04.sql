---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select country, year, sale, csum
   from
   (select country, year, salex sale
    from sales_view_ref
    group by country, year
   )
   model dimension by (country, year)
         measures (sale, 0 csum)
         rules
            (
              s['standard mouse'] = 2
            )
   order by country, year

 

--@FAILURE: Encountered unexpected token: "dimension" <S_IDENTIFIER> recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: Encountered: <S_IDENTIFIER> / "dimension", at line 16, column 10, in lexical state DEFAULT. recorded first on 15 May 2025, 16:24:08
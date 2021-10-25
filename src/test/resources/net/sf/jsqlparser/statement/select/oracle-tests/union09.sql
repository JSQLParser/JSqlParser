---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select * from (
        select row_.*
        from (
                select *
                        from
                        (
                                select results.*, 1 rn
                                from
                                (
                                        (
                                                select  dummy
                                                from    dual
                                                where 1=1
                                        )
                                        union
                                        (
                                                select dummy
                                                from   dual
                                                where 1=1
                                        )
                                ) results
                        )
                        where rn = 1 order by dummy desc
               )row_ 
               where rownum <= 1
        )
where rownum >= 1

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT*FROM(SELECT row_.*FROM(SELECT*FROM(SELECT results.*,1 rn FROM((SELECT dummy FROM dual WHERE 1=1)UNION(SELECT dummy FROM dual WHERE 1=1))results)WHERE rn=1 ORDER BY dummy DESC)row_ WHERE rownum<=1)WHERE rownum>=1 recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: select*from(select row_.*from(select*from(select results.*,1 rn from((select dummy from dual where 1=1)union(select dummy from dual where 1=1))results)where rn=1 order by dummy desc)row_ where rownum<=1)where rownum>=1 recorded first on 25 Oct 2021, 18:55:27
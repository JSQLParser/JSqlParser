---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
((  select "x"."r_no",   
          "x"."i_id",   
	  "x"."ind",	
	  "x"."item",   
	  '0' "o"
   from "x"        
   where ("x"."r_no" = :a))
   minus
   (select "y"."r_no",   
          "y"."i_id",
 	  "y"."ind",	   
 	  "y"."item",   
 	  '0' "o"  
   from "y"
   where ("y"."r_no" = :a))
)
union
(  ( select "y"."r_no",   
          "y"."i_id",   
 	  "y"."ind",	
 	  "y"."item",   
 	  '1' "o"  
   from "y"
   where ("y"."r_no" = :a))
   minus
   (select "x"."r_no",   
          "x"."i_id",
          "x"."ind",   
          "x"."item",   
	  '1' "o"  
    from "x"
    where ("x"."r_no" = :a))
)
order by 4,3,1


--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: ((SELECT "x"."r_no","x"."i_id","x"."ind","x"."item",'0' "o" FROM "x" WHERE("x"."r_no"=:a))MINUS(SELECT "y"."r_no","y"."i_id","y"."ind","y"."item",'0' "o" FROM "y" WHERE("y"."r_no"=:a)))UNION((SELECT "y"."r_no","y"."i_id","y"."ind","y"."item",'1' "o" FROM "y" WHERE("y"."r_no"=:a))MINUS(SELECT "x"."r_no","x"."i_id","x"."ind","x"."item",'1' "o" FROM "x" WHERE("x"."r_no"=:a)))ORDER BY 4,3,1 recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: ((select "x"."r_no","x"."i_id","x"."ind","x"."item",'0' "o" from "x" where("x"."r_no"=:a))minus(select "y"."r_no","y"."i_id","y"."ind","y"."item",'0' "o" from "y" where("y"."r_no"=:a)))union((select "y"."r_no","y"."i_id","y"."ind","y"."item",'1' "o" from "y" where("y"."r_no"=:a))minus(select "x"."r_no","x"."i_id","x"."ind","x"."item",'1' "o" from "x" where("x"."r_no"=:a)))order by 4,3,1 recorded first on 25 Oct 2021, 18:55:27
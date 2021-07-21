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


--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Jul 21, 2021 9:47:13 AM
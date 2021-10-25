---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select *
from t
where
 ( t.type = '2' ) or ( t.type = '3' )
 and  t.cde < 20 
 and  t.se = 'xxx' 
 and  t.id = '000000000002' 
 and ( ( t.sku_attr_1 is null ) or ( t.sku_attr_1 = '*' ) )
 and ( ( t.sku_attr_2 is null ) or ( t.sku_attr_2 = '*' ) )
 and ( ( t.sku_attr_3 is null ) or ( t.sku_attr_3 = '*' ) )
 and ( ( t.sku_attr_4 is null ) or ( t.sku_attr_4 = '*' ) )
 and ( ( t.sku_attr_5 is null ) or ( t.sku_attr_5 = '*' ) )
 and ( ( t.itype is null ) or ( t.itype = '*' ) )
 and ( ( t.bnbr is null ) or ( t.bnbr = '*' ) )
 and ( ( t.stat = '01' ) or ( t.stat = '*' ) )
 and ( ( t.orgn is null ) or ( t.orgn = '*' ) )
 and ( t.mbr = '0000000000001' )
 and ( t.nbr is null )



--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT*FROM t WHERE(t.type='2')OR(t.type='3')AND t.cde<20 AND t.se='xxx' AND t.id='000000000002' AND((t.sku_attr_1 IS NULL)OR(t.sku_attr_1='*'))AND((t.sku_attr_2 IS NULL)OR(t.sku_attr_2='*'))AND((t.sku_attr_3 IS NULL)OR(t.sku_attr_3='*'))AND((t.sku_attr_4 IS NULL)OR(t.sku_attr_4='*'))AND((t.sku_attr_5 IS NULL)OR(t.sku_attr_5='*'))AND((t.itype IS NULL)OR(t.itype='*'))AND((t.bnbr IS NULL)OR(t.bnbr='*'))AND((t.stat='01')OR(t.stat='*'))AND((t.orgn IS NULL)OR(t.orgn='*'))AND(t.mbr='0000000000001')AND(t.nbr IS NULL) recorded first on 25 Oct 2021, 18:46:41
--@FAILURE: select*from t where(t.type='2')or(t.type='3')and t.cde<20 and t.se='xxx' and t.id='000000000002' and((t.sku_attr_1 is null)or(t.sku_attr_1='*'))and((t.sku_attr_2 is null)or(t.sku_attr_2='*'))and((t.sku_attr_3 is null)or(t.sku_attr_3='*'))and((t.sku_attr_4 is null)or(t.sku_attr_4='*'))and((t.sku_attr_5 is null)or(t.sku_attr_5='*'))and((t.itype is null)or(t.itype='*'))and((t.bnbr is null)or(t.bnbr='*'))and((t.stat='01')or(t.stat='*'))and((t.orgn is null)or(t.orgn='*'))and(t.mbr='0000000000001')and(t.nbr is null) recorded first on 25 Oct 2021, 18:55:26
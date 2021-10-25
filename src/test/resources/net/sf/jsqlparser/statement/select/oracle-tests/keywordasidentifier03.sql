---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select ind.index_owner,ind.index_name,ind.uniqueness
 , ind.status,ind.index_type,ind.temporary, ind.partitioned,ind.funcidx_status
 , ind.join_index,ind.columns,ie.column_expression
 , ind.index_name sdev_link_name,'INDEX' sdev_link_type, ind.index_owner sdev_link_owner     
from 
(
   select index_owner,table_owner,index_name,uniqueness, status,index_type,temporary, partitioned,funcidx_status, join_index
   , max(decode(position,1 ,column_name))|| max(decode(position,2 ,', '||column_name))|| max(decode(position,3 ,', '||column_name))|| max(decode(position,4 ,', '||column_name))|| max(decode(position,5 ,', '||column_name))||           max(decode(position,6 ,', '||column_name))||           max(decode(position,7 ,', '||column_name))||           max(decode(position,8 ,', '||column_name))||           max(decode(position,9 ,', '||column_name))||           max(decode(position,10,', '||column_name)) columns 

  from
  (
     select di.owner index_owner,dc.table_owner,dc.index_name,di.uniqueness, di.status, di.index_type, di.temporary, di.partitioned,di.funcidx_status, di.join_index
     , dc.column_name,dc.column_position position          
     from all_ind_columns dc,all_indexes di         
     where dc.table_owner = :OBJECT_OWNER           
     and dc.table_name  =  :OBJECT_NAME           
     and dc.index_name = di.index_name           
     and dc.index_owner = di.owner 
  )    
  group by index_owner,table_owner,index_name,uniqueness, status, index_type, temporary, partitioned,funcidx_status, join_index
) ind,
ALL_IND_EXPRESSIONS ie  
where ind.index_name = ie.index_name(+)
and ind.index_owner = ie.index_owner(+)

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT ind.index_owner,ind.index_name,ind.uniqueness,ind.status,ind.index_type,ind.temporary,ind.partitioned,ind.funcidx_status,ind.join_index,ind.columns,ie.column_expression,ind.index_name sdev_link_name,'INDEX' sdev_link_type,ind.index_owner sdev_link_owner FROM(SELECT index_owner,table_owner,index_name,uniqueness,status,index_type,temporary,partitioned,funcidx_status,join_index,max(decode(position,1,column_name))||max(decode(position,2,','||column_name))||max(decode(position,3,','||column_name))||max(decode(position,4,','||column_name))||max(decode(position,5,','||column_name))||max(decode(position,6,','||column_name))||max(decode(position,7,','||column_name))||max(decode(position,8,','||column_name))||max(decode(position,9,','||column_name))||max(decode(position,10,','||column_name))columns FROM(SELECT di.owner index_owner,dc.table_owner,dc.index_name,di.uniqueness,di.status,di.index_type,di.temporary,di.partitioned,di.funcidx_status,di.join_index,dc.column_name,dc.column_position position FROM all_ind_columns dc,all_indexes di WHERE dc.table_owner=:OBJECT_OWNER AND dc.table_name=:OBJECT_NAME AND dc.index_name=di.index_name AND dc.index_owner=di.owner)GROUP BY index_owner,table_owner,index_name,uniqueness,status,index_type,temporary,partitioned,funcidx_status,join_index)ind,ALL_IND_EXPRESSIONS ie WHERE ind.index_name=ie.index_name(+)AND ind.index_owner=ie.index_owner(+) recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: select ind.index_owner,ind.index_name,ind.uniqueness,ind.status,ind.index_type,ind.temporary,ind.partitioned,ind.funcidx_status,ind.join_index,ind.columns,ie.column_expression,ind.index_name sdev_link_name,'index' sdev_link_type,ind.index_owner sdev_link_owner from(select index_owner,table_owner,index_name,uniqueness,status,index_type,temporary,partitioned,funcidx_status,join_index,max(decode(position,1,column_name))||max(decode(position,2,','||column_name))||max(decode(position,3,','||column_name))||max(decode(position,4,','||column_name))||max(decode(position,5,','||column_name))||max(decode(position,6,','||column_name))||max(decode(position,7,','||column_name))||max(decode(position,8,','||column_name))||max(decode(position,9,','||column_name))||max(decode(position,10,','||column_name))columns from(select di.owner index_owner,dc.table_owner,dc.index_name,di.uniqueness,di.status,di.index_type,di.temporary,di.partitioned,di.funcidx_status,di.join_index,dc.column_name,dc.column_position position from all_ind_columns dc,all_indexes di where dc.table_owner=:object_owner and dc.table_name=:object_name and dc.index_name=di.index_name and dc.index_owner=di.owner)group by index_owner,table_owner,index_name,uniqueness,status,index_type,temporary,partitioned,funcidx_status,join_index)ind,all_ind_expressions ie where ind.index_name=ie.index_name(+)and ind.index_owner=ie.index_owner(+) recorded first on 25 Oct 2021, 18:55:27
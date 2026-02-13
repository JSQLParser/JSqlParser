---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
with
clus_tab as (
select id,
       a.attribute_name aname,
       a.conditional_operator op,
       nvl(a.attribute_str_value, round(a.attribute_num_value),4) val,
       a.attribute_support support,
       a.attribute_confidence confidence
  from table(dbms_data_mining.get_model_details_km('km_sh_clus_sample')) t,
       table(t.rule.antecedent) a
 where a.attribute_confidence > 0.55
),
clust as (
select id,
       cast(collect(cattr(aname, op, to_char(val), support, confidence))
         as cattrs) cl_attrs
  from clus_tab
group by id
),
custclus as (
select t.cust_id, s.cluster_id, s.probability
  from (select cust_id, cluster_set(km_sh_clus_sample, null, 0.2 using *) pset
          from mining_data_apply_v
         where cust_id = 101362) t,
       table(t.pset) s
)
select a.probability prob, a.cluster_id cl_id,
       b.attr, b.op, b.val, b.supp, b.conf
  from custclus a,
       (select t.id, c.*
          from clust t,
               table(t.cl_attrs) c) b
 where a.cluster_id = b.id
order by prob desc, cl_id asc, conf desc, attr asc, val asc

--@FAILURE: Encountered unexpected token: "(" "(" recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: Encountered: "(" / "(", at line 31, column 36, in lexical state DEFAULT. recorded first on 15 May 2025, 16:24:08
--@FAILURE: Encountered: <OPENING_BRACKET> / "(", at line 31, column 36, in lexical state DEFAULT. recorded first on 9 Jul 2025, 17:09:17
--@FAILURE: Encountered: <OPENING_BRACKET> / "(", at line 18, column 13, in lexical state DEFAULT. recorded first on 13 Feb 2026, 12:31:35
--@SUCCESSFULLY_PARSED_AND_DEPARSED first on 13 Feb 2026, 12:40:35
--@FAILURE: Encountered: <K_USING> / "using", at line 31, column 66, in lexical state DEFAULT. recorded first on 13 Feb 2026, 12:47:04
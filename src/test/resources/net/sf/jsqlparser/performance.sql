-- complex-lateral-select-request.txt
SELECT
O.ORDERID,
O.CUSTNAME,
OL.LINETOTAL,
OC.ORDCHGTOTAL,
OT.TAXTOTAL
FROM
ORDERS O,
LATERAL (
SELECT
SUM(NETAMT) AS LINETOTAL
FROM
ORDERLINES LINES
WHERE
LINES.ORDERID=O.ORDERID
) AS OL,
LATERAL (
SELECT
SUM(CHGAMT) AS ORDCHGTOTAL
FROM
ORDERCHARGES CHARGES
WHERE
LINES.ORDERID=O.ORDERID
) AS OC,
LATERAL (
SELECT
SUM(TAXAMT) AS TAXTOTAL
FROM
ORDERTAXES TAXES
WHERE
TAXES.ORDERID=O.ORDERID
) AS OT
;

-- large-sql-issue-235.txt
SELECT
        'CR' AS `^CR`,
        (1 - (SUM((CASE
            WHEN (`tbl`.`AS` = 'Cancelled') THEN (CASE
                WHEN (ROUND((((((period_diff(date_format(`tbl`.`CD`,
                '%Y%m'),
                date_format(SUBTIME(CURRENT_TIMESTAMP(),
                25200),
                '%Y%m')) + month(SUBTIME(CURRENT_TIMESTAMP(),
                25200))) - MONTH('2012-02-01')) - 1) / 3) - ROUND((((month(SUBTIME(CURRENT_TIMESTAMP(),
                25200)) - MONTH('2012-02-01')) - 1) / 3)))) = -3) THEN 1
                ELSE 0
            END)
            ELSE 0
        END)) / SUM((CASE
            WHEN (`tbl`.`AS` = 'Active') THEN (CASE
                WHEN (ROUND((((((period_diff(date_format(`tbl`.`OCD`,
                '%Y%m'),
                date_format(SUBTIME(CURRENT_TIMESTAMP(),
                25200),
                '%Y%m')) + month(SUBTIME(CURRENT_TIMESTAMP(),
                25200))) - MONTH('2012-02-01')) - 1) / 3) - ROUND((((month(SUBTIME(CURRENT_TIMESTAMP(),
                25200)) - MONTH('2012-02-01')) - 1) / 3)))) <= -3) THEN 1
                ELSE 0
            END)
            ELSE 0
        END)))) AS `^P3Q TRR`,
        (1 - (SUM((CASE
            WHEN (`tbl`.`AS` = 'Cancelled') THEN (CASE
                WHEN (ROUND((((((period_diff(date_format(`tbl`.`CD`,
                '%Y%m'),
                date_format(SUBTIME(CURRENT_TIMESTAMP(),
                25200),
                '%Y%m')) + month(SUBTIME(CURRENT_TIMESTAMP(),
                25200))) - MONTH('2012-02-01')) - 1) / 3) - ROUND((((month(SUBTIME(CURRENT_TIMESTAMP(),
                25200)) - MONTH('2012-02-01')) - 1) / 3)))) = -2) THEN 1
                ELSE 0
            END)
            ELSE 0
        END)) / SUM((CASE
            WHEN (`tbl`.`AS` = 'Active') THEN (CASE
                WHEN (ROUND((((((period_diff(date_format(`tbl`.`OCD`,
                '%Y%m'),
                date_format(SUBTIME(CURRENT_TIMESTAMP(),
                25200),
                '%Y%m')) + month(SUBTIME(CURRENT_TIMESTAMP(),
                25200))) - MONTH('2012-02-01')) - 1) / 3) - ROUND((((month(SUBTIME(CURRENT_TIMESTAMP(),
                25200)) - MONTH('2012-02-01')) - 1) / 3)))) <= -2) THEN 1
                ELSE 0
            END)
            ELSE 0
        END)))) AS `^P2Q TRR`,
        (1 - (SUM((CASE
            WHEN (`tbl`.`AS` = 'Cancelled') THEN (CASE
                WHEN (ROUND((((((period_diff(date_format(`tbl`.`CD`,
                '%Y%m'),
                date_format(SUBTIME(CURRENT_TIMESTAMP(),
                25200),
                '%Y%m')) + month(SUBTIME(CURRENT_TIMESTAMP(),
                25200))) - MONTH('2012-02-01')) - 1) / 3) - ROUND((((month(SUBTIME(CURRENT_TIMESTAMP(),
                25200)) - MONTH('2012-02-01')) - 1) / 3)))) = -1) THEN 1
                ELSE 0
            END)
            ELSE 0
        END)) / SUM((CASE
            WHEN (`tbl`.`AS` = 'Active') THEN (CASE
                WHEN (ROUND((((((period_diff(date_format(`tbl`.`OCD`,
                '%Y%m'),
                date_format(SUBTIME(CURRENT_TIMESTAMP(),
                25200),
                '%Y%m')) + month(SUBTIME(CURRENT_TIMESTAMP(),
                25200))) - MONTH('2012-02-01')) - 1) / 3) - ROUND((((month(SUBTIME(CURRENT_TIMESTAMP(),
                25200)) - MONTH('2012-02-01')) - 1) / 3)))) <= -1) THEN 1
                ELSE 0
            END)
            ELSE 0
        END)))) AS `^PQ TRR`,
        (1 - (SUM((CASE
            WHEN ((ROUND((((((period_diff(date_format(`tbl`.`CD`,
            '%Y%m'),
            date_format(SUBTIME(CURRENT_TIMESTAMP(),
            25200),
            '%Y%m')) + month(SUBTIME(CURRENT_TIMESTAMP(),
            25200))) - MONTH('2012-02-01')) - 1) / 3) - ROUND((((month(SUBTIME(CURRENT_TIMESTAMP(),
            25200)) - MONTH('2012-02-01')) - 1) / 3)))) = 0)
            AND (`tbl`.`AS` = 'Cancelled')) THEN 1
            ELSE 0
        END)) / SUM((CASE
            WHEN (`tbl`.`AS` = 'Active') THEN 1
            ELSE 0
        END)))) AS `^CQ TRR`
    FROM
        `tbl`
    GROUP BY
        'CR' LIMIT 25000
;

-- large-sql-issue-566.txt
SELECT "CAMPAIGNID","TARGET_SOURCE","NON_INDV_TYPE","GROUP_SPONSOR","SEGMNTS","COUNTRY_CD","TARGET_STATE","TARGET_CITY","TARGET_ZIP","SIC_CLASS","NAICS_CLASS","GENDER_CD","OCCUPATION","CREDIT_SCORE","MARITAL_STATUS","IMPORT_ID","BIRTH_DT","STATUS"
             FROM (
               SELECT
                 X.CAMPAIGNID,
                 X.FIELDNAME,
                 CASE WHEN Y.VALUE IS NULL THEN 'ALL'
                 ELSE Y.VALUE END AS VALUE
               FROM
            --CREATES A CARTESIAN JOIN TO COMBINE ALL CHARACTERISTICS WITH CAMPAIGN
                 (SELECT
                    CAMPAIGNID,
                    FIELDNAME
                  FROM CAMPAIGN
                    CROSS JOIN (SELECT DISTINCT FIELDNAME
                                FROM FIELDCRITERIA)) X
                 LEFT JOIN
            --RETURNS ALL AVAILABLE CAMPAIGN CHARACTERISTS
                 (
                   SELECT
                     CAMPAIGNID,
                     FIELDNAME,
                     (CASE FIELDNAME
                      WHEN U'BUSINESSTYPE' THEN D.DISPLAYVALUE
                      WHEN U'LEADTARGETSOURCE' THEN E.DISPLAYVALUE
                      ELSE VALUE END) AS VALUE
                   FROM FIELDCRITERIA A, STRINGFIELDCRITERIA_VALUE B
                     LEFT JOIN (SELECT
                                  B.CODE,
                                  B.DISPLAYVALUE,
                                  LOOKUPNAME
                                FROM LOOKUPLIST A, LOOKUPVALUE B
                                WHERE A.ID = B.LOOKUPLIST_ID AND LOOKUPNAME = 'NONINDIVIDUALTYPE') D ON B.VALUE = D.CODE
                     LEFT JOIN (SELECT
                                  B.CODE,
                                  B.DISPLAYVALUE,
                                  LOOKUPNAME
                                FROM LOOKUPLIST A, LOOKUPVALUE B
                                WHERE A.ID = B.LOOKUPLIST_ID AND LOOKUPNAME = 'LEADTARGETSOURCE') E ON B.VALUE = E.CODE
                     ,
                     CAMPAIGN C
                   WHERE A.ID = B.FIELD_CRITERIA_ID
                         AND A.CRITERIA_ID = C.ID
                 ) Y ON X.CAMPAIGNID = Y.CAMPAIGNID AND X.FIELDNAME = Y.FIELDNAME
             )
             PIVOT (MAX(VALUE)
                FOR FIELDNAME
                IN
                   ('LEADTARGETSOURCE' AS TARGET_SOURCE, 'BUSINESSTYPE' AS NON_INDV_TYPE, 'GROUPSPONSOR' AS GROUP_SPONSOR, 'SEGMENTS' AS SEGMNTS, 'COUNTRYCD' AS COUNTRY_CD, 'STATEPROVCD' AS TARGET_STATE,
                   'CITY' AS TARGET_CITY, 'POSTALCODE' AS TARGET_ZIP, 'SICCLASSIFICATION' AS SIC_CLASS, 'NAICSCLASSIFICATION' AS NAICS_CLASS, 'GENDERCD' AS GENDER_CD, 'OCCUPATION' AS OCCUPATION, 'CREDITSCORE' AS CREDIT_SCORE,
                   'MARITALSTATUSCD' AS MARITAL_STATUS, 'IMPORTID' AS IMPORT_ID, 'BIRTHDATE' AS BIRTH_DT, 'STATUS' AS STATUS))
;

-- large-sql-issue-923.txt
SELECT DISTINCT
  CLM_MAP_1.INTERNAL_ID,
  CLM.STATUS_C,
  nvl(LOBCL.LOB_NAME,'UNKNOWN'),
  EPPCL.PRODUCT_TYPE,
  CLM.SERVICE_START_DATE,
  CLM.SERVICE_END_DATE,
  CLM.DATE_RECEIVED,
  CLM_CS.NAME,
  CLM.STATUS_DATE,
  CLM_APSTS.ABBR,
  CLM_CF.NAME,
  case when D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLHI%' then 'HI'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNW%' then 'NW'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLMA%' then 'MAS'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLSC%' then 'SC'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNC%' then 'NC'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLCO%' then 'CO'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLGA%' then 'GA'
else 'Contact BO Developers' end,
  GRP.PLAN_GRP_NAME,
  SERREN_2.NPI,
  SERREN.PROV_NAME,
  D_VTN.TAX_ID,
  VENCLM.VENDOR_NAME,
  (CLM.TOT_BILLED_AMT),
  (CLM.TOT_NET_PAYABLE),
  CASE
	WHEN CLM.AP_STS_C=3 THEN CKR.AP_RUN_DATE
END,
  CASE
WHEN ( case when D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLHI%' then 'HI'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNW%' then 'NW'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLMA%' then 'MAS'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLSC%' then 'SC'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNC%' then 'NC'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLCO%' then 'CO'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLGA%' then 'GA'
else 'Contact BO Developers' end ) IN ('NC','SC') AND
CLM_TRAIT_2.ABBR='CAP'
THEN 'Internal'
WHEN ( case when D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLHI%' then 'HI'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNW%' then 'NW'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLMA%' then 'MAS'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLSC%' then 'SC'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNC%' then 'NC'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLCO%' then 'CO'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLGA%' then 'GA'
else 'Contact BO Developers' end ) IN ('NW')  AND
 (UPPER(VENCLM.VENDOR_NAME) LIKE '%INTRGNL AN%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE  'ANTHEM FOUNDATION%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM SUNNYSIDE%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM WESTSIDE%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANNW REGIONAL LAB%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM CLINIC%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'CARE ESSENTIALS BY ANTHEM NW%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM HOSPITALS%')
THEN 'Internal'
WHEN ( case when D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLHI%' then 'HI'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNW%' then 'NW'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLMA%' then 'MAS'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLSC%' then 'SC'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNC%' then 'NC'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLCO%' then 'CO'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLGA%' then 'GA'
else 'Contact BO Developers' end ) = 'HI' AND
(UPPER(VENCLM.VENDOR_NAME) LIKE '%INTRGNL AN%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE '%HAWAII PERMANENTE MEDICAL GROUP'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM%HOSPITAL%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM FOUNDATION HEALTH PLAN INC%')
THEN 'Internal'
WHEN ( case when D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLHI%' then 'HI'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNW%' then 'NW'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLMA%' then 'MAS'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLSC%' then 'SC'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNC%' then 'NC'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLCO%' then 'CO'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLGA%' then 'GA'
else 'Contact BO Developers' end ) = 'CO' AND
(UPPER(VENCLM.VENDOR_NAME) LIKE '%COLORADO PERMANENTE MEDICAL GROUP%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE '%ANTHEM FOUNDATION HEALTH PLAN%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'KASC FRANKLIN%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'KASC LONETREE%')
Then 'Internal'
WHEN ( case when D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLHI%' then 'HI'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNW%' then 'NW'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLMA%' then 'MAS'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLSC%' then 'SC'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNC%' then 'NC'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLCO%' then 'CO'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLGA%' then 'GA'
else 'Contact BO Developers' end ) = 'MAS' AND
(UPPER(VENCLM.VENDOR_NAME) LIKE '%INTRGNL AN%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE '%ANTHEM FOUNDATION HEALTH PLAN OF THE MID-ATLANTIC STATES%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE '%MID ATLANTIC PERMANENTE MEDICAL GROUP%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM FOUNDATION MEDICAL GROUP%'
OR upper(VENCLM.VENDOR_NAME) = 'ANTHEM MID ATLANTIC')
THEN 'Internal'
WHEN ( case when D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLHI%' then 'HI'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNW%' then 'NW'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLMA%' then 'MAS'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLSC%' then 'SC'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNC%' then 'NC'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLCO%' then 'CO'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLGA%' then 'GA'
else 'Contact BO Developers' end ) = 'GA' AND
(UPPER(VENCLM.VENDOR_NAME) LIKE '%ANTHEM FOUNDATION HEALTH PLAN%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE '%PERMANENTE MEDICAL GROUP%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM FOUNDATION MEDICAL GROUP%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'PMG VENDOR'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM FAMILY CHIROPRACTI%')
THEN 'Internal'
ELSE 'External'
END,
  coalesce(EPPCL.PRODUCT_TYPE ,ZC_PRD_TYP.NAME),
  POS.CITY,
  POS_ST.ABBR,
  TRUNC((sysdate-( PAT.BIRTH_DATE ))/365,0),
    case
	when D_AA_INDICATOR.CLAIM_ID is null
then  'Not AA'
else 'AA'  end,
  ZC_REG_CD.NAME,
  POS_TYPE.NAME,
  POS.POS_CODE,
  CASE
WHEN CLM.ORIG_REV_CLM_ID IS NOT NULL
    THEN 'Reversal Claim'
WHEN  (CLM.ORIG_ADJST_CLM_ID IS NOT NULL AND CLM.ORIG_REV_CLM_ID IS NULL AND CLM.ADJST_CLM_ID IS NULL) OR ("LEGACY_ADJST"."CLAIM_ID" IS NOT NULL)
                THEN 'Adjusted / Revised Claim'
WHEN CLM.ORIG_ADJST_CLM_ID IS NOT NULL  AND CLM.ORIG_REV_CLM_ID IS NULL AND CLM.ADJST_CLM_ID IS NOT NULL
  THEN 'Multiple Adj Interim Claim'
WHEN CLM.ORIG_ADJST_CLM_ID IS NULL AND CLM.ORIG_REV_CLM_ID IS NULL AND  "LEGACY_ADJST"."CLAIM_ID" IS  NULL
    THEN 'Original Claim'
ELSE NULL
END,
  CLM_MAP5.INTERNAL_ID,
  LISTAGG_DTL_INFO.CODE_LIST,
  LISTAGG_HDR_INFO.CODE_LIST
FROM (
   select DISTINCT min(owner) AS
 CLARITY_DATABASE
FROM
ALL_VIEWS  WHERE VIEW_NAME LIKE 'AP_CLAIM'
  And OWNER Like 'HCCL%'
  )  D_CLARITY_DATABASE,  ZC_CLAIM_FORMAT  CLM_CF RIGHT OUTER JOIN AP_CLAIM  CLM ON (CLM.CLAIM_FORMAT_C=CLM_CF.CLAIM_FORMAT_C)
   LEFT OUTER JOIN ZC_CLM_STATUS  CLM_CS ON (CLM.STATUS_C=CLM_CS.STATUS_C)
   LEFT OUTER JOIN PATIENT  PAT ON (PAT.PAT_ID=CLM.PAT_ID)
   LEFT OUTER JOIN ZC_CLM_AP_STAT  CLM_APSTS ON (CLM.AP_STS_C=CLM_APSTS.AP_STS_C)
   LEFT OUTER JOIN CLARITY_POS  POS ON (CLM.LOC_ID=POS.POS_ID)
   LEFT OUTER JOIN ZC_POS_TYPE  POS_TYPE ON (POS.POS_TYPE_C=POS_TYPE.POS_TYPE_C)
   LEFT OUTER JOIN ZC_STATE  POS_ST ON (POS.STATE_C=POS_ST.STATE_C  AND  POS_ST.INTERNAL_ID >= 0  AND POS_ST.INTERNAL_ID <= 51)
   LEFT OUTER JOIN AP_CLAIM_EOB_CODE  CLM_EOB ON (CLM_EOB.CLAIM_ID=CLM.CLAIM_ID)
   LEFT OUTER JOIN CLARITY_EOB_CODE  EOB_CODE ON (EOB_CODE.EOB_CODE_ID=CLM_EOB.EOB_CODE_ID)
   LEFT OUTER JOIN COVERAGE  COVCL ON (CLM.COVERAGE_ID=COVCL.COVERAGE_ID)
   LEFT OUTER JOIN PLAN_GRP  GRP ON (GRP.PLAN_GRP_ID=COVCL.PLAN_GRP_ID)
   LEFT OUTER JOIN ZC_REGION_CODE  ZC_REG_CD ON (GRP.CUR_REGION_CODE_C=ZC_REG_CD.REGION_CODE_C)
   LEFT OUTER JOIN CLARITY_LOB  LOBCL ON (CLM.CLM_LOB_ID=LOBCL.LOB_ID)
   LEFT OUTER JOIN (
  (SELECT
 CLM.CLAIM_ID
 FROM  AP_CLAIM  CLM
 WHERE
 CLM.CLAIM_ID NOT IN
(SELECT CLAIM_ID
FROM AP_CLAIM_CHANGE_HX HX
LEFT OUTER JOIN ECI_BASIC ECI ON HX.CM_LOG_OWNER_ID =ECI.INSTANCE_ID
WHERE((UPPER(DEPLYMNT_DESC) LIKE '%NP%' AND CHANGE_HX_USER_ID NOT IN ( '161NCALTAP3','161NCALTAP1')) --NCAL
OR (UPPER(DEPLYMNT_DESC) LIKE '%CO%' AND CHANGE_HX_USER_ID NOT IN ( '140194','44323593','44323592')) --CO
OR (UPPER(DEPLYMNT_DESC) LIKE '%SP%' AND CHANGE_HX_USER_ID NOT IN ( '1501001006','1501001005','150119165')) --SCAL
OR (UPPER(DEPLYMNT_DESC) LIKE '%MA%' AND CHANGE_HX_USER_ID NOT IN ( '170100056','1213117','19012093','17017391')) --MAS
OR (UPPER(DEPLYMNT_DESC) LIKE '%NW%' AND CHANGE_HX_USER_ID NOT IN ( '19012093','19012091')) --NW
OR (UPPER(DEPLYMNT_DESC) LIKE '%HI%' AND CHANGE_HX_USER_ID NOT IN ( '130HI50101','130HI50100')) --HI
OR (UPPER(DEPLYMNT_DESC) LIKE '%GA%' AND CHANGE_HX_USER_ID NOT IN('2001','200EDIUSER')) -- GA
) )
AND (CLM.STATUS_C IN (3, 4, 5)
AND CLM.ORIG_REV_CLM_ID IS NULL
AND CLM.ORIG_ADJST_CLM_ID IS NULL))
  )  D_AA_INDICATOR ON (D_AA_INDICATOR.CLAIM_ID=CLM.CLAIM_ID)
   LEFT OUTER JOIN CLARITY_EPP  EPPCL ON (CLM.PLAN_ID=EPPCL.BENEFIT_PLAN_ID)
   LEFT OUTER JOIN CLARITY_EPP_2  EPPCL_2 ON (EPPCL_2.BENEFIT_PLAN_ID=EPPCL.BENEFIT_PLAN_ID)
   LEFT OUTER JOIN ZC_PROD_TYPE  ZC_PRD_TYP ON (EPPCL_2.PROD_TYPE_C=ZC_PRD_TYP.PROD_TYPE_C)
   LEFT OUTER JOIN AP_CLAIM_PX  CLD ON (CLM.CLAIM_ID=CLD.CLAIM_ID)
   LEFT OUTER JOIN ZC_POS_TYPE  CLMPOS ON (CLD.POS_TYPE_C=CLMPOS.POS_TYPE_C)
   LEFT OUTER JOIN AP_CLAIM_PX_EOBS  CLD_EOB ON (CLD.TX_ID=CLD_EOB.TX_ID)
   LEFT OUTER JOIN CLARITY_EOB_CODE  EOB_CODE2 ON (CLD_EOB.EOB_CODE_ID=EOB_CODE2.EOB_CODE_ID)
   LEFT OUTER JOIN AP_CLAIM_2  CLM2 ON (CLM.CLAIM_ID=CLM2.CLAIM_ID)
   LEFT OUTER JOIN ZC_CLM_TRAIT_3  CLM_TRAIT ON (CLM2.CLM_TRAIT_3_C=CLM_TRAIT.CLM_TRAIT_3_C)
   LEFT OUTER JOIN ZC_CLM_TRAIT_4  CLM_TRAIT_4 ON (CLM2.CLM_TRAIT_4_C=CLM_TRAIT_4.CLM_TRAIT_4_C
)
   LEFT OUTER JOIN ZC_CLM_TRAIT_2  CLM_TRAIT_2 ON (CLM2.CLM_TRAIT_2_C=CLM_TRAIT_2.CLM_TRAIT_2_C)
   LEFT OUTER JOIN CLARITY_SER  SERREN ON (CLM.PROV_ID=SERREN.PROV_ID)
   LEFT OUTER JOIN CLARITY_SER_2  SERREN_2 ON (SERREN.PROV_ID=SERREN_2.PROV_ID)
   LEFT OUTER JOIN CLARITY_VENDOR  VENCLM ON (VENCLM.VENDOR_ID=CLM.VENDOR_ID)
   LEFT OUTER JOIN (
  select t1.vendor_id,t1.line,t1.TAX_ID
from	vendor_tax_id   t1 ,
(select	vendor_id,max(line) as line from	vendor_tax_id group	by vendor_id)   t2	Where 	t1.vendor_id=t2.vendor_id 	and	t1.line=t2.line
group	by  t1.vendor_id,t1.line,t1.TAX_ID
  )  D_VTN ON (VENCLM.VENDOR_ID=D_VTN.VENDOR_ID)
   LEFT OUTER JOIN (
  SELECT DISTINCT VENDOR_ID,  PLACE_OF_SERVICE_ID FROM VENDOR_POS
WHERE PLACE_OF_SERVICE_ID IN  (SELECT POS_ID FROM CLARITY_POS WHERE  POS_NAME LIKE '%VISITING MEM%')
union all
SELECT distinct
   t1.vendor_id
   ,999 as PLACE_OF_SERVICE_ID
from
   vendor_tax_id t1 inner join
		(
		select    vendor_id,
		max(line) as line
		from      vendor_tax_id
		group    by vendor_id
		)  t2 on             t1.vendor_id=t2.vendor_id
                             and        t1.line=t2.line
WHERE
  t1.TAX_ID  = '811559375'
  )  D_VM ON (D_VM.VENDOR_ID=VENCLM.VENDOR_ID)
   LEFT OUTER JOIN AP_CLAIM  AOC ON (CLM.ORIG_ADJST_CLM_ID=AOC.CLAIM_ID)
   LEFT OUTER JOIN CLM_MAP  CLM_MAP5 ON (AOC.CLAIM_ID=CLM_MAP5.CID  AND  AOC.CM_LOG_OWNER_ID=CLM_MAP5.CM_LOG_OWNER_ID)
   LEFT OUTER JOIN CLM_MAP  CLM_MAP_1 ON (CLM.CLAIM_ID=CLM_MAP_1.CID  AND  CLM.CM_LOG_OWNER_ID=CLM_MAP_1.CM_LOG_OWNER_ID)
   LEFT OUTER JOIN (
  SELECT
CLAIM_ID
,PAT_LIABILITY
,LISTAGG(MNEMONIC,', ') WITHIN GROUP ( ORDER BY MNEMONIC) AS CODE_LIST
,LISTAGG(REMIT_CD,', ') WITHIN GROUP ( ORDER BY MNEMONIC) AS REMIT_CODE_LIST
FROM  (SELECT DISTINCT
        CLM_EOB.CLAIM_ID
        ,CASE WHEN MAX(EOB.ROUTE_FROM_DISC_C)=2 THEN 'Yes' ELSE
		'No'
		END AS PAT_LIABILITY
        ,EOB.MNEMONIC
        ,RMC.RMC_EXTERNAL_ID||'/'||RMK.ABBR AS REMIT_CD
        FROM
        AP_CLAIM_PX_EOBS CLM_EOB,
        CLARITY_EOB_CODE EOB,
		CLARITY_RMC RMC,
		ZC_REMARK_CODE RMK
        WHERE
        CLM_EOB.EOB_CODE_ID=EOB.EOB_CODE_ID
		AND EOB.REMIT_CODE_ID=RMC.REMIT_CODE_ID(+)
		AND EOB.REMARK_CODE_C=RMK.REMARK_CODE_C(+)
        --1 IS PEND CODE    2 IS DENIAL CODE        3 IS INFO CODE
        AND EOB.CODE_TYPE_C=3
        GROUP BY CLM_EOB.CLAIM_ID,EOB.ROUTE_FROM_DISC_C,EOB.MNEMONIC,RMC.RMC_EXTERNAL_ID,RMK.ABBR
		)
GROUP BY CLAIM_ID,PAT_LIABILITY
  )  LISTAGG_DTL_INFO ON (CLM.CLAIM_ID=LISTAGG_DTL_INFO.CLAIM_ID)
   LEFT OUTER JOIN (
      SELECT DISTINCT CLM.CLAIM_ID FROM  (
   select DISTINCT min(owner) AS
 CLARITY_DATABASE
FROM
ALL_VIEWS  WHERE VIEW_NAME LIKE 'AP_CLAIM'
  And OWNER Like 'HCCL%'
  )  D_CLARITY_DATABASE
  ,  CLARITY_EOB_CODE  EOB_CODE
  RIGHT OUTER JOIN AP_CLAIM_EOB_CODE  CLM_EOB ON (EOB_CODE.EOB_CODE_ID=CLM_EOB.EOB_CODE_ID)
   RIGHT OUTER JOIN AP_CLAIM  CLM ON (CLM_EOB.CLAIM_ID=CLM.CLAIM_ID)
   WHERE
  ( ( case when D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLHI%' then 'HI'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNW%' then 'NW'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLMA%' then 'MAS'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLSC%' then 'SC'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNC%' then 'NC'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLCO%' then 'CO'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLGA%' then 'GA'
else 'Contact BO Developers' end ) IN ('SC','NC') AND  EOB_CODE.MNEMONIC IN ('CI134','CLP38','CLI36') AND CLM_EOB.ENTRY_DATE >=TO_DATE ('2017-12-15' ,'YYYY-MM-DD'))
  )  "LEGACY_ADJST" ON (CLM.CLAIM_ID="LEGACY_ADJST"."CLAIM_ID")
   LEFT OUTER JOIN (
  SELECT
CLAIM_ID
,LISTAGG(MNEMONIC,', ') WITHIN GROUP ( ORDER BY MNEMONIC) AS CODE_LIST
,LISTAGG(REMIT_CD,', ') WITHIN GROUP ( ORDER BY MNEMONIC) AS REMIT_CODE_LIST
FROM  (SELECT DISTINCT
        CLM_EOB.CLAIM_ID
        ,EOB.MNEMONIC
        ,RMC.RMC_EXTERNAL_ID||'/'||RMK.ABBR AS REMIT_CD
        FROM
        AP_CLAIM_EOB_CODE CLM_EOB,
        CLARITY_EOB_CODE EOB,
		CLARITY_RMC RMC,
		ZC_REMARK_CODE RMK
        WHERE
        CLM_EOB.EOB_CODE_ID=EOB.EOB_CODE_ID
		AND EOB.REMIT_CODE_ID=RMC.REMIT_CODE_ID(+)
		AND EOB.REMARK_CODE_C=RMK.REMARK_CODE_C(+)
        AND CLM_EOB.RESOLUTION_DATE IS NULL
        --1 IS PEND CODE    2 IS DENIAL CODE        3 IS INFO CODE
        AND EOB.CODE_TYPE_C=3
		)
GROUP BY CLAIM_ID
  )  LISTAGG_HDR_INFO ON (CLM.CLAIM_ID=LISTAGG_HDR_INFO.CLAIM_ID)
   LEFT OUTER JOIN AP_CLAIM_CHECK  CLM_CHK ON (CLM.CLAIM_ID=CLM_CHK.CLAIM_ID)
   LEFT OUTER JOIN AP_CHECK  CKR ON (CLM_CHK.CHECK_ID=CKR.CHECK_ID)
WHERE
( COALESCE(CLM.WORKFLOW_C,0) IN @Prompt(P_WorkflowTypeInclude)  and COALESCE(CLM_TRAIT_4.NAME,'CCA') In @Prompt(P_CCA-TPMG)  )
  AND
  (
   coalesce(EPPCL.PRODUCT_TYPE ,ZC_PRD_TYP.NAME)  IN  @Prompt('Enter Product Type or Leave Blank for All:','A','Claim Header (CLM)\Claim Header IDs (CLM)\Benefit Plan Id (CLM)\Product Type',Multi,Free,Persistent,,User:1,Optional)
   AND
   nvl(LOBCL.LOB_NAME,'UNKNOWN')  LIKE  @Prompt('Enter Line of Business or Leave Blank for All:','A','Claim Header (CLM)\Line Of Business (CLM)\Line Of Business Name (CLM)',Mono,Free,Persistent,,User:2,Optional)
   AND
   SERREN.PROV_NAME  LIKE  @Prompt('Enter Provider Name or Leave Blank for All:','A','Provider (PRV)\Rendering (SERREN)\Prov Name (SERREN)',Mono,Free,Persistent,,User:3,Optional)
   AND
   SERREN_2.NPI  LIKE  @Prompt('Enter Provider ID or Leave Blank for All:','A',,Mono,Free,Persistent,,User:4,Optional)
   AND
   D_VTN.TAX_ID  LIKE  @Prompt('Enter Vendor Tax ID or Leave Blank for All:','A',,Mono,Free,Persistent,,User:5,Optional)
   AND
   VENCLM.VENDOR_NAME  LIKE  @Prompt('Enter Vendor Name or Leave Blank for All:','A','Claim Header Vendor (VENCLM)\Vendor Name (VENCLM)',Mono,Free,Persistent,,User:6,Optional)
   AND
   GRP.PLAN_GRP_NAME  LIKE  @Prompt('Enter Plan Group Name or Leave Blank for All:','A','Plan / Group (GRP)\Plan Grp Name (GRP)',Mono,Free,Persistent,,User:0,Optional)
   AND
   CLM.STATUS_C  =  3
   AND
   CASE
	WHEN CLM.AP_STS_C=3 THEN CKR.AP_RUN_DATE
END  BETWEEN  (CASE
    WHEN ( @Prompt(Enter Claim Finalized From Date:§(relative or absolute date§)) )='t' THEN
		trunc(sysdate)
 WHEN ( @Prompt(Enter Claim Finalized From Date:§(relative or absolute date§)) ) Like 't-%' THEN
			trunc(sysdate)-to_number(Substr(( @Prompt(Enter Claim Finalized From Date:§(relative or absolute date§)) ),3,3))
 WHEN ( @Prompt(Enter Claim Finalized From Date:§(relative or absolute date§)) )='wb' THEN
                          TRUNC(sysdate, 'IW')-1
 WHEN ( @Prompt(Enter Claim Finalized From Date:§(relative or absolute date§)) )='wb-1' THEN
                          TRUNC(sysdate, 'IW')-8
 WHEN ( @Prompt(Enter Claim Finalized From Date:§(relative or absolute date§)) )='we' THEN
                     NEXT_DAY(TRUNC(sysdate,'IW'),'SATURDAY')
 WHEN ( @Prompt(Enter Claim Finalized From Date:§(relative or absolute date§)) )='we-1' THEN
                     NEXT_DAY(TRUNC(sysdate,'IW')-8,'SATURDAY')
    WHEN ( @Prompt(Enter Claim Finalized From Date:§(relative or absolute date§)) )='mb' THEN
		trunc(sysdate,'MM')
    WHEN ( @Prompt(Enter Claim Finalized From Date:§(relative or absolute date§)) )='me' THEN
		trunc(last_day(sysdate))
 WHEN ( @Prompt(Enter Claim Finalized From Date:§(relative or absolute date§)) )='mb-1' THEN
		trunc(trunc(sysdate, 'MM') - 1, 'MM')
 WHEN ( @Prompt(Enter Claim Finalized From Date:§(relative or absolute date§)) )='me-1' THEN
		(trunc(sysdate, 'MM') - 1)
		 WHEN ( @Prompt(Enter Claim Finalized From Date:§(relative or absolute date§)) )='mb-2' THEN
		add_months(trunc(sysdate, 'MM'), - 2)
 WHEN ( @Prompt(Enter Claim Finalized From Date:§(relative or absolute date§)) )='me-2' THEN
		(last_day(add_months (sysdate,-2)))
		WHEN ( @Prompt(Enter Claim Finalized From Date:§(relative or absolute date§)) )='yb' THEN
		trunc(sysdate,'YY')
		 WHEN ( @Prompt(Enter Claim Finalized From Date:§(relative or absolute date§)) )='yb-1' THEN
		trunc(trunc(sysdate, 'YY') - 1, 'YY')
 else
    TO_DATE(( @Prompt(Enter Claim Finalized From Date:§(relative or absolute date§)) ),'MM/dd/yyyy')
END)  AND  (CASE
    WHEN ( @Prompt(Enter Claim Finalized Thru Date:§(relative or absolute date§)) )='t' THEN
		trunc(sysdate)
 WHEN ( @Prompt(Enter Claim Finalized Thru Date:§(relative or absolute date§)) ) Like't-%' THEN
		trunc(sysdate)-to_number(Substr(( @Prompt(Enter Claim Finalized Thru Date:§(relative or absolute date§)) ),3,3))
 WHEN ( @Prompt(Enter Claim Finalized Thru Date:§(relative or absolute date§)) )='wb' THEN
                          TRUNC(sysdate, 'IW')-1
 WHEN ( @Prompt(Enter Claim Finalized Thru Date:§(relative or absolute date§)) )='wb-1' THEN
                          TRUNC(sysdate, 'IW')-8
 WHEN ( @Prompt(Enter Claim Finalized Thru Date:§(relative or absolute date§)) )='we' THEN
                     NEXT_DAY(TRUNC(sysdate,'IW'),'SATURDAY')
 WHEN ( @Prompt(Enter Claim Finalized Thru Date:§(relative or absolute date§)) )='we-1' THEN
                     NEXT_DAY(TRUNC(sysdate,'IW')-8,'SATURDAY')
    WHEN ( @Prompt(Enter Claim Finalized Thru Date:§(relative or absolute date§)) )='mb' THEN
		trunc(sysdate,'MM')
    WHEN ( @Prompt(Enter Claim Finalized Thru Date:§(relative or absolute date§)) )='me' THEN
		trunc(last_day(sysdate))
 WHEN ( @Prompt(Enter Claim Finalized Thru Date:§(relative or absolute date§)) )='mb-1' THEN
		trunc(trunc(sysdate, 'MM') - 1, 'MM')
 WHEN ( @Prompt(Enter Claim Finalized Thru Date:§(relative or absolute date§)) )='me-1' THEN
		(trunc(sysdate, 'MM') - 1)
		WHEN ( @Prompt(Enter Claim Finalized Thru Date:§(relative or absolute date§)) )='mb-2' THEN
		add_months(trunc(sysdate, 'MM'), - 2)
 WHEN ( @Prompt(Enter Claim Finalized Thru Date:§(relative or absolute date§)) )='me-2' THEN
		(last_day(add_months (sysdate,-2)))
		WHEN ( @Prompt(Enter Claim Finalized Thru Date:§(relative or absolute date§)) )='yb' THEN
		trunc(sysdate,'YY')
		 WHEN ( @Prompt(Enter Claim Finalized Thru Date:§(relative or absolute date§)) )='yb-1' THEN
		trunc(trunc(sysdate, 'YY') - 1, 'YY')
 else
    TO_DATE(( @Prompt(Enter Claim Finalized Thru Date:§(relative or absolute date§)) ),'MM/dd/yyyy')
END)
   AND
   (
    EOB_CODE.MNEMONIC  IN  @Prompt('Enter Claim Denial Code(s) or Leave Blank for All:','A','Claim EOB (EOB)\Eob Code Id (CLM_EOB)\Mnemonic (EOB_CODE)',Multi,Free,Persistent,,User:8,Optional)
    OR
    EOB_CODE2.MNEMONIC  IN  @Prompt('Enter Claim Denial Code(s) or Leave Blank for All:','A','Claim Line (CLD)\Claim Line EOB\Eob Code Id (CLD)\Mnemonic (CLD)',Multi,Free,Persistent,,User:7,Optional)
   )
   AND
   CLMPOS.NAME  IN  @Prompt('Enter POS Type(s) or Leave Blank for All:','A','Claim Line (CLD)\Pos Type C (CLD)\Name (CLMPOS)',Multi,Free,Persistent,,User:9,Optional)
   AND
   CASE
WHEN CLM.ORIG_REV_CLM_ID IS NOT NULL
    THEN 'Reversal Claim'
WHEN  (CLM.ORIG_ADJST_CLM_ID IS NOT NULL AND CLM.ORIG_REV_CLM_ID IS NULL AND CLM.ADJST_CLM_ID IS NULL) OR ("LEGACY_ADJST"."CLAIM_ID" IS NOT NULL)
                THEN 'Adjusted / Revised Claim'
WHEN CLM.ORIG_ADJST_CLM_ID IS NOT NULL  AND CLM.ORIG_REV_CLM_ID IS NULL AND CLM.ADJST_CLM_ID IS NOT NULL
  THEN 'Multiple Adj Interim Claim'
WHEN CLM.ORIG_ADJST_CLM_ID IS NULL AND CLM.ORIG_REV_CLM_ID IS NULL AND  "LEGACY_ADJST"."CLAIM_ID" IS  NULL
    THEN 'Original Claim'
ELSE NULL
END  IN  @Prompt('Enter Claim Adjudication Type:','A','Claim Header (CLM)\Clm Adj Type (CLM)\Clm Adj Type Desc (CLM)',Multi,Free,Persistent,,User:13)
   AND
   NVL(CLM_TRAIT.NAME,'KFHP')  IN  @Prompt('Enter ANIC Claim Trait or Leave Blank for All:','A','Claim Header (CLM)\Company Code (CLM)',Multi,Free,Persistent,,User:10,Optional)
   AND
   nvl(CLM2.DENTAL_INFO_YN,'N')  IN  @Prompt('Enter Dental Indicator (Y/N):','A','Claim Header (CLM)\Dental Info Yn (CLM2)',Multi,Free,Persistent,{'N'},User:11,Optional)
   AND
   CLM.ADJST_CLM_ID  Is Null
   AND
   ZC_REG_CD.NAME  IN  @Prompt('Enter CO Service Area Name:','A','Plan / Group (GRP)\Current Region Code C (GRP)\Current Region Code Name (GRP)',Multi,Free,Persistent,,User:12,Optional)
   AND
   ( ( CASE
WHEN D_VM.PLACE_OF_SERVICE_ID IS NOT NULL THEN 'Y' ELSE 'N'
END ) in @Prompt(Visiting Member) OR ( 'ALL') IN @Prompt(Visiting Member)  )
  )
--END--
SELECT DISTINCT
  CLM_MAP_1.INTERNAL_ID,
  EOB_CODE.ROUTE_FROM_DISC_C,
  EOB_CODE.MNEMONIC
FROM
  ZC_POS_TYPE  CLMPOS RIGHT OUTER JOIN AP_CLAIM_PX  CLD ON (CLD.POS_TYPE_C=CLMPOS.POS_TYPE_C)
   LEFT OUTER JOIN AP_CLAIM_PX_EOBS  CLD_EOB ON (CLD.TX_ID=CLD_EOB.TX_ID)
   LEFT OUTER JOIN CLARITY_EOB_CODE  EOB_CODE2 ON (CLD_EOB.EOB_CODE_ID=EOB_CODE2.EOB_CODE_ID)
   RIGHT OUTER JOIN AP_CLAIM  CLM ON (CLM.CLAIM_ID=CLD.CLAIM_ID)
   LEFT OUTER JOIN AP_CLAIM_EOB_CODE  CLM_EOB ON (CLM_EOB.CLAIM_ID=CLM.CLAIM_ID)
   LEFT OUTER JOIN CLARITY_EOB_CODE  EOB_CODE ON (EOB_CODE.EOB_CODE_ID=CLM_EOB.EOB_CODE_ID)
   LEFT OUTER JOIN COVERAGE  COVCL ON (CLM.COVERAGE_ID=COVCL.COVERAGE_ID)
   LEFT OUTER JOIN PLAN_GRP  GRP ON (GRP.PLAN_GRP_ID=COVCL.PLAN_GRP_ID)
   LEFT OUTER JOIN ZC_REGION_CODE  ZC_REG_CD ON (GRP.CUR_REGION_CODE_C=ZC_REG_CD.REGION_CODE_C)
   LEFT OUTER JOIN CLARITY_LOB  LOBCL ON (CLM.CLM_LOB_ID=LOBCL.LOB_ID)
   LEFT OUTER JOIN CLARITY_EPP  EPPCL ON (CLM.PLAN_ID=EPPCL.BENEFIT_PLAN_ID)
   LEFT OUTER JOIN CLARITY_EPP_2  EPPCL_2 ON (EPPCL_2.BENEFIT_PLAN_ID=EPPCL.BENEFIT_PLAN_ID)
   LEFT OUTER JOIN ZC_PROD_TYPE  ZC_PRD_TYP ON (EPPCL_2.PROD_TYPE_C=ZC_PRD_TYP.PROD_TYPE_C)
   LEFT OUTER JOIN AP_CLAIM_2  CLM2 ON (CLM.CLAIM_ID=CLM2.CLAIM_ID)
   LEFT OUTER JOIN ZC_CLM_TRAIT_3  CLM_TRAIT ON (CLM2.CLM_TRAIT_3_C=CLM_TRAIT.CLM_TRAIT_3_C)
   LEFT OUTER JOIN ZC_CLM_TRAIT_4  CLM_TRAIT_4 ON (CLM2.CLM_TRAIT_4_C=CLM_TRAIT_4.CLM_TRAIT_4_C
)
   LEFT OUTER JOIN CLARITY_SER  SERREN ON (CLM.PROV_ID=SERREN.PROV_ID)
   LEFT OUTER JOIN CLARITY_SER_2  SERREN_2 ON (SERREN.PROV_ID=SERREN_2.PROV_ID)
   LEFT OUTER JOIN CLARITY_VENDOR  VENCLM ON (VENCLM.VENDOR_ID=CLM.VENDOR_ID)
   LEFT OUTER JOIN (
  select t1.vendor_id,t1.line,t1.TAX_ID
from	vendor_tax_id   t1 ,
(select	vendor_id,max(line) as line from	vendor_tax_id group	by vendor_id)   t2	Where 	t1.vendor_id=t2.vendor_id 	and	t1.line=t2.line
group	by  t1.vendor_id,t1.line,t1.TAX_ID
  )  D_VTN ON (VENCLM.VENDOR_ID=D_VTN.VENDOR_ID)
   LEFT OUTER JOIN (
  SELECT DISTINCT VENDOR_ID,  PLACE_OF_SERVICE_ID FROM VENDOR_POS
WHERE PLACE_OF_SERVICE_ID IN  (SELECT POS_ID FROM CLARITY_POS WHERE  POS_NAME LIKE '%VISITING MEM%')
union all
SELECT distinct
   t1.vendor_id
   ,999 as PLACE_OF_SERVICE_ID
from
   vendor_tax_id t1 inner join
		(
		select    vendor_id,
		max(line) as line
		from      vendor_tax_id
		group    by vendor_id
		)  t2 on             t1.vendor_id=t2.vendor_id
                             and        t1.line=t2.line
WHERE
  t1.TAX_ID  = '811559375'
  )  D_VM ON (D_VM.VENDOR_ID=VENCLM.VENDOR_ID)
   LEFT OUTER JOIN CLM_MAP  CLM_MAP_1 ON (CLM.CLAIM_ID=CLM_MAP_1.CID  AND  CLM.CM_LOG_OWNER_ID=CLM_MAP_1.CM_LOG_OWNER_ID)
   LEFT OUTER JOIN (
      SELECT DISTINCT CLM.CLAIM_ID FROM  (
   select DISTINCT min(owner) AS
 CLARITY_DATABASE
FROM
ALL_VIEWS  WHERE VIEW_NAME LIKE 'AP_CLAIM'
  And OWNER Like 'HCCL%'
  )  D_CLARITY_DATABASE
  ,  CLARITY_EOB_CODE  EOB_CODE
  RIGHT OUTER JOIN AP_CLAIM_EOB_CODE  CLM_EOB ON (EOB_CODE.EOB_CODE_ID=CLM_EOB.EOB_CODE_ID)
   RIGHT OUTER JOIN AP_CLAIM  CLM ON (CLM_EOB.CLAIM_ID=CLM.CLAIM_ID)
   WHERE
  ( ( case when D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLHI%' then 'HI'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNW%' then 'NW'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLMA%' then 'MAS'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLSC%' then 'SC'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNC%' then 'NC'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLCO%' then 'CO'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLGA%' then 'GA'
else 'Contact BO Developers' end ) IN ('SC','NC') AND  EOB_CODE.MNEMONIC IN ('CI134','CLP38','CLI36') AND CLM_EOB.ENTRY_DATE >=TO_DATE ('2017-12-15' ,'YYYY-MM-DD'))
  )  "LEGACY_ADJST" ON (CLM.CLAIM_ID="LEGACY_ADJST"."CLAIM_ID")
   LEFT OUTER JOIN AP_CLAIM_CHECK  CLM_CHK ON (CLM.CLAIM_ID=CLM_CHK.CLAIM_ID)
   LEFT OUTER JOIN AP_CHECK  CKR ON (CLM_CHK.CHECK_ID=CKR.CHECK_ID)
WHERE
( COALESCE(CLM.WORKFLOW_C,0) IN @Prompt(P_WorkflowTypeInclude)  and COALESCE(CLM_TRAIT_4.NAME,'CCA') In @Prompt(P_CCA-TPMG)  )
  AND
  (
   coalesce(EPPCL.PRODUCT_TYPE ,ZC_PRD_TYP.NAME)  IN  @Prompt('Enter Product Type or Leave Blank for All:','A','Claim Header (CLM)\Claim Header IDs (CLM)\Benefit Plan Id (CLM)\Product Type',Multi,Free,Persistent,,User:1,Optional)
   AND
   nvl(LOBCL.LOB_NAME,'UNKNOWN')  LIKE  @Prompt('Enter Line of Business or Leave Blank for All:','A','Claim Header (CLM)\Line Of Business (CLM)\Line Of Business Name (CLM)',Mono,Free,Persistent,,User:2,Optional)
   AND
   SERREN.PROV_NAME  LIKE  @Prompt('Enter Provider Name or Leave Blank for All:','A','Provider (PRV)\Rendering (SERREN)\Prov Name (SERREN)',Mono,Free,Persistent,,User:3,Optional)
   AND
   SERREN_2.NPI  LIKE  @Prompt('Enter Provider ID or Leave Blank for All:','A',,Mono,Free,Persistent,,User:4,Optional)
   AND
   D_VTN.TAX_ID  LIKE  @Prompt('Enter Vendor Tax ID or Leave Blank for All:','A',,Mono,Free,Persistent,,User:5,Optional)
   AND
   VENCLM.VENDOR_NAME  LIKE  @Prompt('Enter Vendor Name or Leave Blank for All:','A','Claim Header Vendor (VENCLM)\Vendor Name (VENCLM)',Mono,Free,Persistent,,User:6,Optional)
   AND
   GRP.PLAN_GRP_NAME  LIKE  @Prompt('Enter Plan Group Name or Leave Blank for All:','A','Plan / Group (GRP)\Plan Grp Name (GRP)',Mono,Free,Persistent,,User:0,Optional)
   AND
   CLM.STATUS_C  =  3
   AND
   CASE
	WHEN CLM.AP_STS_C=3 THEN CKR.AP_RUN_DATE
END  BETWEEN  (CASE
    WHEN ( @Prompt(Enter Claim Finalized From Date:§(relative or absolute date§)) )='t' THEN
		trunc(sysdate)
 WHEN ( @Prompt(Enter Claim Finalized From Date:§(relative or absolute date§)) ) Like 't-%' THEN
			trunc(sysdate)-to_number(Substr(( @Prompt(Enter Claim Finalized From Date:§(relative or absolute date§)) ),3,3))
 WHEN ( @Prompt(Enter Claim Finalized From Date:§(relative or absolute date§)) )='wb' THEN
                          TRUNC(sysdate, 'IW')-1
 WHEN ( @Prompt(Enter Claim Finalized From Date:§(relative or absolute date§)) )='wb-1' THEN
                          TRUNC(sysdate, 'IW')-8
 WHEN ( @Prompt(Enter Claim Finalized From Date:§(relative or absolute date§)) )='we' THEN
                     NEXT_DAY(TRUNC(sysdate,'IW'),'SATURDAY')
 WHEN ( @Prompt(Enter Claim Finalized From Date:§(relative or absolute date§)) )='we-1' THEN
                     NEXT_DAY(TRUNC(sysdate,'IW')-8,'SATURDAY')
    WHEN ( @Prompt(Enter Claim Finalized From Date:§(relative or absolute date§)) )='mb' THEN
		trunc(sysdate,'MM')
    WHEN ( @Prompt(Enter Claim Finalized From Date:§(relative or absolute date§)) )='me' THEN
		trunc(last_day(sysdate))
 WHEN ( @Prompt(Enter Claim Finalized From Date:§(relative or absolute date§)) )='mb-1' THEN
		trunc(trunc(sysdate, 'MM') - 1, 'MM')
 WHEN ( @Prompt(Enter Claim Finalized From Date:§(relative or absolute date§)) )='me-1' THEN
		(trunc(sysdate, 'MM') - 1)
		 WHEN ( @Prompt(Enter Claim Finalized From Date:§(relative or absolute date§)) )='mb-2' THEN
		add_months(trunc(sysdate, 'MM'), - 2)
 WHEN ( @Prompt(Enter Claim Finalized From Date:§(relative or absolute date§)) )='me-2' THEN
		(last_day(add_months (sysdate,-2)))
		WHEN ( @Prompt(Enter Claim Finalized From Date:§(relative or absolute date§)) )='yb' THEN
		trunc(sysdate,'YY')
		 WHEN ( @Prompt(Enter Claim Finalized From Date:§(relative or absolute date§)) )='yb-1' THEN
		trunc(trunc(sysdate, 'YY') - 1, 'YY')
 else
    TO_DATE(( @Prompt(Enter Claim Finalized From Date:§(relative or absolute date§)) ),'MM/dd/yyyy')
END)  AND  (CASE
    WHEN ( @Prompt(Enter Claim Finalized Thru Date:§(relative or absolute date§)) )='t' THEN
		trunc(sysdate)
 WHEN ( @Prompt(Enter Claim Finalized Thru Date:§(relative or absolute date§)) ) Like't-%' THEN
		trunc(sysdate)-to_number(Substr(( @Prompt(Enter Claim Finalized Thru Date:§(relative or absolute date§)) ),3,3))
 WHEN ( @Prompt(Enter Claim Finalized Thru Date:§(relative or absolute date§)) )='wb' THEN
                          TRUNC(sysdate, 'IW')-1
 WHEN ( @Prompt(Enter Claim Finalized Thru Date:§(relative or absolute date§)) )='wb-1' THEN
                          TRUNC(sysdate, 'IW')-8
 WHEN ( @Prompt(Enter Claim Finalized Thru Date:§(relative or absolute date§)) )='we' THEN
                     NEXT_DAY(TRUNC(sysdate,'IW'),'SATURDAY')
 WHEN ( @Prompt(Enter Claim Finalized Thru Date:§(relative or absolute date§)) )='we-1' THEN
                     NEXT_DAY(TRUNC(sysdate,'IW')-8,'SATURDAY')
    WHEN ( @Prompt(Enter Claim Finalized Thru Date:§(relative or absolute date§)) )='mb' THEN
		trunc(sysdate,'MM')
    WHEN ( @Prompt(Enter Claim Finalized Thru Date:§(relative or absolute date§)) )='me' THEN
		trunc(last_day(sysdate))
 WHEN ( @Prompt(Enter Claim Finalized Thru Date:§(relative or absolute date§)) )='mb-1' THEN
		trunc(trunc(sysdate, 'MM') - 1, 'MM')
 WHEN ( @Prompt(Enter Claim Finalized Thru Date:§(relative or absolute date§)) )='me-1' THEN
		(trunc(sysdate, 'MM') - 1)
		WHEN ( @Prompt(Enter Claim Finalized Thru Date:§(relative or absolute date§)) )='mb-2' THEN
		add_months(trunc(sysdate, 'MM'), - 2)
 WHEN ( @Prompt(Enter Claim Finalized Thru Date:§(relative or absolute date§)) )='me-2' THEN
		(last_day(add_months (sysdate,-2)))
		WHEN ( @Prompt(Enter Claim Finalized Thru Date:§(relative or absolute date§)) )='yb' THEN
		trunc(sysdate,'YY')
		 WHEN ( @Prompt(Enter Claim Finalized Thru Date:§(relative or absolute date§)) )='yb-1' THEN
		trunc(trunc(sysdate, 'YY') - 1, 'YY')
 else
    TO_DATE(( @Prompt(Enter Claim Finalized Thru Date:§(relative or absolute date§)) ),'MM/dd/yyyy')
END)
   AND
   (
    EOB_CODE.MNEMONIC  IN  @Prompt('Enter Claim Denial Code(s) or Leave Blank for All:','A','Claim EOB (EOB)\Eob Code Id (CLM_EOB)\Mnemonic (EOB_CODE)',Multi,Free,Persistent,,User:8,Optional)
    OR
    EOB_CODE2.MNEMONIC  IN  @Prompt('Enter Claim Denial Code(s) or Leave Blank for All:','A','Claim Line (CLD)\Claim Line EOB\Eob Code Id (CLD)\Mnemonic (CLD)',Multi,Free,Persistent,,User:7,Optional)
   )
   AND
   CLMPOS.NAME  IN  @Prompt('Enter POS Type(s) or Leave Blank for All:','A','Claim Line (CLD)\Pos Type C (CLD)\Name (CLMPOS)',Multi,Free,Persistent,,User:9,Optional)
   AND
   EOB_CODE.CODE_TYPE_C  IN  ( 2  )
   AND
   CASE
WHEN CLM.ORIG_REV_CLM_ID IS NOT NULL
    THEN 'Reversal Claim'
WHEN  (CLM.ORIG_ADJST_CLM_ID IS NOT NULL AND CLM.ORIG_REV_CLM_ID IS NULL AND CLM.ADJST_CLM_ID IS NULL) OR ("LEGACY_ADJST"."CLAIM_ID" IS NOT NULL)
                THEN 'Adjusted / Revised Claim'
WHEN CLM.ORIG_ADJST_CLM_ID IS NOT NULL  AND CLM.ORIG_REV_CLM_ID IS NULL AND CLM.ADJST_CLM_ID IS NOT NULL
  THEN 'Multiple Adj Interim Claim'
WHEN CLM.ORIG_ADJST_CLM_ID IS NULL AND CLM.ORIG_REV_CLM_ID IS NULL AND  "LEGACY_ADJST"."CLAIM_ID" IS  NULL
    THEN 'Original Claim'
ELSE NULL
END  IN  @Prompt('Enter Claim Adjudication Type:','A','Claim Header (CLM)\Clm Adj Type (CLM)\Clm Adj Type Desc (CLM)',Multi,Free,Persistent,,User:13)
   AND
   NVL(CLM_TRAIT.NAME,'KFHP')  IN  @Prompt('Enter ANIC Claim Trait or Leave Blank for All:','A','Claim Header (CLM)\Company Code (CLM)',Multi,Free,Persistent,,User:10,Optional)
   AND
   nvl(CLM2.DENTAL_INFO_YN,'N')  IN  @Prompt('Enter Dental Indicator (Y/N):','A','Claim Header (CLM)\Dental Info Yn (CLM2)',Multi,Free,Persistent,{'N'},User:11,Optional)
   AND
   CLM.ADJST_CLM_ID  Is Null
   AND
   ZC_REG_CD.NAME  IN  @Prompt('Enter CO Service Area Name:','A','Plan / Group (GRP)\Current Region Code C (GRP)\Current Region Code Name (GRP)',Multi,Free,Persistent,,User:12,Optional)
   AND
   ( ( CASE
WHEN D_VM.PLACE_OF_SERVICE_ID IS NOT NULL THEN 'Y' ELSE 'N'
END ) in @Prompt(Visiting Member) OR ( 'ALL') IN @Prompt(Visiting Member)  )
  )
--END--
SELECT DISTINCT
  EOB_CODE.MNEMONIC,
  EOB_CODE.EOB_CODE_NAME
FROM
  CLARITY_EOB_CODE  EOB_CODE
WHERE
  EOB_CODE.CODE_TYPE_C  =  2
--END--
SELECT DISTINCT
  CLM_MAP_1.INTERNAL_ID,
  EAP.PROC_CODE,
  CLM_CS.NAME,
  CLM.STATUS_DATE,
  CKR.CHECK_STATUS_C,
  CLM_APSTS.ABBR,
  CASE
	WHEN CLM.AP_STS_C=3 THEN CKR.AP_RUN_DATE
END,
  CASE
WHEN ( case when D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLHI%' then 'HI'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNW%' then 'NW'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLMA%' then 'MAS'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLSC%' then 'SC'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNC%' then 'NC'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLCO%' then 'CO'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLGA%' then 'GA'
else 'Contact BO Developers' end ) IN ('NC','SC') AND
CLM_TRAIT_2.ABBR='CAP'
THEN 'Internal'
WHEN ( case when D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLHI%' then 'HI'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNW%' then 'NW'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLMA%' then 'MAS'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLSC%' then 'SC'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNC%' then 'NC'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLCO%' then 'CO'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLGA%' then 'GA'
else 'Contact BO Developers' end ) IN ('NW')  AND
 (UPPER(VENCLM.VENDOR_NAME) LIKE '%INTRGNL AN%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE  'ANTHEM FOUNDATION%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM SUNNYSIDE%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM WESTSIDE%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANNW REGIONAL LAB%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM CLINIC%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'CARE ESSENTIALS BY ANTHEM NW%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM HOSPITALS%')
THEN 'Internal'
WHEN ( case when D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLHI%' then 'HI'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNW%' then 'NW'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLMA%' then 'MAS'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLSC%' then 'SC'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNC%' then 'NC'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLCO%' then 'CO'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLGA%' then 'GA'
else 'Contact BO Developers' end ) = 'HI' AND
(UPPER(VENCLM.VENDOR_NAME) LIKE '%INTRGNL AN%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE '%HAWAII PERMANENTE MEDICAL GROUP'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM%HOSPITAL%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM FOUNDATION HEALTH PLAN INC%')
THEN 'Internal'
WHEN ( case when D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLHI%' then 'HI'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNW%' then 'NW'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLMA%' then 'MAS'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLSC%' then 'SC'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNC%' then 'NC'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLCO%' then 'CO'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLGA%' then 'GA'
else 'Contact BO Developers' end ) = 'CO' AND
(UPPER(VENCLM.VENDOR_NAME) LIKE '%COLORADO PERMANENTE MEDICAL GROUP%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE '%ANTHEM FOUNDATION HEALTH PLAN%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'KASC FRANKLIN%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'KASC LONETREE%')
Then 'Internal'
WHEN ( case when D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLHI%' then 'HI'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNW%' then 'NW'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLMA%' then 'MAS'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLSC%' then 'SC'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNC%' then 'NC'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLCO%' then 'CO'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLGA%' then 'GA'
else 'Contact BO Developers' end ) = 'MAS' AND
(UPPER(VENCLM.VENDOR_NAME) LIKE '%INTRGNL AN%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE '%ANTHEM FOUNDATION HEALTH PLAN OF THE MID-ATLANTIC STATES%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE '%MID ATLANTIC PERMANENTE MEDICAL GROUP%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM FOUNDATION MEDICAL GROUP%'
OR upper(VENCLM.VENDOR_NAME) = 'ANTHEM MID ATLANTIC')
THEN 'Internal'
WHEN ( case when D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLHI%' then 'HI'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNW%' then 'NW'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLMA%' then 'MAS'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLSC%' then 'SC'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNC%' then 'NC'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLCO%' then 'CO'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLGA%' then 'GA'
else 'Contact BO Developers' end ) = 'GA' AND
(UPPER(VENCLM.VENDOR_NAME) LIKE '%ANTHEM FOUNDATION HEALTH PLAN%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE '%PERMANENTE MEDICAL GROUP%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM FOUNDATION MEDICAL GROUP%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'PMG VENDOR'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM FAMILY CHIROPRACTI%')
THEN 'Internal'
ELSE 'External'
END,
  CASE
WHEN CLM.ORIG_REV_CLM_ID IS NOT NULL
    THEN 'Reversal Claim'
WHEN  (CLM.ORIG_ADJST_CLM_ID IS NOT NULL AND CLM.ORIG_REV_CLM_ID IS NULL AND CLM.ADJST_CLM_ID IS NULL) OR ("LEGACY_ADJST"."CLAIM_ID" IS NOT NULL)
                THEN 'Adjusted / Revised Claim'
WHEN CLM.ORIG_ADJST_CLM_ID IS NOT NULL  AND CLM.ORIG_REV_CLM_ID IS NULL AND CLM.ADJST_CLM_ID IS NOT NULL
  THEN 'Multiple Adj Interim Claim'
WHEN CLM.ORIG_ADJST_CLM_ID IS NULL AND CLM.ORIG_REV_CLM_ID IS NULL AND  "LEGACY_ADJST"."CLAIM_ID" IS  NULL
    THEN 'Original Claim'
ELSE NULL
END,
  ZC_SPEC.NAME,
  EOB_CODE.MNEMONIC,
  EOB_CODE.EOB_CODE_NAME,
  EOB_CODE2.MNEMONIC,
  EOB_CODE2.CODE_TYPE_C,
  EOB_CODE.CODE_TYPE_C,
  EOB_CODE2.EOB_CODE_NAME,
  TRUNC((sysdate-( PAT.BIRTH_DATE ))/365,0),
  Upper(SERRENADDR.CITY),
  SERRENST.ABBR,
    case
	when D_AA_INDICATOR.CLAIM_ID is null
then  'Not AA'
else 'AA'  end,
  ZC_REG_CD.NAME,
  case when D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLHI%' then 'HI'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNW%' then 'NW'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLMA%' then 'MAS'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLSC%' then 'SC'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNC%' then 'NC'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLCO%' then 'CO'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLGA%' then 'GA'
else 'Contact BO Developers' end,
  LISTAGG_DTL_INFO.CODE_LIST,
  LISTAGG_HDR_INFO.CODE_LIST
FROM (
   select DISTINCT min(owner) AS
 CLARITY_DATABASE
FROM
ALL_VIEWS  WHERE VIEW_NAME LIKE 'AP_CLAIM'
  And OWNER Like 'HCCL%'
  )  D_CLARITY_DATABASE,  ZC_CLM_STATUS  CLM_CS RIGHT OUTER JOIN AP_CLAIM  CLM ON (CLM.STATUS_C=CLM_CS.STATUS_C)
   LEFT OUTER JOIN PATIENT  PAT ON (PAT.PAT_ID=CLM.PAT_ID)
   LEFT OUTER JOIN ZC_CLM_AP_STAT  CLM_APSTS ON (CLM.AP_STS_C=CLM_APSTS.AP_STS_C)
   LEFT OUTER JOIN AP_CLAIM_EOB_CODE  CLM_EOB ON (CLM_EOB.CLAIM_ID=CLM.CLAIM_ID)
   LEFT OUTER JOIN CLARITY_EOB_CODE  EOB_CODE ON (EOB_CODE.EOB_CODE_ID=CLM_EOB.EOB_CODE_ID)
   LEFT OUTER JOIN COVERAGE  COVCL ON (CLM.COVERAGE_ID=COVCL.COVERAGE_ID)
   LEFT OUTER JOIN PLAN_GRP  GRP ON (GRP.PLAN_GRP_ID=COVCL.PLAN_GRP_ID)
   LEFT OUTER JOIN ZC_REGION_CODE  ZC_REG_CD ON (GRP.CUR_REGION_CODE_C=ZC_REG_CD.REGION_CODE_C)
   LEFT OUTER JOIN CLARITY_LOB  LOBCL ON (CLM.CLM_LOB_ID=LOBCL.LOB_ID)
   LEFT OUTER JOIN (
  (SELECT
 CLM.CLAIM_ID
 FROM  AP_CLAIM  CLM
 WHERE
 CLM.CLAIM_ID NOT IN
(SELECT CLAIM_ID
FROM AP_CLAIM_CHANGE_HX HX
LEFT OUTER JOIN ECI_BASIC ECI ON HX.CM_LOG_OWNER_ID =ECI.INSTANCE_ID
WHERE((UPPER(DEPLYMNT_DESC) LIKE '%NP%' AND CHANGE_HX_USER_ID NOT IN ( '161NCALTAP3','161NCALTAP1')) --NCAL
OR (UPPER(DEPLYMNT_DESC) LIKE '%CO%' AND CHANGE_HX_USER_ID NOT IN ( '140194','44323593','44323592')) --CO
OR (UPPER(DEPLYMNT_DESC) LIKE '%SP%' AND CHANGE_HX_USER_ID NOT IN ( '1501001006','1501001005','150119165')) --SCAL
OR (UPPER(DEPLYMNT_DESC) LIKE '%MA%' AND CHANGE_HX_USER_ID NOT IN ( '170100056','1213117','19012093','17017391')) --MAS
OR (UPPER(DEPLYMNT_DESC) LIKE '%NW%' AND CHANGE_HX_USER_ID NOT IN ( '19012093','19012091')) --NW
OR (UPPER(DEPLYMNT_DESC) LIKE '%HI%' AND CHANGE_HX_USER_ID NOT IN ( '130HI50101','130HI50100')) --HI
OR (UPPER(DEPLYMNT_DESC) LIKE '%GA%' AND CHANGE_HX_USER_ID NOT IN('2001','200EDIUSER')) -- GA
) )
AND (CLM.STATUS_C IN (3, 4, 5)
AND CLM.ORIG_REV_CLM_ID IS NULL
AND CLM.ORIG_ADJST_CLM_ID IS NULL))
  )  D_AA_INDICATOR ON (D_AA_INDICATOR.CLAIM_ID=CLM.CLAIM_ID)
   LEFT OUTER JOIN CLARITY_EPP  EPPCL ON (CLM.PLAN_ID=EPPCL.BENEFIT_PLAN_ID)
   LEFT OUTER JOIN CLARITY_EPP_2  EPPCL_2 ON (EPPCL_2.BENEFIT_PLAN_ID=EPPCL.BENEFIT_PLAN_ID)
   LEFT OUTER JOIN ZC_PROD_TYPE  ZC_PRD_TYP ON (EPPCL_2.PROD_TYPE_C=ZC_PRD_TYP.PROD_TYPE_C)
   LEFT OUTER JOIN AP_CLAIM_PX  CLD ON (CLM.CLAIM_ID=CLD.CLAIM_ID)
   LEFT OUTER JOIN ZC_POS_TYPE  CLMPOS ON (CLD.POS_TYPE_C=CLMPOS.POS_TYPE_C)
   LEFT OUTER JOIN CLARITY_EAP  EAP ON (CLD.PROC_ID=EAP.PROC_ID)
   LEFT OUTER JOIN AP_CLAIM_PX_EOBS  CLD_EOB ON (CLD.TX_ID=CLD_EOB.TX_ID)
   LEFT OUTER JOIN CLARITY_EOB_CODE  EOB_CODE2 ON (CLD_EOB.EOB_CODE_ID=EOB_CODE2.EOB_CODE_ID)
   LEFT OUTER JOIN AP_CLAIM_2  CLM2 ON (CLM.CLAIM_ID=CLM2.CLAIM_ID)
   LEFT OUTER JOIN ZC_CLM_TRAIT_3  CLM_TRAIT ON (CLM2.CLM_TRAIT_3_C=CLM_TRAIT.CLM_TRAIT_3_C)
   LEFT OUTER JOIN ZC_CLM_TRAIT_4  CLM_TRAIT_4 ON (CLM2.CLM_TRAIT_4_C=CLM_TRAIT_4.CLM_TRAIT_4_C
)
   LEFT OUTER JOIN ZC_CLM_TRAIT_2  CLM_TRAIT_2 ON (CLM2.CLM_TRAIT_2_C=CLM_TRAIT_2.CLM_TRAIT_2_C)
   LEFT OUTER JOIN CLARITY_SER  SERREN ON (CLM.PROV_ID=SERREN.PROV_ID)
   LEFT OUTER JOIN CLARITY_SER_2  SERREN_2 ON (SERREN.PROV_ID=SERREN_2.PROV_ID)
   LEFT OUTER JOIN (
  SELECT
  PRV_SPEC.PROV_ID PROV_ID,
  PRV_SPEC.SPECIALTY_C SPECIALTY_C
FROM
  CLARITY_SER_SPEC  PRV_SPEC
WHERE LINE=1
group by PROV_ID,SPECIALTY_C
  )  D_PRV_SPEC ON (SERREN.PROV_ID=D_PRV_SPEC.PROV_ID)
   LEFT OUTER JOIN ZC_SPECIALTY  ZC_SPEC ON (D_PRV_SPEC.SPECIALTY_C=ZC_SPEC.SPECIALTY_C)
   LEFT OUTER JOIN CLARITY_SER_ADDR  SERRENADDR ON (SERREN.PROV_ID=SERRENADDR.PROV_ID)
   LEFT OUTER JOIN ZC_STATE  SERRENST ON (SERRENADDR.STATE_C=SERRENST.STATE_C  AND  SERRENST.INTERNAL_ID >= 0  AND SERRENST.INTERNAL_ID <= 51)
   LEFT OUTER JOIN CLARITY_VENDOR  VENCLM ON (VENCLM.VENDOR_ID=CLM.VENDOR_ID)
   LEFT OUTER JOIN (
  select t1.vendor_id,t1.line,t1.TAX_ID
from	vendor_tax_id   t1 ,
(select	vendor_id,max(line) as line from	vendor_tax_id group	by vendor_id)   t2	Where 	t1.vendor_id=t2.vendor_id 	and	t1.line=t2.line
group	by  t1.vendor_id,t1.line,t1.TAX_ID
  )  D_VTN ON (VENCLM.VENDOR_ID=D_VTN.VENDOR_ID)
   LEFT OUTER JOIN (
  SELECT DISTINCT VENDOR_ID,  PLACE_OF_SERVICE_ID FROM VENDOR_POS
WHERE PLACE_OF_SERVICE_ID IN  (SELECT POS_ID FROM CLARITY_POS WHERE  POS_NAME LIKE '%VISITING MEM%')
union all
SELECT distinct
   t1.vendor_id
   ,999 as PLACE_OF_SERVICE_ID
from
   vendor_tax_id t1 inner join
		(
		select    vendor_id,
		max(line) as line
		from      vendor_tax_id
		group    by vendor_id
		)  t2 on             t1.vendor_id=t2.vendor_id
                             and        t1.line=t2.line
WHERE
  t1.TAX_ID  = '811559375'
  )  D_VM ON (D_VM.VENDOR_ID=VENCLM.VENDOR_ID)
   LEFT OUTER JOIN CLM_MAP  CLM_MAP_1 ON (CLM.CLAIM_ID=CLM_MAP_1.CID  AND  CLM.CM_LOG_OWNER_ID=CLM_MAP_1.CM_LOG_OWNER_ID)
   LEFT OUTER JOIN (
  SELECT
CLAIM_ID
,PAT_LIABILITY
,LISTAGG(MNEMONIC,', ') WITHIN GROUP ( ORDER BY MNEMONIC) AS CODE_LIST
,LISTAGG(REMIT_CD,', ') WITHIN GROUP ( ORDER BY MNEMONIC) AS REMIT_CODE_LIST
FROM  (SELECT DISTINCT
        CLM_EOB.CLAIM_ID
        ,CASE WHEN MAX(EOB.ROUTE_FROM_DISC_C)=2 THEN 'Yes' ELSE
		'No'
		END AS PAT_LIABILITY
        ,EOB.MNEMONIC
        ,RMC.RMC_EXTERNAL_ID||'/'||RMK.ABBR AS REMIT_CD
        FROM
        AP_CLAIM_PX_EOBS CLM_EOB,
        CLARITY_EOB_CODE EOB,
		CLARITY_RMC RMC,
		ZC_REMARK_CODE RMK
        WHERE
        CLM_EOB.EOB_CODE_ID=EOB.EOB_CODE_ID
		AND EOB.REMIT_CODE_ID=RMC.REMIT_CODE_ID(+)
		AND EOB.REMARK_CODE_C=RMK.REMARK_CODE_C(+)
        --1 IS PEND CODE    2 IS DENIAL CODE        3 IS INFO CODE
        AND EOB.CODE_TYPE_C=3
        GROUP BY CLM_EOB.CLAIM_ID,EOB.ROUTE_FROM_DISC_C,EOB.MNEMONIC,RMC.RMC_EXTERNAL_ID,RMK.ABBR
		)
GROUP BY CLAIM_ID,PAT_LIABILITY
  )  LISTAGG_DTL_INFO ON (CLM.CLAIM_ID=LISTAGG_DTL_INFO.CLAIM_ID)
   LEFT OUTER JOIN (
      SELECT DISTINCT CLM.CLAIM_ID FROM  (
   select DISTINCT min(owner) AS
 CLARITY_DATABASE
FROM
ALL_VIEWS  WHERE VIEW_NAME LIKE 'AP_CLAIM'
  And OWNER Like 'HCCL%'
  )  D_CLARITY_DATABASE
  ,  CLARITY_EOB_CODE  EOB_CODE
  RIGHT OUTER JOIN AP_CLAIM_EOB_CODE  CLM_EOB ON (EOB_CODE.EOB_CODE_ID=CLM_EOB.EOB_CODE_ID)
   RIGHT OUTER JOIN AP_CLAIM  CLM ON (CLM_EOB.CLAIM_ID=CLM.CLAIM_ID)
   WHERE
  ( ( case when D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLHI%' then 'HI'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNW%' then 'NW'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLMA%' then 'MAS'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLSC%' then 'SC'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNC%' then 'NC'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLCO%' then 'CO'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLGA%' then 'GA'
else 'Contact BO Developers' end ) IN ('SC','NC') AND  EOB_CODE.MNEMONIC IN ('CI134','CLP38','CLI36') AND CLM_EOB.ENTRY_DATE >=TO_DATE ('2017-12-15' ,'YYYY-MM-DD'))
  )  "LEGACY_ADJST" ON (CLM.CLAIM_ID="LEGACY_ADJST"."CLAIM_ID")
   LEFT OUTER JOIN (
  SELECT
CLAIM_ID
,LISTAGG(MNEMONIC,', ') WITHIN GROUP ( ORDER BY MNEMONIC) AS CODE_LIST
,LISTAGG(REMIT_CD,', ') WITHIN GROUP ( ORDER BY MNEMONIC) AS REMIT_CODE_LIST
FROM  (SELECT DISTINCT
        CLM_EOB.CLAIM_ID
        ,EOB.MNEMONIC
        ,RMC.RMC_EXTERNAL_ID||'/'||RMK.ABBR AS REMIT_CD
        FROM
        AP_CLAIM_EOB_CODE CLM_EOB,
        CLARITY_EOB_CODE EOB,
		CLARITY_RMC RMC,
		ZC_REMARK_CODE RMK
        WHERE
        CLM_EOB.EOB_CODE_ID=EOB.EOB_CODE_ID
		AND EOB.REMIT_CODE_ID=RMC.REMIT_CODE_ID(+)
		AND EOB.REMARK_CODE_C=RMK.REMARK_CODE_C(+)
        AND CLM_EOB.RESOLUTION_DATE IS NULL
        --1 IS PEND CODE    2 IS DENIAL CODE        3 IS INFO CODE
        AND EOB.CODE_TYPE_C=3
		)
GROUP BY CLAIM_ID
  )  LISTAGG_HDR_INFO ON (CLM.CLAIM_ID=LISTAGG_HDR_INFO.CLAIM_ID)
   LEFT OUTER JOIN AP_CLAIM_CHECK  CLM_CHK ON (CLM.CLAIM_ID=CLM_CHK.CLAIM_ID)
   LEFT OUTER JOIN AP_CHECK  CKR ON (CLM_CHK.CHECK_ID=CKR.CHECK_ID)
WHERE
( COALESCE(CLM.WORKFLOW_C,0) IN @Prompt(P_WorkflowTypeInclude)  and COALESCE(CLM_TRAIT_4.NAME,'CCA') In @Prompt(P_CCA-TPMG)  )
  AND
  (
   coalesce(EPPCL.PRODUCT_TYPE ,ZC_PRD_TYP.NAME)  IN  @Prompt('Enter Product Type or Leave Blank for All:','A','Claim Header (CLM)\Claim Header IDs (CLM)\Benefit Plan Id (CLM)\Product Type',Multi,Free,Persistent,,User:1,Optional)
   AND
   nvl(LOBCL.LOB_NAME,'UNKNOWN')  LIKE  @Prompt('Enter Line of Business or Leave Blank for All:','A','Claim Header (CLM)\Line Of Business (CLM)\Line Of Business Name (CLM)',Mono,Free,Persistent,,User:2,Optional)
   AND
   SERREN.PROV_NAME  LIKE  @Prompt('Enter Provider Name or Leave Blank for All:','A','Provider (PRV)\Rendering (SERREN)\Prov Name (SERREN)',Mono,Free,Persistent,,User:3,Optional)
   AND
   SERREN_2.NPI  LIKE  @Prompt('Enter Provider ID or Leave Blank for All:','A',,Mono,Free,Persistent,,User:4,Optional)
   AND
   D_VTN.TAX_ID  LIKE  @Prompt('Enter Vendor Tax ID or Leave Blank for All:','A',,Mono,Free,Persistent,,User:5,Optional)
   AND
   VENCLM.VENDOR_NAME  LIKE  @Prompt('Enter Vendor Name or Leave Blank for All:','A','Claim Header Vendor (VENCLM)\Vendor Name (VENCLM)',Mono,Free,Persistent,,User:6,Optional)
   AND
   GRP.PLAN_GRP_NAME  LIKE  @Prompt('Enter Plan Group Name or Leave Blank for All:','A','Plan / Group (GRP)\Plan Grp Name (GRP)',Mono,Free,Persistent,,User:0,Optional)
   AND
   CLM.STATUS_C  =  3
   AND
   CASE
	WHEN CLM.AP_STS_C=3 THEN CKR.AP_RUN_DATE
END  Is Null
   AND
   CLMPOS.NAME  IN  @Prompt('Enter POS Type(s) or Leave Blank for All:','A','Claim Line (CLD)\Pos Type C (CLD)\Name (CLMPOS)',Multi,Free,Persistent,,User:7,Optional)
   AND
   (
    EOB_CODE2.MNEMONIC  IN  @Prompt('Enter Claim Denial Code(s) or Leave Blank for All:','A','Claim Line (CLD)\Claim Line EOB\Eob Code Id (CLD)\Mnemonic (CLD)',Multi,Free,Persistent,,User:8,Optional)
    OR
    EOB_CODE.MNEMONIC  IN  @Prompt('Enter Claim Denial Code(s) or Leave Blank for All:','A','Claim EOB (EOB)\Eob Code Id (CLM_EOB)\Mnemonic (EOB_CODE)',Multi,Free,Persistent,,User:9,Optional)
   )
   AND
   NVL(CLM_TRAIT.NAME,'KFHP')  IN  @Prompt('Enter ANIC Claim Trait or Leave Blank for All:','A','Claim Header (CLM)\Company Code (CLM)',Multi,Free,Persistent,,User:10,Optional)
   AND
   nvl(CLM2.DENTAL_INFO_YN,'N')  IN  @Prompt('Enter Dental Indicator (Y/N):','A','Claim Header (CLM)\Dental Info Yn (CLM2)',Multi,Free,Persistent,{'N'},User:11,Optional)
   AND
   CLM.ADJST_CLM_ID  Is Null
   AND
   ZC_REG_CD.NAME  IN  @Prompt('Enter CO Service Area Name:','A','Plan / Group (GRP)\Current Region Code C (GRP)\Current Region Code Name (GRP)',Multi,Free,Persistent,,User:12,Optional)
   AND
   CASE
WHEN CLM.ORIG_REV_CLM_ID IS NOT NULL
    THEN 'Reversal Claim'
WHEN  (CLM.ORIG_ADJST_CLM_ID IS NOT NULL AND CLM.ORIG_REV_CLM_ID IS NULL AND CLM.ADJST_CLM_ID IS NULL) OR ("LEGACY_ADJST"."CLAIM_ID" IS NOT NULL)
                THEN 'Adjusted / Revised Claim'
WHEN CLM.ORIG_ADJST_CLM_ID IS NOT NULL  AND CLM.ORIG_REV_CLM_ID IS NULL AND CLM.ADJST_CLM_ID IS NOT NULL
  THEN 'Multiple Adj Interim Claim'
WHEN CLM.ORIG_ADJST_CLM_ID IS NULL AND CLM.ORIG_REV_CLM_ID IS NULL AND  "LEGACY_ADJST"."CLAIM_ID" IS  NULL
    THEN 'Original Claim'
ELSE NULL
END  IN  @Prompt('Enter Claim Adjudication Type:','A','Claim Header (CLM)\Clm Adj Type (CLM)\Clm Adj Type Desc (CLM)',Multi,Free,Persistent,{'Original Claim'},User:13)
   AND
   ( ( CASE
WHEN D_VM.PLACE_OF_SERVICE_ID IS NOT NULL THEN 'Y' ELSE 'N'
END ) in @Prompt(Visiting Member) OR ( 'ALL') IN @Prompt(Visiting Member)  )
  )
--END--
SELECT DISTINCT
  CLM_MAP_1.INTERNAL_ID,
  CLD.LINE,
  CLM.ACCT_NUM_WITH_VEN
,
  CLM_CF.NAME,
  CASE
WHEN ( case when D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLHI%' then 'HI'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNW%' then 'NW'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLMA%' then 'MAS'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLSC%' then 'SC'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNC%' then 'NC'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLCO%' then 'CO'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLGA%' then 'GA'
else 'Contact BO Developers' end ) IN ('NC','SC') AND
CLM_TRAIT_2.ABBR='CAP'
THEN 'Internal'
WHEN ( case when D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLHI%' then 'HI'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNW%' then 'NW'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLMA%' then 'MAS'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLSC%' then 'SC'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNC%' then 'NC'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLCO%' then 'CO'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLGA%' then 'GA'
else 'Contact BO Developers' end ) IN ('NW')  AND
 (UPPER(VENCLM.VENDOR_NAME) LIKE '%INTRGNL AN%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE  'ANTHEM FOUNDATION%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM SUNNYSIDE%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM WESTSIDE%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANNW REGIONAL LAB%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM CLINIC%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'CARE ESSENTIALS BY ANTHEM NW%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM HOSPITALS%')
THEN 'Internal'
WHEN ( case when D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLHI%' then 'HI'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNW%' then 'NW'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLMA%' then 'MAS'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLSC%' then 'SC'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNC%' then 'NC'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLCO%' then 'CO'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLGA%' then 'GA'
else 'Contact BO Developers' end ) = 'HI' AND
(UPPER(VENCLM.VENDOR_NAME) LIKE '%INTRGNL AN%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE '%HAWAII PERMANENTE MEDICAL GROUP'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM%HOSPITAL%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM FOUNDATION HEALTH PLAN INC%')
THEN 'Internal'
WHEN ( case when D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLHI%' then 'HI'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNW%' then 'NW'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLMA%' then 'MAS'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLSC%' then 'SC'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNC%' then 'NC'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLCO%' then 'CO'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLGA%' then 'GA'
else 'Contact BO Developers' end ) = 'CO' AND
(UPPER(VENCLM.VENDOR_NAME) LIKE '%COLORADO PERMANENTE MEDICAL GROUP%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE '%ANTHEM FOUNDATION HEALTH PLAN%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'KASC FRANKLIN%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'KASC LONETREE%')
Then 'Internal'
WHEN ( case when D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLHI%' then 'HI'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNW%' then 'NW'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLMA%' then 'MAS'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLSC%' then 'SC'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNC%' then 'NC'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLCO%' then 'CO'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLGA%' then 'GA'
else 'Contact BO Developers' end ) = 'MAS' AND
(UPPER(VENCLM.VENDOR_NAME) LIKE '%INTRGNL AN%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE '%ANTHEM FOUNDATION HEALTH PLAN OF THE MID-ATLANTIC STATES%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE '%MID ATLANTIC PERMANENTE MEDICAL GROUP%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM FOUNDATION MEDICAL GROUP%'
OR upper(VENCLM.VENDOR_NAME) = 'ANTHEM MID ATLANTIC')
THEN 'Internal'
WHEN ( case when D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLHI%' then 'HI'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNW%' then 'NW'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLMA%' then 'MAS'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLSC%' then 'SC'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNC%' then 'NC'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLCO%' then 'CO'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLGA%' then 'GA'
else 'Contact BO Developers' end ) = 'GA' AND
(UPPER(VENCLM.VENDOR_NAME) LIKE '%ANTHEM FOUNDATION HEALTH PLAN%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE '%PERMANENTE MEDICAL GROUP%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM FOUNDATION MEDICAL GROUP%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'PMG VENDOR'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM FAMILY CHIROPRACTI%')
THEN 'Internal'
ELSE 'External'
END,
  VENCLM.VENDOR_NAME,
  nvl(LOBCL.LOB_NAME,'UNKNOWN'),
  SERREN.PROV_NAME,
  EAP.PROC_CODE,
  CLM.TYPE_OF_BILL,
  CLM_CS.NAME,
  ZC_SPEC.NAME,
  EOB_CODE.MNEMONIC,
  EOB_CODE.EOB_CODE_NAME,
  (case when length(substr(CLD.MODIFIERS, 0,instr(CLD.MODIFIERS, ',',1,1))) = 0 or  length(substr(CLD.MODIFIERS, 0,instr(CLD.MODIFIERS, ',',1,1))) Is Null  then CLD.MODIFIERS else  substr(CLD.MODIFIERS, 0,instr(CLD.MODIFIERS, ',',1,1)-1) end),
  (case when length(CLD.MODIFIERS) > 3 then substr(CLD.MODIFIERS,4,2) else '0' end),
  (case when length(CLD.MODIFIERS) > 6 then substr(CLD.MODIFIERS,7,2) else '0' end),
  (case when length(CLD.MODIFIERS) > 9 then substr(CLD.MODIFIERS,10,2) else '0' end ),
  CASE
	WHEN CLM.AP_STS_C=3 THEN CKR.AP_RUN_DATE
END,
  CLM.DATE_RECEIVED,
  CLM.SERVICE_END_DATE,
  WQ.WORKQUEUE_NAME,
  CLM.SERVICE_START_DATE,
  CLD.POS_TYPE_C,
  CLMPOS.NAME,
  POS.POS_NAME,
  sum(coalesce(CLD.OVERRIDE_ALLD_AMT,CLD.ALLOWED_AMT,0)),
  CLD.NET_PAYABLE,
  sum(coalesce(CLD.OVRD_COPAY,CLD.COPAYMENT,0)),
  sum(coalesce(CLD.OVRD_COINS,CLD.COINSURANCE,0)),
  sum(CLD.BILLED_AMT),
  EAFMAP.INTERNAL_ID,
  sum(coalesce(CLD.OVRD_DEDUCTIBLE,CLD.DEDUCTIBLE,0)),
  SUM(CLD.PRIM_PAT_PORTION),
  sum(CLD.PRIM_INS_AMOUNT),
  TRUNC((sysdate-( PAT.BIRTH_DATE ))/365,0),
  Upper(SERRENADDR.CITY),
  SERRENST.ABBR,
    case
	when D_AA_INDICATOR.CLAIM_ID is null
then  'Not AA'
else 'AA'  end,
  ZC_REG_CD.NAME,
  case when D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLHI%' then 'HI'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNW%' then 'NW'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLMA%' then 'MAS'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLSC%' then 'SC'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNC%' then 'NC'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLCO%' then 'CO'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLGA%' then 'GA'
else 'Contact BO Developers' end,
  LISTAGG_DTL_INFO.CODE_LIST,
  LISTAGG_HDR_INFO.CODE_LIST,
  CLD.BILLED_AMT
FROM (
   select DISTINCT min(owner) AS
 CLARITY_DATABASE
FROM
ALL_VIEWS  WHERE VIEW_NAME LIKE 'AP_CLAIM'
  And OWNER Like 'HCCL%'
  )  D_CLARITY_DATABASE,  ZC_CLAIM_FORMAT  CLM_CF RIGHT OUTER JOIN AP_CLAIM  CLM ON (CLM.CLAIM_FORMAT_C=CLM_CF.CLAIM_FORMAT_C)
   LEFT OUTER JOIN ZC_CLM_STATUS  CLM_CS ON (CLM.STATUS_C=CLM_CS.STATUS_C)
   LEFT OUTER JOIN PATIENT  PAT ON (PAT.PAT_ID=CLM.PAT_ID)
   LEFT OUTER JOIN CLARITY_POS  POS ON (CLM.LOC_ID=POS.POS_ID)
   LEFT OUTER JOIN EAF_MAP  EAFMAP ON (EAFMAP.CID=POS.POS_ID  AND  POS.CM_LOG_OWNER_ID=EAFMAP.CM_LOG_OWNER_ID)
   LEFT OUTER JOIN AP_CLAIM_EOB_CODE  CLM_EOB ON (CLM_EOB.CLAIM_ID=CLM.CLAIM_ID)
   LEFT OUTER JOIN CLARITY_EOB_CODE  EOB_CODE ON (EOB_CODE.EOB_CODE_ID=CLM_EOB.EOB_CODE_ID)
   LEFT OUTER JOIN COVERAGE  COVCL ON (CLM.COVERAGE_ID=COVCL.COVERAGE_ID)
   LEFT OUTER JOIN PLAN_GRP  GRP ON (GRP.PLAN_GRP_ID=COVCL.PLAN_GRP_ID)
   LEFT OUTER JOIN ZC_REGION_CODE  ZC_REG_CD ON (GRP.CUR_REGION_CODE_C=ZC_REG_CD.REGION_CODE_C)
   LEFT OUTER JOIN CLARITY_LOB  LOBCL ON (CLM.CLM_LOB_ID=LOBCL.LOB_ID)
   LEFT OUTER JOIN (
  (SELECT
 CLM.CLAIM_ID
 FROM  AP_CLAIM  CLM
 WHERE
 CLM.CLAIM_ID NOT IN
(SELECT CLAIM_ID
FROM AP_CLAIM_CHANGE_HX HX
LEFT OUTER JOIN ECI_BASIC ECI ON HX.CM_LOG_OWNER_ID =ECI.INSTANCE_ID
WHERE((UPPER(DEPLYMNT_DESC) LIKE '%NP%' AND CHANGE_HX_USER_ID NOT IN ( '161NCALTAP3','161NCALTAP1')) --NCAL
OR (UPPER(DEPLYMNT_DESC) LIKE '%CO%' AND CHANGE_HX_USER_ID NOT IN ( '140194','44323593','44323592')) --CO
OR (UPPER(DEPLYMNT_DESC) LIKE '%SP%' AND CHANGE_HX_USER_ID NOT IN ( '1501001006','1501001005','150119165')) --SCAL
OR (UPPER(DEPLYMNT_DESC) LIKE '%MA%' AND CHANGE_HX_USER_ID NOT IN ( '170100056','1213117','19012093','17017391')) --MAS
OR (UPPER(DEPLYMNT_DESC) LIKE '%NW%' AND CHANGE_HX_USER_ID NOT IN ( '19012093','19012091')) --NW
OR (UPPER(DEPLYMNT_DESC) LIKE '%HI%' AND CHANGE_HX_USER_ID NOT IN ( '130HI50101','130HI50100')) --HI
OR (UPPER(DEPLYMNT_DESC) LIKE '%GA%' AND CHANGE_HX_USER_ID NOT IN('2001','200EDIUSER')) -- GA
) )
AND (CLM.STATUS_C IN (3, 4, 5)
AND CLM.ORIG_REV_CLM_ID IS NULL
AND CLM.ORIG_ADJST_CLM_ID IS NULL))
  )  D_AA_INDICATOR ON (D_AA_INDICATOR.CLAIM_ID=CLM.CLAIM_ID)
   LEFT OUTER JOIN CLARITY_EPP  EPPCL ON (CLM.PLAN_ID=EPPCL.BENEFIT_PLAN_ID)
   LEFT OUTER JOIN CLARITY_EPP_2  EPPCL_2 ON (EPPCL_2.BENEFIT_PLAN_ID=EPPCL.BENEFIT_PLAN_ID)
   LEFT OUTER JOIN ZC_PROD_TYPE  ZC_PRD_TYP ON (EPPCL_2.PROD_TYPE_C=ZC_PRD_TYP.PROD_TYPE_C)
   LEFT OUTER JOIN AP_CLAIM_PX  CLD ON (CLM.CLAIM_ID=CLD.CLAIM_ID)
   LEFT OUTER JOIN ZC_POS_TYPE  CLMPOS ON (CLD.POS_TYPE_C=CLMPOS.POS_TYPE_C)
   LEFT OUTER JOIN CLARITY_EAP  EAP ON (CLD.PROC_ID=EAP.PROC_ID)
   LEFT OUTER JOIN AP_CLAIM_2  CLM2 ON (CLM.CLAIM_ID=CLM2.CLAIM_ID)
   LEFT OUTER JOIN ZC_CLM_TRAIT_3  CLM_TRAIT ON (CLM2.CLM_TRAIT_3_C=CLM_TRAIT.CLM_TRAIT_3_C)
   LEFT OUTER JOIN ZC_CLM_TRAIT_4  CLM_TRAIT_4 ON (CLM2.CLM_TRAIT_4_C=CLM_TRAIT_4.CLM_TRAIT_4_C
)
   LEFT OUTER JOIN ZC_CLM_TRAIT_2  CLM_TRAIT_2 ON (CLM2.CLM_TRAIT_2_C=CLM_TRAIT_2.CLM_TRAIT_2_C)
   LEFT OUTER JOIN CLARITY_SER  SERREN ON (CLM.PROV_ID=SERREN.PROV_ID)
   LEFT OUTER JOIN CLARITY_SER_2  SERREN_2 ON (SERREN.PROV_ID=SERREN_2.PROV_ID)
   LEFT OUTER JOIN (
  SELECT
  PRV_SPEC.PROV_ID PROV_ID,
  PRV_SPEC.SPECIALTY_C SPECIALTY_C
FROM
  CLARITY_SER_SPEC  PRV_SPEC
WHERE LINE=1
group by PROV_ID,SPECIALTY_C
  )  D_PRV_SPEC ON (SERREN.PROV_ID=D_PRV_SPEC.PROV_ID)
   LEFT OUTER JOIN ZC_SPECIALTY  ZC_SPEC ON (D_PRV_SPEC.SPECIALTY_C=ZC_SPEC.SPECIALTY_C)
   LEFT OUTER JOIN CLARITY_SER_ADDR  SERRENADDR ON (SERREN.PROV_ID=SERRENADDR.PROV_ID)
   LEFT OUTER JOIN ZC_STATE  SERRENST ON (SERRENADDR.STATE_C=SERRENST.STATE_C  AND  SERRENST.INTERNAL_ID >= 0  AND SERRENST.INTERNAL_ID <= 51)
   LEFT OUTER JOIN CLARITY_VENDOR  VENCLM ON (VENCLM.VENDOR_ID=CLM.VENDOR_ID)
   LEFT OUTER JOIN (
  select t1.vendor_id,t1.line,t1.TAX_ID
from	vendor_tax_id   t1 ,
(select	vendor_id,max(line) as line from	vendor_tax_id group	by vendor_id)   t2	Where 	t1.vendor_id=t2.vendor_id 	and	t1.line=t2.line
group	by  t1.vendor_id,t1.line,t1.TAX_ID
  )  D_VTN ON (VENCLM.VENDOR_ID=D_VTN.VENDOR_ID)
   LEFT OUTER JOIN (
  SELECT DISTINCT VENDOR_ID,  PLACE_OF_SERVICE_ID FROM VENDOR_POS
WHERE PLACE_OF_SERVICE_ID IN  (SELECT POS_ID FROM CLARITY_POS WHERE  POS_NAME LIKE '%VISITING MEM%')
union all
SELECT distinct
   t1.vendor_id
   ,999 as PLACE_OF_SERVICE_ID
from
   vendor_tax_id t1 inner join
		(
		select    vendor_id,
		max(line) as line
		from      vendor_tax_id
		group    by vendor_id
		)  t2 on             t1.vendor_id=t2.vendor_id
                             and        t1.line=t2.line
WHERE
  t1.TAX_ID  = '811559375'
  )  D_VM ON (D_VM.VENDOR_ID=VENCLM.VENDOR_ID)
   LEFT OUTER JOIN CLM_MAP  CLM_MAP_1 ON (CLM.CLAIM_ID=CLM_MAP_1.CID  AND  CLM.CM_LOG_OWNER_ID=CLM_MAP_1.CM_LOG_OWNER_ID)
   LEFT OUTER JOIN (
  SELECT
CLAIM_ID
,PAT_LIABILITY
,LISTAGG(MNEMONIC,', ') WITHIN GROUP ( ORDER BY MNEMONIC) AS CODE_LIST
,LISTAGG(REMIT_CD,', ') WITHIN GROUP ( ORDER BY MNEMONIC) AS REMIT_CODE_LIST
FROM  (SELECT DISTINCT
        CLM_EOB.CLAIM_ID
        ,CASE WHEN MAX(EOB.ROUTE_FROM_DISC_C)=2 THEN 'Yes' ELSE
		'No'
		END AS PAT_LIABILITY
        ,EOB.MNEMONIC
        ,RMC.RMC_EXTERNAL_ID||'/'||RMK.ABBR AS REMIT_CD
        FROM
        AP_CLAIM_PX_EOBS CLM_EOB,
        CLARITY_EOB_CODE EOB,
		CLARITY_RMC RMC,
		ZC_REMARK_CODE RMK
        WHERE
        CLM_EOB.EOB_CODE_ID=EOB.EOB_CODE_ID
		AND EOB.REMIT_CODE_ID=RMC.REMIT_CODE_ID(+)
		AND EOB.REMARK_CODE_C=RMK.REMARK_CODE_C(+)
        --1 IS PEND CODE    2 IS DENIAL CODE        3 IS INFO CODE
        AND EOB.CODE_TYPE_C=3
        GROUP BY CLM_EOB.CLAIM_ID,EOB.ROUTE_FROM_DISC_C,EOB.MNEMONIC,RMC.RMC_EXTERNAL_ID,RMK.ABBR
		)
GROUP BY CLAIM_ID,PAT_LIABILITY
  )  LISTAGG_DTL_INFO ON (CLM.CLAIM_ID=LISTAGG_DTL_INFO.CLAIM_ID)
   LEFT OUTER JOIN (
      SELECT DISTINCT CLM.CLAIM_ID FROM  (
   select DISTINCT min(owner) AS
 CLARITY_DATABASE
FROM
ALL_VIEWS  WHERE VIEW_NAME LIKE 'AP_CLAIM'
  And OWNER Like 'HCCL%'
  )  D_CLARITY_DATABASE
  ,  CLARITY_EOB_CODE  EOB_CODE
  RIGHT OUTER JOIN AP_CLAIM_EOB_CODE  CLM_EOB ON (EOB_CODE.EOB_CODE_ID=CLM_EOB.EOB_CODE_ID)
   RIGHT OUTER JOIN AP_CLAIM  CLM ON (CLM_EOB.CLAIM_ID=CLM.CLAIM_ID)
   WHERE
  ( ( case when D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLHI%' then 'HI'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNW%' then 'NW'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLMA%' then 'MAS'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLSC%' then 'SC'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNC%' then 'NC'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLCO%' then 'CO'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLGA%' then 'GA'
else 'Contact BO Developers' end ) IN ('SC','NC') AND  EOB_CODE.MNEMONIC IN ('CI134','CLP38','CLI36') AND CLM_EOB.ENTRY_DATE >=TO_DATE ('2017-12-15' ,'YYYY-MM-DD'))
  )  "LEGACY_ADJST" ON (CLM.CLAIM_ID="LEGACY_ADJST"."CLAIM_ID")
   LEFT OUTER JOIN (
  SELECT
CLAIM_ID
,LISTAGG(MNEMONIC,', ') WITHIN GROUP ( ORDER BY MNEMONIC) AS CODE_LIST
,LISTAGG(REMIT_CD,', ') WITHIN GROUP ( ORDER BY MNEMONIC) AS REMIT_CODE_LIST
FROM  (SELECT DISTINCT
        CLM_EOB.CLAIM_ID
        ,EOB.MNEMONIC
        ,RMC.RMC_EXTERNAL_ID||'/'||RMK.ABBR AS REMIT_CD
        FROM
        AP_CLAIM_EOB_CODE CLM_EOB,
        CLARITY_EOB_CODE EOB,
		CLARITY_RMC RMC,
		ZC_REMARK_CODE RMK
        WHERE
        CLM_EOB.EOB_CODE_ID=EOB.EOB_CODE_ID
		AND EOB.REMIT_CODE_ID=RMC.REMIT_CODE_ID(+)
		AND EOB.REMARK_CODE_C=RMK.REMARK_CODE_C(+)
        AND CLM_EOB.RESOLUTION_DATE IS NULL
        --1 IS PEND CODE    2 IS DENIAL CODE        3 IS INFO CODE
        AND EOB.CODE_TYPE_C=3
		)
GROUP BY CLAIM_ID
  )  LISTAGG_HDR_INFO ON (CLM.CLAIM_ID=LISTAGG_HDR_INFO.CLAIM_ID)
   LEFT OUTER JOIN AP_CLAIM_WQ_ITEM  WQI ON (CLM.CLAIM_ID=WQI.CLAIM_ID)
   LEFT OUTER JOIN AP_CLAIM_WQ  WQ ON (WQI.WORKQUEUE_ID=WQ.WORKQUEUE_ID)
   LEFT OUTER JOIN AP_CLAIM_CHECK  CLM_CHK ON (CLM.CLAIM_ID=CLM_CHK.CLAIM_ID)
   LEFT OUTER JOIN AP_CHECK  CKR ON (CLM_CHK.CHECK_ID=CKR.CHECK_ID)
WHERE
( COALESCE(CLM.WORKFLOW_C,0) IN @Prompt(P_WorkflowTypeInclude)  and COALESCE(CLM_TRAIT_4.NAME,'CCA') In @Prompt(P_CCA-TPMG)  )
  AND
  (
   coalesce(EPPCL.PRODUCT_TYPE ,ZC_PRD_TYP.NAME)  IN  @Prompt('Enter Product Type or Leave Blank for All:','A','Claim Header (CLM)\Claim Header IDs (CLM)\Benefit Plan Id (CLM)\Product Type',Multi,Free,Persistent,,User:1,Optional)
   AND
   nvl(LOBCL.LOB_NAME,'UNKNOWN')  LIKE  @Prompt('Enter Line of Business or Leave Blank for All:','A','Claim Header (CLM)\Line Of Business (CLM)\Line Of Business Name (CLM)',Mono,Free,Persistent,,User:2,Optional)
   AND
   SERREN.PROV_NAME  LIKE  @Prompt('Enter Provider Name or Leave Blank for All:','A','Provider (PRV)\Rendering (SERREN)\Prov Name (SERREN)',Mono,Free,Persistent,,User:3,Optional)
   AND
   SERREN_2.NPI  LIKE  @Prompt('Enter Provider ID or Leave Blank for All:','A',,Mono,Free,Persistent,,User:4,Optional)
   AND
   D_VTN.TAX_ID  LIKE  @Prompt('Enter Vendor Tax ID or Leave Blank for All:','A',,Mono,Free,Persistent,,User:5,Optional)
   AND
   VENCLM.VENDOR_NAME  LIKE  @Prompt('Enter Vendor Name or Leave Blank for All:','A','Claim Header Vendor (VENCLM)\Vendor Name (VENCLM)',Mono,Free,Persistent,,User:6,Optional)
   AND
   GRP.PLAN_GRP_NAME  LIKE  @Prompt('Enter Plan Group Name or Leave Blank for All:','A','Plan / Group (GRP)\Plan Grp Name (GRP)',Mono,Free,Persistent,,User:0,Optional)
   AND
   (
    (case when length(substr(CLD.MODIFIERS, 0,instr(CLD.MODIFIERS, ',',1,1))) = 0 or  length(substr(CLD.MODIFIERS, 0,instr(CLD.MODIFIERS, ',',1,1))) Is Null  then CLD.MODIFIERS else  substr(CLD.MODIFIERS, 0,instr(CLD.MODIFIERS, ',',1,1)-1) end)  IN  ( '77','76'  )
    OR
    (case when length(CLD.MODIFIERS) > 6 then substr(CLD.MODIFIERS,7,2) else '0' end)  IN  ( '77','76'  )
    OR
    (case when length(CLD.MODIFIERS) > 9 then substr(CLD.MODIFIERS,10,2) else '0' end )  IN  ( '77','76'  )
    OR
    (case when length(CLD.MODIFIERS) > 3 then substr(CLD.MODIFIERS,4,2) else '0' end)  IN  ( '77','76'  )
   )
   AND
   (
    CLM.STATUS_C  =  3
    OR
    CLD.STATUS_C  =  1
   )
   AND
   EOB_CODE.MNEMONIC  IN  ( 'CED12','CED44'  )
   AND
   CLMPOS.NAME  IN  @Prompt('Enter POS Type(s) or Leave Blank for All:','A','Claim Line (CLD)\Pos Type C (CLD)\Name (CLMPOS)',Multi,Free,Persistent,,User:7,Optional)
   AND
   NVL(CLM_TRAIT.NAME,'KFHP')  IN  @Prompt('Enter ANIC Claim Trait or Leave Blank for All:','A','Claim Header (CLM)\Company Code (CLM)',Multi,Free,Persistent,,User:8,Optional)
   AND
   nvl(CLM2.DENTAL_INFO_YN,'N')  IN  @Prompt('Enter Dental Indicator (Y/N):','A','Claim Header (CLM)\Dental Info Yn (CLM2)',Multi,Free,Persistent,{'N'},User:9,Optional)
   AND
   CLM.ADJST_CLM_ID  Is Null
   AND
   ZC_REG_CD.NAME  IN  @Prompt('Enter CO Service Area Name:','A','Plan / Group (GRP)\Current Region Code C (GRP)\Current Region Code Name (GRP)',Multi,Free,Persistent,,User:10,Optional)
   AND
   CASE
WHEN CLM.ORIG_REV_CLM_ID IS NOT NULL
    THEN 'Reversal Claim'
WHEN  (CLM.ORIG_ADJST_CLM_ID IS NOT NULL AND CLM.ORIG_REV_CLM_ID IS NULL AND CLM.ADJST_CLM_ID IS NULL) OR ("LEGACY_ADJST"."CLAIM_ID" IS NOT NULL)
                THEN 'Adjusted / Revised Claim'
WHEN CLM.ORIG_ADJST_CLM_ID IS NOT NULL  AND CLM.ORIG_REV_CLM_ID IS NULL AND CLM.ADJST_CLM_ID IS NOT NULL
  THEN 'Multiple Adj Interim Claim'
WHEN CLM.ORIG_ADJST_CLM_ID IS NULL AND CLM.ORIG_REV_CLM_ID IS NULL AND  "LEGACY_ADJST"."CLAIM_ID" IS  NULL
    THEN 'Original Claim'
ELSE NULL
END  IN  @Prompt('Enter Claim Adjudication Type:','A','Claim Header (CLM)\Clm Adj Type (CLM)\Clm Adj Type Desc (CLM)',Multi,Free,Persistent,,User:11)
   AND
   ( ( CASE
WHEN D_VM.PLACE_OF_SERVICE_ID IS NOT NULL THEN 'Y' ELSE 'N'
END ) in @Prompt(Visiting Member) OR ( 'ALL') IN @Prompt(Visiting Member)  )
  )
GROUP BY
  CLM_MAP_1.INTERNAL_ID,
  CLD.LINE,
  CLM.ACCT_NUM_WITH_VEN
,
  CLM_CF.NAME,
  CASE
WHEN ( case when D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLHI%' then 'HI'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNW%' then 'NW'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLMA%' then 'MAS'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLSC%' then 'SC'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNC%' then 'NC'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLCO%' then 'CO'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLGA%' then 'GA'
else 'Contact BO Developers' end ) IN ('NC','SC') AND
CLM_TRAIT_2.ABBR='CAP'
THEN 'Internal'
WHEN ( case when D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLHI%' then 'HI'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNW%' then 'NW'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLMA%' then 'MAS'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLSC%' then 'SC'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNC%' then 'NC'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLCO%' then 'CO'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLGA%' then 'GA'
else 'Contact BO Developers' end ) IN ('NW')  AND
 (UPPER(VENCLM.VENDOR_NAME) LIKE '%INTRGNL AN%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE  'ANTHEM FOUNDATION%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM SUNNYSIDE%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM WESTSIDE%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANNW REGIONAL LAB%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM CLINIC%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'CARE ESSENTIALS BY ANTHEM NW%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM HOSPITALS%')
THEN 'Internal'
WHEN ( case when D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLHI%' then 'HI'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNW%' then 'NW'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLMA%' then 'MAS'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLSC%' then 'SC'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNC%' then 'NC'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLCO%' then 'CO'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLGA%' then 'GA'
else 'Contact BO Developers' end ) = 'HI' AND
(UPPER(VENCLM.VENDOR_NAME) LIKE '%INTRGNL AN%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE '%HAWAII PERMANENTE MEDICAL GROUP'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM%HOSPITAL%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM FOUNDATION HEALTH PLAN INC%')
THEN 'Internal'
WHEN ( case when D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLHI%' then 'HI'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNW%' then 'NW'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLMA%' then 'MAS'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLSC%' then 'SC'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNC%' then 'NC'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLCO%' then 'CO'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLGA%' then 'GA'
else 'Contact BO Developers' end ) = 'CO' AND
(UPPER(VENCLM.VENDOR_NAME) LIKE '%COLORADO PERMANENTE MEDICAL GROUP%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE '%ANTHEM FOUNDATION HEALTH PLAN%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'KASC FRANKLIN%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'KASC LONETREE%')
Then 'Internal'
WHEN ( case when D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLHI%' then 'HI'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNW%' then 'NW'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLMA%' then 'MAS'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLSC%' then 'SC'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNC%' then 'NC'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLCO%' then 'CO'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLGA%' then 'GA'
else 'Contact BO Developers' end ) = 'MAS' AND
(UPPER(VENCLM.VENDOR_NAME) LIKE '%INTRGNL AN%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE '%ANTHEM FOUNDATION HEALTH PLAN OF THE MID-ATLANTIC STATES%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE '%MID ATLANTIC PERMANENTE MEDICAL GROUP%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM FOUNDATION MEDICAL GROUP%'
OR upper(VENCLM.VENDOR_NAME) = 'ANTHEM MID ATLANTIC')
THEN 'Internal'
WHEN ( case when D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLHI%' then 'HI'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNW%' then 'NW'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLMA%' then 'MAS'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLSC%' then 'SC'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNC%' then 'NC'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLCO%' then 'CO'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLGA%' then 'GA'
else 'Contact BO Developers' end ) = 'GA' AND
(UPPER(VENCLM.VENDOR_NAME) LIKE '%ANTHEM FOUNDATION HEALTH PLAN%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE '%PERMANENTE MEDICAL GROUP%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM FOUNDATION MEDICAL GROUP%'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'PMG VENDOR'
OR UPPER(VENCLM.VENDOR_NAME) LIKE 'ANTHEM FAMILY CHIROPRACTI%')
THEN 'Internal'
ELSE 'External'
END,
  VENCLM.VENDOR_NAME,
  nvl(LOBCL.LOB_NAME,'UNKNOWN'),
  SERREN.PROV_NAME,
  EAP.PROC_CODE,
  CLM.TYPE_OF_BILL,
  CLM_CS.NAME,
  ZC_SPEC.NAME,
  EOB_CODE.MNEMONIC,
  EOB_CODE.EOB_CODE_NAME,
  (case when length(substr(CLD.MODIFIERS, 0,instr(CLD.MODIFIERS, ',',1,1))) = 0 or  length(substr(CLD.MODIFIERS, 0,instr(CLD.MODIFIERS, ',',1,1))) Is Null  then CLD.MODIFIERS else  substr(CLD.MODIFIERS, 0,instr(CLD.MODIFIERS, ',',1,1)-1) end),
  (case when length(CLD.MODIFIERS) > 3 then substr(CLD.MODIFIERS,4,2) else '0' end),
  (case when length(CLD.MODIFIERS) > 6 then substr(CLD.MODIFIERS,7,2) else '0' end),
  (case when length(CLD.MODIFIERS) > 9 then substr(CLD.MODIFIERS,10,2) else '0' end ),
  CASE
	WHEN CLM.AP_STS_C=3 THEN CKR.AP_RUN_DATE
END,
  CLM.DATE_RECEIVED,
  CLM.SERVICE_END_DATE,
  WQ.WORKQUEUE_NAME,
  CLM.SERVICE_START_DATE,
  CLD.POS_TYPE_C,
  CLMPOS.NAME,
  POS.POS_NAME,
  CLD.NET_PAYABLE,
  EAFMAP.INTERNAL_ID,
  TRUNC((sysdate-( PAT.BIRTH_DATE ))/365,0),
  Upper(SERRENADDR.CITY),
  SERRENST.ABBR,
    case
	when D_AA_INDICATOR.CLAIM_ID is null
then  'Not AA'
else 'AA'  end,
  ZC_REG_CD.NAME,
  case when D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLHI%' then 'HI'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNW%' then 'NW'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLMA%' then 'MAS'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLSC%' then 'SC'
when  D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLNC%' then 'NC'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLCO%' then 'CO'
When D_CLARITY_DATABASE.CLARITY_DATABASE like '%HCCLGA%' then 'GA'
else 'Contact BO Developers' end,
  LISTAGG_DTL_INFO.CODE_LIST,
  LISTAGG_HDR_INFO.CODE_LIST,
  CLD.BILLED_AMT
--END--
SELECT DISTINCT
  EOB_CODE.MNEMONIC,
  EOB_CODE.EOB_CODE_NAME
FROM
  CLARITY_EOB_CODE  EOB_CODE
WHERE
  EOB_CODE.CODE_TYPE_C  =  3
;

-- large-sql-with-issue-265.txt
with cross_items as
(select i_item_sk ss_item_sk
from item,
(select iss.i_brand_id brand_id
,iss.i_class_id class_id
,iss.i_category_id category_id
from store_sales
,item iss
,date_dim d1
where ss_item_sk = iss.i_item_sk
and ss_sold_date_sk = d1.d_date_sk
and d1.d_year between 1999 AND 1999 + 2
intersect
select ics.i_brand_id
,ics.i_class_id
,ics.i_category_id
from catalog_sales
,item ics
,date_dim d2
where cs_item_sk = ics.i_item_sk
and cs_sold_date_sk = d2.d_date_sk
and d2.d_year between 1999 AND 1999 + 2
intersect
select iws.i_brand_id
,iws.i_class_id
,iws.i_category_id
from web_sales
,item iws
,date_dim d3
where ws_item_sk = iws.i_item_sk
and ws_sold_date_sk = d3.d_date_sk
and d3.d_year between 1999 AND 1999 + 2) x
where i_brand_id = brand_id
and i_class_id = class_id
and i_category_id = category_id
),
avg_sales as
(select avg(quantitylist_price) average_sales
from (select ss_quantity quantity
,ss_list_price list_price
from store_sales
,date_dim
where ss_sold_date_sk = d_date_sk
and d_year between 1999 and 2001
union all
select cs_quantity quantity
,cs_list_price list_price
from catalog_sales
,date_dim
where cs_sold_date_sk = d_date_sk
and d_year between 1998 and 1998 + 2
union all
select ws_quantity quantity
,ws_list_price list_price
from web_sales
,date_dim
where ws_sold_date_sk = d_date_sk
and d_year between 1998 and 1998 + 2) x)
select channel, i_brand_id,i_class_id,i_category_id,sum(sales), sum(number_sales)
from(
select 'store' channel, i_brand_id,i_class_id
,i_category_id,sum(ss_quantityss_list_price) sales
, count() number_sales
from store_sales
,item
,date_dim
where ss_item_sk in (select ss_item_sk from cross_items)
and ss_item_sk = i_item_sk
and ss_sold_date_sk = d_date_sk
and d_year = 1998+2
and d_moy = 11
group by i_brand_id,i_class_id,i_category_id
having sum(ss_quantityss_list_price) > (select average_sales from avg_sales)
union all
select 'catalog' channel, i_brand_id,i_class_id,i_category_id, sum(cs_quantitycs_list_price) sales, count() number_sales
from catalog_sales
,item
,date_dim
where cs_item_sk in (select ss_item_sk from cross_items)
and cs_item_sk = i_item_sk
and cs_sold_date_sk = d_date_sk
and d_year = 1998+2
and d_moy = 11
group by i_brand_id,i_class_id,i_category_id
having sum(cs_quantitycs_list_price) > (select average_sales from avg_sales)
union all
select 'web' channel, i_brand_id,i_class_id,i_category_id, sum(ws_quantityws_list_price) sales , count() number_sales
from web_sales
,item
,date_dim
where ws_item_sk in (select ss_item_sk from cross_items)
and ws_item_sk = i_item_sk
and ws_sold_date_sk = d_date_sk
and d_year = 1998+2
and d_moy = 11
group by i_brand_id,i_class_id,i_category_id
having sum(ws_quantityws_list_price) > (select average_sales from avg_sales)
) y
group by rollup (channel, i_brand_id,i_class_id,i_category_id)
order by channel,i_brand_id,i_class_id,i_category_id
limit 100;
with cross_items as
(select i_item_sk ss_item_sk
from item,
(select iss.i_brand_id brand_id
,iss.i_class_id class_id
,iss.i_category_id category_id
from store_sales
,item iss
,date_dim d1
where ss_item_sk = iss.i_item_sk
and ss_sold_date_sk = d1.d_date_sk
and d1.d_year between 1999 AND 1999 + 2
intersect
select ics.i_brand_id
,ics.i_class_id
,ics.i_category_id
from catalog_sales
,item ics
,date_dim d2
where cs_item_sk = ics.i_item_sk
and cs_sold_date_sk = d2.d_date_sk
and d2.d_year between 1999 AND 1999 + 2
intersect
select iws.i_brand_id
,iws.i_class_id
,iws.i_category_id
from web_sales
,item iws
,date_dim d3
where ws_item_sk = iws.i_item_sk
and ws_sold_date_sk = d3.d_date_sk
and d3.d_year between 1999 AND 1999 + 2) x
where i_brand_id = brand_id
and i_class_id = class_id
and i_category_id = category_id
),
avg_sales as
(select avg(quantitylist_price) average_sales
from (select ss_quantity quantity
,ss_list_price list_price
from store_sales
,date_dim
where ss_sold_date_sk = d_date_sk
and d_year between 1998 and 1998 + 2
union all
select cs_quantity quantity
,cs_list_price list_price
from catalog_sales
,date_dim
where cs_sold_date_sk = d_date_sk
and d_year between 1998 and 1998 + 2
union all
select ws_quantity quantity
,ws_list_price list_price
from web_sales
,date_dim
where ws_sold_date_sk = d_date_sk
and d_year between 1998 and 1998 + 2) x)
select * from
(select 'store' channel, i_brand_id,i_class_id,i_category_id
,sum(ss_quantityss_list_price) sales, count() number_sales
from store_sales
,item
,date_dim
where ss_item_sk in (select ss_item_sk from cross_items)
and ss_item_sk = i_item_sk
and ss_sold_date_sk = d_date_sk
and d_week_seq = (select d_week_seq
from date_dim
where d_year = 1998 + 1
and d_moy = 12
and d_dom = 16)
group by i_brand_id,i_class_id,i_category_id
having sum(ss_quantityss_list_price) > (select average_sales from avg_sales)) this_year,
(select 'store' channel, i_brand_id,i_class_id
,i_category_id, sum(ss_quantityss_list_price) sales, count() number_sales
from store_sales
,item
,date_dim
where ss_item_sk in (select ss_item_sk from cross_items)
and ss_item_sk = i_item_sk
and ss_sold_date_sk = d_date_sk
and d_week_seq = (select d_week_seq
from date_dim
where d_year = 1998
and d_moy = 12
and d_dom = 16)
group by i_brand_id,i_class_id,i_category_id
having sum(ss_quantity*ss_list_price) > (select average_sales from avg_sales)) last_year
where this_year.i_brand_id= last_year.i_brand_id
and this_year.i_class_id = last_year.i_class_id
and this_year.i_category_id = last_year.i_category_id
order by this_year.channel, this_year.i_brand_id, this_year.i_class_id, this_year.i_category_id
limit 100;


-- performanceIssue1397.sql
SELECT "TABLE1"."LABEL" ,
  (CASE WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 001 - Alternative Capacitor Devices - Stuff Sample', 'Registration Q2 002 - Alternative Capacitor Devices - Stuff Sample', 'Registration Q2 005 - Alternative Capacitor Devices - Stuff Sample', 'Registration Q2 2021 - Alternative Capacitor Devices - Stuff Sample', 'Registration Q4 000 - Alternative Capacitor Devices', 'Registration Q4 001 - Alternative Capacitor Devices - Stuff Sample', 'Registration Q4 002 - Alternative Capacitor Devices - Stuff Sample', 'Registration Q4 006 - Alternative Capacitor Devices - Stuff Sample')) THEN 'Alternative Capacitor Devices' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Entry Alt Propane', 'Registration Q2 001 - Entry Alt Propane', 'Registration Q2 002 - Entry Alt Propane', 'Registration Q4 000 - Entry Alt Propane', 'Registration Q4 001 - Entry Alt Propane', 'Registration Q4 002 - Entry Alt Propane', 'Registration Q4 005 - Camper Yeah - Entry Alt')) THEN 'Entry Alt Propane' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q1 000 - Entry Amps', 'Registration Q2 000 - Entry Amps', 'Registration Q2 001 - Entry Amps', 'Registration Q2 002 - Entry Amps', 'Registration Q2 005 - Entry Amps', 'Registration Q4 000 - Entry Amps', 'Registration Q4 001 - Entry Amps', 'Registration Q4 002 - Entry Amps', 'Registration Q4 005 - Entry Amps', 'Registration Q4 006 - Entry Amps')) THEN 'Entry Amps' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Fullsize Amp Alt', 'Registration Q2 001 - Fullsize Amp Alt', 'Registration Q2 002 - Fullsize Amp Alt', 'Registration Q2 005 - Fullsize Amp Alt', 'Registration Q2 2021 - Fullsize Amp Alt', 'Registration Q4 000 - Fullsize Amp Alt', 'Registration Q4 001 - Fullsize Amp Alt', 'Registration Q4 002 - Fullsize Amp Alt', 'Registration Q4 005 - Fullsize Amp Alt', 'Registration Q4 006 - Fullsize Amp Alt')) THEN 'Fullsize Amp Alt' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q1 000 - Purple Device Makes - 450 Ratings', 'Registration Q2 000 - Purple Device Makes - 450 Ratings', 'Registration Q2 001 - Purple Device Makes - 450 Ratings', 'Registration Q2 002 - Purple Device Makes - 450 Ratings', 'Registration Q2 005 - Purple Device Makes - 450 Ratings', 'Registration Q2 2021 - Purple Device Makes - 450 Ratings', 'Registration Q4 000 - Purple Device Makes - 450 Ratings', 'Registration Q4 001 - Purple Device Makes - 450 Ratings', 'Registration Q4 002 - Purple Device Makes - 450 Ratings', 'Registration Q4 005 - Purple Device Makes - 450 Ratings', 'Registration Q4 006 - Purple Device Makes - 450 Ratings')) THEN 'Purple Device Makes' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Purple Device Makes Non-Waterproof - Any-American', 'Registration Q2 001 - Purple Device Makes Non-Waterproof - Any-American', 'Registration Q2 002 - Purple Device Makes Non-Waterproof - Any-American', 'Registration Q4 000 - Purple Device Makes Non-Waterproof - Any-American')) THEN 'Purple Device Makes Non-Waterproof - Any-American' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Waterproof Speakers', 'Registration Q2 001 - Waterproof Speakers', 'Registration Q2 002 - Waterproof Speakers', 'Registration Q2 005 - Waterproof Speakers', 'Registration Q2 2021 - Waterproof Speakers', 'Registration Q4 000 - Waterproof Speakers', 'Registration Q4 001 - Waterproof Speakers', 'Registration Q4 002 - Waterproof Speakers', 'Registration Q4 005 - Waterproof Speakers', 'Registration Q4 006 - Waterproof Speakers')) THEN 'Waterproof Speakers' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Waterproof Makes - 450 Ratings', 'Registration Q2 001 - Waterproof Makes - 450 Ratings', 'Registration Q2 002 - Waterproof Makes - 450 Ratings', 'Registration Q2 005 - Waterproof Makes - 450 Ratings', 'Registration Q2 2021 - Waterproof Makes - 450 Ratings', 'Registration Q4 000 - Waterproof Makes - 450 Ratings', 'Registration Q4 001 - Waterproof Makes - 450 Ratings', 'Registration Q4 002 - Waterproof Makes - 450 Ratings', 'Registration Q4 005 - Waterproof Makes - 450 Ratings', 'Registration Q4 006 - Waterproof Makes - 450 Ratings')) THEN 'Waterproof Makes' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 001 - Waterproof Propane Redsize', 'Registration Q2 002 - Waterproof Propane Redsize', 'Registration Q2 005 - Waterproof Propane Redsize', 'Registration Q2 2021 - Waterproof Propane Redsize', 'Registration Q4 001 - Waterproof Propane Redsize', 'Registration Q4 002 - Waterproof Propane Redsize', 'Registration Q4 005 - Waterproof Propane Redsize', 'Registration Q4 006 - Waterproof Propane Redsize')) THEN 'Waterproof Propane Redsize' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 001 - Waterproof Propane Small', 'Registration Q2 002 - Waterproof Propane Small', 'Registration Q2 005 - Waterproof Propane Small', 'Registration Q4 001 - Waterproof Propane Small', 'Registration Q4 002 - Waterproof Propane Small', 'Registration Q4 005 - Waterproof Propane Small', 'Registration Q4 006 - Waterproof Propane Small')) THEN 'Waterproof Propane Small' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Red-Junk - Waterproof', 'Registration Q2 001 - Red-Junk - Waterproof', 'Registration Q2 002 - Red-Junk - Waterproof', 'Registration Q2 005 - Red-Junk - Waterproof', 'Registration Q2 2021 - Red-Junk - Waterproof', 'Registration Q4 000 - Red-Junk - Waterproof', 'Registration Q4 001 - Red-Junk - Waterproof', 'Registration Q4 002 - Red-Junk - Waterproof', 'Registration Q4 005 - Red-Junk - Waterproof', 'Registration Q4 006 - Red-Junk - Waterproof')) THEN 'Red-Junk Waterproof' WHEN ("TABLE1"."DESCRIPTION" = 'Registration Q2 2021 - Redsize Amps') THEN 'Redsize Amps' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Near-Entry Waterproof', 'Registration Q2 001 - Near-Entry Waterproof', 'Registration Q2 002 - Near-Entry Waterproof', 'Registration Q2 005 - Near-Entry Waterproof', 'Registration Q2 2021 - Near-Entry Waterproof', 'Registration Q4 000 - Near-Entry Waterproof', 'Registration Q4 001 - Near-Entry Waterproof', 'Registration Q4 002 - Near-Entry Waterproof', 'Registration Q4 005 - Near-Entry Waterproof', 'Registration Q4 006 - Near-Entry Waterproof')) THEN 'Near-Entry Waterproof' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Home Theaters - Smooth', 'Registration Q2 001 - Home Theaters - Smooth', 'Registration Q2 002 - Home Theaters - Smooth', 'Registration Q2 005 - Home Theaters - Smooth', 'Registration Q2 2021 - Home Theaters - Smooth', 'Registration Q4 000 - Home Theaters - Smooth', 'Registration Q4 001 - Home Theaters - Smooth', 'Registration Q4 002 - Home Theaters - Smooth', 'Registration Q4 005 - Home Theaters - Smooth', 'Registration Q4 006 - Home Theaters - Smooth')) THEN 'Home Theaters - Smooth' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Home Theaters - Fullsize', 'Registration Q2 001 - Home Theaters - Fullsize', 'Registration Q2 002 - Home Theaters - Fullsize', 'Registration Q2 005 - Home Theaters - Fullsize', 'Registration Q2 2021 - Home Theaters - Fullsize', 'Registration Q4 000 - Home Theaters - Fullsize', 'Registration Q4 001 - Home Theaters - Fullsize', 'Registration Q4 002 - Home Theaters - Fullsize', 'Registration Q4 005 - Home Theaters - Fullsize', 'Registration Q4 006 - Home Theaters - Fullsize')) THEN 'Home Theaters - Fullsize' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Home Theaters - Fullsize - Half Full', 'Registration Q2 001 - Home Theaters - Fullsize - Half Full', 'Registration Q2 002 - Home Theaters - Fullsize - Half Full', 'Registration Q2 005 - Home Theaters - Fullsize - Half Full', 'Registration Q2 2021 - Home Theaters - Fullsize - Half Full', 'Registration Q4 000 - Home Theaters - Fullsize - Half Full', 'Registration Q4 001 - Home Theaters - Fullsize - Half Full', 'Registration Q4 002 - Home Theaters - Fullsize - Half Full', 'Registration Q4 005 - Home Theaters - Fullsize - Half Full')) THEN 'Home Theaters - Fullsize - Half Full' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q1 000 - Small Amps', 'Registration Q1 001 - Small Amps', 'Registration Q1 002 - Small Amps', 'Registration Q1 005 - Small Amps', 'Registration Q1 006 - Small Amps', 'Registration Q1 2021 - Small Amps', 'Registration Q2 000 - Small Amps', 'Registration Q2 001 - Small Amps', 'Registration Q2 002 - Small Amps', 'Registration Q2 005 - Small Amps', 'Registration Q2 2021 - Small Amps', 'Registration Q3 000 - Small Amps', 'Registration Q3 001 - Small Amps', 'Registration Q3 002 - Small Amps', 'Registration Q3 006 - Small Amps', 'Registration Q4 000 - Small Amps', 'Registration Q4 001 - Small Amps', 'Registration Q4 002 - Small Amps', 'Registration Q4 005 - Small Amps', 'Registration Q4 006 - Small Amps')) THEN 'Small Amps' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Small-Entry Amps - Any-American', 'Registration Q2 001 - Small-Entry Amps - Any-American', 'Registration Q2 002 - Small-Entry Amps - Any-American', 'Registration Q4 000 - Small-Entry Amps - Any-American')) THEN 'Small-Entry Amps - Any-American' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 005 - Camper Yeah - Entry Alt', 'Registration Q4 006 - Camper Yeah - Entry Alt')) THEN 'Camper Yeah - Entry Alt' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 2021 - Camper Yeah - Fullsize', 'Registration Q4 006 - Camper Yeah - Fullsize')) THEN 'Camper Yeah - Fullsize' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Camper Yeah - Redsize', 'Registration Q2 001 - Camper Yeah - Redsize', 'Registration Q2 002 - Camper Yeah - Redsize', 'Registration Q2 005 - Camper Yeah - Redsize', 'Registration Q2 2021 - Camper Yeah - Redsize', 'Registration Q4 000 - Camper Yeah - Redsize', 'Registration Q4 001 - Camper Yeah - Redsize', 'Registration Q4 002 - Camper Yeah - Redsize', 'Registration Q4 005 - Camper Yeah - Redsize', 'Registration Q4 006 - Camper Yeah - Redsize')) THEN 'Camper Yeah - Redsize' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q1 000 - Camper Yeah - Small', 'Registration Q1 001 - Camper Yeah - Small', 'Registration Q1 002 - Camper Yeah - Small', 'Registration Q1 005 - Camper Yeah - Small', 'Registration Q1 006 - Camper Yeah - Small', 'Registration Q1 2021 - Camper Yeah - Small', 'Registration Q2 000 - Camper Yeah - Small', 'Registration Q2 001 - Camper Yeah - Small', 'Registration Q2 002 - Camper Yeah - Small', 'Registration Q2 005 - Camper Yeah - Small', 'Registration Q2 2021 - Camper Yeah - Small', 'Registration Q3 000 - Camper Yeah - Small', 'Registration Q3 001 - Camper Yeah - Small', 'Registration Q3 002 - Camper Yeah - Small', 'Registration Q3 006 - Camper Yeah - Small', 'Registration Q4 000 - Camper Yeah - Small', 'Registration Q4 001 - Camper Yeah - Small', 'Registration Q4 002 - Camper Yeah - Small', 'Registration Q4 005 - Camper Yeah - Small', 'Registration Q4 006 - Camper Yeah - Small')) THEN 'Camper Yeah - Small' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Camper Yeah - Small - Any-American', 'Registration Q2 001 - Camper Yeah - Small - Any-American', 'Registration Q2 002 - Camper Yeah - Small - Any-American', 'Registration Q4 000 - Camper Yeah - Small - Any-American')) THEN 'Camper Yeah - Small - Any-American' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Camper Yeah - Premium Redsize Alt', 'Registration Q4 000 - Camper Yeah - Premium Redsize Alt')) THEN 'Camper Yeah Premium Redsize Alt' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 002 - Camper Wagon', 'Registration Q2 005 - Camper Wagon', 'Registration Q2 2021 - Camper Wagon', 'Registration Q4 002 - Camper Wagon', 'Registration Q4 005 - Camper Wagon', 'Registration Q4 006 - Camper Wagon')) THEN 'Camper Wagon' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 005 - Campers Amps', 'Registration Q2 2021 - Campers Amps', 'Registration Q4 005 - Campers Amps', 'Registration Q4 006 - Campers Amps')) THEN 'Campers Amps' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Campery', 'Registration Q2 001 - Campery', 'Registration Q2 002 - Campery', 'Registration Q2 005 - Campery', 'Registration Q2 2021 - Campery', 'Registration Q4 000 - Campery', 'Registration Q4 001 - Campery', 'Registration Q4 002 - Campery', 'Registration Q4 005 - Campery', 'Registration Q4 006 - Campery')) THEN 'Campery' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q1 000 - Upper Reddle Alt', 'Registration Q1 001 - Upper Reddle Alt', 'Registration Q1 002 - Upper Reddle Alt', 'Registration Q1 005 - Upper Reddle Alt', 'Registration Q1 006 - Upper Reddle Alt', 'Registration Q1 2021 - Upper Reddle Alt', 'Registration Q2 000 - Upper Reddle Alt', 'Registration Q2 001 - Upper Reddle Alt', 'Registration Q2 002 - Upper Reddle Alt', 'Registration Q2 005 - Upper Reddle Alt', 'Registration Q3 000 - Upper Reddle Alt', 'Registration Q3 001 - Upper Reddle Alt', 'Registration Q3 002 - Upper Reddle Alt', 'Registration Q3 006 - Upper Reddle Alt', 'Registration Q4 000 - Upper Reddle Alt', 'Registration Q4 001 - Upper Reddle Alt', 'Registration Q4 002 - Upper Reddle Alt', 'Registration Q4 005 - Upper Reddle Alt', 'Registration Q4 006 - Upper Reddle Alt')) THEN 'Upper Reddle Alt' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Upper Reddle Alt - Any-American', 'Registration Q2 001 - Upper Reddle Alt - Any-American', 'Registration Q2 002 - Upper Reddle Alt - Any-American', 'Registration Q4 000 - Upper Reddle Alt - Any-American')) THEN 'Upper Reddle Alt - Any-American' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Fires - Smooth', 'Registration Q2 001 - Fires - Smooth', 'Registration Q2 002 - Fires - Smooth', 'Registration Q2 005 - Fires - Smooth', 'Registration Q2 2021 - Fires - Smooth', 'Registration Q4 000 - Fires - Smooth', 'Registration Q4 001 - Fires - Smooth', 'Registration Q4 002 - Fires - Smooth', 'Registration Q4 005 - Fires - Smooth', 'Registration Q4 006 - Fires - Smooth')) THEN 'Fires - Smooth' ELSE "TABLE1"."DESCRIPTION" END) AS "SEGMENT",
  (CASE WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q1 000 - Entry Amps', 'Registration Q1 000 - Purple Device Makes - 450 Ratings', 'Registration Q1 000 - Small Amps', 'Registration Q1 000 - Camper Yeah - Small', 'Registration Q1 000 - Upper Reddle Alt')) THEN '000 Q1' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Entry Alt Propane', 'Registration Q2 000 - Entry Amps', 'Registration Q2 000 - Fullsize Amp Alt', 'Registration Q2 000 - Purple Device Makes - 450 Ratings', 'Registration Q2 000 - Purple Device Makes Non-Waterproof - Any-American', 'Registration Q2 000 - Waterproof Speakers', 'Registration Q2 000 - Waterproof Makes - 450 Ratings', 'Registration Q2 000 - Red-Junk - Waterproof', 'Registration Q2 000 - Near-Entry Waterproof', 'Registration Q2 000 - Home Theaters - Smooth', 'Registration Q2 000 - Home Theaters - Fullsize', 'Registration Q2 000 - Home Theaters - Fullsize - Half Full', 'Registration Q2 000 - Small Amps', 'Registration Q2 000 - Small-Entry Amps - Any-American', 'Registration Q2 000 - Camper Yeah - Redsize', 'Registration Q2 000 - Camper Yeah - Premium Redsize Alt', 'Registration Q2 000 - Camper Yeah - Small', 'Registration Q2 000 - Camper Yeah - Small - Any-American', 'Registration Q2 000 - Campery', 'Registration Q2 000 - Upper Reddle Alt', 'Registration Q2 000 - Upper Reddle Alt - Any-American', 'Registration Q2 000 - Fires - Smooth')) THEN '000 Q2' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q3 000 - Small Amps', 'Registration Q3 000 - Camper Yeah - Small', 'Registration Q3 000 - Upper Reddle Alt')) THEN '000 Q3' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q4 000 - Alternative Capacitor Devices', 'Registration Q4 000 - Entry Alt Propane', 'Registration Q4 000 - Entry Amps', 'Registration Q4 000 - Fullsize Amp Alt', 'Registration Q4 000 - Purple Device Makes - 450 Ratings', 'Registration Q4 000 - Purple Device Makes Non-Waterproof - Any-American', 'Registration Q4 000 - Waterproof Speakers', 'Registration Q4 000 - Waterproof Makes - 450 Ratings', 'Registration Q4 000 - Red-Junk - Waterproof', 'Registration Q4 000 - Near-Entry Waterproof', 'Registration Q4 000 - Home Theaters - Smooth', 'Registration Q4 000 - Home Theaters - Fullsize', 'Registration Q4 000 - Home Theaters - Fullsize - Half Full', 'Registration Q4 000 - Small Amps', 'Registration Q4 000 - Small-Entry Amps - Any-American', 'Registration Q4 000 - Camper Yeah - Redsize', 'Registration Q4 000 - Camper Yeah - Premium Redsize Alt', 'Registration Q4 000 - Camper Yeah - Small', 'Registration Q4 000 - Camper Yeah - Small - Any-American', 'Registration Q4 000 - Campery', 'Registration Q4 000 - Upper Reddle Alt', 'Registration Q4 000 - Upper Reddle Alt - Any-American', 'Registration Q4 000 - Fires - Smooth')) THEN '000 Q4' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q1 001 - Small Amps', 'Registration Q1 001 - Camper Yeah - Small', 'Registration Q1 001 - Upper Reddle Alt')) THEN '001 Q1' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 001 - Alternative Capacitor Devices - Stuff Sample', 'Registration Q2 001 - Entry Alt Propane', 'Registration Q2 001 - Entry Amps', 'Registration Q2 001 - Fullsize Amp Alt', 'Registration Q2 001 - Purple Device Makes - 450 Ratings', 'Registration Q2 001 - Purple Device Makes Non-Waterproof - Any-American', 'Registration Q2 001 - Waterproof Speakers', 'Registration Q2 001 - Waterproof Makes - 450 Ratings', 'Registration Q2 001 - Waterproof Propane Redsize', 'Registration Q2 001 - Waterproof Propane Small', 'Registration Q2 001 - Red-Junk - Waterproof', 'Registration Q2 001 - Near-Entry Waterproof', 'Registration Q2 001 - Home Theaters - Smooth', 'Registration Q2 001 - Home Theaters - Fullsize', 'Registration Q2 001 - Home Theaters - Fullsize - Half Full', 'Registration Q2 001 - Small Amps', 'Registration Q2 001 - Small-Entry Amps - Any-American', 'Registration Q2 001 - Camper Yeah - Redsize', 'Registration Q2 001 - Camper Yeah - Small', 'Registration Q2 001 - Camper Yeah - Small - Any-American', 'Registration Q2 001 - Campery', 'Registration Q2 001 - Upper Reddle Alt', 'Registration Q2 001 - Upper Reddle Alt - Any-American', 'Registration Q2 001 - Fires - Smooth')) THEN '001 Q2' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q3 001 - Small Amps', 'Registration Q3 001 - Camper Yeah - Small', 'Registration Q3 001 - Upper Reddle Alt')) THEN '001 Q3' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q4 001 - Alternative Capacitor Devices - Stuff Sample', 'Registration Q4 001 - Entry Alt Propane', 'Registration Q4 001 - Entry Amps', 'Registration Q4 001 - Fullsize Amp Alt', 'Registration Q4 001 - Purple Device Makes - 450 Ratings', 'Registration Q4 001 - Waterproof Speakers', 'Registration Q4 001 - Waterproof Makes - 450 Ratings', 'Registration Q4 001 - Waterproof Propane Redsize', 'Registration Q4 001 - Waterproof Propane Small', 'Registration Q4 001 - Red-Junk - Waterproof', 'Registration Q4 001 - Near-Entry Waterproof', 'Registration Q4 001 - Home Theaters - Smooth', 'Registration Q4 001 - Home Theaters - Fullsize', 'Registration Q4 001 - Home Theaters - Fullsize - Half Full', 'Registration Q4 001 - Small Amps', 'Registration Q4 001 - Camper Yeah - Redsize', 'Registration Q4 001 - Camper Yeah - Small', 'Registration Q4 001 - Campery', 'Registration Q4 001 - Upper Reddle Alt', 'Registration Q4 001 - Fires - Smooth')) THEN '001 Q4' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q1 002 - Small Amps', 'Registration Q1 002 - Camper Yeah - Small', 'Registration Q1 002 - Upper Reddle Alt')) THEN '002 Q1' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 002 - Alternative Capacitor Devices - Stuff Sample', 'Registration Q2 002 - Entry Alt Propane', 'Registration Q2 002 - Entry Amps', 'Registration Q2 002 - Fullsize Amp Alt', 'Registration Q2 002 - Purple Device Makes - 450 Ratings', 'Registration Q2 002 - Purple Device Makes Non-Waterproof - Any-American', 'Registration Q2 002 - Waterproof Speakers', 'Registration Q2 002 - Waterproof Makes - 450 Ratings', 'Registration Q2 002 - Waterproof Propane Redsize', 'Registration Q2 002 - Waterproof Propane Small', 'Registration Q2 002 - Red-Junk - Waterproof', 'Registration Q2 002 - Near-Entry Waterproof', 'Registration Q2 002 - Home Theaters - Smooth', 'Registration Q2 002 - Home Theaters - Fullsize', 'Registration Q2 002 - Home Theaters - Fullsize - Half Full', 'Registration Q2 002 - Small Amps', 'Registration Q2 002 - Small-Entry Amps - Any-American', 'Registration Q2 002 - Camper Yeah - Redsize', 'Registration Q2 002 - Camper Yeah - Small', 'Registration Q2 002 - Camper Yeah - Small - Any-American', 'Registration Q2 002 - Camper Wagon', 'Registration Q2 002 - Campery', 'Registration Q2 002 - Upper Reddle Alt', 'Registration Q2 002 - Upper Reddle Alt - Any-American', 'Registration Q2 002 - Fires - Smooth')) THEN '002 Q2' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q3 002 - Small Amps', 'Registration Q3 002 - Camper Yeah - Small', 'Registration Q3 002 - Upper Reddle Alt')) THEN '002 Q3' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q4 002 - Alternative Capacitor Devices - Stuff Sample', 'Registration Q4 002 - Entry Alt Propane', 'Registration Q4 002 - Entry Amps', 'Registration Q4 002 - Fullsize Amp Alt', 'Registration Q4 002 - Purple Device Makes - 450 Ratings', 'Registration Q4 002 - Waterproof Speakers', 'Registration Q4 002 - Waterproof Makes - 450 Ratings', 'Registration Q4 002 - Waterproof Propane Redsize', 'Registration Q4 002 - Waterproof Propane Small', 'Registration Q4 002 - Red-Junk - Waterproof', 'Registration Q4 002 - Near-Entry Waterproof', 'Registration Q4 002 - Home Theaters - Smooth', 'Registration Q4 002 - Home Theaters - Fullsize', 'Registration Q4 002 - Home Theaters - Fullsize - Half Full', 'Registration Q4 002 - Small Amps', 'Registration Q4 002 - Camper Yeah - Redsize', 'Registration Q4 002 - Camper Yeah - Small', 'Registration Q4 002 - Camper Wagon', 'Registration Q4 002 - Campery', 'Registration Q4 002 - Upper Reddle Alt', 'Registration Q4 002 - Fires - Smooth')) THEN '002 Q4' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q1 005 - Small Amps', 'Registration Q1 005 - Camper Yeah - Small', 'Registration Q1 005 - Upper Reddle Alt')) THEN '005 Q1' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 005 - Alternative Capacitor Devices - Stuff Sample', 'Registration Q2 005 - Entry Amps', 'Registration Q2 005 - Fullsize Amp Alt', 'Registration Q2 005 - Purple Device Makes - 450 Ratings', 'Registration Q2 005 - Waterproof Speakers', 'Registration Q2 005 - Waterproof Makes - 450 Ratings', 'Registration Q2 005 - Waterproof Propane Redsize', 'Registration Q2 005 - Waterproof Propane Small', 'Registration Q2 005 - Red-Junk - Waterproof', 'Registration Q2 005 - Near-Entry Waterproof', 'Registration Q2 005 - Home Theaters - Smooth', 'Registration Q2 005 - Home Theaters - Fullsize', 'Registration Q2 005 - Home Theaters - Fullsize - Half Full', 'Registration Q2 005 - Small Amps', 'Registration Q2 005 - Camper Yeah - Entry Alt', 'Registration Q2 005 - Camper Yeah - Redsize', 'Registration Q2 005 - Camper Yeah - Small', 'Registration Q2 005 - Camper Wagon', 'Registration Q2 005 - Campers Amps', 'Registration Q2 005 - Campery', 'Registration Q2 005 - Upper Reddle Alt', 'Registration Q2 005 - Fires - Smooth')) THEN '005 Q2' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q4 005 - Entry Amps', 'Registration Q4 005 - Fullsize Amp Alt', 'Registration Q4 005 - Purple Device Makes - 450 Ratings', 'Registration Q4 005 - Waterproof Speakers', 'Registration Q4 005 - Waterproof Makes - 450 Ratings', 'Registration Q4 005 - Waterproof Propane Redsize', 'Registration Q4 005 - Waterproof Propane Small', 'Registration Q4 005 - Red-Junk - Waterproof', 'Registration Q4 005 - Near-Entry Waterproof', 'Registration Q4 005 - Home Theaters - Smooth', 'Registration Q4 005 - Home Theaters - Fullsize', 'Registration Q4 005 - Home Theaters - Fullsize - Half Full', 'Registration Q4 005 - Small Amps', 'Registration Q4 005 - Camper Yeah - Entry Alt', 'Registration Q4 005 - Camper Yeah - Redsize', 'Registration Q4 005 - Camper Yeah - Small', 'Registration Q4 005 - Camper Wagon', 'Registration Q4 005 - Campers Amps', 'Registration Q4 005 - Campery', 'Registration Q4 005 - Upper Reddle Alt', 'Registration Q4 005 - Fires - Smooth')) THEN '005 Q4' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q1 006 - Small Amps', 'Registration Q1 006 - Camper Yeah - Small', 'Registration Q1 006 - Upper Reddle Alt')) THEN '006 Q1' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q3 006 - Small Amps', 'Registration Q3 006 - Camper Yeah - Small', 'Registration Q3 006 - Upper Reddle Alt')) THEN '006 Q3' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q4 006 - Alternative Capacitor Devices - Stuff Sample', 'Registration Q4 006 - Entry Amps', 'Registration Q4 006 - Fullsize Amp Alt', 'Registration Q4 006 - Purple Device Makes - 450 Ratings', 'Registration Q4 006 - Waterproof Speakers', 'Registration Q4 006 - Waterproof Makes - 450 Ratings', 'Registration Q4 006 - Waterproof Propane Redsize', 'Registration Q4 006 - Waterproof Propane Small', 'Registration Q4 006 - Red-Junk - Waterproof', 'Registration Q4 006 - Near-Entry Waterproof', 'Registration Q4 006 - Home Theaters - Smooth', 'Registration Q4 006 - Home Theaters - Fullsize', 'Registration Q4 006 - Small Amps', 'Registration Q4 006 - Camper Yeah - Entry Alt', 'Registration Q4 006 - Camper Yeah - Fullsize', 'Registration Q4 006 - Camper Yeah - Redsize', 'Registration Q4 006 - Camper Yeah - Small', 'Registration Q4 006 - Camper Wagon', 'Registration Q4 006 - Campers Amps', 'Registration Q4 006 - Campery', 'Registration Q4 006 - Upper Reddle Alt', 'Registration Q4 006 - Fires - Smooth')) THEN '006 Q4' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q1 2021 - Small Amps', 'Registration Q1 2021 - Camper Yeah - Small', 'Registration Q1 2021 - Upper Reddle Alt')) THEN '2021 Q1' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 2021 - Alternative Capacitor Devices - Stuff Sample', 'Registration Q2 2021 - Fullsize Amp Alt', 'Registration Q2 2021 - Purple Device Makes - 450 Ratings', 'Registration Q2 2021 - Waterproof Speakers', 'Registration Q2 2021 - Waterproof Makes - 450 Ratings', 'Registration Q2 2021 - Waterproof Propane Redsize', 'Registration Q2 2021 - Red-Junk - Waterproof', 'Registration Q2 2021 - Redsize Amps', 'Registration Q2 2021 - Near-Entry Waterproof', 'Registration Q2 2021 - Home Theaters - Smooth', 'Registration Q2 2021 - Home Theaters - Fullsize', 'Registration Q2 2021 - Home Theaters - Fullsize - Half Full', 'Registration Q2 2021 - Small Amps', 'Registration Q2 2021 - Camper Yeah - Fullsize', 'Registration Q2 2021 - Camper Yeah - Redsize', 'Registration Q2 2021 - Camper Yeah - Small', 'Registration Q2 2021 - Camper Wagon', 'Registration Q2 2021 - Campers Amps', 'Registration Q2 2021 - Campery', 'Registration Q2 2021 - Fires - Smooth')) THEN '2021 Q2' ELSE "TABLE1"."DESCRIPTION" END) AS "Study_Quarter/Year",
  COUNT(DISTINCT "TABLE2"."ID") AS "ctd:ID:ok"
FROM "SCHEMA1"."TABLE2" "TABLE2"
  INNER JOIN "SCHEMA1"."TABLE1" "TABLE1" ON ("TABLE2"."ID" = "TABLE1"."ID")
WHERE (((CASE WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 001 - Alternative Capacitor Devices - Stuff Sample', 'Registration Q2 002 - Alternative Capacitor Devices - Stuff Sample', 'Registration Q2 005 - Alternative Capacitor Devices - Stuff Sample', 'Registration Q2 2021 - Alternative Capacitor Devices - Stuff Sample', 'Registration Q4 000 - Alternative Capacitor Devices', 'Registration Q4 001 - Alternative Capacitor Devices - Stuff Sample', 'Registration Q4 002 - Alternative Capacitor Devices - Stuff Sample', 'Registration Q4 006 - Alternative Capacitor Devices - Stuff Sample')) THEN 'Alternative Capacitor Devices' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Entry Alt Propane', 'Registration Q2 001 - Entry Alt Propane', 'Registration Q2 002 - Entry Alt Propane', 'Registration Q4 000 - Entry Alt Propane', 'Registration Q4 001 - Entry Alt Propane', 'Registration Q4 002 - Entry Alt Propane', 'Registration Q4 005 - Camper Yeah - Entry Alt')) THEN 'Entry Alt Propane' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q1 000 - Entry Amps', 'Registration Q2 000 - Entry Amps', 'Registration Q2 001 - Entry Amps', 'Registration Q2 002 - Entry Amps', 'Registration Q2 005 - Entry Amps', 'Registration Q4 000 - Entry Amps', 'Registration Q4 001 - Entry Amps', 'Registration Q4 002 - Entry Amps', 'Registration Q4 005 - Entry Amps', 'Registration Q4 006 - Entry Amps')) THEN 'Entry Amps' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Fullsize Amp Alt', 'Registration Q2 001 - Fullsize Amp Alt', 'Registration Q2 002 - Fullsize Amp Alt', 'Registration Q2 005 - Fullsize Amp Alt', 'Registration Q2 2021 - Fullsize Amp Alt', 'Registration Q4 000 - Fullsize Amp Alt', 'Registration Q4 001 - Fullsize Amp Alt', 'Registration Q4 002 - Fullsize Amp Alt', 'Registration Q4 005 - Fullsize Amp Alt', 'Registration Q4 006 - Fullsize Amp Alt')) THEN 'Fullsize Amp Alt' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q1 000 - Purple Device Makes - 450 Ratings', 'Registration Q2 000 - Purple Device Makes - 450 Ratings', 'Registration Q2 001 - Purple Device Makes - 450 Ratings', 'Registration Q2 002 - Purple Device Makes - 450 Ratings', 'Registration Q2 005 - Purple Device Makes - 450 Ratings', 'Registration Q2 2021 - Purple Device Makes - 450 Ratings', 'Registration Q4 000 - Purple Device Makes - 450 Ratings', 'Registration Q4 001 - Purple Device Makes - 450 Ratings', 'Registration Q4 002 - Purple Device Makes - 450 Ratings', 'Registration Q4 005 - Purple Device Makes - 450 Ratings', 'Registration Q4 006 - Purple Device Makes - 450 Ratings')) THEN 'Purple Device Makes' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Purple Device Makes Non-Waterproof - Any-American', 'Registration Q2 001 - Purple Device Makes Non-Waterproof - Any-American', 'Registration Q2 002 - Purple Device Makes Non-Waterproof - Any-American', 'Registration Q4 000 - Purple Device Makes Non-Waterproof - Any-American')) THEN 'Purple Device Makes Non-Waterproof - Any-American' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Waterproof Speakers', 'Registration Q2 001 - Waterproof Speakers', 'Registration Q2 002 - Waterproof Speakers', 'Registration Q2 005 - Waterproof Speakers', 'Registration Q2 2021 - Waterproof Speakers', 'Registration Q4 000 - Waterproof Speakers', 'Registration Q4 001 - Waterproof Speakers', 'Registration Q4 002 - Waterproof Speakers', 'Registration Q4 005 - Waterproof Speakers', 'Registration Q4 006 - Waterproof Speakers')) THEN 'Waterproof Speakers' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Waterproof Makes - 450 Ratings', 'Registration Q2 001 - Waterproof Makes - 450 Ratings', 'Registration Q2 002 - Waterproof Makes - 450 Ratings', 'Registration Q2 005 - Waterproof Makes - 450 Ratings', 'Registration Q2 2021 - Waterproof Makes - 450 Ratings', 'Registration Q4 000 - Waterproof Makes - 450 Ratings', 'Registration Q4 001 - Waterproof Makes - 450 Ratings', 'Registration Q4 002 - Waterproof Makes - 450 Ratings', 'Registration Q4 005 - Waterproof Makes - 450 Ratings', 'Registration Q4 006 - Waterproof Makes - 450 Ratings')) THEN 'Waterproof Makes' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 001 - Waterproof Propane Redsize', 'Registration Q2 002 - Waterproof Propane Redsize', 'Registration Q2 005 - Waterproof Propane Redsize', 'Registration Q2 2021 - Waterproof Propane Redsize', 'Registration Q4 001 - Waterproof Propane Redsize', 'Registration Q4 002 - Waterproof Propane Redsize', 'Registration Q4 005 - Waterproof Propane Redsize', 'Registration Q4 006 - Waterproof Propane Redsize')) THEN 'Waterproof Propane Redsize' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 001 - Waterproof Propane Small', 'Registration Q2 002 - Waterproof Propane Small', 'Registration Q2 005 - Waterproof Propane Small', 'Registration Q4 001 - Waterproof Propane Small', 'Registration Q4 002 - Waterproof Propane Small', 'Registration Q4 005 - Waterproof Propane Small', 'Registration Q4 006 - Waterproof Propane Small')) THEN 'Waterproof Propane Small' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Red-Junk - Waterproof', 'Registration Q2 001 - Red-Junk - Waterproof', 'Registration Q2 002 - Red-Junk - Waterproof', 'Registration Q2 005 - Red-Junk - Waterproof', 'Registration Q2 2021 - Red-Junk - Waterproof', 'Registration Q4 000 - Red-Junk - Waterproof', 'Registration Q4 001 - Red-Junk - Waterproof', 'Registration Q4 002 - Red-Junk - Waterproof', 'Registration Q4 005 - Red-Junk - Waterproof', 'Registration Q4 006 - Red-Junk - Waterproof')) THEN 'Red-Junk Waterproof' WHEN ("TABLE1"."DESCRIPTION" = 'Registration Q2 2021 - Redsize Amps') THEN 'Redsize Amps' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Near-Entry Waterproof', 'Registration Q2 001 - Near-Entry Waterproof', 'Registration Q2 002 - Near-Entry Waterproof', 'Registration Q2 005 - Near-Entry Waterproof', 'Registration Q2 2021 - Near-Entry Waterproof', 'Registration Q4 000 - Near-Entry Waterproof', 'Registration Q4 001 - Near-Entry Waterproof', 'Registration Q4 002 - Near-Entry Waterproof', 'Registration Q4 005 - Near-Entry Waterproof', 'Registration Q4 006 - Near-Entry Waterproof')) THEN 'Near-Entry Waterproof' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Home Theaters - Smooth', 'Registration Q2 001 - Home Theaters - Smooth', 'Registration Q2 002 - Home Theaters - Smooth', 'Registration Q2 005 - Home Theaters - Smooth', 'Registration Q2 2021 - Home Theaters - Smooth', 'Registration Q4 000 - Home Theaters - Smooth', 'Registration Q4 001 - Home Theaters - Smooth', 'Registration Q4 002 - Home Theaters - Smooth', 'Registration Q4 005 - Home Theaters - Smooth', 'Registration Q4 006 - Home Theaters - Smooth')) THEN 'Home Theaters - Smooth' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Home Theaters - Fullsize', 'Registration Q2 001 - Home Theaters - Fullsize', 'Registration Q2 002 - Home Theaters - Fullsize', 'Registration Q2 005 - Home Theaters - Fullsize', 'Registration Q2 2021 - Home Theaters - Fullsize', 'Registration Q4 000 - Home Theaters - Fullsize', 'Registration Q4 001 - Home Theaters - Fullsize', 'Registration Q4 002 - Home Theaters - Fullsize', 'Registration Q4 005 - Home Theaters - Fullsize', 'Registration Q4 006 - Home Theaters - Fullsize')) THEN 'Home Theaters - Fullsize' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Home Theaters - Fullsize - Half Full', 'Registration Q2 001 - Home Theaters - Fullsize - Half Full', 'Registration Q2 002 - Home Theaters - Fullsize - Half Full', 'Registration Q2 005 - Home Theaters - Fullsize - Half Full', 'Registration Q2 2021 - Home Theaters - Fullsize - Half Full', 'Registration Q4 000 - Home Theaters - Fullsize - Half Full', 'Registration Q4 001 - Home Theaters - Fullsize - Half Full', 'Registration Q4 002 - Home Theaters - Fullsize - Half Full', 'Registration Q4 005 - Home Theaters - Fullsize - Half Full')) THEN 'Home Theaters - Fullsize - Half Full' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q1 000 - Small Amps', 'Registration Q1 001 - Small Amps', 'Registration Q1 002 - Small Amps', 'Registration Q1 005 - Small Amps', 'Registration Q1 006 - Small Amps', 'Registration Q1 2021 - Small Amps', 'Registration Q2 000 - Small Amps', 'Registration Q2 001 - Small Amps', 'Registration Q2 002 - Small Amps', 'Registration Q2 005 - Small Amps', 'Registration Q2 2021 - Small Amps', 'Registration Q3 000 - Small Amps', 'Registration Q3 001 - Small Amps', 'Registration Q3 002 - Small Amps', 'Registration Q3 006 - Small Amps', 'Registration Q4 000 - Small Amps', 'Registration Q4 001 - Small Amps', 'Registration Q4 002 - Small Amps', 'Registration Q4 005 - Small Amps', 'Registration Q4 006 - Small Amps')) THEN 'Small Amps' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Small-Entry Amps - Any-American', 'Registration Q2 001 - Small-Entry Amps - Any-American', 'Registration Q2 002 - Small-Entry Amps - Any-American', 'Registration Q4 000 - Small-Entry Amps - Any-American')) THEN 'Small-Entry Amps - Any-American' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 005 - Camper Yeah - Entry Alt', 'Registration Q4 006 - Camper Yeah - Entry Alt')) THEN 'Camper Yeah - Entry Alt' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 2021 - Camper Yeah - Fullsize', 'Registration Q4 006 - Camper Yeah - Fullsize')) THEN 'Camper Yeah - Fullsize' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Camper Yeah - Redsize', 'Registration Q2 001 - Camper Yeah - Redsize', 'Registration Q2 002 - Camper Yeah - Redsize', 'Registration Q2 005 - Camper Yeah - Redsize', 'Registration Q2 2021 - Camper Yeah - Redsize', 'Registration Q4 000 - Camper Yeah - Redsize', 'Registration Q4 001 - Camper Yeah - Redsize', 'Registration Q4 002 - Camper Yeah - Redsize', 'Registration Q4 005 - Camper Yeah - Redsize', 'Registration Q4 006 - Camper Yeah - Redsize')) THEN 'Camper Yeah - Redsize' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q1 000 - Camper Yeah - Small', 'Registration Q1 001 - Camper Yeah - Small', 'Registration Q1 002 - Camper Yeah - Small', 'Registration Q1 005 - Camper Yeah - Small', 'Registration Q1 006 - Camper Yeah - Small', 'Registration Q1 2021 - Camper Yeah - Small', 'Registration Q2 000 - Camper Yeah - Small', 'Registration Q2 001 - Camper Yeah - Small', 'Registration Q2 002 - Camper Yeah - Small', 'Registration Q2 005 - Camper Yeah - Small', 'Registration Q2 2021 - Camper Yeah - Small', 'Registration Q3 000 - Camper Yeah - Small', 'Registration Q3 001 - Camper Yeah - Small', 'Registration Q3 002 - Camper Yeah - Small', 'Registration Q3 006 - Camper Yeah - Small', 'Registration Q4 000 - Camper Yeah - Small', 'Registration Q4 001 - Camper Yeah - Small', 'Registration Q4 002 - Camper Yeah - Small', 'Registration Q4 005 - Camper Yeah - Small', 'Registration Q4 006 - Camper Yeah - Small')) THEN 'Camper Yeah - Small' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Camper Yeah - Small - Any-American', 'Registration Q2 001 - Camper Yeah - Small - Any-American', 'Registration Q2 002 - Camper Yeah - Small - Any-American', 'Registration Q4 000 - Camper Yeah - Small - Any-American')) THEN 'Camper Yeah - Small - Any-American' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Camper Yeah - Premium Redsize Alt', 'Registration Q4 000 - Camper Yeah - Premium Redsize Alt')) THEN 'Camper Yeah Premium Redsize Alt' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 002 - Camper Wagon', 'Registration Q2 005 - Camper Wagon', 'Registration Q2 2021 - Camper Wagon', 'Registration Q4 002 - Camper Wagon', 'Registration Q4 005 - Camper Wagon', 'Registration Q4 006 - Camper Wagon')) THEN 'Camper Wagon' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 005 - Campers Amps', 'Registration Q2 2021 - Campers Amps', 'Registration Q4 005 - Campers Amps', 'Registration Q4 006 - Campers Amps')) THEN 'Campers Amps' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Campery', 'Registration Q2 001 - Campery', 'Registration Q2 002 - Campery', 'Registration Q2 005 - Campery', 'Registration Q2 2021 - Campery', 'Registration Q4 000 - Campery', 'Registration Q4 001 - Campery', 'Registration Q4 002 - Campery', 'Registration Q4 005 - Campery', 'Registration Q4 006 - Campery')) THEN 'Campery' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q1 000 - Upper Reddle Alt', 'Registration Q1 001 - Upper Reddle Alt', 'Registration Q1 002 - Upper Reddle Alt', 'Registration Q1 005 - Upper Reddle Alt', 'Registration Q1 006 - Upper Reddle Alt', 'Registration Q1 2021 - Upper Reddle Alt', 'Registration Q2 000 - Upper Reddle Alt', 'Registration Q2 001 - Upper Reddle Alt', 'Registration Q2 002 - Upper Reddle Alt', 'Registration Q2 005 - Upper Reddle Alt', 'Registration Q3 000 - Upper Reddle Alt', 'Registration Q3 001 - Upper Reddle Alt', 'Registration Q3 002 - Upper Reddle Alt', 'Registration Q3 006 - Upper Reddle Alt', 'Registration Q4 000 - Upper Reddle Alt', 'Registration Q4 001 - Upper Reddle Alt', 'Registration Q4 002 - Upper Reddle Alt', 'Registration Q4 005 - Upper Reddle Alt', 'Registration Q4 006 - Upper Reddle Alt')) THEN 'Upper Reddle Alt' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Upper Reddle Alt - Any-American', 'Registration Q2 001 - Upper Reddle Alt - Any-American', 'Registration Q2 002 - Upper Reddle Alt - Any-American', 'Registration Q4 000 - Upper Reddle Alt - Any-American')) THEN 'Upper Reddle Alt - Any-American' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Fires - Smooth', 'Registration Q2 001 - Fires - Smooth', 'Registration Q2 002 - Fires - Smooth', 'Registration Q2 005 - Fires - Smooth', 'Registration Q2 2021 - Fires - Smooth', 'Registration Q4 000 - Fires - Smooth', 'Registration Q4 001 - Fires - Smooth', 'Registration Q4 002 - Fires - Smooth', 'Registration Q4 005 - Fires - Smooth', 'Registration Q4 006 - Fires - Smooth')) THEN 'Fires - Smooth' ELSE "TABLE1"."DESCRIPTION" END) >= 'Alternative Capacitor Devices') AND ((CASE WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 001 - Alternative Capacitor Devices - Stuff Sample', 'Registration Q2 002 - Alternative Capacitor Devices - Stuff Sample', 'Registration Q2 005 - Alternative Capacitor Devices - Stuff Sample', 'Registration Q2 2021 - Alternative Capacitor Devices - Stuff Sample', 'Registration Q4 000 - Alternative Capacitor Devices', 'Registration Q4 001 - Alternative Capacitor Devices - Stuff Sample', 'Registration Q4 002 - Alternative Capacitor Devices - Stuff Sample', 'Registration Q4 006 - Alternative Capacitor Devices - Stuff Sample')) THEN 'Alternative Capacitor Devices' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Entry Alt Propane', 'Registration Q2 001 - Entry Alt Propane', 'Registration Q2 002 - Entry Alt Propane', 'Registration Q4 000 - Entry Alt Propane', 'Registration Q4 001 - Entry Alt Propane', 'Registration Q4 002 - Entry Alt Propane', 'Registration Q4 005 - Camper Yeah - Entry Alt')) THEN 'Entry Alt Propane' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q1 000 - Entry Amps', 'Registration Q2 000 - Entry Amps', 'Registration Q2 001 - Entry Amps', 'Registration Q2 002 - Entry Amps', 'Registration Q2 005 - Entry Amps', 'Registration Q4 000 - Entry Amps', 'Registration Q4 001 - Entry Amps', 'Registration Q4 002 - Entry Amps', 'Registration Q4 005 - Entry Amps', 'Registration Q4 006 - Entry Amps')) THEN 'Entry Amps' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Fullsize Amp Alt', 'Registration Q2 001 - Fullsize Amp Alt', 'Registration Q2 002 - Fullsize Amp Alt', 'Registration Q2 005 - Fullsize Amp Alt', 'Registration Q2 2021 - Fullsize Amp Alt', 'Registration Q4 000 - Fullsize Amp Alt', 'Registration Q4 001 - Fullsize Amp Alt', 'Registration Q4 002 - Fullsize Amp Alt', 'Registration Q4 005 - Fullsize Amp Alt', 'Registration Q4 006 - Fullsize Amp Alt')) THEN 'Fullsize Amp Alt' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q1 000 - Purple Device Makes - 450 Ratings', 'Registration Q2 000 - Purple Device Makes - 450 Ratings', 'Registration Q2 001 - Purple Device Makes - 450 Ratings', 'Registration Q2 002 - Purple Device Makes - 450 Ratings', 'Registration Q2 005 - Purple Device Makes - 450 Ratings', 'Registration Q2 2021 - Purple Device Makes - 450 Ratings', 'Registration Q4 000 - Purple Device Makes - 450 Ratings', 'Registration Q4 001 - Purple Device Makes - 450 Ratings', 'Registration Q4 002 - Purple Device Makes - 450 Ratings', 'Registration Q4 005 - Purple Device Makes - 450 Ratings', 'Registration Q4 006 - Purple Device Makes - 450 Ratings')) THEN 'Purple Device Makes' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Purple Device Makes Non-Waterproof - Any-American', 'Registration Q2 001 - Purple Device Makes Non-Waterproof - Any-American', 'Registration Q2 002 - Purple Device Makes Non-Waterproof - Any-American', 'Registration Q4 000 - Purple Device Makes Non-Waterproof - Any-American')) THEN 'Purple Device Makes Non-Waterproof - Any-American' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Waterproof Speakers', 'Registration Q2 001 - Waterproof Speakers', 'Registration Q2 002 - Waterproof Speakers', 'Registration Q2 005 - Waterproof Speakers', 'Registration Q2 2021 - Waterproof Speakers', 'Registration Q4 000 - Waterproof Speakers', 'Registration Q4 001 - Waterproof Speakers', 'Registration Q4 002 - Waterproof Speakers', 'Registration Q4 005 - Waterproof Speakers', 'Registration Q4 006 - Waterproof Speakers')) THEN 'Waterproof Speakers' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Waterproof Makes - 450 Ratings', 'Registration Q2 001 - Waterproof Makes - 450 Ratings', 'Registration Q2 002 - Waterproof Makes - 450 Ratings', 'Registration Q2 005 - Waterproof Makes - 450 Ratings', 'Registration Q2 2021 - Waterproof Makes - 450 Ratings', 'Registration Q4 000 - Waterproof Makes - 450 Ratings', 'Registration Q4 001 - Waterproof Makes - 450 Ratings', 'Registration Q4 002 - Waterproof Makes - 450 Ratings', 'Registration Q4 005 - Waterproof Makes - 450 Ratings', 'Registration Q4 006 - Waterproof Makes - 450 Ratings')) THEN 'Waterproof Makes' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 001 - Waterproof Propane Redsize', 'Registration Q2 002 - Waterproof Propane Redsize', 'Registration Q2 005 - Waterproof Propane Redsize', 'Registration Q2 2021 - Waterproof Propane Redsize', 'Registration Q4 001 - Waterproof Propane Redsize', 'Registration Q4 002 - Waterproof Propane Redsize', 'Registration Q4 005 - Waterproof Propane Redsize', 'Registration Q4 006 - Waterproof Propane Redsize')) THEN 'Waterproof Propane Redsize' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 001 - Waterproof Propane Small', 'Registration Q2 002 - Waterproof Propane Small', 'Registration Q2 005 - Waterproof Propane Small', 'Registration Q4 001 - Waterproof Propane Small', 'Registration Q4 002 - Waterproof Propane Small', 'Registration Q4 005 - Waterproof Propane Small', 'Registration Q4 006 - Waterproof Propane Small')) THEN 'Waterproof Propane Small' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Red-Junk - Waterproof', 'Registration Q2 001 - Red-Junk - Waterproof', 'Registration Q2 002 - Red-Junk - Waterproof', 'Registration Q2 005 - Red-Junk - Waterproof', 'Registration Q2 2021 - Red-Junk - Waterproof', 'Registration Q4 000 - Red-Junk - Waterproof', 'Registration Q4 001 - Red-Junk - Waterproof', 'Registration Q4 002 - Red-Junk - Waterproof', 'Registration Q4 005 - Red-Junk - Waterproof', 'Registration Q4 006 - Red-Junk - Waterproof')) THEN 'Red-Junk Waterproof' WHEN ("TABLE1"."DESCRIPTION" = 'Registration Q2 2021 - Redsize Amps') THEN 'Redsize Amps' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Near-Entry Waterproof', 'Registration Q2 001 - Near-Entry Waterproof', 'Registration Q2 002 - Near-Entry Waterproof', 'Registration Q2 005 - Near-Entry Waterproof', 'Registration Q2 2021 - Near-Entry Waterproof', 'Registration Q4 000 - Near-Entry Waterproof', 'Registration Q4 001 - Near-Entry Waterproof', 'Registration Q4 002 - Near-Entry Waterproof', 'Registration Q4 005 - Near-Entry Waterproof', 'Registration Q4 006 - Near-Entry Waterproof')) THEN 'Near-Entry Waterproof' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Home Theaters - Smooth', 'Registration Q2 001 - Home Theaters - Smooth', 'Registration Q2 002 - Home Theaters - Smooth', 'Registration Q2 005 - Home Theaters - Smooth', 'Registration Q2 2021 - Home Theaters - Smooth', 'Registration Q4 000 - Home Theaters - Smooth', 'Registration Q4 001 - Home Theaters - Smooth', 'Registration Q4 002 - Home Theaters - Smooth', 'Registration Q4 005 - Home Theaters - Smooth', 'Registration Q4 006 - Home Theaters - Smooth')) THEN 'Home Theaters - Smooth' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Home Theaters - Fullsize', 'Registration Q2 001 - Home Theaters - Fullsize', 'Registration Q2 002 - Home Theaters - Fullsize', 'Registration Q2 005 - Home Theaters - Fullsize', 'Registration Q2 2021 - Home Theaters - Fullsize', 'Registration Q4 000 - Home Theaters - Fullsize', 'Registration Q4 001 - Home Theaters - Fullsize', 'Registration Q4 002 - Home Theaters - Fullsize', 'Registration Q4 005 - Home Theaters - Fullsize', 'Registration Q4 006 - Home Theaters - Fullsize')) THEN 'Home Theaters - Fullsize' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Home Theaters - Fullsize - Half Full', 'Registration Q2 001 - Home Theaters - Fullsize - Half Full', 'Registration Q2 002 - Home Theaters - Fullsize - Half Full', 'Registration Q2 005 - Home Theaters - Fullsize - Half Full', 'Registration Q2 2021 - Home Theaters - Fullsize - Half Full', 'Registration Q4 000 - Home Theaters - Fullsize - Half Full', 'Registration Q4 001 - Home Theaters - Fullsize - Half Full', 'Registration Q4 002 - Home Theaters - Fullsize - Half Full', 'Registration Q4 005 - Home Theaters - Fullsize - Half Full')) THEN 'Home Theaters - Fullsize - Half Full' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q1 000 - Small Amps', 'Registration Q1 001 - Small Amps', 'Registration Q1 002 - Small Amps', 'Registration Q1 005 - Small Amps', 'Registration Q1 006 - Small Amps', 'Registration Q1 2021 - Small Amps', 'Registration Q2 000 - Small Amps', 'Registration Q2 001 - Small Amps', 'Registration Q2 002 - Small Amps', 'Registration Q2 005 - Small Amps', 'Registration Q2 2021 - Small Amps', 'Registration Q3 000 - Small Amps', 'Registration Q3 001 - Small Amps', 'Registration Q3 002 - Small Amps', 'Registration Q3 006 - Small Amps', 'Registration Q4 000 - Small Amps', 'Registration Q4 001 - Small Amps', 'Registration Q4 002 - Small Amps', 'Registration Q4 005 - Small Amps', 'Registration Q4 006 - Small Amps')) THEN 'Small Amps' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Small-Entry Amps - Any-American', 'Registration Q2 001 - Small-Entry Amps - Any-American', 'Registration Q2 002 - Small-Entry Amps - Any-American', 'Registration Q4 000 - Small-Entry Amps - Any-American')) THEN 'Small-Entry Amps - Any-American' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 005 - Camper Yeah - Entry Alt', 'Registration Q4 006 - Camper Yeah - Entry Alt')) THEN 'Camper Yeah - Entry Alt' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 2021 - Camper Yeah - Fullsize', 'Registration Q4 006 - Camper Yeah - Fullsize')) THEN 'Camper Yeah - Fullsize' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Camper Yeah - Redsize', 'Registration Q2 001 - Camper Yeah - Redsize', 'Registration Q2 002 - Camper Yeah - Redsize', 'Registration Q2 005 - Camper Yeah - Redsize', 'Registration Q2 2021 - Camper Yeah - Redsize', 'Registration Q4 000 - Camper Yeah - Redsize', 'Registration Q4 001 - Camper Yeah - Redsize', 'Registration Q4 002 - Camper Yeah - Redsize', 'Registration Q4 005 - Camper Yeah - Redsize', 'Registration Q4 006 - Camper Yeah - Redsize')) THEN 'Camper Yeah - Redsize' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q1 000 - Camper Yeah - Small', 'Registration Q1 001 - Camper Yeah - Small', 'Registration Q1 002 - Camper Yeah - Small', 'Registration Q1 005 - Camper Yeah - Small', 'Registration Q1 006 - Camper Yeah - Small', 'Registration Q1 2021 - Camper Yeah - Small', 'Registration Q2 000 - Camper Yeah - Small', 'Registration Q2 001 - Camper Yeah - Small', 'Registration Q2 002 - Camper Yeah - Small', 'Registration Q2 005 - Camper Yeah - Small', 'Registration Q2 2021 - Camper Yeah - Small', 'Registration Q3 000 - Camper Yeah - Small', 'Registration Q3 001 - Camper Yeah - Small', 'Registration Q3 002 - Camper Yeah - Small', 'Registration Q3 006 - Camper Yeah - Small', 'Registration Q4 000 - Camper Yeah - Small', 'Registration Q4 001 - Camper Yeah - Small', 'Registration Q4 002 - Camper Yeah - Small', 'Registration Q4 005 - Camper Yeah - Small', 'Registration Q4 006 - Camper Yeah - Small')) THEN 'Camper Yeah - Small' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Camper Yeah - Small - Any-American', 'Registration Q2 001 - Camper Yeah - Small - Any-American', 'Registration Q2 002 - Camper Yeah - Small - Any-American', 'Registration Q4 000 - Camper Yeah - Small - Any-American')) THEN 'Camper Yeah - Small - Any-American' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Camper Yeah - Premium Redsize Alt', 'Registration Q4 000 - Camper Yeah - Premium Redsize Alt')) THEN 'Camper Yeah Premium Redsize Alt' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 002 - Camper Wagon', 'Registration Q2 005 - Camper Wagon', 'Registration Q2 2021 - Camper Wagon', 'Registration Q4 002 - Camper Wagon', 'Registration Q4 005 - Camper Wagon', 'Registration Q4 006 - Camper Wagon')) THEN 'Camper Wagon' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 005 - Campers Amps', 'Registration Q2 2021 - Campers Amps', 'Registration Q4 005 - Campers Amps', 'Registration Q4 006 - Campers Amps')) THEN 'Campers Amps' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Campery', 'Registration Q2 001 - Campery', 'Registration Q2 002 - Campery', 'Registration Q2 005 - Campery', 'Registration Q2 2021 - Campery', 'Registration Q4 000 - Campery', 'Registration Q4 001 - Campery', 'Registration Q4 002 - Campery', 'Registration Q4 005 - Campery', 'Registration Q4 006 - Campery')) THEN 'Campery' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q1 000 - Upper Reddle Alt', 'Registration Q1 001 - Upper Reddle Alt', 'Registration Q1 002 - Upper Reddle Alt', 'Registration Q1 005 - Upper Reddle Alt', 'Registration Q1 006 - Upper Reddle Alt', 'Registration Q1 2021 - Upper Reddle Alt', 'Registration Q2 000 - Upper Reddle Alt', 'Registration Q2 001 - Upper Reddle Alt', 'Registration Q2 002 - Upper Reddle Alt', 'Registration Q2 005 - Upper Reddle Alt', 'Registration Q3 000 - Upper Reddle Alt', 'Registration Q3 001 - Upper Reddle Alt', 'Registration Q3 002 - Upper Reddle Alt', 'Registration Q3 006 - Upper Reddle Alt', 'Registration Q4 000 - Upper Reddle Alt', 'Registration Q4 001 - Upper Reddle Alt', 'Registration Q4 002 - Upper Reddle Alt', 'Registration Q4 005 - Upper Reddle Alt', 'Registration Q4 006 - Upper Reddle Alt')) THEN 'Upper Reddle Alt' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Upper Reddle Alt - Any-American', 'Registration Q2 001 - Upper Reddle Alt - Any-American', 'Registration Q2 002 - Upper Reddle Alt - Any-American', 'Registration Q4 000 - Upper Reddle Alt - Any-American')) THEN 'Upper Reddle Alt - Any-American' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Fires - Smooth', 'Registration Q2 001 - Fires - Smooth', 'Registration Q2 002 - Fires - Smooth', 'Registration Q2 005 - Fires - Smooth', 'Registration Q2 2021 - Fires - Smooth', 'Registration Q4 000 - Fires - Smooth', 'Registration Q4 001 - Fires - Smooth', 'Registration Q4 002 - Fires - Smooth', 'Registration Q4 005 - Fires - Smooth', 'Registration Q4 006 - Fires - Smooth')) THEN 'Fires - Smooth' ELSE "TABLE1"."DESCRIPTION" END) <= 'Fires - Smooth') AND ((CASE WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q1 000 - Entry Amps', 'Registration Q1 000 - Purple Device Makes - 450 Ratings', 'Registration Q1 000 - Small Amps', 'Registration Q1 000 - Camper Yeah - Small', 'Registration Q1 000 - Upper Reddle Alt')) THEN '000 Q1' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 000 - Entry Alt Propane', 'Registration Q2 000 - Entry Amps', 'Registration Q2 000 - Fullsize Amp Alt', 'Registration Q2 000 - Purple Device Makes - 450 Ratings', 'Registration Q2 000 - Purple Device Makes Non-Waterproof - Any-American', 'Registration Q2 000 - Waterproof Speakers', 'Registration Q2 000 - Waterproof Makes - 450 Ratings', 'Registration Q2 000 - Red-Junk - Waterproof', 'Registration Q2 000 - Near-Entry Waterproof', 'Registration Q2 000 - Home Theaters - Smooth', 'Registration Q2 000 - Home Theaters - Fullsize', 'Registration Q2 000 - Home Theaters - Fullsize - Half Full', 'Registration Q2 000 - Small Amps', 'Registration Q2 000 - Small-Entry Amps - Any-American', 'Registration Q2 000 - Camper Yeah - Redsize', 'Registration Q2 000 - Camper Yeah - Premium Redsize Alt', 'Registration Q2 000 - Camper Yeah - Small', 'Registration Q2 000 - Camper Yeah - Small - Any-American', 'Registration Q2 000 - Campery', 'Registration Q2 000 - Upper Reddle Alt', 'Registration Q2 000 - Upper Reddle Alt - Any-American', 'Registration Q2 000 - Fires - Smooth')) THEN '000 Q2' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q3 000 - Small Amps', 'Registration Q3 000 - Camper Yeah - Small', 'Registration Q3 000 - Upper Reddle Alt')) THEN '000 Q3' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q4 000 - Alternative Capacitor Devices', 'Registration Q4 000 - Entry Alt Propane', 'Registration Q4 000 - Entry Amps', 'Registration Q4 000 - Fullsize Amp Alt', 'Registration Q4 000 - Purple Device Makes - 450 Ratings', 'Registration Q4 000 - Purple Device Makes Non-Waterproof - Any-American', 'Registration Q4 000 - Waterproof Speakers', 'Registration Q4 000 - Waterproof Makes - 450 Ratings', 'Registration Q4 000 - Red-Junk - Waterproof', 'Registration Q4 000 - Near-Entry Waterproof', 'Registration Q4 000 - Home Theaters - Smooth', 'Registration Q4 000 - Home Theaters - Fullsize', 'Registration Q4 000 - Home Theaters - Fullsize - Half Full', 'Registration Q4 000 - Small Amps', 'Registration Q4 000 - Small-Entry Amps - Any-American', 'Registration Q4 000 - Camper Yeah - Redsize', 'Registration Q4 000 - Camper Yeah - Premium Redsize Alt', 'Registration Q4 000 - Camper Yeah - Small', 'Registration Q4 000 - Camper Yeah - Small - Any-American', 'Registration Q4 000 - Campery', 'Registration Q4 000 - Upper Reddle Alt', 'Registration Q4 000 - Upper Reddle Alt - Any-American', 'Registration Q4 000 - Fires - Smooth')) THEN '000 Q4' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q1 001 - Small Amps', 'Registration Q1 001 - Camper Yeah - Small', 'Registration Q1 001 - Upper Reddle Alt')) THEN '001 Q1' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 001 - Alternative Capacitor Devices - Stuff Sample', 'Registration Q2 001 - Entry Alt Propane', 'Registration Q2 001 - Entry Amps', 'Registration Q2 001 - Fullsize Amp Alt', 'Registration Q2 001 - Purple Device Makes - 450 Ratings', 'Registration Q2 001 - Purple Device Makes Non-Waterproof - Any-American', 'Registration Q2 001 - Waterproof Speakers', 'Registration Q2 001 - Waterproof Makes - 450 Ratings', 'Registration Q2 001 - Waterproof Propane Redsize', 'Registration Q2 001 - Waterproof Propane Small', 'Registration Q2 001 - Red-Junk - Waterproof', 'Registration Q2 001 - Near-Entry Waterproof', 'Registration Q2 001 - Home Theaters - Smooth', 'Registration Q2 001 - Home Theaters - Fullsize', 'Registration Q2 001 - Home Theaters - Fullsize - Half Full', 'Registration Q2 001 - Small Amps', 'Registration Q2 001 - Small-Entry Amps - Any-American', 'Registration Q2 001 - Camper Yeah - Redsize', 'Registration Q2 001 - Camper Yeah - Small', 'Registration Q2 001 - Camper Yeah - Small - Any-American', 'Registration Q2 001 - Campery', 'Registration Q2 001 - Upper Reddle Alt', 'Registration Q2 001 - Upper Reddle Alt - Any-American', 'Registration Q2 001 - Fires - Smooth')) THEN '001 Q2' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q3 001 - Small Amps', 'Registration Q3 001 - Camper Yeah - Small', 'Registration Q3 001 - Upper Reddle Alt')) THEN '001 Q3' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q4 001 - Alternative Capacitor Devices - Stuff Sample', 'Registration Q4 001 - Entry Alt Propane', 'Registration Q4 001 - Entry Amps', 'Registration Q4 001 - Fullsize Amp Alt', 'Registration Q4 001 - Purple Device Makes - 450 Ratings', 'Registration Q4 001 - Waterproof Speakers', 'Registration Q4 001 - Waterproof Makes - 450 Ratings', 'Registration Q4 001 - Waterproof Propane Redsize', 'Registration Q4 001 - Waterproof Propane Small', 'Registration Q4 001 - Red-Junk - Waterproof', 'Registration Q4 001 - Near-Entry Waterproof', 'Registration Q4 001 - Home Theaters - Smooth', 'Registration Q4 001 - Home Theaters - Fullsize', 'Registration Q4 001 - Home Theaters - Fullsize - Half Full', 'Registration Q4 001 - Small Amps', 'Registration Q4 001 - Camper Yeah - Redsize', 'Registration Q4 001 - Camper Yeah - Small', 'Registration Q4 001 - Campery', 'Registration Q4 001 - Upper Reddle Alt', 'Registration Q4 001 - Fires - Smooth')) THEN '001 Q4' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q1 002 - Small Amps', 'Registration Q1 002 - Camper Yeah - Small', 'Registration Q1 002 - Upper Reddle Alt')) THEN '002 Q1' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 002 - Alternative Capacitor Devices - Stuff Sample', 'Registration Q2 002 - Entry Alt Propane', 'Registration Q2 002 - Entry Amps', 'Registration Q2 002 - Fullsize Amp Alt', 'Registration Q2 002 - Purple Device Makes - 450 Ratings', 'Registration Q2 002 - Purple Device Makes Non-Waterproof - Any-American', 'Registration Q2 002 - Waterproof Speakers', 'Registration Q2 002 - Waterproof Makes - 450 Ratings', 'Registration Q2 002 - Waterproof Propane Redsize', 'Registration Q2 002 - Waterproof Propane Small', 'Registration Q2 002 - Red-Junk - Waterproof', 'Registration Q2 002 - Near-Entry Waterproof', 'Registration Q2 002 - Home Theaters - Smooth', 'Registration Q2 002 - Home Theaters - Fullsize', 'Registration Q2 002 - Home Theaters - Fullsize - Half Full', 'Registration Q2 002 - Small Amps', 'Registration Q2 002 - Small-Entry Amps - Any-American', 'Registration Q2 002 - Camper Yeah - Redsize', 'Registration Q2 002 - Camper Yeah - Small', 'Registration Q2 002 - Camper Yeah - Small - Any-American', 'Registration Q2 002 - Camper Wagon', 'Registration Q2 002 - Campery', 'Registration Q2 002 - Upper Reddle Alt', 'Registration Q2 002 - Upper Reddle Alt - Any-American', 'Registration Q2 002 - Fires - Smooth')) THEN '002 Q2' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q3 002 - Small Amps', 'Registration Q3 002 - Camper Yeah - Small', 'Registration Q3 002 - Upper Reddle Alt')) THEN '002 Q3' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q4 002 - Alternative Capacitor Devices - Stuff Sample', 'Registration Q4 002 - Entry Alt Propane', 'Registration Q4 002 - Entry Amps', 'Registration Q4 002 - Fullsize Amp Alt', 'Registration Q4 002 - Purple Device Makes - 450 Ratings', 'Registration Q4 002 - Waterproof Speakers', 'Registration Q4 002 - Waterproof Makes - 450 Ratings', 'Registration Q4 002 - Waterproof Propane Redsize', 'Registration Q4 002 - Waterproof Propane Small', 'Registration Q4 002 - Red-Junk - Waterproof', 'Registration Q4 002 - Near-Entry Waterproof', 'Registration Q4 002 - Home Theaters - Smooth', 'Registration Q4 002 - Home Theaters - Fullsize', 'Registration Q4 002 - Home Theaters - Fullsize - Half Full', 'Registration Q4 002 - Small Amps', 'Registration Q4 002 - Camper Yeah - Redsize', 'Registration Q4 002 - Camper Yeah - Small', 'Registration Q4 002 - Camper Wagon', 'Registration Q4 002 - Campery', 'Registration Q4 002 - Upper Reddle Alt', 'Registration Q4 002 - Fires - Smooth')) THEN '002 Q4' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q1 005 - Small Amps', 'Registration Q1 005 - Camper Yeah - Small', 'Registration Q1 005 - Upper Reddle Alt')) THEN '005 Q1' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 005 - Alternative Capacitor Devices - Stuff Sample', 'Registration Q2 005 - Entry Amps', 'Registration Q2 005 - Fullsize Amp Alt', 'Registration Q2 005 - Purple Device Makes - 450 Ratings', 'Registration Q2 005 - Waterproof Speakers', 'Registration Q2 005 - Waterproof Makes - 450 Ratings', 'Registration Q2 005 - Waterproof Propane Redsize', 'Registration Q2 005 - Waterproof Propane Small', 'Registration Q2 005 - Red-Junk - Waterproof', 'Registration Q2 005 - Near-Entry Waterproof', 'Registration Q2 005 - Home Theaters - Smooth', 'Registration Q2 005 - Home Theaters - Fullsize', 'Registration Q2 005 - Home Theaters - Fullsize - Half Full', 'Registration Q2 005 - Small Amps', 'Registration Q2 005 - Camper Yeah - Entry Alt', 'Registration Q2 005 - Camper Yeah - Redsize', 'Registration Q2 005 - Camper Yeah - Small', 'Registration Q2 005 - Camper Wagon', 'Registration Q2 005 - Campers Amps', 'Registration Q2 005 - Campery', 'Registration Q2 005 - Upper Reddle Alt', 'Registration Q2 005 - Fires - Smooth')) THEN '005 Q2' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q4 005 - Entry Amps', 'Registration Q4 005 - Fullsize Amp Alt', 'Registration Q4 005 - Purple Device Makes - 450 Ratings', 'Registration Q4 005 - Waterproof Speakers', 'Registration Q4 005 - Waterproof Makes - 450 Ratings', 'Registration Q4 005 - Waterproof Propane Redsize', 'Registration Q4 005 - Waterproof Propane Small', 'Registration Q4 005 - Red-Junk - Waterproof', 'Registration Q4 005 - Near-Entry Waterproof', 'Registration Q4 005 - Home Theaters - Smooth', 'Registration Q4 005 - Home Theaters - Fullsize', 'Registration Q4 005 - Home Theaters - Fullsize - Half Full', 'Registration Q4 005 - Small Amps', 'Registration Q4 005 - Camper Yeah - Entry Alt', 'Registration Q4 005 - Camper Yeah - Redsize', 'Registration Q4 005 - Camper Yeah - Small', 'Registration Q4 005 - Camper Wagon', 'Registration Q4 005 - Campers Amps', 'Registration Q4 005 - Campery', 'Registration Q4 005 - Upper Reddle Alt', 'Registration Q4 005 - Fires - Smooth')) THEN '005 Q4' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q1 006 - Small Amps', 'Registration Q1 006 - Camper Yeah - Small', 'Registration Q1 006 - Upper Reddle Alt')) THEN '006 Q1' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q3 006 - Small Amps', 'Registration Q3 006 - Camper Yeah - Small', 'Registration Q3 006 - Upper Reddle Alt')) THEN '006 Q3' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q4 006 - Alternative Capacitor Devices - Stuff Sample', 'Registration Q4 006 - Entry Amps', 'Registration Q4 006 - Fullsize Amp Alt', 'Registration Q4 006 - Purple Device Makes - 450 Ratings', 'Registration Q4 006 - Waterproof Speakers', 'Registration Q4 006 - Waterproof Makes - 450 Ratings', 'Registration Q4 006 - Waterproof Propane Redsize', 'Registration Q4 006 - Waterproof Propane Small', 'Registration Q4 006 - Red-Junk - Waterproof', 'Registration Q4 006 - Near-Entry Waterproof', 'Registration Q4 006 - Home Theaters - Smooth', 'Registration Q4 006 - Home Theaters - Fullsize', 'Registration Q4 006 - Small Amps', 'Registration Q4 006 - Camper Yeah - Entry Alt', 'Registration Q4 006 - Camper Yeah - Fullsize', 'Registration Q4 006 - Camper Yeah - Redsize', 'Registration Q4 006 - Camper Yeah - Small', 'Registration Q4 006 - Camper Wagon', 'Registration Q4 006 - Campers Amps', 'Registration Q4 006 - Campery', 'Registration Q4 006 - Upper Reddle Alt', 'Registration Q4 006 - Fires - Smooth')) THEN '006 Q4' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q1 2021 - Small Amps', 'Registration Q1 2021 - Camper Yeah - Small', 'Registration Q1 2021 - Upper Reddle Alt')) THEN '2021 Q1' WHEN ("TABLE1"."DESCRIPTION" IN ('Registration Q2 2021 - Alternative Capacitor Devices - Stuff Sample', 'Registration Q2 2021 - Fullsize Amp Alt', 'Registration Q2 2021 - Purple Device Makes - 450 Ratings', 'Registration Q2 2021 - Waterproof Speakers', 'Registration Q2 2021 - Waterproof Makes - 450 Ratings', 'Registration Q2 2021 - Waterproof Propane Redsize', 'Registration Q2 2021 - Red-Junk - Waterproof', 'Registration Q2 2021 - Redsize Amps', 'Registration Q2 2021 - Near-Entry Waterproof', 'Registration Q2 2021 - Home Theaters - Smooth', 'Registration Q2 2021 - Home Theaters - Fullsize', 'Registration Q2 2021 - Home Theaters - Fullsize - Half Full', 'Registration Q2 2021 - Small Amps', 'Registration Q2 2021 - Camper Yeah - Fullsize', 'Registration Q2 2021 - Camper Yeah - Redsize', 'Registration Q2 2021 - Camper Yeah - Small', 'Registration Q2 2021 - Camper Wagon', 'Registration Q2 2021 - Campers Amps', 'Registration Q2 2021 - Campery', 'Registration Q2 2021 - Fires - Smooth')) THEN '2021 Q2' ELSE "TABLE1"."DESCRIPTION" END) = '2021 Q2') AND ((CASE WHEN ("TABLE1"."CODE" = 'No Answer') THEN 0 ELSE 1 END) <> 0) AND ("TABLE1"."DESCRIPTION" = 'Familiar With (G1)'))
GROUP BY 1,
  2,
  3
;

-- simple_parsing.txt
sELect g.*, A.K from B, KLJ as A;

select * from TABLE_A;

select * from TABLE_A;

select * from TABLE_A LIMIT 34;

select * from TABLE_A LIMIT ?;

select * from TABLE_A LIMIT 34,?;

select * from TABLE_A LIMIT ?,?;

select * from TABLE_A LIMIT ? OFFSET 3;

select * from TABLE_A LIMIT ? OFFSET ?;

select * from TABLE_A LIMIT ALL OFFSET ?;

select * from TABLE_A LIMIT ALL OFFSET 3;

select * from TABLE_A OFFSET 3;

select A,sdf,sch.tab.col from TABLE_A;

select k, * from K as skldjfl where i=0;

select MAX(k+2), COUNT(*), MYCOL from K;

SELECT * FROM TA2 LEFT JOIN O USING (col1, col2)
where D.OasSD = 'asdf' And (kj >= 4 OR l < 'sdf');

seLECT 	my as KIO, lio aS
NE fRom TA2 LEFT OUter 		JOIN O as TA3
where D.OasSD = 'asdf' And (kj >= 4 OR l < 'sdf');

select * from a
INNer Join TAB_2 ON i.o = p.l whEre 'sdf'>'asdf' AND
	(
	OL<>?
			OR
	L NOT IN (SELECT * FROM KJSD)
	);

select * from k where L IS NOT NUll;

(select sdf from sdfd) UNION (select * from k);

update mytab set jk=das, d=kasd+asd/d+3 where KL>= ds OR (k not in (SELECT K from KS));

insert into tabName VALUES ('sdgf', ?, ?);

insert into myschama.tabName2 (col1, col2, col3) VALUES ('sdgf', ?, ?);

delete from jk;

delete from asdff where INI = 94 OR (ASD>9 AND (SELECT MAX(ID) from myt) > ?);

select  *  from  (  SELECT  intermediate.id  as  id  ,  intermediate.date  as
date  FROM  (  SELECT  DISTINCT  ON  (  id  )  *  FROM  (  SELECT
wct_workflows.workflow_id  as  id  ,  wct_transaction.date  as  date  FROM
wct_audit_entry  ,  wct_transaction  ,  wct_workflows  WHERE
(  wct_audit_entry.privilege  =  'W'  or  wct_audit_entry.privilege  =
'C'  )  and  wct_audit_entry.outcome  =  't'  and
wct_audit_entry.transaction_id  =  wct_transaction.transaction_id  and
wct_transaction.user_id  = 164 and  wct_audit_entry.object_id  =
wct_workflows.active_version_id)));

(select  *  from  (  SELECT  intermediate.id  as  id  ,  intermediate.date  as
date  FROM  (  SELECT  DISTINCT   (  id  )  FROM  (  SELECT
wct_workflows.workflow_id  as  id  ,  wct_transaction.date  as  date  FROM
wct_audit_entry  ,  wct_transaction  ,  wct_workflows  WHERE
(  wct_audit_entry.privilege  =  'W'  or  wct_audit_entry.privilege  =
'C'  )  and  wct_audit_entry.outcome  =  't'  and
wct_audit_entry.transaction_id  =  wct_transaction.transaction_id  and
wct_transaction.user_id  = 164 and  wct_audit_entry.object_id  =
wct_workflows.active_version_id))))  UNION ( SELECT  wct_workflows.workflow_id  as
id  ,  wct_transaction.date  as  date  FROM  wct_audit_entry  ,
wct_transaction  ,  wct_workflows  WHERE  (  wct_audit_entry.privilege  =
'W'  or  wct_audit_entry.privilege  =  'C'  )  and  wct_audit_entry.outcome
=  't'  and  wct_audit_entry.transaction_id  =
wct_transaction.transaction_id  and  wct_transaction.user_id  = 164 and
p= 'asd');

select  *  from  (  SELECT  intermediate.id  as  id  ,  intermediate.date  as
date  FROM  (  SELECT  DISTINCT   (  id  )   FROM  (  SELECT
wct_workflows.workflow_id  as  id  ,  wct_transaction.date  as  date  FROM
wct_audit_entry  ,  wct_transaction  ,  wct_workflows  WHERE
(  wct_audit_entry.privilege  =  'W'  or  wct_audit_entry.privilege  =
'C'  )  and  wct_audit_entry.outcome  =  't'  and
wct_audit_entry.transaction_id  =  wct_transaction.transaction_id  and
wct_transaction.user_id  = 164 and  wct_audit_entry.object_id  =
wct_workflows.active_version_id ))) UNION  SELECT  wct_workflows.workflow_id  as
id  ,  wct_transaction.date  as  date  FROM  wct_audit_entry  ,
wct_transaction  ,  wct_workflows  WHERE  (  wct_audit_entry.privilege  =
'W'  or  wct_audit_entry.privilege  =  'C'  )  and  wct_audit_entry.outcome
=  't'  and  wct_audit_entry.transaction_id  =
wct_transaction.transaction_id  and  wct_transaction.user_id  = 164 and
afdf=  (  select  wct_audit_entry.object_id  from  wct_audit_entry  ,
wct_workflow_archive  where  wct_audit_entry.object_id  =
wct_workflow_archive.archive_id  and  wct_workflows.workflow_id  =
wct_workflow_archive.workflow_id  )
UNION  SELECT  wct_workflows.workflow_id
as  id  ,  wct_transaction.date  as  date  FROM  wct_audit_entry  ,
wct_transaction  ,  wct_workflows  WHERE  (  wct_audit_entry.privilege  =
'W'  OR  wct_audit_entry.privilege  =  'E'  OR  wct_audit_entry.privilege  =
'A'  )  and  wct_audit_entry.outcome  =  't'  and
wct_audit_entry.transaction_id  =  wct_transaction.transaction_id  and
wct_transaction.user_id  = 164 and  wct_audit_entry.object_id  =
wct_workflows.workflow_id    UNION SELECT * FROM interm2  ,  wct_workflow_docs  WHERE
interm2.id  =  wct_workflow_docs.document_id  ORDER BY  id  ,  date  DESC
;

replace df set ki='oasdf', dsd=asd+dd;

(select sdf from sdfd) UNION (select * from k) ORDER BY 1,2;

(select sdf from sdfd) UNION (select * from k) ORDER BY 1,asd.sd ;

(select sdf from sdfd) UNION (select * from k) UNION (select * from k2) LIMIT 0,2;

select sdf from sdfd UNION select * from k join j on k.p = asdf.f;

select  *  from  (  select  persistence_dynamic_ot.pdl_id  ,
acs_objects.default_domain_class  as  attribute0  ,
acs_objects.object_type  as  attribute1  ,  acs_objects.display_name
as  attribute2  ,  persistence_dynamic_ot.dynamic_object_type  as
attribute3  ,  persistence_dynamic_ot.pdl_file  as  attribute4  from
persistence_dynamic_ot  ,  acs_objects  where
persistence_dynamic_ot.pdl_id  =  acs_objects.object_id  );

SELECT * FROM table1 WHERE column1 > ALL (SELECT column2 FROM table1);

INSERT INTO mytable (col1, col2, col3) SELECT * FROM mytable2;

insert into foo ( x )  select a from b;

select (case when a > 0 then b + a else 0 end) p from mytable;

SELECT BTI.*, BTI_PREDECESSOR.objid AS predecessor_objid, BTI_PREDECESSOR.item_id
AS predecessor_item_id, BTIT_PREDECESSOR.bt_item_type_key AS predecessor_type_key,
CAT.catalog_key, S.objid AS state_objid, S.state_key, S.is_init_state,
S.is_final_state, mlS.name AS state, BTIT.bt_item_type_key, BTP.bt_processor_key,
mlBTP.name AS bt_processor_name , CU.objid AS cust_user_objid , CU.title AS
cust_user_title , CU.firstname AS cust_user_firstname , CU.lastname AS
cust_user_lastname , CU.salutation2pv AS cust_user_salutation2pv , PV_CU.name_option
AS cust_user_salutation , A_CU.email AS cust_user_email , '' AS use_option_field,
'' AS use_readerlist , BTI_QUOTATION.quotation_type2pv , BTI_QUOTATION.is_mandatory
AS quotation_is_mandatory , BTI_QUOTATION.is_multiple AS quotation_is_multiple
, BTI_QUOTATION.expiration_datetime AS quotation_expiration_datetime ,
BTI_QUOTATION.hint_internal AS quotation_hint_internal , BTI_QUOTATION.hint_external
AS quotation_hint_external , BTI_QUOTATION.filter_value AS quotation_filter_value
, BTI_QUOTATION.email_cc AS quotation_email_cc , BTI_QUOTATION.notification1_datetime
AS notification1_datetime , BTI_QUOTATION.notification2_datetime AS
notification2_datetime , BTI_RFQ.filter_value AS request_for_quotation_filter_value
FROM tBusinessTransactionItem BTI LEFT OUTER JOIN tBusinessTransactionItem_Quotation
BTI_QUOTATION ON BTI_QUOTATION.this2business_transaction_item = BTI.objid LEFT
OUTER JOIN tBusinessTransactionItem_RequestForQuotation BTI_RFQ ON
BTI_RFQ.this2business_transaction_item = BTI.objid LEFT OUTER JOIN
tBusinessTransactionItem BTI_PREDECESSOR ON BTI_PREDECESSOR.objid
= BTI.predecessor2bt_item, tBusinessTransactionItemType BTIT_PREDECESSOR
, tBusinessTransactionItemType BTIT, tBusinessTransactionProcessor BTP,
mltBusinessTransactionProcessor mlBTP, tLanguagePriority LP_BTP, tState S, mltState
mlS, tLanguagePriority LP_S, tCatalog CAT
, tBusinessTransactionItem2BusinessTransaction BTI2BT ,
tBusinessTransactionItem2SessionCart BTI2SC , tSessionCart SC , tCustUser CU_MASTER
, tCustUser CU , tPopValue PV_CU , tAddress A_CU , tAddress2CustUser A2CU WHERE
BTI.objid <> -1 AND BTI_PREDECESSOR.this2bt_item_type = BTIT_PREDECESSOR.objid
AND BTI.this2bt_item_type = BTIT.objid AND BTI.this2bt_processor = BTP.objid
AND mlBTP.this2master = BTP.objid AND mlBTP.this2language = LP_BTP.item2language
AND LP_BTP.master2language = 0 AND LP_BTP.this2shop = 0 AND LP_BTP.priority
= (SELECT MIN(LP_BTP2.priority) FROM tLanguagePriority LP_BTP2,
mltBusinessTransactionProcessor mlBTP2 WHERE LP_BTP2.master2language = 0 AND
LP_BTP2.this2shop = 0 AND LP_BTP2.item2language = mlBTP2.this2language
AND mlBTP2.this2master = BTP.objid ) AND BTI.this2catalog = CAT.objid AND S.objid
= BTI.bt_item2state AND mlS.this2master = S.objid AND mlS.this2language
= LP_S.item2language AND LP_S.master2language = 0 AND LP_S.this2shop = 0 AND
LP_S.priority = (SELECT MIN(LP_S2.priority) FROM tLanguagePriority LP_S2, mltState
mlS2 WHERE LP_S2.master2language = 0 AND LP_S2.this2shop = 0 AND LP_S2.item2language
= mlS2.this2language AND mlS2.this2master = S.objid ) AND BTI.objid
= BTI2BT.this2business_transaction_item AND CU_MASTER.objid = 1101 AND
CU.this2customer = CU_MASTER.this2customer AND SC.this2custuser = CU.objid AND
BTI.objid = BTI2SC.this2business_transaction_item AND BTI.bt_item2state = 6664
AND BTI2SC.is_master_cart_item = 1 AND BTI2SC.this2session_cart = SC.objid AND
EXISTS (SELECT NULL FROM tBusinessTransaction BT, tBusinessTransactionType BTT
WHERE BT.objid = BTI2BT.this2business_transaction AND BTT.objid = BT.this2bt_type
AND BTT.business_transaction_type_key = 'order:master_cart') AND PV_CU.objid
= CU.salutation2pv AND A2CU.this2custuser = CU.objid AND A2CU.is_billing_default
= 1 AND A2CU.this2address = A_CU.objid ORDER BY BTI.dbobj_create_datetime DESC;

WITH
DINFO (DEPTNO, AVGSALARY, EMPCOUNT) AS
(SELECT OTHERS.WORKDEPT, AVG(OTHERS.SALARY), COUNT(*)
FROM EMPLOYEE OTHERS
GROUP BY OTHERS.WORKDEPT
),
DINFOMAX AS
(SELECT MAX(AVGSALARY) AS AVGMAX FROM DINFO)
SELECT THIS_EMP.EMPNO, THIS_EMP.SALARY,
DINFO.AVGSALARY, DINFO.EMPCOUNT, DINFOMAX.AVGMAX
FROM EMPLOYEE THIS_EMP, DINFO, DINFOMAX
WHERE THIS_EMP.JOB = 'SALESREP'
AND THIS_EMP.WORKDEPT = DINFO.DEPTNO;

select * from Person where deptname='it' AND NOT (age=24);

select * from unnest(array[4,5,6]) with ordinality;


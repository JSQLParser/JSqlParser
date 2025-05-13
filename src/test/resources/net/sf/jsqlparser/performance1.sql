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


-- large-sql-issue-923-2.txt
 WITH
 ---------------------------------------------------------------------------------------------------------
 AP_CLAIMS AS
  (
       SELECT CLM.INTERNAL_ID AS CLAIM_ID,
       AP.CLAIM_ID AS CLM,
       ADJST_CLM_ID,
       TOT_BILLED_AMT,
       TOT_NET_PAYABLE,
       ENTRY_DATE,
       ORIG_REV_CLM_ID,
       ORIG_ADJST_CLM_ID,
       EPM.PAYOR_NAME,
       CKR.CHECK_NUMBER,
       CKR.CHECK_DATE,
       COALESCE(CLM_EOB.CLM_CD_1,'') ||CASE WHEN CLM_EOB.CLM_CD_2 IS NULL OR CLM_EOB.CLM_CD_2='' THEN '' ELSE ', ' END ||
       COALESCE(CLM_EOB.CLM_CD_2,'') ||CASE WHEN CLM_EOB.CLM_CD_3 IS NULL OR CLM_EOB.CLM_CD_3='' THEN '' ELSE ', ' END ||
       COALESCE(CLM_EOB.CLM_CD_3,'') || CASE WHEN CLM_EOB.CLM_CD_4 IS NULL OR CLM_EOB.CLM_CD_4='' THEN '' ELSE ', ' END ||
       COALESCE(CLM_EOB.CLM_CD_4,'') ||CASE WHEN CLM_EOB.CLM_CD_5 IS NULL OR CLM_EOB.CLM_CD_5='' THEN '' ELSE ', ' END ||
       COALESCE(CLM_EOB.CLM_CD_5,'') ||CASE WHEN CLM_EOB.CLM_CD_6 IS NULL OR CLM_EOB.CLM_CD_6='' THEN '' ELSE ', ' END ||
       COALESCE(CLM_EOB.CLM_CD_6,'') ||CASE WHEN CLM_EOB.CLM_CD_7 IS NULL OR CLM_EOB.CLM_CD_7='' THEN '' ELSE ', ' END ||
       COALESCE(CLM_EOB.CLM_CD_7,'') ||CASE WHEN CLM_EOB.CLM_CD_8 IS NULL OR CLM_EOB.CLM_CD_8='' THEN '' ELSE ', ' END ||
       COALESCE(CLM_EOB.CLM_CD_8,'') ||CASE WHEN CLM_EOB.CLM_CD_9 IS NULL OR CLM_EOB.CLM_CD_9='' THEN '' ELSE ', ' END ||
       COALESCE(CLM_EOB.CLM_CD_9,'') ||CASE WHEN CLM_EOB.CLM_CD_10 IS NULL OR CLM_EOB.CLM_CD_10='' THEN '' ELSE ', ' END ||
       COALESCE(CLM_EOB.CLM_CD_10,'') AS CLM_EOB,
       MIN_EOB_DATE,
        CASE AP.STATUS_C WHEN 1 THEN 'NEW' WHEN 2 THEN 'PENDING' WHEN 3 THEN 'DENIED' WHEN 4 THEN 'CLEAN' WHEN 5 THEN 'VOID' ELSE ' ' END AS CLAIM_STATUS,
       CKR.AP_RUN_DATE,
       MAILED_DATE AS CHK_MAIL_DATE,
       AP2.PENALTY_AMT ,
       AP2.INTEREST_TOTAL
       FROM AP_CLAIM AP
        INNER JOIN CLM_MAP CLM ON AP.CLAIM_ID = CLM.CID
        LEFT OUTER JOIN AP_CLAIM_CHECK DOFR ON AP.CLAIM_ID = DOFR.CLAIM_ID /*1-9-2019 Matt: Switched to AP_CLAIM_CHECK for Epic 2018 upgrade */
        LEFT OUTER JOIN AP_CHECK CKR ON DOFR.CHECK_ID = CKR.CHECK_ID
        LEFT OUTER JOIN ANTHEM_EPM EPM ON EPM.PAYOR_ID = AP.PAYOR_ID
        LEFT JOIN AP_CLAIM_2 AP2 ON AP.CLAIM_ID = AP2.CLAIM_ID
        LEFT OUTER JOIN
                                (
                                     SELECT CEOB.CLAIM_ID,
                                     'Y' AS CLM_LVL_PND,
                                     MIN(CEOB.ENTRY_DATE) AS MIN_EOB_DATE,
                                     MAX(CASE WHEN CEOB.LINE=1 THEN EOB.MNEMONIC ELSE NULL END) AS CLM_CD_1,
                                     MAX(CASE WHEN CEOB.LINE=2 THEN EOB.MNEMONIC ELSE NULL END) AS CLM_CD_2,
                                     MAX(CASE WHEN CEOB.LINE=3 THEN EOB.MNEMONIC ELSE NULL END) AS CLM_CD_3,
                                     MAX( CASE WHEN CEOB.LINE=4 THEN EOB.MNEMONIC ELSE NULL END) AS CLM_CD_4,
                                     MAX(CASE WHEN CEOB.LINE=5 THEN EOB.MNEMONIC ELSE NULL END) AS CLM_CD_5,
                                     MAX(CASE WHEN CEOB.LINE=6 THEN EOB.MNEMONIC ELSE NULL END) AS CLM_CD_6,
                                     MAX(CASE WHEN CEOB.LINE=7 THEN EOB.MNEMONIC ELSE NULL END) AS CLM_CD_7,
                                     MAX(CASE WHEN CEOB.LINE=8 THEN EOB.MNEMONIC ELSE NULL END) AS CLM_CD_8,
                                     MAX(CASE WHEN CEOB.LINE=9 THEN EOB.MNEMONIC ELSE NULL END) AS CLM_CD_9,
                                     MAX(CASE WHEN CEOB.LINE=10 THEN EOB.MNEMONIC ELSE NULL END) AS CLM_CD_10
                                     FROM
                                     AP_CLAIM_EOB_CD CEOB
                                     LEFT OUTER JOIN ANTHEM_EOB_CODE EOB ON CEOB.EOB_CODE_ID=EOB.EOB_CODE_ID
                                     WHERE EOB.CODE_TYPE_C = 2
                                     GROUP BY CEOB.CLAIM_ID
                               )CLM_EOB ON CLM_EOB.CLAIM_ID = AP.CLAIM_ID --WHERE CLM.INTERNAL_ID IN (2350795,3416519,4239160,6151259)
       WHERE
 	NVL(DOFR.SERVICE_COUNT,0)  >  0 /*1-9-2019 Matt: Switched to AP_CLAIM_CHECK for Epic 2018 upgrade */
        GROUP BY
 	   CLM.INTERNAL_ID,
       AP.CLAIM_ID,
       ADJST_CLM_ID,
       TOT_BILLED_AMT,
       TOT_NET_PAYABLE,
       ENTRY_DATE,
       ORIG_REV_CLM_ID,
       ORIG_ADJST_CLM_ID,
       EPM.PAYOR_NAME,
       CKR.CHECK_NUMBER,
       CKR.CHECK_DATE,
       COALESCE(CLM_EOB.CLM_CD_1,'') ||CASE WHEN CLM_EOB.CLM_CD_2 IS NULL OR CLM_EOB.CLM_CD_2='' THEN '' ELSE ', ' END ||
       COALESCE(CLM_EOB.CLM_CD_2,'') ||CASE WHEN CLM_EOB.CLM_CD_3 IS NULL OR CLM_EOB.CLM_CD_3='' THEN '' ELSE ', ' END ||
       COALESCE(CLM_EOB.CLM_CD_3,'') || CASE WHEN CLM_EOB.CLM_CD_4 IS NULL OR CLM_EOB.CLM_CD_4='' THEN '' ELSE ', ' END ||
       COALESCE(CLM_EOB.CLM_CD_4,'') ||CASE WHEN CLM_EOB.CLM_CD_5 IS NULL OR CLM_EOB.CLM_CD_5='' THEN '' ELSE ', ' END ||
       COALESCE(CLM_EOB.CLM_CD_5,'') ||CASE WHEN CLM_EOB.CLM_CD_6 IS NULL OR CLM_EOB.CLM_CD_6='' THEN '' ELSE ', ' END ||
       COALESCE(CLM_EOB.CLM_CD_6,'') ||CASE WHEN CLM_EOB.CLM_CD_7 IS NULL OR CLM_EOB.CLM_CD_7='' THEN '' ELSE ', ' END ||
       COALESCE(CLM_EOB.CLM_CD_7,'') ||CASE WHEN CLM_EOB.CLM_CD_8 IS NULL OR CLM_EOB.CLM_CD_8='' THEN '' ELSE ', ' END ||
       COALESCE(CLM_EOB.CLM_CD_8,'') ||CASE WHEN CLM_EOB.CLM_CD_9 IS NULL OR CLM_EOB.CLM_CD_9='' THEN '' ELSE ', ' END ||
       COALESCE(CLM_EOB.CLM_CD_9,'') ||CASE WHEN CLM_EOB.CLM_CD_10 IS NULL OR CLM_EOB.CLM_CD_10='' THEN '' ELSE ', ' END ||
       COALESCE(CLM_EOB.CLM_CD_10,''),
       MIN_EOB_DATE,
        CASE AP.STATUS_C WHEN 1 THEN 'NEW' WHEN 2 THEN 'PENDING' WHEN 3 THEN 'DENIED' WHEN 4 THEN 'CLEAN' WHEN 5 THEN 'VOID' ELSE ' ' END,
       CKR.AP_RUN_DATE,
       MAILED_DATE,
       AP2.PENALTY_AMT ,
       AP2.INTEREST_TOTAL
 )
 -----------------------------------------------------------------------------------------------------
 ,DUPROW AS
 (
             SELECT
             CLM.INTERNAL_ID AS ORIG_INTERNAL_ID,
              AP.CLAIM_ID AS ORIG_CLAIM_ID,
              AP.DATE_RECEIVED AS ORIG_DATE_RECEIVED,  ---ADD FIELD HERE
              AP2.INTEREST_TOTAL,
             AP2.PENALTY_AMT,
             REF1.EXTERNAL_ID_NUM AS AUTH_NUM,
             LOB.LOB_NAME,
              AP.SERV_DATE,
              AP.TOT_BILLED_AMT,
              AP.TOT_NET_PAYABLE,
              OAJ.CLM_EOB AS TRUE_ORIG_EOB,
              ADJ.CLM_EOB AS ORIG_EOB,
              ADJ2.CLM_EOB AS AJ1_EOB,
              ADJ3.CLM_EOB AS AJ2_EOB,
              ADJ4.CLM_EOB AS AJ3_EOB,
              ADJ5.CLM_EOB AS AJ4_EOB,
              ADJ6.CLM_EOB AS AJ5_EOB,
              OAJ.MIN_EOB_DATE AS TRUE_ORIG_EOB_DATE,
              ADJ.MIN_EOB_DATE AS AJ1_MIN_EOB_DATE,
              ADJ2.MIN_EOB_DATE AS AJ2_MIN_EOB_DATE,
              ADJ3.MIN_EOB_DATE AS AJ3_MIN_EOB_DATE,
            ADJ4.MIN_EOB_DATE AS AJ4_MIN_EOB_DATE,
              ADJ5.MIN_EOB_DATE AS AJ5_MIN_EOB_DATE,
               CASE AP.STATUS_C WHEN 1 THEN 'NEW' WHEN 2 THEN 'PENDING' WHEN 3 THEN 'DENIED' WHEN 4 THEN 'CLEAN' WHEN 5 THEN 'VOID' ELSE ' ' END AS STATUS_C,
              VEN.VENDOR_NAME,
              COALESCE ( ADJ5.CLAIM_STATUS,ADJ4.CLAIM_STATUS,ADJ3.CLAIM_STATUS,ADJ2.CLAIM_STATUS ,ADJ.CLAIM_STATUS, (CASE AP.STATUS_C WHEN 1 THEN 'NEW' WHEN 2 THEN 'PENDING' WHEN 3 THEN 'DENIED' WHEN 4 THEN 'CLEAN' WHEN 5 THEN 'VOID' ELSE ' ' END)) AS KINAL_CLAIM_STATUS,
              COALESCE(ADJ5.TOT_NET_PAYABLE, ADJ4.TOT_NET_PAYABLE, ADJ3.TOT_NET_PAYABLE, ADJ2.TOT_NET_PAYABLE, ADJ.TOT_NET_PAYABLE, AP.TOT_NET_PAYABLE) AS KINAL_TOT_NET_PAYBALE,
              COALESCE(ADJ5.PENALTY_AMT, ADJ4.PENALTY_AMT, ADJ3.PENALTY_AMT, ADJ2.PENALTY_AMT, ADJ.PENALTY_AMT, AP2.PENALTY_AMT) AS KINAL_PENALTY_AMT,
              COALESCE(ADJ5.INTEREST_TOTAL, ADJ4.INTEREST_TOTAL, ADJ3.INTEREST_TOTAL, ADJ2.INTEREST_TOTAL, ADJ.INTEREST_TOTAL, AP2.INTEREST_TOTAL) AS KINAL_INTEREST_AMT,
              DUP.INTERNAL_ID AS DUPE_INTERNAL_ID,
              DUP.CLAIM_ID AS DUPE_CLAIM_ID,
              CRM,
              CRM_DATE,
              CRM_TOPIC,
              CRM_SUBTOPIC,
              ROW_NUMBER() OVER(PARTITION BY CLM.INTERNAL_ID ORDER BY DUP.INTERNAL_ID ) AS ROW_NUM
              FROM AP_CLAIM AP
               INNER JOIN CLM_MAP CLM ON AP.CLAIM_ID = CLM.CID AND AP.ORIG_ADJST_CLM_ID IS NULL AND AP.ORIG_REV_CLM_ID IS NULL
              INNER JOIN PATIENT PAT ON PAT.PAT_ID = AP.PAT_ID
              INNER JOIN AP_CLAIM_2 AP2 ON AP2.CLAIM_ID = AP.CLAIM_ID
              LEFT OUTER JOIN ANTHEM_VENDOR VEN ON VEN.VENDOR_ID = AP.VENDOR_ID -- ADD JOIN HERE
              LEFT OUTER JOIN CLAIM_REFERRAL_LK REF ON REF.CLAIM_ID = AP.CLAIM_ID AND REF.LINE = 1
             LEFT OUTER JOIN REFERRAL REF1 ON REF1.REFERRAL_ID = REF.REFERRAL_ID
              LEFT OUTER JOIN ANTHEM_LOB LOB ON LOB.LOB_ID = AP.CLM_LOB_ID
              LEFT OUTER JOIN (
                                                              SELECT A.SUBJ_CLAIM_ID,
                                                             MAX(CASE WHEN ROWNUMB = 1 THEN CRM END) AS CRM,
                                                              MAX(CASE WHEN ROWNUMB = 1 THEN ENTRY_DATE ELSE NULL END) AS CRM_DATE,
                                                              MAX(CASE WHEN ROWNUMB = 1 THEN NAME ELSE NULL    END) AS CRM_TOPIC,
                                                              MAX( CASE WHEN ROWNUMB = 1 THEN SUBNAME ELSE NULL END) AS CRM_SUBTOPIC
                                                                       FROM( SELECT CSS.SUBJ_CLAIM_ID,
                                                                                          CSS.ENTRY_DATE,
                                                                                         TOPIC.NAME,
                                                                                          SUB.NAME AS SUBNAME,
                                                                                          NMAP.INTERNAL_ID AS CRM,
                                                                                          ROW_NUMBER() OVER (PARTITION BY CSS.SUBJ_CLAIM_ID
                                                                                          ORDER BY RECORD_ENTRY_TIME) AS ROWNUMB
                                                                                      FROM CUST_SERVICE CSS
                                                                                      LEFT OUTER JOIN ZC_NCS_TOPIC TOPIC ON TOPIC.TOPIC_C = CSS.TOPIC_C
                                                                                      LEFT OUTER JOIN ZC_SUB_TOPIC SUB ON SUB.SUB_TOPIC_C = CSS.SUB_TOPIC_C
                                                                                      LEFT OUTER JOIN NCS_MAP NMAP ON CSS.COMM_ID=NMAP.CID
                                                                                      WHERE SUB.NAME LIKE ('%Itemized%' )
                                                                           ) A
                                                                          GROUP BY A.SUBJ_CLAIM_ID
                                                    ) CRM_NUM ON CRM_NUM.SUBJ_CLAIM_ID = AP.CLAIM_ID
             LEFT JOIN AP_CLAIMS ADJ ON AP.CLAIM_ID = ADJ.ORIG_ADJST_CLM_ID
             LEFT JOIN AP_CLAIMS ADJ2 ON ADJ.CLM= ADJ2.ORIG_ADJST_CLM_ID
             LEFT JOIN AP_CLAIMS ADJ3 ON ADJ2.CLM = ADJ3.ORIG_ADJST_CLM_ID
             LEFT JOIN AP_CLAIMS ADJ4 ON ADJ3.CLM = ADJ4.ORIG_ADJST_CLM_ID
             LEFT JOIN AP_CLAIMS ADJ5 ON ADJ4.CLM = ADJ5.ORIG_ADJST_CLM_ID
             LEFT JOIN AP_CLAIMS ADJ6 ON ADJ5.CLM = ADJ6.ORIG_ADJST_CLM_ID
             LEFT OUTER JOIN AP_CLAIMS OAJ ON OAJ.CLM = AP.CLAIM_ID
             INNER JOIN (
                              SELECT
                              AP.CLAIM_ID
                              FROM AP_CLAIM AP
                              LEFT OUTER JOIN AP_CLAIM_EOB_CODE CLMEOB ON CLMEOB.CLAIM_ID = AP.CLAIM_ID
                              LEFT OUTER JOIN ANTHEM_EOB_CODE CODE ON CODE.EOB_CODE_ID = CLMEOB.EOB_CODE_ID
                              WHERE
                              CLMEOB.EOB_CODE_ID IN (124306)-- CED23
                              AND CODE.CODE_TYPE_C = 2 --DENIAL CODES ONLY
                               GROUP BY AP.CLAIM_ID) EOB1 ON EOB1.CLAIM_ID = AP.CLAIM_ID
             LEFT OUTER JOIN (
                                                            SELECT CLAIM_ID,
                                                            SERV_DATE,
                                                            TOT_BILLED_AMT,
                                                            VENDOR_ID,
                                                            TOT_NET_PAYABLE,
                                                            CLM.INTERNAL_ID,
                                                            PAT_ID
                                                            FROM AP_CLAIM AP2
                                                            INNER JOIN CLM_MAP CLM ON CLM.CID = AP2.CLAIM_ID
                                                            WHERE AP2.ORIG_ADJST_CLM_ID IS NULL
                                                            AND AP2.ORIG_REV_CLM_ID IS NULL --FIRST PASS ORIGINAL DUPLICATES ONLY
                                                             AND AP2.STATUS_C <>5
                                                          )DUP ON DUP.SERV_DATE <= AP.SERV_DATE +3 AND DUP.SERV_DATE >= AP.SERV_DATE -3 AND AP.TOT_BILLED_AMT <= DUP.TOT_BILLED_AMT + 500 AND AP.TOT_BILLED_AMT >= DUP.TOT_BILLED_AMT - 500 AND AP.VENDOR_ID = DUP.VENDOR_ID AND DUP.PAT_ID = AP.PAT_ID AND DUP.CLAIM_ID <> AP.CLAIM_ID --ON DUP.SERV_DATE >= AP.SERV_DATE +3
             WHERE AP.STATUS_C IN (1,2,3) --ORIGINAL CLAIM HAS TO BE DENIED AND NO PAYMENT
             AND AP.TOT_NET_PAYABLE = 0
             AND AP.TOT_BILLED_AMT >=10000.00
             AND AP.ORIG_REV_CLM_ID IS NULL
             AND AP.ORIG_ADJST_CLM_ID IS NULL
 )
 -----------------------------------------------------------------------------------------------------------------------
 ,CODE1 AS
 (
     SELECT A.ORIG_INTERNAL_ID,
     A.ORIG_CLAIM_ID,
      A.SERV_DATE AS ORIG_SERV_DATE,
      A.ORIG_DATE_RECEIVED,  -----ADD NEW FIELD HERE
     A.AUTH_NUM,
     A.LOB_NAME,
      A.TOT_BILLED_AMT AS ORIG_BILLED_AMT,
       A.INTEREST_TOTAL,
      A.PENALTY_AMT,
      A.TOT_NET_PAYABLE AS ORIG_TOT_NET_PAYABLE,
      A.VENDOR_NAME AS ORIG_VENDOR,
      A.STATUS_C AS ORIG_STATUS,
      A.KINAL_CLAIM_STATUS AS ORIG_KINAL_CLAIM_STATUS,
      A.KINAL_TOT_NET_PAYBALE AS ORIG_KINAL_NET_PAYABLE,
      A.KINAL_PENALTY_AMT AS ORIG_KINAL_PENALTY_AMT,
      A.KINAL_INTEREST_AMT AS ORIG_KINAL_INTEREST_AMT,
      A.TRUE_ORIG_EOB AS ORIG_EOB,
      A.ORIG_EOB AS ADJ1_EOB,
      A.AJ1_EOB AS ADJ2_EOB,
      A.AJ2_EOB AS ADJ3_EOB,
      A.AJ3_EOB AS ADJ4_EOB,
      A.AJ4_EOB AS ADJ5_EOB,
      A.TRUE_ORIG_EOB_DATE,
      A.AJ1_MIN_EOB_DATE,
      A.AJ2_MIN_EOB_DATE,
      A.AJ3_MIN_EOB_DATE,
      A.AJ4_MIN_EOB_DATE,
      A.AJ5_MIN_EOB_DATE,
      COALESCE(A.AJ5_MIN_EOB_DATE, A.AJ4_MIN_EOB_DATE, A.AJ3_MIN_EOB_DATE, A.AJ2_MIN_EOB_DATE, A.AJ1_MIN_EOB_DATE ,A.TRUE_ORIG_EOB_DATE) AS ORIGINAL_KINAL_EOB_DATE,
      CRM,
      CRM_DATE,
      CRM_TOPIC,
      CRM_SUBTOPIC,
      MAX( CASE WHEN A.ROW_NUM = 1 THEN A.DUPE_INTERNAL_ID END) AS DUP_CLM_1,
      MAX( CASE WHEN A.ROW_NUM = 1 THEN A.DUPE_CLAIM_ID     END) AS DUP_CLM_ID_1,
      MAX( CASE WHEN A.ROW_NUM = 2 THEN A.DUPE_INTERNAL_ID END) AS DUP_CLM_2,
      MAX( CASE WHEN A.ROW_NUM = 2 THEN A.DUPE_CLAIM_ID     END) AS DUP_CLM_ID_2,
      MAX( CASE WHEN A.ROW_NUM = 3 THEN A.DUPE_INTERNAL_ID END) AS DUP_CLM_3,
      MAX( CASE WHEN A.ROW_NUM = 3 THEN A.DUPE_CLAIM_ID     END) AS DUP_CLM_ID_3,
      MAX( CASE WHEN A.ROW_NUM = 4 THEN A.DUPE_INTERNAL_ID END) AS DUP_CLM_4,
      MAX( CASE WHEN A.ROW_NUM = 4 THEN A.DUPE_CLAIM_ID END) AS DUP_CLM_ID_4,
      MAX( CASE WHEN A.ROW_NUM = 5 THEN A.DUPE_INTERNAL_ID END) AS DUP_CLM_5,
      MAX( CASE WHEN A.ROW_NUM = 5 THEN A.DUPE_CLAIM_ID     END) AS DUP_CLM_ID_5
      FROM DUPROW A
      GROUP BY
 	 A.ORIG_INTERNAL_ID,
     A.ORIG_CLAIM_ID,
      A.SERV_DATE,
      A.ORIG_DATE_RECEIVED,  -----ADD NEW FIELD HERE
     A.AUTH_NUM,
     A.LOB_NAME,
      A.TOT_BILLED_AMT,
       A.INTEREST_TOTAL,
      A.PENALTY_AMT,
      A.TOT_NET_PAYABLE,
      A.VENDOR_NAME,
      A.STATUS_C,
      A.KINAL_CLAIM_STATUS,
      A.KINAL_TOT_NET_PAYBALE,
      A.KINAL_PENALTY_AMT,
      A.KINAL_INTEREST_AMT,
      A.TRUE_ORIG_EOB,
      A.ORIG_EOB,
      A.AJ1_EOB,
      A.AJ2_EOB,
      A.AJ3_EOB,
      A.AJ4_EOB,
      A.TRUE_ORIG_EOB_DATE,
      A.AJ1_MIN_EOB_DATE,
      A.AJ2_MIN_EOB_DATE,
      A.AJ3_MIN_EOB_DATE,
      A.AJ4_MIN_EOB_DATE,
      A.AJ5_MIN_EOB_DATE,
      COALESCE(A.AJ5_MIN_EOB_DATE, A.AJ4_MIN_EOB_DATE, A.AJ3_MIN_EOB_DATE, A.AJ2_MIN_EOB_DATE, A.AJ1_MIN_EOB_DATE ,A.TRUE_ORIG_EOB_DATE),
      CRM,
      CRM_DATE,
      CRM_TOPIC,
      CRM_SUBTOPIC
 )
 ----------------------------------------------------------------------------------------------------------------------------------
 --MAIN QUERY SELECT STARTS HERE
 SELECT
 --ORIGINAL
 ORIG_INTERNAL_ID AS CLAIM_ID,
 ORIG_SERV_DATE,
 --INTEREST_TOTAL,
 --PENALTY_AMT,
 ORIG_VENDOR,
 PAYOR_NAME AS ORIG_PAYOR,
 ORIG_BILLED_AMT,
 ORIG_STATUS,
 ORIG_EOB,
 ORIG_EOB_DATE,
 ORIG_KINAL_NET_PAYABLE,
 ORIG_KINAL_CLAIM_STATUS,
 TRIM(REPLACE(ORIG_KINAL_EOB,',','')) AS ORIG_KINAL_EOB,
 ORIG_KINAL_EOB_DATE,
 DUP_CLM_1,
 DUPE_1_KINAL_TOT_NET_PAYBALE,
 DUPE_1_KINAL_CLAIM_STATUS,
 CASE WHEN DUPE_1_KINAL_CLAIM_STATUS = 'CLEAN' THEN NULL ELSE TRIM(REPLACE(DUP_1_KINAL_EOB,',','')) END AS DUP_1_KINAL_EOB,
 DUP_1_KINAL_EOB_DATE,
 DUP_1_KINAL_INTEREST,
 DUP_1_KINAL_PENALTY,
 DUP_CLM_2,
 DUPE_2_KINAL_TOT_NET_PAYBALE,
 DUPE_2_KINAL_CLAIM_STATUS,
 CASE WHEN DUPE_2_KINAL_CLAIM_STATUS = 'CLEAN' THEN NULL ELSE TRIM(REPLACE(DUP_2_KINAL_EOB,',','')) END AS DUP_2_KINAL_EOB,
 DUP_2_KINAL_EOB_DATE,
 DUP_2_KINAL_INTEREST,
 DUP_2_KINAL_PENALTY,
 DUP_CLM_3,
 DUPE_3_KINAL_TOT_NET_PAYBALE,
 DUPE_3_KINAL_CLAIM_STATUS,
 CASE WHEN DUPE_3_KINAL_CLAIM_STATUS = 'CLEAN' THEN NULL ELSE TRIM(REPLACE(DUP_3_KINAL_EOB,',',''))END AS DUP_3_KINAL_EOB,
 DUP_3_KINAL_EOB_DATE,
 DUP_3_KINAL_INTEREST,
 DUP_3_KINAL_PENALTY,
 DUP_CLM_4,
 DUPE_4_KINAL_TOT_NET_PAYBALE,
 DUPE_4_KINAL_CLAIM_STATUS,
 CASE WHEN DUPE_4_KINAL_CLAIM_STATUS = 'CLEAN' THEN NULL ELSE TRIM(REPLACE(DUP_4_KINAL_EOB,',','')) END AS DUP_4_KINAL_EOB,
 DUP_4_KINAL_EOB_DATE,
 DUP_4_KINAL_INTEREST,
 DUP_4_KINAL_PENALTY,
 ALL_KINAL_NET_PAYABLE,
 ALL_KINAL_NET_PENALTY,
 ALL_KINAL_NET_INTEREST,
 ALL_KINAL_STATUS,
 -- DUP 5
 CASE WHEN ALL_KINAL_STATUS = 'CLEAN' THEN NULL ELSE TRIM(REPLACE(ALL_KINAL_EOB,',',''))END AS ALL_KINAL_EOB,
 ALL_KINAL_EOB_DATE,
 CRM,
 CRM_DATE,
 CRM_TOPIC,
 CRM_SUBTOPIC
 ,ENTRY_DATE AS CLH66_ENTRY_DATE
 ,RESOLUTION_DATETIME AS CLH66_RESOLVED_DATETIME
 ,INFO_CODE1
 ,INFO_CODE1_ENTRY_DATE
 ,INFO_CODE2
 ,INFO_CODE2_ENTRY_DATE
 ,INFO_CODE3
 ,INFO_CODE3_ENTRY_DATE
   FROM
 --MAIN QUERY  TABLE BEGINS  TEST3
     (
         SELECT
         AP.ORIG_INTERNAL_ID
         ,AP.ORIG_CLAIM_ID
         ,AP.ORIG_SERV_DATE
         ,AP.ORIG_DATE_RECEIVED  -- ADD FIELD HERE
         ,AP.INTEREST_TOTAL
         ,AP.PENALTY_AMT
         ,AP.AUTH_NUM
         ,AP.LOB_NAME
         ,AP.ORIG_VENDOR
         ,AP.ORIG_BILLED_AMT
         ,AP.ORIG_TOT_NET_PAYABLE
         ,AP.ORIG_STATUS
         ,AP.ORIG_EOB
         ,AP.TRUE_ORIG_EOB_DATE AS ORIG_EOB_DATE
         ,AP.ORIG_KINAL_NET_PAYABLE
         ,AP.ORIG_KINAL_CLAIM_STATUS
         ,AP_CLAIMS.PAYOR_NAME
         ,COALESCE(ADJ5_EOB, ADJ4_EOB, ADJ3_EOB, ADJ2_EOB, ADJ1_EOB, ORIG_EOB) AS ORIG_KINAL_EOB --,AJ1_MIN_EOB_DATE, AJ2_MIN_EOB_DATE, AJ3_MIN_EOB_DATE, AJ4_MIN_EOB_DATE, AJ5_MIN_EOB_DATE
         ,COALESCE(AJ5_MIN_EOB_DATE, AJ4_MIN_EOB_DATE, AJ3_MIN_EOB_DATE, AJ2_MIN_EOB_DATE, AJ1_MIN_EOB_DATE , TRUE_ORIG_EOB_DATE) AS ORIG_KINAL_EOB_DATE ------,COALESCE(REPLACE(ADJ5_EOB, '',NULL), REPLACE(ADJ4_EOB, '',NULL), REPLACE(ADJ3_EOB, '',NULL), REPLACE(ADJ2_EOB, '',NULL), REPLACE(ADJ1_EOB, '',NULL), REPLACE(ORIG_EOB, '',NULL)) AS ORIG_KINAL_EOB REPLACE BLANK WITH NULLS NO LONGER NEEDED
         ,(COALESCE(TEST2.DUPE_1_KINAL_TOT_NET_PAYBALE,0)+COALESCE(TEST2.DUPE_2_KINAL_TOT_NET_PAYBALE,0)+COALESCE( TEST2.DUPE_3_KINAL_TOT_NET_PAYBALE,0)+COALESCE( TEST2.DUPE_4_KINAL_TOT_NET_PAYBALE,0)+COALESCE( TEST2.DUPE_5_KINAL_TOT_NET_PAYBALE,0)) AS ALL_DUPES_TOT_NET_PAYABLE
         ,CASE WHEN (( COALESCE(TEST2.DUPE_1_KINAL_TOT_NET_PAYBALE,0)+COALESCE(TEST2.DUPE_2_KINAL_TOT_NET_PAYBALE,0)+COALESCE( TEST2.DUPE_3_KINAL_TOT_NET_PAYBALE,0) +COALESCE( TEST2.DUPE_4_KINAL_TOT_NET_PAYBALE,0) +COALESCE( TEST2.DUPE_5_KINAL_TOT_NET_PAYBALE,0)) >0 )OR AP.ORIG_KINAL_NET_PAYABLE >0 THEN 'Y' ELSE 'N' END AS IS_CLAIM_SATISFIED
         --DUP 1
         ,AP.DUP_CLM_1
         ,TEST2.DUPE_1_KINAL_TOT_NET_PAYBALE
         ,TEST2.DUPE_1_KINAL_CLAIM_STATUS AS DUPE_1_KINAL_CLAIM_STATUS
         ,REPLACE(TEST2.DUP_1_KINAL_EOB,'',NULL) AS DUP_1_KINAL_EOB
         ,TEST2.DUP_1_KINAL_EOB_DATE
         ,TEST2.DUP_1_KINAL_INTEREST
         ,TEST2.DUP_1_KINAL_PENALTY
         --DUP 2
         ,AP.DUP_CLM_2
         ,TEST2.DUPE_2_KINAL_TOT_NET_PAYBALE
         ,TEST2.DUPE_2_KINAL_CLAIM_STATUS AS DUPE_2_KINAL_CLAIM_STATUS
         ,REPLACE(TEST2.DUP_2_KINAL_EOB,'',NULL) AS DUP_2_KINAL_EOB
         ,TEST2.DUP_2_KINAL_EOB_DATE
         ,TEST2.DUP_2_KINAL_INTEREST
         ,TEST2.DUP_2_KINAL_PENALTY
         ---DUP 3
         ,AP.DUP_CLM_3
         ,TEST2.DUPE_3_KINAL_TOT_NET_PAYBALE
         ,TEST2.DUPE_3_KINAL_CLAIM_STATUS AS DUPE_3_KINAL_CLAIM_STATUS
         ,REPLACE(TEST2.DUP_3_KINAL_EOB,'',NULL) AS DUP_3_KINAL_EOB
         ,TEST2.DUP_3_KINAL_EOB_DATE
         ,TEST2.DUP_3_KINAL_INTEREST
         ,TEST2.DUP_3_KINAL_PENALTY
         --DUP 4
         ,AP.DUP_CLM_4
         ,TEST2.DUPE_4_KINAL_TOT_NET_PAYBALE
         ,TEST2.DUPE_4_KINAL_CLAIM_STATUS AS DUPE_4_KINAL_CLAIM_STATUS
         ,REPLACE(TEST2.DUP_4_KINAL_EOB,'',NULL) AS DUP_4_KINAL_EOB
         ,TEST2.DUP_4_KINAL_EOB_DATE
         ,TEST2.DUP_4_KINAL_INTEREST
         ,TEST2.DUP_4_KINAL_PENALTY
         --ALL KINAL
         ,COALESCE(TEST2.DUPE_4_KINAL_TOT_NET_PAYBALE,TEST2.DUPE_3_KINAL_TOT_NET_PAYBALE,TEST2.DUPE_2_KINAL_TOT_NET_PAYBALE,TEST2.DUPE_1_KINAL_TOT_NET_PAYBALE,AP.ORIG_KINAL_NET_PAYABLE) ALL_KINAL_NET_PAYABLE
         ,COALESCE(TEST2.DUPE_4_KINAL_CLAIM_STATUS,TEST2.DUPE_3_KINAL_CLAIM_STATUS,TEST2.DUPE_2_KINAL_CLAIM_STATUS,TEST2.DUPE_1_KINAL_CLAIM_STATUS,AP.ORIG_KINAL_CLAIM_STATUS) AS ALL_KINAL_STATUS
         ,COALESCE(REPLACE(TEST2.DUP_4_KINAL_EOB,'',NULL), REPLACE(TEST2.DUP_3_KINAL_EOB,'',NULL),REPLACE(TEST2.DUP_2_KINAL_EOB,'',NULL),REPLACE(TEST2.DUP_1_KINAL_EOB,'',NULL),AP.ADJ5_EOB,AP.ADJ4_EOB, AP.ADJ3_EOB, AP.ADJ2_EOB, AP.ADJ1_EOB, AP.ORIG_EOB) AS ALL_KINAL_EOB
         ,COALESCE(TEST2.DUP_4_KINAL_EOB_DATE, TEST2.DUP_3_KINAL_EOB_DATE, TEST2.DUP_2_KINAL_EOB_DATE, TEST2.DUP_1_KINAL_EOB_DATE, AJ5_MIN_EOB_DATE, AJ4_MIN_EOB_DATE,AJ3_MIN_EOB_DATE, AJ2_MIN_EOB_DATE,AJ1_MIN_EOB_DATE , AP.TRUE_ORIG_EOB_DATE) AS ALL_KINAL_EOB_DATE
         ,COALESCE(TEST2.DUP_4_KINAL_PENALTY, TEST2.DUP_3_KINAL_PENALTY, TEST2.DUP_2_KINAL_PENALTY, TEST2.DUP_1_KINAL_PENALTY, AP.ORIG_KINAL_PENALTY_AMT) AS ALL_KINAL_NET_PENALTY
         ,COALESCE(TEST2.DUP_4_KINAL_INTEREST, TEST2.DUP_3_KINAL_INTEREST, TEST2.DUP_2_KINAL_INTEREST, TEST2.DUP_1_KINAL_INTEREST, AP.ORIG_KINAL_INTEREST_AMT) AS ALL_KINAL_NET_INTEREST
         ,AP.CRM
         ,AP.CRM_DATE
         ,AP.CRM_TOPIC
         ,AP.CRM_SUBTOPIC
 		,CLH.ENTRY_DATE
 		,CLH.RESOLUTION_DATETIME
 		,INFO_CODE.INFO_CODE1
 		,INFO_CODE.INFO_CODE1_ENTRY_DATE
 		,INFO_CODE.INFO_CODE2
 		,INFO_CODE.INFO_CODE2_ENTRY_DATE
 		,INFO_CODE.INFO_CODE3
 		,INFO_CODE.INFO_CODE3_ENTRY_DATE
         FROM CODE1 AP
         LEFT OUTER JOIN AP_CLAIMS ON AP_CLAIMS.CLM = AP.ORIG_CLAIM_ID
 		left outer join (select ap.claim_id, clm.internal_id,  eob.mnemonic, eob.code_type_c, apc.entry_Date,  apc.resolution_datetime
 									from ap_claim ap
 									inner join ap_Claim_eob_code apc on apc.claim_id = ap.claim_id
 									inner join ANTHEM_Eob_code eob on eob.eob_code_id = apc.eob_code_id
 									inner join clm_map clm on clm.cid = ap.claim_id
 									where
 									eob.mnemonic = ('CLH66')
 									) clh on clh.internal_id = ap.orig_internal_id
 		left outer join (select a.internal_id
 									,max(case when rownuma = 1 then mnemonic else null end) as Info_code1
 									,max(case when rownuma = 1 then entry_Date else null end) as Info_Code1_Entry_date
 									,max(case when rownuma = 2 then mnemonic else null end) as Info_code2
 									,max(case when rownuma = 2 then entry_Date else null end) as Info_Code2_Entry_date
 									,max(case when rownuma = 3 then mnemonic else null end) as Info_code3
 									,max(case when rownuma = 3 then entry_Date else null end) as Info_Code3_Entry_date
 									from(
 												select clm.internal_id, EOB.MNEMONIC, APC.ENTRY_DATE,	ROW_NUMBER() OVER(PARTITION BY Ap.claim_id ORDER BY apc.entry_Date ) AS ROWNUMA
 												from
 												ap_claim ap
 												inner join ap_Claim_eob_code apc on apc.claim_id = ap.claim_id
 												inner join ANTHEM_Eob_code eob on eob.eob_code_id = apc.eob_code_id
 												inner join clm_map clm on clm.cid = ap.claim_id
 												where
 												 eob.code_type_c = 3 --information  AND
 												) a  group by a.internal_id
 								) info_code on info_code.internal_id = ap.orig_internal_id
           --- GETTING THE INFO OF THE DUPS ------
           LEFT OUTER JOIN (
                                                         SELECT
                                                         ORIG_INTERNAL_ID,
                                                         ORIG_CLAIM_ID,
                                                         ORIG_STATUS,
                                                         ORIG_KINAL_CLAIM_STATUS,
                                                         MAX(CASE WHEN DUPLICATE_CLAIM_NUMBER IN  '1' THEN COALESCE(CLAIM_STATUS2,CLAIM_STATUS1) ELSE NULL END) AS DUPE_1_KINAL_CLAIM_STATUS,
                                                         MAX(CASE WHEN DUPLICATE_CLAIM_NUMBER IN  '2' THEN COALESCE(CLAIM_STATUS2,CLAIM_STATUS1) ELSE NULL END) AS DUPE_2_KINAL_CLAIM_STATUS,
                                                         MAX(CASE WHEN DUPLICATE_CLAIM_NUMBER IN  '3' THEN COALESCE(CLAIM_STATUS2,CLAIM_STATUS1) ELSE NULL END) AS DUPE_3_KINAL_CLAIM_STATUS,
                                                         MAX(CASE WHEN DUPLICATE_CLAIM_NUMBER IN  '4' THEN COALESCE(CLAIM_STATUS2,CLAIM_STATUS1) ELSE NULL END) AS DUPE_4_KINAL_CLAIM_STATUS,
                                                         MAX(CASE WHEN DUPLICATE_CLAIM_NUMBER IN  '5' THEN COALESCE(CLAIM_STATUS2,CLAIM_STATUS1) ELSE NULL END) AS DUPE_5_KINAL_CLAIM_STATUS,
                                                         MAX(CASE WHEN DUPLICATE_CLAIM_NUMBER IN  '1' THEN COALESCE(TOT_NET_PAYABLE2,TOT_NET_PAYABLE1) ELSE NULL END) AS DUPE_1_KINAL_TOT_NET_PAYBALE,
                                                         MAX(CASE WHEN DUPLICATE_CLAIM_NUMBER IN  '2' THEN COALESCE(TOT_NET_PAYABLE2,TOT_NET_PAYABLE1) ELSE NULL END) AS DUPE_2_KINAL_TOT_NET_PAYBALE,
                                                         MAX(CASE WHEN DUPLICATE_CLAIM_NUMBER IN  '3' THEN COALESCE(TOT_NET_PAYABLE2,TOT_NET_PAYABLE1) ELSE NULL END) AS DUPE_3_KINAL_TOT_NET_PAYBALE,
                                                         MAX(CASE WHEN DUPLICATE_CLAIM_NUMBER IN  '4' THEN COALESCE(TOT_NET_PAYABLE2,TOT_NET_PAYABLE1) ELSE NULL END) AS DUPE_4_KINAL_TOT_NET_PAYBALE,
                                                         MAX(CASE WHEN DUPLICATE_CLAIM_NUMBER IN  '5' THEN COALESCE(TOT_NET_PAYABLE2,TOT_NET_PAYABLE1) ELSE NULL END) AS DUPE_5_KINAL_TOT_NET_PAYBALE,
                                                         MAX(CASE WHEN DUPLICATE_CLAIM_NUMBER IN  '1' THEN COALESCE(CLM_EOB2,CLM_EOB1) ELSE NULL END) AS DUP_1_KINAL_EOB,
                                                         MAX(CASE WHEN DUPLICATE_CLAIM_NUMBER IN  '2' THEN COALESCE(CLM_EOB2,CLM_EOB1) ELSE NULL END) AS DUP_2_KINAL_EOB,
                                                         MAX(CASE WHEN DUPLICATE_CLAIM_NUMBER IN  '3' THEN COALESCE(CLM_EOB2,CLM_EOB1) ELSE NULL END) AS DUP_3_KINAL_EOB,
                                                         MAX(CASE WHEN DUPLICATE_CLAIM_NUMBER IN  '4' THEN COALESCE(CLM_EOB2,CLM_EOB1) ELSE NULL END) AS DUP_4_KINAL_EOB,
                                                         MAX(CASE WHEN DUPLICATE_CLAIM_NUMBER IN  '5' THEN COALESCE(CLM_EOB2,CLM_EOB1) ELSE NULL END) AS DUP_5_KINAL_EOB,
                                                        MAX(CASE WHEN DUPLICATE_CLAIM_NUMBER IN  '1' THEN COALESCE(MIN_EOB_DATE2,MIN_EOB_DATE1) ELSE NULL END) AS DUP_1_KINAL_EOB_DATE,
                                                         MAX(CASE WHEN DUPLICATE_CLAIM_NUMBER IN  '2' THEN COALESCE(MIN_EOB_DATE2,MIN_EOB_DATE1) ELSE NULL END) AS DUP_2_KINAL_EOB_DATE,
                                                         MAX(CASE WHEN DUPLICATE_CLAIM_NUMBER IN  '3' THEN COALESCE(MIN_EOB_DATE2,MIN_EOB_DATE1) ELSE NULL END) AS DUP_3_KINAL_EOB_DATE,
                                                         MAX(CASE WHEN DUPLICATE_CLAIM_NUMBER IN  '4' THEN COALESCE(MIN_EOB_DATE2,MIN_EOB_DATE1) ELSE NULL END) AS DUP_4_KINAL_EOB_DATE,
                                                         MAX(CASE WHEN DUPLICATE_CLAIM_NUMBER IN  '5' THEN COALESCE(MIN_EOB_DATE2,MIN_EOB_DATE1) ELSE NULL END) AS DUP_5_KINAL_EOB_DATE,
                                                         MAX(CASE WHEN DUPLICATE_CLAIM_NUMBER IN  '1' THEN COALESCE(PENALTY2, PENALTY1) ELSE NULL END) AS DUP_1_KINAL_PENALTY,
                                                         MAX(CASE WHEN DUPLICATE_CLAIM_NUMBER IN  '2' THEN COALESCE(PENALTY2, PENALTY1) ELSE NULL END) AS DUP_2_KINAL_PENALTY,
                                                         MAX(CASE WHEN DUPLICATE_CLAIM_NUMBER IN  '3' THEN COALESCE(PENALTY2, PENALTY1) ELSE NULL END) AS DUP_3_KINAL_PENALTY,
                                                         MAX(CASE WHEN DUPLICATE_CLAIM_NUMBER IN  '4' THEN COALESCE(PENALTY2, PENALTY1) ELSE NULL END) AS DUP_4_KINAL_PENALTY,
                                                         MAX(CASE WHEN DUPLICATE_CLAIM_NUMBER IN  '5' THEN COALESCE(PENALTY2, PENALTY1) ELSE NULL END) AS DUP_5_KINAL_PENALTY,
                                                         MAX(CASE WHEN DUPLICATE_CLAIM_NUMBER IN  '1' THEN COALESCE(INTEREST2, INTEREST1) ELSE NULL END) AS DUP_1_KINAL_INTEREST,
                                                         MAX(CASE WHEN DUPLICATE_CLAIM_NUMBER IN  '2' THEN COALESCE(INTEREST2, INTEREST1) ELSE NULL END) AS DUP_2_KINAL_INTEREST,
                                                         MAX(CASE WHEN DUPLICATE_CLAIM_NUMBER IN  '3' THEN COALESCE(INTEREST2, INTEREST1) ELSE NULL END) AS DUP_3_KINAL_INTEREST,
                                                         MAX(CASE WHEN DUPLICATE_CLAIM_NUMBER IN  '4' THEN COALESCE(INTEREST2, INTEREST1) ELSE NULL END) AS DUP_4_KINAL_INTEREST,
                                                         MAX(CASE WHEN DUPLICATE_CLAIM_NUMBER IN  '5' THEN COALESCE(INTEREST2, INTEREST1) ELSE NULL END) AS DUP_5_KINAL_INTEREST
                                                         FROM
                                                              --- DUP UNION BEGINS ( CALLED TEST)
                                                                           (
                                                                                   SELECT
                                                                                    ORIG_INTERNAL_ID AS ORIG_INTERNAL_ID,
                                                                                   ORIG_CLAIM_ID AS ORIG_CLAIM_ID,
                                                                                     ORIG_STATUS AS ORIG_STATUS,
                                                                                     ORIG_KINAL_CLAIM_STATUS AS ORIG_KINAL_CLAIM_STATUS,
                                                                                     AP1.CLAIM_STATUS AS CLAIM_STATUS1,
                                                                                     ADJ.CLAIM_STATUS AS CLAIM_STATUS2,
                                                                                     AP1.TOT_NET_PAYABLE AS TOT_NET_PAYABLE1,
                                                                                     ADJ.TOT_NET_PAYABLE AS TOT_NET_PAYABLE2,
                                                                                     AP1.CLM_EOB AS CLM_EOB1,
                                                                                     ADJ.CLM_EOB AS CLM_EOB2,
                                                                                     AP1.MIN_EOB_DATE AS MIN_EOB_DATE1,
                                                                                     ADJ.MIN_EOB_DATE AS MIN_EOB_DATE2,
                                                                                       AP1.PENALTY_AMT AS PENALTY1 ,
                                                                                       ADJ.PENALTY_AMT  AS PENALTY2,
                                                                                       AP1.INTEREST_TOTAL AS INTEREST1,
                                                                                       ADJ.INTEREST_TOTAL AS INTEREST2,
                                                                                    '1' AS DUPLICATE_CLAIM_NUMBER
                                                                                   FROM CODE1 AP
                                                                                    LEFT JOIN AP_CLAIMS AP1 ON AP.DUP_CLM_ID_1 = AP1.CLM
                                                                                   LEFT JOIN AP_CLAIMS ADJ ON AP.DUP_CLM_ID_1 = ADJ.ORIG_ADJST_CLM_ID -- ADJ FOR DUP1
                                                                                    UNION ALL
                                                                                     SELECT
                                                                                     ORIG_INTERNAL_ID AS ORIG_INTERNAL_ID,
                                                                                   ORIG_CLAIM_ID AS ORIG_CLAIM_ID,
                                                                                     ORIG_STATUS AS ORIG_STATUS,
                                                                                     ORIG_KINAL_CLAIM_STATUS AS ORIG_KINAL_CLAIM_STATUS,
                                                                                     AP2.CLAIM_STATUS  ,
                                                                                     ADJ_2.CLAIM_STATUS ,
                                                                                     AP2.TOT_NET_PAYABLE  ,
                                                                                     ADJ_2.TOT_NET_PAYABLE ,
                                                                                     AP2.CLM_EOB AS CLM_EOB1,
                                                                                     ADJ_2.CLM_EOB AS CLM_EOB2,
                                                                                     AP2.MIN_EOB_DATE AS MIN_EOB_DATE1,
                                                                                     ADJ_2.MIN_EOB_DATE AS MIN_EOB_DATE2,
                                                                                     AP2.PENALTY_AMT  AS PENALTY1,
                                                                                       ADJ_2.PENALTY_AMT AS PENALTY2 ,
                                                                                       AP2.INTEREST_TOTAL AS INTEREST1,
                                                                                       ADJ_2.INTEREST_TOTAL AS INTEREST2,
                                                                                     '2' AS DUPLICATE_CLAIM_NUMBER
                                                                                   FROM CODE1 AP
                                                                                   LEFT JOIN AP_CLAIMS AP2 ON AP.DUP_CLM_ID_2 = AP2.CLM
                                                                                    LEFT JOIN AP_CLAIMS ADJ_2 ON AP.DUP_CLM_ID_2 = ADJ_2.ORIG_ADJST_CLM_ID
                                                                                     UNION ALL
                                                                                    SELECT
                                                                                     ORIG_INTERNAL_ID AS ORIG_INTERNAL_ID,
                                                                                   ORIG_CLAIM_ID AS ORIG_CLAIM_ID,
                                                                                     ORIG_STATUS AS ORIG_STATUS,
                                                                                     ORIG_KINAL_CLAIM_STATUS AS ORIG_KINAL_CLAIM_STATUS,
                                                                                     AP3.CLAIM_STATUS ,
                                                                                     ADJ_3.CLAIM_STATUS,
                                                                                    AP3.TOT_NET_PAYABLE  ,
                                                                                     ADJ_3.TOT_NET_PAYABLE ,
                                                                                     AP3.CLM_EOB AS CLM_EOB1,
                                                                                     ADJ_3.CLM_EOB AS CLM_EOB2,
                                                                                     AP3.MIN_EOB_DATE AS MIN_EOB_DATE1,
                                                                                     ADJ_3.MIN_EOB_DATE AS MIN_EOB_DATE2,
                                                                                       AP3.PENALTY_AMT  AS PENALTY1,
                                                                                      ADJ_3.PENALTY_AMT AS PENALTY2 ,
                                                                                       AP3.INTEREST_TOTAL AS INTEREST1,
                                                                                       ADJ_3.INTEREST_TOTAL AS INTEREST2,
                                                                                     '3' AS DUPLICATE_CLAIM_NUMBER
                                                                                   FROM CODE1 AP
                                                                                   LEFT JOIN AP_CLAIMS AP3 ON AP.DUP_CLM_ID_3 = AP3.CLM
                                                                                   LEFT JOIN AP_CLAIMS ADJ_3 ON AP.DUP_CLM_ID_3 = ADJ_3.ORIG_ADJST_CLM_ID
                                                                                     UNION ALL
                                                                                    SELECT
                                                                                     ORIG_INTERNAL_ID AS ORIG_INTERNAL_ID,
                                                                                   ORIG_CLAIM_ID AS ORIG_CLAIM_ID,
                                                                                     ORIG_STATUS AS ORIG_STATUS,
                                                                                     ORIG_KINAL_CLAIM_STATUS AS ORIG_KINAL_CLAIM_STATUS,
                                                                                     AP4.CLAIM_STATUS,
                                                                                     ADJ_4.CLAIM_STATUS,
                                                                                     AP4.TOT_NET_PAYABLE  ,
                                                                                     ADJ_4.TOT_NET_PAYABLE ,
                                                                                     AP4.CLM_EOB AS CLM_EOB1,
                                                                                     ADJ_4.CLM_EOB AS CLM_EOB2,
                                                                                     AP4.MIN_EOB_DATE AS MIN_EOB_DATE1,
                                                                                     ADJ_4.MIN_EOB_DATE AS MIN_EOB_DATE2,
                                                                                       AP4.PENALTY_AMT  AS PENALTY1,
                                                                                       ADJ_4.PENALTY_AMT AS PENALTY2 ,
                                                                                       AP4.INTEREST_TOTAL AS INTEREST1,
                                                                                       ADJ_4.INTEREST_TOTAL AS INTEREST2,
                                                                                     '4' AS DUPLICATE_CLAIM_NUMBER
                                                                                   FROM CODE1 AP
                                                                                   LEFT JOIN AP_CLAIMS AP4 ON AP.DUP_CLM_ID_4 = AP4.CLM
                                                                                   LEFT JOIN AP_CLAIMS ADJ_4 ON AP.DUP_CLM_ID_4 = ADJ_4.ORIG_ADJST_CLM_ID
                                                                                     UNION ALL
                                                                                    SELECT
                                                                                     ORIG_INTERNAL_ID AS ORIG_INTERNAL_ID,
                                                                                   ORIG_CLAIM_ID AS ORIG_CLAIM_ID,
                                                                                     ORIG_STATUS AS ORIG_STATUS,
                                                                                     ORIG_KINAL_CLAIM_STATUS AS ORIG_KINAL_CLAIM_STATUS,
                                                                                     AP5.CLAIM_STATUS,
                                                                                     ADJ_5.CLAIM_STATUS,
                                                                                     AP5.TOT_NET_PAYABLE  ,
                                                                                     ADJ_5.TOT_NET_PAYABLE ,
                                                                                     AP5.CLM_EOB AS CLM_EOB1,
                                                                                     ADJ_5.CLM_EOB AS CLM_EOB2,
                                                                                     AP5.MIN_EOB_DATE AS MIN_EOB_DATE1,
                                                                                     ADJ_5.MIN_EOB_DATE AS MIN_EOB_DATE2,
                                                                                       AP5.PENALTY_AMT  AS PENALTY1,
                                                                                       ADJ_5.PENALTY_AMT AS PENALTY2 ,
                                                                                       AP5.INTEREST_TOTAL AS INTEREST1,
                                                                                       ADJ_5.INTEREST_TOTAL AS INTEREST2,
                                                                                     '5' AS DUPLICATE_CLAIM_NUMBER
                                                                                   FROM CODE1 AP
                                                                                   LEFT JOIN AP_CLAIMS AP5 ON AP.DUP_CLM_ID_5 = AP5.CLM
                                                                                   LEFT JOIN AP_CLAIMS ADJ_5 ON AP.DUP_CLM_ID_5 = ADJ_5.ORIG_ADJST_CLM_ID
                                                                        )TEST
                                                                        --- DUP UNION ENDS ( CALLED TEST)
                                                         GROUP BY
 														ORIG_INTERNAL_ID,
                                                         ORIG_CLAIM_ID,
                                                         ORIG_STATUS,
                                                         ORIG_KINAL_CLAIM_STATUS
                                         )TEST2  ON TEST2.ORIG_INTERNAL_ID=AP.ORIG_INTERNAL_ID
                                       -- GETTING DUP INFO ENDS TEST1
   ) TEST3
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
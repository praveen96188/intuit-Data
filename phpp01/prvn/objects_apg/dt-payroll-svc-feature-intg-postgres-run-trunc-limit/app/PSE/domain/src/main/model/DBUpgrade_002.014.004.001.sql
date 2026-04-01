--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Constraint C_PSP_OFFERING1;
ALTER TABLE PSP_OFFERING
 DROP CONSTRAINT C_PSP_OFFERING1;

ALTER TABLE PSP_OFFERING
 ADD CONSTRAINT C_PSP_OFFERING1
  CHECK (OFFERING_CODE IN('DIYDDYEAREND', 'DIYDDFY143', 'AP79FY14', 'AP79MEFY14', 'AP89FY14', 'PAP75FY14', 'DIYDDFY14', 'SYM3FY14', 'BillPaymentSTD3FY14', 'SYMFY14', 'COSTCO54', 'COSTCO64', 'COSTCO84', 'COSTCO74', 'COSTCO572', 'COSTCO672', 'DIYDDSTD', 'DIYDDSTD3', 'QBOEDD', 'CheckDistribution', 'ThirdParty401k', 'BillPaymentSTD3', 'Tax', 'Cloud', 'RiskAssessment', 'AP69MEFY13', 'PAPAV1142', 'AP63EEEO', 'AP79FY13', 'MAJORACCT', 'APAV115', 'APAV125ME2', 'APAV1352', 'SUP125TEST', 'APDIOCESE', 'APPAP99YR', 'ASST60', 'ASSTAD2P3', 'ASSTEOSUP', 'COSTCO49', 'COSTCO59', 'PAP71FY13', 'PAP582', 'PAP58DD145', 'PAP58DD2', 'AP59ME2', 'AP69DD145', 'AP69DD1502', 'AP59MED145', 'AP692', 'AP69DD2', 'AP69W22', 'UsageBilling', 'SYM1FY13', 'SYM2FY13', 'WorkersComp', 'COSTCO57', 'COSTCO67', 'ViewMyPaycheck', 'PAP67FY13', 'COSTCO52', 'COSTCO62', 'BillPaymentSTD4', 'CloudV2', 'SYMPAPFY14'));

PROMPT finished DBUpgrade_002.014.004.001.sql
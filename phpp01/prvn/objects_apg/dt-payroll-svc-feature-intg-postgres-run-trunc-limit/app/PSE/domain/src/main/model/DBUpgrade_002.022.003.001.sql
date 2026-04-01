--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Constraint C_PSP_OFFERING2;
ALTER TABLE PSP_OFFERING
 DROP CONSTRAINT C_PSP_OFFERING2;

ALTER TABLE PSP_OFFERING
 ADD CONSTRAINT C_PSP_OFFERING2
  CHECK (SERVICE_CODE IN('DirectDeposit', 'Tax', 'BillPayment', 'ThirdParty401k', 'CheckDistribution', 'Cloud', 'RiskAssessment', 'WorkersComp', 'ViewMyPaycheck', 'CloudV2', 'Guideline401k'));

Prompt Constraint C_PSP_SERVICE0;
ALTER TABLE PSP_SERVICE
 DROP CONSTRAINT C_PSP_SERVICE0;

ALTER TABLE PSP_SERVICE
 ADD CONSTRAINT C_PSP_SERVICE0
  CHECK (SERVICE_CD IN('DirectDeposit', 'Tax', 'BillPayment', 'ThirdParty401k', 'CheckDistribution', 'Cloud', 'RiskAssessment', 'WorkersComp', 'ViewMyPaycheck', 'CloudV2', 'Guideline401k'));

Prompt Constraint C_PSP_ENTITLEMENT_CODE_OFF0;
ALTER TABLE PSP_ENTITLEMENT_CODE_OFFERING
 DROP CONSTRAINT C_PSP_ENTITLEMENT_CODE_OFF0;

ALTER TABLE PSP_ENTITLEMENT_CODE_OFFERING
 ADD CONSTRAINT C_PSP_ENTITLEMENT_CODE_OFF0
  CHECK (SERVICE_CD IN('DirectDeposit', 'Tax', 'BillPayment', 'ThirdParty401k', 'CheckDistribution', 'Cloud', 'RiskAssessment', 'WorkersComp', 'ViewMyPaycheck', 'CloudV2', 'Guideline401k'));

PROMPT finished DBUpgrade_002.022.003.001.sql
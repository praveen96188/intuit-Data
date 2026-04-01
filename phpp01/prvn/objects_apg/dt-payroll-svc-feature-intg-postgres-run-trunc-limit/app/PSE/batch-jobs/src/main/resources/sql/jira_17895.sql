BEGIN

UPDATE psp_entitlement pe
SET pe.SUBSCRIPTION_START_DATE =GREATEST (ADD_MONTHS (PE.SUBSCRIPTION_START_DATE, -12),PE.CREATED_DATE) , pe.modified_date=sys_extract_utc(systimestamp),PE.MODIFIER_ID='PSP-2531'
WHERE entitlement_offering_code IN ('037893', '145897', '145279')
AND PE.SUBSCRIPTION_START_DATE > pe.created_date + 60
AND EXISTS
(SELECT 1
FROM psp_entitlement_code pec
WHERE PE.ENTITLEMENT_CODE_FK = PEC.ENTITLEMENT_CODE_SEQ
AND PEC.ASSET_ITEM_NUMBER = '1101349');

dbms_output.put_line('Number of Rows Updated For NCD fix psp_entitlement : ' ||  SQL%ROWCOUNT);
commit;

EXCEPTION
  WHEN OTHERS THEN
    DBMS_OUTPUT.PUT_LINE ('Error : ' || SQLERRM);
END;
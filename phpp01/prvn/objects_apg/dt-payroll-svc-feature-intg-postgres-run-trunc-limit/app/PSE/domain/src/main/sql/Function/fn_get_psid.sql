CREATE OR REPLACE FUNCTION FN_GET_PSID (p_state IN VARCHAR)
    RETURN psp_company.source_company_id%type
AS
    psid psp_company.source_company_id%type;
BEGIN

    if (p_state = 'AL') then select PSID_AL.nextval into psid from dual;
    elsif (p_state = 'AK') then select PSID_AK.nextval into psid from dual;
    elsif (p_state = 'AZ') then select PSID_AZ.nextval into psid from dual;
    elsif (p_state = 'AR') then select PSID_AR.nextval into psid from dual;
    elsif (p_state = 'CA') then select PSID_CA.nextval into psid from dual;
    elsif (p_state = 'CO') then select PSID_CO.nextval into psid from dual;
    elsif (p_state = 'CT') then select PSID_CT.nextval into psid from dual;
    elsif (p_state = 'DE') then select PSID_DE.nextval into psid from dual;
    elsif (p_state = 'DC') then select PSID_DC.nextval into psid from dual;
    elsif (p_state = 'FL') then select PSID_FL.nextval into psid from dual;
    elsif (p_state = 'GA') then select PSID_GA.nextval into psid from dual;
    elsif (p_state = 'HI') then select PSID_HI.nextval into psid from dual;
    elsif (p_state = 'ID') then select PSID_ID.nextval into psid from dual;
    elsif (p_state = 'IL') then select PSID_IL.nextval into psid from dual;
    elsif (p_state = 'IN') then select PSID_IN.nextval into psid from dual;
    elsif (p_state = 'IA') then select PSID_IA.nextval into psid from dual;
    elsif (p_state = 'KS') then select PSID_KS.nextval into psid from dual;
    elsif (p_state = 'KY') then select PSID_KY.nextval into psid from dual;
    elsif (p_state = 'LA') then select PSID_LA.nextval into psid from dual;
    elsif (p_state = 'ME') then select PSID_ME.nextval into psid from dual;
    elsif (p_state = 'MD') then select PSID_MD.nextval into psid from dual;
    elsif (p_state = 'MA') then select PSID_MA.nextval into psid from dual;
    elsif (p_state = 'MI') then select PSID_MI.nextval into psid from dual;
    elsif (p_state = 'MN') then select PSID_MN.nextval into psid from dual;
    elsif (p_state = 'MS') then select PSID_MS.nextval into psid from dual;
    elsif (p_state = 'MO') then select PSID_MO.nextval into psid from dual;
    elsif (p_state = 'MT') then select PSID_MT.nextval into psid from dual;
    elsif (p_state = 'NE') then select PSID_NE.nextval into psid from dual;
    elsif (p_state = 'NV') then select PSID_NV.nextval into psid from dual;
    elsif (p_state = 'NH') then select PSID_NH.nextval into psid from dual;
    elsif (p_state = 'NJ') then select PSID_NJ.nextval into psid from dual;
    elsif (p_state = 'NM') then select PSID_NM.nextval into psid from dual;
    elsif (p_state = 'NY') then select PSID_NY.nextval into psid from dual;
    elsif (p_state = 'NC') then select PSID_NC.nextval into psid from dual;
    elsif (p_state = 'ND') then select PSID_ND.nextval into psid from dual;
    elsif (p_state = 'OH') then select PSID_OH.nextval into psid from dual;
    elsif (p_state = 'OK') then select PSID_OK.nextval into psid from dual;
    elsif (p_state = 'OR') then select PSID_OR.nextval into psid from dual;
    elsif (p_state = 'PA') then select PSID_PA.nextval into psid from dual;
    elsif (p_state = 'RI') then select PSID_RI.nextval into psid from dual;
    elsif (p_state = 'SC') then select PSID_SC.nextval into psid from dual;
    elsif (p_state = 'SD') then select PSID_SD.nextval into psid from dual;
    elsif (p_state = 'TN') then select PSID_TN.nextval into psid from dual;
    elsif (p_state = 'TX') then select PSID_TX.nextval into psid from dual;
    elsif (p_state = 'UT') then select PSID_UT.nextval into psid from dual;
    elsif (p_state = 'VT') then select PSID_VT.nextval into psid from dual;
    elsif (p_state = 'VA') then select PSID_VA.nextval into psid from dual;
    elsif (p_state = 'WA') then select PSID_WA.nextval into psid from dual;
    elsif (p_state = 'WV') then select PSID_WV.nextval into psid from dual;
    elsif (p_state = 'WI') then select PSID_WI.nextval into psid from dual;
    elsif (p_state = 'WY') then select PSID_WY.nextval into psid from dual;
    else select PSID_DEFAULT.nextval into psid from dual;
    end if;

   RETURN psid;

END FN_GET_PSID;
/
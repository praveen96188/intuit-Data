CREATE OR REPLACE FUNCTION FN_GET_PSID (p_state IN VARCHAR) returns psp_company.source_company_id%type
AS $$
DECLARE
psid psp_company.source_company_id%type;
BEGIN
   if (p_state = 'AL') then select nextval('PSID_AL') into psid;
    elsif (p_state = 'AK') then select nextval('PSID_AK') into psid;
    elsif (p_state = 'AZ') then select nextval('PSID_AZ') into psid;
    elsif (p_state = 'AR') then select nextval('PSID_AR') into psid;
    elsif (p_state = 'CA') then select nextval('PSID_CA') into psid;
    elsif (p_state = 'CO') then select nextval('PSID_CO') into psid;
    elsif (p_state = 'CT') then select nextval('PSID_CT') into psid;
    elsif (p_state = 'DE') then select nextval('PSID_DE') into psid;
    elsif (p_state = 'DC') then select nextval('PSID_DC') into psid;
    elsif (p_state = 'FL') then select nextval('PSID_FL') into psid;
    elsif (p_state = 'GA') then select nextval('PSID_GA') into psid;
    elsif (p_state = 'HI') then select nextval('PSID_HI') into psid;
    elsif (p_state = 'ID') then select nextval('PSID_ID') into psid;
    elsif (p_state = 'IL') then select nextval('PSID_IL') into psid;
    elsif (p_state = 'IN') then select nextval('PSID_IN') into psid;
    elsif (p_state = 'IA') then select nextval('PSID_IA') into psid;
    elsif (p_state = 'KS') then select nextval('PSID_KS') into psid;
    elsif (p_state = 'KY') then select nextval('PSID_KY') into psid;
    elsif (p_state = 'LA') then select nextval('PSID_LA') into psid;
    elsif (p_state = 'ME') then select nextval('PSID_ME') into psid;
    elsif (p_state = 'MD') then select nextval('PSID_MD') into psid;
    elsif (p_state = 'MA') then select nextval('PSID_MA') into psid;
    elsif (p_state = 'MI') then select nextval('PSID_MI') into psid;
    elsif (p_state = 'MN') then select nextval('PSID_MN') into psid;
    elsif (p_state = 'MS') then select nextval('PSID_MS') into psid;
    elsif (p_state = 'MO') then select nextval('PSID_MO') into psid;
    elsif (p_state = 'MT') then select nextval('PSID_MT') into psid;
    elsif (p_state = 'NE') then select nextval('PSID_NE') into psid;
    elsif (p_state = 'NV') then select nextval('PSID_NV') into psid;
    elsif (p_state = 'NH') then select nextval('PSID_NH') into psid;
    elsif (p_state = 'NJ') then select nextval('PSID_NJ') into psid;
    elsif (p_state = 'NM') then select nextval('PSID_NM') into psid;
    elsif (p_state = 'NY') then select nextval('PSID_NY') into psid;
    elsif (p_state = 'NC') then select nextval('PSID_NC') into psid;
    elsif (p_state = 'ND') then select nextval('PSID_ND') into psid;
    elsif (p_state = 'OH') then select nextval('PSID_OH') into psid;
    elsif (p_state = 'OK') then select nextval('PSID_OK') into psid;
    elsif (p_state = 'OR') then select nextval('PSID_OR') into psid;
    elsif (p_state = 'PA') then select nextval('PSID_PA') into psid;
    elsif (p_state = 'RI') then select nextval('PSID_RI') into psid;
    elsif (p_state = 'SC') then select nextval('PSID_SC') into psid;
    elsif (p_state = 'SD') then select nextval('PSID_SD') into psid;
    elsif (p_state = 'TN') then select nextval('PSID_TN') into psid;
    elsif (p_state = 'TX') then select nextval('PSID_TX') into psid;
    elsif (p_state = 'UT') then select nextval('PSID_UT') into psid;
    elsif (p_state = 'VT') then select nextval('PSID_VT') into psid;
    elsif (p_state = 'VA') then select nextval('PSID_VA') into psid;
    elsif (p_state = 'WA') then select nextval('PSID_WA') into psid;
    elsif (p_state = 'WV') then select nextval('PSID_WV') into psid;
    elsif (p_state = 'WI') then select nextval('PSID_WI') into psid;
    elsif (p_state = 'WY') then select nextval('PSID_WY') into psid;
    else select nextval('PSID_DEFAULT') into psid;
    end if;

   RETURN psid;
END;
$$
  LANGUAGE plpgsql;
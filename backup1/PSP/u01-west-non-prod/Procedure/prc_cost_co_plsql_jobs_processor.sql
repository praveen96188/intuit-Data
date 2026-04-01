
CREATE OR REPLACE PROCEDURE prc_cost_co_plsql_jobs_processor(
  OUT v_ce_ins_cnt             int ,
  OUT v_ced_ins_cnt            int
)
  LANGUAGE plpgsql AS
$$
DECLARE
  total_rows NUMERIC;
  v_ce_seq varchar;

  cost_cursor CURSOR FOR SELECT co.company_fk, co.company_offering_seq, o.offering_code,o.s_k_u
                         FROM psp_company_offering co, psp_offering o
                         WHERE co.offering_fk = o.offering_seq
                           AND co.company_fk IN
                               (Select Distinct comp_seq from  (select distinct comp_seq,LATEST_PAYROLL_RUN,PSID,NAME from(
                                                                                                                            select PC.company_seq comp_seq,max(PC.source_company_id) OVER(PARTITION BY PC.source_company_id) PSID,PC.LEGAL_NAME AS NAME,PC.SIGN_UP_DATE,
                                                                                                                                   o.offering_code "Present_Offering_code",
                                                                                                                                   pe.SUBSCRIPTION_START_DATE,pe.SUBSCRIPTION_END_DATE,PE.SUBSCRIPTION_NUMBER,pe.ENTITLEMENT_OFFERING_CODE,
                                                                                                                                   min(date_trunc('day',PPR.PAYROLL_RUN_DATE)) OVER(PARTITION BY PC.source_company_id) LATEST_PAYROLL_RUN  FROM psp_company PC
                                                                                                                                                                                                                                                  JOIN PSP_ENTITLEMENT_UNIT peu ON peu.company_fk=PC.COMPANY_SEQ AND peu.ENTITLEMENT_UNIT_STATUS='Activated'
                                                                                                                                                                                                                                                  JOIN PSP_ENTITLEMENT pe ON pe.entitlement_seq=peu.entitlement_fk
                                                                                                                                                                                                                                                  JOIN PSP_PAYROLL_RUN ppr ON PPR.COMPANY_FK=PC.COMPANY_SEQ AND PPR.PAYROLL_RUN_DATE>=to_timestamp('22-JUL-2013', 'DD-Mon-YYYY')
                                                                                                                                                                                                                                                  join psp_company_offering co on PC.company_seq=co.company_fk
                                                                                                                                                                                                                                                  join psp_offering o  on co.offering_fk = o.offering_seq AND o.service_code = 'DirectDeposit' AND o.reporting_type = 'Tax' and o.offering_code='COSTCO672'
                                                                                                                            where PC.SIGN_UP_DATE >=to_timestamp('22-JUL-2013', 'DD-Mon-YYYY') ) s
                                                                where (select round(extract('years' from age) * 12 + extract('months' from age) + extract('days' from age) / 30)
                                                                       from (select age(CURRENT_TIMESTAMP, LATEST_PAYROLL_RUN)) age) >=13) comp)
                           AND o.service_code = 'DirectDeposit'
                           AND o.reporting_type = 'Tax' and o.offering_code='COSTCO672';

begin
  v_ce_ins_cnt :=0;
  v_ced_ins_cnt :=0;

  for i in cost_cursor
    loop
      begin
        update psp_company_offering set offering_fk='9888f25c-31bb-41d0-89ce-27facf9b10c0',modifier_id='PSP-5052',modified_date=timezone('UTC', CURRENT_TIMESTAMP)  where company_offering_seq=i.company_offering_seq;

        select gen_random_uuid() into v_ce_seq from dual;

        Insert into PSP_COMPANY_EVENT
        (COMPANY_EVENT_SEQ,
         VERSION, CREATOR_ID, CREATED_DATE, MODIFIER_ID, MODIFIED_DATE,
         REALM_ID, EVENT_TIME_STAMP, STATUS_EFFECTIVE_DATE, STATUS_CD, EVENT_TYPE_CD,
         EVENT_TOKEN, SOURCE_ID, COMPANY_FK, NOTE_LAST_UPDATED_DATE)
        Values(v_ce_seq, 0, 'PSP-5052',timezone('UTC', CURRENT_TIMESTAMP) ,'PSP-5052',timezone('UTC', CURRENT_TIMESTAMP),-1,timezone('UTC', CURRENT_TIMESTAMP),timezone('UTC', CURRENT_TIMESTAMP),'Active','OfferingUpdated',0,NULL,i.company_fk,NULL);

        GET DIAGNOSTICS total_rows := ROW_COUNT;
        v_ce_ins_cnt:=v_ce_ins_cnt+total_rows;

        Insert into PSP_COMPANY_EVENT_DETAIL
        (COMPANY_EVENT_DETAIL_SEQ,
         VERSION, CREATOR_ID, CREATED_DATE, MODIFIER_ID, MODIFIED_DATE,
         REALM_ID, VALUE, EVENT_DETAIL_TYPE_CD, COMPANY_EVENT_FK, EVENT_DETAIL_SUBTYPE,
         COMPANY_FK)
        Values
        (gen_random_uuid() , 0, 'PSP-5052',timezone('UTC', CURRENT_TIMESTAMP) ,'PSP-5052',timezone('UTC', CURRENT_TIMESTAMP)  , -1,i.s_k_u , 'OldStringValue', V_CE_SEQ,NULL,I.COMPANY_FK);

        GET DIAGNOSTICS total_rows := ROW_COUNT;
        v_ced_ins_cnt:=v_ced_ins_cnt+total_rows;

        Insert into PSP_COMPANY_EVENT_DETAIL
        (COMPANY_EVENT_DETAIL_SEQ,
         VERSION, CREATOR_ID, CREATED_DATE, MODIFIER_ID, MODIFIED_DATE,
         REALM_ID, VALUE, EVENT_DETAIL_TYPE_CD, COMPANY_EVENT_FK, EVENT_DETAIL_SUBTYPE,
         COMPANY_FK)
        Values
        (gen_random_uuid() , 0, 'PSP-5052',timezone('UTC', CURRENT_TIMESTAMP) ,'PSP-5052',timezone('UTC', CURRENT_TIMESTAMP)  , -1,'COSTCO84', 'NewStringValue', V_CE_SEQ,
         NULL, I.COMPANY_FK);

        GET DIAGNOSTICS total_rows := ROW_COUNT;
        v_ced_ins_cnt:=v_ced_ins_cnt+total_rows;

      end;
    end loop;

end;

$$;
with all_companies as
         (select pe.license_number,
                 pe.customer_id,
                 ec.edition_type,
                 ec.asset_item_cd,
                 pc.source_company_id,
                 pc.legal_name,
                 eu.entitlement_unit_status,
                 entitlement_state,
                 pc.company_seq,
                 pc.legal_address_fk
          from pspadm.psp_entitlement pe
                   inner join pspadm.psp_entitlement_code ec
                              on ec.entitlement_code_seq = pe.entitlement_code_fk and
                                 entitlement_state = 'Enabled'
                   inner join pspadm.psp_entitlement_unit eu
                              on eu.entitlement_fk = pe.entitlement_seq and eu.entitlement_unit_status in
                                                                            ('Activated',
                                                                             'PendingActivation',
                                                                             'PendingReactivation',
                                                                             'ErrorActivating',
                                                                             'ActivationHold')
                                  and eu.last_validation_date > current_timestamp - interval '180 days'
                   inner join pspadm.psp_company pc on eu.company_fk = pc.company_seq),
     all_company_taxes as (select source_company_id,
                                  company_seq,
                                  state,
                                  (case
                                       when STATE_TAX_AGENCIES_COUNT > 1 OR LOCAL_TAX_AGENCIES_COUNT > 1 then 'Yes'
                                       else 'No' end)  MULTI_STATE,
                                  (case
                                       when LOCAL_TAX_AGENCIES_COUNT > 0 THEN 'LOCAL'
                                       when STATE_TAX_AGENCIES_COUNT > 0 THEN 'STATE'
                                       ELSE 'FED' end) APPLICABLE_TAXES
                           from (select source_company_id,
                                        company_seq,
                                        state,
                                        count(distinct STATE_TAX_AGENCY) STATE_TAX_AGENCIES_COUNT,
                                        count(distinct LOCAL_TAX_AGENCY) LOCAL_TAX_AGENCIES_COUNT
                                 from (select source_company_id,
                                              pc.company_seq,
                                              paddr.state,
                                              (case when agency_fk in ('IRS', 'SSA') then agency_fk end) FEDERAL_TAX_AGENCY,
                                              (case
                                                   when agency_fk similar to
                                                        ('(LA|OK|AZ|ME|AR|AS|MA|PR|CA|RI|MN|SC|CT|MS|DC|MT|NE|TT|GA|UT|GU|VT|HI|NJ|VA|ID|NM|VI|IL|NC|IA|ND|WI|KS|MP)%')
                                                       then substring(agency_fk, 0, 3) end)              STATE_TAX_AGENCY,
                                              (case
                                                   when agency_fk similar to ('(AL|CO|DE,IN|KY|MD|MO|MI|NY|OH|OR|WV|PA)%')
                                                       then substring(agency_fk, 0, 3) end)              LOCAL_TAX_AGENCY
                                       from all_companies pc
                                                inner join pspadm.psp_address paddr on pc.legal_address_fk = paddr.address_seq
                                                inner join pspadm.psp_company_agency pca on pc.company_seq = pca.company_fk
                                                inner join pspadm.psp_agency pa on pca.agency_fk = pa.agency_id) company_tax_agency
                                 group by source_company_id, company_seq, state) company_tax_agencies),
     all_dd_companies as (select epc.*,
                                 case
                                     when cs.status_cd in ('ActiveCurrent') then 'DD'
                                     when cs.status_cd in
                                          ('PendingPinCreation', 'PendingBankVerification', 'PendingFirstPayroll')
                                         then 'PaperCheck'
                                     when cs.status_cd in ('Cancelled', 'Terminated') then 'PaperCheck'
                                     when cs.status_cd is null then 'PaperCheck'
                                     else cs.status_cd end as direct_deposit_status
                          from all_companies epc
                                   inner join pspadm.psp_company_service cs
                                              on epc.company_seq = cs.company_fk and cs.service_fk = 'DirectDeposit'),
     all_moneymovemnt as (select copayType.source_company_id,
                                 copayType.company_seq,
                                 case
                                     when payment_methods = 'DD+PaperCheck' then 'DD+PaperCheck'
                                     when payment_methods = 'PaperCheck+DD' then 'DD+PaperCheck'
                                     when payment_methods = 'DD' then 'DD+PaperCheck'
                                     else payment_methods end as companyMoneyMovementType
                          from (select coPay.source_company_id,
                                       coPay.company_seq,
                                       string_agg(moneymovementtype, '+') as payment_methods
                                from (select eepaycheck.source_company_id,
                                             eepaycheck.company_seq,
                                             case
                                                 when count(distinct eepaycheck.ee_payment_method) > 1 then 'DD'
                                                 else eepaycheck.ee_payment_method end as moneymovementtype
                                      from (select epc.source_company_id,
                                                   epc.company_seq,
                                                   epc.direct_deposit_status,
                                                   pc.source_employee_fk,
                                                   case
                                                       when direct_deposit_status = 'DD' and pc.d_d_employee_fk is not null
                                                           then 'DD'
                                                       else 'PaperCheck' end as ee_payment_method,
                                                   pc.d_d_employee_fk
                                            from all_dd_companies epc
                                                     inner join pspadm.psp_payroll_run pr on epc.company_seq = pr.company_fk
                                                     inner join pspadm.psp_paycheck pc on pc.payroll_run_fk = pr.payroll_run_seq
                                            where pr.payroll_run_date > CURRENT_DATE - 180) eepaycheck
                                      group by eepaycheck.source_company_id, eepaycheck.company_seq,
                                               eepaycheck.ee_payment_method) coPay
                                group by coPay.source_company_id, coPay.company_seq) copayType
                          group by copayType.source_company_id, copayType.company_seq, copayType.payment_methods),
     all_employees as (select epc.source_company_id,
                              epc.company_seq,
                              ee.status_cd,
                              count(*) eeCount
                       from all_companies epc
                                inner join pspadm.psp_employee ee
                                           on epc.company_seq = ee.company_fk and ee.status_cd = 'Active'
                       group by epc.source_company_id,
                                epc.company_seq, ee.status_cd),
     all_payroll_items as (select coWageItems.company_seq,
                                  coWageItems.source_company_id,
                                  case
                                      when coWageItems.companyWageType = 'Basic+Advanced' then 'Basic+Advanced'
                                      when coWageItems.companyWageType = 'Advanced+Basic' then 'Basic+Advanced'
                                      when coWageItems.companyWageType = 'Advanced' then 'Basic+Advanced'
                                      else coWageItems.companyWageType end as companyWageItemType
                           from (select payitmes.company_seq,
                                        payitmes.source_company_id,
                                        string_agg(payitmes.wageType, '+') as companyWageType
                                 from (select coItems.source_company_id, coItems.company_seq, coItems.wageType
                                       from (select epc.source_company_id,
                                                    epc.company_seq,
                                                    case
                                                        when lower(cpi.payroll_item_fk) in
                                                             ('salary', 'bonus', 'commission', 'tips', 'hourly')
                                                            then 'Basic'
                                                        else 'Advanced' end wageType
                                             from all_companies epc
                                                      inner join pspadm.psp_company_payroll_item cpi
                                                                 on epc.company_seq = cpi.company_fk
                                             group by epc.source_company_id,
                                                      epc.company_seq, cpi.payroll_item_fk) coItems
                                       group by coItems.source_company_id, coItems.company_seq,
                                                coItems.wageType) payitmes
                                 group by payitmes.company_seq, payitmes.source_company_id) coWageItems
                           group by coWageItems.company_seq, coWageItems.source_company_id, coWageItems.companyWageType)
select cohort_table.cohortType,cohort_table.edition_type, count(*) 
from (select coh.company_seq,
             coh.source_company_id,
             coh.edition_type,
             coh.license_number,
             coh.customer_id,
             coh.asset_item_cd,
             coh.entitlement_state,
             coh.entitlement_unit_status,
             case
                 when coh.eeCount <= 10 and coh.MULTI_STATE = 'No' and coh.APPLICABLE_TAXES = 'FED' and
                      coh.companyMoneyMovementType = 'PaperCheck' and coh.companyWageItemType = 'Basic' then 'CohortA'
                 when coh.eeCount <= 10 and coh.MULTI_STATE = 'No' and coh.APPLICABLE_TAXES = 'STATE' and
                      coh.companyMoneyMovementType = 'DD+PaperCheck' and coh.companyWageItemType = 'Basic'
                     then 'CohortB'
                 when coh.eeCount <= 50 and coh.MULTI_STATE = 'Yes' and coh.APPLICABLE_TAXES != 'LOCAL' and
                      coh.companyMoneyMovementType = 'DD+PaperCheck' and coh.companyWageItemType = 'Basic+Advanced'
                     then 'CohortC'
                 when coh.APPLICABLE_TAXES = 'LOCAL' and coh.companyMoneyMovementType = 'DD+PaperCheck' and
                      coh.companyWageItemType = 'Basic+Advanced' then 'CohortD'
                 else 'CohortOther' end cohortType
      from (select co.company_seq,
                   co.source_company_id,
                   co.edition_type,
                   co.license_number,
                   co.customer_id,
                   co.asset_item_cd,
                   co.entitlement_state,
                   co.entitlement_unit_status,
                   tax.state,
                   tax.MULTI_STATE,
                   tax.APPLICABLE_TAXES,
                   ee.eeCount,
                   mmt.companyMoneyMovementType,
                   wage.companyWageItemType
            from all_companies co
                     left outer join all_company_taxes tax on tax.company_seq = co.company_seq
                     left outer join all_moneymovemnt mmt on mmt.company_seq = co.company_seq
                     left outer join all_employees ee on ee.company_seq = co.company_seq
                     left outer join all_payroll_items wage on wage.company_seq = co.company_seq) coh) cohort_table
group by cohort_table.cohortType,cohort_table.edition_type;

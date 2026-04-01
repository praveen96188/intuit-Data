set timing on echo on feedback on
select SOURCE_COMPANY_ID, TaxCurrentCash, others, (TaxCurrentCash - others) netbalance
from (
       select SOURCE_COMPANY_ID,
              TaxCurrentCash,
              (TaxCurrentLiability + AgencyTaxRefund + ERPayable + ERLiabilityOffset + ERSUITaxDue) others
       from (
              select /*+ PARALLEL(4) */ c.SOURCE_COMPANY_ID,
                                        pspadm.fn_get_ledger_balance(c.COMPANY_SEQ, 'TaxCurrentCash') TaxCurrentCash,
                                        pspadm.fn_get_ledger_balance(c.COMPANY_SEQ,
                                                                     'TaxCurrentLiability')           TaxCurrentLiability,
                                        pspadm.fn_get_ledger_balance(c.COMPANY_SEQ,
                                                                     'AgencyTaxRefund')               AgencyTaxRefund,
                                        pspadm.fn_get_ledger_balance(c.COMPANY_SEQ,
                                                                     'ERPayable')                     ERPayable,
                                        pspadm.fn_get_ledger_balance(c.COMPANY_SEQ,
                                                                     'ERLiabilityOffset')             ERLiabilityOffset,
                                        pspadm.fn_get_ledger_balance(c.COMPANY_SEQ,
                                                                     'ERSUITaxDue')                   ERSUITaxDue
              from pspadm.PSP_COMPANY c,
                   pspadm.PSP_COMPANY_SERVICE cs
              where c.COMPANY_SEQ = cs.COMPANY_FK
                and cs.SERVICE_FK = 'Tax'
            )) where (TaxCurrentCash - others) <> 0;



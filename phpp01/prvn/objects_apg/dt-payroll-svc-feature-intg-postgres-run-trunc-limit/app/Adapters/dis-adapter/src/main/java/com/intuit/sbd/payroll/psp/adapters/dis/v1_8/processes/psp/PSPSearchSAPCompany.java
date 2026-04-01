package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.processes.psp;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects.SAPCompanyDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.requests.SearchSAPCompanyRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses.ResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses.SearchSAPCompanyResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.processes.DISProcessInterface;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManagerHelper;
import com.intuit.sbd.payroll.psp.domain.EntityChange;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 *
 * Query Company Full Process
 *
 */
public class PSPSearchSAPCompany extends DISProcessInterface {
    private static final SpcfLogger logger;
    static {
        logger = PayrollServices.getLogger(PSPSearchSAPCompany.class);
    }

    private SearchSAPCompanyRequestDISDTO searchSAPCompanyRequestDISDTO;
    private SearchSAPCompanyResponseDISDTO searchSAPCompanyResponseDISDTO;

    /***
     * Constructor
     * @param pSearchSAPCompanyRequestDISDTO
     */
    public PSPSearchSAPCompany(SearchSAPCompanyRequestDISDTO pSearchSAPCompanyRequestDISDTO) {
        searchSAPCompanyRequestDISDTO = pSearchSAPCompanyRequestDISDTO;
    }

    @Override
    public Object process() throws Throwable {
        logger.debug("Entering PSPQueryCompanyFull.process()");
        searchSAPCompanyResponseDISDTO = new SearchSAPCompanyResponseDISDTO();
        try {
            PayrollServices.beginUnitOfWork();

            String sourceCompanyId = searchSAPCompanyRequestDISDTO.getSourceCompanyId();
            String requestEin = searchSAPCompanyRequestDISDTO.getEin();
            String realmId = searchSAPCompanyRequestDISDTO.getRealmId();
            List<String> einsToSearchInSAP = new ArrayList<String>();
            einsToSearchInSAP.add(requestEin);
            // If no PSID is passed in, search passed in EIN as well as any companies that
            //   had an entity change with this EIN as the old EIN.
            if (sourceCompanyId == null) {
                for (EntityChange entityChange : findEntityChangesWithOldEin(requestEin)) {
                    einsToSearchInSAP.add(entityChange.getNewEIN());
                }
            }

            List<SAPCompanyDISDTO> coList = new ArrayList<SAPCompanyDISDTO>();
            for (String ein : einsToSearchInSAP) {
                coList.addAll(PSPHelper.createSAPCompanyList(ein, sourceCompanyId, realmId, searchSAPCompanyRequestDISDTO));
            }
            PayrollServices.rollbackUnitOfWork();
            for (SAPCompanyDISDTO sapCompanyDISDTO : coList) {
                try {
                    PSPRequestContextManagerHelper.getPSPRequestContextManager().setRequestContextCompanyFromPSID(sapCompanyDISDTO.getCompanyId());
                    sapCompanyDISDTO.updateSAPCompanyDISDTO();
                } finally {
                    PSPRequestContextManagerHelper.getPSPRequestContextManager().clearRequestContextCompany();
                }
            }

            searchSAPCompanyResponseDISDTO.setCompanies(coList);
        } catch (Throwable t) {
            throw new Exception(t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        logger.debug("Leaving PSPQueryCompanyFull.process()");
        return searchSAPCompanyResponseDISDTO;
    }

    @Override
    public ResponseDISDTO getResponse() {
        return searchSAPCompanyResponseDISDTO;
    }

    private DomainEntitySet<EntityChange> findEntityChangesWithOldEin(String pEin) {
        List<String> oldEinEncList = EncryptionUtils.deterministicEncryptWithAllKeys(EntityChange.OldEinKeyName, pEin);
        Expression<EntityChange> query = new Query<EntityChange>()
                .Where(EntityChange.OldEinEnc().in(oldEinEncList));

        return Application.find(EntityChange.class, query);
    }

}

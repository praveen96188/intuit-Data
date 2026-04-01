package com.intuit.sbd.payroll.psp.jss.processors.workerscomp.service;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.WorkersCompPaycheckPendingState;
import com.intuit.sbd.payroll.psp.domain.WorkersCompPaycheckStateCode;
import com.intuit.sbd.payroll.psp.jss.processors.workerscomp.model.PayrollDTO;
import com.intuit.sbd.payroll.psp.jss.processors.workerscomp.model.PayrollDtoCompanyFileInfo;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Component
public abstract class WorkersCompService<T> {
    private static final SpcfLogger logger = SpcfLogManager.getLogger(WorkersCompService.class);
    private static final String WC_FILE_PATH = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "wc_server_send_dir");
    protected WorkersCompFileGenerator fileGenerator;
    abstract protected List<Set<SpcfUniqueId>> getCompaniesWithPendingPaychecks();
    abstract protected PayrollDTO<T> getDTOFromDomainObject(List<WorkersCompPaycheckPendingState> pendingPaychecks);
    abstract boolean validateDto(PayrollDTO<T> dto);
    public WorkersCompService(WorkersCompFileGenerator fileGenerator)
    {
        this.fileGenerator=fileGenerator;
    }

    public List<PayrollDtoCompanyFileInfo> createPayrollDataforWC() {
        SpcfCalendar currentDate = PSPDate.getPSPTime();
        return createPayrollDataforWC(currentDate,Arrays.asList(WorkersCompPaycheckStateCode.PendingNew,WorkersCompPaycheckStateCode.PendingDelete,
                WorkersCompPaycheckStateCode.PendingEdit));
    }
    public List<PayrollDtoCompanyFileInfo> createPayrollDataforWC(SpcfCalendar currentDate, List<WorkersCompPaycheckStateCode> subsCode) {
        List<PayrollDtoCompanyFileInfo> payrollDtoCompanyFileInfoList= new ArrayList<>();

        try {
            List<Set<SpcfUniqueId>> batches = getCompaniesWithPendingPaychecks();
            // Process one batch at a time
            if (CollectionUtils.isNotEmpty(batches)  && batches.size() > 0) {
                for (Set<SpcfUniqueId> set : batches) {

                    logger.info("createPayrollDataforWC() batch: ");
                    PayrollDtoCompanyFileInfo payrollDtoCompanyFileInfo = new PayrollDtoCompanyFileInfo();
                    // Get payroll
                    List<WorkersCompPaycheckPendingState> pendingPaychecks =WorkersCompPaycheckPendingState.getPendingPaychecks(new HashSet<SpcfUniqueId>(set),currentDate,subsCode);
                    if(pendingPaychecks!=null) {
                        PayrollDTO<T> dto = getDTOFromDomainObject(pendingPaychecks);
                        // Push payroll to WC service
                        if (validateDto(dto)) {
                            String fileName = fileGenerator.generateEncrptFilesFromPayrollObject(dto.getPayroll());
                            payrollDtoCompanyFileInfo.setPayrollDTO(dto);
                            payrollDtoCompanyFileInfo.setFileName(fileName);
                            payrollDtoCompanyFileInfoList.add(payrollDtoCompanyFileInfo);
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            try {
                FileUtils.cleanDirectory(new File(WC_FILE_PATH));
            } catch (IOException exception) {
                logger.info("Error while clean pgp directory", exception);
            }
            throw new RuntimeException(e.getMessage(),e);
        }
        logger.info("Action=pushPayrollDataToWC2, status=complete");
        return payrollDtoCompanyFileInfoList;
    }

}

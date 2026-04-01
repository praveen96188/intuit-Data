package com.intuit.sbd.payroll.psp.batchjobs.soxReport.service;

import com.intuit.sbd.payroll.psp.batchjobs.soxReport.dao.SoxQueryFactory;
import com.intuit.sbd.payroll.psp.batchjobs.soxReport.dao.SoxUserDAO;
import com.intuit.sbd.payroll.psp.batchjobs.soxReport.domain.SoxDataManager;
import com.intuit.sbd.payroll.psp.batchjobs.soxReport.models.SoxUserDataModel;
import com.intuit.sbd.payroll.psp.jss.util.BatchJobConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SoxAppUserService extends SoxUserService {

    @Autowired
    public SoxAppUserService(SoxBatchService processor, SoxUserDAO soxUserDAO, SoxQueryFactory soxQueryFactory) {
        super(BatchJobConstants.ACCESS_TYPE_APPLICATION, processor, soxUserDAO, soxQueryFactory);
    }

    protected List<SoxUserDataModel> getUserData(SoxDataManager dataManager) {
        return soxUserDAO.queryDatabase(dataManager, accessType);
    }
}

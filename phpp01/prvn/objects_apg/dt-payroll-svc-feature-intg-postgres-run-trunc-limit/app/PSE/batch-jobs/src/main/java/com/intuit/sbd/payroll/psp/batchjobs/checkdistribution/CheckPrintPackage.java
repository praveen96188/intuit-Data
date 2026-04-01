package com.intuit.sbd.payroll.psp.batchjobs.checkdistribution;


import com.intuit.sbd.payroll.psp.api.dtos.CheckPrintDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CheckPrintPaycheckDTO;
import com.intuit.sbd.payroll.psp.batchjobs.util.CheckUtils;
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by IntelliJ IDEA.
 * User: vrepka
 * Date: Jan 28, 2010
 * Time: 2:11:06 PM
 */


public class CheckPrintPackage {

    public static byte[] generateCoverPage(CheckPrintDTO checkPrintDTO, Date currentDate) {
        boolean isTestPrint = false;

        if (checkPrintDTO.getPaychecks().size() > 0) {
            isTestPrint = checkPrintDTO.getPaychecks().get(0).getIsTestCheck();
        }

        CheckDistributionCoverPage checkDistributionCoverPage = new CheckDistributionCoverPage(new Document(PageSize.A4, 36, 36, 252, 108));
        return checkDistributionCoverPage.generateCoverPage(checkPrintDTO, currentDate, isTestPrint);
    }

    public static byte[] generatePaychecks(CheckPrintDTO checkPrintDTO) throws Exception {

        List<byte[]> paycheckPdfs = new ArrayList<byte[]>(checkPrintDTO.getPaychecks().size());
        for (CheckPrintPaycheckDTO checkPrintPaycheckDTO : checkPrintDTO.getPaychecks()) {
            checkPrintPaycheckDTO.calculateTotals();
            CheckDistributionPaycheck checkDistributionPaycheck = new CheckDistributionPaycheck(new Document(PageSize.A4, 33, 33, 38, 0), CheckUtils.getMICRFont());
            paycheckPdfs.add(checkDistributionPaycheck.generatePaycheck(checkPrintDTO, checkPrintPaycheckDTO));
        }
        return CheckUtils.combinePdfs(paycheckPdfs);
    }

}
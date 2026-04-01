package com.intuit.sbd.payroll.psp.checksum;

import com.intuit.sbd.payroll.psp.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

public class EmployeeChecksum {
    
	private static final String PSP_OFFLOADS = "3b672729-dc4e-012a-fc4f-005056c02727";
	private static final String DD_OFFLOADS = "3b672730-dc4e-012a-fc4f-005056c02727";

    public static final Logger LOGGER = LoggerFactory.getLogger(EmployeeChecksum.class);

    public static List<Object[]> findEmployeeChecksumWithSummary(Checksum checksumCDM) {
    	
        String[] paramNames = new String[10];
        paramNames[0] = "partitionSize";
        paramNames[1] = "psp_offloads";
        paramNames[2] = "dd_offloads";
        paramNames[3] = "initiationDateStart";
        paramNames[4] = "initiationDateEnd";
        paramNames[5] = "PaycheckDateStart";
        paramNames[6] = "PaycheckDateEnd";
        paramNames[7] = "PaycheckSettlementDateStart";
        paramNames[8] = "PaycheckSettlementDateEnd";
        paramNames[9] = "approvalDateTimeEnd";

        List<Object[]> employeeChecksumResponse =
                Application.executeNamedQuery("checksumEmployee", paramNames, 
                        new Object[]{checksumCDM.getPartioningSize(),PSP_OFFLOADS, DD_OFFLOADS,
                                new Timestamp(checksumCDM.getInitiationDateTimeStart().getTimeInMilliseconds()),
                                new Timestamp(checksumCDM.getInitiationDateTimeEnd().getTimeInMilliseconds()), 
                                new Date(checksumCDM.getDdSettlementDateStart().getTimeInMilliseconds()),
                                new Date(checksumCDM.getDdSettlementDateEnd().getTimeInMilliseconds()),
                                new Date(checksumCDM.getCheckDateStart().getTimeInMilliseconds()), 
                                new Date(checksumCDM.getCheckDateEnd().getTimeInMilliseconds()),
                                new Timestamp(checksumCDM.getApprovalDateTimeEnd().getTimeInMilliseconds())});

        if(employeeChecksumResponse == null) {
            LOGGER.info("The checksum response is null");
        }

        return employeeChecksumResponse;    
    }

    public static List<Object[]> getEmployeeChecksumSummaryWithPartition(Checksum checksumCDM) {
        
        String[] paramNames = new String[12];
        paramNames[0] = "partitionSize";
        paramNames[1] = "oldPartition";
        paramNames[2] = "partition";
        paramNames[3] = "psp_offloads";
        paramNames[4] = "dd_offloads";
        paramNames[5] = "initiationDateStart";
        paramNames[6] = "initiationDateEnd";
        paramNames[7] = "PaycheckDateStart";
        paramNames[8] = "PaycheckDateEnd";
        paramNames[9] = "PaycheckSettlementDateStart";
        paramNames[10] = "PaycheckSettlementDateEnd";
        paramNames[11] = "approvalDateTimeEnd";
    	
        List<Object[]> employeeChecksumResponse =
                Application.executeNamedQuery("employeeChecksumSummaryWithPartition", paramNames , 
                        new Object[]{checksumCDM.getPartioningSize(),checksumCDM.getOldPartition(),checksumCDM.getPartition(),
                        		PSP_OFFLOADS, DD_OFFLOADS,
                                new Timestamp(checksumCDM.getInitiationDateTimeStart().getTimeInMilliseconds()),
                                new Timestamp(checksumCDM.getInitiationDateTimeEnd().getTimeInMilliseconds()), 
                                new Date(checksumCDM.getDdSettlementDateStart().getTimeInMilliseconds()),
                                new Date(checksumCDM.getDdSettlementDateEnd().getTimeInMilliseconds()),
                                new Date(checksumCDM.getCheckDateStart().getTimeInMilliseconds()), 
                                new Date(checksumCDM.getCheckDateEnd().getTimeInMilliseconds()),
                                new Timestamp(checksumCDM.getApprovalDateTimeEnd().getTimeInMilliseconds())});
    
        
        if(employeeChecksumResponse == null) {
            LOGGER.info("The checksum response is null");
        }
        
        return employeeChecksumResponse;    
    }
    
    public static List<Object[]> getChecksumPaycheckDetail(Checksum checksumCDM) {

        String[] paramNames = new String[12];
        paramNames[0] = "partitionSize";
        paramNames[1] = "oldPartition";
        paramNames[2] = "partition";
        paramNames[3] = "psp_offloads";
        paramNames[4] = "dd_offloads";
        paramNames[5] = "initiationDateStart";
        paramNames[6] = "initiationDateEnd";
        paramNames[7] = "PaycheckDateStart";
        paramNames[8] = "PaycheckDateEnd";
        paramNames[9] = "PaycheckSettlementDateStart";
        paramNames[10] = "PaycheckSettlementDateEnd";
        paramNames[11] = "approvalDateTimeEnd";
        
        List<Object[]> employeeChecksumResponse =
                Application.executeNamedQuery("employeeChecksumPaycheckDetail", paramNames , 
                        new Object[]{checksumCDM.getPartioningSize(),checksumCDM.getOldPartition(),checksumCDM.getPartition(),
                        		PSP_OFFLOADS, DD_OFFLOADS,
                                new Timestamp(checksumCDM.getInitiationDateTimeStart().getTimeInMilliseconds()),
                                new Timestamp(checksumCDM.getInitiationDateTimeEnd().getTimeInMilliseconds()), 
                                new Date(checksumCDM.getDdSettlementDateStart().getTimeInMilliseconds()),
                                new Date(checksumCDM.getDdSettlementDateEnd().getTimeInMilliseconds()),
                                new Date(checksumCDM.getCheckDateStart().getTimeInMilliseconds()), 
                                new Date(checksumCDM.getCheckDateEnd().getTimeInMilliseconds()),
                                new Timestamp(checksumCDM.getApprovalDateTimeEnd().getTimeInMilliseconds())});
      
        if(employeeChecksumResponse == null) {
            LOGGER.info("The checksum response is null");
        }
        
        return employeeChecksumResponse;
    }
}

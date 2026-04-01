package com.intuit.sbd.payroll.psp.batchjobs.billing;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import com.intuit.ems.payroll.psp.gateway.brm.BRMFileUploader;
import com.intuit.idps.service.IdpsException;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.StreamUtil;
import com.intuit.sbd.payroll.psp.domain.Entitlement;
import com.intuit.sbd.payroll.psp.domain.EntitlementStateCode;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import com.intuit.idps.domain.item.Key;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.IDPSFileStreamManager;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.fileencryption.IDPSFileReader;

import java.io.*;
import java.util.*;

/**
 * User: sshetty
 * Date: 12/9/13
 */
public class ProcessErrorCsv {
    private List<String[]> mEntitlementList = null;
    private String[] mLicenseNumList = null;
    List<String[]> mOutputList = new ArrayList<String[]>();
    private String mFileName = null;

    private static SpcfLogger mLogger = Application.getLogger(ProcessErrorCsv.class);

    public void process(String fileName) {
        File file = new File(BRMFileUploader.LOCAL_RECV_DIR + fileName);
        if (file == null || !file.exists()) {
            mLogger.error("There is no file by name " + fileName + " in the recv folder. Please check manually.");
            throw new RuntimeException("No file found  by name " + fileName + " the the receive folder.");
        }

        mFileName = fileName;
        if(fileName.startsWith("PSP")){
            loadEntitlementsInFile(file);
            processErrorFile();
            writeToFile(mFileName);
        }

    }

    public void processFiles(){
        mLogger.info("Started processing files..");
        File folderPath = new File(BRMFileUploader.LOCAL_RECV_DIR);
        File[] files = folderPath.listFiles();
        mLogger.info("Files found under path"+folderPath.getAbsolutePath());
        for(File file:files){
            if(!file.getName().isEmpty()){
                mFileName = file.getName();
                loadEntitlementsInFile(file);
                //checking for null entitlement list and its size=1 since some files are empty

                if(mEntitlementList!=null && mEntitlementList.size()>=1){
                    mLogger.info("entiltlementList size for file "+file.getName() + " is "+mEntitlementList.size());
                    mLogger.info("Entitlement loaded for file "+file);
                    processErrorFile();
                    mLogger.info("Error file processing completed");
                    writeToFile(mFileName);
                    mLogger.info("Writing to file completed");
                }else{
                    mLogger.info("Skipping file processing as entitlementlist for file"+file.getName()+" is empty");
                }
            }
        }

    }

    private void writeToFile(String mFileName) {
        OutputStreamWriter fileWriter = null;
        boolean fileGenSuccess = true;
        try {
            fileWriter = new FileWriter(new File(BRMFileUploader.LOCAL_WORK_DIR, mFileName));
            mLogger.info("Writing " +mFileName + " to location" +BRMFileUploader.LOCAL_WORK_DIR+mFileName);
            CSVWriter csvWriter = new CSVWriter(fileWriter, ',', CSVWriter.NO_QUOTE_CHARACTER);
            csvWriter.writeAll(mOutputList);
            csvWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            fileGenSuccess = false;
            mLogger.error("Can not proceed. IO error of BRM Error file", e);
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    fileGenSuccess = false;
                    mLogger.error("failed to close BRM file", e);
                }
            }
        }
        if (!fileGenSuccess) {
            File outputFile = new File(mFileName);
            try {
                outputFile.delete();
            } catch (Throwable e) {
                throw new RuntimeException("BRM file is not generated for reupload due to IOException. Please check file system.");
            }

        }

    }

    public void processErrorFile() {
        SpcfCalendar calendar = CalendarUtils.getFirstDayOfPrevMonth(PSPDate.getPSPTime());
        CalendarUtils.clearTime(calendar);

        PayrollServices.beginUnitOfWork();
        // Get the entitlements for the license numbers in the error file
        // whose status is enabled or if disabled whose subscription end date is after the previous month had started
        DomainEntitySet<Entitlement> entitlementDomainEntity = Application.find(Entitlement.class,
                Entitlement.LicenseNumber().in(mLicenseNumList)
                        .And((Entitlement.EntitlementState().equalTo(EntitlementStateCode.Enabled)
                                .Or(Entitlement.EntitlementState().equalTo(EntitlementStateCode.Disabled)
                                        .And(Entitlement.SubscriptionEndDate().greaterOrEqualThan(calendar)))))
        ).sort(Entitlement.LicenseNumber());
        PayrollServices.rollbackUnitOfWork();
        int count = 0;
        for (Entitlement entitlement : entitlementDomainEntity) {
            if(mEntitlementList!=null){
                while (count < mEntitlementList.size()) {
                    String dbLicenseNum = entitlement.getLicenseNumber();
                    String errLicenseNum = mEntitlementList.get(count)[0];

                    if (errLicenseNum.compareTo(dbLicenseNum) > 0) {
                        break;
                    }

                    String eoc = mEntitlementList.get(count)[1];
                    if (dbLicenseNum.equalsIgnoreCase(errLicenseNum)) {
                        eoc = entitlement.getEntitlementOfferingCode();
                    }

                    SpcfCalendar monthEnd = CalendarUtils.getLastDayOfMonth(calendar);

                    if (EntitlementStateCode.Disabled.equals(entitlement.getEntitlementState()) && entitlement.getSubscriptionEndDate() != null) {
                        monthEnd = entitlement.getSubscriptionEndDate();
                    }
                    String[] record = new String[5];
                    record[0] = mEntitlementList.get(count)[0];
                    record[1] = eoc;
                    record[2] = mEntitlementList.get(count)[2];
                    record[3] = mEntitlementList.get(count)[3];
                    record[4] = CalendarUtils.convertCalendarToXmlStringNoMilliSeconds(monthEnd);

                    mOutputList.add(record);

                    count++;
                }
            }

        }

        while(count < mEntitlementList.size()){
            mOutputList.add(mEntitlementList.get(count));
            count++;
        }
    }



    public void loadEntitlementsInFile(File pFile) {
        CSVReader reader = null;
        try {
            if(!pFile.getName().isEmpty() && !pFile.getName().contains("swp") && !pFile.getName().startsWith(".")){
                if(StreamUtil.isFileIDPSEncrypted(pFile))
                {
                    try {

                        Key key = IDPSFileStreamManager.newKeyHandleLatest();
                        reader = new CSVReader(new IDPSFileReader( pFile, key));
                    }catch(IdpsException e)
                    {
                        throw new RuntimeException("Can not proceed. IDPS error of BRM Error file", e);
                    }
                }
                else{
                    reader = new CSVReader(new FileReader(pFile));
                }
                if(reader!=null){
                    mEntitlementList = reader.readAll();
                }else{
                    mLogger.info("EntitlementList is null for file"+pFile.getName());
                }

            }



        } catch (FileNotFoundException e) {
            throw new RuntimeException("Failed to find the file at the given location. Please check the file system.", e);
        } catch (IOException e) {
            throw new RuntimeException("Can not proceed. IO error of BRM Error file", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    mLogger.error("failed to close BRM file", e);
                }
            }
        }
        // Remove the header row
        if(mEntitlementList!=null && mEntitlementList.size()>0){
            String[] entitlementHeader = Arrays.copyOf(mEntitlementList.get(0),5);
            mOutputList.add(entitlementHeader);
            mEntitlementList.remove(0);
            Collections.sort(mEntitlementList, new Comparator<String[]>() {

                public int compare(String[] entitlement1, String[] entitlement2) {
                    return entitlement1[0].compareTo(entitlement2[0]);
                }
            });
            mLicenseNumList = new String[mEntitlementList.size()];
            for (int i = 0; i < mEntitlementList.size(); i++) {
                mLicenseNumList[i] = mEntitlementList.get(i)[0].trim();
            }
        }else{
            mLogger.info("EntitlementList is null for file"+pFile.getName());
        }


    }
}

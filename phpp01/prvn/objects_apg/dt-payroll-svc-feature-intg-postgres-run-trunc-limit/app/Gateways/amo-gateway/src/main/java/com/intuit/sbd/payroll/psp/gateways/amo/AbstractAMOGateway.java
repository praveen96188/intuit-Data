package com.intuit.sbd.payroll.psp.gateways.amo;

import com.intuit.idps.domain.item.Key;
import com.intuit.iep.customerasset.intuitcustomerassetabo.v1.AssetType;
import com.intuit.iep.customerasset.intuitcustomerassetabo.v1.EntitlementType;
import com.intuit.iep.customerasset.intuitcustomerassetabo.v1.SyncCustomerAssetDataAreaType;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.common.utils.S3ConnectionException;
import com.intuit.sbd.payroll.psp.common.utils.S3UploadException;
import com.intuit.sbd.payroll.psp.common.utils.S3UploadUtils;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.IDPSFileStreamManager;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.StreamUtil;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.fileencryption.IDPSFileReader;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.fileencryption.IDPSFileWriter;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.AssetTypeCode;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.EntitlementCode;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.xml.sax.InputSource;

import javax.xml.bind.*;
import java.io.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 6, 2010
 * Time: 8:33:06 AM
 */
public abstract class AbstractAMOGateway {
    private static SpcfLogger logger = SpcfLogManager.getLogger(AbstractAMOGateway.class);
    protected static List<String> validItemNumberList;
    private static JAXBContext jaxbContext;

    private Unmarshaller mUnmarshaller;
    private Marshaller mMarshaller;

    public abstract Collection<AMODTO> getMessages(int numberOfMessagesToGet);

    protected void initializeItemNumberList() {
        if(validItemNumberList == null) {
            DomainEntitySet<EntitlementCode> entitlementCodes = Application.find(EntitlementCode.class);
            if(entitlementCodes.size() == 0) {
                throw new RuntimeException("Could not find item numbers");
            }
            validItemNumberList = new ArrayList<String>();
            for (EntitlementCode entitlementCode : entitlementCodes) {
                //Do not add Trial asset item numbers
                if (AssetTypeCode.Trial.equals(entitlementCode.getAssetTypeCd())) {
                    continue;
                }

                // find distinct item numbers, could do this with a query, but there < 20 rows in the table and this is just as easy
                if(!validItemNumberList.contains(entitlementCode.getAssetItemNumber())) {
                    validItemNumberList.add(entitlementCode.getAssetItemNumber());
                }
            }
        }
    }

    protected void initializeUnmarshaller() throws JAXBException {
        if(mUnmarshaller == null) {
            initializeContext();
            mUnmarshaller = jaxbContext.createUnmarshaller();
        }
    }

    protected void initializeMarshaller() throws JAXBException {
        if(mMarshaller == null) {
            initializeContext();
            mMarshaller = jaxbContext.createMarshaller();
        }
    }

    private void initializeContext() throws JAXBException {
        if(jaxbContext == null) {
            jaxbContext = JAXBContext.newInstance(SyncCustomerAssetDataAreaType.class);
        }
    }

    protected void processStringMessage(Map<String, AMODTO> pEntitlementMessageMap, String pStrMsg) throws JAXBException {
        // unmarshal the message
        JAXBElement<?> jaxbElement = (JAXBElement<?>) mUnmarshaller.unmarshal(new StringReader(pStrMsg));
        SyncCustomerAssetDataAreaType syncCustomerAssetDataAreaType = (SyncCustomerAssetDataAreaType)jaxbElement.getValue();

        processMessage(pEntitlementMessageMap, syncCustomerAssetDataAreaType);
    }

    protected void processMessage(Map<String, AMODTO> pEntitlementMessageMap, SyncCustomerAssetDataAreaType syncCustomerAssetDataAreaType) {
        initializeItemNumberList();

        SyncCustomerAssetDataAreaTypeDTO syncCustomerAssetDataAreaTypeDTO = new SyncCustomerAssetDataAreaTypeDTO(syncCustomerAssetDataAreaType);

        for (AssetType assetType : syncCustomerAssetDataAreaType.getSyncCustomerAsset().getAsset()) {
            // filter out items numbers we don't care about
            if(assetType.getItem() != null) {
                if(!validItemNumberList.contains(assetType.getItem().getId().getValue())){
                    continue;
                }
            }

            EntitlementType entitlement = assetType.getEntitlement();
            AssetType.EntitlementTransfer entitlementTransfer = assetType.getEntitlementTransfer();
            // we cannot match updates without entitlement information
            if(entitlement == null && entitlementTransfer == null) {
                continue;
            }

            if(entitlement != null) {
                String orderNumber = null;
                if(assetType.getOrderInfo() != null) {
                    orderNumber = getStringValue(assetType.getOrderInfo().getOrderNumber());
                }

                String licenseNumber = null;
                if(entitlement.getLicenseId() != null) {
                    licenseNumber = getStringValue(entitlement.getLicenseId().getValue());
                }

                String eoc = null;
                if(entitlement.getEntitlementId() != null) {
                    eoc = getStringValue(entitlement.getEntitlementId().getValue());
                }

                // skip unidentified messages
                if(licenseNumber == null || eoc == null) {
                    continue;
                }

                // group by psp entitlement
                String key = licenseNumber + eoc;

                AMODTO amodto = pEntitlementMessageMap.get(key);
                if(amodto == null) {
                    amodto = new AMODTO();
                    amodto.setLicenseNumber(licenseNumber);
                    amodto.setEntitlementOfferingCode(eoc);
                    pEntitlementMessageMap.put(key, amodto);
                }
                if (!amodto.getMessages().contains(syncCustomerAssetDataAreaTypeDTO)) {
                    amodto.addMessage(syncCustomerAssetDataAreaTypeDTO);
                }
            }

            if(entitlementTransfer != null) {
                String sourceLicenseNumber = null;
                if(entitlementTransfer.getSource() != null && entitlementTransfer.getSource().getLicenseId() != null) {
                    sourceLicenseNumber = entitlementTransfer.getSource().getLicenseId().getValue();
                }

                String destinationLicenseNumber = null;
                if(entitlementTransfer.getTarget() != null && entitlementTransfer.getTarget().getLicenseId() != null) {
                    destinationLicenseNumber = entitlementTransfer.getTarget().getLicenseId().getValue();
                }

                // skip unidentified messages
                if(sourceLicenseNumber == null || destinationLicenseNumber == null) {
                    continue;
                }

                AMODTO amodto = pEntitlementMessageMap.get(sourceLicenseNumber + destinationLicenseNumber);
                if(amodto == null) {
                    amodto = new AMODTO();
                    amodto.setSourceLicenseNumber(sourceLicenseNumber);
                    amodto.setDestinationLicenseNumber(destinationLicenseNumber);
                    pEntitlementMessageMap.put(sourceLicenseNumber, amodto);
                }
                if (!amodto.getMessages().contains(syncCustomerAssetDataAreaTypeDTO)) {
                    amodto.addMessage(syncCustomerAssetDataAreaTypeDTO);
                }
            }
        }
    }

    private String getStringValue(String s) {
        if(s == null) {
            return s;
        }

        return s.trim().length() > 0 ? s.trim() : null;
    }

    public void writeMessagesToFiles(Collection<AMODTO> pMessages) throws S3ConnectionException,S3UploadException{
        try {
            initializeMarshaller();
        } catch (JAXBException e) {
            logger.error("Failed to create marshaller.", e);
            return;
        }

        String outputDir = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_amo_message_dir");
        List<SyncCustomerAssetDataAreaType> writtenFiles = new ArrayList<SyncCustomerAssetDataAreaType>();
        for (AMODTO message : pMessages) {
            for (SyncCustomerAssetDataAreaTypeDTO syncCustomerAssetDataAreaTypeDTO : message.getMessages()) {
                if(writtenFiles.contains(syncCustomerAssetDataAreaTypeDTO.getSyncCustomerAssetDataAreaType())) {
                    continue;
                }
                writtenFiles.add(syncCustomerAssetDataAreaTypeDTO.getSyncCustomerAssetDataAreaType());
                try {
                    StringWriter stringWriter = new StringWriter();
                    mMarshaller.marshal(syncCustomerAssetDataAreaTypeDTO.getSyncCustomerAssetDataAreaType(), stringWriter);
                    writeMessageToFile(outputDir, stringWriter.toString());
                } catch (JAXBException e) {
                    logger.error("Failed to marshall message. For " + syncCustomerAssetDataAreaTypeDTO.getEntitlementMessageId(), e);
                }
            }
        }
    }

    protected void writeMessageToFile(String pOutputDir, String pMessage) throws S3UploadException, S3ConnectionException {
        try {
            String fileFullName = pOutputDir + File.separator + UUID.randomUUID().toString() + ".txt";


            Key key  = IDPSFileStreamManager.newKeyHandleLatest();
            OutputStreamWriter fileWriter = new IDPSFileWriter(fileFullName,key);

            fileWriter.write(pMessage);
            fileWriter.close();

            String batchJobName = BatchJobType.AMOMessageProcessor.name();
            S3UploadUtils.archive(batchJobName,pOutputDir,fileFullName);

        } catch (IOException e) {
            logger.error("Failed to write message from AMO to file.\n", e);
        }
    }

    protected Map<String, AMODTO> readMessagesFromFiles() {
        Map<String, AMODTO> entitlementMessageMap = new HashMap<String, AMODTO>();

        try {
            File messageDir = new File(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_amo_message_dir"));
            File[] files = messageDir.listFiles();
            if(files != null) {
                for (File file : files) {
                    initializeUnmarshaller();
                    processMessage(entitlementMessageMap, (SyncCustomerAssetDataAreaType)mUnmarshaller.unmarshal(new InputSource(new StringReader(readFile(file)))));
                    file.delete();
                }
            }
        } catch (Exception e) {
            logger.error("Error reading saved amo messages.", e);
        }

        return entitlementMessageMap;
    }

    private String readFile(File pFile) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        InputStreamReader fileReader = null;

        if(StreamUtil.isFileIDPSEncrypted(pFile))
        {
            Key key  = IDPSFileStreamManager.newKeyHandleLatest();
            fileReader = new IDPSFileReader(pFile,key);
        }
        else{
            fileReader = new FileReader(pFile);
        }
        BufferedReader input = new BufferedReader(fileReader);
        try {
            String line;
            while ((line = input.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(System.getProperty("line.separator"));
            }
        } finally {
            input.close();
        }

        return stringBuilder.toString();
    }
}

package com.intuit.sbd.payroll.psp.tools;

import com.intuit.ems.payroll.psp.gateways.ers.ERSGatewayFactory;
import com.intuit.ems.payroll.psp.gateways.ers.EntitlementInfoDTO;
import com.intuit.ems.payroll.psp.gateways.ers.IERSGateway;
import com.intuit.idps.domain.item.Key;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementUnitDTO;
import com.intuit.sbd.payroll.psp.batchjobs.ers.ERSListener;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.StreamUtil;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.gateways.amo.AMOListener;
import com.intuit.sbd.payroll.psp.gateways.amo.AMOWSGatewayFactory;
import com.intuit.sbd.payroll.psp.gateways.amo.GetCustomerAssetResponseTypeDTO;
import com.intuit.sbd.payroll.psp.gateways.amo.IAMOWSGateway;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.portability.SpcfUniqueId;

import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.IDPSFileStreamManager;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.fileencryption.IDPSFileReader;

import java.io.*;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jjones1
 * Date: 6/21/13
 * Time: 2:47 PM
 */
public class EntitlementSync {

    public static void main(String[] args) throws Throwable{
        try {

            String filePath;
            if (args.length == 1) {
                filePath = args[0];
            } else {
                throw new Exception("Missing file path input parameter.");
            }

            File file = new File(filePath);
            if (!file.exists()) {
                throw new Exception(String.format("File %s does not exists.", filePath));
            }
            InputStreamReader fileReader=null;
            if(StreamUtil.isFileIDPSEncrypted(filePath)){
                Key key = IDPSFileStreamManager.newKeyHandleLatest();
                fileReader = new IDPSFileReader( file, key);
            }else{    
            	fileReader = new FileReader(file);
            }
            
        	BufferedReader input =  new BufferedReader(fileReader);
            Application.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.BatchJob));

            // For each license and eoc in the file
            String line;
            String entitlementUnitId = null;
            while (( line = input.readLine()) != null){
                try {
                    String[] licAndEoc = line.split(",");

                    if (licAndEoc.length != 2) {
                        System.out.println("Error reading License and EOC " + line);
                        continue;
                    }

                    PayrollServices.beginUnitOfWork();
                    Entitlement entitlement = Entitlement.findEntitlement(licAndEoc[0], licAndEoc[1]);

                    if (entitlement == null) {
                        System.out.println("Could not find entitlement " + line);
                        continue;
                    }

                    DomainEntitySet<EntitlementUnit> entitlementUnits = entitlement.getActiveEntitlementUnitCollection();
                    if (entitlementUnits.isNotEmpty()) {
                        entitlementUnitId = entitlementUnits.getFirst().getId().toString();
                    } else {
                        entitlementUnits = entitlement.getEntitlementUnitCollection();
                        if (entitlementUnits.isNotEmpty()) {
                            entitlementUnitId = entitlementUnits.getFirst().getId().toString();
                        } else {
                            System.out.println("Could not find any entitlement units " + line);
                            continue;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }

                if (entitlementUnitId != null) {
                    try {
                        syncEntitlementUnitFromSourceSystems(entitlementUnitId);
                        System.out.println("Successfully synced " + line);
                    } catch (Throwable t) {
                        System.out.println("Error syncing entitlement unit " + entitlementUnitId);
                    }
                }
            }

            System.out.println("Process complete.");
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            throw t;
        }
    }

    public static void syncEntitlementUnitFromSourceSystems(String pEntitlementUnitId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            List<String> syncOptions = Arrays.asList(SystemParameter.findStringValue(SystemParameter.Code.ERS_DATA_SYNC_OPTIONS, "EntitlementState,EntitlementUnitStatus").split(","));
            if (syncOptions.isEmpty()) {
                throw new Exception("Sync is currently disabled.");
            }

            EntitlementUnit entitlementUnit = Application.findById(EntitlementUnit.class, SpcfUniqueId.createInstance(pEntitlementUnitId));
            if (entitlementUnit == null) {
                throw new Exception("Unable to find EntitlementUnit specified.");
            }

            if (entitlementUnit.isHistoric()) {
                throw new Exception("Historic entitlement units cannot be updated");
            }
            EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(entitlementUnit);

            boolean ersUpdated = syncEntitlementDataFromERS(entitlementUnit.getCompany(), syncOptions, entitlementUnitDTO);
            boolean amoUpdated = syncEntitlementDataFromAMO(entitlementUnit.getCompany(), entitlementUnitDTO);

            if (ersUpdated || amoUpdated) {
                ProcessResult processResult = PayrollServices.entitlementManager.syncEntitlementUnit(entitlementUnit.getId(), entitlementUnitDTO);
                if (!processResult.isSuccess()) {
                    throw new Exception("Error updating entitlement " + processResult.toString());
                }
            }
            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            throw new Exception("Error syncing entitlement data with source system.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private static boolean syncEntitlementDataFromERS(Company pCompany, List<String> pSyncOptions, EntitlementUnitDTO pEntitlementUnitDTO) throws Throwable {
        boolean response = false;

        try {
            String license = pEntitlementUnitDTO.getLicenseNumber();
            String eoc = pEntitlementUnitDTO.getEntitlementOfferingCode();

            ERSListener ersListener = new ERSListener(pCompany, TransmissionType.QueryEntitlement);
            IERSGateway ersGateway = ERSGatewayFactory.createInstance();
            if (ersGateway == null) {
                return response;
            }

            EntitlementInfoDTO entitlementInfoDTO = ersGateway.getEntitlementInfo(license, eoc, true, ersListener);
            if (entitlementInfoDTO == null) {
                return response;
            }

            response = true;

            entitlementInfoDTO.copyErsDtoToPspDto(pSyncOptions, pEntitlementUnitDTO);
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            throw t;
        }

        return response;
    }

    private static boolean syncEntitlementDataFromAMO(Company pCompany, EntitlementUnitDTO pEntitlementUnitDTO) throws Throwable {
        boolean response = false;

        try {
            String license = pEntitlementUnitDTO.getLicenseNumber();
            String eoc = pEntitlementUnitDTO.getEntitlementOfferingCode();

            AMOListener amoListener = new AMOListener(pCompany, TransmissionType.QueryCustomerAsset);
            IAMOWSGateway amowsGateway = AMOWSGatewayFactory.createInstance();
            if (amowsGateway == null) {
                return response;
            }

            GetCustomerAssetResponseTypeDTO getCustomerAssetResponseTypeDTO = amowsGateway.getCustomerAsset(license, eoc, amoListener);
            if (getCustomerAssetResponseTypeDTO == null) {
                return response;
            }

            response = true;

            getCustomerAssetResponseTypeDTO.copyAmoDtoToPspDto(pEntitlementUnitDTO);
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            throw t;
        }

        return response;
    }

}

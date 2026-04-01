package com.intuit.sbd.payroll.psp.webservices;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.webservices.wsdto.OffloadBatchWSDTO;
import com.intuit.sbd.payroll.psp.webservices.wsdto.OffloadGroupWSDTO;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Feb 11, 2008
 * Time: 10:30:32 AM
 * To change this template use File | Settings | File Templates.
 */
@WebService()
public class OffloadGroupWS {

    @WebMethod
    public void add(@WebParam(name = "offloadGroupCD") String offloadGroupCD,
                    @WebParam(name = "cutoffTime") String cutoffTime) throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (offloadGroupCD == null || offloadGroupCD.trim().length() == 0) {
            throw new RuntimeException("No offloadGroupCD is specified");
        }

        if (cutoffTime == null || cutoffTime.trim().length() == 0) {
            throw new RuntimeException("No cutoffTime specified for the new offload group");
        }
        if (!cutoffTime.matches("\\d\\d:\\d\\d:\\d\\d")) {
            throw new RuntimeException("cutoffTime must be in HH:MM:SS format");
        }
        try {
            PayrollServices.beginUnitOfWork();
            OffloadGroup offloadGroup = OffloadGroup.findOffloadGroup(offloadGroupCD);
            if (offloadGroup == null) {
                offloadGroup = new OffloadGroup();
                offloadGroup.setOffloadGroupCd(offloadGroupCD);
                offloadGroup.setCutoffTime(cutoffTime);
                offloadGroup.setName("Test"+offloadGroupCD);
                offloadGroup.setDescription("Test "+cutoffTime +" Offload Group");
                Application.save(offloadGroup);
            }
            PayrollServices.commitUnitOfWork();
        }catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void delete(@WebParam(name = "offloadGroupCD") String offloadGroupCD) throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (offloadGroupCD == null || offloadGroupCD.trim().length() == 0) {
            throw new RuntimeException("No offloadGroupCD is specified");
        }
        try {
            PayrollServices.beginUnitOfWork();
            OffloadGroup offloadGroup = OffloadGroup.findOffloadGroup(offloadGroupCD);
            if (offloadGroup == null) {
                throw new RuntimeException("OffloadGroup with the specified code "+ offloadGroupCD  +" doesn't exists");
            }

            for (OffloadBatch offloadBatch : Application.find(OffloadBatch.class, OffloadBatch.OffloadGroup().equalTo(offloadGroup))) {
                for (NACHAFile nachaFile : offloadBatch.getNACHAFileCollection()) {
                    Application.delete(nachaFile);
                }
                Application.delete(offloadBatch);
            }

            Application.delete(offloadGroup);
            PayrollServices.commitUnitOfWork();
        }catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void update(@WebParam(name = "offloadGroupCD") String offloadGroupCD,
                       @WebParam(name = "cutoffTime") String cutoffTime) throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (offloadGroupCD == null || offloadGroupCD.trim().length() == 0) {
            throw new RuntimeException("No offloadGroupCD is specified");
        }

        if (cutoffTime == null || cutoffTime.trim().length() == 0) {
            throw new RuntimeException("No cutoffTime specified the new offload group");
        }
        if (!cutoffTime.matches("\\d\\d:\\d\\d:\\d\\d")) {
            throw new RuntimeException("cutoffTime must be in HH:MM:SS format");
        }
        try {
            PayrollServices.beginUnitOfWork();
            OffloadGroup offloadGroup = OffloadGroup.findOffloadGroup(offloadGroupCD);
            if (offloadGroup == null) {
                throw new RuntimeException("OffloadGroup with the specified code "+ offloadGroupCD  +" doesn't exists");
            }
            offloadGroup.setCutoffTime(cutoffTime);
            Application.save(offloadGroup);
            PayrollServices.commitUnitOfWork();
        }catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void scheduleSecondOffload(@WebParam(name = "offloadGroupCD") String offloadGroupCD) throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (offloadGroupCD == null || offloadGroupCD.trim().length() == 0) {
            throw new RuntimeException("No offloadGroupCD is specified");
        }

        try {
            PayrollServices.beginUnitOfWork();
            OffloadGroup offloadGroup = OffloadGroup.findOffloadGroup(offloadGroupCD);
            if (offloadGroup == null) {
                throw new RuntimeException("OffloadGroup with the specified code " + offloadGroupCD + " doesn't exists");
            }

            SpcfCalendar currentDate = PSPDate.getPSPTime();
            CalendarUtils.clearTime(currentDate);

            DomainEntitySet<SecondOffload> secondOffloads = Application.find(SecondOffload.class, SecondOffload.EffectiveDate().equalTo(currentDate));

            if (secondOffloads.size() > 0) {
                throw new RuntimeException("SecondOffload already scheduled with the specified code " + offloadGroupCD
                        + " for current date " + currentDate);
            }

            SecondOffload secondOffload = new SecondOffload();
            secondOffload.setOffloadGroup(offloadGroup);
            secondOffload.setEffectiveDate(currentDate);
            secondOffload.setOverrideCutoffTime("19:00:00");
            Application.save(secondOffload);
            offloadGroup.createOffloadBatchAndNachaFileRecords(currentDate);
            PayrollServices.commitUnitOfWork();
        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void removeSecondOffload(@WebParam(name = "offloadGroupCD") String offloadGroupCD) throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (offloadGroupCD == null || offloadGroupCD.trim().length() == 0) {
            throw new RuntimeException("No offloadGroupCD is specified");
        }

        try {
            PayrollServices.beginUnitOfWork();
            OffloadGroup offloadGroup = OffloadGroup.findOffloadGroup(offloadGroupCD);

            if (offloadGroup == null) {
                throw new RuntimeException("OffloadGroup with the specified code " + offloadGroupCD + " doesn't exists");
            }

            SpcfCalendar today7PM = PSPDate.getPSPTime();
            today7PM.setValues(today7PM.getYear(), today7PM.getMonth(), today7PM.getDay(), 19, 0, 0, 0);

            SecondOffload secondOffload = offloadGroup.getSecondOffload(today7PM);
            if (secondOffload == null) {
                throw new RuntimeException("No scheduled SecondOffload exists for the date and time: " + today7PM.toLocal().toString());
            }

            Application.delete(secondOffload);

            PayrollServices.commitUnitOfWork();
        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public Collection<OffloadGroupWSDTO> queryOffloadGroups() throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<OffloadGroup> offloadGropus = Application.find(OffloadGroup.class);
            PayrollServices.commitUnitOfWork();
            return buildOffloadGroupDTOs(offloadGropus);
        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public String getOffloadCutoffTime(@WebParam(name = "offloadGroupCD") String offloadGroupCD) throws Exception{
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (offloadGroupCD == null || offloadGroupCD.trim().length() == 0) {
            throw new RuntimeException("No offloadGroupCD is specified");
        }
        try {
            PayrollServices.beginUnitOfWork();
            OffloadGroup offloadGroup = OffloadGroup.findOffloadGroup(offloadGroupCD);
            if (offloadGroup == null) {
                throw new RuntimeException("OffloadGroup with the specified code "+ offloadGroupCD  +" doesn't exists");
            }            
            PayrollServices.commitUnitOfWork();
            return offloadGroup.getCutoffTime();
        }catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public Collection<OffloadBatchWSDTO> queryOffloadBatches(@WebParam(name = "excludeInProcess") boolean excludeInProcess) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();
            Expression<OffloadBatch> query = new Query<OffloadBatch>().OrderBy(OffloadBatch.StatusEffeciveDate().Descending());
            if(excludeInProcess) {
                query = ((Query<OffloadBatch>)query).Where(OffloadBatch.StatusCd().notEqualTo(OffloadBatchStatus.InProcess));
            }
            DomainEntitySet<OffloadBatch> offloadBatches = Application.find(OffloadBatch.class, query);
            Collection<OffloadBatchWSDTO> offloadBatchWSDTOs = buildOffloadBatchDTOs(offloadBatches);
            PayrollServices.commitUnitOfWork();
            return offloadBatchWSDTOs;
        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private Collection<OffloadGroupWSDTO> buildOffloadGroupDTOs(DomainEntitySet<OffloadGroup> pOffloadGroups) {
        ArrayList<OffloadGroupWSDTO> offloadGroupDTOs = new ArrayList<OffloadGroupWSDTO>();
        for (OffloadGroup offloadGroup : pOffloadGroups) {
            OffloadGroupWSDTO dto = buildOffloadGroupDTO(offloadGroup);
            offloadGroupDTOs.add(dto);
        }
        return offloadGroupDTOs;
    }

    private OffloadGroupWSDTO buildOffloadGroupDTO(OffloadGroup pOffloadGroup) {
        OffloadGroupWSDTO offloadGroupWSDTO = new OffloadGroupWSDTO();
        offloadGroupWSDTO.offloadGroupCd = pOffloadGroup.getOffloadGroupCd();
        offloadGroupWSDTO.name = pOffloadGroup.getName();
        offloadGroupWSDTO.description = pOffloadGroup.getDescription();
        offloadGroupWSDTO.cutoffTime = pOffloadGroup.getCutoffTime();

        return offloadGroupWSDTO;
    }

    private Collection<OffloadBatchWSDTO> buildOffloadBatchDTOs(DomainEntitySet<OffloadBatch> pOffloadBatches) {
        ArrayList<OffloadBatchWSDTO> offloadBatchDTOs = new ArrayList<OffloadBatchWSDTO>();
        for (OffloadBatch offloadBatch : pOffloadBatches) {
            OffloadBatchWSDTO dto = buildOffloadBatchDTO(offloadBatch);
            offloadBatchDTOs.add(dto);
        }
        return offloadBatchDTOs;
    }

    private OffloadBatchWSDTO buildOffloadBatchDTO(OffloadBatch pOffloadBatch) {
        OffloadBatchWSDTO offloadBatchWSDTO = new OffloadBatchWSDTO();
        offloadBatchWSDTO.id = pOffloadBatch.getId().toString();
        offloadBatchWSDTO.offloadGroupCd = pOffloadBatch.getOffloadGroup().getOffloadGroupCd();
        offloadBatchWSDTO.status = pOffloadBatch.getStatusCd().toString();
        if(pOffloadBatch.getOffloadDate() != null){
            offloadBatchWSDTO.insertDate = new Date(pOffloadBatch.getOffloadDate().toLocal().getTimeInMilliseconds());
        }

        if(pOffloadBatch.getStatusEffeciveDate() != null){
            offloadBatchWSDTO.statusChangeDate = new Date(pOffloadBatch.getStatusEffeciveDate().toLocal().getTimeInMilliseconds());
        }
        
        return offloadBatchWSDTO;
    }
}

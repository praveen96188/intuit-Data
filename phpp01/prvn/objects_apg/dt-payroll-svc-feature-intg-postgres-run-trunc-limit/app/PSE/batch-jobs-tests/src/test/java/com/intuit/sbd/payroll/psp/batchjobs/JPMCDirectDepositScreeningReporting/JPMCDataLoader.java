package com.intuit.sbd.payroll.psp.batchjobs.JPMCDirectDepositScreeningReporting;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.accountservice.AccountServiceSyncCore;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company2Dataloader;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.workflows.WorkflowState;
import com.intuit.sbd.payroll.psp.workflows.Workflows;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Random;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;

/**
 * Created by suganyas315 on 7/13/15.
 */
public class JPMCDataLoader {
    public static void createReportData() {
        int i;
        for (i = 0; i < 10; i++) {
            CompanyDTO companyDTO = DataLoadServices.createCompany(SourceSystemCode.QBDT, "1212" + i);

            CompanyAdditionalInfoDTO companyAdditionalInfoDTO = new CompanyAdditionalInfoDTO();
            companyAdditionalInfoDTO.setIndustry("Agriculture, Ranching, or Farming");
            companyDTO.setCompanyAdditionalInfo(companyAdditionalInfoDTO);

            Collection<ContactDTO> contactDTOs = companyDTO.getContacts();

            for (ContactDTO contact : contactDTOs) {
                if (contact.getContactRoleCd().equals(ContactRole.PrimaryPrincipal)) {
                    contact.setSocialSecurityNumber("12345678" + i);
                    //simple thing to stimulate date of birth
                    Calendar date = new GregorianCalendar(1990, 3, 7);
                    SpcfCalendar dob = CalendarUtils.convertToSpcfCalendar(date);

                    CalendarUtils.addBusinessDays(dob, i);
                    contact.setDateOfBirth(new DateDTO(dob));
                }
            }
            Company company = DataLoadServices.newCompany(companyDTO, "123456");

            //Creates around 10 events for dd activation
            DataLoadServices.addDDService(company);
        }

        //One Assited Company
        Company company = DataLoadServices.getCompanyNoEagerLoad("12120");
        DataLoadServices.addTaxService(company);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
    }

    public static void createReportDataForOFAC() {
        int i;
        for (i = 0; i < 10; i++) {
            CompanyDTO companyDTO = DataLoadServices.createCompany(SourceSystemCode.QBDT, "1212" + i);

            CompanyAdditionalInfoDTO companyAdditionalInfoDTO = new CompanyAdditionalInfoDTO();
            companyAdditionalInfoDTO.setIndustry("Agriculture, Ranching, or Farming");
            companyDTO.setCompanyAdditionalInfo(companyAdditionalInfoDTO);

            Collection<ContactDTO> contactDTOs = companyDTO.getContacts();

            for (ContactDTO contact : contactDTOs) {
                if (contact.getContactRoleCd().equals(ContactRole.PrimaryPrincipal)) {
                    contact.setSocialSecurityNumber("12345678" + i);
                    //simple thing to stimulate date of birth
                    Calendar date = new GregorianCalendar(1990, 3, 7);
                    SpcfCalendar dob = CalendarUtils.convertToSpcfCalendar(date);

                    CalendarUtils.addBusinessDays(dob, i);
                    contact.setDateOfBirth(new DateDTO(dob));
                }
            }
            Company company = DataLoadServices.newCompany(companyDTO, "123456");

            //Creates around 10 events for dd activation
            DataLoadServices.addDDService(company);
            DataLoadServices.addEEs(company,2);
            DataLoadServices.addPayees(company,2);
        }

    }
    public static void createReportData(int size){
        int i;
        for (i = 0; i < size; i++) {
            CompanyDTO companyDTO = DataLoadServices.createCompany(SourceSystemCode.QBDT, "1212" + i,generateRandom());
            Company company = DataLoadServices.newCompany(companyDTO, "123456");
            DataLoadServices.addDDService(company);


        }
    }

    public static void createSMSReportData(int size){
        int i;
        for (i = 0; i < size; i++) {
            CompanyDTO companyDTO = DataLoadServices.createCompany(SourceSystemCode.QBDT, "2212" + i,generateRandom());
            Company company = DataLoadServices.newCompany(companyDTO, "123456");
            DataLoadServices.addDDService(company);

            PayrollServices.beginUnitOfWork();

            company = Company.findCompany(company.getSourceCompanyId(), SourceSystemCode.QBDT);
            Company2Dataloader company2Dataloader = new Company2Dataloader();
            assertSuccess(PayrollServices.companyManager.addCompanyBankAccount(
                    company.getSourceSystemCd(), company.getSourceCompanyId(), company2Dataloader.getCompany1BankAccount(), false, false));
            company.setWorkflowState(Workflows.OII, WorkflowState.ENABLED);
            company.setWorkflowState(Workflows.ACTIVATE_DIRECT_DEPOSIT, WorkflowState.ENABLED);
            company.setWorkflowState(Workflows.MONEY_MOVEMENT_ONBOARDING, WorkflowState.ENABLED);
            PayrollServices.commitUnitOfWork();
        }
    }

    public static String generateRandom() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        // first not 0 digit
        sb.append(random.nextInt(9) + 1);
        // rest of 8 digits
        for (int i = 0; i < 8; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    public static void createReportDataWithCancelledOrTerminatedDDService(){
        createReportDataForOFAC();
        PayrollServices.beginUnitOfWork();
        for(int i=1;i<4;i++){
            Company company = DataLoadServices.getCompanyNoEagerLoad("1212" + i);
            DataLoadServices.cancelDDService(company);
        }
        for(int i=4;i<7;i++){
            Company company = DataLoadServices.getCompanyNoEagerLoad("1212" + i);
            DataLoadServices.terminateDDService(company);
        }
        PayrollServices.commitUnitOfWork();
    }

    public static void createSMSReportData() {

        int i;
        for (i = 0; i < 1; i++) {
            CompanyDTO companyDTO = DataLoadServices.createCompany(SourceSystemCode.QBDT, "1213" + i);

            CompanyAdditionalInfoDTO companyAdditionalInfoDTO = new CompanyAdditionalInfoDTO();
            companyAdditionalInfoDTO.setIndustry("Agriculture, Ranching, or Farming");
            companyDTO.setCompanyAdditionalInfo(companyAdditionalInfoDTO);

            Collection<ContactDTO> contactDTOs = companyDTO.getContacts();

            for (ContactDTO contact : contactDTOs) {
                if (contact.getContactRoleCd().equals(ContactRole.PrimaryPrincipal)) {
                    contact.setSocialSecurityNumber("12345678" + i);
                    //simple thing to stimulate date of birth
                    Calendar date = new GregorianCalendar(1990, 3, 7);
                    SpcfCalendar dob = CalendarUtils.convertToSpcfCalendar(date);

                    CalendarUtils.addBusinessDays(dob, i);
                    contact.setDateOfBirth(new DateDTO(dob));
                }
            }
            Company company = DataLoadServices.newCompany(companyDTO, "123456");


            //Creates around 10 events for dd activation
            DataLoadServices.addDDService(company);
            DataLoadServices.addEEs(company,2);
            DataLoadServices.addPayees(company,2);

            PayrollServices.beginUnitOfWork();

            company = Company.findCompany(company.getSourceCompanyId(), SourceSystemCode.QBDT);
            Company2Dataloader company2Dataloader = new Company2Dataloader();
            assertSuccess(PayrollServices.companyManager.addCompanyBankAccount(
                    company.getSourceSystemCd(), company.getSourceCompanyId(), company2Dataloader.getCompany1BankAccount(), false, false));
            company.setWorkflowState(Workflows.OII, WorkflowState.ENABLED);
            company.setWorkflowState(Workflows.ACTIVATE_DIRECT_DEPOSIT, WorkflowState.ENABLED);
            company.setWorkflowState(Workflows.MONEY_MOVEMENT_ONBOARDING, WorkflowState.ENABLED);
            PayrollServices.commitUnitOfWork();


        }

    }
}

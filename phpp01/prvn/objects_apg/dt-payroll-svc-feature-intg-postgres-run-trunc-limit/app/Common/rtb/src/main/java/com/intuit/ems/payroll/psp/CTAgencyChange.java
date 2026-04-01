package com.intuit.ems.payroll.psp;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import org.apache.commons.collections.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CTAgencyChange {

    private boolean doCommit = false;
    private PaymentTemplate paymentTemplate = null;

    private static String OLD_AGENCY = "CTDOL";
    private static String NEW_AGENCY = "CTPLA";

    public static void main(String[] args) throws IOException {
        String changeMode = "";
        List<String> psid = null;
        CTAgencyChange ctAgencyChange = new CTAgencyChange();
        System.out.println("Arguments passed "+args.length);
        if(args.length == 2 || args.length == 3){
            changeMode = args[0].toUpperCase();

            if("YES".equalsIgnoreCase(args[1].toUpperCase()))
                ctAgencyChange.doCommit = true;

            ctAgencyChange.paymentTemplate = PayrollServices.entityFinder.findById(PaymentTemplate.class, "CT-PFML-PAYMENT");

            if(args.length == 3){
                psid = Arrays.asList(args[2].trim().split(","));
                System.out.println("Number of companies passed "+psid.size());
            }

            switch (changeMode){
                case "FORWARD":
                    ctAgencyChange.processAllCompanies(Mode.FORWARD,psid,OLD_AGENCY);
                    break;
                case "ROLLBACK":
                    ctAgencyChange.processAllCompanies(Mode.ROLLBACK,psid,NEW_AGENCY);
                    break;
                    default:
                        System.out.println("Mode not seleted correctly");
            }
        }else
            System.out.println("Number of agruments are not correct");


    }
    private void changeCTAgency(Company company) {
        if(company==null){
            System.out.println("Company Not found :"+company.getSourceCompanyId());
            return;
        }
        CompanyAgency companyAgency = createCompanyAgency(company);
        updateCompanyLaw(company, companyAgency);
        updateCompanyAgencyPaymentTemplate(company, companyAgency);

    }
    private CompanyAgency createCompanyAgency(Company company) {
        System.out.println("Creating Company Agency for "+company.getSourceCompanyId());
        CompanyAgency companyAgency = new CompanyAgency();
        Agency agency = Application.findById(Agency.class, "CTPLA");
        companyAgency.setAgency(agency);
        companyAgency.setCompany(company);
        companyAgency.setErFicaDeferralEnabled(false);

        companyAgency.setIntuitResponsibilityStartDate(CalendarUtils.getFirstDayOfQuarter(PSPDate.getPSPTime()));
        Application.save(companyAgency);
        return companyAgency;
    }
    private void updateCompanyLaw(Company company, CompanyAgency companyAgency) {
        System.out.println("Updating company Law for "+company.getSourceCompanyId());

        DomainEntitySet<CompanyLaw> companyLawSet =
                Application.find(CompanyLaw.class,
                        CompanyLaw.CompanyAgency().Company().equalTo(company)
                                .And(CompanyLaw.Law().LawId().equalTo(Law.CT_PAID_LEAVE)));

        System.out.println(company.getSourceCompanyId()+" :  number of law record present : "+companyLawSet.size());

        for(CompanyLaw companyLaw : companyLawSet){
            companyLaw.setCompanyAgency(companyAgency);
            Application.save(companyLaw);
        }

    }
    private void updateCompanyAgencyPaymentTemplate(Company company, CompanyAgency companyAgency) {
        System.out.println("Updating Company Agency Payment Template for "+company.getSourceCompanyId());
        CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = CTAgencyChange.findCAPT(company, paymentTemplate);
        companyAgencyPaymentTemplate.setCompanyAgency(companyAgency);
        Application.save(companyAgencyPaymentTemplate);
    }
    private void rollbackCTAgency(Company company) {
        if(company==null){
            System.out.println("ROLLBACK : Company Not found :"+company.getSourceCompanyId());
            return;
        }
        CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(company,"CTDOL");
        rollbackCompanyLaw(company,companyAgency);
        rollbackCompanyAgencyPaymentTemplate(company,companyAgency);
        deleteCompanyAgency(company);


    }
    private void rollbackCompanyLaw(Company company, CompanyAgency companyAgency) {
        System.out.println("Rollbacking company Law for "+company.getSourceCompanyId());

        DomainEntitySet<CompanyLaw> companyLawSet =
                Application.find(CompanyLaw.class,
                        CompanyLaw.CompanyAgency().Company().equalTo(company)
                                .And(CompanyLaw.Law().LawId().equalTo(Law.CT_PAID_LEAVE)));

        System.out.println(company.getSourceCompanyId()+" :  number of law record present : "+companyLawSet.size());

        for(CompanyLaw companyLaw : companyLawSet){
            companyLaw.setCompanyAgency(companyAgency);
            Application.save(companyLaw);
        }
    }

    private void rollbackCompanyAgencyPaymentTemplate(Company company, CompanyAgency companyAgency) {
        System.out.println("Rollbacking Company Agency Payment Template for "+company.getSourceCompanyId());
        CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = CTAgencyChange.findCAPT(company, paymentTemplate);
        companyAgencyPaymentTemplate.setCompanyAgency(companyAgency);
        Application.save(companyAgencyPaymentTemplate);
    }
    private void deleteCompanyAgency(Company company) {
        System.out.println("ROLLBACK : deleting Company Agency..");
        CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(company,"CTPLA");
        Application.delete(companyAgency);
    }
    private void processAllCompanies(Mode mode, List<String> psids, String pAgency) throws IOException {

        int count = 0;
        Application.beginUnitOfWork();
        Application.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.QBDTAdapter));
        try {
            ArrayList<Company> comList = getListOfCompanyAgency(pAgency,psids);
            System.out.println("Total Number of companies : "+comList.size());
            for (Company com : comList) {
                count++;
                System.out.println("Started Processeing for "+com.getSourceCompanyId());
                switch (mode) {
                    case FORWARD:
                        changeCTAgency(com);
                        break;
                    case ROLLBACK:
                        rollbackCTAgency(com);
                        break;
                }
            }
            if(doCommit)
                Application.commitUnitOfWork();
            System.out.println("Finished total company "+count);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            Application.rollbackUnitOfWork();
        }
    }

    enum Mode {
        FORWARD,
        ROLLBACK
    }

    public static CompanyAgencyPaymentTemplate findCAPT(Company pCompany, PaymentTemplate paymentTemplate) {
        CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = null;

        NaturalKey naturalKey = new NaturalKey(CompanyAgencyPaymentTemplate.class, pCompany.getId(), paymentTemplate.getAgency().getAgencyId(), paymentTemplate.getPaymentTemplateCd());
        SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);

        if (primaryKey != null) {
            companyAgencyPaymentTemplate = Application.findById(CompanyAgencyPaymentTemplate.class, primaryKey);
        } else {
            DomainEntitySet<CompanyAgencyPaymentTemplate> companyAgencyPaymentTemplates = Application.find(CompanyAgencyPaymentTemplate.class, new Query<CompanyAgencyPaymentTemplate>()
                    .Where(CompanyAgencyPaymentTemplate.CompanyAgency().Company().equalTo(pCompany)
                            .And(CompanyAgencyPaymentTemplate.PaymentTemplate().equalTo(paymentTemplate))));

            if (companyAgencyPaymentTemplates.size() > 1) {
                throw new RuntimeException(
                        "Query for company agency payment template for company " + pCompany.getSourceCompanyId() + " and payment template " + paymentTemplate.getPaymentTemplateCd() + " did not return 0 or 1 results as expected");
            }

            if (!companyAgencyPaymentTemplates.isEmpty()) {
                companyAgencyPaymentTemplate = companyAgencyPaymentTemplates.get(0);
                Application.getSessionCache().addPrimaryKey(naturalKey, companyAgencyPaymentTemplate.getId());
            }
        }

        return companyAgencyPaymentTemplate;
    }


    public ArrayList<Company> getListOfCompanyAgency(String agency, List<String> psids){

        Criterion<CompanyAgencyPaymentTemplate> where = CompanyAgencyPaymentTemplate.PaymentTemplate().equalTo(paymentTemplate)
                .And(CompanyAgencyPaymentTemplate.CompanyAgency().Agency().AgencyId().equalTo(agency));


        if(CollectionUtils.isNotEmpty(psids)){
            where = where.And(CompanyAgencyPaymentTemplate.CompanyAgency().Company().SourceCompanyId().in(psids));
        }
        DomainEntitySet<CompanyAgencyPaymentTemplate> companyAgencyPaymentTemplates = Application.find(CompanyAgencyPaymentTemplate.class,new Query<CompanyAgencyPaymentTemplate>()
        .Where(where));

        ArrayList<Company> companyList = new ArrayList<Company>();
        for (CompanyAgencyPaymentTemplate capt : companyAgencyPaymentTemplates) {
            companyList.add(capt.getCompanyAgency().getCompany());
        }
        return companyList;
    }
}
package com.intuit.sbd.payroll.psp;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.StringUtils;
import org.hibernate.FlushMode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * User: dweinberg
 * Date: 10/3/11
 * Time: 4:52 PM
 * Verifies both AIDs and ACH Flags
 * Requires DAC and AS/400 set to production
 */
public class AIDVerifier {

    protected static final SpcfLogger logger = Application.getLogger(AIDVerifier.class);

    private static Connection conn;

    private static ThreadLocal<PreparedStatement> aciPreparedStatement = new ThreadLocal<PreparedStatement>();
    private static ThreadLocal<PreparedStatement> pItemPreparedStatement = new ThreadLocal<PreparedStatement>();

    public static void main(String[] args) {
        ExecutorService executor = null;

        try {
            Application.initialize();
            ApplicationSecondary.initialize();
            Application.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.AS400Migration));

            int processors = Runtime.getRuntime().availableProcessors();
            int threadCount = processors * (2);
            executor = Executors.newFixedThreadPool(threadCount);

            logger.info("Loading ACH Company Credit Payment Methods");

            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
            List<SpcfUniqueId> aCHCreditCompanyPaymentMethods = getAllACHCreditCompanyPaymentMethods();
            PayrollServices.rollbackUnitOfWork();

            logger.info("Got " + aCHCreditCompanyPaymentMethods.size() + " ACH Company Credit Payment Methods");

            CompletionService<StringBuilder> completionService = new ExecutorCompletionService<StringBuilder>(executor);

            for (final SpcfUniqueId cMethodId : aCHCreditCompanyPaymentMethods) {
                completionService.submit(new Callable<StringBuilder>() {
                    public StringBuilder call() throws Exception {
                        StringBuilder companyReport = new StringBuilder();
                        try {
                            Application.initialize();
                            ApplicationSecondary.initialize();
                            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.AS400Migration));
                            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

                            CompanyPaymentTemplatePaymentMethod cMethod = Application.findById(CompanyPaymentTemplatePaymentMethod.class, cMethodId);
                            String state = cMethod.getCompanyAgencyPaymentTemplate().getPaymentTemplate().getPaymentTemplateCd().substring(0,2);
                            Law law = SourceSystemLawAssoc.findLawBySourceSystemAndSourceId(SourceSystemCode.QBDT, state + " SIT");
                            CompanyLaw companyLaw = CompanyLaw.findCompanyLaw(cMethod.getCompanyAgencyPaymentTemplate().getCompanyAgency(), law);
                            if (companyLaw == null) {
                                companyReport.append("skipping ").append(cMethodId).append(" because company law not found\n");
                                return companyReport;
                            }
                            String lawSourceId = companyLaw.getSourceId();
                            Company company = cMethod.getCompanyAgencyPaymentTemplate().getCompanyAgency().getCompany();
                            String companyId = company.getSourceCompanyId();
                            String ein = company.getFedTaxId().substring(0,2)+"-"+company.getFedTaxId().substring(2);

                            if (!StringUtils.isNumeric(companyId)) {
                                companyReport.append("skipping ").append(companyId).append(" because not numeric\n");
                                return companyReport;
                            }

                            List<String> problemTemplateCodes = new ArrayList<String>(6);
                            {
                                PreparedStatement aciStatement = getACIStatement();
                                aciStatement.setString(1, ein);
                                aciStatement.setString(2, state);
                                ResultSet aciResultSet = aciStatement.executeQuery();

                                boolean aciValue;
                                if (!aciResultSet.next()) {
                                    aciValue = false;
                                } else {
                                    String value = StringUtils.trim(aciResultSet.getString("ACI_VALUE"));
                                    aciValue = (StringUtils.equals("Y", value));
                                }
                                if (cMethod.getAgentEnabled() != aciValue) {
                                    companyReport.append(companyId).append(" has incorrect ACH flag for ").append(state).append(". AS/400: ").append(Boolean.toString(aciValue)).append("; PSP: ").append(Boolean.toString(cMethod.getAgentEnabled())).append("\n");
                                    companyReport.append("INSERT INTO PSPEVENT values(").append(company.getFedTaxId()).append(", CURRENT TIMESTAMP, 'REGACHW', null, null);\n");
                                    problemTemplateCodes.add(cMethod.getCompanyAgencyPaymentTemplate().getPaymentTemplate().getPaymentTemplateCd());
                                }

                                aciResultSet.close();
                            }

                            {
                                PreparedStatement pItemStatement = getPItemStatement();
                                pItemStatement.setString(1, companyId);
                                pItemStatement.setString(2, lawSourceId);
                                ResultSet pItemResultSet = pItemStatement.executeQuery();
                                if (!pItemResultSet.next()) {
                                    companyReport.append(companyId).append(" did not exist in IQPITEM for ").append(state).append("\n");
                                } else {
                                    String compId = StringUtils.trim(pItemResultSet.getString("PIT_COMPID"));
                                    String agencyTaxpayerId = StringUtils.trim(cMethod.getCompanyAgencyPaymentTemplate().getAgencyTaxpayerId());
                                    if (!(StringUtils.isEmpty(agencyTaxpayerId) && StringUtils.isEmpty(compId)) && !StringUtils.equals(agencyTaxpayerId, compId)) {
                                        companyReport.append(companyId).append(" has incorrect AID for ").append(state).append(". AS/400: ").append(compId).append("; PSP: ").append(agencyTaxpayerId).append("\n");
                                        companyReport.append("INSERT INTO PSPEVENT values(").append(companyId).append(", CURRENT TIMESTAMP, 'IQPITEM', '").append(lawSourceId).append("', null);\n");
                                        problemTemplateCodes.add(cMethod.getCompanyAgencyPaymentTemplate().getPaymentTemplate().getPaymentTemplateCd());
                                    }
                                    if (pItemResultSet.next()) {
                                        companyReport.append(companyId).append(" has multiple rows for IQPITEM for ").append(state).append("\n");
                                        problemTemplateCodes.add(cMethod.getCompanyAgencyPaymentTemplate().getPaymentTemplate().getPaymentTemplateCd());
                                    }
                                }
                                pItemResultSet.close();
                            }

                        if (problemTemplateCodes.size() > 0) {
                            companyReport
                                    .append(companyId)
                                    .append(" did not match. Status: ")
                                    .append(company.getService(ServiceCode.Tax).getStatusCd().toString())
                                    .append("; holds: ")
                                    .append(company.getOnHoldNotesString());

                            int q4PayrollsCount = PayrollRun.findPayrollRunsForQuarter(company, CalendarUtils.getFirstDayOfQuarter(2011, 4)).size();
                            companyReport.append("; Q4 Payrolls: ").append(q4PayrollsCount);

                            if (q4PayrollsCount > 0){
                                int mmtCount = 0;
                                for (MoneyMovementTransaction moneyMovementTransaction : Application.find(MoneyMovementTransaction.class,
                                        MoneyMovementTransaction.Company().equalTo(company)
                                                .And(MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().in(problemTemplateCodes.toArray(new String[5])))
                                                .And(MoneyMovementTransaction.MoneyMovementPaymentMethod().notIn(PaymentMethod.HPDE, PaymentMethod.HPDERefund)))) {
                                    mmtCount++;
                                    if (mmtCount == 1) { //i.e. first
                                        companyReport.append("; bad MMTs: ");
                                    }
                                    companyReport.append(moneyMovementTransaction.getInitiationDate().toString()).append(" ");
                                }

                                if (mmtCount > 0) {
                                    logger.info(companyReport.toString() + "\n");
                                }
                            }

                            companyReport.append("\n");
                        }


                        } catch (Throwable t) {
                            t.printStackTrace();
                            logger.error("error processing " + cMethodId,  t);
                        } finally {
                            PayrollServices.rollbackUnitOfWork();
                        }



                        return companyReport;
                    }
                });
            }

            StringBuilder totalReport = new StringBuilder();

            int total = 0;
            //noinspection UnusedDeclaration
            for (SpcfUniqueId cMethodId : aCHCreditCompanyPaymentMethods) {
                Future<StringBuilder> f = completionService.take();
                StringBuilder companyReport = f.get();
                total++;
                totalReport.append(companyReport);
                if (total % 1000 == 0) {
                    logger.info("Completed processing " + total + " of " + aCHCreditCompanyPaymentMethods.size() + " companies");
                    //logger.info(totalReport);
                }
            }

            logger.info("\n\n\n\n\n\n\n******************************\nCompleted processing " + total + " of " + aCHCreditCompanyPaymentMethods.size() + " companies");
            logger.info(totalReport.toString());

        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            ThreadingUtils.shutdownAndAwaitTermination(executor, 10, 300);
        }



    }

    private static List<SpcfUniqueId> getAllACHCreditCompanyPaymentMethods() {
        String hql = "select cMethod.Id \n" +
                " from com.intuit.sbd.payroll.psp.domain.CompanyPaymentTemplatePaymentMethod cMethod\n" +
                " where cMethod.PaymentMethod = 'ACHCredit'\n" +
                " and cMethod.CompanyAgencyPaymentTemplate.PaymentTemplate.SupportStartDate is not null";

        org.hibernate.Query hibernateQuery = Application.createHibernateQuery(hql);

        //noinspection unchecked
        return hibernateQuery.list();
    }

    private static PreparedStatement getACIStatement() throws Throwable {
        if(aciPreparedStatement.get() == null) {
            aciPreparedStatement.set(conn.prepareStatement("select ACI_VALUE from TXACI where ACI_EIN = ? and ACI_STATE = ? AND ACI_CODE = 'REGACHW' order by aci_effective_date desc"));
        }
        return aciPreparedStatement.get();
    }

    private static PreparedStatement getPItemStatement() throws Throwable {
        if(pItemPreparedStatement.get() == null) {
            pItemPreparedStatement.set(conn.prepareStatement("select PIT_COMPID from IQPITEM where PIT_USERID = ? and PIT_PITEMID = ?"));
        }
        return pItemPreparedStatement.get();
    }
}

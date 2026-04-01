package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.interceptor.manager.DomainEntityChangeManager;
import com.intuit.sbd.payroll.psp.interceptor.model.DomainEntityChangeModel;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.HqlBuilder;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Entity;
import java.text.Normalizer;

/**
 * Hand-written business logic
 */
@Entity // Annotate the class with @Entity for compile time BytecodeEnhancement for attribute lazy loading
public class EntitlementMessage extends BaseEntitlementMessage {


    public static final long PROCESS_WITH_NEXT_BATCH_TOKEN = -1L;

    /**
     * Default constructor.
     */
    public EntitlementMessage()
    {
        super();
    }

    public static EntitlementMessage findEntitlementMessagesByIdAndLicenseNumber(String pEntitlementMessageId, String licenseNumber) {
        DomainEntitySet<EntitlementMessage> entitlementMessages = Application.find(EntitlementMessage.class,
                EntitlementMessage.LicenseNumber().equalTo(licenseNumber)
                        .And(EntitlementMessage.Id().equalTo(
                                SpcfUniqueId.createInstance(pEntitlementMessageId))));
        return entitlementMessages.getFirst();
    }

    public static DomainEntitySet<EntitlementMessage> findNewEntitlementMessages(int pBatchSize, long pBatchToken) {
        if(pBatchSize > 0) {
            return Application.find(EntitlementMessage.class, new Query<EntitlementMessage>()
                    .Where(EntitlementMessage.Status().equalTo(EntitlementMessageStatusCode.New)
                        .And(EntitlementMessage.Token().equalTo(pBatchToken).Or(EntitlementMessage.Token().equalTo(PROCESS_WITH_NEXT_BATCH_TOKEN))))
                    .OrderBy(EntitlementMessage.CreatedDate())
                    .LimitResults(0, pBatchSize));
        } else {
            return Application.find(EntitlementMessage.class, new Query<EntitlementMessage>()
                    .Where(EntitlementMessage.Status().equalTo(EntitlementMessageStatusCode.New))
                    .OrderBy(EntitlementMessage.CreatedDate()));
        }
    }

    public static DomainEntitySet<EntitlementMessage> findCompanyEntitlementMessages(SourceSystemCode pSourceSystemCode,
                                                                                     String pSourceCompanyId,
                                                                                     SpcfCalendar pFromDate,
                                                                                     SpcfCalendar pToDate) {
        Company company = Company.findCompany(pSourceCompanyId, pSourceSystemCode);
        String query = " select entitlementMessage" +
                " from  com.intuit.sbd.payroll.psp.domain.EntitlementUnit entitlementUnit," +
                "       com.intuit.sbd.payroll.psp.domain.Entitlement entitlement," +
                "       com.intuit.sbd.payroll.psp.domain.EntitlementMessage entitlementMessage" +
                " where entitlementUnit.Company = :company" +
                "   and entitlementUnit.Entitlement = entitlement" +
                "   and entitlementMessage.LicenseNumber = entitlement.LicenseNumber" +
                "   and entitlementMessage.EntitlementOfferingCode = entitlement.EntitlementOfferingCode";
        if(pFromDate != null && pToDate != null) {
            query += "   and entitlementMessage.CreatedDate between :fromDate and :toDate";
        } else if(pFromDate != null) {
            query += "   and entitlementMessage.CreatedDate > :fromDate";
        } else  if(pToDate != null) {
            query += "   and entitlementMessage.CreatedDate < :toDate";
        }

         query += " ORDER BY entitlementMessage.LicenseNumber, entitlementMessage.Id";

        org.hibernate.Query hibernateQuery = Application.createHibernateQuery(query);
        hibernateQuery.setParameter("company", company);
        if(pFromDate != null) {
            hibernateQuery.setParameter("fromDate", pFromDate);
        }
        if(pToDate != null) {
            hibernateQuery.setParameter("toDate", pToDate);
        }

        DomainEntitySet<EntitlementMessage> entitlementMessages = new DomainEntitySet<EntitlementMessage>();
        entitlementMessages.addAll(hibernateQuery.list());
        return entitlementMessages;
    }

     public static DomainEntitySet<EntitlementMessage> findEntitlementMessages(SpcfCalendar pFromDate,
                                                                               SpcfCalendar pToDate,
                                                                               int pFirstIndex,
                                                                               int pMaxResults) {

         HqlBuilder hql = new HqlBuilder(" select entitlementMessage" +
                 " from  com.intuit.sbd.payroll.psp.domain.EntitlementUnit entitlementUnit," +
                 "       com.intuit.sbd.payroll.psp.domain.Entitlement entitlement," +
                 "       com.intuit.sbd.payroll.psp.domain.EntitlementMessage entitlementMessage" +
                 " where entitlementUnit.Entitlement = entitlement" +
                 "   and entitlementMessage.LicenseNumber = entitlement.LicenseNumber" +
                 "   and entitlementMessage.EntitlementOfferingCode = entitlement.EntitlementOfferingCode");
         if(pFromDate != null && pToDate != null) {
             hql.append("   and entitlementMessage.CreatedDate between :fromDate and :toDate");
             hql.setParameter("fromDate", pFromDate);
             hql.setParameter("toDate", pToDate);
         } else if(pFromDate != null) {
             hql.append("   and entitlementMessage.CreatedDate > :fromDate");
             hql.setParameter("fromDate", pFromDate);
         } else  if(pToDate != null) {
             hql.append("   and entitlementMessage.CreatedDate < :toDate");
             hql.setParameter("toDate", pToDate);
         }
         hql.append(" ORDER BY entitlementMessage.LicenseNumber, entitlementMessage.Id");

         return hql.find(pFirstIndex, pMaxResults);
     }

    public static DomainEntitySet<EntitlementMessage> findSkippedEntitlementMessages(String pLicenseNumber, String pEOC) {
        return Application.find(EntitlementMessage.class, new Query<EntitlementMessage>()
                .Where(EntitlementMessage.LicenseNumber().equalTo(pLicenseNumber)
                        .And(EntitlementMessage.EntitlementOfferingCode().equalTo(pEOC))
                        .And(EntitlementMessage.Status().in(EntitlementMessageStatusCode.SkippedOldTimestamp, EntitlementMessageStatusCode.SkippedEntitlementNotFound))));
    }

    public static DomainEntitySet<EntitlementMessage> findEntitlementMessages(String pLicenseNumber, String pOrderNumber) {
        Criterion<EntitlementMessage> where =
                EntitlementMessage.LicenseNumber().equalTo(pLicenseNumber)
                        .And(EntitlementMessage.OrderNumber().equalTo(pOrderNumber));

        return Application.find(EntitlementMessage.class, where);
    }

    @Override
    public String getMessage()
    {
        DomainEntityChangeModel existingDomainEntityChangeModel = DomainEntityChangeManager.getDomainEntityChangeModelContext();
        try{
            DomainEntityChangeManager.setDomainEntityChangeModelContext(this.getClass(), this);
            return cleanTextContent(super.getMessage());
        } finally {
            DomainEntityChangeManager.setDomainEntityChangeModelContext(existingDomainEntityChangeModel);
        }
    }
    @Override
    public void setMessage( String  pMessage)
    {
        super.setMessage(cleanTextContent(pMessage));

    }

    private String cleanTextContent(String text)
    {
        if(StringUtils.isEmpty(text)) {
            return text;
        }
        else {
            text = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
            return text.trim();
        }
    }

}
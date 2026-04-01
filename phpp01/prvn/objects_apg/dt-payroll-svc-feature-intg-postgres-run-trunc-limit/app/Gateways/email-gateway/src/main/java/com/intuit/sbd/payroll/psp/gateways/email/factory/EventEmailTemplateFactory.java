package com.intuit.sbd.payroll.psp.gateways.email.factory;

import com.intuit.sbd.payroll.psp.gateways.email.factory.product.EventEmailTemplate;
import com.intuit.sbd.payroll.psp.gateways.email.factory.product.EventEmail;
import com.intuit.sbd.payroll.psp.gateways.email.util.EventStatus;
import com.intuit.sbd.payroll.psp.domain.CompanyEvent;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyEventEmail;
import com.intuit.sbd.payroll.psp.domain.EventEmailTemplateTypeCode;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.Application;

import java.util.List;
import java.util.Map;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jul 26, 2008
 * Time: 1:22:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class EventEmailTemplateFactory {
    static Collection<EventEmailTemplate> createTemplatesForEvents(List<EventStatus> pEvents) {
        // all events passed into this method are from the same payroll run.

        List<EventEmail> emailList = EventEmailFactory.createEventEmailForEvents(pEvents);
        Map<EventEmailTemplateTypeCode, EventEmailTemplate> templateList =
                new Hashtable<EventEmailTemplateTypeCode, EventEmailTemplate>();

        for (EventEmail email : emailList) {
            EventEmailTemplate template = templateList.get(email.getTemplateId());

            if (template != null) {
                template.addEmailRecipient(email);
            } else {
                template = new EventEmailTemplate(email.getTemplateId());
                template.addEmailRecipient(email);
                templateList.put(template.getTemplateId(), template);
            }
        }

        return templateList.values();
    }

    public static Collection<CompanyEventEmailManager> buildCompanyEmailManagers(DomainEntitySet<CompanyEventEmail> pEventList) {
        // generate a map of companies that we will be sending email for
        Map<String, CompanyEventEmailManager> managerMap = new Hashtable<String, CompanyEventEmailManager>();

        for (CompanyEventEmail event : pEventList) {
            CompanyEvent companyEvent = event.getCompanyEvent();
            Company company = companyEvent.getCompany();
            String key = company.getSourceSystemCd().toString() + ":" + company.getSourceCompanyId();

            CompanyEventEmailManager manager = managerMap.get(key);

            if (manager != null) {
                manager.addEvent(event);
            } else {
                manager = new CompanyEventEmailManager(company);
                manager.addEvent(event);
                managerMap.put(key, manager);
            }

            // keep the cache clear of noise
            Application.evict(company);
        }

        return managerMap.values();
    }

    public static List<EventEmailTemplate> buildMasterTemplateList(Collection<CompanyEventEmailManager> pManagers) {
        // merge the like templates from all companies into one master list of templates
        // (each template will only appear in the list once, all like templates will be merged)
        List<EventEmailTemplate> masterTemplateList = new Vector<EventEmailTemplate>();

        // create the master template list with empty templates
        for (EventEmailTemplateTypeCode id : EventEmailTemplateTypeCode.values()) {
            masterTemplateList.add(new EventEmailTemplate(id));
        }

        // iterate over all the company managers and merge their templates into the master list
        for (CompanyEventEmailManager manager : pManagers) {
            manager.createEmailTemplates();
            manager.mergeTemplatesIntoMaster(masterTemplateList);
        }

        return masterTemplateList;
    }
}

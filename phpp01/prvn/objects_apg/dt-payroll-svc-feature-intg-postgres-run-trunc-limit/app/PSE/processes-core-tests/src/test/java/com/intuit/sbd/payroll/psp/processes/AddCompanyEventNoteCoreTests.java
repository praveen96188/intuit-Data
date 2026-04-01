package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.AddCompanyDataLoader;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * User: rsakhamuri
 * Date: Sep 17, 2008
 * Time: 10:15:23 AM
 */
public class AddCompanyEventNoteCoreTests {
     @Before
    public void runBeforeEachTest() {
        AddCompanyDataLoader.beforeEachTest();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testCompanyNotes() {
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        AddCompanyDataLoader.dataloader.persistTestCompanyService(result.getResult());
        PayrollServices.commitUnitOfWork();

        assertEquals(0, result.getMessages().size());

        // Add a note to the company
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.addCompanyNote(company1.getSourceSystemCd(), company1.getCompanyId(), null, "10000112518", "My test notes.", false));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Application.findById(Company.class, result.getResult().getId());

        assertTrue("Number of notes", company.getCompanyNoteCollection().size() == 1);
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.ManualNoteEvent, null, null, null);
        assertTrue("Number of events", companyEvents.size() == 1);
        CompanyEvent companyEvent = companyEvents.get(0);
        DomainEntitySet<CompanyNote> companyNotesSet = companyEvent.getCompanyNoteCollection();
        assertEquals("Number of event notes", 1, companyNotesSet.size());
        Iterator<CompanyNote> notesItr = companyNotesSet.iterator();

        CompanyNote companyNote= notesItr.next();
        assertEquals("Insert User Id", "10000112518", companyNote.getInsertUserId());
        assertEquals("Notes", "My test notes.", companyNote.getNotes());
        assertEquals("Company Event", companyEvent, companyNote.getCompanyEvent());
        assertFalse(companyNote.getAlert());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testCompanyEventNotes() {
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        company1.setSourceSystemCd(SourceSystemCode.QBDT);
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        PayrollServices.commitUnitOfWork();
        DataLoadServices.addDDService(result.getResult());

        assertEquals(0, result.getMessages().size());

        PayrollServices.beginUnitOfWork();
        Company company = Application.findById(Company.class, result.getResult().getId());

        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company);
        assertEquals("Number of events", 4, companyEvents.size());
        CompanyEvent companyEvent = companyEvents.find(CompanyEvent.EventTypeCd().equalTo(EventTypeCode.CustomerSignedUp)).get(0);
        DomainEntitySet<CompanyNote> companyNotesSet = companyEvent.getCompanyNoteCollection();
        assertTrue("Number of event notes", companyNotesSet.size() == 0);
        PayrollServices.commitUnitOfWork();

        // Add a note to the company event
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.addCompanyNote(company.getSourceSystemCd(), company.getSourceCompanyId(), companyEvent.getId().toString(), "10000112518", "My test notes.", true));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        companyEvent = Application.findById(CompanyEvent.class, companyEvent.getId());
        companyNotesSet = companyEvent.getCompanyNoteCollection();
        assertTrue("Number of event notes", companyNotesSet.size() == 1);
        
        Iterator<CompanyNote> notesItr = companyNotesSet.iterator();

        CompanyNote companyNote= notesItr.next();
        assertEquals("Insert User Id", "10000112518", companyNote.getInsertUserId());
        assertEquals("Notes", "My test notes.", companyNote.getNotes());
        assertEquals("Company Event", companyEvent, companyNote.getCompanyEvent());
        assertTrue(companyNote.getAlert());
        PayrollServices.commitUnitOfWork();

        // Add another note to the company event
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 10, SpcfTimeZone.getLocalTimeZone()));
        assertSuccess(PayrollServices.companyManager.addCompanyNote(company.getSourceSystemCd(), company.getSourceCompanyId(), companyEvent.getId().toString(), "10000112518", "My test notes.", false));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        companyEvent = Application.findById(CompanyEvent.class, companyEvent.getId());
        companyNotesSet = companyEvent.getCompanyNoteCollection();
        assertTrue("Number of event notes", companyNotesSet.size() == 2);
        assertEquals("Note Last Updated Date", companyEvent.getNoteLastUpdatedDate().toLocal().getYear(), 2007);
        assertEquals("Note Last Updated Date", companyEvent.getNoteLastUpdatedDate().toLocal().getMonth(), 9);
        assertEquals("Note Last Updated Date", companyEvent.getNoteLastUpdatedDate().toLocal().getDay(), 10);
        assertEquals("Note Last Updated Date", companyEvent.getNoteLastUpdatedDate().toLocal().getHour(), PSPDate.getPSPTime().getHour());
        notesItr = companyNotesSet.iterator();

        companyNote= notesItr.next();
        assertEquals("Insert User Id", "10000112518", companyNote.getInsertUserId());
        assertEquals("Notes", "My test notes.", companyNote.getNotes());
        assertEquals("Company Event", companyEvent, companyNote.getCompanyEvent());
        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testCompanyNotes_WithMorethan4000Chars() {
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        AddCompanyDataLoader.dataloader.persistTestCompanyService(result.getResult());
        PayrollServices.commitUnitOfWork();

        assertEquals(0, result.getMessages().size());

        // Add a note to the company
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.addCompanyNote(company1.getSourceSystemCd(), company1.getCompanyId(), null, "10000112518", "When a company note length is over 4000 characters it's not being truncated before persisting it to the database, causing an error.AddCompanyNoteCore should be modified to truncate notes that are longer than 4000 characters.When a company note length is over 4000 characters it's not being truncated before persisting it to the database, causing an error.AddCompanyNoteCore should be modified to truncate notes that are longer than 4000 characters.When a company note length is over 4000 characters it's not being truncated before persisting it to the database, causing an error.AddCompanyNoteCore should be modified to truncate notes that are longer than 4000 characters.When a company note length is over 4000 characters it's not being truncated before persisting it to the database, causing an error.AddCompanyNoteCore should be modified to truncate notes that are longer than 4000 characters.When a company note length is over 4000 characters it's not being truncated before persisting it to the database, causing an error.AddCompanyNoteCore should be modified to truncate notes that are longer than 4000 characters.When a company note length is over 4000 characters it's not being truncated before persisting it to the database, causing an error.AddCompanyNoteCore should be modified to truncate notes that are longer than 4000 characters.When a company note length is over 4000 characters it's not being truncated before persisting it to the database, causing an error.AddCompanyNoteCore should be modified to truncate notes that are longer than 4000 characters.When a company note length is over 4000 characters it's not being truncated before persisting it to the database, causing an error.AddCompanyNoteCore should be modified to truncate notes that are longer than 4000 characters.When a company note length is over 4000 characters it's not being truncated before persisting it to the database, causing an error.AddCompanyNoteCore should be modified to truncate notes that are longer than 4000 characters.When a company note length is over 4000 characters it's not being truncated before persisting it to the database, causing an error.AddCompanyNoteCore should be modified to truncate notes that are longer than 4000 characters.When a company note length is over 4000 characters it's not being truncated before persisting it to the database, causing an error.AddCompanyNoteCore should be modified to truncate notes that are longer than 4000 characters.When a company note length is over 4000 characters it's not being truncated before persisting it to the database, causing an error.AddCompanyNoteCore should be modified to truncate notes that are longer than 4000 characters.When a company note length is over 4000 characters it's not being truncated before persisting it to the database, causing an error.AddCompanyNoteCore should be modified to truncate notes that are longer than 4000 characters.When a company note length is over 4000 characters it's not being truncated before persisting it to the database, causing an error.AddCompanyNoteCore should be modified to truncate notes that are longer than 4000 characters.When a company note length is over 4000 characters it's not being truncated before persisting it to the database, causing an error.AddCompanyNoteCore should be modified to truncate notes that are longer than 4000 characters.vWhen a company note length is over 4000 characters it's not being truncated before persisting it to the database, causing an error.AddCompanyNoteCore should be modified to truncate notes that are longer than 4000 characters.vWhen a company note length is over 4000 characters it's not being truncated before persisting it to the database, causing an error.AddCompanyNoteCore should be modified to truncate notes that are longer than 4000 characters.When a company note length is over 4000 characters it's not being truncated before persisting it to the database, causing an error.AddCompanyNoteCore should be modified to truncate notes 1234", false));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Application.findById(Company.class, result.getResult().getId());

        assertTrue("Number of notes", company.getCompanyNoteCollection().size() == 1);
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.ManualNoteEvent, null, null, null);
        assertTrue("Number of events", companyEvents.size() == 1);
        CompanyEvent companyEvent = companyEvents.get(0);
        DomainEntitySet<CompanyNote> companyNotesSet = companyEvent.getCompanyNoteCollection();
        assertEquals("Number of event notes", 1, companyNotesSet.size());
        Iterator<CompanyNote> notesItr = companyNotesSet.iterator();

        CompanyNote companyNote = notesItr.next();
        assertEquals("Insert User Id", "10000112518", companyNote.getInsertUserId());
        assertEquals("Notes", "When a company note length is over 4000 characters it's not being truncated before persisting it to the database, causing an error.AddCompanyNoteCore should be modified to truncate notes that are longer than 4000 characters.When a company note length is over 4000 characters it's not being truncated before persisting it to the database, causing an error.AddCompanyNoteCore should be modified to truncate notes that are longer than 4000 characters.When a company note length is over 4000 characters it's not being truncated before persisting it to the database, causing an error.AddCompanyNoteCore should be modified to truncate notes that are longer than 4000 characters.When a company note length is over 4000 characters it's not being truncated before persisting it to the database, causing an error.AddCompanyNoteCore should be modified to truncate notes that are longer than 4000 characters.When a company note length is over 4000 characters it's not being truncated before persisting it to the database, causing an error.AddCompanyNoteCore should be modified to truncate notes that are longer than 4000 characters.When a company note length is over 4000 characters it's not being truncated before persisting it to the database, causing an error.AddCompanyNoteCore should be modified to truncate notes that are longer than 4000 characters.When a company note length is over 4000 characters it's not being truncated before persisting it to the database, causing an error.AddCompanyNoteCore should be modified to truncate notes that are longer than 4000 characters.When a company note length is over 4000 characters it's not being truncated before persisting it to the database, causing an error.AddCompanyNoteCore should be modified to truncate notes that are longer than 4000 characters.When a company note length is over 4000 characters it's not being truncated before persisting it to the database, causing an error.AddCompanyNoteCore should be modified to truncate notes that are longer than 4000 characters.When a company note length is over 4000 characters it's not being truncated before persisting it to the database, causing an error.AddCompanyNoteCore should be modified to truncate notes that are longer than 4000 characters.When a company note length is over 4000 characters it's not being truncated before persisting it to the database, causing an error.AddCompanyNoteCore should be modified to truncate notes that are longer than 4000 characters.When a company note length is over 4000 characters it's not being truncated before persisting it to the database, causing an error.AddCompanyNoteCore should be modified to truncate notes that are longer than 4000 characters.When a company note length is over 4000 characters it's not being truncated before persisting it to the database, causing an error.AddCompanyNoteCore should be modified to truncate notes that are longer than 4000 characters.When a company note length is over 4000 characters it's not being truncated before persisting it to the database, causing an error.AddCompanyNoteCore should be modified to truncate notes that are longer than 4000 characters.When a company note length is over 4000 characters it's not being truncated before persisting it to the database, causing an error.AddCompanyNoteCore should be modified to truncate notes that are longer than 4000 characters.vWhen a company note length is over 4000 characters it's not being truncated before persisting it to the database, causing an error.AddCompanyNoteCore should be modified to truncate notes that are longer than 4000 characters.vWhen a company note length is over 4000 characters it's not being truncated before persisting it to the database, causing an error.AddCompanyNoteCore should be modified to truncate notes that are longer than 4000 characters.When a company note length is over 4000 characters it's not being truncated before persisting it to the database, causing an error.AddCompanyNoteCore should be modified to truncate notes 123", companyNote.getNotes());
        assertEquals("Company Event", companyEvent, companyNote.getCompanyEvent());
        PayrollServices.commitUnitOfWork();
    }    

}

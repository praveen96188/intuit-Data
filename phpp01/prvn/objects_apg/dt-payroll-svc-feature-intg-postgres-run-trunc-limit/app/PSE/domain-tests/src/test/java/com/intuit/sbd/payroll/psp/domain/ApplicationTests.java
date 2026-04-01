package com.intuit.sbd.payroll.psp.domain;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import junit.framework.Assert;

/**
 * Created by IntelliJ IDEA.
 * User: achaves
 * Date: Jun 27, 2008
 * Time: 10:35:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class ApplicationTests {
    @Before
    public void runBeforeTest () {
        PayrollServicesTest.beforeEachTest();
    }

    @After
    public void runAfterEachTest () {
        PayrollServicesTest.afterEachTest();
    }


    @Test
    public void ModifyDataObjectTest() {
        Application.beginUnitOfWork();
        DomainEntitySet<OffloadGroup> offloadGroups = Application.find(OffloadGroup.class);
        OffloadGroup defaultOffloadGroup = offloadGroups.get(0);

        String description = defaultOffloadGroup.getDescription();
        String id = defaultOffloadGroup.getOffloadGroupCd();

        defaultOffloadGroup.setDescription(description + "Test");
        Application.save(defaultOffloadGroup);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        OffloadGroup modifiedOffloadGroup = OffloadGroup.findOffloadGroup(id);
        Assert.assertEquals(modifiedOffloadGroup.getDescription(), description + "Test");

        modifiedOffloadGroup.setDescription(description);
        Application.save(modifiedOffloadGroup);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        modifiedOffloadGroup = OffloadGroup.findOffloadGroup(id);
        Assert.assertEquals(modifiedOffloadGroup.getDescription(), description);

        modifiedOffloadGroup.setDescription(description + "Test");
        Application.save(modifiedOffloadGroup);
        Application.commitUnitOfWork();
    }
}

package com.intuit.sbd.payroll.psp;

import com.intuit.sbd.payroll.psp.bedl.BedlProcessor;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Nov 12, 2010
 * Time: 9:25:21 AM
 * To change this template use File | Settings | File Templates.
 */
public class HbmForHQLGenerator {
    public void generateCode(BedlProcessor pBedlProcessor) throws IOException {
        String generatedHbmFolder = pBedlProcessor.getBedlFolderName() + "../../../target/test/";

        File hbmFolder = new File(generatedHbmFolder);
        hbmFolder.mkdirs();

        String hbmFullFileName = generatedHbmFolder + "hibernate.cfg.xml";
        //deleting existing file if any
        File hbmFile = new File(hbmFullFileName);
        hbmFile.delete();


        StringTemplateGroup templates = new StringTemplateGroup("HbmForHQL", Generator.getTemplateLocation() + "domainEntity");
        StringTemplate t = templates.getInstanceOf("HbmForHQL");

        t.setAttribute("bedlDataEntitys", pBedlProcessor.getDataEntities());
        t.setAttribute("bedlDataObjects", pBedlProcessor.getDataObjects());
        BufferedWriter outputFile = new BufferedWriter(new FileWriter(hbmFullFileName));
        outputFile.write(t.toString());
        outputFile.close();

    }
}
